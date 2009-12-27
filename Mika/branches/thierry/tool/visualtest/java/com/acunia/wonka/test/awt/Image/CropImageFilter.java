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

package com.acunia.wonka.test.awt.Image;

// import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.FilteredImageSource;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class CropImageFilter extends VisualTestImpl implements MouseListener, MouseMotionListener {

  private Image original;
  private Image cropped;
  private java.awt.image.CropImageFilter filter;
  private FilteredImageSource source;
  
  private Rectangle crop;
  private Point lastClick;

  private boolean pressed = false;
    
  public CropImageFilter() {
    super();

    setBackground(Color.black);

    addMouseListener(this);
    addMouseMotionListener(this);

    crop = new Rectangle(72, 59, 77, 18);
    lastClick = new Point();
    
    original = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/lena2.png"));
    
    this.repaint();
  }

  public void update(Graphics g) {
    g.setColor(Color.black);
    g.fillRect(0, 0, 500, 500);
    g.drawImage(original, 10, 10, null);
    g.setColor(Color.white);
    g.drawRect(10 + crop.x, 10 + crop.y, crop.width, crop.height);
   
    if(pressed == false) {
      filter = new java.awt.image.CropImageFilter(crop.x, crop.y, crop.width, crop.height);
      source = new FilteredImageSource(original.getSource(), filter);
      cropped = createImage(source);
      g.drawImage(cropped, 220, 10, null);
    }

  }

  public void paint(Graphics g) {
    update(g);
  }

  public String getHelpText(){
    return "";
  }

  public void mouseClicked(MouseEvent event) {
  }

  public void mouseEntered(MouseEvent event) {
  }

  public void mouseExited(MouseEvent event) {
  }

  public void mousePressed(MouseEvent event) {
    pressed = true;
    lastClick.setLocation(event.getX() - 10, event.getY() - 10);
    crop.setBounds(lastClick.x, lastClick.y, 0, 0);
    
    repaint();
  }

  public void mouseReleased(MouseEvent event) {
    pressed = false;
    repaint();
  }

  public void mouseDragged(MouseEvent event) {
    int x1 = lastClick.x;
    int y1 = lastClick.y;
    int x2 = event.getX() - 10;
    int y2 = event.getY() - 10;
    
    crop.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    repaint();
  }

  public void mouseMoved(MouseEvent event) {
  }
  
}

