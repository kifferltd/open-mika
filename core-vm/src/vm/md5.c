/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

/*
** md5 signatures, written from the spec (rfc1321)
**
** Written May 21-22, 1998 by orc@pell.chi.il.us (david parsons)
**
** This code is released into the public domain.
**
** $Id: md5.c,v 1.2 2004/11/18 22:56:54 cvs Exp $
**
** Steven hacked the public domain version very heavily for use in Wonka.
** 
*/

#include <string.h>

#include "ts-mem.h"
#include "md5.h"

#define a         sums[0]
#define b         sums[1]
#define c         sums[2]
#define d         sums[3]


#ifdef BAR
struct md5buffer {
    w_word sums[4];

#define a	sums[0]
#define b	sums[1]
#define c	sums[2]
#define d	sums[3]

    unsigned char bytes[64];		/* text we're processing */
    int len;				/* how many bytes we've clocked into the */
					/* bytes[] array */
    long total;				/* how many bytes in this message? */
} ;

typedef struct md5buffer *JH;
#endif



/*
 * md5_init() -- the easy part of generating an md5 signature
 */
w_md5Acc allocMD5Acc(w_thread thread) {

w_md5Acc target = (w_md5Acc)allocMem(sizeof(w_MD5Acc));

  if (target) {
    target->thread = thread;
    target->a = 0x67452301;	/* seed the four accumulators */
    target->b = 0xefcdab89;	/* in intel byte order */
    target->c = 0x98badcfe;
    target->d = 0x10325476;

				/* zero out the buffer */
    memset(target->bytes, 0, sizeof target->bytes);
    target->len = 0;	/* with nothing in it */
    target->total = 0;
  }

  return target;

}

static w_word T[64] = {
    0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee, 0xf57c0faf, 0x4787c62a,
    0xa8304613, 0xfd469501, 0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
    0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821, 0xf61e2562, 0xc040b340,
    0x265e5a51, 0xe9b6c7aa, 0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
    0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed, 0xa9e3e905, 0xfcefa3f8,
    0x676f02d9, 0x8d2a4c8a, 0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
    0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70, 0x289b7ec6, 0xeaa127fa,
    0xd4ef3085, 0x04881d05, 0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
    0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039, 0x655b59c3, 0x8f0ccc92,
    0xffeff47d, 0x85845dd1, 0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
    0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391,
} ;

/*
 * md5_munch() encodes 512 bits (==64 bytes) into the ongoing md5
 * signature.
 */
