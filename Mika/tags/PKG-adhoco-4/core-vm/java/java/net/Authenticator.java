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


package java.net;

public abstract class Authenticator {

	private static Authenticator authent;
	
	public synchronized static void setDefault(Authenticator auth){
  	authent = auth;
	}

    public static PasswordAuthentication requestPasswordAuthentication(
      InetAddress addr, int port, String protocol, String prompt, String scheme) {
    
    return requestPasswordAuthentication(null, addr, port, protocol, prompt, scheme);
  }
  
  public static PasswordAuthentication requestPasswordAuthentication(String host, 
      InetAddress addr, int port, String protocol, String prompt, String scheme) {
		if (authent == null) {
		 	return null;
		}			
    authent.host = host;
		authent.addr = addr;
		authent.port = port;
		authent.protocol = protocol;
		authent.prompt = prompt;
		authent.scheme = scheme;
		return authent.getPasswordAuthentication();		
	}

	private InetAddress addr;
	private int port;
	private String protocol;	
	private String prompt;
	private String scheme;
  private String host;		
	
/**
** This method should be overridden by subclasses.
** @remark returns null
*/
	protected PasswordAuthentication getPasswordAuthentication() {
	 	return null;
	}
	
	protected final int getRequestingPort(){
	   	return port;
	}
	protected final String getRequestingPrompt(){
	   	return prompt;
	}
	protected final String getRequestingProtocol(){
	   	return protocol;
	}
	protected final InetAddress getRequestingSite(){
	   	return addr;
	}
	protected final String getRequestingScheme(){
	   	return scheme;
	}
  protected final String getRequestingHost() {
    return host;
  }
}
