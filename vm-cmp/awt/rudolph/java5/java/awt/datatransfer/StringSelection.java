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
package java.awt.datatransfer;

import java.io.IOException;
import java.io.StringReader;

public class StringSelection implements Transferable, ClipboardOwner{
  private String data;

  public StringSelection(String data){
    this.data = data;
  }

  public DataFlavor[] getTransferDataFlavors(){
    DataFlavor[] dataFlavor = new DataFlavor[2];
    dataFlavor[0] = DataFlavor.stringFlavor;
    dataFlavor[1] = DataFlavor.plainTextFlavor;
    return dataFlavor;
  }
  
  public boolean isDataFlavorSupported(DataFlavor flavor) throws NullPointerException{
    if(flavor == null) throw new NullPointerException("flavor is null");
    if(flavor.equals(DataFlavor.stringFlavor) || flavor.equals(DataFlavor.plainTextFlavor)) return true;
    return false;
  }

  public Object getTransferData(DataFlavor flavor)
                       throws UnsupportedFlavorException, IOException{
    if(flavor.equals(DataFlavor.stringFlavor)){
      return data;
    }
    if(flavor.equals(DataFlavor.plainTextFlavor)){
      return new StringReader(data);
    }
    throw new UnsupportedFlavorException(flavor);
  }

  public void lostOwnership(Clipboard clipboard,
                          Transferable contents){
  }

}
