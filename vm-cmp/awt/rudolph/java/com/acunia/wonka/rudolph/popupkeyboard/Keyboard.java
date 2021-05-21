/**************************************************************************
* Copyright (c) 2003 by Punch Telematix. All rights reserved.             *
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

package com.acunia.wonka.rudolph.popupkeyboard;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Keyboard extends Window implements MouseListener {

  public static final boolean POPUPKEYBOARD = false;

  private KeyboardButton keys_low[];
  private KeyboardButton keys_cap[];
  private KeyboardButton keys_nrs[];

  private KeyboardButton current_keys[];
  private Vector pressed_keys;
  
  private Image keys_low_img;
  private Image keys_cap_img;
  private Image keys_nrs_img;

  private KeyboardButton k_123_1;
  private KeyboardButton k_123_2;
  private KeyboardButton k_shift;
  private KeyboardButton k_caps;
  private KeyboardButton k_ctrl;
  private KeyboardButton k_enter;
  private KeyboardButton k_enter_num;
  private KeyboardButton lastPressed = null;

  private boolean f_caps = false;
  private boolean f_shift = false;
  private boolean f_num = false;
  private boolean f_ctrl = false;

  private int kbdWidth = 360;
  private int kbdHeight = 200;
  private int textfieldheight = 40;

  private TextField textfield;
  private TextComponent target;

  public Keyboard(Frame owner) {
    super(owner);
    buildKbd();
    addMouseListener(this);

    current_keys = keys_low;  
  }

  private void buildKbd() {
    keys_low = new KeyboardButton[60];
    keys_cap = new KeyboardButton[60];
    keys_nrs = new KeyboardButton[47];
    pressed_keys = new Vector();

    k_123_1 = new KeyboardButtonText("123", new int[]{  0,  19,  19,   0}, new int[]{ 0,  0, 16, 16}, 0, '\0');
    k_123_2 = new KeyboardButtonText("123", new int[]{  0,  19,  19,   0}, new int[]{ 0,  0, 20, 20}, 0, '\0');
    k_shift = new KeyboardButtonText("Shift", new int[]{  0,  33,  33,   0}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_SHIFT, '\0');
    k_caps = new KeyboardButtonText("CAP", new int[]{  0,  29,  29,   0}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_CAPS_LOCK, '\0');
    k_ctrl = new KeyboardButtonText("Ctrl", new int[]{  0,  22,  22,   0}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_CONTROL, '\0');
    
    k_enter = new KeyboardButtonPoly(new int[]{-6, -2, -3, 4, 4, 6, 6, -3, -2}, new int[]{24, 20, 23, 23, 21, 21, 25, 25, 28}, 
                                     new int[]{227, 239, 239, 213, 213, 227}, new int[]{ 32, 32, 64, 64, 48, 48}, KeyEvent.VK_ENTER, '\n');
    k_enter_num = new KeyboardButtonPoly(new int[]{2, 6, 5, 12, 12, 14, 14, 5, 6}, new int[]{10, 6, 9, 9, 7, 7, 11, 11, 14}, 
                                         new int[]{ 221, 239, 239, 221}, new int[]{60, 60, 80, 80}, KeyEvent.VK_ENTER, '\n');
    /*
    ** Normal keyboard.
    */

    keys_low[ 0] = k_123_1;
    keys_low[ 1] = new KeyboardButtonText("1", new int[]{ 19,  36,  36,  19}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_1, '1');
    keys_low[ 2] = new KeyboardButtonText("2", new int[]{ 36,  53,  53,  36}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_2, '2');
    keys_low[ 3] = new KeyboardButtonText("3", new int[]{ 53,  70,  70,  53}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_3, '3');
    keys_low[ 4] = new KeyboardButtonText("4", new int[]{ 70,  87,  87,  70}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_4, '4');
    keys_low[ 5] = new KeyboardButtonText("5", new int[]{ 87, 104, 104,  87}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_5, '5');
    keys_low[ 6] = new KeyboardButtonText("6", new int[]{104, 121, 121, 104}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_6, '6');
    keys_low[ 7] = new KeyboardButtonText("7", new int[]{121, 138, 138, 121}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_7, '7');
    keys_low[ 8] = new KeyboardButtonText("8", new int[]{138, 155, 155, 138}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_8, '8');
    keys_low[ 9] = new KeyboardButtonText("9", new int[]{155, 172, 172, 155}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_9, '9');
    keys_low[10] = new KeyboardButtonText("0", new int[]{172, 189, 189, 172}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_0, '0');
    keys_low[11] = new KeyboardButtonText("-", new int[]{189, 205, 205, 189}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '-');
    keys_low[12] = new KeyboardButtonText("=", new int[]{205, 221, 221, 205}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '=');
    keys_low[13] = new KeyboardButtonPoly(new int[]{4, 9, 9, 14, 14, 9, 9}, new int[]{8, 3, 6, 6, 10, 10, 13}, 
                                          new int[]{221, 239, 239, 221}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_BACK_SPACE, '\0');

    keys_low[14] = new KeyboardButtonText("Tab", new int[]{  0,  25,  25,   0}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_TAB, '\0');
    keys_low[15] = new KeyboardButtonText("q", new int[]{ 25,  43,  43,  25}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Q, 'q');
    keys_low[16] = new KeyboardButtonText("w", new int[]{ 43,  61,  61,  43}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_W, 'w');
    keys_low[17] = new KeyboardButtonText("e", new int[]{ 61,  79,  79,  61}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_E, 'e');
    keys_low[18] = new KeyboardButtonText("r", new int[]{ 79,  97,  97,  79}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_R, 'r');
    keys_low[19] = new KeyboardButtonText("t", new int[]{ 97, 115, 115,  97}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_T, 't');
    keys_low[20] = new KeyboardButtonText("y", new int[]{115, 133, 133, 115}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Y, 'y');
    keys_low[21] = new KeyboardButtonText("u", new int[]{133, 151, 151, 133}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_U, 'u');
    keys_low[22] = new KeyboardButtonText("i", new int[]{151, 169, 169, 151}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_I, 'i');
    keys_low[23] = new KeyboardButtonText("o", new int[]{169, 187, 187, 169}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_O, 'o');
    keys_low[24] = new KeyboardButtonText("p", new int[]{187, 205, 205, 187}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_P, 'p');
    keys_low[25] = new KeyboardButtonText("[", new int[]{205, 222, 222, 205}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_OPEN_BRACKET, '[');
    keys_low[26] = new KeyboardButtonText("]", new int[]{222, 239, 239, 222}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_CLOSE_BRACKET, ']');
    
    keys_low[27] = k_caps;
    keys_low[28] = new KeyboardButtonText("a", new int[]{ 29,  47,  47,  29}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_A, 'a');
    keys_low[29] = new KeyboardButtonText("s", new int[]{ 47,  65,  65,  47}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_S, 's');
    keys_low[30] = new KeyboardButtonText("d", new int[]{ 65,  83,  83,  65}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_D, 'd');
    keys_low[31] = new KeyboardButtonText("f", new int[]{ 83, 101, 101,  83}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_F, 'f');
    keys_low[32] = new KeyboardButtonText("g", new int[]{101, 119, 119, 101}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_G, 'g');
    keys_low[33] = new KeyboardButtonText("h", new int[]{119, 137, 137, 119}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_H, 'h');
    keys_low[34] = new KeyboardButtonText("j", new int[]{137, 155, 155, 137}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_J, 'j');
    keys_low[35] = new KeyboardButtonText("k", new int[]{155, 173, 173, 155}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_K, 'k');
    keys_low[36] = new KeyboardButtonText("l", new int[]{173, 191, 191, 173}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_L, 'l');
    keys_low[37] = new KeyboardButtonText(";", new int[]{191, 209, 209, 191}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_SEMICOLON, ';');
    keys_low[38] = new KeyboardButtonText("'", new int[]{209, 227, 227, 209}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_QUOTE, '\'');
    keys_low[39] = k_enter;

    keys_low[40] = k_shift;
    keys_low[41] = new KeyboardButtonText("z", new int[]{ 33,  51,  51,  33}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_Z, 'z');
    keys_low[42] = new KeyboardButtonText("x", new int[]{ 51,  69,  69,  51}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_X, 'x');
    keys_low[43] = new KeyboardButtonText("c", new int[]{ 69,  87,  87,  69}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_C, 'c');
    keys_low[44] = new KeyboardButtonText("v", new int[]{ 87, 105, 105,  87}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_V, 'v');
    keys_low[45] = new KeyboardButtonText("b", new int[]{105, 123, 123, 105}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_B, 'b');
    keys_low[46] = new KeyboardButtonText("n", new int[]{123, 141, 141, 123}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_N, 'n');
    keys_low[47] = new KeyboardButtonText("m", new int[]{141, 159, 159, 141}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_M, 'm');
    keys_low[48] = new KeyboardButtonText(",", new int[]{159, 177, 177, 159}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_COMMA, ',');
    keys_low[49] = new KeyboardButtonText(".", new int[]{177, 195, 195, 177}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_PERIOD, '.');
    keys_low[50] = new KeyboardButtonText("/", new int[]{195, 213, 213, 195}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_SLASH, '/');

    keys_low[51] = k_ctrl;
    keys_low[52] = new KeyboardButtonText("au", new int[]{ 22,  44,  44,  22}, new int[]{ 64, 64, 80, 80}, 0, '\0');
    keys_low[53] = new KeyboardButtonText("`", new int[]{ 44,  62,  62,  44}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_BACK_QUOTE, '`');
    keys_low[54] = new KeyboardButtonText("\\", new int[]{ 62,  80,  80,  62}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_BACK_SLASH, '\\');
    keys_low[55] = new KeyboardButtonText(" ", new int[]{ 80, 167, 167,  80}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_SPACE, ' ');
    keys_low[56] = new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{4, 8, 7, 12, 12, 7, 8}, 
                                          new int[]{167, 185, 185, 167}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UP, '\0');
    keys_low[57] = new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{12, 8, 9, 4, 4, 9, 8}, 
                                          new int[]{185, 203, 203, 185}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_DOWN, '\0');
    keys_low[58] = new KeyboardButtonPoly(new int[]{5, 9, 8, 13, 13, 8, 9}, new int[]{8, 4, 7, 7, 9, 9, 12},
                                          new int[]{203, 221, 221, 203}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_LEFT, '\0');
    keys_low[59] = new KeyboardButtonPoly(new int[]{13, 9, 10, 5, 5, 10, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                          new int[]{221, 239, 239, 221}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_RIGHT, '\0');

    /*
    ** Keyboard with shift pressed in.
    */
    
    keys_cap[ 0] = k_123_1;
    keys_cap[ 1] = new KeyboardButtonText("!", new int[]{ 19,  36,  36,  19}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '!');
    keys_cap[ 2] = new KeyboardButtonText("@", new int[]{ 36,  53,  53,  36}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '@');
    keys_cap[ 3] = new KeyboardButtonText("#", new int[]{ 53,  70,  70,  53}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '#');
    keys_cap[ 4] = new KeyboardButtonText("$", new int[]{ 70,  87,  87,  70}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '$');
    keys_cap[ 5] = new KeyboardButtonText("%", new int[]{ 87, 104, 104,  87}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '%');
    keys_cap[ 6] = new KeyboardButtonText("^", new int[]{104, 121, 121, 104}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '^');
    keys_cap[ 7] = new KeyboardButtonText("&", new int[]{121, 138, 138, 121}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '&');
    keys_cap[ 8] = new KeyboardButtonText("*", new int[]{138, 155, 155, 138}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '*');
    keys_cap[ 9] = new KeyboardButtonText("(", new int[]{155, 172, 172, 155}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '(');
    keys_cap[10] = new KeyboardButtonText(")", new int[]{172, 189, 189, 172}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, ')');
    keys_cap[11] = new KeyboardButtonText("_", new int[]{189, 205, 205, 189}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '_');
    keys_cap[12] = new KeyboardButtonText("+", new int[]{205, 221, 221, 205}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_UNDEFINED, '+');
    keys_cap[13] = new KeyboardButtonText("Del", new int[]{221, 239, 239, 221}, new int[]{ 0,  0, 16, 16}, KeyEvent.VK_DELETE, '\0');

    keys_cap[14] = new KeyboardButtonText("Tab", new int[]{  0,  25,  25,   0}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_TAB, '\0');
    keys_cap[15] = new KeyboardButtonText("Q", new int[]{ 25,  43,  43,  25}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Q, 'Q');
    keys_cap[16] = new KeyboardButtonText("W", new int[]{ 43,  61,  61,  43}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_W, 'W');
    keys_cap[17] = new KeyboardButtonText("E", new int[]{ 61,  79,  79,  61}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_E, 'E');
    keys_cap[18] = new KeyboardButtonText("R", new int[]{ 79,  97,  97,  79}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_R, 'R');
    keys_cap[19] = new KeyboardButtonText("T", new int[]{ 97, 115, 115,  97}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_T, 'T');
    keys_cap[20] = new KeyboardButtonText("Y", new int[]{115, 133, 133, 115}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_Y, 'Y');
    keys_cap[21] = new KeyboardButtonText("U", new int[]{133, 151, 151, 133}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_U, 'U');
    keys_cap[22] = new KeyboardButtonText("I", new int[]{151, 169, 169, 151}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_I, 'I');
    keys_cap[23] = new KeyboardButtonText("O", new int[]{169, 187, 187, 169}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_O, 'O');
    keys_cap[24] = new KeyboardButtonText("P", new int[]{187, 205, 205, 187}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_P, 'P');
    keys_cap[25] = new KeyboardButtonText("{", new int[]{205, 222, 222, 205}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '{');
    keys_cap[26] = new KeyboardButtonText("}", new int[]{222, 239, 239, 222}, new int[]{ 16, 16, 32, 32}, KeyEvent.VK_UNDEFINED, '}');
    
    keys_cap[27] = k_caps;
    keys_cap[28] = new KeyboardButtonText("A", new int[]{ 29,  47,  47,  29}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_A, 'A');
    keys_cap[29] = new KeyboardButtonText("S", new int[]{ 47,  65,  65,  47}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_S, 'S');
    keys_cap[30] = new KeyboardButtonText("D", new int[]{ 65,  83,  83,  65}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_D, 'D');
    keys_cap[31] = new KeyboardButtonText("F", new int[]{ 83, 101, 101,  83}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_F, 'F');
    keys_cap[32] = new KeyboardButtonText("G", new int[]{101, 119, 119, 101}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_G, 'G');
    keys_cap[33] = new KeyboardButtonText("H", new int[]{119, 137, 137, 119}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_H, 'H');
    keys_cap[34] = new KeyboardButtonText("J", new int[]{137, 155, 155, 137}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_J, 'J');
    keys_cap[35] = new KeyboardButtonText("K", new int[]{155, 173, 173, 155}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_K, 'K');
    keys_cap[36] = new KeyboardButtonText("L", new int[]{173, 191, 191, 173}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_L, 'L');
    keys_cap[37] = new KeyboardButtonText(":", new int[]{191, 209, 209, 191}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, ':');
    keys_cap[38] = new KeyboardButtonText("\"", new int[]{209, 227, 227, 209}, new int[]{ 32, 32, 48, 48}, KeyEvent.VK_UNDEFINED, '\\');
    keys_cap[39] = k_enter;

    keys_cap[40] = k_shift;
    keys_cap[41] = new KeyboardButtonText("Z", new int[]{ 33,  51,  51,  33}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_Z, 'Z');
    keys_cap[42] = new KeyboardButtonText("X", new int[]{ 51,  69,  69,  51}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_X, 'X');
    keys_cap[43] = new KeyboardButtonText("C", new int[]{ 69,  87,  87,  69}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_C, 'C');
    keys_cap[44] = new KeyboardButtonText("V", new int[]{ 87, 105, 105,  87}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_V, 'V');
    keys_cap[45] = new KeyboardButtonText("B", new int[]{105, 123, 123, 105}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_B, 'B');
    keys_cap[46] = new KeyboardButtonText("N", new int[]{123, 141, 141, 123}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_N, 'N');
    keys_cap[47] = new KeyboardButtonText("M", new int[]{141, 159, 159, 141}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_M, 'M');
    keys_cap[48] = new KeyboardButtonText("<", new int[]{159, 177, 177, 159}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '<');
    keys_cap[49] = new KeyboardButtonText(">", new int[]{177, 195, 195, 177}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '>');
    keys_cap[50] = new KeyboardButtonText("?", new int[]{195, 213, 213, 195}, new int[]{ 48, 48, 64, 64}, KeyEvent.VK_UNDEFINED, '?');

    keys_cap[51] = k_ctrl;
    keys_cap[52] = new KeyboardButtonText("au", new int[]{ 22,  44,  44,  22}, new int[]{ 64, 64, 80, 80}, 0, '\0');
    keys_cap[53] = new KeyboardButtonText("~", new int[]{ 44,  62,  62,  44}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '~');
    keys_cap[54] = new KeyboardButtonText("|", new int[]{ 62,  80,  80,  62}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UNDEFINED, '|');
    keys_cap[55] = new KeyboardButtonText(" ", new int[]{ 80, 167, 167,  80}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_SPACE, ' ');
    keys_cap[56] = new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{4, 8, 7, 12, 12, 7, 8},
                                          new int[]{167, 185, 185, 167}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_UP, '\0');
    keys_cap[57] = new KeyboardButtonPoly(new int[]{8, 4, 7, 7, 9, 9, 12}, new int[]{12, 8, 9, 4, 4, 9, 8},
                                          new int[]{185, 203, 203, 185}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_DOWN, '\0');
    keys_cap[58] = new KeyboardButtonPoly(new int[]{5, 9, 8, 13, 13, 8, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                          new int[]{203, 221, 221, 203}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_LEFT, '\0');
    keys_cap[59] = new KeyboardButtonPoly(new int[]{13, 9, 10, 5, 5, 10, 9}, new int[]{8, 4, 7, 7, 9, 9, 12}, 
                                          new int[]{221, 239, 239, 221}, new int[]{ 64, 64, 80, 80}, KeyEvent.VK_RIGHT, '\0');

    /*
    ** Numerical Keyboard.
    */

    keys_nrs[ 0] = k_123_2;
    keys_nrs[ 1] = new KeyboardButtonText("[", new int[]{  19,  36,  36,  19}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_OPEN_BRACKET, '\0');
    keys_nrs[ 2] = new KeyboardButtonText("]", new int[]{  36,  53,  53,  36}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_CLOSE_BRACKET, '\0');
    keys_nrs[ 3] = new KeyboardButtonText("{", new int[]{  53,  70,  70,  53}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '{');
    keys_nrs[ 4] = new KeyboardButtonText("}", new int[]{  70,  87,  87,  70}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '}'); 
    
    keys_nrs[ 5] = new KeyboardButtonText("7", new int[]{  93, 113, 113,  93}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_NUMPAD7, '7');
    keys_nrs[ 6] = new KeyboardButtonText("8", new int[]{ 113, 133, 133, 113}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_NUMPAD8, '8');
    keys_nrs[ 7] = new KeyboardButtonText("9", new int[]{ 133, 153, 153, 133}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_NUMPAD9, '9');

    keys_nrs[ 8] = new KeyboardButtonText("#", new int[]{ 159, 179, 179, 159}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '#');
    keys_nrs[ 9] = new KeyboardButtonText("%", new int[]{ 179, 199, 199, 179}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '%');
    keys_nrs[10] = new KeyboardButtonText("=", new int[]{ 199, 219, 219, 199}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_UNDEFINED, '=');
    keys_nrs[11] = new KeyboardButtonPoly(new int[]{5, 10, 10, 15, 15, 10, 10}, new int[]{10, 5, 8, 8, 12, 12, 15}, 
                                          new int[]{ 219, 239, 239, 219}, new int[]{ 0,  0, 20, 20}, KeyEvent.VK_BACK_SPACE, '\0');

    keys_nrs[12] = new KeyboardButtonText("^", new int[]{   0,  19,  19,   0}, new int[]{20, 20, 40, 40}, KeyEvent.VK_UNDEFINED, '^');
    keys_nrs[13] = new KeyboardButtonText(",", new int[]{  19,  36,  36,  19}, new int[]{20, 20, 40, 40}, KeyEvent.VK_SEPARATER, ',');
    keys_nrs[14] = new KeyboardButtonText(".", new int[]{  36,  53,  53,  36}, new int[]{20, 20, 40, 40}, KeyEvent.VK_DECIMAL, '.');
    keys_nrs[15] = new KeyboardButtonText("<", new int[]{  53,  70,  70,  53}, new int[]{20, 20, 40, 40}, KeyEvent.VK_UNDEFINED, '<');
    keys_nrs[16] = new KeyboardButtonText(">", new int[]{  70,  87,  87,  70}, new int[]{20, 20, 40, 40}, KeyEvent.VK_UNDEFINED, '>');
    
    keys_nrs[17] = new KeyboardButtonText("4", new int[]{  93, 113, 113,  93}, new int[]{20, 20, 40, 40}, KeyEvent.VK_NUMPAD4, '4');
    keys_nrs[18] = new KeyboardButtonText("5", new int[]{ 113, 133, 133, 113}, new int[]{20, 20, 40, 40}, KeyEvent.VK_NUMPAD5, '5');
    keys_nrs[19] = new KeyboardButtonText("6", new int[]{ 133, 153, 153, 133}, new int[]{20, 20, 40, 40}, KeyEvent.VK_NUMPAD6, '6');

    keys_nrs[20] = new KeyboardButtonText("+", new int[]{ 159, 179, 179, 159}, new int[]{20, 20, 40, 40}, KeyEvent.VK_ADD, '+');
    keys_nrs[21] = new KeyboardButtonText("-", new int[]{ 179, 199, 199, 179}, new int[]{20, 20, 40, 40}, KeyEvent.VK_SUBTRACT, '-');
    keys_nrs[22] = new KeyboardButtonText("*", new int[]{ 199, 219, 219, 199}, new int[]{20, 20, 40, 40}, KeyEvent.VK_MULTIPLY, '*');
    keys_nrs[23] = new KeyboardButtonText("/", new int[]{ 219, 239, 239, 219}, new int[]{20, 20, 40, 40}, KeyEvent.VK_DIVIDE, '/');
    
    keys_nrs[24] = new KeyboardButtonText(" ", new int[]{   0,  19,  19,   0}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, '\0');
    keys_nrs[25] = new KeyboardButtonText(" ", new int[]{  19,  36,  36,  19}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, '\0');
    keys_nrs[26] = new KeyboardButtonText(":", new int[]{  36,  53,  53,  36}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, ':');
    keys_nrs[27] = new KeyboardButtonText("\\", new int[]{  53,  70,  70,  53}, new int[]{40, 40, 60, 60}, KeyEvent.VK_BACK_SLASH, '\\');
    keys_nrs[28] = new KeyboardButtonText("|", new int[]{  70,  87,  87,  70}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UNDEFINED, '|');
    
    keys_nrs[29] = new KeyboardButtonText("1", new int[]{  93, 113, 113,  93}, new int[]{40, 40, 60, 60}, KeyEvent.VK_NUMPAD1, '1');
    keys_nrs[30] = new KeyboardButtonText("2", new int[]{ 113, 133, 133, 113}, new int[]{40, 40, 60, 60}, KeyEvent.VK_NUMPAD2, '2');
    keys_nrs[31] = new KeyboardButtonText("3", new int[]{ 133, 153, 153, 133}, new int[]{40, 40, 60, 60}, KeyEvent.VK_NUMPAD3, '3');

    keys_nrs[32] = new KeyboardButtonPoly(new int[]{10, 6, 9, 9, 11, 11, 14}, new int[]{6, 10, 9, 14, 14, 9, 10},
                                          new int[]{ 159, 179, 179, 159}, new int[]{40, 40, 60, 60}, KeyEvent.VK_UP, '\0');
    keys_nrs[33] = new KeyboardButtonPoly(new int[]{10, 6, 9, 9, 11, 11, 14}, new int[]{14, 10, 11, 6, 6, 11, 10},
                                          new int[]{ 179, 199, 199, 179}, new int[]{40, 40, 60, 60}, KeyEvent.VK_DOWN, '\0');
    keys_nrs[34] = new KeyboardButtonPoly(new int[]{6, 10, 9, 14, 14, 9, 10}, new int[]{10, 6, 9, 9, 11, 11, 14}, 
                                          new int[]{ 199, 219, 219, 199}, new int[]{40, 40, 60, 60}, KeyEvent.VK_LEFT, '\0');
    keys_nrs[35] = new KeyboardButtonPoly(new int[]{14, 10, 11, 6, 6, 11, 10}, new int[]{10, 6, 9, 9, 11, 11, 14}, 
                                          new int[]{ 219, 239, 239, 219}, new int[]{40, 40, 60, 60}, KeyEvent.VK_RIGHT, '\0');
    
    keys_nrs[36] = new KeyboardButtonText("$", new int[]{   0,  19,  19,   0}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '$'); 
    keys_nrs[37] = new KeyboardButtonText(" ", new int[]{  19,  36,  36,  19}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0'); 
    keys_nrs[38] = new KeyboardButtonText("\u20AC", new int[]{  36,  53,  53,  36}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0'); 
    keys_nrs[39] = new KeyboardButtonText("\u00A3", new int[]{  53,  70,  70,  53}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0');
    keys_nrs[40] = new KeyboardButtonText("\u00A5", new int[]{  70,  87,  87,  70}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '\0');
    
    keys_nrs[41] = new KeyboardButtonText("(", new int[]{  93, 113, 113,  93}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, '(');
    keys_nrs[42] = new KeyboardButtonText("0", new int[]{ 113, 133, 133, 113}, new int[]{60, 60, 80, 80}, KeyEvent.VK_NUMPAD0, '0');
    keys_nrs[43] = new KeyboardButtonText(")", new int[]{ 133, 153, 153, 133}, new int[]{60, 60, 80, 80}, KeyEvent.VK_UNDEFINED, ')');

    keys_nrs[44] = new KeyboardButtonText("Tab", new int[]{ 159, 183, 183, 159}, new int[]{60, 60, 80, 80}, KeyEvent.VK_TAB, '\0');
    keys_nrs[45] = new KeyboardButtonText("Space", new int[]{ 183, 221, 221, 183}, new int[]{60, 60, 80, 80}, KeyEvent.VK_SPACE, ' ');
    keys_nrs[46] = k_enter_num;
    
    setLayout(null);
    textfield = new TextField("");
    textfield.setFont(new Font("Helvetica", Font.PLAIN, 12));
    setFont(new Font("Helvetica", Font.PLAIN, 12));

    kbdWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    kbdHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

    add(textfield);
    textfield.validate();

    kbdHeight -= textfieldheight;

    textfield.setBounds(2, 0, kbdWidth - 4, textfieldheight);

    int size = keys_nrs.length;
    for(int i=0; i < size; i++) {
      keys_nrs[i].setScale(kbdWidth, kbdHeight, 240, 81);
      keys_nrs[i].setTranslate(0, textfieldheight);
    }
    size = keys_low.length;
    for(int i=0; i < size; i++) {
      keys_low[i].setScale(kbdWidth, kbdHeight, 240, 81);
      keys_low[i].setTranslate(0, textfieldheight);
    }
    size = keys_cap.length;
    for(int i=0; i < size; i++) {
      keys_cap[i].setScale(kbdWidth, kbdHeight, 240, 81);
      keys_cap[i].setTranslate(0, textfieldheight);
    }
  }

  public void update(Graphics g) {
    paint(g);
  }

  private Image drawKeyboard() {
    KeyboardButton local[] = current_keys;
    Image result = createImage(getSize().width, getSize().height);
    Graphics img = result.getGraphics();
    int size = local.length;
    for(int i=0; i < size; i++) {
      local[i].paint_img(img);
    }
    return result;
  }

  public void paint(Graphics g) {
    if(current_keys == keys_low) {
      if(keys_low_img == null) keys_low_img = drawKeyboard();
      g.drawImage(keys_low_img, 0, 0, null);
    }
    else if(current_keys == keys_cap) {
      if(keys_cap_img == null) keys_cap_img = drawKeyboard();
      g.drawImage(keys_cap_img, 0, 0, null);
    }
    else if(current_keys == keys_nrs) {
      if(keys_nrs_img == null) keys_nrs_img = drawKeyboard();
      g.drawImage(keys_nrs_img, 0, 0, null);
    }
    
    int size = pressed_keys.size();
    KeyboardButton key;
    for(int i=0; i < size; i++) {
      ((KeyboardButton)pressed_keys.elementAt(i)).paint(g);
    }
  }

  private KeyboardButton getKey(int x, int y) {
    KeyboardButton local[] = current_keys;
    int size = local.length;
    for(int i=0; i < size; i++) {
      if(local[i].contains(x, y)) {
        return local[i];
      }
    }
    return null;
  }

  public void sendKeyEvent(int keyCode, char keyChar) {
    if(target != null) {
      target.setText(textfield.getText());
      target.setCaretPosition(textfield.getCaretPosition());
      target.select(textfield.getSelectionStart(), textfield.getSelectionEnd());
      KeyEvent ke = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, keyChar);
      target.dispatchEvent(ke);
      ke = new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, keyCode, keyChar);
      target.dispatchEvent(ke);
      keyTyped(keyCode, keyChar);
      textfield.setText(target.getText());
      textfield.setCaretPosition(target.getCaretPosition());
      textfield.select(target.getSelectionStart(), target.getSelectionEnd());
    }
  }

  public Dimension getPreferredSize() {
    return new Dimension(kbdWidth, kbdHeight + textfieldheight);
  }

  public Dimension getMinimumSize() {
    return new Dimension(kbdWidth, kbdHeight + textfieldheight);
  }

  public Dimension getMaximumSize() {
    return new Dimension(kbdWidth, kbdHeight + textfieldheight);
  }

  public void open(TextComponent component) {
    if(component != textfield){
      setBounds(0, 0, getPreferredSize().width, getPreferredSize().height);
      toFront();
      show();
      target = component;
      textfield.setText(target.getText());
      textfield.setCaretPosition(target.getCaretPosition());
      textfield.select(target.getSelectionStart(), target.getSelectionEnd());
      text = target.getText();
    }
  }

  public void close() {
    toBack();
    setVisible(false);
    //dispose();
  }

  private void setPressed(KeyboardButton key, boolean state) {
    key.setPressed(state);
    if(state) {
      pressed_keys.add(key);
    }
    else {
      pressed_keys.remove(key);
    }
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
          setPressed(key, false);
          //System.out.println("k_caps = false");
        }
        else {
          f_caps = true;
          current_keys = keys_cap;
          setPressed(key, true);
          //System.out.println("k_caps = true");
        }
        lastPressed = null;
        repaint();
      }
      else if(key == k_123_1) {
        f_caps = false;
        f_shift = false;
        f_num = true;
        current_keys = keys_nrs;
        pressed_keys.clear();
        setPressed(k_123_2, true);
        repaint();
      }
      else if(key == k_123_2) {
        setPressed(k_123_2, false);
        current_keys = keys_low;
        f_num = false;
        repaint();
      }
      else if(key == k_shift) {
        if(key.getPressed()) {
          setPressed(key, false);
          if(f_caps) current_keys = keys_cap; else current_keys = keys_low;
          f_shift = false;
          //System.out.println("k_shift = false");
        }
        else {
          setPressed(key, true);
          if(f_caps) current_keys = keys_low; else current_keys = keys_cap;
          f_shift = true;
          //System.out.println("k_shift = true");
        }
        repaint();
      }
      else if(key == k_enter || key == k_enter_num) {
        close();
        if(target != null) {
          target.dispatchEvent(new ActionEvent(target, ActionEvent.ACTION_PERFORMED, ""));
        }
      }
      else {
        lastPressed = key;
        setPressed(key, true);
        repaint();
        sendKeyEvent(key.getKeyEvent(), key.getKeyChar());
      }

    }
    textfield.requestFocus();
  }

  public void mouseReleased(MouseEvent event) {
    if(lastPressed != null) {
      setPressed(lastPressed, false);
      if(f_shift) {
        f_shift = false;
        setPressed(k_shift, false);
        if(f_caps) current_keys = keys_cap; else current_keys = keys_low;
      }
      lastPressed = null;

      repaint();
    }
    textfield.requestFocus();
  }

  public void mouseClicked(MouseEvent event) {
    textfield.requestFocus();
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
 
  /*
  ** Fake TextField.
  */
  
  private String text = "";
  private int position;
  private int selectionStart;
  private int selectionStop;

  private int getCaretPosition() {
    position = textfield.getCaretPosition();
    return position;
  }
  
  private void setCaretPosition(int newposition) {
    textfield.setCaretPosition(newposition);
    position = textfield.getCaretPosition();
  }

  private int getSelectionStart() {
    selectionStart = textfield.getSelectionStart();
    return selectionStart;
  }
 
  private int getSelectionEnd() {
    selectionStop = textfield.getSelectionEnd();
    return selectionStop;
  }

  private void select(int start, int end) {
    textfield.select(start, end);
    getSelectionStart();
    getSelectionEnd();
  }

  public void keyTyped(int keyCode, char keyChar) {
    getSelectionStart();
    getSelectionEnd();
    getCaretPosition();
    switch(keyCode) {
      case KeyEvent.VK_LEFT:
        moveCaret(getCaretPosition() - 1);
        break;

      case KeyEvent.VK_RIGHT:
        moveCaret(getCaretPosition() + 1);
        break;

      case KeyEvent.VK_BACK_SPACE:
        if(getSelectionStart() < getSelectionEnd()) {
          deleteSelection();
        }
        else {
          deleteCaret(getCaretPosition() -1);
        }
        break;

      case KeyEvent.VK_DELETE:
        if(getSelectionStart() < getSelectionEnd()) {
          deleteSelection();
        }
        else {
          deleteCaret(getCaretPosition());
        }
        break;

      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_CONTROL:
      case KeyEvent.VK_UP:
      case KeyEvent.VK_DOWN:
        break;

      default:
        if(keyChar != '\0') {
          if(getSelectionStart() < getSelectionEnd() ){
            insertSelection(keyChar);
          }
          else {
            insertCaret(getCaretPosition(), keyChar);
          }
        }
    }
  }

  private boolean moveCaret(int newpos) {
    if(newpos < 0 || newpos > text.length() || newpos == getCaretPosition()) {
      return false ;
    }
    setCaretPosition(newpos);
    select(newpos, newpos);
    return true;
  }

  private boolean deleteCaret(int newpos){
    if(newpos<0 || newpos>=text.length()) {
      return false ;
    }
    text = new String(new StringBuffer(text).deleteCharAt(newpos));
    textfield.setText(text);

    setCaretPosition(newpos);
    select(newpos, newpos);

    return true;
  }

  private void deleteSelection(){
    text = new String(new StringBuffer(text).delete(selectionStart, selectionStop));
    textfield.setText(text);

    setCaretPosition(selectionStart);
    select(position, position);
  }

  private boolean insertCaret(int newpos, char c){
    if(newpos<0 || newpos>text.length()) {
      return false ;
    }
    text = new String(new StringBuffer(text).insert(newpos,c));
    textfield.setText(text);

    setCaretPosition(newpos+1);
    select(position, position);
    
    return true;
  }

  private void insertSelection(char c){
    StringBuffer buf =new StringBuffer(text);
    buf.delete(selectionStart, selectionStop);
    buf.insert(selectionStart,c);
    text = new String(buf);
    textfield.setText(text);

    setCaretPosition(selectionStart);
    select(position, position);
  }
  
}

