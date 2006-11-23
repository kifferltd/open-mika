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


package com.acunia.wonka.rudolph.peers;

import java.awt.peer.*;
import java.awt.event.*;
import java.awt.*;

public class DefaultList extends DefaultComponent implements ListPeer, MouseListener, MouseMotionListener, KeyListener, FocusListener {
	private static final int UP_KEY_CODE = KeyEvent.VK_UP;
	private static final int DOWN_KEY_CODE = KeyEvent.VK_DOWN;
	private static final int SELECT_KEY_CODE = KeyEvent.VK_SPACE;
	
	public static final Font DEFAULT_FONT = new Font("courP14", 0, 14);
	public static final int BORDER = 15;
	public static final int LABELDESCENT = 4;
	public static final int LABELOFFSET = 5;

  private int minimumLines;
	private int labelheight;

	private Dimension viewport;
	private Dimension listport;
	private Image bufferedImage;
	private Graphics bufferedGraphics;
	private Color[] listColors;
	private Font listFont;
	private FontMetrics listFontMetrics;

	private HListScrollPainter hScroll;
	private boolean hScrollVisible;
	private VListScrollPainter vScroll;
	private boolean vScrollVisible;

	private boolean[] itemSelected;
	private boolean[] TMPitemSelected;
	private int currentSelected;
	private int lastSelected;
	private boolean multiple;

	private int currentItem = -1;

	private boolean repaintListArea;
	private boolean repaintViewArea;
	private boolean repaintHScroll;
	private boolean repaintVScroll;

	final static ScrollRunner mouseEventThread = new ScrollRunner();

	private int lineOffset;
	private int charOffset;

	private int maximumLineWidth = 0;

	private Point lastMousePosition;

	public DefaultList(List list) {
		super(list);
		
		viewport = new Dimension();
		listport = new Dimension();
		listColors = RudolphPeer.getBarColors();
		listFont = Component.DEFAULT_FONT;
		listFontMetrics = new FontMetrics(listFont);
		super.setFont(listFont);

		hScroll = new HListScrollPainter();
		hScroll.setBarColors(listColors);
		vScroll = new VListScrollPainter();
		vScroll.setBarColors(listColors);

		currentSelected = -1;
		lastSelected = -1;

		list.addMouseListener(this);
		list.addMouseMotionListener(this);
		lastMousePosition = new Point(0, 0);
		
		list.addKeyListener(this);
		list.addFocusListener(this);
	}

	public void add(String item, int index) {
		int lineWidth;
		List list = (List)component;
		if (multiple) {
			TMPitemSelected = new boolean[list.getItemCount()];
			for (int i = 0; i < index; ++i) {
			  TMPitemSelected[i] = itemSelected[i];
			}
			TMPitemSelected[index] = false;
			for (int i = index; i < itemSelected.length; ++i) {
				TMPitemSelected[i + 1] = itemSelected[i];
			}
			itemSelected = TMPitemSelected;
		}
		lineWidth = calculateLineWidth(index);
		if (lineWidth > maximumLineWidth) {
			maximumLineWidth = lineWidth;
		}
		scrollBars();
		repaintViewArea = true;
		paint(getGraphics());
	}

	public void delItems(int start, int end) {
		List list = (List)component;
		if (multiple) {
			TMPitemSelected = new boolean[list.getItemCount()];
      for (int i = 0; i < start; ++i) {
				TMPitemSelected[i] = itemSelected[i];
			}
			for (int i = start; i < itemSelected.length - (end - start); ++i) {
				TMPitemSelected[i] = itemSelected[i + 1];
			}
      itemSelected = TMPitemSelected;
		}
		calculateMaximumWidth();
		scrollBars();
		repaintViewArea = true;
		paint(getGraphics());
	}

	public void deselect(int index) {
		List list = (List)component;
		if (list.getItemCount() == 0 || index < 0 || index >= list.getItemCount()) {
			return;
		}
		if (currentSelected == index) {
			currentSelected = -1;
		}
		if (multiple) {
			itemSelected[index] = false;
		}
		repaintViewArea = true;
		paint(getGraphics());
	}

	public Dimension getMinimumSize(int rows) {
		return null;
	}

	public Dimension getPreferredSize(int rows) {
		return null;
	}

