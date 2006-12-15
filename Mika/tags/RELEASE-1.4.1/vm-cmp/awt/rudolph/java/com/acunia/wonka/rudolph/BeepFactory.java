/**************************************************************************
* Copyright (c) 2004 Chris Gray, /k/ Embedded Java Solutions.             *
* All rights reserved.                                                    *
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


