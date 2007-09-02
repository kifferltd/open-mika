/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

/*
** $Id: AttributedCharacterIterator.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.Set;
import java.util.Map;
import java.io.InvalidObjectException;
import java.io.Serializable;

public interface AttributedCharacterIterator extends CharacterIterator {

  public Set getAllAttributeKeys();
  public Object getAttribute(Attribute attri);
  public Map getAttributes();
  public int getRunLimit();
  public int getRunLimit(Attribute attri);
  public int getRunLimit(Set attributes);
  public int getRunStart();
  public int getRunStart(Attribute attri);
  public int getRunStart(Set attributes);

  public static class Attribute implements Serializable {

    private static final long serialVersionUID = -9142742483513960612L;

    public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");
    public static final Attribute LANGUAGE = new Attribute("language");
    public static final Attribute READING = new Attribute("reading");

    //TODO add readObject to check class invariants ...

    private String name;

    protected Attribute(String name){
      this.name = name;
    }

    public final boolean equals(Object o){
      return (this == o);
    }

    protected String getName(){
      return name;
    }

    public final int hashCode(){
      return super.hashCode();
    }

    /**
    ** According to the specs this method must be overriden by subclasses.
    ** the default implementation just throws an InvalidObjectException ...
    */
    protected Object readResolve() throws InvalidObjectException {
      throw new InvalidObjectException("readResolve was not overriden by subclass");
    }

    public String toString(){
      return this.getClass().getName()+'('+name+')';
    }
  }

}
