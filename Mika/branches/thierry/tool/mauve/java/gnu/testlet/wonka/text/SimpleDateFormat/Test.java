/*************************************************************************
/* Test.java -- Test java.text.SimpleDateFormat
/*
/* Copyright (c) 1998, 1999, 2001 Free Software Foundation, Inc.
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

// Tags: JDK1.1

package gnu.testlet.wonka.text.SimpleDateFormat;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.text.*;
import java.util.*;

public class Test implements Testlet
{

public void 
test(TestHarness harness)
{
  harness.setclass("java.text.SimpleDateFormat");
  harness.checkPoint("basic tests");

  String pattern_chars = "GyMdhHmsSEDFwWakKz";
  String pattern = "EEEE, MMMM d, yyyy h:mm:ss 'o''clock' a";

  DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);
  SimpleDateFormat sdf = new SimpleDateFormat(pattern, dfs);
  harness.check(sdf.getDateFormatSymbols(), dfs, "getDateFormatSymbols() init");

  String[] ampms = { "am ", "pm " };
  dfs.setAmPmStrings(ampms);
  sdf.setDateFormatSymbols(dfs);
  harness.check(sdf.getDateFormatSymbols(), dfs, "set/getDateFormatSymbols()");
  
  harness.check(sdf.toPattern(), pattern, "toPattern init");
  String new_pattern = "EMdyH";
  sdf.applyPattern(new_pattern);
  harness.check(sdf.toPattern(), new_pattern, "apply/toPattern()");
  sdf.applyPattern(pattern);

  harness.check(sdf.equals(new SimpleDateFormat(pattern, dfs)), "equals()");
  harness.check(sdf.clone().equals(sdf) == true, "clone()");

  sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
  Date d = new Date(0);
  String formatted_date = sdf.format(d);
  harness.check(formatted_date,
     "Thursday, January 1, 1970 12:00:00 o'clock am ", "format()");

  sdf.setLenient(false);
  try
    {
      harness.check(sdf.parse(formatted_date), d, "parse() strict");
    }
  catch(Throwable e)
    {
      harness.debug(e);
      harness.check(false, "parse() strict");
    }

  sdf.setTimeZone(TimeZone.getDefault());

  // Now do some lenient parsing tests.  These might not all work.
  dfs = new DateFormatSymbols(Locale.US);
  sdf = new SimpleDateFormat(pattern, dfs);

  sdf.setLenient(true);
}

} // class Test

