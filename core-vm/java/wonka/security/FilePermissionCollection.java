/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package wonka.security;

import java.io.FilePermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Vector;

/**
 * The class FilePermissionCollection is designed to optimize performance
 * of the `implies' operation, which must determine whether a candidate 
 * FilePermission is implied by any permission in the collection.
 * This we do using a `tree' data structure, in which the `leaves' are
 * Integer's holding a bitmask of permissions: e.g. the four FilePremissions 
 * \texttt{\{"/foo/bar/baz","read,write"\}, 
 * \{"/foo/bar/quux","read,execute"\}, \{"/foo/*","read"\}}, and 
 * \textt{\{"/something/else","read"\}} would result in the following 
 * tree:
 *
 *                 +---> bar = READ|WRITE
 *                 | 
 *  -+---> foo/bar +---> baz = READ|EXECUTE
 *   |             |
 *   |             +---> * =   READ
 *   |
 *   +---> something +---> else = READ
 *
 * Each node in the tree implies a `/' (or rather a SEP_CHAR),
 * and a leading or trailing `/' is not represented.  The leading `/'
 * is not needed because we assume all paths are absolute (if relative
 * paths were supported, they would be converted in the constructor of
 * FilePermission).  The trailing `/' is not needed because 
 * `some-directory/' and `some-directory' are synonymous.
 *
 * Each level of the tree is a hashtable in which they keys are directory
 * or file names. The data associated with each key is either a FilePermission
 * (leaf node) or another hashtable which holds the possible sequels, and 
 * so on recursively. 
 *
 * Note: this is essentially the same algorithm as that used in
 * BasicPermissionsCollection, except that we always branch at a '/',
 * and therefore the '/' can be made implicit.
 *
 * TODO: replace the leaf elements by instances of Integer, holding a 4-bit
 * mask to represent the permissions.  This will make implies() faster, but
 * means that e.g. elements() will need to reconstruct the FilePermissions
 * on-the-fly. --> elements are stored in a HashSet. this has the advantage
 * that if a permission is added which is equal to a previously added that we don't
 * have to add it in the hashTable structure
 */
public class FilePermissionCollection extends PermissionCollection {

  private final static String DASH = "-";
  private final static String STAR = "*";

  private final static int EXECUTE = 1;
  private final static int WRITE   = 2;
  private final static int READ    = 4;
  private final static int DELETE  = 8;

  private static final int INITIAL_TABSIZE=11;

  private static final String SEP = System.getProperty("file.separator");
  private static final char   SEP_CHAR = SEP.charAt(0);
  private static final String SEP_DASH = SEP + DASH;
  private static final String SEP_STAR = SEP + STAR;

  private Hashtable prefixes;
  private HashSet elements;

  /**
  ** this value is used to contain the actions implied by the FilePermission "<<ALL FILES>>"
  ** this is added so file names like '<<ALL FILES>>/dir' don't cause strange things ...
  */
  private int allMask=0;

  /**
   ** Constructor FilePermissionCollection() builds an empty Hashtable and an empty HashSet.
   */
  public FilePermissionCollection() {
    prefixes = new Hashtable(INITIAL_TABSIZE);
    elements = new HashSet();
  }

  /**
   ** Method actions2bitmask converts an ``actions'' string into a bitmask.
   ** (note: the action string should be one retrieved from getActions();
   */
  private static int actions2bitmask(String actions) {
    int result = 0;
    String remainder = actions; //.toLowerCase();--> not needed (see note)
    while (remainder != null) {
      int comma = remainder.indexOf(',');
      String action;
      if (comma < 0) {
        action = remainder;
        remainder = null;
      }
      else {
        action = remainder.substring(0,comma);
        remainder = remainder.substring(comma+1);
      }
    // we use a string retrieved from getActions --> each actions is only mentioned once,
    // they are comma seperated, and no whitespace added ...
      if (action.equals("execute")) {
        // System.out.println("  Recognised action '"+action+"'");
        result |= EXECUTE;
      }
      else if (action.equals("write")) {
        // System.out.println("  Recognised action '"+action+"'");
        result |= WRITE;
      }
      else if (action.equals("read")) {
        // System.out.println("  Recognised action '"+action+"'");
        result |= READ;
      }
      else if (action.equals("delete")) {
        // System.out.println("  Recognised action '"+action+"'");
        result |= DELETE;
      }
      else {
        //this case sould not occur ...
        // System.out.println("  Ignoring unknown or redundant action '"+action+"'");
      }
    }

    return result;
  }

