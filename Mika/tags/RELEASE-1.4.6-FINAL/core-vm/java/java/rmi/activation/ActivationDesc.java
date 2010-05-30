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

package java.rmi.activation;

import java.io.Serializable;
import java.rmi.MarshalledObject;

public final class ActivationDesc extends Object implements Serializable {

  private static final long serialVersionUID = 7455834104417690957L;

  private ActivationGroupID groupID;
  private String className;
  private String location;
  private MarshalledObject data;
  private boolean restart;

  public ActivationDesc(String className, String location, MarshalledObject data) throws ActivationException {
    groupID = ActivationGroup.currentGroupID();
    if(groupID == null){
      throw new IllegalArgumentException("groupID is null");
    }
    this.className = className;
    this.location = location;
    this.data = data;
  }
  
  public ActivationDesc(String className, String location, MarshalledObject data, boolean restart) throws ActivationException {
    this(className, location, data);
    this.restart = restart;
  }
  
  public ActivationDesc(ActivationGroupID groupID, String className, String location, MarshalledObject data) {
    if(groupID == null){
      throw new IllegalArgumentException("groupID is null");
    }
    this.groupID = groupID;
    this.className = className;
    this.location = location;
    this.data = data;
  }
  
  public ActivationDesc(ActivationGroupID groupID, String className, String location, MarshalledObject data, boolean restart) {
    this(groupID, className, location, data);
    this.restart = restart;
  }
  
  public ActivationGroupID getGroupID() {
    return groupID;
  }
  
  public String getClassName() {
    return className;
  }
  
  public String getLocation() {
    return location;
  }
  
  public MarshalledObject getData() {
    return data;
  }
  
  public boolean getRestartMode() {
    return restart;
  }
  
  public boolean equals(Object obj) {
    if(getClass().isInstance(obj)){
      ActivationDesc ad = (ActivationDesc) obj;
      return restart == ad.restart && groupID.equals(ad.groupID) && data.equals(ad.data)
          && className.equals(ad.className) && location.equals(ad.location);
    }
    return false;
  }
  
  public int hashCode() {
    int hash = restart ? (int)serialVersionUID : 0;
    return hash ^ groupID.hashCode() ^ className.hashCode() ^ location.hashCode() ^ data.hashCode();
  }
}

