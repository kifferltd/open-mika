/**************************************************************************
* Copyright (c) 2006, 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.util.jar;


import java.security.cert.Certificate;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.OutputStream;

class JarVerifier {

  class VerifierEntry extends OutputStream {
        public void write(int value) {
        }

        void verify() {
        }
  }
  
  static void verifyBytes(byte[] bytes, String algorithm, String value) throws NoSuchAlgorithmException {
  }

  static String[] getDigestKeyAlgortihm(Attributes attributes) {
    return null;
  }

  static String[] getDigestKeyAlgortihm(Attributes attributes, String pattern) {
    return null;
  }

  static void verifyBlockSignatureFile(String name, JarEntry je, JarFile jf) {
  }
  
  static boolean verifyManifest(JarFile file) throws IOException {
    return false;
  }

  int mainAttributesEnd;
  
  JarVerifier(String name) {
  }

  Certificate[] getCertificates(String name) {
    return null;
  }
  
  void setManifest(Manifest mf) {
  }
  
  void addMetaEntry(String name, byte[] buf) {
  }
  
  VerifierEntry initEntry(String name) {
    return null;
  }
  
  synchronized boolean readCertificates() {
    return false;
  }

  void removeMetaEntries() {
  }

  boolean isSignedJar() {
    return false;
  }

}
