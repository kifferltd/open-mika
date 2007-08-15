/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

package com.acunia.wonka.rudolph;
import java.awt.datatransfer.*;
import java.awt.dnd.peer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.*;

public class DropTargetEvent extends java.awt.dnd.DropTargetEvent{
  private static com.acunia.wonka.rudolph.DropTargetEvent dte;
  private static boolean inProgress = false;
  private static DragSourceContext dragSourceContext;

  private DropTargetEvent(java.awt.dnd.DropTargetContext dtc){
    super(dtc);
  }

  public static com.acunia.wonka.rudolph.DropTargetEvent getDropTargetEvent(){
    return dte;
  }

  public static void setDropTargetContext(java.awt.dnd.DropTargetContext dtc){
    dte = new DropTargetEvent(dtc);
  }

  public static boolean setInProgress(boolean inProgr, DragSourceContext dsc){
    if(inProgress && inProgr) return false;
    if(inProgress && !inProgr) dragSourceContext.setCursor(Cursor.getDefaultCursor());
    inProgress = inProgr;
    dragSourceContext = dsc;
    return true;
  }
  
  public static DragSourceContext getDragSourceContext(){
    if(!inProgress) return null;
    return dragSourceContext;
  }

  public static boolean getInProgress(){
    return inProgress;
  }
  
  public void dispatch(MouseEvent event){
    if(inProgress)((com.acunia.wonka.rudolph.DropTargetContext)getDropTargetContext()).getDropTargetContextPeer().getEventDispatcher().dispatchEvent(this, event);
  }
}



  