	public int[] getSelectedIndexes() {
		int[] selection;
		List list = (List)component;
		if (list.getItemCount() == 0 || currentSelected >= list.getItemCount()) {
			selection = new int[0];
		}
		else if (!multiple && currentSelected < 0) {
			selection = new int[0];
		}
		else if (!multiple) {
			selection = new int[1];
			selection[0] = currentSelected;
		}
		else {
			int selected = 0;
			for (int i = 0; i < itemSelected.length; ++i) {
				if (itemSelected[i]) {
					selected++;
				}
			}
			selection = new int[selected];
			selected = 0;
			for (int i = 0; i < itemSelected.length; ++i) {
				if (itemSelected[i]) {
					selection[selected] = i;
					selected++;
				}
			}
		}
		return selection;
	}

	public void makeVisible(int target) {
		List list = (List)component;
		if (target < 0) {
			target = 0;
		}
		else if (target >= list.getItemCount()) {
			target = list.getItemCount() -1;
		}

		if (target < lineOffset) {
			lineOffset = target;
		}
		else if (target > (lineOffset + listport.height / labelheight)) {
			lineOffset = target - listport.height / labelheight;
		}
    vScroll.setBarPos(lineOffset);
    repaintListArea = true;
    repaintVScroll = true;
		paint(getGraphics());
	}

	public void removeAll() {
		if (multiple) {
			itemSelected = null;
		}
		currentSelected = -1;
		lastSelected = -1;
		maximumLineWidth = 0;
		scrollBars();
		repaintViewArea = true;
		paint(getGraphics());
	}

	public void select(int index) {
		List list = (List)component;
		if (list.getItemCount() == 0) {
			throw new ArrayIndexOutOfBoundsException("Attempt to select element " + index + " of list size 0");
		}
		else if (index < 0 || index >= list.getItemCount()) {
			throw new ArrayIndexOutOfBoundsException("Attempt to select element " + index + " of list size " + list.getItemCount());
		}
	  currentSelected = index;
		lastSelected = index;
		if (multiple) {
		  itemSelected[index] = true;
		}
		repaintViewArea = true;
		paint(getGraphics());
	}

	public void setMultipleMode(boolean mode) {
		multiple = mode;
		if (!mode) {
			itemSelected = null;
		}
		else {
			itemSelected = new boolean[0];
		}
	}

	protected void processActionEvent(ActionEvent ae) {
		component.dispatchEvent(ae);
	}

	protected void processItemEvent(ItemEvent ie) {
		component.dispatchEvent(ie);
	}

	private int calculateLineWidth(int pos) {
		List list = (List)component;
    return listFontMetrics.stringWidth(list.getItem(pos));
	}

	private void calculateMaximumWidth() {
		maximumLineWidth = 0;
		List list = (List)component;
		if (list.getItemCount() == 0) {
			return;
		}
		for (int i = 0; i < list.getItemCount(); ++i) {
			if (maximumLineWidth < listFontMetrics.stringWidth(list.getItem(i))) {
				maximumLineWidth = listFontMetrics.stringWidth(list.getItem(i));
			}
		}
	}

	public Dimension getMinimumSize() {
		List list = (List)component;
		minimumLines = list.getRows();
		return new Dimension(maximumLineWidth + vScroll.getMinimumThickness(), getHeight(minimumLines, listFontMetrics));
	}

	public Dimension getPreferredSize() {
		List list = (List)component;
		minimumLines = list.getRows();
		return new Dimension(maximumLineWidth + vScroll.getMinimumThickness(), getHeight(minimumLines, listFontMetrics));
	}

	public static int getHeight(int lines, FontMetrics lfm) {
		return (lines * (lfm.getHeight() + LABELDESCENT) + 4);
	}

	public void setBackground(Color cb) {
	  super.setBackground(cb);
		listColors = RudolphPeer.getBarColors(cb, component.getForeground());
		hScroll.setBarColors(listColors);
		vScroll.setBarColors(listColors);
		repaintViewArea = true;
		paint(getGraphics());
	}

	public void setForeground(Color cf) {
	  super.setForeground(cf);
		listColors[4] = cf;
		hScroll.setBarColors(listColors);
		vScroll.setBarColors(listColors);
		repaintViewArea = true;
		paint(getGraphics());
	}

