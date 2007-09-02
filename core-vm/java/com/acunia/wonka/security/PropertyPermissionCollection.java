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

/**
 * $Id: PropertyPermissionCollection.java,v 1.3 2006/04/18 11:35:27 cvs Exp $
 */

package com.acunia.wonka.security;

import java.util.PropertyPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The class PropertyPermissionCollection is designed to optimize performance
 * of the `implies' operation, which must determine whether a candidate 
 * PropertyPermission is implied by any permission in the collection.
 * This we do using a `trie' data structure for each action: 
 * e.g. the four PropertyPremissions {"foo.bar","read,write"}, 
 * {"foo.baz","read"}, {"foo.qu*","read"}, and {"something.else","read"} 
 * would result in the following tree for "read":
 *
 *                       +---> r
 *                       | 
 *  r+---> foo. +---> ba +---> z
 *  e|          |
 *  a|          +---> qu ---> *
 *  d|
 *   +---> something.else
 *
 * Each level of the tree is a hashtable in which they keys are property
 * names. The data associated with each key is either a PropertyPermission
 * (leaf node) or another hashtable which holds the possible sequels, and 
 * so on recursively. 
 *
 * Note: this is essentially the same algorithm as that used in
 * BasicPermissionsCollection, except that we have two tries
 * (one for each action) instead of just one.
 */
public class PropertyPermissionCollection extends PermissionCollection {

  private static int INITIAL_TABSIZE = 11;

  private Hashtable read_prefixes;
  private Hashtable write_prefixes;

  /**
   ** Constructor PropertyPermissionCollection() builds two empty Hashtables.
   */
  public PropertyPermissionCollection() {
    read_prefixes = new Hashtable(INITIAL_TABSIZE);
    write_prefixes = new Hashtable(INITIAL_TABSIZE);
  }

  /** Method add_to(PropertyPermission,Hashtable) rebuilds a Hashtable
   *  to take account of a new PropertyPermission.
   *
   * If the name of the Permission ends in `*' then we note this 
   * and then try first to add the name without the trailing `*'.
   * We will "remember" the trailing `*' later when the rest of 
   * the name is exhausted.
   *
   * We then try to match the name of the new permission against each key
   * of the root hashtable ('prefixes') in turn, looking for the longest
   * prefix common to both the name and the key; the longest such prefix
   * then determines what should happen next:
   * - if its length is zero, then in fact we have a new prefix.  We add
   *   an entry to the (root) hashtable with the new name as key and
   *   the Permission object as data.
   *   Example in the illustration: something/else
   * - if the longest prefix is shorter than the key in which it is found,
   *   or the key is a leaf rather than a branch node (associated data is
   *   a Permission not a Hashtable), then we need to split a key.  The 
   *   current key is removed from the hashtable and replaced by an entry 
   *   with the shorter prefix as key and as data a new hashtable containing 
   *   one entry, namely a mapping from the remainder of the existing key to 
   *   the data that was referenced by the old key.  We then resume the 
   *   algorithm with the new hashtable and the remainder of the name.
   *   Example: hashtable contains
   *      +---> foo.bar.baz
   *   and we wish to add 'foo.bar.quux'.  We replace the existing entry
   *   'foo.bar.baz' by a link to a new hashtable:
   *     +---> foo.bar. ---> baz
   *   and then add 'quux' to the new hashtable.
   * - else the longest prefix exactly matches some key: we follow the
   *   link from this key to its associated Hashtable and resume the
   *   algorithm with the new hashtable and the remainder of the name.
   */
  private void add_to (PropertyPermission newperm, Hashtable prefixes) throws SecurityException {
    // System.out.println("Prefix table before adding permission '"+newperm+"': "+prefixes);
    Hashtable table = prefixes;
    String name = newperm.getName();
    String longest_prefix = "";
    String target_matched = "";
    int namelen = name.length();
    int longest_prefix_length = 0;

    boolean wild = false;
    if (name.endsWith(".*")) {
      wild  = true;
      namelen -= 1;
      name = name.substring(0,namelen);
      // System.out.println("Name ends in '*', first insert '"+name+"' ...");
    }

    while (namelen > 0) {
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
          prefix = prefix.substring(0,--prefixlen);
        }
      }

