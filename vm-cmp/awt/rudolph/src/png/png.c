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
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

/*
** $Id: png.c,v 1.2 2005/06/14 09:46:04 cvs Exp $
*/

#include <string.h>

#include "ts-mem.h"
#include "threads.h"
#include "wonka.h"
#include "deflate_driver.h"
#include "wpng.h"

// TODO: errorhandling is pretty rudimentary now, we should return NULL pointer instead, provide errno, and a full string message.
// TODO: gAMA and tRNS should also be implemented once alpha channel is used properly in Wonka

w_ubyte header[8] = { 137, 80, 78, 71, 13, 10, 26, 10 };
w_ubyte ihdr[4] = { 'I', 'H', 'D', 'R'};
w_ubyte plte[4] = { 'P', 'L', 'T', 'E'};
w_ubyte idat[4] = { 'I', 'D', 'A', 'T'};
w_ubyte iend[4] = { 'I', 'E', 'N', 'D'};

// TODO: throw this out ?? used to calculate PLTE size
//unsigned int pow2[16] = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 
//                          2048, 4096, 8192, 16384, 32768};

// TODO : should this be static ??
w_word pal[256];

// these functions accept signed numbers, but DONT work correctly on them !!
// calculate ceil(c / 8)
/* omitting 'inline' as gcc (5.2.1) is choking on it */
w_int ceil_div8(w_int c) {
  if ((c & 0x7) == 0) return c >> 3;
  else return (c >> 3) + 1;
}

// calculate ceil(c / 4)
/* omitting 'inline' as gcc (5.2.1) is choking on it */
w_int ceil_div4(w_int c) {
  if ((c & 0x3) == 0) return c >> 2;
  else return (c >> 2) + 1;
}

// calculate ceil(c / 2)
/* omitting 'inline' as gcc (5.2.1) is choking on it */
w_int ceil_div2(w_int c) {
  if ((c & 0x1) == 0) return c >> 1;
  else return (c >> 1) + 1;
}

// calculate ceil(c / 1) (trivial case)
/* omitting 'inline' as gcc (5.2.1) is choking on it */
w_int ceil_div1(w_int c) {
  return c;
}

// calculate bytes per pixel
w_int bpp(w_int bit_depth, w_int color_type) {
  switch (color_type) {
    case 0:                                     // GRAYSCALE
      return ceil_div8(bit_depth);
    case 2:                                     // RGB
      return 3 * bit_depth / 8;
    case 3:                                     // PLTE
      return ceil_div8(bit_depth);
    case 4:                                     // GRAYSCALE + ALPHA
      return 2 * bit_depth / 8;
    case 6:                                     // RGB + ALPHA
      return 4 * bit_depth / 8;
    default:
      return 0;
  }
}

// calculate bytes per scanline
w_int bps(w_int bit_depth, w_int color_type, w_int width) {
  switch (color_type) {
    case 0:                                     // GRAYSCALE
      return ceil_div8(width * bit_depth);
    case 2:                                     // RGB
      return width * 3 * bit_depth / 8;
    case 3:                                     // PLTE
      return ceil_div8(width * bit_depth);
    case 4:                                     // GRAYSCALE + ALPHA
      return width * 2 * bit_depth / 8;
    case 6:                                     // RGB + ALPHA
      return width * 4 * bit_depth / 8;
    default:
      return 0;
  }
}

w_int PaethPredictor(w_int a, w_int b, w_int c) {
  w_int p, pa, pb, pc;

  // a = left, b = above, c = upper left
  p = a + b - c;
  pa = abs(p - a);
  pb = abs(p - b);
  pc = abs(p - c);
  // return nearest of a, b, c
  // breaking ties in order a, b, c
  if ((pa <= pb) && (pa <= pc)) return a;
  else if (pb <= pc) return b;
  else return c;
}

