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

class StringTest {

  public int test() {

    char[] cstr = { 'a', 'b', 'c', '\t', 'A', 'B', 'C', ' ', '1', '2', '3' };
    String s = new String(" abc\tABC 123\t");
    String d = new String(cstr);
    
    try {
      s.charAt(s.length());
      return 100;
    }
    catch (IndexOutOfBoundsException ex) {
    }
    
    try {
      s.charAt(-1);
      return 110;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    if (s.charAt(7) != 'C') {
      return 120;
    }

    try {
      s.endsWith(null);
      return 130;
    }
    catch (NullPointerException e) {
    }
    
    if (s.endsWith("123")) {
      return 140;
    }

    if (! d.endsWith("123")) {
      return 150;
    }

    try {
      s.startsWith(null);
      return 160;
    }
    catch (NullPointerException e) {
    }
    
    if (s.startsWith("abc")) {
      return 170;
    }

    if (! d.startsWith("abc")) {
      return 180;
    }

    if (! s.startsWith("abc", 1)) {
      return 190;
    }

    if (s.startsWith("abc", 2)) {
      return 200;
    }

    if (s.startsWith("abc", -1)) {
      return 210;
    }

    if (s.startsWith("abc", s.length())) {
      return 220;
    }

    /*
    ** substring testing...
    */

    try {
      s.substring(-1);
      return 230;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    try {
      s.substring(s.length());
    }
    catch (IndexOutOfBoundsException ex) {
      return 240;
    }

    try {
      s.substring(s.length() + 1);
      return 250;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    try {
      s.substring(4, -1);
      return 260;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    try {
      s.substring(4, s.length() + 1);
      return 270;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    if (! s.substring(4).equals("\tABC 123\t")) {
      return 280;
    }

    if (! s.substring(4, s.length() - 5).equals("\tABC")) {
      return 290;
    }

    /*
    ** concat tests...
    */
    
    if (! "Steven ".concat("Buytaert").equals("Steven Buytaert")) {
      return 300;
    }

    try {
      "A".concat(null);
      return 310;
    }
    catch (NullPointerException e) {
    }

    String concat_test = new String("concat test");
    String concat_result = concat_test.concat("");

    if (concat_test != concat_result) {
      return 320;
    }

    concat_test = new String("");
    concat_result = concat_test.concat("test");
    
    if (! concat_result.equals("test")) {
      return 330;
    }

    String str = new String();
    try {
      str = str.concat("smartmove");
      if (! str.equals("smartmove")) {
        return 340;
      }
    }
    catch (Exception e) {
      return 350;
    }
    
    /*
    ** replace tests...
    */

    if (! "mesquite in your cellar".replace('e', 'o').equals("mosquito in your collar")) {
      return 360;
    }

    if (! "War of the worlds".replace('x', 'z').equals("War of the worlds")) {
      return 370;
    }

    /*
    ** compareTo tests...
    */
    
    String compareStr = "this is a test";
    int r;
        
    r = compareStr.compareTo("this is a test and more");
    if (r >= 0) {
      return 380;
    }

    r = compareStr.compareTo("this is not a test");
    if (r >= 0) {
      return 390;
    }

    r = compareStr.compareTo("this is a test");
    if (r != 0) {
      return 400;
    }

    r = compareStr.compareTo("no, this is not a test");
    if (r <= 0) {
      return 410;
    }

    r = compareStr.compareTo("this");
    if (r <= 0) {
      return 420;
    }

    try {
      r = compareStr.compareTo(null);
      return 430;
    }
    catch (NullPointerException e) {
    }

    String a = new String();
    String e = new String(cstr, 3, 3);

    r = d.compareTo(s.trim());
    if (r != 0) {
      return 440;
    }
    
    r = d.compareTo(a);
    if (r != 11) {
      return 450;
    }

    r = d.compareTo(s);
    if (r != 65) {
      return 460;
    }

    r = d.compareTo(e);
    if (r != 88) {
      return 470;
    }

    r = d.toLowerCase().compareTo(d);
    if (r != 32) {
      return 480;
    }

    r = d.compareTo(d.substring(0, d.length() - 2));
    if (r != 2) {
      return 490;
    }
    
    r = a.compareTo(d);
    if (r != -11) {
      return 500;
    }

    r = s.compareTo(d);
    if (r != -65) {
      return 510;
    }

    r = a.compareTo(d);
    if (r != -11) {
      return 520;
    }

    r = s.compareTo(d);
    if (r != -65) {
      return 530;
    }

    r = e.compareTo(d);
    if (r != -88) {
      return 540;
    }

    r = d.compareTo(d.toLowerCase());
    if (r != -32) {
      return 550;
    }

    r = d.substring(0, d.length() - 2).compareTo(d);
    if (r != -2) {
      return 560;
    }

    try {
      "abc".compareTo(null);
      return 570;
    }
    catch (NullPointerException ex) {
    }

    r = "abc".compareTo("bcdef");
    if (r >= 0) {
      return 580;
    }

    r = "abc".compareTo("abc");
    if (r != 0) {
      return 590;
    }

    r = "abc".compareTo("aabc");
    if (r <= 0) {
      return 600;
    }
    
    r = "".compareTo("abc");
    if (r >= 0) {
      return 610;
    }
 
    /*
    ** indexOf tests...
    */
    
    r = s.indexOf(' ');
    if (r != 0) {
      return 620;
    }
    
    r = s.indexOf(' ', 1);
    if ( r != 8) {
      return 630;
    }

    r = s.indexOf(' ', 10);
    if (r != -1) {
      return 640;
    }
    
    r = s.indexOf(' ', -1);
    if (r != 0) {
      return 650;
    }

    r = s.indexOf(' ', s.length());
    if (r != -1) {
      return 660;
    }

    r = s.indexOf("abc");
    if (r != 1) {
      return 670;
    }

    r = s.indexOf("abc", 1);
    if (r != 1) {
      return 680;
    }

    r = s.indexOf("abc", 10);
    if (r != -1) {
      return 690;
    }

    try {
      "abc".indexOf(null);
      return 700;
    }
    catch (NullPointerException ex) {
    }

    r = s.lastIndexOf(' ');
    if (r != 8) {
      return 710;
    }

    r = s.lastIndexOf(' ', 1);
    if (r != 0) {
      return 720;
    }

    r = s.lastIndexOf(' ', 10);
    if (r != 8) {
      return 730;
    }
    
    r = s.lastIndexOf(' ', -1);
    if (r != -1) {
      return 740;
    }

    r = s.lastIndexOf(' ', s.length());
    if (r != 8) {
      return 750;
    }
    
    r = s.lastIndexOf("abc");
    if (r != 1) {
      return 760;
    }

    r = s.lastIndexOf("abc", 1);
    if (r != 1) {
      return 770;
    }

    r = s.lastIndexOf("abc", 10);
    if (r != 1) {
      return 780;
    }

    String is = "Madam, I'm Adam";
    
    if (! is.substring(is.lastIndexOf(' ') + 1).equals("Adam")) {
      return 790;
    }
    
    if (! is.substring(0, is.indexOf(' ')).equals("Madam,")) {
      return 800;
    }

    try {
      "abc".lastIndexOf(null);
      return 810;
    }
    catch (NullPointerException ex) {
    }
    
    /*
    ** hashCode tests
    */

    a = new String();
    String c = new String(new StringBuffer("abc\tABC 123"));  

    if ("85".hashCode() != 1789) {
      System.out.println(">>>" + "85".hashCode() + "<<<");
      return 820;
    }

    if ("869".hashCode() != 55547) {
      System.out.println(">>>" + "869".hashCode() + "<<<");
      return 830;
    }
    
    if (a.hashCode() != 0) {
      return 840;
    }
    
    if (s.hashCode() != -524164548) {
      System.out.println(">>>" + s.hashCode() + "<<<");
      return 850;
    }
 
    if (c.hashCode() != -822419571) {
      return 860;
    }

    c = new String("This is a fairly long string that we want the hashcode to be calculated of since it is longer than 32 characters...");
    if (c.hashCode() != -1758297321) {
      return 870;
    }

    c = new String("this string should be so long that it overflows the internally calculated table of factors of 31 that is used in the calculation of the string hash code in Wonka. If you are still reading this, you need psychiatric help!");
    if (c.hashCode() != 998730225) {
      return 880;
    }
 
    /*
    ** trim tests...
    */
    
    String source = "   laura";
    String dest;
    
    dest = source.trim();
    if (! dest.equals("laura")) {
      return 890;
    }

    source = "                 ";
    dest = source.trim();
    if (! dest.equals("")) {
      return 900;
    }
    
    source = "laura";
    dest = source.trim();
    if (dest != source) {
      return 910;
    }
    
    source = "l       ";
    dest = source.trim();
    if (! dest.equals("l")) {
      return 920;
    }

    source = "          l";
    dest = source.trim();
    if (! dest.equals("l")) {
      return 930;
    }

    source = "    l      ";
    dest = source.trim();
    if (! dest.equals("l")) {
      return 940;
    }

    source = "   l a u r a     ";
    dest = source.trim();
    if (! dest.equals("l a u r a")) {
      return 950;
    }

    /*
    ** toUpperCase and toLowerCase tests...
    */
    
    if (! "".toUpperCase().equals("")) {
      return 960;
    }

    if (! "French Fries".toUpperCase().equals("FRENCH FRIES")) {
      return 970;
    }

    source = "FRENCH FRIES";
    dest = source.toUpperCase();
    if (!dest.equals(source)) {
      return 980;
    }

    if (! "".toLowerCase().equals("")) {
      return 990;
    }

    if (! "French Fries".toLowerCase().equals("french fries")) {
      return 1000;
    }

    source = "french fries";
    dest = source.toLowerCase();
    if (!(dest.equals(source))) {
      return 1010;
    }

    if (! "these are allready lower case characters and French Fries".toLowerCase().equals("these are allready lower case characters and french fries")) {
      return 1020;
    }

    if (! "THESE ARE ALLREADY UPPER CASE CHARACTERS AND French Fries".toUpperCase().equals("THESE ARE ALLREADY UPPER CASE CHARACTERS AND FRENCH FRIES")) {
      return 1030;
    }

    /*
    ** toCharArray tests...
    */

    char[] charr = "abcde".toCharArray();
    if (charr[0] != 'a') {
      return 1040;
    }
    if (charr[1] != 'b') {
      return 1050;
    }
    if (charr[2] != 'c') {
      return 1060;
    }
    if (charr[3] != 'd') {
      return 1070;
    }
    if (charr[4] != 'e') {
      return 1080;
    }
    if (charr.length != 5) {
      return 1090;
    }

    charr = "".toCharArray();
    if (charr.length > 0) {
      return 1100;
    }

    /*
    ** intern tests...
    */
    
    String hp = "hp";
    String nullstr = "";
    
    if (hp.intern() != hp.intern()) {
      return 1110;
    }
    
    if ("hpq".intern() == hp.intern()) {
      return 1120;
    }
    
    if (nullstr.intern() != "".intern()) {
      return 1130;
    }
    
    hp = "";
    if ("".intern() != hp.intern()) {
      return 1140;
    }
    
    StringBuffer buff = new StringBuffer();
    buff.append('a');
    buff.append('b');
    if ("ab".intern() != buff.toString().intern()) {
      return 1150;
    }
    
    buff = new StringBuffer();
    if ("".intern() != buff.toString().intern()) {
      return 1160;
    }

    /*
    ** regionMatches tests...
    */

    try {
      boolean res = "abc".regionMatches(true, 0, null, 0, 2);
      return 1170;
    }
    catch (NullPointerException ex) {
    }
    
    if ("abcd".regionMatches(true, -1, "abcd", 0, 2)) {
      return 1180;
    }

    if ("abcd".regionMatches(true, 0, "abcd", -1, 2)) {
      return 1190;
    }

    if ("abcd".regionMatches(true, 0, "abcd", 0, 10)) {
      return 1200;
    }

    if ("abcd".regionMatches(true, 1, "ab", 0, 3)) {
      return 1210;
    }

    if (! "abcd".regionMatches(true, 1, "abc", 1, 2)) {
      return 1220;
    }

    if (! "abcd".regionMatches(true, 1, "abc", 1, 0)) {
      return 1230;
    }

    if (! "abcd".regionMatches(true, 1, "ABC", 1, 2)) {
      return 1240;
    }

    if ("abcd".regionMatches(false, 0, "ABC", 1, 2)) {
      return 1250;
    }

    try {
      boolean res = "abc".regionMatches(0 , null , 0 , 2);
      return 1260;
    }    
    catch (NullPointerException ex) {
    }
    
    if ("abcd".regionMatches(-1 , "abcd" , 0 , 2 )) {
      return 1270;
    }

    if ("abcd".regionMatches(0, "abcd" , -1, 2 )) {
      return 1280;
    }

    if ("abcd".regionMatches(0, "abcd" , 0, 10)) {
      return 1290;
    }

    if ("abcd".regionMatches(0, "ab" , 0, 3)) {
      return 1300;
    }

    if (! "abcd".regionMatches(1, "abc" , 1, 2)) {
      return 1310;
    }

    if (! "abcd".regionMatches(1, "abc" , 1, 0)) {
      return 1320;
    }

    if ("abcd".regionMatches(1, "ABC" , 1, 2)) {
      return 1330;
    }
 
    /*
    ** valueOf tests...
    */
    
    if ( ! String.valueOf('C').equals("C")) {
      return 1340;
    }
 
    /*
    ** getChars tests...
    */
    
    str = "abcdefghijklmn";
    try {
      str.getChars(0, 3, null, 1);
      return 1350;
    }
    catch (NullPointerException ex) {
    }
    
    char[] dst = new char[5];
    try {
      str.getChars(-1, 3, dst, 1);
      return 1360;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    try {
      str.getChars(4, 3, dst, 1);
      return 1370;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    try {
      str.getChars(1, 15, dst, 1);
      return 1380;
    }
    catch (IndexOutOfBoundsException ex) {
    }
    
    try {
      str.getChars(1, 5, dst, -1);
      return 1390;
    }
    catch (IndexOutOfBoundsException ex) {
    }

    try {
      str.getChars(1, 10, dst, 1);
      return 1400;
    }
    catch (IndexOutOfBoundsException ex) {
    }
    
    str.getChars(0, 5, dst, 0);
    if (dst[0] != 'a') {
      return 1410;
    }    
    if (dst[1] != 'b') {
      return 1420;
    }    
    if (dst[2] != 'c') {
      return 1430;
    }    
    if (dst[3] != 'd') {
      return 1440;
    }    
    if (dst[4] != 'e') {
      return 1450;
    }    

    dst[0] = dst[1] = dst[2] = dst[3] = dst[4] = ' ';
    str.getChars(0, 0, dst, 0);
    if (dst[0] != ' ') {
      return 1460;
    }    
    if (dst[1] != ' ') {
      return 1470;
    }    
    if (dst[2] != ' ') {
      return 1480;
    }    
    if (dst[3] != ' ') {
      return 1490;
    }    
    if (dst[4] != ' ') {
      return 1500;
    }    

    str.getChars(0, 1, dst, 0);
    if (dst[0] != 'a') {
      return 1510;
    }    
    if (dst[1] != ' ') {
      return 1520;
    }    
    if (dst[2] != ' ') {
      return 1530;
    }    
    if (dst[3] != ' ') {
      return 1540;
    }    
    if (dst[4] != ' ') {
      return 1550;
    }    

    /*
    ** getBytes tests...
    */
    
    str = "abcdefghijklmn";
    byte[] dst1;
    dst1 = str.getBytes();
    if (dst1[0] != 'a') {
      return 1560;
    }
    if (dst1[1] != 'b') {
      return 1570;
    }
    if (dst1[2] != 'c') {
      return 1580;
    }
    if (dst1[3] != 'd') {
      return 1590;
    }
    if (dst1[4] != 'e') {
      return 1600;
    }
    if (dst1[5] != 'f') {
      return 1610;
    }
    if (dst1[6] != 'g') {
      return 1620;
    }
    if (dst1[7] != 'h') {
      return 1630;
    }
    if (dst1[8] != 'i') {
      return 1640;
    }
    if (dst1[9] != 'j') {
      return 1650;
    }
    if (dst1[10] != 'k') {
      return 1660;
    }
    if (dst1[11] != 'l') {
      return 1670;
    }
    if (dst1[12] != 'm') {
      return 1680;
    }
    if (dst1[13] != 'n') {
      return 1690;
    }

    /*
    ** equalsIgnoreCase tests...
    */
    
    if ("hi".equalsIgnoreCase(null)) {
      return 1700;
    }

    if (! "hi".equalsIgnoreCase("HI")) {
      return 1710;
    }

    if ("hi".equalsIgnoreCase("pq")) {
      return 1720;
    }

    if ("hi".equalsIgnoreCase("HI ")) {
      return 1730;
    }

    return 0;

  }

}
