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


package gnu.testlet.wonka.io.PrintWriter; //complete the package name ...

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
*  this file contains a help class used for the java.io.OutputStream test <br>
*  we have this help-class which extends OutputStream since <br>
*  OutputStream is an abstract class
*/
public class SMExPrintWriter extends PrintWriter
{
        private boolean overwrite;

        public  SMExPrintWriter(Writer wr,int i) {
        	super(wr);
        	overwrite=true;
	}
	
        public  SMExPrintWriter(Writer wr) {
        	super(wr);
        	overwrite=false;
	}
        public  SMExPrintWriter(Writer wr,boolean f) {
        	super(wr,f);
        	overwrite=false;
	}
        public  SMExPrintWriter(OutputStream wr) {
        	super(wr);
        	overwrite=false;
	}
        public  SMExPrintWriter(OutputStream wr,boolean f) {
        	super(wr,f);
        	overwrite=false;
	}
	public Object getLock() {
		return lock;
	}
	public Writer getOut() {
		return out;
	}
	
	public void setError() {
		super.setError();
	}
	public void println() {
	 	try {if (overwrite) out.write("#new separator#");}
	 	catch (Exception e){}
	 	super.println();
	}
	
}

