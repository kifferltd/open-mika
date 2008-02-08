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

import java.lang.reflect.Field;

public class FieldTest {

  public class Sample_1 {

                                  // indexes into fields_1  
    public int i_int;             //      0
    public float i_float;         //      1
    public short i_short;         //      2
    public byte i_byte;           //      3
    public double i_double;       //      4
    public String i_string;       //      5
    public char i_char;           //      6
    public long i_long;           //      7
    public boolean i_boolean;     //      8

    public Sample_1() {
      i_int = 1;
      i_float = 2.0f;
      i_short = 3;
      i_byte = 0x04;
      i_double = 5.0;
      i_string = new String("six");
      i_char = '7';
      i_long = 8;
      i_boolean = true;
    }
  
  }
  
  Sample_1 sample_1;
  Sample_2 sample_2;

  public int test() {

    Field[] fields_1 = sample_1.getClass().getFields();
    Class[] classes_1;
    Object[] results_1;

    /*
    ** Test publically accessible fields of Sample_1
    */
    
    try {
      results_1 = new Object[fields_1.length];
      classes_1 = new Class[fields_1.length];
      for (int i = 0; i < fields_1.length; i++) {
        results_1[i] = fields_1[i].get(sample_1);
        classes_1[i] = results_1[i].getClass();
      }
    }
    catch (IllegalAccessException ex) {
      return 100;
    }

    /*
    ** check the name of the type with Class.getName()
    */

    if (! fields_1[0].getType().getName().equals("int")) {
      return 110;
    }

    if (! fields_1[1].getType().getName().equals("float")) {
      return 120;
    }

    if (! fields_1[2].getType().getName().equals("short")) {
      return 130;
    }

    if (! fields_1[3].getType().getName().equals("byte")) {
      return 140;
    }

    if (! fields_1[4].getType().getName().equals("double")) {
      return 150;
    }

    if (! fields_1[5].getType().getName().equals("java.lang.String")) {
      return 160;
    }

    if (! fields_1[6].getType().getName().equals("char")) {
      return 170;
    }

    if (! fields_1[7].getType().getName().equals("long")) {
      return 180;
    }

    if (! fields_1[8].getType().getName().equals("boolean")) {
      return 190;
    }

    /*
    ** Check the getType and getDeclaringClass methods...
    */
    
    if (! fields_1[0].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 200;
    }
    if (! fields_1[0].getType().toString().equals("int")) {
      return 210;
    }

    if (! fields_1[1].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 220;
    }
    if (! fields_1[1].getType().toString().equals("float")) {
      return 230;
    }

    if (! fields_1[2].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 240;
    }
    if (! fields_1[2].getType().toString().equals("short")) {
      return 250;
    }

    if (! fields_1[3].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 260;
    }
    if (! fields_1[3].getType().toString().equals("byte")) {
      return 270;
    }

    if (! fields_1[4].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 280;
    }
    if (! fields_1[4].getType().toString().equals("double")) {
      return 290;
    }

    if (! fields_1[5].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 300;
    }
    if (! fields_1[5].getType().toString().equals("class java.lang.String")) {
      return 310;
    }

    if (! fields_1[6].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 320;
    }
    if (! fields_1[6].getType().toString().equals("char")) {
      return 330;
    }
    
    if (! fields_1[7].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 340;
    }
    if (! fields_1[7].getType().toString().equals("long")) {
      return 350;
    }

    if (! fields_1[8].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.FieldTest$Sample_1")) {
      return 360;
    }
    if (! fields_1[8].getType().toString().equals("boolean")) {
      return 370;
    }
    
    if (fields_1.length != 9) {
      return 380;
    }
    if (! classes_1[0].toString().equals("class java.lang.Integer")) {
      return 390;
    }
    if (((Integer)results_1[0]).intValue() != 1) {
      return 400;
    }
    if (! classes_1[1].toString().equals("class java.lang.Float")) {
      return 410;
    }
    if (((Float)results_1[1]).floatValue() != 2.0) {
      return 420;
    }
    if (! classes_1[2].toString().equals("class java.lang.Short")) {
      return 430;
    }
    if (((Short)results_1[2]).shortValue() != 3) {
      return 440;
    }
    if (! classes_1[3].toString().equals("class java.lang.Byte")) {
      return 450;
    }
    if (((Byte)results_1[3]).byteValue() != 4) {
      return 460;
    }
    if (! classes_1[4].toString().equals("class java.lang.Double")) {
      return 470;
    }
    if (((Double)results_1[4]).doubleValue() != 5.0) {
      return 480;
    }
    if (! classes_1[5].toString().equals("class java.lang.String")) {
      return 490;
    }
    if (! ((String)results_1[5]).equals("six")) {
      return 500;
    }
    if (! classes_1[6].toString().equals("class java.lang.Character")) {
      return 510;
    }
    if (((Character)results_1[6]).charValue() != '7') {
      return 520;
    }
    if (! classes_1[7].toString().equals("class java.lang.Long")) {
      return 530;
    }
    if (((Long)results_1[7]).longValue() != 8) {
      return 540;
    }
    if (! classes_1[8].toString().equals("class java.lang.Boolean")) {
      return 550;
    }
    if (! ((Boolean)results_1[8]).booleanValue()) {
      return 560;
    }
    
    /*
    ** Test field names
    */

    if (! fields_1[0].toString().equals("public int gnu.testlet.wonka.vm.FieldTest$Sample_1.i_int")) {
      return 570;
    }
    if (! fields_1[1].toString().equals("public float gnu.testlet.wonka.vm.FieldTest$Sample_1.i_float")) {
      return 580;
    }
    if (! fields_1[2].toString().equals("public short gnu.testlet.wonka.vm.FieldTest$Sample_1.i_short")) {
      return 590;
    }
    if (! fields_1[3].toString().equals("public byte gnu.testlet.wonka.vm.FieldTest$Sample_1.i_byte")) {
      return 600;
    }
    if (! fields_1[4].toString().equals("public double gnu.testlet.wonka.vm.FieldTest$Sample_1.i_double")) {
      return 610;
    }
    if (! fields_1[5].toString().equals("public java.lang.String gnu.testlet.wonka.vm.FieldTest$Sample_1.i_string")) {
      return 620;
    }
    if (! fields_1[6].toString().equals("public char gnu.testlet.wonka.vm.FieldTest$Sample_1.i_char")) {
      return 630;
    }
    if (! fields_1[7].toString().equals("public long gnu.testlet.wonka.vm.FieldTest$Sample_1.i_long")) {
      return 640;
    }
    if (! fields_1[8].toString().equals("public boolean gnu.testlet.wonka.vm.FieldTest$Sample_1.i_boolean")) {
      return 650;
    }

    /*
    ** Test specific field get methods
    */
    
    try {
      if (fields_1[0].getInt(sample_1) != 1) {
        return 660;
      }
    }
    catch (IllegalAccessException ex) {
      return 670;
    }
    catch (IllegalArgumentException ex) {
      return 680;
    }

    try {
      if (fields_1[1].getFloat(sample_1) != 2.0f) {
        return 690;
      }
    }
    catch (IllegalAccessException ex) {
      return 700;
    }
    catch (IllegalArgumentException ex) {
      return 710;
    }

    try {
      if (fields_1[2].getShort(sample_1) != 3) {
        return 720;
      }
    }
    catch (IllegalAccessException ex) {
      return 730;
    }
    catch (IllegalArgumentException ex) {
      return 740;
    }

    try {
      if (fields_1[3].getByte(sample_1) != 4) {
        return 750;
      }
    }
    catch (IllegalAccessException ex) {
      return 760;
    }
    catch (IllegalArgumentException ex) {
      return 770;
    }

    try {
      if (fields_1[4].getDouble(sample_1) != 5.0) {
        return 780;
      }
    }
    catch (IllegalAccessException ex) {
      return 790;
    }
    catch (IllegalArgumentException ex) {
      return 800;
    }

    try {
      if (fields_1[6].getChar(sample_1) != '7') {
        return 810;
      }
    }
    catch (IllegalAccessException ex) {
      return 820;
    }
    catch (IllegalArgumentException ex) {
      return 830;
    }

    try {
      if (fields_1[7].getLong(sample_1) != 8) {
        return 840;
      }
    }
    catch (IllegalAccessException ex) {
      return 850;
    }
    catch (IllegalArgumentException ex) {
      return 860;
    }

    try {
      if (! fields_1[8].getBoolean(sample_1)) {
        return 870;
      }
    }
    catch (IllegalAccessException ex) {
      return 880;
    }
    catch (IllegalArgumentException ex) {
      return 890;
    }

    /*
    ** Check implicit widening conversions and the exceptions that can happen...
    */

    // for boolean

    try {
      try {
        fields_1[6].getBoolean(sample_1);
        return 900;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].getBoolean(sample_1);
        return 910;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[2].getBoolean(sample_1);
        return 920;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].getBoolean(sample_1);
        return 930;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].getBoolean(sample_1);
        return 940;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[1].getBoolean(sample_1);
        return 950;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[4].getBoolean(sample_1);
        return 960;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 970;
    }

    // for char

    try {
      try {
        fields_1[8].getChar(sample_1);
        return 980;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].getChar(sample_1);
        return 990;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[2].getChar(sample_1);
        return 1000;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].getChar(sample_1);
        return 1010;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].getChar(sample_1);
        return 1020;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[1].getChar(sample_1);
        return 1030;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[4].getChar(sample_1);
        return 1040;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1050;
    }

    // for byte

    try {
      try {
        fields_1[8].getByte(sample_1);
        return 1060;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].getByte(sample_1);
        return 1070;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[2].getByte(sample_1);
        return 1080;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].getByte(sample_1);
        return 1090;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].getByte(sample_1);
        return 1100;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[1].getByte(sample_1);
        return 1110;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[4].getByte(sample_1);
        return 1120;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1130;
    }

    // for int

    try {
      if (fields_1[3].getInt(sample_1) != 4) {
        return 1140;
      }
      if (fields_1[2].getInt(sample_1) != 3) {
        return 1150;
      }
      if (fields_1[6].getInt(sample_1) != 7 + '0') {
        return 1160;
      }
      try {
        fields_1[1].getInt(sample_1);
        return 1170;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].getInt(sample_1);
        return 1180;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[4].getInt(sample_1);
        return 1190;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1200;
    }
    catch (IllegalArgumentException ex) {
      return 1210;
    }

    // for float

    try {
      if (fields_1[3].getFloat(sample_1) != 4.0) {
        return 1220;
      }
      if (fields_1[2].getFloat(sample_1) != 3.0) {
        return 1230;
      }
      if (fields_1[6].getFloat(sample_1) != (float)(7 + '0')) {
        return 1240;
      }
      if (fields_1[0].getFloat(sample_1) != 1.0) {
        return 1250;
      }
      if (fields_1[7].getFloat(sample_1) != 8.0) {
        return 1260;
      }

      // Now the unpermitted conversions

      try {
        fields_1[4].getInt(sample_1);
        return 1270;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1280;
    }
    catch (IllegalArgumentException ex) {
      return 1290;
    }

    // for short

    try {
      if (fields_1[3].getShort(sample_1) != 4) {
        return 1300;
      }

      // Now the unpermitted conversions

      try {
        fields_1[6].getShort(sample_1);
        return 1310;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].getShort(sample_1);
        return 1320;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].getShort(sample_1);
        return 1330;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[1].getShort(sample_1);
        return 1340;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[4].getShort(sample_1);
        return 1350;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1360;
    }
    catch (IllegalArgumentException ex) {
      return 1370;
    }

    // for double

    try {
      if (fields_1[3].getDouble(sample_1) != 4.0) {
        return 1380;
      }
      if (fields_1[2].getDouble(sample_1) != 3.0) {
        return 1390;
      }
      if (fields_1[6].getDouble(sample_1) != (double)(7 + '0')) {
        return 1400;
      }
      if (fields_1[0].getDouble(sample_1) != 1.0) {
        return 1410;
      }
      if (fields_1[7].getDouble(sample_1) != 8.0) {
        return 1420;
      }
      if (fields_1[1].getDouble(sample_1) != 2.0) {
        return 1430;
      }
    }
    catch (IllegalAccessException ex) {
      return 1440;
    }
    catch (IllegalArgumentException ex) {
      return 1450;
    }

    // for long

    try {
      if (fields_1[3].getLong(sample_1) != 4) {
        return 1460;
      }
      if (fields_1[2].getLong(sample_1) != 3) {
        System.out.println("Got " + fields_1[2].getLong(sample_1));
        return 1470;
      }
      if (fields_1[6].getLong(sample_1) != 7 + '0') {
        return 1480;
      }
      if (fields_1[0].getLong(sample_1) != 1) {
        return 1490;
      }

      // Now unpermitted ones...
      
      try {
        fields_1[1].getLong(sample_1);
        return 1500;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[4].getLong(sample_1);
        return 1510;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1520;
    }
    catch (IllegalArgumentException ex) {
      return 1530;
    }

    /*
    ** Check explicit set methods and the widening conversions for these...
    */
    
    try {
      fields_1[0].setInt(sample_1, 100);
      if (sample_1.i_int != 100) {
        return 1540;
      }
      fields_1[7].setInt(sample_1, 100);
      if (sample_1.i_long != 100) {
        return 1550;
      }
      fields_1[1].setInt(sample_1, 100);
      if (sample_1.i_float != 100.0f) {
        return 1560;
      }
      fields_1[4].setInt(sample_1, 100);
      if (sample_1.i_double != 100.0) {
        return 1570;
      }
    }
    catch (IllegalAccessException ex) {
      return 1580;
    }
    catch (IllegalArgumentException ex) {
      return 1590;
    }

    try {
      fields_1[1].setFloat(sample_1, 101.0f);
      if (sample_1.i_float != 101.0f) {
        return 1600;
      }
      fields_1[4].setFloat(sample_1, 101.0f);
      if (sample_1.i_double != 101.0) {
        return 1610;
      }
    }
    catch (IllegalAccessException ex) {
      return 1620;
    }
    catch (IllegalArgumentException ex) {
      return 1630;
    }

    try {
      fields_1[2].setShort(sample_1, (short)102);
      if (sample_1.i_short != 102) {
        return 1640;
      }
      fields_1[0].setShort(sample_1, (short)102);
      if (sample_1.i_int != 102) {
        return 1650;
      }
      fields_1[7].setShort(sample_1, (short)102);
      if (sample_1.i_long != 102) {
        return 1660;
      }
      fields_1[1].setShort(sample_1, (short)102);
      if (sample_1.i_float != 102.0f) {
        return 1670;
      }
      fields_1[4].setShort(sample_1, (short)102);
      if (sample_1.i_double != 102.0) {
        return 1680;
      }
    }
    catch (IllegalAccessException ex) {
      return 1690;
    }
    catch (IllegalArgumentException ex) {
      return 1700;
    }

    try {
      fields_1[3].setByte(sample_1, (byte)103);
      if (sample_1.i_byte != 103) {
        return 1710;
      }
      fields_1[2].setByte(sample_1, (byte)103);
      if (sample_1.i_short != 103) {
        return 1720;
      }
      fields_1[0].setByte(sample_1, (byte)103);
      if (sample_1.i_int != 103) {
        return 1730;
      }
      fields_1[7].setByte(sample_1, (byte)103);
      if (sample_1.i_long != 103) {
        return 1740;
      }
      fields_1[1].setByte(sample_1, (byte)103);
      if (sample_1.i_float != 103.0f) {
        return 1750;
      }
      fields_1[4].setByte(sample_1, (byte)103);
      if (sample_1.i_double != 103.0f) {
        return 1760;
      }
    }
    catch (IllegalAccessException ex) {
      return 1770;
    }
    catch (IllegalArgumentException ex) {
      return 1780;
    }

    /*
    ** Check the IllegalArgumentException capabilities of the explicit set methods...
    */

    // for setBoolean
    
    try {
      try {
        fields_1[4].setBoolean(sample_1, false);
        return 1790;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[1].setBoolean(sample_1, false);
        return 1800;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].setBoolean(sample_1, false);
        return 1810;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].setBoolean(sample_1, false);
        return 1820;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].setBoolean(sample_1, false);
        return 1830;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[2].setBoolean(sample_1, false);
        return 1840;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].setBoolean(sample_1, false);
        return 1850;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1860;
    }

    // for setChar
    
    try {
      try {
        fields_1[2].setChar(sample_1, 'c');
        return 1870;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].setChar(sample_1, 'c');
        return 1880;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1890;
    }

    // for setByte
    
    try {
      try {
        fields_1[6].setByte(sample_1, (byte)0x01);
        return 1900;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[8].setByte(sample_1, (byte)(0x01));
        return 1910;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1920;
    }

    // for setShort
    
    try {
      try {
        fields_1[3].setShort(sample_1, (short)2);
        return 1930;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].setShort(sample_1, (short)2);
        return 1940;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[8].setShort(sample_1, (short)2);
        return 1950;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 1960;
    }

    // for setInt
    
    try {
      try {
        fields_1[2].setInt(sample_1, 3);
        return 1970;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].setInt(sample_1, 3);
        return 1980;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].setInt(sample_1, 3);
        return 1990;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[8].setInt(sample_1, 3);
        return 2000;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 2010;
    }

    // for setLong
    
    try {
      try {
        fields_1[2].setLong(sample_1, 3);
        return 2020;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].setLong(sample_1, 3);
        return 2030;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].setLong(sample_1, 3);
        return 2040;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[8].setLong(sample_1, 3);
        return 2050;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].setLong(sample_1, 3);
        return 2060;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 2070;
    }

    // for setFloat
    
    try {
      try {
        fields_1[2].setFloat(sample_1, 3.0f);
        return 2080;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].setFloat(sample_1, 3.0f);
        return 2090;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].setFloat(sample_1, 3.0f);
        return 2100;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[8].setFloat(sample_1, 3.0f);
        return 2110;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].setFloat(sample_1, 3.0f);
        return 2120;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].setFloat(sample_1, 3.0f);
        return 2130;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 2140;
    }

    // for setDouble
    
    try {
      try {
        fields_1[2].setDouble(sample_1, 3.0);
        return 2150;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[3].setDouble(sample_1, 3.0);
        return 2160;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[6].setDouble(sample_1, 3.0);
        return 2170;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[8].setDouble(sample_1, 3.0);
        return 2180;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[0].setDouble(sample_1, 3.0);
        return 2190;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[7].setDouble(sample_1, 3.0);
        return 2200;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_1[1].setDouble(sample_1, 3.0);
        return 2210;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 2220;
    }

    /*
    ** Check object set method...
    */
    
    try {
      fields_1[0].set(sample_1, results_1[0]);
      if (sample_1.i_int != 1) {
        return 2230;
      }
      fields_1[1].set(sample_1, results_1[1]);
      if (sample_1.i_float != 2.0f) {
        return 2240;
      }
      fields_1[2].set(sample_1, results_1[2]);
      if (sample_1.i_short != 3) {
        return 2250;
      }
      fields_1[3].set(sample_1, results_1[3]);
      if (sample_1.i_byte != 4) {
        return 2260;
      }
      fields_1[4].set(sample_1, results_1[4]);
      if (sample_1.i_double != 5.0) {
        return 2270;
      }
      fields_1[5].set(sample_1, "set");
      if (! sample_1.i_string.equals("set")) {
        return 2280;
      }
      fields_1[6].set(sample_1, results_1[6]);
      if (sample_1.i_char != 7 + '0') {
        return 2290;
      }
      fields_1[7].set(sample_1, results_1[7]);
      if (sample_1.i_long != 8) {
        return 2300;
      }
      fields_1[8].set(sample_1, results_1[8]);
      if (! sample_1.i_boolean) {
        return 2310;
      }
    }
    catch (IllegalAccessException ex) {
      return 2320;
    }
    catch (IllegalArgumentException ex) {
      return 2330;
    }

    /*
    ** Try the IllegalArgumentException for the set method...
    */

    try {
      fields_1[0].set(sample_1, "set");
      if (! sample_1.i_string.equals("is integer field")) {
        return 2340;
      }
    }
    catch (IllegalAccessException ex) {
      return 2350;
    }
    catch (IllegalArgumentException ex) {
    }

    /*
    **
    ** OK, we do the same thing now for static variables, not in an inner class.
    **
    */

    Field[] fields_2 = sample_2.getClass().getFields();
    Class[] classes_2;
    Object[] results_2;

    /*
    ** Test publically accessible fields of Sample_2
    */
    
    try {
      results_2 = new Object[fields_2.length];
      classes_2 = new Class[fields_2.length];
      for (int i = 0; i < fields_2.length; i++) {
        results_2[i] = fields_2[i].get(sample_2);
        classes_2[i] = results_2[i].getClass();
      }
    }
    catch (IllegalAccessException ex) {
      return 2360;
    }

    /*
    ** Check the getType and getDeclaringClass methods...
    */
    
    if (! fields_2[0].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2370;
    }
    if (! fields_2[0].getType().toString().equals("int")) {
      return 2380;
    }

    if (! fields_2[1].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2390;
    }
    if (! fields_2[1].getType().toString().equals("float")) {
      return 2400;
    }

    if (! fields_2[2].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2410;
    }
    if (! fields_2[2].getType().toString().equals("short")) {
      return 2420;
    }

    if (! fields_2[3].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2430;
    }
    if (! fields_2[3].getType().toString().equals("byte")) {
      return 2440;
    }

    if (! fields_2[4].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2450;
    }
    if (! fields_2[4].getType().toString().equals("double")) {
      return 2460;
    }

    if (! fields_2[5].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2470;
    }
    if (! fields_2[5].getType().toString().equals("class java.lang.String")) {
      return 2480;
    }

    if (! fields_2[6].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2490;
    }
    if (! fields_2[6].getType().toString().equals("char")) {
      return 2500;
    }
    
    if (! fields_2[7].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2510;
    }
    if (! fields_2[7].getType().toString().equals("long")) {
      return 2520;
    }

    if (! fields_2[8].getDeclaringClass().toString().equals("class gnu.testlet.wonka.vm.Sample_2")) {
      return 2530;
    }
    if (! fields_2[8].getType().toString().equals("boolean")) {
      return 2540;
    }

    /*
    ** Check the hashCode
    ** hashCodes changed due to change in package names
    ** but is tested in SMFieldTest
    */

/*    if (fields_2[0].hashCode() != 413762408) {
      th.debug("hashCode = "+fields_2[0].hashCode()+",but exp 413762408");
      return 2550;
    }

    if (fields_2[1].hashCode() != 1970403035) {
      th.debug("hashCode = "+fields_2[0].hashCode()+",but exp 413762408");
      return 2560;
    }

    if (fields_2[2].hashCode() != 1915113723) {
      return 2570;
    }

    if (fields_2[3].hashCode() != -678511585) {
      return 2580;
    }

    if (fields_2[4].hashCode() != -492407722) {
      return 2590;
    }

    if (fields_2[5].hashCode() != 138694678) {
      return 2600;
    }

    if (fields_2[6].hashCode() != -678539543) {
      return 2610;
    }

    if (fields_2[7].hashCode() != -679339133) {
      return 2620;
    }

    if (fields_2[8].hashCode() != 889201943) {
      return 2630;
    }
*/
    if (fields_2.length != 9) {
      return 2640;
    }
    if (! classes_2[0].toString().equals("class java.lang.Integer")) {
      return 2650;
    }
    if (((Integer)results_2[0]).intValue() != 1) {
      return 2660;
    }
    if (! classes_2[1].toString().equals("class java.lang.Float")) {
      return 2670;
    }
    if (((Float)results_2[1]).floatValue() != 2.0) {
      return 2680;
    }
    if (! classes_2[2].toString().equals("class java.lang.Short")) {
      return 2690;
    }
    if (((Short)results_2[2]).shortValue() != 3) {
      return 2700;
    }
    if (! classes_2[3].toString().equals("class java.lang.Byte")) {
      return 2710;
    }
    if (((Byte)results_2[3]).byteValue() != 4) {
      return 2720;
    }
    if (! classes_2[4].toString().equals("class java.lang.Double")) {
      return 2730;
    }
    if (((Double)results_2[4]).doubleValue() != 5.0) {
      return 2740;
    }
    if (! classes_2[5].toString().equals("class java.lang.String")) {
      return 2750;
    }
    if (! ((String)results_2[5]).equals("SIX")) {
      return 2760;
    }
    if (! classes_2[6].toString().equals("class java.lang.Character")) {
      return 2770;
    }
    if (((Character)results_2[6]).charValue() != '7') {
      return 2780;
    }
    if (! classes_2[7].toString().equals("class java.lang.Long")) {
      return 2790;
    }
    if (((Long)results_2[7]).longValue() != 8) {
      return 2800;
    }
    if (! classes_2[8].toString().equals("class java.lang.Boolean")) {
      return 2810;
    }
    if (! ((Boolean)results_2[8]).booleanValue()) {
      return 2820;
    }
    
    /*
    ** Test field names
    */

    if (! fields_2[0].toString().equals("public static int gnu.testlet.wonka.vm.Sample_2.s_int")) {
      return 2830;
    }
    if (! fields_2[1].toString().equals("public static float gnu.testlet.wonka.vm.Sample_2.s_float")) {
      return 2840;
    }
    if (! fields_2[2].toString().equals("public static short gnu.testlet.wonka.vm.Sample_2.s_short")) {
      return 2850;
    }
    if (! fields_2[3].toString().equals("public static byte gnu.testlet.wonka.vm.Sample_2.s_byte")) {
      return 2860;
    }
    if (! fields_2[4].toString().equals("public static double gnu.testlet.wonka.vm.Sample_2.s_double")) {
      return 2870;
    }
    if (! fields_2[5].toString().equals("public static java.lang.String gnu.testlet.wonka.vm.Sample_2.s_string")) {
      return 2880;
    }
    if (! fields_2[6].toString().equals("public static char gnu.testlet.wonka.vm.Sample_2.s_char")) {
      return 2890;
    }
    if (! fields_2[7].toString().equals("public static long gnu.testlet.wonka.vm.Sample_2.s_long")) {
      return 2900;
    }
    if (! fields_2[8].toString().equals("public static boolean gnu.testlet.wonka.vm.Sample_2.s_boolean")) {
      return 2910;
    }

    /*
    ** Test specific field get methods
    */
    
    try {
      if (fields_2[0].getInt(sample_2) != 1) {
        return 2920;
      }
    }
    catch (IllegalAccessException ex) {
      return 2930;
    }
    catch (IllegalArgumentException ex) {
      return 2940;
    }

    try {
      if (fields_2[1].getFloat(sample_2) != 2.0f) {
        return 2950;
      }
    }
    catch (IllegalAccessException ex) {
      return 2960;
    }
    catch (IllegalArgumentException ex) {
      return 2970;
    }

    try {
      if (fields_2[2].getShort(sample_2) != 3) {
        return 2980;
      }
    }
    catch (IllegalAccessException ex) {
      return 2990;
    }
    catch (IllegalArgumentException ex) {
      return 3000;
    }

    try {
      if (fields_2[3].getByte(sample_2) != 4) {
        return 3010;
      }
    }
    catch (IllegalAccessException ex) {
      return 3020;
    }
    catch (IllegalArgumentException ex) {
      return 3030;
    }

    try {
      if (fields_2[4].getDouble(sample_2) != 5.0) {
        return 3040;
      }
    }
    catch (IllegalAccessException ex) {
      return 3050;
    }
    catch (IllegalArgumentException ex) {
      return 3060;
    }

    try {
      if (fields_2[6].getChar(sample_2) != '7') {
        return 3070;
      }
    }
    catch (IllegalAccessException ex) {
      return 3080;
    }
    catch (IllegalArgumentException ex) {
      return 3090;
    }

    try {
      if (fields_2[7].getLong(sample_2) != 8) {
        return 3100;
      }
    }
    catch (IllegalAccessException ex) {
      return 3110;
    }
    catch (IllegalArgumentException ex) {
      return 3120;
    }

    try {
      if (! fields_2[8].getBoolean(sample_2)) {
        return 3130;
      }
    }
    catch (IllegalAccessException ex) {
      return 3140;
    }
    catch (IllegalArgumentException ex) {
      return 3150;
    }

    /*
    ** Check implicit widening conversions and the exceptions that can happen...
    */

    // for boolean

    try {
      try {
        fields_2[6].getBoolean(sample_2);
        return 3160;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].getBoolean(sample_2);
        return 3170;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[2].getBoolean(sample_2);
        return 3180;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].getBoolean(sample_2);
        return 3190;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].getBoolean(sample_2);
        return 3200;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[1].getBoolean(sample_2);
        return 3210;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[4].getBoolean(sample_2);
        return 3220;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3230;
    }

    // for char

    try {
      try {
        fields_2[8].getChar(sample_2);
        return 3240;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].getChar(sample_2);
        return 3250;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[2].getChar(sample_2);
        return 3260;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].getChar(sample_2);
        return 3270;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].getChar(sample_2);
        return 3280;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[1].getChar(sample_2);
        return 3290;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[4].getChar(sample_2);
        return 3300;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3310;
    }

    // for byte

    try {
      try {
        fields_2[8].getByte(sample_2);
        return 3320;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].getByte(sample_2);
        return 3330;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[2].getByte(sample_2);
        return 3340;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].getByte(sample_2);
        return 3350;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].getByte(sample_2);
        return 3360;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[1].getByte(sample_2);
        return 3370;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[4].getByte(sample_2);
        return 3380;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3390;
    }

    // for int

    try {
      if (fields_2[3].getInt(sample_2) != 4) {
        return 3400;
      }
      if (fields_2[2].getInt(sample_2) != 3) {
        return 3410;
      }
      if (fields_2[6].getInt(sample_2) != 7 + '0') {
        return 3420;
      }
      try {
        fields_2[1].getInt(sample_2);
        return 3430;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].getInt(sample_2);
        return 3440;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[4].getInt(sample_2);
        return 3450;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3460;
    }
    catch (IllegalArgumentException ex) {
      return 3470;
    }

    // for float

    try {
      if (fields_2[3].getFloat(sample_2) != 4.0) {
        return 3480;
      }
      if (fields_2[2].getFloat(sample_2) != 3.0) {
        return 3490;
      }
      if (fields_2[6].getFloat(sample_2) != (float)(7 + '0')) {
        return 3500;
      }
      if (fields_2[0].getFloat(sample_2) != 1.0) {
        return 3510;
      }
      if (fields_2[7].getFloat(sample_2) != 8.0) {
        return 3520;
      }

      // Now the unpermitted conversions

      try {
        fields_2[4].getInt(sample_2);
        return 3530;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3540;
    }
    catch (IllegalArgumentException ex) {
      return 3550;
    }

    // for short

    try {
      if (fields_2[3].getShort(sample_2) != 4) {
        return 3560;
      }

      // Now the unpermitted conversions

      try {
        fields_2[6].getShort(sample_2);
        return 3570;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].getShort(sample_2);
        return 3580;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].getShort(sample_2);
        return 3590;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[1].getShort(sample_2);
        return 3600;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[4].getShort(sample_2);
        return 3610;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3620;
    }
    catch (IllegalArgumentException ex) {
      return 3630;
    }

    // for double

    try {
      if (fields_2[3].getDouble(sample_2) != 4.0) {
        return 3640;
      }
      if (fields_2[2].getDouble(sample_2) != 3.0) {
        return 3650;
      }
      if (fields_2[6].getDouble(sample_2) != (double)(7 + '0')) {
        return 3660;
      }
      if (fields_2[0].getDouble(sample_2) != 1.0) {
        return 3670;
      }
      if (fields_2[7].getDouble(sample_2) != 8.0) {
        return 3680;
      }
      if (fields_2[1].getDouble(sample_2) != 2.0) {
        return 3690;
      }
    }
    catch (IllegalAccessException ex) {
      return 3700;
    }
    catch (IllegalArgumentException ex) {
      return 3710;
    }

    // for long

    try {
      if (fields_2[3].getLong(sample_2) != 4) {
        return 3720;
      }
      if (fields_2[2].getLong(sample_2) != 3) {
        System.out.println("Got " + fields_2[2].getLong(sample_2));
        return 3730;
      }
      if (fields_2[6].getLong(sample_2) != 7 + '0') {
        return 3740;
      }
      if (fields_2[0].getLong(sample_2) != 1) {
        return 3750;
      }

      // Now unpermitted ones...
      
      try {
        fields_2[1].getLong(sample_2);
        return 3760;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[4].getLong(sample_2);
        return 3770;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 3780;
    }
    catch (IllegalArgumentException ex) {
      return 3790;
    }

    /*
    ** Check explicit set methods and the widening conversions for these...
    */
    
    try {
      fields_2[0].setInt(sample_2, 100);
      if (Sample_2.s_int != 100) {
        return 3800;
      }
      fields_2[7].setInt(sample_2, 100);
      if (Sample_2.s_long != 100) {
        return 3810;
      }
      fields_2[1].setInt(sample_2, 100);
      if (Sample_2.s_float != 100.0f) {
        return 3820;
      }
      fields_2[4].setInt(sample_2, 100);
      if (Sample_2.s_double != 100.0) {
        return 3830;
      }
    }
    catch (IllegalAccessException ex) {
      return 3840;
    }
    catch (IllegalArgumentException ex) {
      return 3850;
    }

    try {
      fields_2[1].setFloat(sample_2, 101.0f);
      if (Sample_2.s_float != 101.0f) {
        return 3860;
      }
      fields_2[4].setFloat(sample_2, 101.0f);
      if (Sample_2.s_double != 101.0) {
        return 3870;
      }
    }
    catch (IllegalAccessException ex) {
      return 3880;
    }
    catch (IllegalArgumentException ex) {
      return 3890;
    }

    try {
      fields_2[2].setShort(sample_2, (short)102);
      if (Sample_2.s_short != 102) {
        return 3900;
      }
      fields_2[0].setShort(sample_2, (short)102);
      if (Sample_2.s_int != 102) {
        return 3910;
      }
      fields_2[7].setShort(sample_2, (short)102);
      if (Sample_2.s_long != 102) {
        return 3920;
      }
      fields_2[1].setShort(sample_2, (short)102);
      if (Sample_2.s_float != 102.0f) {
        return 3930;
      }
      fields_2[4].setShort(sample_2, (short)102);
      if (Sample_2.s_double != 102.0) {
        return 3940;
      }
    }
    catch (IllegalAccessException ex) {
      return 3950;
    }
    catch (IllegalArgumentException ex) {
      return 3960;
    }

    try {
      fields_2[3].setByte(sample_2, (byte)103);
      if (Sample_2.s_byte != 103) {
        return 3970;
      }
      fields_2[2].setByte(sample_2, (byte)103);
      if (Sample_2.s_short != 103) {
        return 3980;
      }
      fields_2[0].setByte(sample_2, (byte)103);
      if (Sample_2.s_int != 103) {
        return 3990;
      }
      fields_2[7].setByte(sample_2, (byte)103);
      if (Sample_2.s_long != 103) {
        return 4000;
      }
      fields_2[1].setByte(sample_2, (byte)103);
      if (Sample_2.s_float != 103.0f) {
        return 4010;
      }
      fields_2[4].setByte(sample_2, (byte)103);
      if (Sample_2.s_double != 103.0f) {
        return 4020;
      }
    }
    catch (IllegalAccessException ex) {
      return 4030;
    }
    catch (IllegalArgumentException ex) {
      return 4040;
    }

    /*
    ** Check the IllegalArgumentException capabilities of the explicit set methods...
    */

    // for setBoolean
    
    try {
      try {
        fields_2[4].setBoolean(sample_2, false);
        return 4050;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[1].setBoolean(sample_2, false);
        return 4060;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].setBoolean(sample_2, false);
        return 4070;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].setBoolean(sample_2, false);
        return 4080;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].setBoolean(sample_2, false);
        return 4090;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[2].setBoolean(sample_2, false);
        return 4100;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].setBoolean(sample_2, false);
        return 4110;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4120;
    }

    // for setChar
    
    try {
      try {
        fields_2[2].setChar(sample_2, 'c');
        return 4130;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].setChar(sample_2, 'c');
        return 4140;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4150;
    }

    // for setByte
    
    try {
      try {
        fields_2[6].setByte(sample_2, (byte)0x01);
        return 4160;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[8].setByte(sample_2, (byte)(0x01));
        return 4170;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4180;
    }

    // for setShort
    
    try {
      try {
        fields_2[3].setShort(sample_2, (short)2);
        return 4190;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].setShort(sample_2, (short)2);
        return 4200;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[8].setShort(sample_2, (short)2);
        return 4210;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4220;
    }

    // for setInt
    
    try {
      try {
        fields_2[2].setInt(sample_2, 3);
        return 4230;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].setInt(sample_2, 3);
        return 4240;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].setInt(sample_2, 3);
        return 4250;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[8].setInt(sample_2, 3);
        return 4260;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4270;
    }

    // for setLong
    
    try {
      try {
        fields_2[2].setLong(sample_2, 3);
        return 4280;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].setLong(sample_2, 3);
        return 4290;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].setLong(sample_2, 3);
        return 4300;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[8].setLong(sample_2, 3);
        return 4310;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].setLong(sample_2, 3);
        return 4320;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4330;
    }

    // for setFloat
    
    try {
      try {
        fields_2[2].setFloat(sample_2, 3.0f);
        return 4340;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].setFloat(sample_2, 3.0f);
        return 4350;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].setFloat(sample_2, 3.0f);
        return 4360;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[8].setFloat(sample_2, 3.0f);
        return 4370;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].setFloat(sample_2, 3.0f);
        return 4380;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].setFloat(sample_2, 3.0f);
        return 4390;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4400;
    }

    // for setDouble
    
    try {
      try {
        fields_2[2].setDouble(sample_2, 3.0);
        return 4410;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[3].setDouble(sample_2, 3.0);
        return 4420;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[6].setDouble(sample_2, 3.0);
        return 4430;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[8].setDouble(sample_2, 3.0);
        return 4440;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[0].setDouble(sample_2, 3.0);
        return 4450;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[7].setDouble(sample_2, 3.0);
        return 4460;
      }
      catch (IllegalArgumentException ex) {
      }
      try {
        fields_2[1].setDouble(sample_2, 3.0);
        return 4470;
      }
      catch (IllegalArgumentException ex) {
      }
    }
    catch (IllegalAccessException ex) {
      return 4480;
    }

    /*
    ** Check object set method...
    */
    
    try {
      fields_2[0].set(sample_2, results_2[0]);
      if (Sample_2.s_int != 1) {
        System.out.println(">>>" + Sample_2.s_int);
        return 4490;
      }
      fields_2[1].set(sample_2, results_2[1]);
      if (Sample_2.s_float != 2.0f) {
        return 4500;
      }
      fields_2[2].set(sample_2, results_2[2]);
      if (Sample_2.s_short != 3) {
        return 4510;
      }
      fields_2[3].set(sample_2, results_2[3]);
      if (Sample_2.s_byte != 4) {
        return 4520;
      }
      fields_2[4].set(sample_2, results_2[4]);
      if (Sample_2.s_double != 5.0) {
        return 4530;
      }
      fields_2[5].set(sample_2, "set");
      if (! Sample_2.s_string.equals("set")) {
        return 4540;
      }
      fields_2[6].set(sample_2, results_2[6]);
      if (Sample_2.s_char != 7 + '0') {
        return 4550;
      }
      fields_2[7].set(sample_2, results_2[7]);
      if (Sample_2.s_long != 8) {
        return 4560;
      }
      fields_2[8].set(sample_2, results_2[8]);
      if (! Sample_2.s_boolean) {
        return 4570;
      }
    }
    catch (IllegalAccessException ex) {
      return 4580;
    }
    catch (IllegalArgumentException ex) {
      return 4590;
    }

    /*
    ** Try the IllegalArgumentException for the set method...
    */

    try {
      fields_2[0].set(sample_2, "set");
      if (! Sample_2.s_string.equals("is integer field")) {
        return 4600;
      }
    }
    catch (IllegalAccessException ex) {
      return 4610;
    }
    catch (IllegalArgumentException ex) {
    }

    return 0;
    
  }

  public FieldTest() {
   
    sample_1 = new Sample_1();
    sample_2 = new Sample_2();
    
  }

  static void main(String[] args) {

    FieldTest ft = new FieldTest();
    int r = ft.test();
    if (r != 0) {
      System.out.println("Error at " + r);
    }
    else {
      System.out.println("FieldTest worked out ok");
    }

  }
  
}
