/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

package java.awt;

import java.util.*;
import java.awt.peer.*;
import java.awt.event.*;

public class Container extends Component {

  private LayoutManager layoutManager;

  private Vector children;

  ContainerListener containerListener;

  boolean focusCycleRoot;

  private boolean focusTraversalPolicyProvider;

  private FocusTraversalPolicy focusTraversalPolicy;

  boolean isRemoved; // set to true/false on removeNotify() enter/exit


  public Container() {
    // Assign default container size:
    width = 0;
    height = 0;

    // Setup component vector:
    children = new Vector();

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

    toolkit.lockAWT();
    try {

      // if(children.contains(component)) {
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
        children.addElement(component);
      }
      else {
        children.insertElementAt(component, position);
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

    } finally {
      toolkit.unlockAWT();
    }
  }
  
  public void addNotify() {
    /*
    Component c;
    for (int i = 0; i < getComponentCount(); i++) {
      c = (Component)children.elementAt(i);
      c.addNotify();
    }
    */

    if(notified == false) {
      super.addNotify();
    }
  }
 
  public void removeNotify() {
    Component c;
    toolkit.lockAWT();
    try {
      isRemoved = true;
 
      for (int i = 0; i < getComponentCount(); i++) {
        c = (Component)children.elementAt(i);
        c.removeNotify();
      }
      super.removeNotify();
    } finally {
      isRemoved = false;
      toolkit.unlockAWT();
    }
  }
 
