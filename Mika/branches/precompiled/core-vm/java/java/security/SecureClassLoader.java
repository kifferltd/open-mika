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

/**
 *
 * $Id: SecureClassLoader.java,v 1.2 2005/09/12 22:04:51 cvs Exp $
 *
 */
public class SecureClassLoader extends ClassLoader {

  protected SecureClassLoader() throws SecurityException {
  }

  protected SecureClassLoader(ClassLoader parent) throws SecurityException {
    super(parent);
    if (this instanceof wonka.vm.ApplicationClassLoader) {
      Security.reloadProviders();
    }
  }

  protected final Class defineClass( String name, byte []b, int off, int len, CodeSource cs) {
    if (cs != null) {
      PermissionCollection pc = getPermissions(cs);
      if (pc == null) {
        return defineClass(name, b, off, len);
      }

      return defineClass(name, b, off, len, new ProtectionDomain(cs, pc));
    }
    else {
      return defineClass(name, b, off, len);
    }
  }

  protected PermissionCollection getPermissions(CodeSource cs){
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      GetPolicy gp = new GetPolicy();

      return gp.get().getPermissions(cs);

    }

    // HACK! HACK! HACK! Just to get out of an initialisation foul-up ...
    Policy policy = Policy.getPolicy();
    if (policy == null) {
      // Policy file not loaded yet
      return null;
    }
    return Policy.getPolicy().getPermissions(cs) ;
  }
}

