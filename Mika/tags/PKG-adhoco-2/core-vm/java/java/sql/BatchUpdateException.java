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

/*
** $Id: BatchUpdateException.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

public class BatchUpdateException extends SQLException {

  private static final long serialVersionUID = 5977529877145521757L;

  private int[] updateCounts;
  
  public BatchUpdateException() {
    this(null);
  }
  
  public BatchUpdateException(int[] updateCounts) {
    this(null, updateCounts);
  }
  
  public BatchUpdateException(String reason, int[] updateCounts) {
    this(reason, null, updateCounts);
  }
  
  public BatchUpdateException(String reason, String SQLState, int[] updateCounts) {
    this(reason, SQLState, 0, updateCounts);
  }
  
  public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts) {
    super(reason, SQLState, vendorCode);
    this.updateCounts = updateCounts;
  }

  public int[] getUpdateCounts() {
    return updateCounts;
  }
  
}
