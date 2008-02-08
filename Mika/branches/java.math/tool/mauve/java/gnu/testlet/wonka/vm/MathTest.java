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


package gnu.testlet.wonka.vm;


public class MathTest {

  static boolean near(double a, double b) {
  
    double should_be_zero = a;
    double four_epsilon = +8.8817841970012523e-16;

    /*
    ** Check signs first
    */
    
    if (b > 0.0 && a < 0.0) {
      System.out.println("B (" + b + ") is negative while A (" + a + ") is positive.");
      return false;
    }

    if (b < 0.0 && a > 0.0) {
      System.out.println("B (" + b + ") is positive while A (" + a + ") is negative.");
      return false;
    }
    
    /*
    ** Now check epsilon
    */
    
    if (b != 0.0) {
      should_be_zero = Math.abs((b - a) / b);
    }

    if (should_be_zero > four_epsilon) {
      System.out.println("NEAR: result " + a + " is not near enough to " + b);
      return false;
    }

    return true;
    
  }
  
  public static int test() {

    if (Math.E != 2.7182818284590452354) {
      return 140;
    }
    if (Math.PI != 3.14159265358979323846) {
      return 150;
    }
    if (! (new Double(Math.sin(Double.NaN))).isNaN()) {
      return 160;
    }
    if (! (new Double(Math.sin( Double.POSITIVE_INFINITY ))).isNaN()) {
      return 170;
    }
    if (! (new Double(Math.sin( Double.NEGATIVE_INFINITY ))).isNaN()) {
      return 180;
    }
    if (Math.sin( -0.0 ) != -0.0 ) {
      return 190;
    } 
    if (Math.sin( 0.0 ) != 0.0 ) {
      return 200;
    } 
    if (! (new Double(Math.cos( Double.NaN ))).isNaN()) {
      return 210;
    }
    if (! (new Double(Math.cos( Double.POSITIVE_INFINITY ))).isNaN()) {
      return 220;
    }
    if (! (new Double(Math.cos( Double.NEGATIVE_INFINITY ))).isNaN()) {
      return 230;
    }
    if (! (new Double(Math.tan( Double.NaN ))).isNaN()) {
      return 240;
    }
    if (! (new Double(Math.tan( Double.POSITIVE_INFINITY ))).isNaN()) {
      return 250;
    }
    if (! (new Double(Math.tan( Double.NEGATIVE_INFINITY ))).isNaN()) {
      return 260;
    }
    if (Math.tan( -0.0 ) != -0.0) {
      return 270;
    }
    if (Math.tan( 0.0 ) != 0.0) {
      return 280;
    }
    if (Math.sin( Math.PI / 2.0 + Math.PI /6.0 ) <= 0.0) {
      return 290;
    }
    if (Math.cos( Math.PI / 2.0 + Math.PI /6.0 ) >= 0.0) {
      return 300;
    }
    if (Math.tan( Math.PI / 2.0 + Math.PI /6.0 ) >= 0.0) {
      return 310;
    }

    if (! (new Double(Math.asin( Double.NaN ))).isNaN()) {
      return 320;
    }

    if (Math.asin( -0.0 ) != -0.0 ) {
      return 330;
    }
    if (Math.asin( 0.0 ) != 0.0 ) {
      return 340;
    }
    if (! (new Double(Math.asin( 10.0 ))).isNaN()) {
      return 350;
    }
    if (! (new Double(Math.acos( Double.NaN ))).isNaN()) {
      return 360;
    }
    if (! (new Double(Math.acos( 10.0 ))).isNaN()) {
      return 370;
    }
    if (! (new Double(Math.atan( Double.NaN ))).isNaN()) {
      return 380;
    }
    if (Math.atan( -0.0 ) != -0.0) {
      return 390;
    }
    if (Math.atan( 0.0 ) != 0.0) {
      return 400;
    }
    if (! (new Double( Math.atan2 (1.0 , Double.NaN ))).isNaN()) {
      return 410;
    }
    if (! (new Double( Math.atan2 (Double.NaN,1.0 ))).isNaN()) {
      return 420;
    }

    if (Math.atan2(0.0, 10.0 ) != -0.0 || Math.atan2(2.0, Double.POSITIVE_INFINITY) != -0.0) {
      return 430;
    }

    if (Math.atan2(-0.0, 10.0 ) != -0.0 || Math.atan2(-2.0 , Double.POSITIVE_INFINITY ) != -0.0) {
      return 440;
    }

    if (Math.atan2(0.0, -10.0 ) != Math.PI || Math.atan2(2.0 , Double.NEGATIVE_INFINITY ) != Math.PI) {
      return 450;
    }

    if (Math.atan2(-0.0, -10.0 ) != -Math.PI || Math.atan2(-2.0 , Double.NEGATIVE_INFINITY ) != -Math.PI) {
      return 460;
    }
   
    if (Math.atan2(10.0, 0.0 ) != Math.PI/2.0 || Math.atan2(Double.POSITIVE_INFINITY , 3.0) != Math.PI /2.0) {
      return 470;
    }
    if (Math.atan2(-10.0, 0.0 ) != -Math.PI/2.0 || Math.atan2(Double.NEGATIVE_INFINITY , 3.0) != -Math.PI /2.0) {
      return 480;
    }

    if (! near(Math.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY ), Math.PI/4.0)) {
      return 490;
    }

