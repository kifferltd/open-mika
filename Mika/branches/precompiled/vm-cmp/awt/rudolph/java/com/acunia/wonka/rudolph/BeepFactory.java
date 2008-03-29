/**************************************************************************
* Copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/


package com.acunia.wonka.rudolph;


/**
 ** Determines the beep implementation to be used and instantiates it.
 */
class BeepFactory {

  /**
   ** The name of the system property which holds the name of the class which must be used to implement Toolkit.beep().
   */
  static final String BEEP_CLASS_PROPERTY = "wonka.rudolph.beep.impl";

  /**
   ** The default value of BEEP_CLASS_PROPERTY.
   */
  static final String BEEP_CLASS_DEFAULT = "com.acunia.wonka.rudolph.DefaultBeep";

  /**
   ** The duration of the beep in milliseconds.
   */
  static final String BEEP_DURATION_PROPERTY = "wonka.rudolph.beep.duration";

  /**
   ** The default value of BEEP_DURATION_PROPERTY.
   */
  static final int BEEP_DURATION_DEFAULT = 200;

  /**
  ** The volume of the beep, on a scale from 0 (silence) to 100.
   */
  static final String BEEP_VOLUME_PROPERTY = "wonka.rudolph.beep.volume";

  /**
   ** The default value of BEEP_VOLUME_PROPERTY.
   */
  static final int BEEP_VOLUME_DEFAULT = 100;

  /**
   ** The freqiency of the beep, in Hertz.
   */
  static final String BEEP_FREQUENCY_PROPERTY = "wonka.rudolph.beep.frequency";

  /**
   ** The default value of BEEP_FREQUENCY_PROPERTY.
   */
  static final int BEEP_FREQUENCY_DEFAULT = 1000;


  /**
   ** The object to which Toolkit.beep() will delegate.
   */
  private static BeepImpl theBeep;

  /**
   ** Get the object to which Toolkit.beep() will delegate.
   ** It will be created if it does not already exist.
   */
  static synchronized BeepImpl getInstance() {
    String beep_class_name = null;
    Class cl = null;

    if (theBeep == null) {
      try {
        beep_class_name = System.getProperty(BEEP_CLASS_PROPERTY, BEEP_CLASS_DEFAULT).trim();
        cl = Class.forName(beep_class_name);
        int d = Integer.getInteger(BEEP_DURATION_PROPERTY, BEEP_DURATION_DEFAULT).intValue();
        int v = Integer.getInteger(BEEP_VOLUME_PROPERTY, BEEP_VOLUME_DEFAULT).intValue();
        int f = Integer.getInteger(BEEP_FREQUENCY_PROPERTY, BEEP_FREQUENCY_DEFAULT).intValue();
        theBeep = (BeepImpl)cl.newInstance();
        theBeep.setDuration(d);
        theBeep.setVolume(v);
        theBeep.setFrequency(f);
      }
      catch (ClassNotFoundException cnfe) {
        System.err.println("Rudolph: failed to load beep implementation class " + beep_class_name);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    return theBeep;
  }
}