// first byte has to be initialised to 0x80 and has to be in front of the scanline
// this is the grayscale version, converts to 8 bits
// im is pointer to filtered scanline - 1
// w is the byte counter in the this scanline
w_ubyte getNextUnshifted(w_ubyte *im, w_int *w, w_int bit_depth) {
  w_int ret;
  
  switch (bit_depth) {
    case 1:
      ret = im[*w] & 0x80;
      im[*w] = im[*w] << 1;
      if (im[*w] == 0) {
        *w = *w + 1;
        ret = im[*w] & 0x80;
        im[*w] = (im[*w] << 1) | 0x1;
      }
      ret = (ret << 8) - ret;
      ret = ret >> 7;
      break;
    case 2:
      ret = im[*w] & 0xc0;
      im[*w] = im[*w] << 2;
      if (im[*w] == 0) {
        *w = *w + 1;
        ret = im[*w] & 0xc0;
        im[*w] = (im[*w] << 2) | 0x1;
      }
      ret = (ret << 8) - ret;
      ret = ret / 0xc0;
      break;
    case 4:
      ret = im[*w] & 0xf0;
      im[*w] = im[*w] << 4;
      if (im[*w] == 0) {
        *w = *w + 1;
        ret = im[*w] & 0xf0;
        im[*w] = (im[*w] << 4) | 0x1;
      }
      ret = (ret << 8) - ret;
      ret = ret / 0xf0;
      break;
    case 8:
      *w = *w + 1;
      ret = im[*w];
      break;
    case 16:
      *w = *w + 1;
      ret = im[*w];
      *w = *w + 1;
      break;
    // I already checked this, but IF due to what reason soever this should happen we have to inc w, or everything crashes !!
    default:
      *w = *w + 1;
      ret = 0;
      break;
  }
  return (w_ubyte)ret;
}

// first byte has to be initialised to 0x80 and has to be in front of the scanline
// this is the normal version, only converts 16 to 8 bits
// im is pointer to filtered scanline - 1
// w is the byte counter in the this scanline
w_ubyte getNext(w_ubyte *im, w_int *w, w_int bit_depth) {
  w_ubyte ret;
  
  switch (bit_depth) {
    case 1:
      ret = (im[*w] & 0x80) >> 7;
      im[*w] = im[*w] << 1;
      if (im[*w] == 0) {
        *w = *w + 1;
        ret = (im[*w] & 0x80) >> 7;
        im[*w] = (im[*w] << 1) | 0x1;
      }
      break;
    case 2:
      ret = (im[*w] & 0xc0) >> 6;
      im[*w] = im[*w] << 2;
      if (im[*w] == 0) {
        *w = *w + 1;
        ret = (im[*w] & 0xc0) >> 6;
        im[*w] = (im[*w] << 2) | 0x1;
      }
      break;
    case 4:
      ret = (im[*w] & 0xf0) >> 4;
      im[*w] = im[*w] << 4;
      if (im[*w] == 0) {
        *w = *w + 1;
        ret = (im[*w] & 0xf0) >> 4;
        im[*w] = (im[*w] << 4) | 0x1;
      }
      break;
    case 8:
      *w = *w + 1;
      ret = im[*w];
      break;
    case 16:
      *w = *w + 1;
      ret = im[*w];
      *w = *w + 1;
      break;
    // I already checked this, but IF due to what reason soever this should happen we have to inc w, or everything crashes !!
    default:
      *w = *w + 1;
      ret = 0;
      break;
  }
  return ret;
}

// filter one scanline
// firstline is 1 for the first line of an image, else 0
// fimage is a pointer to the scanline - 1 (filter type)
w_void filter_image(w_int firstline, w_int bpp_const, w_int bps_const, w_ubyte *fimage) {
  w_int j;
  
  switch (fimage[0]) {
    case 0:                     // None
      break;
    case 1:                     // Sub
      for (j = 1; j < bps_const ; j++) {
        if ((j - bpp_const) >= 1) fimage[j] = fimage[j] + fimage[j - bpp_const];
      }
      break;
    case 2:                     // Up
     for (j = 1; j < bps_const; j++) {
        if (!firstline) fimage[j] = fimage[j] + fimage[j - bps_const];
      }
      break;
    case 3:                     // Average
      for (j = 1; j < bps_const; j++) {
        if (firstline && (j - bpp_const < 1)) fimage[j] = fimage[j];
        else if (firstline) fimage[j] = fimage[j] + fimage[j - bpp_const] / 2;
        else if (j - bpp_const < 1) fimage[j] = fimage[j] + fimage[j - bps_const] / 2;
        else fimage[j] = fimage[j] + (fimage[j - bpp_const] + fimage[j - bps_const]) / 2;
      }
      break;
    case 4:                     // Paeth
      for (j = 1; j < bps_const; j++) {
        if (firstline && (j - bpp_const < 1)) fimage[j] = fimage[j];
        else if (firstline) fimage[j] = fimage[j] + PaethPredictor(fimage[j - bpp_const], 0, 0);
        else if (j - bpp_const < 1) fimage[j] = fimage[j] + PaethPredictor(0, fimage[j - bps_const], 0);
        else fimage[j] = fimage[j] + PaethPredictor(fimage[j - bpp_const], fimage[j - bps_const], fimage[j - bps_const - bpp_const]);
      }
      break;
    default:
      // TODO: we should stop processing here
      woempa(9,"  -ERROR - Unknowen filter algorithm !!!\n");
      break;
  }     
}

