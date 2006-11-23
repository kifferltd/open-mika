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

package java.awt.peer;

import java.awt.image.*;
import java.awt.*;

public interface ComponentPeer {
  
  int checkImage(Image image, int width, int height, ImageObserver obs);
  Image createImage(int width, int height);
  Image createImage(ImageProducer prod);
  void dispose();
  ColorModel getColorModel();
  FontMetrics getFontMetrics(Font font);
  Graphics getGraphics();
  Point getLocationOnScreen();
  Dimension getMinimumSize();
  Dimension getPreferredSize();
  Toolkit getToolkit();
  boolean handleEvent(AWTEvent event);
  boolean isFocusTraversable();
  void paint(Graphics g);
  boolean prepareImage(Image img, int width, int height, ImageObserver obs);
  void print(Graphics g);
  void repaint(long ms, int x, int y, int w, int h);
  void requestFocus();
  void setBounds(int x, int y, int w, int h);
  void setBackground(Color c);
  void setCursor(Cursor cursor);
  void setEnabled(boolean enable);
  void setFont(Font font);
  void setForeground(Color c);
  void setVisible(boolean visible);

  /*
  ** Deprecated methods
  */
  
  void disable();
  void enable();
  void hide();
  Dimension minimumSize();
  Dimension preferredSize();
  void reshape(int x, int y, int w, int h);
  void show();

}

