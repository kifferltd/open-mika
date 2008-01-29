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

import java.util.*;
import java.awt.peer.*;
import java.awt.event.*;

public class Container extends Component {

  private LayoutManager layoutManager;
  private Vector componentVector;

  ContainerListener containerListener;

  public Container() {
    // Assign default container size:
    width = 0;
    height = 0;

    // Setup component vector:
    componentVector = new Vector();

    // Set the default layout manager:
    layoutManager = new BorderLayout();

    // Set the default listener (none):
    containerListener = null;
  }

  public Component add(Component component) {
    addImpl(component, null, -1);
    return component;
  }

  public void add(Component component, Object constraints) {
    addImpl(component, constraints, -1);
  }
  
  /**
   * @status  implemented
   * @remark  The third parameter 'int position' is currently ignored
   */
  public void add(Component component, Object constraints, int position) {
    addImpl(component, constraints, position);
  }

  /**
   * @status  implemented
   * @remark  The third parameter 'int position' is currently ignored
   */
  public Component add(Component component, int position) {
    addImpl(component, null, position);
    return component;
  }

  /**
   * @status  implemented
   * @remark  If the layout manager associated with this container, is of type
   * 'LayoutManager2', the container notifies the layout manager of the addition
   * by calling the layout manager's method 'addLayoutComponent(Component, Object)'
   * and not its method 'addLayoutComponent(String, Component)'. The effect is the
   * same (nothing is done) except that an IllegalArgumentException will be thrown.
   * In fact this is logical since a call of the present method with a LayoutManager2
   * can be considered as a programming error.
   */
  public Component add(String name, Component component) {
    addImpl(component, name, -1);
    return component;
  }

  /**
   * @status  implemented to AWTEventMulticaster functionality
   */
  public synchronized void addContainerListener(java.awt.event.ContainerListener listener) {
    containerListener = AWTEventMulticaster.add(containerListener, listener);
  }

  /**
   * @status  implemented
   * @remark  fully EventMulticaster-compliant throwing componentAdded(ContainerEvent) to all subscribed listeners
   */   
  protected void addImpl(Component component, Object constraints, int position) {

    if ((component instanceof Component) == false) {
      throw new IllegalArgumentException("You can only add types derived from java.awt.Component.");
    }

    synchronized(getTreeLock()) {

      // if(componentVector.contains(component)) {
      //   return;
      // }
      
      // Reparent the component and tidy up the tree's state:
      if (component.parent != null) {
         component.parent.removeComponent(component);
      }

      component.addNotify();

      // Add component to underlying native data structure:
      if (component instanceof Container) {
        // Check whether is an instance of java.awt.Window:
        if (component instanceof Window) {
          System.out.println("You can't add a java.awt.Window to a java.awt.Container.");
          throw new IllegalArgumentException("You can't add a java.awt.Window to a java.awt.Container.");
        }

        // Check whether we don't create component loops:
        for (Container container = this; container != null; container = container.parent) {
          if (container == component) throw new IllegalArgumentException("Container.addImpl(): attempt container's parent to itself.");
        }
      }

      // Add component to data structure, taking position into account:
      if (position == -1) {
        componentVector.addElement(component);
      }
      else {
        componentVector.insertElementAt(component, position);
      }

      // Set parent:
      component.parent = this;
 
      // Set background color:
      // if (fg == false && parent.background != n component.background = parent.background;

      // Notify layoutmanger of added component:
      if (layoutManager != null) {
        if (layoutManager instanceof LayoutManager2) {
          ((LayoutManager2)layoutManager).addLayoutComponent(component, constraints);
        }
        else {
          layoutManager.addLayoutComponent((String)constraints, component);
        }
      }

      // throw ContainerEvent to listener
      super.dispatchEventImpl(new ContainerEvent(this, ContainerEvent.COMPONENT_ADDED, component));
      
      // Redo the layout:
      if (valid) {
        invalidate();
      }
     
      /*
      ** Normally addNotify will invalidate the children of a container when it's added 
      ** to the component tree. Since we don't have this mechanism yet, we've added 
      ** this obscure method to overcome this problem. It should be removed when addNotify
      ** does 'The Right Thing(tm)'
      */
      
      invalidateChildren();

    }
  }
  
  public void addNotify() {
    /*
    Component c;
    for (int i = 0; i < getComponentCount(); i++) {
      c = (Component)componentVector.elementAt(i);
      c.addNotify();
    }
    */

    if(notified == false) {
      super.addNotify();
    }
  }
 
  public void removeNotify() {
    Component c;

    for (int i = 0; i < getComponentCount(); i++) {
      c = (Component)componentVector.elementAt(i);
      c.removeNotify();
    }

    super.removeNotify();
  }
 
