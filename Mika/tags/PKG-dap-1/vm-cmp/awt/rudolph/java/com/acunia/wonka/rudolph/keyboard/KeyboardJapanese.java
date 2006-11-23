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

package com.acunia.wonka.rudolph.keyboard;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class KeyboardJapanese extends Keyboard {

  private Vector keys_hira;
  private Vector keys_kata;
  private Vector keys_nrs;
  private Vector keys_common;
  private Vector keys_common_hira;
  private Vector keys_common_kata;
  private Vector keys_shift_hira;
  private Vector keys_shift_kata;

  private Vector current_keys;
  
  private KeyboardButton k_123_1;
  private KeyboardButton k_123_2;
  private KeyboardButton k_shift;
  private KeyboardButton k_caps;
  private KeyboardButton k_ctrl;
  private KeyboardButton k_kata;
  private KeyboardButton k_hira;
  private KeyboardButton k_accent;
  private KeyboardButton k_circle;
  private KeyboardButton k_backspace;
  private KeyboardButton lastPressed = null;
  private KeyboardButton lastKey = null;

//  private Image background1;
//  private Image background2;

  private boolean f_caps = false;
  private boolean f_shift = false;
  private boolean f_num = false;
  private boolean f_ctrl = false;
  private boolean f_hira = true;

  private Component lastSource;

  public KeyboardJapanese() {
    super();
    
//    background1 = Toolkit.getDefaultToolkit().createImage(background1_data);
//    background2 = Toolkit.getDefaultToolkit().createImage(background2_data);

    current_keys = keys_hira;
  }

  protected void buildKbd() {
   
    /*
    ** The different lists of keys.
    */
    
    keys_hira = new Vector();
    keys_kata = new Vector();
    keys_nrs = new Vector();
    keys_shift_hira = new Vector();
    keys_shift_kata = new Vector();
    keys_common_hira = new Vector();
    keys_common_kata = new Vector();
    keys_common = new Vector();
  
    /*
    ** Common keys
    */

    k_123_1 = new KeyboardButtonText("123", new int[]{  0,  19,  19,   0}, new int[]{ 0,  0, 16, 16}, 0, '\0');
    k_123_2 = new KeyboardButtonText("123", new int[]{  0,  19,  19,   0}, new int[]{ 0,  0, 20, 20}, 0, '\0');
    k_shift = new KeyboardButtonText("Shift", new int[]{  0,  33,  33,   0}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_SHIFT, '\0');
    k_caps = new KeyboardButtonText("CAP", new int[]{  0,  29,  29,   0}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_CAPS_LOCK, '\0');
    k_ctrl = new KeyboardButtonText("Ctrl", new int[]{  0,  22,  22,   0}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_CONTROL, '\0');
    k_backspace = new KeyboardButtonPoly(new int[]{4, 9, 9, 14, 14, 9, 9}, new int[]{8, 3, 6, 6, 10, 10, 13}, 
                                             new int[]{221, 239, 239, 221}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_BACK_SPACE, '\0');
    
    k_hira = new KeyboardButtonTextJapanese("\u30ab\u30bf", new int[]{ 22,  44,  44,  22}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '\0', 0);
    k_kata = new KeyboardButtonTextJapanese("\u3072\u3089", new int[]{ 22,  44,  44,  22}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '\0', 0);
    
    k_accent = new KeyboardButtonTextJapanese("\u309b", new int[]{193, 208, 208, 193}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u309b', 0);
    k_circle = new KeyboardButtonTextJapanese("\u309c", new int[]{208, 224, 224, 208}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u309c', 0);

    keys_common.add(k_123_1);
    keys_common.add(new KeyboardButtonTextJapanese("\u3078", new int[]{178, 192, 192, 178}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3078', 2));
    keys_common.add(new KeyboardButtonTextJapanese("\u30fc", new int[]{192, 207, 207, 192}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30fc', 0));
    keys_common.add(new KeyboardButtonTextJapanese("\u300c", new int[]{207, 221, 221, 207}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u300c', 0));
    keys_common.add(k_backspace);
    
    keys_common.add(new KeyboardButtonText("Tab",    new int[]{  0,  25,  25,   0}, new int[]{16, 16, 32, 32}, KeyEvent.VK_TAB, '\0'));
    keys_common.add(k_accent);
    keys_common.add(k_circle);
    keys_common.add(new KeyboardButtonTextJapanese("\u300d", new int[]{224, 239, 239, 224}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u300d', 0));
    keys_common.add(k_caps);
    keys_common.add(new KeyboardButtonTextJapanese("\u3002", new int[]{212, 227, 227, 212}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u3002', 0));
    keys_common.add(new KeyboardButtonPoly(new int[]{-6, -2, -3, 4, 4, 6, 6, -3, -2}, new int[]{24, 20, 23, 23, 21, 21, 25, 25, 28}, 
                                             new int[]{227, 239, 239, 213, 213, 227}, new int[]{32, 32, 64, 64, 48, 48}, KeyEvent.VK_ENTER, '\n'));
    keys_common.add(k_shift);
    keys_common.add(new KeyboardButtonTextJapanese("\u3001", new int[]{198, 213, 213, 198}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3001', 0));
    keys_common.add(k_ctrl);
    keys_common.add(new KeyboardButtonTextJapanese("\u30fb", new int[]{ 44,  62,  62,  44}, new int[]{64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '\u30fb', 0));
    keys_common.add(new KeyboardButtonText("?",      new int[]{ 62,  80,  80,  62}, new int[]{64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '?'));
    keys_common.add(new KeyboardButtonText(" ",      new int[]{ 80, 167, 167,  80}, new int[]{64, 64, 80, 80}, KeyEvent.VK_SPACE, ' '));
    keys_common.add(new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{4, 8, 7, 12, 12, 7, 8}, 
                                             new int[]{167, 185, 185, 167}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UP, '\0'));
    keys_common.add(new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{12, 8, 9, 4, 4, 9, 8}, 
                                             new int[]{185, 203, 203, 185}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_DOWN, '\0'));
    keys_common.add(new KeyboardButtonPoly(new int[]{5, 9, 8, 13, 13, 8, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                             new int[]{203, 221, 221, 203}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_LEFT, '\0'));
    keys_common.add(new KeyboardButtonPoly(new int[]{13, 9, 10, 5, 5, 10, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                             new int[]{221, 239, 239, 221}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_RIGHT, '\0'));

    /*
    ** Hiragana keyboard.
    */
    
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u306c", new int[]{ 19,  33,  33,  19}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u306c', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3075", new int[]{ 33,  48,  48,  33}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3075', 2));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u307b", new int[]{163, 178, 178, 163}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u307b', 2));
    
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u305f", new int[]{ 25,  40,  40,  25}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u305f', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3066", new int[]{ 40,  56,  56,  40}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3066', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3059", new int[]{ 71,  86,  86,  71}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3059', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u304b", new int[]{ 86, 101, 101,  86}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u304b', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3093", new int[]{101, 117, 117, 101}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3093', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u306a", new int[]{117, 132, 132, 117}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u306a', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u306b", new int[]{132, 147, 147, 132}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u306b', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3089", new int[]{147, 163, 163, 147}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3089', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u305b", new int[]{163, 178, 178, 163}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u305b', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3092", new int[]{178, 193, 193, 178}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3092', 0));
    
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3061", new int[]{ 29,  44,  44,  29}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u3061', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3068", new int[]{ 44,  59,  59,  44}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u3068', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3057", new int[]{ 59,  75,  75,  59}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u3057', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u306f", new int[]{ 75,  90,  90,  75}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u306f', 2));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u304d", new int[]{ 90, 105, 105,  90}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u304d', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u304f", new int[]{105, 120, 120, 105}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u304f', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u307e", new int[]{120, 136, 136, 120}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u307e', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u306e", new int[]{136, 151, 151, 136}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u306e', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u308a", new int[]{151, 166, 166, 151}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u308a', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u308c", new int[]{166, 181, 181, 166}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u308c', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3051", new int[]{181, 197, 197, 181}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u3051', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3080", new int[]{197, 212, 212, 197}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u3080', 0));

    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3055", new int[]{ 48,  63,  63,  48}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3055', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u305d", new int[]{ 63,  78,  78,  63}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u305d', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3072", new int[]{ 78,  93,  93,  78}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3072', 2));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3053", new int[]{ 93, 108, 108,  93}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3053', 1));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u307f", new int[]{108, 123, 123, 108}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u307f', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3082", new int[]{123, 138, 138, 123}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3082', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u306d", new int[]{138, 153, 153, 138}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u306d', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u308b", new int[]{153, 168, 168, 153}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u308b', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u3081", new int[]{168, 183, 183, 168}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3081', 0));
    keys_common_hira.add(new KeyboardButtonTextJapanese("\u308d", new int[]{183, 198, 198, 183}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u308d', 0));

    keys_common_hira.add(k_kata);
    keys_common_hira.addAll(keys_common);

    keys_hira.add(new KeyboardButtonTextJapanese("\u3042", new int[]{ 48,  62,  62,  48}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3042', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3046", new int[]{ 62,  77,  77,  62}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3046', 1));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3048", new int[]{ 77,  91,  91,  77}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3048', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u304a", new int[]{ 91, 106, 106,  91}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u304a', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3084", new int[]{106, 120, 120, 106}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3084', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3086", new int[]{120, 134, 134, 120}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3086', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3088", new int[]{134, 149, 149, 134}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3088', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u308f", new int[]{149, 163, 163, 149}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u308f', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3044", new int[]{ 56,  71,  71,  56}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3044', 0));
    keys_hira.add(new KeyboardButtonTextJapanese("\u3064", new int[]{ 33,  48,  48,  33}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3064', 1));

    keys_hira.addAll(keys_common_hira);

    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3041", new int[]{ 48,  62,  62,  48}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3041', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3045", new int[]{ 62,  77,  77,  62}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3045', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3047", new int[]{ 77,  91,  91,  77}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3047', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3049", new int[]{ 91, 106, 106,  91}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3049', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3083", new int[]{106, 120, 120, 106}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3083', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3085", new int[]{120, 134, 134, 120}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3085', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3087", new int[]{134, 149, 149, 134}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u3087', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u308e", new int[]{149, 163, 163, 149}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u308e', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3043", new int[]{ 56,  71,  71,  56}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u3043', 0));
    keys_shift_hira.add(new KeyboardButtonTextJapanese("\u3063", new int[]{ 33,  48,  48,  33}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u3063', 0));

    keys_shift_hira.addAll(keys_common_hira);

    /*
    ** Katakana keyboard.
    */
    
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30cc", new int[]{ 19,  33,  33,  19}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30cc', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30d5", new int[]{ 33,  48,  48,  33}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30d5', 2));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30db", new int[]{163, 178, 178, 163}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30db', 2));

    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30bf", new int[]{ 25,  40,  40,  25}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30bf', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30c6", new int[]{ 40,  56,  56,  40}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30c6', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30b9", new int[]{ 71,  86,  86,  71}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30b9', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ab", new int[]{ 86, 101, 101,  86}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30ab', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30f3", new int[]{101, 117, 117, 101}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30f3', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ca", new int[]{117, 132, 132, 117}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30ca', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30cb", new int[]{132, 147, 147, 132}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30cb', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30e9", new int[]{147, 163, 163, 147}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30e9', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30bb", new int[]{163, 178, 178, 163}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30bb', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30f2", new int[]{178, 193, 193, 178}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30f2', 0));
    
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30c1", new int[]{ 29,  44,  44,  29}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30c1', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30c8", new int[]{ 44,  59,  59,  44}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30c8', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30b7", new int[]{ 59,  75,  75,  59}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30b7', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30cf", new int[]{ 75,  90,  90,  75}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30cf', 2));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ad", new int[]{ 90, 105, 105,  90}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30ad', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30af", new int[]{105, 120, 120, 105}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30af', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30de", new int[]{120, 136, 136, 120}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30de', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ce", new int[]{136, 151, 151, 136}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30ce', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ea", new int[]{151, 166, 166, 151}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30ea', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ec", new int[]{166, 181, 181, 166}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30ec', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30b1", new int[]{181, 197, 197, 181}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30b1', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30e0", new int[]{197, 212, 212, 197}, new int[]{32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\u30e0', 0));

    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30b5", new int[]{ 48,  63,  63,  48}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30b5', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30bd", new int[]{ 63,  78,  78,  63}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30bd', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30d2", new int[]{ 78,  93,  93,  78}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30d2', 2));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30b3", new int[]{ 93, 108, 108,  93}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30b3', 1));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30df", new int[]{108, 123, 123, 108}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30df', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30e2", new int[]{123, 138, 138, 123}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30e2', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30cd", new int[]{138, 153, 153, 138}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30cd', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30eb", new int[]{153, 168, 168, 153}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30eb', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30e1", new int[]{168, 183, 183, 168}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30e1', 0));
    keys_common_kata.add(new KeyboardButtonTextJapanese("\u30ed", new int[]{183, 198, 198, 183}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30ed', 0));

    keys_common_kata.add(k_hira);
    keys_common_kata.addAll(keys_common);

    keys_kata.add(new KeyboardButtonTextJapanese("\u30a2", new int[]{ 48,  62,  62,  48}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a2', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30a6", new int[]{ 62,  77,  77,  62}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a6', 1));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30a8", new int[]{ 77,  91,  91,  77}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a8', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30aa", new int[]{ 91, 106, 106,  91}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30aa', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30e4", new int[]{106, 120, 120, 106}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30e4', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30e6", new int[]{120, 134, 134, 120}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30e6', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30e8", new int[]{134, 149, 149, 134}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30e8', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30ef", new int[]{149, 163, 163, 149}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30ef', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30a4", new int[]{ 56,  71,  71,  56}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30a4', 0));
    keys_kata.add(new KeyboardButtonTextJapanese("\u30c4", new int[]{ 33,  48,  48,  33}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30c4', 1));

    keys_kata.addAll(keys_common_kata);

    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30a1", new int[]{ 48,  62,  62,  48}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a1', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30a5", new int[]{ 62,  77,  77,  62}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a5', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30a7", new int[]{ 77,  91,  91,  77}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a7', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30a9", new int[]{ 91, 106, 106,  91}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30a9', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30e3", new int[]{106, 120, 120, 106}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30e3', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30e5", new int[]{120, 134, 134, 120}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30e5', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30e7", new int[]{134, 149, 149, 134}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30e7', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30ee", new int[]{149, 163, 163, 149}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\u30ee', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30a3", new int[]{ 56,  71,  71,  56}, new int[]{16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '\u30a3', 0));
    keys_shift_kata.add(new KeyboardButtonTextJapanese("\u30c3", new int[]{ 33,  48,  48,  33}, new int[]{48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '\u30c3', 0));

    keys_shift_kata.addAll(keys_common_kata);

    /*
    ** Numerical Keyboard.
    */
    
    keys_nrs.add(k_123_2);
    keys_nrs.add(new KeyboardButtonText("[", new int[]{  19,  36,  36,  19}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_OPEN_BRACKET, '\0'));
    keys_nrs.add(new KeyboardButtonText("]", new int[]{  36,  53,  53,  36}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_CLOSE_BRACKET, '\0'));
    keys_nrs.add(new KeyboardButtonText("{", new int[]{  53,  70,  70,  53}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '{'));
    keys_nrs.add(new KeyboardButtonText("}", new int[]{  70,  87,  87,  70}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '}')); 
    
    keys_nrs.add(new KeyboardButtonText("7", new int[]{  93, 113, 113,  93}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_NUMPAD7, '7'));
    keys_nrs.add(new KeyboardButtonText("8", new int[]{ 113, 133, 133, 113}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_NUMPAD8, '8'));
    keys_nrs.add(new KeyboardButtonText("9", new int[]{ 133, 153, 153, 133}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_NUMPAD9, '9'));

    keys_nrs.add(new KeyboardButtonText("#", new int[]{ 159, 179, 179, 159}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '#'));
    keys_nrs.add(new KeyboardButtonText("%", new int[]{ 179, 199, 199, 179}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '%'));
    keys_nrs.add(new KeyboardButtonText("=", new int[]{ 199, 219, 219, 199}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '='));
    keys_nrs.add(new KeyboardButtonPoly(new int[]{5, 10, 10, 15, 15, 10, 10}, new int[]{10, 5, 8, 8, 12, 12, 15}, 
                                             new int[]{ 219, 239, 239, 219}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_BACK_SPACE, '\0'));

    keys_nrs.add(new KeyboardButtonText("^", new int[]{   0,  19,  19,   0}, new int[]{20, 20, 40, 40}, KeyEvent.VK_UNDEFINED, '^'));
    keys_nrs.add(new KeyboardButtonText(",", new int[]{  19,  36,  36,  19}, new int[]{20, 20, 40, 40}, KeyEvent.VK_SEPARATER, ','));
    keys_nrs.add(new KeyboardButtonText(".", new int[]{  36,  53,  53,  36}, new int[]{20, 20, 40, 40}, KeyEvent.VK_DECIMAL, '.'));
    keys_nrs.add(new KeyboardButtonText("<", new int[]{  53,  70,  70,  53}, new int[]{20, 20, 40, 40}, KeyEvent.VK_UNDEFINED, '<'));
    keys_nrs.add(new KeyboardButtonText(">", new int[]{  70,  87,  87,  70}, new int[]{20, 20, 40, 40}, KeyEvent.VK_UNDEFINED, '>'));
    
    keys_nrs.add(new KeyboardButtonText("4", new int[]{  93, 113, 113,  93}, new int[]{20, 20, 40, 40}, KeyEvent.VK_NUMPAD4, '4'));
    keys_nrs.add(new KeyboardButtonText("5", new int[]{ 113, 133, 133, 113}, new int[]{20, 20, 40, 40}, KeyEvent.VK_NUMPAD5, '5'));
    keys_nrs.add(new KeyboardButtonText("6", new int[]{ 133, 153, 153, 133}, new int[]{20, 20, 40, 40}, KeyEvent.VK_NUMPAD6, '6'));

    keys_nrs.add(new KeyboardButtonText("+", new int[]{ 159, 179, 179, 159}, new int[]{20, 20, 40, 40}, KeyEvent.VK_ADD, '+'));
    keys_nrs.add(new KeyboardButtonText("-", new int[]{ 179, 199, 199, 179}, new int[]{20, 20, 40, 40}, KeyEvent.VK_SUBTRACT, '-'));
    keys_nrs.add(new KeyboardButtonText("*", new int[]{ 199, 219, 219, 199}, new int[]{20, 20, 40, 40}, KeyEvent.VK_MULTIPLY, '*'));
    keys_nrs.add(new KeyboardButtonText("/", new int[]{ 219, 239, 239, 219}, new int[]{20, 20, 40, 40}, KeyEvent.VK_DIVIDE, '/'));
    
    keys_nrs.add(new KeyboardButtonText(" ", new int[]{   0,  19,  19,   0}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, '\0'));
    keys_nrs.add(new KeyboardButtonText(" ", new int[]{  19,  36,  36,  19}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, '\0'));
    keys_nrs.add(new KeyboardButtonText(":", new int[]{  36,  53,  53,  36}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, ':'));
    keys_nrs.add(new KeyboardButtonText("\\", new int[]{  53,  70,  70,  53}, new int[]{40, 40, 60, 60}, KeyEvent.VK_BACK_SLASH, '\\'));
    keys_nrs.add(new KeyboardButtonText("|", new int[]{  70,  87,  87,  70}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, '|'));
    
    keys_nrs.add(new KeyboardButtonText("1", new int[]{  93, 113, 113,  93}, new int[]{40, 40, 60, 60}, KeyEvent.VK_NUMPAD1, '1'));
    keys_nrs.add(new KeyboardButtonText("2", new int[]{ 113, 133, 133, 113}, new int[]{40, 40, 60, 60}, KeyEvent.VK_NUMPAD2, '2'));
    keys_nrs.add(new KeyboardButtonText("3", new int[]{ 133, 153, 153, 133}, new int[]{40, 40, 60, 60}, KeyEvent.VK_NUMPAD3, '3'));

    keys_nrs.add(new KeyboardButtonPoly(new int[]{10, 6, 9, 9, 11, 11, 14}, new int[]{6, 10, 9, 14, 14, 9, 10}, 
                                             new int[]{ 159, 179, 179, 159}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UP, '\0'));
    keys_nrs.add(new KeyboardButtonPoly(new int[]{10, 6, 9, 9, 11, 11, 14}, new int[]{14, 10, 11, 6, 6, 11, 10}, 
                                             new int[]{ 179, 199, 199, 179}, new int[]{40, 40, 60, 60}, KeyEvent.VK_DOWN, '\0'));
    keys_nrs.add(new KeyboardButtonPoly(new int[]{6, 10, 9, 14, 14, 9, 10}, new int[]{10, 6, 9, 9, 11, 11, 14}, 
                                             new int[]{ 199, 219, 219, 199}, new int[]{40, 40, 60, 60}, KeyEvent.VK_LEFT, '\0'));
    keys_nrs.add(new KeyboardButtonPoly(new int[]{14, 10, 11, 6, 6, 11, 10}, new int[]{10, 6, 9, 9, 11, 11, 14}, 
                                             new int[]{ 219, 239, 239, 219}, new int[]{40, 40, 60, 60}, KeyEvent.VK_RIGHT, '\0'));
    
    keys_nrs.add(new KeyboardButtonText("$", new int[]{   0,  19,  19,   0}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '$')); 
    keys_nrs.add(new KeyboardButtonText(" ", new int[]{  19,  36,  36,  19}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0')); 
    keys_nrs.add(new KeyboardButtonText("\u20AC", new int[]{  36,  53,  53,  36}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0')); 
    keys_nrs.add(new KeyboardButtonText("\u00A3", new int[]{  53,  70,  70,  53}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0'));
    keys_nrs.add(new KeyboardButtonText("\u00A5", new int[]{  70,  87,  87,  70}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0'));
    
    keys_nrs.add(new KeyboardButtonText("(", new int[]{  93, 113, 113,  93}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '('));
    keys_nrs.add(new KeyboardButtonText("0", new int[]{ 113, 133, 133, 113}, new int[]{60, 60, 80, 80}, KeyEvent.VK_NUMPAD0, '0'));
    keys_nrs.add(new KeyboardButtonText(")", new int[]{ 133, 153, 153, 133}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, ')'));

    keys_nrs.add(new KeyboardButtonText("Tab", new int[]{ 159, 183, 183, 159}, new int[]{60, 60, 80, 80}, KeyEvent.VK_TAB, '\0'));
    keys_nrs.add(new KeyboardButtonText("Space", new int[]{ 183, 221, 221, 183}, new int[]{60, 60, 80, 80}, KeyEvent.VK_SPACE, ' '));
    keys_nrs.add(new KeyboardButtonPoly(new int[]{2, 6, 5, 12, 12, 14, 14, 5, 6}, new int[]{10, 6, 9, 9, 7, 7, 11, 11, 14}, 
                                             new int[]{ 221, 239, 239, 221}, new int[]{60, 60, 80, 80}, KeyEvent.VK_ENTER, '\n'));
    
  }

  public void paint(Graphics g) {
    if(f_num) {
//      g.drawImage(background2, 0, 0, null);
    }
    else {
//      g.drawImage(background1, 0, 0, null);
    }
    Iterator iter = current_keys.iterator();
    while(iter.hasNext()) {
      KeyboardButton key = (KeyboardButton)iter.next();
      key.paint(g);
    }
  }

  private KeyboardButton getKey(int x, int y) {
    Iterator iter = current_keys.iterator();
    while(iter.hasNext()) {
      KeyboardButton key = (KeyboardButton)iter.next();
      if(key.contains(x, y)) {
        return key;
      }
    }
    return null;
  }

  /*
  ** Mouse events.
  */
  
  public void mousePressed(MouseEvent event) {
    KeyboardButton key = getKey(event.getX(), event.getY());
    
    if(key == null) {
      return;
    }
    
    if(key == k_kata) {
      current_keys = (f_shift ? keys_shift_kata : keys_kata);
      f_hira = false;
    }
    else if(key == k_hira) {
      current_keys = (f_shift ? keys_shift_hira : keys_hira);
      f_hira = true;
    }
    else if(key == k_shift) {
      if(key.getPressed()) {
        key.setPressed(false);
        if(f_hira) current_keys = keys_hira; else current_keys = keys_kata;
        f_shift = false;
      }
      else {
        key.setPressed(true);
        if(f_hira) current_keys = keys_shift_hira; else current_keys = keys_shift_kata;
        f_shift = true;
      }
    }
    else if(key == k_caps) {
      if(key.getPressed()) {
        f_caps = false;
        if(f_hira) current_keys = keys_hira; else current_keys = keys_kata;
        key.setPressed(false);
      } 
      else {
        f_caps = true;
        if(f_hira) current_keys = keys_shift_hira; else current_keys = keys_shift_kata;
        key.setPressed(true);
      }
      lastPressed = null;
    }
    else if(key == k_123_1) {
      current_keys = keys_nrs;
      f_num = true;        
      k_123_2.setPressed(true);
    }
    else if(key == k_123_2) {
      if(f_caps) {
        if(f_hira) current_keys = keys_shift_hira; else current_keys = keys_shift_kata;
      }
      else {
        if(f_hira) current_keys = keys_hira; else current_keys = keys_kata;
      }
      f_num = false;
      if(k_shift.getPressed()) {
        f_shift = false;
        k_shift.setPressed(false);
        if(f_caps) {
          if(f_hira) current_keys = keys_shift_hira; else current_keys = keys_shift_kata;
        }
        else {
          if(f_hira) current_keys = keys_hira; else current_keys = keys_kata;
        }
      }
    }
    else {
      KeyEvent ke;
      Component source = Component.getFocusComponent();
      lastPressed = key;
      lastSource = source;
      key.setPressed(true);

      if(source != null) {

        if((key == k_accent || key == k_circle) && (lastKey instanceof KeyboardButtonTextJapanese)) {
          int accent = ((KeyboardButtonTextJapanese)lastKey).getAccent();
          if(accent >= 1 && key == k_accent) {

            /*
            ** First send a backspace to delete the previous character, then send the character with the accent.
            */

            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, k_backspace.getKeyEvent(), k_backspace.getKeyChar()));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_TYPED,   System.currentTimeMillis(), 0, k_backspace.getKeyEvent(), k_backspace.getKeyChar()));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, lastKey.getKeyEvent(), 
                  (char)(lastKey.getKeyChar() + 1)));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_TYPED,   System.currentTimeMillis(), 0, lastKey.getKeyEvent(), 
                  (char)(lastKey.getKeyChar() + 1)));
          }
          else if(accent >= 2 && key == k_circle) {

            /*
            ** First send a backspace to delete the previous character, then send the character with the accent.
            */

            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, k_backspace.getKeyEvent(), k_backspace.getKeyChar()));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_TYPED,   System.currentTimeMillis(), 0, k_backspace.getKeyEvent(), k_backspace.getKeyChar()));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, lastKey.getKeyEvent(), 
                  (char)(lastKey.getKeyChar() + 2)));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_TYPED,   System.currentTimeMillis(), 0, lastKey.getKeyEvent(), 
                  (char)(lastKey.getKeyChar() + 2)));
          }
          else {

            /*
            ** The character has no accents, send the accent by itself.
            */

            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key.getKeyEvent(), key.getKeyChar()));
            source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_TYPED,   System.currentTimeMillis(), 0, key.getKeyEvent(), key.getKeyChar()));
          }
        }
        else {
          source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key.getKeyEvent(), key.getKeyChar()));
          source.dispatchEvent(new KeyEvent(source, KeyEvent.KEY_TYPED,   System.currentTimeMillis(), 0, key.getKeyEvent(), key.getKeyChar()));
        }
      }

      lastKey = key;
    }

    repaint();
  }
  
  public void mouseReleased(MouseEvent event) {
    if(lastPressed != null) {
      lastPressed.setPressed(false);
      if(f_shift) {
        f_shift = false;
        k_shift.setPressed(false);
        if(f_caps) {
          if(f_hira) current_keys = keys_shift_hira; else current_keys = keys_shift_kata;
        }
        else {
          if(f_hira) current_keys = keys_hira; else current_keys = keys_kata;
        }
      }
      if(lastSource != null) {
        KeyEvent ke = new KeyEvent(lastSource, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, lastPressed.getKeyEvent(), lastPressed.getKeyChar());
        lastSource.dispatchEvent(ke);
      }
      lastPressed = null;
      repaint();
    }
  }
 
} 

