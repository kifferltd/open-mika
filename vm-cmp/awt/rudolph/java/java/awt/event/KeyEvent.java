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


package java.awt.event;

import java.awt.Toolkit;
import java.util.Hashtable;

public class KeyEvent extends InputEvent {

  /*
  ** Key event ID's...
  */
  
  public static final int KEY_FIRST = 400;
  public static final int KEY_TYPED = 400;
  public static final int KEY_PRESSED = 401;
  public static final int KEY_RELEASED = 402;
  public static final int KEY_LAST = 402;

  /*
  ** Swing might need these?
  **
http://java.sun.com/j2se/1.4/docs/api/constant-values.html#java.awt.event.KeyEvent.VK_KP_LEFT
  */
  public static final int KEY_LOCATION_LEFT = 2;
  public static final int KEY_LOCATION_NUMPAD = 4;
  public static final int KEY_LOCATION_RIGHT = 3;
  public static final int KEY_LOCATION_STANDARD = 1;
  public static final int KEY_LOCATION_UNKNOWN = 0;

  /*
  ** Key codes...
  */

  public static final char CHAR_UNDEFINED = 0;
  public static final int VK_UNDEFINED = 0;
  public static final int VK_CANCEL =   3;
  public static final int VK_BACK_SPACE =   8;
  public static final int VK_TAB = 9;
  public static final int VK_ENTER = 10;
  public static final int VK_CLEAR =  12;
  public static final int VK_SHIFT = 16;
  public static final int VK_CONTROL =  17;
  public static final int VK_ALT =  18;
  public static final int VK_PAUSE = 19;
  public static final int VK_CAPS_LOCK =  20;
  public static final int VK_KANA = 21;
  public static final int VK_FINAL = 24;
  public static final int VK_KANJI = 25;
  public static final int VK_ESCAPE = 27;
  public static final int VK_CONVERT = 28;
  public static final int VK_NONCONVERT = 29;
  public static final int VK_ACCEPT =  30;
  public static final int VK_MODECHANGE = 31;
  public static final int VK_SPACE = 32;
  public static final int VK_PAGE_UP = 33;
  public static final int VK_PAGE_DOWN = 34;
  public static final int VK_END =  35;
  public static final int VK_HOME = 36;
  public static final int VK_LEFT = 37;
  public static final int VK_UP = 38;
  public static final int VK_RIGHT = 39;
  public static final int VK_DOWN =  40;
  public static final int VK_COMMA =  44;
  public static final int VK_PERIOD = 46;
  public static final int VK_SLASH = 47;
  
  public static final int VK_0 =  48;
  public static final int VK_1 =  49;
  public static final int VK_2 =  50;
  public static final int VK_3 =  51;
  public static final int VK_4 =  52;
  public static final int VK_5 =  53;
  public static final int VK_6 =  54;
  public static final int VK_7 =  55;
  public static final int VK_8 =  56;
  public static final int VK_9 =  57;

  public static final int VK_SEMICOLON =  59;

  public static final int VK_A =  65;
  public static final int VK_B =  66;
  public static final int VK_C =  67;
  public static final int VK_D =  68;
  public static final int VK_E =  69;
  public static final int VK_F =  70;
  public static final int VK_G =  71;
  public static final int VK_H =  72;
  public static final int VK_I =  73;
  public static final int VK_J =  74;
  public static final int VK_K =  75;
  public static final int VK_L =  76;
  public static final int VK_M =  77;
  public static final int VK_N =  78;
  public static final int VK_O =  79;
  public static final int VK_P =  80;
  public static final int VK_Q =  81;
  public static final int VK_R =  82;
  public static final int VK_S =  83;
  public static final int VK_T =  84;
  public static final int VK_U =  85;
  public static final int VK_V =  86;
  public static final int VK_W =  87;
  public static final int VK_X =  88;
  public static final int VK_Y =  89;
  public static final int VK_Z =  90;

