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



package gnu.testlet.wonka.util.zip;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipTest implements Testlet {

   private TestHarness th;
	
   public void test(TestHarness harness) {
	
 	th = harness;
 	th.setclass("java.util.zip.Adler32");
 	quick_Adler();
 	th.setclass("java.util.zip.CRC32");
 	quick_CRC();
 	th.setclass("java.util.zip.Deflater");
	test_Deflater();
 	quick_Deflater();
 	th.setclass("java.util.zip.ZipOutputStream");
 	test_stored();
 	th.setclass("java.util.zip.DeflaterOutputStream");
	test_streams();
 	th.setclass("java.util.zip.ZipOutputStream");
	create_file(); 		 	
 	th.setclass("java.util.jar.JarOutputStream");
	create_Jarfile();
 	th.setclass("java.util.jar.JarFile");
	//quickJarFile();		 	
  }

  public void test_Deflater(){
   	th.checkPoint("Deflater()");
   	try {
    	Deflater def = new Deflater();
    	def.end();
    	def.finished();
    	def.getTotalIn();
    	def.getTotalOut();
    	def.getAdler();
    	def.needsInput();
    	def.finish();   	
      def.reset();   	
   	}
   	catch(Exception e) {
   	 	e.printStackTrace();
   	}  	
  }

  public void test_stored(){
   	th.checkPoint("ZipOutputStream(java.io.OutputStream)");
   	try {
   		File file = new File("stored.zip");
		int i=0;
		FileOutputStream fos = new FileOutputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipEntry ze = new ZipEntry("entry1");
		CRC32 crc = new CRC32();
                String s ="Acunia\ntest stored data";
		byte[] buf = s.getBytes();
		crc.update(buf);
		ze.setSize(23);
		ze.setCrc(crc.getValue());
		ze.setCompressedSize(23);
		ze.setMethod(0);
		ZipOutputStream zos =new ZipOutputStream(bos);
		zos.putNextEntry(ze);
		zos.write(buf);
		zos.setMethod(0);
		ze = new ZipEntry("entry2");
                String s2 ="Acunia\ntest2 stored data";
		buf = s2.getBytes();
		crc.reset();
		crc.update(buf);
		ze.setSize(24);
		ze.setTime(0);
		ze.setCrc(crc.getValue());
		ze.setCompressedSize(24);
		zos.putNextEntry(ze);
		zos.write(buf);
		buf = new byte[10000];
	   	for (i=0 ; i < 10000 ; i++) {
	   	 	buf[i] = (byte)i;
	   	}
		crc.reset();
		crc.update(buf);
		ze = new ZipEntry("entry3");
		ze.setMethod(8);
		ze.setSize(10000);
    int csize = calcCompressedSize(buf);
		ze.setCrc(crc.getValue());
		ze.setCompressedSize(csize);
		zos.putNextEntry(ze);
		zos.write(buf);
		ze = new ZipEntry("entry4");
		zos.setMethod(8);
		ze.setSize(10000);
		ze.setCrc(crc.getValue());     
    ze.setCompressedSize(csize);
		zos.putNextEntry(ze);
		zos.write(buf);
	   	byte [] buf2 = (byte[])buf.clone();
                zos.close();
	   	buf = bos.toByteArray();
                fos.write(buf);
                fos.close();
		FileInputStream fis = new FileInputStream(file);
 	
 	     th.setclass("java.util.zip.ZipInputStream");
	   	th.checkPoint("ZipInputStream(java.io.InputStream)");
		ZipInputStream zip =new ZipInputStream(fis);
		buf = new byte[10000];
		
		ze = zip.getNextEntry();
		i = zip.read(buf);
		crc.reset();
		crc.update(buf,0,i);
	   	th.checkPoint("getNextEntry()java.util.zip.ZipEntry");
		th.check(i,23, "23 stored bytes read");		
		th.check(new String(buf,0,i) ,s , "data read");
		th.check(ze.getName(),"entry1", "check name ...");
		th.check(ze.getSize(), 23 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(),23 ,"check compressed size ...");
		th.check(ze.getMethod(),0,"check method ...");
		//th.debug("date ="+new Date(ze.getTime()));
		
		ze = zip.getNextEntry();
		i = zip.read(buf);
		th.check(i,24, "24 stored bytes read");
		th.check(new String(buf,0,i) ,s2 , "data read");
		crc.reset();
		crc.update(buf,0,i);
		th.check(ze.getName(),"entry2", "check name ...");
		th.check(ze.getSize(), 24 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(),24 ,"check compressed size ...");
		th.check(ze.getMethod(),0,"check method ...");
		//th.debug("date ="+new Date(ze.getTime()));
		
		ze = zip.getNextEntry();
		i = zip.read(buf);
		th.check(i,10000, "10000 stored bytes read");
		th.check(Arrays.equals(buf,buf2), "verify data read");
		crc.reset();
		crc.update(buf,0,i);
		th.check(ze.getName(),"entry3", "check name ...");
		th.check(ze.getSize(), 10000 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(), csize ,"check compressed size ...");
		th.check(ze.getMethod(),8,"check method ...");
		ze = zip.getNextEntry();
		i = zip.read(buf);
		th.check(i,10000, "10000 stored bytes read");
		th.check(Arrays.equals(buf,buf2), "verify data read");
		crc.reset();
		crc.update(buf,0,i);
		th.check(ze.getName(),"entry4", "check name ...");
		th.check(ze.getSize(), 10000 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(),csize ,"check compressed size ...");
		th.check(ze.getMethod(),8,"check method ...");
		//th.debug("date ="+new Date(ze.getTime()));
		
		th.check(zip.getNextEntry(),null , "null expected");	
		th.check(zip.getNextEntry(),null , "null expected");	
		th.check(zip.getNextEntry(),null , "null expected");	
		zip.close();
		
 	 th.setclass("java.util.zip.ZipFile");
	 	th.checkPoint("ZipFile(java.io.File)");
		ZipFile zf = new ZipFile(file);
		Enumeration e = zf.entries();
		InputStream in;
		ze = (ZipEntry)e.nextElement();
		in = zf.getInputStream(ze);
		i = in.read(buf);
		crc.reset();
		crc.update(buf,0,i);
		th.check(i,23, "23 stored bytes read");
		th.check(new String(buf,0,i) ,s , "data read");
		th.check(ze.getName(),"entry1", "check name ...");
		th.check(ze.getSize(), 23 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(),23 ,"check compressed size ...");
		th.check(ze.getMethod(),0,"check method ...");
		//th.debug("date ="+new Date(ze.getTime()));
		
	   	th.checkPoint("entries()java.util.Enumeration");
		ze = (ZipEntry)e.nextElement();
		in = zf.getInputStream(ze);
		i = in.read(buf);
		th.check(i,24, "24 stored bytes read");
		th.check(new String(buf,0,i) ,s2 , "data read");
		crc.reset();
		crc.update(buf,0,i);
		th.check(ze.getName(),"entry2", "check name ...");
		th.check(ze.getSize(), 24 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(),24 ,"check compressed size ...");
		th.check(ze.getMethod(),0,"check method ...");
		//th.debug("date ="+new Date(ze.getTime()));
		
	   	th.checkPoint("getInputStream(java.util.zip.ZipEntry)java.io.InputStream");
		ze = (ZipEntry)e.nextElement();
		in = zf.getInputStream(ze);
		i = in.read(buf);
		th.check(i,10000, "10000 stored bytes read");
		th.check(Arrays.equals(buf,buf2), "verify data read");
		crc.reset();
		crc.update(buf,0,i);
		th.check(ze.getName(),"entry3", "check name ...");
		th.check(ze.getSize(), 10000 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(), csize,"check compressed size ...");
		th.check(ze.getMethod(),8,"check method ...");
		//th.debug("date ="+new Date(ze.getTime()));
		
		ze = (ZipEntry)e.nextElement();
		in = zf.getInputStream(ze);
		i = in.read(buf);
		th.check(i,10000, "10000 stored bytes read");
		th.check(Arrays.equals(buf,buf2), "verify data read");
		crc.reset();
		crc.update(buf,0,i);
		th.check(ze.getName(),"entry4", "check name ...");
		th.check(ze.getSize(), 10000 , "check size ...");
		th.check(ze.getCrc(), crc.getValue() , "check crc32 ...");
		th.check(ze.getCompressedSize(),csize ,"check compressed size ...");
		th.check(ze.getMethod(),8,"check method ...");
		th.check(!e.hasMoreElements() , "no more Entries");
		//th.debug("date ="+new Date(ze.getTime()));
        }
   	catch(Exception e) {
   	 	th.fail("unwanted exception");
   	 	e.printStackTrace();
   	}
  }

  private int calcCompressedSize(byte[] buf) {
    Deflater def = new Deflater(8, true);
    def.setInput(buf);
    def.finish();
    buf = new byte[5000];
    while (def.deflate(buf) > 0);
    return def.getTotalOut();
  }

  public void quickJarFile() {
   	th.checkPoint("JarFile(java.lang.String)");
   	try {
   		File file = new File("pyramid.jar");
		byte[] buf = new byte [1000];
		int i=0;
		FileInputStream fis = new FileInputStream(file);
		i = fis.read(buf,0,1000);
		JarFile zf = new JarFile(file);
   		th.checkPoint("getManifest()java.util.zip.Manifest");
		th.check(zf.getManifest() != null ,"Manifest is avialable");
		JarEntry ze=null;
   		th.checkPoint("entries()java.util.Enumeration");
		Enumeration e = zf.entries();
		while (e.hasMoreElements()) {
			ze = (JarEntry) e.nextElement();
		}
		e = zf.entries();
		InputStream is = zf.getInputStream(ze);
		i = is.read(buf,0,1000);
		file = new File(ze.getName());
		file = new File(file.getName());
		//th.debug("createNewFile says :"+file.createNewFile());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(buf,0,i);
		fos.close();
   	}
   	catch(Exception e) {
   	 	th.fail("unwanted exception");
   	 	e.printStackTrace();
   	}
  }
  public void create_Jarfile() {
   	th.checkPoint("JarOutputStream(java.io.OutputStream)");
   	try {
   		File file = new File("test2.jar");
   		file.createNewFile();
		String s = "abcdefghijklmnopqrstuvwxyz";
 		String sa = "Acunia rules !, yeah baby \nmore input";
  		ZipEntry ze;
		FileOutputStream fos = new FileOutputStream(file);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		JarOutputStream gzo = new JarOutputStream(bas);
		ze = new ZipEntry("./gnu/jtest.txt");
		ze.setComment("cool\r\n");
		ze.setExtra("toppie\r\n".getBytes());
		gzo.putNextEntry(ze);
		gzo.write(s.getBytes());		
		ze.setComment("\noverwrite\r\n");
		ze.setExtra("floppie\r\n".getBytes());
		try {
			gzo.putNextEntry(ze);
			th.fail("should throw ZipException");
		}
		catch(ZipException zipe) { th.check(true);}		
		ze = new ZipEntry("./jAcunia.txt");
		gzo.putNextEntry(ze);
		gzo.write(sa.getBytes());
		gzo.setComment("hello world JAR UP !!!");		
		gzo.finish();
		fos.write(bas.toByteArray());
		fos.close();
 	
 	     th.setclass("java.util.jar.JarInputStream");
   		th.checkPoint("JarInputStream(java.io.InputStream)");
		FileInputStream fin = new FileInputStream(file);
		JarInputStream zip = new JarInputStream(fin);
		byte [] bytes = new byte [100];
		ze = zip.getNextEntry();
		th.check(zip.getManifest() != null ,"Manifest should be found");
		th.check( ze.getName() , "./gnu/jtest.txt");
		int read = zip.read(bytes,0,100);
		th.check(new String(bytes,0,read), s, "checking contents");
		th.check(zip.read(bytes) , -1, "no bytes available");
		ze = zip.getNextEntry();
		th.check( ze.getName() , "./jAcunia.txt");
		read = zip.read(bytes,0,100);
		th.check(read , 37, "37 bytes available");
		th.check(new String(bytes, 0 ,read) , sa ,"checking data");
		ze = zip.getNextEntry();
		th.check( ze, null, "null should be returned");
		th.check(zip.read(bytes) , -1, "no bytes available");
		
 	     th.setclass("java.util.jar.JarFile");
   		th.checkPoint("JarFile(java.io.File)");
		JarFile zf = new JarFile(file);
		Enumeration e = zf.entries();
		while (e.hasMoreElements()) {
			ze = (ZipEntry) e.nextElement();
			InputStream is = zf.getInputStream(ze);
			byte [] buf = new byte [100];
			int i = is.read(buf,0,100);
			th.debug("entry data :"+new String(buf,0,i));
		}
   	}
   	catch(Exception e) {
   	 	th.fail("unwanted exception");
   	 	e.printStackTrace();
   	}
  }



  public void create_file() {
   	th.checkPoint("ZipOutputStream(java.io.OutputStream)");
   	try {
   		File file = new File("./test.zip");
   		file.createNewFile();
	  	String s = "abcdefghijklmnopqrstuvwxyz";
 		  String sa = "Acunia rules !, yeah baby \nmore input";
  		ZipEntry ze;
		  FileOutputStream fos = new FileOutputStream(file);
		  ByteArrayOutputStream bas = new ByteArrayOutputStream();
  		for (int i=0; i <3 ; i++) {
   			ZipOutputStream gzo = new ZipOutputStream(bas);
   			ze = new ZipEntry("./gnu/test.txt");
		  	ze.setComment("cool\r\n");
			  ze.setExtra("toppie\r\n".getBytes());
			  gzo.putNextEntry(ze);
			  gzo.write(s.getBytes());		
			  ze.setComment("\noverwrite\r\n");
			  ze.setExtra("floppie\r\n".getBytes());
			  ze = new ZipEntry("./Acunia.txt");
    	  th.checkPoint("putNextEntry(java.util.zip.ZipEntry)void");
			  gzo.putNextEntry(ze);
			  gzo.write(sa.getBytes());
			  gzo.setComment("hello world");		
	  		gzo.finish();
		  }
		  fos.write(bas.toByteArray());
		  fos.close();
      
      ZipOutputStream gzo = new ZipOutputStream(bas);
      ze = new ZipEntry("./gnu/test.txt");
      gzo.putNextEntry(ze);      
      try {
        gzo.putNextEntry(ze);
        th.fail("should throw ZipException");
      }
      catch(ZipException zipe) { th.check(true);}   
 	
   	  th.setclass("java.util.zip.ZipInputStream");
  	  th.checkPoint("ZipInputStream(java.io.InputStream)");		
  		FileInputStream fin = new FileInputStream(file);
  		ZipInputStream zip = new ZipInputStream(fin);
  		byte [] bytes = new byte [100];
  		th.check(zip.read(bytes) , -1, "no bytes available");
  		ze = zip.getNextEntry();
  		th.check( ze.getName() , "./gnu/test.txt");
  		int read = zip.read(bytes,0,100);
  		th.check(read , 26, "26 bytes available");
  		th.check(new String(bytes,0,read), s, "checking contents");
  		th.check(zip.read(bytes) , -1, "no bytes available");
  	  th.checkPoint("getNextEntry()java.util.zip.ZipEntry");		
  		ze = zip.getNextEntry();
  		th.check( ze.getName() , "./Acunia.txt");
  		read = zip.read(bytes,0,100);
  		th.check(read , 37, "37 bytes available");
  		th.check(new String(bytes, 0 ,read) , sa ,"checking data");
  		bytes = new byte [100];
  		th.check(zip.read(bytes) , -1, "no bytes available");
  		ze = zip.getNextEntry();
      th.check(ze != null, "entry must be available ...");
  		th.check( ze.getName() , "./gnu/test.txt","got name");
  		read = zip.read(bytes,0,100);
  		th.check(read , 26, "26 bytes available");
  		th.check(new String(bytes,0,read), s, "checking contents");
  		th.check(zip.read(bytes) , -1, "no bytes available");
  		ze = zip.getNextEntry();
  		th.check( ze.getName() , "./Acunia.txt","got name");
  		read = zip.read(bytes,0,100);
  		th.check(read , 37, "37 bytes available");
  		th.check(new String(bytes, 0 ,read) , sa ,"checking data");
  		ze = zip.getNextEntry();
  		th.check( ze.getName() , "./gnu/test.txt");
  		ze = zip.getNextEntry();
  		th.check( ze.getName() , "./Acunia.txt","got name");
  		th.check(ze.getSize(), -1L, "checking Size");
  		th.check(ze.getCompressedSize(), -1L, "checking Size");
  		th.check(ze.getCrc(), -1L, "checking Size");
  	  th.checkPoint("closeEntry()void");		
  		zip.closeEntry();
  		th.check(ze.getSize(), 37L, "checking Size");
  		th.check(ze.getCompressedSize(), 66L, "checking Size");
  		th.check(ze.getCrc(), 266064239L, "checking Size");
  		ze = zip.getNextEntry();
  		th.check( ze, null, "null should be returned");
  		
   	  th.setclass("java.util.zip.ZipFile");
  	  th.checkPoint("ZipFile(java.io.File)");		
  		ZipFile zf = new ZipFile(file);
  		Enumeration e = zf.entries();
  		ze = (ZipEntry) e.nextElement();
  		InputStream is = zf.getInputStream(ze);
  		byte [] buf = new byte [100];
  		is.read(buf,0,100);
  		ze = (ZipEntry) e.nextElement();
  		is = zf.getInputStream(ze);
  		is.read(buf,0,100);
   	}
   	catch(Exception e) {
   	 	th.fail("unwanted exception");
   	 	e.printStackTrace();
   	}

  }

  public void test_streams(){
  	th.checkPoint("DeflaterOutputStream(java.io.OutputStream)");
  	try {
  		ByteArrayOutputStream bas = new ByteArrayOutputStream();
  		DeflaterOutputStream gzo = new DeflaterOutputStream(bas);
   		byte [] buf = new byte[10000];
   		for (int i=0 ; i < 10000 ; i++) {
   	 		buf[i] = (byte)i;
   		}
  		gzo.write(buf,0, 10000);
  		gzo.finish();
  		bas.write(buf,0,10);
  		Inflater inf = new Inflater(false);
  		byte [] buf2 = bas.toByteArray();
  		th.check(inf.needsInput(),"input needed");
  		th.check(!inf.finished(), "new Inflater is not finished");
  		inf.setInput(buf2);
  		buf2 = new byte[10000];
  		inf.inflate(buf2);
   		th.check(Arrays.equals(buf , buf2), "array should be equal -- 1");
   		inf = new Inflater(false);
  		buf2 = bas.toByteArray();
  		th.check(inf.needsInput(),"input needed");
  		th.check(!inf.finished(), "new Inflater is not finished");
  		inf.setInput(buf2);
  		buf2 = new byte[10000];
  		inf.inflate(buf2);
   		th.check(Arrays.equals(buf , buf2), "array should be equal -- 2");
   		th.check(inf.getRemaining(), 10,"checking remaining bytes");
   		inf = new Inflater();
  		buf2 = new byte[10000];
  		InflaterInputStream gzi = new InflaterInputStream(new ByteArrayInputStream(bas.toByteArray()),inf);
		th.check(gzi.read(buf2), 10000 , "checking return value");
		th.check(gzi.available(), 1, "stream has finished");
   		th.check(inf.getRemaining(), 10,"checking remaining bytes");
   		th.check(Arrays.equals(buf , buf2), "array should be equal");
   		
  	}
  	catch(Exception e){
  		th.fail("got unwanted exception -- de/inflater streams -- 1");
  		e.printStackTrace();	
  	}
 	th.setclass("java.util.zip.InflaterInputStream");
  	th.checkPoint("InflaterInputStream(java.io.InputStream)");
  	try {
  		ByteArrayOutputStream bas = new ByteArrayOutputStream();
  		DeflaterOutputStream gzo = new DeflaterOutputStream(bas,new Deflater(8,true));
   		byte [] buf = new byte[10000];
   		for (int i=0 ; i < 10000 ; i++) {
   	 		buf[i] = (byte)i;
   		}
	   	byte [] buf2 = (byte[])buf.clone();
  		gzo.write(buf,0, 10000);
  		gzo.finish();
  		Inflater inf = new Inflater(true);
  		InflaterInputStream gzi = new InflaterInputStream(new ByteArrayInputStream(bas.toByteArray()),inf);
  		buf = new byte[10000];
		  th.check(gzi.read(buf), 10000 , "checking return value");
   		th.check(Arrays.equals(buf , buf2), "array should be equal");
   		//th.check(!inf.finished() , "not finished yet (still expects adler32)");
   		bas = new ByteArrayOutputStream();
   		gzo = new DeflaterOutputStream(bas,new Deflater(8,true));
  		gzo.write(buf2,0, 10000);
  		gzo.finish();
  		bas.write(buf2,0,10);
  		inf = new Inflater(true);
  		gzi = new InflaterInputStream(new ByteArrayInputStream(bas.toByteArray()),inf);
  		buf = new byte[10000];
	  	th.check(gzi.read(buf), 10000 , "checking return value");
   		th.check(Arrays.equals(buf , buf2), "array should be equal");
  		th.check(gzi.read(buf), -1 , "checking return value");  		
   		th.check(inf.finished() , "finished (used extra bytes to fake adler32)");
   		th.check(inf.getRemaining() ,10, "10 bytes left");
  	}
  	catch(Exception e){
  		th.fail("got unwanted exception -- de/inflater streams -- 2");
  		e.printStackTrace();	
  	}
 	th.setclass("java.util.zip.GZIPInputStream");
  	th.checkPoint("GZIPInputStream(java.io.InputStream");
  	try {
  		ByteArrayOutputStream bas = new ByteArrayOutputStream();
  		GZIPOutputStream gzo = new GZIPOutputStream(bas);
   		byte [] buf = new byte[10000];
   		for (int i=0 ; i < 10000 ; i++) {
   	 		buf[i] = (byte)i;
   		}
	   	byte [] buf2 = (byte[])buf.clone();
  		gzo.write(buf,0, 10000);
  		gzo.finish();
  		GZIPInputStream gzi = new GZIPInputStream(new ByteArrayInputStream(bas.toByteArray()));
  		buf = new byte[10000];
		  th.check(gzi.read(buf), 10000 , "checking return value");
 	   th.setclass("java.util.zip.GZIPOutputStream");
  		th.checkPoint("GZIPOutputStream(java.io.OutputStream");
   		th.check(Arrays.equals(buf , buf2), "array should be equal");
  	}
  	catch(Exception e){
  		th.fail("got unwanted exception -- GZIPstreams");
  		e.printStackTrace();	
  	}
  }	

  public void quick_Deflater() {
  	th.checkPoint("Deflater(int,boolean)");
   	byte [] buf = new byte[10000];
   	for (int i=0 ; i < 10000 ; i++) {
   	 	buf[i] = (byte)i;
   	}
   	byte [] buf2 = (byte[])buf.clone();
   	Deflater def = new Deflater(8,false);
  	th.checkPoint("needsInput()boolean");
   	th.check(def.needsInput(), "a new Defalter needs input");
  	th.checkPoint("finished()boolean");
   	th.check(!def.finished() , "a new Deflater is not finished");
   	def.setInput(buf,0,10000);
   	buf = new byte[10000];
   	def.finish();   	
  	th.checkPoint("deflate(byte[],int,int)int");
   	th.check(def.deflate(buf,0,100), 100, "checking return value");
   	int rem = 100 + def.deflate(buf,100,9900);
  	th.checkPoint("DEFAULT_COMPRESSION(public)int");
   	th.check(Deflater.DEFAULT_COMPRESSION, -1, "DEFAULT_COMPRESSION");
  	th.checkPoint("BEST_COMPRESSION(public)int");
   	th.check(Deflater.BEST_COMPRESSION, 9, "BEST_COMPRESSION ");
  	th.checkPoint("BEST_SPEED(public)int");
   	th.check(Deflater.BEST_SPEED, 1, "BEST_SPEED");
  	th.checkPoint("NO_COMPRESSION(public)int");
   	th.check(Deflater.NO_COMPRESSION, 0, "NO_COMPRESSION ");
  	th.checkPoint("DEFLATED(public)int");
   	th.check(Deflater.DEFLATED, 8, "DEFLATED");
  	th.checkPoint("DEFAULT_STRATEGY(public)int");
   	th.check(Deflater.DEFAULT_STRATEGY, 0, "DEFAULT_STRATEGY");
  	th.checkPoint("FILTERED(public)int");
   	th.check(Deflater.FILTERED, 1, "FILTERED");
  	th.checkPoint("HUFFMAN_ONLY(public)int");
   	th.check(Deflater.HUFFMAN_ONLY, 2, "HUFFMAN_ONLY");
	
	
 	th.setclass("java.util.zip.Inflater");
	Inflater inf = new Inflater(false);
  	th.checkPoint("needsInput()boolean");
   	th.check(inf.needsInput(), "a new Defalter needs input");
  	th.checkPoint("finished()boolean");
   	th.check(!inf.finished() , "a new Deflater is not finished");
   	inf.setInput(buf,0,rem + 10);
   	buf = new byte[10000];
  	th.checkPoint("inflate(byte[],int,int)int");
   	try {
   		th.check(inf.inflate(buf,0,100), 100, "checking return value");
   		th.check(inf.inflate(buf,100,9900), 9900, "checking return value");
   		th.check(inf.getRemaining(), 10,"checking remaining bytes");
   	}
   	catch(DataFormatException dfe) {
   		th.fail("shouldn't throw a DataFormatException");
   		dfe.printStackTrace();	
   	}
   	th.check(Arrays.equals(buf , buf2), "array should be equal");
  }
  	
  public void quick_Adler(){
  	th.checkPoint("getValue()long");
   	Adler32 ad = new Adler32();
   	th.check(ad.getValue() , 1L , "basic value -- 1");
  	th.checkPoint("update(int)void");
   	ad.update(255);
   	th.check(ad.getValue() , 256L * 65536L + 256L , "adding one byte");
   	ad.update(25);
   	th.check(ad.getValue() , 537L * 65536L + 281L , "adding another byte");
  	th.checkPoint("reset()void");
   	ad.reset();
   	th.check(ad.getValue() , 1L , "basic value -- 2");
  	th.checkPoint("update(byte[])void");
   	try {
   		ad.update(null);
   		th.fail("should throw a NullPointerException -- 1");
   	}
   	catch (NullPointerException npe) { th.check(true , "caught NullPointerException -- 1"); }
  	th.checkPoint("update(byte[],int,int)void");
   	try {
   		ad.update(null, 1, 2);
   		th.fail("should throw a NullPointerException -- 2");
   	}
   	catch (NullPointerException npe) { th.check(true , "caught NullPointerException -- 2"); }
   	byte [] buf = new byte[5];
   	try {
   		ad.update(buf,-1,2);
   		th.fail("should throw a IndexOutOfBoundsException -- 3");
   	}
   	catch (ArrayIndexOutOfBoundsException npe) { th.check(true , "caught IndexOutOfBoundsException -- 3"); }
   	try {
   		ad.update(buf,1,-2);
   		th.fail("should throw a IndexOutOfBoundsException -- 4");
   	}
   	catch (ArrayIndexOutOfBoundsException npe) { th.check(true , "caught IndexOutOfBoundsException -- 4"); }
   	try {
   		ad.update(buf,4,2);
   		th.fail("should throw a IndexOutOfBoundsException -- 5");
   	}
   	catch (ArrayIndexOutOfBoundsException npe) { th.check(true , "caught IndexOutOfBoundsException -- 5"); }
   	buf = new byte[1000];
   	for (int i=0 ; i < 1000 ; i++) {
   	 	buf[i] = (byte)i;
   	}
  	th.checkPoint("update(byte[])void");
   	ad.update(buf);
   	th.check(ad.getValue() , 486795068L , "complex value -- 1");
   	buf = new byte[3000];
   	for (int i=0 ; i < 3000 ; i++) {
   	 	buf[i] = (byte)255;
   	}
   	ad.update(buf);
   	th.check(ad.getValue() , 3513947192L , "complex value -- 2");
   	buf = new byte[3000];
   	for (int i=0 ; i < 3000 ; i++) {
   	 	buf[i] = (byte)127;
   	}
   	ad.update(buf);
   	th.check(ad.getValue() , 1959224538L , "complex value -- 3");
   	ad.reset();
   	th.check(ad.getValue() , 1L , "basic value -- 4");
  	th.checkPoint("update(byte[],int,int)void");
   	ad.update(buf,5,0);
   	th.check(ad.getValue() , 1L , "basic value -- 5");
   	for (int i=0 ; i < 1000 ; i++) {
   	 	buf[i] = (byte)i;
   	}
   	ad.update(buf,0,500);
   	ad.update(buf,500,500);
   	th.check(ad.getValue() , 486795068L , "complex value -- 1 bis");
  }

  public void quick_CRC(){
  	th.checkPoint("getValue()long");
   	CRC32 ad = new CRC32();
   	th.check(ad.getValue() , 0L , "basic value -- 1");
  	th.checkPoint("update(int)void");
   	ad.update(255);
   	th.check(ad.getValue() , 4278190080L , "adding one byte");
   	ad.update(25);
   	th.check(ad.getValue() , 3063301965L , "adding another byte");
  	th.checkPoint("reset()void");
   	ad.reset();
   	th.check(ad.getValue() , 0L , "basic value -- 2");
  	th.checkPoint("update(byte[])void");
   	try {
   		ad.update(null);
   		th.fail("should throw a NullPointerException -- 1");
   	}
   	catch (NullPointerException npe) { th.check(true , "caught NullPointerException -- 1"); }
  	th.checkPoint("update(byte[],int,int)void");
   	try {
   		ad.update(null, 1, 2);
   		th.fail("should throw a NullPointerException -- 2");
   	}
   	catch (NullPointerException npe) { th.check(true , "caught NullPointerException -- 2"); }
   	byte [] buf = new byte[5];
   	try {
   		ad.update(buf,-1,2);
   		th.fail("should throw a IndexOutOfBoundsException -- 3");
   	}
   	catch (ArrayIndexOutOfBoundsException npe) { th.check(true , "caught IndexOutOfBoundsException -- 3"); }
   	try {
   		ad.update(buf,1,-2);
   		th.fail("should throw a IndexOutOfBoundsException -- 4");
   	}
   	catch (ArrayIndexOutOfBoundsException npe) { th.check(true , "caught IndexOutOfBoundsException -- 4"); }
   	try {
   		ad.update(buf,4,2);
   		th.fail("should throw a IndexOutOfBoundsException -- 5");
   	}
   	catch (ArrayIndexOutOfBoundsException npe) { th.check(true , "caught IndexOutOfBoundsException -- 5"); }
   	buf = new byte[1000];
   	for (int i=0 ; i < 1000 ; i++) {
   	 	buf[i] = (byte)i;
   	}
  	th.checkPoint("update(byte[])void");
   	ad.update(buf);
   	th.check(ad.getValue() , 1961098049L , "complex value -- 1");
   	buf = new byte[3000];
   	for (int i=0 ; i < 3000 ; i++) {
   	 	buf[i] = (byte)255;
   	}
   	ad.update(buf);
   	th.check(ad.getValue() , 319854888L , "complex value -- 2");
   	buf = new byte[3000];
   	for (int i=0 ; i < 3000 ; i++) {
   	 	buf[i] = (byte)127;
   	}
   	ad.update(buf);
   	th.check(ad.getValue() , 1717866834L , "complex value -- 3");
   	ad.reset();
   	th.check(ad.getValue() , 0L , "basic value -- 3");
  	th.checkPoint("update(byte[],int,int)void");
   	th.check(ad.getValue() , 0L , "basic value -- 4");
   	ad.update(buf,5,0);
   	th.check(ad.getValue() , 0L , "basic value -- 5");
   	for (int i=0 ; i < 1000 ; i++) {
   	 	buf[i] = (byte)i;
   	}
   	ad.update(buf,0,500);
   	ad.update(buf,500,500);
   	th.check(ad.getValue() , 1961098049L , "complex value -- 1 bis");
  }

  public void printStream(byte [] s, int len ,String message) {
    System.out.println(message+"--> Length of array: "+len);
    for (int i=0; i<len;i++) {
      System.out.print(Byte.toString(s[i])+", ");
      if ((i+1)%16==0) {
       System.out.println();
      }
    }
    System.out.println("\n");


  }
}