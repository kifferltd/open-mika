/**************************************************************************
* Parts copyright (c) 2001, 2003 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2004, 2008 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include <fcntl.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <signal.h>
#include <stdio.h>
#include <unistd.h>
#include "comms.h"
#include "sio.h"
#include "ts-mem.h"
#include "oswald.h"
#include "asyncio.h"
#include "wonka.h"
#include "driver_byteserial.h"

/*
** Currently we have 8 hard-coded comm ports. Should be made smarter.
** For each comm port we have a w_Sio structure, a w_Device,
** and two termios buffers; one to store the state when
** Wonka was started up (so we can restore it on exit),
** the other to store (and manipulate) the current state.
** For each file descriptor we have a w_Wfd structure and
** a w_Device, for each wonkaterm we have a w_Wt structure 
** and a w_Device.
*/

static struct termios sio0_oldtermios;
static struct termios sio0_newtermios;
static struct termios sio1_oldtermios;
static struct termios sio1_newtermios;
static struct termios sio2_oldtermios;
static struct termios sio2_newtermios;
static struct termios sio3_oldtermios;
static struct termios sio3_newtermios;
static struct termios sio4_oldtermios;
static struct termios sio4_newtermios;
static struct termios sio5_oldtermios;
static struct termios sio5_newtermios;
static struct termios sio6_oldtermios;
static struct termios sio6_newtermios;
static struct termios sio7_oldtermios;
static struct termios sio7_newtermios;

#define SIO0_DEFAULT_BITRATE B115200
#define SIO1_DEFAULT_BITRATE B115200
#define SIO2_DEFAULT_BITRATE B115200
#define SIO3_DEFAULT_BITRATE B115200
#define SIO4_DEFAULT_BITRATE B115200
#define SIO5_DEFAULT_BITRATE B115200
#define SIO6_DEFAULT_BITRATE B115200
#define SIO7_DEFAULT_BITRATE B115200

/*
** The pathnames for the eight serial ports
*/
char * pathname[8];

typedef struct w_Control_Sio {
  struct pollfd pollfd;
/*
** clock speed (bits/sec)
*/
  w_int      bitrate;
/*
** Which flags (DTR, RTS, DSR, CTS, CD, RI) are supported by this UART
*/
  w_short   validflags;
/*
** Values of the IOFLAGS for this sio
**   oldflags = already reported via ioevt
**   newflags = latest available values
**   thisflag: only one bit is set, indicating which bit of newflags
**             caused ioevt to return control to its caller last time.
**   thisvalue: either zero or equal to thisflag, depending on whether
**              the value of the flag identified by thisflag was 0 or 1.
*/
  w_short   oldflags;
  w_short   newflags;
  w_short   thisflag;
  w_short   thisvalue;
/*
** Other flags used internally for congestion control etc.
*/
  w_word     miscflags;
/*
** Field to store the result of a 'wait_for_event'
*/
  w_word     event_data;
/*
** Pointers to the termios structure as it was before we grabbed the sio,
** and as it is now
*/
  w_termios  oldtermios;
  w_termios  newtermios;
/*
** The name of the actual device e.g. '/dev/ttyS0'
*/
  char       *devicename;

} w_Control_Sio;

typedef w_Control_Sio *w_control_sio;

/*
** Somewhat ad-hoc function to set the path of an sio device
*/
void sio_set_path(int n, char *s) {
  if (n >= 0 && n < 8) {
    char *buf = allocMem(strlen(s) + 1);
    strcpy(buf, s);
    pathname[n] = buf;
  }
  else {
    woempa(9, "sio devnum %d out of range\n", n);
  }
}

/*
** Prototypes 
*/

w_void sio_initDevice(w_device device); 
w_void sio_termDevice(w_device device); 
w_device sio_open(w_device device, w_driver_perm mode);
w_void sio_close(w_device device);
w_driver_status sio_read(w_device device, w_ubyte *bytes, w_int length, w_int *lread, x_sleep timeout);
w_driver_status sio_write(w_device device, w_ubyte *bytes, w_int length, w_int *lwritn, x_sleep timeout);
w_driver_status sio_seek(w_device device, w_int offset, x_sleep timeout);
w_driver_status sio_set(w_device device, w_driver_ioctl command, w_word param, x_sleep timeout);
w_driver_status sio_query(w_device device, w_driver_ioctl query, w_word *reply, x_sleep timeout);

