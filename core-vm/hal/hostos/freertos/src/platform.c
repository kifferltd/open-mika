/**************************************************************************
* Copyright (c) 2021, 2023 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "oswald.h"
#include "wonka.h"
#include <stdio.h>
/*
#include <signal.h>
#include <string.h>
#include <unistd.h>
*/

void w_dump_info(void);

int wonka_killed;
int dumping_info;

x_thread heartbeat_thread = NULL;

w_boolean Wonka_static_useCli(w_thread thread, w_instance theClass) {
#ifdef FREERTOS_CLI
  return true;
#else
  return false;
#endif
}

#define ROW_WIDTH 16
#define NO_OF_ROWS 16 // WAS: 64

uint8_t saved_low_memory[NO_OF_ROWS][ROW_WIDTH];

void grab_low_memory() {
  memcpy(saved_low_memory, NULL, ROW_WIDTH*NO_OF_ROWS);
  for (int row = 0; row < NO_OF_ROWS; ++row) {
    uint8_t* saved_row = saved_low_memory[row];
    woempa(1, "%p : %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x\n",
      row*ROW_WIDTH, 
      saved_row[0], saved_row[1], saved_row[2], saved_row[3], 
      saved_row[4], saved_row[5], saved_row[6], saved_row[7], 
      saved_row[8], saved_row[9], saved_row[10], saved_row[11], 
      saved_row[12], saved_row[13], saved_row[14], saved_row[15]);
  }
}

void _lowMemoryCheck(const char *fun, int line) {
  for (int row = 0; row < NO_OF_ROWS; ++row) {
    // HACK HACK HACK - we use 0x00000070/74 for debug purposes
    if (row == 7) continue;
    //
    uint8_t* saved_row = &saved_low_memory[row][0];
    uint8_t *found_row = (uint8_t*)(row*ROW_WIDTH);
    if (memcmp(found_row, saved_row, ROW_WIDTH)) {

      _wabort(fun, line, ABORT_WONKA, "Urk - low memory corruption in row %p!\n"
        "  found   : %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x\n"
        "  expected: %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x\n",
        found_row,
        found_row[0],  found_row[1],  found_row[2],  found_row[3],
        found_row[4],  found_row[5],  found_row[6],  found_row[7],
        found_row[8],  found_row[9],  found_row[10], found_row[11],
        found_row[12], found_row[13], found_row[14], found_row[15], 
        saved_row[0], saved_row[1], saved_row[2], saved_row[3], 
        saved_row[4], saved_row[5], saved_row[6], saved_row[7], 
        saved_row[8], saved_row[9], saved_row[10], saved_row[11], 
        saved_row[12], saved_row[13], saved_row[14], saved_row[15]
      );
    }
  }
  woempa(1, "Low memory all present and correct at %s:%d.\n", fun, line);
}
