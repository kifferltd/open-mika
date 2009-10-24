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



/****************************************************************************************************************************************/
/**
* Main class PhysicalFileTest:
* => tests for the physical existance of a file using isFile, isDirectory, canRead, canWrite... all of this must be false
*     if the target file does NOT exist , also check the definition of length() and LastModified() for nonexisting files
* => Preform the same tests on existing files and test setting a file read-only or defining its modification time
* => Tests the class functions of an instance of Class File
* => Tests building a file using CreateNewFile and deleting it using Delete
* => Tests building a file using CreateTempFile
* => Tests listing the contents of a directory using list and listFiles with and without filter
* => Tests the correct behavior of File.ListRoots() in Linux environment
* => Tests renaming a file using RenameTo
*/
public class PhysicalFileTest implements Testlet
{

  /****************************************************************************************************************************************/
  /**
  * our instance of the list filter FilternameFilter
  * Number1Filter filters out all the files 'file<level><fileno>.scr' obtained by calling getFilePath(root, level, fileno)
  * and only lets pass those files that have fileno == 1
  */
  class NumberOneFilter implements FilenameFilter {
    public boolean accept(File dir, String name)   {    return( name.endsWith("1.scr") || name.startsWith("level") );  }
  }

  final static String DIRSTUB  = "level_";
  final static String FILESTUB = "file";
  final static String FILEEND  = ".scr";
  final static String TEMPEND  = ".tmp";
  final static String SUBDIR1  = "subdir_1";
  final static String SUBDIR2  = "subdir_2";
  protected static TestHarness harness;


/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                                        < NEW SECTION >                                                             **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Tests a number of specific Class functions Class.getClass, getName, forName(), instanceOf...
*/
  private void testClass()
  {
    boolean mustfail = false;
    try
    {
      File current = new File(""); //current dir
      File fulldir = current.getCanonicalFile();
      String fullpath = current.getCanonicalPath();
      String target;

      //current directory
      harness.checkPoint("Class tests current dir");
      doClassTests(current,current);
      doClassTests(fulldir,current);
      doClassTests(new File(fullpath),current);

      // sub directory
      target = getDirPath(null,1);
      doClassTests(target, fullpath, fulldir, current);

      // file in current dir
      target = getFilePath(null,0,1);
      doClassTests(target, fullpath, fulldir, current);

      // file in super dir
      target = getFilePath(null,1,1);
      doClassTests(target, fullpath, fulldir, current);

      // Special canonical construction
      target = getDirPath(null,1)+".."+File.separator+getFilePath(null,0,1); //  => level1/../file01
      doClassTests(target, fullpath, fulldir, current);

      //special conditions
      mustfail = true;
      harness.checkPoint("Class tests null class, should throw NullPointerException");
      doClassTests((File)null, new File(""));
      harness.fail("Class tests null class, should throw NullPointerException");

    }
    catch (NullPointerException e)
    {
      harness.check(mustfail, "Class tests null class, should throw NullPointerException : "+e.toString());
    }
    catch (Exception e)
    {
      harness.fail(e.toString());
    }
  }
/****************************************************************************************************************************************/
/**
* do the actual tests
*/
  private void doClassTests(String target, String root, File rootdir, File dummy) throws Exception
  {
    harness.checkPoint("Class tests file <"+target+">");
    doClassTests(new File(target), dummy);
    doClassTests(new File(root+File.separator+target), dummy);
    doClassTests(new File(root, target), dummy);
    doClassTests(new File(rootdir, target), dummy);
  }

