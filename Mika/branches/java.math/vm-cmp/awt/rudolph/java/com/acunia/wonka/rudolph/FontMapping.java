/**************************************************************************
* Copyright (c) 2003 by Punch Telematix. All rights reserved.             *
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
 * FontMapping.java
 *
 * Created on 10. Dezember 2003, 08:14
 */

package com.acunia.wonka.rudolph;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 *
 * @author  jbader
 */
public class FontMapping
{
	private static final Properties fontMapping;
	
	static
	{
		final String MAPPING_FILENAME = "fontmapping.properties";
		
		fontMapping = new Properties();
		
		try
		{
			InputStream propstream = ClassLoader.getSystemResourceAsStream(MAPPING_FILENAME);
			if (propstream == null) {
				System.err.println("FontMapping: unable to load system resource " + MAPPING_FILENAME);
			}
			else {
				fontMapping.load(propstream);
				propstream.close();
			}
		}
		catch(FileNotFoundException ex)
		{
			// mapping file not found
			System.out.println("font mapping file ("+MAPPING_FILENAME+") not found");
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			System.err.println(FontMapping.class.getName()+": unable to load "+MAPPING_FILENAME);
		}
	}
	
	/** Creates a new instance of FontMapping */
	private FontMapping()
	{
	}	
	
	public static String map(String name)
	{
		return fontMapping.getProperty(name, name);
	}
	
}
