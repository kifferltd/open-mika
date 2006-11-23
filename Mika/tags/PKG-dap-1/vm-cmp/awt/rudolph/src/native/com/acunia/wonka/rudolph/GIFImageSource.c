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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/


/* $Id: GIFImageSource.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include <string.h>
#include <stdio.h>

#include "awt-classes.h"
#include "arrays.h"
#include "fields.h"
//#include "threads.h"       // currentWonkaThread
//#include "ts-mem.h"        // allocMem(), releaseMem()
//#include "heap.h"

static jfieldID   GF_x;
static jfieldID   GF_y;
static jfieldID   GF_width;
static jfieldID   GF_height;
static jfieldID   GF_transparent;
static jfieldID   GF_pixels;
static jfieldID   GIS_pixels;

#ifdef GIF_SUPPORT

static jclass     GIFFrame_class;
static jfieldID   GF_delay;
static jfieldID   GF_disposal;
static jfieldID   GF_colormodel;
static jmethodID  GF_constructor;
static jclass     GIS_class;
static jmethodID  GIS_addFrame;
static jfieldID   GIS_colormodel;
static jfieldID   GIS_background;
static jclass     IC_class;
static jmethodID  IC_constructor;
static jmethodID  IC_constructor2;

static w_void JNI_init(JNIEnv *env) {

  /*
  ** Get the values for all these fields and cache them so the next time
  ** we need them, we can skip this step.
  */
  
  GIFFrame_class  = (*env)->FindClass(env, "com/acunia/wonka/rudolph/GIFFrame");
  GF_pixels       = (*env)->GetFieldID(env, GIFFrame_class, "pixels", "[B");
  GF_x            = (*env)->GetFieldID(env, GIFFrame_class, "x", "I");
  GF_y            = (*env)->GetFieldID(env, GIFFrame_class, "y", "I");
  GF_width        = (*env)->GetFieldID(env, GIFFrame_class, "width", "I");
  GF_height       = (*env)->GetFieldID(env, GIFFrame_class, "height", "I");
  GF_delay        = (*env)->GetFieldID(env, GIFFrame_class, "delay", "I");
  GF_disposal     = (*env)->GetFieldID(env, GIFFrame_class, "disposal", "I");
  GF_transparent  = (*env)->GetFieldID(env, GIFFrame_class, "transparent", "I");
  GF_colormodel   = (*env)->GetFieldID(env, GIFFrame_class, "colorModel", "Ljava/awt/image/ColorModel;");
  GF_constructor  = (*env)->GetMethodID(env, GIFFrame_class, "<init>", "()V");
  GIS_class       = (*env)->FindClass(env, "com/acunia/wonka/rudolph/GIFImageSource");
  GIS_addFrame    = (*env)->GetMethodID(env, GIS_class, "addFrame", "(Lcom/acunia/wonka/rudolph/GIFFrame;)V");
  GIS_colormodel  = (*env)->GetFieldID(env, GIS_class, "colorModel", "Ljava/awt/image/ColorModel;");
  GIS_pixels      = (*env)->GetFieldID(env, GIS_class, "pixels", "[B");
  GIS_background  = (*env)->GetFieldID(env, GIS_class, "background", "B");
  IC_class        = (*env)->FindClass(env, "java/awt/image/IndexColorModel");
  IC_constructor  = (*env)->GetMethodID(env, IC_class, "<init>", "(II[BIZ)V");
  IC_constructor2 = (*env)->GetMethodID(env, IC_class, "<init>", "(II[BIZI)V");
}

#define FLAG_BITS        0x0007
#define FLAG_PALETTE     0x0080
#define FLAG_INTERLACED  0x0040

#define BLOCK_IMAGE      0x002c
#define BLOCK_EXT        0x0021
#define BLOCK_NULL       0x0000
#define BLOCK_TRAILER    0x003b

#define EXT_TEXT         0x0001
#define EXT_CONTROL      0x00f9
#define EXT_COMMENT      0x00fe
#define EXT_APPLICATION  0x00ff

// #define woempa(b, a...) if(b >= 6) _woempa("", "", 0, 9, ##a)

typedef struct gif_Params {
  w_ushort   width;
  w_ushort   height;
  w_ushort   bits;
  w_ubyte    flags;
  w_ubyte    back;
  w_ubyte    aspect;
} gif_Params;

