/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
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
