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
*                                                                         *
* Enhancements copyright (C) 2005 by Chris Gray, /k/ Embedded Java        *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: Array.java,v 1.2 2005/07/02 12:52:52 cvs Exp $
*/

package java.lang.reflect;

public final class Array {

  /*
  ** This class is a utility: it cannot be instantiated.
  */
  private Array(){}

  public static native Object newInstance(Class componentType, int length)
    throws NullPointerException, NegativeArraySizeException;
    
  public static native Object newInstance(Class componentType, int dimensions[])
    throws NullPointerException, IllegalArgumentException, NegativeArraySizeException;
  
  public static native int getLength(Object array)
    throws NullPointerException, IllegalArgumentException;
  
  public static native Object get(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException;

  public static boolean getBoolean(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
    boolean [] za;
    try {
      za = (boolean[])array;
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException(array + " is not an array of boolean");
    }
    return za[index];
  }

  public static byte getByte(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
    byte [] ba;
    try {
      ba = (byte[])array;
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException(array + " is not an array of byte");
    }
    return ba[index];
  }

  public static char getChar(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
    char [] ca;
    try {
      ca = (char[])array;
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException(array + " is not an array of char");
    }
    return ca[index];
  }

  public static short getShort(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
      Class arrayclass = array.getClass().getComponentType();
      if ( arrayclass == null) throw new IllegalArgumentException(array + " is not an Array");
      /* [CG 20050702] WAS:
      if (!arrayclass.isPrimitive()) throw new IllegalArgumentException(array + " is not an Array of primitive types");
      if (!arrayclass.toString().equals("byte") && !arrayclass.toString().equals("short"))
       	throw new IllegalArgumentException(array + " is not an Array of short or byte");

      Number nm;
      try {
          nm = (Number)Array.get(array , index);
          }
      catch (ClassCastException cce) { throw new IllegalArgumentException(array + " is not an Array of numeric primitive types");}
      return nm.shortValue();
      */

      if (arrayclass == byte.class) {
        return ((byte[])array)[index];
      }
      else if (arrayclass == short.class) {
        return ((short[])array)[index];
      }
      else {
       	throw new IllegalArgumentException(array + " is not an array of short or byte");
      }
  }

  public static int getInt(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
      Class arrayclass = array.getClass().getComponentType();
      if ( arrayclass == null) throw new IllegalArgumentException(array + " is not an Array");
    /* [CG 20050702] WAS:
      if (!arrayclass.isPrimitive()) throw new IllegalArgumentException(array + " is not an Array of primitive types");
      if (arrayclass.toString().equals("char")) { return (int)getChar(array,index);}
      if (!arrayclass.toString().equals("byte") && !arrayclass.toString().equals("short") && !arrayclass.toString().equals("int"))
      	throw new IllegalArgumentException("wrong primitive array type");
      Number nm;
      try {
          nm = (Number)Array.get(array , index);
          }
      catch (ClassCastException cce) { throw new IllegalArgumentException(array + " is not an Array of numeric primitive types");}
      return nm.intValue();
    */

      if (arrayclass == int.class) {
        return ((int[])array)[index];
      }
      else if (arrayclass == short.class) {
        return ((short[])array)[index];
      }
      else if (arrayclass == char.class) {
        return ((char[])array)[index];
      }
      else if (arrayclass == byte.class) {
        return ((byte[])array)[index];
      }
      else {
       	throw new IllegalArgumentException(array + " is not an array of int, char, short or byte");
      }
  }

  public static long getLong(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
      Class arrayclass = array.getClass().getComponentType();
      if ( arrayclass == null) throw new IllegalArgumentException(array + " is not an Array");
    /* [CG 20050702] WAS:
      if (!arrayclass.isPrimitive()) throw new IllegalArgumentException(array + " is not an Array of primitive types");
      if (arrayclass.toString().equals("char")) { return (long)getChar(array,index);}
      if (arrayclass.toString().equals("float") || arrayclass.toString().equals("double"))
      	throw new IllegalArgumentException("wrong primitive array type");
      Number nm;
      try {
          nm = (Number)Array.get(array , index);
          }
      catch (ClassCastException cce) { throw new IllegalArgumentException(array + " is not an Array of numeric primitive types");}
      return nm.longValue();
    */

      if (arrayclass == long.class) {
        return ((long[])array)[index];
      }
      else if (arrayclass == int.class) {
        return ((int[])array)[index];
      }
      else if (arrayclass == short.class) {
        return ((short[])array)[index];
      }
      else if (arrayclass == char.class) {
        return ((char[])array)[index];
      }
      else if (arrayclass == byte.class) {
        return ((byte[])array)[index];
      }
      else {
       	throw new IllegalArgumentException(array + " is not an array of int, char, short or byte");
      }
  }

  public static float getFloat(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
      Class arrayclass = array.getClass().getComponentType();
      if ( arrayclass == null) throw new IllegalArgumentException(array + " is not an Array");
    /* [CG 20050702] WAS:
      if (!arrayclass.isPrimitive()) throw new IllegalArgumentException(array + " is not an Array of primitive types");
      if (arrayclass.toString().equals("char")) { return (float)getChar(array,index);}
      if (arrayclass.toString().equals("double"))
      	throw new IllegalArgumentException("wrong primitive array type");
      Number nm;
      try {
          nm = (Number)Array.get(array , index);
          }
      catch (ClassCastException cce) { throw new IllegalArgumentException(array + " is not an Array of numeric primitive types");}
      return nm.floatValue();
    */

      if (arrayclass == float.class) {
        return ((float[])array)[index];
      }
      else if (arrayclass == long.class) {
        return ((long[])array)[index];
      }
      else if (arrayclass == int.class) {
        return ((int[])array)[index];
      }
      else if (arrayclass == short.class) {
        return ((short[])array)[index];
      }
      else if (arrayclass == char.class) {
        return ((char[])array)[index];
      }
      else if (arrayclass == byte.class) {
        return ((byte[])array)[index];
      }
      else {
       	throw new IllegalArgumentException(array + " is not an array of int, char, short or byte");
      }
  }

  public static double getDouble(Object array, int index)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
      Class arrayclass = array.getClass().getComponentType();
      if ( arrayclass == null) throw new IllegalArgumentException(array + " is not an Array");
    /* [CG 20050702] WAS:
      if (!arrayclass.isPrimitive()) throw new IllegalArgumentException(array + " is not an Array of primitive types");
      if (arrayclass.toString().equals("char")) { return (double)getChar(array,index);}
      Number nm;
      try {
          nm = (Number)Array.get(array , index);
          }
      catch (ClassCastException cce) { throw new IllegalArgumentException(array + " is not an Array of numeric primitive types");}
      return nm.doubleValue();
    */

      // [CG 20050702] HAD ONCE BEEN:
    /*
    double [] da;
    try {
      da = (double[]) array;
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException();
    }
    return da[index]; 
    */

      // [CG 20050702] NOW:

      if (arrayclass == double.class) {
        return ((double[])array)[index];
      }
      else if (arrayclass == float.class) {
        return ((float[])array)[index];
      }
      else if (arrayclass == long.class) {
        return ((long[])array)[index];
      }
      else if (arrayclass == int.class) {
        return ((int[])array)[index];
      }
      else if (arrayclass == short.class) {
        return ((short[])array)[index];
      }
      else if (arrayclass == char.class) {
        return ((char[])array)[index];
      }
      else if (arrayclass == byte.class) {
        return ((byte[])array)[index];
      }
      else {
       	throw new IllegalArgumentException(array + " is not an array of int, char, short or byte");
      }
  }

