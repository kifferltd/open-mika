/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved. Parts copyright (c) 2005, 2006 by Chris Gray, /k/ Embedded    *
* Java Solutions. All rights reserved.                                    *
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <sys/time.h>
#include <sys/kd.h>
#include <unistd.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/fb.h>
#include <sys/mman.h>

#include "ts-mem.h"
#include "rudolph.h"
#include "canvas.h"
#include "Event.h"
#include "pixel.h"

/*
** The file descriptor of the framebuffer device.
*/
static int fbfd = -1;

/*
** The struct which describes the screen as seen by the software.
*/
r_screen screen;

/*
** The beginning of where the framebuffer is mmap'd into memory.
*/
static char *fbp = 0;

/*
** The beginning of the next virtual screen to write to.
*/
static char *fb_page = 0;

/*
** The variable information returned by ioctl(..., FBIOGET_VSCREENINFO, ...).
*/
static struct fb_var_screeninfo vinfo;

/*
** The fixed information returned by ioctl(..., FBIOGET_FSCREENINFO, ...).
*/
static struct fb_fix_screeninfo finfo;

/*
** The number of virtual screens available.
*/
static int fb_cnt = 0;
static int fast_blit = 0;
static long int screensize = 0;
static long int real_screensize = 0;
extern char* awt_args;
extern char* awt_splash;

/*
** awt_zoom is TRUE iff the (deprecated) 'zoom' function was requested in the awt_args.
*/
w_boolean awt_zoom;

/*
** awt_virtual is TRUE iff a virtual screen size was specified in the awt_args.
*/
w_boolean awt_virtual;

#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
/*
** Following variables are only used if awt_virtual is TRUE.
** awt_real_width       : the physical screen width, taken from vinfo.xres.
** awt_real_height      : the physical screen height, taken from vinfo.yres.
** awt_virtual_width    : the virtual screen width, taken from awt_args.
** awt_virtual_height   : the virtual screen height, taken from awt_args.
** awt_virtual_gcd_x    : the greatest common divisor of physical and virtual width.
** awt_virtual_gcd_y    : the greatest common divisor of physical and virtual height.
** awt_virtual_lcm_x    : the lowest common multiple of physical and virtual width.
** awt_virtual_lcm_y    : the lowest common multiple of physical and virttual height.
** real_granularity_x   : the number of times awt_virtual_gcd_x divides into awt_real_width.
** virtual_granularity_x: the number of times awt_virtual_gcd_x divides into awt_virtual_width.
** real_granularity_y   : the number of times awt_virtual_gcd_y divides into awt_real_height.
** virtual_granularity_y: the number of times awt_virtual_gcd_y divides into awt_virtual_height.
**                        Let the screen width be divided into axt_virtual_lcm_x equal parts 
**                        and the screen height be divided into axt_virtual_lcm_y equal parts; 
**                        then we call each resulting subdivision of a pixel a 'grain'.
**                        Thus real_granularity_x horizontal grains will correspond to one real pixel,
**                        virtual_granularity_x horizontal grains will correspond to one virtual pixel,
**                        virtual_granularity_y vertical grains will correspond to one virtual pixel and
**                        real_granularity_y vertical grains will correspond to one real pixel.
** grains_per_real_pixel: the number of grains contained in one real pixel.
*/
w_int awt_real_width;
w_int awt_real_height;
w_int awt_virtual_width;
w_int awt_virtual_height;
static w_int awt_virtual_gcd_x;
static w_int awt_virtual_gcd_y;
static w_int awt_virtual_lcm_x;
static w_int awt_virtual_lcm_y;
static w_int real_granularity_x;
static w_int virtual_granularity_x;
static w_int real_granularity_y;
static w_int virtual_granularity_y;
static w_int grains_per_real_pixel;
#endif

#ifdef HQ2X
void InitLUTs(void);
void hq2x_32(unsigned char * pIn, unsigned char * pOut, int Xres, int Yres, int BpL);
#endif

// TODO: the memcpy looks broken to me, maybe we should call screen_update?
void show_splash(void) {
  int fd, n = 0;
  if (awt_splash) {
    woempa(7, "Opening splash screen file %s\n", awt_splash);
    if ((fd = open(awt_splash, O_RDONLY)) > 0) {
      while((n = read(fd, fbp, 1000000)) > 0);
      memcpy(screen->video, fbp, screensize);
    }
    else {
      woempa(7, "Unable to open splash screen file!\n");
    }
  }
  else {
    woempa(7, "No splash screen specified\n");
  }
}

