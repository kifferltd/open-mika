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

/* $Id: ps2mouse.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include <fcntl.h>
#include <sys/ioctl.h>

#ifdef STANDALONE

typedef int w_int;
typedef unsigned int w_word;
typedef char w_byte;
typedef unsigned char w_ubyte;
typedef void w_void;

typedef struct r_Screen {
  w_int width;
  w_int height;
} r_Screen;

typedef r_Screen  *r_screen;

r_Screen Screen = {640, 480};
r_screen screen = &Screen;
#define R_EVENT_MOUSE_DRAGGED 1
#define R_EVENT_MOUSE_PRESSED 2
#define R_EVENT_MOUSE_RELEASED 3
// #define woempa(a, b...) printf(##b)
#define woempa(a, b...) 
#define wabort(a, b...) printf(##b)
#define allocMem(a) malloc(a)

#else 

#include "wonka.h"
#include "ts-mem.h"
#include "rudolph.h"
#include "canvas.h"
#include "Event.h"

#endif

static w_int mfd = 0;
static w_int ps2_packet_length = 3;

static w_int ps2_pos_x = 0;
static w_int ps2_pos_y = 0;
static w_int ps2_state = 0;

static w_int ps2_pos_x_old = 0;
static w_int ps2_pos_y_old = 0;
static w_int ps2_state_old = 0;

static w_ubyte* mouse_path;

static w_void ps2_flush(w_void) {
  w_ubyte c;
  w_int result;
  do {
    result = read(mfd, &c, 1);
  } while(errno != EAGAIN && errno != EINTR && result != 0);
}

w_int mouse_poll(w_int *state, w_int *x, w_int *y) {
  static w_ubyte  pos = 0;
  static w_ubyte  buf[256];
  static w_ubyte  packet[4];
  w_ubyte  count = 0;
  w_int    len = 0;
  w_int    i;
  w_int    dx, dy;
  w_int    buttons;

  while (len <= 0) {
    count++;
    if(count > 10) {

      /*
      ** Could not read anything in time...
      */

      return 0;
    }
    len = read(mfd, buf, 128);
  }

  printf("len: %d (%d)\n", len, ps2_packet_length);

  for(i = 0; i < len; i++) {

    if (!(pos == 0  && (buf[i] & 0xC0))) {

      packet[pos++] = buf[i];

      if (pos == ps2_packet_length) {

        pos = 0;

        if (!(packet[0] & 0x08)) {
          woempa(9, "Lost PS/2 sync...\n");
          i++;
        }
        else {

          buttons = packet[0] & 0x07;

          dx = (packet[0] & 0x10) ? packet[1] - 256 : packet[1];
          dy = (packet[0] & 0x20) ? packet[2] - 256 : packet[2];

          ps2_pos_x += dx;
          ps2_pos_y -= dy;

          if(ps2_pos_x < 0) ps2_pos_x = 0;
          if(ps2_pos_y < 0) ps2_pos_y = 0;
          if(ps2_pos_x > screen->width) ps2_pos_x = screen->width;
          if(ps2_pos_y > screen->height) ps2_pos_y = screen->height;

          woempa(9, "buttons: %x, dx: %d, dy: %d, x: %d, y: %d\n", buttons, dx, dy, ps2_pos_x, ps2_pos_y);

          *x = ps2_pos_x;
          *y = ps2_pos_y;
          *state = 0;

          if((buttons & 0x01) == 1 && ps2_state == 0) {
            *state = R_EVENT_MOUSE_PRESSED;
          }
          else if((buttons & 0x01) == 0 && ps2_state == 1) {
            *state = R_EVENT_MOUSE_RELEASED;
          }
          else if((buttons & 0x01) == 1 && ps2_state == 1) {
            *state = R_EVENT_MOUSE_DRAGGED;
          }

          ps2_state = (buttons & 0x01);
        }
      }
    }
  }

  /*
  ** TODO: This is not quite right yet...
  */

  if(ps2_pos_x != ps2_pos_x_old || 
      ps2_pos_y != ps2_pos_y_old ||
      ps2_state != ps2_state_old) {

//    *x = ps2_pos_x_old = ps2_pos_x; 
//    *y = ps2_pos_y_old = ps2_pos_y;
//    *state = ps2_state_old = ps2_state;
  }

  return 1;
}

static w_int ps2_write(w_ubyte *data, w_int len) {
  w_int i;
  w_int result = 0;
  w_int r = 0;

  ps2_flush();

  for (i = 0; i < len; i++) {
    w_ubyte c = 0;

    write(mfd, &data[i], 1);
    result = 0;
    while(result != 1) {
      result = read(mfd, &c, 1);
    }

    if (c != 0xFA) {
      woempa(9, "PS/2: Missed ack...\n");
      r = 1;
    }
  }

  ps2_flush();

  return r;
}

static w_int ps2_read_id(w_void) {
  w_ubyte c = 0xF2;
  w_int   result;

  ps2_flush();

  write(mfd, &c, 1);

  result = 0;
  while(result != 1) {
    result = read(mfd, &c, 1);
  }

  if (c != 0xFA) {
    woempa(9, "PS2: No ack when requesting ID... %x\n", c);
    return -1;
  }

  result = 0;
  while(result != 1) {
    result = read(mfd, &c, 1);
  }

    woempa(9, "ID : %x\n", c);
  ps2_flush();

  return c;
}

w_void mouse_close(w_void) {
  close(mfd);
}

w_void mouse_set_path(char *s) {
  w_ubyte *buf = allocMem(strlen(s) + 1);
  strcpy(buf, s);
  mouse_path = buf;
}

w_void mouse_init(w_void) {
  w_ubyte basic_init[] = {0xF4, 0xF3, 100};
  w_ubyte imps2_init[] = {0xF3, 200, 0xF3, 100, 0xF3, 80};
  w_ubyte ps2_init[] = {0xE6, 0xF4, 0xF3, 100, 0xE8, 3};
  w_int mouse_id;

  woempa(9, "called mouse_init()\n");
    
  mfd = open("/dev/psaux", O_RDWR | O_SYNC | O_NONBLOCK);
  if (mfd == -1) {
    wabort(ABORT_WONKA, "error: Failed to initialize PS/2 mouse\n");
  }
                                       
  if (ps2_write(basic_init, sizeof(basic_init)) != 0) {
    wabort(ABORT_WONKA, "error: Failed to initialize PS/2 mouse\n");
  }

  mouse_id = ps2_read_id();

  if (mouse_id != 3) mouse_id = 0;

  ps2_write(ps2_init, sizeof(ps2_init));

  if (mouse_id == 3) {
    if (ps2_write(imps2_init, sizeof(imps2_init)) != 0) {
      wabort(ABORT_WONKA, "error: Failed to initialize IMPS/2 mouse\n");
    }
    ps2_packet_length = 4;
  }

  woempa(9, "Succesfully initialized PS/2 mouse\n");
}

extern w_void draw_cursor(w_int, w_int, r_pixel*);

void mouse_flush(void) {
  draw_cursor(ps2_pos_x, ps2_pos_y, screen->video);
}

#ifdef STANDALONE

void main() {
  w_int x, y, s;
  mouse_init();
  while(1) {
    if(mouse_poll(&s, &x, &y) != 0) {
      printf("buttons: %d, x: %d, y: %d\n", s, x, y);
    }
    usleep(200);
  }
}

#endif
