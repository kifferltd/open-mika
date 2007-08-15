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

#include <sys/types.h>
#include <sys/audioio.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

#include <errno.h>

#include "wonka.h"
#if !defined(TEST)
#include "oswald.h"
#endif

#define AUDIO_FILE_ENCODING_MULAW_8             1
#define AUDIO_FILE_ENCODING_LINEAR_8            2
#define AUDIO_FILE_ENCODING_LINEAR_16           3
#define AUDIO_FILE_ENCODING_LINEAR_24           4
#define AUDIO_FILE_ENCODING_LINEAR_32           5
#define AUDIO_FILE_ENCODING_ALAW_8              27

w_void play_au(w_ubyte *file_data)
{
  w_int fd;
  audio_info_t    info;
  w_int refsig = ((w_int)0x2e736e64);  /* '.', 's', 'n', 'd' */
  w_int sig;
  w_int header_size;
  w_int audio_encoding;
  w_int sample_rate;
  w_int channels;

  w_ubyte *sound_data;
  w_word sound_size;
  w_word dataout;

  w_int i;

  size_t bufsize;
  size_t nb;
  size_t nw;

  static struct {
    int     file_encoding;
    int     encoding;
    int     precision;
  } file2sw_encodings[] = {
    { AUDIO_FILE_ENCODING_MULAW_8,          AUDIO_ENCODING_ULAW,    8 },
    { AUDIO_FILE_ENCODING_LINEAR_8,         AUDIO_ENCODING_SLINEAR_BE, 8 },
    { AUDIO_FILE_ENCODING_LINEAR_16,        AUDIO_ENCODING_SLINEAR_BE, 16 },
    { AUDIO_FILE_ENCODING_LINEAR_24,        AUDIO_ENCODING_SLINEAR_BE, 24 },
    { AUDIO_FILE_ENCODING_LINEAR_32,        AUDIO_ENCODING_SLINEAR_BE, 32 },
    { AUDIO_FILE_ENCODING_ALAW_8,           AUDIO_ENCODING_ALAW,    8 },
    { -1, -1 }
  };


  /*
  ** Check the signature of the data.
  */
  sig = ntohl(((unsigned int *)file_data)[0]);
  
  if(sig != refsig) {
    printf("Wrong signature\n");
    return;
  }

  /*
  ** Get the header of the .au file.
  */

  header_size = ntohl(((unsigned int *)file_data)[1]);
  sound_size = ntohl(((unsigned int *)file_data)[2]);
  audio_encoding = ntohl(((unsigned int *)file_data)[3]);
  sample_rate = ntohl(((unsigned int *)file_data)[4]);
  channels = ntohl(((unsigned int *)file_data)[5]);

  sound_data = &file_data[header_size];

  /*
  ** Open the sound device.
  */

  fd = open("/dev/audio", O_WRONLY);

  if(fd == -1) {
    printf("Error opening device\n");
    printf("Error was(%d): %s\n", errno, sys_errlist[errno]);
    return;
  }

  if(fcntl(fd, F_SETFL, O_NONBLOCK)) {
    printf("Error occurred while making fd non blocking\n");
    close(fd);
    return;
  }
  if (ioctl(fd, AUDIO_GETINFO, &info) < 0) {
    printf("Failed to get audio info\n");
    return;
  }
  bufsize = info.play.buffer_size;

  AUDIO_INITINFO(&info);

  for (i = 0; file2sw_encodings[i].file_encoding != -1; i++) {
    if (file2sw_encodings[i].file_encoding == audio_encoding) {
       info.play.encoding = file2sw_encodings[i].encoding;
       info.play.precision = file2sw_encodings[i].precision;
       break;
    }
  }
  if (file2sw_encodings[i].file_encoding == -1) {
    printf("Unrecognised file encoding\n");
    return;
  }
  info.play.sample_rate = sample_rate;
  info.play.channels = channels;
  info.mode = AUMODE_PLAY_ALL;

  if (ioctl(fd, AUDIO_SETINFO, &info) < 0)
    printf("Failed to set audio info\n");

  dataout = 0;
  while (sound_size == 0 || dataout < sound_size) {
    nb = sound_size - dataout;
    if (nb > bufsize)
       nb = bufsize;
    nw = write(fd, sound_data + dataout, nb);
    if (nw != nb)
      printf("Audio device write failed\n");
      return;
    dataout += nw;
  }
  if (ioctl(fd, AUDIO_DRAIN) < 0)
    printf("Audio drain ioctl failed");
  close(fd);
}
