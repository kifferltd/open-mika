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

// V.1.01.00 2001/08/29 : first release
// V.1.01.01 2001/08/29 : excluded CVS directories from files list
// V.1.01.02 2001/08/29 : Option to only load checked files
// V.1.02.01 2001/08/31 : Double list and quick scan possibilities
// V.1.02.02 2001/08/31 : bugfixes and selective file-load for subdirs
// V.1.02.03 2001/08/31 : added buttons for showing scripts and c/h/java files
// V.1.02.03 2001/09/03 : logging file
// V.1.02.04 2001/09/03 : edit screen allows to place header on current text position / button for header files only
// V.1.02.05 2001/09/03 : added buttons for same directory and last directory


// Author: N.Oberfeld
// Version 1.01.01
// Created: 2001/08/29

package com.acunia.wonka.test.awt.Image;

//import
//import java.io.*;
//import java.net.*;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.ImageObserver;

import com.acunia.wonka.test.awt.VisualTestImpl;



public class ImageObserverTest2 extends VisualTestImpl implements ActionListener, ItemListener {
	
  /****************************************************************/
  /** variables
  */
  private Button build;
  private Button checkWidth;
  private Button prepareImage;
  private Button resizeImage;
  private Button checkImage;
  private Button redrawImage;
  private Checkbox toCanvas;
  private Checkbox toMain;
  private Checkbox toList;
  private Checkbox toNull;

  private ImageComponent canvas;
  private Image image;

  ImageObserver observer;

  private ImageList display;
  /****************************************************************/
  /** constructor
  */
  public ImageObserverTest2() {
    super();
    // build screen
    setLayout(new BorderLayout());

    display = new ImageList();
    display.add("Observer ImageUpdate() dispayed here");
    add(display, BorderLayout.SOUTH);

    canvas = new ImageComponent();
    observer = canvas;
    add(canvas, BorderLayout.CENTER);

    Panel actions = new Panel(new GridLayout(10,1));
      build = new Button("build image");
      build.addActionListener(this);
      actions.add(build);

      resizeImage = new Button("resize image");
      resizeImage.addActionListener(this);
      actions.add(resizeImage);

      prepareImage = new Button("prepare image");
      prepareImage.addActionListener(this);
      actions.add(prepareImage);

      checkWidth = new Button("get width");
      checkWidth.addActionListener(this);
      actions.add(checkWidth);

      checkImage = new Button("check image");
      checkImage.addActionListener(this);
      actions.add(checkImage);

      redrawImage = new Button("repaint()");
      redrawImage.addActionListener(this);
      actions.add(redrawImage);

      CheckboxGroup g = new CheckboxGroup();

      toMain = new Checkbox("Observer to main ",false,g);
      toMain.addItemListener(this);
      actions.add(toMain);

      toCanvas = new Checkbox("Observer to canvas",true,g);
      toCanvas.addItemListener(this);
      actions.add(toCanvas);

      toList = new Checkbox("Observer to display List",false,g);
      toList.addItemListener(this);
      actions.add(toList);

      toNull = new Checkbox("Observer to null",false,g);
      toNull.addItemListener(this);
      actions.add(toNull);
    add(actions, BorderLayout.EAST);

    image = null;

    show();
  }

