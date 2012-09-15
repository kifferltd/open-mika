/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

package java.awt;

public class Cursor implements java.io.Serializable {

  private static final long serialVersionUID = 8028237497568985504L;
  
  public static final int CROSSHAIR_CURSOR = 1;
  public static final int DEFAULT_CURSOR = 0;
  public static final int E_RESIZE_CURSOR = 11;
  public static final int HAND_CURSOR = 12;
  public static final int MOVE_CURSOR = 13;
  public static final int N_RESIZE_CURSOR = 8;
  public static final int NE_RESIZE_CURSOR = 7;
  public static final int NW_RESIZE_CURSOR = 6;
  public static final int S_RESIZE_CURSOR = 9;
  public static final int SE_RESIZE_CURSOR = 5;
  public static final int SW_RESIZE_CURSOR = 4;
  public static final int TEXT_CURSOR = 2;
  public static final int W_RESIZE_CURSOR = 10;
  public static final int WAIT_CURSOR = 3;
  public static final int CUSTOM_CURSOR = -1;

  private int type;
  private String name;
  private static String[] nameType= {"Default Cursor","Crosshair Cursor","Text Cursor","Wait Cursor","Southwest Resize Cursor",
                 "Southeast Resize Cursor","Northwest Resize Cursor","Northeast Resize Cursor","North Resize Cursor",
		 "South Resize Cursor","West Resize Cursor","East Resize Cursor","Hand Cursor","Move Cursor"};

  protected static Cursor[] predefined;

  public Cursor(int type) {
    if(type>=0 && type <=13){
      this.type = type;
      this.name = nameType[type];
    } else throw new IllegalArgumentException();
  }

  protected Cursor(String name){
    this.name = name;
    this.type = CUSTOM_CURSOR;
  }

  public static Cursor getDefaultCursor() {
    return new Cursor(DEFAULT_CURSOR);
  }

  public static Cursor getPredefinedCursor(int type) {
    return new Cursor(type);
  }
  
  public int getType() {
    return type;
  }
//  methods 1.2

  public String getName(){
    return name;
  }
  
  public String toString(){
    return (this.getClass().getName()+"["+getName()+"]");
  }
}