/*
** sio_init sets the port state to default values, and saves
** the current state so that it can be restored later.
*/

w_void sio_initDevice(w_device device) {
  w_control_sio c;
  device->control = allocClearedMem(sizeof(w_Control_Sio));
  c = device->control;


  c->validflags = IOFLAG_DTR|IOFLAG_RTS|IOFLAG_DSR|IOFLAG_CTS|IOFLAG_CD|IOFLAG_RI;
  switch(device->familyMember) {
    case 0: c->bitrate = SIO0_DEFAULT_BITRATE;
            c->devicename = pathname[0];
            c->oldtermios = &sio0_oldtermios;
            c->newtermios = &sio0_newtermios;
            break;
    case 1: c->bitrate = SIO1_DEFAULT_BITRATE;
            c->devicename = pathname[1];
            c->oldtermios = &sio1_oldtermios;
            c->newtermios = &sio1_newtermios;
            break;
    case 2: c->bitrate = SIO2_DEFAULT_BITRATE;
            c->devicename = pathname[2];
            c->oldtermios = &sio2_oldtermios;
            c->newtermios = &sio2_newtermios;
            break;
    case 3: c->bitrate = SIO3_DEFAULT_BITRATE;
            c->devicename = pathname[3];
            c->oldtermios = &sio3_oldtermios;
            c->newtermios = &sio3_newtermios;
            break;
    case 4: c->bitrate = SIO4_DEFAULT_BITRATE;
            c->devicename = pathname[4];
            c->oldtermios = &sio4_oldtermios;
            c->newtermios = &sio4_newtermios;
            break;
    case 5: c->bitrate = SIO5_DEFAULT_BITRATE;
            c->devicename = pathname[5];
            c->oldtermios = &sio5_oldtermios;
            c->newtermios = &sio5_newtermios;
            break;
    case 6: c->bitrate = SIO6_DEFAULT_BITRATE;
            c->devicename = pathname[6];
            c->oldtermios = &sio6_oldtermios;
            c->newtermios = &sio6_newtermios;
            break;
    case 7: c->bitrate = SIO7_DEFAULT_BITRATE;
            c->devicename = pathname[7];
            c->oldtermios = &sio7_oldtermios;
            c->newtermios = &sio7_newtermios;
            break;
    default:
            break;
  }
            
  // atexit(sio_term);
}

w_void sio_termDevice(w_device device) {
  w_control_sio sio = device->control;
  
  if (sio->pollfd.fd>=0) {
    tcsetattr(sio->pollfd.fd, TCSAFLUSH, sio->oldtermios);
  }
  sio_close(device);

  releaseMem(device->control);
}


w_device sio_open(w_device device, w_driver_perm mode) {
  w_control_sio sio = device->control;

  if(device->usage == 0) {

    sio->miscflags = mode = wdp_read|wdp_write;
  
    sio->pollfd.fd = open(sio->devicename,O_RDWR);
    sio->pollfd.events = POLLIN|POLLOUT;

    if(x_async_register(sio->pollfd.fd)) {
      return NULL;
    }
      
    /*
    ** If a tty, save current (pre-wonka) settings in oldtermios.  (If not a tty,
    ** this call returns nonzero and we skip the rest of the tty-related stuff).
    */
    if (tcgetattr(sio->pollfd.fd,sio->oldtermios)==0) {
      /*
      ** Copy oldtermios to newtermios and make some changes:
      ** echo off, canonical mode off, extended i/p proc off, signal chars off
      ** no SIGINT on BREAK, CR>NL off, i/p parity off, strip8 off, CTS/RTS off
      ** size 8 bits, no parity check, no output processing
      ** read is blocking with no timeout
      */
      w_memcpy(sio->newtermios,sio->oldtermios,sizeof(struct termios));
      sio->newtermios->c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);
      sio->newtermios->c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
      sio->newtermios->c_cflag &= ~(CSIZE | PARENB);
      sio->newtermios->c_cflag |= CREAD | CS8 | CLOCAL;
      sio->newtermios->c_oflag &= ~(OPOST);
      sio->newtermios->c_cc[VMIN] = 1;
      sio->newtermios->c_cc[VTIME] = 0;
      tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
    }

    sio->oldflags = 0;
    sio->newflags = 0;
  }


  device->usage += 1;
  device->driver->usage += 1;
  
  return device;
}