// process scanlines, do the filtering and the bitrate conversion
w_void process_scanlines(w_int width, w_int height, w_int bpp_const, w_int bps_const, w_int bit_depth, w_int color_type, w_ubyte *fimage, w_ubyte *image) {
  w_word *limage = (w_word *)image;
  w_int i, j, j2;

  for (i = 0; i < height; i++) {
    woempa(5, "  -scanline %i has filter type %i\n", i, fimage[i * bps_const]);

    // filter the decompressed scanline
    filter_image((i == 0), bpp_const, bps_const, fimage + i * bps_const);

    // initialise for getNext
    fimage[i * bps_const] = 0x80;
    j = 0;
    switch (color_type) {
      case 0:                                     // GRAYSCALE
        for (j2 = 0; j2 < width; j2++) {
          image[i * 4 * width + j2 * 4] = image[i * 4 * width + j2 * 4 + 1] = image[i * 4 * width + j2 * 4 + 2] = getNextUnshifted(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 3] = 255;
        }
        break;
      case 2:                                     // RGB
        for (j2 = 0; j2 < width; j2++) {
          image[i * 4 * width + j2 * 4] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 1] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 2] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 3] = 255;
        }
        break;
      case 3:                                     // PLTE
        for (j2 = 0; j2 < width; j2++) {
          limage[i * width + j2] = pal[getNext(fimage + i * bps_const, &j, bit_depth)];
        }
        break;
      case 4:                                     // GRAYSCALE + ALPHA
        for (j2 = 0; j2 < width; j2++) {
          image[i * 4 * width + j2 * 4 ] = image[i * 4 * width + j2 * 4 + 1] = image[i * 4 * width + j2 * 4 + 2] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 3] = getNext(fimage + i * bps_const, &j, bit_depth);
        }
        break;
      case 6:                                     // RGB + ALPHA
        for (j2 = 0; j2 < width; j2++) {
          image[i * 4 * width + j2 * 4 ] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 1] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 2] = getNext(fimage + i * bps_const, &j, bit_depth);
          image[i * 4 * width + j2 * 4 + 3] = getNext(fimage + i * bps_const, &j, bit_depth);
        }
        break;
    }
  }
}

w_ubyte interlace_row_offs[7] = { 0, 0, 4, 0, 2, 0, 1 };
w_ubyte interlace_row_inc[7] = { 8, 8, 8, 4, 4, 2, 2 };
w_ubyte interlace_col_offs[7] = { 0, 4, 0, 2, 0, 1, 0 };
w_ubyte interlace_col_inc[7] = { 8, 8, 4, 4, 2, 2, 1 };

typedef w_int (*interlace_div)(w_int);
/* This is presumably what gcc (5.2.1) is choking on if the functions are declared as inline. */
/* Frankly I can't blame gcc from being grumpy about this ... */
interlace_div interlace_width_div[7] = { ceil_div8, ceil_div8, ceil_div4, ceil_div4, ceil_div2, ceil_div2, ceil_div1 };

