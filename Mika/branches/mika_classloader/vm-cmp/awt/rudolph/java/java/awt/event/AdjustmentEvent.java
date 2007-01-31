/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/



package java.awt.event;

public class AdjustmentEvent extends java.awt.AWTEvent {

public static final int ADJUSTMENT_FIRST = 601;
public static final int ADJUSTMENT_VALUE_CHANGED = 601;
public static final int ADJUSTMENT_LAST = 601;

/** the command types*/
public static final int UNIT_INCREMENT = 1;
public static final int UNIT_DECREMENT = 2;
public static final int BLOCK_DECREMENT = 3;
public static final int BLOCK_INCREMENT = 4;
public static final int TRACK = 5;

  /*****************************************************************/
  /**
  * Variables for type and value
  */
  // Object eventSource; from java.util.EventObject
  // int eventId;        from java.util.EventObject
  int commandType;
  int barPosition;

  /*****************************************************************/
  /**
  * Constructor
  */
  public AdjustmentEvent(java.awt.Adjustable source, int id, int type, int value) {
    super(source, id);
    commandType = type;
    barPosition = value;
  }

  /**
  * get the values loaded by a call to  AdjustmentListener.processAdjustmentEvent(AdjustmentEvent)
  */
  /** from java.util.EventObject */
  //  public Object getSource() {return super.getSource() }
  /** from java.awt.AWTEvent */
  // public int getID() {return super.getID() }

  /*****************************************************************/
  /**
  * returns the [Adjustable] interface that threw the event (set by AdjustmentEvent(source, id, type, value))
  *  This is equivalent to the super  java.util.EventObject.getSource(), only that public Object getSource returns the [Object] that did it
  * @status  implemented & tested
  * @remark  alternatively use <(java.awt.Adjustable)getSource() >
  */
  public java.awt.Adjustable getAdjustable() {
    return (java.awt.Adjustable)source;
  }
  //{  return((java.awt.Adjustable)getSource());  }

  /*****************************************************************/
  /**
  * returns the type of command  (UNIT_INCREMENT/UNIT_DECREMENT/BLOCK_INCREMENT/BLOCK_DECREMENT/TRACK)
  * @status  implemented & tested
  * @remark  implemented
  */
  public int getAdjustmentType() {
    return  commandType;
  }

  /*****************************************************************/
  /**
  * returns the scrollbar position <value> set by AdjustmentEvent(source, id, type, value)
  * @status  implemented & tested
  * @remark  alternatively use <( (java.awt.Adjustable)getSource()).getValue() >
  */
  public int getValue() {
    return barPosition;  
  }

  /*****************************************************************/
  /**
  * returns default String description
  * @status  implemented
  * @remark  overrides Object.toString()
  */
  public String toString() {
    String commandstring = "[TRACK]";
    if(commandType == UNIT_INCREMENT) {
      commandstring = "[UNIT_INCREMENT]";
    }
    else if(commandType == UNIT_DECREMENT) {
      commandstring = "[UNIT_DECREMENT]";
    }
    else if(commandType == BLOCK_INCREMENT) {
      commandstring = "[BLOCK_INCREMENT]";
    }
    else if(commandType == BLOCK_DECREMENT) {
      commandstring = "[BLOCK_DECREMENT]";
    }
    return getClass().getName() +commandstring+source;
  }

  /*****************************************************************/
  /**
  * returns parameter String description
  * @status  implemented
  * @remark  overrides AWTEvent.paramString()
  */
  public String paramString() {
    return getClass().getName() +" type="+commandType+" value="+barPosition+" from "+source;
  }
}