w_void sio_close(w_device device) {
  w_control_sio sio = device->control;

  if(device->usage == 1) {
    if (sio->miscflags & (wdp_read | wdp_write)) {
      sio->miscflags &= ~(wdp_read | wdp_write);
      if (sio->pollfd.fd>=0) {
        x_async_unregister(sio->pollfd.fd);
        close(sio->pollfd.fd);
        sio->pollfd.fd = -1;
      }
    }
  }

  device->usage -= 1;
  device->driver->usage -= 1;
}

w_driver_status sio_read(w_device device, w_ubyte *bytes, w_int length, w_int *lread, x_sleep timeout) {
  w_control_sio sio = device->control;
  w_int result = read(sio->pollfd.fd, bytes, (w_word)length);

  while(result == -1) {
    if(errno != EAGAIN){
      return wds_internal_error;
    }
    x_async_block(sio->pollfd.fd, timeout);
    result = read(sio->pollfd.fd, bytes, (w_word)length);
  }

  *lread = result;

  return wds_success;
  
}

w_driver_status sio_write(w_device device, w_ubyte *bytes, w_int length, w_int *lwritn, x_sleep timeout) {
  w_control_sio sio = device->control;
  w_int result = write(sio->pollfd.fd, bytes, (w_word)length);

  while(result == -1) {
    if(errno != EAGAIN) {
      return wds_internal_error;
    }
    x_async_block(sio->pollfd.fd, timeout);
    result = write(sio->pollfd.fd, bytes, (w_word)length);
  }

  *lwritn = result;

  return wds_success;
}


w_driver_status sio_seek(w_device device, w_int offset, x_sleep timeout) {
  return wds_not_implemented;
}


