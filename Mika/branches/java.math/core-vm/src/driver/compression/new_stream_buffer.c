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

#include "new_deflate_internals.h"

/* ------------------------------------------------------------------------------------------------------- */
// HELPERS
/* ------------------------------------------------------------------------------------------------------- */

/*
** TODO : Fix errorhandling
**        Check mem allocations
**        Improve speed
*/

/*
** An array that relates the symbol values for the dynamic huffman table reconstruction
** against the code length in bits that the symbol value requires.
** In other words, when we read in bit_order[0], we get the code length in bits
** for the alphabet element with symbol value 16. It is used in buildDynamicDictionary.
*/

const w_byte new_bit_order[] = {
  16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
};

/*
** Tables that relate the extra number of bits to read in and the base value against
** which the extra bits interpreted as integers have to be added.
*/

/*
** length code = 257 + index
**
** bits base
*/
const z_Extra new_length_table[] = {
  { 0,   3 },
  { 0,   4 },
  { 0,   5 },
  { 0,   6 },
  { 0,   7 },
  { 0,   8 },
  { 0,   9 },
  { 0,  10 },
  { 1,  11 },
  { 1,  13 },
  { 1,  15 },
  { 1,  17 },
  { 2,  19 },
  { 2,  23 },
  { 2,  27 },
  { 2,  31 },
  { 3,  35 },
  { 3,  43 },
  { 3,  51 },
  { 3,  59 },
  { 4,  67 },
  { 4,  83 },
  { 4,  99 },
  { 4, 115 },
  { 5, 131 },
  { 5, 163 },
  { 5, 195 },
  { 5, 227 },
  { 0, 258 }
};

/*
** A quick lookup table for reversing the bits sequence in a byte.
*/
const unsigned short new_bitReverse[] = {
    0x00, 0x80, 0x40, 0xc0, 0x20, 0xa0, 0x60, 0xe0,
    0x10, 0x90, 0x50, 0xd0, 0x30, 0xb0, 0x70, 0xf0,
    0x08, 0x88, 0x48, 0xc8, 0x28, 0xa8, 0x68, 0xe8,
    0x18, 0x98, 0x58, 0xd8, 0x38, 0xb8, 0x78, 0xf8,
    0x04, 0x84, 0x44, 0xc4, 0x24, 0xa4, 0x64, 0xe4,
    0x14, 0x94, 0x54, 0xd4, 0x34, 0xb4, 0x74, 0xf4,
    0x0c, 0x8c, 0x4c, 0xcc, 0x2c, 0xac, 0x6c, 0xec,
    0x1c, 0x9c, 0x5c, 0xdc, 0x3c, 0xbc, 0x7c, 0xfc,
    0x02, 0x82, 0x42, 0xc2, 0x22, 0xa2, 0x62, 0xe2,
    0x12, 0x92, 0x52, 0xd2, 0x32, 0xb2, 0x72, 0xf2,
    0x0a, 0x8a, 0x4a, 0xca, 0x2a, 0xaa, 0x6a, 0xea,
    0x1a, 0x9a, 0x5a, 0xda, 0x3a, 0xba, 0x7a, 0xfa,
    0x06, 0x86, 0x46, 0xc6, 0x26, 0xa6, 0x66, 0xe6,
    0x16, 0x96, 0x56, 0xd6, 0x36, 0xb6, 0x76, 0xf6,
    0x0e, 0x8e, 0x4e, 0xce, 0x2e, 0xae, 0x6e, 0xee,
    0x1e, 0x9e, 0x5e, 0xde, 0x3e, 0xbe, 0x7e, 0xfe,
    0x01, 0x81, 0x41, 0xc1, 0x21, 0xa1, 0x61, 0xe1,
    0x11, 0x91, 0x51, 0xd1, 0x31, 0xb1, 0x71, 0xf1,
    0x09, 0x89, 0x49, 0xc9, 0x29, 0xa9, 0x69, 0xe9,
    0x19, 0x99, 0x59, 0xd9, 0x39, 0xb9, 0x79, 0xf9,
    0x05, 0x85, 0x45, 0xc5, 0x25, 0xa5, 0x65, 0xe5,
    0x15, 0x95, 0x55, 0xd5, 0x35, 0xb5, 0x75, 0xf5,
    0x0d, 0x8d, 0x4d, 0xcd, 0x2d, 0xad, 0x6d, 0xed,
    0x1d, 0x9d, 0x5d, 0xdd, 0x3d, 0xbd, 0x7d, 0xfd,
    0x03, 0x83, 0x43, 0xc3, 0x23, 0xa3, 0x63, 0xe3,
    0x13, 0x93, 0x53, 0xd3, 0x33, 0xb3, 0x73, 0xf3,
    0x0b, 0x8b, 0x4b, 0xcb, 0x2b, 0xab, 0x6b, 0xeb,
    0x1b, 0x9b, 0x5b, 0xdb, 0x3b, 0xbb, 0x7b, 0xfb,
    0x07, 0x87, 0x47, 0xc7, 0x27, 0xa7, 0x67, 0xe7,
    0x17, 0x97, 0x57, 0xd7, 0x37, 0xb7, 0x77, 0xf7,
    0x0f, 0x8f, 0x4f, 0xcf, 0x2f, 0xaf, 0x6f, 0xef,
    0x1f, 0x9f, 0x5f, 0xdf, 0x3f, 0xbf, 0x7f, 0xff
};

