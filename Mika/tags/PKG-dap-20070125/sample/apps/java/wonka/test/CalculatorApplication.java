//
// file: CalculatorApplication.java
//
// (C) 1997-1999 Beta Nine. All Rights Reserved.
// written by sven@beta9.be - http://www.beta9.be
// $Id: CalculatorApplication.java,v 1.1 2005/10/24 14:47:49 cvs Exp $
//

package wonka.test;
import java.awt.*;
import java.awt.event.*;

// our main application class
public class CalculatorApplication 
    extends Frame 
    implements ActionListener, Runnable {

    // keep track of our calculator and the components that we created
    private Calculator calculator;
    private Button 
        one, two, three, 
        four, five, six, 
        seven, eight, nine, 
        zero;
    private Button    
        plus, subtract, multiply, divide, dot, enter, 
        invert, clear, reciprocal, info;
    private Label display;
    private Thread runner;
    private boolean run;
    private Runtime rt = Runtime.getRuntime();
         
    private Font displayFont;

    // some constants
    private final static int GAP = 6;
    private static boolean toggle = false;
    private static String infoMessage[] = {
      "Acunia drives telematics       ",
      "The quick brown fox jumps over the lazy dog",
      "All work and no play makes Jack a dull boy",
      "The silver swan    Who living had no note    "+
      "When death approached unlocked her silent throat:    "+
      "\"Farewell all joys,    Oh death come close mine eyes    "+
      "More geese than swans now live, more fools than wise.\"",
      "Have you nothing better to do with your time?",
      "It'll fall off if you keep playing with it like that",
      "Don't you ever get fed up with these stupid scrolling messages?"
      };
    private static int messageNumber = 0;

    private final static int FONT_SIZE = 24;
    private final static String FONT_NAME = "Dialog";
        
    private static final long DOUBLE_CLICK_MILLIS = 500;
    private long clear_clicked = System.currentTimeMillis();
    private long last_clear_clicked;

    // our constructor
    public CalculatorApplication(String label) {
        super(label);
	init();
        setSize(400, 234);
        setVisible(true);
    }
        
    public CalculatorApplication() {
      this("Wonka Test Calculator");
    }

    // application entry point
    public static void main(String args[]) {
        CalculatorApplication calculatorApplication = 
            new CalculatorApplication("SmartCalculator");
        synchronized(CalculatorApplication.class) {
          try { 
            CalculatorApplication.class.wait();
          } catch (InterruptedException e) {}
        }
    }
        
    // init the application's GUI: create our user interface
    public void init() {
        Panel buttons, top;
        Color w = Color.white, d = Color.darkGray;

        calculator = new Calculator();
        
        displayFont = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
        setFont(displayFont);
        setBackground(Color.black);

        buttons = new Panel();
        buttons.setLayout(new GridLayout(5, 4, GAP, GAP));
                
        buttons.add(clear = new Button("C"));
        clear.setBackground(d);
        clear.setForeground(Color.red);
	clear.addActionListener(this);

        buttons.add(info = new Button("?"));
        info.setBackground(d);
        info.setForeground(w);
	info.addActionListener(this);

        buttons.add(reciprocal = new Button("1/x"));
        reciprocal.setBackground(d);
        reciprocal.setForeground(Color.yellow);
	reciprocal.addActionListener(this);

        buttons.add(invert = new Button("+/-"));
        invert.setBackground(d);
        invert.setForeground(Color.yellow);
	invert.addActionListener(this);

        buttons.add(seven = new Button("7"));
        seven.setBackground(w);
	seven.addActionListener(this);

        buttons.add(eight = new Button("8"));
        eight.setBackground(w);
	eight.addActionListener(this);

        buttons.add(nine = new Button("9"));
        nine.setBackground(w);
	nine.addActionListener(this);

        buttons.add(divide = new Button("/"));
        divide.setBackground(d);
        divide.setForeground(Color.orange);
	divide.addActionListener(this);

        buttons.add(four = new Button("4"));
        four.setBackground(w);
	four.addActionListener(this);

        buttons.add(five = new Button("5"));
        five.setBackground(w);
	five.addActionListener(this);

        buttons.add(six = new Button("6"));
        six.setBackground(w);
	six.addActionListener(this);

        buttons.add(multiply = new Button("*"));
        multiply.setBackground(d);
        multiply.setForeground(Color.orange);
	multiply.addActionListener(this);

        buttons.add(one = new Button("1"));
        one.setBackground(w);
	one.addActionListener(this);

        buttons.add(two = new Button("2"));
        two.setBackground(w);
	two.addActionListener(this);

        buttons.add(three = new Button("3"));
        three.setBackground(w);
	three.addActionListener(this);

        buttons.add(subtract = new Button("-"));
        subtract.setBackground(d);
        subtract.setForeground(Color.orange);
	subtract.addActionListener(this);

        buttons.add(zero = new Button("0"));
        zero.setBackground(w);
	zero.addActionListener(this);

        buttons.add(dot = new Button("."));
        dot.setBackground(w);
	dot.addActionListener(this);

        buttons.add(enter = new Button("="));
        enter.setBackground(d);
        enter.setForeground(Color.red);
	enter.addActionListener(this);

        buttons.add(plus = new Button("+"));
        plus.setBackground(d);
        plus.setForeground(Color.orange);
	plus.addActionListener(this);
                
        
        top = new Panel();
        top.setLayout(new GridLayout(1, 1, GAP, GAP));
	top.add(display = new Label("", Label.RIGHT));
        display.setFont(new Font("Courier", Font.BOLD, FONT_SIZE + 4));
        display.setBackground(Color.black);
        display.setForeground(Color.green);
                
        setLayout(new BorderLayout(GAP, GAP));
	add("North", top);
        add("Center", buttons);
        updateDisplay();
    }
   
    // update the display
    private boolean updateDisplay() {
        String str;
        double acc = calculator.accumulator();
        boolean isInteger = ((double)((int)acc) == acc);
        
        if (!isInteger) {
            str = (new Float(acc)).toString();
        } else {
            int zeros = calculator.trailingDigits();
                
            str = (new Integer((int)acc)).toString();
            if (zeros > 0) {
                str += ".";
                while(--zeros > 0) str += "0";
            }
        }
        display.setText(str);
        return true;
    }
   
    // handle the buttons
    public void actionPerformed(ActionEvent event) {
        hideInfoMessage();
        if (event.getSource() == one) {
            calculator.one();
            updateDisplay();
        } else if (event.getSource() == two) {
            calculator.two();
            updateDisplay();
        } else if (event.getSource() == two) {
            calculator.two();
            updateDisplay();
        } else if (event.getSource() == three) {
            calculator.three();
            updateDisplay();
        } else if (event.getSource() == four) {
            calculator.four();
            updateDisplay();
        } else if (event.getSource() == five) {
            calculator.five();
            updateDisplay();
        } else if (event.getSource() == six) {
            calculator.six();
            updateDisplay();
        } else if (event.getSource() == seven) {
            calculator.seven();
            updateDisplay();
        } else if (event.getSource() == eight) {
            calculator.eight();
            updateDisplay();
        } else if (event.getSource() == nine) {
            calculator.nine();
            updateDisplay();
        } else if (event.getSource() == zero) {
            calculator.zero();
            updateDisplay();
        } else if (event.getSource() == dot) {
            calculator.dot();
            updateDisplay();
        } else if (event.getSource() == enter) {
            calculator.enter();
            updateDisplay();
        } else if (event.getSource() == plus) {
            calculator.plusOperator();
            updateDisplay();
        } else if (event.getSource() == subtract) {
            calculator.subtractOperator();
            updateDisplay();
        } else if (event.getSource() == multiply) {
            calculator.multiplyOperator();
            updateDisplay();
        } else if (event.getSource() == divide) {
            calculator.divideOperator();
            updateDisplay();
        } else if (event.getSource() == clear) {
            last_clear_clicked = clear_clicked;
            clear_clicked = System.currentTimeMillis();
            if (clear_clicked - last_clear_clicked < DOUBLE_CLICK_MILLIS) {
              System.exit(0);
            }
            calculator.clear();
            updateDisplay();
        } else if (event.getSource() == invert) {
            calculator.invert();
            updateDisplay();
        } else if (event.getSource() == reciprocal) {
            calculator.reciprocal();
            updateDisplay();
        } else if (event.getSource() == info) {
            showInfoMessage();
        }
    }
   
    // start the animation of the display
    private void showInfoMessage() {
        if (runner == null ) {
            runner = new Thread(this);
            runner.setPriority(Thread.NORM_PRIORITY + 3);
	    run = true;
            runner.start();
        }
    }
        
    // stop the animantion of the display and reset
    private void hideInfoMessage() {
        if (runner != null) {
            run = false;
            runner = null;
            updateDisplay();
        }
    }
        
    // the actual animation of the display
    public void run() {
        FontMetrics fm = display.getGraphics().getFontMetrics(displayFont);
        int displayWidth = display.getWidth();
 
        String message;
        if (toggle) {
          message = infoMessage[messageNumber];
        }
        else {
          message = "Current heap usage: "+(rt.totalMemory()-rt.freeMemory())+" bytes";
        }
        String excerpt = "";
        int textWidth = 0;
        int index = 0;
        int behead = 0;

        while (run && index < message.length()) {
	    excerpt = excerpt + message.charAt(index++);
            while (fm.stringWidth(excerpt) > displayWidth) {
              ++behead;
              excerpt = excerpt.substring(1);
            }
            display.setText(excerpt);
            try { Thread.sleep(100); } catch (InterruptedException e) {};
        }
                
        while (run && behead < message.length()) {
	    excerpt = excerpt + " ";
            while (fm.stringWidth(excerpt) > displayWidth) {
              ++behead;
              excerpt = excerpt.substring(1);
            }
            display.setText(excerpt);
            try { Thread.sleep(100); } catch (InterruptedException e) {};
        }
                
        updateDisplay();
        if (toggle) {
          ++messageNumber;
        }
        if (messageNumber == infoMessage.length) {
          messageNumber = 0;
        }
        toggle = !toggle;
    }
                
    // stop any ongoing animation
    public void stop() {
        if (runner != null) {
            run = false;
            runner = null;
        }
    }
}

