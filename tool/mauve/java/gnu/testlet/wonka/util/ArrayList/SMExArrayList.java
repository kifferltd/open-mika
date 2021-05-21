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


package gnu.testlet.wonka.util.ArrayList; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/*
*  this class extends ArrayList and is used to test java.uitl.ArrayList
*  used by SMArrayListTest
*/
public class SMExArrayList extends ArrayList
{
	private boolean didRemoveRange=false;
	private int from = -1;
	private int to   = -1;
	public SMExArrayList(Collection c){
		super(c);
	}
	
	public void removeRange(int fidx, int tidx) {
		didRemoveRange=true;
		to   = tidx;
		from = fidx;
		super.removeRange(fidx, tidx);
	}
	
	public boolean get_dRR() {
		return didRemoveRange;
	}
	public void set_dRR(boolean b) {
		didRemoveRange = b;
	}
	public int get_to() {
		return to;
	}
	public int get_from() {
		return from;
	}
}	