/*
** distance code = index
**
**  bits  base
*/
const z_Extra new_distance_table[] = {
  {  0,     1 },
  {  0,     2 },
  {  0,     3 },
  {  0,     4 },
  {  1,     5 },
  {  1,     7 },
  {  2,     9 },
  {  2,    13 },
  {  3,    17 },
  {  3,    25 },
  {  4,    33 },
  {  4,    49 },
  {  5,    65 },
  {  5,    97 },
  {  6,   129 },
  {  6,   193 },
  {  7,   257 },
  {  7,   385 },
  {  8,   513 },
  {  8,   769 },
  {  9,  1025 },
  {  9,  1537 },
  { 10,  2049 },
  { 10,  3073 },
  { 11,  4097 },
  { 11,  6145 },
  { 12,  8193 },
  { 12, 12289 },
  { 13, 16385 },
  { 13, 24577 }
};


/*
** This fucntion gets a new block from the input queue, and frees the previous block
** if one
** If nomoreinput is set, we don't block anymore on the queue
*/
w_void new_getNewBlock(w_inflate_control l) {
  // free old partial data
  if (l->par_in != NULL) {
    woempa(1, "-------Freeing.\n");

    if (l->par_in->data != NULL) releaseMem(l->par_in->data);

    releaseMem(l->par_in);
    l->par_in = NULL;
    l->offset_in = 0;

    // previous block was a dictionary, so set dictionary to 0
    if (l->dictionary) l->dictionary = 0;
  }
  else {
    woempa(1, "-------Not Freeing.\n");
  }

  if(l->first_link_in) {
    w_deflate_queueelem qe = l->first_link_in;
    l->first_link_in = qe->next;
    if(qe->next == NULL) {
      l->last_link_in = NULL;
    }
    l->need_more_input = 0;
    l->par_in = qe;
  } else {
    l->need_more_input = !l->nomoreinput;
  }
}

/*
** The following call can only be done on a byte aligned stream !
*/
w_byte new_readLiteralByte(w_inflate_control bs) {
  w_byte dd;
  
  if (bs->offset_in == 0 || bs->offset_in >= bs->par_in->size) {
    new_getNewBlock(bs);
    
    // we had ends, just return 0
    if (bs->par_in == NULL) return 0;
    
    bs->offset_in = 0;
  }

  dd = bs->par_in->data[bs->offset_in];
  bs->offset_in += 1;

  bs->processed_size +=1;
    
  return dd;
}

w_void new_readByteAlign(w_inflate_control bs) {
  bs->i_bits = 0x01;
}

