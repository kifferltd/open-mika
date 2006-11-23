/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package java.awt;

import java.util.*;      // EventListener definitions
import java.awt.event.*; // specific events definitions
import java.io.*;        //  ObjectOutputStream and IOException for save() and saveInternal()

/*
** AWTEvent multicaster implementation to allow to forward
** AWT events to multiple listeners;
** not compliant with java 1.2 yet as Serialization methods Save() and SaveInternal() are only provided as stubs.
*/

public class AWTEventMulticaster implements ActionListener, AdjustmentListener, ItemListener, TextListener, MouseListener, MouseMotionListener, KeyListener, FocusListener, ComponentListener, ContainerListener, WindowListener {

  /*
  ** a: the AWTEventListener to cast the events to 
  */
  
  protected final EventListener a;
  
  /*
  ** b: the multicaster next in command to tell to cast the events to his 1) own listener 2) next in command respectingly 
  */
  
  protected final EventListener b;

  /*
  ** constructor
  */
  
  public AWTEventMulticaster(EventListener next_in_command, EventListener own_listener) {
    a = own_listener;
    b = next_in_command;
  }

  /*
  ** internal add / remove implementations
  */

  /*
  ** Add new listener to group
  **    Possible cases
  ** => group is null : return this listener
  ** => group is listener: return this listener
  ** => add listener to (other)listener: return new EventMulticaster containing these two
  ** => group isAWTEventMulticaster and group contains listener: return group
  ** => group isAWTEventMulticaster and doesn't contain listener: new Multicaster containing listener and group
  */
  
  protected static EventListener addInternal(EventListener group, EventListener newlistener) {
    if (group == null || group == newlistener) {
      return newlistener;
    }
    else if (group instanceof AWTEventMulticaster && ((AWTEventMulticaster)group).contains(newlistener)) {
      return group;
    }
    else {
      // group is either a multicaster that doesn't contain the listener yet, or a simple EventListener
      return new AWTEventMulticaster(group, newlistener);
    }
  }

  /*
  ** remove specific listeners from group
  **    Possible cases
  ** => group is null : do nothing / return null
  ** => group is listener, but not old listener: do nothing / return group
  ** => group is current listener: return null
  ** => group is AWTEventMulticaster but doesn't contain listener: do nothing / return group
  ** => group is AWTEventMulticaster and contains listener:
  **     - find the AWTEventMulticaster that contains the listener
  **     - link the group previously pointing to this caster to the group this caster points to
  */
  
  /*
  ** remove specific listener from group
  */
  
  protected static EventListener removeInternal(EventListener group, EventListener oldlistener) {
    if (group == null || group == oldlistener) {
      return null;
    }
    else if (group instanceof AWTEventMulticaster && ((AWTEventMulticaster)group).contains(oldlistener) ) {
      // rebuild the total MultiCaster list the old listener excluded
      return ((AWTEventMulticaster)group).rebuildListExcluding(oldlistener);

    }
    else {
      //group either AWTEventMulticaster or simple event and doesn't contain listener
      return group;
    }
  }

  /*
  ** auxilliary: recursively checks if a specified targeet listener
  ** is contained in the current Multicaster,
  ** (or in one of his Multicaster variables)
  */

  private boolean contains(EventListener target) {
    if (a == target){
      return true;
    }
    else if (b instanceof AWTEventMulticaster) {
      return ((AWTEventMulticaster)b).contains(target);
    }
    else {
      // b also a simple EventListener
      return(b==target);
    }
  }

  /*
  ** auxilliary: recursively builds a new multicaster three
  ** out of the current one EXCLUDING the specified listener
  */

  private EventListener rebuildListExcluding(EventListener target) {
    if (a == target) {
      return b;
    }
    else if (b instanceof AWTEventMulticaster) {
      // with a not equal to the target, b must contain it
      return new AWTEventMulticaster(((AWTEventMulticaster)b).rebuildListExcluding(target), a);
    }
    else {
      // with a not equal to the target, b must BE the target (must contain it as only element <self>) => return a
      return a;
    }

  }

  /*
  ** Serialization
  */
  
