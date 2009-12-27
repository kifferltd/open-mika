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


// Author: N. Oberfeld
// Created: 2001/03/13

import java.awt.*;
import java.awt.event.*;

public class Metronome  extends Frame
{
  final static float HERZES[]={60.0f,63.5f,67.0f,71.0f,75.5f,80.0f,85.0f,90.0f,95.0f,101.0f,107.0f,113.0f};
	
	final static Color BLINKLIGHT = new Color(255,196,64);
	final static Color BLINKDARK   = new Color(196,64,64);

  /** Variables */
  private Keyboard 				keyboard;
  private KeyboardLed			led;
  private TextField display;
  private float[] blinkRate;		
  private int[] blinkTime;
  /** constructor */
  public Metronome(int octaves)
  {
    super("Keyboard Demo");
    //set blink rate and blink time
    blinkRate = new float[octaves*12 +1];
    blinkTime = new int[octaves*12 +1];
  	if(octaves==3)
  	{
      for(int i=0; i<12; i++)
      {
    	  blinkRate[i]=HERZES[i]/2;
    	  blinkTime[i]=(int)(120000.0/HERZES[i]);
    	  blinkRate[i+12]=HERZES[i];
    	  blinkTime[i+12]=(int)(60000.0/HERZES[i]);    	
    	  blinkRate[i+24]=HERZES[i]*2;
    	  blinkTime[i+24]=(int)(30000.0/HERZES[i]);
    	}
  	  blinkRate[36]=HERZES[0]*4;
  	  blinkTime[36]=(int)(15000.0/HERZES[0]);
   }
   else if(octaves==2)
   {
      for(int i=0; i<12; i++)
    	{
    	  blinkRate[i]=HERZES[i];
    	  blinkTime[i]=(int)(60000.0/HERZES[i]);
    	  blinkRate[i+12]=HERZES[i]*2;
    	  blinkTime[i+12]=(int)(30000.0/HERZES[i]);
    	}
    	blinkRate[24]=HERZES[0]*4;
    	blinkTime[24]=(int)(15000.0/HERZES[0]);
   }
   else
   {
      for(int i=0; i<12; i++)
    	{
    	  blinkRate[i]=HERZES[i];
    	  blinkTime[i]=(int)(60000.0/HERZES[i]);    	
    	}
    	blinkRate[12]=HERZES[0]*2;
    	blinkTime[12]=(int)(30000.0/HERZES[0]);
    }

    //layout
    keyboard = new Keyboard(octaves);
    add(keyboard, BorderLayout.NORTH);
    led = new KeyboardLed();
    add(led, BorderLayout.WEST);
    display = new TextField(); // new KeyboardDisplay();
    add(display,BorderLayout.CENTER);

    setSize(400,234);
    show();
  }
  		
  /*************************************************
  * Interfaces between keyboard, text and led
  */
  public void 	keyPressedEvent(int note)
  {
    if(note>=0)
    {
    	display.setText("Metronome "+blinkRate[note]+" Bpm");
      led.setBlinkRate(blinkTime[note]);
    }
    else
    {
      display.setText("( Metronome off )");
      led.setBlinkRate(-1 );
    }
  }
  		
  /** main */
  static public void main (String[] args)
  {
    new Metronome(3);
  }

  /**************************************************************************************************************************************/
  /**
  * Inner class Keyboard
  */
	class Keyboard extends Component implements MouseListener, MouseMotionListener
	{
		private Dimension size;
		private int keys;
		private int keyWidth;
		private int keysOffset;
		private boolean[] halfValid;
		private int halfkeysOffset;
		
		public Keyboard(int octaves)
		{
			super();
			size = new Dimension();
			keys = octaves*7;
			halfValid = new boolean[keys];
			for(int i=0; i<octaves; i++)
			{
		    halfValid[7*i]=true;
		    halfValid[7*i+1]=true;
		    halfValid[7*i+2]=false;
		    halfValid[7*i+3]=true;
		    halfValid[7*i+4]=true;
		    halfValid[7*i+5]=true;
		    halfValid[7*i+6]=false;
			}
				
      this.addMouseListener(this);
      this.addMouseMotionListener(this);
		}
	
	
    private int keyToNote(int keyno)
    {
    	int scale=keyno/7;
    	int note = keyno%7;
    	if(note<0)
    		return -1;
    	else if(note<1)
    		return scale*12;
    	else if(note<2)
    		return scale*12+2;
    	else if(note<3)
    		return scale*12+4;
    	else if(note<4)
    		return scale*12+5;
    	else if(note<5)
    		return scale*12+7;
    	else if(note<6)
    		return scale*12+9;
    	else
    		return scale*12+11;
    }

    private int halfToNote(int halfno)
    {
    	int scale=halfno/7;
    	int note = halfno%7;
    	if(note<0)
    		return -1;
    	else if(note<1)
    		return scale*12+1;
    	else if(note<2)
    		return scale*12+3;
    	else if(note<3)
    		return scale*-1;
    	else if(note<4)
    		return scale*12+6;
    	else if(note<5)
    		return scale*12+8;
    	else if(note<6)
    		return scale*12+10;
    	else
    		return -1;
    }

    /**
    * field sizes and key key dimensions
    */
		public Dimension getPreferredSize()
    {
      return new Dimension(this.getSize().width, 100);
    }

    private void setFieldSize(Dimension newsize)
    {
    	size.setSize(newsize);
    	keyWidth = newsize.width/(keys+1);
    	keysOffset = (newsize.width%(keys+1))/2;
    	halfkeysOffset = (newsize.width%(keys+1)+keyWidth)/2;
    }

    /**
    * mouse listeners : detect key pressed
    */
    public  void mouseClicked(MouseEvent e)  {}
    public  void mouseEntered(MouseEvent e)  {}
    public  void mouseExited(MouseEvent e)   {}
    public  void mouseMoved(MouseEvent e)    {}

