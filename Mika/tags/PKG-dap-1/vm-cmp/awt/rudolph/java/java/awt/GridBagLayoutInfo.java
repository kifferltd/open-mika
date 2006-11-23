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

package java.awt;

// package protected class; used in GridBagLayout

class GridBagLayoutInfo implements java.io.Serializable {

  // static field ensuring compatibility between java and wonka generated serialised objects
  private static final long serialVersionUID = -4899416460737170217L;

  int xLeft;
  int yTop;
  int ncols;
  int nrows;
  int columnWidths[];
  int rowHeights[];
  double columnWeights[];
  double rowWeights[];

  GridBagLayoutInfo(int width, int height) {
    ncols = width;
    nrows = height;
    columnWidths  = new int[ncols];
    rowHeights    = new int[nrows];
    columnWeights = new double[ncols];
    rowWeights    = new double[nrows];
  }
}
