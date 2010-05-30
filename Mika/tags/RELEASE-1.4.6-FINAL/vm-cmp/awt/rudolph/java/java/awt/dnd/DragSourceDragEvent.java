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

package java.awt.dnd;

public class DragSourceDragEvent extends DragSourceEvent{
  private int dropAction;
  private int targetActions;
  private int gestureModifiers;
  
  public DragSourceDragEvent(DragSourceContext dsc,
                           int dropAction, int actions, int modifiers){
    super(dsc);
    this.dropAction = dropAction;
    this.targetActions = actions;
    this.gestureModifiers = modifiers;
  }

  public int getTargetActions(){
    int source = getDragSourceContext().getSourceActions();
    if(source==DnDConstants.ACTION_COPY_OR_MOVE){
      if(targetActions==DnDConstants.ACTION_COPY_OR_MOVE){
        if(dropAction==DnDConstants.ACTION_COPY_OR_MOVE || dropAction==DnDConstants.ACTION_COPY || dropAction==DnDConstants.ACTION_MOVE)
          return dropAction;
        }
        else{
          if(targetActions==DnDConstants.ACTION_COPY || targetActions==DnDConstants.ACTION_MOVE)
            return targetActions; //there`s only one posibility for action, user can`t chose
        }
    } else{
      if(source==DnDConstants.ACTION_COPY){
        if(targetActions==DnDConstants.ACTION_COPY_OR_MOVE || targetActions==DnDConstants.ACTION_COPY)
          return source; //there`s only one posibility for action, user can`t chose
      } else{
        if(source==DnDConstants.ACTION_MOVE){
          if(targetActions==DnDConstants.ACTION_COPY_OR_MOVE || targetActions==DnDConstants.ACTION_MOVE)
            return source; //there`s only one posibility for action, user can`t chose
        } else{
          if(source==DnDConstants.ACTION_LINK || source==DnDConstants.ACTION_REFERENCE){
            if(targetActions==DnDConstants.ACTION_LINK || targetActions==DnDConstants.ACTION_REFERENCE)
              return source; //these 2 actions are equal
          }
        }
      }
    }
    return DnDConstants.ACTION_NONE; //if there`re no possibilities
  }

  public int getGestureModifiers(){
    return gestureModifiers;
  }

  public int getUserAction(){
    return targetActions;
  }

  public int getDropAction(){
    return getTargetActions();
  }
}
