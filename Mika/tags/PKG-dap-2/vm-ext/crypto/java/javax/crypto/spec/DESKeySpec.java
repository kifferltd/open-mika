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

import java.security.spec.KeySpec;

public class DESKeySpec implements KeySpec {

 public static int DES_KEY_LEN = 8;

 private byte [] keyBytes;

 public DESKeySpec(byte[] key) throws java.security.InvalidKeyException {
   this(key,0);
 }

 public  DESKeySpec(byte[] key, int offset) throws java.security.InvalidKeyException {
   if((key.length - offset - DES_KEY_LEN) < 0){
     throw new java.security.InvalidKeyException();
   }
   keyBytes = new byte[DES_KEY_LEN];
   System.arraycopy(key,offset,keyBytes,0,DES_KEY_LEN);
 }

 public byte[] getKey() {
   return (byte[])keyBytes.clone();
 }

 public static boolean isParityAdjusted(byte[] key, int offset) throws java.security.InvalidKeyException {
   if((key.length - offset - DES_KEY_LEN) < 0){
     throw new java.security.InvalidKeyException();
   }
   //TODO ...
   return false;
 }

 public static boolean isWeak(byte[] key, int offset) throws java.security.InvalidKeyException {
   if((key.length - offset - DES_KEY_LEN) < 0){
     throw new java.security.InvalidKeyException();
   }
   //TODO ...
   return false;
 }

}