  public static final int VK_OPEN_BRACKET = 91;
  public static final int VK_BACK_SLASH =  92;
  public static final int VK_CLOSE_BRACKET =  93;

  public static final int VK_NUMPAD0 =  96;  
  public static final int VK_NUMPAD1 =  97;  
  public static final int VK_NUMPAD2 =  98;  
  public static final int VK_NUMPAD3 =  99;  
  public static final int VK_NUMPAD4 = 100;  
  public static final int VK_NUMPAD5 = 101;  
  public static final int VK_NUMPAD6 = 102;  
  public static final int VK_NUMPAD7 = 103;  
  public static final int VK_NUMPAD8 = 104;  
  public static final int VK_NUMPAD9 = 105;  

  public static final int VK_MULTIPLY = 106;
  public static final int VK_ADD = 107;
  public static final int VK_SEPARATER = 108;
  public static final int VK_SUBTRACT = 109;
  public static final int VK_DECIMAL = 110;
  public static final int VK_DIVIDE = 111;
  
  public static final int VK_F1 = 112;
  public static final int VK_F2 = 113;
  public static final int VK_F3 = 114;
  public static final int VK_F4 = 115;
  public static final int VK_F5 = 116;
  public static final int VK_F6 = 117;
  public static final int VK_F7 = 118;
  public static final int VK_F8 = 119;
  public static final int VK_F9 = 120;
  public static final int VK_F10 = 121;
  public static final int VK_F11 = 122;
  public static final int VK_F12 = 123;

  public static final int VK_DELETE = 127;
  public static final int VK_NUM_LOCK = 144;
  public static final int VK_SCROLL_LOCK = 145;
  public static final int VK_PRINTSCREEN = 154;
  public static final int VK_INSERT = 155;
  public static final int VK_HELP = 156;
  public static final int VK_META = 157;
  public static final int VK_BACK_QUOTE = 192;
  public static final int VK_QUOTE = 222;

  /*
  ** More 1.2+ stuff
  */
  public static final int VK_ALL_CANDIDATES = 256;
  public static final int VK_ALPHANUMERIC = 240;
  public static final int VK_ALT_GRAPH = 65406;
  public static final int VK_AMPERSAND = 150;
  public static final int VK_ASTERISK = 151;
  public static final int VK_AT = 512;
  public static final int VK_CIRCUMFLEX = 514;
  public static final int VK_CODE_INPUT = 256;
  public static final int VK_COLON = 513;
  public static final int VK_COMPOSE = 65312;
  public static final int VK_COPY = 65485;
  public static final int VK_CUT = 65489;
  public static final int VK_DEAD_ABOVEDOT = 134;
  public static final int VK_DEAD_ABOVERING = 136;
  public static final int VK_DEAD_ACUTE = 129;
  public static final int VK_DEAD_BREVE = 133;
  public static final int VK_DEAD_CARON = 138;
  public static final int VK_DEAD_CEDILLA = 139;
  public static final int VK_DEAD_CIRCUMFLEX = 130;
  public static final int VK_DEAD_DIAERESIS = 135;
  public static final int VK_DEAD_DOUBLEACUTE = 137;
  public static final int VK_DEAD_GRAVE = 128;
  public static final int VK_DEAD_IOTA = 141;
  public static final int VK_DEAD_MACRON = 132;
  public static final int VK_DEAD_OGONEK = 140;
  public static final int VK_DEAD_SEMIVOICED_SOUND = 143;
  public static final int VK_DEAD_TILDE = 131;
  public static final int VK_DEAD_VOICED_SOUND = 142;
  public static final int VK_DOLLAR = 515;
  public static final int VK_EQUALS = 61;
  public static final int VK_EURO_SIGN = 516;
  public static final int VK_EXCLAMATION_MARK = 517;
  public static final int VK_F13 = 61440;
  public static final int VK_F14 = 61441;
  public static final int VK_F15 = 61442;
  public static final int VK_F16 = 61443;
  public static final int VK_F17 = 61444;
  public static final int VK_F18 = 61445;
  public static final int VK_F19 = 61446;
  public static final int VK_F20 = 61447;
  public static final int VK_F21 = 61448;
  public static final int VK_F22 = 61449;
  public static final int VK_F23 = 61450;
  public static final int VK_F24 = 61451;
  public static final int VK_FIND = 65488;
  public static final int VK_FULL_WIDTH = 243;
  public static final int VK_GREATER = 160;
  public static final int VK_HALF_WIDTH = 244;
  public static final int VK_HIRAGANA = 242;
  public static final int VK_INPUT_METHOD_ON_OFF = 263;
  public static final int VK_INVERTED_EXCLAMATION_MARK = 518;
  public static final int VK_JAPANESE_HIRAGANA = 260;
  public static final int VK_JAPANESE_KATAKANA = 259;
  public static final int VK_JAPANESE_ROMAN = 261;
  public static final int VK_KANA_LOCK = 262;
  public static final int VK_KATAKANA = 241;
  public static final int VK_KP_DOWN = 225;
  public static final int VK_KP_LEFT = 226;
  public static final int VK_KP_RIGHT = 227;
  public static final int VK_KP_UP = 224;
  public static final int VK_LEFT_PARENTHESIS = 519;
  public static final int VK_LESS = 153;
  public static final int VK_MINUS = 45;
  public static final int VK_NUMBER_SIGN = 520;
  public static final int VK_PASTE = 65487;
  public static final int VK_PLUS = 521;
  public static final int VK_PREVIOUS_CANDIDATE = 256;
  public static final int VK_PROPS = 65482;
  public static final int VK_QUOTEDBL = 152;
  public static final int VK_RIGHT_PARENTHESIS = 522;
  public static final int VK_ROMAN_CHARACTERS = 245;
  public static final int VK_SEPARATOR = 108;
  public static final int VK_STOP = 65480;
  public static final int VK_UNDERSCORE = 523;
  public static final int VK_UNDO = 65483;

