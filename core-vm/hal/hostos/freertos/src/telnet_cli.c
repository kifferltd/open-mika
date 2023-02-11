/**************************************************************************
* Copyright (c) 2023 by KIFFER Ltd. All rights reserved.                  *
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

#ifdef FREERTOS_CLI

#include "FreeRTOS_IP.h"
#include "FreeRTOS_Sockets.h"
#include "FreeRTOS_CLI_Console.h"
#include "cli.h"
#include "oswald.h"
#include "misc.h"
// just for the MIN function ...
#include "sys/param.h"

// FIXME currently we only allow a single connection to port 2323
// Solution is probably to launch a task for each accepted call

static Socket_t xServerSocket = FREERTOS_INVALID_SOCKET;
static Socket_t xAcceptedSocket = FREERTOS_INVALID_SOCKET;

static BaseType_t jdwp_enabled = 0;

static TaskHandle_t _xCliConsoleHandle;

static xConsoleIO_t _xSocketConsoleIO =
{
    .read  = _socket_read,
    .write = _socket_write
};

static const CLI_Command_Definition_t xPingCommandDefinition =
{
    "classpath",
    "\r\nclasspath:\r\nEdit the classpath.\r\n"
    "    syntax: classpath " ITAL "where what" NORM "\r\n",
    _classpath,
    0
};

static const CLI_Command_Definition_t xBreakpointCommandDefinition =
{
    "breakpoint",
    "\r\nbreakpoint:\r\nCall _sys_bkpt().\r\n",
    _breakpoint,
    0
};

static const CLI_Command_Definition_t xJdwpCommandDefinition =
{
    "jdwp",
    "\r\njdwp\r\nSpecify a JDWP connector which can be used with a JDWP client such as jdb.\r\n"
    "    syntax: jdwp " ITAL "transport server suspend" NORM "\r\n"
    "    (defaults  : dt_socket 8888   y       y)\r\n",
    _jdwp,
    0
};

static const CLI_Command_Definition_t xEchoCommandDefinition =
{
    "echo",
    "\r\necho <...>:\r\n Echos variable number of parameters.\r\n\r\n",
    _echo,
    -1
};

static const CLI_Command_Definition_t xRunCommandDefinition =
{
    "run",
    "\r\nrun:\r\n Executes a class file or (with -jar) a jar file.\r\n\r\n",
    _run,
    -1
};

static char cCommandBuffer[ CMD_MAX_INPUT_SIZE ];
static char cOutputBuffer[ CMD_MAX_OUTPUT_SIZE ];

static int32_t _socket_read( char * const pcInputBuffer,
                           uint32_t uInputBufferLength )
{
    BaseType_t lBytesReceived = FreeRTOS_recv( xAcceptedSocket, pcInputBuffer, uInputBufferLength, 0 );
    while( lBytesReceived == 0 ) {
        /* No data was received, but FreeRTOS_recv() did not return an error.  Timeout? */
        lBytesReceived = FreeRTOS_recv( xAcceptedSocket, pcInputBuffer, uInputBufferLength, 0 );
    }
    if( lBytesReceived > 0 ) {
        /* Data was received, process it */
        return lBytesReceived;
    }

    /* Error (maybe the connected socket already shut down the socket?).  Attempt graceful shutdown. */
    FreeRTOS_shutdown( xAcceptedSocket, FREERTOS_SHUT_RDWR );
    while( FreeRTOS_recv( xAcceptedSocket, pcInputBuffer, uInputBufferLength, 0 ) >= 0 ) {
        // TODO - limit the time we can spend here
        vTaskDelay( 250 );
    }
    FreeRTOS_closesocket( xAcceptedSocket );
    xAcceptedSocket = FREERTOS_INVALID_SOCKET;

    vTaskDelete(_xCliConsoleHandle);
}