/*
** Sends an error message on the output queue
*/
w_void new_errorFlush(w_inflate_control bs) {
  bs->offset_bek_out = 0;
  bs->size_bek_out = 0;
  bs->finished = 1;
}

/*
** Send all available data we still have and then send an ENDS message
*/
w_void new_bekkenFlush(w_inflate_control bs) {
  w_deflate_queueelem qe = allocMem(sizeof(w_Deflate_QueueElem));
  if (!qe) {
    wabort(ABORT_WONKA, "Unable to allocate qe\n");
  }

  woempa(1, "-=-- Flush bekken.\n");

  qe->data = allocMem((unsigned)bs->size_bek_out);
  if (!qe->data) {
    wabort(ABORT_WONKA, "Unable to allocate qe->data\n");
  }
  qe->size = bs->size_bek_out;
  qe->index=0;
  qe->release = 1;
  qe->next = NULL;
  woempa(1, "Block size %i and offset %i\n", bs->size_bek_out, bs->offset_bek_out);

  if (bs->offset_bek_out - bs->size_bek_out >= 0) {
    w_memcpy(qe->data, bs->output_bekken + (bs->offset_bek_out - bs->size_bek_out), (unsigned)bs->size_bek_out);
  } else {
    //  --------------------------
    //  |      |            | |  |
    //  --------------------------
    //  0      offset       ^ ^  33*1024
    //                      | |
    //                      | 1024+offset
    //               33*1024+offset-size
    //
    // beginnen bij 33*1024 + (offset - size)
    // eindigen bij 33*1024
    // dus lengte size - offset
    w_memcpy(qe->data, bs->output_bekken + 33 * 1024 + bs->offset_bek_out - bs->size_bek_out, (unsigned)(bs->size_bek_out - bs->offset_bek_out));
    w_memcpy(qe->data + (bs->size_bek_out - bs->offset_bek_out), bs->output_bekken, (unsigned)bs->offset_bek_out);
  }
  
  if(bs->last_link_out) {
    bs->last_link_out->next = qe;
  } else {
    bs->first_link_out = qe;
  }
  bs->last_link_out = qe;

  bs->finished = 1;
  bs->offset_bek_out = 0;
  bs->size_bek_out = 0;
}

/*
** This is called when the output block is full, and sends it to the output queue
*/
w_void new_bekkenSendBlock(w_inflate_control bs) {
  w_deflate_queueelem qe = allocMem(sizeof(w_Deflate_QueueElem));
  if (!qe) {
    wabort(ABORT_WONKA, "Unable to allocate qe\n");
  }

  woempa(1, "-=-- Flush send block.\n");

  qe->data = allocMem(1024);
  if (!qe->data) {
    wabort(ABORT_WONKA, "Unable to allocate qe->data\n");
  }
  qe->size = 1024;
  qe->index = 0;
  qe->release = 1;
  qe->next = NULL;
  woempa(1, "Block size %i and offset %i\n", bs->size_bek_out, bs->offset_bek_out);
  if (bs->offset_bek_out + 1024 <= 33*1024) {
    w_memcpy(qe->data, bs->output_bekken + bs->offset_bek_out, 1024);
  } 
  else {
    w_memcpy(qe->data, bs->output_bekken + bs->offset_bek_out, (unsigned)(33 * 1024 - bs->offset_bek_out));
    w_memcpy(qe->data + (33 * 1024 - bs->offset_bek_out), bs->output_bekken, (unsigned)(bs->offset_bek_out - 32 * 1024));
  }

  if(bs->last_link_out) {
    bs->last_link_out->next = qe;
  } else {
    bs->first_link_out = qe;
  }
  bs->last_link_out = qe;
  bs->size_bek_out -= 1024;
}

/*
** Byte-aligns the output stream, and thus flushes the remaining bits, padding with zero's
**
*/
w_void new_writeByteAlign(w_inflate_control bs) {
  if (bs->o_mask != 0x1) {
    new_writeLiteralByte(bs, bs->o_bits);
    
    bs->o_bits = 0;
    bs->o_mask = 0x1;
  }
}
