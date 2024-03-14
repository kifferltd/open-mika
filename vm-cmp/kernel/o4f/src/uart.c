/**************************************************************************
* Copyright (c) 2020, 2021, 2022, 2024 by KIFFER Ltd.                     *
* All rights reserved.                                                    *
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
#include "iot_uart.h"
#include "im4000uart.h"

#define NUM_IOT_UARTS 3
#define UART_TYPEAHEAD_BUFFER_SIZE 64;

static const char* uart_names[] = { "COM1", "COM2", "COM3", NULL};
static const char* debug_uart_name = "COM3";

static IotUARTHandle_t uart_handle[NUM_IOT_UARTS];
static SemaphoreHandle_t uart_read_mutex[NUM_IOT_UARTS];
static SemaphoreHandle_t uart_write_mutex[NUM_IOT_UARTS];
static StaticSemaphore_t uart_read_mutex_storage[NUM_IOT_UARTS];
static StaticSemaphore_t uart_write_mutex_storage[NUM_IOT_UARTS];

static IotUARTConfig_t defaultUARTConfig = { .ulBaudrate = 115200, .ucWordlength = 8u, .xParity = eUartParityNone, .xStopbits = eUartStopBitsOne, .ucFlowControl = 1u };
static char uart_rx_buf[1];

void x_uart_init(im4000_com_t uart_idx) {
    // TODO check n is in range else abort

    uart_read_mutex[uart_idx] = xSemaphoreCreateMutexStatic(&uart_read_mutex_storage[uart_idx]);
    configASSERT( NULL != uart_read_mutex[uart_idx] );
    uart_write_mutex[uart_idx] = xSemaphoreCreateMutexStatic(&uart_write_mutex_storage[uart_idx]);
    configASSERT( NULL != uart_write_mutex[uart_idx] );

    uart_handle[uart_idx] = iot_uart_open( uart_idx );
    configASSERT( NULL != uart_handle[uart_idx] );

   /* Set default UART configuration */
    x_int iResult = iot_uart_ioctl(uart_handle[uart_idx], eUartSetConfig, &defaultUARTConfig);
    configASSERT( IOT_UART_SUCCESS == iResult );

}

static im4000_com_t name2im4000_com(const char* name) {
    im4000_com_t uart_idx = 0xdeadbeef;
    for (char **candidate = uart_names; *candidate; candidate++) {
        if (strncmp(name, *candidate, 4) == 0) {
            uart_idx = candidate - (char**)uart_names;
            break;
        }
    }
    configASSERT( 0xdeadbeef != uart_idx );

    return uart_idx;
}

x_int x_uart_init_by_name(const char* name) {
    im4000_com_t uart_idx = name2im4000_com(name);
    x_uart_init(uart_idx);
    return uart_idx;
}

x_int x_uart_read(im4000_com_t uart_idx, const void *buf, size_t count) {
    if (!uart_read_mutex[uart_idx] || !uart_handle[uart_idx]) {
        wabort(ABORT_WONKA, "UART %d must be initialised before reading!", uart_idx);
    }

    w_int bytes_read = -1;
    w_boolean isSchedulerRunning = xTaskGetSchedulerState() == taskSCHEDULER_RUNNING;
    if (isSchedulerRunning) { 
      xSemaphoreTake(uart_read_mutex[uart_idx], portMAX_DELAY);
    }

    w_int rc = iot_uart_read_sync(uart_handle[uart_idx], buf, count);
    if (IOT_UART_SUCCESS == rc ) {
      rc = iot_uart_ioctl(uart_handle[uart_idx], eGetRxNoOfbytes, &bytes_read);
    }
    if (IOT_UART_SUCCESS != rc ) {
      printf("iot_uart_ioctl(eGetRxNoOfbytes) returned %d\r\n", rc);
      bytes_read = -rc;
    }

    if (isSchedulerRunning) { 
      xSemaphoreGive(uart_read_mutex[uart_idx]);
    }
}

x_int x_uart_read_by_name(const char* name, const void *buf, size_t count) {
    im4000_com_t uart_idx = name2im4000_com(name);
    return x_uart_read(uart_idx, buf, count);
}

void x_uart_write(im4000_com_t uart_idx, const void *buf, size_t count) {
    if (!uart_read_mutex[uart_idx] || !uart_handle[uart_idx]) {
        wabort(ABORT_WONKA, "UART %d must be initialised before writing!", uart_idx);
    }
    
    w_boolean isSchedulerRunning = xTaskGetSchedulerState() == taskSCHEDULER_RUNNING;
    if (isSchedulerRunning) { 
      xSemaphoreTake(uart_write_mutex[uart_idx], portMAX_DELAY);
    }
    iot_uart_write_sync(uart_handle[uart_idx], buf, count);
    if (isSchedulerRunning) { 
      xSemaphoreGive(uart_write_mutex[uart_idx]);
// HACK to avoid buffer over-runs
      x_thread_sleep(count/7 + 1);
    }
}

void x_uart_write_by_name(const char* name, const void *buf, size_t count) {
    im4000_com_t uart_idx = name2im4000_com(name);
    x_uart_write(uart_idx, buf, count);
}


// TODO figure out all this async stuff - currently the code below is not used

// TODO turn this into a type-ahead buffer
static w_byte poll_buf[3];
static w_int poll_available[3];

static void x_uart_poll_callback(im4000_com_t n, IotUARTOperationStatus_t xStatus, void * pvUserContext ) {
  w_int bytes_read = 0;
  
  if (IOT_UART_SUCCESS == xStatus ) {
    iot_uart_ioctl(uart_handle[n], eGetRxNoOfbytes, &bytes_read);
    poll_available[n] |= bytes_read;
  }
}

w_int x_uart_poll(im4000_com_t n) {
  w_int result = -1;
  if (ensureDebugUartIsInitialised()) {
    w_boolean isSchedulerRunning = xTaskGetSchedulerState() == taskSCHEDULER_RUNNING;
    if (isSchedulerRunning) { 
      xSemaphoreTake(uart_read_mutex, portMAX_DELAY);
    }

    if (poll_available[n]) {
      result = poll_buf[n];
      poll_available[n] = 0;
    }

    switch (result) {
    case 3:
      printf("CONTROL_C_EVENT\n");

    case 28:
      printf("CONTROL_BREAK_EVENT\n");

    default:
      ;
    }

    if (isSchedulerRunning) { 
      xSemaphoreGive(uart_read_mutex);
    }
  }

  return result;
}


