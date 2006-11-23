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

public class Clipboard {
  protected Transferable contents;
  protected ClipboardOwner owner;
  private String nameClipboard;

  public Clipboard(String name){
    nameClipboard = name;
  }
  
  public Transferable getContents(Object requestor){
    return contents;
  }
  
  public String getName(){
    return nameClipboard;
  }
    
  public void setContents(Transferable contents, ClipboardOwner owner){
    if(this.owner != null && this.contents != null){
      if(this.owner != owner){
        this.owner.lostOwnership(this,this.contents);
        System.out.println("notify lose ownership of previous content ("+this.contents+") at owner ("+this.owner+")");
      }
    }
    this.owner = owner;
    this.contents = contents;
  }

}
