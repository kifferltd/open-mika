/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: CharSequence.java,v 1.1 2006/03/27 08:19:05 cvs Exp $
 */
package java.lang;

/**
 * CharSequence:
 *
 * @author ruelens
 *
 * created: Mar 24, 2006
 */
public interface CharSequence {
  public int length();
  public char charAt(int index);
  public CharSequence subSequence(int start, int end);
  public String toString();
  
}