#ifdef AWT_PIXELFORMAT_c332
/*
 * Set up our 8-bit palette.
 */
#define PALETTE_SIZE 256

static struct fb_cmap *oldCmap = NULL;

static int screen_set_palette(int fd) {
  struct fb_cmap cmap;
  int ret;
  unsigned int i;

  if (oldCmap == NULL) {
    // Save original palette setting.
    oldCmap = allocMem(sizeof(*oldCmap)
			      + 4 * sizeof(*(oldCmap->red)) * PALETTE_SIZE);
    if (oldCmap) {
      oldCmap->start = 0;
      oldCmap->len = PALETTE_SIZE;

      // Our palette buffer starts after the end of the fb_cmap struct.
      oldCmap->red = (void *) (oldCmap + 1);
      oldCmap->green = oldCmap->red + PALETTE_SIZE;
      oldCmap->blue = oldCmap->green + PALETTE_SIZE;
      oldCmap->transp = oldCmap->blue + PALETTE_SIZE;

      if (ioctl(fd, FBIOGETCMAP, oldCmap) < 0) {
        perror("Failed to save system palette: FBIOGETCMAP");
        releaseMem(oldCmap);
        oldCmap = NULL;
      }
    }
  }

  cmap.start = 0;
  cmap.len = PALETTE_SIZE;
  cmap.red = allocMem(3 * sizeof(*(cmap.red)) * PALETTE_SIZE);
  if (cmap.red) {
    cmap.green = cmap.red + PALETTE_SIZE;
    cmap.blue = cmap.green + PALETTE_SIZE;
    cmap.transp = NULL;

    for (i = 0; i < cmap.len; i++) {
      cmap.red[i] = pixel2red(i) * 0xffff / 0x07;
      cmap.green[i] = pixel2green(i) * 0xffff / 0x07;
      cmap.blue[i] = pixel2blue(i) * 0xffff / 0x03;
    }
    ret = ioctl(fd, FBIOPUTCMAP, &cmap);
    releaseMem(cmap.red);
  }
  else {
    ret = -1;
  }

  if (ret < 0) {
    perror("Failed to set palette; colours will be weird: FBIOPUTCMAP");
  }

  return ret;
}

static void screen_restore_palette(int fd) {
  if (oldCmap) {
    if (ioctl(fd, FBIOPUTCMAP, oldCmap) < 0) {
      perror("Failed to restore system palette: FBIOPUTCMAP");
    }
  }
}
#endif

#define CONSOLEDEV "/dev/tty"

static w_int gcd(w_int m, w_int n) {
  w_int n0 = m;
  w_int n1 = n;

  while (TRUE) {
    if (n0 == n1) {

      return n0;

    }
    else if (n0 > n1) {
      n0 -= n1;
    }
    else {
      n1 -= n0;
    }
  }
}


