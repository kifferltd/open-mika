/**************************************************************************
* Copyright  (c){ return Math. ;} 2001 by Acunia N.V. All rights reserved.                 *
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
** $Id: StrictMath.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.lang;

public final class StrictMath{

  private StrictMath(){}

  public static final double E = 2.718281828459045d;
  public static final double PI = 3.141592653589793d;


  public static double abs(double d){
    return Math.abs(d);
  }

  public static float abs(float f){
    return Math.abs(f);
  }

  public static int abs(int i){
    return Math.abs(i);
  }

  public static long abs(long l){
    return Math.abs(l);
  }

  public static double acos(double d){
    return Math.acos(d);
  }

  public static double asin(double d){
    return Math.asin(d);
  }

  public static double atan(double d){
    return Math.atan(d);
  }

  public static double atan2(double d, double e){
    return Math.atan2(d,e);
  }

  public static double ceil(double d){
    return Math.ceil(d);
  }

  public static double cos(double d){
    return Math.cos(d);
  }

  public static double exp(double d){
    return Math.exp(d);
  }

  public static double floor(double d){
    return Math.floor(d);
  }
  public static double IEEEremainder(double d, double e){
    return Math.IEEEremainder(d,e);
  }

  public static double log(double d){
    return Math.log(d);
  }

  public static double max(double d, double e){
    return Math.max(d,e);
  }

  public static float max(float f, float g){
    return Math.max(f,g);
  }

  public static int max(int i, int j){
    return Math.max(i,j);
  }

  public static long max(long l, long m){
    return Math.max(l,m);
  }

  public static double min(double d, double e){
    return Math.min(d,e);
  }

  public static float min(float f, float g){
    return Math.min(f,g);
  }

  public static int min(int i, int j){
    return Math.min(i,j);
  }

  public static long min(long l, long m){
    return Math.min(l,m);
  }

  public static double pow(double d, double e){
    return Math.pow(d,e);
  }

  public static double random(){
    return Math.random();
  }

  public static double rint(double d){
    return Math.rint(d);
  }

  public static long round(double d){
    return Math.round(d);
  }

  public static int round(float a){
    return Math.round(a);
  }

  public static double sin(double d){
    return Math.sin(d);
  }

  public static double sqrt(double d){
    return Math.sqrt(d);
  }

  public static double tan(double d){
    return Math.tan(d);
  }

  public static double toDegrees(double d){
    return Math.toDegrees(d);
  }

  public static double toRadians(double d){
    return Math.toRadians(d);
  }
}

