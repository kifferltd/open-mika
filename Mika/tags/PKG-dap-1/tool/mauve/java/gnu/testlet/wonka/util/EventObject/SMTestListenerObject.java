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



package gnu.testlet.wonka.util.EventObject;
import java.util.*;

/**
* this class is used together with SMEventObjectTest
*
*
*/
public class SMTestListenerObject implements SMTestListener {

   private SMEventObjectTest ob;
   	
   public SMTestListenerObject( SMEventObjectTest owner) {
   	ob = owner;	
   }
	
   public void fireTestEvent(SMTestEvent evt) {
	ob  = (SMEventObjectTest) evt.getSource();
	ob.SMevt = evt;
	ob.count++;
   }
	

}