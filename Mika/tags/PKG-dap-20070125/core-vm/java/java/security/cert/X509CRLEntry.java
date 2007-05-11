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


package java.security.cert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @version $Id: X509CRLEntry.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 *
 */

public abstract class X509CRLEntry implements X509Extension {

  public X509CRLEntry(){}

  public boolean equals(Object other){
        if (!(other instanceof X509CRLEntry)){
  	        return false;
        }
        X509CRLEntry x509 = (X509CRLEntry) other;
        try {
                return Arrays.equals(x509.getEncoded(), getEncoded());
        } catch(CRLException e){ return false; }
  }

  public int hashCode(){
        try {
                return getEncoded().hashCode();
        } catch(CRLException e){ return 0; }
  }

  public abstract byte[] getEncoded() throws CRLException;
  public abstract Date getRevocationDate();
  public abstract BigInteger getSerialNumber();
  public abstract boolean hasExtensions();
  public abstract String toString();
}
