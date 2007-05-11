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


/**
 *
 * @version	$Id: CodeSource.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 *
 */

package java.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketPermission;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Vector;

public class CodeSource implements Serializable {

  private static final long serialVersionUID = 4977541819976013951L;

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int l = in.readInt();
    if(l > 0){
      certs = new Certificate[l];
      for(int i = 0 ; i < l ; l++){
        String type = (String) in.readObject();
        in.readInt();
        try {
          certs[i] = CertificateFactory.getInstance(type).generateCertificate(in);
        }
        catch(java.security.cert.CertificateException ce){
          throw new IOException(ce.toString());
        }
      }
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    int l = (certs == null ? 0 : certs.length);
    out.writeInt(l);
    for(int i = 0 ; i < l ; i++){
      Certificate cert = certs[i];
      out.writeObject(cert.getType());
      try {
        byte[] bytes = cert.getEncoded();
        out.writeInt(bytes.length);
        out.write(bytes);
      }
      catch(java.security.cert.CertificateEncodingException cee){
        throw new IOException(cee.toString());
      }
    }
  }

  private URL url;
  private transient Certificate[] certs;

  /**
   * Constructor CodeSource(URL,Certificate[]) creates a CodeSource
   * with the given URL and a snapshot of the given Certificates's
   * (else some joker could change the Certificate[] afterward).
   */
  public CodeSource (URL url, Certificate[] certs) {
    this.url = url;
    if (certs != null) {
      this.certs = (Certificate[])certs.clone();
    }
    else {
      this.certs = null;
    }
  }

  /**
   * Method hashCode() returns a value which depends on the URL and
   * on the certificates, but not on the order of the latter.
   */
  public int hashCode() {
    int hash = (url == null ? -12345 : url.hashCode());
    if(certs != null){
      for (int i = 0; i< this.certs.length; ++i) {
        hash ^= this.certs[i].hashCode();
      }
    }
    return hash;
  }

  /**
   * Method equals(Object) returns true iff the Object is a CodeSource     
   * and is composed of the same location and the same Certificate's
   * (but not necessarily in the same order).
   */
  public boolean equals (Object obj) {
    boolean answer = false;
    if (obj instanceof CodeSource) {
      CodeSource target = (CodeSource)obj;
      if ( (url == null ? target.url == null : url.equals(target.url)) ) {
        Certificate[] targetcerts = target.getCertificates();
        if(certs == null || target.certs == null){
          return (certs == target.certs);
        }
        if (certs.length == target.certs.length) {
// O.K., everything else checks and we just need to compare the Certificate's.
// We copy the target's certs into a Vector and look for each of the current
// CodeSource's certs in this Vector.  Each time we find a match, we remove
// the corresponding item in the Vector, so the search gets shorter every time.
          Vector targets = new Vector(certs.length);
          for (int i = 0; i < certs.length; ++i) {
            targets.addElement(targetcerts[i]);
          }

          int found = 0;
          for (int i = 0; i < certs.length; ++i) {
            found = targets.indexOf(certs[i]);
            if (found >= 0) {
              targets.removeElementAt(found);
            }
            else break;
          }
          answer = (found >= 0);

/*
  Johan's code:
          boolean notfound;
          answer = true;
          int i = 0;
          int j;
          while ((i<certs.length) && answer) {
            j = certs.length -i -1;
            notfound = true;
            while (notfound && (j>=i)) {
              if (certs[i].equals(targetcerts[j])) {
                notfound = false;
                targetcerts[j] = targetcerts[i];
              }
              j--;
            }
            answer = !notfound;
            i++;
          }
*/
        }
      }
    }
    return answer;
  }

  public final URL getLocation() {
    return this.url;
  }

  public final Certificate[] getCertificates() {
    return this.certs;
  }

