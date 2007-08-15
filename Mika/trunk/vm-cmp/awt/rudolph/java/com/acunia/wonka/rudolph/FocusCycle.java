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
 * FocusCycle.java
 *
 * Created on 6. Oktober 2003, 12:59
 */

package com.acunia.wonka.rudolph;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;

/**
 *
 * @author  jbader
 */
public class FocusCycle
{	
	/** Creates a new instance of FocusCycle */
	private FocusCycle()
	{
	}
		
	public static boolean next(Component currentComponent)
	{		
		if(currentComponent==null)
		{
			throw new IllegalArgumentException("currentComponent cannot be null");
		}
						
		Container rootContainer = getRootContainer(currentComponent);
		
		synchronized(rootContainer.getTreeLock())
		{
			if(currentComponent.getParent()!=null)
			{					
				Component target = currentComponent;
				do {
					boolean found = false;
					Container container = target.getParent();
										
					Component component;
					for(int i=0;i<container.getComponentCount();i++) {
						component = container.getComponent(i);
						if(found) {
							if(isFocusable(component)) {
								component.requestFocus();
								return true;
							}
							else if((component instanceof Container) 
								&& (component.isVisible())
								&& (component.isEnabled()))
							{
								if(focusForward((Container) component))
								{
									return true;
								}
							}
						}
						else if(component == target) {
							found = true;
						}
						
					}
					
					if(container instanceof Dialog)
					{
						if(((Dialog) container).isModal())
						{
							return FocusCycle.focusFirstComponent(container);	
						}
					}					
					
					target = container;					
					
				} while((target!=rootContainer) && (target.getParent() != null));
			}
			
			if(focusForward(rootContainer)) {
				return true;
			}
			
			return false;
		}		
	}
	
	public static boolean prev(Component currentComponent)
	{		
		if(currentComponent==null)
		{
			throw new IllegalArgumentException("currentComponent cannot be null");
		}
						
		Container rootContainer = getRootContainer(currentComponent);
		
		synchronized(rootContainer.getTreeLock())
		{
			if(currentComponent.getParent()!=null)
			{					
				Component target = currentComponent;
				do {
					boolean found = false;
					Container container = target.getParent();
					Component component;
					for(int i=container.getComponentCount()-1;i>=0;i--) {
						component = container.getComponent(i);
						if(found) {
							if(isFocusable(component)) {
								component.requestFocus();
								return true;
							}
							else if((component instanceof Container) 
								&& (component.isVisible())
								&& (component.isEnabled()))
							{
								if(focusBackward((Container) component))
								{
									return true;
								}
							}
						}
						else if(component == target) {
							found = true;
						}
						
					}
					
					if(container instanceof Dialog)
					{
						if(((Dialog) container).isModal())
						{
							return FocusCycle.focusLastComponent(container);	
						}
					}										
					
					target = container;					
					
				} while((target!=rootContainer) && (target.getParent() != null));
			}
			
			if(focusBackward(rootContainer)) {
				return true;
			}
			
			return false;
		}		
	}
	
	public static boolean focusFirstComponent(Container container)
	{
		Container rootContainer = getRootContainer(container);

		synchronized(rootContainer.getTreeLock())
		{		
			for(int i=0;i<container.getComponentCount();i++) {
				Component component = container.getComponent(i);
				if(component.isFocusTraversable() && 
				component.isVisible() && 
				component.isEnabled()) 			
				{
					component.requestFocus();
					return true;
				}
				else if((component instanceof Container) 
					&& (component.isVisible())
					&& (component.isEnabled()))
				{
					if(focusFirstComponent((Container) component))
					{
						return true;
					}

				}
			}
		}
		
		return false;
	}
	
	public static boolean focusLastComponent(Container container)
	{
		Container rootContainer = getRootContainer(container);

		synchronized(rootContainer.getTreeLock())
		{				
			for(int i=container.getComponentCount()-1;i>=0;i--) {
				Component component = container.getComponent(i);
				if(component.isFocusTraversable() && 
				component.isVisible() && 
				component.isEnabled()) 			
				{
					component.requestFocus();
					return true;
				}
				else if((component instanceof Container) 
					&& (component.isVisible())
					&& (component.isEnabled()))
				{
					if(focusLastComponent((Container) component))
					{
						return true;
					}

				}
			}
		}
		
		return false;
	}	
	
	
	protected static boolean isFocusable(Component component)
	{
		Container container = component.getParent();
		
		boolean rootsFocusable = true;
				
		while((container!=null) && (rootsFocusable))
		{
			rootsFocusable = (container.isVisible() && container.isEnabled());
			container = container.getParent();
		}		
		
		boolean result = (rootsFocusable && 
			component.isFocusTraversable() && 
			component.isVisible() && 
			component.isEnabled());		
				
		return result;
	}	
		
	protected static Container getRootContainer(Component component)
	{		
		Container parentContainer = component.getParent();
		Container rootContainer = null;
		
		while(parentContainer!=null)
		{
			rootContainer = parentContainer;
			parentContainer = parentContainer.getParent();
		}
		
		if(rootContainer==null) {
			rootContainer = (Container) component;
		}
		
		return rootContainer;		
	}
	
	protected static boolean focusForward(Container container) {
		for(int i=0;i<container.getComponentCount();i++) {
			Component component = container.getComponent(i);
			if(isFocusable(component)) {
				component.requestFocus();
				return true;
			}
			else if((component instanceof Container) 
				&& (component.isVisible())
				&& (component.isEnabled()))
			{
				if(focusForward((Container) component))
				{
					return true;
				}
				
			}
		}
		
		return false;
	}
	
	protected static boolean focusBackward(Container container) {
		for(int i=container.getComponentCount()-1;i>=0;i--) {
			Component component = container.getComponent(i);
			if(isFocusable(component)) {
				component.requestFocus();
				return true;
			}
			else if((component instanceof Container) 
				&& (component.isVisible())
				&& (component.isEnabled()))
			{
				if(focusBackward((Container) component))
				{
					return true;
				}
				
			}
		}
		
		return false;
	}	
}
