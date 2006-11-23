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

package gnu.testlet.wonka.io.File; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ..
import java.net.URL; // URL definitions for toURL ..
import java.net.MalformedURLException; // URL definitions for toURL ..


/****************************************************************************************************************************************/
/**
* This file handles part of the testing of the File class, notably the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* this tests are done for a variety of constructors for a number of cases:
*   => a file in the current directory
*   => a file in an imaginary directory higher then the current one
*   => a file in the base directory of the current one ( ../)
*   => a file in an imaginary directory parrallel to the current one (../paralleldir )
*   => a file in an imaginary directory constructed from the root
*
* also tested are the basic inherited from Object : equals(), compareTo(), hashCode() and toString().
* toUrl() is tested in a different file, as are all exist- Create- delete- and mkdir options and all filelength and file properties
*/

public class FileTest implements Testlet
{
  protected static TestHarness harness;

/****************************************************************************************************************************************/
/**
* Simply test if File.separator is the String representation of File.separatorChar
* and if File.pathSeparator is the String representation of File.pathSeparatorChar
*/
  private void testSeparators()
	{
    harness.checkPoint("Testing file separators");
		harness.verbose("File separator:"+ File.separator);
		harness.check(File.separator, new String(""+File.separatorChar) );

		harness.verbose("File path separator:"+ File.pathSeparator);
		harness.check(File.pathSeparator, new String(""+File.pathSeparatorChar) );
  }

/****************************************************************************************************************************************/
/**
* Simply test if new File(null) casts a NullPointerException, like its definition states
*/
  private void testNullConstructors()
	{
    String testfilestring = "testfile.scr";

    /*==== null constructors: File(null) =>exception  ====*/
    harness.checkPoint("null constructors: File(null) =>exception");
    try
    {
      File testfile1=new File(null);
      harness.fail(" constructor < File(null) > should throw NullPointerException");
    }
    catch(NullPointerException e)
    {
     harness.check(true," constructor < File(null) > threw NullPointer as defined : "+e.toString());
    }
    catch(Exception e)
    {
      harness.fail(" constructor < File(null) > should throw NullPointerException, threw : "+e.toString());
    }


    /*====  null constructors: File(string null, filestring) =>allowed ====*/
    harness.checkPoint("null constructors: File(string null, filestring) =>allowed");
    try
    {
      String nullstring = null;
      harness.check(new File(nullstring, testfilestring), new File(testfilestring), "File(string null, string) definition");
    }
    catch(Exception e)
    {
      harness.fail(" constructor < File(string null, string) > should be allowed, threw : "+e.toString());
    }


    /*==== null constructors: File(pathstring, null) =>exception  ====*/
    harness.checkPoint("null constructors: File(pathstring, null) =>exception");
    try
    {
      File testfile1=new File("", null);
      harness.fail(" constructor < File(pathstring, null) > should throw NullPointerException");
    }
    catch(NullPointerException e)
    {
     harness.check(true," constructor < File(pathstring, null) > threw NullPointer as defined : "+e.toString());
    }
    catch(Exception e)
    {
      harness.fail(" constructor < File(pathstring, null) > should throw NullPointerException, threw : "+e.toString());
    }


    /*==== null constructors: File(file null, filestring) =>allowed  ====*/
    harness.checkPoint("null constructors: File(file null, filestring) =>allowed");
    try
    {
      File nullfile = null;
      harness.check(new File(nullfile, testfilestring), new File(testfilestring), "File(File, string) definition");
    }
    catch(Exception e)
    {
      harness.fail(" constructor < File(file null, string) > should be allowed, threw : "+e.toString());
    }

    /*==== null constructors: File(pathfile, null) =>exception  ====*/
    harness.checkPoint("null constructors: File(pathfile, null) =>exception");
    try
    {
      File newfile = new File("");
      harness.check(true,"constructor < File(emptystring) > is allowed");
      File testfile1=new File(newfile, null);
      harness.fail(" constructor < File(pathfile, null) > should throw NullPointerException");
    }
    catch(NullPointerException e)
    {
     harness.check(true," constructor < File(pathfile, null) > threw NullPointer as defined : "+e.toString());
    }
    catch(Exception e)
    {
      harness.fail(" constructor < File(pathfile, null) > should throw NullPointerException, threw : "+e.toString());
    }

/**
NOTE: there seems to be some controversy on what to do with File("",filestring) and File(new File(""), filestring)
By algorithm, File(dirstring, filestring) and File(dirfile, filestring) are treated  by the Sun  SDK
as File('dirstring+separator+filestring') so that File("","title.txt") and File(new File(""),"title.txt")
should be resolved as File("/title.txt"), which is a file in the root directory rather then in the current one.


Then again, File(null String, filestring) and File(null File, filestring) are treated as File(filestring)
There are however NO spceifications on how the File constructors should be resolved, so it is open to interpretion
as to File("","title.txt") and File(new File(""),"title.txt") should refer to the root or to the current dir.

Example:
      File currentdir = new File("");                          //Example /home/filetests (Note, we're uuusing unix/Linux here....)
      String currentdirstring = currentdir.getCanonicalPath();
      File canonicalcurrentdir = currentdir.getCanonicalFile();
      String testfilestring = "testfile.txt";

      File testfile1 = new File("testfile.tmp");
      System.out.println("<"+testfile1.getPath()+">" );           // => <testfile.tmp>
      System.out.println("<"+testfile1.getCanonicalPath()+">" );  // => </home/filetests/testfile.tmp>

      File testfile2 = new File("");
      System.out.println("<"+testfile2.getPath()+">" );           // => <> (an empty String)
      System.out.println("<"+testfile2.getCanonicalPath()+">" );  // => </home/filetests>

      File testfile3 = new File("","testfile.tmp");
      // THe current code translates this line as <testfile3 = new File(""+File.separator+"testfile.tmp");>
      // being <testfile3 = new File("/testfile.tmp");>
      System.out.println("<"+testfile3.getPath()+">" );           // => </testfile.tmp> (a file in the root directory)
      System.out.println("<"+testfile3.getCanonicalPath()+">" );  // => </testfile.tmp> (a file in the root directory)

      To make it even more complex:
      File testfile4 = new File(testfile2,"testfile.tmp");
      // while testfile2 represents the current directory, testfile2.getPath() is an empty String, which again makes that Java...
      //......translates this line as <testfile4 = new File(""+File.separator+"testfile.tmp");>
      // being <testfile4 = new File("/testfile.tmp");>
      System.out.println("<"+testfile4.getPath()+">" );           // => </testfile.tmp> (a file in the root directory)
      System.out.println("<"+testfile4.getCanonicalPath()+">" );  // => </testfile.tmp> (a file in the root directory)

      while:
      File testfile5 = new File(testfile2.getCanonicalFile,"testfile.tmp");
      //....equals <testfile5 = new File("/home/filetests/testfile.tmp");> (Unix/linux)
      // and <testfile4 = new file(c:\filetests\testfile.tmp);>(Microsoft)
      System.out.println("<"+testfile5.getPath()+">" );           // => </home/filetests/testfile.tmp> (the current dir again)
      System.out.println("<"+testfile5.getCanonicalPath()+">" );  // => </home/filetests/testfile.tmp> (the current dir again)


      (For Microsoft, the output would become:
      Testfile3 = new File (\testfile.tmp);
      testfile4 = new file(\testfile.tmp);
      testfile5 = new file(c:\filetests\testfile.tmp);
      Although I'm not sure what this would do in a Microsoft system, I'm pretty sure that cases 3 and 4
      won't represent a file in the current directory either)

In this series of tests, we will test each of the condition by comparing the files to the root AND the current dir,
so at least two of the next four tests will ALLWAYS fail and then wisely leave this topic to the interpretations of the
virtual machine to test
**/
    try
    {
      harness.checkPoint("File('',string) = File(string) or file('/string') : One of the next tests WILL fail");
      harness.check(new File("",testfilestring), new File(testfilestring)  );
      harness.check(new File("",testfilestring), new File(File.separator+testfilestring)  );

      harness.checkPoint("File(File(''),string) = File(string) or file('/string') : One of the next tests WILL fail");
      harness.check(new File(new File(""),testfilestring), new File(testfilestring) );
      harness.check(new File(new File(""),testfilestring), new File(File.separator+testfilestring) );
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }

  }

/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Tests a number of functions derived from Object, such as equals, compareTo, hashcode and toString()
*/
  private void testFileEquals()
  {
    try
    {
      File currentdir = new File("");
      String currentdirstring = currentdir.getCanonicalPath();
      File canonicalcurrentdir = currentdir.getCanonicalFile();

      String testfilestring = "testfile.scr";
      String testfilepath = currentdirstring + File.separator + testfilestring;

      File testfile1=new File(testfilestring);
      File testfile2=new File(testfilestring);

      File relative1=new File((String)null,testfilestring);
      File relative2=new File((File)null,testfilestring);
      //File relative3=new File("",testfilestring);
      //File relative4=new File(currentdir,testfilestring);

      File absolute0=new File(currentdirstring+File.separator+testfilestring);
      File absolute1=new File(currentdirstring, testfilestring);
      File absolute2=new File(canonicalcurrentdir, testfilestring);

      //represent the same physical files (same canonical representation)
      harness.checkPoint("<getCanonicalFile()> full dir file target");
      File canonical = testfile1.getCanonicalFile();
      harness.check(testfile1.getCanonicalFile(),canonical,"value invariant to self");
      harness.check(testfile2.getCanonicalFile(),canonical,"same constructor");
      harness.check(relative1.getCanonicalFile(),canonical,"relative1: same target");
      harness.check(relative2.getCanonicalFile(),canonical,"relative2: same target");
      harness.check(absolute0.getCanonicalFile(),canonical,"absolute0: same target");
      harness.check(absolute1.getCanonicalFile(),canonical,"absolute1: same target");
      harness.check(absolute2.getCanonicalFile(),canonical,"absolute2: same target");


      // File.equals(file)
      harness.checkPoint("object.equals()");
      harness.check(testfile1,testfile1, "reflection, equals to self");
      harness.check(testfile1,testfile2, "same constructor");
      harness.check(testfile2,testfile1, "symmetricity");
      harness.check(testfile1,relative1, "implicit equivalent constructor");
      harness.check(testfile1,relative2, "implicit equivalent constructor");
      harness.check(relative1,relative2, "transitivity");

      harness.check(absolute0, absolute1,"implicit equivalent constructor");
      harness.check(absolute1, absolute2,"implicit equivalent constructor");
      harness.check(absolute0, absolute2,"(symmetric)transitivity");

      harness.check(!testfile1.equals(absolute0),"same target, different constructor => different object");
      harness.check(!testfile2.equals(absolute0),"transitivity");
      harness.check(!absolute0.equals(testfile1),"symmetricity");
      harness.check(!relative1.equals(absolute1),"same target, different constructor => different object");
      harness.check(!relative2.equals(absolute2),"same target, different constructor => different object");


      // File.getPath() as base
      harness.checkPoint("getPath equals definitions");
      harness.check(testfile1.getPath(), testfile1.getPath(),"value invariant to self");
      harness.check(testfile1.getPath(), testfile2.getPath(),"same constructor");
      harness.check(testfile1.getPath(), relative1.getPath(),"implicit equivalent constructor");
      harness.check(testfile1.getPath(), relative2.getPath(),"implicit equivalent constructor");

      harness.check(absolute0.getPath(), absolute1.getPath(),"same constructor");
      harness.check(absolute0.getPath(), absolute2.getPath(),"same constructor");

      String path = testfile1.getPath();
      harness.check(!path.equals(absolute0.getPath()), "different constructor");
      harness.check(!path.equals(absolute1.getPath()), "different constructor");
      harness.check(!path.equals(absolute2.getPath()), "different constructor");


      // File.toString() as equal to GetPath()
      harness.checkPoint("toString definitions");
      harness.check(testfile1.toString(), testfile1.toString(),"value invariant to self");
      harness.check(testfile1.toString(), testfile2.toString(),"same constructor");
      harness.check(testfile1.toString(), relative1.toString(),"implicit equivalent constructor");
      harness.check(testfile1.toString(), relative2.toString(),"implicit equivalent constructor");

      harness.check(absolute0.toString(), absolute1.toString(),"same constructor");
      harness.check(absolute0.toString(), absolute2.toString(),"same constructor");

      String text = testfile1.toString();
      harness.check(!text.equals(absolute0.toString()), "different constructor");
      harness.check(!text.equals(absolute1.toString()), "different constructor");
      harness.check(!text.equals(absolute2.toString()), "different constructor");


      // File.hashcode() as derived from GetPath()
      harness.checkPoint("hashcode equals");
      harness.check(testfile1.hashCode(),testfile1.hashCode(),"reflexive to self");
      harness.check(testfile1.hashCode(),testfile2.hashCode(),"same constructor");

      harness.check(testfile1.hashCode(),relative1.hashCode(),"equivalent constructor");
      harness.check(testfile1.hashCode(),relative2.hashCode(),"equivalent constructor");


      harness.check(absolute0.hashCode(), absolute1.hashCode(),"equivalent constructor");
      harness.check(absolute0.hashCode(), absolute2.hashCode(),"equivalent constructor");

      harness.checkPoint("hashcode definition");
      int hashrelative =(testfilestring.hashCode() )^1234321; // hashCode definition = hashcode(file.getPath() )xor(1234321)
      int hashabsolute =(testfilepath.hashCode() )^1234321; // hashCode definition

      harness.check(testfile1.hashCode(),hashrelative,"hashCode definition");
      harness.check(testfile2.hashCode(),hashrelative,"hashCode definition");
      harness.check(relative1.hashCode(),((relative1.toString()).hashCode())^1234321,"hashCode definition");
      harness.check(relative2.hashCode(),((relative1.toString()).hashCode())^1234321,"hashCode definition");

      harness.check(absolute0.hashCode(),hashabsolute,"hashCode definition");
      harness.check(absolute1.hashCode(),hashabsolute,"hashCode definition");
      harness.check(absolute2.hashCode(),hashabsolute,"hashCode definition");

    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }

    //compareTo(different class) throws error
    try
    {
      String testfilestring = "testfile.scr";
      File testfile1=new File(testfilestring);

      testfile1.compareTo(testfilestring);
      harness.fail("compareTo different classes should throw ClassCastException");
    }
    catch(ClassCastException e)
    {
     harness.check(true,"compareTo different classes throws ClassCastException : "+e.toString());
    }
    catch(Exception e)
    {
      harness.fail("compareTo different classes should throw ClassCastException, threw : "+e.toString());
    }
  }


/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for the current directory and for files in that current directory
*/
  private void testFileConstructorsCurrentDir(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("file constructors on current directory");
   try
   {
      File currentdir = new File("");
      String canonicalrootpath=currentdir.getCanonicalPath();      //  => /home/wonka
      int pos = canonicalrootpath.lastIndexOf('/');
      String superpath = canonicalrootpath.substring(0,pos);       //  => /home
      String currentdirname = canonicalrootpath.substring(pos+1);  //  => wonka
        harness.verbose("current canonicalpath = "+canonicalrootpath);
        harness.verbose("superpath = "+superpath);
        harness.verbose("currentdirname = "+currentdirname);

      File testfile;


      //current directory:
      //testfile = new File(""); //== currentdir
      harness.checkPoint("Testing current dir File('')");
/**
NOTE: file resolvement discards the separator at the end of a directory, so there is some controversy
as to File("/") is equal to File(""): the current dir or should apply to directory '/' : the root
Untill this is resolved, all corresponding tests are left out
**/
/*
      testfile = new File(File.separator);
      harness.checkPoint("Testing current dir File('/')");
//  => regards File("/") as File("") : current dir
      testFileProperties(testfile, "", "", null, canonicalrootpath, canonicalrootpath, do_tests, verbose);
      testFileProperties(!testfile.isAbsolute(),"relative current dir isAbsolute()")
//  => regards File("/") as Root instead of current dir
      testFileProperties(testfile, "", "", null, "", "", do_tests, verbose);
      testFileProperties(testfile.isAbsolute(),"root dir isAbsolute()")
*/
//      testfile = new File('home/wonka');
      testfile = new File(canonicalrootpath);
      harness.checkPoint("Testing File( full currentdir.getCanonicalPath() <"+canonicalrootpath+"> = )");
      testFileProperties(testfile, canonicalrootpath, currentdirname, superpath, canonicalrootpath, canonicalrootpath,
                                                                                                              do_tests, verbose);
//      testfile = new File('home/wonka/');
      testfile = new File(canonicalrootpath+File.separator);
      harness.checkPoint("Testing File(full currentdir path + file separator ending<"+canonicalrootpath+File.separator+">)");
      testFileProperties(testfile, canonicalrootpath, currentdirname, superpath, canonicalrootpath, canonicalrootpath,
                                                                                                              do_tests, verbose);
      //testFileProperties(testfile, currentcanonicalroot, currentdirname, superpath, canonicalrootpath, canonicalrootpath
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }




/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for a file in the current directory , both in relative as in full path form
*/
  private void testFileConstructorsCurrentFile(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("files in current directory");
   //harness.fail("files in current directory : do_tests: "+do_tests+" , verbose: "+verbose);
   try
   {
      String testfilestring = "testfile.scr";                                   //  => testfile.scr
      File currentdir = new File("");
      String canonicalrootpath=currentdir.getCanonicalPath();                   //  => /home/wonka
      String testfilepath = canonicalrootpath + File.separator+testfilestring;  //  => /home/wonka/testfile.scr
        harness.verbose("canonicalrootpath = "+canonicalrootpath);

      File testfile;


      //  relative file path
      // ===================
      // new File('testfile.scr');
      harness.checkPoint("new File("+testfilestring+");");
      testfile = new File(testfilestring);
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);

      // new File(string null,'testfile.scr');
      harness.checkPoint("new File(String null, "+testfilestring+");");
      String nullstring = null;
      testfile = new File(nullstring, testfilestring);
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);

      // new File(file null, 'testfile.scr');
      harness.checkPoint("new File(File null, "+testfilestring+");");
      File nullfile = null;
      testfile = new File(nullfile, testfilestring);
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);

/**
NOTE: there seems to be some controversy on what to do with File("",filestring) and File(new File(""), filestring)
It is open to interpretion wether File("","title.txt") and File(new File(""),"title.txt") should refer to
File("/title.txt") : the root or to File("title.txt") : the current dir.
Therefore, all tests referring to File("","title.txt") and File(new File(""),"title.txt") are edited out
and can be filled in again according to the specific algorithm of the VM

As file resolvement discards the separator at the end of a directory, the same applies to
File("/","title.txt") and File(new File("/"),"title.txt"), so they are equally discarted

**/
/*
      // new File("", 'testfile.scr');
      harness.checkPoint("new File('', "+testfilestring+");");
      testfile = new File("",testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                     do_tests, verbose);

      // new File(File(""), 'testfile.scr');
      harness.checkPoint("new File(<currentdir=new File('')>, "+testfilestring+");");
      testfile = new File(currentdir,testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                      do_tests, verbose);

      // new File("/", 'testfile.scr');
      harness.checkPoint("new File('', "+testfilestring+");");
      testfile = new File("",testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                      do_tests, verbose);

      // new File(File('/'), 'testfile.scr')
      harness.checkPoint("new File('/', "+testfilestring+");");
      testfile = new File(new File("/"),testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, "/"+testfilestring, testfilestring, "/", testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                      do_tests, verbose);
*/



