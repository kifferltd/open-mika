package com.acunia.wonka.test.awt.dnd;

import java.awt.TextField;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;

class DragText extends TextField implements Transferable, DragGestureListener,
                 DragSourceListener {
  private static DataFlavor dataFlavors[] =
          new DataFlavor[] { createDataFlavor(), DataFlavor.plainTextFlavor, DataFlavor.stringFlavor };
  private static DataFlavor createDataFlavor() {
    try {
      return new DataFlavor("text/plain; charset=iso8859-1", "String");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


 DragText(String txt) {
    super(txt);
    DragSource ds = DragSource.getDefaultDragSource();
    ds.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_COPY, this);
  }

  public void dragGestureRecognized(DragGestureEvent dge) {
    try{dge.startDrag(null, this, null);}
    catch(InvalidDnDOperationException idndoe){System.out.println("InvalidDnDOperationException: "+idndoe.getMessage());}
  }

  public void dragEnter(DragSourceDragEvent dsde) {
    dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
  }

  public void dragOver(DragSourceDragEvent dsde) {
    Thread.dumpStack();
  }

  public void dragGestureChanged(DragSourceDragEvent dsde) {
    Thread.dumpStack();
  }

  public void dragExit(DragSourceEvent dse) {
    dse.getDragSourceContext().setCursor(null);
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {
  }

  public DataFlavor[] getTransferDataFlavors() {
    return dataFlavors;
  }

  public boolean isDataFlavorSupported(DataFlavor df) {
    for (int i = 0 ; i < dataFlavors.length; i++)
      if (dataFlavors[i].equals(df)) return true;
    return false;
  }

  public Object getTransferData(DataFlavor df) throws
                UnsupportedFlavorException , IOException {
    if (!isDataFlavorSupported(df)) throw new UnsupportedFlavorException(df);
    String originalText = getText();
    if (DataFlavor.stringFlavor.equals(df)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      try {
        oos.writeObject(originalText);
      } catch (Exception e) {
        throw new IOException();
      }
      return new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    } else {
      StringBufferInputStream sbis = new StringBufferInputStream(originalText);
      return sbis;
    }
  }
}
