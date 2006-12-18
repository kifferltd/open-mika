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


/* $Id: Point.java,v 1.1 2005/06/15 09:05:18 cvs Exp $ */

package java.awt;

public class Point implements java.io.Serializable {

  private static final long serialVersionUID = -5276940640259749850L;
  
  public int x;
  public int y;

  public Point() {
    this(0, 0);
  }
  
  public Point(Point p) {
    this(p.x, p.y);
  }
  
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  public boolean equals(Object object) {
    if (object instanceof Point) {
       Point point = (Point) object;
       return (x == point.x) && (y == point.y);
    }
    else {
      return super.equals(object);
    }
  }
  
  public Point getLocation() {
    return new Point(x, y);
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  /**
   * @status not implemented
   * @remark not implemented
   */ 
  native public int hashCode();
  
  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  public void setLocation(Point point) {
    setLocation(point.x, point.y);
  }
  
  public String toString() {
    return getClass().getName() + "[x = " + x + ", y = " + y + "]";
  }

  public void move(int x, int y) {
    this.x = x;
    this.y = y;
  }
 
  public void translate(int x, int y) {
    this.x += x;
    this.y += y;
  }
    
}