static void
md5_munch(w_md5Acc target)
{
    w_word aa = target->a,
	  bb = target->b,
	  cc = target->c,
	  dd = target->d;

    w_word coder[16];
    int ix;

    for (ix = 0; ix < 16; ix++)
	coder[ix] = target->bytes[ix*4] + (target->bytes[1+(ix*4)] << 8)
					+ (target->bytes[2+(ix*4)] << 16)
					+ (target->bytes[3+(ix*4)] << 24);

#define BARREL(val,s)	( (val << s) | (val >> (32-s)) )

    /* gross iteration time: round 1 */
#define F(x,y,z)	((x & y) | (z & ~x))
#define R1(A,B,C,D,k,s,i)	A += F(B,C,D) + coder[k] + T[i-1]; \
				A = B + BARREL(A, s)

    R1(aa,bb,cc,dd,0,7,1);	R1(dd,aa,bb,cc,1,12,2);
    R1(cc,dd,aa,bb,2,17,3);	R1(bb,cc,dd,aa,3,22,4);
    R1(aa,bb,cc,dd,4,7,5);	R1(dd,aa,bb,cc,5,12,6);
    R1(cc,dd,aa,bb,6,17,7);	R1(bb,cc,dd,aa,7,22,8);
    R1(aa,bb,cc,dd,8,7,9);	R1(dd,aa,bb,cc,9,12,10);
    R1(cc,dd,aa,bb,10,17,11);	R1(bb,cc,dd,aa,11,22,12);
    R1(aa,bb,cc,dd,12,7,13);	R1(dd,aa,bb,cc,13,12,14);
    R1(cc,dd,aa,bb,14,17,15);	R1(bb,cc,dd,aa,15,22,16);

    /* round 2 */
#define	G(x,y,z)	((x & z) | (y & ~z))
#define R2(A,B,C,D,k,s,i)	A += G(B,C,D) + coder[k] + T[i-1]; \
				A = B + BARREL(A, s)

    R2(aa,bb,cc,dd,1,5,17);	R2(dd,aa,bb,cc,6,9,18);
    R2(cc,dd,aa,bb,11,14,19);	R2(bb,cc,dd,aa,0,20,20);
    R2(aa,bb,cc,dd,5,5,21);	R2(dd,aa,bb,cc,10,9,22);
    R2(cc,dd,aa,bb,15,14,23);	R2(bb,cc,dd,aa,4,20,24);
    R2(aa,bb,cc,dd,9,5,25);	R2(dd,aa,bb,cc,14,9,26);
    R2(cc,dd,aa,bb,3,14,27);	R2(bb,cc,dd,aa,8,20,28);
    R2(aa,bb,cc,dd,13,5,29);	R2(dd,aa,bb,cc,2,9,30);
    R2(cc,dd,aa,bb,7,14,31);	R2(bb,cc,dd,aa,12,20,32);

     /* Round 3. */
#define	H(x,y,z)	(x ^ y ^ z)
#define R3(a,b,c,d,k,s,t)	a += H(b,c,d) + coder[k] + T[t-1]; \
				a = b + BARREL(a, s)

    R3(aa,bb,cc,dd,5,4,33);	R3(dd,aa,bb,cc,8,11,34);
    R3(cc,dd,aa,bb,11,16,35);	R3(bb,cc,dd,aa,14,23,36);
    R3(aa,bb,cc,dd,1,4,37);	R3(dd,aa,bb,cc,4,11,38);
    R3(cc,dd,aa,bb,7,16,39);	R3(bb,cc,dd,aa,10,23,40);
    R3(aa,bb,cc,dd,13,4,41);	R3(dd,aa,bb,cc,0,11,42);
    R3(cc,dd,aa,bb,3,16,43);	R3(bb,cc,dd,aa,6,23,44);
    R3(aa,bb,cc,dd,9,4,45);	R3(dd,aa,bb,cc,12,11,46);
    R3(cc,dd,aa,bb,15,16,47);	R3(bb,cc,dd,aa,2,23,48);

     /* Round 4. */
#define	I(x,y,z)	(y ^ (x | ~z))
#define R4(a,b,c,d,k,s,t)	a += I(b,c,d) + coder[k] + T[t-1]; \
				a = b + BARREL(a, s)

    R4(aa,bb,cc,dd,0,6,49);	R4(dd,aa,bb,cc,7,10,50);
    R4(cc,dd,aa,bb,14,15,51);	R4(bb,cc,dd,aa,5,21,52);
    R4(aa,bb,cc,dd,12,6,53);	R4(dd,aa,bb,cc,3,10,54);
    R4(cc,dd,aa,bb,10,15,55);	R4(bb,cc,dd,aa,1,21,56);
    R4(aa,bb,cc,dd,8,6,57);	R4(dd,aa,bb,cc,15,10,58);
    R4(cc,dd,aa,bb,6,15,59);	R4(bb,cc,dd,aa,13,21,60);
    R4(aa,bb,cc,dd,4,6,61);	R4(dd,aa,bb,cc,11,10,62);
    R4(cc,dd,aa,bb,2,15,63);	R4(bb,cc,dd,aa,9,21,64);

    target->a += aa;
    target->b += bb;
    target->c += cc;
    target->d += dd;
}

void processMD5Acc(w_md5Acc target, w_byte *buf, w_size len) {

    w_size ix;

    for (ix = 0; ix < len; ix++) {
	/* clock bytes into the buffer nice and slowly, one by one
	 */
	target->bytes[target->len++] = *buf++;

	/* then process away when we've got enough
	 */
	if (target->len >= 64) {
	    md5_munch(target);
	    target->len = 0;
	    memset(target->bytes, 0, sizeof target->bytes);
	}
    }
    target->total += (len * 8);
}


void finishMD5Acc(w_md5Acc target, w_word signature[]) {

    static unsigned char pad[64] = { 0x80 };	/* rest filled with 0x00... */
    w_size siz;
    unsigned char total[8];
    unsigned char *sig = (unsigned char *)signature;
    int ix;

    /* pad out to the end of this block, then spit out the accumulators. */

    if (target->len != 56) {	/* need to pad out to 56 bytes, so we
				 * can then add the length (8 bytes) in
				 * and end up with 512 bits.
				 */
	if (target->len > 56)
	    siz = (64 - target->len) + 56;
	else
	    siz = 56 - target->len;

	memset(total, 0, 8);
	total[0] = target->total;
	total[1] = (target->total >> 8);
	total[2] = (target->total >> 16);
	total[3] = (target->total >> 24);

	memset(pad+1, 0, (sizeof pad)-1);
	processMD5Acc(target, pad, siz);
	processMD5Acc(target, total, 8);

	wassert(target->len == 0);	/* a little bit of paranoia */
    }
    for (ix = 0; ix < 4; ix++) {
	sig[   ix*4 ] = target->sums[ix];
	sig[1+(ix*4)] = target->sums[ix] >> 8;
	sig[2+(ix*4)] = target->sums[ix] >> 16;
	sig[3+(ix*4)] = target->sums[ix] >> 24;
    }
#ifdef CRYPTO_CAN_ROT_YOUR_BRAIN
    memset(target, 0, sizeof *target);
#endif

}
