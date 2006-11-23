/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/
package com.acunia.wonka.rudolph.peers;
import java.awt.dnd.peer.*;
import java.awt.dnd.*;

public class DefaultDropTargetPeer implements DropTargetPeer{
  private DropTarget[] targets;

  public void addDropTarget(DropTarget dt){
    DropTarget temp[] = new DropTarget[targets.length+1];
    boolean exists = false;
    for(int i =0; i<targets.length; i++){
      temp[i]=targets[i];
      if(targets[i]==dt) exists = true;
    }
    if(!exists){
      temp[temp.length]=dt;
      targets = temp;
    }
  }

  public void removeDropTarget(DropTarget dt){
    if(targets.length > 1){
      DropTarget temp[] = new DropTarget[targets.length-1];
      int i;
      for(i = 0; i < temp.length && targets[i]!=dt; i++){
        temp[i] = targets[i];
      }
      if(i==temp.length && targets[temp.length+1] == dt) targets = temp;
      else{
        if(temp.length != i){
          for(int j = temp.length; j>= i; j--){
            temp[j] = targets[j+1];
          }
        targets = temp;
        }
      }
    }
    else if(targets.length == 1 & targets[0] == dt) targets = null;
  }

}