typedef gif_Params  *gif_params;

static inline void gif_next_code(w_ubyte **p, w_ubyte **q, w_ubyte **data) {
  w_int block_size;
  (*p)++;
  if(*p >= *q) {
    block_size = *((*data)++);
    *p = *data;
    *q = *data + block_size;
    *data += block_size;
  }
}

w_int gif_unpack(w_ubyte *data, w_ubyte *image, w_word bits, gif_params params) {

  w_int    code_size = bits + 1;             // Current code size in bits.
  w_int    next_code = (1 << bits) + 2;      // Next entry.
  w_short  this_code;                        // Current entry.
  w_int    prev_code = -1;                   // Previous entry.
  w_int    prev_token = -1;                  // Last decoded entry.
  w_int    current_code;                     // Current code.
  w_int    bits_left = 8;                    // Bits left in *p;
  w_ubyte  *p = NULL;                        // Current byte;
  w_ubyte  *q = NULL;                        // Last byte + 1;
  w_ubyte  *u;
  w_word   current_line = 0;
  w_word   current_pixel = 0;
  w_word   current_pass = 0;

  w_ubyte  *begin = data;
  w_ubyte  *first_stack;
  w_ubyte  *last_stack;
  w_int    *stack;
  w_int    result = 0;

  static w_int word_mask[] = { 0x0000, 0x0001, 0x0003, 0x0007, 0x000f, 0x001f, 0x003f, 0x007f,
                               0x00ff, 0x01ff, 0x03ff, 0x07ff, 0x0fff, 0x1fff, 0x3fff, 0x7fff };
                               
  static w_int interlaced[] = { 8, 8, 4, 2, 0 };
  static w_int start[] = { 0, 4, 2, 1, 0 };
  
  if(bits < 2 || bits > 8) return 0;

  first_stack = allocClearedMem(4096);
  if(!first_stack) {
    return 0;
  }
  
  last_stack = allocClearedMem(4096);
  if(!last_stack) {
    releaseMem(first_stack);
    return 0;
  }
  
  stack = allocClearedMem(4096 * sizeof(w_int));
  if(!stack) {
    releaseMem(first_stack);
    releaseMem(last_stack);
    return 0;
  }
  
  woempa(5, "bits: %d\n", bits);

  while(1) {
    woempa(5, "bits_left: %d\n", bits_left);
    if(bits_left == 8) {
      gif_next_code(&p, &q, &data);
      bits_left = 0;
    }

    this_code = *p;
    current_code = code_size + bits_left;
    
    woempa(5, "this_code: %d\n", this_code);
    woempa(5, "current_code: %d\n", current_code);
    
    /*
    ** Get the next code. (code_size bits wide)
    */
    
    if(current_code <= 8) {
      *p >>= code_size;
      bits_left = current_code;
    }
    else {
      gif_next_code(&p, &q, &data);
      this_code |= *p << (8 - bits_left);
      if(current_code <= 16) {
        bits_left = current_code - 8;
        *p >>= bits_left;
      }
      else {
        gif_next_code(&p, &q, &data);
        this_code |= *p << (16 - bits_left);
        bits_left = current_code - 16;
        *p >>= bits_left;
      }
    }

    /*
    ** Mask it so that we have only <code_size> bits
    */
    
    this_code &= word_mask[code_size];
    
    current_code = this_code;

    /*
    ** If we arrive at a code (1 << bits) + 1
    ** (e.g. 257 if we have 8 bits) we are at the end
    ** of the image block.
    */
    
    if(this_code == (1 << bits) + 1) {
      result = (int)data - (int)begin;
      break;
    }

    if(this_code > next_code) {

      /*
      ** Error, should not happen...
      */

      woempa(9, "Error\n");
      break;
    }

    /*
    ** If we arrive at a code (1 << bits)
    ** (e.g. 256 if we have 8 bits) we need to reinitialize
    ** the string table and start over with the original
    ** code_size.
    */
    
    if(this_code == (1 << bits)) {
      next_code = (1 << bits) + 2;
      code_size = bits + 1;
      prev_token = -1;
      prev_code = -1;
      continue;
    }

    u = first_stack;

    if(this_code == next_code) {
      if(prev_code == -1) {

        /*
        ** Bad code...
        */

        woempa(9, "Bad code\n");
        break;
      }

      *u++ = prev_token;
      this_code = prev_code;
    }

    /*
    ** Search the string table.
    */
    
    while(this_code >= (1 << bits)) {
      *u++ = last_stack[this_code];
      this_code = stack[this_code];
    }

    prev_token = this_code;

    while(1) {

      /*
      ** Store the decoded pixel in the image.
      */

      woempa(5, "line : %d,  pixel: %d\n", current_line, current_pixel);

      image[current_line * params->width + current_pixel] = this_code;
      
      current_pixel++;
      
      if(current_pixel >= params->width) {

        /*
        ** We crossed the line, go to the beginning of the next line.
        */
        
        current_pixel = 0;
        
        if(params->flags & FLAG_INTERLACED) {

          /*
          ** The image is interlaced. Go to the next interlaced line.
          */
          
          current_line += interlaced[current_pass];
          
          if(current_line >= params->height) {

            /*
            ** Jumped to far... Go to the next pass and start again
            ** from the top.
            */
            
            current_pass++;
            current_line = start[current_pass];
          }
        }
        else {

          /*
          ** Image is not interlaced, go to the next line.
          */
          
          current_line++;
        }
      }
            
      if(u <= first_stack) break;
      this_code = *--u;
    }

    if(next_code < 4096 && prev_code != -1) {

      stack[next_code] = prev_code;
      last_stack[next_code] = prev_token;
      next_code++;

      /*
      ** If the next code is larger than the current code_size,
      ** we need to increment the code_size.
      */
      
      if(next_code >= (1 << code_size) && code_size < 12) {
        code_size++;
      }
    }
    
    prev_code = current_code;
    
  }

  releaseMem(first_stack);
  releaseMem(last_stack);
  releaseMem(stack);

  return result;
   
}

