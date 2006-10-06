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
*                                                                         *
**************************************************************************/


/*
** $Id: sha.c,v 1.4 2005/01/06 21:55:06 cvs Exp $
**
** A Wonka specific implementation of the NIST FIPS-180, Secure Hashing Algorithm (SHA-1).
** This work is a heavily hacked version of the SHA that was released by
** Jim Gillogly in July 1994, which was in itself based on prior work of Peter Gutmann. Both
** these authors placed their work in the public domain and are hereby credited. Thank you guys,
** I owe you a beer...
**
** The main change is that the original code only calculated the hash on a message that was
** digested in one go. In a streaming environment you need a 'process' function that can hash
** parts of data and which produces the correct hash afterwards. This required major changes
** and the use of a SHA accumulator structure, w_SHA.
*/

#include <string.h>

#if defined(TEST) || defined(HEADER)
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

typedef unsigned int w_word;
typedef int w_int;
typedef unsigned char w_byte;
typedef struct w_SHA * w_sha;
typedef unsigned int w_size;
typedef signed long long w_long;

#ifdef DEBUG
#define woempa(level, format, a...)      {                                         \
  char wbuffer[256];                                                               \
  sprintf(wbuffer, "%15s %3d : "##format"\15", __FUNCTION__, __LINE__, ##a);       \
  write(0, wbuffer, strlen(wbuffer));                                              \
}
#else
#define woempa(level, format, a...)
#endif /* DEBUG */
#endif /* TEST */

#include "wstrings.h"
#include "sha.h"
#include "ts-mem.h"

w_sha allocSha() {

  w_sha sha = allocClearedMem(sizeof(w_SHA));
  
  if (!sha) {
    wabort(ABORT_WONKA, "Unable to allocate sha buffer\n");
  }
  woempa(1, "Alloced SHA %p, size %d bytes.\n", sha, sizeof(w_SHA));

  /*
  ** Seed the accumulators...
  */

  sha->signature.W[0] = 0x67452301;
  sha->signature.W[1] = 0xefcdab89;
  sha->signature.W[2] = 0x98badcfe;
  sha->signature.W[3] = 0x10325476;
  sha->signature.W[4] = 0xc3d2e1f0;

  return sha;
  
}

void releaseSha(w_sha sha) {

  woempa(1, "Releasing SHA %p.\n", sha);
  releaseMem(sha);
  
}

#if __BYTE_ORDER == __LITTLE_ENDIAN
void byteReverse(w_word *buffer, w_int byteCount ) {

  w_word value;
  w_int count;
  
  byteCount /= sizeof(w_word);
  for (count = 0; count < byteCount; count++) {
    value = (buffer[count] << 16) | (buffer[count] >> 16);
    buffer[count] = ((value & 0xFF00FF00L) >> 8) | ((value & 0x00FF00FFL) << 8);
  }

}
#else
#define byteReverse(b, c)
#endif

/*
** The MADGIC functions and constants that will be used in grokSha()
*/

#define f0(x, y, z)                   (z ^ (x & (y ^ z)))
#define f1(x, y, z)                   (x ^ y ^ z)
#define f2(x, y, z)                   ((x & y) | (z & (x | y)))
#define f3(x, y, z)                   (x ^ y ^ z)

#define K0 0x5a827999
#define K1 0x6ed9eba1
#define K2 0x8f1bbcdc
#define K3 0xca62c1d6

/*
** Barrel roll used in grokSha() macros...
*/

#define S(n, X)                     (((X) << (n)) | ((X) >> (32 - (n))))

#define r0(f, K)                                                       \
    temp = S(5, A) + f(B, C, D) + E + *p0++ + K;                       \
    E = D;                                                             \
    D = C;                                                             \
    C = S(30, B);                                                      \
    B = A;                                                             \
    A = temp

#define r1(f, K)                                                       \
    temp = *p1++ ^ *p2++ ^ *p3++ ^ *p4++;                              \
    temp = S(5, A) + f(B, C, D) + E + (*p0++ = S(1,temp)) + K;         \
    E = D;                                                             \
    D = C;                                                             \
    C = S(30, B);                                                      \
    B = A;                                                             \
    A = temp

/*
** Process a full w_SHA buffer for an intermediate result.
*/