    /** mouse down: detect the key and post key event to parent*/
    public  void mousePressed(MouseEvent e)  {  getKey(e.getX(), e.getY()); 	}
    public  void mouseDragged(MouseEvent e)  {  getKey(e.getX(), e.getY()); 	}

    /** mouse up: no more key pressed*/
    public  void mouseReleased(MouseEvent e) {}  //{keyPressedEvent(-1,-1); }

    /** Helper function to detect key pressed*/
    private void getKey(int x, int y)
    {
     	int note =-1;
     	if(y>20 && y<55 && x>halfkeysOffset) //half tones
     	{
     		note=(x-halfkeysOffset)/keyWidth;
     		if(note>=keys || !halfValid[note])
     			note=-1;
     		else
     		{
     			note=halfToNote(note);
     			keyPressedEvent(note);
     		}
     	}
     	
     	if(y>20 && x>keysOffset && note<0) //either under halfkeys, out of bounds or in field without half key (halfkeys[i]=-1)
     	{
     		note=(x-keysOffset)/keyWidth;
     		if(note>keys)
     			note=-1;
     		else
     			keyPressedEvent(keyToNote(note));
     	}
     	if(note<0)
     	  keyPressedEvent(-1);

    }

    /**
    * Paint
    */
		public void paint(Graphics g)	
		{	
			update(g);
  	}
  	
  	public void update(Graphics g)
  	{
  		if(!size.equals(this.getSize()) )
  			setFieldSize(this.getSize());
  		g.setColor(Color.white);
  		g.drawLine(2,2,size.width-2,2);
  		g.drawLine(2,2,2,20);
  		g.drawLine(4,4,4,18);
  		g.drawLine(4,4,18,4);
  		
  		g.setColor(Color.black);
  		g.drawLine(size.width-2,2,size.width-2,20);
  		g.drawLine(2,20,size.width-2,20);
  		g.drawLine(18,4,18,18);
  		g.drawLine(4,18,18,18);
  		
  		g.setColor(Color.red);
  		g.drawLine(6,6,16,16);
  		g.drawLine(6,16,16,6);
  		
  		//draw keys
  		int x=keysOffset+1;
  		int dx=keyWidth-2;
  		for(int i=0; i<=keys; i++)
  		{
  			g.setColor(Color.white);
  			g.fillRect(x,21,dx,77);
  			g.setColor(Color.black);
  			g.drawLine(x,98,x+dx,98);
  			g.drawLine(x+dx,21,x+dx,98);
  			x+=keyWidth;
  		}
  		//draw halfkeys
  		x=halfkeysOffset+1;
  		for(int i=0; i<keys; i++)
  		{
  			if(halfValid[i])
  			{
    			g.setColor(Color.black);
    			g.fillRect(x,20,dx,35);
    			g.setColor(Color.gray);
   				g.drawLine(x,20,x,54);
  			}
  			x+=keyWidth;
  		}  		
  	}
 	}
	
  /**************************************************************************************************************************************/
  /**
  * Inner class KeyboardLed
  */
	class KeyboardLed extends Component implements Runnable
	{
		private Dimension size;
		// private Rectangle led; width&height are 30 by definition, and with total width=50, x=(50-30)/2, which leaves us with only y
		private int led_y,led_y30;
		private int blinkRate;
    private long blinkTime;
    private Thread blinkerThread;
		private boolean flashing;
		public KeyboardLed()
		{
			super();
			size = new Dimension();
			blinkRate = -1;
			blinkTime=-1;
			flashing = false;
			blinkerThread = null;
		}
	
    /**
    * Sizes and offset vars
    */
		public Dimension getPreferredSize()
    {
      return new Dimension(50,this.getSize().height);
    }
    private void setFieldSize(Dimension newsize)
    {
    	size.setSize(newsize);
    	led_y=(newsize.height-30)/2;
    	led_y30=led_y+30;
    }

    /**
    * setting blink rate & launch runner if needed
    */
    public synchronized void setBlinkRate(int rate)
    {
 			if(blinkRate<0)
    	{
    		blinkRate = rate;
    		blinkTime = System.currentTimeMillis() + rate;
   			blinkerThread = new Thread(this,"scrollboxThread");
   			blinkerThread.start();
    	}
    	else
    		blinkRate = rate;
    }
    /**
    * run: force blinking
    */
    public void run()
    {
      if(Thread.currentThread() == blinkerThread)
      {
       	try
      	{
     	    long sleeptime;
     	    while(blinkRate>0)
      	  {
    	  	  sleeptime = blinkTime - System.currentTimeMillis();
    	  	  Thread.sleep(sleeptime);
      	    blinkTime = System.currentTimeMillis() + blinkRate;
      	
     	      flashing = true;
     	      this.repaint();
     	      Thread.sleep(120);
     	      flashing=false;
     	      this.repaint();
    	    }
      	}
        catch(InterruptedException e)
        {
          System.out.println(e.toString() );
        }


      }    	
    }

    /**
    * Paint
    */
		public void paint(Graphics g)	
		{	
			update(g);
  	}
  	
  	public void update(Graphics g)
  	{
  		if(!size.equals(this.getSize()))
  			setFieldSize(this.getSize());
    	g.setColor((flashing)?Color.red:Color.lightGray);
  		g.drawLine(10, led_y, 30,led_y30);
  		g.drawLine(10, led_y30, 30,led_y);
  		g.drawLine(10, led_y, 30,led_y);
  		g.drawLine(10, led_y30, 30,led_y30);
  		g.drawLine(10, led_y, 10,led_y30);
  		g.drawLine(30, led_y, 30,led_y30);
  	}
	}
	
		
}


