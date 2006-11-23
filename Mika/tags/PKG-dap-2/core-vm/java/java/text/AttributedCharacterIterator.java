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