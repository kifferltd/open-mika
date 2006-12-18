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

package gnu.testlet.wonka.util.AbstractSequentialList; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...


/*
*  this class extends AbstractSequentialList and is used to test java.uitl.AbstractSequentialList
*  (since it is an abstract class)
*  used by SMAbstractListTest
*/
public class SMExASList extends AbstractSequentialList
{
	
	public LinkedList v = new LinkedList();
	
	public SMExASList(){
		super();
	}
	public SMExASList(List l){
		super();
		v.addAll(l);
	}
	
	public int size() {
		return v.size();
	}
	public ListIterator listIterator(int idx) {
	 	return v.listIterator(idx);
	}
}	