// process interlaced scanlines, do the filtering and the bitrate conversion
w_void process_interlaced_scanlines(w_int width, w_int height, w_int bpp_const, w_int bps_const, w_int bit_depth, w_int color_type, w_ubyte *fimage, w_ubyte *image) {
  w_word *limage = (w_word *)image;
  w_int i, j, j2, offset, l;
  
  offset = 0;
  for (l = 0; l < 7; l++) {
    woempa(5, "  -PASS %i\n", l + 1);
    
    for (i = interlace_row_offs[l]; i < height; i += interlace_row_inc[l]) {

      woempa(5, "  -scanline %i has filter type %i\n", i, fimage[offset]);

      // really small images don't have all passes
      if (width - interlace_col_offs[l] > 0) {

        // filter the decompressed scanline
        filter_image((i == interlace_row_offs[l]), bpp_const, bps(bit_depth, color_type, interlace_width_div[l](width - interlace_col_offs[l])) + 1, fimage + offset);

        // initialise for getNext
        fimage[offset] = 0x80;
        j = 0;
        switch (color_type) {
          case 0:                                     // GRAYSCALE
            for (j2 = interlace_col_offs[l]; j2 < width; j2 += interlace_col_inc[l]) {
              image[i * 4 * width + j2 * 4] = image[i * 4 * width + j2 * 4 + 1] = image[i * 4 * width + j2 * 4 + 2] = getNextUnshifted(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 3] = 255;
            }
            break;
          case 2:                                     // RGB
            for (j2 = interlace_col_offs[l]; j2 < width; j2 += interlace_col_inc[l]) {
              image[i * 4 * width + j2 * 4] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 1] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 2] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 3] = 255;
            }
            break;
          case 3:                                     // PLTE
            for (j2 = interlace_col_offs[l]; j2 < width; j2 += interlace_col_inc[l]) {
              limage[i * width + j2] = pal[getNext(fimage + offset, &j, bit_depth)];
            }
            break;
          case 4:                                     // GRAYSCALE + ALPHA
            for (j2 = interlace_col_offs[l]; j2 < width; j2 += interlace_col_inc[l]) {
              image[i * 4 * width + j2 * 4 ] = image[i * 4 * width + j2 * 4 + 1] = image[i * 4 * width + j2 * 4 + 2] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 3] = getNext(fimage + offset, &j, bit_depth);
            }
            break;
          case 6:                                     // RGB + ALPHA
            for (j2 = interlace_col_offs[l]; j2 < width; j2 += interlace_col_inc[l]) {
              image[i * 4 * width + j2 * 4 ] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 1] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 2] = getNext(fimage + offset, &j, bit_depth);
              image[i * 4 * width + j2 * 4 + 3] = getNext(fimage + offset, &j, bit_depth);
            }
            break;
        }
        // jump scanline
        offset += bps(bit_depth, color_type, interlace_width_div[l](width - interlace_col_offs[l])) + 1;
      }
    }
  }
}