	public void setFont(Font f) {
		List list = (List)component;
		super.setFont(f);
		listFont = (f == null) ? DEFAULT_FONT : f;
		listFontMetrics = new FontMetrics(listFont);

		if (list.getItemCount() != 0) {
			calculateMaximumWidth();
			scrollBars();
			hScroll.setLineStep(listFontMetrics.getMaxAdvance());
		  repaintViewArea = true;
		}
	}

	public void scrollBars() {
		List list = (List)component;
		labelheight = 2 + listFontMetrics.getHeight() + 2;
		int fullscreenlabels = viewport.height / labelheight;
		int barredlabels = (viewport.height - RudolphScrollbarPeer.HSCROLL_HEIGHT) / labelheight;
		int innerwidth = viewport.width - LABELOFFSET - 2;
		int barredwidth = innerwidth - RudolphScrollbarPeer.VSCROLL_WIDTH;

		if (list.getItemCount() == 0) {
			hScrollVisible = false;
			vScrollVisible = false;
			lineOffset = 0;
			listport.setSize(viewport);
		}
		else if (innerwidth >= maximumLineWidth && fullscreenlabels >= list.getItemCount()) {
			hScrollVisible = false;
			charOffset = 0;
			vScrollVisible = false;
			lineOffset = 0;
			listport.setSize(viewport);
		}
		else if (barredwidth >= maximumLineWidth) {
			listport.setSize(viewport.width - RudolphScrollbarPeer.VSCROLL_WIDTH, viewport.height);
			hScrollVisible = false;
			charOffset = 0;
			vScrollVisible = true;
			vScroll.setOffset(listport.width);
			lineOffset = vScroll.setScreenHeight(listport.height, fullscreenlabels, list.getItemCount());
		}
		else if (barredlabels >= list.getItemCount()) {
			listport.setSize(viewport.width, viewport.height - RudolphScrollbarPeer.HSCROLL_HEIGHT);
			hScrollVisible = true;
			hScroll.setOffset(listport.height);
			charOffset = hScroll.setScreenWidth(listport.width, maximumLineWidth);
			vScrollVisible = false;
			lineOffset = 0;
		}
		else {
			listport.setSize(viewport.width - RudolphScrollbarPeer.VSCROLL_WIDTH, viewport.height - RudolphScrollbarPeer.HSCROLL_HEIGHT);
			hScrollVisible = true;
			hScroll.setOffset(listport.height);
			charOffset = hScroll.setScreenWidth(listport.width, maximumLineWidth);
			vScrollVisible = true;
			vScroll.setOffset(listport.width);
			lineOffset = vScroll.setScreenHeight(listport.height, barredlabels, list.getItemCount());
		}
		repaintViewArea = true;
	}

	public static int getLabel(int y, FontMetrics lfm) {
		return (y - 2) / (2 + lfm.getHeight() + 2);
	}

	public static int getInnerLength(int screenwidth) {
		return screenwidth - LABELOFFSET - 2;
	}

	private void itemSelected(int index) {
		List list = (List)component;
		if (list.getItemCount() != 0 && index >= 0 && index < list.getItemCount()) {
			if (currentSelected == index) {
				currentSelected = -1;
			}
			else {
				currentSelected = index;
				lastSelected = index;
			}
			
			if (list.getItemCount() != 0) {
				int selected;
				if (multiple) {
					itemSelected[index] = !itemSelected[index];
					selected = itemSelected[index] ? ItemEvent.SELECTED : ItemEvent.DESELECTED;
					
					this.currentItem = index;
					
					if(!itemSelected[index])
					{
						this.currentSelected = -1;
					}
				}
				else {
					selected = (currentSelected >= 0) ? ItemEvent.SELECTED : ItemEvent.DESELECTED;
				}
				processItemEvent(new ItemEvent((ItemSelectable)component, ItemEvent.ITEM_STATE_CHANGED, new String(list.getItem(index)), selected));
			}
		}
	}

	public void repaint(ScrollPainter alignation) {
		if (alignation == hScroll) {
			charOffset = hScroll.getBarPos();
			repaintHScroll = true;
		}
		else {
			lineOffset = vScroll.getBarPos();
			repaintVScroll = true;
		}
		repaintListArea = true;
		paint(getGraphics());
	}