    if (! near(Math.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Math.PI*3.0/4.0)) {
      return 500;
    }
    if (! near(Math.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), -Math.PI/4.0)) {
      return 510;
    }

    if (! near(Math.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), -Math.PI*3.0/4.0)) {
      return 520;
    }

    if (! (new Double(Math.exp( Double.NaN ))).isNaN()) {
      return 530;
    }
    if (! (new Double(Math.exp( Double.POSITIVE_INFINITY))).isInfinite()) {
      return 540;
    }
    if (! near(Math.exp(Double.NEGATIVE_INFINITY), 0.0)) {
      return 550;
    }
    if (! (new Double(Math.log( Double.NaN ))).isNaN()) {
      return 560;
    }
    if (! (new Double(Math.log( -1.0 ))).isNaN()) {
      return 570;
    }
    if (! (new Double(Math.log( Double.POSITIVE_INFINITY ))).isInfinite()) {
      return 580;
    }
    if (! (new Double(Math.sqrt( Double.NaN ))).isNaN()) {
      return 590;
    }
    if (! (new Double(Math.sqrt( -10.0 ))).isNaN()) {
      return 600;
    }
    if (! (new Double(Math.sqrt( Double.NaN ))).isNaN()) {
      return 610;
    }
    if (! (new Double(Math.sqrt( -10.0 ))).isNaN()) {
      return 620;
    }
    if (! (new Double(Math.sqrt( Double.POSITIVE_INFINITY))).isInfinite()) {
      return 630;
    }
    if (! near(Math.sqrt( -0.0), 0.0)) {
      return 640;
    }
    if (! near(Math.sqrt( 0.0), 0.0)) {
      return 650;
    }

    if (! near(Math.sqrt(4.0), 2.0)) {
      return 660;
    }

    if (! near(Math.pow(1.0 , 0.0 ), 1.0)) {
      return 670;
    }
    if (! near(Math.pow(2.0 , -0.0 ), 1.0 )) {
      return 680;
    } 
    if (! near(Math.pow(123.0 , 1.0 ), 123.0 )) {
      return 690;
    }
    if (! (new Double(Math.pow( 10.0, Double.NaN ))).isNaN()) {
      return 700;
    }
    if (! (new Double(Math.pow( Double.NaN, 1.0 ))).isNaN()) {
      return 710;
    }
    if (! (new Double(Math.pow( 2.0, Double.POSITIVE_INFINITY ))).isInfinite()) {
      return 720;
    }
    if (! (new Double(Math.pow( 0.5, Double.NEGATIVE_INFINITY ))).isInfinite()) {
      return 730;
    }
    if (! near(Math.pow( 1.5, Double.NEGATIVE_INFINITY ), 0.0)) {
      return 740;
    }
    if (! near(Math.pow( 0.5, Double.POSITIVE_INFINITY ), 0.0)) {
      return 750;
    }
    if (! (new Double(Math.pow( 1.0, Double.POSITIVE_INFINITY ))).isNaN()) {
      return 760;
    }
    if (! near(Math.pow( 0.0, 1.0), 0.0)) {
      return 770;
    }
    if (! near(Math.pow( Double.POSITIVE_INFINITY, -1.0 ), 0.0)) {
      return 780;
    }
    if (! (new Double(Math.pow( 0.0, -1.0 ))).isInfinite()) {
      return 790;
    }
    if (! (new Double(Math.pow( Double.POSITIVE_INFINITY, 1.0 ))).isInfinite()) {
      return 800;
    }
    if (! near(Math.pow( -0.0, 5.0), -0.0)) {
      return 810;
    }
    if (! near(Math.pow( Double.NEGATIVE_INFINITY , -7.0 ), -0.0)) {
      return 820;
    }
    if (Math.pow( -2.0, 6.0) != Math.pow(2.0,6.0)) {
      return 830;
    } 
    if (Math.pow( -2.0, 5.0) != -Math.pow(2.0,5.0)) {
      return 840;
    }

    if (! (new Double(Math.IEEEremainder( Double.NaN, 1.0 ))).isNaN()) {
      return 850;
    }
    if (! (new Double(Math.IEEEremainder( 1.0,Double.NaN))).isNaN()) {
      return 860;
    }
    if (! (new Double(Math.IEEEremainder( Double.POSITIVE_INFINITY , 2.0))).isNaN()) {
      return 870;
    }
    if (! (new Double(Math.IEEEremainder( 2.0,0.0))).isNaN()) {
      return 880;
    }
    if (! near(Math.IEEEremainder( 3.0, Double.POSITIVE_INFINITY), 3.0)) {
      return 890;
    }
	
    if (! near(Math.ceil(5.0), 5.0)) {
      return 900;
    }
    if (Math.ceil(0.0) != 0.0) {
      return 910;
    }
    if (Math.ceil(-0.0) != -0.0) {
      return 920;
    }
    if (! (new Double(Math.ceil(Double.POSITIVE_INFINITY))).isInfinite()) {
      return 930;
    }
    if (! (new Double(Math.ceil(Double.NaN))).isNaN()) {
      return 940;
    }
    if (Math.ceil(-0.5) != -0.0) {
      return 950;
    }
    if (Math.ceil( 2.5 ) != 3.0) {
      return 960;
    }

    if (Math.floor(5.0) != 5.0) {
      return 970;
    }
    if (Math.floor(2.5) != 2.0) {
      return 980;
    }
    if (! (new Double(Math.floor(Double.POSITIVE_INFINITY))).isInfinite()) {
      return 990;
    }
    if (! (new Double(Math.floor(Double.NaN))).isNaN()) {
      return 1000;
    }
    if (Math.floor(0.0) != 0.0) {
      return 1010;
    }
    if (Math.floor(-0.0) != -0.0) {
      return 1020;
    }

    if (Math.rint( 2.3 ) != 2.0) {
      return 1030;
    }
    if (Math.rint( 2.7 ) != 3.0) {
      return 1040;
    }
    if (Math.rint( 2.5) != 2.0) {
      return 1050;
    }
    if (Math.rint( 2.0) != 2.0) {
      return 1060;
    }
    if (Math.rint( 2.0) != 2.0) {
      return 1070;
    }
    if (! (new Double(Math.rint(Double.POSITIVE_INFINITY))).isInfinite()) {
      return 1080;
    }
    if (! (new Double(Math.rint(Double.NaN))).isNaN()) {
      return 1090;
    }
    if (Math.rint(0.0) != 0.0) {
      return 1100;
    }
    if (Math.rint(-0.0) != -0.0) {
      return 1110;
    }

    if (Math.round( 3.4 ) != 3 ) {
      return 1120;
    }
    if (Math.round( 9.55 ) != 10 ) {
      return 1130;
    }
    if (Math.round(Double.NaN) != 0 ) {
      return 1140;
    }

    if (Math.round( -3.4D ) != -3 ) {
      return 1150;
    }
    if (Math.round( -9.55D ) != -10 ) {
      return 1160;
    }

    if (Math.round( -3.4f ) != -3 ) {
      return 1170;
    }
    if (Math.round( -9.55f ) != -10 ) {
      return 1180;
    }

    float f1 = Integer.MIN_VALUE;
    f1 -= 5;
    if (Math.round(f1) != Integer.MIN_VALUE) {
      return 1190;
    }
    if (Math.round(Float.NEGATIVE_INFINITY) != Integer.MIN_VALUE) {
      return 1200;
    }

    f1 = Integer.MAX_VALUE;
    f1 += 5;
    if (Math.round(f1) != Integer.MAX_VALUE) {
      return 1210;
    }
    if (Math.round(Float.POSITIVE_INFINITY) != Integer.MAX_VALUE) {
      return 1220;
    }

    double d1 = Long.MIN_VALUE;
    d1 -= 5;
    if (Math.round(d1) != Long.MIN_VALUE) {
      return 1230;
    }
    if (Math.round(Double.NEGATIVE_INFINITY) != Long.MIN_VALUE) {
      return 1240;
    }

    d1 = Long.MAX_VALUE;
    d1 += 5;
    if (Math.round(d1) != Long.MAX_VALUE) {
      return 1250;
    }
    if (Math.round(Double.POSITIVE_INFINITY) != Long.MAX_VALUE) {
      return 1260;
    }

    if (Math.round( 3.4f ) != 3) {
      return 1270;
    } 
    if (Math.round( 9.55f ) != 10) { 
      return 1280;
    }
    if (Math.round(Float.NaN) != 0) {
      return 1290;
    }

    if (Math.random() < 0.0 || Math.random() > 1.0) {
      return 1300;
    }
    
    if (Math.abs(10) != 10 ) {
      return 1310;
    }
    if (Math.abs(-23) != 23 ) {
      return 1320;
    }
    if (Math.abs(Integer.MIN_VALUE) != Integer.MIN_VALUE) {
      return 1330;
    }
    if(Math.abs(-0) != 0 ) {
      return 1340;
    }
    if (Math.abs(1000L) != 1000 ) {
      return 1350;
    }
    if (Math.abs(-2334242L) != 2334242 ) {
      return 1360;
    }
    if (Math.abs( Long.MIN_VALUE ) != Long.MIN_VALUE ) {
      return 1370;
    }
    if (Math.abs( 0.0f ) != 0.0f || Math.abs(-0.0f) != 0.0f ) {
      return 1380;
    } 
    if (! (new Float(Math.abs( Float.POSITIVE_INFINITY ))).isInfinite() ) {
      return 1390;
    }
    if (! (new Float(Math.abs( Float.NaN ))).isNaN() ) {
      return 1400;
    }
    if (Math.abs( 23.34f ) != 23.34f ) {
      return 1410;
    }
    if (Math.abs( 0.0 ) != 0.0 || Math.abs(-0.0) != 0.0 ) {
      return 1420;
    }
    if (! (new Double(Math.abs( Double.POSITIVE_INFINITY ))).isInfinite() ) {
      return 1430;
    }
    if (! (new Double(Math.abs( Double.NaN ))).isNaN() ) {
      return 1440;
    }
    if (Math.abs( 23.34 ) != 23.34 ) {
      return 1450;
    }

    if (Math.min( 100 , 12 ) != 12 ) {
      return 1460;
    }
    if (Math.min( Integer.MIN_VALUE , Integer.MIN_VALUE + 1 ) != Integer.MIN_VALUE ) {
      return 1470;
    }
    if (Math.min( Integer.MAX_VALUE , Integer.MAX_VALUE -1 ) != Integer.MAX_VALUE -1 ) {
      return 1480;
    }
    if (Math.min( 10 , 10 ) != 10 ) {
      return 1490;
    }
    if (Math.min( 0 , -0 ) != -0 ) {
      return 1500;
    }
    if (Math.min( 100L , 12L ) != 12L ) {
      return 1510;
    }
    if (Math.min( Long.MIN_VALUE , Long.MIN_VALUE + 1 ) != Long.MIN_VALUE ) {
      return 1520;
    }
    if (Math.min( Long.MAX_VALUE , Long.MAX_VALUE -1 ) != Long.MAX_VALUE -1 ) {
      return 1530;
    }
    if (Math.min( 10L , 10L ) != 10L ) {
      return 1540;
    }
    if (Math.min( 0L , -0L ) != -0L ) {
      return 1550;
    }
    if (Math.min( 23.4f , 12.3f ) != 12.3f ) {
      return 1560;
    }
    if (! (new Float(Math.min( Float.NaN ,  1.0f ))).isNaN()  ) {
      return 1570;
    }
    if (Math.min( 10.0f , 10.0f ) != 10.0f ) {
      return 1580;
    }
    if (Math.min( 0.0f , -0.0f ) != -0.0f ) {
      return 1590;
    }
    if (Math.min( 23.4 , 12.3 ) != 12.3 ) {
      return 1600;
    }
    if (! (new Double(Math.min( Double.NaN ,  1.0 ))).isNaN()  ) {
      return 1610;
    }
    if (Math.min( 10.0 , 10.0 ) != 10.0 ) {
      return 1620;
    }
    if (Math.min( 0.0 , -0.0 ) != -0.0 ) {
      return 1630;
    }

    if (Math.max( 100 , 12 ) != 100 ) {
      return 1640;
    }
    if (Math.max( Integer.MAX_VALUE , Integer.MAX_VALUE - 1 ) != Integer.MAX_VALUE ) {
      return 1650;
    }
    if (Math.max( Integer.MIN_VALUE , Integer.MIN_VALUE + 1 ) != Integer.MIN_VALUE +1 ) {
      return 1660;
    }
    if (Math.max( 10 , 10 ) != 10 ) {
      return 1670;
    }
    if (Math.max( 0 , -0 ) != 0 ) {
      return 1680;
    }
    if (Math.max( 100L , 12L ) != 100L ) {
      return 1690;
    }
    if (Math.max( Long.MAX_VALUE , Long.MAX_VALUE - 1 ) != Long.MAX_VALUE ) {
      return 1700;
    }
    if (Math.max( Long.MIN_VALUE , Long.MIN_VALUE +1 ) != Long.MIN_VALUE + 1 ) {
      return 1710;
    }
    if (Math.max( 10L , 10L ) != 10L ) {
      return 1720;
    }
    if (Math.max( 0L , -0L ) != 0L ) {
      return 1730;
    }
    if (Math.max( 23.4f , 12.3f ) != 23.4f ) {
      return 1740;
    }
    if (! (new Float(Math.max( Float.NaN ,  1.0f ))).isNaN()  ) {
      return 1750;
    }
    if (Math.max( 10.0f , 10.0f ) != 10.0f ) {
      return 1760;
    }
    if (Math.max( 0.0f , -0.0f ) != 0.0f ) {
      return 1770;
    }
    if (Math.max( 23.4 , 12.3 ) != 23.4 ) {
      return 1780;
    }
    if (! (new Double(Math.max( Double.NaN ,  1.0 ))).isNaN()  ) {
      return 1790;
    }
    if (Math.max( 10.0 , 10.0 ) != 10.0 ) {
      return 1800;
    }
    if (Math.max( 0.0 , -0.0 ) != 0.0 ) {
      return 1810;
    }

    /*
    ** All is cool...
    */

    return 0;
    
  }

  public static void main(String args[]) {
  
    int r = MathTest.test();
    
    System.out.println(">>>" + r + "<<<");

  }
   
}
