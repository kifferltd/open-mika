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

package wonka.vm;

import java.util.Timer;
import java.util.TimerTask;

public class SystemTimer {

  private static Timer timer;

  private SystemTimer(){}

  public synchronized static Timer getSystemTimer(){
    if(timer == null){
      timer = new Timer(true);
    }
    return timer;
  }

  public static void scheduleSystemTask(TimerTask tt, long period){
    getSystemTimer().schedule(tt, 0, period);
  }

  public static void scheduleOneTimeSystemTask(TimerTask tt, long delay){
    getSystemTimer().schedule(tt, delay);
  }


}