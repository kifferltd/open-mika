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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;

class DragDropText extends TextField implements Transferable, DragGestureListener,
                 DragSourceListener, DropTargetListener {
  private static    DataFlavor dataFlavors[] = new DataFlavor[] {createDataFlavor(), DataFlavor.plainTextFlavor, DataFlavor.stringFlavor };
  private static DataFlavor createDataFlavor() {
    try {
      return new DataFlavor("text/plain; charset=iso8859-1", "String");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  DragDropText(String txt) {
    super(txt);
    DragSource ds = DragSource.getDefaultDragSource();
    ds.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_COPY, this);
    setDropTarget(new DropTarget(this, this));
  }

  public void dragGestureRecognized(DragGestureEvent dge) {
    try{dge.startDrag(null, this, null);}
    catch(InvalidDnDOperationException idndoe){System.out.println("InvalidDnDOperationException: "+idndoe.getMessage());}
  }

  public void dragEnter(DragSourceDragEvent dsde) {
    dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
  }

  public void dragOver(DragSourceDragEvent dsde) {
  }

  public void dragGestureChanged(DragSourceDragEvent dsde) {
  }

  public void dragExit(DragSourceEvent dse) {
    dse.getDragSourceContext().setCursor(null);
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
    Thread.dumpStack();
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {
    Thread.dumpStack();
  }

  public DataFlavor[] getTransferDataFlavors() {
    return dataFlavors;
  }

  public boolean isDataFlavorSupported(DataFlavor df) {
    for (int i = 0 ; i < dataFlavors.length; i++){
      if (dataFlavors[i].equals(df)) return true;
    }
    return false;
  }

  public Object getTransferData(DataFlavor transferdf) throws
                UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(transferdf)) throw new UnsupportedFlavorException(transferdf);

    String originalText = getText();
    if (DataFlavor.stringFlavor.equals(transferdf)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);

      try {
        oos.writeObject(originalText);
      } catch (Exception e) { throw new IOException(); }

      return new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    } else {
      StringBufferInputStream sbis = new StringBufferInputStream(originalText);
      return sbis;
    }
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    dtde.acceptDrag(DnDConstants.ACTION_COPY);
  }

  public void dragOver(DropTargetDragEvent dtde) {
    dtde.acceptDrag(DnDConstants.ACTION_COPY);
  }

  public void dragExit(DropTargetEvent dtde) {
    repaint();
  }

  public void drop(DropTargetDropEvent dtde) {
    DropTargetContext dtc = dtde.getDropTargetContext();

    boolean outcome = false;
    if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
    else {
      dtde.rejectDrop();
      return;
    }

    DataFlavor[] dataflavors = dtde.getCurrentDataFlavors();
    DataFlavor   currentDataflavor = null;

    for (int i = 0; i < dataflavors.length; i++) {
      if (DataFlavor.plainTextFlavor.equals(dataflavors[i])) {
        currentDataflavor = dataflavors[i];
        break;
      }
    }

    if (currentDataflavor != null) {
      Transferable transf  = dtde.getTransferable();
      InputStream  is = null;

      try {
        is = (InputStream)transf.getTransferData(currentDataflavor);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        dtc.dropComplete(false);

        return;
      } catch (UnsupportedFlavorException ufe) {
        ufe.printStackTrace();
        dtc.dropComplete(false);
        repaint();
        return;
      }

      if (is != null) {
        String newText = getText();

        try {
          int length = is.available();
          byte[] string = new byte[length];
          is.read(string, 0, length);
          for (int i = 0; i < length; i++){
            if (string[i] == 0) {
              length = i;
              break;
            }
          }
          newText = new String(string, 0, length);
          outcome = true;
        } catch (Exception e) {
          e.printStackTrace();
          dtc.dropComplete(false);
          repaint();
          return;
        } finally {
          setText(newText);
        }
      } else outcome = false;
    }

    repaint();
    validate();
    dtc.dropComplete(outcome);
  }

  public void dragScroll(DropTargetDragEvent dtde) {
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
  }
}

