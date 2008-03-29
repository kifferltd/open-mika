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

import java.security.spec.AlgorithmParameterSpec;

public class RC5ParameterSpec implements AlgorithmParameterSpec {

 private byte[] iv;
 private int version;
 private int wordSize;
 private int rounds;

 public RC5ParameterSpec(int version, int rounds, int wordSize) {
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
 }

 public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv){
        this(version, rounds, wordSize, iv, 0);
 }

 public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv, int offset){
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = new byte[8];
        System.arraycopy(iv,offset,this.iv,0,8);
 }

 public boolean equals(Object obj) {
        if(!(obj instanceof RC5ParameterSpec)){
                return false;
        }
        RC5ParameterSpec rps = (RC5ParameterSpec)obj;
        return  this.iv == null &&
                rps.iv == null &&
                this.version == rps.version &&
                this.rounds == rps.rounds &&
                this.wordSize == rps.wordSize;
 }

 public int getVersion(){
        return version;
 }

 public int getRounds(){
        return rounds;
 }

 public int getWordSize(){
        return wordSize;
 }

 public byte[] getIV(){
        return iv;
 }

 public int hashCode(){
        return version ^ rounds ^ wordSize ^ (iv == null ? 0xabcd0000 : iv.hashCode());
 }
}
