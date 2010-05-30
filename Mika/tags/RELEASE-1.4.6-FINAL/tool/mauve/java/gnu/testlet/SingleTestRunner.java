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

public class SingleTestRunner
    extends TestHarness 
    //implements gnu.testlet.config
{

//definitions
  final static String TESTCLASSFILE = new String("wonkatests");
//variables:
  private int count = 0;
  private int failures = 0;
  private int total = 0;
  private boolean verbose = false;
  private boolean debug = false;
  private String classname;
  private String description;
  private String current_checkitem;
//  private RandomAccessFile outfile = null;



/************************************************************
* Constructor
*/
  protected SingleTestRunner (boolean verbose, boolean debug)
  {
    this.verbose = verbose;
    this.debug = debug;
  }




/************************************************************
* run test and end report jobs
*/

  protected void runtest (String name)
  {
    // Try to ensure we start off with a reasonably clean slate.
    System.gc();
    System.runFinalization();

    checkPoint (null);

    Testlet t = null;
    try
  	{
  	  // string to class
  	  verbose ("... Loading Class <"+ name+">");
  	  Class k = Class.forName (name);
  	  if(k == null)
  	  {
    	    debug("class <"+ name+"> could not be loaded");
  	  }
  	  else
  	  {
    	  verbose ("... Constructing instance <"+ name+">");
        // class name to object
        Object o = k.newInstance();
        if(o == null)
        {
    	    debug("instance <"+ name+"> could not be built");
        }
        else if (!(o instanceof Testlet))
    	    debug("Not of type testlet");
    	  else
    	  {
    	  	t = (Testlet)o;
      	  verbose ("... Running testlet<"+ name+"> : ");
    	  }
  	  }
  	}
    catch (Throwable ex)
  	{
  	  //print <FAIL> message
  	  printLine( "FAIL: uncaught exception loading " + name);
  	  verbose("error : " + ex.toString());
  	  //print error debug
  	  debug (ex);
  	  // one more test done and failed
  	  ++failures;
  	  ++total;
  	}

    if (t != null)  // name string refers to a valid testlet all right
  	{
  	  description = name;
  	  verbose("testing "+ description);
  	  try
 	    {
 	      t.test (this);
 	    }
  	  catch (Throwable ex)
 	    {
 	    	if (current_checkitem != null)
 	    		printLine("FAIL: " + name + ": uncaught exception at  < " + current_checkitem + " >  number " + (count + 1));
 	    	else
 	    		printLine("FAIL: " + name + ": uncaught exception at check number " + (count + 1));
 	    		
 	      verbose( "error : " + ex.toString());
    	  //print error debug
    	  debug (ex);
    	  // one more test done and failed
 	      ++failures;
 	      ++total;
 	    }
  	}
  }

  protected int done ()
  {
    printLine(failures + " of " + total + " tests failed");
//    if (outfile != null)
  //  	closeOutputFile();
    return failures > 0 ? 1 : 0;
  }


/************************************************************
* Coding the check and start checking functions functions
* defined in the TestHarness base class and used by the different testlets
*/
  public void check (boolean check_succeeded)
  {
/*    if (! check_succeeded) // check fail
  	{
  	  printLine(getDescription ("FAIL"));
  	  failures++;
  	}
  	else
  		verbose(getDescription ("PASS"));

   	count++;
   	total++;
*/
	check(check_succeeded, "no message");
	}

  public void check (boolean check_succeeded, String message)
  {
    if (! check_succeeded) // check fail
  	{
  	  printLine(getDescription ("FAIL $$"+classname+"$$"+current_checkitem+"$$"+message+"$$"));
  	  failures++;
  	}
  	else
  		verbose(getDescription ("PASS "+message));

   	count++;
   	total++;
	}

  public void checkPoint (String checkitemname)
    {
      current_checkitem = checkitemname;
      count = 0;
  		verbose("");
  		verbose("      <"+checkitemname+">");
    }
//added code

	public void setclass (String cname)
	{
		classname = cname;
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
/*   	// The following code assumes File.separator is a single character.
   	if (File.separator.length () > 1)
			throw new Error ("File.separator length is greater than 1");

   	String realName = name.replace ('#', File.separator.charAt (0));
   	//realName = getSourceDirectory () + File.separator	+ realName;
		try
		{
    	
    	//new output file
    	printLine("*** Write output to <" + realName + "> ***");
    	outfile = new RandomAccessFile(realName , "rw");
    	// position writer at end of file
    	outfile.seek(outfile.length() );
    	printLine("*** output file opened ***");
  	}
  	catch(IOException e)
  	{
  		System.err.println(e);
  		outfile = null;
  	}
*/
  }

  public void closeOutputFile()
  {
/*		if(outfile == null)
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
  */
  }

	public InputStream getResourceStream (String name)
    throws ResourceNotFoundException
	{
   	// The following code assumes File.separator is a single character.
   	if (File.separator.length () > 1)
			throw new Error ("File.separator length is greater than 1");

   	String realName = name.replace ('#', File.separator.charAt (0));

        InputStream s = ClassLoader.getSystemResourceAsStream(name);

        if (s == null) throw new ResourceNotFoundException();

     	return s;
/* Was:
   	try
    {
     	return
       	new FileInputStream (realName );
//       	new FileInputStream (getSourceDirectory () + File.separator	+ realName );
    }
   	catch (FileNotFoundException ex)
    {
     	throw new ResourceNotFoundException (ex.getLocalizedMessage ());
    }
*/
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
/*    	
   	//mirror to output file
   	if(outfile != null) //exists
   	{
   		try
   		{
   			outfile.writeBytes(message);
    		}
   		catch(IOException e)
   		{
   		 	System.err.println(e);
   		}
   	}
*/
  }

  public void printLine(String message)
  {
   	// write to screen
   	System.out.println(message);	
/*    	
   	//mirror to output file
   	if(outfile != null) //exists
   	{
   		try
   		{
   			outfile.writeBytes(message + "\n");
    		}
   		catch(IOException e)
   		{
   		 	System.err.println(e);
   		}
   	}
*/
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
			printLine("Exception <" + ex.getMessage() + "> : " + ex.toString());
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
    new File("/tmp/mauve").mkdirs();
    return "/tmp/mauve/";
  }


 /************************************************************
* MAIN
*/

  /************************************************************
* MAIN
*/
  public static void main (String[] args)
  {
    boolean verbose = false;
    boolean debug = false;
    boolean immediately = false;
    String testfile = null;
    String outfile = "TestHarnessOut.scr";
    int i;


    for (i = 0; i < args.length; i++)
  	{
  	  if (args[i].equalsIgnoreCase("-v"))
  	    verbose = true;
  	  else if (args[i].equalsIgnoreCase("-d"))
  	    debug = true;
  	  else if (args[i].equalsIgnoreCase("-o"))
  	  {
  	    if(i< (args.length-1))
  	    {
  	      outfile = args[i+1];
  	      i++;
  	    }
  	  }
   	  else
  	    testfile = args[i];
    }
  		

  	// build instance of our test harness  (this)
    SingleTestRunner runner	= new SingleTestRunner (verbose, debug);
    runner.setOutputFile(outfile);
    runner.runtest(testfile);
    runner.done();
  }

  public java.io.File getResourceFile(String name) throws gnu.testlet.ResourceNotFoundException {
   throw new gnu.testlet.ResourceNotFoundException();
  }
}


