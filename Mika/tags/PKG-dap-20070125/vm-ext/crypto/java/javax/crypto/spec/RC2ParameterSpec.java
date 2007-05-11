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


package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class RC2ParameterSpec implements AlgorithmParameterSpec {

 private byte[] iv;
 private int effectiveKeyBits;

 public RC2ParameterSpec(int effectiveKeyBits) {
        this.effectiveKeyBits = effectiveKeyBits;
 }

 public RC2ParameterSpec(int effectiveKeyBits, byte[] iv){
        this(effectiveKeyBits, iv, 0);
 }

 public RC2ParameterSpec(int effectiveKeyBits, byte[] iv, int offset){
        this.effectiveKeyBits = effectiveKeyBits;
        this.iv = new byte[8];
        System.arraycopy(iv,offset,this.iv,0,8);
 }

 public boolean equals(Object obj) {
        if(!(obj instanceof RC2ParameterSpec)){
                return false;
        }
        RC2ParameterSpec rps = (RC2ParameterSpec)obj;
        return  this.iv == null &&
                rps.iv == null &&
                this.effectiveKeyBits == rps.effectiveKeyBits;
 }

 public int getEffectiveKeyBits(){
        return effectiveKeyBits;
 }

 public byte[] getIV(){
        return iv;
 }

 public int hashCode(){
        return effectiveKeyBits ^ (iv == null ? 0xabcd0000 : iv.hashCode());
 }
}