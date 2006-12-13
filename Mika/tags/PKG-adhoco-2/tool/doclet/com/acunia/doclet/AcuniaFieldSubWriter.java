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

public class AcuniaFieldSubWriter extends FieldSubWriter {

	public AcuniaFieldSubWriter(SubWriterHolderWriter w){
	  super(w);
	}

	protected void printTags(ProgramElementDoc member) {
	  super.printTags(member);
          FieldDoc field = (FieldDoc)member;
	  String name = field.qualifiedName();
	  String type = field.type().qualifiedTypeName()+field.type().dimension();
	  String mod ="()";
	  if(field.isPublic()){
	   	mod = "(public)";
	  }else if (field.isProtected()){
	   	mod = "(protected)";
	  }else if (field.isPrivate()){
	   	mod = "(private)";
	  }
	  name = AcuniaMethodSubWriter.FailSearch(name, mod, type);
          if (!name.startsWith("no tests")){
          	writer.dl();
      	  	writer.dt();
      	  	writer.boldText("doclet.result");
      	  	writer.dd();
	
   	  	writer.print(name);
      	  	writer.ddEnd();
      	  	writer.dlEnd();
      	  }
        }
}