  public void doLayout() {
    synchronized (getTreeLock()) {
      if (layoutManager != null) {
        layoutManager.layoutContainer(this);
      }
    }
  }

  public void layout() {
    doLayout();
  }

  /**
   * @status  Compliant to specs
   * @remark  when using LayoutManager2 type manager, maps to LayoutManager2.getLayoutAlignmentX() (which is not implemented yet)
   */
  public float getAlignmentX() {
    if(layoutManager == null || !(layoutManager instanceof LayoutManager2)) {
      // by default every component, this includes our container,is center.aligned
      // return super.getAalignmentX();
      return Component.CENTER_ALIGNMENT;
    }
    else {
      //layoutManager is LayoutManager2 => get the manager's alignment
      return ((LayoutManager2)layoutManager).getLayoutAlignmentX(this);
    }
  }

  /**
   * @status  Compliant to specs
   * @remark  when using LayoutManager2 type manager, maps to LayoutManager2.getLayoutAlignmentY() (which is not implemented yet)
   */
  public float getAlignmentY() {
    if (layoutManager == null || !(layoutManager instanceof LayoutManager2)) {
      // by default every component, this includes our container,is center.aligned
      // return super.getAalignmentX();
      return Component.CENTER_ALIGNMENT;
    }
    else {
      //layoutManager is LayoutManager2 => get the manager's alignment
      return ((LayoutManager2)layoutManager).getLayoutAlignmentY(this);
    }
  }

  public Component getComponent(int index) {
    return (Component)componentVector.elementAt(index);
  }
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */
  native public Component getComponentAt(int x, int y);

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  native public Component getComponentAt(Point point);
  
  public int getComponentCount() {
    return componentVector.size();
  }
  
  public Component[] getComponents() {
    Component[] components = new Component[this.componentVector.size()];
    return (Component[])(this.componentVector.toArray(components));
  }

  public Insets getInsets() {
    return new Insets(0, 0, 0, 0);
  }
  
  public LayoutManager getLayout() {
    return layoutManager;
  }

  public Dimension getMaximumSize() {
    synchronized (getTreeLock()) {
      if (maximumSize == null || valid == false) {
        if (layoutManager != null && layoutManager instanceof LayoutManager2) {
          maximumSize = ((LayoutManager2)layoutManager).maximumLayoutSize(this);
        }
        else {
          maximumSize = super.getMaximumSize();
        }
      }
      return maximumSize;
    }
  }

  /**
   * @status  implemented
   * @remark  compliant with the specification
   */

  public Dimension getMinimumSize() {
    synchronized(getTreeLock()) {
      if (minSize == null || valid == false) {
        if (layoutManager != null) {
          minSize = layoutManager.minimumLayoutSize(this);
        }
        else {
          minSize = getBoundingBox();
        }
      }      
      return minSize;
    }
  }

  private Dimension getBoundingBox() {
    Component c = null;
    int count = getComponentCount();
    int wmax = 0;
    int hmax = 0;;
    int w;
    int h;
    int i;
    
    for (i = 0; i < count; i++) {
      c = (Component)componentVector.elementAt(i);
      
      Dimension s = c.getSize();  
      Dimension d = c.getMinimumSize();  
      
      w = c.getX() + (int)Math.max(d.getWidth(), s.getWidth());
      h = c.getY() + (int)Math.max(d.getHeight(), s.getHeight());
      
      if (w > wmax) {
        wmax = w;
      }
      
      if (h > hmax) {
        hmax = h;
      }
    }
    return new Dimension (wmax + getInsets().right, hmax + getInsets().bottom);
  }

  public Dimension getPreferredSize() {
    synchronized(getTreeLock()) {
      if (prefSize == null || valid == false) {
        if (layoutManager != null) {
          prefSize = layoutManager.preferredLayoutSize(this);
        }
        else {
          prefSize = getBoundingBox();
        }
      }
      return prefSize;
    }
  }
  
  void invalidateChildren() {
    Component c;
    for (int i = 0; i < getComponentCount(); i++) {
      c = (Component)componentVector.elementAt(i);
      c.invalidate();
      if(c instanceof Container) {
        ((Container)c).invalidateChildren();
      }
    }
  }
  
  public void invalidate() {
    synchronized (getTreeLock()) {
      super.invalidate();
      if (layoutManager != null) {
        if (layoutManager instanceof LayoutManager2) {
          ((LayoutManager2)layoutManager).invalidateLayout(this);
        }
      }
    }
  }
 
  /**
   * Is ancester of given component :
   * either is components parent, or is parent of parent of... of components parent
   * recursively: is ancestor when either is components parent or (parent excist and) is ancestor of components parent
   */
  public boolean isAncestorOf(Component component) {
    Container parent = component.getParent();
    if (parent == null) {
      return false;
    }
    else if (parent == this) {
      return true;
    }
    else {
      return isAncestorOf(parent);
    }
  }


  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  native public void list(java.io.PrintStream out, int indent);

  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  native public void list(java.io.PrintWriter out, int indent);
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  native public void paintComponents(Graphics context);

  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  native protected String paramString();

  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  native public void print(Graphics context);

  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  native public void printComponents(Graphics context);
  