	public void paint(Graphics g) {
    if(g == null) return;

		List list = (List)component;
		if (bufferedImage == null || !viewport.equals(component.getSize())) {
		  viewport.setSize(component.getSize());
			scrollBars();
			bufferedImage = component.createImage(viewport.width, viewport.height);
			if (bufferedImage != null && bufferedGraphics != null) {
				bufferedGraphics.dispose();
			}
			bufferedGraphics = bufferedImage.getGraphics();
			repaintViewArea = true;
		}

		if (component.getForeground() != null && !component.getForeground().equals(listColors[4])) {
			listColors[4] = component.getForeground();
			hScroll.setBarColors(listColors);
			vScroll.setBarColors(listColors);
			repaintViewArea = true;
		}
		
		if (component.getBackground() != null && !component.getBackground().equals(listColors[0])) {
			listColors = RudolphPeer.getBarColors(component.getBackground(), listColors[4]);
			hScroll.setBarColors(listColors);
			vScroll.setBarColors(listColors);
			repaintViewArea = true;
		}

		if (repaintViewArea) {
			bufferedGraphics.clearRect(1, 1, viewport.width - 2, viewport.height - 2);
		}

		if (repaintListArea || repaintViewArea) {
			if (!repaintViewArea) {
			  bufferedGraphics.clearRect(1, 1, listport.width - 2, listport.height - 2);
			}


			/* Now we'll start drawing the labels */
			int xt = 1 + LABELOFFSET - charOffset;
			int yt = 1 + listFontMetrics.getHeight() - listFontMetrics.getDescent();
			int xl = 1;
			int xr = 1 + listport.width - 4;
			int yl = 1;
			int wl = listport.width - 2;
			int hl = labelheight;
			int tmpOffset = lineOffset;
			for (int i = lineOffset; i < (lineOffset + listport.height / labelheight) && (i < list.getItemCount()); ++i) {
				if (i == currentSelected) {
					bufferedGraphics.setColor(listColors[3]);
					bufferedGraphics.fillRect(xl, yl, wl, hl);
					
					if(i==currentItem)
					{
						bufferedGraphics.setColor(listColors[1]);
						bufferedGraphics.drawRect(xl, yl, wl-1, hl-1);										
					}					
					
					yl += labelheight;
					bufferedGraphics.setColor(listColors[2]);
					bufferedGraphics.drawString(list.getItem(tmpOffset++), xt, yt);
					yt += labelheight;
				}
				else if (itemSelected != null && itemSelected[i]) {
          bufferedGraphics.setColor(listColors[1]);
					bufferedGraphics.fillRect(xl, yl, wl, hl);
					
					if(i==currentItem)
					{
						bufferedGraphics.setColor(listColors[3]);
						bufferedGraphics.drawRect(xl, yl, wl-1, hl-1);										
					}					
					
					yl += labelheight;
					bufferedGraphics.setColor(listColors[2]);
					bufferedGraphics.drawString(list.getItem(tmpOffset++), xt, yt);
					yt += labelheight;
				}
				else if((i==currentItem) && (this.multiple)) {
					// draw current item
					String text = list.getItem(tmpOffset++);
					
					bufferedGraphics.setColor(listColors[3]);
					bufferedGraphics.drawRect(xl, yl, wl-1, hl-1);				
					
					yl += labelheight;
					bufferedGraphics.setColor(listColors[4]);
					bufferedGraphics.drawString(text, xt, yt);
										
					yt += labelheight;					
				}
				else {
					yl += labelheight;
          if(component.isEnabled()) {
  					bufferedGraphics.setColor(listColors[4]);
          }
          else {
  					bufferedGraphics.setColor(SystemColor.textInactiveText);
          }
					bufferedGraphics.drawString(list.getItem(tmpOffset++), xt, yt);
					yt += labelheight;
				}
			}
			repaintListArea = false;
		}

		if (hScrollVisible) {
			hScroll.paint(bufferedGraphics);
			repaintHScroll = false;
		}

		if (vScrollVisible) {
			vScroll.paint(bufferedGraphics);
			repaintVScroll = false;
		}

		repaintViewArea = false;
		
		/* At last a nice border around the list is drawn */
		bufferedGraphics.setColor(listColors[1]);
		bufferedGraphics.drawLine(0, 0, listport.width, 0);
		bufferedGraphics.drawLine(0, 1, 0, listport.height - 1);
		bufferedGraphics.setColor(listColors[2]);
		bufferedGraphics.drawLine(listport.width - 1, 1, listport.width - 1, listport.height - 1);
		bufferedGraphics.drawLine(1, listport.height - 1, listport.width - 1, listport.height - 1);
		bufferedGraphics.setColor(listColors[0]);
		bufferedGraphics.drawLine(listport.width, 0, listport.width, listport.height - 1);

		/* Finally we draw everything to the screen */
		g.drawImage(bufferedImage, 0, 0, component);
		
		super.paint(g);
	}