w_driver_status sio_set(w_device device, w_driver_ioctl command, w_word param, x_sleep timeout) {
  w_control_sio sio = device->control;
  w_word temp;

  switch (command) {
    case(wdi_set_bitrate):
      switch (param) {
        case 75:
          cfsetispeed(sio->newtermios,B75);
          cfsetospeed(sio->newtermios,B75);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 110:
          cfsetispeed(sio->newtermios,B110);
          cfsetospeed(sio->newtermios,B110);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 134:
          cfsetispeed(sio->newtermios,B134);
          cfsetospeed(sio->newtermios,B134);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 150:
          cfsetispeed(sio->newtermios,B150);
          cfsetospeed(sio->newtermios,B150);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 200:
          cfsetispeed(sio->newtermios,B200);
          cfsetospeed(sio->newtermios,B200);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 300:
          cfsetispeed(sio->newtermios,B300);
          cfsetospeed(sio->newtermios,B300);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 600:
          cfsetispeed(sio->newtermios,B600);
          cfsetospeed(sio->newtermios,B600);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 1200:
          cfsetispeed(sio->newtermios,B1200);
          cfsetospeed(sio->newtermios,B1200);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 1800:
          cfsetispeed(sio->newtermios,B1800);
          cfsetospeed(sio->newtermios,B1800);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 2400:
          cfsetispeed(sio->newtermios,B2400);
          cfsetospeed(sio->newtermios,B2400);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 4800:
          cfsetispeed(sio->newtermios,B4800);
          cfsetospeed(sio->newtermios,B4800);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 9600:
          cfsetispeed(sio->newtermios,B9600);
          cfsetospeed(sio->newtermios,B9600);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 19200:
          cfsetispeed(sio->newtermios,B19200);
          cfsetospeed(sio->newtermios,B19200);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 38400:
          cfsetispeed(sio->newtermios,B38400);
          cfsetospeed(sio->newtermios,B38400);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 57600:
          cfsetispeed(sio->newtermios,B57600);
          cfsetospeed(sio->newtermios,B57600);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 115200:
          cfsetispeed(sio->newtermios,B115200);
          cfsetospeed(sio->newtermios,B115200);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        case 230400:
          cfsetispeed(sio->newtermios,B230400);
          cfsetospeed(sio->newtermios,B230400);
          tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);
          break;

        default:
          return wds_illegal_value;
      }
    
      tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);

      return wds_success;

    case(wdi_set_databits):
      switch (param) {
        case 5:
          sio->newtermios->c_cflag &= ~CSIZE;
          sio->newtermios->c_cflag |= CS5;
          break;

        case 6:
          sio->newtermios->c_cflag &= ~CSIZE;
          sio->newtermios->c_cflag |= CS6;
          break;

        case 7:
          sio->newtermios->c_cflag &= ~CSIZE;
          sio->newtermios->c_cflag |= CS7;
          break;

        case 8:
          sio->newtermios->c_cflag &= ~CSIZE;
          sio->newtermios->c_cflag |= CS8;
          break;

        default:
          return wds_illegal_value;
      }
    
      tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);

      return wds_success;

    case(wdi_set_stopbits):
      switch (param) {
        case 1:
          sio->newtermios->c_cflag &= ~CSTOPB;
          break;

        case 2:
          sio->newtermios->c_cflag |= CSTOPB;
          break;

        default:
          return wds_illegal_value;
      }
    
      tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);

      return wds_success;

    case(wdi_set_flowcontrol):
      switch (param) {
// looks like Linux doesn't support separate flow control for i/p & o/p
        case 0:
          sio->newtermios->c_cflag &= ~CRTSCTS;
          break;

        case FLOWCON_CTSRTS_IN:
        case FLOWCON_CTSRTS_OUT:
        case FLOWCON_CTSRTS_IN+FLOWCON_CTSRTS_OUT:
          sio->newtermios->c_cflag |= CRTSCTS;
          break;

        default:
          return wds_illegal_value;
      }
    
      tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);

      return wds_success;

    case(wdi_set_parity):
      switch (param) {
        case PARITY_NONE:
          sio->newtermios->c_cflag &= ~PARENB; // do not generate/check parity
          sio->newtermios->c_iflag &= ~INPCK;  // disable parity checking
          break;
 
        case PARITY_ODD:
          sio->newtermios->c_cflag |= PARENB;  // do generate/check parity
          sio->newtermios->c_cflag |= PARODD;  // set parity odd
          sio->newtermios->c_cflag |= PARMRK;  // report bad parity as \377 \0 X
          sio->newtermios->c_iflag &= ~IGNPAR; // do not ignore parity errors
          sio->newtermios->c_iflag |= INPCK;   // enable parity checking
          break;

        case PARITY_EVEN:
          sio->newtermios->c_cflag |= PARENB;  // do generate/check parity
          sio->newtermios->c_cflag &= ~PARODD; // set parity even
          sio->newtermios->c_cflag |= PARMRK;  // report bad parity as \377 \0 X
          sio->newtermios->c_iflag &= ~IGNPAR; // do not ignore parity errors
          sio->newtermios->c_iflag |= INPCK;   // enable parity checking
          break;

        default:
          return wds_illegal_value;
      }
    
      tcsetattr(sio->pollfd.fd,TCSAFLUSH,sio->newtermios);

      return wds_success;

    case(wdi_set_signals):
      if ((param & ((IOFLAG_DTR|IOFLAG_RTS)|(IOFLAG_DTR|IOFLAG_RTS)<<16))==param) {
        if (param&(IOFLAG_DTR<<16)) {
          ioctl(sio->pollfd.fd,TIOCMGET,&temp);
          if (param&IOFLAG_DTR) {
            temp |= TIOCM_DTR;
          }
          else temp &= ~TIOCM_DTR;
          ioctl(sio->pollfd.fd,TIOCMSET,&temp);
        }

        if (param&(IOFLAG_RTS<<16)) {
          ioctl(sio->pollfd.fd,TIOCMGET,&temp);
          if (param&IOFLAG_RTS) {
            temp |= TIOCM_RTS;
          }
          else temp &= ~TIOCM_RTS;
          ioctl(sio->pollfd.fd,TIOCMSET,&temp);
        }
        return wds_success;
      }
      else {
        return wds_illegal_value;
      }

    case(wdi_send_break):
      tcsendbreak(sio->pollfd.fd, (w_int)param/250);
      return wds_success;

    case(wdi_set_rx_bufsize):
    case(wdi_set_rx_threshold):
    case(wdi_set_rx_timeout):
    case(wdi_set_tx_bufsize):
    case(wdi_set_tx_threshold):
    case(wdi_set_tx_timeout):    
      return wds_success;

    default:
      return wds_no_such_command;
  }
}

