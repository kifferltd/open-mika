/*
 * Copyright (c) 2002 Imsys AB, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * Imsys AB ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * IMSYS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. IMSYS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.sun.cldc.io.j2me.comm;

import java.io.*;

public class SerialPortInputStream extends InputStream
{
	public SerialPortInputStream()
    {
    	super();
    }


	public int read(byte[] b,int off,int len) 
		throws IOException
    {
        if (b == null) {
            throw new NullPointerException();
        }
		else if ((off < 0) || (off > b.length) || (len < 0) ||
                 ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
		else if (len == 0) {
            return 0;
        }

    	return readBuffer(b,off,len);
    }

    public void close() throws IOException
	{
	}

    public synchronized void mark(int readlimit)
	{
	}

    public synchronized void reset() throws IOException
	{
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported()
	{
        return false;
    }

    public native int read() throws IOException;
	private native int readBuffer(byte[] b,int off,int len);
    public native int available()
		throws IOException;

}
