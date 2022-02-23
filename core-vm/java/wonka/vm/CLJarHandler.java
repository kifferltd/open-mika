/**************************************************************************
* Copyright (c) 2022 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

package wonka.vm;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.*;

public class CLJarHandler extends ClassLoaderURLHandler {

  private JarFile jf;
  private Manifest man;
  private URL url;

  CLJarHandler(JarFile file, URL url){
    this.url = url;
    jf = file;
    try {
      man = jf.getManifest();
    }
    catch (java.io.IOException ioe){
      man = super.getManifest();
    }
  }

  public byte[] getByteArray(String resource) throws IOException {
    InputStream in = null;
    try {
      java.util.zip.ZipEntry ze = jf.getEntry(resource);
      if(ze != null){
        in = jf.getInputStream(ze);
        int len = in.available();
        byte[] bytes = new byte[len];
        in.read(bytes,0,len);
        return bytes;
      }
    } catch(Exception e) {
    } finally {
      if (in != null) {
        in.close();
      }
    }

    return null;
  }

  public InputStream getInputStream(String resource){
    try {
      java.util.zip.ZipEntry ze = jf.getEntry(resource);
      if(ze != null){
        return jf.getInputStream(ze);
      }
    } catch(Exception e){}

    return null;
  }

  public Manifest getManifest(){
    return man;
  }

  public URL getURL(String resource) {
    try {
      if (jf.getEntry(resource) != null) {
        return new URL(url, resource);
      } else {
        String directory = resource + "/";
        if (jf.getEntry(directory) != null) {
          return new URL(url, directory);
        }
      }
    } catch (Exception e) { }
    return null;
  }

  public String toString() {
    return "CLJarHandler for jarfile " + jf + ", URL " + url;
  }
}
