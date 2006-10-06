/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: verify.c,v 1.3 2004/11/30 10:08:10 cvs Exp $
*/

#include "clazz.h"
#include "checks.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "threads.h"
#include "ts-mem.h"
#include "wonka.h"
#include <string.h>  /* for strlen */

/*
** References of the form [J+JVM ...] are to the book _Java and the Java
** Virtual Machine_ by Robert Staerk, Joachim Schmid, and Egon Boerger.
*/

/*
** Generate an instance of VerifyError using the name of the failing class
** and two strings, one C (ISO 8859-1) and one Java.
*/

void createVerifyError(w_clazz failing_clazz, char *msg, w_string str) {

  char    *cptr;
  w_char  *buffer;
  w_size   length;
  w_string message;
  w_thread thread = currentWonkaThread;

  length = string_length(failing_clazz->dotified);
  if (msg) {
    length += 2 + strlen(msg);
  }
  if (str) {
    length += 2 + string_length(str);
  }

  buffer = allocMem(length * sizeof(w_char));
  woempa(1, "buffer: %p, length %d\n", buffer, length);
  if (!buffer) {
    wabort(ABORT_WONKA, "Unable to allocate buffer\n");
  }

  length = w_string2chars(failing_clazz->dotified, buffer);
  woempa(1, "Wrote `%w', length now %d chars\n", failing_clazz->dotified, length);
  if (msg) {
    buffer[length++] = ':';
    buffer[length++] = ' ';
    for (cptr = msg; *cptr;) {
      buffer[length++] = *cptr++;
    }
    woempa(1, "Wrote `%s', length now %d chars\n", msg, length);
  }
  if (str) {
    buffer[length++] = ':';
    buffer[length++] = ' ';
    length += w_string2chars(str, buffer + length);
    woempa(1, "Wrote `%w', length now %d chars\n", str, length);
  }

  message = unicode2String(buffer, length);
  if (!message) {
    wabort(ABORT_WONKA, "Unable to create message\n");
  }
  releaseMem(buffer);
  woempa(9,"%w: VerifyError: %w\n", failing_clazz->dotified, message);
  throwException(thread, clazzVerifyError, "%w", message);
  failing_clazz->failure_message = message;

}

/*
** So far we do not perform any verification ...
** TODO: go yak-trekking in Mongolia
*/

void verifyClass(w_clazz clazz) {


}