  private static final Class Boolean_class = Boolean.class;
  private static final Class Byte_class = Byte.class;
  private static final Class Short_class = Short.class;
  private static final Class Character_class = Character.class;
  private static final Class Integer_class = Integer.class;
  private static final Class Long_class = Long.class;
  private static final Class Float_class = Float.class;
  private static final Class Double_class = Double.class;

  public static void set(Object array, int index, Object value)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException 
  {
    Class arrayclass = array.getClass().getComponentType();
    if ( arrayclass == null) {
      throw new IllegalArgumentException(array + " is not an Array");
    }

    if (arrayclass.isPrimitive()) {
      Class value_class = value.getClass();
      if (value_class == Boolean_class && arrayclass == boolean.class) {
        ((boolean[])array)[index] = ((Boolean)value).booleanValue();
      }
      else if (value_class == Byte_class && arrayclass == byte.class) {
        ((byte[])array)[index] = ((Byte)value).byteValue();
      }
      else if (value_class == Byte_class && arrayclass == short.class) {
        ((short[])array)[index] = ((Byte)value).byteValue();
      }
      else if (value_class == Byte_class && arrayclass == int.class) {
        ((int[])array)[index] = ((Byte)value).byteValue();
      }
      else if (value_class == Byte_class && arrayclass == long.class) {
        ((long[])array)[index] = ((Byte)value).byteValue();
      }
      else if (value_class == Byte_class && arrayclass == float.class) {
        ((float[])array)[index] = ((Byte)value).byteValue();
      }
      else if (value_class == Byte_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Byte)value).byteValue();
      }
      else if (value_class == Short_class && arrayclass == short.class) {
        ((short[])array)[index] = ((Short)value).shortValue();
      }
      else if (value_class == Short_class && arrayclass == int.class) {
        ((int[])array)[index] = ((Short)value).shortValue();
      }
      else if (value_class == Short_class && arrayclass == long.class) {
        ((long[])array)[index] = ((Short)value).shortValue();
      }
      else if (value_class == Short_class && arrayclass == float.class) {
        ((float[])array)[index] = ((Short)value).shortValue();
      }
      else if (value_class == Short_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Short)value).shortValue();
      }
      else if (value_class == Character_class && arrayclass == char.class) {
        ((char[])array)[index] = ((Character)value).charValue();
      }
      else if (value_class == Character_class && arrayclass == int.class) {
        ((int[])array)[index] = ((Character)value).charValue();
      }
      else if (value_class == Character_class && arrayclass == long.class) {
        ((long[])array)[index] = ((Character)value).charValue();
      }
      else if (value_class == Character_class && arrayclass == float.class) {
        ((float[])array)[index] = ((Character)value).charValue();
      }
      else if (value_class == Character_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Character)value).charValue();
      }
      else if (value_class == Integer_class && arrayclass == int.class) {
        ((int[])array)[index] = ((Integer)value).intValue();
      }
      else if (value_class == Integer_class && arrayclass == long.class) {
        ((long[])array)[index] = ((Integer)value).intValue();
      }
      else if (value_class == Integer_class && arrayclass == float.class) {
        ((float[])array)[index] = ((Integer)value).intValue();
      }
      else if (value_class == Integer_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Integer)value).intValue();
      }
      else if (value_class == Long_class && arrayclass == long.class) {
        ((long[])array)[index] = ((Long)value).longValue();
      }
      else if (value_class == Long_class && arrayclass == float.class) {
        ((float[])array)[index] = ((Long)value).longValue();
      }
      else if (value_class == Long_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Long)value).longValue();
      }
      else if (value_class == Float_class && arrayclass == float.class) {
        ((float[])array)[index] = ((Float)value).floatValue();
      }
      else if (value_class == Float_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Float)value).floatValue();
      }
      else if (value_class == Double_class && arrayclass == double.class) {
        ((double[])array)[index] = ((Double)value).doubleValue();
      }
      else {
        throw new IllegalArgumentException("Cannot assign element of type " + value_class + " to array of " + arrayclass);
      }
    }
    else {
      try {
        ((Object[])array)[index] = value;
      }
      catch (ArrayStoreException ase) {
        throw new IllegalArgumentException("Cannot assign " + value + " to " + array);
      }
    }
  }

  public static void setBoolean(Object array, int index, boolean z)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == boolean.class) {
      ((boolean[])array)[index] = z;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of boolean");
    }
  }

  public static void setByte(Object array, int index, byte b)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == byte.class) {
      ((byte[])array)[index] = b;
    }
    else if (arrayclass == short.class) {
      ((short[])array)[index] = b;
    }
    else if (arrayclass == int.class) {
      ((int[])array)[index] = b;
    }
    else if (arrayclass == long.class) {
      ((long[])array)[index] = b;
    }
    else if (arrayclass == float.class) {
      ((float[])array)[index] = b;
    }
    else if (arrayclass == double.class) {
      ((double[])array)[index] = b;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of byte, short, int, long, float, or double");
    }
  }

  public static void setChar(Object array, int index, char c)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == char.class) {
      ((char[])array)[index] = c;
    }
    else if (arrayclass == int.class) {
      ((int[])array)[index] = c;
    }
    else if (arrayclass == long.class) {
      ((long[])array)[index] = c;
    }
    else if (arrayclass == float.class) {
      ((float[])array)[index] = c;
    }
    else if (arrayclass == double.class) {
      ((double[])array)[index] = c;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of char, int, long, float, or double");
    }
  }

  public static void setShort(Object array, int index, short s)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == short.class) {
      ((short[])array)[index] = s;
    }
    else if (arrayclass == int.class) {
      ((int[])array)[index] = s;
    }
    else if (arrayclass == long.class) {
      ((long[])array)[index] = s;
    }
    else if (arrayclass == float.class) {
      ((float[])array)[index] = s;
    }
    else if (arrayclass == double.class) {
      ((double[])array)[index] = s;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of short, int, long, float, or double");
    }
  }

  public static void setInt(Object array, int index, int i)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == int.class) {
      ((int[])array)[index] = i;
    }
    else if (arrayclass == long.class) {
      ((long[])array)[index] = i;
    }
    else if (arrayclass == float.class) {
      ((float[])array)[index] = i;
    }
    else if (arrayclass == double.class) {
      ((double[])array)[index] = i;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of int, long, float, or double");
    }

  }

  public static void setLong(Object array, int index, long l)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == long.class) {
      ((long[])array)[index] = l;
    }
    else if (arrayclass == float.class) {
      ((float[])array)[index] = l;
    }
    else if (arrayclass == double.class) {
      ((double[])array)[index] = l;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of long, float, or double");
    }
  }

  public static void setFloat(Object array, int index, float f)
  throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == float.class) {
      ((float[])array)[index] = f;
    }
    else if (arrayclass == double.class) {
      ((double[])array)[index] = f;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of float or double");
    }
  }


  public static void setDouble(Object array, int index, double d)
    throws NullPointerException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
    Class arrayclass = array.getClass().getComponentType();
    if (arrayclass == double.class) {
      ((double[])array)[index] = d;
    }
    else {
      throw new IllegalArgumentException(array + " is not an array of double");
    }
  }

}
