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

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <sys/soundcard.h>
#include <sys/ioctl.h>
#include "sound.h"
#include "ts-mem.h"
#include "oswald.h"

#define swap_short(s) (((s & 0x00FF) << 8) | \
                       ((s & 0xFF00) >> 8))
#define swap_int(i)   (((i & 0x000000FF) << 24) | \
                       ((i & 0x0000FF00) << 8) | \
                       ((i & 0x00FF0000) >> 8) | \
                       ((i & 0xFF000000) >> 24))
 
w_int ulaw2linear(w_word ulaw) {
  static w_int exp_lut[8]={0,132,396,924,1980,4092,8316,16764};
  w_int sign, exponent, mantissa, sample;
  w_ubyte ulawbyte = (w_ubyte)ulaw;
  
  ulawbyte = ~ulawbyte;
  sign = (ulawbyte & 0x80);
  exponent = (ulawbyte >> 4) & 0x07;
  mantissa = ulawbyte & 0x0F;
  sample = exp_lut[exponent] + (mantissa << (exponent+3));
  if (sign != 0) sample = -sample;
  return(sample);
}

w_void play(w_ubyte *file_data, w_int length, CallBack* callback, JNIEnv *env, jobject thisObj) {

  w_int i;
  w_int temp;
  char refsig[] = {'.', 's', 'n', 'd'};
  w_int *testsig = (w_int *)&refsig;
  w_int dspfd;
  
  w_int sig;
  w_int header_size;
  w_int sample_data_size;
  w_int audio_encoding;
  w_int sample_rate;
  w_int channels;
  w_short *new_data;
  w_ubyte *sound_data;
  w_word sound_size;

  w_int size;
  w_int pos;
  w_int interrupted = 0;

  /*
  ** Get the header of the .au file.
  */

  sig = ((unsigned int *)file_data)[0];
  header_size = swap_int(((unsigned int *)file_data)[1]);
  sample_data_size = swap_int(((unsigned int *)file_data)[2]);
  audio_encoding = swap_int(((unsigned int *)file_data)[3]);
  sample_rate = swap_int(((unsigned int *)file_data)[4]);
  channels = swap_int(((unsigned int *)file_data)[5]);

  sound_size = sample_data_size << 1;

  /*
  ** Check the signature of the data.
  */
  
  if(sig != *testsig) {
    printf("Wrong signature\n");
    return;
  }

  /*
  ** Open the dsp device.
  */

  dspfd = open("/dev/dsp", O_WRONLY, 0);


  if(dspfd == -1) {
    printf("Error opening device\n");
    return;
  }

  if(fcntl(dspfd, F_SETFL, O_NONBLOCK)){
    printf("Error occured while making dspfd non blocking\n");
    close(dspfd);
    return;
  }

  /*
  ** Try to set the dsp device to 16 bit signed (little endian)
  */
  
  temp = AFMT_S16_LE;
  if (ioctl(dspfd, SNDCTL_DSP_SETFMT, &temp) == -1) {
    printf("SNDCTL_DSP_SETFMT failed\n");
    close(dspfd);
    return;
  }
  if (temp != AFMT_S16_LE) {
    printf("not supported format, instead use this:  %d\n", temp);
    close(dspfd);
    return;
  }

  /*
  ** Set the number of channels (1 = mono, 2 = stereo).
  */

  temp = channels - 1;
  if (ioctl(dspfd, SNDCTL_DSP_STEREO, &temp) == -1) {
    printf("SNDCTL_DSP_STEREO failed\n");
    close(dspfd);
    return;
  }

  if (channels != temp+1) {
    printf("Oops, requested number of channels not available\n");
    close(dspfd);
    return;
  }

  /*
  ** Check the number of channels (1 = mono, 2 = stereo).
  */

/*
  temp = channels;
  if (ioctl(dspfd, SNDCTL_DSP_CHANNELS, &temp) == -1) {
    printf("SNDCTL_DSP_CHANNELS failed\n");
    return;
  }
*/
  /*
  ** Set the sample rate.
  */

  temp = sample_rate;
  if (ioctl(dspfd, SNDCTL_DSP_SPEED, &temp)==-1) {
    printf("SNDCTL_DSP_SPEED failed\n");
    close(dspfd);
    return;
  }

  /*
  ** Convert the original data from mu-law to 16 bit signed.
  */


  sound_data = &file_data[header_size];
  //fprintf(stderr, " sound_size : %d\n", sound_size);
  //fprintf(stderr, " sample_data_size : %d\n", sample_data_size);
  new_data = allocMem(sound_size);
  if(new_data == NULL){
    close(dspfd);
    return;
  }

  for(i=0; i < sample_data_size; i++) {
    new_data[i] = (w_short)(ulaw2linear((w_word)sound_data[i]));
  }
  
  size = 0;
  pos = 0;

  /* we have to write at least 4 bytes */
  while(pos < (signed)sound_size - 3) {
    audio_buf_info info;

    if(callback(env,thisObj)){
      interrupted = 1;
      break;
    }

    if(ioctl(dspfd, SNDCTL_DSP_GETOSPACE, &info) == -1){
      //printf("Failed to get audio_buf_info\n");
      break;
    }
    else {
      size_t max = (size_t)(sound_size - pos);
      size_t space = (size_t)(info.fragments * info.fragsize);
      space = (space > max ? max : space);

      if(space == 0){
        //printf("no space available -- sleeping\n");
        x_thread_sleep(x_usecs2ticks(250 * 1000));

        continue;
      }

      //printf("Writing %d bytes at %d\n", max, pos);

      size = write(dspfd, (w_ubyte *)new_data + pos, space);
      //printf("Writing %d bytes at %d (wrote %d)\n", max, pos, size);
      if(size == -1){
        break;
      }
      else {
        pos += size;
      }
    }
  }

  temp = 0;
  if(ioctl(dspfd, SNDCTL_DSP_POST, &temp) == -1) {
    printf("SNDCTL_DSP_POST failed\n");
  }

  while(TRUE) {
    
    if(!interrupted) {

      audio_buf_info info;
      
      if(ioctl(dspfd, SNDCTL_DSP_GETOSPACE, &info) != -1) {
        int total = (info.fragstotal * info.fragsize - info.bytes) / (2 * channels);
        long long total_time = (total * 1000) /  sample_rate;
        x_size sleeptime = ((x_size) total_time) *1000;
        x_thread_sleep(x_usecs2ticks(sleeptime));
      }
      
      if(ioctl(dspfd, SNDCTL_DSP_GETOSPACE, &info) != -1) {
        if(info.fragstotal * info.fragsize >= info.bytes) {
          close(dspfd);
          releaseMem(new_data);
          return;
        }
      }
      else {
        close(dspfd);
        releaseMem(new_data);
        return;
      }
    }
  }

}

