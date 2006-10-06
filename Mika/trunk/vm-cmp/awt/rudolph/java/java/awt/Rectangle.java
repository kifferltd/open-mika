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

package java.awt;

public class Rectangle implements Shape, java.io.Serializable {

  private static final long serialVersionUID = -4345857070255674764L;

  /****************************************************************/
  /**  public member variables
  */
  /****************************************************************/
  public int x;
  public int y;
  public int width;
  public int height;

  /****************************************************************/
  /** Constructors
  */
  /****************************************************************/

  /** void constructor  */
  public Rectangle() {
    this(0, 0, 0, 0);
  }

  /** full constructor  */
  public Rectangle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /****************************************************************/
  /** full constructor from other classes  */
  public Rectangle(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }

  public Rectangle(int width, int height) {
    this(0, 0, width, height);
  }

  public Rectangle(Point p, Dimension d) {
    this(p.x, p.y, d.width, d.height);
  }
    
  public Rectangle(Point p) {
    this(p.x, p.y, 0, 0);
   }
    
  public Rectangle(Dimension d) {
    this(0, 0, d.width, d.height);
  }


  /****************************************************************/
  /**  'null' and equality functions
  */
  /****************************************************************/
  public boolean isEmpty() {
    return ((width <= 0) || (height <= 0));
  }

  public boolean equals(Object object) {
    if(object instanceof Rectangle) {
      Rectangle other = (Rectangle)object;
      return(other.x == x && other.y == y && other.width == width && other.height == height);
    }
    return false;
  }
  /****************************************************************/
  /** Data access
  */
  /****************************************************************/

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  /****************************************************************/
  /** Boundaries
  */
  /****************************************************************/

  public Rectangle getBounds() {
    return new Rectangle(x, y, width, height);
  }  

  public void setBounds(Rectangle r) {
    setBounds(r.x, r.y, r.width, r.height);
  }

