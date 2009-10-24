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
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.io.IOException;
import java.util.ArrayList;

public class DropTargetContext{

  DropTargetContextPeer peer;
  private TransferableProxy transferableProxy;

  public DropTarget getDropTarget(){
    return peer.getDropTarget();
  }

  public Component getComponent(){
    return peer.getDropTarget().getComponent();
  }

  public void addNotify(java.awt.dnd.peer.DropTargetContextPeer dtcp){
    peer = dtcp;
    transferableProxy = (TransferableProxy)createTransferableProxy(peer.getTransferable(),peer.isTransferableJVMLocal());
  }

  public void removeNotify(){
    peer = null;
  }

  protected void setTargetActions(int actions){
    peer.setTargetActions(actions);
  }

  protected int getTargetActions(){
    return peer.getTargetActions();
  }

  public void dropComplete(boolean success) throws InvalidDnDOperationException{
    peer.dropComplete(success);
  }

  protected void acceptDrag(int dragOperation){
    peer.acceptDrag(dragOperation);
  }

  protected void rejectDrag(){
    peer.rejectDrag();
  }

  protected void acceptDrop(int dropOperation){
    peer.acceptDrop(dropOperation);
  }

  protected void rejectDrop(){
    peer.rejectDrop();
  }

  protected DataFlavor[] getCurrentDataFlavors(){
    return (transferableProxy.getTransferDataFlavors());
  }

  protected java.util.List getCurrentDataFlavorsAsList(){
    DataFlavor[] temp = transferableProxy.getTransferDataFlavors();
    java.util.List theReturnList = new ArrayList();
    for(int i = 0; i< temp.length; i++){
      theReturnList.add(temp[i]);
    }
    return theReturnList;
  }

  protected boolean isDataFlavorSupported(DataFlavor df){
    return (transferableProxy.isDataFlavorSupported(df));
  }

  protected Transferable getTransferable() throws InvalidDnDOperationException{
    return transferableProxy;
  }

  protected Transferable createTransferableProxy(Transferable t, boolean local){
    return new TransferableProxy(t,local);
  }


  protected class TransferableProxy implements Transferable{
    protected Transferable transferable;
    protected boolean isLocal;

    TransferableProxy(Transferable t, boolean local){
      transferable = t;
      isLocal = local;
    }

    public DataFlavor[] getTransferDataFlavors(){
      if(peer != null){
        transferable = peer.getTransferable();
        isLocal = peer.isTransferableJVMLocal();
      }
      return transferable.getTransferDataFlavors();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor){
      if(peer != null){
        transferable = peer.getTransferable();
        isLocal = peer.isTransferableJVMLocal();
      }
      return transferable.isDataFlavorSupported(flavor);
    }

    public Object getTransferData(DataFlavor df)
                       throws UnsupportedFlavorException, IOException{
      if(peer != null){
        transferable = peer.getTransferable();
        isLocal = peer.isTransferableJVMLocal();
      }
      return transferable.getTransferData(df);
    }
  }
}

