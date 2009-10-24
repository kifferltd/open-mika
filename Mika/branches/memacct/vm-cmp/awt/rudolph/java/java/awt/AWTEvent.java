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

public class AWTEvent extends java.util.EventObject {

  private static final long serialVersionUID = -1825314779160409405L;
  
  /****************************************************************/
  /** definitions */
  public final static long ACTION_EVENT_MASK       = 0x00000000000000080;
  public final static long ADJUSTMENT_EVENT_MASK   = 0x00000000000000100;
  public final static long ITEM_EVENT_MASK         = 0x00000000000000200;
  public final static long TEXT_EVENT_MASK         = 0x00000000000000400;
  public final static long COMPONENT_EVENT_MASK    = 0x00000000000000001;
  public final static long CONTAINER_EVENT_MASK    = 0x00000000000000002;
  public final static long FOCUS_EVENT_MASK        = 0x00000000000000004;
  public final static long KEY_EVENT_MASK          = 0x00000000000000008;
  public final static long MOUSE_EVENT_MASK        = 0x00000000000000010;
  public final static long MOUSE_MOTION_EVENT_MASK = 0x00000000000000020;
  public final static long WINDOW_EVENT_MASK       = 0x00000000000000040;

  public final static int RESERVED_ID_MAX = 1999;
  
  /****************************************************************/
  /** variables */
  //protected Object  EventObject.source;
  protected int id;
  protected boolean consumed;

  /****************************************************************/
  /** constructor */
  public AWTEvent(Object source, int id) {
    super(source);

    this.id = id;
  }
  
  public AWTEvent(Event event) {
    super(event.target);
    this.id = event.id;
  }

  /****************************************************************/
  /** get function ID */
  public int getID() {
    return id;
  }

  /****************************************************************/
  /** Consume/is consumed */

  /** protected function consume: designed to be overridden by derived when they want their own event consuming */
  protected void consume() {
    consumed = true;
  }

  /** protected acces to flag consumed */
  protected boolean isConsumed() {
    return consumed;
  }


  /****************************************************************/
  /** Diagnostics */
  public String toString() {

    String name = (source instanceof Component)? ((Component)source).getName() : null;
    if (name == null){
      name = source.toString();
    }

    return this.getClass().getName() + ": " + name;
  }
   
  public String paramString() {
    return getClass().getName() +"[Function id="+id+"] from="+source;
  }
}