  /** Method add_action(FilePermission,int) rebuilds the prefixes
   *  to take account of a new FilePermission.  The int denotes
   *  the actions, i.e. one of READ, WRITE, EXECUTE, DELETE.
   *
   * We try to match the name of the new permission against each key
   * of the root hashtable ('prefixes') in turn, looking for the longest
   * prefix common to both the name and the key; the longest such prefix
   * then determines what should happen next:
   * - if its length is zero, then in fact we have a new prefix.  We add
   *   an entry to the (root) hashtable with the new name as key and
   *   the action as data.
   *   Example in the illustration: something/else
   * - if the longest prefix is shorter than the key in which it is found,
   *   then we need to split a key.  The current key is removed from the
   *   hashtable and replaced by an entry with the shorter prefix as key
   *   and as data a new hashtable containing one entry, namely a mapping
   *   from the remainder of the existing key to the data that was referenced
   *   by the old key.  We then resume the algorithm with the new hashtable
   *   and the remainder of the name.
   *   Example: hashtable contains
   *      +---> foo/bar/baz
   *   and we wish to add 'foo/bar/quux'.  We replace the existing entry
   *   'foo.bar.baz' by a link to a new hashtable:
   *     +---> foo/bar ---> baz
   *   and then add 'quux' to the new hashtable.
   * - else the longest prefix exactly matches some key: we follow the
   *   link from this key to its associated Hashtable and resume the
   *   algorithm with the new hashtable and the remainder of the name.
   *
   *   note : acunia/dir has no match acuniaCompany/dir
   */
  private void add_action (FilePermission newperm, int action) throws SecurityException {
    // System.out.println("Prefix table before adding permission '"+newperm+"': "+prefixes);
    Hashtable table = prefixes;
    String name = newperm.getName();
    String longest_prefix = "";
    String target_matched = "";
    int longest_prefix_length = 0;
    int namelen = name.length();
    boolean wild = (name.endsWith(SEP_STAR) || name.endsWith(SEP_DASH));
    String wildstring=null;
    if (wild) {
     	 namelen -= 2;
     	 wildstring = name.substring(namelen+1);
     	 name = name.substring(0,namelen);	
    }
    if (name.charAt(namelen-1) == SEP_CHAR) {
      namelen -= 1;
      name = name.substring(0,namelen);
      // System.out.println("Name ends in '" + SEP + "', use just '"+name+"'.");
    }

    while (namelen >= 0) {
      if (namelen > 0 && name.charAt(0) == SEP_CHAR) {
        name = name.substring(1);
        --namelen;
      }
      // System.out.println("Looking for prefix '"+name+"' in "+table);
      Enumeration e = table.keys();
      while (e.hasMoreElements()) {
        String prefix = name;
        int prefixlen = namelen;
        String target = (String)e.nextElement();
        // System.out.println("  Trying '"+target+"'");
        while (prefixlen>longest_prefix_length) {
          if (target.startsWith(prefix)) {
            longest_prefix = prefix;
            target_matched = target;
            longest_prefix_length = prefixlen;
          }
          int i = prefix.lastIndexOf(SEP_CHAR);
          if (i<0) {
            prefix = "";
            prefixlen = 0;
          }
          else {
            prefix = prefix.substring(0,i);
            prefixlen = i;
          }
        }
      }

      if (longest_prefix_length == 0) {
        if (wild) {
          // System.out.println("  No match found, adding new entry: '"+name+" --> add wild card");
          Hashtable new_table = new Hashtable(INITIAL_TABSIZE);
          table.put(name,new_table);
          table = new_table;	
          name = wildstring;
          namelen = 1;
          wild = false;
        }
        else {
          Integer new_actions = new Integer(action);
          // System.out.println("  No match found, adding new entry: '"+name+"'->"+new_actions);
          table.put(name,new_actions);
          name = "";
          namelen = -1;
        }

      }
      else {
        Object old_data = table.get(target_matched);
        if (longest_prefix_length < target_matched.length()) {
          // System.out.println("  Partial match found, splitting entry for '"+target_matched+"' after '"+longest_prefix+"' ("+longest_prefix_length+" chars)");
          Hashtable new_table = new Hashtable(INITIAL_TABSIZE);
          new_table.put(target_matched.substring(longest_prefix_length+1),old_data);
          // System.out.println("  New hashtable: "+new_table);
          table.remove(target_matched);
          table.put(longest_prefix,new_table);
          // System.out.println("  Old hashtable: "+table);
          table = new_table;
        }
        else if (old_data instanceof Integer) {
          // System.out.println("  Total match found, merging with entry for '"+target_matched+"'");
          // first case "" (and not wild)
            if (!wild && namelen == longest_prefix_length) {
          	Integer old_actions = (Integer)old_data;
          	if ((old_actions.intValue() & action) == 0) {
            		table.put(target_matched,new Integer(old_actions.intValue() | action));
          	}
           }
           else {
          	Hashtable new_table = new Hashtable(INITIAL_TABSIZE);           	
       		table.put(target_matched,new_table);
       		table = new_table;
       		table.put("",old_data);
           }
         }
         else {
          // System.out.println("  Total match found for '"+longest_prefix+"'");
          table = (Hashtable)old_data;
          // System.out.println("  --> switch to hashtable: "+table);
         }
         if (wild && namelen == longest_prefix_length) {
        	name =wildstring;
        	namelen = 1;
        	wild =false;
         }
         else {
         	name = name.substring(longest_prefix_length);
	        namelen = name.length();
	 }
         longest_prefix = "";
         longest_prefix_length = 0;
         // System.out.println("  Remainder of name is '"+name+"'");
      }
    }
    // System.out.println("Prefix table after adding permission '"+newperm+"': "+prefixes);
  }

