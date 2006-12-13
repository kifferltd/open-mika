/*
 * FocusControlFactory.java
 *
 * Created on 6. Oktober 2003, 12:06
 */

package com.acunia.wonka.rudolph;

/**
 *
 * @author  jbader
 */
public class FocusControlFactory
{
	private static final FocusControl focusControl = new TabFocusControl();
	
	/** Creates a new instance of FocusControlFactory */
	private FocusControlFactory()
	{
	}
	
	public static FocusControl create()
	{
		return focusControl;
	}
	
}
