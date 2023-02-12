// Copyright (c) 1998, 1999  Cygnus Solutions
// Written by Tom Tromey <tromey@cygnus.com>

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.

// KNOWN BUGS:
//   - should look for /*{ ... }*/ and treat contents as expected
//     output of test.  In this case we should redirect System.out
//     to a temp file we create.

package gnu.testlet;
import java.io.*;
import java.security.*;

public class TestRunner
    extends TestHarness 
    //implements gnu.testlet.config
{

  static String testclassfile;
  private int count = 0;
  private int failures = 0;
  private int total = 0;
  private int loop = 0;
  private boolean verbose = false;
  private boolean debug = false;
  private String description;
  private String classname;  
  private String current_checkitem;
  private FileWriter outfile = null;

  static int loops = 0;

  static {
    String testclassfilename = System.getProperty("gnu.testlet.script.name", "wonkatest");
    testclassfile = "/resource/" + testclassfilename + ".properties";
  }

/************************************************************
* Constructor
*/
  protected TestRunner (boolean verbose, boolean debug, int loop)
  {
    this.verbose = verbose;
    this.debug = debug;
    this.loop = loop;
  }

  public TestRunner(){}


/************************************************************
* run test and end report jobs
*/
  protected void decodeLine(String line)
  {
     line = line.trim();
    if(line.length() == 0)
       printLine(line);
    else if(line.startsWith("//"))
      printLine("   ( " + line.substring(2) + " )");
    else if(line.startsWith("#"))
      printLine("    => " + line.substring(1) );
    else if((line.toLowerCase()).startsWith("def") )
    {
      // search definition options
      line = (line.substring(3)).trim();    
       if( line.equalsIgnoreCase("debug") )
        setDebug(true);
      else if (line.equalsIgnoreCase("nodebug") )
        setDebug(false);
      else if (line.equalsIgnoreCase("verbose") )
        setVerbose(true);
      else if (line.equalsIgnoreCase("noverbose") )
        setVerbose(false);
      else if ((line.toLowerCase()).startsWith("file") )
      {
        line = (line.substring(4)).trim();
        setOutputFile(line);
      }
      else if (line.equalsIgnoreCase("nofile") )
        closeOutputFile();
     }
     else
    {
      verbose("----");
      verbose(line);
    runtest (line);
    }
  }    

  protected void runtest (String name)
  {
    // Try to ensure we start off with a reasonably clean slate.
    Runtime rt = Runtime.getRuntime();
    long free = rt.freeMemory();
    System.gc();
    System.runFinalization();
    long tot = rt.totalMemory();
    debug("TESTRUNNER -- MEMORY STATUS : "+(tot - rt.freeMemory())+" used of "+tot+" before starting '"+name
              +"' (was before gc "+(tot-free)+")");
    checkPoint (null);

    Testlet t = null;
    try
    {
      // string to class
      Class k = Class.forName (name);
      System.out.println("TestRunner.runtest()"+k);
      // class name to object
      Object o = k.newInstance();
      //object to testlet
      if (o instanceof Testlet)
        t = (Testlet)o;
    }
    catch (Throwable ex)
    {
      //print <FAIL> message
      printLine( "FAIL: uncaught exception loading " + name);
      verbose("error : " + ex.toString());
      //print error debug
      debug (ex);
      //if(debug){
        ex.printStackTrace();
      //}

      // one more test done and failed
      ++failures;
      ++total;
    }

    if (t != null)  // name string refers to a valid testlet all right
    {
      description = name;
      try
       {
         t.test (this);
       }
      catch (Throwable ex)
       {
         printLine("FAIL $$"+classname+"$$"+current_checkitem+"$$uncaught '"+ex+"' while running '"+name+"'$$");
         verbose( "error : " + ex.toString());
        //print error debug
        debug (ex);
        if(debug){
          ex.printStackTrace();
        }
    // one more test done and failed
         ++failures;
         ++total;
       }
    }
  }

  protected int done ()
  {
    printLine(failures + " of " + total + " tests failed" + (loops > 0 ? "during pass " + (loop + 1) + " of " + loops : ""));
    if (outfile != null)
      closeOutputFile();
    return failures > 0 ? 1 : 0;
  }


/************************************************************
* Coding the check and start checking functions functions
* defined in the TestHarness base class and used by the different testlets
*/
  public void check (boolean check_succeeded)
  {
  check(check_succeeded,"no Message");

  }

  public void check (boolean check_succeeded, String message)
  {
    if (! check_succeeded) // check fail
    {
      printLine(getDescription ("FAIL $$"+classname+"$$"+current_checkitem+"$$"+message+"$$"));  
      failures++;
    }
    else
      verbose(getDescription ("PASS $$"+classname+"$$"+current_checkitem+"$$"+message+"$$"));

     count++;
     total++;
  }

  public void checkPoint (String checkitemname)
    {
      current_checkitem = checkitemname;
      count = 0;
    }


  public void setclass (String cname)
  {
    classname = cname;
  }
  
/************************************************************
* Getters
*/

public String getTestClass() {
  return classname;
}

public String getCheckPoint() {
  return current_checkitem;
}

public int getTestsTotal() {
  return total;
}

public int getTestsFailed() {
  return failures;
}


/************************************************************
* options and files setting functions
*/
  public void setDebug(boolean state)
  {
    debug = state;
    if (state)
      printLine("***  set Debug  ***");
    else
      printLine("***  Debug off  ***");
  }

  public void setVerbose(boolean state)
  {
    verbose = state;
    if (state)
      printLine("*** set Verbose ***");
    else
      printLine("*** verbose off ***");
  }

  public void setOutputFile(String name)
  {
     // The following code assumes File.separator is a single character.
/*     if (File.separator.length () > 1)
      throw new Error ("File.separator length is greater than 1");

     String realName = name.replace ('#', File.separator.charAt (0));
     //realName = getSourceDirectory () + File.separator  + realName;
*/    try
    {
      if (loops > 0) name = name + "." + (loop+1);
      //new output file
      printLine("*** Write output to <" + name + "> ***");
       outfile = new FileWriter(name);
      // position writer at end of file
      //outfile.seek(outfile.length() );
    }
    catch(IOException e)
    {
      e.printStackTrace();
      System.err.println(e);
      outfile = null;
    }
  }

  public void closeOutputFile()
  {
    if(outfile == null)
        printLine("*** no output file to close ***");
    else
    {
      try
      {
         outfile.close();
         outfile = null;
        printLine("*** output file closed ***");
      }
      catch(IOException e)
      {
        System.err.println(e);
      }
      }      
 }

  public InputStream getResourceStream (String name)
    throws ResourceNotFoundException
  {
     System.out.println("calling TestRunner.getResourceStream(" + name +")");
     // The following code assumes File.separator is a single character.
     if (File.separator.length () > 1)
      throw new Error ("File.separator length is greater than 1");

     String realName = name.replace ('#', File.separator.charAt (0));

     InputStream s = getClass().getClassLoader().getResourceAsStream(name); 

     if (s == null) throw new ResourceNotFoundException();

     return s;
   }

  public File getResourceFile(String name) throws ResourceNotFoundException
  {
    // The following code assumes File.separator is a single character.
    if (File.separator.length() > 1)
      throw new Error("File.separator length is greater than 1");
    String realName = name.replace('#', File.separator.charAt(0));
    File f = new File(getSourceDirectory() + File.separator + realName);
    if (!f.exists())
      {
        throw new ResourceNotFoundException("cannot find mauve resource file"
                                            + ": " + getSourceDirectory()
                                            + File.separator + realName);
      }
    return f;
  }


  public Reader getResourceReader (InputStream istream)
    throws ResourceNotFoundException
  {
      return ( new BufferedReader(new InputStreamReader(istream) ) );
  }

  public Reader getResourceReader (String name)
    throws ResourceNotFoundException
  {
      return ( new BufferedReader(new InputStreamReader(getResourceStream(name) ) ) );
  }

   
/************************************************************
* Verbose and Debug definitions and functions
*/
  public void printSingle(String message)
  {
     // write to screen
     System.out.print(message);  
      
     //mirror to output file
     if(outfile != null) //exists
    {
     try
       {
         outfile.write(message);
        }
       catch(IOException e)
       {
          System.err.println(e);
       }
     }
  }

  public void printLine(String message)
  {
     // write to screen
     System.out.println(message);  
      
     //mirror to output file
     if(outfile != null) //exists
     {
       try
       {
         outfile.write(message + "\n");
        }
       catch(IOException e)
       {
          System.err.println(e);
       }
     }
  }

  public void printLine(String message, boolean write_always)
  {
    if(write_always || verbose)
     printLine(message);
  }


  public void verbose(String message)
  {
    if(verbose)
      printLine(message);  
  }

  public void debug (String message)
  {
    if (debug)
      printLine(message);
  }

  public void debug (String message, boolean newline)
  {
    if (debug && newline)
       printLine (message);
    else if(debug)
      printSingle(message);
  }

  public void debug (Throwable ex)
  {
    if (debug)
    {
      printLine(ex.getMessage());
      printLine(ex.toString());
      //ex.printStackTrace(System.out);
    }
  }

  public void debug(Object[] o, String desc)
  {
    debug("Dumping Object Array: " + desc);
    if (o == null)
    {
      debug("null");
      return;
    }

    for (int i = 0; i < o.length; i++)
      if (o[i] instanceof Object[])
        debug((Object[])o[i], desc + " element " + i);
      else
        debug("  Element " + i + ": " + o[i]);
  }


  private final String getDescription (String pf)
  {
    String completedescription = pf + ": " + description;
    if(current_checkitem != null)
      completedescription +=": " + current_checkitem;
    completedescription += " (number " + (count + 1) + ")";

    return completedescription;
  }

/************************************************************
* filling in some functions from the TestHarness base class
*/


  public String getSourceDirectory ()
  {
    return "./";
  }

  public String getTempDirectory ()
  {
    final File tempdir = new File("/tmp/mauve");
    AccessController.doPrivileged(new PrivilegedAction() {
      public Object run() {
        tempdir.mkdir();
        return null; // nothing to return
      }
    });
    return "/tmp/mauve/";
  }


 /************************************************************
* MAIN
*/
  public static void runTests(TestRunner runner, String testfile){
    BufferedReader r = null;
    try
    {
      r   = (BufferedReader)runner.getResourceReader(testfile);
    }
     catch (ResourceNotFoundException ex)
    {
       runner.printLine(ex.getLocalizedMessage ());
       return;
    }

// for all of the lines: read the class and run the class tests:
// first line:
    String cname = null;
    String cname_always;
    boolean notflaggedoff = true;
    int flaggedoffstart;
    int flaggedoffstop;
    try
    {
      cname = r.readLine ();
      runner.verbose(cname);
    }
    catch (IOException iox)
    {
       System.err.println(iox.getLocalizedMessage ());
    }

// now untill end of file:
    while (cname != null)
    {
      try
      {
        flaggedoffstart = cname.indexOf("/*");
        flaggedoffstop  = cname.indexOf("*/");
        cname_always="";
System.out.println("cname = " + cname);
System.out.println("flaggedoffstart = " + flaggedoffstart + ", flaggedoffstop = " + flaggedoffstop + ", notflaggedoff = " + notflaggedoff);

        while(flaggedoffstart>=0 || flaggedoffstop >=0)
        {
          if(flaggedoffstart>=0 && flaggedoffstop >=0)
          {
            if(flaggedoffstart < flaggedoffstop && notflaggedoff)
              cname = cname.substring(0,flaggedoffstart) + cname.substring(flaggedoffstop +2);
            else if (flaggedoffstart > flaggedoffstop && notflaggedoff == false)
            {
              cname_always = cname_always+ cname.substring(flaggedoffstop+2,flaggedoffstart);
              cname = cname.substring(flaggedoffstart+2);
            }
            else if(flaggedoffstart < flaggedoffstop && notflaggedoff == false)
            {
              cname = cname.substring(flaggedoffstop +2);
              notflaggedoff = true;
            }
            else if (flaggedoffstart > flaggedoffstop && notflaggedoff)
            {
              cname_always = cname_always+ cname.substring(flaggedoffstop+2,flaggedoffstart);
              cname = cname.substring(flaggedoffstart+2);
              notflaggedoff = false;
            }
          }
          else if (flaggedoffstart>=0)
          {
            if(notflaggedoff)
              cname_always = cname_always+ cname.substring(0,flaggedoffstart);
            cname = cname.substring(flaggedoffstart+2);
            notflaggedoff= false;
          }
          else if (flaggedoffstop >=0)
          {
            cname = cname.substring(flaggedoffstop +2);
            notflaggedoff = true;
          }

          flaggedoffstart = cname.indexOf("/*");
          flaggedoffstop  = cname.indexOf("*/");
        }

        if(notflaggedoff)
          runner.decodeLine(cname_always + cname);
        else if(cname_always.length() >0)
          runner.decodeLine(cname_always);
        
        // new line (if exists)
        cname = r.readLine ();
      }
      catch (IOException iox)
      {
         System.err.println(iox.getLocalizedMessage ());
      }
    }

  }


  public static void main (String[] args)
  {
    boolean verbose = false;
    boolean debug = false;
    String outfile = null;
    int i;

    for (i = 0; i < args.length; i++)
    {
      if (args[i].equals("-verbose")){
        verbose = true;
      }
      else if (args[i].equals("-debug")){
        debug = true;
      }
      else {
        try {
          int temp = Integer.parseInt(args[i]);
          if (loops < 0) {
            System.err.println("*** negative number of passes, ignoring ***");
          }
          else {
            loops = temp;
          }
        }
        catch(NumberFormatException nfe){
          testclassfile = args[i];
        }
      }
    }

   TestRunner runner = null;
    // build instance of our test harness  (this)
    for(int j = 0 ; j < (loops == 0 ? 1 : loops) ; j++){

      runner  = new TestRunner (verbose, debug, j);

       /*this part is a change from the original code *************/
      runTests(runner, testclassfile);
      // construct a buffered file reader, taking in all the files to check:
      //BufferedReader r   = new BufferedReader (new InputStreamReader (System.in));
      /*changes end here *****************************************/
      // print conclusions:
      runner.done();
      System.out.println("\n");
    }
    System.exit(0);
  }
}