r_screen screen_init(void) {
/* [CG 20051217] This may help on some platforms, but CC9P is not one of them ...
  int consolefd;

  consolefd = open(CONSOLEDEV, O_WRONLY);
  if (consolefd < 0) {
    printf("screen_init(): unable to open " CONSOLEDEV "\n");
  }
  else {
    write(consolefd, "\033[9;0]\033[?33l\033[?25l", 18);
    close(consolefd);
  }
*/

  if(awt_args) {
    w_printf("awt_args: %s\n", awt_args);
    if (strcmp(awt_args, "zoom") == 0) {
      awt_zoom = 1;
      woempa(7, "AWT zoom enabled\n");
      w_printf("AWT zoom enabled\n");
    }
#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
    else if (index(awt_args, 'x')) {
      sscanf(awt_args, "%dx%d", &awt_virtual_width, &awt_virtual_height);
      if (awt_virtual_width <= 0 || awt_virtual_width > 32767 || awt_virtual_height <= 0 || awt_virtual_height > 32767) {
        wabort(ABORT_WONKA, "Invalid virtual screen geometry %dx%d\n", awt_virtual_width, awt_virtual_height);
      }
      woempa(7, "Virtual geometry: %d, %d\n", awt_virtual_width, awt_virtual_height);
      woempa(7, "Virtual width = %d, height = %d\n", awt_virtual_width, awt_virtual_height);
      awt_virtual = TRUE;
    }
#endif
  }
  
  if (fbfd < 0) {

    /* 
    ** Open the file for reading and writing:
    */
    
    fbfd = open("/dev/fb0", O_RDWR);
    
    if (fbfd < 0) {
      wabort(ABORT_WONKA, "cannot open framebuffer device.\n");
    }
    
    woempa(9, "successfully opened framebuffer device ...\n");

    /*
    ** Get fixed screen information:
    */

    if (ioctl(fbfd, FBIOGET_FSCREENINFO, &finfo)) {
      wabort(ABORT_WONKA, "error reading fixed information.\n");
    }
            
    /*
    ** Get variable screen information: 
    */
    
    if (ioctl(fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
      wabort(ABORT_WONKA, "error reading variable information.\n");
    }                          
    
    /* 
    ** Figure out the size of the screen in bytes:
    */
    
    real_screensize = vinfo.xres * vinfo.yres * vinfo.bits_per_pixel / 8;

    if(finfo.line_length == (vinfo.xres * vinfo.bits_per_pixel / 8)) {
      fast_blit = 1;
    }
    
    /* 
    ** Map framebuffer device to memory:
    */
    
    fbp = (char *)mmap(0, finfo.smem_len, PROT_READ | PROT_WRITE, MAP_SHARED, fbfd, 0);

    if ((int)fbp == -1) { 
      wabort(ABORT_WONKA, "failed to map framebuffer device to memory.\n");
    }

    if(finfo.ypanstep == 1) {
      fb_cnt = finfo.smem_len / real_screensize;
      if(fb_cnt < 2) {
        fb_cnt = 1;
        fb_page = fbp;
      }
      else {
        fb_page = fbp + real_screensize;
      }
    }
    else {
      fb_page = fbp;
    }

    ioctl(fbfd, KDSETMODE, KD_GRAPHICS);
    vinfo.xres_virtual = vinfo.xres;
    vinfo.yres_virtual = vinfo.yres;
	  ioctl(fbfd, FBIOPUT_VSCREENINFO, &vinfo);

    woempa(9, "succesfully mapped framebuffer device to memory ...\n");

    woempa(9, "--------------------------------------------------------\n");
    woempa(9, "framebuffer screen specifications:\n");
    woempa(9, "   x resolution: %3d, y resolution: %3d, bits per pixel: %3d\n", vinfo.xres, vinfo.yres, vinfo.bits_per_pixel);
    woempa(9, "   x offset    : %3d, y offset    : %3d, line length   : %3d\n", vinfo.xoffset, vinfo.yoffset, finfo.line_length);
    woempa(9, "   memory      : %p\n", fbp);
    woempa(9, "--------------------------------------------------------\n");

    /*
    ** Initialze screen structure:
    */

    if(awt_zoom) {
      screensize = real_screensize / 4;
#ifdef HQ2X
      InitLUTs();
#endif
    }
#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
    else if (awt_virtual) {
      screensize = pixels2bytes(awt_virtual_width * awt_virtual_height);
    }
#endif
    else {
      screensize = real_screensize;
    }

    screen = allocMem(sizeof(r_Screen));

    if(swap_display) {
      screen->height = vinfo.xres;
      screen->width = vinfo.yres;
    } else {
      screen->height = vinfo.yres;
      screen->width = vinfo.xres;
    }

    if(awt_zoom) {
      screen->height /= 2;
      screen->width  /= 2;
    }
#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
    else if (awt_virtual) {
      if (swap_display) {
        wabort(ABORT_WONKA, "Virtual screen size cannot (yet) be combined with swapping, sorry.");
      }
      screen->height = awt_virtual_height;
      screen->width = awt_virtual_width;
      awt_real_width = vinfo.xres;
      awt_real_height = vinfo.yres;
      awt_virtual_gcd_x = gcd(awt_real_width, awt_virtual_width);
      awt_virtual_lcm_x = awt_real_width * awt_virtual_width / awt_virtual_gcd_x;
      awt_virtual_gcd_y = gcd(awt_real_height, awt_virtual_height);
      awt_virtual_lcm_y = awt_real_height * awt_virtual_height / awt_virtual_gcd_y;
      woempa(7, "Physical width = %d, height = %d\n", screen->width, screen->height);
      woempa(7, "GCD x = %d, y = %d\n", awt_virtual_gcd_x, awt_virtual_gcd_y);
      woempa(7, "LCM x = %d, y = %d\n", awt_virtual_lcm_x, awt_virtual_lcm_y);
      real_granularity_x = awt_virtual_lcm_x / awt_real_width;
      virtual_granularity_x = awt_virtual_lcm_x / awt_virtual_width;
      real_granularity_y = awt_virtual_lcm_y / awt_real_height;
      virtual_granularity_y = awt_virtual_lcm_y / awt_virtual_height;
      woempa(7, "real_granularity_x = %d / %d = %d\n",  awt_virtual_lcm_x, awt_real_width, real_granularity_x);
      woempa(7, "virtual_granularity_x = %d / %d = %d\n",  awt_virtual_lcm_x, awt_virtual_width, virtual_granularity_x);
      woempa(7, "real_granularity_y = %d / %d = %d\n",  awt_virtual_lcm_y, awt_real_height, real_granularity_y);
      woempa(7, "virtual_granularity_y = %d / %d = %d\n",  awt_virtual_lcm_y, awt_virtual_height, virtual_granularity_y);
      grains_per_real_pixel = (awt_virtual_width / awt_virtual_gcd_x) * (awt_virtual_height / awt_virtual_gcd_y);
    }
#endif

    screen->video = allocClearedMem(screensize);

#ifdef AWT_PIXELFORMAT_c332
    // Blank screen before setting palette to prevent flicker
    // FIXME - This is wrong if awt_zoom or awt_virtual is used
    memcpy(fbp, screen->video, screensize);
    
	// proveo: don't overwrite the system palette
    //screen_set_palette(fbfd);
#endif

    show_splash();

    woempa(9, "finished screen_init()\n");
  }
  
  return screen;
}

void screen_shutdown(void) {
  // int consolefd;

  if ((int) fbp > 0) {
    munmap(fbp, (unsigned)(vinfo.yres * finfo.line_length));
    fbp = NULL;
  }
  if (fbfd != -1) {
#ifdef AWT_PIXELFORMAT_c332
    if (oldCmap) {
      screen_restore_palette(fbfd);
      releaseMem(oldCmap);
      oldCmap = NULL;
    }
#endif
    close(fbfd);
    fbfd = -1;
  }

/* [CG 20051217] This may help on some platforms, but CC9P is not one of them ...
  consolefd = open(CONSOLEDEV, O_WRONLY);
  if (consolefd < 0) {
    printf("screen_shutdown(): unable to open " CONSOLEDEV "\n");
  }
  else {
    write(consolefd, "\033[9;15]\033[?33h\033[?25h\033[?0c", 24);
    close(consolefd);
  }
*/
}

static void blitter(w_word * dst, w_word * src, w_size size) {

  w_int num_words = size / sizeof(w_word);
  w_int duffs = (num_words + 63) / 64;

  switch (num_words & 0x3f) {
    default:
    case  0: do { *dst++ = *src++;
    case 63:      *dst++ = *src++;
    case 62:      *dst++ = *src++;
    case 61:      *dst++ = *src++;
    case 60:      *dst++ = *src++;
    case 59:      *dst++ = *src++;
    case 58:      *dst++ = *src++;
    case 57:      *dst++ = *src++;
    case 56:      *dst++ = *src++;
    case 55:      *dst++ = *src++;
    case 54:      *dst++ = *src++;
    case 53:      *dst++ = *src++;
    case 52:      *dst++ = *src++;
    case 51:      *dst++ = *src++;
    case 50:      *dst++ = *src++;
    case 49:      *dst++ = *src++;
    case 48:      *dst++ = *src++;
    case 47:      *dst++ = *src++;
    case 46:      *dst++ = *src++;
    case 45:      *dst++ = *src++;
    case 44:      *dst++ = *src++;
    case 43:      *dst++ = *src++;
    case 42:      *dst++ = *src++;
    case 41:      *dst++ = *src++;
    case 40:      *dst++ = *src++;
    case 39:      *dst++ = *src++;
    case 38:      *dst++ = *src++;
    case 37:      *dst++ = *src++;
    case 36:      *dst++ = *src++;
    case 35:      *dst++ = *src++;
    case 34:      *dst++ = *src++;
    case 33:      *dst++ = *src++;
    case 32:      *dst++ = *src++;
    case 31:      *dst++ = *src++;
    case 30:      *dst++ = *src++;
    case 29:      *dst++ = *src++;
    case 28:      *dst++ = *src++;
    case 27:      *dst++ = *src++;
    case 26:      *dst++ = *src++;
    case 25:      *dst++ = *src++;
    case 24:      *dst++ = *src++;
    case 23:      *dst++ = *src++;
    case 22:      *dst++ = *src++;
    case 21:      *dst++ = *src++;
    case 20:      *dst++ = *src++;
    case 19:      *dst++ = *src++;
    case 18:      *dst++ = *src++;
    case 17:      *dst++ = *src++;
    case 16:      *dst++ = *src++;
    case 15:      *dst++ = *src++;
    case 14:      *dst++ = *src++;
    case 13:      *dst++ = *src++;
    case 12:      *dst++ = *src++;
    case 11:      *dst++ = *src++;
    case 10:      *dst++ = *src++;
    case  9:      *dst++ = *src++;
    case  8:      *dst++ = *src++;
    case  7:      *dst++ = *src++;
    case  6:      *dst++ = *src++;
    case  5:      *dst++ = *src++;
    case  4:      *dst++ = *src++;
    case  3:      *dst++ = *src++;
    case  2:      *dst++ = *src++;
    case  1:      *dst++ = *src++;
            } while (--duffs > 0);
  }

}

int awt_enabled = 1;

/**
 * If there are multiple frame buffers available, flip to the next one (cyclically).
 */
static inline void page_flip(void) {
  if(fb_cnt > 1) {
    vinfo.xoffset = 0;
    if(fbp == fb_page) {
      vinfo.yoffset = 0;
      fb_page = fbp + real_screensize;
    }
    else {
      vinfo.yoffset = vinfo.yres;
      fb_page = fbp;
    }
    ioctl(fbfd, FBIOPAN_DISPLAY, &vinfo);
  }
}

#ifdef HQ2X
/**
 * Swap and double pixels for some forgotten output device.
 */
static void do_awt_zoom_hq2x(void *buffer) {
    hq2x_32((unsigned char *)video, (unsigned char *)buffer, screen->width, screen->height, screen->width * 4 * 2);
   
    for(y = 0; y < screen->height * 2; y++) {
      for(x = 0; x < screen->width * 2; x++) {
        int pixel = buffer[y * screen->width * 2 + x];
        ((short *)buffer)[y * screen->width * 2 + x]  = rgb2pixel((pixel >> 16) & 0xff, (pixel >> 8) & 0xff, pixel & 0xff);
      }
    }
}
#else

/**
 * Implement zooming, i.e. expand every point in the video buffer to fill 4
 * pixels on the screen.
 *
 * In the following diagram coordinates such as 0,0 represent points in the
 * video buffer and the letters a..o represent interpolated points.
 *
 * a  b  c  d  e  f  ...    a = b = 0,0; c = d = 1,0; e = f = 2,0; etc..
 * g 0,0 h 1,0 i 2,0 ...    g = 0,0; h = (0,0+1,0)/2; i = (1,0+2,0)/2; etc..
 * j  k  l  m  n  o  ...    j = k = (0,0+0,1)/2; l = (0,0+0,1+1,0+1,1)/4; m = (1,0+1,1)/2; etc..
 * p 0,1 q 1,1 r 2,1 ...    p = 0,1; q = (0,1+1,1)/2; r = (1,1+2,1)/2; etc..
 * ...
 *
 * The first row and the first column are special in that they are cloned from
 * the second row and second column respectively. The rest form a repeating 
 * pattern:
 *             1 a 2                      a = (1+2)/2
 *             b c d     b = (1+3)/2    c = (1+2+3+4)/4    d = (2+4)/2
 *             3 e 4                      e = (3+4)/2
 * The algorithm scans the video buffer in the conventional order, generating
 * four output pixels each time. After the first column we take advantage of
 * the fact that the new point 1 is the old point 2 and the new point 4 is the
 * old point 3, and avoid re-extracting the three individual colours. This
 * makes the code a bit noisy, for which I apologise.       [CG 20070819]
 */
static void do_awt_zoom(void *buffer) {
    w_ushort *video = (w_ushort *)screen->video;
    w_int x = 0;
    w_int y = 0;
    w_int buffer_offset = 0;
    w_int video_offset = 0;
    w_ushort *pixelbuffer = buffer;
    w_int pixel1;
    w_int pixel2;
    w_int pixel3;
    w_int pixel4;
    w_int red1;
    w_int green1;
    w_int blue1;
    w_int red2;
    w_int green2;
    w_int blue2;
    w_int red3;
    w_int green3;
    w_int blue3;
    w_int red4;
    w_int green4;
    w_int blue4;

    // video buffer 0,0 => output 0,0 0,1 1,0 1,1
//printf("x = 0 y = 0 video_offset = %d (%d, %d) buffer_offset = %d (%d, %d)\n", video_offset, video_offset % screen->width, video_offset / screen->width, buffer_offset, buffer_offset % (screen->width * 2), buffer_offset / (screen->width * 2));
    pixel1 = video[0];
    red1 = pixel2red(pixel1);
    green1 = pixel2green(pixel1);
    blue1 = pixel2blue(pixel1);
    pixelbuffer[buffer_offset] = 
    pixelbuffer[buffer_offset + 1] = 
    pixelbuffer[buffer_offset + screen->width * 2] = 
    pixelbuffer[buffer_offset + screen->width * 2 + 1] = pixel1;
    ++video_offset;
    buffer_offset += 2;

    // remainder of video buffer row 0 => output buffer rows 0, 1
    for (x = 1; x < screen->width; ++x) {
//printf("x = %d y = 0 video_offset = %d (%d, %d) buffer_offset = %d (%d, %d)\n", x, video_offset, video_offset % screen->width, video_offset / screen->width, buffer_offset, buffer_offset % (screen->width * 2), buffer_offset / (screen->width * 2));
      pixel2 = video[video_offset];
      red2 = pixel2red(pixel2);
      green2 = pixel2green(pixel2);
      blue2 = pixel2blue(pixel2);
      pixelbuffer[buffer_offset] = 
      pixelbuffer[buffer_offset + screen->width * 2] = 
        rgb2pixel((red1 + red2) / 2, (green1 + green2) / 2, (blue1 + blue2) / 2);
      pixelbuffer[buffer_offset + 1] = 
      pixelbuffer[buffer_offset + screen->width * 2 + 1] = pixel2;
      ++video_offset;
      buffer_offset += 2;

      // copy pixel2 to pixel1 ready for next column
      pixel1 = pixel2;
      red1 = pixel2red(pixel1);
      green1 = pixel2green(pixel1);
      blue1 = pixel2blue(pixel1);
    }

    // remaining rows
    for (y = 1; y < screen->height; ++y) {
      buffer_offset = screen->width * y * 4;

      // first column (cf. 0,0)
//printf("x = 0 y = %d video_offset = %d (%d, %d) buffer_offset = %d (%d, %d)\n", y, video_offset, video_offset % screen->width, video_offset / screen->width, buffer_offset, buffer_offset % (screen->width * 2), buffer_offset / (screen->width * 2));
      pixel1 = video[video_offset - screen->width];
      red1 = pixel2red(pixel1);
      green1 = pixel2green(pixel1);
      blue1 = pixel2blue(pixel1);
      pixel3 = video[video_offset];
      red3 = pixel2red(pixel3);
      green3 = pixel2green(pixel3);
      blue3 = pixel2blue(pixel3);
      pixelbuffer[buffer_offset] = 
      pixelbuffer[buffer_offset + 1] = 
        rgb2pixel((red1 + red3) / 2, (green1 + green3) / 2, (blue1 + blue3) / 2);
      pixelbuffer[buffer_offset + screen->width * 2] =
      pixelbuffer[buffer_offset + screen->width * 2 + 1] = pixel3;
      ++video_offset;
      buffer_offset += 2;

      // remaining columns
      for (x = 1; x < screen->width; ++x) {
//printf("x = %d y = %d video_offset = %d (%d, %d) buffer_offset = %d (%d, %d)\n", x, y, video_offset, video_offset % screen->width, video_offset / screen->width, buffer_offset, buffer_offset % (screen->width * 2), buffer_offset / (screen->width * 2));
        pixel2 = video[video_offset - screen->width];
        red2 = pixel2red(pixel2);
        green2 = pixel2green(pixel2);
        blue2 = pixel2blue(pixel2);
        pixel4 = video[video_offset];
        red4 = pixel2red(pixel4);
        green4 = pixel2green(pixel4);
        blue4 = pixel2blue(pixel4);
        pixelbuffer[buffer_offset] = rgb2pixel((red1 + red2 + red3 + red4) / 4, (green1 + green2 + green3 + green4) / 4, (blue1 + blue2 + blue3 + blue4) / 4);
        pixelbuffer[buffer_offset + 1] = rgb2pixel((red2 + red4) / 2, (green2 + green4) / 2, (blue2 + blue4) / 2);
        pixelbuffer[buffer_offset + screen->width * 2] = rgb2pixel((red3 + red4) / 2, (green3 + green4) / 2, (blue3 + blue4) / 2);
        pixelbuffer[buffer_offset + screen->width * 2 + 1] = pixel4;
        ++video_offset;
        buffer_offset += 2;

      // copy pixel2 to pixel1, pixel4 to pixel3 ready for next column
        pixel1 = pixel2;
        red1 = red2;
        green1 = green2;
        blue1 = blue2;
        pixel3 = pixel4;
        red3 = red4;
        green3 = green4;
        blue3 = blue4;
      }
    }
}
#endif

#ifdef AWT_VIRTUAL_SCREEN_SUPPORT

/*
** Copy one line of pixels to the output buffer. Each pixel in linebuffer 
** consists of three w_int's (r, g, b), scaled up by grains_per_real_pixel.
** These pixels are packed into the dest buffer in screen format, e.g. c565
** or pp888.
*/ 
static void pack_linebuffer(char *dest, w_int *linebuffer, w_int linelength) {
  w_int i;
  r_pixel p;
  w_int psize = pixels2bytes(1);
  w_int *inptr = linebuffer;
  char *outptr = dest;

  woempa(7, "dest = %p, linebuffer = %p, linelength = %d\n", dest, linebuffer, linelength);
  for (i = 0; i < linelength; ++i) {
    p = rgb2pixel(inptr[0] / grains_per_real_pixel, inptr[1] / grains_per_real_pixel, inptr[2] / grains_per_real_pixel); 
    inptr += 3;
    memcpy(outptr, &p, psize);
    outptr += psize;
  }
  woempa(7, "on exit outptr = %p (offset %d), inptr = %p (offset %d) \n", outptr, outptr - dest, inptr, inptr - linebuffer);
}

/*
** I hope you have as much fun reading this as I did coding it. :-)
** [CG 20060514]
**
** We divide the screen rectangle into awt_virtual_lcm_x * awt_virtual_lcm_y
** 'grains'; each 'real' or 'virtual' pixel consists of a number of these
** grains. For example, suppose the real screen size is 320x240 and we wish
** to simulate a virtual screen 800x480; then we divide the screen into
** 1600 x 480 grains, and each real pixel is 5x2 grains and each virtual
** pixel is 2x1. In principle we just scan across the grains, reading the
** value of the corresponding virtual pixel and adding it into the corresponding
** virtual pixel with a weight of 1/grains_per_real_pixel (in our example,
** 1/10).
**
** Now for the refinements:
** 1. To avoid excessive quantisation, we only apply the division by 
**    grains_per_real_pixel after all grains have been accumulated, i.e. in
**    pack_linebuffer().
** 2. We avoid repeatedly unpacking the same input pixel.
** 3. If several grains horizontally or vertically correspond to both the same 
**    the same real pixel and the same virtual pixel then we lump them
**    together using fractional_x/y_increment.
*/
static void update_virtual_buffer(char *buffer, char *video, int x1, int y1, int x2, int y2) {
  w_int source_red = 0;;
  w_int source_green = 0;;
  w_int source_blue = 0;;
  r_pixel source_pixel;
  w_int fractional_x;
  w_int fractional_y;
  w_int real_x = 0;
  w_int real_y = 0;
  w_int virtual_x = 0;
  w_int virtual_y = 0;
  w_int fractional_x_next_real;
  w_int fractional_x_next_virtual;
  w_int fractional_x_increment;
  w_int fractional_y_next_real;
  w_int fractional_y_next_virtual;
  w_int fractional_y_increment;
  w_int pixel_size = pixels2bytes(1);
  char *dest = buffer;
  char *source = video;
  static w_int *linebuffer;
  w_int *mix;

  if (!linebuffer) {
    linebuffer = allocClearedMem(awt_real_width * sizeof(w_word) * 3);
  }
  mix = linebuffer;

  woempa(7, "fractional_x = 0, fractional_y = 0\n");

 fractional_y_next_real = real_granularity_y;
 fractional_y_next_virtual = virtual_granularity_y;
 fractional_y_increment = virtual_granularity_y < real_granularity_y ? virtual_granularity_y : real_granularity_y;

  for (fractional_y = 0; fractional_y < awt_virtual_lcm_y;) {
    if ((fractional_y % virtual_granularity_y) == 0)  {
      memcpy(&source_pixel, source, pixel_size);
      source_red = pixel2red(source_pixel);
      source_green = pixel2green(source_pixel);
      source_blue = pixel2blue(source_pixel);
      woempa(1, "source_pixel = %08x, rgb = (%d, %d, %d)\n", source_pixel, source_red, source_green, source_blue);
    }

    fractional_x_next_real = real_granularity_x;
    fractional_x_next_virtual = virtual_granularity_x;
    fractional_x_increment = virtual_granularity_x < real_granularity_x ? virtual_granularity_x : real_granularity_x;
    for (fractional_x = 0; fractional_x < awt_virtual_lcm_x;) {
      mix[0] += source_red * fractional_x_increment * fractional_y_increment;
      mix[1] += source_green * fractional_x_increment * fractional_y_increment;
      mix[2] += source_blue * fractional_x_increment * fractional_y_increment;
      woempa(1, "linebuffer[%d] = (%d, %d, %d)\n", (mix - linebuffer) / 3, mix[0], mix [1], mix[2]);

      fractional_x += fractional_x_increment;
      // these comparisons should be ==, we're just being cautious
      if (fractional_x >= fractional_x_next_virtual)  {
      woempa(1, "fractional_x = %d, fractional_y = %d\n", fractional_x, fractional_y);
        virtual_x = fractional_x / virtual_granularity_x;
        fractional_x_next_virtual += virtual_granularity_x;
        woempa(1, "Advancing to next virtual pixel %d\n", virtual_x);
        source += pixel_size;
        woempa(1, "source = %p, mix = linebuffer + %d, dest = %p\n", source, mix - linebuffer, dest);
        memcpy(&source_pixel, source, pixel_size);
        source_red = pixel2red(source_pixel);
        source_green = pixel2green(source_pixel);
        source_blue = pixel2blue(source_pixel);
        woempa(1, "source_pixel = %08x, rgb = (%d, %d, %d)\n", source_pixel, source_red, source_green, source_blue);
      }
      if (fractional_x >= fractional_x_next_real)  {
        real_x = fractional_x / real_granularity_x;
        fractional_x_next_real += real_granularity_x;
        woempa(1, "Advancing to next real pixel %d\n", real_x);
        woempa(1, "source = %p, mix = linebuffer + %d, dest = %p\n", source, mix - linebuffer, dest);
        mix += 3;
      }
      fractional_x_increment = (fractional_x_next_real < fractional_x_next_virtual ?  fractional_x_next_real : fractional_x_next_virtual) - fractional_x; 
    }

    fractional_y += fractional_y_increment;
    woempa(1, "fractional_x = %d, fractional_y = %d\n", fractional_x, fractional_y);
    if (fractional_y >= fractional_y_next_real)  {
      real_y = fractional_y / real_granularity_y;
      woempa(7, "Advancing to next real line %d\n", real_y);
      pack_linebuffer(dest, linebuffer, awt_real_width);
      dest += awt_real_width * pixel_size;
      memset(linebuffer, 0, awt_real_width * sizeof(w_word) * 3);
      woempa(7, "source = %p, mix = linebuffer + %d, dest = %p\n", source, mix - linebuffer, dest);
      fractional_y_next_real += real_granularity_y;
    }
    if (fractional_y >= fractional_y_next_virtual)  {
      virtual_y = fractional_y / virtual_granularity_y;
      woempa(7, "Advancing to next virtual line %d\n");
      woempa(7, "source = %p, mix = linebuffer + %d, dest = %p\n", source, mix - linebuffer, dest);
      source = video + virtual_y * awt_virtual_width * pixel_size;
      fractional_y_next_virtual += virtual_granularity_y;
    }

    fractional_y_increment = (fractional_y_next_real < fractional_y_next_virtual ?  fractional_y_next_real : fractional_y_next_virtual) - fractional_y; 
    mix = linebuffer;
    real_x = virtual_x = 0;
  }
}
#endif

void screen_update(int x1, int y1, int x2, int y2) {

  w_ubyte       *fb = fb_page;
  w_int         x, y, size;
  w_ubyte       *scr;
  static w_word *buffer;
  w_ushort      *video = (w_ushort *)screen->video;

  if(awt_zoom) {
    if(!buffer) {
      buffer = allocMem(screensize * 4 * 4);
      if (!buffer) {

        return;

      }
    }
  }
  else if (awt_virtual) {
    if(!buffer) {
      buffer = allocMem(real_screensize);
      if (!buffer) {

        return;

      }
    }
  }
  else {
    buffer = (w_word *)screen->video;
  }

  if(awt_zoom) {
#ifdef HQ2X
    do_awt_zoom_hq2x(buffer);
#else
    do_awt_zoom(buffer);
#endif
  }
#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
  else if (awt_virtual) {
    update_virtual_buffer((char *)buffer, (char *)video, x1, y1, x2, y2);
  }
#endif
  
  if (fast_blit) {
    blitter((w_word*)fb, (w_word*)buffer, real_screensize);
  }
  else {
    size = vinfo.xres * vinfo.bits_per_pixel / 8;
    scr = (w_ubyte *)buffer;
    
    for (y = 0; y < (w_int)vinfo.yres; y++) {
      memcpy(fb, scr, size);
      fb += finfo.line_length;
      scr += size;
    }
  }

  page_flip();
}