  /**
   * The specification used here is based on that given at 
   * http://java.sun.com/j2se/1.3/docs/api/java/security/CodeSource.html
   */
  public boolean implies (CodeSource codesource) {

// 1. codesource must not be null.
    if (codesource==null) {

      return false;

    }

// 2. If this object's certificates are not null, then all of them must be
//    present in codesource's certificates.
    if (certs != null) {
      Certificate[] targetcerts = codesource.getCertificates();
      if(targetcerts == null){
        return false;
      }
// We copy the target's certs into a Vector and look for each of the current
// CodeSource's certs in this Vector.  Each time we find a match, we remove
// the corresponding item in the Vector, so the search gets shorter every time.
      Vector targets = new Vector(certs.length);
      for (int i = 0; i < certs.length; ++i) {
        targets.addElement(targetcerts[i]);
      }

      int found = 0;
      for (int i = 0; i < certs.length; ++i) {
        found = targets.indexOf(certs[i]);
        if (found >= 0) {
          targets.removeElementAt(found);
        }
        else break;
      }
      if (found < 0) {

        return false;

      }
    }

// 3. If this object's location is not null, then:
    if (url != null) {
// 3a.  codesource's location must not be null.
      if (codesource.url == null) {

        return false;

      }

// 3b.  If this location equals codesource's location, return true immediately.
      if (codesource.url == this.url) {

        return true;

      }

// 3c.  The protocol of this objects's location must be equal to the
//      protocol of codesource's location.
      if (! this.url.getProtocol().equals(codesource.url.getProtocol())) {

        return false;

      }

// 3d.  If the host of this object's location is not null, then a
//      SocketPermission for the host of this object's location must imply
//      a SocketPermission for the host of codesource's location.
//      (The spec doesn't mention the action, so we specify "resolve" on
//      both sides).
      String host = this.url.getHost();
      if (host != null
          &&! (new SocketPermission(host,"resolve")).implies(new SocketPermission(codesource.url.getHost(),"resolve"))) {

        return false;

      }

// 3e.  If the port  of this object's location is not -1, then it must be
//      equal to the port of codesource's location.
      int port = this.url.getPort();
      if (port != -1 && ! (port == codesource.url.getPort())) {

        return false;

      }

// 3f.  If the file part of this object's location does not equal the file
//      part of codesource's location then one of the following must hold:
      String thisfile = this.url.getFile();
      String thatfile = codesource.url.getFile();
      if (!thisfile.equals(thatfile)) {
        int slash = thisfile.lastIndexOf('/');
        int slashdash = thisfile.indexOf('-',slash);
        int slashstar = thisfile.indexOf('*',slash);

//      * If the file part of this object's location ends with "/-", the file
//        part of codesource's location must contain the file part of this 
//        object's location, without the trailing "-", as a prefix.
        if (slashdash == thisfile.length()-1
            && ! thatfile.startsWith(thisfile.substring(0,slashdash))) {
          return false;

        }

//      * If the file part of this object's location ends with "/*", the file
//        part of codesource's location must contain the file part of this 
//        object's location, without the trailing "*", as a prefix, and the
//        remainder of the file part of codesource's location must not contain
//        any '/' character.
        else if (slashstar == thisfile.length()-1) {
          int thatslash = thatfile.lastIndexOf('/');
          if(! thatfile.startsWith(thisfile.substring(0,slashstar))) {
            
            return false;

          }
          else if (thatslash > slashstar) {

            return false;

          }
        }

//      * If the file part of this object's location ends with '/', the file
//        part of codesource's location must contain the file part of this
//        object's location as a prefix.
        else if (slash == thisfile.length()-1
            && ! thatfile.startsWith(thisfile)) {

          return false;

        }

//      * Otherwise the file part of codesource's location must contain the
//        file part of this object's location, with "/" appended, as a prefix.
        else if (slash != thisfile.length()-1 && 
                 slashstar != thisfile.length()-1 && 
                 slashdash != thisfile.length()-1 && 
                 !thatfile.startsWith(thisfile + "/")) {
          
          return false;

      }
// 3g.  If the reference part of this object's location is not null, it must
//      equal the file part of codesource's location.. 
      String thisref = this.url.getRef();
      if (thisref != null && thisref.equals(codesource.getLocation().getRef())) {

        return false;

      }
    }
  }

  return true;

/*
  Johan's code :
    boolean answer = false;
    if (codesource != null) {
      if (certs != null) {
        boolean notfound;
        Certificate[] targetcerts = codesource.getCertificates();
        int i = 0;
        answer = true;
        while ( (i < certs.length) && answer ) {
          int j = 0;
          notfound = true;
          while ( notfound && (j<targetcerts.length) ) {
            notfound = !(certs[i].equals(targetcerts[j]));
            j++;
          }
          answer = !notfound;
          i++;
        }
      }
    }
    return answer;
*/
  }
	
	public String toString ()
	{
		return url + " " + certs;
	}
}
