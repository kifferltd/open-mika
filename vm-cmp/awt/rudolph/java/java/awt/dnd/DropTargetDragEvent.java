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
import java.awt.datatransfer.*;
import java.awt.*;

public class DropTargetDragEvent extends DropTargetEvent{
  private Point location;
  private int dropAction;
  private int actions;

  public DropTargetDragEvent(DropTargetContext dtc, Point cursorLocn,
                           int dropAction, int srcActions){
    super(dtc);
    this.location = cursorLocn;
    this.dropAction = dropAction;
    this.actions = srcActions;
  }

  public Point getLocation(){
    return location;
  }

  public DataFlavor[] getCurrentDataFlavors(){
    return context.getCurrentDataFlavors();
  }

  public java.util.List getCurrentDataFlavorsAsList(){
    return context.getCurrentDataFlavorsAsList();
  }

  public boolean isDataFlavorSupported(DataFlavor df){
    return context.isDataFlavorSupported(df);
  }

  public int getSourceActions(){
    return actions;
  }

  public int getDropAction(){
    return dropAction;
  }

  public void acceptDrag(int dragOperation){
    context.acceptDrag(dragOperation);
  }

  public void rejectDrag(){
    context.rejectDrag();
  }
}