w_png_image w_png_read(w_ubyte *mem, w_int length) {
  w_int chunk_nr = 0, pos = 0, zlib_hdr = 2;
  w_int len, lread, offs, i, size = 0, ok, err = 0;
  w_int bit_depth = 0, color_type = 0, width = 0, height = 0;
  w_int plte_occ = 0, ihdr_occ = 0, idat_occ = 0, interlace = 0;

  w_word crc, c_crc;

  w_ubyte *image = 0, *fimage = 0, *bigbuf = 0;
  w_ubyte str[10];

  w_png_image p = 0;
  w_device device;
  w_driver_status s;

  if((device = deviceBSOpen("unzip_", wdp_none)) == 0) {
    woempa(10, "Unknown device unzip0\n");    
    // TODO: is this exit(0) really necessary
    exit(0);
  }

  offs = 0;

  // check for PNG header
  if (memcmp(mem, header, 8)) {
    woempa(9,"Header not ok !!\n");
    err = 1;
  } 
  else {
    woempa(5, "Header ok.\n\n");
  }
  pos += 8;

  ok = 1;
  while (ok && !err) {
    chunk_nr += 1;

    // read chunk length
    len = (mem[pos] << 24)  + (mem[pos + 1] << 16) + (mem[pos + 2] << 8) + mem[pos + 3];
    pos += 4;

    // read chunk type
    w_memcpy(str, mem + pos, 4);
    str[4] = '\0';
    pos += 4;

    // pointer to chunk body
    bigbuf = mem + pos;
    pos += len;

    // chunk CRC
    crc = (mem[pos] << 24)  + (mem[pos + 1] << 16) + (mem[pos + 2] << 8) + mem[pos + 3];
    pos += 4;

    woempa(5, "Chunk found.\n");
    woempa(5, "  length = %u\n", len);
    woempa(5, "  type = %s\n", str);
    woempa(5, "    ancillary bit = %i\n", (str[0] & 0x20) >> 5);
    woempa(5, "    private bit = %i\n", (str[1] & 0x20) >> 5);
    woempa(5, "    reserved bit = %i\n", (str[2] & 0x20) >> 5);
    woempa(5, "    safe-to-copy bit = %i\n", (str[3] & 0x20) >> 5);
    woempa(5, "  CRC = 0x%x\n", crc);

    // calculate chunks CRC
    c_crc = CCITT_32(bigbuf - 4, (w_size)(len + 4));
    woempa(5, "  -calculated CRC is 0x%x\n", c_crc);

    if (pos > length) {
        woempa(9,"  -reading outside buffer, erronous PNG !!\n");
        err = 1;
    } 
    else if (memcmp(str, ihdr, 4) == 0) {
      // chunk is IHDR type
      if (crc != c_crc) {
        woempa(9,"  -CHUNK IS CORRUPT, and it is critical !!\n");
        err = 1;
      } 
      else if (chunk_nr != 1) {
        woempa(9,"  -IHDR chunk is supposed to be the first chunck !!\n");
        err = 1;
      } 
      else if (len != 13) {
        woempa(9,"  -IHDR chunk length is not 13 bytes !!\n");
        err = 1;
      } 
      else {
        width = (bigbuf[0] << 24)  + (bigbuf[1] << 16) + (bigbuf[2] << 8) + bigbuf[3];
        woempa(5, "  -width = %u\n", width);

        height = (bigbuf[4] << 24)  + (bigbuf[5] << 16) + (bigbuf[6] << 8) + bigbuf[7];
        woempa(5, "  -height = %u\n", height);

        if (width == 0 || height == 0) {
          woempa(9,"  -INVALID WIDTH OR HEIGHT\n");
          err = 1;
        }

        bit_depth = bigbuf[8];
        woempa(5, "  -bit depth = %u\n", bit_depth);

        if (bit_depth != 1 && bit_depth != 2 && bit_depth != 4 && bit_depth != 8 && bit_depth != 16) {
          woempa(9,"  -UNKNOWN BITDEPTH\n");
          err = 1;
        }

        color_type = bigbuf[9];
        woempa(5, "  -color type = %u\n", color_type);

        if (color_type != 0 && color_type != 2 && color_type != 3 && color_type != 4 && color_type != 6) {
          woempa(9,"  -UNKNOWN COLORTYPE\n");
          err = 1;
        }

        woempa(5, "  -compression method = %u\n", bigbuf[10]);

        if (bigbuf[10] != 0) {
          woempa(9,"  -UNKNOWN COMPRESSION METHOD\n");
          err = 1;
        }

        woempa(5, "  -filter method = %u\n", bigbuf[11]);

        if (bigbuf[11] != 0) {
          woempa(9,"  -UNKNOWN FILTER TYPE\n");
          err = 1;
        }

        interlace = bigbuf[12];
        woempa(5, "  -interlace method = %u\n", interlace);

        if (interlace != 0 && interlace != 1) {
          woempa(9,"  -UNKNOWN INTERLACE METHOD\n");
          err = 1;
        }

        if (!err) {
          // allocate 32 bit image (RGBA)
          image = allocMem((unsigned)(4 * width * height));
          // and decompressed data buffer
          if (interlace) {
            // calculate interlaced blocksize
            size = 
              (bps(bit_depth, color_type, ceil_div8(width)) + 1) * ceil_div8(height); 

            if (height - 4 > 0) {
              size += (bps(bit_depth, color_type, ceil_div4(width)) + 1) * ceil_div8(height - 4);
            }
            if (height - 2 > 0) {
              size += (bps(bit_depth, color_type, ceil_div2(width)) + 1) * ceil_div4(height - 2);
            }
            if (height - 1 > 0) {
              size += (bps(bit_depth, color_type, width) + 1) * ceil_div2(height - 1);
            }

            if (width - 4 > 0) {
              size += (bps(bit_depth, color_type, ceil_div8(width - 4)) + 1) * ceil_div8(height);
            }
            if (width - 2 > 0) {
              size += (bps(bit_depth, color_type, ceil_div4(width - 2)) + 1) * ceil_div4(height);
            }
            if (width - 1 > 0) {
              size += (bps(bit_depth, color_type, ceil_div2(width - 1)) + 1) * ceil_div2(height);
            }
          } 
          else {
            size = bps(bit_depth, color_type, width) * height + height;
          }
          fimage = allocMem((unsigned)size);

          ihdr_occ = 1;
        }
      }
    } 
    else if (memcmp(str, plte, 4) == 0) {
      // chunk is PLTE type
      if (crc != c_crc) {
        woempa(9,"  -CHUNK IS CORRUPT, and it is critical !!\n");
        err = 1;
      } 
      else if (color_type == 0 || color_type == 4) {
        // no critical error, just ignore this chunk
        woempa(5, "  -PLTE chunk is not supposed to be here\n");
      } 
      else if (ihdr_occ == 0) {
        woempa(9,"  -no IHDR found !!!\n");
        err = 1;
      } 
      else if (len % 3 != 0 || len > 256 * 3) {
        woempa(9,"  -PLTE chunks length is not dividable by 3 or is to big!!!\n");
        err = 1;
      } 
      else {
        i = 0;
        while (i < len) {
          woempa(5, "  -color %i\n", i / 3);

          woempa(5, "    R %i\n", bigbuf[i]);
          woempa(5, "    G %i\n", bigbuf[i + 1]);
          woempa(5, "    B %i\n", bigbuf[i + 2]);
          
          pal[i / 3] = bigbuf[i] + (bigbuf[i + 1] << 8) + (bigbuf[i + 2] << 16) + (255 << 24);
          
          i = i + 3;
        }
// TODO: Get rid of this ??
//        if ((len / 3) > pow2[bit_depth]) {
//          // just ignore this error
//          woempa(5, "  -More colors in PLTE then supported by bit_depth\n");
//        }
        plte_occ = 1;
      }
    } 
    else if (memcmp(str, idat, 4) == 0) {
      // chunk is IDAT type
      if (crc != c_crc) {
        woempa(9,"  -CHUNK IS CORRUPT, and it is critical !!\n");
        err = 1;
      } 
      else if (color_type == 3 && plte_occ == 0) {
        woempa(9,"  -AIAI, type 3 and no PLTE found.\n");
        err = 1;
      } 
      else if (ihdr_occ == 0) {
        woempa(9,"  -no IHDR found !!!\n");
        err = 1;
      } 
      else {
        // it is a zlib stream, so strip header and footer
        // TODO: HOW DO WE GET THE FOOTER OFF ??
        // TODO: check return value of write !!!!!!
        if (len > 0) {
          if (zlib_hdr > 0) {
            if ((len - zlib_hdr) <= 0) {                  // len != 0 so has to be 1 or 2
              // ok, lets skip it
              zlib_hdr -= len;
            } 
            else {
              deviceBSWrite(device, bigbuf + zlib_hdr, (signed)(len - zlib_hdr), &lread, 10);
              zlib_hdr -= lread;
            }
          } 
          else {
            deviceBSWrite(device, bigbuf, (signed)len, &lread, 10);
          }
        }
      }
      idat_occ = 1;
    } 
    else if (memcmp(str, iend, 4) == 0) {
      if (crc != c_crc) {
        woempa(9,"  -CHUNK IS CORRUPT, and it is critical !!\n");
        err = 1;
      } 
      else if (ihdr_occ == 0) {
        woempa(9,"  -no IHDR found !!!\n");
        err = 1;
      } 
      else {
        woempa(5, "  -this is the end of file signature, so stop\n");
        ok = 0;
      }
    } 
    else {
      if (crc != c_crc) {
        woempa(5, "  -CHUNK IS CORRUPT, but it is not critical, so just ignore it !!\n");
      } 
      else if (ihdr_occ == 0) {
        woempa(9,"  -no IHDR found !!!\n");
        err = 1;
      } 
      else {

        // TODO : check if non critical bit is on !!!
        woempa(5, "  - NON CRITICAL CHUNK\n");
      }
    }
  }

  // TODO : do we have to do it all here, or can't we do this progressivelly ?

  if (image && fimage && !err) {    
    s = wds_success;
    offs = 0;
    while (s == wds_success && size - offs > 0) {
      s = deviceBSRead(device, fimage + offs, (signed)(size - offs), &lread, 10);
      offs += lread;
    }

    if (s == wds_internal_error) {
      woempa(9, "  -UNZIP ERROR !!\n");
      err = 1;
    } 
    else {
      if (s == wds_data_exhausted) {
        woempa(7, "  -AIAI, no full buffer received, we did not calculate the decompressed size correctly, or device errors\n");
      } 
      else {     
        s = deviceBSRead(device, fimage, 1, &lread, 10);
        if (s != wds_data_exhausted) {
          woempa(7, "  -AIAI, there is still data left on the device, we did not calculate the decompressed size correctly\n");
          err = 1;
        }
      }

      woempa(5, "  -decompressed size should be %i and is %i\n", size, offs);
      if (interlace) {
        process_interlaced_scanlines(width, height, bpp(bit_depth, color_type), bps(bit_depth, color_type, width) + 1, bit_depth, color_type, fimage, image);
      } 
      else {
        process_scanlines(width, height, bpp(bit_depth, color_type), bps(bit_depth, color_type, width) + 1, bit_depth, color_type, fimage, image);
      }
    } 
  }

  deviceBSClose(device);

  if (fimage != NULL) releaseMem(fimage);  

  if (err) {
    return NULL;
  }
    
  p = allocMem(sizeof(w_png_Image));
  p->width = width;
  p->height = height;
  p->data = image;

  return p;
}