  protected void saveInternal(ObjectOutputStream out, String key) throws IOException {
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).saveInternal(out,key);
    }
    else {
      saveEventListener(out,key,a);
    }
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).saveInternal(out,key);
    }
    else {
      saveEventListener(out,key,b);
    }

  }


  protected static void save(ObjectOutputStream out, String key, EventListener target) throws IOException {
    if (target == null) {
    }
    else if (target instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)target).saveInternal(out, key);
    }
    else {
      saveEventListener(out,key,target);
    }
  }

  private static void saveEventListener(ObjectOutputStream out, String key, EventListener target) throws IOException {
    //still have to fill in this code
  }

  /*
  ** Implementation for the different listeners
  ** this class is an interface of
  */
  
  /*
  ** ActionListener implementation
  */
  
  /*
  ** Add new ActionListener
  */

  public static ActionListener add(ActionListener group, ActionListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (ActionListener)addInternal(newlistener,group);
    }
    else {
      return (ActionListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific ActionListener
  */

  public static ActionListener remove(ActionListener group, ActionListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (ActionListener)removeInternal(oldlistener,group);
    }
    else {
      return (ActionListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** ActionListener implementation
  */

  public void actionPerformed(ActionEvent a_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).actionPerformed(a_evt);
    }
    else if (b instanceof ActionListener) {
      ((ActionListener)b).actionPerformed(a_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).actionPerformed(a_evt);
    }
    else if (a instanceof ActionListener) {
      ((ActionListener)a).actionPerformed(a_evt);
    }
  }

  /*
  ** AdjustmentListener implementation
  */
  
  /*
  ** Add new AdjustmentListener
  */

  public static AdjustmentListener add(AdjustmentListener group, AdjustmentListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (AdjustmentListener)addInternal(newlistener,group);
    }
    else {
      return (AdjustmentListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Itemlistener
  */

  public static AdjustmentListener remove(AdjustmentListener group, AdjustmentListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (AdjustmentListener)removeInternal(oldlistener,group);
    }
    else {
      return (AdjustmentListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** AdjustmentListener implementation
  */

  public void adjustmentValueChanged(AdjustmentEvent a_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).adjustmentValueChanged(a_evt);
    }
    else if (b instanceof AdjustmentListener) {
      ((AdjustmentListener)b).adjustmentValueChanged(a_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).adjustmentValueChanged(a_evt);
    }
    else if (a instanceof AdjustmentListener) {
      ((AdjustmentListener)a).adjustmentValueChanged(a_evt);
    }
  }

  /*
  ** ItemListener implementation
  */
  
  /*
  ** Add new ItemListener
  */

  public static ItemListener add(ItemListener group, ItemListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (ItemListener)addInternal(newlistener,group);
    }
    else {
      return (ItemListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Itemlistener
  */

  public static ItemListener remove(ItemListener group, ItemListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (ItemListener)removeInternal(oldlistener,group);
    }
    else {
      return (ItemListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** ItemListener implementation
  */

  public void itemStateChanged(ItemEvent i_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).itemStateChanged(i_evt);
    }
    else if (b instanceof ItemListener) {
      ((ItemListener)b).itemStateChanged(i_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).itemStateChanged(i_evt);
    }
    else if (a instanceof ItemListener) {
      ((ItemListener)a).itemStateChanged(i_evt);
    }
  }

  /*
  ** textListener implementation
  */
  
  /*
  ** Add new TextListener
  */

  public static TextListener add(TextListener group, TextListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (TextListener)addInternal(newlistener,group);
    }
    else {
      return (TextListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Textlistener
  */

  public static TextListener remove(TextListener group, TextListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (TextListener)removeInternal(oldlistener,group);
    }
    else {
      return (TextListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** TextListener implementation
  */

  public void textValueChanged(TextEvent t_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).textValueChanged(t_evt);
    }
    else if (b instanceof TextListener) {
      ((TextListener)b).textValueChanged(t_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).textValueChanged(t_evt);
    }
    else if (a instanceof TextListener) {
      ((TextListener)a).textValueChanged(t_evt);
    }
  }


  /*
  ** KeyListener implementation
  */
  
  /*
  ** Add new KeyListener
  */

  public static KeyListener add(KeyListener group, KeyListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (KeyListener)addInternal(newlistener,group);
    }
    else {
      return (KeyListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Keylistener
  */

  public static KeyListener remove(KeyListener group, KeyListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (KeyListener)removeInternal(oldlistener,group);
    }
    else {
      return (KeyListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** KeyListener implementation keyPressed
  */

  public void keyPressed(KeyEvent k_evt) {
    if(!k_evt.isConsumed())
	{	
		// tell our own listener
		if (a == null) {
		}
		else if (a instanceof AWTEventMulticaster) {
		  ((AWTEventMulticaster)a).keyPressed(k_evt);
		}
		else if (a instanceof KeyListener) {
		  ((KeyListener)a).keyPressed(k_evt);
		}
	}
	  
    if(!k_evt.isConsumed())
	{
		//tell next in command
		if (b == null) {
		}
		else if (b instanceof AWTEventMulticaster) {
		  ((AWTEventMulticaster)b).keyPressed(k_evt);
		}
		else if (b instanceof KeyListener) {
		  ((KeyListener)b).keyPressed(k_evt);
		}
	}
  }

  /*
  ** KeyListener implementation keyReleased
  */

  public void keyReleased(KeyEvent k_evt) {
    if(!k_evt.isConsumed())
	{	
		// tell our own listener
		if (a == null) {
		}
		else if (a instanceof AWTEventMulticaster) {
		  ((AWTEventMulticaster)a).keyReleased(k_evt);
		}
		else if (a instanceof KeyListener) {
		  ((KeyListener)a).keyReleased(k_evt);
		}
	}
	  
    if(!k_evt.isConsumed())
	{	  
		//tell next in command
		if (b == null) {
		}
		else if (b instanceof AWTEventMulticaster) {
		  ((AWTEventMulticaster)b).keyReleased(k_evt);
		}
		else if (b instanceof KeyListener) {
		  ((KeyListener)b).keyReleased(k_evt);
		}
	}	
  }

  /*
  ** KeyListener implementation keyTyped
  */
  public void keyTyped(KeyEvent k_evt) {
    if(!k_evt.isConsumed())
	{	  	
		// tell our own listener
		if (a == null) {
		}
		else if (a instanceof AWTEventMulticaster) {
		  ((AWTEventMulticaster)a).keyTyped(k_evt);
		}
		else if (a instanceof KeyListener) {
		  ((KeyListener)a).keyTyped(k_evt);
		}
	}
	  
    if(!k_evt.isConsumed())
	{	  	  
		//tell next in command
		if (b == null) {
		}
		else if (b instanceof AWTEventMulticaster) {
		  ((AWTEventMulticaster)b).keyTyped(k_evt);
		}
		else if (b instanceof KeyListener) {
		  ((KeyListener)b).keyTyped(k_evt);
		}
	}	
  }


  /*
  ** MouseListener/MouseMotionListener implementation
  */
  
  /*
  ** Add new MouseListener
  */

  public static MouseListener add(MouseListener group, MouseListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (MouseListener)addInternal(newlistener,group);
    }
    else {
      return (MouseListener)addInternal(group, newlistener);
    }
  }

  /*
  ** Add new MouseMotionListener
  */
  
  public static MouseMotionListener add(MouseMotionListener group, MouseMotionListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (MouseMotionListener)addInternal(newlistener,group);
    }
    else {
      return (MouseMotionListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Mouselistener
  */

  public static MouseListener remove(MouseListener group, MouseListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (MouseListener)removeInternal(oldlistener,group);
    }
    else {
      return (MouseListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** remove specific MouseMotionlistener
  */

  public static MouseMotionListener remove(MouseMotionListener group, MouseMotionListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (MouseMotionListener)removeInternal(oldlistener,group);
    }
    else {
      return (MouseMotionListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** MouseListener implementation mouseEntered
  */

  public void mouseEntered(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mouseEntered(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseListener)b).mouseEntered(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mouseEntered(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseListener)a).mouseEntered(m_evt);
    }
  }

  /*
  ** MouseListener implementation mouseExited
  */

  public void mouseExited(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mouseExited(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseListener)b).mouseExited(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mouseExited(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseListener)a).mouseExited(m_evt);
    }
  }

  /*
  ** MouseListener implementation mousePressed
  */

  public void mousePressed(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mousePressed(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseListener)b).mousePressed(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mousePressed(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseListener)a).mousePressed(m_evt);
    }
  }

  /*
  ** MouseListener implementation mouseReleased
  */

  public void mouseReleased(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mouseReleased(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseListener)b).mouseReleased(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mouseReleased(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseListener)a).mouseReleased(m_evt);
    }
  }

  /*
  ** MouseListener implementation mouseClicked
  */

  public void mouseClicked(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mouseClicked(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseListener)b).mouseClicked(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mouseClicked(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseListener)a).mouseClicked(m_evt);
    }
  }

  /*
  ** MouseMotionListener implementation mouseMoved
  */

  public void mouseMoved(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mouseMoved(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseMotionListener)b).mouseMoved(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mouseMoved(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseMotionListener)a).mouseMoved(m_evt);
    }
  }

  /*
  ** MouseMotionListener implementation mouseDragged
  */

  public void mouseDragged(MouseEvent m_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).mouseDragged(m_evt);
    }
    else if (b instanceof MouseListener) {
      ((MouseMotionListener)b).mouseDragged(m_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).mouseDragged(m_evt);
    }
    else if (a instanceof MouseListener) {
      ((MouseMotionListener)a).mouseDragged(m_evt);
    }
  }

  /*
  ** FocusListener implementation
  */
  
  /*
  ** Add new FocusListener
  */
  
  public static FocusListener add(FocusListener group, FocusListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (FocusListener)addInternal(newlistener,group);
    }
    else {
      return (FocusListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Focuslistener
  */

  public static FocusListener remove(FocusListener group, FocusListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (FocusListener)removeInternal(oldlistener,group);
    }
    else {
      return (FocusListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** FocusListener implementation focusGained
  */

  public void focusGained(FocusEvent f_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).focusGained(f_evt);
    }
    else if (b instanceof FocusListener) {
      ((FocusListener)b).focusGained(f_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).focusGained(f_evt);
    }
    else if (a instanceof FocusListener) {
      ((FocusListener)a).focusGained(f_evt);
    }
  }

  /*
  ** FocusListener implementation focusLost
  */
  
  public void focusLost(FocusEvent f_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).focusLost(f_evt);
    }
    else if (b instanceof FocusListener) {
      ((FocusListener)b).focusLost(f_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).focusLost(f_evt);
    }
    else if (a instanceof FocusListener) {
      ((FocusListener)a).focusLost(f_evt);
    }
  }

  /*
  ** ComponentListener implementation
  */
  
  /*
  ** Add new ComponentListener
  */

  public static ComponentListener add(ComponentListener group, ComponentListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (ComponentListener)addInternal(newlistener,group);
    }
    else {
      return (ComponentListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Componentlistener
  */

  public static ComponentListener remove(ComponentListener group, ComponentListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (ComponentListener)removeInternal(oldlistener,group);
    }
    else {
      return (ComponentListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** ComponentListener implementation componentHidden 
  */

  public void componentHidden(ComponentEvent c_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).componentHidden(c_evt);
    }
    else if (b instanceof ComponentListener) {
      ((ComponentListener)b).componentHidden(c_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).componentHidden(c_evt);
    }
    else if (a instanceof ComponentListener) {
      ((ComponentListener)a).componentHidden(c_evt);
    }
  }

  /*
  ** ComponentListener implementation componentHidden 
  */

  public void componentShown(ComponentEvent c_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).componentShown(c_evt);
    }
    else if (b instanceof ComponentListener) {
      ((ComponentListener)b).componentShown(c_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).componentShown(c_evt);
    }
    else if (a instanceof ComponentListener) {
      ((ComponentListener)a).componentShown(c_evt);
    }
  }

  /*
  ** ComponentListener implementation componentMoved 
  */

  public void componentMoved(ComponentEvent c_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).componentMoved(c_evt);
    }
    else if (b instanceof ComponentListener) {
      ((ComponentListener)b).componentMoved(c_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).componentMoved(c_evt);
    }
    else if (a instanceof ComponentListener) {
      ((ComponentListener)a).componentMoved(c_evt);
    }
  }

  /*
  ** ComponentListener implementation componentResized
  */

  public void componentResized(ComponentEvent c_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).componentResized(c_evt);
    }
    else if (b instanceof ComponentListener) {
      ((ComponentListener)b).componentResized(c_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).componentResized(c_evt);
    }
    else if (a instanceof ComponentListener) {
      ((ComponentListener)a).componentResized(c_evt);
    }
  }

  /*
  ** ContainerListener implementation
  */
  
  /*
  ** Add new ContainerListener
  */

  public static ContainerListener add(ContainerListener group, ContainerListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (ContainerListener)addInternal(newlistener,group);
    }
    else {
      return (ContainerListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific Containerlistener
  */

  public static ContainerListener remove(ContainerListener group, ContainerListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (ContainerListener)removeInternal(oldlistener,group);
    }
    else {
      return (ContainerListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** ContainerListener implementation componentAdded
  */

  public void componentAdded(ContainerEvent c_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).componentAdded(c_evt);
    }
    else if (b instanceof ContainerListener) {
      ((ContainerListener)b).componentAdded(c_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).componentAdded(c_evt);
    }
    else if (a instanceof ContainerListener) {
      ((ContainerListener)a).componentAdded(c_evt);
    }
  }

  /*
  ** ContainerListener implementation componentRemoved 
  */

  public void componentRemoved(ContainerEvent c_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).componentRemoved(c_evt);
    }
    else if (b instanceof ContainerListener) {
      ((ContainerListener)b).componentRemoved(c_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).componentRemoved(c_evt);
    }
    else if (a instanceof ContainerListener) {
      ((ContainerListener)a).componentRemoved(c_evt);
    }
  }
  
  /*
  ** WindowListener implementation
  */
  
  /*
  ** Add new WindowListener
  */

  public static WindowListener add(WindowListener group, WindowListener newlistener) {
    if (newlistener == null || newlistener instanceof AWTEventMulticaster) {
      return (WindowListener)addInternal(newlistener,group);
    }
    else {
      return (WindowListener)addInternal(group, newlistener);
    }
  }

  /*
  ** remove specific WindowListener
  */

  public static WindowListener remove(WindowListener group, WindowListener oldlistener) {
    if (oldlistener == null || oldlistener instanceof AWTEventMulticaster) {
      return (WindowListener)removeInternal(oldlistener,group);
    }
    else {
      return (WindowListener)removeInternal(group, oldlistener);
    }
  }

  /*
  ** WindowListener implementation windowOpened
  */

  public void windowOpened(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowOpened(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowOpened(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowOpened(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowOpened(w_evt);
    }
  }

  /*
  ** WindowListener implementation windowClosing
  */

  public void windowClosing(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowClosing(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowClosing(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowClosing(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowClosing(w_evt);
    }
  }

  /*
  ** WindowListener implementation windowClosed 
  */

  public void windowClosed(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowClosed(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowClosed(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowClosed(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowClosed(w_evt);
    }
  }

  /*
  ** WindowListener implementation windowActivated 
  */

  public void windowActivated(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowActivated(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowActivated(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowActivated(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowActivated(w_evt);
    }
  }

  /*
  ** WindowListener implementation windowDeactivated 
  */

  public void windowDeactivated(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowDeactivated(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowDeactivated(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowDeactivated(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowDeactivated(w_evt);
    }
  }

  /*
  ** WindowListener implementation  windowIconified
  */

  public void windowIconified(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowIconified(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowIconified(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowIconified(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowIconified(w_evt);
    }
  }

  /*
  ** WindowListener implementation windowDeiconified
  */

  public void windowDeiconified(WindowEvent w_evt) {
    //tell next in command
    if (b == null) {
    }
    else if (b instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)b).windowDeiconified(w_evt);
    }
    else if (b instanceof WindowListener) {
      ((WindowListener)b).windowDeiconified(w_evt);
    }
    // tell our own listener
    if (a == null) {
    }
    else if (a instanceof AWTEventMulticaster) {
      ((AWTEventMulticaster)a).windowDeiconified(w_evt);
    }
    else if (a instanceof WindowListener) {
      ((WindowListener)a).windowDeiconified(w_evt);
    }
  }

}