void grokSha(w_sha sha) {

  w_word temp;
  w_word i;

  w_word *p0;
  w_word *p1;
  w_word *p2;
  w_word *p3;
  w_word *p4;

  w_word A = sha->signature.W[0]; 
  w_word B = sha->signature.W[1];
  w_word C = sha->signature.W[2];
  w_word D = sha->signature.W[3];
  w_word E = sha->signature.W[4];

  p0 = sha->buffer.W;

  /* NOTE: Replaced the whole bunch of define constructs with for loops. */
  /* This reduces code around 1000%, but since this is rather critical */
  /* stuff (Serialization), I left the old code in */

  for(i=0; i<16; i++) { r0(f0,K0); }
  
  /*
  r0(f0,K0); r0(f0,K0); r0(f0,K0); r0(f0,K0); r0(f0,K0);
  r0(f0,K0); r0(f0,K0); r0(f0,K0); r0(f0,K0); r0(f0,K0);
  r0(f0,K0); r0(f0,K0); r0(f0,K0); r0(f0,K0); r0(f0,K0);
  r0(f0,K0);
  */
  
  p1 = &sha->buffer.W[13]; 
  p2 = &sha->buffer.W[8]; 
  p3 = &sha->buffer.W[2]; 
  p4 = &sha->buffer.W[0];

  
  for(i=0; i<4; i++) { r1(f0,K0); }
  for(i=0; i<20; i++) { r1(f1,K1); }
  for(i=0; i<20; i++) { r1(f2,K2); }
  for(i=0; i<20; i++) { r1(f3,K3); }
  
/*
             r1(f0,K0); r1(f0,K0); r1(f0,K0); r1(f0,K0);
             
  r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1);
  r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1);
  r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1);
  r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1); r1(f1,K1);
  
  r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2);
  r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2);
  r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2);
  r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2); r1(f2,K2);
  
  r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3);
  r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3);
  r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3);
  r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3); r1(f3,K3);
*/
  
  sha->signature.W[0] += A; 
  sha->signature.W[1] += B; 
  sha->signature.W[2] += C; 
  sha->signature.W[3] += D; 
  sha->signature.W[4] += E;

}

/*
** Process a block of data to calculate the intermediate hash values and/or buffer
** the data in our accumulator when not enough is available for a full grokSha() round.
*/

void processSha(w_sha sha, w_byte *data, w_size length) {

    /*
    ** Adjust the length counts
    */
    
    if ((sha->lo_length += (length << 3)) < (w_size)(length << 3)) {
      sha->hi_length += 1;
    }
//    woempa(9, "Current lo_length: %d bits.\n", sha->lo_length);
    
    /*
    ** Process 16 longs at a time or buffer them up for the next time around...
    */
    
    while (length) {
      if (sha->buffered + length >= 64) {
//        woempa(9, "Have full buffer now, had %d bytes buffered from previous round.\n", sha->buffered);
        w_memcpy(&sha->buffer.B[sha->buffered], data, (w_size) (64 - sha->buffered));
        data += (64 - sha->buffered);
        length -= (64 - sha->buffered);
        byteReverse(sha->buffer.W, 64);
        grokSha(sha);
        sha->buffered = 0;
      }
      else {
        w_memcpy(&sha->buffer.B[sha->buffered], data, length);
        sha->buffered += length;
        length = 0;
//        woempa(9, "Buffer not full, only %d bytes buffered, waiting for more data or finish...\n", sha->buffered);
      }
    }

}

/*
** Finish of a SHA accumulator so that the correct signature appears in the
** sha->signature words.
*/

void finishSha(w_sha sha) {

  /*
  ** Pad the buffer, first append a single bit and then all 0's, we
  ** will overwrite the number of 0's for the length, if the buffer
  ** is filled too much to append the length, we'll perform an extra round.
  */
  
  sha->buffer.B[sha->buffered++] = 0x80;
  memset(&sha->buffer.B[sha->buffered], 0x00, (w_size) (64 - sha->buffered));

  /*
  ** Now see if we have to grokSha() only once (when we can get the bitlength of the message
  ** in the current buffer, without overflowing the buffer) or if we have to do
  ** it in two rounds of grokSha(), if the buffer space isn't large enough to accomodate the
  ** bitlength data, which requires 8 bytes.
  */

  if (sha->buffered <= 56) {
    woempa(9, "Only %d bytes used in buffer; one extra round...\n", sha->buffered);
    sha->buffer.W[14] = sha->hi_length;
    sha->buffer.W[15] = sha->lo_length;
    byteReverse(sha->buffer.W, 56);
    grokSha(sha);
  }
  else {
    woempa(9, "No room, %d bytes used, doing two extra rounds...\n", sha->buffered);
    byteReverse(sha->buffer.W, 64);
    grokSha(sha);
    memset(&sha->buffer.B, 0x00, 320);
    sha->buffer.W[14] = sha->hi_length;
    sha->buffer.W[15] = sha->lo_length;
    byteReverse(sha->buffer.W, 56);
    grokSha(sha);
  }

}