	/* inner classes for scrollbar painting */

	class HListScrollPainter extends ScrollPainter {
		public HListScrollPainter() {
		  super(RudolphScrollbarPeer.HSCROLL_HEIGHT, RudolphScrollbarPeer.HSCROLL_LINEUPWIDTH, RudolphScrollbarPeer.HSCROLL_LINEDNWIDTH, RudolphScrollbarPeer.HSCROLL_MINIMUMBOXWIDTH);
		}

		public int getPos(int x, int y) {
			return x;
		}

		public int getThickness(int width, int height) {
			return height;
		}

		public Dimension getSize(int scrollbarlength, int scrollbarthickness) {
			return new Dimension (scrollbarlength, scrollbarthickness);
		}

		public Dimension getPreferredSize(int width, int height) {
			return new Dimension(width, minimumThickness);
		}

		public Dimension getMinimumSize() {
			return new Dimension(lineUpSpan + lineDnSpan + screenRange, minimumThickness);
		}

		public Dimension getPreferredSize() {
			return new Dimension(lineUpSpan + lineDnSpan + screenRange, minimumThickness);
		}

		public Dimension getCurrentSize() {
			return new Dimension(lineUpSpan + lineDnSpan + screenRange, minimumThickness);
		}

		public void setThickness(int width, int height) {
			currentThickness = (height > minimumThickness) ? height : minimumThickness;
		}

		public boolean setRange(int width, int height) {
			return setRange(width);
		}

		public int getField(int x, int y) {
			return getField(x);
		}

		public synchronized int setActive(int x, int y) {
			currentActive = (y > barOffset) ? getField(x) : RudolphScrollbarPeer.FIELD_NONESELECTED;
			return currentActive;
		}

		public int setScreenWidth(int totalwidth, int maximumlength) {
			screenRange = totalwidth - lineUpSpan - lineDnSpan;
			barSpan = getInnerLength(totalwidth);
			barRange = maximumlength;
			if (screenRange <= 0) {
				screenRange = 0;
				screenSpan = 0;
				screenPos = 0;
				crippledSpan = totalwidth;
			}
			else {
				crippledSpan = -1;
				if (barSpan > barRange) {
					barSpan = barRange;
					barPos = 0;
				}
				else if ((barPos + barSpan) > barRange) {
					barPos = barRange - barSpan;
				}
				setScreen();
				blockStep = (barSpan > lineStep) ? barSpan - lineStep : lineStep;
			}
			return barPos;
		}

		public void paint(Graphics g) {
			if (crippledSpan < 0) {
				RudolphScrollbarPeer.paintHScrollbar(0, barOffset, minimumThickness, paintedScreenPos, screenSpan, screenRange, currentActive, barColors, g);
			}
			else {
				RudolphScrollbarPeer.paintCrippledHScrollbar(0, barOffset, minimumThickness, crippledSpan, currentActive, barColors, g);
			}
		}
	}

	class VListScrollPainter extends ScrollPainter {
		public VListScrollPainter() {
			super(RudolphScrollbarPeer.VSCROLL_WIDTH, RudolphScrollbarPeer.VSCROLL_LINEUPHEIGHT, RudolphScrollbarPeer.VSCROLL_LINEDNHEIGHT, RudolphScrollbarPeer.VSCROLL_MINIMUMBOXHEIGHT);
		}

		public int getPos(int x, int y) {
			return y;
		}

		public int getThickness(int width, int height) {
			return width;
		}

		public Dimension getSize(int scrollbarlength, int scrollbarthickness) {
			return new Dimension(scrollbarthickness, scrollbarlength);
		}

    public Dimension getPreferredSize(int width, int height) {
			return new Dimension(minimumThickness, height);
		}

		public Dimension getMinimumSize() {
			return new Dimension(minimumThickness, lineUpSpan + lineDnSpan + minimumScreenSpan);
		}

