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
