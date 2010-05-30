// Tags: JDK1.4

// Copyright (C) 1998 Cygnus Solutions

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

package gnu.testlet.wonka.lang.Character;

import java.util.Random;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.UnicodeSubsets;

public class mirrored14 implements Testlet
{

  public void test (TestHarness harness) {
    // Check the mirrored ones exhaustively
    harness.check (Character.isMirrored((char)0x0028), "Character 0028 should be mirrored");
    harness.check (Character.isMirrored((char)0x0029), "Character 0029 should be mirrored");
    harness.check (Character.isMirrored((char)0x003c), "Character 003c should be mirrored");
    harness.check (Character.isMirrored((char)0x003e), "Character 003e should be mirrored");
    harness.check (Character.isMirrored((char)0x005b), "Character 005b should be mirrored");
    harness.check (Character.isMirrored((char)0x007d), "Character 007d should be mirrored");
    harness.check (Character.isMirrored((char)0x007b), "Character 007b should be mirrored");
    harness.check (Character.isMirrored((char)0x00ab), "Character 00ab should be mirrored");
    harness.check (Character.isMirrored((char)0x00bb), "Character 00bb should be mirrored");
    harness.check (Character.isMirrored((char)0x2039), "Character 2039 should be mirrored");
    harness.check (Character.isMirrored((char)0x203a), "Character 203a should be mirrored");
    harness.check (Character.isMirrored((char)0x2045), "Character 2045 should be mirrored");
    harness.check (Character.isMirrored((char)0x2046), "Character 2046 should be mirrored");
    harness.check (Character.isMirrored((char)0x207d), "Character 207d should be mirrored");
    harness.check (Character.isMirrored((char)0x207e), "Character 207e should be mirrored");
    harness.check (Character.isMirrored((char)0x208d), "Character 208d should be mirrored");
    harness.check (Character.isMirrored((char)0x208e), "Character 2078 should be mirrored");
    // a few random others
    harness.check (!Character.isMirrored((char)0x0020), "Character 0020 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x0030), "Character 0030 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x0055), "Character 0055 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x0077), "Character 0077 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x0099), "Character 0099 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x00af), "Character 00af should not be mirrored");
    harness.check (!Character.isMirrored((char)0x00b6), "Character 00b6 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x2035), "Character 2035 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x2333), "Character 2333 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x2444), "Character 2444 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x2555), "Character 2555 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x2666), "Character 2666 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x3333), "Character 3333 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x4444), "Character 4444 should not be mirrored");
    harness.check (!Character.isMirrored((char)0x8888), "Character 8888 should not be mirrored");
    harness.check (!Character.isMirrored((char)0xEEEE), "Character EEEE should not be mirrored");
  }
}

