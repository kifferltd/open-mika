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