      if (longest_prefix_length == 0) {
        // System.out.println("  No match found, adding new entry: '"+name+"'->"+newperm);
	if (wild) {
          Hashtable new_table = new Hashtable(INITIAL_TABSIZE);
	  table.put(name,new_table);
          table = new_table;
			
        }
	else {
          table.put(name,newperm);
	}
        name = "";
        namelen = 0;
      } else {
        Object old_data = table.get(target_matched);
        if (longest_prefix_length < target_matched.length()
            || old_data instanceof Permission) {
          // System.out.println("  Partial match found, splitting entry for '"+target_matched+"' after '"+longest_prefix+"' ("+longest_prefix_length+" chars)");
          Hashtable new_table = new Hashtable(INITIAL_TABSIZE);
          new_table.put(target_matched.substring(longest_prefix_length),old_data);
          // System.out.println("  New hashtable: "+new_table);
          table.remove(target_matched);
          table.put(longest_prefix,new_table);
          // System.out.println("  Old hashtable: "+table);
          table = new_table;
        }
        else {
          // System.out.println("  Total match found for '"+longest_prefix+"'");
          table = (Hashtable)old_data;
          // System.out.println("  --> switch to hashtable: "+table);
        }
        name = name.substring(longest_prefix_length);
        namelen = name.length();
        longest_prefix = "";
        longest_prefix_length = 0;
        // System.out.println("  Remainder of name is '"+name+"'");
      }
      if (wild  && namelen==0) {
        // System.out.println("Now deal with the trailing '*' ...");
        name = "*";
        namelen = 1;
        wild = false;
      }
    }
    // System.out.println("Prefix table after adding permission '"+newperm+"': "+prefixes);
  }

  /**
   * Method add(Permission) rebuilds the Hashtables to take account
   * of the new Permission (which must be a PropertyPermission).
   */
  public synchronized void add (Permission permission) throws SecurityException {
    if (super.isReadOnly()) throw new SecurityException("read-only");

    PropertyPermission newperm;
    try {
      newperm = (PropertyPermission) permission;
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException("not a PropertyPermission");
    }

    if (newperm.getActions().indexOf("read") >= 0) {
      add_to(newperm,read_prefixes);
    }
    if (newperm.getActions().indexOf("write") >= 0) {
      add_to(newperm,write_prefixes);
    }
  }

  /**
   * Method implies_action tests whether a given action is implied by any
   * of the PropertyPermissions in this collection.
   *
   * We first test to see whether the whole pathname of the permission is a
   * key of the action's hashtable ('prefixes'), with a Permission as data.
   * If so then the algorithm terminates successfully.  It also terminates
   * successfully if the hashtable contains a key "-", or if the hashtable 
   * contains a key "*" and the pathname does not contain `/' (the associated 
   * data will always be a Permission in this case).  Otherwise, we try to 
   * match the pathname of the new permission against each key in the hashtable:
   * if any key is a prefix of the pathname then the associated data must be a
   * Hashtable, and we resume the algorithm with the remainder of the name
   * in this hashtable.
   */
  public boolean implies_action (String path, Hashtable prefixes) {
    if (prefixes.get("*") != null) {

      return true;

    }

    Hashtable table = prefixes;
    String name = path;
    int namelen = name.length();

    while (namelen >= 0) {
      // System.out.println("Looking for prefix '"+name+"' in "+table);
      Object data = table.get(name);
      if (data instanceof Permission) {
          // System.out.println("  Found an exact match for '"+name+"'->"+data+", success!");
          return true;
      }
      data = table.get(new String("*"));
      if (data != null) {
        // System.out.println("  Found a * ->"+data+", success!");

        return namelen > 0;

      }
      Enumeration e = table.keys();
      boolean found = false;
      while (e.hasMoreElements()) {
        String target = (String)e.nextElement();
        // System.out.println("  Trying '"+target+"'");
        if (name.startsWith(target)) {
          data = table.get(target);
          if (data instanceof Hashtable) {
            table = (Hashtable)data;
            // System.out.println("  Matched prefix '"+target+"'->hashtable: "+table);
            name = name.substring(target.length());
            namelen = name.length();
            // System.out.println("  Remainder of name is '"+name+"'");
            found = true;
            break;
          }
          else System.err.println("  Matched prefix '"+target+"', but ->Permission (?!)");
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
   * of the PropertyPermissions in this collection. The permission must of
   * course be a PropertyPermission. A PropertyPermission is implied by this
   * collection if none of its actions are not implied by this collection
   * for the given path.  (If you like double negatives, do not fail to
   * raise your hand.)
   */
  public boolean implies (Permission permission) {
    PropertyPermission tryperm;
    String trypath;

    try {
      tryperm = (PropertyPermission) permission;
    }
    catch (ClassCastException e) {
      // System.out.println(permission+" is not a PropertyPermission!");

        return false;

    }

    trypath = tryperm.getName();

    return (tryperm.getActions().indexOf("read") < 0    || this.implies_action(trypath,read_prefixes))
        && (tryperm.getActions().indexOf("write") < 0   || this.implies_action(trypath,write_prefixes))
        ;
  }

  /**
   * Method to enumerate one subtree of a prefix tree.  Recursively, of course.
   * Uses a temporary Vector, which is ugly but so be it.
   */
  private Enumeration enumerate_subtree(Hashtable table) {
    Vector temp = new Vector();
    Enumeration e = table.elements();

    // System.out.println("Start outer enumeration");
    while (e.hasMoreElements()) {
      Object data = e.nextElement();

      // System.out.println("  next element: "+data);
      if (data instanceof Hashtable) {
        // System.out.println("  Element is Hashtable, start inner enumeration");
        Hashtable next_table = (Hashtable)data;
        Enumeration ee = enumerate_subtree(next_table);
        while (ee.hasMoreElements()) {
        // System.out.println("    next element: "+data);
          temp.addElement(ee.nextElement());
        }
      }
      else {
        // System.out.println("  Element is Permission, add "+data+" to Vector");
        temp.addElement(data);
        // System.out.println("  -> Vector: "+temp);
      }
    }
    // System.out.println("End outer enumeration");

    // System.out.println("Final Vector: "+temp);
    return temp.elements();
  }

  /**
   * The elements() method returns an instance of PropertyPermissionEnumeration
   * (see above).
   */    
  public synchronized Enumeration elements() {
    Hashtable accumulator = new Hashtable();
    Enumeration e;

    e = enumerate_subtree(read_prefixes);
    while (e.hasMoreElements()) {
      Object p = e.nextElement();
      // System.out.println("read: put "+p);
      accumulator.put(p,p);
    }
    e = enumerate_subtree(write_prefixes);
    while (e.hasMoreElements()) {
      Object p = e.nextElement();
      // System.out.println("write: put "+p);
      accumulator.put(p,p);
    }
    return accumulator.keys();
  }

}