void _socket_write( const char * const pcOutputBuffer,
                  uint32_t uOutputBufferLength )
{
    if (xAcceptedSocket == FREERTOS_INVALID_SOCKET) {
        return;
    }

    BaseType_t xAlreadyTransmitted = 0, xBytesSent = 0;
    size_t xLenToSend;

    while( xAlreadyTransmitted < uOutputBufferLength ) {
        /* How many bytes are left to send? */
        xLenToSend = uOutputBufferLength - xAlreadyTransmitted;
        xBytesSent = FreeRTOS_send( xAcceptedSocket, pcOutputBuffer + xAlreadyTransmitted, xLenToSend, 0 );

        if( xBytesSent >= 0 ) {
            xAlreadyTransmitted += xBytesSent;
        }
        else {
            // TODO error reporting - rc is -errno
            break;
        }
    }
}

static BaseType_t _classpath( char * pcWriteBuffer,
                         size_t xWriteBufferLen,
                         const char * pcCommandString )
{
    BaseType_t xReturnValue = pdFALSE;
    int32_t iStatus = 0;

    ( void )pcCommandString;

    configASSERT( NULL != pcWriteBuffer );

    iStatus = snprintf( pcWriteBuffer, xWriteBufferLen - 1u, "pong\r\n" );
    configASSERT( ( iStatus >= 0 ) && ( iStatus < ( int32_t )(xWriteBufferLen - 1u) ) );

    return xReturnValue;
}

static BaseType_t _breakpoint( char * pcWriteBuffer,
                               size_t xWriteBufferLen,
                               const char * pcCommandString )
{
    BaseType_t xReturnValue = pdFALSE;

    ( void )pcWriteBuffer;
    ( void )xWriteBufferLen;
    ( void )pcCommandString;

    _sys_bkpt();

    return xReturnValue;
}

#define MAX_JDWP_PARAMS_LENGTH 127
static char jdwp_buffer[MAX_JDWP_PARAMS_LENGTH+1];
static const char *jdwp_parameters = NULL;
static const char *jdwp_transportParameter;
static const char *jdwp_addressParameter;
static const char *jdwp_serverParameter;
static const char *jdwp_suspendParameter;

static BaseType_t _jdwp( char * pcWriteBuffer,
                          size_t xWriteBufferLen,
                          const char * pcCommandString )
{
    BaseType_t xReturnValue = pdFALSE;
    BaseType_t xParameterStringLength = 0;

    jdwp_transportParameter = FreeRTOS_CLIGetParameter( pcCommandString, 1, &xParameterStringLength);
    if( NULL == jdwp_transportParameter ) {
        jdwp_transportParameter = "dt_socket";
    }
    else {
        jdwp_addressParameter = FreeRTOS_CLIGetParameter( pcCommandString, 2, &xParameterStringLength);
        if( NULL == jdwp_addressParameter ) {
            jdwp_addressParameter = "8888";
        }
        else {
            jdwp_serverParameter = FreeRTOS_CLIGetParameter( pcCommandString, 3, &xParameterStringLength);
            if( NULL == jdwp_serverParameter ) {
                jdwp_serverParameter = "y";
            }
            else {
                jdwp_suspendParameter = FreeRTOS_CLIGetParameter( pcCommandString, 4, &xParameterStringLength);
                if( NULL == jdwp_suspendParameter ) {
                    jdwp_suspendParameter = "y";
                }
            }
        }
    }

    snprintf( jdwp_buffer, MAX_JDWP_PARAMS_LENGTH, "-Xrunjdwp:transport=%s,address=%s,server=%s,suspend=%s",
              jdwp_transportParameter, jdwp_addressParameter, jdwp_serverParameter, jdwp_suspendParameter );
    jdwp_parameters = jdwp_buffer;
    snprintf( pcWriteBuffer, xWriteBufferLen, "adding parameters -Xdebug %s\r\n", jdwp_buffer);

    return xReturnValue;
}

