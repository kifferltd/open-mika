/**************************************************************************
* Copyright (c) 2012 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written            *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER                  *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.awt;

/**
 ** The parent class of all AWT components.
 ** Hopelessly incomplete dummy implementation just so that stuff like javax.beans can compile.
 */
public abstract class Component implements java.awt.image.ImageObserver, MenuContainer, java.io.Serializable {

  public static void revertFocus() {
	throw new HeadlessException();;
  }

  public static Component getFocusComponent() {
	throw new HeadlessException();;
  }
  
  public void setName(String name) {
	throw new HeadlessException();;
  }

  public String getName() {
	throw new HeadlessException();;
  }

  public void setForeground(Color color) {
	throw new HeadlessException();;
  }

  public Color getForeground() {
	throw new HeadlessException();;
  }

  public void setBackground(Color color) {
	throw new HeadlessException();;
  }

  public Color getBackground() {
	throw new HeadlessException();;
  }

  public void setBounds(int x, int y, int width, int height) {
	throw new HeadlessException();;
  }

  public void setBounds(Rectangle rectangle) {
	throw new HeadlessException();;
  }

  public Rectangle getBounds() {
	throw new HeadlessException();;
  }

  public void setSize(int w, int h) {
	throw new HeadlessException();;
  }

}


