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
package com.acunia.wonka.test.awt.dnd;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceListener;


public class DragSourceContext2 extends DragSourceContext{
  public DragSourceContext2(java.awt.dnd.peer.DragSourceContextPeer dscp,
                       DragGestureEvent trigger, Cursor dragCursor,
                       Image dragImage, Point offset, Transferable t,
                       DragSourceListener dsl){
    super(dscp,trigger,dragCursor, dragImage, offset, t, dsl);
  }

  public static int getDefault(){
    return DEFAULT;
   }

  public static int getEnter(){
    return ENTER;
  }

  public static int getOver(){
    return OVER;
  }

  public static int getChanged(){
    return CHANGED;
  }
}

