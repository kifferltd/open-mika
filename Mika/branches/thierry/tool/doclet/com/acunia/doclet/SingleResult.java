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

  public class SingleResult {

    private String clss=null;
    private String method=null;
    private List params=null;
    private String retval=null;
    private String TestMessage=null;

    public SingleResult(String cl,String meth,List prm,String retvl,String Mssg) {
      clss=cl;
      method=meth;
      params=prm;
      retval=retvl;
      TestMessage=Mssg; 
    }

    public SingleResult() {
    }

    public String getClazz() {
      return clss;
    }

    public String getMethod() {
      return method;
    }

    public List getParams() {
      return params;
    }

    public String getReturnValue() {
      return retval;
    }

    public String getTestMessage() {
      if (TestMessage==null) {
        return "No Message Available";
      }
      else {
        return TestMessage;
      }
    }

    public void setClass(String cl) {
      clss=cl;
    }

    public void setMethod(String meth) {
      method=meth;
    }

    public void setParams(List prm) {
      params=prm;
    }

    public void setReturnValue(String retvl) {
      retval=retvl;
    }

    public void setTestMessage(String Mssg) {
      TestMessage=Mssg;
    }

    public boolean equals(Object e){
     	if (!(e instanceof SingleResult)){
     	 	return false;
     	}
     	SingleResult sr = (SingleResult)e;
     	return  (this.clss   == null ? sr.clss   == null : this.clss.equals(sr.clss))
     	    &&	(this.method == null ? sr.method == null : this.method.equals(sr.method))
     	    &&	(this.params == null ? sr.params == null : this.params.equals(sr.params))
     	    &&	(this.retval == null ? sr.retval == null : this.retval.equals(sr.retval));

    }

    public String toString(){
     	StringBuffer buf = new StringBuffer(clss);
     	buf.append('$');
     	buf.append(method);
     	buf.append('(');
     	if (params != null){
     		Iterator it = params.iterator();
     		while(it.hasNext()){
     	 		buf.append(it.next()+",");
     		}
     		if(buf.charAt(buf.length()-1) == ','){
     	 		buf.deleteCharAt(buf.length()-1);
     		}
     	}
     	buf.append(')');
     	if (retval != null && (!retval.equals("no returntype"))){
     	 	buf.append(retval);
     	}
     	buf.append('$');
     	buf.append(TestMessage);
     	return buf.toString();
    }
}