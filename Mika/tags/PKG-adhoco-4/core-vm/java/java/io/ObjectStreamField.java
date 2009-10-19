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
** $Id: ObjectStreamField.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.io;

import java.lang.reflect.Field;

public class ObjectStreamField implements Comparable {

  Class type;
  char code;
  String name;
  String typeString;
  boolean isPrimitive;

  /** will only be set by the ObjectStreamClass when needed ! */
  Field field;
  private boolean unshared;

  private native void create(String name, Class type);

  public ObjectStreamField (String name, Class type) {
    create(name, type);
  }

  public ObjectStreamField (String name, Class type, boolean unshared) {
    this.unshared = unshared;
    create(name, type);
  }

  ObjectStreamField(String name, String desc, char code){
    this.name = name;
    typeString = desc;
    this.code = code;
  }

  public boolean isUnshared() {
    return unshared; 
  }
  
  public String getName() {
    return this.name;
  }
  
  public Class getType() {
    return this.type;
  }

  public char getTypeCode() {
    return this.code;
  }

  public String getTypeString(){
    return typeString;
  }

  public boolean isPrimitive() {
    return isPrimitive;
  }

  public String toString() {
    return "ObjectStreamField: name = '"+name+"' of "+type;
  }

  public int compareTo (Object o) {
    ObjectStreamField target = (ObjectStreamField)o;
    boolean p = isPrimitive;
    int answer;
    if (p != target.isPrimitive) {
      answer =  (p ? -1 : 1 );
    }
    else {
     answer = name.compareTo(target.name);
    }
    return answer;
  }

  //TODO ...

  public int getOffset(){
    System.out.println("method java.io.ObjectStreamField.getOffset() is not implemented");
    return -1;
  }

  protected void setOffset(int off){
    System.out.println("method java.io.ObjectStreamField.setOffset(int) is not implemented");
  }


}