static BaseType_t _echo( char * pcWriteBuffer,
                         size_t xWriteBufferLength,
                         const char * pcCommandString )
{
    const char * pcParameter = NULL;
    BaseType_t xParameterStringLength = 0;
    BaseType_t xReturnValue = 0;
    static UBaseType_t xParameterNumber = 0u;
    int32_t xNumCharsPrinted = 0;

    configASSERT( NULL != pcWriteBuffer );

    if( 0u == xParameterNumber )
    {
        xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "parameters:\r\n" );
        configASSERT( ( xNumCharsPrinted >= 0 ) && ( xNumCharsPrinted < ( int32_t )xWriteBufferLength ) );

        xParameterNumber = 1L;
        xReturnValue = pdPASS;
    }
    else
    {
        pcParameter = FreeRTOS_CLIGetParameter( pcCommandString,
                                                xParameterNumber,
                                                &xParameterStringLength);
        if( NULL != pcParameter )
        {
            memset( pcWriteBuffer, 0x00, xWriteBufferLength );
            xNumCharsPrinted = snprintf( pcWriteBuffer,
                                         xWriteBufferLength,
                                         "%d: %.*s\r\n",
                                         xParameterNumber,
                                         xParameterStringLength,
                                         pcParameter );
            configASSERT( ( xNumCharsPrinted >= 0 ) && ( xNumCharsPrinted < ( int32_t )xWriteBufferLength ) );

            xReturnValue = pdTRUE;
            xParameterNumber++;
        }
        else
        {
            pcWriteBuffer[ 0 ] = 0x00;
            xReturnValue = pdFALSE;
            xParameterNumber = 0u;
        }
    }

    return xReturnValue;
}

static BaseType_t _run( char * pcWriteBuffer,
                         size_t xWriteBufferLen,
                         const char * pcCommandString )
{
// TODO - make this more flexible
#define MAX_COMMAND_PARAMS 32
    const char * pcParameter = NULL;
    BaseType_t xParameterStringLength = 0;
    static UBaseType_t xParameterNumber = 0u;
    int32_t xNumCharsPrinted = 0;
    int32_t xNumPrefixParams = 0;
    static char parametersCopy[CMD_MAX_INPUT_SIZE + 1];
    static char *parameterArray[MAX_COMMAND_PARAMS + 1];

    BaseType_t xReturnValue = pdFALSE;

    if (jdwp_enabled) {
      parameterArray[xNumPrefixParams + 1] = "-Xdebug";
      parameterArray[xNumPrefixParams + 2] = jdwp_buffer;
      xNumPrefixParams += 2;
    }

    parameterArray[xNumPrefixParams + 1] = parametersCopy;

    // just in case we get through the whole for-loop without getting a NULL
    command_line_argument_count = MAX_COMMAND_PARAMS;
    parameterArray[MAX_COMMAND_PARAMS] = NULL;

    for (xParameterNumber = xNumPrefixParams + 1; xParameterNumber <= MAX_COMMAND_PARAMS; ++xParameterNumber) {
        pcParameter = FreeRTOS_CLIGetParameter( pcCommandString,
                                                xParameterNumber,
                                                &xParameterStringLength);
        if( NULL != pcParameter ) {
          memcpy(parameterArray[xParameterNumber], pcParameter, xParameterStringLength);
          parameterArray[xParameterNumber][xParameterStringLength] = 0;
          parameterArray[xParameterNumber+1] = parameterArray[xParameterNumber] + xParameterStringLength + 1;
        }
        else
        {
            pcWriteBuffer[ 0 ] = 0x00;
            command_line_argument_count = xParameterNumber - 1;
            parameterArray[xParameterNumber] = NULL;
            break;
        }
    }

    xParameterNumber = 0u;
    command_line_arguments = parameterArray + 1;
    initWonka();
    configASSERT( NULL != pcWriteBuffer );

//    configASSERT( ( iStatus >= 0 ) && ( iStatus < ( int32_t )(xWriteBufferLen - 1u) ) );

    xTaskNotifyWait(0xffffffffu, 0xffffffffu, NULL, portMAX_DELAY);
//    vTaskPrioritySet(NULL, 0);

    return pdFALSE;
}

