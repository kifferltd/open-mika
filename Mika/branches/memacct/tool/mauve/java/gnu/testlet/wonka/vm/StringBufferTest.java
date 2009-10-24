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
import gnu.testlet.*;

class StringBufferTest {

  public int test(TestHarness th) {

    try {
      new StringBuffer(-1);
      return 100;
    }
    catch (NegativeArraySizeException e) {
    }

    StringBuffer str1 = new StringBuffer();
    if (str1.length() != 0) {
      return 110;
    }
    if (str1.capacity() != 16) {
      return 120;
    }
    if (! str1.toString().equals("")) {
      return 130;
    }
    
    StringBuffer str2 = new StringBuffer("testing");
    if (str2.length() != 7) {
      return 140;
    }
    if (! str2.toString().equals("testing")) {
      return 150;
    }

    StringBuffer str4 = new StringBuffer("hi there");
    if (str4.length() != 8) {
      return 160;
    }
    if (! str4.toString().equals("hi there")) {
      return 170;
    }
    if (str4.capacity() != 24) {
      return 180;
    }

    StringBuffer strbuf = new StringBuffer(0);
    if (! strbuf.append("hiii").toString().equals("hiii")) {
      return 190;
    }
    
    strbuf = new StringBuffer(10);
    if (strbuf.capacity() != 10) {
      return 200;
    }
    th.debug("Debug info -- passing 200");
    str1 = new StringBuffer("03041965");
    if (! str1.toString().equals("03041965")) {
      return 210;
    }
    
    str1 = new StringBuffer();
    if (! str1.toString().equals("")) {
      return 220;
    }
    
    /*
    ** capacity tests...
    */
    
    str1 = new StringBuffer("");
    str2 = new StringBuffer("pentiumpentiumpentium");
    if (str1.capacity() != 16) {
      return 230;
    }
    if (str2.capacity() != 37) {
      return 240;
    }

    str1.ensureCapacity(17);
    if (str1.capacity() != 34) {
      return 250;
    }
    
    /*
    ** setLength tests...
    */
    
    str1 = new StringBuffer("ba");
    try {
      str1.setLength(-1);
      return 260;
    }
    catch (IndexOutOfBoundsException e) {
    }
    
    str1. setLength(4);
    if (str1.length() != 4) {
      return 270;
    }
    
    if (str1.charAt(0) != 'b') {
      return 280;
    }
    if (str1.charAt(1) != 'a') {
      return 290;
    }
    if (str1.charAt(2) != '\u0000') {
      return 300;
    }
    th.debug("Debug info -- passing 300");
    if (str1.charAt(3) != '\u0000') {
      return 310;
    }

    /*
    ** charAt tests...
    */
    
    str1 = new StringBuffer("abcd");    
    if (str1.charAt(0) != 'a') {
      return 320;
    }
    if (str1.charAt(1) != 'b') {
      return 330;
    }
    if (str1.charAt(2) != 'c') {
      return 340;
    }
    if (str1.charAt(3) != 'd') {
      return 350;
    }
    
    try {
      str1.charAt(4);
      return 360;
    }
    catch (IndexOutOfBoundsException e) {
    }

    try {
      str1.charAt(-1);
      return 370;
    }
    catch (IndexOutOfBoundsException e) {
    }
    
    /*
    ** getChars tests...
    */

    str1 = new StringBuffer("abcdefghijklmn");
    try {
      str1.getChars(0, 3, null, 1);
      return 380;
    }
    catch (NullPointerException e) {
      /*
      ** dst is null
      */
    }

    char[] dst = new char[5];
    try {
      str1.getChars(-1, 3, dst, 1);
      return 390;
    }
    catch (IndexOutOfBoundsException e) {
      /*
      ** Index out of bounds of StringBuffer - srcOffset
      */
    }
    
    try {
      str1.getChars(4, 3, dst, 3);
      return 400;
    }
    catch (IndexOutOfBoundsException e) {
    th.debug("Debug info -- passing 400");
      /*
      **
      */      
    }

    try {
      str1.getChars(1, 15, dst, 1);
      return 410;
    }
    catch (IndexOutOfBoundsException e) {
    }

    try {
      str1.getChars(1, 5, dst, -1);
      return 420;
    }
    catch (IndexOutOfBoundsException e) {
    }

    try {
      str1.getChars(1, 10, dst, 1);
      return 430;
    }
    catch (IndexOutOfBoundsException e) {
    }

    str1.getChars(0, 5, dst, 0);
    if (dst[0] != 'a') {
      return 440;
    }
    if (dst[1] != 'b') {
      return 450;
    }
    if (dst[2] != 'c') {
      return 460;
    }
    if (dst[3] != 'd') {
      return 470;
    }
    if (dst[4] != 'e') {
      return 480;
    }

    dst[0] = dst[1] = dst[2] = dst[3] = dst[4] = ' ';
    str1.getChars(0, 1, dst, 0);
    if (dst[0] != 'a') {
      return 490;
    }
    if (dst[1] != ' ') {
      return 500;
    }
    th.debug("Debug info -- passing 500");
    if (dst[2] != ' ') {
      return 510;
    }
    if (dst[3] != ' ') {
      return 520;
    }
    if (dst[4] != ' ') {
      return 530;
    }

    /*
    ** append tests...
    */
    
    str1 = new StringBuffer();
    Object NULL = null;
    
    if (! str1.append(NULL).toString().equals("null")) {
      return 540;
    }
    if (! str1.append(new Integer(100)).toString().equals("null100")) {
      return 550;
    }

    str1 = new StringBuffer("hi");
    str1.append(" there");
    str1.append(" buddy");
    if (! str1.toString().equals("hi there buddy")) {
      return 560;
    }

    str1 = new StringBuffer();
    str1 = str1.append("sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd");
    if (! str1.toString().equals("sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd")) {
      return 570;
    }

    str1 = new StringBuffer();
    char[] carr = null;
    try {
      str1 = str1.append(carr);
      return 580;
    }
    catch (NullPointerException e) {
    }
    
    char[] carr1 = {'h', 'i', 't', 'h', 'e', 'r'};
    str1 = new StringBuffer("!");
    str1 = str1.append(carr1);
    if (! str1.toString().equals("!hither")) {
      return 590;
    }

    str1 = new StringBuffer();
    try {
      str1 = str1.append(carr1, -1, 3);
      return 600;
    }
    catch (IndexOutOfBoundsException e) {
    }
    th.debug("Debug info -- passing 600");

    str1 = new StringBuffer("!");
    str1 = str1.append(carr1, 2, 3);
    if (! str1.toString().equals("!the")) {
      return 610;
    }

    str1 = new StringBuffer();
    str1 = str1.append(true);
    if (! str1.toString().equals("true")) {
      return 620;
    }
    str1 = str1.append(false);
    if (! str1.toString().equals("truefalse")) {
      return 630;
    }
    str1 = str1.append(20);
    if (! str1.toString().equals("truefalse20")) {
      return 640;
    }

    str1 = new StringBuffer();
    str1 = str1.append(2034L);
    if (! str1.toString().equals("2034")) {
      return 650;
    }

// Wait until we fix the floating point formatting stuff...
    str1 = new StringBuffer();
    str1 = str1.append(12.5f);
    if (! str1.toString().equals("12.5")) {
      System.out.println(">>>" + str1.toString() + "<<<");
      return 660;
    }

    str1 = new StringBuffer();
    str1 = str1.append(12.35);
    if (! str1.toString().equals("12.35")) {
      System.out.println(">>>" + str1.toString() + "<<<");
      return 670;
    }


    /*
    ** insert tests...
    */

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(5, NULL);
    if (! str1.toString().equals("12345null67")) {
      System.out.println(">>>" + str1 + "<<<");
      return 680;
    }

    try {
      str1 = str1.insert(-1, new Object());
      return 690;
    }
    catch (IndexOutOfBoundsException e) {
    }

    str1 = new StringBuffer("1234567");
    try {
      str1 = str1.insert(8, new Object());
      return 700;
    }
    catch (IndexOutOfBoundsException e) {
    }
    th.debug("Debug info -- passing 700");

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(4, "inserted");
    if (! str1.toString().equals("1234inserted567")) {
      return 710;
    }

    str1 = new StringBuffer("1234567");
    char cdata[] = null;
    try {
      str1 = str1.insert(4, cdata);
      return 720;
    }
    catch (NullPointerException e) {
    }

    cdata = new char[1];
    try {
      str1 = str1.insert(-1, cdata);
      return 730;
    }
    catch (IndexOutOfBoundsException e) {
    }

    try {
      str1 = str1.insert(8, cdata);
      return 740;
    }
    catch (IndexOutOfBoundsException e) {
    }

    str1 = new StringBuffer("1234567");
    char[] cdata1 = {'h', 'e', 'l', 'l', 'o'};
    str1 = str1.insert(4, cdata1);
    if (! str1.toString().equals("1234hello567")) {
      return 750;
    }

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(0, true);
    if (! str1.toString().equals("true1234567")) {
      return 760;
    }

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(7, false);
    if (! str1.toString().equals("1234567false")) {
      return 770;
    }

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(0, 'c');
    if (! str1.toString().equals("c1234567")) {
      return 780;
    }

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(7, 'b');
    if (! str1.toString().equals("1234567b")) {
      return 790;
    }

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(7, 999);
    if (! str1.toString().equals("1234567999")) {
      return 800;
    }
    th.debug("Debug info -- passing 800");

    str1 = new StringBuffer("1234567");
    str1 = str1.insert(3, (long)1230);
    if (! str1.toString().equals("12312304567")) {
      return 810;
    }

    /*
    ** setCharAt tests...
    */
    
    str1 = new StringBuffer("1234567");
    try {
      str1.setCharAt(-1, 'A');
      return 820;
    }
    catch (IndexOutOfBoundsException e) {
    }

    try {
      str1.setCharAt(7, 'A');
      return 830;
    }
    catch (IndexOutOfBoundsException e) {
    }

    str1.setCharAt(3, 'A');
    if (! str1.toString().equals("123A567")) {
      return 840;
    }

    /*
    ** substring tests...
    */
    
    str1 = new StringBuffer("1234567");
    String str;
    try {
      str = str1.substring(-1, 4);
      return 850;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str = str1.substring(0, 8);
      return 860;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str = str1.substring(8, 6);
      return 870;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str = str1.substring(0, -1);
      return 880;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str = str1.substring(2, 1);
      return 890;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    str = str1.substring(2, 4);
    if (! str.equals("34")) {
      return 900;
    }
    th.debug("Debug info -- passing 900");

    str = str1.substring(2);
    if (! str.equals("34567")) {
      return 910;
    }

    /*
    ** deleteCharAt tests...
    */
    
    str1 = new StringBuffer("123456789");
    try {
      str1.deleteCharAt(-1);
      return 920;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str1.deleteCharAt(9);
      return 930;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    str1.deleteCharAt(4);
    if (! str1.toString().equals("12346789")) {
      return 940;
    }
    
    if (str1.length() != 8) {
      return 950;
    }

    /*
    ** replace tests...
    */
    
    str1 = new StringBuffer("1234567890");
    try {
      str1.replace(-1, 4, "A");
      return 960;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str1.replace(str1.length() + 1, 4, "A");
      return 970;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    try {
      str1.replace(4, 3, "A");
      return 980;
    }
    catch (StringIndexOutOfBoundsException e) {
    }

    str1.replace(4, 6, "FS");
    if (! str1.toString().equals("1234FS7890")) {
      return 990;
    }

    str1 = new StringBuffer("1234567890");
    str1.replace(4, 6, "fivesix");
    if (! str1.toString().equals("1234fivesix7890")) {
      return 1000;
    }
    th.debug("Debug info -- passing 1000");

    str1 = new StringBuffer("1234567890");
    str1.replace(4, 100, "this is a fairly long replacement for the numbers 'five' 'six' 'seven' 'eight' 'nine' 'zero'");
    if (! str1.toString().equals("1234this is a fairly long replacement for the numbers 'five' 'six' 'seven' 'eight' 'nine' 'zero'")) {
      return 1010;
    }
    th.debug("Debug info -- passing 1010");

    str1 = new StringBuffer("1234567890");
    str1.replace(4, 7, "fivesix");
    if (! str1.toString().equals("1234fivesix890")) {
      return 1020;
    }
    th.debug("Debug info -- passing 1020");

    str1 = new StringBuffer("one two three four five six seven eight nine zero");
    str1.replace(0, 7, "0 1");
    if (! str1.toString().equals("0 1 three four five six seven eight nine zero")) {
      return 1030;
    }

    /*
    ** reverse test...
    */
    th.debug("Debug info -- passing 1030");

    str1 = new StringBuffer("1234567890");
    str1 = str1.reverse();
    if (! str1.toString().equals("0987654321")) {
      return 1040;
    }

    str1 = new StringBuffer("123456789");
    str1 = str1.reverse();
    if (! str1.toString().equals("987654321")) {
      return 1050;
    }

    str1 = new StringBuffer("");
    str1 = str1.reverse();
    if (! str1.toString().equals("")) {
      return 1060;
    }

    str1 = new StringBuffer("A");
    str1 = str1.reverse();
    if (! str1.toString().equals("A")) {
      return 1070;
    }

    return 0;

  }

}
