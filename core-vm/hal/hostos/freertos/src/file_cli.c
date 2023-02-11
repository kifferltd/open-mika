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

#include "FreeRTOS.h"
#include "FreeRTOS_CLI_Console.h"
#include "cli.h"
#include "ff_stdio.h"

const CLI_Command_Definition_t xFileCommandDefinition =
{
    "file",
    "\r\nfile <...>:\r\n File operations.\r\n"
    "    syntax: file " ITAL "op path" NORM "\r\n"
    "    where " ITAL "op" NORM " is one of\r\n"
    "        " BOLD "stat" NORM " - show file or directory properties\r\n"
    "r\n",
    _file,
    -1
};

static int _file_output_line_number;

BaseType_t _file( char * pcWriteBuffer,
                  size_t xWriteBufferLength,
                  const char * pcCommandString )
{
    FF_Stat_t pxStatBuffer; 
    FF_FindData_t *pxFindStruct;
    const char * actionParameter = NULL;
    const char * pathParameter = NULL;
    BaseType_t xParameterStringLength = 0;
    BaseType_t xReturnValue = 0;
    int32_t xNumCharsPrinted = 0;

    configASSERT( NULL != pcWriteBuffer );

    actionParameter = FreeRTOS_CLIGetParameter( pcCommandString, 1, &xParameterStringLength);
    pathParameter = FreeRTOS_CLIGetParameter( pcCommandString, 2, &xParameterStringLength);
    // TODO what about embedded spaces?

    if (0 == strncmp("stat", actionParameter, 4)) {
        int rc = ff_stat(pathParameter, &pxStatBuffer );
        snprintf( pcWriteBuffer, xWriteBufferLength, "Executed ff_stat on %s, rc = %d\n", pathParameter, rc);
        return 0;
        if (0 <= rc) {
            switch(_file_output_line_number++ ) {
            case 0u:
                xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength,
                    "%s is a %s, size = %d, atime = %d\r\n",
                    pathParameter,
                    FF_IFDIR == pxStatBuffer.st_mode ? "directory" : "regular file",
                    pxStatBuffer.st_size,
                    pxStatBuffer.st_atime
                );
                configASSERT( ( xNumCharsPrinted >= 0 ) && ( xNumCharsPrinted < ( int32_t )xWriteBufferLength ) );

                if (FF_IFDIR == pxStatBuffer.st_mode) {
                    xReturnValue = pdPASS;
                }
                break;

            case 1u:
                pxFindStruct = ( FF_FindData_t * ) pvPortMalloc( sizeof( FF_FindData_t ) );
                configASSERT( NULL != pxFindStruct );
                memset( pxFindStruct, 0x00, sizeof( FF_FindData_t ) );
                if( 0 == ff_findfirst( pathParameter, pxFindStruct ) ) {
                    xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "Directory contents:\r\n");
                    xReturnValue = pdPASS;
                }
                else {
                    xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "Could not read directory, errno = %d\r\n", stdioGET_ERRNO());
                }
                
                break;

            default:
                if( ( pxFindStruct->ucAttributes & FF_FAT_ATTR_DIR ) != 0 )
                {
                    xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "  directory ");
                }
                else if( pxFindStruct->ucAttributes & FF_FAT_ATTR_READONLY )
                {
                    xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "  read-only ");
                }
                else
                {
                    xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "  writeable ");
                }

                xNumCharsPrinted = snprintf( pcWriteBuffer + xNumCharsPrinted, xWriteBufferLength - xNumCharsPrinted, "%s\r\n", pxFindStruct->pcFileName);

               if ( 0 == ff_findnext( pxFindStruct ) ) {
                    xReturnValue = pdPASS;
               }
               else {
                   vPortFree( pxFindStruct );
               }
            }
        }
    }
    else {
        xNumCharsPrinted = snprintf( pcWriteBuffer, xWriteBufferLength, "unknown action %s, actions are: stat\r\n", actionParameter);
        configASSERT( ( xNumCharsPrinted >= 0 ) && ( xNumCharsPrinted < ( int32_t )xWriteBufferLength ) );
    }

    return xReturnValue;
}

#endif

