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

public class KeyboardDefault extends Keyboard {

  protected Vector keys_low;
  protected Vector keys_cap;
  protected Vector keys_nrs;

  protected Image keys_low_img;
  protected Image keys_cap_img;
  protected Image keys_nrs_img;
  
  protected Vector current_keys;
  
  private KeyboardButton k_123_1;
  private KeyboardButton k_123_2;
  private KeyboardButton k_shift;
  private KeyboardButton k_caps;
  private KeyboardButton k_ctrl;
  private KeyboardButton lastPressed = null;

  protected Image background1;
  protected Image background2;

  private boolean f_caps = false;
  private boolean f_shift = false;
  private boolean f_num = false;
  private boolean f_ctrl = false;

  protected Component lastSource;

  public KeyboardDefault() {
    super();

    background1 = Toolkit.getDefaultToolkit().createImage(background1_data);
    background2 = Toolkit.getDefaultToolkit().createImage(background2_data);

    current_keys = keys_low;
  }

  protected void buildKbd() {
    
    keys_low = new Vector();
    keys_cap = new Vector();
    keys_nrs = new Vector();
   
    k_123_1 = new KeyboardButtonText("123", new int[]{  0,  19,  19,   0}, new int[]{ 0,  0, 16, 16}, 0, '\0');
    k_123_2 = new KeyboardButtonText("123", new int[]{  0,  19,  19,   0}, new int[]{ 0,  0, 20, 20}, 0, '\0');
    k_shift = new KeyboardButtonText("Shift", new int[]{  0,  33,  33,   0}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_SHIFT, '\0');
    k_caps = new KeyboardButtonText("CAP", new int[]{  0,  29,  29,   0}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_CAPS_LOCK, '\0');
    k_ctrl = new KeyboardButtonText("Ctrl", new int[]{  0,  22,  22,   0}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_CONTROL, '\0');
    
    /*
    ** Normal keyboard.
    */
    
    keys_low.add(k_123_1);
    keys_low.add(new KeyboardButtonText("1", new int[]{ 19,  36,  36,  19}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_1, '1'));
    keys_low.add(new KeyboardButtonText("2", new int[]{ 36,  53,  53,  36}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_2, '2'));
    keys_low.add(new KeyboardButtonText("3", new int[]{ 53,  70,  70,  53}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_3, '3'));
    keys_low.add(new KeyboardButtonText("4", new int[]{ 70,  87,  87,  70}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_4, '4'));
    keys_low.add(new KeyboardButtonText("5", new int[]{ 87, 104, 104,  87}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_5, '5'));
    keys_low.add(new KeyboardButtonText("6", new int[]{104, 121, 121, 104}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_6, '6'));
    keys_low.add(new KeyboardButtonText("7", new int[]{121, 138, 138, 121}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_7, '7'));
    keys_low.add(new KeyboardButtonText("8", new int[]{138, 155, 155, 138}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_8, '8'));
    keys_low.add(new KeyboardButtonText("9", new int[]{155, 172, 172, 155}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_9, '9'));
    keys_low.add(new KeyboardButtonText("0", new int[]{172, 189, 189, 172}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_0, '0'));
    keys_low.add(new KeyboardButtonText("-", new int[]{189, 205, 205, 189}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '-')); 
    keys_low.add(new KeyboardButtonText("=", new int[]{205, 221, 221, 205}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '='));
    keys_low.add(new KeyboardButtonPoly(new int[]{4, 9, 9, 14, 14, 9, 9}, new int[]{8, 3, 6, 6, 10, 10, 13}, 
                                             new int[]{221, 239, 239, 221}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_BACK_SPACE, '\0'));

    keys_low.add(new KeyboardButtonText("Tab", new int[]{  0,  25,  25,   0}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_TAB, '\0'));
    keys_low.add(new KeyboardButtonText("q", new int[]{ 25,  43,  43,  25}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Q, 'q'));
    keys_low.add(new KeyboardButtonText("w", new int[]{ 43,  61,  61,  43}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_W, 'w'));
    keys_low.add(new KeyboardButtonText("e", new int[]{ 61,  79,  79,  61}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_E, 'e'));
    keys_low.add(new KeyboardButtonText("r", new int[]{ 79,  97,  97,  79}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_R, 'r'));
    keys_low.add(new KeyboardButtonText("t", new int[]{ 97, 115, 115,  97}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_T, 't'));
    keys_low.add(new KeyboardButtonText("y", new int[]{115, 133, 133, 115}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Y, 'y'));
    keys_low.add(new KeyboardButtonText("u", new int[]{133, 151, 151, 133}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_U, 'u'));
    keys_low.add(new KeyboardButtonText("i", new int[]{151, 169, 169, 151}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_I, 'i'));
    keys_low.add(new KeyboardButtonText("o", new int[]{169, 187, 187, 169}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_O, 'o'));
    keys_low.add(new KeyboardButtonText("p", new int[]{187, 205, 205, 187}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_P, 'p'));
    keys_low.add(new KeyboardButtonText("[", new int[]{205, 222, 222, 205}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_OPEN_BRACKET, '['));
    keys_low.add(new KeyboardButtonText("]", new int[]{222, 239, 239, 222}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_CLOSE_BRACKET, ']'));
    
    keys_low.add(k_caps);
    keys_low.add(new KeyboardButtonText("a", new int[]{ 29,  47,  47,  29}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_A, 'a'));
    keys_low.add(new KeyboardButtonText("s", new int[]{ 47,  65,  65,  47}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_S, 's'));
    keys_low.add(new KeyboardButtonText("d", new int[]{ 65,  83,  83,  65}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_D, 'd'));
    keys_low.add(new KeyboardButtonText("f", new int[]{ 83, 101, 101,  83}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_F, 'f'));
    keys_low.add(new KeyboardButtonText("g", new int[]{101, 119, 119, 101}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_G, 'g'));
    keys_low.add(new KeyboardButtonText("h", new int[]{119, 137, 137, 119}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_H, 'h'));
    keys_low.add(new KeyboardButtonText("j", new int[]{137, 155, 155, 137}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_J, 'j'));
    keys_low.add(new KeyboardButtonText("k", new int[]{155, 173, 173, 155}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_K, 'k'));
    keys_low.add(new KeyboardButtonText("l", new int[]{173, 191, 191, 173}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_L, 'l'));
    keys_low.add(new KeyboardButtonText(";", new int[]{191, 209, 209, 191}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_SEMICOLON, ';'));
    keys_low.add(new KeyboardButtonText("'", new int[]{209, 227, 227, 209}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_QUOTE, '\'')); 
    keys_low.add(new KeyboardButtonPoly(new int[]{-6, -2, -3, 4, 4, 6, 6, -3, -2}, new int[]{24, 20, 23, 23, 21, 21, 25, 25, 28}, 
                                             new int[]{227, 239, 239, 213, 213, 227}, new int[]{ 32, 32, 64, 64, 48, 48}, KeyEvent.VK_ENTER, '\n'));

    keys_low.add(k_shift);
    keys_low.add(new KeyboardButtonText("z", new int[]{ 33,  51,  51,  33}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_Z, 'z'));
    keys_low.add(new KeyboardButtonText("x", new int[]{ 51,  69,  69,  51}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_X, 'x'));
    keys_low.add(new KeyboardButtonText("c", new int[]{ 69,  87,  87,  69}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_C, 'c'));
    keys_low.add(new KeyboardButtonText("v", new int[]{ 87, 105, 105,  87}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_V, 'v'));
    keys_low.add(new KeyboardButtonText("b", new int[]{105, 123, 123, 105}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_B, 'b'));
    keys_low.add(new KeyboardButtonText("n", new int[]{123, 141, 141, 123}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_N, 'n'));
    keys_low.add(new KeyboardButtonText("m", new int[]{141, 159, 159, 141}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_M, 'm'));
    keys_low.add(new KeyboardButtonText(",", new int[]{159, 177, 177, 159}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_COMMA, ','));
    keys_low.add(new KeyboardButtonText(".", new int[]{177, 195, 195, 177}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_PERIOD, '.'));
    keys_low.add(new KeyboardButtonText("/", new int[]{195, 213, 213, 195}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_SLASH, '/'));

    keys_low.add(k_ctrl);
    keys_low.add(new KeyboardButtonText("au", new int[]{ 22,  44,  44,  22}, new int[]{ 64, 64, 80, 80}, 0, '\0'));
    keys_low.add(new KeyboardButtonText("`", new int[]{ 44,  62,  62,  44}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_BACK_QUOTE, '`'));
    keys_low.add(new KeyboardButtonText("\\", new int[]{ 62,  80,  80,  62}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_BACK_SLASH, '\\'));
    keys_low.add(new KeyboardButtonText(" ", new int[]{ 80, 167, 167,  80}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_SPACE, ' '));
    keys_low.add(new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{4, 8, 7, 12, 12, 7, 8}, 
                                             new int[]{167, 185, 185, 167}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UP, '\0'));
    keys_low.add(new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{12, 8, 9, 4, 4, 9, 8}, 
                                             new int[]{185, 203, 203, 185}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_DOWN, '\0'));
    keys_low.add(new KeyboardButtonPoly(new int[]{5, 9, 8, 13, 13, 8, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                             new int[]{203, 221, 221, 203}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_LEFT, '\0'));
    keys_low.add(new KeyboardButtonPoly(new int[]{13, 9, 10, 5, 5, 10, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                             new int[]{221, 239, 239, 221}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_RIGHT, '\0'));

    /*
    ** Keyboard with shift pressed in.
    */
    
    keys_cap.add(k_123_1);
    keys_cap.add(new KeyboardButtonText("!", new int[]{ 19,  36,  36,  19}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '!'));
    keys_cap.add(new KeyboardButtonText("@", new int[]{ 36,  53,  53,  36}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '@'));
    keys_cap.add(new KeyboardButtonText("#", new int[]{ 53,  70,  70,  53}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '#'));
    keys_cap.add(new KeyboardButtonText("$", new int[]{ 70,  87,  87,  70}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '$'));
    keys_cap.add(new KeyboardButtonText("%", new int[]{ 87, 104, 104,  87}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '%'));
    keys_cap.add(new KeyboardButtonText("^", new int[]{104, 121, 121, 104}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '^'));
    keys_cap.add(new KeyboardButtonText("&", new int[]{121, 138, 138, 121}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '&'));
    keys_cap.add(new KeyboardButtonText("*", new int[]{138, 155, 155, 138}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '*'));
    keys_cap.add(new KeyboardButtonText("(", new int[]{155, 172, 172, 155}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '('));
    keys_cap.add(new KeyboardButtonText(")", new int[]{172, 189, 189, 172}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, ')'));
    keys_cap.add(new KeyboardButtonText("_", new int[]{189, 205, 205, 189}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '_'));
    keys_cap.add(new KeyboardButtonText("+", new int[]{205, 221, 221, 205}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '+'));
    keys_cap.add(new KeyboardButtonText("Del", new int[]{221, 239, 239, 221}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '\0'));

    keys_cap.add(new KeyboardButtonText("Tab", new int[]{  0,  25,  25,   0}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_TAB, '\0'));
    keys_cap.add(new KeyboardButtonText("Q", new int[]{ 25,  43,  43,  25}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Q, 'Q'));
    keys_cap.add(new KeyboardButtonText("W", new int[]{ 43,  61,  61,  43}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_W, 'W'));
    keys_cap.add(new KeyboardButtonText("E", new int[]{ 61,  79,  79,  61}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_E, 'E'));
    keys_cap.add(new KeyboardButtonText("R", new int[]{ 79,  97,  97,  79}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_R, 'R'));
    keys_cap.add(new KeyboardButtonText("T", new int[]{ 97, 115, 115,  97}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_T, 'T'));
    keys_cap.add(new KeyboardButtonText("Y", new int[]{115, 133, 133, 115}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Y, 'Y'));
    keys_cap.add(new KeyboardButtonText("U", new int[]{133, 151, 151, 133}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_U, 'U'));
    keys_cap.add(new KeyboardButtonText("I", new int[]{151, 169, 169, 151}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_I, 'I'));
    keys_cap.add(new KeyboardButtonText("O", new int[]{169, 187, 187, 169}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_O, 'O'));
    keys_cap.add(new KeyboardButtonText("P", new int[]{187, 205, 205, 187}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_P, 'P'));
    keys_cap.add(new KeyboardButtonText("{", new int[]{205, 222, 222, 205}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '{'));
    keys_cap.add(new KeyboardButtonText("}", new int[]{222, 239, 239, 222}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '}'));
    
    keys_cap.add(k_caps);
    keys_cap.add(new KeyboardButtonText("A", new int[]{ 29,  47,  47,  29}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_A, 'A'));
    keys_cap.add(new KeyboardButtonText("S", new int[]{ 47,  65,  65,  47}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_S, 'S'));
    keys_cap.add(new KeyboardButtonText("D", new int[]{ 65,  83,  83,  65}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_D, 'D'));
    keys_cap.add(new KeyboardButtonText("F", new int[]{ 83, 101, 101,  83}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_F, 'F'));
    keys_cap.add(new KeyboardButtonText("G", new int[]{101, 119, 119, 101}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_G, 'G'));
    keys_cap.add(new KeyboardButtonText("H", new int[]{119, 137, 137, 119}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_H, 'H'));
    keys_cap.add(new KeyboardButtonText("J", new int[]{137, 155, 155, 137}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_J, 'J'));
    keys_cap.add(new KeyboardButtonText("K", new int[]{155, 173, 173, 155}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_K, 'K'));
    keys_cap.add(new KeyboardButtonText("L", new int[]{173, 191, 191, 173}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_L, 'L'));
    keys_cap.add(new KeyboardButtonText(":", new int[]{191, 209, 209, 191}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, ':'));
    keys_cap.add(new KeyboardButtonText("\"", new int[]{209, 227, 227, 209}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\\'));
    keys_cap.add(new KeyboardButtonPoly(new int[]{-6, -2, -3, 4, 4, 6, 6, -3, -2}, new int[]{24, 20, 23, 23, 21, 21, 25, 25, 28}, 
                                             new int[]{227, 239, 239, 213, 213, 227}, new int[]{ 32, 32, 64, 64, 48, 48}, KeyEvent.VK_ENTER, '\n'));

    keys_cap.add(k_shift);
    keys_cap.add(new KeyboardButtonText("Z", new int[]{ 33,  51,  51,  33}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_Z, 'Z'));
    keys_cap.add(new KeyboardButtonText("X", new int[]{ 51,  69,  69,  51}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_X, 'X'));
    keys_cap.add(new KeyboardButtonText("C", new int[]{ 69,  87,  87,  69}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_C, 'C'));
    keys_cap.add(new KeyboardButtonText("V", new int[]{ 87, 105, 105,  87}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_V, 'V'));
    keys_cap.add(new KeyboardButtonText("B", new int[]{105, 123, 123, 105}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_B, 'B'));
    keys_cap.add(new KeyboardButtonText("N", new int[]{123, 141, 141, 123}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_N, 'N'));
    keys_cap.add(new KeyboardButtonText("M", new int[]{141, 159, 159, 141}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_M, 'M'));
    keys_cap.add(new KeyboardButtonText("<", new int[]{159, 177, 177, 159}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '<'));
    keys_cap.add(new KeyboardButtonText(">", new int[]{177, 195, 195, 177}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '>'));
    keys_cap.add(new KeyboardButtonText("?", new int[]{195, 213, 213, 195}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '?'));

    keys_cap.add(k_ctrl);
    keys_cap.add(new KeyboardButtonText("au", new int[]{ 22,  44,  44,  22}, new int[]{ 64, 64, 80, 80}, 0, '\0'));
    keys_cap.add(new KeyboardButtonText("~", new int[]{ 44,  62,  62,  44}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '~'));
    keys_cap.add(new KeyboardButtonText("|", new int[]{ 62,  80,  80,  62}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '|'));
    keys_cap.add(new KeyboardButtonText(" ", new int[]{ 80, 167, 167,  80}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_SPACE, ' '));
    keys_cap.add(new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{4, 8, 7, 12, 12, 7, 8}, 
                                             new int[]{167, 185, 185, 167}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UP, '\0'));
    keys_cap.add(new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{12, 8, 9, 4, 4, 9, 8}, 
                                             new int[]{185, 203, 203, 185}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_DOWN, '\0'));
    keys_cap.add(new KeyboardButtonPoly(new int[]{5, 9, 8, 13, 13, 8, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                             new int[]{203, 221, 221, 203}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_LEFT, '\0'));
    keys_cap.add(new KeyboardButtonPoly(new int[]{13, 9, 10, 5, 5, 10, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                             new int[]{221, 239, 239, 221}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_RIGHT, '\0'));

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

  private Image drawKeyboard(Graphics g) {
    Image result = createImage(getSize().width, getSize().height);
    Graphics img = result.getGraphics();
    Iterator iter = current_keys.iterator();
    while(iter.hasNext()) {
      KeyboardButton key = (KeyboardButton)iter.next();
      key.paint_img(img);
    }
    return result;
  }
  
  public void paint(Graphics g) {
    Iterator iter = current_keys.iterator();
    if(current_keys == keys_low) {
      if(keys_low_img == null) keys_low_img = drawKeyboard(g);
      g.drawImage(keys_low_img, 0, 0, null);
    }
    else if(current_keys == keys_cap) {
      if(keys_cap_img == null) keys_cap_img = drawKeyboard(g);
      g.drawImage(keys_cap_img, 0, 0, null);
    }
    else if(current_keys == keys_nrs) {
      if(keys_nrs_img == null) keys_nrs_img = drawKeyboard(g);
      g.drawImage(keys_nrs_img, 0, 0, null);
    }
    while(iter.hasNext()) {
      KeyboardButton key = (KeyboardButton)iter.next();
      if(key.getPressed()) {
        key.paint(g);
      }
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

  public void sendKeyEvent(Component source, int id, int modifiers, int keyCode, char keyChar) {
    KeyEvent ke = new KeyEvent(source, id, System.currentTimeMillis(), modifiers, keyCode, keyChar);
    source.dispatchEvent(ke);
  }

  /*
  ** Mouse events.
  */
  
  public void mousePressed(MouseEvent event) {
    KeyboardButton key = getKey(event.getX(), event.getY());
    
    if(key != null) {
     
      if(key == k_caps) {
        if(key.getPressed()) {
          f_caps = false;
          current_keys = keys_low;
          key.setPressed(false);
        } 
        else {
          f_caps = true;
          current_keys = keys_cap;
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
          current_keys = keys_cap;
        }
        else {
          current_keys = keys_low;
        }
        f_num = false;
        if(k_shift.getPressed()) {
          f_shift = false;
          k_shift.setPressed(false);
          if(f_caps) current_keys = keys_cap; else current_keys = keys_low;
        }
      }
      else if(key == k_shift) {
        if(key.getPressed()) {
          key.setPressed(false);
          if(f_caps) current_keys = keys_cap; else current_keys = keys_low;
          f_shift = false;
        }
        else {
          key.setPressed(true);
          if(f_caps) current_keys = keys_low; else current_keys = keys_cap;
          f_shift = true;
        }
      }
      else {
        Component source = Component.getFocusComponent();
        lastPressed = key;
        lastSource = source;
        key.setPressed(true);
        if(source != null) {
          KeyEvent ke;
          sendKeyEvent(source, KeyEvent.KEY_PRESSED, 0, key.getKeyEvent(), key.getKeyChar());
          sendKeyEvent(source, KeyEvent.KEY_TYPED, 0, key.getKeyEvent(), key.getKeyChar());
        }
    
      }
      
      repaint();

    }
  }
  
  public void mouseReleased(MouseEvent event) {
    if(lastPressed != null) {
      lastPressed.setPressed(false);
      if(f_shift) {
        f_shift = false;
        k_shift.setPressed(false);
        if(f_caps) current_keys = keys_cap; else current_keys = keys_low;
      }
      sendKeyEvent(lastSource, KeyEvent.KEY_RELEASED, 0, lastPressed.getKeyEvent(), lastPressed.getKeyChar());
      lastPressed = null;
      repaint();
    }
  }
 
  private static final byte[] background1_data = {
 -119,   80,   78,   71,   13,   10,   26,   10,    0,    0,    0,   13,   73,   72,   68,   82,    0,    0,    0,  -16,    0,    0,    0,   81,    8,
    6,    0,    0,    0,  -20,   21,   43,  -39,    0,    0,    0,    6,   98,   75,   71,   68,    0,    0,    0,    0,    0,    0,   -7,   67,  -69,
  127,    0,    0,    0,    9,  112,   72,   89,  115,    0,    0,   11,   18,    0,    0,   11,   18,    1,  -46,  -35,  126,   -4,    0,    0,    0,
    7,  116,   73,   77,   69,    7,  -46,    1,   22,    9,   15,   34, -122,   25,   80,  -45,    0,    0,    5,  123,   73,   68,   65,   84,  120,
 -100,  -19,  -35,   49,  111,  -37,   70,   24,  -58,  -15,  -41,  -79,   70,   15,  105,    9,  120,  -10,  -22,  -81,   17,  116, -118,  -47,   41,
   48,   58,  -90,   99,  -48,   15,   83,  100,   42, -100,  -91,  -94, -112,   41,   21,   50,  -39,  -50,   84,  -28,   75,   36,  -79,   87,  -37,
 -101,   13,   41,   10,   96,  -81,  -58,  117, -120,  121,   58, -110,  114,   67,  -15, -114,  -28,  123,  -89,   -1,    3,    8,  -96,   20,  -35,
  -17,   61,  -32,   32, -111,   60,  -33,   69,   91,   34,   98, -124,   16,   18,  101,   70,  -59,  -63,   -3,   -3,  -67,   23,  -76,  -67,  -67,
  109, -113,  -37,   90,   24,   24,  -38, -115,  -95,  -37,   87,  -99,   45,   17,   49,    5,  -12,  -27,  -45,  -89,   86,  -40,  124,   54, -109,
  -29, -109,   19,   -7,  -13,  -11,  107,  105,  107,   97,   96,  104,   55, -122,  110,  -65,  -54,   25, -119, -120,   24,   99,  -28,  -84,  -27,
 -121,   87,   68,   36,  -53,   50,  123,   29,  -34,  -42,  -62,  -64,  -48,  110,   12,  -35,  126, -107,   99,   63,  -64,  -34,   55,  -62,  -58,
  -40, -114,  -75,  -74,   48,   48,  -76,   27,   67,  -73,  -81,   56,  -93,  -22,   11,  -19,   61,  -29,   62,  -63,  -64,   72,  -46,   24,  -70,
  125,  -43,    9,  118,    6,  118,   47,   13,  -38,   90,   24,   24,  -38, -115,  -95,  -37,   87,   29,  123,    6,   54,   30,  -33,    6,   15,
 -128,  115,  -40,  -46,  -62,  -64,  -48,  110,   12,  -35,  -66,  -30,  -40,   51,  -80,   -9,   37,  -76,  -37,  -87,  -74, -105,   55,   24,   24,
  -54, -115,  -95,  -37,   87,   29,   38,  -79,   48,   48,   98,  106,   95,  113, -104,  -60,  -62,  -64, -120,  -88,  125,  -43,   97,   18,   11,
    3,   35,  -94,  -10,   85, -121,   73,   44,   12, -116, -104,  -38,   87,   28,   38,  -79,   48,   48,   34,  106,   95,  117,  -98,   20,  -40,
  -41,  111,  -33,  -60,   60,   -4,  -61,  -70, -113,  113,  -98,  -37,  -50,  -76,  -75,   48,   48,  -76,   27,   67,  -73,   95,  -27,  108,   61,
  -68,   38,  127,  -68,  122,   37,   62,   -7,  -21,  -24,  -56,   30,  -73,  -75,   48,   48,  -76,   27,   67,  -73,  -81,   58,  -10,    3,   76,
    8, -119,   47,  118,   18,  -21,  -18,  -18,  -82, -109,    2,   59,   59,   59,  -10,  -40,  -89,    6,   14,   78,   42,   78,   72,   99,   75,
   68,  -52,  -19,  -19,  -83, -120, -120,  -41, -106,  -62,   85, -103,  -51,  -25,  114,  124,  114,   34,   71,  111,  -34, -120,   79,   13,   28,
 -100,   84, -100,  -48,  -58,  114,   63,  -16,  -25,  -49,  107,   33,   77, -110,  101,   89,  -23,  -90,  -67,  109,   13,   28, -100,   84, -100,
  -48,   70,  -80, -107,   88, -113,   37,  -12,  -54,   19,   28, -100,  -40,  -99, -112,  -58,  114,   37,   86,   87,   31,   96,  -89,  -96,   79,
   13,   28, -100,   84, -100, -112,  -58,  -88,   -6,   66,  -16,   56,   29,  -12,  -86, -127, -125, -109, -118,   19,  -48,    8,  -74,   18,  -21,
  -47,   58,  -74,  -98,   95,   13,   28, -100,   84, -100, -112,   70,  -72,  -19, -124, -113,   86,   90,  -34,  -80, -121,   -8,  -26,  -61,  -63,
 -119,  -34,    9,  104,   48, -119, -123, -125,  -45,  -77,  -61,   36,   22,   14,   78,  -60,   78,   72, -125,   73,   44,   28, -100,  -66,   29,
   38,  -79,  112,  112,  -30,  117,   66,   26,   76,   98,  -31,  -32,  -12,  -19,    4,   52,  -98,   20,  -49,   23, -117,  -59,   -9,   23,    3,
   62,  -58,  -29,  113,  -23,   26,  -65,  109,   13,   28, -100,   84, -100,  -48, -122,  -35,   78,   -8,   -5,  -53, -105,   63,   -8,  -56,  -73,
   75,   62, -103,  -40,   99,  -97,   26,   56,   56,  -87,   56,   33,   13,  -10,    3,   19,   18,  113,  -20,   44,  -12,  -51,  -51,   77,   47,
    5,  119,  119,  119,  -19,  -79,   79,   77,   28, -100,   77,  118, -118,   -9,  111, -119, -120,  -71,  -66,  -66,   22,   17,  -23,  100,   75,
  -95, -101,  -30,  119,   77,   39,  111,  -33, -118,   79,   77,   28, -100,   77,  118,  -36,   -9,  -37,   51,  112,  -24,  -51,   -4,  -85, -110,
  101,   89,  -23,    6,  -34,  -21,   -9,   81,  113,  112,   54,  -44,  113,  -33,  -33,   -3,  -97, -111,   42,   41,  -22,   -8,  -42,  -60,  -63,
  -39,  100,  -89,  120,   79,  -72,   -1,  -40,  -67,  105, -100,   58,   94,   53,  113,  112,   54,  -39,  -87,  -98, -127,  -91,  -89,   15,  -80,
   -5,   77,  -29,   83,   19,    7,  103, -109,  -99,  -46,   25,  -72,  -49,   75,  -24,  -94,  115,   42,   86,  -60,  -32,  -32,  -60,  -22,  -72,
  103,   96,   -9, -123,  -82,   83,  -70,   60,  -16,   -7,  -58,  -62,  -63,  -39,   96, -121,   73,   44,   28, -100, -120,  -99,  -46,   37,  116,
  -47,  -80, -105,  -60,   62,  121, -128, -125,  -93,  -63,   97,   18,   11,    7,   39,   94,  -89,  116,    6,  102,   18,   11,    7,   39,   50,
 -121,   73,   44,   28, -100,  120,  -99,  -30,   -3,  -53,  -33,    7,   94,   44,  -60, -120,  116,   -6,   -8,   59,  -49,   75, -105,   10,  109,
  107,  -30,  -32,  -92,  -20,   52,  109,   39,  -30,  108,   39,   -4,  -19,  -16,   80,   -6,  -56,  -69,  -23,  -44,   30,   -5,  -44,  -60,  -63,
   73,  -47,  -71,  -70,  -70, -110,   47,   13,  -42,   84,  -25,  121,   46,  -17,  -90,   83,  -10,    3,   19,  -94,   37, -105, -105, -105,   34,
  -46,  108,   83,  -60,  120,   50, -111,  127,  -90,  -45,  -27,   61,  -16,  -59,  -59,   69,  103,   29,   -5,  -65,  -20,  -19,  -19,  -39,   99,
  -97,   62,  -32,  -32,  -60,  -18,   24,  -77,  -58,  -81,   21,  -70, -109,   88,   69,  -95,   62,  -74,   20,  -70, -103,  -51,  -25,  114,   -8,
  -30, -123,   76,  -33,  -65,   -9,  -22,    3,   14,   78,   74,   78, -109,  -44,   86,   98,  117,  -67, -103,  127,   85,  -78,   44,   43,  -35,
  -68,   -5,   -4,  110,   43,   14,   78,   42,   78, -109,   20,  117,   -6,   95, -119,   85,  -21, -119,  113,   14,   61,   -6, -128, -125, -109,
 -110,  -45,  -80,   78,  -17,   43,  -79,  106,   -3,  -80,   -3,  -15,  -21,    3,   14,   78,   74,   78,  -45,   58,   -3,  -81,  -60,  -86,  -11,
  -60,   72, -112,   62,  -32,  -32,  -92,  -28,   52,  -84,  -45,   -5,   74,  -84,  122,   63, -116,   -5,    4,    7,    7,  103, -115,   58, -125,
  -97, -127,  -35,   75,   14,  -97,   62,  -32,  -32,  -92,  -28,   52,  -83,  -61,   36,   22,   14, -114,   70,  -89,   97,  -99,  -31,   39,  -79,
 -100,  123, -122,   16, -105,   46,   56,   56,   41,   56,   77,  -21,   12,  126,    9,   93,   36,   84,   31,  112,  112,   82,  114,  126,   20,
   38,  -79,  112,  112,   20,   58,   77,  -21,   12,  126,    6,  -42,   54,  121, -128, -125,  -93,  -63,  105,   90,  -57,  -98, -127,  -65,   46,
   22,  -14,  -45,  -45,  -89,   29, -106,  -84,   39,  -49,  -13,  -46,  -13,  -74,  125,  -64,  -63,   73,  -55,   89,  -89, -114,  -35,   78,   -8,
  -21,  -63,   65,  -16,   66,   77,  114,  124,  122,  106, -113,  125,   -6, -128, -125, -109, -110,  -45,  -76,   14,   -5, -127,    9, -119,   56,
  -10,   18,   -6,   -4,   -4,   60,   24,  -70,  -65,  -65,  111, -113,   53,  -70,  -38,   28,   82, -113,  -74,   49,  -46,  -22, -116,   68,   68,
  -50,  -50,  -50,  -60, -104,   48,  -37,  -97,  -26,  -77, -103,   28,   60,  127,   46,  -89,   31,   62,  -88,  116,  -75,   57,  -92,   30,  109,
   99,  -92,  -39,  -79,  103,  -32,   38,   -1,   15,   79, -109,   -4, -100,  101,  -91,  -23,  115,  109,  -82,   54, -121,  -44,  -93,  109, -116,
   52,   59,  -99,  -84,  -60,  114,  -89,  -46,   53,  -70,  -38,   28,   82, -113,  -74,   49,  -46,  -22,  116,  -13,  119,   96,  103,   57, -103,
   74,   87, -101,   67,  -22,  -47,   54,   70,   74,  -99,   78,   86,   98,  117,  -75,   26,   69,  -37,  106, -103,  -66,   86,  -35,  108,   98,
  -76, -115, -111,   86,  -89, -109,   51,  112,   87,  -85,   81,  -76,  -83, -106,  -23,  107,  -43,  -51,   38,   70,  -37,   24,  105,  117,  -70,
  -39,   78,  -24,   88,   42,   93,  109,   14,  -87,   71,  -37,   24,   41,  117, -104,  -60,   82,  -32, -112,  122,  -76, -115, -111,   86, -121,
   73,   44,   13,   14,  -87,   71,  -37,   24,   41,  117, -104,  -60,   82,  -32, -112,  122,  -76, -115, -111,   86, -121,   73,   44,    5,   14,
  -87,   71,  -37,   24,  105,  117, -104,  -60,  -46,  -32, -112,  122,  -76, -115, -111,   82,  -57,   -2,   62,  -16,   98,  -79,   -8,   -2,  -94,
  -25,   99,   92,   -7,  -83,   83,  109,  -82,   54, -121, -121,   -2,   49,  -46,  -20,  -40,  -19, -124,  -65,   60,  123,  -42,   -2,  -37,  -96,
 -110,  127,   63,  126,  -76,  -57,   26,   93,  109,   14,  -87,   71,  -37,   24,  105,  117,  -40,   15,   76,   72,  -60,   -7,   15,  -34,  -95,
  -38,  -23,  -96,   53,  126,   96,    0,    0,    0,    0,   73,   69,   78,   68,  -82,   66,   96, -126 };

  private static final byte[] background2_data = {
 -119,   80,   78,   71,   13,   10,   26,   10,    0,    0,    0,   13,   73,   72,   68,   82,    0,    0,    0,  -16,    0,    0,    0,   81,    8,
    2,    0,    0,    0,   99,  119,  -68, -114,    0,    0,    0,    6,   98,   75,   71,   68,    0,    0,    0,    0,    0,    0,   -7,   67,  -69,
  127,    0,    0,    0,    9,  112,   72,   89,  115,    0,    0,   11,   18,    0,    0,   11,   18,    1,  -46,  -35,  126,   -4,    0,    0,    0,
    7,  116,   73,   77,   69,    7,  -46,    1,   22,    9,   36,   18,  -42,  -80,  -99,   22,    0,    0,    5,  -86,   73,   68,   65,   84,  120,
 -100,  -19,  -99,  -49,  110,   20,   73,   12,  -58,   11,  -44, -109,  -89,    9,   92, -109,  -27, -102,  -64,   17,    2,  -41,   60,   25,   11,
  -41,   60,  -64,  114,  -49,    3,  -28,   21, -110,  -45,  116,   66, -108,  -37,   76,   38,  -71,   48, -111,   50,   28,  -90,   51,   44,  -40,
   64,   -7,   79,   87, -107,   71,  -33,  -89, -107,   86,   42,  -21,  -25,  -78,   45,  112, -105,  -69,   41,   77,   74,   16,  -76,   69,  122,
  -74,   -2,  -33,  -29,  -29,  -29,   -3,   98, -111,  -55,  -12,  125,  -65,   -5,  -14,  -91,    8,   12, -127,   80, -119,  -54,  -94,   11,  -64,
  -99,  -91, -118, -104, -123, -102,  -19,  -42,  112,   74,  105,   58,  -99,  102,  -62,   15,  -53,  -27,  -15,  -15,  -15,  -55,  -55,   73,   62,
  -40,   62,   66,   77,  -46,  -78,  -24,    2,  -16,  101,  -87,   41,   98,   22,   22,  -74,   75,   41,  -35,  -35,  -34,  -10,   18,  120,   50,
 -103,   92,   94,   94, -118,  -64,  -10,   17,   42,  105,   89,  116,    1,   -8,  -78,   84,   17,  -77,  -80,  -80,   93,   74,  105,  -75,   90,
  -83,   68,  -12, -109,   20,   96,  -77, -120,  -81, -109,   90,  108,   59, -111,  -44,   98,  -69,  -75, -125,   36,  -12,   48,  -20,   40,    1,
   91,   71,   56, -125,  -76,   44,  -70,    0,   60,   89,  -50,   16,   47,   11,    3,  -37,  -91, -108,   86,   41,   41,   90,  -96,   20,  108,
   28,   97,  -42,  -27,  101,  -47,    5,  -32,  -56,   50,  -21,    1,  -77,  -80,  -80,   79,   71,   14,   85, -113,   87, -128,  -51,   34,  -66,
   78,  106,  -79,  -19,   68,   82, -117,  -59, -111,   99,  -69,   30,  -42, -100,   33,   94,   22,  -27, -113,   28, -125,   11,  -59,   89,  -91,
   85,  -60,  -41,   73,   45,  -74,  -99,   72,  106,  -79,  -24,  -48,  -37,  -43,  -37,   56,   67,  -68,   44,   48,   20,  -38,   17,  102,   61,
  -32,   72,  -60,  -84,    7,  -52,    2,   67,  -95,    3,  -30,  -21,    4,   67,   33, -122,  -62,  -38,    8,  103,    8,   -9,  -64,  101,   13,
  -15,  -78,  -64,   80,  104,   71,  124,  -99,   96,   40,  -60,   80,   88,   27,  -31,   12,  -31,   -6,   19,  107, -120, -105,    5,   58,  -76,
   29,  -15,  117, -126,   14,   93,  -77,   67,  -21,  -90,   46,   41,  -40,   56,  -62,  -82,   43,   70,   19,   69,    0, -114,   44,  -69,   30,
   46,   11,   11,  -37,  -91, -108,   -6,  -23,  116,   -7,  -16,  -80,   51, -103,  100, -110,   -1,  126,   -2,   60, -101,  -51,   68,   96,   -5,
    8, -107,  -76,   44,  -70,    0,  124,   89,  -86, -120,   89,   88,  -40,  -31,  -58,  -54,   -5,  -93,  -93,  -85,  -81,   95,   51,  -31,   -7,
  124,  126,  113,  113,   33,    2,   67,   32,   84,  -94,  -78,  -24,    2,  112,  103,  -87,   34,  102,  -31,   91,    1,    8, -118,  -86,  -95,
   67,   47,   22,   11,  -47,  -99,  -62,   -3,   87,  -81,   68,   96,    8, -124,   74,   84,   22,   93,    0,  -18,   44,   85,  -60,   44,  -44,
  108,  -73, -122, -109,  -16,   78,  -31,  -69,  -73,  111,   -1,   -5,  -14,   37,   31,  108,   31,  -95,   38,  105,   89,  116,    1,   -8,  -78,
  -44,   20,   49,   11,   11,  -37,  -91, -108,  -18,  -26,  -13,  -66,  -17,  -13,  -31,  -55,  100,  114,  121,  117,   37,    2,  -37,   71,  -88,
  -92,  101,  -47,    5,  -32,  -53,   82,   69,  -52,  -62,  -62,  -30,   61,  -12,   40,   78,  -16,   30,   26,   95,   10,  107,   35, -100,   33,
  -36,  119,   50,  -42,   16,   47,   11,  124,   41,  -76,   35,  -66,   78,  -48,  -95,  -15,  -91,  -80,   50,  -62,  -82, -121,   -5,   78,  -58,
  -82, -121,  -53,  -62,   -6,  -91,   16,   71, -114,  -33,   25,  -62,   61,  112,   89,   67,  -68,   44,  112,  -28,  -80,   35,  -66,   78,  112,
  -28,  -64,   80,   88,   27,  -31,   12,  -31,   -6,   19,  107, -120, -105,    5,   58,  -76,   29,  -15,  117, -126,   14, -115,  -95,  -80,   50,
  -62,  -82, -121,   27, -119,  -40,  -11,  112,   89,   96,   40,  -12,   64,   56,   67,  -72,    7,   46,  107, -120, -105,    5, -114,   28,  118,
  -60,  -41,    9, -114,   28,   24,   10,  107,   35, -100,   33,   92,  127,   98,   13,  -15,  -78,   64, -121,  -74,   35,  -66,   78,  -48,  -95,
  -47,  -95,  107,   35, -100,   33,   92,  127,   98,   13,  -15,  -78,   48,  118,  -24,  -87,  -10,   78,   97,   62,  -40,   62,   66,   37,   45,
 -117,   46,    0,   95, -106,   42,   98,   22,   22,  118,  -72,  -79,  -14,   -6,  -16,   48,   -1,   10,  -41,  108,   54,  -69,  -71,  -71,   17,
 -127,   33,   16,   42,   81,   89,  116,    1,  -72,  -77,   84,   17,  -77,  -16,  -83,    0,    4,   69,  -43,  -48,  -95,  -81,  -81,  -81,   69,
  119,   10,   15,  -33,  -68,   17, -127,   33,   16,   42,   81,   89,  116,    1,  -72,  -77,   84,   17,  -77,   80,  -77,  -35,   26,   78,   41,
  -27,   -1, -112,  -42,  114,  -71,   60,   56,   56,   56,   61,   61,  -51,    7,  -37,   71,  -88,   73,   90,   22,   93,    0,  -66,   44,   53,
   69,  -52,  -62,  -62,  106,  -17,   20,  -82,  127,   79,   78,  116,  117,  -81,  109, -124,   74,  121,  -77,   77,   24, -128,   47,   75,   21,
   49,   11,   11, -117,   -9,  -48,  -93,   56,  -63,  123,  104,  -68, -121,  -82, -115,  112, -122,  112,  111,   97,   89,   67,  -68,   44,  -16,
  -91,  -48, -114,   -8,   58,   65, -121,   70, -121,  -82, -115,  112, -122,  112,   -3, -119,   53,  -60,  -53,  -94,  124, -121,   30,   -2,  -35,
  -86,    4,  108,   28,   97,  -42,   85,  125,   66,   17, -128,   35,  -53,  -84,    7,  -52,  -62,  -62,  -30,   71, -125,   70,  113,   82, -117,
  109,   39, -110,   90,   44, -114,   28,  -37,  -11,  -80,  -26,   12,  -15,  -78,  -64,   80,  104,   71,  124,  -99,   96,   40,  -60,   80,   88,
   27,  -31,   12,  -31,   -6,   19,  107, -120, -105,    5, -122,   66,   59,  -62,  -84,    7,   28, -119, -104,  -11, -128,   89,   96,   40,  116,
   64,  124,  -99,   96,   40,  -60,   80,   88,   27,  -31,   12,  -31,   30,  -72,  -84,   33,   94,   22,   24,   10,  -19, -120,  -81,   19,   12,
 -123,   24,   10,  107,   35, -100,   33,   92,  127,   98,   13,  -15,  -78,   48,  -80,  -49,   83,   74,  -45,  -66,   -1,  -74,   92,  -82,  -98,
   -2,  102,   -4,  -11,  -65, -113,  -97,   62,   13,  119,  -65,  -78,  -63,  -10,   17,   42,  105,   89,  116,    1,   -8,  -78,  -37, -111, -123,
 -123,   29,  110,  -84,   -4,  -77,  -65,   47,  -70,   83,  120,  127,  127,   47,    2,   67,   32,   84,  -94,  -78,  -24,    2,  112,  103,  -87,
   34,  102,  -31,   91,    1,    8, -118,  -86,  -95,   67,  -97,  -97,  -97, -117,  -18,   20,   30,  125,   -8,   32,    2,   67,   32,   84,  -94,
  -78,  -24,    2,  112,  103,  -87,  -92,  -34,  -68,  -94,   42,  -71,  -29, -122,  -22,  -42,   88,   18,  -34,   41,  -36,  -37,  -37,   59,   59,
   59,  -53,    7,  -37,   71,  -88,   73,   90,   22,   93,    0,  -66,   44,   53,   41,  -68,  121,   69,   85,  114,  -57,   13,  -43,  -91, -108,
  -18,  110,  111,  101,  119,   10,  119,  118, -122,  -69,   95,  -39,   96,   -5,    8, -107,  -76,   44,  -70,    0,  124,   89,   42, -123,   55,
  -81,  -88,   74,  -18,  -72,  -95,  -16,  -91,  112,   20,   39,  -37,  -15,  -91,  112,   12,   63,   99,  -17, -120,   -9,  -48,  -37,  -11,    6,
 -105,   51,   40,  -68,  121,   69,   85,  114,   71,  124,   41,   28,  -47,   73,   45,  118,   60,  111,  -66,   81, -115,  -73,   35,   58,   52,
   58,  -12, -120,   81, -107,  -36,  -47,  -44,  -95,  -41,  -80,    8,  108,   28,   97,  -42,   85,   29,   66,   17, -128,   35,  -53,  -84, -101,
   59,  -85,   37,  -86, -110,   59,   -2,  -17,   15,   52, -122,   66,  111,   39,   24,   10,  107,  -19, -120,   35,    7, -114,   28,   35,   70,
   85,  114,   71,   12, -123,   35,   58,  -63,   80,   88,  107,  -57,  -95,   67,   43,  -98,  -23,   82,  -80,  117, -124,   53,   24,  -70,  108,
   29, -106,   53,   56,  117,  -24,   98,   71,   14,  -35, -114,   63,  117,  104,  -35, -111,   67,    4,   54, -114,   48,  -21,   18,   39, -106,
    0,   28,   89,  102,   93,  -27,  -51,   43,  -86, -110,   59,   -2,   60,   20,  106,   55,   22,   31,    6,   90,   69,  124,  -99,  -44,   98,
  -57,  -13,   86,  122,   36,  -44,  -18, -120,  -95,   16,   67,  -31, -120,   81, -107,  -36,  -47,   52,   20,  110,   30,   10,   -7,   96,  -29,
    8,  -77,  -82,   26,   74,   20,    1,   56,  -78,  -52,  -70,  121, -104,  -77,   68,   85,  114,  -57,   31,  127,  -96,  -11,  -25,  125,    5,
  -40,   44,  -30,  -21,  -92,   22,   59,  -98,  -73, -126,   67,  -95,  101,   71,   -4,   78,  -31,   86,   -3,  -62,   31, -107,  -62, -101,   87,
   84,   37,  119,   -4,  -11,  119,   10,   95,  -20,  -18,  -26,   95,  -31, -102,  -49,  -25, -101,  -65,   58, -103,   96,    8, -124,   74,   84,
   22,   93,    0,  -18,   44, -107,  -44, -101,   87,   84,   37,  119,   -4,  115,    5,   32,   40,  -86,  -66,    3, -108,   98,   -1, -104,   54,
   86,  -65,  -41,    0,    0,    0,    0,   73,   69,   78,   68,  -82,   66,   96, -126 };

}

