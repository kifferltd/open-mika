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

import java.io.*;
import java.util.*;

public class FailResultParser {

  private List resultList;
/*
  public static void main(String[] args) {
    new FailResultParser();
  }
*/
  public List getResultList() {
    return resultList;
  }


  public FailResultParser(String filename) {

    List params=null;
    SingleResult res = null;
    StringBuffer buf=null;
    resultList = new LinkedList();
    int counter=0;
    int line=1;
    int value=0;
    char kar;

    try {
    FileInputStream fis = new FileInputStream(filename);
    while((value=fis.read())!=-1) {
      kar=(char) value;
      //System.out.println("-----------> "+kar);
      switch(kar) {
        case '$' :   if (counter==0) {
                     res=new SingleResult();
                     buf = null;
                     counter++;
                     break;
                   }
                   if (counter==1)
                   { counter++;
                     if (buf == null) res.setClass("no Class");
                     else res.setClass(buf.toString());
                     buf=null;
                     break;
                   }
                   /*if (counter==3) {
                     counter++;
                     res.setMethod(buf.toString());
                     buf=null;
                     break;
                   }       */
                   if (counter==4) {
                     counter++;
                     if (buf != null)
                     res.setReturnValue(buf.toString());
                     else res.setReturnValue("no returntype found");
                     buf=null;
                     break;
                   }
                   if (counter==5)
                   { 	counter++;
                     	if (buf!=null)
                     		{ // No message
                       		res.setTestMessage(buf.toString());
                       		buf=null;
                     		}
                     	resultList.add(res);
                     	break;
                   }
                   break;
        case ',' : if (counter!=3)
        		{ // '(' in the message
          		if (buf!=null){ buf.append(kar);}
                     	else 	{
                       		buf=new StringBuffer();
                       		buf.append(kar);
                     		}
            		break;
                   	}
		   if (params!=null)
		   	{
		   	if (buf != null)
                     	params.add(buf.toString());
                     	//System.out.println("adding "+buf.toString()+" to the params list");
                   	}
                   else if (buf != null)
                   		{
                     		params = new LinkedList();
                     		params.add(buf.toString());
                     		//System.out.println("adding "+buf.toString()+" to the params list");
                   		}
                   buf=null;
                   break;
        case '(' : if (counter==5) { // '(' in the message
                     if (buf!=null) {
                       buf.append(kar);
                     }
                     else {
                       buf=new StringBuffer();
                       buf.append(kar);
                     }
                     break;
                   }
                   if (counter != 2 ) break;
		   counter++;  //counter wordt 3
                   if (buf == null) buf =new StringBuffer("no method");
                   res.setMethod(buf.toString());
                   buf=null;
                   break;
        case ')' : if (counter==5) { // ')' in the message
                     if (buf!=null) {
                       buf.append(kar);
                     }
                     else {
                       buf=new StringBuffer();
                       buf.append(kar);
                     }
                     break;
                   }
                   if (counter != 3 ) break;
		   counter++;
                   if (buf==null) {
                     break; // no params '()'
                   }
                   if (params!=null) {
                     params.add(buf.toString());
                     //System.out.println("adding "+buf.toString()+" to the params list");
                   }
                   else {
                     params = new LinkedList();
		     params.add(buf.toString());
                     //System.out.println("adding "+buf.toString()+" to the params list");
                   }
                   buf=null;
                   res.setParams(params);
                   params=null;
                   break;
        case '\n' :  if (counter==4) {
                     	if (buf == null) res.setReturnValue("no returntype");
                     	else res.setReturnValue(buf.toString());
                     	res.setTestMessage(" :>)");
                     	resultList.add(res);
                     			
                     	}
                     // this happens if we are parsing the tested.file !!!	
                     counter=0;
                     line++;
                     break;

        default : if (buf!=null) {
                    buf.append(kar);
                  }
                  else {
                    buf=new StringBuffer();
                    buf.append(kar);
                  }
                  break;
      }
    }
    } //try
    catch(Exception e) {
      System.out.println("[VN] Got Exception: "+e+" on line"+line);
    }
/*
    Enumeration enum = resultList.elements();
    while(enum.hasMoreElements()) {
      res = (SingleResult) enum.nextElement();
      System.out.println("Class = "+res.getClazz());
      System.out.println("Method = "+res.getMethod());
      List v = res.getParams();
      if (v!=null) {
        Enumeration e = v.elements();
        while(e.hasMoreElements()) {
          System.out.println("param = "+(String)e.nextElement());
        }
      }
      System.out.println("ReturnValue = "+res.getReturnValue());
      System.out.println("TestMessage = "+res.getTestMessage()+"\n\n");
    }
 */
  }
}