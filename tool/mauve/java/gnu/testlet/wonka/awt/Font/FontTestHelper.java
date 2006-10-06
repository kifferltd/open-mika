// A helper for FontTest.java, to test subclassing the Font class
// Tags: not-a-test

// Copyright (C) 2000 Cygnus Solutions

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

// Author: J. Vandeneede
// Created: 2001/01/08

package gnu.testlet.wonka.awt.Font;
import java.awt.*;

public class FontTestHelper extends Font
  {
  /* //Linux:
  String fontName = new String("Dialog");
  int fontSize=12;
  */
  // wonka:
  String fontName = new String("courP10");
  int fontSize=10;

  FontTestHelper ()
    {
    /* //Linux:
    super("Dialog", Font.PLAIN, 12);
    */
    // wonka
    super("courP10", Font.PLAIN, 10);
    }
  public boolean testIt()
    {
    //  use 'equals' to compare strings; '==' compares references!

    Font fnt = new Font(fontName, PLAIN, fontSize);

    if (!name.equals(fontName))
      {
      // System.out.println("[JVDE] class FontHelp : Font.name test failed");
      return false;
      }
    else if (style != PLAIN)
      {
      // System.out.println("[JVDE] class FontHelp : Font.style test failed");
      return false;
      }
    else if (size != fontSize)
      {
      // System.out.println("[JVDE] class FontHelp : Font.size test failed");
      return false;
      }
    else if (!fnt.equals(this))
      {
      // System.out.println("[JVDE] class FontHelp : Font.equals test failed");
      return false;
      }
    return true;
    }
  }

