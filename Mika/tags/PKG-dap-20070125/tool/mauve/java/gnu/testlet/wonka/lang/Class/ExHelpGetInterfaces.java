// A helper for reflect.java.
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

package gnu.testlet.wonka.lang.Class;

import gnu.testlet.Testlet;

public class ExHelpGetInterfaces extends HelpGetInterfaces implements java.io.Externalizable, java.io.Serializable {
  public void writeExternal(java.io.ObjectOutput out){}
  public void readExternal(java.io.ObjectInput in){}
}