  public void doLayout() {
    toolkit.lockAWT();
    try {
      if (layoutManager != null) {
        layoutManager.layoutContainer(this);
      }
    } finally {
      toolkit.unlockAWT();
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
    return (Component)children.elementAt(index);
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
    return children.size();
  }
  
  public Component[] getComponents() {
    Component[] components = new Component[this.children.size()];
    return (Component[])(this.children.toArray(components));
  }

  public Insets getInsets() {
    return new Insets(0, 0, 0, 0);
  }
  
  public LayoutManager getLayout() {
    return layoutManager;
  }

  public Dimension getMaximumSize() {
    toolkit.lockAWT();
    try {
      if (maximumSize == null || valid == false) {
        if (layoutManager != null && layoutManager instanceof LayoutManager2) {
          maximumSize = ((LayoutManager2)layoutManager).maximumLayoutSize(this);
        }
        else {
          maximumSize = super.getMaximumSize();
        }
      }
      return maximumSize;
    } finally {
      toolkit.unlockAWT();
    }
  }

  /**
   * @status  implemented
   * @remark  compliant with the specification
   */

  public Dimension getMinimumSize() {
    toolkit.lockAWT();
    try {
      if (minSize == null || valid == false) {
        if (layoutManager != null) {
          minSize = layoutManager.minimumLayoutSize(this);
        }
        else {
          minSize = getBoundingBox();
        }
      }      
      return minSize;
    } finally {
      toolkit.unlockAWT();
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
      c = (Component)children.elementAt(i);
      
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
    toolkit.lockAWT();
    try {
      if (prefSize == null || valid == false) {
        if (layoutManager != null) {
          prefSize = layoutManager.preferredLayoutSize(this);
        }
        else {
          prefSize = getBoundingBox();
        }
      }
      return prefSize;
    } finally {
      toolkit.unlockAWT();
    }
  }
  
  void invalidateChildren() {
    Component c;
    for (int i = 0; i < getComponentCount(); i++) {
      c = (Component)children.elementAt(i);
      c.invalidate();
      if(c instanceof Container) {
        ((Container)c).invalidateChildren();
      }
    }
  }
  
  public void invalidate() {
    toolkit.lockAWT();
    try {
      super.invalidate();
      if (layoutManager != null) {
        if (layoutManager instanceof LayoutManager2) {
          ((LayoutManager2)layoutManager).invalidateLayout(this);
        }
      }
    } finally {
      toolkit.unlockAWT();
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
    toolkit.lockAWT();
    try {

      // Remove component from layout manager (if any):
      if (layoutManager != null) {
        layoutManager.removeLayoutComponent(component);
      }

      // Remove component from children:
      if (children.removeElement(component)) {

        // Set parent component to null:
        component.parent = null;

        // Invalidate container:
        if (valid) {
          invalidate();
        }
        
        // throw ContainerEvent to listener
        super.dispatchEventImpl(new ContainerEvent(this, ContainerEvent.COMPONENT_REMOVED, component));
      }
    } finally {
      toolkit.unlockAWT();
    }
  }
  
  public void remove(Component component) {
    removeComponent(component);
  }

  public void remove(int position) {
    removeComponent((Component) children.elementAt(position));
  }
  
  public void removeAll() {
    int size = children.size();
    for (int i = 0; i < size; i++) {
      removeComponent((Component) children.elementAt(0));
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
      toolkit.lockAWT();
      try {
        ((ContainerPeer)peer).beginValidate();

        validateTree();

        ((ContainerPeer)peer).endValidate();
      } finally {
        toolkit.unlockAWT();
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
      for (int i = 0; i < children.size(); i++) {
        ((Component)children.elementAt(i)).validate();
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
    for (int i = 0; i < children.size(); i++) {
      Component c = (Component)children.elementAt(i);

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
    for (int i = 0; i < children.size(); i++) {
      Component c = (Component)children.elementAt(i);
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
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

    public boolean isFocusCycleRoot(Container container) {
      toolkit.lockAWT();
      try {
        if (focusCycleRoot && container == this) {
          return true;
        }
        return super.isFocusCycleRoot(container);
      } finally {
        toolkit.unlockAWT();
      }
    }

    public boolean isFocusCycleRoot() {
        toolkit.lockAWT();
        try {
            return focusCycleRoot;
        } finally {
            toolkit.unlockAWT();
        }
    }

   public final boolean isFocusTraversalPolicyProvider() {
        return focusTraversalPolicyProvider;
    }

    public FocusTraversalPolicy getFocusTraversalPolicy() {
        toolkit.lockAWT();
        try {
            if (isFocusTraversalPolicyProvider() || focusCycleRoot) {
                if (isFocusTraversalPolicySet()) {
                    return focusTraversalPolicy;
                }
                Container root = getFocusCycleRootAncestor();
                return ((root != null) ? root.getFocusTraversalPolicy() : KeyboardFocusManager
                        .getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy());
            }
            return null;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public boolean isFocusTraversalPolicySet() {
        toolkit.lockAWT();
        try {
            return focusTraversalPolicy != null;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void setFocusTraversalPolicy(FocusTraversalPolicy policy) {
        FocusTraversalPolicy oldPolicy;
        toolkit.lockAWT();
        try {
            oldPolicy = focusTraversalPolicy;
            focusTraversalPolicy = policy;
        } finally {
            toolkit.unlockAWT();
        }
        // TODO
        // firePropertyChange("focusTraversalPolicy", oldPolicy, policy); //$NON-NLS-1$
    }

    public Set getFocusTraversalKeys(int id) {
        toolkit.lockAWT();
        try {
            return super.getFocusTraversalKeys(id);
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void transferFocusDownCycle() {
        toolkit.lockAWT();
        try {
            if (isFocusCycleRoot()) {
                KeyboardFocusManager kfm = KeyboardFocusManager
                        .getCurrentKeyboardFocusManager();
                Container root = kfm.getCurrentFocusCycleRoot();
                FocusTraversalPolicy policy = getFocusTraversalPolicy();
                if (root != this) {
                    root = this;
                    kfm.setGlobalCurrentFocusCycleRoot(root);

                }
                policy.getDefaultComponent(root).requestFocus();
            }
        } finally {
            toolkit.unlockAWT();
        }
    }

    /**
     * Find which focus cycle root to take when doing keyboard focus traversal
     * and focus owner is a container & focus cycle root itself.
     * 
     * @return
     */
    Container getFocusTraversalRoot() {
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Container root = kfm.getCurrentFocusCycleRoot();
        Container container = this;
        while ((root != container) && (container != null)) {
            container = container.getFocusCycleRootAncestor();
        }
        return (container == root) ? root : null;
    }


    int getComponentIndex(Component comp) {
        return children.indexOf(comp);
    }



}