w_void GIFImageSource_readImage(JNIEnv *env, jobject thisObject, jbyteArray imageFile) {
  
  w_ubyte    *image = instance2Array_byte(imageFile);
  w_word     len = 0;
  w_word     position = 0;
  w_ubyte    keep_going = 1;
  gif_params params;
  w_ubyte    code_size;
  w_ubyte    block_size;
  w_ubyte    *image_data = NULL;
  w_int      unpack_result = 0;
  w_ushort   x, y, w, h, delay;
  w_ushort   full_width, full_height;
  w_ubyte    flags;
  jobject    GIFFrame;
  jobject    ColorModel;
  jbyteArray Array;
  jbyteArray palette;
  w_int      transparent = -1;

  /*
  ** Setup some JNI fields.
  */

  if(!GIFFrame_class) JNI_init(env);
  
  /*
  ** To be sure we have a GIFFrame even when the file contains no
  ** control blocks, we allocate one at the beginning.
  */
  
  GIFFrame = (*env)->NewObject(env, GIFFrame_class, GF_constructor);

  /*
  ** Allocate memory for the parameters
  */ 
  
  params = allocMem(sizeof(gif_Params));
  if (!GIFFrame || !params) {

    return;

  }
  
  /*
  ** The header of a GIF file looks like this :
  **
  **  char[6]  signature  (GIF87a or GIF89a)
  **  short    width
  **  short    height
  **  char     flags
  **  char     background
  **  char     aspect
            GIFFrame = (*env)->NewObject(env, GIFFrame_class, GF_constructor);
  **
  ** We only read in the first frame of the image and
  ** since every frame has it's own header with width
  ** & height, we discard the ones from the header.
  **
  ** Jump over the signature.
  */ 
 
  position += 6;

  /*
  ** Get width and height.
  */
  
  w_memcpy(&full_width, &image[position], 2);
  position += 2;
  
  w_memcpy(&full_height, &image[position], 2);
  position += 2;
  
  /*
  ** Get the flags. We need these to know if the GIF
  ** file has a global palette for all it's frames.
  ** And also to known the number of bits/pixel.
  */

  params->flags = image[position++];
  params->bits = (params->flags & FLAG_BITS) + 1;

  /*
  ** Get the background.
  */

  (*env)->SetByteField(env, thisObject, GIS_background, image[position++]);

  /*
  ** Jump over aspect. 
  */

  position++;
  
  /*
  ** Get the global palette.
  */
  
  if((params->flags & FLAG_PALETTE) == FLAG_PALETTE) {
    len = 3 * (1 << params->bits);
    woempa(6, "Palette length : %d\n", len);

    /*
    ** Generate an IndexColorModel of this palette and
    ** store it in the GIFImageSource instance.
    */

    palette = (*env)->NewByteArray(env, len);

    if(palette) {
      (*env)->SetByteArrayRegion(env, palette, 0, len, &image[position]);

      ColorModel = (*env)->NewObject(env, IC_class, IC_constructor, params->bits, 1 << params->bits, palette, 0, JNI_FALSE);
// Not needed      (*env)->DeleteLocalRef(env, palette);
      
      (*env)->SetObjectField(env, thisObject, GIS_colormodel, ColorModel);
// Not needed      (*env)->DeleteLocalRef(env, ColorModel);
    }

    position += len;
  }

  /*
  ** Parse the different blocks in the file.
  ** We only need the first image block, the other
  ** blocks are discarded.
  */

  while(keep_going) {
    switch(image[position++]) {
      case BLOCK_IMAGE:

        /*
        ** Get parameters from the block header. 
        ** The header looks like this:
        **
        **  short   x
        **  short   y
        **  short   width
        **  short   height
        **  char    flags
        **
        ** Get the x and y position of this frame.
        */

        w_memcpy(&x, &image[position], 2);
        position += 2;
        
        w_memcpy(&y, &image[position], 2);
        position += 2;
        
        /* 
        ** Get the width and height of this frame.
        */
        
        w_memcpy(&params->width, &image[position], 2);
        position += 2;
        w = params->width;
        
        w_memcpy(&params->height, &image[position], 2);
        position += 2;
        h = params->height;

        /*
        ** Get the flags of this frame.
        */
        
        params->flags = image[position++];
        params->bits = (params->flags & FLAG_BITS) + 1;
        
        woempa(6, "Image Block ->  x: %d  y: %d  w: %d  h: %d%s\n", x, y, w, h, (params->flags & FLAG_PALETTE ? ", custom palette" : ""));

        /*
        ** Check if the image block has a local palette and read it.
        ** (We overwrite the global palette since we only support the first frame).
        */
        
        if(params->flags & FLAG_PALETTE) {
          len = 3 * (1 << params->bits);
          woempa(6, "Palette length : %d\n", len);
    
          palette = (*env)->NewByteArray(env, len);

          if(palette) {
          
            /*
            ** Generate an IndexColorModel of this palette.
            */

            (*env)->SetByteArrayRegion(env, palette, 0, len, &image[position]);

            if(transparent != -1 ) {

              /*
              ** Generate an IndexColorModel with a transparent pixel.
              */
              
              ColorModel = (*env)->NewObject(env, IC_class, IC_constructor2, params->bits, 
                                             1 << params->bits, palette, 0, JNI_FALSE, transparent);
// Not needed              (*env)->DeleteLocalRef(env, palette);
              transparent = -1;
            }
            else {
              
              /*
              ** Generate an IndexColorModel without a transparent pixel.
              */
              
              ColorModel = (*env)->NewObject(env, IC_class, IC_constructor, params->bits, 
                                             1 << params->bits, palette, 0, JNI_FALSE);
            }

            /*
            ** Store the IndexColorModel in the current GIFFrame instance.
            */
            
            (*env)->SetObjectField(env, GIFFrame, GF_colormodel, ColorModel);
// Not needed            (*env)->DeleteLocalRef(env, ColorModel);
          }

          position += len;
        }
        else if(transparent != -1) {

          /*
          ** The current frame has a transparent pixel, but no new palette. 
          ** We need to clone the parent palette and enable transparency.
          */

          /*
          ** EViL trick : Enable transparency in the global palette :
          */

          ColorModel = (*env)->GetObjectField(env, thisObject, GIS_colormodel);
          setIntegerField(ColorModel, F_IndexColorModel_trans, transparent);
// Not needed          (*env)->DeleteLocalRef(env, ColorModel);
        }

        /*
        ** Get the code size with wich we need to start decompression.
        */
        
        code_size = image[position++];

        /*
        ** Allocate memory to hold the pixel data.
        */

        image_data = allocMem((w_word)(params->width * params->height));
        if (!image_data) {
          releaseMem(params);

          return;

        }
        
        /*
        ** Now we need to uncompress the image data.
        */

        unpack_result = gif_unpack(image + position, image_data, code_size, params);
       
        woempa(9, "  -> Read %d bytes of image data\n", unpack_result);

        /*
        ** Jump over the image data and the terminator.
        */
        
        position += unpack_result;
        
        position += 1;

        /*
        ** Store the parameters in the GIFFrame.
        */
        
        (*env)->SetIntField(env, GIFFrame, GF_x, x);
        (*env)->SetIntField(env, GIFFrame, GF_y, y);
        (*env)->SetIntField(env, GIFFrame, GF_width, params->width);
        (*env)->SetIntField(env, GIFFrame, GF_height, params->height);
       
        len = params->width * params->height;

        /*
        ** Store the image data (pixels) in the GIFFrame.
        */
        
        Array = (*env)->NewByteArray(env, len);

        if(Array) {
          (*env)->SetByteArrayRegion(env, Array, 0, len, (w_byte *)image_data);
          (*env)->SetObjectField(env, GIFFrame, GF_pixels, Array);
// Not needed          (*env)->DeleteLocalRef(env, Array);
        }
        else {
          woempa(10, "WARNING: could not allocate array to hold image data.\n");
        }

        /*
        ** Add the GIFFrame to the producer.
        */
        
        (*env)->CallVoidMethod(env, thisObject, GIS_addFrame, GIFFrame);
// Not needed        (*env)->DeleteLocalRef(env, GIFFrame);
       
        /*
        ** Release the image_data.
        */

        releaseMem(image_data);
        
        break;
      
      case BLOCK_EXT:
        woempa(6, "Extension Block\n");
        
        switch(image[position++]) {
          case EXT_CONTROL:
           
            /*
            ** We need to parse a graphical control block (if there's one)
            ** to know which is the transparant color (if there's one).
            **
            ** This block looks like this:
            **
            **  char   block_size
            **  char   flags
            **  short  delay
            **  char   transparent_color
            **  char   terminator ( = 0x00)
            **
            ** The flags:
            **  0x01   transparant color
            **  0x02   user input
            **  0x1C   disposal method of frames
            **           0 - do nothing
            **           1 - leave frame in place
            **           2 - restore background color
            **           3 - restore original background
            **  0xE0   reserved
            */

            position += 1;              // Jump over the block_size

            /*
            ** Get the flags and the delay field.
            */
            
            flags = image[position++]; 
        
            w_memcpy(&delay, &image[position], 2);
            position += 2;   
            
            woempa(6, "  Control Block  -->  delay: %d%s, disposal: %d\n", delay, (flags & 0x01 ? ", transparent color" : ""), (flags >> 2) & 3);

            /*
            ** Allocate a new GIFFrame instance.
            */
            
            GIFFrame = (*env)->NewObject(env, GIFFrame_class, GF_constructor);
            
            /*
            ** Check if there is a transparent pixel.
            ** (and store it in the current GIFFrame).
            */
            
            if(flags & 0x01) {
              transparent = image[position++];
              (*env)->SetIntField(env, GIFFrame, GF_transparent, transparent);
              woempa(6, "    transparent color : %d\n", transparent);
            }
            else {
              position++;
            }

            position += 1;            // Jump over the terminator

            /*
            ** Store the delay and the disposal method in the GIFFrame instance.
            */
            
            (*env)->SetIntField(env, GIFFrame, GF_delay, delay);
            (*env)->SetIntField(env, GIFFrame, GF_disposal, (flags >> 2) & 3);
            
            break;

          case EXT_APPLICATION:

            /*
            ** Parsing of an application block.
            ** This is the structure of an application block :
            **
            **  char     block_size
            **  char[8]  application_id
            **  char[3]  authentication
            **  
            ** Loops of this --->
            **  char     block_size
            **  char[]   block_size bytes of data 
            ** <--- Until we reach the terminator.
            **
            **  char     terminator (always 0x00)
            **
            ** We don't want any of this data but we need to skip it.
            */
            
            woempa(6, "  Application Block\n");

            position += 1;            // Jump over the block_size
            position += 8;            // Jump over the application_id
            position += 3;            // Jump over the authentication
            
            while((len = image[position++]) != 0) { 
              position += len;
            }
            
            break;

          case EXT_TEXT:
            
            /*
            ** Parsing of a text block.
            ** This is the structure of an application block :
            **
            **  char     block_size
            **  short    left
            **  short    top
            **  short    gridwidth
            **  short    gridheight
            **  char     cellwidth
            **  char     cellheight
            **  char     foreground color
            **  char     background color
            **  
            ** Loops of this --->
            **  char     block_size
            **  char[]   block_size bytes of text 
            ** <--- Until we reach the terminator.
            **
            **  char     terminator (always 0x00)
            **
            ** We don't want any of this data but we need to skip it.
            */
            
            woempa(6, "  Text Block\n");

            position += 1;            // Jump over the block_size
            position += 12;           // Jump over all the rest.

            while((len = image[position++]) != 0) { 
              position += len;
            }
            
            break;
            
          case EXT_COMMENT:
            
            /*
            ** Parsing of a comment block.
            ** This is the structure of an application block :
            **
            ** Loops of this --->
            **  char     block_size
            **  char[]   block_size bytes of text 
            ** <--- Until we reach the terminator.
            **
            **  char     terminator (always 0x00)
            **
            ** We don't want any of this data but we need to skip it.
            */
            
            woempa(6, "  Comment Block\n");

            while((len = image[position++]) != 0) { 
              position += len;
            }
            
            break;
            
          default:
              
            /*
            ** Unknown extension. 
            ** Read in the block size and skip over it..
            */
            
            woempa(6, "  Unknown Block\n");
            
            block_size = image[position++];
            position += block_size;
        }
        
        break;
      
      case BLOCK_NULL: 
        woempa(6, "NULL Block\n");
        break;
        
      case BLOCK_TRAILER: 

        /*
        ** We have reached the end of the file.
        ** Stopping running in loops and return.
        */
        
        woempa(6, "Trailer\n");
        keep_going = 0;
        break;
        
      default:
        woempa(9, "Block Error (%x, %c)\n", image[position - 1], image[position - 1]);
        keep_going = 0;
    }
  }

  if(!unpack_result) {
    woempa(10, "WARNING: error decoding GIF image.\n");
    releaseMem(params);
    return;
  }

  /*
  ** Store the width and height of the complete image in
  ** the GIFImageSource, but only if decoding was successful.
  ** If not, both these fields will be kept zero.
  */
  
  setIntegerField(thisObject, F_GIFImageSource_width, full_width);
  setIntegerField(thisObject, F_GIFImageSource_height, full_height);

  /*
  ** Release memory:
  */

  releaseMem(params);
  
  return;
}

