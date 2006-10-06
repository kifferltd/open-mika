package wonka.test;

import java.util.Date;
import java.util.TimeZone;

class DateTest {

  public static void main(String[] args) {
    System.out.println("Available TimeZone ids:");
    String[] ids = TimeZone.getAvailableIDs();
    for (int i = 0; i < ids.length; ++i) {
      System.out.println(TimeZone.getTimeZone(ids[i]));
    }

    System.out.println();
    System.out.println("Default timezone: " + TimeZone.getDefault());
    System.out.println("Date/time now: " + new java.util.Date());
  }
}

