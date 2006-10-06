/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: ListResourceBundle.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

public abstract class ListResourceBundle extends ResourceBundle{


      private Hashtable hash;

      protected abstract Object[][] getContents();


/**
** This method should not be overridden by subclasses.
*/
     public  Enumeration getKeys() {
	if (hash == null) {
	      	buildHashTable();
	}
	return hash.keys();       	
      }

      public final Object handleGetObject(String key) {
	if (hash == null) {
	      	buildHashTable();
	}
       	return hash.get(key);
      }

      private synchronized void buildHashTable() {
          if (hash == null) {
           	Object [][] arrays = getContents();
           	int len = arrays.length;
		hash = new Hashtable(len);
		for (int i = 0 ; i < len ; i++) {
		 	hash.put(arrays[i][0],arrays[i][1]);
		}
          }
      }

}