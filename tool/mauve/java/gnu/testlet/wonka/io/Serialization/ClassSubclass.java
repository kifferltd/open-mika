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


package gnu.testlet.wonka.io.Serialization;

import java.util.Arrays;

public class ClassSubclass extends SM_Simple {
  private boolean bool1 = true;
  private boolean bool2 = false;

  private int[] intArraySubclass = {63, 31, 9999};

  public boolean equals(Object object) {
    ClassSubclass other = (ClassSubclass) object;
    return super.equals(other)
        && (this.bool1 == other.bool1)
        && (this.bool2 == other.bool2)
        && Arrays.equals(this.intArraySubclass, other.intArraySubclass);
  }
}