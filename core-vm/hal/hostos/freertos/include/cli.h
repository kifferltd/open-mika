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

#include "FreeRTOS.h"
#include "task.h"

#define CMD_MAX_INPUT_SIZE 127
#define CMD_MAX_OUTPUT_SIZE 255

#define NORM "\033[0m"
#define BOLD "\033[1m"
#define ITAL "\033[3m"
#define UNDR "\033[4m"
#define STRK "\033[9m"

static int32_t _socket_read( char * const pcInputBuffer, uint32_t uInputBufferLength );

void _socket_write( const char * const pcOutputBuffer, uint32_t uOutputBufferLength );

static BaseType_t _classpath( char * pcWriteBuffer, size_t xWriteBufferLen, const char * pcCommandString );

static BaseType_t _breakpoint( char * pcWriteBuffer, size_t xWriteBufferLen, const char * pcCommandString );

static BaseType_t _jdwp( char * pcWriteBuffer, size_t xWriteBufferLen, const char * pcCommandString );

static BaseType_t _echo( char * pcWriteBuffer, size_t xWriteBufferLength, const char * pcCommandString );

static BaseType_t _run( char * pcWriteBuffer, size_t xWriteBufferLen, const char * pcCommandString );

static BaseType_t _file( char * pcWriteBuffer, size_t xWriteBufferLength, const char * pcCommandString );

extern const CLI_Command_Definition_t xFileCommandDefinition;

void startTelnetConsole(void);

void stopTelnetConsole(TaskHandle_t handle);


