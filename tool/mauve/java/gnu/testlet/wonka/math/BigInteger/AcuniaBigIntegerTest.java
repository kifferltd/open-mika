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


package gnu.testlet.wonka.math.BigInteger;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.math.BigInteger;

public class AcuniaBigIntegerTest implements Testlet {

  protected TestHarness th;

  public static String string(long i){
    if(i < 0){
      return "-"+Long.toHexString(-i);
    }
    return Long.toHexString(i);
  }

  public void test (TestHarness harness){
    th = harness;
    th.setclass("java.math.BigInteger");
    test_BigInteger();
    //Math methods
    test_abs();
    test_add();    
    test_divide();
    test_divideAndRemainder();
    test_gcd();
    test_isProbablePrime();
    test_max();
    test_min();
    test_mod();
    test_modInverse();
    test_modPow();
    test_multiply();
    test_negate();
    test_pow();
    test_remainder();
    test_signum();
    test_subtract();

    //Bit Methods
    test_and();
    test_andNot();
    test_bitCount();
    test_bitLength();
    test_clearBit();
    test_flipBit();
    test_getLowestSetBit();
    test_not();
    test_or();
    test_setBit();
    test_shiftLeft();
    test_shiftRight();
    test_testBit();
    test_xor();

    //Number Api
    test_doubleValue();
    test_floatValue();
    test_intValue();
    test_longValue();
    test_toByteArray();
    test_valueOf();

    //other
    test_compareTo();
    test_equals();
    test_hashCode();
    test_toString();
  }


/**
* implemented. <br>
*
*/
  public void test_mod(){
    th.checkPoint("mod(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");

    try {
      big.mod(new BigInteger("-1"));
      th.fail("should throw a ArithmeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }

    try {
      big.mod(new BigInteger("0"));
      th.fail("should throw a ArithmeticException -- 2");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }

    try {
      big.mod(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    mod(5,3,2);
    mod(-5,3,1);
    mod(97125,3,0);
    mod(-5,364,359);
    mod(5,367,5);
    mod(12345565L,3123343L,2975536L);
    mod(89912334565L, 66526576858L, 23385757707L);
    mod(-223484583885L, 309879709809L,86395125924L);

    mod("12345678912345678998765432198765432112348579835","49849247825673457836783952785",
      "26676731909214354525437952490");
    mod("-2345678912342987998765432198765432112348579835","49849247825673457836783952785",
      "11087801140580011135649879880");
    mod("1234567891234567899876543219876543218234982345789247829412348579835","979697674643549849247825673457836783952785",
      "790925574855982590643417942974388632756275");
    mod("49247284972849679567567886567276278257876678768612345678912345678998765432198765432112348579835",
      "988988908098090804984924782567345872972137987836783952785","573526136119471212993887594038030919565414271460411480300");
    mod("-908989080789687955950050508770609970780070780781234567891234567899876543219876543211234857983509900",
      "247825673457836783952785","37062322574424925400690");

    mod("90989348094820347823898323489252456827598927592578925672748825789257898298482912345678912345678998765432198765432112348579835",
      "7834784782489249274274874782579425783456739538534949849247825673457836783952785",
      "5546643523640844048863099802162939135974441874753845385280281062922452880173410");
    mod("12595169882142283058787447089812853162078546088874822155657097980002670056181352673433732488947917960041709939568152849678193089215197306333528584512224310149987503053769751572423347770899874291867727199442092407404744981673710505833273896634738144081494344761947833572284816502017039986684349442937160340899161976178765614872094882670934983796812332879066887348480442893378169785377245521845273569539539773238393397021091618736969308193728311009880144704157004774012457041202902926030133325469072661872212193963372173462343736574158248032940567756192613779103115564971552122198540215535577513148663304192271568202441","131009616406159179162913685179993967377776515470994664680106764842205651333876778055213173599437198429385889729704466823974855674920418775815163030433063264243445034529461535267348205915800846479289019272754481122823707916179703259052987796228701379618828894269291659555116742096335644124316799349357169624021","42284807799608020664304728439022667605720777686894801588488645927479456473813584626301023952229711667202512498992682408684666207393430371791798217876474118991318098385358194306242260934038775420972370074139692381229999158464813335452823080352130892723669036993117310237213893837260227904387595532133264743018");
  }

  public void mod(long a, long b, long result){
    BigInteger bigA = new BigInteger(""+a);
    BigInteger bigB = new BigInteger(""+b);
    BigInteger bigR = bigA.mod(bigB);
    th.check(bigR.toString(),String.valueOf(result),"calculating '"+a+"'.mod("+b+")");
  }

  public void mod(String a, String b, String result){
    BigInteger bigA = new BigInteger(a);
    BigInteger bigB = new BigInteger(b);
    BigInteger bigR = bigA.mod(bigB);
    th.check(bigR.toString(),result,"calculating '"+a+"'.mod("+b+")");
  }

/**
* implemented. <br>
*
*/
  public void test_pow(){
    th.checkPoint("pow(int)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.pow(-1);
      th.fail("should throw a ArithmeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }

    pow(3,2,9);
    pow(3,3,27);
    pow(-3,3,-27);
    pow(3,4,81);
    pow(3,0,1);

    pow(13, 7, 62748517L);
    pow(13, 8, 62748517L*13L);
    pow(13, 9, 62748517L*13L*13L);
    pow(10, 9,  1000000000L);
    pow(10,10, 10000000000L);
    pow(10,11,100000000000L);

    pow(-13, 7,-62748517L);
    pow(-13, 8, 62748517L*13L);
    pow(-13, 9,-62748517L*13L*13L);
    pow(-10, 9,  -1000000000L);
    pow(-10,10,  10000000000L);
    pow(-10,11,-100000000000L);
    pow(2,62, -(Long.MIN_VALUE/2));
    pow(-2,63, Long.MIN_VALUE);

    pow( "12345465787976254133563567488754",0, "1");
    pow( "12345465787976254133563567488754",1, "12345465787976254133563567488754");
    pow("-12345465787976254133563567488754",0, "1");
    pow("-12345465787976254133563567488754",1,"-12345465787976254133563567488754");
    pow( "12347488754",13,
      "1550680446606988651762105322305800252485435313796217419007"
      +"24457136870656316148982583442128258673696684158584590588118214760479268864");
    pow("-12347488754",13,
      "-1550680446606988651762105322305800252485435313796217419007"
      +"24457136870656316148982583442128258673696684158584590588118214760479268864");
    pow( "12345465787976254",8,
      "539584967814122950020074892817791857631912354200438188908235060725598962020201059241364920125512432870577507923419187675323105536");
    pow("-12345465787976254",8,
      "539584967814122950020074892817791857631912354200438188908235060725598962020201059241364920125512432870577507923419187675323105536");
  }

  public void pow(long a, int b, long result){
    BigInteger bigA = new BigInteger(""+a);
    BigInteger bigR = bigA.pow(b);
    th.check(bigR.toString(),String.valueOf(result),"calculating '"+a+"'.pow("+b+")");
  }

  public void pow(String a, int b, String result){
    BigInteger bigA = new BigInteger(a);
    BigInteger bigR = bigA.pow(b);
    th.check(bigR.toString(), result, "calculating '"+a+"'.pow("+b+")");
  }

/**
* implemented. <br>
*
*/
  public void test_modPow(){
    th.checkPoint("modPow(java.math.BigInteger,java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.modPow(new BigInteger("-1"),new BigInteger("35"));
      th.fail("should throw a ArithmeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }
    try {
      big.modPow(BigInteger.ONE, new BigInteger("-1"));
      th.fail("should throw a ArithmeticException -- 2");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }
    try {
      big.modPow(big,null);
      th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    try {
      big.modPow(null,big);
      th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException npe){
      th.check(true);
    }


    th.check(big.modPow(new BigInteger("-1"),new BigInteger("34")).toString(10),"23","modInverse should be calculated");

    modPow(3,2,2,1);
    modPow(3,3,2,1);
    modPow(3,3,4,3);
    modPow(3,4,4,1);
    modPow(13, 7, 25, 62748517L%25);
    modPow(13, 8, 17, 62748517L*13L%17);
    modPow(13, 9, 13,0);
    modPow(10, 9, 987654, 1000000000L%987654);
    modPow(10,10, 7654, 10000000000L %7654);
    modPow(10,11, 2345, 100000000000L%2345);
    modPow("12345678998786532212235467776","6533301","9977562414123435667567","2984929374493681814177");
    modPow("12345678998786","65","9977562414","86997848");

    modPow("-12345678998786532212235467776","6533301","9977562414123435667567","6992633039629753853390");
    modPow("9988998967776","123354545301","995667567","323137518");
    modPow("12345678998786532212235467776","123456777776533301","567","523");
    modPow("123445665544", "9765444434", "7655343423", "2237096950");
    modPow("12344567665544", "976445444434", "76559343423", "74613601942");
    modPow("1234456623235544", "9765443344434", "76553233243423", "40834256987944");
    modPow("12344566665544", "9765655444434", "76545545343423", "49693535965165");
    modPow("1234456651223544", "976665444434", "7655343423", "5222167482");
    modPow("1234456655334545666575444", "9765444434", "7655343423", "395675569");
    modPow("1234456121365544", "976544445656575756734", "7655343423", "7004255545");
    modPow("12233233445665544", "976546644434", "76553433345423", "63477018453688");
    modPow("123445665544", "976546644434", "76553445454575473423", "65846576269487214328");
    modPow("234354123445665544", "976544466434", "7655343423", "1418788855");
    modPow("123445665544", "97654467654354434", "7655343423", "1821783142");
    modPow("1234456453353465544", "976665444434", "7655343423", "7017850057");
    modPow("34123445665544", "964456457765876444434", "7655343423", "2556264946");
    modPow("1234456654355544", "97654449434", "7655343423", "3461552019");
    modPow("123445665544", "976544894434", "7655343423", "2851454272");
    modPow("1234478665544", "976548844434", "7655343423", "1755030250");
    modPow("6776587123445665544", "976548844434", "76553423433423", "32340146903101");
    modPow("123445877665544", "978865444434", "765235343423", "217635236115");
    modPow("1234457665544", "97654744434", "7655343465423", "2726195001748");
    modPow("123445688765544", "976577444434", "7655545443423", "4150620734877");
    /*
    benchMark(
        "12344223546576775869870997846343213124257898700987645331265479867544356798675457696854325679123456789098765543430976545342329876554421333345546665", 
        "97651786799698565456121223544654645675467676212345567788999999956533567744456432546709876570709057623424235454453452567658578578764566546334444437", 
        "94545454564544342386564543231323435464675767980707076596847345766453232456752341234213124142352");   
         
  }

  private void benchMark(String a, String b, String c) {
    System.out.println("AcuniaBigIntegerTest.benchMark()");
    BigInteger bigA = new BigInteger(a);
    BigInteger bigB = new BigInteger(b);
    BigInteger bigC = new BigInteger(c);
    bigA.modPow(bigB,bigC);
    long stime = System.currentTimeMillis();
    for (int j=0; j < 150 ; j++) {
      for(int i=0 ; i < 15 ; i++) {
        bigA.modPow(bigB,bigC);
      }
      Runtime.getRuntime().gc();
      System.out.println("freeMemory = "+Runtime.getRuntime().freeMemory());
    }
    long etime = System.currentTimeMillis();
    
    System.out.println("modPow took "+(etime-stime)+" ms");
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    //*/
}

  public void modPow(long a, long b, long c,long result){
    BigInteger bigA = new BigInteger(""+a);
    BigInteger bigB = new BigInteger(""+b);
    BigInteger bigC = new BigInteger(""+c);
    BigInteger bigR = bigA.modPow(bigB,bigC);
    th.check(bigR.toString(), String.valueOf(result),"calculating '"+a+"'.modPow("+b+","+c+")");
  }

  public void modPow(String a, String b, String c,String result){
    BigInteger bigA = new BigInteger(a);
    BigInteger bigB = new BigInteger(b);
    BigInteger bigC = new BigInteger(c);
    BigInteger bigR = bigA.modPow(bigB,bigC);
    th.check(bigR.toString(), result,"calculating '"+a+"'.modPow("+b+","+c+")");
  }

/**
* implemented. <br>
*
*/
  public void test_multiply(){
    th.checkPoint("multiply(java.math.BigInteger)java.math.BigInteger");

    BigInteger big = new BigInteger("12345");
    try {
      big.multiply(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    multiply(0x7fabcdefL,0xabcdef12L);
    multiply(0x7fabcdefL,0xcdef12L);
    multiply(0xabcdefL,0xabcdef12L);
    multiply(5,7);
    multiply(2,256);
    multiply(1024,7);
    multiply(1024,1024);
    multiply(7,1025);
    multiply(7,1);
    multiply(0,1025);
    multiply(15,0);
    multiply(12345,1234567);
    multiply(123456789,123456789);

    multiply("123456789987654321","987654321123456789","121932632103337905662094193112635269");

    multiply("123456789123456789098654323456643211234567865432","1234567895856746258569137014789257913679146781367",
      "152415788377376085417465147944675539635643256078436354258399545659453653659773023673270381005544");

    multiply("1234567891234567890986543234598765432123456786643211234567865432",
      "12345678958567462585691370147892579112345678908765432123456765432345673679146781367",
      "1524157883773760854174651479486673071902548822341049884949884280953381349915660783517"+
      "8840677868043724081007266894620111101470529335993270381005544");

    multiply("123456789123456789098765432123456789098654323456643211234567865432",
      "123456789585674625812345678569137014789257913679112345678654346781367",
      "1524157883773760853625800690774431864556931169407623900333839951271437991277816505044"+
      "5760106981204278751782203252401380090556781005544");

    multiply("123456789123456733489098765432123456789098654323456643211234567865432",
      "12345678958567462581234567833569137014789257913679112345678654346781367",
      "15241578837737601670867090279707885023964113745670678721918698257608657"+
      "56663070083706495794806066981204278751782203252401380090556781005544");
    multiply("123456789123456789098334455765432123456789098654323456643211234567865432",
      "1234567895856746258123456785691370167678847892057913679112345678654346781367",
      "1524157883773760853620479995019526736968086442256347695885460360859966399341"+
      "19194755977732728699401197872461397701161382203252401380090556781005544");
    multiply("12345678912345678909876543212345688990789098654323456643211234567865432",
      "123456789585674625812345678569137014789257913670765439112345678654346781367",
      "152415788377376085362580069077443310911540307002031154688342332905941294877"+
      "8604664370387034837705031377735431662402826563572401380090556781005544");

  }

  public void multiply(long a, long b){
    BigInteger bigi = new BigInteger(""+a);
    BigInteger big2 = new BigInteger(""+b);

    th.check(bigi.multiply(big2).toString(10), ""+(a*b), "multiplying '"+a+"' * '"+b+"'");
    th.check(bigi.negate().multiply(big2).toString(10), ""+(-a*b), "multiplying '-"+a+"' * '"+b+"'");
    th.check(bigi.multiply(big2.negate()).toString(10), ""+(-a*b), "multiplying '"+a+"' * '-"+b+"'");
    th.check(bigi.negate().multiply(big2.negate()).toString(10), ""+(a*b), "multiplying '-"+a+"' * '-"+b+"'");
  }

  public void multiply(String a, String b, String result){
    BigInteger bigi = new BigInteger(a);
    BigInteger big2 = new BigInteger(b);

    th.check(bigi.multiply(big2).toString(), result, "multiplying '"+a+"' * '"+b+"'");
    th.check(bigi.multiply(big2.negate()).toString(), "-"+result, "multiplying '"+a+"' * '-"+b+"'");
    th.check(bigi.negate().multiply(big2).toString(), "-"+result, "multiplying '-"+a+"' * '"+b+"'");
    th.check(bigi.negate().multiply(big2.negate()).toString(), result, "multiplying '-"+a+"' * '-"+b+"'");
  }

/**
* implemented. <br>
*
*/
  public void test_divide(){
    th.checkPoint("divide(java.math.BigInteger)java.math.BigInteger");
/*
    BigInteger big = new BigInteger("12345");
    try {
      big.divide(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }
*/
    divide("12345678912345678998765432198765432112348579835","49849247825673457836783952785", "247660284775398017");
/*
    divide("90989348094820347823898323489252456827598927592578925672748825789257898298482912345678912345678998765432198765432112348579835",
      "7834784782489249274274874782579425783456739538534949849247825673457836783952785",
      "11613509575678660510806770219390185177513850505");

    divide("12345678998765432456789","1000000", "12345678998765432");

    divide("1234", "123","10");
    divide("123123123", "123","1001001");
    divide("12341234598", "123","100335240");
    divide("12341234567888998765432221124566", "123","100335240389341453377497732720");
    divide("123123123947294782347924783248792482824", "123432423","997494183090732832393031636823");
    divide("123456789012345678922109840947294523456789123234445", "12345678989592729420987654321",
      "9999999928429889029787");

    divide("987654421123456789012345678922109840947294523456789123234445", "98812345678989592729420987654321",
      "9995253268574729634797919952");

    divide("1987654421123456789012345678922109840947294523456789123234445", "181",
      "10981516138803628668576495463658065419598312284291652614554");
    divide("19876544211234567890123456789221098409472945234567891232344456", "481",
      "41323376738533405176971843636634300227594480737147383019427");

    divide("19876544211234567890123456789221098409472945234567891232344456",
      "19876544211234567890123456789221098409472945234567891232344456","1");
*/
    //more tests in divideAndRemainderr*/
  }

  public void divide(String a, String b, String result){
    BigInteger bigi = new BigInteger(a);
    BigInteger big2 = new BigInteger(b);
    th.check(bigi.divide(big2).toString(), result, "testing '"+a+"' / '"+b+"'");
/*
    th.check(bigi.divide(big2.negate()).toString(), "-"+result, "testing '"+a+"' / '-"+b+"'");
    th.check(bigi.negate().divide(big2).toString(), "-"+result, "testing '-"+a+"' / '"+b+"'");
    th.check(bigi.negate().divide(big2.negate()).toString(), result, "testing '-"+a+"' / '-"+b+"'");
*/
  }

/**
* implemented. <br>
*
*/
  public void test_divideAndRemainder(){
    th.checkPoint("divideAndRemainder(java.math.BigInteger)java.math.BigInteger");
    try {
      divideAndRemainder(1, 0);
      th.fail("didn't throw Exception");
    }
    catch(ArithmeticException ae){
      th.check(true, "caught correct Exception");
    }

    BigInteger big = new BigInteger("12345");
    try {
      big.divideAndRemainder(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    divideAndRemainder(221L, 20124435L);
    divideAndRemainder( 221, 1);
    divideAndRemainder( 221,  20);
    divideAndRemainder( 21,  200);

    divideAndRemainder( 0xf221,  0x2000);
    divideAndRemainder( Long.MAX_VALUE, Long.MAX_VALUE/3);

    divideAndRemainder( 0xf221,  20);
    divideAndRemainder( 0x7ff1afafL,0x04356820L);
    divideAndRemainder( 0x7fffffffL,0x43abd820L);
    divideAndRemainder( 0x7ed1afafL,0x0fffffffL);
    divideAndRemainder( 0x7221afafL,0x12345678L);
    divideAndRemainder( 0x7221afafL,0x87654321L);
    divideAndRemainder( 0x7221afafL,0x7fedacffL);
    divideAndRemainder( 0x7221afafL,0x00435678L);
    divideAndRemainder( 0x7221afafL,0x00567820L);
    divideAndRemainder( 0xffee221L,  230);
    divideAndRemainder( 0xf221,  20);

    divideAndRemainder( 0xf221,  200);
    divideAndRemainder( 0x7221,  231);
    divideAndRemainder( Long.MAX_VALUE, 111);
    divideAndRemainder( Long.MAX_VALUE, 231);
    divideAndRemainder( 0xf221, -20);
    divideAndRemainder(22112344545L, 201232354L);
    divideAndRemainder(221232345365647L, 90887896786L);
    divideAndRemainder( 221098409472945L,79592729420L);
    divideAndRemainder(221232345365647L,90887896786L);
    divideAndRemainder(221098409472945L,89592729420L);
    divideAndRemainder( Long.MAX_VALUE, 895L);
    divideAndRemainder( "123456789098765432101","1234567890987654321", "100", "1");
    divideAndRemainder( "1234567890987654321123456789098765432191","1234567890987654321", "1000000000000000000100", "91");
    divideAndRemainder( "12345678909876548674236723468743211234567890987654321919867946738461347618467124671894814761894781478194791479",
      "12345678909876543217914678247782349823478987896147682137612369",
      "1000000000000000441962089331183170935361892503913", "6715597036491976622262763579503321650514768988002025385091582");

    divideAndRemainder( "1234567976542454676543454675643245656489098765432101","1234567890987654321",
      "1000000069299388863175125850931579", "809699412890729242");
    divideAndRemainder( "123456785784578457645677676878978879098765432101","123468677876567890987654321",
      "999903683329294884717", "54781761811183194823519944");
    divideAndRemainder( "1234567890986876876786786786788765432101","12345678976867870987654321",
      "99999999457307", "3339014710196691851858554");

    divideAndRemainder( "12345678909877834768423645267865432101","12",
      "1028806575823152897368637105655452675", "1");

    divideAndRemainder("1987654421123456789012345678922109840947294523456789123234445", "181",
      "10981516138803628668576495463658065419598312284291652614554","171");
    divideAndRemainder("19876544211234567890123456789221098409472945234567891232344456", "481",
      "41323376738533405176971843636634300227594480737147383019427","69");

    divideAndRemainder( "12345678909765445675676543458765432101","123456787678687890987654321",
      "100000001149", "44807534098724613950617272");
    divideAndRemainder( "1234567890987654365546453243564545675544567565456776546788765434567652101","91234567890987654321",
      "13531799618569877847450903862313109613935372513897557", "1689025149845258304");
    divideAndRemainder( "12345678909174561456246285625892659259536539536578959249469498765432101","781234567890987654321",
      "15802781157652588279342366959355276738471005685664", "609366041877148077957");
    divideAndRemainder( "12345672864284724786472846285268562527847648768763476234867789098765432101","168234567890988767654321",
      "73383686950023082859905370324735874795318968354252", "61088368877718958909209");

  }

  public void divideAndRemainder(long a, long b){
    BigInteger bigi = new BigInteger(""+a);
    BigInteger big2 = new BigInteger(""+b);

    BigInteger div = bigi.divide(big2);
    BigInteger rem = bigi.remainder(big2);
    BigInteger[] big = bigi.divideAndRemainder(big2);
    th.check(div.toString(10),""+(a / b), "division  '"+a+"' / '"+b+"'");

    th.check(rem.toString(10),""+(a % b), "remainder '"+a+"' % '"+b+"'");
    th.check(big[0].toString(10),""+(a / b), "divideAndRemainder[0] '"+a+"' / '"+b+"'");
    th.check(big[1].toString(10),""+(a % b), "divideAndRemainder[0] '"+a+"' % '"+b+"'");

    div = bigi.divide(big2.negate());
    rem = bigi.remainder(big2.negate());
    big = bigi.divideAndRemainder(big2.negate());
    th.check(div.toString(10),""+(a / (-b)), "division  '"+a+"' / '-"+b+"'");
    th.check(rem.toString(10),""+(a % (-b)), "remainder '"+a+"' % '-"+b+"'");
    th.check(big[0].toString(10),""+(a / (-b)), "divideAndRemainder[0] '"+a+"' / '-"+b+"'");
    th.check(big[1].toString(10),""+(a % (-b)), "divideAndRemainder[0] '"+a+"' % '-"+b+"'");

    div = bigi.negate().divide(big2);
    rem = bigi.negate().remainder(big2);
    big = bigi.negate().divideAndRemainder(big2);
    th.check(div.toString(10),""+((-a) / b), "division  '-"+a+"' / '"+b+"'");
    th.check(rem.toString(10),""+((-a) % b), "remainder '-"+a+"' % '"+b+"'");
    th.check(big[0].toString(10),""+((-a) / b), "divideAndRemainder[0] '-"+a+"' / '"+b+"'");
    th.check(big[1].toString(10),""+((-a) % b), "divideAndRemainder[0] '-"+a+"' % '"+b+"'");

    div = bigi.negate().divide(big2.negate());
    rem = bigi.negate().remainder(big2.negate());
    big = bigi.negate().divideAndRemainder(big2.negate());
    th.check(div.toString(10),""+((-a) / (-b)), "division  '-"+a+"' / '-"+b+"'");
    th.check(rem.toString(10),""+((-a) % (-b)), "remainder '-"+a+"' % '-"+b+"'");
    th.check(big[0].toString(10),""+((-a) / (-b)), "divideAndRemainder[0] '-"+a+"' / '-"+b+"'");
    th.check(big[1].toString(10),""+((-a) % (-b)), "divideAndRemainder[0] '-"+a+"' % '-"+b+"'");

  }

  public void divideAndRemainder(String a, String b, String q, String r){
    BigInteger bigi = new BigInteger(a);
    BigInteger big2 = new BigInteger(b);

    BigInteger div = bigi.divide(big2);
    BigInteger rem = bigi.remainder(big2);
    BigInteger[] big = bigi.divideAndRemainder(big2);
    th.check(div.toString(10),""+q, "division  '"+a+"' / '"+b+"'");
    th.check(rem.toString(10),""+r, "remainder '"+a+"' % '"+b+"'");

    th.check(big[0].toString(10),""+q, "divideAndRemainder[0] '"+a+"' / '"+b+"'");
    th.check(big[1].toString(10),""+r, "divideAndRemainder[0] '"+a+"' % '"+b+"'");

    div = bigi.divide(big2.negate());
    rem = bigi.remainder(big2.negate());
    big = bigi.divideAndRemainder(big2.negate());
    th.check(div.toString(10),"-"+q, "division  '"+a+"' / '-"+b+"'");
    th.check(rem.toString(10),r, "remainder '"+a+"' % '-"+b+"'");
    th.check(big[0].toString(10),"-"+q, "divideAndRemainder[0] '"+a+"' / '-"+b+"'");
    th.check(big[1].toString(10),r, "divideAndRemainder[0] '"+a+"' % '-"+b+"'");

    div = bigi.negate().divide(big2);
    rem = bigi.negate().remainder(big2);
    big = bigi.negate().divideAndRemainder(big2);
    th.check(div.toString(10),"-"+q, "division  '-"+a+"' / '"+b+"'");
    th.check(rem.toString(10),"-"+r, "remainder '-"+a+"' % '"+b+"'");
    th.check(big[0].toString(10),"-"+q, "divideAndRemainder[0] '-"+a+"' / '"+b+"'");
    th.check(big[1].toString(10),"-"+r, "divideAndRemainder[0] '-"+a+"' % '"+b+"'");

    div = bigi.negate().divide(big2.negate());
    rem = bigi.negate().remainder(big2.negate());
    big = bigi.negate().divideAndRemainder(big2.negate());
    th.check(div.toString(10),q, "division  '-"+a+"' / '-"+b+"'");
    th.check(rem.toString(10),"-"+r, "remainder '-"+a+"' % '-"+b+"'");
    th.check(big[0].toString(10),q, "divideAndRemainder[0] '-"+a+"' / '-"+b+"'");
    th.check(big[1].toString(10),"-"+r, "divideAndRemainder[0] '-"+a+"' % '-"+b+"'");
  }

/**
*   not implemented. <br>
*
*/
  public void test_BigInteger(){
    th.checkPoint("BigInteger(java.lang.String)");

    BigInteger bigi = new BigInteger("00");
    th.check(bigi.toString(), "0");
    bigi = new BigInteger("-00");
    th.check(bigi.toString(10), "0");
    bigi = new BigInteger("12345678901234567890");
    th.check(bigi.toString(10),"12345678901234567890");
    bigi = new BigInteger("123456789012345678901");
    th.check(bigi.toString(10),"123456789012345678901");
    bigi = new BigInteger("1234567890123456789012");
    th.check(bigi.toString(10),"1234567890123456789012");
    bigi = new BigInteger("12345678901234567890123");
    th.check(bigi.toString(10),"12345678901234567890123");
    bigi = new BigInteger("123456789012345678901234");
    th.check(bigi.toString(10),"123456789012345678901234");
    bigi = new BigInteger("987654321123456789012345678901234");
    th.check(bigi.toString(10),"987654321123456789012345678901234");
    bigi = new BigInteger("9998889998889991234567890123456789012345678901234567890");
    th.check(bigi.toString(10),"9998889998889991234567890123456789012345678901234567890");

    th.checkPoint("BigInteger(java.lang.String,int)");

    bigi = new BigInteger("00",10);
    th.check(bigi.toString(10), "0");
    bigi = new BigInteger("-00",10);
    th.check(bigi.toString(10), "0");
    bigi = new BigInteger("ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890",16);
    th.check(bigi.toString(16), "ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890".toLowerCase());
    bigi = new BigInteger("12345678901234567890",10);
    th.check(bigi.toString(10),"12345678901234567890");
    bigi = new BigInteger("123456789012345678901",10);
    th.check(bigi.toString(10),"123456789012345678901");
    bigi = new BigInteger("1234567890123456789012",10);
    th.check(bigi.toString(10),"1234567890123456789012");
    bigi = new BigInteger("12345678901234567890123",10);
    th.check(bigi.toString(10),"12345678901234567890123");
    bigi = new BigInteger("123456789012345678901234",10);
    th.check(bigi.toString(10),"123456789012345678901234");
    bigi = new BigInteger("987654321123456789012345678901234",10);
    th.check(bigi.toString(10),"987654321123456789012345678901234");
    bigi = new BigInteger("9998889998889991234567890123456789012345678901234567890",10);
    th.check(bigi.toString(10),"9998889998889991234567890123456789012345678901234567890");

    th.checkPoint("BigInteger(byte[])");

    th.checkPoint("BigInteger(int,byte[])");

    th.checkPoint("BigInteger(int,java.util.Random)");

    th.checkPoint("BigInteger(int,int,java.util.Random");

  }

/**
* implemented. <br>
*
*/
  public void test_abs(){
    th.checkPoint("abs()java.math.BigInteger");
    BigInteger bigi = new BigInteger("0");

    th.check(bigi.abs(), bigi);
    bigi = new BigInteger("12345");
    th.check(bigi.abs(), bigi);
    bigi = new BigInteger("-987654");
    th.check(bigi.abs(), bigi.negate());
  }

/**
*  implemented. <br>
*
*/
  public void test_add(){
    th.checkPoint("add(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.add(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }


    checkAdd(12345,0);
    checkAdd(0,123445);
    checkAdd(12345,97887978897L);
    checkAdd(123876543356523455L,87328947988738994L);
    checkAdd("123000789000456000123000789000456000", "456000123000789000456000123000789",
             "123456789123456789123456789123456789");
    checkAdd("9832367467284628657256256234863741685483467851362486146123672641784378776837613762396194714149146384716234761234314",
      "-9832367467284628657256256234863741685483467851362486146123672641784378776837613762396194714149146384716234761234313","1");
  }

  private void checkAdd(long a, long b){
    BigInteger bigi = new BigInteger(""+a);
    BigInteger big2 = new BigInteger(""+b);
    th.check(bigi.add(big2).toString(), ""+(a+b), "adding '"+a+"' + '"+b+"'");
    th.check(bigi.add(big2.negate()).toString(), ""+(a-b), "adding '"+a+"' + '-"+b+"'");
    th.check(bigi.negate().add(big2).toString(), ""+(-a+b), "adding '-"+a+"' + '"+b+"'");
    th.check(bigi.negate().add(big2.negate()).toString(), ""+(-a-b), "adding '-"+a+"' + '-"+b+"'");
  }

  private void checkAdd(String a, String b, String result){
    BigInteger bigi = new BigInteger(a);
    BigInteger big2 = new BigInteger(b);
    th.check(bigi.add(big2).toString(), result, "adding '"+a+"' + '"+b+"'");
    th.check(bigi.negate().add(big2.negate()).toString(), "-"+result, "adding '-"+a+"' + '-"+b+"'");
  }

/**
* implemented. <br>
*
*/
  public void test_gcd(){
    th.checkPoint("gcd(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.gcd(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }




    th.check(BigInteger.ZERO.gcd(BigInteger.ZERO),BigInteger.ZERO, "checking gcd(0,0) = 0");
    th.check(BigInteger.ZERO.gcd(BigInteger.ONE),BigInteger.ONE, "checking gcd(0,1) = 1");
    th.check(BigInteger.ONE.gcd(BigInteger.ZERO),BigInteger.ONE, "checking gcd(1,0) = 1");

    checkGcd("1234567","567","1");
    checkGcd("1","567","1");
    checkGcd("2","4","2");
    checkGcd("121","11","11");
    checkGcd("121","88","11");
    checkGcd("144","96","48");
    checkGcd("1234567952834824285972578257825782578475978562456972787948778257823578925782957844982357692423478234789234992",
      "567874979859578257823048402580925840840894204835720580582502852095820589258028052809589258290892953487923478",
      "22");
    checkGcd("12398776545432568654123415654643546576876545678969857624124148793578278178788876567622228242848484248",
      "56736365245647564789874235345476587351243245343444848448747848719873448848484948348918484848484848",
      "8");
    checkGcd("9498409480148719471238231052058009143459820685534654676473452547535151234567986762565768309389192182",
      "90809110371032398123478924748913748794769147613613681283375534657576768658468356231312335657689886663434567"
      ,"1");
  }

  private void checkGcd(String a, String b, String result){
    BigInteger bigi = new BigInteger(a);
    BigInteger big2 = new BigInteger(b);
    th.check(bigi.gcd(big2).toString(), result, "gcd '"+a+"' and '"+b+"'");
    th.check(bigi.gcd(big2.negate()).toString(), result, "gcd '"+a+"' and '-"+b+"'");
    th.check(bigi.negate().gcd(big2).toString(), result, "gcd '-"+a+"' and '"+b+"'");
    th.check(bigi.negate().gcd(big2.negate()).toString(), result, "gcd '-"+a+"' and '-"+b+"'");
  }

/**
*  implemented. <br>
*
*/
  public void test_isProbablePrime(){
    th.checkPoint("isProbablePrime(int)boolean");
    checkIsProbablePrime("0",5,   false);
    checkIsProbablePrime("1",5,   false);
    checkIsProbablePrime("2",500000,   true);
    checkIsProbablePrime("3",500000,   true);
    checkIsProbablePrime("5",500000,   true);
    checkIsProbablePrime("7",500000,   true);
    checkIsProbablePrime("15",8, false);
    checkIsProbablePrime("13",10, true);
    checkIsProbablePrime("31",10, true);
    checkIsProbablePrime("97",5,  true);
    checkIsProbablePrime("138787878978778978",12, false);
    checkIsProbablePrime("121231",10, false);
    checkIsProbablePrime("137591",10,  false);
    checkIsProbablePrime("139991",30, true);
    checkIsProbablePrime("319473174637471487194761496173364164781496714827645824783784894841931783994784",10, false);
    checkIsProbablePrime("8238473497847819397837878997828178297829827819232434322443423423423423423423491",30,  false);
  }

  private void checkIsProbablePrime(String value, int k, boolean prime){
    BigInteger bigi = new BigInteger(value);
    th.check(prime == bigi.isProbablePrime(k),"checking prime "+value);
    th.check(prime == bigi.negate().isProbablePrime(k),"checking prime -"+value);
  }

/**
* implemented. <br>
*
*/
  public void test_max(){
    th.checkPoint("max(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.max(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    BigInteger bigi = new BigInteger("123456");
    BigInteger equal = new BigInteger("123456");
    th.check(bigi.max(equal) == equal);
    BigInteger less = new BigInteger("12356");
    th.check(bigi.max(less) == bigi);
    BigInteger more = new BigInteger("1234567");
    th.check(bigi.max(more) == more);

  }

/**
* implemented. <br>
*
*/
  public void test_min(){
    th.checkPoint("min(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.min(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    BigInteger bigi = new BigInteger("123456");
    BigInteger equal = new BigInteger("123456");
    th.check(bigi.min(equal) == equal);
    BigInteger less = new BigInteger("12356");
    th.check(bigi.min(less) == less);
    BigInteger more = new BigInteger("1234567");
    th.check(bigi.min(more) == bigi);

  }

/**
* implemented. <br>
*
*/
  public void test_modInverse(){
    th.checkPoint("modInverse(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.modInverse(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    checkModInverse(1,2,1);
    checkModInverse(3,2,1);
    checkModInverse(4,3,1);
    checkModInverse(3,4,3);
    checkModInverse(4,5,4);
    checkModInverse(5,6,5);
    checkModInverse(4,5,4);
    checkModInverse(5,6,5);
    checkModInverse(17,9,8);
    checkModInverse(-5,6,1);
    checkModInverse(-5,4,3);
    checkModInverse(-6,5,4);
    checkModInverse(9,5,4);
    checkModInverse(6,7,6);
    checkModInverse(12345,34,23);
    checkModInverse(123456789123456789L,987654321987654322L,76304088771625089L);
    checkModInverseFail(12,-1);
    checkModInverseFail(13,-17);
    checkModInverseFail(13,0);
    checkModInverseFail(13,26);
    checkModInverseFail(13131313,1313);
    checkModInverseFail(123456789123456789L,98765432198765431L);
    checkModInverseFail(1943734598389458956L,1294784387578919234L);
  }

  private void checkModInverse(long a, long b, long result){
    BigInteger bigA = new BigInteger(""+a);
    BigInteger bigB = new BigInteger(""+b);
    BigInteger res  = new BigInteger(""+result);
    try {
      th.check(bigA.modInverse(bigB), res, "verifying modInverse of "+a+" and "+b);
    }
    catch(ArithmeticException ae){
      th.fail("verifying modInverse of "+a+" and "+b+" --> "+ae);
    }
  }

  private void checkModInverseFail(long a, long b){
    BigInteger bigA = new BigInteger(""+a);
    BigInteger bigB = new BigInteger(""+b);
    try {
      bigA.modInverse(bigB);
      th.fail("modInverse of "+a+" and "+b+" should have thrown a ArithmeticException");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_negate(){
    th.checkPoint("negate()java.math.BigInteger");
    BigInteger bigi = new BigInteger("0");
    th.check(bigi.negate(), bigi);
    checkNegate("1");
    checkNegate("1123443");
    checkNegate("129427849785879502857025872058259857285943534654636345");
    checkNegate("1852758927942957252957829472549024792085295082350285902");
    checkNegate("990830918303307104814804852589148845838054582905749534592570257820598340538520409248021");
    checkNegate("795873490268036584205896832892508285952057805725712572507525725257995561895282959156729515215671925626951269562159151");
  }

  private void checkNegate(String value){
    BigInteger bigA = new BigInteger(value);
    BigInteger bigB = new BigInteger("-"+value);
    th.check(bigA.negate(), bigB, "negating "+value);
    th.check(bigB.negate(), bigA, "negating -"+value);
  }


/**
* implemented. <br>
* more tests in divideAndRemainder ...
*/
  public void test_remainder(){
    th.checkPoint("remainder(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");

    try {
      big.remainder(new BigInteger("0"));
      th.fail("should throw a ArithmeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }

    try {
      big.remainder(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    remainder(5,3,2);
    remainder(-5,3,-2);
    remainder(97125,3,0);
    remainder(-5,364,-5);
    remainder(5,367,5);
    remainder(12345565L,3123343L,2975536L);
    remainder(89912334565L, 66526576858L, 23385757707L);

    remainder("12345678912345678998765432198765432112348579835","49849247825673457836783952785",
      "26676731909214354525437952490");
    remainder("1234567891234567899876543219876543218234982345789247829412348579835","979697674643549849247825673457836783952785",
      "790925574855982590643417942974388632756275");
    remainder("49247284972849679567567886567276278257876678768612345678912345678998765432198765432112348579835",
      "988988908098090804984924782567345872972137987836783952785","573526136119471212993887594038030919565414271460411480300");
    remainder("90989348094820347823898323489252456827598927592578925672748825789257898298482912345678912345678998765432198765432112348579835",
      "7834784782489249274274874782579425783456739538534949849247825673457836783952785",
      "5546643523640844048863099802162939135974441874753845385280281062922452880173410");
  }

  public void remainder(long a, long b, long result){
    BigInteger bigA = new BigInteger(""+a);
    BigInteger bigB = new BigInteger(""+b);
    BigInteger bigR = bigA.remainder(bigB);
    th.check(bigR.toString(),String.valueOf(result),"calculating '"+a+"'.remainder("+b+")");
  }

  public void remainder(String a, String b, String result){
    BigInteger bigA = new BigInteger(a);
    BigInteger bigB = new BigInteger(b);
    BigInteger bigR = bigA.remainder(bigB);
    th.check(bigR.toString(),result,"calculating '"+a+"'.remainder("+b+")");
  }


/**
* implemented. <br>
*
*/
  public void test_signum(){
    th.checkPoint("signum()int");
    BigInteger bigi = new BigInteger("123456");
    th.check(bigi.signum(), 1);
    bigi = new BigInteger("-123456");
    th.check(bigi.signum(), -1);
    bigi = new BigInteger("0000");
    th.check(bigi.signum(), 0);
  }

/**
* implemented. <br>
*
*/
  public void test_subtract(){
    th.checkPoint("subtract(java.math.BigInteger)java.math.BigInteger");
    BigInteger big = new BigInteger("12345");
    try {
      big.subtract(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }

    checkSubtract(12345,0);
    checkSubtract(0,123445);
    checkSubtract(12345,97887978897L);
    checkSubtract(123876543356523455L,87328947988738994L);
    checkSubtract("123456789123456789123456789123456789", "456789123456789123456789123456789", "123000000000000000000000000000000000");
    checkSubtract("9832367467284628657256256234863741685483467851362486146123672641784378776837613762396194714149146384716234761234314",
      "9832367467284628657256256234863741685483467851362486146123672641784378776837613762396194714149146384716234761234313","1");
  }

  private void checkSubtract(long a, long b){
    BigInteger bigi = new BigInteger(""+a);
    BigInteger big2 = new BigInteger(""+b);
    th.check(bigi.subtract(big2).toString(), ""+(a-b), "subtracting '"+a+"' - '"+b+"'");
    th.check(bigi.subtract(big2.negate()).toString(), ""+(a+b), "subtracting '"+a+"' - '-"+b+"'");
    th.check(bigi.negate().subtract(big2).toString(), ""+(-a-b), "subtracting '-"+a+"' - '"+b+"'");
    th.check(bigi.negate().subtract(big2.negate()).toString(), ""+(-a+b), "subtracting '-"+a+"' - '-"+b+"'");
  }

  private void checkSubtract(String a, String b, String result){
    BigInteger bigi = new BigInteger(a);
    BigInteger big2 = new BigInteger(b);
    th.check(bigi.subtract(big2).toString(), result, "subtracting '"+a+"' - '"+b+"'");
    th.check(bigi.negate().subtract(big2.negate()).toString(), "-"+result, "subtracting '-"+a+"' - '-"+b+"'");
  }

/**
*   not implemented. <br>
*
*/
  public void test_and(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_andNot(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_bitCount(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_bitLength(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_clearBit(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_flipBit(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getLowestSetBit(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_not(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_or(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_setBit(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_shiftLeft(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_shiftRight(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_testBit(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_xor(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_doubleValue(){
    th.checkPoint("doubleValue()double");
    check_doubleValue("4");
    check_doubleValue("40");
    check_doubleValue("400");
    check_doubleValue("412");
    check_doubleValue("4345");
    check_doubleValue("98765432198765433");
    check_doubleValue("9876543219876543000111000", 9.876543219876543E24);
    check_doubleValue("129876543219876543000111000", 1.2987654321987654E26);
    BigInteger tobig = new BigInteger("10").pow(309);
    th.check(tobig.doubleValue(), Double.POSITIVE_INFINITY, "checking large numbers");
    th.check(tobig.negate().doubleValue(), Double.NEGATIVE_INFINITY, "checking large numbers");
  }

  private void check_doubleValue(String value){
    th.check(new BigInteger(value).doubleValue(), Double.parseDouble(value), "checking parseDouble of '"+value+"'");
    value = "-"+value;
    th.check(new BigInteger(value).doubleValue(), Double.parseDouble(value), "checking parseDouble of '"+value+"'");
  }

  private void check_doubleValue(String value, double result){
    th.check(new BigInteger(value).doubleValue(), result, "checking parseDouble of '"+value+"'");
    value = "-"+value;
    th.check(new BigInteger(value).doubleValue(), -result, "checking parseDouble of '"+value+"'");
  }

/**
*   not implemented. <br>
*
*/
  public void test_floatValue(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_intValue(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_longValue(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_toByteArray(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_valueOf(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_compareTo(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("()");
    long add = Long.MAX_VALUE / 979;
    for (long i=0 ; i >= 0 ; i+= add) {
      String value = Long.toString(i);
      th.check(new BigInteger(value).toString(), value, value);
    }
    
    add = Long.MIN_VALUE / 957;
    for (long i=0 ; i <= 0 ; i+= add) {
      String value = Long.toString(i);
      th.check(new BigInteger(value).toString(), value, value);
    }
  }
}
