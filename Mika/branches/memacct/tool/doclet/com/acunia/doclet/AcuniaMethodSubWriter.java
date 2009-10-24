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
 * @(#)AcuniaMethodSubWriter.java	
 *
 */
package com.acunia.doclet;

import com.sun.tools.doclets.standard.*;
import java.util.*;
import com.sun.tools.doclets.*;
import com.sun.javadoc.*;

/**
 *
 * @author Gerrit Ruelens
 * @author Dries Buytaert
 */
public class AcuniaMethodSubWriter extends MethodSubWriter {

    protected AcuniaMethodSubWriter(SubWriterHolderWriter writer) {
        super(writer);
    }

    protected void printTags(ProgramElementDoc member) {
      super.printTags(member);
      MethodDoc method = (MethodDoc)member;

      writer.dl();
      writer.dt();
      writer.boldText("doclet.result");
      writer.dd();
      writer.print(FailSearch(method.qualifiedName(), method.signature(), method.returnType().toString()));
      writer.ddEnd();
      writer.dt();
      writer.boldText("doclet.status");
      writer.dd();
      Tag t1[] = method.tags("status");
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

      Tag t2[] = method.tags("remark");
      if (t2.length > 0) {
        writer.print(t2[0].text());
      }
      else {
        writer.print("compliant to specification");
      }
      writer.ddEnd();
      writer.dlEnd();
    }

    public static synchronized String FailSearch(String qn,String signature,String returnType) {
      List fList = AcuniaDoclet.getFailResults();
      List pList = AcuniaDoclet.getPassResults();
      Iterator enum = fList.iterator();
      while(enum.hasNext())
	{
        SingleResult res = (SingleResult) enum.next();
        String qfn = res.getClazz() +"."+res.getMethod();
        //System.out.println("*** Comparing "+qfn+" with "+qn);
        if (qfn.equals(qn))
        	{
          	//System.out.println("*** We might have a Winner here ... "+qfn);
          	//System.out.println("*** Comparing "+res.getReturnValue()+" with "+returnType);
          	if (!(res.getReturnValue().equals(returnType)))
          		{
    		      	//System.out.println("wrong returntype");
            		continue;
          		}
          	List paramlist = res.getParams();
          	if (paramlist!=null) {
            	Iterator e=paramlist.iterator();
            	String paramString1="(";
            	String paramString2="(";
            	String hlp=null ;
            	while(e.hasNext())
            		{
            		hlp = (String) e.next();
              		paramString1=paramString1+hlp;
              		paramString2=paramString2+hlp;
              		if (e.hasNext())
              			{
                		paramString1=paramString1+", ";
                		paramString2=paramString2+",";
              			}
            		}
            	paramString1=paramString1+")";
            	paramString2=paramString2+")";
            	//System.out.println("*** Comparing "+paramString1+" with "+signature);
            	if (!(paramString1.equals(signature) || paramString2.equals(signature)))
            		{
	          	//System.out.println("wrong parametertypes");
          		continue;
            		}

          	}
          	else{
          	    //System.out.println("null paramList encountered");
          	    if (!"()".equals(signature)) {
          	     	continue;
          	    }
          	}
          	//System.out.println("Returning "+res.getTestMessage());
          	while(fList.remove(res)){};
          	while(pList.remove(res)){};
          	return "<B><FONT COLOR=\"red\">FAIL:</FONT></B> "+res.getTestMessage();
        	}
	else
		{
          	continue;
        	}
      	}
      //System.out.println("method didn't fail test :"+qn+signature+returnType);
      return PassSearch(qn,signature, returnType);
    }
/**
*  PassSearch
*
*  Looks if the method is tested
*/
    public static synchronized String PassSearch(String qn,String signature,String returnType) {
      List pList = AcuniaDoclet.getPassResults();
      Iterator enum = pList.iterator();
      while(enum.hasNext())
      {
        SingleResult res = (SingleResult) enum.next();
        String qfn = res.getClazz() +"."+res.getMethod();
	//System.out.println("*** Comparing "+qfn+" with "+qn);
        if (qfn.equals(qn))  {
	   	//System.out.println("*** We might have a Winner here ... "+qfn);
	   	//System.out.println("*** Comparing "+res.getReturnValue()+" with "+returnType);
          	if (!(res.getReturnValue().equals(returnType))) {
          		//System.out.println("wrong returntype");
          		continue;
          	}
          	List paramlist = res.getParams();
          	if (paramlist!=null) {
            		Iterator e=paramlist.iterator();
            		String paramString1="(";
            		String sthlp = null;
            		String paramString2="(";
            		while(e.hasNext())  {
            			sthlp = (String) e.next();
              			paramString1=paramString1+sthlp;
              			paramString2=paramString2+sthlp;
              			if (e.hasNext()) {
                			paramString1=paramString1+", ";
                			paramString2=paramString2+",";
              			}
            		}
            		paramString1=paramString1+")";
            		paramString2=paramString2+")";
            		//System.out.println("*** Comparing "+paramString2+" with "+signature);
            		if (!(paramString1.equals(signature)||paramString1.equals(signature))) {
	          		//System.out.println("wrong parametertypes");
        	  		continue;
            		}

          	}
          	else{
          	    //System.out.println("null paramList encountered");
          	    if (!"()".equals(signature)) {
          	     	continue;
          	    }
          	}
          	//System.out.println("Returning passed tests");
          	pList.remove(res);
          	return "passed tests";
        	}
	else
		{
      		//System.out.println("wrong method");
        	continue;
        	}
      }
      //System.out.println("no tests implemented for this method");
      return "no tests implemented for this method";

    }

}


