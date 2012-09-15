/**************************************************************************
* Copyright (c) 2012 by Chris Gray. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Chris Gray nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL CHRIS GRAY OR OTHER CONTRIBUTORS BE LIABLE  FOR ANY   *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,WHETHER     *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE  OR        *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN     *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.awt;

/**
 ** Dummy implemenation just to be able to compile java beans.
 */
public class Rectangle implements Shape, java.io.Serializable {

  private static final long serialVersionUID = -4345857070255674764L;

  /** void constructor  */
  public Rectangle() {
    this(0, 0, 0, 0);
  }

  /** full constructor  */
  public Rectangle(int x, int y, int width, int height) {
  }

  public Rectangle(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }

  public Rectangle(int width, int height) {
    this(0, 0, width, height);
  }

  public boolean isEmpty() {
    throw new HeadlessException();
  }

  public boolean equals(Object object) {
    throw new HeadlessException();
  }
}