void vCommandConsoleTask(void *pSocketPtr)
{
    FreeRTOS_CLIEnterConsoleLoop( _xSocketConsoleIO,
                                  cCommandBuffer,
                                  sizeof( cCommandBuffer ),
                                  cOutputBuffer,
                                  sizeof( cOutputBuffer ) );
}

void vCommandServerTask(void *dummy)
{
    BaseType_t rc;
    struct freertos_sockaddr xBindAddress;
    struct freertos_sockaddr xAcceptedAddress;
    static const TickType_t xReceiveTimeOut = portMAX_DELAY;
    const BaseType_t xBacklog = 1;

    FreeRTOS_CLIRegisterCommand( &xPingCommandDefinition );
    FreeRTOS_CLIRegisterCommand( &xBreakpointCommandDefinition );
    FreeRTOS_CLIRegisterCommand( &xJdwpCommandDefinition );
    FreeRTOS_CLIRegisterCommand( &xEchoCommandDefinition );
    FreeRTOS_CLIRegisterCommand( &xFileCommandDefinition );
    FreeRTOS_CLIRegisterCommand( &xRunCommandDefinition );

    xServerSocket = FreeRTOS_socket( FREERTOS_AF_INET,
                               FREERTOS_SOCK_STREAM,
                               FREERTOS_IPPROTO_TCP );
    configASSERT( xServerSocket != FREERTOS_INVALID_SOCKET );
    
    FreeRTOS_setsockopt( xServerSocket,
                         0,
                         FREERTOS_SO_RCVTIMEO,
                         &xReceiveTimeOut,
                         sizeof( xReceiveTimeOut ) );
    xBindAddress.sin_port = FreeRTOS_htons( 2323 );// 0x1309; 

    // TODO: wait for network to be UP
    vTaskDelay( 5000 );

    printf("telnet_cli: binding to port 2323\n");
    rc = FreeRTOS_bind( xServerSocket, &xBindAddress, sizeof( &xBindAddress ) );
    configASSERT( rc ==0 );
    /* The bind was successful, put the server socket into listen mode and start accepting. */
    rc = FreeRTOS_listen( xServerSocket, xBacklog );
    printf("telnet_cli: bound to port 2323, rc = %d\n", rc);
    configASSERT( rc ==0 );
        
    for (;;) {
        xAcceptedSocket = FreeRTOS_accept( xServerSocket, &xAcceptedAddress, sizeof( struct freertos_sockaddr ));
        configASSERT(FREERTOS_INVALID_SOCKET != xAcceptedSocket);
        switch (xTaskCreate( vCommandConsoleTask, "Telnet CLI Console", 2048, NULL, 50, &_xCliConsoleHandle)) {
            case pdPASS:
                printf("Telnet CLI Console launched\n");
                break;

            case errCOULD_NOT_ALLOCATE_REQUIRED_MEMORY:
                printf("Telnet CLI Server not launched, out of memoryd\n");
                break;

            default: 
                printf("Telnet CLI Server not launched, reason unknown\n");
        }
    }
}

void startTelnetConsole() {
    TaskHandle_t handle = NULL;
    switch (xTaskCreate( vCommandServerTask, "Telnet CLI Server", 2048, NULL, 50, &handle)) {
        case pdPASS:
            printf("Telnet CLI Server launched\n");
            return handle;

        case errCOULD_NOT_ALLOCATE_REQUIRED_MEMORY:
            printf("Telnet CLI Server not launched, out of memoryd\n");
            return NULL;

        default: 
            printf("Telnet CLI Server not launched, reason unknown\n");
            return NULL;
    }
}

void stopTelnetConsole(TaskHandle_t handle) {
    vTaskDelete(handle);
}
#endif

