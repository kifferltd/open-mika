/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by /k/ Embedded Java Solutions.                *
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
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class Provider extends Properties{

  private static final long serialVersionUID = -4298000515446427739L;

  /**
  ** Fields dictated by the Serialized Form ...
  */
  private String info;
  private String name;
  private double version;

  private static void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSecurityAccess(permission);
      }
    }
  }

  protected Provider(String name, double version, String info){
        this.info = info;
        this.name = name;
        this.version = version;
  }

  public String getInfo(){
        return info;
  }

  public String getName(){
        return name;
  }

  public double getVersion(){
        return version;
  }

  public String toString()  {
        return name+" version: "+version;
  }

  public Set entrySet(){
        return Collections.unmodifiableSet(super.entrySet());
  }

  public Set keySet(){
        return Collections.unmodifiableSet(super.keySet());
  }

  public Collection values(){
        return Collections.unmodifiableCollection(super.values());
  }

  public void clear() {
    permissionCheck("clearProviderProperties."+name);
    super.clear();
  }

  public void load(InputStream in) throws IOException {
    super.load(in);
  }

  public Object put(Object key, Object value) {
    permissionCheck("putProviderProperty."+name);
    return super.put(key,value);
  }

  public void putAll(Map t){
    permissionCheck("putProviderProperty."+name);
    Iterator it = t.entrySet().iterator();
    while (it.hasNext()){
      Map.Entry me = (Map.Entry)it.next();
      super.put(me.getKey(),me.getValue());
    }
  }

  /**
  ** REMOVE THIS METHOD DEBUG ONLY  ...
  */
  public String getProperty(String nm){
    //System.out.println("\n\n\n-------\nLOOKING FOR PROPERTY '"+nm+"' in :\n"+super.toString()+"\n\n\n\n---------\n");
    return super.getProperty(nm);
  }


  public Object remove(Object key) {
    permissionCheck("removeProviderProperty."+name);
    return super.remove(key);
  }


}
