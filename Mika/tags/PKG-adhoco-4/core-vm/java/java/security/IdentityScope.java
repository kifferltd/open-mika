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


package java.security;

import wonka.vm.DeprecatedMethodError;
import java.util.Enumeration;

/**
 *
 * @version	$Id: IdentityScope.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 * @deprecated
 */
public abstract class IdentityScope extends Identity {

  private static final long serialVersionUID = -2337346281189773310L;

  protected IdentityScope(){
  }

  public IdentityScope(String name){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  public IdentityScope(String name, IdentityScope scope) throws KeyManagementException{}

  public static IdentityScope getSystemScope(){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  protected static void setSystemScope(IdentityScope scope){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  public abstract int size();
  public abstract Identity getIdentity(String name);

  public Identity getIdentity(Principal principal){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  public abstract Identity getIdentity(PublicKey key);
  public abstract void addIdentity(Identity identity) throws KeyManagementException;
  public abstract void removeIdentity(Identity identity) throws KeyManagementException;
  public abstract Enumeration identities();

  public String toString(){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }
}
