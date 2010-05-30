// Tags: not-a-test

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

package gnu.testlet.wonka.util.ResourceBundle;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Enumeration;

public class Resource8 extends ResourceBundle 
{
  protected Object handleGetObject(String key) 
    throws MissingResourceException
    {
      if (key.compareTo ("class") == 0)
	return this.getClass().getName();
      else
	throw new MissingResourceException ("s", "className", "key");
    }

  public Enumeration getKeys()
    {
      return null;
    }
}

