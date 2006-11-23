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


package gnu.testlet.wonka.io.Serialization;

/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */
public class Bug extends RuntimeException implements NestedException
{
  private NestedException nestedException = null;

  public Throwable getNestedException()
  {
	return nestedException == null
			? null
			: nestedException.getNestedException();
  }                
  private Bug (String message)
  {
	super (message);
  }                  
  public Bug(Throwable exc, String message)
  {
	super (message);
	this.nestedException = new NestedExceptionImpl(exc);
  }                
  public void printStackTrace (java.io.PrintStream stream)
  {
	super.printStackTrace(stream);
	if (nestedException != null)
	{
	  nestedException.printStackTrace(stream);
	}
  }                
  public void printStackTrace (java.io.PrintWriter writer)
  {
	super.printStackTrace(writer);
	if (nestedException != null)
	{
	  nestedException.printStackTrace(writer);
	}
  }                
  public void printStackTrace ()
  {
	super.printStackTrace();
	if (nestedException != null)
	{
	  nestedException.printStackTrace();
	}
  }                
  private Bug()
  {
	super();
  }                
  public static void when (boolean expression, String message)
  {
	if (expression)
	{
	  throw new Bug(message);
	}
  }                        
  public static void when (boolean expression)
  {
	if (expression)
	{
	  throw new Bug();
	}
  }                        
}
