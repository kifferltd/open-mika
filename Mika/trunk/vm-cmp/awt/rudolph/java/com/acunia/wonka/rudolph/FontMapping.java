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