#else 

/*
** No GIF support 
*/

#include <stdio.h>

w_void GIFImageSource_readImage(JNIEnv *env, jobject thisObject, jbyteArray imageFile) {

  /*
  ** Be nice and show a message that this is not possible.
  */
  
  fprintf(stderr, "--== This version of Wonka is compiled without GIF support ==--\n");
  return;

}

#endif

w_void GIFImageSource_copyFrame(JNIEnv *env, jobject thisObject, jobject frame) {
  w_int i, j;
  w_ubyte pixel;
  w_int x = (*env)->GetIntField(env, frame, GF_x);
  w_int y = (*env)->GetIntField(env, frame, GF_y);
  w_int w = (*env)->GetIntField(env, frame, GF_width);
  w_int h = (*env)->GetIntField(env, frame, GF_height);
  w_int trans = (*env)->GetIntField(env, frame, GF_transparent);
  w_int width = getIntegerField(thisObject, F_GIFImageSource_width);
  w_ubyte *pixels = instance2Array_byte((*env)->GetObjectField(env, thisObject, GIS_pixels));
  w_ubyte *frame_pixels = instance2Array_byte((*env)->GetObjectField(env, frame, GF_pixels));

  for(j = 0 ; j < h; j++) {
    for(i = 0; i < w; i++) {
      pixel = frame_pixels[j * w + i];
      if(trans == -1 || trans != pixel) {
        pixels[(y + j) * width + (x + i)] = pixel;
      }
    }
  }
}
  
