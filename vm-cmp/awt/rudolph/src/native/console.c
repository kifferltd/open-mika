/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/* 
** $Id: console.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ 
*/

#include <string.h>

#include "wstrings.h"
#include "ts-mem.h"
#include "threads.h"
#include "canvas.h"
#include "console.h"
#include "platform.h"
#include "Font.h"
#include "driver_byteserial.h"


void console_clear(r_buffer buffer) {
   memset((unsigned char *)buffer->data, 0, pixels2bytes(buffer->fw * buffer->fh));
}

void console_PutString(char *text) {
  static r_buffer buffer = NULL;
  static r_font font = NULL;
  static int y = 0;
  
  if (font == NULL) {
    // Load the console font:
    Font_initialize ("courR12", BOOTCLASSDIR"/font/courR12.pcf");
    font = Font_getFont((char *)"courR12", 1, 12);
    Font_loadFont(font);

    // Allocate and initialize off-screen buffer:
    buffer = (r_buffer)allocMem(sizeof(r_Buffer) + pixels2bytes(screen->width * screen->height) + 2);
    buffer->fw = screen->width;
    buffer->fh = screen->height;
              
    // Clear console:
    console_clear(buffer);
        
    // Initialize parameters: */
    y = 10;
  }
  
  if (y > screen->height - 4 * font->size) {
    console_clear(buffer);
    y = 10;
  }
  else {
    y = y + 2 * font->size;
  }

  // Copy buffer to video memory:
  memcpy((unsigned char *)screen->video, (unsigned char *)buffer->data, (size_t)pixels2bytes(screen->width * screen->height));
}

#define CONSOLE_X_OFFSET 5
#define CONSOLE_Y_OFFSET 2
#define CONSOLE_FONT_SIZE 12
#define CONSOLE_FONT_HEIGHT 20

typedef struct w_Control_Console {
  r_buffer buffer;
  r_font   font;
  w_int    x;
  w_int    y;
} w_Control_Console;

typedef w_Control_Console *w_control_console;

void console_cls(w_control_console cons) {
  cons->x = CONSOLE_X_OFFSET;
  cons->y = CONSOLE_Y_OFFSET;
}

void console_cr(w_control_console cons) {
  memcpy((unsigned char *)(screen->video + screen->width * cons->y), (unsigned char *)cons->buffer->data, (size_t)pixels2bytes(screen->width * CONSOLE_FONT_HEIGHT));

  cons->x = CONSOLE_X_OFFSET;
}

void console_lf(w_control_console cons) {
  console_cr(cons); // Do a cr too, for good measure
  cons->y += CONSOLE_FONT_HEIGHT;
  memset((unsigned char *)cons->buffer->data, 0, pixels2bytes(screen->width * CONSOLE_FONT_HEIGHT));
  if (cons->y >= screen->height - CONSOLE_FONT_HEIGHT) {
    console_cls(cons);
  }
}

w_void con_initDevice(w_device device) {
  w_control_console con;
  device->control = allocMem(sizeof(w_Control_Console));
  con = device->control;
}

w_void con_termDevice(w_device device) {
  releaseMem(device->control);
}

w_device con_open(w_device device, w_driver_perm mode) {
  w_control_console cons = device->control;
  
  cons->buffer = NULL;
  cons->font = NULL;
  cons->x = 0;
  cons->y = 0;

  if (cons->font == NULL) {
    // Load the console font:
    Font_initialize("courR12", BOOTCLASSDIR"/font/courR12.pcf");
    cons->font = Font_getFont((char *)"courR12", 1, 12);
    Font_loadFont(cons->font);
  }

  if (cons->buffer == NULL) {
    // Allocate and initialize off-screen buffer:
    cons->buffer = (r_buffer)allocMem(sizeof(r_Buffer) + pixels2bytes(screen->width * CONSOLE_FONT_HEIGHT) + 2);
    cons->buffer->fw = screen->width;
    cons->buffer->fh = CONSOLE_FONT_HEIGHT;
  }
        
  // Initialize parameters:
  cons->x = CONSOLE_X_OFFSET;
  cons->y = CONSOLE_Y_OFFSET;

  device->usage += 1;
  device->driver->usage += 1;
  
  return device;
}

w_void con_close(w_device device) {
  w_control_console cons = device->control;

  if (cons->buffer) {
    releaseMem(cons->buffer);
    cons->buffer = NULL;
  }

  device->usage -= 1;
  device->driver->usage -= 1;
}

w_driver_status con_read(w_device device, w_ubyte *bytes, w_int length, w_int *lread, x_sleep timeout) {
  *lread = 0;
  return wds_success;
}

w_driver_status con_write(w_device device, w_ubyte *bytes, w_int length, w_int *lwritn, x_sleep timeout) {
  w_control_console cons = device->control;
  int w;
  w_byte *rem_bytes;
  w_size rem_length;
  w_size this_time = 0;

  if (cons->y >= screen->height - CONSOLE_FONT_HEIGHT) {
    console_cls(cons);
  }

  rem_bytes = bytes;
  rem_length = length;
  while (rem_length > 0) {
    this_time = 0;
    while ((w_int)(rem_bytes - bytes + this_time) < length 
           && rem_bytes[this_time] != '\r' 
           && rem_bytes[this_time] != '\n') {
      ++this_time;
    }
    w = Font_getCStringWidth(cons->font, rem_bytes, (int)this_time);
    while (cons->x + w > screen->width) {
      --this_time;
      w = Font_getCStringWidth(cons->font, rem_bytes, (int)this_time);
    }

    Font_drawStringAligned(cons->buffer, cons->font, cons->x, 0, screen->width - cons->x, cons->font->size * 2, cstring2String(rem_bytes, this_time), R_WHITE, (unsigned int)ALIGNMENT_LEFT);

    cons->x += w;
    if ((w_int)(rem_bytes - bytes + this_time) < length) {
      if (rem_bytes[this_time] == '\r') {
        console_cr(cons);
        ++this_time;
      }
      else if (rem_bytes[this_time] == '\n') {
        console_lf(cons);
        ++this_time;
      }
      else {
        console_lf(cons);
      }
    }
    rem_bytes += this_time;
    rem_length -= this_time;
  }

  *lwritn = length;
  return wds_success;
}

w_driver_status con_seek(w_device device, w_int offset, x_sleep timeout) {
  return wds_not_implemented;
}

w_driver_status con_set(w_device device, w_driver_ioctl command, w_word param, x_sleep timeout) {
  return wds_not_implemented;
}

w_driver_status con_query(w_device device, w_driver_ioctl query, w_word *reply, x_sleep timeout) {
  return wds_not_implemented;
}

w_Driver_ByteSerial con_driver = {
  "rudolph-console-driver_v0.1",
  "con",
  0,
  NULL,
  con_initDevice,
  con_termDevice,  
  con_open,
  con_close,  
  con_read,
  con_write,
  con_seek,
  con_set,
  con_query
};

void startConsole(void) {
  registerDevice("con0", "con", 0, wdt_byte_serial);
  registerDriver((w_driver)&con_driver);
}