  /**
   * Method add(Permission) rebuilds the Hashtables to take account
   * of the new Permission (which must be a FilePermission).
   */
  public synchronized void add (Permission permission) throws SecurityException {
    if (super.isReadOnly()) throw new SecurityException("read-only");

    FilePermission newperm;
    try {
      newperm = (FilePermission) permission;
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException("not a FilePermission");
    }
    if(elements.add(newperm)) {
    	if (newperm.getName().equals("<<ALL FILES>>")) { //special case ...
    		allMask |= actions2bitmask(newperm.getActions());
    	}
    	else {
    		add_action(newperm,actions2bitmask(newperm.getActions()));
    	}
    }
   // else {System.out.println("permission not added --> already an equal object in the collection");  }
  }

//  private int clearBits(int actions , int mask) {
//   	return (actions & (~mask));



  /**
   * Method implies_action tests whether a given action is implied by any
   * of the FilePermissions in this collection.
   *
   * We first test to see whether the whole pathname of the permission is a
   * key of the `prefixes' hashtable, with as value a FilePermission
   * which implies te target action.  If so then the algorithm terminates
   * successfully.  It also terminates successfully if the hashtable contains
   * a key "-", or if the hashtable contains a key "*" and the pathname does
   * `not contain `/', and the associated value is a FilePermission which
   * implies the target action.  Otherwise, we try to match the pathname
   * of the new permission against each key in the hashtable:
   * if any key is a prefix of the pathname then the associated data must be a
   * Hashtable, and we resume the algorithm with the remainder of the name
   * in this hashtable.
   */
  public boolean implies_action (String path, int action) {
    // System.out.println("Testing whether path "+path+" implies action "+action+", allMask="+allMask);

    if ((allMask & action) == action) {  // this test verifies if the special token <<ALL FILES>>
     	
     	return true;		   // has been set for this action ...
    }
    action = (action & (~allMask));

    Object data;
    Hashtable table = prefixes;
    String name = path;
    int namelen = name.length();

    if (namelen > 0 && name.charAt(namelen-1) == SEP_CHAR) {
    //names ending with '/' are directories. But they are handled like files (they are stored without the slash)
       	namelen--;
       	name = name.substring(0,namelen);
    }
      if (namelen > 0 && name.charAt(0) == SEP_CHAR) {
        name = name.substring(1);
        --namelen;
      }

    while (namelen >= 0) {
      // System.out.println("Looking for prefix '"+name+"' in "+table);
      data = table.get(name);
      if (data != null) {
        try {
          Integer found_actions = (Integer)data;
          if ((found_actions.intValue() & action) == action) {
          // System.out.println("  Found an exact match for '"+name+"'->"+data+", success!");

            return true;

          }
          else {
    		action = (action & (~found_actions.intValue()));
          }
        } catch (ClassCastException e) {
          // Not an exact match, fall through
        }
      }

      data = table.get(DASH);
      if (data != null && namelen > 0) {
        try {
          Integer found_actions = (Integer)data;
          if ((found_actions.intValue() & action) == action) {
        // System.out.println("  Found a - ->"+data+", success!");

            return true;

          }
    	  action = (action & (~found_actions.intValue()));
        } catch (ClassCastException e) {
          // System.err.println("Odd: entry for '-' is not terminal when searching for "+path+" in "+this);
        }
      }

      data = table.get(STAR);
      if (data != null && namelen > 0) {
        try {
          Integer found_actions = (Integer)data;
          if ((found_actions.intValue() & action) == action && name.indexOf(SEP_CHAR) < 0) {
        // System.out.println("  Found a * ->"+data+", success!");

            return true;

          }
    	  action = (action & (~found_actions.intValue()));
        } catch (ClassCastException e) {
          // System.err.println("Odd: entry for '*' is not terminal when searching for "+path+" in "+this);
        }
      }

      Enumeration e = table.keys();
      boolean found = false;
      while (e.hasMoreElements()) {
        String target = (String)e.nextElement();
        // System.out.println("  Trying '"+target+"'");
        if (name.startsWith(target)) {
          data = table.get(target);
          if (data instanceof Hashtable) {
            //we should verify if the next char in  the name is a SEP_CHAR
            if (target.length() < namelen && name.charAt(target.length()) != SEP_CHAR) {
		continue;             	
            }
            table = (Hashtable)data;
            // System.out.println("  Matched prefix '"+target+"'->hashtable: "+table);
            name = name.substring((target.length() < namelen ? target.length()+1 : target.length()));
            namelen = name.length();
            // System.out.println("  Remainder of name is '"+name+"'");
            found = true;
            break;
          }
          else {
            // when we reach this point we have a prefix with a mask (this a dir or a file)
            // if name is target+'/' then we are ok !
            if (name.equals(target + SEP)){								
            	int found_actions = ((Integer)data).intValue();
            	if ((found_actions & action)==action){
			return true;	     	
		}
		action = (action & (~found_actions));
            }
            // System.err.println("  Matched prefix '"+target+"', but -> "+data+" (?!)");
          }
        }
      }
      if (!found) {
        break;
      }
    }
    // System.out.println("  No match found, failed.");

    return false;
  }

