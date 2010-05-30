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
package java.awt.dnd.peer;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

public interface DropTargetContextPeer{
  public void acceptDrag(int dragAction) ;
  public void acceptDrop(int dropAction) ;
  public void dropComplete(boolean success) ;
  public DropTarget getDropTarget() ;
  public int getTargetActions() ;
  public Transferable getTransferable() throws InvalidDnDOperationException ;
  public DataFlavor[] getTransferDataFlavors() ;
  public boolean isTransferableJVMLocal() ;
  public void rejectDrag() ;
  public void rejectDrop() ;
  public void setTargetActions(int actions) ;
}