  private void doClassTests(File target, File dummy) throws Exception
  {
      Class targetclass = target.getClass();

      harness.check (target instanceof File , "target instanceof File");
      harness.check (targetclass, dummy.getClass(), "target class = File.class");
      harness.check (targetclass.getName(), "java.io.File", "target class getName = <java.io.File>");
      harness.check (targetclass, Class.forName("java.io.File"), "target class = new class(java.io.File)" );
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
* Tests a number of functions on creating, existing, deleting for each of the given files
*/
  private void testConstruction(boolean absolute)
  {
    harness.checkPoint("Test construction, existence deleting of directories and files: absolute path:"+absolute);
    String root = (absolute)?getCanonicalRoot():null;
    File testfile;
    File level1=getDir(root,1);

    //directory construction using mkdir()
    //root path
    //checking dir < File(level1/) > :existing root path
    testfile = getDir(root,1);
    harness.checkPoint("Construction of directory <"+testfile+"> using mkdir()");
    doDirConstructionTests(testfile);

    //existing relative path
    level1.mkdir(); //build existing path
    //checking dir < File(level1/) > :existing relative path
    testfile = getDir(root,2);
    harness.checkPoint("Construction of directory <"+testfile+"> using mkdir()");
    doDirConstructionTests(testfile);

    //checking dir < File(level1/level2) > non-existing relative path using mkdir()=> should fail
    testfile = getDir(root,3);
    harness.checkPoint("Construction of directory <"+testfile+"> in non-existing path, using mkdir()");
    failDirConstructionTests(testfile,"creating dir in nonexisting directory returns false");

    // checking <File(level1/level2/..)> level1 still existing
    testfile = getResolvedRootDir("testdir",absolute);
    harness.checkPoint("Construction of canonical resolved directory <"+testfile+"> through existing path, using mkdir()");
    doDirConstructionTests(testfile);

    level1.delete(); //delete again

    // checking <File(level1/level2/..)> level1 no longer existing => should fail
    testfile = getResolvedRootDir("testdir",absolute);
    harness.checkPoint("Construction of canonical resolved directory <"+testfile+"> through existing path, using mkdir()");
    failDirConstructionTests(testfile,"creating canonicAL dir through nonexisting directory returns false");


    //directory constructions using mkdirs(): multiple dir paths at once
    //checking dir < File(level1/) using mkdirs() > : existing relative path
    harness.checkPoint("Construction of directory <"+getDirPath(root,1)+"> using mkdirs()");
    doDirConstructionTests(root,1);
    //checking dir < File(level1/) using mkdirs() > : non-existing relative path
    harness.checkPoint("Construction of relative file <"+getDirPath(root,3)+"> using mkdirs()");
    doDirConstructionTests(root,3);


    //file constructors using createNewFile()
    //checking dir < File(file01.scr) > :existing root path
    testfile = getFile(root,0,1);
    harness.checkPoint("Construction of file <"+testfile+"> using createNewFile()");
    doFileConstructionTests(testfile);    //leave the file existing for full path checking

    //relative path
    level1.mkdir(); //build existing path
    //checking dir < File(level1/file11.scr) > :existing directory
    testfile = getFile(root,1,1);
    harness.checkPoint("Construction of file <"+testfile+"> using createNewFile()");
    doFileConstructionTests(testfile);

    //checking dir < File(level1/level2/file21.scr) > non-existing directory => should fail
    testfile = getFile(root,2,1);
    harness.checkPoint("Construction of file <"+testfile+"> in non-existing path, using createNewFile()");
    failFileConstructionTests(testfile,"creating dir in nonexisting directory returns false");

    //checking dir < File(level1/../file01.scr) > existing directory using mkdir()=> should fail
    testfile = getResolvedRootFile(1, absolute);
    harness.checkPoint("Construction of canonical resolved file <"+testfile+"> in existing path, using createNewFile()");
    doFileConstructionTests(testfile);

    level1.delete(); //delete again

    //checking dir < File(level1/file01.scr) > non-existing relative path using mkdir()=> should fail
    testfile = getResolvedRootFile(1, absolute);
    harness.checkPoint("Construction of canonical resolved file <"+testfile+"> in non-existing path, using createNewFile()");
    failFileConstructionTests(testfile,"using non-exixting directory");

  }


/****************************************************************************************************************************************/
/**
* perform a series of tests as to existance, construction and properties to a given file
* we suppose the directory to be non-existant in the beginning of the tests and we will delete it after the tests again
*/
  private void doDirConstructionTests(File testdir)
  {
    //check if (now non-existing) directory has the properties defined for non-existing files
    checkBasicNonExisting(testdir);

    //delete again
    harness.check(!testdir.delete(),"Deleting no longer existing file must return false" );
    //construct new directory
    harness.check(testdir.mkdir(),"Creating new directory");
    harness.check(!testdir.mkdir(),"Creating already existing directory should throw error");

    //basic file properties
    checkBasicExisting(testdir,/*isdirectory =*/true);

    // setlastmodified
    harness.check(testdir.setLastModified(1000L),"Setting last modified time");
    harness.check(testdir.lastModified(),1000L,"last modified time, just set");
    harness.check(testdir.setLastModified(2005L),"Setting last modified time, trunkated to 1000 milliseconds");
    harness.check(testdir.lastModified(),2000L,"last modified time, just set, trunkated to 1000 milliseconds");

    //setreadonly
    harness.check(testdir.setReadOnly(),"Setting file to read-only");
    harness.check(testdir.canRead(),"setReadOnly() must still must alow read" );
    harness.check(!testdir.canWrite(),"setReadOnly() may not alow write" );

    //delete the created directory again
    harness.check(testdir.delete(),"Deleting existing file" );
  }

/****************************************************************************************************************************************/
/**
* perform a series of tests as to existance, construction and properties to a given file, explicitly using mkdirs to create multiple
* directories at once and the  getDirPath(root/level)/ deleteFileStructure(int currentlevel, int filesperlevel, boolean checkexists)
* functions to build and delete a complete three using the level1/level2 syntax
* we suppose the directory to be non-existant in the beginning of the tests and we will delete it after the tests again
*/
  private void doDirConstructionTests(String root, int level)
  {
    File testdir = getDir(root, level); //a discrete directory path
    //check if (now non-existing) directory has the properties defined for non-existing files
    checkBasicNonExisting(testdir);

    //delete again
    harness.check(!testdir.delete(),"Deleting no longer existing file must return false" );

    //construct new directory in one line
    harness.check(testdir.mkdirs(),"Creating new directory");
    harness.check(!testdir.mkdirs(),"Creating already existing directory should throw error");

    //basic file properties
    checkBasicExisting(testdir,/*isdirectory =*/true);

    // setlastmodified
    harness.check(testdir.setLastModified(1000L),"Setting last modified time");
    harness.check(testdir.lastModified(),1000L,"last modified time, just set");
    harness.check(testdir.setLastModified(2005L),"Setting last modified time, trunkated to 1000 milliseconds");
    harness.check(testdir.lastModified(),2000L,"last modified time, just set, trunkated to 1000 milliseconds");

    //setreadonly
    harness.check(testdir.setReadOnly(),"Setting file to read-only");
    harness.check(testdir.canRead(),"setReadOnly() must still must alow read" );
    harness.check(!testdir.canWrite(),"setReadOnly() may not alow write" );

    //delete the file structure again
    deleteFileStructure(level,0,true); //"Deleting existing file structure"
  }

/****************************************************************************************************************************************/
/**
* perform a series of tests as to existance, construction and properties to a given file directory
* we suppose the file to be non-existant in the beginning of the tests and we will delete it after the tests again
*/
  private void doFileConstructionTests(File testfile)
  {
    //check if (now non-existing) file has the properties defined for non-existing files
    checkBasicNonExisting(testfile);

    harness.check(!testfile.delete(),"Deleting no longer existing file must return false" );
    try
    {
      harness.check(testfile.createNewFile(),"Creating new (non-existing) file");
      harness.check(!testfile.createNewFile(),"Creating already-existing file must return false");
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }

    //basic file properties
    checkBasicExisting(testfile,/*isdirectory =*/false);

    // setlastmodified
    harness.check(testfile.setLastModified(1000L),"Setting last modified time");
    harness.check(testfile.lastModified(),1000L,"last modified time, just set");
    harness.check(testfile.setLastModified(2005L),"Setting last modified time, trunkated to 1000 milliseconds");
    harness.check(testfile.lastModified(),2000L,"last modified time, just set, trunkated to 1000 milliseconds");

    //setreadonly
    harness.check(testfile.setReadOnly(),"Setting file to read-only");
    harness.check(testfile.canRead(),"setReadOnly() must still must alow read" );
    harness.check(!testfile.canWrite(),"setReadOnly() may not alow write" );

    harness.check(testfile.delete(),"Deleting existing file" );
  }

/****************************************************************************************************************************************/
/**
* explicitly tries to construct a directory on a path that does not exist using mkdir()
* This test is sheduled to fail to construct the desired file, and display the given reason string
* (Using mkdirs(), the test would nevertheless succeed, perform doDirConstructionTests(root, level) for that tests)
*/
  private void failDirConstructionTests(File testdir, String errormessage)
  {
    //check if (now non-existing) file has the properties defined for non-existing files
    checkBasicNonExisting(testdir);

    try
    {
      harness.check(!testdir.mkdir(),errormessage);
    }
    catch(Exception ex)
    {
     harness.fail(" unexpected exception <"+ex.toString()+">");
    }

    harness.check(!testdir.delete(),"Deleting non existing dir must return false" );
  }

/****************************************************************************************************************************************/
/**
* perform a series of tests as to existance, construction and properties to a given file
* This test is sheduled to fail to construct the desired file, either
*/
  private void failFileConstructionTests(File testfile, String errormessage)
  {
    //check if (now non-existing) file has the properties defined for non-existing files
    checkBasicNonExisting(testfile);

    try
    {
      testfile.createNewFile();
      harness.fail(errormessage+" : file nevertheless constructed");
    }
    catch(IOException iox)
    {
      harness.check(true ,errormessage + " IO exception <"+iox.toString()+">");
    }
    catch(Exception ex)
    {
     harness.fail(" unexpected exception <"+ex.toString()+">");
    }

    harness.check(!testfile.delete(),"Deleting non existing file must return false" );
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
* File object with same cannoical path refer to same physical file
* we make different File objects referring to the sane physical files and check the construction/deletion
* of the file througgh one of this file objects mirrored to the other
*/
	private void testCanonicalConstruction()
  {
    harness.checkPoint("Test construction and access of equivalent files");
    String canonicalroot = getCanonicalRoot();
    File currentrel;   //relative
    File currentabs;  //absolute
    File canonicalrel;
    File canonicalabs;
    //files in current dir
    currentrel = getFile(null,0,1);   //relative
    currentabs = getFile(canonicalroot,0,1);  //absolute
    canonicalrel = getResolvedRootFile(1,/*absolute*/false);
    canonicalabs = getResolvedRootFile(1,/*absolute*/true);

    File level = getDir(null,1);
    level.mkdir();

    //test for equivalent physical files
    testPhysicalPresence(currentrel, currentabs, canonicalrel, canonicalabs, /*isdir*/false);
    testPhysicalPresence(currentabs, canonicalrel, canonicalabs, currentrel, /*isdir*/false);
    testPhysicalPresence(canonicalrel, canonicalabs, currentrel, currentabs, /*isdir*/false);
    testPhysicalPresence(canonicalabs, currentrel, currentabs, canonicalrel, /*isdir*/false);

    //test for equivalent physical directories
    testPhysicalPresence(currentrel, currentabs, canonicalrel, canonicalabs, /*isdir*/true);
    testPhysicalPresence(currentabs, canonicalrel, canonicalabs, currentrel, /*isdir*/true);
    testPhysicalPresence(canonicalrel, canonicalabs, currentrel, currentabs, /*isdir*/true);
    testPhysicalPresence(canonicalabs, currentrel, currentabs, canonicalrel, /*isdir*/true);

    level.delete();
  }
/****************************************************************************************************************************************/
/**
*
**/
  private void testPhysicalPresence(File main, File equivalent1, File equivalent2, File equivalent3, boolean isdir)
  {
    try
    {
      //build file or dir
      if(isdir)
        harness.check(main.mkdir(),"building dir <"+main+">");
      else
         harness.check(main.createNewFile(),"building file <"+main+">");
      //check if equivalent files equally created
      checkBasicExisting(equivalent1 ,isdir); //absolute equally created
      checkBasicExisting(equivalent2 ,isdir); //absolute equally created
      checkBasicExisting(equivalent3 ,isdir); //absolute equally created
      //delete file/dir again
      main.delete();
      //check if equivalent files equally deleted
      checkBasicNonExisting(equivalent1); //absolute equally deleted
      checkBasicNonExisting(equivalent2); //absolute equally deleted
      checkBasicNonExisting(equivalent3); //absolute equally deleted
   }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
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
* test deleting of files and directories:
* => deleting of existing file: should pass
* => deleting of existing file set read-only: should pass
* => deleting of non-existing file : should fail
* => deleting of non-empty dir : should fail
* => deleting of existing empty dir : should pass
* => deleting of non-existing dir : should fail
*/
	private void testDeleting(boolean absolute)
	{
    // build directory with 3 files in it
    // level1/file 11 to level1/file13
    String root = (absolute)?getCanonicalRoot():null;
    buildFileStructure(root,1,3,true);
    File target;


    harness.checkPoint("deleting of existing file: should pass");
    target = getFile(root,1,3);
    harness.check(target.delete(),"deleting <"+target+">");
    harness.check(!target.exists(),"<"+target+"> deleted");

    harness.checkPoint("deleting of existing file set read-only: should pass");
    target = getFile(root,1,2);
    target.setReadOnly();
    harness.check(target.delete(),"deleting <"+target+">");
    harness.check(!target.exists(),"<"+target+"> deleted");

    harness.checkPoint("deleting of non-existing file : should fail");
    target = getFile(root,1,0);
    harness.check(!target.exists(),"<"+target+"> non-existing");
    harness.check(!target.delete(),"deleting nonexisting <"+target+"> must fail");

    harness.checkPoint("deleting of non-empty dir : should fail");
    target = getDir(root,1);
    harness.check(!target.delete(),"deleting non-empty dir <"+target+"> must fail");

    harness.checkPoint("deleting of non-existing dir : should fail");
    target = getDir(root,3);
    harness.check(!target.exists(),"<"+target+"> non-existing");
    harness.check(!target.delete(),"deleting non-existing dir <"+target+"> must fail");

    harness.checkPoint("deleting of existing empty dir : should pass");
    //delete last file in directory
    target = getFile(root,1,1);
    harness.check(target.delete(),"deleting <"+target+">");
    //delete directory
    target = getDir(root,1);
    harness.check(target.delete(),"deleting empty dir <"+target+"> should pass");
    harness.check(!target.exists(),"<"+target+"> deleted");
	
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
* File object with same cannonical path refer to same physical file
* we make different File objects referring to the sane physical files and check the construction/deletion
* of the file througgh one of this file objects mirrored to the other
*/
	private void testRenameFile(boolean absolute)
  {
    harness.checkPoint("Test renaming of files");
    String root = (absolute)?getCanonicalRoot():null;
    File sub1 = (absolute)?new File(root,SUBDIR1):new File(SUBDIR1);
    File sub2 = (absolute)?new File(root,SUBDIR2):new File(SUBDIR2);
    File target;
    File source = getFile(root,0,1);


    //files in current dir
    harness.checkPoint("Renaming files in current dir");
    doRenameFileTests(source, getFile(null,0,2), 0, false );   //file01, file02, relative path, no creation time, no readonly
    doRenameFileTests(source, getFile(null,0,2), 2000, true );   //file01, file02, relative path, creation time and readonly

    File level1= getDir(null,1,true);  //create this dir
    //files in subdir
    harness.checkPoint("Renaming files in different dir");
    doRenameFileTests(source, getFile(null,1,1), 0, false );   //file01, file02, relative path, no creation time, no readonly
    doRenameFileTests(source, getFile(null,1,1), 2000, true );   //file01, file02, relative path, creation time and readonly
    level1.delete();


    //File to existing file
    harness.checkPoint("Renaming files to already existing file");
    target = getFile(root,0,3,true);
    doRenameFileTests(source, target, 0, false, true, false, false );
    doRenameFileTests(source, target, 2000, true, true, false, false );
    harness.checkPoint("Renaming files to already existing file set to read-only");
    target.setReadOnly();
    doRenameFileTests(source, target, 0, false, true, false, false );
    doRenameFileTests(source, target, 2000, true, true, false, false );
    target.delete();

    //File to existing dir
    harness.checkPoint("Renaming files to already existing directory (must fail)");
    sub1.mkdir();
    failRenameFileTests(source, sub1, /*create*/true, /*isDir*/false,"Renaming file to existing directory" );
    sub1.delete();


    // dir to dir
    harness.checkPoint("Renaming directory to other directory");
    doRenameFileTests(sub1, sub2, 0,/*set readonly*/false,/*create*/true,/*delete*/true,/*isdir*/true);
    doRenameFileTests(sub1, sub2, 2000,/*set readonly*/true ,/*create*/true,/*delete*/true,/*isdir*/true);
    //dir to existing file

    harness.checkPoint("Renaming directory to existing directory");
    sub2.mkdir();
    doRenameFileTests(sub1, sub2, 0,/*set readonly*/false,/*create*/true,/*delete*/false,/*isdir*/true);
    doRenameFileTests(sub1, sub2, 2000,/*set readonly*/false,/*create*/true,/*delete*/false,/*isdir*/true);
    sub2.setReadOnly();
    doRenameFileTests(sub1, sub2, 0,/*set readonly*/false,/*create*/true,/*delete*/false,/*isdir*/true);
    doRenameFileTests(sub1, sub2, 2000,/*set readonly*/false,/*create*/true,/*delete*/false,/*isdir*/true);
    sub2.delete();

    //dir to existing file
    harness.checkPoint("Renaming directory to existing file");
    target = getFile(root,0,3,true);
    failRenameFileTests(sub1, target, /*create*/true, /*isDir*/true,"target exists and explicitly is a file");
    target.delete();
/*
    try
    {
/*
      //empty dir to non-empty dir
      sub1.mkdir();
      target1.createNewFile();
      harness.checkPoint("Renaming empty dir to existing  nonempty dir");
      failRenameFileTests(sub2, sub1, true, true,"Renaming empty dir to existing  nonempty dir");
      target1.delete();
      sub1.delete();

      // non-empty dir to nonexisting empty dir

      sub1.mkdir();
      target1.createNewFile();
      harness.checkPoint("Renaming existing nonempty dir to non-existing dir");
      failRenameFileTests(sub1, sub2,false, true,"Renaming non empty dir to new non-existing dir");
      //doRenameFileTests(sub1, sub2,0,false,false, true, true);
      target1.delete();
      sub1.delete();
      sub2.delete();
/*

      // non-empty dir to other non-empty dir
      harness.checkPoint("Renaming existing nonempty dir to existing non-empty dir");
      sub1.mkdir();
      target1.createNewFile();
      sub2.mkdir();
      target2.createNewFile();
      throwExRenameFileTests(sub1, sub2,"Renaming existing nonempty dir to existing nonempty dir");
      target1.delete();
      target2.delete();
      sub1.delete();
      sub2.delete();


      // non-empty dir to existing empty dir
      harness.checkPoint("Renaming existing nonempty dir to existing empty dir");
      sub1.mkdir();
      target1.createNewFile();
      sub2.mkdir();
      failRenameFileTests(sub1, sub2, false, true,"Renaming non empty dir to existing  empty dir");
      target1.delete();
      sub1.delete();
      sub2.delete();
*/
/*
    }
    catch (Exception ex)
    {
      harness.fail(ex.toString());
    }
*/

    // rename to file in non-existing subdir(impossible to create): should fail
    harness.checkPoint("Renaming files to unexisting subdir");
    failRenameFileTests(source, getFile(root,2,2) ,true, false, "target in unexisting subdir");

    // rename dir to own subdir(impossible to create): should fail
    harness.checkPoint("Renaming dir to own subdir");
    failRenameFileTests(sub1, new File(sub1,"sub3") ,true, false, "target is own subdir");

    // renaming non-existing file to other : should throw an exception
    harness.checkPoint("Renaming non-existing file");
    failRenameFileTests(getFile(root,0,3), getFile(root,0,2), false, false, "target in unexisting subdir");

    // to null file: should throw an exception
    harness.checkPoint("Renaming files nullfile");
    throwExRenameFileTests(source, null,"target is null");
  }


/****************************************************************************************************************************************/
/**
* Make the desired file, rename it to the new target and do some tests on them  (delete the fiel afterwards)
*/
  private void doRenameFileTests(File source, File dest, long creationtime, boolean readonly)
  {doRenameFileTests(source, dest, creationtime, readonly, /*create*/true, /*delete*/true, /*isdir*/ false); }

/****************************************************************************************************************************************/
/**
* same for desired dir  (no create and delete, no file length)
*/
  private void doRenameFileTests(File source, File dest, long creationtime, boolean readonly, boolean create, boolean delete, boolean isdir)
  {
    try
    {
      long length = 0;
      //create source if asked so
      if(create && isdir)
        harness.verbose((source.mkdir())?"directory <"+source+"> constructed":"dirctory <"+source+"> already existing" );
      else if(create)
        harness.verbose((source.createNewFile())?"File <"+source+"> constructed":"File <"+source+"> already existing" );

      if(!isdir)
        length = source.length();

      //get/set desired settings
      creationtime = (creationtime/1000L)*1000L;  //creationtime rounded down to multiple of 1000 ms
      if(creationtime>0L)
        source.setLastModified(creationtime);
      else
        creationtime = source.lastModified();
      //set readonly
      if(readonly)
        source.setReadOnly();

      // rename source to destination
      harness.check(source.renameTo(dest), "Renaming <"+source+"> to <"+dest+"> failed");
      // destination should be created now
      harness.check(dest.exists(),"Renaming <"+source+"> to <"+dest+"> did not build destination dir");
      // source should be deleted now
      harness.check(!source.delete(),"Renaming <"+source+"> to <"+dest+"> did not delete original dir");

      //check if new file inherited data from source correctly
      //last modified
      harness.check(dest.lastModified(),creationtime,"Renamed file <"+dest+"> did not retain original creation time");
      //read only
      harness.check(dest.canRead(),"Renamed file <"+dest+"> not allowed to read");
      harness.check((dest.canWrite()) == !readonly,"Renamed file <"+dest+"> did not retain read-only setting "+readonly);
      //is directory
      if(isdir)
        harness.check(dest.isDirectory(),"Renamed dir <"+dest+"> is not a directory any longer "+readonly);
      else
      {
        harness.check(dest.length(),length,"Renamed file <"+dest+"> did not retain original length "+length);
        harness.check(dest.isFile(),"Renamed file <"+dest+"> is not a file any longer");
      }
      //delete source if asked so
      if(delete)
        harness.verbose((dest.delete())?"<"+dest+"> deleted":"<"+dest+"> could not be deleted" );
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }
  }

/****************************************************************************************************************************************/
/**
* idem, but this time the renaming should fail
*/
  private void failRenameFileTests(File source, File dest, boolean create, boolean isdir, String reason)
  {
    try
    {
      //create source file if necessary
      if(create && isdir)
        harness.verbose((source.mkdir())?"source dir <"+source+"> constructed":"source dir <"+source+"> already exists" );
      else if(create)
        harness.verbose((source.createNewFile())?"source File <"+source+"> constructed":"source File <"+source+"> already exists" );
      else
        harness.verbose((source.exists())?"destination File <"+source+"> already exists":"destination File <"+source+"> not existing" );
      harness.verbose((dest.exists())?"destination File <"+dest+"> already exists":"destination File <"+dest+"> not existing" );

      // rename source to destination : SHOULD FAIL
      harness.check(!(source.renameTo(dest)), "Renaming <"+source+"> to <"+dest+"> ("+reason+") should fail ");
      //delete source and destination file again
      harness.verbose((source.delete())?"File <"+source+"> deleted":"File <"+source+"> could not be deleted" );
      harness.verbose((dest.delete())?"File <"+dest+"> deleted":"File <"+dest+"> could not be deleted" );
    }
    catch(Exception ex)
    {
     harness.fail( "Renaming <"+source+"> to <"+dest+"> ("+reason+") threw error : "+ex);
    }
  }
/****************************************************************************************************************************************/
/**
* idem, but this time the renaming should throw an exception
*/
  private void throwExRenameFileTests(File source, File dest, String reason)
  {
    try
    {
      //create source file if necessary
      //harness.verbose((source.exists())?"destination File <"+source+"> already exists":"destination File <"+source+"> not existing" );
      //harness.verbose((dest.exists())?"destination File <"+dest+"> already exists":"destination File <"+dest+"> not existing" );

      // rename source to destination : SHOULD THROW ERROR
      source.renameTo(dest);
      harness.fail("Renaming <"+source+"> to <"+dest+"> ("+reason+")did not throw desired exception");

      //delete source and destination file again
      harness.verbose((source.delete())?"File <"+source+"> deleted":"File <"+source+"> could not be deleted" );
      harness.verbose((dest.delete())?"File <"+dest+"> deleted":"File <"+dest+"> could not be deleted" );
    }
    catch(Exception ex)
    {
     harness.check(true,"Renaming <"+source+"> to <"+dest+"> ("+reason+") threw desired exception : "+ex);
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
* File object with same cannoical path refer to same physical file
* we make different File objects referring to the sane physical files and check the construction/deletion
* of the file througgh one of this file objects mirrored to the other
*/
	private void testCreateTempFile()
  {
    File currentdir = null;
    String currentpath = null;
    currentdir  = getCanonicalDir();    //  => /home/wonka
    currentpath = getCanonicalRoot();    //  => /home/wonka
    File created;
    String createdpath;



    try
    {
      String currentstub = currentpath+File.separator+FILESTUB;  // => /home/wonka/tempfile.tmp
    harness.checkPoint("createTempFile constructor");
      File created1 = File.createTempFile(FILESTUB, TEMPEND, currentdir); //, new File(""));
      checkTempFile(created1, currentstub, TEMPEND);

    harness.checkPoint("createTempFile constructor, null suffix");
      File created2 = File.createTempFile(FILESTUB, null, currentdir); //, new File(""));
      checkTempFile(created2, currentstub, TEMPEND);
      harness.check(!created2.equals(created1),"Files <"+created2+">,<"+created1+"> : no 2 tempfiles should be the same");

    harness.checkPoint("createTempFile constructor, full path from root");
      File created3 = File.createTempFile(currentstub, TEMPEND, new File(File.separator));
      checkTempFile(created3, currentstub, TEMPEND);
      harness.check(!created3.equals(created1),"Files <"+created3+">,<"+created1+"> : no 2 tempfiles should be the same");
      harness.check(!created3.equals(created2),"Files <"+created3+">,<"+created2+"> : no 2 tempfiles should be the same");

    harness.checkPoint("deleting created temp files");
      harness.check(created1.delete(),"Delete existing file<"+created1+">");
      harness.check(created2.delete(),"Delete existing file<"+created1+">");
      harness.check(created3.delete(),"Delete existing file<"+created1+">");
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }


    try
    {
    harness.checkPoint("createTempFile constructor null dir");
      File created1 = File.createTempFile(FILESTUB, TEMPEND, null); //, new File(""));
      // get temp dir
      createdpath = created1.getPath();
      int pos = createdpath.lastIndexOf(File.separatorChar); //last '/' on /home/tmp/tempfile1234.tmp
      String tempdirstub = createdpath.substring(0,pos+1)+FILESTUB;        //  => /home/tmp/tempfile
      //check file properties
      checkTempFile(created1, tempdirstub, TEMPEND);

    harness.checkPoint("createTempFile constructor, no dir");
      File created2 = File.createTempFile(FILESTUB, TEMPEND); //, new File(""));
      checkTempFile(created2, tempdirstub, TEMPEND);
      harness.check(!created2.equals(created1),"Files <"+created2+">,<"+created1+"> : no 2 tempfiles should be the same");

    harness.checkPoint("createTempFile constructor,  null suffix, no dir");
      File created3 = File.createTempFile(FILESTUB, null);
      checkTempFile(created3, tempdirstub, TEMPEND);
      harness.check(!created3.equals(created1),"Files <"+created3+">,<"+created1+"> : no 2 tempfiles should be the same");
      harness.check(!created3.equals(created2),"Files <"+created3+">,<"+created2+"> : no 2 tempfiles should be the same");

    harness.checkPoint("deleting created temp files");
      harness.check(created1.delete(),"Delete existing file<"+created1+">");
      harness.check(created2.delete(),"Delete existing file<"+created1+">");
      harness.check(created3.delete(),"Delete existing file<"+created1+">");
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }



    String nonexiststub ="nonexist"+File.separator+FILESTUB;
    File nonexistdir = null;
    try
    {
      nonexistdir = new File(currentpath + File.separator + "nonexist");
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }

    harness.checkPoint("createTempFile constructor, unexisting relative dir");
    checkFileIOX(nonexiststub, TEMPEND, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");
    checkFileIOX(nonexiststub, TEMPEND, null, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");
    checkFileIOX(nonexiststub, null, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");
    checkFileIOX(nonexiststub, null, null, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");

    harness.checkPoint("createTempFile constructor, unexisting absolute dir");
    checkFileIOX(nonexiststub, TEMPEND, currentdir, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");
    checkFileIOX(nonexiststub, null, currentdir, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");
    checkFileIOX(FILESTUB, TEMPEND, nonexistdir, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");
    checkFileIOX(FILESTUB, null, nonexistdir, "temp file in nonexisting dir <tmp/nonexist/> should throw IO exception");

    harness.checkPoint("createTempFile constructor, invalid prefix");
    try
    {
      created = File.createTempFile("abcd", null, currentdir);
      harness.check(created.delete() );
      created = File.createTempFile("abc", null, currentdir);
      harness.check(created.delete() );
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }
    try
    {
      created = File.createTempFile("ab", null, currentdir);
      harness.fail("temp file fewer then 3 characters should throw illegal argument exception");
    }
    catch(IllegalArgumentException iax)
    {
     harness.check(true,"temp file fewer then 3 characters should throw illegal argument exception : " +iax.toString());
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }

  }

/****************************************************************************************************************************************/
/**
* Help function: check the temp file data: general file existance tests, start and stop substrings of file string, length of mid string
* file identifier
*/
  private void checkTempFile(File tempfile, String fullprefix, String suffix)
  {
        String filepath = tempfile.getPath();
        harness.checkPoint("Checking temp file <"+filepath+">");

        //basic physical file properties
        checkBasicExisting(tempfile ,/*isDirectory =*/false);

        // prefix and suffix
        harness.check(filepath.startsWith(fullprefix),"<"+filepath+"> should start with prefix <"+fullprefix+">");
        harness.check(filepath.endsWith(suffix)  ,"<"+filepath+"> should end with suffix <"+suffix+">");

        //identifier string
        int idlength = filepath.length() - fullprefix.length() - suffix.length();
        harness.check((idlength>=5), "temp file should have identifier part of at least 5 characters" );
  }

  private void checkFileIOX(String prefix, String suffix, File dirfile, String message)
  {
    try
    {
      File.createTempFile(prefix, suffix, dirfile);
      harness.fail(message);
    }
    catch(IOException iox)
    {
     harness.check(true,message+" : "+iox.toString());
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
    }
  }

  private void checkFileIOX(String prefix, String suffix ,String message)
  {
    try
    {
      File.createTempFile(prefix, suffix);
      harness.fail(message);
    }
    catch(IOException iox)
    {
     harness.check(true,message+" : "+iox.toString());
    }
    catch(Exception ex)
    {
     harness.fail(ex.toString());
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
* Tests the List, ListFiles and ListRoots commands on a constructed file/directory
*/
	private void testListContents(boolean nullfilter, boolean absolute)
	{
    String test ="File.list(), File.listFiles() ";
    test+=(nullfilter)?"NULL filter ":"no filters ";
    test+=(absolute)?"absolute path":"relative path";
    harness.checkPoint(test);

    String root = (absolute)?getCanonicalRoot():null;

    // build a file structure /level1/level2/level3/file31.scr....file35.scr
    buildFileStructure(root,/*level =*/3,/*fles per level*/5,/*test existance*/false); //true);	

    String currentpath = getCanonicalRoot();

    //listing files and file names of directory level2:
    // we expect five files file21 to file25 and one directory level3:
    String[] names;
    File[]   files;
    if(nullfilter)
    {
      FilenameFilter ourfilter = null;
      names = getDir(root,2).list(ourfilter);
      files = getDir(root,2).listFiles(ourfilter);
    }
    else
    {
      names = getDir(root,2).list();
      files = getDir(root,2).listFiles();
    }

    harness.checkPoint("File.list(), File.listFiles() no filters : list sizes");
    //String  rootpath = getDirPath(/*level*/2); //level1/level2/
    // we expect five files file21 to file25 and one directory level3:
    harness.check(names.length,6,"list() : we expect five files file21 to file25 and one directory level3");
    harness.check(files.length,6,"listFiles() : we expect five files file21 to file25 and one directory level3");

    File   rootfile;
    String filename;
    harness.checkPoint("File.list(), File.listFiles() no filters : files in list");
    //files file21 to file25
    for (int i=1; i<=5; i++)
    {
      rootfile = getFile(root,2,i);
      filename = FILESTUB+ "2"+i+FILEEND;

      harness.check(contains(filename, names),"checking existance of string <"+filename+">in file names list");
      harness.check(contains(rootfile, files),"checking existance of file <"+rootfile+">in files list");
    }

    // directory 'level3'
    harness.checkPoint("File.list(), File.listFiles() no filters : dir in list");
    rootfile = getDir(root,3);
    filename = DIRSTUB+"3";

    harness.check(contains(filename, names),"checking existance of "+filename+"in root list");
    harness.check(contains(rootfile, files),"checking existance of "+rootfile+"in root list");

    // delete file structure again /level1/level2/level3/file31.scr....file35.scr
    deleteFileStructure(root,/*level =*/3,/*fles per level*/5,/*test existance*/false); //true);	
	}
	
	
	
/****************************************************************************************************************************************/
/**
* Same as above, but this time apply the FilenameFilter NumberOneFilter that only lists those files that either are level<x> dirs
* or have file number one
*/
	private void testListFilteredContents(boolean absolute)
	{
    String test ="File.list(), File.listFiles() ";
    test+=(absolute)?"absolute path":"relative path";
    harness.checkPoint(test+" Specified FilenameFilter instance");

    String root = (absolute)?getCanonicalRoot():null;

    // build a file structure /level1/level2/level3/file31.scr....file35.scr
    buildFileStructure(root,/*level =*/3,/*fles per level*/5,/*test existance*/false); //true);	

    //listing files and file names of directory level2, applying filter NumberOneFilter:
    //( this fitler only passes the directories starting with 'level', and the files with filenumber 1
    // <level2> contains five files  'file21' to 'file25' and one directory 'level3':
    // we expect the filter to list only two files file21 and  directory level3:
    NumberOneFilter ourfilter = new NumberOneFilter();
    String[] names = getDir(root,2).list(ourfilter);
    File[]   files = getDir(root,2).listFiles(ourfilter);

    harness.checkPoint("File.list(), File.listFiles() no filters : list sizes");
    //String  rootpath = getDirPath(/*level*/2); //level1/level2/
    // we expect five files file21 to file25 and one directory level3:
    harness.check(names.length,2,"list() : we expect the filter to list only two files file21 and  directory level3");
    harness.check(files.length,2,"listFiles() : we expect the filter to list only two files file21 and  directory level3");

    File   rootfile;
    String filename;
    //files file22 to file25, must not be listed
    harness.checkPoint("File.list(), File.listFiles() discrete FinenameFilter : files discarted");
    for (int i=2; i<=5; i++)
    {
      rootfile = getFile(root,2,i);
      filename = FILESTUB+ "2"+i+FILEEND;

      harness.check(!contains(filename, names),"checking existance of "+filename+"in full list : suppressed by filter");
      harness.check(!contains(rootfile, files),"checking existance of "+rootfile+"in root list : suppressed by filter");
    }

    // file 'file21'
    harness.checkPoint("File.list(), File.listFiles() discrete FinenameFilter : file retained");
    rootfile = getFile(root,2,1);
    filename = FILESTUB+ "21"+FILEEND;

    harness.check(contains(filename, names),"checking existance of "+filename+"in root list");
    harness.check(contains(rootfile, files),"checking existance of "+rootfile+"in root list");

    // directory 'level3'
    harness.checkPoint("File.list(), File.listFiles() no filters : dir in list");
    rootfile = getDir(root,3);
    filename = DIRSTUB+"3";

    harness.check(contains(filename, names),"checking existance of "+filename+"in root list");
    harness.check(contains(rootfile, files),"checking existance of "+rootfile+"in root list");

    // delete file structure again /level1/level2/level3/file31.scr....file35.scr
    deleteFileStructure(root,/*level =*/3,/*fles per level*/5,/*test existance*/false); //true);	
	}

/****************************************************************************************************************************************/
/**
* quick check wether a File or File string representation is present in the current list. With the list being very small
* (5 files, 1 directory) we can just do a straightforward item-to-item scan instead of any special ordered list search routines
*/

  private boolean contains(File target, File[] items)
  {
    boolean result = false;
    for(int i=0; i<items.length && !result; i++)
      result = (target.equals(items[i]) )? true: false;
    return result;
  }

  private boolean contains(String target, String[] items)
  {
    boolean result = false;
    for(int i=0; i<items.length && !result; i++)
      result = (target.equals(items[i]) )? true: false;
    return result;
  }
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                             Some auxilliary functions for the tests above                                          **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/**
* get Canonical File and String representation of the current root (== /home/wonka)
*/
  private String getCanonicalRoot()
  {
    String root = "";
    try
    {
      File zerodir= new File("");
      root = zerodir.getCanonicalPath();    //  => /home/wonka
    }
    catch(IOException iox)
    {
     harness.fail("IO exception constructing current dir : "+iox.toString());
    }
    return root;
	}
	
	private File getCanonicalDir()
  {
    File root = null;
    try
    {
      File zerodir= new File("");
      root = zerodir.getCanonicalFile();    //  => /home/wonka
    }
    catch(IOException iox)
    {
     harness.fail("IO exception constructing current dir : "+iox.toString());
    }
    return root;
	}
	
	private File getResolvedRootFile(int fileno, boolean absolute)
  {
    File root = null;
    try
    {
      String rootstring="";
      if(absolute)
      {
        File zerodir= new File("");
        rootstring = zerodir.getCanonicalPath();    //  => /home/wonka
        rootstring += File.separator;               //  => /home/wonka/

      }
      rootstring += DIRSTUB+"1" + File.separator;   //  => level1/
      rootstring += ".." + File.separator;          //  => level/../
      rootstring += FILESTUB+"0"+fileno+FILEEND;    //  => level/../File02.scr
      root = new File(rootstring);                  //  => [/home/wonka/]level1/../File02.scr
    }
    catch(IOException iox)
    {
     harness.fail("IO exception constructing current dir : "+iox.toString());
    }
    return root;
	}
	
	private File getResolvedRootDir(String testdir, boolean absolute)
  {
    File root = null;
    try
    {
      String rootstring="";
      if(absolute)
      {
        File zerodir= new File("");
        rootstring = zerodir.getCanonicalPath();    //  => /home/wonka
        rootstring += File.separator;               //  => /home/wonka/
      }
      rootstring += DIRSTUB+"1" + File.separator;   //  => level1/
      rootstring += ".." + File.separator;  ;       //  => level1/../
      rootstring += ".." + testdir;                 //  => level1/../testdir
      root = new File(rootstring);    //  => [/home/wonka/]level1/../testdir
    }
    catch(IOException iox)
    {
     harness.fail("IO exception constructing current dir : "+iox.toString());
    }
    return root;
	}
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* tests File.listRoots FOR UNIX/LINUX SYSTEMS ONLY
* static File.listRoots() lists all the 'root directories' from the current computer and operating system. In Dos/Windows
*	this is a list of all available drives: (a:\, c:\, d:\ ....) in Unix/Linux, this returns just one single root : '/'
*
* as this class is written for the Acunia Wonka vm primarily, (working on a Linux embedded system) we will ONLY check if
* the returned list of roots is compliant to the Linux specs
*/
	private void testListRoots()
	{
  	if(File.separatorChar == '/') //linux system
  	{
  	  harness.checkPoint("testing File.listRoots() for Unix/Linux system");
  	  File[] roots = File.listRoots();
  	  //according to the listRoots() definition for Linux, the call should return only one element, namely '/'
  	  harness.check(roots.length,1,"root for linux should return only one element");
  	  harness.check(roots[0].getPath(), "/", "root for linux should return '/'" );
  	}
	}
	
	
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Helper function : check basic properties of an existing or of non-existing file/directory
*/
  public void checkBasicExisting(File testfile, boolean isdirectory)
  {
    harness.check(testfile.exists(),"newly created file must exist" );
    if(isdirectory)
    {
      harness.check(testfile.isDirectory(),"explicitly created as directory" );
      harness.check(!testfile.isFile(),"explicitly created as directory, not as file" );
    }
    else
    {
      harness.check(!testfile.isDirectory(),"explicitly created as file, not as directory" );
      harness.check(testfile.isFile(),"explicitly created as file" );
    }
    harness.check(testfile.canRead(),"created without special read/write definitions must alow read" );
    harness.check(testfile.canWrite(),"created without special read/write definitions must alow write" );
    harness.check(testfile.lastModified()>0,"lastModified() is time in milliseconds" );
    if(!isdirectory)
      harness.check(testfile.length(),0,"Newly created file has length 0");
  }

  public void checkBasicNonExisting(File testfile)
  {
    harness.check(!testfile.exists(),"Deleted file can not exist" );
    harness.check(!testfile.isDirectory(),"isDirectory() Deleted file false by definition" );
    harness.check(!testfile.isFile(),"isFile() Deleted file false by definition" );
    harness.check(!testfile.canRead(),"canRead() Deleted file false by definition" );
    harness.check(!testfile.canWrite(),"canWrite() Deleted file false by definition" );
    harness.check(testfile.lastModified(),0,"lastModified() Deleted file null by definition" );
    harness.check(testfile.length(),0,"nonexisting file has length 0 by definition");
  }





/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Helper function to find the File object and path string of a constructed test file.
* The path is of form <root/level1/level2/.../level+n/file+n+m.scr
*/

  private String getFilePath(String root, int levelno, int fileno)
  {
    String path = getDirPath(root, levelno);
    path+=FILESTUB+levelno+fileno+FILEEND;

    return path;
  }
  private String getFilePath(int levelno, int fileno) {return getFilePath(null, levelno, fileno); }

  private File getFile(String root, int levelno, int fileno, boolean create)
  {
		File testfile = null;
		try
		{
		  testfile = new File(getFilePath(root, levelno, fileno));
		  if(create)
		    testfile.createNewFile();
		}
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
    return testfile;
  }
  private File getFile(int levelno, int fileno, boolean create) {return getFile(null, levelno, fileno,create); }
  private File getFile(String root, int levelno, int fileno) {return getFile(root, levelno, fileno, false); }
  private File getFile(int levelno, int fileno) {return getFile(null, levelno, fileno, false); }
/****************************************************************************************************************************************/
/**
* Helper function to find the File object and path string of a constructed test directory.
* The path is of form <root/level1/level2/.../level+n/
*/
  private String getDirPath(String root, int levelno)
  {
    // update the root if necessary
    String path = updateRoot(root);

    //add the directories
    for(int i=1;i<=levelno;i++)
      path+=DIRSTUB+i+File.separator;
    //okay, return the path
    return path;
  }
  private String getDirPath(int levelno) {return getDirPath(null, levelno);}

  private File getDir(String root, int levelno, boolean create)
  {
		File testdir = null;
		try
		{
		  testdir = new File(getDirPath(root, levelno));
		  if(create)
		    testdir.mkdir();
		}
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
    return testdir;
  }
  private File getDir(int levelno, boolean create) {return getDir(null, levelno, create);}
  private File getDir(String root, int levelno) {return getDir(root, levelno, false);}
  private File getDir(int levelno) {return getDir(null, levelno, false);}

/****************************************************************************************************************************************/
/**
* Helper function to transform the root string either to an empty string or to a form <rootname>+separator
*/
  private String updateRoot(String root)
  {
    // update the root if necessary
    String path = "";
    if(root != null)
    {
      path = root.trim();
      if(path.equals(File.separator))
        path = "";
      else if(path.length()>0)
      {
        if(!path.endsWith(File.separator))
          path+=File.separator;
      }
    }
    return path;
  }


/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* Build a file and directory structure. The structure consists out of a number of levels, where each level consists out a base dir level<n>
* containing:
* k files "testfile<n><fileno= 0 to k>.scr"
* a higher directory "level<n+1>", again containing k files "testfile<n+1><fileno>.scr" and another directory "level<n+2>"
* construction is done recursively untill a certain end level is reached
*/
  public void buildFileStructure(int endlevel, int filesperlevel, boolean checkexists)
    {buildFileStructure(null,1,endlevel, filesperlevel, checkexists);}
  public void buildFileStructure(String root, int endlevel, int filesperlevel, boolean checkexists)
    {buildFileStructure(root,1,endlevel, filesperlevel, checkexists);}
  public void buildFileStructure(String root,int currentlevel, int endlevel, int filesperlevel, boolean checkexists)
  {
    // build the directory:
    mkdir( getDir(root, currentlevel), checkexists);

    //build the files in this directory
    for(int i=1; i<=filesperlevel; i++)
      createNewFile( getFile(root, currentlevel,i), checkexists);

    //if needed, build another directory... recursively
    if(currentlevel <endlevel)
      buildFileStructure(root, currentlevel+1, endlevel, filesperlevel, checkexists);
  }

/****************************************************************************************************************************************/
/**
* delete the file and directory structure. mentioned above (recursively)
*/
  public void deleteFileStructure(int currentlevel, int filesperlevel, boolean checkexists)
    {deleteFileStructure(null,1,currentlevel, filesperlevel, checkexists);}
  public void deleteFileStructure(String root, int currentlevel, int filesperlevel, boolean checkexists)
    {deleteFileStructure(root,1,currentlevel, filesperlevel, checkexists);}
  public void deleteFileStructure(String root,int stoplevel, int currentlevel, int filesperlevel, boolean checkexists)
  {
    //delete all the files in this directory
    for(int i=1; i<=filesperlevel; i++)
      delete( getFile(root, currentlevel,i), checkexists);

    //delete this directory
    delete(getDir(root,currentlevel), checkexists);

    //delete all underlying directories... recursively
    if(currentlevel>stoplevel)
      deleteFileStructure(root, stoplevel, currentlevel-1, filesperlevel, checkexists);
  }

/****************************************************************************************************************************************/
/**
* create, delete a file, make a directory with the option to check if the desired file or directory was already created/deleted
*/
  private void mkdir(File target, boolean checkexists)
  {
    boolean result = target.mkdir();
    if(checkexists)
      harness.check(result,"directory <"+target+"> already existing");
  }

  private void createNewFile(File target, boolean checkexists)
  {
    try
    {
      boolean result = target.createNewFile();
      if(checkexists)
        harness.check(result,"File <"+target+"> already existing");
    }
    catch(Exception e)
    {
     harness.fail(e.toString());
    }
  }

  private void delete(File target, boolean checkexists)
  {
    boolean result = target.delete();
    if(checkexists)
      harness.check(result,"File or directory <"+target+"> already deleted");
  }


/****************************************************************************************************************************************/
/****************************************************************************************************************************************/
/**                                                                                                                                    **/
/**                                             the actual tests                                                                       **/
/**                                                                                                                                    **/
/****************************************************************************************************************************************/
/****************************************************************************************************************************************/

/****************************************************************************************************************************************/
/**
* The main tests:
* Next to calling the differnet test functions, we also write a number of File.deleteOnExit tests. As the tests needs to be
* performed both just before and after all the other test, it is best to write it right into this routine.
*/
  public void test(TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.io.File");
		
		// delete on exit test first part right before the other tests:
		File onexittest = null;
		harness.checkPoint("deleteonexit: building and marking file");
		try
		{
		  onexittest = new File("existswhilerunning.txt");		
  		harness.check(!onexittest.exists(),"should be deleted by last time program was running");
      harness.check(onexittest.createNewFile(),"Creating non-existing file");
      onexittest.deleteOnExit();
  		harness.check(onexittest.exists(),"marked for deletion, but not yet deleted");
		}
    catch(Exception e)
    {
     harness.fail(e.toString());
    }

		// the different file tests
		testClass();                                                  // Class.getClass, forName(), instanceOf...
		testConstruction(/*absolute*/true);                           //test file and directory construction with the standard, absolute path
		testConstruction(/*absolute*/false);                          //test file and directory construction with the standard, relative path
		testCanonicalConstruction();                                  //File object with same cannoical path refer to same physical file
		testDeleting(/*absolute*/true);                               // Deleting files, empty subdirs, non-empty subdirs(absolute path)
		testDeleting(/*absolute*/false);                              // Deleting files, empty subdirs, non-empty subdirs(relative path)
		testRenameFile(/*absolute*/true);                             // renaming files(absolute path)
		testRenameFile(/*absolute*/false);                            // renaming files(relative path)
		testCreateTempFile();                                         // creating files using CreateTempFile()
		testListContents(/*hasnullfilter*/false,/*absolute*/true);    // list() and ListFiles()              (absolute path)
		testListContents(/*hasnullfilter*/false,/*absolute*/false);   // list() and ListFiles()              (relative path)
		testListContents(/*hasnullfilter*/true,/*absolute*/true);     // list/ListFiles(filter = null) (absolute path)
		testListContents(/*hasnullfilter*/true,/*absolute*/false);    // list/ListFiles(filter = null) (relative path)
		testListFilteredContents(/*absolute*/true);                   // list/ListFiles(specific filter) (absolute path)
		testListFilteredContents(/*absolute*/false);                  // list/ListFiles(specific filter) (relative path)
		testListRoots();             // in Linux systems, listRoots() should return one root: "/"
		
		
		// delete on exit test last part, just before closing down:
		harness.checkPoint("deleteonexit: last check on marked file");
		harness.check(onexittest.exists(),"marked for deletion, but not yet deleted");
		//when program exits now, file <existswhilerunning.txt >should be deleted
		testListFilteredContents(/*absolute*/true);   // list(filter) and ListFiles(filter) for given FilenameFilter
	
	}
}
