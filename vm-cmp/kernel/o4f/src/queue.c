/**************************************************************************
* Copyright (c) 2021 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
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

#include "oswald.h"

/* 
 * Prototype:
 *    x_status x_queue_create(x_queue queue, void *queue_start,
 *                         w_size queue_size);
 * Description: 
 *    Creates a message queue that is typically used for
 *    inter-thread communication.  
 * Implementation:
 */
                                                                                                         
x_status x_queue_create(x_queue queue, void *messages, w_size capacity) {
    QueueHandle_t qh = xQueueCreate(capacity, sizeof(void*));
    queue->handle = qh;
    if (!qh) {
        return xs_insufficient_memory;
    }
    queue->magic = 0xb5a69788;
    return xs_success;
}

/*
 * Prototype:
 *   x_status x_queue_delete(x_queue queue);
 * Description:
 *   Deletes the specified message queue.
 */
 
x_status x_queue_delete(x_queue queue) {
    if (queue->magic != 0xb5a69788) {
        return xs_deleted;
    }
    if (queue->handle) {
        vQueueDelete(queue->handle);
    }
    queue->magic = 0;
    queue->handle = 0;
    return xs_success;
}

/*
 * Prototype:
 *   x_status x_queue_receive(x_queue queue, void **destination_ptr,
 *                         x_sleep wait_option);
 * Description:
 *   This service retrieves a message from the queue.  The message 
 *   retrieved is copied from the queue into the memory area specified
 *   by the destination pointer.
 * Implementation:
 */
unsigned int x_queue_receive(x_queue queue, void **msg, x_sleep owait) {
    if (queue->magic != 0xb5a69788 || !queue->handle) {
        return xs_deleted;
    }
    setFlag(x_thread_current()->flags, TF_RECEIVING);
    BaseType_t rc = xQueueReceive(queue->handle, msg, owait);
    unsetFlag(x_thread_current()->flags, TF_RECEIVING);
    return rc == pdPASS ? xs_success : xs_no_instance;
}

/*
 * Prototype:
 *   x_status x_queue_send(x_queue queue, void *source_ptr, 
 *                      x_sleep wait_option);
 * Description:
 *   This service sends a message to the specified message queue.  
 *   The message send is copied to the queue from the memory area
 *   specified by the source pointer.
 * Implementation:
 */
                                                                                                 
unsigned int x_queue_send(x_queue queue, void *msg, x_sleep wait) {
    if (queue->magic != 0xb5a69788 || !queue->handle) {
        return xs_deleted;
    }
    setFlag(x_thread_current()->flags, TF_SENDING);
    BaseType_t rc = xQueueSendToBack(queue->handle, msg, wait);
    unsetFlag(x_thread_current()->flags, TF_SENDING);
    return rc == pdPASS ? xs_success : xs_no_instance;
}

/*
 * Prototype:
 *   x_status x_queue_flush(x_queue queue, void(*do_this)(void *data))
 * Description:
 *   This service retrieves each message from the queue in turn.  The 
 *   function `do_this' is called on each message as it is received.
 * Implementation:
 */
 
unsigned int x_queue_flush(x_queue queue, void(*do_this)(void *data)) {
    if (queue->magic != 0xb5a69788 || !queue->handle) {
        return xs_deleted;
    }
    setFlag(x_thread_current()->flags, TF_RECEIVING);
    while (uxQueueMessagesWaiting(queue->handle)) {
        void *data;
        BaseType_t rc = xQueueReceive(queue->handle, &data, 0);
        if (rc != pdPASS) {
            break;
        }
        do_this(data);
    }
    unsetFlag(x_thread_current()->flags, TF_RECEIVING);

    return xs_success;
}
