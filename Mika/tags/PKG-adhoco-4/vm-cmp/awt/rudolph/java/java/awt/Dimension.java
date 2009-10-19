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

