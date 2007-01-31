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

package java.awt.dnd;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.peer.*;

public class DragSource{
  private DragSourceContext dragSourceContext;
  private FlavorMap flavorMap = SystemFlavorMap.getDefaultFlavorMap();
  private static boolean dragImageSupported = false;  //does the system support image drag

  public static final Cursor DefaultCopyDrop = Toolkit.getDefaultToolkit().createCustomCursor(null, new Point(32,32), "CopyDrop32x32");
  public static final Cursor DefaultMoveDrop = Toolkit.getDefaultToolkit().createCustomCursor(null, new Point(32,32), "MoveDrop32x32");
  public static final Cursor DefaultLinkDrop = Toolkit.getDefaultToolkit().createCustomCursor(null, new Point(32,32), "LinkDrop32x32");
  public static final Cursor DefaultCopyNoDrop = Toolkit.getDefaultToolkit().createCustomCursor(null, new Point(32,32), "CopyNoDrop32x32");
  public static final Cursor DefaultMoveNoDrop = Toolkit.getDefaultToolkit().createCustomCursor(null, new Point(32,32), "MoveNoDrop32x32");
  public static final Cursor DefaultLinkNoDrop = Toolkit.getDefaultToolkit().createCustomCursor(null, new Point(32,32), "LinkNoDrop32x32");

  public DragSource(){
    ;
  }

  public static DragSource getDefaultDragSource(){
    return new DragSource();
  }

  public static boolean isDragImageSupported(){
    return dragImageSupported;
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor,
                      Image dragImage, Point imageOffset, Transferable transferable,
                      DragSourceListener dsl, FlavorMap flavorMap)
      throws InvalidDnDOperationException{
    this.flavorMap = flavorMap;
    DragSourceContextPeer dscp = Toolkit.getDefaultToolkit().createDragSourceContextPeer(trigger);
    dragSourceContext=createDragSourceContext(dscp, trigger, dragCursor, dragImage,
                                                    imageOffset, transferable, dsl);
    dscp.startDrag(dragSourceContext, dragCursor, dragImage, imageOffset);
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor,
                      Transferable transferable, DragSourceListener dsl, FlavorMap flavorMap)
       throws InvalidDnDOperationException{
    this.flavorMap = flavorMap;
    DragSourceContextPeer dscp = Toolkit.getDefaultToolkit().createDragSourceContextPeer(trigger);
    Image dragImage = null;
    Point imageOffset = null;
    dragSourceContext=createDragSourceContext(dscp, trigger, dragCursor, dragImage,
                                                    imageOffset, transferable, dsl);
    dscp.startDrag(dragSourceContext, dragCursor, dragImage, imageOffset);
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor, Image dragImage,
      Point dragOffset, Transferable transferable, DragSourceListener dsl)
      throws InvalidDnDOperationException{
    DragSourceContextPeer dscp = Toolkit.getDefaultToolkit().createDragSourceContextPeer(trigger);
    Point imageOffset = null;
    dragSourceContext=createDragSourceContext(dscp, trigger, dragCursor, dragImage,
                                                    imageOffset, transferable, dsl);
    dscp.startDrag(dragSourceContext, dragCursor, dragImage, imageOffset);
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor,
      Transferable transferable, DragSourceListener dsl)
      throws InvalidDnDOperationException{
    DragSourceContextPeer dscp = Toolkit.getDefaultToolkit().createDragSourceContextPeer(trigger);
    Image dragImage = null;
    Point imageOffset = null;
    dragSourceContext=createDragSourceContext(dscp, trigger, dragCursor, dragImage,
                                                    imageOffset, transferable, dsl);
    dscp.startDrag(dragSourceContext, dragCursor, dragImage, imageOffset);
  }

  protected DragSourceContext createDragSourceContext(java.awt.dnd.peer.DragSourceContextPeer dscp,
                                                    DragGestureEvent dgl, Cursor dragCursor, Image dragImage,
                                                    Point imageOffset, Transferable t, DragSourceListener dsl){
    return new DragSourceContext(dscp, dgl, dragCursor, dragImage, imageOffset, t, dsl);
  }

  public FlavorMap getFlavorMap(){
    return flavorMap;
  }

  public DragGestureRecognizer createDragGestureRecognizer(Class recognizerAbstractClass,
                                                         Component c, int actions, DragGestureListener dgl){
    return (Toolkit.getDefaultToolkit()).createDragGestureRecognizer(recognizerAbstractClass,this,c,actions,dgl);
  }

  public DragGestureRecognizer createDefaultDragGestureRecognizer(Component c, int actions,
                                                                DragGestureListener dgl){
    return (Toolkit.getDefaultToolkit()).createDragGestureRecognizer(MouseDragGestureRecognizer.class,this,c,actions,dgl);
  }
}
