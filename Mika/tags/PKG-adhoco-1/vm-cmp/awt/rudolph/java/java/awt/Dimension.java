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

public class Dimension implements java.io.Serializable {
  
  private static final long serialVersionUID = 4723952579491349524L;

  /*
  ** variables
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
   
  public int height;
  public int width;

  /*
  ** Constructors
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public Dimension() {
    this(0,0);
  }
  
  /*
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public Dimension(Dimension dimension) {
    this(dimension.width, dimension.height);
  }

  /*
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public Dimension(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  /*
  ** Equality function
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public boolean equals(Object obj) {
    if (obj instanceof Dimension) {
       Dimension dimension = (Dimension)obj;
       return (width == dimension.width) && (height == dimension.height);
     }
     else {
       return false;
     }
  }
  
  /*
  ** data access
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public Dimension getSize() {
    return new Dimension(this.width, this.height);
  }

  /*
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public double getWidth() {
    return width;
  }

  /*
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public double getHeight() {
    return height;
  }
  
  /*
  ** data manipulation
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public void setSize(Dimension dimension) {
    this.width = dimension.width;
    this.height = dimension.height;
  }
  
  /*
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */
  
  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  /*
  ** String function
  ** @status  Complient with java specs
  ** @remark  Complient with java specs
  */

  public String toString() {
    return getClass().getName() + "[width = " + width + ", height = " + height + "]";
  }
}