static const w_word oldbitrates[] = { 0, 50, 75, 110, 134, 150, 200, 300, 600, 1200, 1800, 2400, 4800, 9600, 19200, 38400 };
static const w_word newbitrates[] = { 57600, 115200, 230400, 460800, 500000, 576000, 921600, -1, 1000000, 1115200, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000 };

w_driver_status sio_query(w_device device, w_driver_ioctl query, w_word *reply, x_sleep timeout) {
  w_control_sio sio = device->control;
  w_word        temp;
  w_short       changedflags;
  w_word        event = 0;

  switch (query) {
    case(wdi_get_bitrate):
      temp = cfgetospeed(sio->newtermios);
      *reply = temp <= 15 ? oldbitrates[temp] : temp > 0010017 ? -1 : temp <= 0010000 ? -1 : newbitrates[temp - 0010000];
      return wds_success;

    case(wdi_get_databits):
      temp = sio->newtermios->c_cflag & CSIZE;
      if (temp==CS5) {
        *reply = 5;
      }
      else if (temp==CS6) {
        *reply = 6;
      }
      else if (temp==CS7) {
        *reply = 7;
      }
      else if (temp==CS8) {
        *reply = 8;
      }
      else {
        return wds_illegal_value;
      }
      return wds_success;

    case(wdi_get_stopbits):
      temp = sio->newtermios->c_cflag & CSTOPB;
      if (temp) {
        *reply = 2;
      }
      else {
        *reply = 1;
      }
      return wds_success;

    case(wdi_get_flowcontrol):
      if (sio->newtermios->c_cflag & CRTSCTS) {
        *reply = FLOWCON_CTSRTS_IN+FLOWCON_CTSRTS_OUT;
      }
      else {
        *reply = FLOWCON_NONE;
      }
      return wds_success;

    case(wdi_get_parity):
      if (!(sio->newtermios->c_cflag&PARENB)) {
        *reply = PARITY_NONE;
      }
      else if (sio->newtermios->c_cflag&PARODD) {
        *reply = PARITY_ODD;
      }
      else *reply = PARITY_EVEN;
      return wds_success;

    case(wdi_get_signals):
      *reply = (sio->oldflags & (IOFLAG_OE|IOFLAG_AVAIL));
      ioctl(sio->pollfd.fd,TIOCMGET,&temp);
      if (temp&TIOCM_DSR) *reply |= IOFLAG_DSR;
      if (temp&TIOCM_CTS) *reply |= IOFLAG_CTS;
      if (temp&TIOCM_CD) *reply |= IOFLAG_CD;
      if (temp&TIOCM_RI) *reply |= IOFLAG_RI;
      return wds_success;

    case(wdi_get_available):
      if (ioctl(sio->pollfd.fd,FIONREAD,reply) == -1) {
        *reply = (w_word)-1;
        return wds_internal_error;
      }
      return wds_success;
      
    case(wdi_wait_for_event):
      if (sio->thisflag) {
        sio->oldflags = (sio->oldflags & ~sio->thisflag) | (sio->thisvalue & sio->thisflag);
        sio->oldflags &= ~IOFLAG_AVAIL;
        sio->thisflag = 0;
        sio->thisvalue = 0;
      }

      {
        w_word avail;
        w_int fd = sio->pollfd.fd;

        if(fd < 1){
          *reply = (w_word)-1;
          return wds_internal_error;
        }

        if (ioctl(fd,FIONREAD,&avail) == -1) {
          *reply = (w_word)-1;
          return wds_internal_error;
        }

        while (!event) {
          int retval;
          /*
          ** Set sio->newflags to reflect the current state.
          */
          sio->newflags = 0;

          x_async_block(fd, timeout);
          //x_thread_sleep(x_millis2ticks(400));



          if(sio->pollfd.fd <= 1){
            return wds_internal_error;
          }

          retval = poll(&sio->pollfd,1,0);


          if(retval == -1){
            *reply = (w_word)-1;
            return wds_internal_error;
          }

          if (sio->pollfd.revents&POLLIN) sio->newflags |= IOFLAG_AVAIL;
          if (sio->pollfd.revents&POLLERR) sio->newflags |= IOFLAG_OE;

          ioctl(fd,TIOCMGET,&temp);

          if (temp&TIOCM_DSR) sio->newflags |= IOFLAG_DSR;
          if (temp&TIOCM_CTS) sio->newflags |= IOFLAG_CTS;
          if (temp&TIOCM_CD)  sio->newflags |= IOFLAG_CD;

          /*
          ** Set changedflags to show the difference between the current state
          ** and the state as last reported to the higher levels.
          */

          changedflags = sio->newflags ^ sio->oldflags;

          /*
          ** For the flags IOFLAG_OE and IOFLAG_AVAIL, only positive-going
          ** transitions are reported to the higher levels: so it we
          ** detect a negative-going transition then we just reset the
          ** corresponding bit in sio->oldflags, to prevent the same
          ** transition from being detected again next time.
          */

          if (changedflags & IOFLAG_OE) {
            if (sio->newflags & IOFLAG_OE) {
              sio->thisflag = IOFLAG_OE;
              sio->thisvalue = IOFLAG_OE;
              event = IOEVT_ERROR_DETECTED;
              sio->event_data = IOFLAG_OE;
            }
            else {
              sio->oldflags &= ~IOFLAG_OE;
            }
          }
          else if (changedflags & IOFLAG_AVAIL) {
            w_word now;
            if (ioctl(fd,FIONREAD,&now) == -1) {
              *reply = (w_word)-1;
              return wds_internal_error;
            }

            if(now < avail){
              avail = now;
            }
            else if ((now > avail) && (sio->newflags & IOFLAG_AVAIL)) {
              sio->thisflag = IOFLAG_AVAIL;
              sio->thisvalue = IOFLAG_AVAIL;
              event = IOEVT_DATA_AVAILABLE;
              sio->event_data = IOFLAG_AVAIL;
            }
            else {
              sio->oldflags &= ~IOFLAG_AVAIL;
            }
          }

          /*
          ** For the modem signals we report all transitions.
          */

          else if (changedflags & (IOFLAG_DSR|IOFLAG_CTS|IOFLAG_CD|IOFLAG_RI)) {
            sio->thisflag = changedflags & (IOFLAG_DSR|IOFLAG_CTS|IOFLAG_CD|IOFLAG_RI);
            sio->thisvalue = sio->newflags & changedflags & (IOFLAG_DSR|IOFLAG_CTS|IOFLAG_CD|IOFLAG_RI);
            event = IOEVT_SIGNALS_CHANGED;
            sio->event_data = sio->newflags | changedflags<<16;
          }

          /*
          ** If we got this without setting `event' then nothing interesting
          ** has happened, so we just go round the loop again ...
          */

          else if (changedflags) {
            sio->oldflags ^= changedflags;
          }
        }
      }

      *reply = event;   
      return wds_success;
      
    case(wdi_get_event_data):
      *reply = sio->event_data;
      return wds_success;

    default:
      return wds_no_such_command;
  }
}


w_Driver_ByteSerial sio_driver = {
  "serial-I/O-driver-linux_v0.1",
  "sio",
  0,
  NULL,
  sio_initDevice,
  sio_termDevice,  
  sio_open,
  sio_close,  
  sio_read,
  sio_write,
  sio_seek,
  sio_set,
  sio_query
};