		public Dimension getPreferredSize() {
			return new Dimension(minimumThickness, lineUpSpan + lineDnSpan + screenRange);
		}

		public Dimension getCurrentSize() {
			return new Dimension(minimumThickness, lineUpSpan + lineDnSpan + screenRange);
		}

		public void setThickness(int width, int height) {
			currentThickness = (width > minimumThickness) ? width : minimumThickness;
		}

		public boolean setRange(int width, int height) {
			return setRange(height);
		}

		public int getField(int x, int y) {
			return getField(y);
		}

		public synchronized int setActive(int x, int y) {
			currentActive = (x > barOffset) ? getField(y) : RudolphScrollbarPeer.FIELD_NONESELECTED;
			return currentActive;
		}

		public int setScreenHeight(int totalheight, int linespan, int linerange) {
			screenRange = totalheight - lineUpSpan - lineDnSpan;
			barSpan = linespan;
			barRange = linerange;
			if (screenRange <= 0) {
				screenRange = 0;
				screenSpan = 0;
				screenPos = 0;
				crippledSpan = totalheight;
			}
			else {
				crippledSpan = -1;
				if (barSpan > barRange) {
					barSpan = barRange;
					barPos = 0;
				}
				else if ((barPos + barSpan) > barRange) {
					barPos = barRange - barSpan;
				}
				setScreen();
				blockStep=(linespan > 1) ? linespan - 1 : 1;
			}
			return barPos;
		}

		public void paint(Graphics g) {
			if(crippledSpan < 0) {
			  RudolphScrollbarPeer.paintVScrollbar(barOffset, 0, minimumThickness, paintedScreenPos, screenSpan, screenRange, currentActive, barColors, g);
			}
			else {
			  RudolphScrollbarPeer.paintCrippledVScrollbar(barOffset, 0, minimumThickness, crippledSpan, currentActive, barColors, g);
			}
		}
	}

	/* deprecated ListPeer methods */

	public void addItem(String item, int index) {
	}

	public void clear() {
	}

	public Dimension minimumSize(int rows) {
		return null;
	}

	public Dimension preferredSize(int rows) {
		return null;
	}

	public void setMultipleSelections(boolean multiple) {
	}

	/* MouseListener implementations */

