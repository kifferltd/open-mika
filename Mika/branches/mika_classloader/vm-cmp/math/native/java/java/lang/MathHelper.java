/**************************************************************************
* Copyright  (c) 2005 by Chris Gray, /k/ Embedded Java Solutions.         *
* All rights reserved.                                                    *
**************************************************************************/

package java.lang;

/**
 * A helper class for java.lang.Math; only visible within java.lang. 
 * This version contains all-java implementations of the various algorithms; 
 * see under the math/java source tree for a version using native code.
 */
class MathHelper {
  /**
   ** Convert a float when we already know that
   ** it is finite, positive, non-Nan, and between 0.001 and 10000000.
   */
  static native String toString_internal(float absvalue);

  /**
   ** Convert a double when we already know that
   ** it is finite, positive, non-Nan, and between 0.001 and 10000000.
   */
  static native String toString_internal(double absvalue);

}


