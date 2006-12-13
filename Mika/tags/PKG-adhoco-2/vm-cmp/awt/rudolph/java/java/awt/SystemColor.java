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

final public class SystemColor extends Color {

  private static final long serialVersionUID = 4503142729533789064L;

  /************************************************************/
  /** variables*/
  int index;
  static int[] colors;

  /************************************************************/
  /** static definitions colornames*/
  final public static int DESKTOP = 0;
  final public static int ACTIVE_CAPTION = 1;
  final public static int ACTIVE_CAPTION_TEXT = 2;
  final public static int ACTIVE_CAPTION_BORDER = 3;
  final public static int INACTIVE_CAPTION = 4;
  final public static int INACTIVE_CAPTION_TEXT = 5;
  final public static int INACTIVE_CAPTION_BORDER = 6;
  final public static int WINDOW = 7;
  final public static int WINDOW_BORDER = 8;
  final public static int WINDOW_TEXT = 9;
  final public static int MENU = 10;
  final public static int MENU_TEXT = 11;
  final public static int TEXT = 12;
  final public static int TEXT_TEXT = 13;
  final public static int TEXT_HIGHLIGHT = 14;
  final public static int TEXT_HIGHLIGHT_TEXT = 15;
  final public static int TEXT_INACTIVE_TEXT = 16;
  final public static int CONTROL = 17;
  final public static int CONTROL_TEXT = 18;
  final public static int CONTROL_HIGHLIGHT = 19;
  final public static int CONTROL_LT_HIGHLIGHT = 20;
  final public static int CONTROL_SHADOW = 21;
  final public static int CONTROL_DK_SHADOW = 22;
  final public static int SCROLLBAR = 23;
  final public static int INFO = 24;
  final public static int INFO_TEXT = 25;
  
  final public static int NUM_COLORS = 26;
  
  /************************************************************/
  /** static definitions SystenColor colors*/

  final public static SystemColor desktop;
  final public static SystemColor activeCaption;
  final public static SystemColor activeCaptionText;
  final public static SystemColor activeCaptionBorder;
  final public static SystemColor inactiveCaption;
  final public static SystemColor inactiveCaptionText;
  final public static SystemColor inactiveCaptionBorder;
  final public static SystemColor window;
  final public static SystemColor windowBorder;
  final public static SystemColor windowText;
  final public static SystemColor menu;
  final public static SystemColor menuText;
  final public static SystemColor text;
  final public static SystemColor textText;
  final public static SystemColor textHighlight;
  final public static SystemColor textHighlightText;
  final public static SystemColor textInactiveText;
  final public static SystemColor control;
  final public static SystemColor controlText;
  final public static SystemColor controlHighlight;
  final public static SystemColor controlLtHighlight;
  final public static SystemColor controlShadow;
  final public static SystemColor controlDkShadow;
  final public static SystemColor scrollbar;
  final public static SystemColor info;
  final public static SystemColor infoText;

  
  /************************************************************/
  /** static definitions color values*/
  static {
    colors = new int[26];
  
    colors[0]  = 0xff8b0000;    // desktop
    colors[1]  = 0xff00009b;    // active_caption
    colors[2]  = 0xffffffff;    // active_caption_text
    colors[3]  = 0xffc0c0c0;    // active_caption_border
    colors[4]  = 0xff808080;    // inactive_caption
    colors[5]  = 0xffc0c0c0;    // inactive_caption_text
    colors[6]  = 0xffc0c0c0;    // inactive_caption_border
    colors[7]  = 0xffd3d3d3;    // window
    colors[8]  = 0xff000000;    // window_border
    colors[9]  = 0xff000000;    // window_text
    colors[10] = 0xffc0c0c0;    // menu
    colors[11] = 0xff000000;    // menu_text
    colors[12] = 0xffc0c0c0;    // text
    colors[13] = 0xff000000;    // text_text
    colors[14] = 0xff000080;    // text_highlight
    colors[15] = 0xffffffff;    // text_highlight_text
    colors[16] = 0xff808080;    // text_inactive_text
    colors[17] = 0xffd3d3d3;    // control
    colors[18] = 0xff000000;    // controlText
    colors[19] = 0xffe0e0e0;    // control_highlight
    colors[20] = 0xffe0e0e0;    // control_lt_highlight
    colors[21] = 0xff575757;    // control_shadow
    colors[22] = 0xff575757;    // control_dk_shadow
    colors[23] = 0xffe0e0e0;    // scrollbar
    colors[24] = 0xffe0e000;    // info
    colors[25] = 0xff000000;    // info_text

    desktop = new SystemColor(DESKTOP);
    activeCaption = new SystemColor(ACTIVE_CAPTION);
    activeCaptionText = new SystemColor(ACTIVE_CAPTION_TEXT);
    activeCaptionBorder = new SystemColor(ACTIVE_CAPTION_BORDER);
    inactiveCaption = new SystemColor(INACTIVE_CAPTION);
    inactiveCaptionText = new SystemColor(INACTIVE_CAPTION_TEXT);
    inactiveCaptionBorder = new SystemColor(INACTIVE_CAPTION_BORDER);
    window = new SystemColor(WINDOW);
    windowBorder = new SystemColor(WINDOW_BORDER);
    windowText = new SystemColor(WINDOW_TEXT);
    menu = new SystemColor(MENU);
    menuText = new SystemColor(MENU_TEXT);
    text = new SystemColor(TEXT);
    textText = new SystemColor(TEXT_TEXT);
    textHighlight = new SystemColor(TEXT_HIGHLIGHT);
    textHighlightText = new SystemColor(TEXT_HIGHLIGHT_TEXT);
    textInactiveText = new SystemColor(TEXT_INACTIVE_TEXT);
    control = new SystemColor(CONTROL);
    controlText = new SystemColor(CONTROL_TEXT);
    controlHighlight = new SystemColor(CONTROL_HIGHLIGHT);
    controlLtHighlight = new SystemColor(CONTROL_LT_HIGHLIGHT);
    controlShadow = new SystemColor(CONTROL_SHADOW);
    controlDkShadow = new SystemColor(CONTROL_DK_SHADOW);
    scrollbar = new SystemColor(SCROLLBAR);
    info = new SystemColor(INFO);
    infoText = new SystemColor(INFO_TEXT);
  }

  /************************************************************/
  /** Constructor */
  SystemColor(int index) {
    super(colors[index]);
    this.index = index;
  }

  /************************************************************/
  /** RGB function */
  public int getRGB() {
    return colors[index];
  }

  /************************************************************/
  /** diagnostics */
  public String toString () {
    return "SystemColor [r = "+ getRed() +", g = "+ getGreen() +", b = " + getBlue() + "]";
  }
}
