/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: loempa.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include "loempa.h"
#include "oswald.h"
#include <stdio.h>
#include <stdarg.h>
#include <windows.h>

#define BSIZE 160

size_t write(int fd, const void *buf, size_t count); 

/*
** DEBUG levels
**	Level 5: show all the available information
**	Level 7: show only a message when you enter a function or there is an error
**	Level 9: show only the errors that occured
*/

void loempa(const int level, const char *fmt, ...) {

	if (level >=5 ) {
		va_list			ap;
		char			buffer[BSIZE];
		TCHAR			bluffer[BSIZE];
		unsigned char	* cursor = buffer;
		unsigned int	i;

		for (i = 0; i < BSIZE; i++) {
			*cursor++ = 0;
		}
		_snprintf(buffer, BSIZE, "O4W : ","");
		va_start(ap, fmt);
		_vsnprintf(buffer + strlen(buffer), BSIZE - strlen(buffer), fmt, ap);
		va_end(ap);

		// copy the buffer for writing to a windows screen
		for(i=0;i<strlen(buffer);i++){
			bluffer[i]=buffer[i];
		}
		bluffer[strlen(buffer)-1] = '\0';

		#ifdef WINCE

			/*
			** Write the message to a file
			** Write the message to the listbox
			** When there are more the 200 messages posted, clear them!
			*/

			fprintf(fp_1,buffer);
			SendMessage(ListBox,LB_ADDSTRING,strlen(buffer),(LPARAM)(LPTSTR )bluffer);
			if(SendMessage(ListBox,LB_GETCOUNT,0,0) > 200){
				SendMessage(ListBox,LB_RESETCONTENT,0,0);
				fclose(fp_1);
				fopen("My Documents/l4w.txt","w"); 
			}
			UpdateWindow(ListBox);
		#endif

		#ifdef WINNT

			/*
			** Just write to the command prompt
			*/

			write(1,buffer, strlen(buffer ));
		#endif
	}
}
