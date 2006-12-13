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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
**************************************************************************/


/*
** $Id: misc.c,v 1.3 2004/11/18 23:51:52 cvs Exp $
*/

#include <ctype.h>
#include <stdio.h> // fopen, fwrite & fclose
#include <sys/time.h>
#include <unistd.h>

#include "misc.h"
#include "ts-mem.h"
#include "threads.h"

void dumpfile ( w_byte *filename, w_byte *data, w_size count)
{
#if defined(LINUX) || defined(NETBSD)
	FILE *fp;
	unsigned int tmp;
	char buffer [ 256];
	
	fp = fopen ( filename, "wb");

	if ( fp != NULL)
	{
		tmp = fwrite ( data, sizeof ( char), count, fp); 
		if ( count != tmp)
		{
			x_snprintf ( buffer, 256, "to less bytes writen\n");
			PutString ( buffer);
		}

		fclose ( fp);
	}
	else
		x_snprintf ( buffer, 256, "couldn't make file %s\n", filename);
		PutString ( buffer);
#endif
}
                                                                                                                                                                                                                                
#if defined(LINUX) || defined(NETBSD)
unsigned long timeDifference(struct timeval *start, struct timeval *stop) {

  if (start->tv_sec == stop->tv_sec) {
    return stop->tv_usec - start->tv_usec;
  }  else {
    return start->tv_usec + stop->tv_usec + (1000000 * (stop->tv_sec - start->tv_sec));
  }
  
}
#endif

#define TEXT_BUFFER_SIZE 18

void hexdump(w_byte *data, w_size count) {

  w_byte text[TEXT_BUFFER_SIZE];
  char buffer[256];
  w_size i;
  w_size j;
  
  j = 0;
  memset(text, 0, TEXT_BUFFER_SIZE);
  x_snprintf(buffer, 256, "Dumping %d bytes, starting from 0x%08x to 0x%08x.\12\15", count, (int)data, (int)(data + count));
  PutString(buffer);
  x_snprintf(buffer, 256, " 0x%08x\12\15      +\12\15", (int)data);
  PutString(buffer);
  x_snprintf(buffer, 256, " 0x00000000 :  ");
  PutString(buffer);
  text[0] = '|';
  for (i = 0; i < count; i++) {
    x_snprintf(buffer, 256, "%02x  ", data[i]);
    PutString(buffer);
    text[j + 1] = isgraph(data[i]) ? data[i] : '.';
    j += 1;
    if (j == 16) {
      j = 0;
      x_snprintf(buffer, 256, "  %s|\12\15 0x%08x :  ", text, i + 1);
      PutString(buffer);
      memset(text, 0, TEXT_BUFFER_SIZE);
      text[0] = '|';
    }
  }
  if (j != 0) {
    for (i = j; i < 16; i++) {
      x_snprintf(buffer, 256, "--  "); 
      PutString(buffer);
    }
    x_snprintf(buffer, 256, "  %s", text);
    PutString(buffer);
    for (i = j; i < 16; i++) {
      x_snprintf(buffer, 256, "-");
      PutString(buffer);
    }
    x_snprintf(buffer, 256, "|");
    PutString(buffer);
  }
  x_snprintf(buffer, 256, "\12\15 END OF DUMP \12\15");
  PutString(buffer);
}


void getfile ( w_thread thread, w_byte *filename, w_byte **data, w_size *count)
{
#ifdef LINUX
	FILE *fp;
	char buffer [ 256];
	w_size length;

	*count = 0;
	fp = fopen ( filename, "rb");

	if ( fp != NULL)
	{
		if ( !fseek ( fp, 0L, SEEK_END))
		{
			length = ( w_size)ftell ( fp);

			rewind ( fp);

			*data = ( w_byte *)allocMem(length * sizeof ( w_byte));
			if ( *data)
			{
				*count = fread ( *data, sizeof ( unsigned char), length, fp); 
				if ( length != *count)
				{
					x_snprintf ( buffer, 256, "could read enough bytes %d\n", *count);
					PutString ( buffer);	
				}
			}
			else
			{
				x_snprintf ( buffer, 256, "couldn't allocate enough memory\n");
				PutString ( buffer);	
			}
		}
		fclose ( fp);
	}
	else
	{
		x_snprintf ( buffer, 256, "couldn't open file %s\n", filename);
		printf ( "couldn't open file %s\n", filename);
		PutString ( buffer);
	}
#endif
}

