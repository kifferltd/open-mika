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


package com.acunia.doclet;

import com.sun.tools.doclets.standard.*;
import java.util.*;
import com.sun.tools.doclets.*;
import com.sun.javadoc.*;
import java.io.*;

public class AcuniaConstructorSubWriter extends ConstructorSubWriter {

	public AcuniaConstructorSubWriter(SubWriterHolderWriter w){
	  super(w);
	}

	protected void printTags(ProgramElementDoc member) {
	  super.printTags(member);
	  try{
		FileWriter fw = new FileWriter("constructors.info",true);
            	if (member.isProtected()) {
                	fw.write("   protected ");
            	} else if (member.isPrivate()) {
                	fw.write("   private ");
            	} else if (member.isPublic()) {
                	fw.write("   public ");
            	} else {
                	fw.write("   Package_private ");
            	}
		fw.write(member.qualifiedName());
		fw.write(((ConstructorDoc)member).signature()+'\n');
		fw.flush();
		fw.close();
	  } catch(IOException ioe){}
          ConstructorDoc constr = (ConstructorDoc)member;
          writer.dl();
      	  writer.dt();
      	  writer.boldText("doclet.result");
      	  writer.dd();
	  String name = constr.qualifiedName()+"."+constr.name();
	  //System.out.println("using :"+name);
   	  writer.print(AcuniaMethodSubWriter.FailSearch(name, constr.signature(), "no returntype"));
      	  writer.ddEnd();
      	  writer.dt();
      	  writer.boldText("doclet.status");
      	  writer.dd();
          Tag t1[] = constr.tags("status");
      	  if (t1.length > 0) {
        	writer.print(t1[0].text());
          }
          else {
        	writer.print("implemented");
      	  }
      	  writer.ddEnd();
      	  writer.dt();
      	  writer.boldText("doclet.remark");
      	  writer.dd();

          Tag t2[] = constr.tags("remark");
          if (t2.length > 0) {
        	writer.print(t2[0].text());
          }
      	  else {
        	writer.print("compliant to specification");
          }
          writer.ddEnd();
      	  writer.dlEnd();
        }
}