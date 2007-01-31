package com.acunia.wonka.test.awt.dnd;

import java.awt.TextField;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.io.InputStream;

class DropText extends TextField implements
                DropTargetListener {
  DropText(String txt){
    super(txt);
    setDropTarget(new DropTarget(this, this));
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    dtde.acceptDrag(DnDConstants.ACTION_COPY);
  }

  public void dragOver(DropTargetDragEvent dtde) {
    dtde.acceptDrag(DnDConstants.ACTION_COPY);
  }

  public void dragExit(DropTargetEvent dte) {
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

    DataFlavor[] dataFlavors = dtde.getCurrentDataFlavors();
    DataFlavor   currentDataFlavor = null;

    for (int i = 0; i < dataFlavors.length; i++) {
      if (DataFlavor.plainTextFlavor.equals(dataFlavors[i])) {
        currentDataFlavor = dataFlavors[i];
        break;
      }
    }

    if (currentDataFlavor != null) {
      Transferable transf  = dtde.getTransferable();
      InputStream  is = null;

      try {
        is = (InputStream)transf.getTransferData(currentDataFlavor);
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
