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

/*
** $Id: MessageDigestSpi.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

public abstract class MessageDigestSpi {

  public MessageDigestSpi(){}

  public Object clone() throws CloneNotSupportedException {
        return super.clone();
  }

  protected abstract byte[] engineDigest();
  protected abstract void engineReset();
  protected abstract void engineUpdate(byte input);
  protected abstract void engineUpdate(byte[] input, int offset, int len);

/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** Default implementation just calls engineDigest() and copies the result,
** serious providers should override this.
** @param buf buffer in which to write the digest
** @param offset offset within <code>buf</code> where digest should start
** @param len space available to write digest
** @throws DigestException if an arror occurs, in particular if <code>len</code>
** is less than the length of the digest or is out of range. 


*/
  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        if (len < engineGetDigestLength()) {
            engineReset();
            throw new DigestException("too short");
        }
        if (offset < 0) {
            engineReset();
            throw new DigestException("negative offset");
        }
        if (offset > buf.length - len) {
            engineReset();
            throw new DigestException("buffer overrun");
        }
        byte tmp[] = engineDigest();
        if (len < tmp.length) {
            throw new DigestException("too short");
        }
        System.arraycopy(tmp, 0, buf, offset, tmp.length);
        return tmp.length;            
  }

/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing. It simply returns 0.
**
** @remark should be overridden
*/
  protected int engineGetDigestLength() {
        return 0;
  }
}
