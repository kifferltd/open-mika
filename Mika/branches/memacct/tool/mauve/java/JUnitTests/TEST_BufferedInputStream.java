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


/*
** $Id: TEST_BufferedInputStream.java,v 1.1 2004/12/06 13:29:16 cvs Exp $
*/

package JUnitTests;

import gnu.testlet.*;
import java.io.*;
/**
 * Test cases for java.io.BufferedInputStream
 *
 * Note:
 *   These test scenarios make use of a ByteArrayInputStream to feed the
 *   BufferedInputStream with data. We therefore make the assumption that
 *   this ByteArrayInputStream provides the expected behavior.
 *
 * Dependencies: 
 *   java.io.ByteArrayInputStream
 *
 * int read()
 *   Spec:  Reads the next byte of data from the input stream. The value byte 
 *          is returned as an int in the range 0 to 255. If no byte is available 
 *          because the end of the stream has been reached, the value -1 is 
 *          returned. This method blocks until input data is available, the end 
 *          of the stream is detected, or an exception is thrown. 
 *   Test1: 
 */

public class TEST_BufferedInputStream extends Mv_Assert {
    // ---------------------------------------------------------
    // Standard JUnit test framework
    // ---------------------------------------------------------
    public TEST_BufferedInputStream() { }

    // ---------------------------------------------------------
    // Actual test scenarios
    // ---------------------------------------------------------
    /**
     * Fill a buffer with a well-known pattern of data and return
     * a ByteArrayInputStream build on top of this buffer.
     */
    private InputStream getDataSource(byte[] buf) {
	for( int i=0; i<buf.length; i++) {
	    buf[i] = (byte) (i%255);
	}
	return new ByteArrayInputStream(buf);
    }

    /**
     * int read()
     */
    public void testRead1() throws Exception {
	// Create data to read
	byte[] buf = new byte[100];
	BufferedInputStream bis = new BufferedInputStream(getDataSource(buf), 10);
	
	// Read data from buffered inputstream, byte per byte
	// and compare them with original data
	for( int i=0; i<buf.length; i++) {
	    int readed = bis.read();
	    
	    if ( readed != buf[i] ) {
		fail("wrong byte read - read:" + readed + ", buf[" + i + "]:" + buf[i]);
	    }
	}
	
	// Try to read further, we should expect -1
	if (bis.read() != -1) {
	    fail("failed (read() didn't return -1 at end of stream");
	}
    }

    /**
     * int read(byte[], int, int)
     */
    public void testRead2() throws Exception {
	// Create data to read
	byte[] buf = new byte[100];
	BufferedInputStream bis = new BufferedInputStream(getDataSource(buf), 10);
	    
	// Read data from buffered inputstream, 30 bytes per 30 bytes
	// and compare them with original data.
	// In addition, copy these data at different offset in the target
	// buffer.
	int offset= 0;
	int len   = 30;
	byte[] b  = new byte[buf.length+len];
	
	while( offset < buf.length ) {
	    int readed = bis.read(b, offset, len);
	    
	    if (readed == -1) {
		fail("EOF reached too soon, offset=" + offset);
	    }
	    
	    for( int i=offset; i<offset+readed; i++) {
		if ( b[i] != buf[i] ) {
		    fail("wrong byte read - b[" + i + "]=" + b[i] + ", buf[" + i);
		}
	    }

	    offset += readed;
	}
	
	// test EOF
	if (bis.read() != -1) {
	    fail("EOF no thrown in time");
	}
    }

    /**
     * long skip(long)
     */
    public void testSkip1() throws Exception {
	// Create data to read
	byte[] buf = new byte[100];
	BufferedInputStream bis = new BufferedInputStream(getDataSource(buf), 10);
	
	// Skip some bytes a read the following one
	int skipped = 20;
	long toSkip = skipped;
	
	while( toSkip > 0 ) {
	    toSkip -= bis.skip(toSkip);
	}
	
	// Read next byte and compare it with original
	int readed = bis.read();
	if ((byte) readed != buf[skipped] ) {
	    fail("Wrong byte read after skip - read:" + readed + ", expected:" + buf[skipped]);
	}
    }

    /**
     * void mark(int)
     * void reset()
     */
    public void testMark1() throws Exception {
	// Create data to read
	byte[] buf = new byte[100];
	BufferedInputStream bis = new BufferedInputStream(getDataSource(buf), 10);
	
	// read a few bytes from stream
	int markPos = 10;
	for( int i=0; i<markPos; i++) {
	    bis.read();
	}
	
	// mark the current position
	int readLimit = 89;
	bis.mark(readLimit);
	
	// read a bit further, stay within the mark
	for( int i=0; i<(readLimit/2); i++) {
	    bis.read();
	}
	
	// reset the stream, read byte and compare with original
	bis.reset();
	int readed = bis.read();
	
	if ((byte) readed != buf[markPos]) {
	    fail("Stream not reset at right position");
	}
	
	// read further than the limit, and try to reset the stream
	bis.reset();
	for( int i=0; i<readLimit+1; i++ ) {
	    bis.read();
	}
	
	try {
	    bis.reset();
	    
	    // If reached, means no exception was thrown
	    fail("No exception thrown when resetting after readLimit");
	}
	catch(IOException ioe) {
	}
    }
}