  /*
  ** Variables
  */

  //protected Object   EventObject.source;
  //protected int      AWTEvent.id;
  //protected boolean  AWTEvent.consumed;
  //protected int      InputEvent.modifiers;
  //protected long     InputEvent.timeStamp;
  protected int keyCode;
  protected char keyChar;

  private static Hashtable key_props = new Hashtable();
  private static Hashtable key_defs = new Hashtable();

  /*
  ** Static initializer
  */

  static {
    key_props.put(new Integer(VK_BACK_SPACE),  "AWT.backSpace");
    key_props.put(new Integer(VK_TAB),         "AWT.tab");
    key_props.put(new Integer(VK_ENTER),       "AWT.enter");
    key_props.put(new Integer(VK_CLEAR),       "AWT.clear");
    key_props.put(new Integer(VK_SHIFT),       "AWT.shift");
    key_props.put(new Integer(VK_CONTROL),     "AWT.control");
    key_props.put(new Integer(VK_ALT),         "AWT.alt");
    key_props.put(new Integer(VK_PAUSE),       "AWT.pause");
    key_props.put(new Integer(VK_CAPS_LOCK),   "AWT.capsLock");
    key_props.put(new Integer(VK_KANA),        "AWT.kana");
    key_props.put(new Integer(VK_FINAL),       "AWT.final"); 
    key_props.put(new Integer(VK_KANJI),       "AWT.kanji");
    key_props.put(new Integer(VK_ESCAPE),      "AWT.escape");
    key_props.put(new Integer(VK_CONVERT),     "AWT.convert");
    key_props.put(new Integer(VK_NONCONVERT),  "AWT.noconvert");
    key_props.put(new Integer(VK_ACCEPT),      "AWT.accept");
    key_props.put(new Integer(VK_MODECHANGE),  "AWT.modechange");
    key_props.put(new Integer(VK_SPACE),       "AWT.space");
    key_props.put(new Integer(VK_PAGE_UP),     "AWT.pgup");
    key_props.put(new Integer(VK_PAGE_DOWN),   "AWT.pgdn");
    key_props.put(new Integer(VK_END),         "AWT.end");
    key_props.put(new Integer(VK_HOME),        "AWT.home");
    key_props.put(new Integer(VK_LEFT),        "AWT.left");
    key_props.put(new Integer(VK_UP),          "AWT.up");
    key_props.put(new Integer(VK_RIGHT),       "AWT.right");
    key_props.put(new Integer(VK_DOWN),        "AWT.down");
    key_props.put(new Integer(VK_NUMPAD0),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD1),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD2),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD3),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD4),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD5),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD6),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD7),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD8),     "AWT.numpad");
    key_props.put(new Integer(VK_NUMPAD9),     "AWT.numpad");
    key_props.put(new Integer(VK_MULTIPLY),    "AWT.multiply");
    key_props.put(new Integer(VK_ADD),         "AWT.add");
    key_props.put(new Integer(VK_SEPARATER),   "AWT.separater");
    key_props.put(new Integer(VK_SUBTRACT),    "AWT.subtract");
    key_props.put(new Integer(VK_DECIMAL),     "AWT.decimal");
    key_props.put(new Integer(VK_DIVIDE),      "AWT.divide");
    key_props.put(new Integer(VK_F1),          "AWT.f1");
    key_props.put(new Integer(VK_F2),          "AWT.f2");
    key_props.put(new Integer(VK_F3),          "AWT.f3");
    key_props.put(new Integer(VK_F4),          "AWT.f4");
    key_props.put(new Integer(VK_F5),          "AWT.f5");
    key_props.put(new Integer(VK_F6),          "AWT.f6");
    key_props.put(new Integer(VK_F7),          "AWT.f7");
    key_props.put(new Integer(VK_F8),          "AWT.f8");
    key_props.put(new Integer(VK_F9),          "AWT.f9");
    key_props.put(new Integer(VK_F10),         "AWT.f10");
    key_props.put(new Integer(VK_F11),         "AWT.f11");
    key_props.put(new Integer(VK_F12),         "AWT.f12");
    key_props.put(new Integer(VK_DELETE),      "AWT.delete");
    key_props.put(new Integer(VK_NUM_LOCK),    "AWT.numLock");
    key_props.put(new Integer(VK_SCROLL_LOCK), "AWT.scrollLock");
    key_props.put(new Integer(VK_PRINTSCREEN), "AWT.printScreen");
    key_props.put(new Integer(VK_INSERT),      "AWT.insert");
    key_props.put(new Integer(VK_HELP),        "AWT.help");
    key_props.put(new Integer(VK_META),        "AWT.meta");
    key_props.put(new Integer(VK_BACK_QUOTE),  "AWT.backQuote");
    key_props.put(new Integer(VK_QUOTE),       "AWT.quote");

    key_defs.put(new Integer(VK_BACK_SPACE),  "BackSpace");
    key_defs.put(new Integer(VK_TAB),         "Tab");
    key_defs.put(new Integer(VK_ENTER),       "Enter");
    key_defs.put(new Integer(VK_CLEAR),       "Clear");
    key_defs.put(new Integer(VK_SHIFT),       "Shift");
    key_defs.put(new Integer(VK_CONTROL),     "Ctrl");
    key_defs.put(new Integer(VK_ALT),         "Alt");
    key_defs.put(new Integer(VK_PAUSE),       "Pause");
    key_defs.put(new Integer(VK_CAPS_LOCK),   "Caps Lock");
    key_defs.put(new Integer(VK_KANA),        "Kana");
    key_defs.put(new Integer(VK_FINAL),       "Final");
    key_defs.put(new Integer(VK_KANJI),       "Kanji");
    key_defs.put(new Integer(VK_ESCAPE),      "Escape");
    key_defs.put(new Integer(VK_CONVERT),     "Convert");
    key_defs.put(new Integer(VK_NONCONVERT),  "No Convert");
    key_defs.put(new Integer(VK_ACCEPT),      "Accept");
    key_defs.put(new Integer(VK_MODECHANGE),  "Mode Change");
    key_defs.put(new Integer(VK_SPACE),       "Space");
    key_defs.put(new Integer(VK_PAGE_UP),     "Page Up");
    key_defs.put(new Integer(VK_PAGE_DOWN),   "Page Down");
    key_defs.put(new Integer(VK_END),         "End");
    key_defs.put(new Integer(VK_HOME),        "Home");
    key_defs.put(new Integer(VK_LEFT),        "Left");
    key_defs.put(new Integer(VK_UP),          "Up");
    key_defs.put(new Integer(VK_RIGHT),       "Right");
    key_defs.put(new Integer(VK_DOWN),        "Down");
    key_defs.put(new Integer(VK_COMMA),       ");");
    key_defs.put(new Integer(VK_PERIOD),      ".");
    key_defs.put(new Integer(VK_SLASH),       "/");
    key_defs.put(new Integer(VK_0),           "c0");
    key_defs.put(new Integer(VK_1),           "c1");
    key_defs.put(new Integer(VK_2),           "c2");
    key_defs.put(new Integer(VK_3),           "c3");
    key_defs.put(new Integer(VK_4),           "c4");
    key_defs.put(new Integer(VK_5),           "c5");
    key_defs.put(new Integer(VK_6),           "c6");
    key_defs.put(new Integer(VK_7),           "c7");
    key_defs.put(new Integer(VK_8),           "c8");
    key_defs.put(new Integer(VK_9),           "c9");
    key_defs.put(new Integer(VK_SEMICOLON),   ";");
    key_defs.put(new Integer(VK_A),           "A");
    key_defs.put(new Integer(VK_B),           "B");
    key_defs.put(new Integer(VK_C),           "C");
    key_defs.put(new Integer(VK_D),           "D");
    key_defs.put(new Integer(VK_E),           "E");
    key_defs.put(new Integer(VK_F),           "F");
    key_defs.put(new Integer(VK_G),           "G");
    key_defs.put(new Integer(VK_H),           "H");
    key_defs.put(new Integer(VK_I),           "I");
    key_defs.put(new Integer(VK_J),           "J");
    key_defs.put(new Integer(VK_K),           "K");
    key_defs.put(new Integer(VK_L),           "L");
    key_defs.put(new Integer(VK_M),           "M");
    key_defs.put(new Integer(VK_N),           "N");
    key_defs.put(new Integer(VK_O),           "O");
    key_defs.put(new Integer(VK_P),           "P");
    key_defs.put(new Integer(VK_Q),           "Q");
    key_defs.put(new Integer(VK_R),           "R");
    key_defs.put(new Integer(VK_S),           "S");
    key_defs.put(new Integer(VK_T),           "T");
    key_defs.put(new Integer(VK_U),           "U");
    key_defs.put(new Integer(VK_V),           "V");
    key_defs.put(new Integer(VK_W),           "W");
    key_defs.put(new Integer(VK_X),           "X");
    key_defs.put(new Integer(VK_Y),           "Y");
    key_defs.put(new Integer(VK_Z),           "Z");
    key_defs.put(new Integer(VK_OPEN_BRACKET),"(");
    key_defs.put(new Integer(VK_BACK_SLASH),  "\\");
    key_defs.put(new Integer(VK_CLOSE_BRACKET),")");
    key_defs.put(new Integer(VK_NUMPAD0),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD1),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD2),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD3),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD4),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD5),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD6),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD7),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD8),     "NumPad");
    key_defs.put(new Integer(VK_NUMPAD9),     "NumPad");
    key_defs.put(new Integer(VK_MULTIPLY),    "NumPad *");
    key_defs.put(new Integer(VK_ADD),         "NumPad +");
    key_defs.put(new Integer(VK_SEPARATER),   "NumPad ,");
    key_defs.put(new Integer(VK_SUBTRACT),    "NumPad -");
    key_defs.put(new Integer(VK_DECIMAL),     "NumPad .");
    key_defs.put(new Integer(VK_DIVIDE),      "NumPad /");
    key_defs.put(new Integer(VK_F1),          "F1");
    key_defs.put(new Integer(VK_F2),          "F2");
    key_defs.put(new Integer(VK_F3),          "F3");  
    key_defs.put(new Integer(VK_F4),          "F4");
    key_defs.put(new Integer(VK_F5),          "F5");
    key_defs.put(new Integer(VK_F6),          "F6");
    key_defs.put(new Integer(VK_F7),          "F7");
    key_defs.put(new Integer(VK_F8),          "F8");
    key_defs.put(new Integer(VK_F9),          "F9");
    key_defs.put(new Integer(VK_F10),         "F10");
    key_defs.put(new Integer(VK_F11),         "F11");
    key_defs.put(new Integer(VK_F12),         "F12");
    key_defs.put(new Integer(VK_DELETE),      "Delete");
    key_defs.put(new Integer(VK_NUM_LOCK),    "Num Lock");
    key_defs.put(new Integer(VK_SCROLL_LOCK), "Scroll Lock");
    key_defs.put(new Integer(VK_PRINTSCREEN), "Print Screen");
    key_defs.put(new Integer(VK_INSERT),      "Insert");
    key_defs.put(new Integer(VK_HELP),        "Help");
    key_defs.put(new Integer(VK_META),        "Meta");
    key_defs.put(new Integer(VK_BACK_QUOTE),  "Back Quote");
    key_defs.put(new Integer(VK_QUOTE),       "Quote");
  }
  
  /*
  ** Constructor
  */

  public KeyEvent(java.awt.Component source, int id, long when, int modifiers, int keycode, char keychar) {
    super(source, id);
    this.timeStamp = when;
    this.modifiers = modifiers;
    this.keyCode = keycode;
    this.keyChar = keychar;
  }

  public KeyEvent(java.awt.Component source, int id, long when, int modifiers, int keycode) {
    super(source, id);
    this.timeStamp = when;
    this.modifiers = modifiers;
    this.keyCode = keycode;
    this.keyChar = CHAR_UNDEFINED;
  }

  /*
  ** Primary data access
  */

  /** return the key character  */
  public char getKeyChar() {
    return keyChar;
  }

  /** assign a new key character  */
  public void setKeyChar(char newkeychar) {
    keyChar = newkeychar;
  }

  /** return the key code  */
  public int getKeyCode() {
    return keyCode;
  }

  /** assign a new key code  */
  public void setKeyCode(int newkeycode) {
    keyCode = newkeycode;
  }

  /** from InputEvent  */
  // public int getModifiers() { return modifiers;}

  /** assign a new set of modifiers  */
  public void setModifiers(int newmodifiers) {
    modifiers = newmodifiers;
  }

  /**
  ** check if key in the list of <action keys> : home, page-up, page-down etc...
  ** @remark: the 27 action keys checked are complient to the Java 1.1.2/1988 standard, may differ from latest 1.2 or 1.3 definitions
  */
  
  public boolean isActionKey(){
    return (keyCode==VK_HOME || keyCode==VK_END || keyCode==VK_PAGE_UP || keyCode==VK_PAGE_DOWN || keyCode==VK_INSERT 
            // the 6 scroll keys, excluding VK_DELETE, which is a unicode character
            || keyCode==VK_UP || keyCode==VK_DOWN || keyCode==VK_LEFT || keyCode==VK_RIGHT // cursor keys
            || keyCode==VK_F1 || keyCode==VK_F2 || keyCode==VK_F3 || keyCode==VK_F4 || keyCode==VK_F5 || keyCode==VK_F6
            || keyCode==VK_F7 || keyCode==VK_F8 || keyCode==VK_F9 || keyCode==VK_F10 || keyCode==VK_F11 || keyCode==VK_F12 // function keys
            || keyCode==VK_PRINTSCREEN || keyCode==VK_SCROLL_LOCK || keyCode==VK_CAPS_LOCK || keyCode==VK_NUM_LOCK
            || keyCode==VK_PAUSE);
  }

  public static String getKeyModifiersText(int modifiers) {
    return getKeyModifiersExText(extractModifiers(modifiers));
  }

  static String getKeyModifiersExText(int modifiersEx) {
    String text = ""; //$NON-NLS-1$

    if ((modifiersEx & InputEvent.META_DOWN_MASK) != 0) {
      text += Toolkit.getProperty("AWT.meta", "Meta"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if ((modifiersEx & InputEvent.CTRL_DOWN_MASK) != 0) {
      text += ((text.length() > 0) ? "+" : "") + //$NON-NLS-1$ //$NON-NLS-2$
              Toolkit.getProperty("AWT.control", "Ctrl"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if ((modifiersEx & InputEvent.ALT_DOWN_MASK) != 0) {
      text += ((text.length() > 0) ? "+" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    Toolkit.getProperty("AWT.alt", "Alt"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if ((modifiersEx & InputEvent.SHIFT_DOWN_MASK) != 0) {
      text += ((text.length() > 0) ? "+" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    Toolkit.getProperty("AWT.shift", "Shift"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if ((modifiersEx & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
      text += ((text.length() > 0) ? "+" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    Toolkit.getProperty("AWT.altGraph", "Alt Graph"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return text;
  }

  public static String getKeyText(int keycode) {
    String keytext = System.getProperty("AWT.unknown","Unknown Keycode");
    if(key_props.get(new Integer(keycode)) != null) {
      keytext = System.getProperty((String)key_props.get(new Integer(keycode)), (String)key_defs.get(new Integer(keycode)));
    } 
    else if(key_defs.get(new Integer(keycode)) != null) {
      keytext = (String)key_defs.get(new Integer(keycode));
    }
    return keytext;
  }
  
  /*
  **  Diagnostics
  */

  public String toString() {
    String descriptor = getClass().getName() +"[ ";
    if(id == KEY_TYPED) {
      descriptor += "KEY_TYPED";
    }
    else if(id == KEY_PRESSED) {
      descriptor += "KEY_PRESSED";
    }
    else if(id == KEY_RELEASED) {
      descriptor += "KEY_RELEASED";
    }
    else {
      descriptor += "UNKNOWN EVENT "+id;
    }
    descriptor += ", time="+timeStamp+", key code="+keyCode;
    descriptor += ", char="+keyChar+" modifiers="+modifiers;
    descriptor += "] on "+ source;
    return descriptor;
  }

  public String paramString() {
    return getClass().getName() +"[source="+source+", id="+id+", time="+timeStamp+", key code="+keyCode+
              " char="+keyChar+" modifiers="+modifiers+"]";
  }
  
  // From Apache Harmony
    private static int extractModifiers(int modifiers) {
        int mod = 0;

        if (((modifiers & SHIFT_MASK) != 0)
                || ((modifiers & SHIFT_DOWN_MASK) != 0)) {
            mod |= SHIFT_MASK | SHIFT_DOWN_MASK;
        }
        if (((modifiers & CTRL_MASK) != 0)
                || ((modifiers & CTRL_DOWN_MASK) != 0)) {
            mod |= CTRL_MASK | CTRL_DOWN_MASK;
        }
        if (((modifiers & META_MASK) != 0)
                || ((modifiers & META_DOWN_MASK) != 0)) {
            mod |= META_MASK | META_DOWN_MASK;
        }
        if (((modifiers & ALT_MASK) != 0) || ((modifiers & ALT_DOWN_MASK) != 0)) {
            mod |= ALT_MASK | ALT_DOWN_MASK;
        }
        if (((modifiers & ALT_GRAPH_MASK) != 0)
                || ((modifiers & ALT_GRAPH_DOWN_MASK) != 0)) {
            mod |= ALT_GRAPH_MASK | ALT_GRAPH_DOWN_MASK;
        }

        return mod;
    }

}
