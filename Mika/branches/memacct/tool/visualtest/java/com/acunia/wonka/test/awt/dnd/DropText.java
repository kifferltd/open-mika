/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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
