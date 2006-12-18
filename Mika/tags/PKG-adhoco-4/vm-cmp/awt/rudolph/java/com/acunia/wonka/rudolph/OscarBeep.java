/**************************************************************************
* Copyright (c) 2004 Chris Gray, /k/ Embedded Java Solutions.             *
* All rights reserved.                                                    *
**************************************************************************/

package com.acunia.wonka.rudolph;

import java.io.*;

/**
 ** Implementation of Toolkit.beep() for Oscar.
 */
class OscarBeep extends BeepImpl {

  /**
   ** Path to the beep device.
   */
  static final String OSCAR_BEEP_DEVICE = "/proc/sys/karo/beep";
    
  FileOutputStream beep_fos;
  
  boolean flag = false;
  int internalFrequency = -1;
  
  public OscarBeep()
  {
      try
      {
        File beep_file = new File(OSCAR_BEEP_DEVICE);
        beep_fos = new FileOutputStream(beep_file);
      }
      catch(IOException ex)
      {
          ex.printStackTrace();
      }
  }
  
  public synchronized void beep() {
      
      //long begin = System.currentTimeMillis();
      
      //int[] oldVolume = getCurrentVolume();
      
      //System.out.println("(1) "+(System.currentTimeMillis()-begin));
      
      //System.out.println("currentVolume: ["+oldVolume[0]+":"+oldVolume[1]+"]");
      
      //setVolume(this.volume, this.volume);
      
      //System.out.println("(2) "+(System.currentTimeMillis()-begin));
      
      if(this.internalFrequency==-1)
      {
          this.internalFrequency = this.frequency*100;
      }      
      
      setFrequency();            
      // due to problesm with the beep pseudo file
      if(this.flag)
      {
        this.internalFrequency += 1;
      }
      else
      {
        this.internalFrequency -= 1;
      }
      
      this.flag = !this.flag;
      
      //System.out.println("(3) "+(System.currentTimeMillis()-begin));
      
      switchBeepOn();
      
      //System.out.println("(4) "+(System.currentTimeMillis()-begin));
      
      try
      {
        Thread.sleep(this.duration);
      }
      catch(InterruptedException ex)
      {          
      }
      
      //System.out.println("(5) "+(System.currentTimeMillis()-begin));
      
      switchBeepOff();
      
      //System.out.println("(6) "+(System.currentTimeMillis()-begin));
      
      //setVolume(oldVolume[0], oldVolume[1]);
      
      //System.out.println("(7) "+(System.currentTimeMillis()-begin));
  }
    
  protected void switchBeepOn()
  {
      switchBeep("-1");
  }
  
  protected void switchBeepOff()
  {
      switchBeep("0");
  }  
  
  protected void setFrequency()
  {
      switchBeep("0 "+this.internalFrequency);
  }
  
  protected void switchBeep(String param)
  {
      //System.out.println("write param: '"+param+"'");
      
    try {
        
        
        //PrintStream beep_ps = new PrintStream(beep_fos);		
        
        beep_fos.write(param.getBytes());
		
//        beep_ps.print(param);                                
		
//        beep_ps.close();
        beep_fos.flush();        
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }      
  }
}

