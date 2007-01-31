package com.acunia.wonka.test.awt;

import java.awt.*;

public class Summary extends VisualTestImpl{

  static long startTime;
  static int ok_count;
  static int bad_count;
  static int loaded_count;
  static long heap_total;
  static long heap_used_min;
  static long heap_used_max;

  static void init(long millis) {
    startTime = millis;
    heap_total = Runtime.getRuntime().totalMemory();
    heap_used_min = heap_total;
  }

  static void freeMemory(long free) {
    long used = heap_total - free;
    if (used < heap_used_min) heap_used_min = used;
    if (used > heap_used_max) heap_used_max = used;
  }

  static void testLoaded() {
    ++loaded_count;
  }

  static void testOK() {
    ++ok_count;
  }

  static void testBad() {
    ++bad_count;
  }

  String reason_text;

  Summary(String reason) {
    reason_text = "Tests halted because " + reason + ".";
  }

  private class MainCanvas extends Panel {

    MainCanvas() {
      setLayout(new BorderLayout());
      add(new Label("        "), BorderLayout.WEST);
      Label l = new Label("Summary of results");
      l.setFont(new Font("courier", Font.PLAIN, 17));
      add(l, BorderLayout.NORTH);
      Panel main = new Panel(new GridLayout(8, 1));
      if (startTime != 0) {
        long duration = System.currentTimeMillis() - startTime;
        StringBuffer duration_text = new StringBuffer("Test duration: ");
        if (duration < 10000) {
          duration_text.append(duration * 0.001);
        }
        else {
          if (duration >= 172800000) {
            duration_text.append(duration / 86400000);
            duration_text.append(" days ");
            duration %= 86400000;
          }
          else if (duration >= 86400000) {
            duration_text.append("1 day ");
            duration -= 86400000;
          }
          if (duration < 3600000 || duration >= 7200000) {
            duration_text.append(duration / 3600000);
            duration_text.append(" hours ");
            duration %= 3600000;
          }
          else {
            duration_text.append("1 hour ");
            duration -= 3600000;
          }
          if (duration < 60000 || duration >= 120000) {
            duration_text.append(duration / 60000);
            duration_text.append(" minutes ");
            duration %= 60000;
          }
          else {
            duration_text.append("1 minute ");
            duration -= 60000;
          }
          if (duration < 1000 || duration >= 2000) {
            duration_text.append(duration / 1000);
            duration_text.append(" seconds.");
          }
          else {
            duration_text.append("1 second.");
          }
        }

        main.add(new Label(duration_text.toString()));
      }
      if (heap_used_min < heap_total && heap_used_max > 0) {
        main.add(new Label("Heap size = " + heap_total + " bytes, used = " + heap_used_min + " min, " + heap_used_max + " max."));
      }
      main.add(new Label(reason_text));
      main.add(new Label(loaded_count + " tests were executed."));
      if (ok_count > 0 || bad_count > 0) {
        main.add(new Label(ok_count + " tests were marked OK."));
        main.add(new Label(bad_count + " tests were marked bad."));
      }
      add(main, BorderLayout.CENTER);
      setVisible(true);
      repaint();
    }
  }

  public Panel getPanel(VisualTester vte){
    vt = vte;
    return new MainCanvas();
  }

  public String getHelpText() {
    return "This is the results summary page. If you got here, the tests terminated normally.";
  }
}