	public void mouseClicked(MouseEvent e) {
		List list = (List)component;
		int click = getLabel(e.getY(), listFontMetrics) + lineOffset;
    if (click == currentSelected && list.getItemCount() != 0) {
      processActionEvent(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, new String(list.getItem(click))));
		}
	}

  public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
		if (multiple && currentSelected >= 0) {
			currentSelected = -1;
			repaintListArea = true;
		}
		if (hScrollVisible && hScroll.isSelected()) {
			mouseEventThread.stopRunner(hScroll);
			repaintHScroll = true;
		}
		if (vScrollVisible && vScroll.isSelected()) {
			mouseEventThread.stopRunner(vScroll);
			repaintVScroll = true;
		}
		if (repaintListArea || repaintHScroll || repaintVScroll) {
      paint(getGraphics());
		}
	}

  public void mousePressed(MouseEvent e) {
    int x = e.getX();
		int y = e.getY();
		if (hScrollVisible && y > listport.height) {
			int active = hScroll.setActive(x, y);
			if (active == AdjustmentEvent.TRACK) {
				lastMousePosition.setLocation(x, y);
				repaintHScroll = true;
        paint(getGraphics());
			}
			else if (active == AdjustmentEvent.UNIT_DECREMENT && hScroll.lineUp()) {
				mouseEventThread.setRunner(hScroll, component);
				repaint(hScroll);
			}
			else if (active == AdjustmentEvent.UNIT_DECREMENT) {
				repaintHScroll = true;
        paint(getGraphics());
			}
			else if (active == AdjustmentEvent.UNIT_INCREMENT && hScroll.lineDn()) {
				mouseEventThread.setRunner(hScroll, component);
				repaint(hScroll);
			}
			else if (active == AdjustmentEvent.UNIT_INCREMENT) {
				repaintHScroll = true;
        paint(getGraphics());
			}
			else if (active == AdjustmentEvent.BLOCK_DECREMENT && hScroll.pageUp()) {
				mouseEventThread.setRunner(hScroll, component);
				repaint(hScroll);
			}
			else if (active == AdjustmentEvent.BLOCK_INCREMENT && hScroll.pageDn()) {
				mouseEventThread.setRunner(hScroll, component);
				repaint(hScroll);
			}
		}
		else if (vScrollVisible && x > listport.width) {
			int active = vScroll.setActive(x, y);
			if (active == AdjustmentEvent.TRACK) {
				lastMousePosition.setLocation(x, y);
				repaintVScroll = true;
        paint(getGraphics());
			}
			else if (active == AdjustmentEvent.UNIT_DECREMENT && vScroll.lineUp()) {
				mouseEventThread.setRunner(vScroll, component);
				repaint(vScroll);
			}
			else if (active == AdjustmentEvent.UNIT_DECREMENT) {
				repaintVScroll = true;
        paint(getGraphics());
			}
			else if (active == AdjustmentEvent.UNIT_INCREMENT && vScroll.lineDn()) {
				mouseEventThread.setRunner(vScroll, component);
				repaint(vScroll);
			}
			else if (active == AdjustmentEvent.UNIT_INCREMENT) {
				repaintVScroll = true;
        paint(getGraphics());
			}
			else if (active == AdjustmentEvent.BLOCK_DECREMENT && vScroll.pageUp()) {
				mouseEventThread.setRunner(vScroll, component);
				repaint(vScroll);
			}
			else if (active == AdjustmentEvent.BLOCK_INCREMENT && vScroll.pageDn()) {
				mouseEventThread.setRunner(vScroll, component);
				repaint(vScroll);
			}
		}
		else {
			List list = (List)component;
			lastMousePosition.setLocation(x, y);
			int click = getLabel(y, listFontMetrics) + lineOffset;
			if (list.getItemCount() != 0 && click <= list.getItemCount()) {
				itemSelected(click);
				repaintListArea = true;
        paint(getGraphics());
			}
		}
	}

	public void mouseReleased(MouseEvent event) {
		if (multiple && currentSelected >= 0) {
			currentSelected = -1;
			repaintListArea = true;
		}
		if (hScrollVisible && hScroll.isSelected()) {
			mouseEventThread.stopRunner(hScroll);
			repaintHScroll = true;
		}
		if (vScrollVisible && vScroll.isSelected()) {
			mouseEventThread.stopRunner(vScroll);
			repaintVScroll = true;
		}
		if (repaintListArea || repaintHScroll || repaintVScroll) {
      paint(getGraphics());
		}
	}

	/* MouseMotionListener implementations */

	public void mouseMoved(MouseEvent event) {
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (vScrollVisible) {
			if (vScroll.getActive() == AdjustmentEvent.TRACK) {
				if (vScroll.moveBar(y - lastMousePosition.y)) {
					repaint(vScroll);
					lastMousePosition.y = y;
				}
			}
			else if (x < 0 || x > listport.width) {
				mouseEventThread.stopRunner(vScroll);
			}
			else if (y > 0 && y < BORDER && y < lastMousePosition.y && vScroll.lineUp()) {
				mouseEventThread.setRunner(vScroll, component, ScrollRunner.SCROLL_UP);
				repaint(vScroll);
				lastMousePosition.y = y;
			}
			else if (y > BORDER && y < lastMousePosition.y) {
				mouseEventThread.stopRunner(vScroll);
				lastMousePosition.y =  y;
			}
			else if (y < listport.height && y > (listport.height - BORDER) && y > lastMousePosition.y && vScroll.lineDn()) {
				mouseEventThread.setRunner(vScroll, component, ScrollRunner.SCROLL_DOWN);
				repaint(vScroll);
				lastMousePosition.y = y;
			}
			else if (y < (listport.height - BORDER) && y > lastMousePosition.y ) {
				mouseEventThread.stopRunner(vScroll);
				lastMousePosition.y = y;
			}
		}
		if (hScrollVisible) {
			if (hScroll.getActive() == AdjustmentEvent.TRACK) {
				if (hScroll.moveBar(x - lastMousePosition.x)) {
					repaint(hScroll);
					lastMousePosition.x = x;
				}
			}
			else if (y < 0 || y > listport.height) {
				mouseEventThread.stopRunner(hScroll);
			}
			else if (x > 0 && x < BORDER && x < lastMousePosition.x && hScroll.lineUp()) {
				mouseEventThread.setRunner(hScroll, component, ScrollRunner.SCROLL_UP);
				repaint(vScroll);
				lastMousePosition.x = x;
			}
			else if (x > BORDER && x < lastMousePosition.x) {
				mouseEventThread.stopRunner(hScroll);
				lastMousePosition.x = x;
			}
			else if (x < listport.width && x > (listport.width - BORDER) && x > lastMousePosition.x && hScroll.lineDn()) {
				mouseEventThread.setRunner(hScroll, component, ScrollRunner.SCROLL_DOWN);
				repaint(vScroll);
				lastMousePosition.x = x;
			}
			else if(x < (listport.width - BORDER) && x > lastMousePosition.x) {
				mouseEventThread.stopRunner(hScroll);
				lastMousePosition.x = x;
			}
		}
    refresh(DefaultComponent.REFRESH_LOCAL);
	}
	
  public boolean isFocusTraversable() {
    return true;
  }	
  
  public void keyPressed(KeyEvent event)
  {
  }
  
  public void keyReleased(KeyEvent event)
  {
  }
  
  public void keyTyped(KeyEvent event)
  {
	  wonka.vm.Etc.woempa(9, "keyTyped on list. "+event.paramString());

	  List list = (List) this.component;

	  switch(event.getKeyCode())
	  {
		  case SELECT_KEY_CODE:
			  if(this.multiple)
			  {
				  itemSelected(this.currentItem);
			  }
			  else
			  {
				  itemSelected(this.currentSelected);					
			  }
			  
			  this.repaintViewArea = true;
			  paint(getGraphics());

		      break;

		  case UP_KEY_CODE:
			  if(this.multiple)
			  {
				  if(this.currentItem<=0)
				  {
					int lastIndex = list.getItemCount()-1;
					this.currentItem = lastIndex;
					this.lineOffset=lastIndex-this.listport.height / this.labelheight +1;
					this.repaintHScroll=true;
				  }
				  else
				  {
					this.currentItem--;										

					if(this.currentItem<this.lineOffset)
					{
						this.lineOffset--;
						this.repaintHScroll=true;		
					}
				  }
			  }
			  else
			  {
				  if(this.currentSelected<=0)
				  {
					itemSelected(list.getItemCount()-1);
					this.lineOffset=this.currentSelected-this.listport.height / this.labelheight +1;
					this.repaintHScroll=true;
				  }
				  else
				  {
					int newSelected = this.currentSelected-1;
					itemSelected(newSelected);					
					if(this.currentSelected<this.lineOffset)
					{
						this.lineOffset--;
						this.repaintHScroll=true;		
					}					
					wonka.vm.Etc.woempa(9, "lineOffset: "+lineOffset);
				  }
				  
			  }
			  
			  this.repaintViewArea = true;
 		  	  paint(getGraphics());	  

			  break;

		  case DOWN_KEY_CODE:
			  if(this.multiple)
			  {
				  if(this.currentItem>=list.getItemCount()-1)
				  {
					this.currentItem=0;						
					this.lineOffset=0;
					this.repaintHScroll=true;				
				  }
				  else
				  {
					this.currentItem++;

					if(this.currentItem>(this.lineOffset + this.listport.height / this.labelheight -1))
					{
						this.lineOffset++;
						this.repaintHScroll=true;
					}
				  }
			  }
			  else
			  {
				  if(this.currentSelected>=list.getItemCount()-1)
				  {
					itemSelected(0);
					this.lineOffset=0;
					this.repaintHScroll=true;				
				  }
				  else
				  {
					int newSelected = this.currentSelected+1;
					itemSelected(newSelected);
					if(this.currentSelected>(this.lineOffset + this.listport.height / this.labelheight -1))
					{
						this.lineOffset++;
						this.repaintHScroll=true;
					}										  
				  }				  
			  }
			  
			  this.repaintViewArea = true;
			  paint(getGraphics());	  

			  break;
	  }	  
  }
    
  public void focusGained(FocusEvent event)
  {
	  if(this.currentSelected>=0) {
		wonka.vm.Etc.woempa(9, "item "+this.currentSelected+" is selected");
		this.currentItem = this.currentSelected;
	  }
	  else
	  {
		wonka.vm.Etc.woempa(9, "no item is selected");  
	  }
  }
  
  public void focusLost(FocusEvent event)
  {
	  this.currentItem = -1;
  	  repaintViewArea = true;
	  paint(getGraphics());	  	  
  }  
}
