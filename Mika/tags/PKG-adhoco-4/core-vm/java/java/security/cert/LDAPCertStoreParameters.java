/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: LDAPCertStoreParameters.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
package java.security.cert;

/**
 * LDAPCertStoreParameters:
 *
 * @author ruelens
 *
 * created: Apr 11, 2006
 */
public class LDAPCertStoreParameters implements CertStoreParameters {

  private static final int LDAP_PORT = 389;
  private int port;
  private String server;

  /**
   * 
   */
  public LDAPCertStoreParameters() {
    this("localhost", LDAP_PORT);
  }

  public LDAPCertStoreParameters(String string) {
    this(string,LDAP_PORT);
  }
  public LDAPCertStoreParameters(String string, int port) {
    if(string == null) {
      throw new NullPointerException();
    }
    this.server = string;
    this.port = port;
  }

  public int getPort() {
    return port;
  }
   
  public String getServerName() {
    return server;
  }
  
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  
  public String toString() {
    return super.toString()+" "+ server+":"+port;
  }
}
