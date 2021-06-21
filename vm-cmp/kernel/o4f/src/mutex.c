/**************************************************************************
* Copyright (c) 2021 by Kiffer Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Kiffer Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include <sys/time.h>

#include "oswald.h"

/*
 * Prototype:
 *   x_status x_mutex_create(x_mutex mutex);
 * Description:
 *   Creates a mutex.
 * Implementation:
 *   It is just mapped to the mutex function provided by FreeRTOS.
 */
x_status x_mutex_create(x_mutex mutex) {

  mutex->owner_mutex = xSemaphoreCreateMutex();
  if (!mutex->owner_mutex) {
    return xs_insufficient_memory;
  }

  return xs_success;
}

/*
 * Prototype:
 *   x_status x_mutex_delete(x_mutex mutex);
 * Description:
 *   Deletes the specified mutex.
 * Implementation:
 *   It is just mapped to the mutex function provided by FreeRTOS.
 */
x_status x_mutex_delete(x_mutex mutex) {

  vSemaphoreDelete(mutex->owner_mutex);

  return x_success;
}

/*
 * Prototype:
 *   x_status x_mutex_lock(x_mutex mutex, x_sleep timeout);
 * Description:
 *   Tries to lock the specified mutex within the specified time.
 * Implementation:
 *   It is just mapped to the mutex function provided by FreeRTOS.
 */
x_status x_mutex_lock(x_mutex mutex, x_sleep timeout) {

  BaseType_t rc = xSemaphoreTake(mutex->owner_mutex, timeout);

  return rc == pdPASS ? xs_success : xs_no_instance;
}

/*
 * Prototype:
 *   x_status x_mutex_unlock(x_mutex mutex);
 * Description:
 *   Unlocks the specified mutex.
 * Implementation:
 *   It is just mapped to the mutex function provided by FreeRTOS.
 */
x_status x_mutex_unlock(x_mutex mutex) {

  BaseType_t rc = xSemaphoreGive(mutex->owner_mutex);

  return rc == pdPASS ? xs_success : xs_not_owner;
}
