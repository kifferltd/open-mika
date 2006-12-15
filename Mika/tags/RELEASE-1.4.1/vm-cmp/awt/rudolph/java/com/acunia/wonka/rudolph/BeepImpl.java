/**************************************************************************
* Copyright (c) 2004 Chris Gray, /k/ Embedded Java Solutions.             *
* All rights reserved.                                                    *
**************************************************************************/

package com.acunia.wonka.rudolph;

/**
 ** Skeleton for a Toolkit.beep() implementation.
 ** The API allows the duration, volume, and frequency of the beep to be set.
 ** On some implementations, some or all of these parameters may have no effect.
 */
abstract class BeepImpl implements Beep {
  /**
   ** The beep duration, in milliseconds.
   */
  protected int duration;

  /**
   ** The beep volume, on a scale from 0 (silence) to 100 (max).
   */
  protected int volume;

  /**
   ** The beep frequency, in Hertz.
   */
  protected int frequency;

  /**
   ** Default constructor used by Toolkit initialization.
   */
  protected BeepImpl() {}

  /**
   ** Set the beep duration in milliseconds.
   */
  protected void setDuration(int d) {
    duration = d;
  }

  /**
   ** Set the beep volume, on a scale from 0 (silence) to 100 (max).
   */
  protected void setVolume(int v) {
    volume = v;
  }

  /**
   ** Set the beep frequency in Hertz.
   */
  protected void setFrequency(int f) {
    frequency = f;
  }

  /**
   ** Sound the beep.
   */
  public abstract void beep();
}