  /****************************************************************/
  /**button pressed : do the action desired */
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if(source == build){
       // (re)load image from file
      image=canvas.buildImage(200,100);
      printData("Build image <"+image+">canvas size (200,100)");
    }
    else if(source == checkWidth && image != null) {
      printData("image.getwidth() returns "+image.getWidth(observer) );
    }
    else if(source == prepareImage && image != null) {
      printData("prepareImage() returns "+canvas.prepareImage(image,observer) );
    }
    else if(source == resizeImage && image != null) {
      int width = image.getWidth(observer);
      int height =image.getHeight(observer);
      if(width>2 && height>2){
        width /=2;
        height/=2;
        image = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        printData("rescaled image to new size ("+width+", "+height+")");
        canvas.setImage(image);
      }
      else {
        printData("rescaling image ("+width+", "+height+"): image too small to rescale any further");
      }

    }
    else if(source == checkImage && image != null) {
      int check = canvas.checkImage(image,observer);
      printData("checkImage() returns "+ check, check );
    }
    else if(source == redrawImage) {
      //printData("repainting image <"+image+">"); is done there
      canvas.repaint();
    }
  }

  /****************************************************************/
  /** ItemListener radio checkbox clicked: swap observer to desired target
  */
  public void itemStateChanged(ItemEvent evt) {
    Object source = evt.getSource();
    if(source == toCanvas) {
      printData("ImageObserver is the image canvas" );
      observer = canvas;
    }
    else if(source == toMain) {
      printData("ImageObserver is main application" );
      observer = this;
    }
    else if(source == toList) {
      printData("ImageObserver is display list" );
      observer = display;
    }
    else if(source == toNull) {
      printData("ImageObserver is <null>" );
      observer = null;
    }
  }

  /****************************************************************/
  /** spy on imageUpdate*/
  public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h){
    boolean result = super.imageUpdate(img,flags,x,y,w,h);
    printData("main application: imageUpdate(x="+x+",y="+y+",w="+w+",h="+h+") returns "+result, flags);
    return result;
  }


  /****************************************************************/
  /**print a line on the list*/
  void printData(String text) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    System.out.println(text);
    display.add(text,0);
  }

  /****************************************************************/
  /** print  on the list a line of text and a line with the corresponding ImageObserver flags*/
  void printData(String line1, int line2) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    String text = "Flags: ";
    if((line2 & ImageObserver.ERROR)>0) {
      text += "<ERROR> ";
    }
    if((line2 & ImageObserver.ABORT)>0) {
      text += "<ABORT> ";
    }
    if((line2 & ImageObserver.WIDTH)>0) {
      text += "<WIDTH> ";
    }
    if((line2 & ImageObserver.HEIGHT)>0) {
      text += "<HEIGHT> ";
    }
    if((line2 & ImageObserver.PROPERTIES)>0) {
      text += "<PROPERTIES> ";
    }
    if((line2 & ImageObserver.SOMEBITS)>0) {
      text += "<SOMEBITS> ";
    }
    if((line2 & ImageObserver.FRAMEBITS)>0) {
      text += "<FRAMEBITS> ";
    }
    if((line2 & ImageObserver.ALLBITS)>0) {
      text += "<ALLBITS> ";
    }
    System.out.println(line1);
    System.out.println(text);
    display.add(text,0);
    display.add(line1,0);
  }

  /****************************************************************/
  /** inner class component with spy on ImageUpdate function */
  /****************************************************************/
  class ImageComponent extends Component {
    /** variables*/
    Image componentImage;

    /** constructor*/
    public ImageComponent() {
      super();
      componentImage = null;
    }

    /** set image*/
    public Image buildImage(int width, int height) {
      componentImage = this.createImage(width, height);
      Graphics g = componentImage.getGraphics();
      g.setColor(Color.red);
      g.fillRect(0,0,width-1, height-1);
      g.setColor(Color.blue);
      g.drawLine(1, 1, width-1, height-1);
      g.drawLine(width-1, 1, 1, height-1);
      return componentImage;

    }

    public void setImage(Image img) {
      componentImage = img;
    }

    /** spy on imageUpdate*/
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h){
      boolean result = super.imageUpdate(img,flags,x,y,w,h);
      printData("Image panel: imageUpdate(x="+x+",y="+y+",w="+w+",h="+h+") returns "+result, flags);
      return result;
    }

    /** paint* /
    public void paint(Graphics g) {
      printData("Painting component Image "+componentImage);
      if(componentImage != null){
        g.drawImage(componentImage,0,0,observer);
      }
    }
*/
    public void paint(Graphics g) {
      printData("Painting component Image "+componentImage);
      this.update(g);
    }

    public void update(Graphics g) {
      if(componentImage != null){
        printData("Update:repainting component Image size = ("+componentImage.getWidth(observer)+", "+componentImage.getHeight(observer)+")");
        g.clearRect(0,0,this.getSize().width-2,this.getSize().height-2);
        g.drawImage(componentImage,0,0,observer);
      }
    }
  }

  /****************************************************************/
  /** inner class List with spy on ImageUpdate function */
  /****************************************************************/
  class ImageList extends List {
    /** constructor*/
    public ImageList() {
      super(3,true);
    }

    /** spy on imageUpdate*/
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h){
      boolean result = super.imageUpdate(img,flags,x,y,w,h);
      printData("Display list: imageUpdate(x="+x+",y="+y+",w="+w+",h="+h+") returns "+result, flags);
      return result;
    }
  }


  public String getHelpText(){
    return "Test on the throwing of imageUpdate-functions to an ImageObserver for created image canvasses:\n"+
           "The screen consists out of a display area, a message list and a number of buttons. Clicking one of the buttons triggers"+
           " the manipulation of an Image object and shows on the list all imageUpdate-functions received by the ImageObserver selected\n"+
           "The button options are:\n"+
           "-> build image: creates an Image canvas and paints it in red with blue diagonals"+
           " (it isn't shown yet however, this is done by the 'repaint()' button)\n"+
           "-> resize image: constructs a new image out of the current one by rescaling it to half the size (calling Image.getScaledInstance())\n"+
           "-> prepare image: sends a call Component.PrepareImage(Image, ImageObserver) to prepare the Image for display"+
           " and maps all imageUpdate calls to the given ImageObserver\n"+
           "-> get width: checks for the curent Image width (by calling Image.getWidth(ImageObserver)  )"+
           " and maps all imageUpdate calls to the given ImageObserver\n"+
           "-> Check image: checks for the status of the curent Image (by calling Component.checkImage(Image, ImageObserver)  )"+
           " and maps all imageUpdate calls to the given ImageObserver\n"+
           "-> repaint image: sends a Component.repaint() signal that triggers a painting of the component"+
           " (using Graphics.drawImage(Image, position, ImageObserver) All imageUpdate calls are mapped to the given ImageObserver\n\n"+
           " By means of the radiobuttons, you can change the ImageObserver desired to: \n"+
           "-> the main application itself\n"+
           "-> the image window\n"+
           "-> the list\n"+
           "-> NULL";
  }

  /********************/
  /** test main */
  static public void main (String[] args) {
    new ImageObserverTest2();
  }

  // (end of class CopyWriter
}