  /**
   * Method 'implies' tests whether a given permission is implied by any
   * of the FilePermissions in this collection. The permission must of
   * course be a FilePermission. A FilePermission is implied by this
   * collection if none of its actions are not implied by this collection
   * for the given path.  (If you like double negatives, do not fail to
   * raise your hand.)
   */
  public boolean implies (Permission permission) {
    FilePermission tryperm;
    try {
      tryperm = (FilePermission) permission;
    }
    catch (ClassCastException e) {
      // System.out.println(permission+" is not a FilePermission!");

        return false;

    }
    return implies_action(tryperm.getName() , actions2bitmask(tryperm.getActions()));	

/*
    trypath = tryperm.getName();

    return (tryperm.getActions().indexOf("read") < 0    || this.implies_action(trypath,READ))
        && (tryperm.getActions().indexOf("write") < 0   || this.implies_action(trypath,WRITE))
        && (tryperm.getActions().indexOf("execute") < 0 || this.implies_action(trypath,EXECUTE))
        && (tryperm.getActions().indexOf("delete") < 0  || this.implies_action(trypath,DELETE))
        ;
*/
  }

  /**
   * The elements() method.
   * a hashSet only provides an iterator which can throw ConcurrentModificationExceptions.
   * so we make it easy for ourselves and put everything in a vector and call elements() on the vector ...
   */    
  public synchronized Enumeration elements() {
    return new Vector(elements).elements();
  }


}
