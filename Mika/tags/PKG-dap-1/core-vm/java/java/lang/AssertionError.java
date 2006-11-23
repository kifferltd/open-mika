/**************************************************************************
* Copyright (C) 2005 Chris Gray, /k/ Embedded Java Solutions.             *
* Permission is hereby granted to distribute this source code under the   *
* terms of the Wonka Public Licence.                                      *
**************************************************************************/

/*
** $Id: AssertionError.java,v 1.2 2006/02/17 10:53:19 cvs Exp $
*/

package java.lang;

public class AssertionError extends Error {

  public AssertionError() {
  }

  public AssertionError(boolean b) {
    super(b ? "true" : "false");
  }

  public AssertionError(char c) {
    super(Character.toString(c));
  }

  public AssertionError(double d) {
    super(Double.toString(d));
  }

  public AssertionError(float f) {
    super(Float.toString(f));
  }

  public AssertionError(int i) {
    super(Integer.toString(i));
  }

  public AssertionError(long l) {
    super(Long.toString(l));
  }

  public AssertionError(Object o) {
    super(o == null ? null : o.toString());
    if (o instanceof Throwable) {
      initCause((Throwable)o);
    }
  }
}