#ifdef TEST
int main(int argc, char *argv[]) {

  w_sha sha;
  w_int i;
  char as[1000];
  
  memset(as, 'a', 1000);
  
  sha = allocSha();
  processSha(sha, "abc", 3);
  finishSha(sha);
  woempa(1, "SIGNATURE: 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x\n", sha->signature.W[0], sha->signature.W[1], sha->signature.W[2], sha->signature.W[3], sha->signature.W[4]);
  woempa(1, "(%02x %02x %02x %02x) (%02x %02x %02x %02x) (%02x %02x %02x %02x) (%02x %02x %02x %02x) (%02x %02x %02x %02x)\n",
    sha->signature.B[0], sha->signature.B[1],sha->signature.B[2],sha->signature.B[3],sha->signature.B[4],sha->signature.B[5],sha->signature.B[6],
    sha->signature.B[7],sha->signature.B[8],sha->signature.B[9],sha->signature.B[10],sha->signature.B[11],sha->signature.B[12],sha->signature.B[13],
    sha->signature.B[14],sha->signature.B[15],sha->signature.B[16],sha->signature.B[17],sha->signature.B[18],sha->signature.B[19]);
  if (sha->signature.W[0] != 0xa9993e36 || sha->signature.W[1] != 0x4706816a || sha->signature.W[2] != 0xba3e2571 || sha->signature.W[3] != 0x7850c26c || sha->signature.W[4] != 0x9cd0d89d) {
    woempa(9, "Houston, we have a %s.\n", "problem");
    abort();
  }
  else {
    woempa(9, "Result is OK according to %s.\n", "NIST FIPS-180");
  }
  releaseSha(sha);

  sha = allocSha();
  processSha(sha, "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", (w_int)strlen("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"));
  finishSha(sha);
  woempa(1, "SIGNATURE: 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x\n", sha->signature.W[0], sha->signature.W[1], sha->signature.W[2], sha->signature.W[3], sha->signature.W[4]);
  if (sha->signature.W[0] != 0x84983e44 || sha->signature.W[1] != 0x1c3bd26e || sha->signature.W[2] != 0xbaae4aa1 || sha->signature.W[3] != 0xf95129e5 || sha->signature.W[4] != 0xe54670f1) {
    woempa(9, "Houston, we have a %s.\n", "problem");
    abort();
  }
  else {
    woempa(9, "Result is OK according to %s.\n", "NIST FIPS-180");
  }
  releaseSha(sha);

  sha = allocSha();
  processSha(sha, "abcdbcdecdefdefgefghfghigh", (w_int)strlen("abcdbcdecdefdefgefghfghigh"));
  processSha(sha, "ijhijkijkljklmklmnlmnomnopnopq", (w_int)strlen("ijhijkijkljklmklmnlmnomnopnopq"));
  finishSha(sha);
  woempa(1, "SIGNATURE: 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x\n", sha->signature.W[0], sha->signature.W[1], sha->signature.W[2], sha->signature.W[3], sha->signature.W[4]);
  if (sha->signature.W[0] != 0x84983e44 || sha->signature.W[1] != 0x1c3bd26e || sha->signature.W[2] != 0xbaae4aa1 || sha->signature.W[3] != 0xf95129e5 || sha->signature.W[4] != 0xe54670f1) {
    woempa(9, "Houston, we have a %s.\n", "problem");
    abort();
  }
  else {
    woempa(9, "Result is OK according to %s.\n", "NIST FIPS-180");
  }
  releaseSha(sha);

  sha = allocSha();
  for (i = 0; i < 1000; i++) {
    processSha(sha, as, 1000);
  }
  finishSha(sha);
  woempa(1, "SIGNATURE: 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x\n", sha->signature.W[0], sha->signature.W[1], sha->signature.W[2], sha->signature.W[3], sha->signature.W[4]);
  if (sha->signature.W[0] != 0x34aa973c || sha->signature.W[1] != 0xd4c4daa4 || sha->signature.W[2] != 0xf61eeb2b || sha->signature.W[3] != 0xdbad2731 || sha->signature.W[4] != 0x6534016f) {
    woempa(9, "Houston, we have a %s.\n", "problem");
    abort();
  }
  else {
    woempa(9, "Result is OK according to %s.\n", "NIST FIPS-180");
  }
  releaseSha(sha);

  sha = allocSha();
  for (i = 0; i < 10000; i++) {
    processSha(sha, as, 100);
  }
  finishSha(sha);
  woempa(1, "SIGNATURE: 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x\n", sha->signature.W[0], sha->signature.W[1], sha->signature.W[2], sha->signature.W[3], sha->signature.W[4]);
  if (sha->signature.W[0] != 0x34aa973c || sha->signature.W[1] != 0xd4c4daa4 || sha->signature.W[2] != 0xf61eeb2b || sha->signature.W[3] != 0xdbad2731 || sha->signature.W[4] != 0x6534016f) {
    woempa(9, "Houston, we have a %s.\n", "problem");
    abort();
  }
  else {
    woempa(9, "Result is OK according to %s.\n", "NIST FIPS-180");
  }
  releaseSha(sha);

  return 0;
  
}
#endif
