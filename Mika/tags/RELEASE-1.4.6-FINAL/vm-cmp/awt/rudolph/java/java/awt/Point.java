/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

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
