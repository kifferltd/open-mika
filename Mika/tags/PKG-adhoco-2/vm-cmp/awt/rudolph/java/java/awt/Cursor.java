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