  protected void processContainerEvent(java.awt.event.ContainerEvent event) {
    if (containerListener != null) {
      switch(event.getID()) {
        case ContainerEvent.COMPONENT_ADDED:
          containerListener.componentAdded(event);
          break;
        case ContainerEvent.COMPONENT_REMOVED:
          containerListener.componentRemoved(event);
          break;
      }
    }
  }
  
  protected void processEvent(AWTEvent event) {
    if (event instanceof ContainerEvent) {   
      processContainerEvent((ContainerEvent) event);
    }
    else {
      super.processEvent(event);
    }
  }
  
  /**
   * @status  implemented
   * @remark  fully EventMulticaster-compliant throwing componentAdded(ContainerEvent) to all subscribed listeners
   */


  private void removeComponent(Component component) {
    synchronized (getTreeLock()) {

      // Remove component from layout manager (if any):
      if (layoutManager != null) {
        layoutManager.removeLayoutComponent(component);
      }

      // Remove component from componentVector:
      if (componentVector.removeElement(component)) {

        // Set parent component to null:
        component.parent = null;

        // Invalidate container:
        if (valid) {
          invalidate();
        }
        
        // throw ContainerEvent to listener
        super.dispatchEventImpl(new ContainerEvent(this, ContainerEvent.COMPONENT_REMOVED, component));
      }
    }
  }
  
  public void remove(Component component) {
    removeComponent(component);
  }

  public void remove(int position) {
    removeComponent((Component) componentVector.elementAt(position));
  }
  
  public void removeAll() {
    int size = componentVector.size();
    for (int i = 0; i < size; i++) {
      removeComponent((Component) componentVector.elementAt(0));
        // NOTE: we have to use "removeComponent()" rather than "remove()" because "remove()" can be overwritten.
    }
  }

  /**
   * @status  implemented to AWTEventMulticaster functionality
   */
  public synchronized void removeContainerListener(java.awt.event.ContainerListener listener) {
    containerListener = AWTEventMulticaster.remove(containerListener, listener);
  }

  public void setLayout(LayoutManager manager) {
    layoutManager = manager;

    if (valid) {
      invalidate();
    }
  }
  
  public void validate() {
    if (valid == false) {
      synchronized (getTreeLock()) {
        ((ContainerPeer)peer).beginValidate();

        validateTree();

        ((ContainerPeer)peer).endValidate();
      }
    }
  }

  public void validateTree() {
    if (valid == false) {
      // Tell the renderer we are busy validating:
      validate = true;

      // Validate the container itself:
      valid = true;

      // Layout the container (if required):
      layout();

      // Validate components of Container and validate nested Containers:
      for (int i = 0; i < componentVector.size(); i++) {
        ((Component)componentVector.elementAt(i)).validate();
      } 

      // Tell the renderer we are done validating:
      validate = false;
    }
  }

  public void setVisible(boolean condition) {
    super.setVisible(condition);
    sendComponentEvent(this, condition);
  }

  private void sendComponentEvent(Container container, boolean condition) {
    if(condition) {
      int count = container.getComponentCount(); 
      for(int i=0; i<count; i++) {
        Component c = container.getComponent(i);
        if(c.visible) {
          if(c.componentListener != null) {
            c.dispatchEvent(new ComponentEvent(c, ComponentEvent.COMPONENT_SHOWN));
          }
          if(c instanceof Container) {
            sendComponentEvent((Container)c, condition);
          }
        }
      }
    }
  }
  
  void enableAllEvents() {
    eventsEnabled = true;
    for (int i = 0; i < componentVector.size(); i++) {
      Component c = (Component)componentVector.elementAt(i);

      if(c instanceof Container) {
        ((Container)c).enableAllEvents();
      }
      else {
        c.eventsEnabled = true;
      }
    } 
  }

  void disableAllEvents() {
    eventsEnabled = false;
    for (int i = 0; i < componentVector.size(); i++) {
      Component c = (Component)componentVector.elementAt(i);
      if(c instanceof Container) {
        ((Container)c).disableAllEvents();
      }
      else {
        c.eventsEnabled = false;
      }
    } 
  }

  /*
  ** The following methods are deprecated...
  */

  public int countComponents() {
    return getComponentCount();
  }

  public Insets insets() {
    return getInsets();
  }
  
  private void readObject(java.io.ObjectInputStream s) throws ClassNotFoundException, java.io.IOException {
    System.out.println("Not yet implemented");
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    System.out.println("Not yet implemented");
  }

}
