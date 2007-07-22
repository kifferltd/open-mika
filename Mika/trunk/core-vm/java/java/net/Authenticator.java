/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
