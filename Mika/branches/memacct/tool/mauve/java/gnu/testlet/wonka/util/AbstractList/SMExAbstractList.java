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


package gnu.testlet.wonka.util.AbstractList; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/*
*  this class extends AbstractList and is used to test java.uitl.AbstractList
*  (since it is an abstract class)
*  used by SMAbstractListTest
*/
public class SMExAbstractList extends AbstractList
{
	private boolean edit=true;
	private boolean didRemoveRange=false;
	private boolean updateMC=false;
	private boolean sleepy=false;
	private int from = -1;
	private int to   = -1;
	
	public Vector v = new Vector();
	
	public SMExAbstractList(){
		super();
	}
	
	public int size() {
		if (sleepy){
			try { Thread.sleep(150L); }
			catch(Exception e) {}
		}
		return v.size();
	}
		
	public Object get(int idx) {
		return v.get(idx);
	}
	
	public int getMC() {
		return modCount;
	}	
	
	public void set_edit(boolean b) {
	 	edit = b;
	}
	public void set_sleepy(boolean b) {
	 	sleepy = b;
	}
	public void set_updateMC(boolean b) {
	 	updateMC = b;
	}
	
	public void add(int idx, Object o) {
		if (edit) {
		 	v.add(idx , o);
		}
		else super.add(idx,o);
		if (updateMC) modCount++;
	}

	public Object remove(int idx) {
		if (edit) {
			if (updateMC) modCount++;
			return v.remove(idx);
		}
		//System.out.println("calling remove from AbstractList");	
		return super.remove(idx);
	}
	
	public Object set(int idx , Object o) {
		if (edit) {
			if (updateMC) modCount++;
			return v.set(idx , o);
		}
		return super.set(idx , o);
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