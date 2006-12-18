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


package gnu.testlet.wonka.lang.reflect.Constructor;


public class NewInstanceClass
{

    private String types = "";
    private String arg1  = "";
    private String arg2  = "";

    /*
     * PUBLIC Constructor without any parameter
     */
    public NewInstanceClass()
    {
	    //types = "";
	    //arg1="";
	    //arg2="";
    }

    /*
     * PUBLIC Constructor with a single String argument (object)
     */
    public NewInstanceClass(String arg1)
    {
//System.out.println(".........NewInstanceClass(String arg1)");
	    types = "Ljava.lang.String;";
	    this.arg1 = " "+arg1;
    }

    /*
     * PUBLIC Constructor with a single int argument (data type)
     */
    public NewInstanceClass(int arg1)
    {
//System.out.println(".........NewInstanceClass(int arg1)");
	    types = "I";
    	this.arg1 = " "+arg1;
    }

    /*
     * PUBLIC Constructor with a single Integer argument (object)
     */
    public NewInstanceClass(Integer arg1)
    {
//System.out.println(".........NewInstanceClass(Integer arg1)");
	    types = "Ljava.lang.Integer;";
    	this.arg1 = " "+arg1.toString();
    }

    /*
     * PUBLIC Constructor with two arguments of type String
     */
    public NewInstanceClass(String arg1, String arg2)
    {
	    types = "Ljava.lang.String;Ljava.lang.String;";
    	this.arg1 = " "+arg1;
    	this.arg2 = ", "+arg2;
    }

    /*
     * PRIVATE Constructor with two 'Integer' as argument
     */
    private NewInstanceClass(int arg1, int arg2)
    {
	    types = "II";
    	this.arg1 = " "+arg1;
    	this.arg2 = ", "+arg2;
    }

    public String toString()
    {
	    return (getClass().getName()+"("+types+arg1+arg2+")");
    }
}
