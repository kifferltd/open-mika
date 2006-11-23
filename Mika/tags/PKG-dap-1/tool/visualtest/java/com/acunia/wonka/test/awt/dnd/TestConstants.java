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
package com.acunia.wonka.test.awt.dnd;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class TestConstants extends VisualTestImpl{
  private TextArea tekstScherm;
  public TestConstants() {
    setLayout(new GridLayout(1,1));
    tekstScherm = new TextArea();
    add(tekstScherm);
    VulHetSchermMetConstanten();
    setVisible(true);
  }
  
  public String getHelpText(){
    return "";
  }

  private void VulHetSchermMetConstanten(){try{
/*    tekstScherm.append("DnDConstants.ACTION_NONE = "+DnDConstants.ACTION_NONE+'\n');
    tekstScherm.append("DnDConstants.ACTION_COPY = "+DnDConstants.ACTION_COPY+'\n');
    tekstScherm.append("DnDConstants.ACTION_MOVE = "+DnDConstants.ACTION_MOVE+'\n');
    tekstScherm.append("DnDConstants.ACTION_COPY_OR_MOVE = "+DnDConstants.ACTION_COPY_OR_MOVE+'\n');
    tekstScherm.append("DnDConstants.ACTION_LINK = "+DnDConstants.ACTION_LINK+'\n');
    tekstScherm.append("DnDConstants.ACTION_REFERENCE = "+DnDConstants.ACTION_REFERENCE+'\n');
    tekstScherm.append("DragSource.DefaultCopyDrop.toString() = "+(DragSource.DefaultCopyDrop).toString()+'\n');
    tekstScherm.append("DragSource.DefaultMoveDrop.getName() = "+(DragSource.DefaultMoveDrop).getName()+'\n');
    tekstScherm.append("DragSource.DefaultLinkDrop = "+(DragSource.DefaultLinkDrop)+'\n');
    tekstScherm.append("DragSource.DefaultCopyNoDrop = "+(DragSource.DefaultCopyNoDrop)+'\n');
    tekstScherm.append("DragSource.DefaultMoveNoDrop = "+(DragSource.DefaultMoveNoDrop)+'\n');
    tekstScherm.append("DragSource.DefaultLinkNoDrop = "+(DragSource.DefaultLinkNoDrop)+'\n');
    for(int i=0; i<14; i++){
      tekstScherm.append("new Cursor("+i+") = "+(new Cursor(i))+'\n');
      System.out.println("new Cursor("+i+") = "+(new Cursor(i)));
    }

*/
    tekstScherm.append('\n'+"DragSource uitkammen"+'\n');
    DragSource dragSource = new DragSource();
    tekstScherm.append("dragSource.isDragImageSupported() = "+DragSource.isDragImageSupported()+'\n');
    tekstScherm.append("dragSource.getFlavorMap() = "+dragSource.getFlavorMap()+'\n');
    dragSource = DragSource.getDefaultDragSource();
    tekstScherm.append("dragSource = DragSource.getDefaultDragSource()"+DragSource.getDefaultDragSource()+'\n');
    tekstScherm.append("dragSource.isDragImageSupported() = "+DragSource.isDragImageSupported()+'\n');
    tekstScherm.append("dragSource.getFlavorMap() = "+dragSource.getFlavorMap()+'\n');
    tekstScherm.append("DragSourceContext2.getDefault() = "+DragSourceContext2.getDefault()+'\n');
    tekstScherm.append("DragSourceContext2.getEnter() = "+DragSourceContext2.getEnter()+'\n');
    tekstScherm.append("DragSourceContext2.getOver() = "+DragSourceContext2.getOver()+'\n');
    tekstScherm.append("DragSourceContext2.getChanged() = "+DragSourceContext2.getChanged()+'\n'+'\n');

    tekstScherm.append('\n'+"DragGestureRecognizer uitkammen"+'\n');
    Button button = new Button("button");
    button.addActionListener(new AcListener());
    DragGestureRecognizer dgr;
    DGL dgl = new DGL();
    try {
      dgr = dragSource.createDragGestureRecognizer(DragGestureRecognizer.class,button, 3, dgl);
      if(dgr!=null){
        tekstScherm.append("dgr.getClass() = "+dgr.getClass()+'\n');
        tekstScherm.append("dgr.getComponent() ="+dgr.getComponent()+'\n');
        tekstScherm.append("dgr.getDragSource() ="+dgr.getDragSource()+'\n');
        tekstScherm.append("dgr.getSourceActions() ="+dgr.getSourceActions()+'\n');
        tekstScherm.append("dgr.getTriggerEvent() ="+dgr.getTriggerEvent()+'\n');

        tekstScherm.append("aanmaken ArrayList aList"+'\n');
        java.util.ArrayList aList = new ArrayList();
        MouseEvent mouseEvent = new MouseEvent(dgr.getComponent(), MouseEvent.MOUSE_CLICKED, 15, MouseEvent.CTRL_MASK,
                                15, 15, 0, false);
        aList.add(mouseEvent);
//        aList.add(dgr.getTriggerEvent());
        tekstScherm.append("aanmaken DragGestureEvent dge"+'\n');
        DragGestureEvent dge = new DragGestureEvent(dgr, 2, new Point(20,5), aList);
        tekstScherm.append("aanmaken DragSourceListener2 dsl"+'\n');
        DragSourceListener2 dsl = new DragSourceListener2();
        tekstScherm.append("bij dgl dragGestureRecognized oproepen"+'\n');
        dgl.dragGestureRecognized(dge);
        tekstScherm.append("dragSource.startDrag(...)"+'\n');
        dragSource.startDrag(dge, DragSource.DefaultCopyDrop, new StringSelection("voorbeeldje"), dsl);
      }
      else tekstScherm.append("dgr is null");}
    catch(NullPointerException npe){tekstScherm.append("NullPointerException bij dgr: "+npe.getMessage()+'\n');}
    catch(IllegalArgumentException iae){tekstScherm.append("IllegalArgumentException bij dgr: "+iae.getMessage()+'\n');}
    catch(InvalidDnDOperationException idndoe){tekstScherm.append("InvalidDnDOperationException bij dgr: "+idndoe.getMessage()+'\n');}
    catch(Exception e){tekstScherm.append("Exception bij dgr: "+e.getMessage()+'\n');}

    tekstScherm.append('\n'+"DropTarget uitkammen"+'\n');
    DropTarget droptarget = new DropTarget();
    tekstScherm.append("droptarget.getComponent() = "+droptarget.getComponent()+'\n');
    tekstScherm.append("droptarget.getDefaultActions() = "+droptarget.getDefaultActions()+'\n');
    tekstScherm.append("droptarget.isActive() = "+droptarget.isActive()+'\n');
    tekstScherm.append("droptarget.getFlavorMap() = "+droptarget.getFlavorMap()+'\n');
    tekstScherm.append("droptarget.getFlavorMap().encodeDataFlavor(DataFlavor.stringFlavor) = "+SystemFlavorMap.encodeDataFlavor(DataFlavor.stringFlavor)+'\n');
    tekstScherm.append("droptarget.getDropTargetContext() = "+droptarget.getDropTargetContext()+'\n');
    tekstScherm.append("droptarget.addDropTargetListener(new DTL) = "+'\n');
    try{
      droptarget.addDropTargetListener(new DTL());
      tekstScherm.append("addDropTargetListener gelukt, dus er was geen listener aanwezig"+'\n');
    } catch(TooManyListenersException tmle){tekstScherm.append("addDropTargetListener mislukt, dus een listener aanwezig: "+tmle.getMessage()+'\n');}
    tekstScherm.append("droptarget.addDropTargetListener(new DTL) = "+'\n');
    try{
      droptarget.addDropTargetListener(new DTL());
      tekstScherm.append("addDropTargetListener gelukt, dus er was geen listener aanwezig"+'\n');
    } catch(TooManyListenersException tmle){tekstScherm.append("addDropTargetListener mislukt, dus een listener aanwezig: "+tmle.getMessage()+'\n');}

    DropTargetContext dtc = droptarget.getDropTargetContext();
    tekstScherm.append("dtc.getDropTarget() = "+dtc.getDropTarget()+'\n');
    tekstScherm.append("dtc.getComponent() = "+dtc.getComponent()+'\n'+'\n'+'\n');

    DropTargetDragEvent dtde = new DropTargetDragEvent(dtc, new Point(10,10), 2, 3);
    DropTargetDropEvent dtdrope = new DropTargetDropEvent(dtc, new Point(15,15), 2, 3);
    DropTargetEvent dte = new DropTargetEvent(dtc);
    tekstScherm.append("droptarget.dragEnter(dtde) = "+'\n');
    droptarget.dragEnter(dtde);
    tekstScherm.append("droptarget.dragOver(dtde) = "+'\n');
    droptarget.dragOver(dtde);
    tekstScherm.append("droptarget.dropActionChanged(dtde) = "+'\n');
    droptarget.dropActionChanged(dtde);
    tekstScherm.append("droptarget.dragExit(dte) = "+'\n');
    droptarget.dragExit(dte);
    tekstScherm.append("droptarget.drop(dtdrope) = "+'\n');
    droptarget.drop(dtdrope);


    tekstScherm.append('\n'+"Toolkit uitkammen"+'\n');
    Toolkit toolkit = (new Button()).getToolkit();
    tekstScherm.append("toolkit.getClass() = "+toolkit.getClass()+'\n');
    
  }catch(Exception e){ tekstScherm.append("Exception : "+e.getMessage());}
  }

  public class DGL implements DragGestureListener{
    public DGL(){
      ;
    }

    public void dragGestureRecognized(DragGestureEvent dge){
      dge.startDrag(null,new StringSelection("voorbeeldje van DragGestureListener"), null);
    }
  }

  public class DragSourceListener2 implements DragSourceListener{
    public DragSourceListener2(){
      ;
    }

    public void dragEnter(DragSourceDragEvent dsde){
    }
    public void dragOver(DragSourceDragEvent dsde){
    }
    public void dropActionChanged(DragSourceDragEvent dsde){
    }
    public void dragExit(DragSourceEvent dse){
    }
    public void dragDropEnd(DragSourceDropEvent dsde){
    }
  }

  public class DTL implements DropTargetListener{
    public DTL(){
      ;
    }
    public void dragEnter(DropTargetDragEvent dtde){
    }
    public void dragOver(DropTargetDragEvent dtde){
    }
    public void dropActionChanged(DropTargetDragEvent dtde){
    }
    public void dragExit(DropTargetEvent dte){
    }
    public void drop(DropTargetDropEvent dtde){
    }


  }

  public class AcListener implements ActionListener{
    public AcListener(){
      ;
    }

    public void actionPerformed(ActionEvent e){
      ;
    }
  }
}
