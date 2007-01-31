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
 * @(#)AcuniaStandard.java
 *
 */
package com.acunia.doclet;

import com.sun.tools.doclets.standard.*;
import com.sun.tools.doclets.*;
import com.sun.javadoc.*;
import java.util.*;
import java.io.*;

/**
 * The class with "start" method, calls individual Writers.
 * this class should be passed to javadoc using the -doclet commandline option
 *
 * @author Gerrit Ruelens
 * @author Dries Buytaert
 */
public class AcuniaDoclet extends com.sun.tools.doclets.standard.Standard {

    static List FailResultList,PassResultList;

    /**
     * The "start" method as required by Javadoc.
     *
     * we will look for fail.file and tested.file.  those files will be parsed so
     * the acunia testResult tag can be added
     *
     * @param Root
     * @return boolean
     */
    public static boolean start(RootDoc root) throws IOException {
        try { 
            System.out.println("**** Parsing the results ***");
            FailResultParser FailParser = new FailResultParser("fail.file");
            FailResultParser PassParser = new FailResultParser("tested.file");
       	    FailResultList = FailParser.getResultList();
            System.out.println("*** Putting "+FailResultList.size() +" results in testResultList ***");
       	    PassResultList = PassParser.getResultList();
       	    System.out.println("*** Putting "+PassResultList.size() +" results in testResultList ***");
            System.out.println("**** Setting Acunia Configuration ****");
            HtmlDocWriter.configuration = new AcuniaConfiguration();
            configuration().setOptions(root);
            (new AcuniaDoclet()).startGeneration(root);
            FileWriter fw = new FileWriter("fail.left");
            Iterator e = FailResultList.iterator();
            while (e.hasNext()){
                fw.write(e.next()+"\n");              	
            }
            fw.flush();
            fw.close();
            fw = new FileWriter("tested.left");
            e = PassResultList.iterator();
            while (e.hasNext()){
                fw.write(e.next()+"\n");              	
            }
            fw.flush();
            fw.close();
        } catch (DocletAbortException exc) {
	    exc.printStackTrace();
            return false; // message has already been displayed
        }
        return true;
    }

    public static List getFailResults() {
      return FailResultList;
    }

    public static List getPassResults() {
      return PassResultList;
    }

    /**
     * Instantiate an AcuniaClassWriter for each Class within the ClassDoc[]
     * passed to it and generate Documentation for that.
     */
    protected void generateClassCycle(ClassDoc[] arr, ClassTree classtree,
                            boolean nopackage) throws DocletAbortException {
        Arrays.sort(arr);
        for(int i = 0; i < arr.length; i++) {
            if (configuration().nodeprecated && 
                     arr[i].tags("deprecated").length > 0) {
                continue;
            }
            ClassDoc prev = (i == 0)? 
                            null:
                            arr[i-1];
            ClassDoc curr = arr[i];
            ClassDoc next = (i+1 == arr.length)? 
                            null:
                            arr[i+1];

            AcuniaClassWriter.generate(curr, prev, next, classtree, nopackage);
        }
    }

}
        

