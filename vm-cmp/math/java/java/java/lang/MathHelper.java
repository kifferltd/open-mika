/**************************************************************************
* Copyright (c) 2023 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

package java.lang;

/**
 * A helper class for java.lang.Math; only visible within java.lang. 
 * This version contains all-java implementations of the various algorithms; 
 * see under the vm-cmp/math/native source tree for a version using native code.
 */
class MathHelper {
  /**
   ** Convert a float when we already know that
   ** it is finite, positive, non-Nan, and between 0.001 and 10000000.
   **<p>As with so many things, the problem is knowing when to stop.
   ** We want to print just enough digits so that when the result is
   ** converted back to an ideal (infinite precision) real number, the
   ** closest number to that real number that can be represented as a 
   ** float is the number we started with.  Our approach is to calculate
   ** delta = the difference between the float we are printing and the
   ** adjacent floats, and to stop when we are within delta of our target.
   */
  static String toString_internal(float absvalue) {
    float delta = (absvalue - Float.intBitsToFloat(Float.floatToIntBits(absvalue) - 1));
    float residue = absvalue - (int)absvalue;

    StringBuffer buffer = new StringBuffer(32);

    buffer.append((int)absvalue);
    buffer.append('.');
    double pow10 = 10.0;
    int nextdigit = (int)(residue * pow10);
    buffer.append(Character.forDigit(nextdigit, 10));
    residue -= nextdigit / pow10;
    while (residue > delta) {
      pow10 *= 10.0;
      nextdigit = (int)(residue * pow10);
      if (nextdigit < 9 && ((nextdigit+1) / pow10) - residue < delta) {
    ++nextdigit;
      }
      buffer.append(Character.forDigit(nextdigit, 10));
      residue -= nextdigit / pow10;
    }

    return buffer.toString();
  }

  /**
   ** Convert a double when we already know that
   ** it is finite, positive, non-Nan, and between 0.001 and 10000000.
   **<p>As with so many things, the problem is knowing when to stop.
   ** We want to print just enough digits so that when the result is
   ** converted back to an ideal (infinite precision) real number, the
   ** closest number to that real number that can be represented as a 
   ** double is the number we started with.  Our approach is to calculate
   ** delta = the difference between the double we are printing and an
   ** adjacent double, and to stop when we are within delta of our target.
   */
  static String toString_internal(double absvalue) {
    double delta = (absvalue - Double.longBitsToDouble(Double.doubleToLongBits(absvalue) ^ 1));
    if (delta < 0) {
      delta = -delta;
    }
    double residue = absvalue - (int)absvalue;

    StringBuffer buffer = new StringBuffer(32);

    buffer.append((int)absvalue);
    buffer.append('.');
    double pow10 = 10.0;
    int nextdigit = (int)(residue * pow10);
    buffer.append((char)('0'+nextdigit));
/* WAS :
    residue -= nextdigit / pow10;
    while (residue > delta) {
      pow10 *= 10.0;
      nextdigit = (int)(residue * pow10);
      buffer.append((char)('0'+nextdigit));
      residue -= nextdigit / pow10;
    }
*/

/* THEN WE TRIED:
    double powmin10 = 0.1;
    double sofar = (int)absvalue + nextdigit * powmin10;
    residue = absvalue - sofar;
    while (residue > delta) {
      pow10 *= 10.0;
      powmin10 /= 10.0;
      nextdigit = (int)(residue * pow10);
      buffer.append((char)('0'+nextdigit));
      sofar += nextdigit * powmin10;
      residue = absvalue - sofar;
    }
*/

    residue *= 10.0;
    delta *= 10.0;
    residue -= nextdigit;
    while (residue > delta) {
      nextdigit = (int)(residue * pow10);
      buffer.append((char)('0'+nextdigit));
      residue *= 10.0;
      delta *= 10.0;
      residue -= nextdigit;
    }

    boolean carry;
    int l = buffer.length();

    if(l >= 18 ) {
      int k = -1;
      int d17 = Character.forDigit(buffer.charAt(17), 10);
//      int d18 = l > 18 ? Character.forDigit(buffer.charAt(18), 10) : 5;
//      carry = residue > delta * 0.5 || d18 > 5 || d17 == 9 || (d18 == 5 /* && (d17 % 2) == 1 */ );
      if (l > 18 && d17 > 5) { // HACK HACK HACK
        buffer.setCharAt(17, '9');
        d17 = 9;
      }
      carry = residue > delta * 0.5 || d17 == 9 || l > 18;
      l = 18;

      if (carry) {
        while (l > 0 && carry) {
          char ch = buffer.charAt(l - 1);
          switch (ch) {
          case '.':
            k = l;
            --l;
            break;

          case '9':
            buffer.setCharAt(--l, '0');
            break;

          default:
            buffer.setCharAt(l - 1, (char)(ch + 1));
            carry = false;
          }
        }

        if (carry) {
          buffer.insert(0, '1');
          ++k;
          ++l;
        }
        if (k > l) {
          l = k + 1;
        }
      }
      else {
        while (buffer.charAt(l - 1) == '0') {
          --l;
        }
        if (buffer.charAt(l - 1) == '.') {
          ++l;
        }
      }
      buffer.setLength(l);
    }

    // BEGIN UGLY HACK
    /*
    if (l > 16 && buffer.substring(l - 9, l - 1).equals("99999999")) {
      // System.out.println("HACK HACK HACK : WAS " + buffer.toString());
      l -= 9;
      int k = 0;
      carry = true;
      while (l > 0 && carry) {
        char ch = buffer.charAt(l - 1);
        switch (ch) {
        case '.':
          k = l;
          --l;
          break;

        case '9':
          buffer.setCharAt(--l, '0');
          break;

        default:
          buffer.setCharAt(l - 1, (char)(ch + 1));
          carry = false;
        }
      }

      if (carry) {
        buffer.insert(0, '1');
        ++k;
        ++l;
      }
      buffer.setLength(k > l ? k + 1 : l);
      // System.out.println("HACK HACK HACK : NOW " + buffer.toString());
    }
    else if (l > 17 && buffer.substring(l - 9, l - 1).equals("00000000")) {
      // System.out.println("HACK HACK HACK : WAS " + buffer.toString());
      l -= 9;
      while (buffer.charAt(l - 1) == '0') {
        --l;
      }
      if (buffer.charAt(l - 1) == '.') {
        ++l;
      }
      buffer.setLength(l);
      // System.out.println("HACK HACK HACK : NOW " + buffer.toString());
    }
    */
    // END UGLY HACK

    //System.out.println("translated '"+Long.toHexString(Double.doubleToLongBits(absvalue))+"' to '"+buffer+"'");
    return buffer.toString();
  }

}

