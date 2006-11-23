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
