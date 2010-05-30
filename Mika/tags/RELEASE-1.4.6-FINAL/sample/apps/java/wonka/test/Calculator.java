//
// file: Calculator.java
//
// (C) 1997-1999 Beta Nine. All Rights Reserved.
// written by sven@beta9.be - http://www.beta9.be
// $Id: Calculator.java,v 1.1 2005/10/24 14:47:49 cvs Exp $
//
package wonka.test;

public class Calculator {

    // I model the internal workings of a calculator, not the actual UI.
    // You can operate me like a classical calcualtor,
    // i.e. my public methods represent the calculator keys'.
    // My accumulator is the value the user sees.

    // internal structure
    // holds the working number (which is being edited)
    private double accumulatorValue;
    // holds other operand (previous accumulator)
    private double transientValue;
    // holds the current operator 
    private int operatorCode; 
    // used in decimal number input
    private int trailingDigits;         
    // used in decimal number input
    private boolean startNewDigit;      
        
    // operator code constants
    private final static int NO_OPERATOR = 0;
    private final static int DIVIDE_OPERATOR = 1;
    private final static int MULTIPLY_OPERATOR = 2;
    private final static int PLUS_OPERATOR = 3;
    private final static int SUBTRACT_OPERATOR = 4;
        
    // initialize 
    public Calculator() {
        accumulatorValue = transientValue = 0.0;
        operatorCode = NO_OPERATOR;
        trailingDigits = 0;
        startNewDigit = true;
    }
        
    // accessing the accumulator
    public double accumulator() { 
        return accumulatorValue;
    }
        
    // accessing the number of trailingDigits
    public int trailingDigits() {
        return trailingDigits;
    } 
        
    // key: /
    public void divideOperator() {
        acceptOperator(DIVIDE_OPERATOR);
    }
        
    // key: *
    public void multiplyOperator() {
        acceptOperator(MULTIPLY_OPERATOR);
    }
        
    // key: +   
    public void plusOperator() {
        acceptOperator(PLUS_OPERATOR);
    }
        
    // key: -
    public void subtractOperator() {
        acceptOperator(SUBTRACT_OPERATOR);
    }
        
    // key: 0
    public void zero() {
        newDigit(0);
    }
        
    // key: 1
    public void one() {
        newDigit(1);
    }
        
    // key: 2
    public void two() {
        newDigit(2);
    }
        
    // key: 3
    public void three() {
        newDigit(3);
    }
        
    // key: 4
    public void four() {
        newDigit(4);
    }
        
    // key: 5
    public void five() {
        newDigit(5);
    }
        
    // key: 6
    public void six() {
        newDigit(6);
    }
        
    // key: 7
    public void seven() {
        newDigit(7);
    }
        
    // key: 8
    public void eight() {
        newDigit(8);
    }
        
    // key: 9
    public void nine() {
        newDigit(9);
    }
        
    // key: .
    public void dot() {
        if (startNewDigit) {
            accumulatorValue = 0.0;
            startNewDigit = false;
        }
        if (trailingDigits == 0) {
            trailingDigits = 1;
        }
    }
        
    // key: C
    public void clear() {
        accumulatorValue = transientValue = 0.0;
        operatorCode = NO_OPERATOR;
        trailingDigits = 0;
        startNewDigit = true;
    }
        
    // key: =
    // key: ENTER
    public void enter() {
        try {
            switch (operatorCode) {
            case NO_OPERATOR: 
                break;
            case PLUS_OPERATOR:
                accumulatorValue = transientValue + accumulatorValue;
                break;
            case SUBTRACT_OPERATOR:;
                accumulatorValue = transientValue - accumulatorValue;
                break;
            case MULTIPLY_OPERATOR:;
                accumulatorValue = transientValue * accumulatorValue;
                break;
            case DIVIDE_OPERATOR:;
                accumulatorValue = transientValue / accumulatorValue;
                break;
            }
        }
        catch (ArithmeticException e) {
            accumulatorValue = 0;
        }
        finally {
            transientValue = 0.0;
            operatorCode = NO_OPERATOR;
            trailingDigits = 0;
            startNewDigit = true;
        }
    }
        
    // key: +/-
    public void invert() {
        accumulatorValue = - accumulatorValue;
    }
        
    // key: 1/x
    public void reciprocal() {
        if (accumulatorValue == 0) return;
        accumulatorValue = 1 / accumulatorValue;
        startNewDigit = true;
        trailingDigits = 0;
    }
        
    // a new operator is entered
    private void acceptOperator(int code) {
        if (operatorCode == NO_OPERATOR) {
            transientValue = accumulatorValue;
            startNewDigit = true;
        } else {
            // user is cascading operations
            enter();
        }
        operatorCode = code;
        trailingDigits = 0;
    }
        
    // a new digit is entered
    private void newDigit(int digit) {
        if (startNewDigit) {
            transientValue = accumulatorValue;
            startNewDigit = false;
            accumulatorValue = (double)digit;
        } else {
            boolean negative = false;
            if (accumulatorValue < 0) {
                accumulatorValue = - accumulatorValue;
                negative = true;
            }
            if (trailingDigits == 0) {
                accumulatorValue = accumulatorValue * 10 + (double)digit;
            } else {
                accumulatorValue += 
                    (double)digit / Math.pow(10, (double)trailingDigits);
                trailingDigits++;
            }
            if (negative) accumulatorValue = - accumulatorValue;
        }
    }
        
}

