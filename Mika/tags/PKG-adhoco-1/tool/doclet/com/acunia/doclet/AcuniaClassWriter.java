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


/*
 * @(#)AcuniaClassWriter
 *
 */

package com.acunia.doclet;

import com.sun.tools.doclets.standard.*;
import com.sun.tools.doclets.*;
import com.sun.javadoc.*;
import java.io.*;
import java.lang.*;
import java.util.*;

/**
 *
 */
public class AcuniaClassWriter extends ClassWriter {

    public AcuniaClassWriter(String path, String filename, ClassDoc classdoc,
                ClassDoc prev, ClassDoc next, ClassTree classtree,
                boolean nopackage) throws IOException, DocletAbortException {

	super(path, filename, classdoc, prev, next, classtree, nopackage);
        methodSubWriter = new AcuniaMethodSubWriter(this);
	constrSubWriter = new AcuniaConstructorSubWriter(this);
	fieldSubWriter = new AcuniaFieldSubWriter(this);
	try {
		FileWriter fw = new FileWriter("constructors.info",true);
		fw.write(classdoc.modifiers()+" ");
		if(!classdoc.isInterface()){
			fw.write("class "); 
		}
		fw.write(classdoc.qualifiedName()+'\n');
		fw.flush();
		fw.close();

	}catch(IOException ieo){}
    }

    public static void generate(ClassDoc classdoc, ClassDoc prev,
                             ClassDoc next, ClassTree classtree, 
                             boolean nopackage) throws DocletAbortException {
            ClassWriter clsgen;
            String path = 
               DirectoryManager.getDirectoryPath(classdoc.containingPackage());
            String filename = classdoc.name() + ".html";
            try {
                clsgen = new AcuniaClassWriter(path, filename, classdoc,
                                         prev, next, classtree, nopackage);
                clsgen.generateClassFile();
                clsgen.close();
            } catch (IOException exc) {
                Standard.configuration().standardmessage.
                    error("doclet.exception_encountered",
                           exc.toString(), filename);
                throw new DocletAbortException();
            }
    }
}


