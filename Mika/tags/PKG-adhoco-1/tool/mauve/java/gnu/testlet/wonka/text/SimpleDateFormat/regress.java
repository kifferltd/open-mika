// Regression test for libgcj/Classpath SimpleDateFormat bugs

// Tags: JDK1.1

// Copyright (c) 1999, 2001  Free Software Foundation

// This file is part of Mauve.

package gnu.testlet.wonka.text.SimpleDateFormat;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.text.*;
import java.util.*;

public class regress implements Testlet
{
  // These must all be in the same format, with the timezone as the
  // characters after the final space, since that is what the code
  // expects.  They must also all represent the same time.
  public static String[] dates =
  {
    "Fri, 18 May 2001 12:18:06 CDT",
    "Fri, 18 May 2001 13:18:06 EDT",
    "Fri, 18 May 2001 12:18:06 EST",
    "Fri, 18 May 2001 17:18:06 GMT",
    "Fri, 18 May 2001 10:18:06 PDT"
  };

  public void test (TestHarness harness)
  {
    // We don't check the results but just that this works at all.  This
    // is a regression test for libgcj.
    harness.setclass("java.text.SimpleDateFormat");
    harness.checkPoint ("parsing regression");
    DateFormat cdf = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss zzzz");
    boolean ok = true;
    Date d = null;
    try
      {
	d = cdf.parse ("Fri, 18 May 2001 20:18:06 GMT");
      }
    catch (ParseException _)
      {
	ok = false;
      }
    harness.check (ok);

    Calendar k = Calendar.getInstance (TimeZone.getTimeZone ("GMT"));
    k.setTime (d);
    harness.check (k.get(Calendar.HOUR),        8, "check hour");
    harness.check (k.get(Calendar.HOUR_OF_DAY), 20, "check hour-of-day");

    cdf = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss zzz");
    cdf.setTimeZone (TimeZone.getTimeZone ("GMT"));
    //harness.debug("timezone = "+TimeZone.getTimeZone ("GMT")+", uses dayLightsSVG "+TimeZone.getTimeZone ("GMT").useDaylightTime());
    for (int i = 0; i < dates.length; ++i)
      {
	String tz = dates[i].substring (dates[i].lastIndexOf (' ') + 1,
					dates[i].length ());
	try
	  {
	    d = cdf.parse (dates[i]);
	    harness.check (cdf.format (d), "Fri, 18 May 2001 17:18:06 GMT",
			   tz);
	  }
	catch (ParseException _)
	  {
	    harness.debug (_);
	    harness.check (false, tz);
	  }
      }

    cdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss"); 
    try
      {
	d = cdf.parse ("03-22-2001 15:54:27");
	harness.check (cdf.format (d), "03-22-2001 15:54:27",
		       "local timezone");
      }
    catch (ParseException _)
      {
	harness.debug (_);
	harness.check (false, "local timezone");
      }
  }
}