  public void setBounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
 
  }  

  /** @remark deprecated*/
  public void reshape(int x, int y, int width, int height) {
    setBounds(x, y, width, height);
  }  

  /** enlarge boundaries */
  public void grow(int h, int v) {
    x -= h;
    width += h+h;
    y -= v;
    height -= v+v;
  }

  /****************************************************************/
  /** Starting point
  */
  /****************************************************************/
  public Point getLocation() {
    return new Point(x, y);
  }  

  public void setLocation(Point p) {
    setLocation(p.x, p.y);
  }  

  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }  

  /** move Starting point   */
  public void translate(int x, int y) {
    this.x += x;
    this.y += y;
  }  

  /** @remark deprecated*/
  public void move(int x, int y) {
    this.x = x;
    this.y = y;
  }  

  /****************************************************************/
  /** Rectangle size
  */
  /****************************************************************/
  public Dimension getSize() {
    return new Dimension(width, height);
  }  

  public void setSize(Dimension d) {
    resize(d.width, d.height);
  }  

  public void setSize(int width, int height) {
    resize(width, height);
  }  

  /** deprecated  */
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
  }  

  /****************************************************************/
  /** Containing function
  */
  /****************************************************************/
  public boolean contains(Point p) {
    return contains(p.x, p.y);
  }
  
  public boolean contains(int x, int y) {
    if ((x >= this.x) && (x < this.x + this.width) && (y >= this.y) && (y < this.y + this.height)) {
      return true;
    }
    else {
      return false;
    }
  }

  /** @remark  since java 1.3 */
  public boolean contains(Rectangle other) {
    // calculate distance from x/y
    // => distance from other rectangle end (other.x+other.w) to this rectangle start (x). Must be in range (0 --- w)
    int distance = other.x + other.width -x;
    if(distance < 0 || distance > width) {
      return false;
    }
    //id for vertical bounds
    distance = other.y + other.height -y;
    if(distance < 0 || distance > height) {
      return false;
    }
    // => distance from other rectangle start (other.x) to this rectangle start (x). Must be in range (0 --- w)
    distance = other.x - x;
    if(distance < 0 || distance > width) {
      return false;
    }
    //id for vertical bounds
    distance = other.y - y;
    if(distance < 0 || distance > height) {
      return false;
    }
    return true;
  }

  /** @remark  since java 1.3 */
  public boolean contains(int other_x, int other_y, int other_width, int other_height) {
    int distance = other_x + other_width -x;
    if(distance < 0 || distance > width) {
      return false;
    }
    //id for vertical bounds
    distance = other_y + other_height -y;
    if(distance < 0 || distance > height) {
      return false;
    }
    // => distance from other rectangle start (other.x) to this rectangle start (x). Must be in range (0 --- w)
    distance = other_x - x;
    if(distance < 0 || distance > width) {
      return false;
    }
    //id for vertical bounds
    distance = other_y - y;
    if(distance < 0 || distance > height) {
      return false;
    }
    return true;
  }
   
  /*
  ** inside is deprecated.
  */
  
  public boolean inside(int x, int y) {
    if ((x >= this.x) && (x < this.x + this.width) && (y >= this.y) && (y < this.y + this.height)) {
      return true;
    }
    else {
      return false;
    }
  }

  /****************************************************************/
  /** Enlarge current rectangle to contain given Point
  */
  /****************************************************************/
  public void add(int newx, int newy) {
    if(newx < x) {
      width = width + x - newx;
      x = newx;
    }
    else if(newx > (x + width)) {
      width = newx - x;
    }

    if(newy < y) {
      height = height + y - newy;
      y = newy;
    }
    else if(newy > (y + height)) {
      height = newy - y;
    }
  }

  /**
   * @remark  instead of masking to add(x,y), this is written directly
   */
  public void add(Point pt) {
    if(pt.x < x) {
      width = width + x - pt.x;
      x = pt.x;
    }
    else if(pt.x > (x + width)) {
      width = pt.x - x;
    }

    if(pt.y < y) {
      height = height + y - pt.y;
      y = pt.y;
    }
    else if(pt.y > (y + height)) {
      height = pt.y - y;
    }
  }

  /****************************************************************/
  /** Enlarge current rectangle to contain given rectangle
  */
  /****************************************************************/
  public void add(Rectangle other) {
    int newx0 = (other.x < x)? other.x : x;
    int newx1 = ((other.x + other.width) > (x + width))? other.x + other.width : x+width;
    int newy0 = (other.y < y)? other.y : y;
    int newy1 = ((other.y + other.height) > (y + height))? other.y + other.height : y+height;

    x = newx0;
    width = newx1 - newx0;
    y = newy0;
    height = newy1 - newy0;
  }

  /****************************************************************/
  /** Intersection of two rectangles
  */
  /****************************************************************/
  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  public boolean intersects(Rectangle other){
    if((other.x + other.width) < x) {
      return false;
    }
    if((x + width) < other.x) {
      return false;
    }
    if((other.y + other.height) < y) {
      return false;
    }
    if((y + height) < other.y) {
      return false;
    }
    return true;
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  /** Rectangle c = a & b (note that the original rectangle remains unchanged)*/
  public Rectangle intersection(Rectangle other) {
    int newx0 = (other.x > x)? other.x : x;
    int newx1 = ((other.x + other.width) < (x + width))? other.x + other.width : x+width;
    if(newx0>newx1) {
      return new Rectangle();
    }
    int newy0 = (other.y > y)? other.y : y;
    int newy1 = ((other.y + other.height) < (y + height))? other.y + other.height : y+height;
    if(newy0>newy1) {
      return new Rectangle();
    }

    return new Rectangle(newx0, newy0, newx1 - newx0, newy1 - newy0);
  }

  /****************************************************************/
  /** union of two rectangles
  */
  /****************************************************************/
  /** Rectangle c = a U b (note that the original rectangle remains unchanged)*/
  public Rectangle union(Rectangle other) {
    int newx0 = (other.x < x)? other.x : x;
    int newx1 = ((other.x + other.width) > (x + width))? other.x + other.width : x+width;
    int newy0 = (other.y < y)? other.y : y;
    int newy1 = ((other.y + other.height) > (y + height))? other.y + other.height : y+height;

    return new Rectangle(newx0, newy0, newx1 - newx0, newy1 - newy0);
  }



  /****************************************************************/
  /** Diagnostics
  */
  /****************************************************************/
  public String toString() {
    return getClass().getName() + "[x = " + x + ", y = " + y + ", width = " + width + ", height = " + height + "]";
  }
}