      //  absolute file path
      // ===================
      // new File('home/wonka/testfile.scr');
      harness.checkPoint("new File("+testfilepath+");");
      testfile = new File(testfilepath);
      testFileProperties(testfile, testfilepath, testfilestring, canonicalrootpath, testfilepath, testfilepath, do_tests, verbose);

      // new File('home/wonka', 'testfile.scr');
      harness.checkPoint("new File("+canonicalrootpath+", "+testfilestring+");");
      testfile = new File(canonicalrootpath, testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, canonicalrootpath, testfilepath, testfilepath, do_tests, verbose);

      // new File('home/wonka/', 'testfile.scr');
      harness.checkPoint("new File("+canonicalrootpath + File.separator+", "+testfilestring+");");
      testfile = new File(canonicalrootpath + File.separator, testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, canonicalrootpath, testfilepath, testfilepath, do_tests, verbose);

      // new File(File('home/wonka', 'testfile.scr');
      harness.checkPoint("current dir canonical form, "+testfilestring+");");
      testfile = new File(new File(canonicalrootpath), testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, canonicalrootpath, testfilepath, testfilepath, do_tests, verbose);

    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }



/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for a file in a subdirectory of the current directory , both in relative as in full path form
*/
  private void testFileConstructorsSubdirFile(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("file constructors on current directory");
   try
   {
      String subdirstring = "subdir";                                             //  => subdir
      String testfilestring = "testfile.scr";                                     //  => testfile.scr
      String testfilepath = subdirstring + File.separator+testfilestring;         //  => subdir/testfile.scr
      File subdirfile = new File(subdirstring);

      File currentdir = new File("");
      String canonicalrootpath = currentdir.getCanonicalPath();                   //  => /home/wonka
      String canonicaldirpath =  canonicalrootpath+ File.separator+subdirstring;  //  => /home/wonka/subdir
      String canonicalfilepath =  canonicaldirpath+ File.separator+testfilestring;//  => /home/wonka/subdir/testfile.scr
        harness.verbose("canonicalrootpath = "+canonicalrootpath);
        harness.verbose("canonicaldirpath = "+canonicaldirpath);
        harness.verbose("canonicalfilepath = "+canonicalfilepath);

      File testfile;

      //  relative file path
      // ===================
      // new File(File('subdir/testfile.scr');
      harness.checkPoint("new File("+testfilepath+");");
      testfile = new File(testfilepath);
      testFileProperties(testfile, testfilepath, testfilestring, subdirstring, canonicalfilepath, canonicalfilepath, do_tests, verbose);

      // new File(File('subdir', 'testfile.scr');
      harness.checkPoint("new File("+subdirstring+", "+testfilestring+");");
      testfile = new File(subdirstring, testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, subdirstring, canonicalfilepath, canonicalfilepath, do_tests, verbose);

      // new File(File(File('subdir'), 'testfile.scr');
      harness.checkPoint("new File( File<"+subdirstring+">, "+testfilestring+");");
      testfile = new File(new File(subdirstring), testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, subdirstring, canonicalfilepath, canonicalfilepath, do_tests, verbose);


      //  absolute file path
      // ===================
      // new File('home/wonka/subdir/testfile.scr');
        // equivalent to File('home/wonka/testfile.scr') tested above

      // new File('home/wonka/subdir', 'testfile.scr');
      harness.checkPoint("new File("+canonicalrootpath+", "+testfilepath+");");
      testfile = new File(canonicalrootpath, testfilepath);
      testFileProperties(testfile, canonicalfilepath, testfilestring, canonicaldirpath, canonicalfilepath, canonicalfilepath,
                                                                                                                  do_tests, verbose);
      // new File('home/wonka', 'subdir/testfile.scr');
      harness.checkPoint("new File("+canonicaldirpath+", "+testfilestring+");");
      testfile = new File(canonicaldirpath,testfilestring );
      testFileProperties(testfile, canonicalfilepath, testfilestring, canonicaldirpath, canonicalfilepath, canonicalfilepath,
                                                                                                                  do_tests, verbose);
      // new File(File('home/wonka/subdir'), 'testfile.scr');
      harness.checkPoint("new File( File<"+canonicalrootpath+">, "+testfilepath+");");
      testfile = new File(new File(canonicalrootpath), testfilepath);
      testFileProperties(testfile, canonicalfilepath, testfilestring, canonicaldirpath, canonicalfilepath, canonicalfilepath,
                                                                                                                  do_tests, verbose);
      // new File(File('home/wonka'), 'subdir/testfile.scr');
      harness.checkPoint("new File( File<"+canonicaldirpath+">, "+testfilestring+");");
      testfile = new File(new File(canonicaldirpath),testfilestring );
      testFileProperties(testfile, canonicalfilepath, testfilestring, canonicaldirpath, canonicalfilepath, canonicalfilepath,
                                                                                                                  do_tests, verbose);

    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }

/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for the super directory of the current directory  using <../> and <subdir/..>
*/
  private void testFileConstructorsSuperDir(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("file constructors on super(../) directory");
   try
   {

      String superstring = "..";                                           //  => ..

      File currentdir = new File("");
      String subdirstring  = "subdir";                                            //  => subdir
      String canonicalrootpath = currentdir.getCanonicalPath();                   //  => /home/wonka
      String canonicaldirpath =  canonicalrootpath+ File.separator +subdirstring;  //  => /home/wonka/subdir
        harness.verbose("canonicalrootpath = "+canonicalrootpath);
        harness.verbose("canonicaldirpath = "+canonicaldirpath);

      int pos = canonicalrootpath.lastIndexOf('/');
      String superpath = canonicalrootpath.substring(0,pos);                  //  => /home
      String currentdirname = canonicalrootpath.substring(pos+1);             //  => wonka
        harness.verbose("superpath = "+superpath);
        harness.verbose("currentdirname = "+currentdirname);

      File testfile;
      String target;

      // super directory, relative path
      // ==============================
      //constructed absolute path = current absolute path + attached string
      target = canonicalrootpath+ File.separator + superstring;
      //testfile = new File("..");
      harness.checkPoint("new File("+superstring+");");
      testfile = new File(superstring);
      testFileProperties(testfile, superstring, superstring, null, target, superpath, do_tests, verbose);
      //testfile = new File("../");
      harness.checkPoint("new File("+superstring + File.separator+");");
      testfile = new File(superstring + File.separator);
      testFileProperties(testfile, superstring, superstring, null, target, superpath, do_tests, verbose);

      // super directory, absolute path
      // ==============================
      target =canonicaldirpath+ File.separator + superstring; //  => /home/wonka/subdir/..
      //testfile = new File("/home/wonka/subdir/..");
      harness.checkPoint("new File("+target+");");
      testfile = new File(target);
      testFileProperties(testfile, target, superstring, canonicaldirpath, target, canonicalrootpath, do_tests, verbose);
      //testfile = new File("/home/wonka/subdir", "..");
      harness.checkPoint("new File("+canonicalrootpath+", "+superstring+");");
      testfile = new File(canonicaldirpath, superstring);
      testFileProperties(testfile, target, superstring, canonicaldirpath, target, canonicalrootpath, do_tests, verbose);
      //testfile = new File(<new File("/home/wonka/subdir") >, "..");
      harness.checkPoint("new File( <new file("+canonicaldirpath+")>"+superstring+");");
      testfile = new File(new File(canonicaldirpath),superstring);
      testFileProperties(testfile, target, superstring, canonicaldirpath, target, canonicalrootpath, do_tests, verbose);
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }



/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for files in the super directory of the current directory using <../filename> and <subdir/../filename>
*/
  private void testFileConstructorsSuperdirFile(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("file constructors on files in super(../) directory");
   try
   {

      String superstring = "..";                                           //  => ..
      String testfilestring = "testfile.scr";                              //  => testfile.scr
      String testfilepath = superstring + File.separator+testfilestring;   //  => ../testfile.scr

      File currentdir = new File("");
      String subdirstring  = "subdir";                                            //  => subdir
      String canonicalrootpath = currentdir.getCanonicalPath();                   //  => /home/wonka
      String canonicaldirpath =  canonicalrootpath+ File.separator +subdirstring;  //  => /home/wonka/subdir
      String canonicalfilepath = canonicalrootpath+ File.separator +testfilestring;//  => /home/wonka/testfile.scr
        harness.verbose("canonicalrootpath = "+canonicalrootpath);
        harness.verbose("canonicaldirpath = "+canonicaldirpath);
        harness.verbose("canonicalfilepath = "+canonicalfilepath);

      int pos = canonicalrootpath.lastIndexOf('/');
      String superpath = canonicalrootpath.substring(0,pos);                  //  => /home
      String currentdirname = canonicalrootpath.substring(pos+1);             //  => wonka
      String superfilename = superpath+File.separator+testfilestring;   //  => wonka/testfile.scr
        harness.verbose("superpath = "+superpath);
        harness.verbose("currentdirname = "+currentdirname);
        harness.verbose("superfilename = "+superfilename);

      File testfile;
      String target;
      String parent;
      String absolute;

      // file in super directory, relative path
      // ======================================
      absolute = canonicalrootpath+ File.separator + testfilepath;
      //testfile = new File("../file1.scr");
      harness.checkPoint("new File("+testfilepath+");");
      testfile = new File(testfilepath);
      testFileProperties(testfile, testfilepath, testfilestring, superstring, absolute, superfilename, do_tests, verbose);
      //testfile = new File("..","file1.scr");
      harness.checkPoint("new File("+superstring+", "+testfilestring+");");
      testfile = new File(superstring,testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, superstring, absolute, superfilename, do_tests, verbose);
      //testfile = new File(new File(".."),"file1.scr");
      harness.checkPoint("new File(<new File("+superstring+") >, "+testfilestring+");");
      testfile = new File(new File(superstring),testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, superstring, absolute, superfilename, do_tests, verbose);

      // file accessed from sub directory , relative path
      // ================================================
      target = subdirstring + File.separator + testfilepath;            //  => subdir/../testfile.scr
      parent = subdirstring + File.separator + superstring;            //  => subdir/..
      absolute = canonicaldirpath + File.separator + testfilepath; //  => home/wonka/subdir/../testfile.scr
      //testfile = new File("subdir/../file1.scr");
      harness.checkPoint("new File("+target+");");
      testfile = new File(target);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
       //testfile = new File("subdir/..","file1.scr");
      harness.checkPoint("new File("+parent+", "+testfilestring+");");
      testfile = new File(subdirstring,testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("subdir/.."),"file1.scr");
      harness.checkPoint("new File( <new File("+parent+")> , "+testfilepath+");");
      testfile = new File(new File(subdirstring),testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
       //testfile = new File("subdir","../file1.scr");
      harness.checkPoint("new File("+subdirstring+", "+testfilepath+");");
      testfile = new File(subdirstring,testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("subdir"),"../file1.scr");
      harness.checkPoint("new File( <new File("+subdirstring+")> , "+testfilepath+");");
      testfile = new File(new File(subdirstring),testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);

      // file in super directory, absolute path
      // ======================================
      target =canonicaldirpath+ File.separator + testfilepath; //  => /home/wonka/subdir/../testfile.scr
      parent = canonicaldirpath+ File.separator + superstring; //  => /home/wonka/subdir/..
      //testfile = new File("/home/wonka/subdir/../file1.scr");
      harness.checkPoint("new File("+target+");");
      testfile = new File(target);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File("/home/wonka/subdir/..","file1.scr");
      harness.checkPoint("new File("+parent+", "+testfilestring+");");
      testfile = new File(parent,testfilestring);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("/home/wonka/subdir/.."),"file1.scr");
      harness.checkPoint("new File(<new File("+parent+") >, "+testfilestring+");");
      testfile = new File(new File(parent),testfilestring);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File("/home/wonka/subdir","../file1.scr");
      harness.checkPoint("new File("+canonicaldirpath+", "+testfilepath+");");
      testfile = new File(canonicaldirpath,testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("/home/wonka/subdir"),"../file1.scr");
      harness.checkPoint("new File(<new File("+canonicaldirpath+") >, "+testfilepath+");");
      testfile = new File(new File(canonicaldirpath),testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }




/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for the super directory of the current directory  using <../> and <subdir/..>
*/
  private void testFileConstructorsParalleldirFile(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("file constructors on paraallel(../parallel) directory");
   try
   {

      String superstring = "..";                                              //  => ..
      String parallelstring = "parallel";                                     //  => parallel
      String parallelpath = superstring + File.separator + parallelstring;    //  => ../parallel
      String testfilestring = "testfile.scr";                                 //  => testfile.scr
      String testfilepath = parallelpath + File.separator + testfilestring; //  => ../parallel/testfile.scr
        harness.verbose("parallelpath = "+parallelpath);
        harness.verbose("testfilepath = "+testfilepath);

      File currentdir = new File("");
      String canonicalrootpath = currentdir.getCanonicalPath();                     //  => /home/wonka
      int pos = canonicalrootpath.lastIndexOf('/');
      String superpath = canonicalrootpath.substring(0,pos);                        //  => /home
      String canonicaldirpath =  superpath+ File.separator +parallelstring;         //  => /home/parallel
      String canonicalfilepath = canonicaldirpath+ File.separator +testfilestring;  //  => /home/parallel/testfile.scr
        harness.verbose("canonicalrootpath = "+canonicalrootpath);
        harness.verbose("canonicaldirpath = "+canonicaldirpath);
        harness.verbose("canonicalfilepath = "+canonicalfilepath);

      File testfile;
      String target;
      String parent;
      String absolute;

      // parallel directory, relative path
      // =================================
      absolute = canonicalrootpath + File.separator + parallelpath; //  => /home/wonka/../parallel
      //testfile = new File("../parallel");
      harness.checkPoint("new File("+parallelpath+");");
      testfile = new File(parallelpath);
      testFileProperties(testfile,parallelpath , parallelstring, superstring, absolute, canonicaldirpath, do_tests, verbose);
      //testfile = new File("../parallel/");
      harness.checkPoint("new File("+parallelpath + File.separator +");");
      testfile = new File(parallelpath + File.separator);
      testFileProperties(testfile,parallelpath , parallelstring, superstring, absolute, canonicaldirpath, do_tests, verbose);


      // ====> these are equal to the tests <file in super directory> above
      //
      // parallel directory , absolute path
      // ==================================
      //testfile = new File("home/wonka/subdir/../parallel");
      //testfile = new File("home/wonka/subdir/..","parallel");
      //testfile = new File(new File("home/wonka/subdir/.."),"parallel");
      //testfile = new File("home/wonka/subdir","../parallel");
      //testfile = new File(new File("home/wonka/subdir"),"../parallel");

      // file in parallel directory, relative path
      // =========================================
      absolute = canonicalrootpath + File.separator + testfilepath; //  => /home/wonka/../parallel/testfile.scr
      parent = parallelstring + File.separator + testfilestring; //  => parallel/testfile.scr
      //testfile = new File("../parallel/file1.scr");
      harness.checkPoint("new File("+testfilepath+");");
      testfile = new File(testfilepath);
      testFileProperties(testfile, testfilepath, testfilestring, parallelpath, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File("../parallel","file1.scr");
      harness.checkPoint("new File("+parallelpath+", "+testfilestring+");");
      testfile = new File(parallelpath, testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, parallelpath, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("../parallel"),"file1.scr");
      harness.checkPoint("new File( <new File("+parallelpath+")> , "+testfilestring+");");
      testfile = new File(new File(parallelpath), testfilestring);
      testFileProperties(testfile, testfilepath, testfilestring, parallelpath, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File("..","parallel/file1.scr");
      harness.checkPoint("new File("+superstring+", "+parent+");");
      testfile = new File(superstring, parent);
      testFileProperties(testfile, testfilepath, testfilestring, parallelpath, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File(".."),"parallel/file1.scr");
      harness.checkPoint("new File( <new File("+superstring+")> , "+parent+");");
      testfile = new File(new File(superstring), parent);
      testFileProperties(testfile, testfilepath, testfilestring, parallelpath, absolute, canonicalfilepath, do_tests, verbose);


      String subdirstring  = "subdir";                                                   //  => subdir
      String canonicalsubdirpath =  canonicalrootpath+ File.separator +subdirstring;            //  => /home/wonka/subdir
      String canonicalparalleldirpath =  canonicalrootpath+ File.separator +parallelstring;  //  => /home/wonka/parallel
      canonicalfilepath = canonicalparalleldirpath + File.separator +testfilestring;     //  => /home/wonka/parallel/testfile.scr
        harness.verbose("canonicalrootpath = "+canonicalrootpath);
        harness.verbose("canonicalsubdirpath = "+canonicalsubdirpath);
        harness.verbose("canonicalparalleldirpath = "+canonicalparalleldirpath);
        harness.verbose("canonicalfilepath = "+canonicalfilepath);

      // parallel sub directory , relative path
      // ======================================
      target = subdirstring + File.separator + testfilepath;      //  => subdir/../parallel/testfile.scr
      parent = subdirstring + File.separator + parallelpath;      //  => subdir/../parallel
      absolute =canonicalsubdirpath + File.separator + testfilepath; //  => /home/wonka/subdir/../parallel/testfile.scr
      //testfile = new File("subdir/../parallel/file1.scr");
      harness.checkPoint("new File("+target+");");
      testfile = new File(target);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File("subdir/../parallel","file1.scr");
      harness.checkPoint("new File("+parent+", "+testfilestring+");");
      testfile = new File(parent, testfilestring);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("subdir/../parallel"),"file1.scr");
      harness.checkPoint("new File(<new File("+parent+")> ,"+testfilestring+");");
      testfile = new File(new File(parent), testfilestring);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File("subdir","../parallel/file1.scr");
      harness.checkPoint("new File("+subdirstring+","+testfilepath+");");
      testfile = new File(subdirstring, testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("subdir"),"../parallel/file1.scr");
      harness.checkPoint("new File(<new File("+subdirstring+")> ,"+testfilepath+");");
      testfile = new File(new File(subdirstring), testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, absolute, canonicalfilepath, do_tests, verbose);

      // file in parallel directory , absolute path
      // ==========================================
      target =  canonicalsubdirpath + File.separator + testfilepath;      //  => /home/wonka/subdir/../parallel/testfile.scr
      parent =  canonicalsubdirpath + File.separator + parallelpath;      //  => /home/wonka/subdir/../parallel
      absolute =canonicalsubdirpath + File.separator + testfilepath; //  => /home/wonka/subdir/../parallel/testfile.scr
      //testfile = new File("home/wonka/subdir/../parallel/file1.scr");
      harness.checkPoint("new File("+target+");");
      testfile = new File(target);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File("home/wonka/subdir/../parallel","file1.scr");
      harness.checkPoint("new File("+parent+", "+testfilestring+");");
      testfile = new File(parent, testfilestring);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("home/wonka/subdir/../parallel"),"file1.scr");
      harness.checkPoint("new File(<new File("+parent+")> ,"+testfilestring+");");
      testfile = new File(new File(parent), testfilestring);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File("home/wonka/subdir/..","parallel/file1.scr");
      String tempdirpath = canonicalsubdirpath + File.separator + superstring; //  => /home/wonka/subdir/..
      String tempfilepath = parallelstring + File.separator + testfilestring;  //  => parallel/testfilestring
      harness.checkPoint("new File("+tempdirpath+", "+tempfilepath+");");
      testfile = new File(tempdirpath, tempfilepath);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("home/wonka/subdir/.."),"parallel/file1.scr");
      harness.checkPoint("new File(<new File("+tempdirpath+")> ,"+tempfilepath+");");
      testfile = new File(new File(tempdirpath), tempfilepath);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File("home/wonka/subdir","../parallel/file1.scr");
      harness.checkPoint("new File("+canonicalsubdirpath+", "+testfilepath+");");
      testfile = new File(canonicalsubdirpath, testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);
      //testfile = new File(new File("home/wonka/subdir"),"../parallel/file1.scr");
      harness.checkPoint("new File(<new File("+canonicalsubdirpath+")> ,"+testfilepath+");");
      testfile = new File(new File(canonicalsubdirpath), testfilepath);
      testFileProperties(testfile, target, testfilestring, parent, target, canonicalfilepath, do_tests, verbose);

    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }

/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* This tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for the super directory of the current directory  using <../> and <subdir/..>
*/
  private void testFileConstructorsRootdirFile(boolean do_tests, boolean verbose)
  {
   harness.checkPoint("file constructors on a full root(home/rootdir) directory");
   try
   {

      String rootstring = File.separator +"home";                           //  => /home
      String rootdirstring = "wonka";                                       //  => wonka
      String rootdirpath = rootstring + File.separator + rootdirstring; //  => /home/wonka
      String testfilestring = "testfile.scr";                               //  => testfile.scr
      String testfilepath = rootdirpath + File.separator + testfilestring;  //  => /home/wonka/testfile.scr


      File testfile;
      String target;
      String parent;
      String absolute;

      // full root directory, (by definition absolute path)
      // =================================
      target = rootdirpath + File.separator;
      //testfile = new File("/home/wonka/");
      harness.checkPoint("new File("+target+");");
      testfile = new File(target);
      testFileProperties(testfile,rootdirpath , rootdirstring, rootstring, rootdirpath, rootdirpath, do_tests, verbose);
      //testfile = new File("/home", "wonka/");
      target = rootdirstring + File.separator;
      harness.checkPoint("new File("+rootstring+", "+target+");");
      testfile = new File(rootstring, target);
      testFileProperties(testfile,rootdirpath , rootdirstring, rootstring, rootdirpath, rootdirpath, do_tests, verbose);
      //testfile = new File(<new File("/home")>, "wonka/")
      harness.checkPoint("new File(<new File("+rootstring+")>, "+target+");");
      testfile = new File(new File(rootstring), target);
      testFileProperties(testfile,rootdirpath , rootdirstring, rootstring, rootdirpath, rootdirpath, do_tests, verbose);

      // full root file, (by definition absolute path)
      // =================================
      //testfile = new File("/home/wonka/testfile.scr");
      harness.checkPoint("new File("+testfilepath+");");
      testfile = new File(testfilepath);
      testFileProperties(testfile,testfilepath , testfilestring, rootdirpath, testfilepath, testfilepath, do_tests, verbose);
      //testfile = new File("/home/wonka", "testfile.scr");
      harness.checkPoint("new File("+rootdirpath+","+testfilestring+");");
      testfile = new File(rootdirpath, testfilestring);
      testFileProperties(testfile,testfilepath , testfilestring, rootdirpath, testfilepath, testfilepath, do_tests, verbose);
      //testfile = new File(<new File("/home/wonka")>, "testfile.scr")
      harness.checkPoint("new File(<new File("+rootdirpath+")>,"+testfilestring+");");
      testfile = new File(new File(rootdirpath), testfilestring);
      testFileProperties(testfile,testfilepath , testfilestring, rootdirpath, testfilepath, testfilepath, do_tests, verbose);

      target = rootdirstring + File.separator + testfilestring; // wonka/testfile.scr
      //testfile = new File("/home", "wonka/testfile.scr");
      harness.checkPoint("new File("+rootstring+","+target+");");
      testfile = new File(rootstring, target);
      testFileProperties(testfile,testfilepath , testfilestring, rootdirpath, testfilepath, testfilepath, do_tests, verbose);
      //testfile = new File(<new File("/home")>, "wonka/testfile.scr")
      harness.checkPoint("new File(<new File("+rootstring+")>,"+target+");");
      testfile = new File(new File(rootstring), target);
      testFileProperties(testfile,testfilepath , testfilestring, rootdirpath, testfilepath, testfilepath, do_tests, verbose);


/**
NOTE: there seems to be some controversy on what to do with File("",filestring) and File(new File(""), filestring)
It is open to interpretion wether File("","title.txt") and File(new File(""),"title.txt") should refer to
File("/title.txt") : the root or to File("title.txt") : the current dir.
Therefore, all tests referring to File("","title.txt") and File(new File(""),"title.txt") are edited out
and can be filled in again according to the specific algorithm of the VM
**/
/*
      // new File("", 'testfile.scr');
      harness.checkPoint("new File('', "+testfilestring+");");
      testfile = new File("",testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                    do_tests, verbose);

      // new File(File(""), 'testfile.scr');
      harness.checkPoint("new File(<currentdir=new File('')>, "+testfilestring+");");
      testfile = new File(currentdir,testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                    do_tests, verbose);
*/

/**
NOTE: file resolvement discards the separator at the end of a directory, so there is some controversy
as to File("/") is equal to File(""): the current dir or should apply to directory '/' : the root
Untill this is resolved, all corresponding tests are left out
the same applies to File("/","title.txt") and File(new File("/"),"title.txt"),
**/
/*
      testfile = new File(File.separator);
      harness.checkPoint("Testing current dir File('/')");
//  => regards File("/") as File("") : current dir
      testFileProperties(testfile, "", "", null, canonicalrootpath, canonicalrootpath, do_tests, verbose);
//  => regards File("/") as Root instead of current dir
      testFileProperties(testfile, "", "", null, "", "", do_tests, verbose);

      // new File("/", 'testfile.scr');
      harness.checkPoint("new File('', "+testfilestring+");");
      testfile = new File("",testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, testfilestring, testfilestring, null, testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                    do_tests, verbose);

      // new File(File('/'), 'testfile.scr')
      harness.checkPoint("new File('/', "+testfilestring+");");
      testfile = new File(new File("/"),testfilestring);
//  => regards File("",string) as File(string)
      testFileProperties(testfile, "/"+testfilestring, testfilestring, "/", testfilepath, testfilepath, do_tests, verbose);
//  => regards File("",string) as File('/string') : in root instead of current dir
      testFileProperties(testfile, File.separator+testfilestring, testfilestring, File.separator, testfilepath, testfilepath,
                                                                                                                    do_tests, verbose);
*/
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }


/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Tests the toURL() function for files and directories
*/
	private void testFileToURL()
	{
    try
    {
      File currentdir = new File("");
      File   canonicalfile = currentdir.getCanonicalFile();
      String canonicalpath = currentdir.getCanonicalPath();

      String testfilestring = "testfile.scr";
      String testfilepath   = canonicalpath+File.separator+"testfile.scr";

      harness.checkPoint("url tests for absolute file");
      URL target1 = doURLTests(new File(testfilepath));

      harness.checkPoint("comparing URL from same file");
      File mirrorfile=new File(testfilepath);
      URL mirror1 = mirrorfile.toURL();
      // do the comparing...
      harness.check(target1,mirror1,"same files same url's");
      harness.check(target1.getFile(),mirror1.getFile(),"same files same url.getFile()");
      harness.check(target1.toString(),mirror1.toString(),"same files same url.toString()");
      //harness.check(target1.sameFile(mirror1),"same files : ULR.sameFile() is true");
      harness.fail("URL.sameFile() not yet defined for Wonka");

      harness.checkPoint("url tests for absolute files constructed with absolute dir/dir string");
      URL target2 = doURLTests(new File(canonicalfile,testfilestring));
      URL target3 = doURLTests(new File(canonicalpath,testfilestring));
      harness.checkPoint("comparing URL from same file, different constructors");
      harness.check(target1,target2,"same files/different constructors, same url's");
      harness.check(target1.getFile(),target2.getFile(),"same files same url.getFile()");
      harness.check(target1.toString(),target2.toString(),"same files same url.toString()");
//      harness.check(target1.sameFile(target2),"equivalent absolute and relative files : ULR.sameFile() is true");
      harness.fail("URL.sameFile() not yet defined for Wonka");
      harness.check(target1,target3,"same files/different constructors, same url's");
      harness.check(target1.getFile(),target3.getFile(),"same files same url.getFile()");
      harness.check(target1.toString(),target3.toString(),"same files same url.toString()");
      harness.fail("URL.sameFile() not yet defined for Wonka");
//      harness.check(target1.sameFile(target3),"equivalent absolute and relative files : ULR.sameFile() is true");


      harness.checkPoint("url tests for relative file");
      URL target4 = doURLTests(new File(testfilestring));
      harness.checkPoint("comparing URL from absolute and relative file");
      harness.check(target1,target4,"absolute and relative files, same url's");
      harness.check(target1.getFile(),target4.getFile(),"same files same url.getFile()");
      harness.check(target1.toString(),target4.toString(),"same files same url.toString()");
      harness.fail("URL.sameFile() not yet defined for Wonka");
//      harness.check(target1.sameFile(target4),"equivalent absolute and relative files : ULR.sameFile() is true");


      harness.checkPoint("url tests for absolute and relative dirs");
      URL targetdir1 = doURLTests(new File(canonicalpath), /*isdir*/true);
      URL targetdir2 = doURLTests(canonicalfile, /*isdir*/true);
      harness.checkPoint("urls of directories should end with '/', indepent of operating system");
      harness.check((targetdir1.toString()).endsWith("/"),"directory<"+targetdir1+"> should end with '/')");
      harness.check((targetdir2.toString()).endsWith("/"),"directory<"+targetdir2+"> should end with '/')");

      harness.checkPoint("comparing URL from same dirs, different constructors");
      harness.check(targetdir1,targetdir2,"same files/different constructors, same url's");
      harness.check(targetdir1.getFile(),targetdir2.getFile(),"same files same url.getFile()");
      harness.check(targetdir1.toString(),targetdir2.toString(),"same files same url.toString()");
      harness.fail("URL.sameFile() not yet defined for Wonka");
//      harness.check(targetdir1.sameFile(targetdir2),"equivalent absolute and relative files : ULR.sameFile() is true");

      harness.checkPoint("Special case: URL(File['']) not recognised as dir");
      URL targetdir3 = doURLTests(currentdir, /*isdir*/false);
      harness.check(!(targetdir3.toString()).endsWith("/"),"directory<''> not recognised as dir: should not end with '/')");

      harness.check(!(targetdir1.equals(targetdir3)),"equivalent path but not regarded as dir");
      harness.check(!((targetdir1.getFile()).equals(targetdir3.getFile()) ),"File('').grtFile not regarded as dir");
      harness.check(targetdir1.toString(),targetdir3.toString()+"/","File('').toString() not regarded as dir");
      harness.fail("URL.sameFile() not yet defined for Wonka");
//      harness.check(!(targetdir1.sameFile(targetdir3)),"ULR.sameFile() File('').toString() not regarded as dir");

      harness.checkPoint("Special case: file explicitly created as dir => URL explicitly recognised as dir");
      File explicit = new File("newdir");
      explicit.mkdir();
      URL targetdir4 = doURLTests(explicit, /*isdir*/true);
      harness.check(explicit.isDirectory(),"File.isDir() => URL file regarded as dir" );
      harness.check((targetdir4.toString()).endsWith("/"),"File created as dir should end with '/')");
      explicit.delete();
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
	}
/****************************************************************************************************************************************/
/**
* do the actual URL tests
*/
  private URL doURLTests(File source) { return doURLTests(source, /*isdir*/false); }
  private URL doURLTests(File source, boolean isdir)
  {
    URL dest = null;
    try
    {
      dest = source.toURL();
      harness.verbose("file <"+source+"> to URL : URL <"+dest+">");
      harness.verbose("URL <"+dest+"> to file :<"+dest.getFile()+">");

      //harness.check( (dest.toString()).endsWith(dest.getFile()),"(file.toURL()).toString()");
      harness.check(dest.getProtocol(), "file","(file.toURL()).Protocol = file");
      harness.check(dest.getPort(),-1,"(file.toURL()).port = not_set/-1 by definition");
      harness.check(dest.getHost(),"","(file.toURL()).Host = empty string");
      if(isdir)
        harness.check(dest.getFile(), source.getCanonicalPath()+'/',"(directory.toURL()).getFile() = complete path + ending slash");
      else
        harness.check(dest.getFile(), source.getCanonicalPath(),"(file.toURL()).getFile() = complete path of file");
      harness.check(dest.getRef(),null,"(file.toURL()).Ref = empty string");
    }
    catch(Exception ex)
    {
      harness.fail(ex.toString());
    }
    return dest;
  }

  private void throwExURLTests(File target, String reason)
  {
    URL dest = null;
    try
    {
      target.toURL(); //shold throw exception()
      harness.fail("toURL()("+reason+")should throw  MalformedURLException");
    }
    catch(MalformedURLException mux)
    {
      harness.check(true, "toURL()("+reason+")threw desired MalformedURLException : "+mux);
    }
    catch(Exception ex)
    {
      harness.fail("toURL()("+reason+")should throw  MalformedURLException, threw : "+ex);
    }
  }
	

/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* in Unix/Linux, a file starting with a '.' is considered hidden
*/
  private void testHiddenFiles()
  {
   if(File.separatorChar == '/') //unix/linux
   {
     harness.checkPoint("Checking hidden files (Unix/Linux only)");
     //in unix/linux, a file is considerd hidden when it starts with a '.'
     try
     {
        File currentdir = new File("");
        String canonicalroot=currentdir.getCanonicalPath();                   //  => /home/wonka

        File testfile;
        String testfilename;

        // public file, relative path
        testfilename = "public.scr";
        testfile = new File(testfilename);
        harness.check(!testfile.isHidden(),"["+testfilename+".isHidden() ]must be false");
        // public file, absolute path
        testfilename = canonicalroot+ File.separator+ testfilename;
        testfile = new File(testfilename);
        harness.check(!testfile.isHidden(),"["+testfilename+".isHidden() ]must be false");

        // hidden file, relative path
        testfilename = ".hidden.scr";
        testfile = new File(testfilename);
        harness.check(testfile.isHidden(),"[("+testfilename+").isHidden() ]must be true");
        // hidden file, absolute path
        testfilename = canonicalroot+ File.separator+ testfilename;
        testfile = new File(testfilename);
        harness.check(testfile.isHidden(),"[("+testfilename+").isHidden() ]must be true");

        // hidden dir, relative path
        testfilename = ".hiddendir";
        testfile = new File(testfilename);
        harness.check(testfile.isHidden(),"[("+testfilename+").isHidden() ]must be true");
        // hidden dir, absolute path
        testfilename = canonicalroot+ File.separator+ testfilename;
        testfile = new File(testfilename);
        harness.check(testfile.isHidden(),"[("+testfilename+").isHidden() ]must be true");

        // public file in hidden dir, relative path
        testfilename = ".hiddendir/notpublic.scr";
        testfile = new File(testfilename);
        harness.check(!testfile.isHidden(),"["+testfilename+".isHidden() ] public file in hidden dir is NOT hidden");
        // public file in hidden dir, absolute path
        testfilename = canonicalroot+ File.separator+ testfilename;
        testfile = new File(testfilename);
        harness.check(!testfile.isHidden(),"["+testfilename+".isHidden() ] public file in hidden dir is NOT hidden");


      }
      catch(Exception e)
      {
       harness.fail(e.toString());
      }
    }
  }

/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Helper function: verbose path and parent representations of a given file
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
*/
  private void verboseFileProperties(File targetfile)
  {
      harness.verbose("target file => getPath :          <"+targetfile.getPath()+">");
      harness.verbose("target file => getName :          <"+targetfile.getName()+">");
      harness.verbose("target file => getParent :        <"+targetfile.getParent()+">");
      harness.verbose("target file => getAbsolutePath :  <"+targetfile.getAbsolutePath()+">");
    try
    {
      harness.verbose("target file => getCanonicalPath : <"+targetfile.getCanonicalPath()+">");
    }
    catch(IOException iox)
    {
     harness.fail("IO exception canonicalPath for <"+targetfile.toString()+"> : "+iox.toString());
    }
  }

/****************************************************************************************************************************************/
/**
* Helper function: tests the behavior of the File constructors and the path and parent representations
* (getPath(), getName(), getParent(), getAbsolutePath(), getCanonicalPath(), getParentFile(), getAbsoluteFile(), getCanonicalFile()  )
* for a given file (the calues to test are equally given
*/
  private void testFileProperties(File targetfile, String path, String name, String parent, String absolute, String canonical)
  {
    // path & name
    harness.check(targetfile.getPath(), path , "getPath()");
    harness.check(targetfile.toString(), path , "toString() definition");
    harness.check(targetfile.getName(), name, "getName()");

    //isAbsolute definition
    if(path.startsWith(File.separator))
      harness.check(targetfile.isAbsolute(),"isAbsolute() must be true for"+targetfile);
    else
      harness.check(!targetfile.isAbsolute(),"isAbsolute() must be false for"+targetfile);

    //parent
    harness.check(targetfile.getParent(), parent, "getParent()");
    if(parent==null)
      harness.check(targetfile.getParentFile(), null, "getParentFile() definition (parent = null)");
    else
      harness.check(targetfile.getParentFile(), new File(parent), "getParentFile() definition");
    //absolute file
    harness.check(targetfile.getAbsolutePath(), absolute, "getAbsolutePath()");
    if(absolute==null)
      harness.check(targetfile.getAbsoluteFile(), null, "getAbsoluteFile() definition (absolute form == null)");
    else
    {
      harness.check(targetfile.getAbsoluteFile(), new File(absolute), "getAbsoluteFile() definition");
      harness.check((targetfile.getAbsoluteFile()).isAbsolute(),"isAbsolute() definition absolute file");
    }
    //canonic form of file
    try
    {
      harness.check(targetfile.getCanonicalPath(), canonical, "getCanonicalPath()");
      if(canonical==null)
        harness.check(targetfile.getCanonicalFile(), null, "getCannonicalFile() definition (canonical form == null)");
      else
      {
        harness.check(targetfile.getCanonicalFile(), new File(canonical), "getCanonicalFile() definition");
        harness.check((targetfile.getAbsoluteFile()).isAbsolute(),"isAbsolute() definition canonical file");
      }
    }
    catch(IOException iox)
    {
      harness.fail("IO exception canonicalPath for <"+targetfile.toString()+"> : "+iox.toString());
    }


  }

  private void testFileProperties(File targetfile, String path, String name, String parent, String absolute, String canonical,
                                                                                                boolean do_tests, boolean verbose)
  {
    if(verbose)
      verboseFileProperties(targetfile);
    if(do_tests)
      testFileProperties(targetfile,path, name, parent, absolute, canonical);
  }


/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                       The actual tests                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/


  public void test(TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.io.File");
		
		testSeparators();
		testNullConstructors();
		testFileEquals();
				
		testFileConstructorsCurrentDir(/*do tests =*/true, /*verbose =*/false);
		testFileConstructorsCurrentFile(/*do tests =*/true, /*verbose =*/false);
		testFileConstructorsSubdirFile(/*do tests =*/true, /*verbose =*/false);
		testFileConstructorsSuperdirFile(/*do tests =*/true, /*verbose =*/false);
		testFileConstructorsSuperDir(/*do tests =*/true, /*verbose =*/false);
		testFileConstructorsParalleldirFile(/*do tests =*/true, /*verbose =*/false);
		testFileConstructorsRootdirFile(/*do tests =*/true, /*verbose =*/false);
				
		testFileToURL();
		
		testHiddenFiles();
	}
}
