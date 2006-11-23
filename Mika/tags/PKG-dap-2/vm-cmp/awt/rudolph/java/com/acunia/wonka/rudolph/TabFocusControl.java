/*
 * TabFocusControl.java
 *
 * Created on 6. Oktober 2003, 12:07
 */

package com.acunia.wonka.rudolph;

import java.awt.Component;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.util.*;
import java.io.Serializable;

/**
 *
 * @author  jbader
 */
public class TabFocusControl implements FocusControl, Serializable
{
	public void processKeyEvent(KeyEvent event)
	{
		if((event.getID()==KeyEvent.KEY_TYPED) 
//			&& (event.getKeyCode()==KeyEvent.VK_TAB))
			&& (event.getKeyChar()=='\t'))
		{		
			if(event.isShiftDown())
			{
				Component.revertFocus();		
			}
			else
			{
				event.getComponent().transferFocus();
			}
			
			event.consume();
		}
		
	}		
}
