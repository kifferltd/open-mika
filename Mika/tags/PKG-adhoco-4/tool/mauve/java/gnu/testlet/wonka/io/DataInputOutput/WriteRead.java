/*************************************************************************
/* WriteRead.java -- Tests Data{Input,Output}Stream's
/*
/* Copyright (c) 1998, 1999 Free Software Foundation, Inc.
/* Written by Aaron M. Renn (arenn@urbanophile.com)
/*
/* This program is free software; you can redistribute it and/or modify
/* it under the terms of the GNU General Public License as published 
/* by the Free Software Foundation, either version 2 of the License, or
/* (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful, but
/* WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU General Public License for more details.
/*
/* You should have received a copy of the GNU General Public License
/* along with this program; if not, write to the Free Software Foundation
/* Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
/*************************************************************************/

// Tags: JDK1.0
// Uses: ReadStream

package gnu.testlet.wonka.io.DataInputOutput;

import java.io.*;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class WriteRead implements Testlet
{

public void
test(TestHarness harness)
{
  harness.setclass("java.io.Writer");

  byte[] ba;

  // First write it.
  try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
      DataOutputStream dos = new DataOutputStream(baos);

      dos.writeBoolean(true);
      dos.writeBoolean(false);
      dos.writeByte((byte)8);
      dos.writeByte((byte)-122);
      dos.writeChar((char)'a');
      dos.writeChar((char)'\uE2D2');
      dos.writeShort((short)32000);
      dos.writeInt((int)8675309);
      dos.writeLong(696969696969L);
      dos.writeFloat((float)3.1415);
      dos.writeDouble((double)999999999.999);
      dos.writeUTF("Testing code is such a boring activity but it must be done");
      dos.writeUTF("a-->\u01FF\uA000\u6666\u0200RRR");
      dos.close();
      ba = baos.toByteArray();

      harness.check(true, "DataOutputStream write (conditionally");
    }
  catch (Exception e)
    {
      harness.debug(e);
      harness.check(false, "DataOutputStream write");
      return;
    }

  // Now read it
  try
    {
      ByteArrayInputStream is = new ByteArrayInputStream(ba);
      DataInputStream dis = new DataInputStream(is); 

      harness.debug("Reading data written during write phase.");
      ReadStream.runReadTest(dis, harness);

      dis.close();
    }
  catch (Exception e)
    {
      harness.debug(e);
      harness.check(false, "Read data written during write phase");
    }
}

} // class WriteRead

