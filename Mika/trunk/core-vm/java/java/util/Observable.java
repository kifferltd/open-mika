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
** $Id: Observable.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/
package java.util;

public class Observable {

  private HashSet observers;
  private boolean changed;

  public Observable(){
  	observers = new HashSet(11);
  }

  protected void clearChanged(){
	changed = false;
  }

  protected void setChanged(){
   	changed = true;
  }
  public void addObserver(Observer o){
   	if (o == null) {
   	 	throw new NullPointerException();
   	}
   	observers.add(o);
  }
  public int countObservers(){
   	return observers.size();
  }

  public void deleteObserver(Observer o){
   	observers.remove(o);
  }

  public void deleteObservers(){
   	observers.clear();
  }

  public boolean hasChanged(){
   	return changed;
  }

  public void notifyObservers(){
   	notifyObservers(null);
  }

  public void notifyObservers(Object arg){
   	if(changed) {
   		Iterator it = observers.iterator();
   		while (it.hasNext()){
   			((Observer)it.next()).update(this,arg); 	
   		}
   	        changed = false;
	}
  }
}
