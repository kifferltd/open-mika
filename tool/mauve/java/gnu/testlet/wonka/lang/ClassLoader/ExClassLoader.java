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


package gnu.testlet.wonka.lang.ClassLoader; //complete the package name ...

import java.util.HashMap;

public class ExClassLoader extends ClassLoader {	
	
	private HashMap hm;
        private String name;
	
	public ExClassLoader(HashMap classes, String name){
		hm = classes;
		this.name = name;
	}
	
	public ExClassLoader(ClassLoader cl, HashMap classes, String name){
		super(cl);
		hm = classes;
		this.name = name;
	}

        public Class loadClass(String n)throws ClassNotFoundException {
//          	System.out.println("called loadClass "+n);          	
         	return super.loadClass(n);
        }
	
        protected Class loadClass(String n, boolean b)throws ClassNotFoundException {
//          	System.out.println("called loadClass "+n+" and boolean "+b);          	
         	return super.loadClass(n,b);
        }
		
	protected Class findClass(String className) throws ClassNotFoundException {
          	//System.out.println(name+" is looking for "+className);          	
		Object o = hm.get(className);
		if (o == null){ 	
	 	 	throw new ClassNotFoundException(className+" not found");		
		} 	
		byte [] bytes = (byte[])o;
		return defineClass(className, bytes, 0, bytes.length);
	}
        public String toString(){
         	return super.toString()+" "+name;
        }
}