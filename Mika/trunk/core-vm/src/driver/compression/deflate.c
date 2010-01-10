/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
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

#include "deflate_internals.h"

#define INF_WOEMP_LEV_1  2

/*
** An array that relates the symbol values for the dynamic huffman table reconstruction
** against the code length in bits that the symbol value requires.
** In other words, when we read in bit_order[0], we get the code length in bits
** for the alphabet element with symbol value 16. It is used in buildDynamicDictionary.
*/

const w_byte bit_order[] = {
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
const z_Extra length_table[] = {
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
** distance code = index
**
**  bits  base
*/
const z_Extra distance_table[] = {
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
** A quick lookup table for reversing the bits sequence in a byte.
*/
const unsigned short bitReverse[] = {
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

typedef struct w_Zip_Huff {
  w_short code;
  w_short code_length;
  w_short next;
} w_Zip_Huff;
typedef struct w_Zip_Huff *w_zip_huff;

typedef struct w_Zip_Huff_Init {
  w_short freq;               // we work in blocks of 32k, so this is ok
  w_short head;
  w_short tail;
} w_Zip_Huff_Init;
typedef struct w_Zip_Huff_Init *w_zip_huff_init;

/*
** Deflating and Storing
*/


/*
** Memlevel defines memory usage (this is taken from zlib, there default is 8)
** 1 = 256 hash entries
** ..
** 5 = 4096
** ..
** 9 = 65536 hash entries
*/
#define MEM_LEVEL 5
#define HASH_BITS (MEM_LEVEL + 7)
#define HASH_LENGTH (1 << HASH_BITS)
#define HASH_MASK (HASH_LENGTH - 1)
#define HASH_SHIFT ((HASH_BITS + 3 - 1) / 3)

static inline w_word running_hash_function(w_word h, w_int c) {
  return (((h) << HASH_SHIFT) ^ (c)) & HASH_MASK;
}

// find if the given hash entry is a match and returns its length
static inline w_short find_longest_match(w_deflate_control l, w_int entry) {
  w_int end, othis;
  w_ubyte *this, *that;

  // never go past input and past max_count
  end = 258;
  if (end > (l->offset_bek_in - l->lookahead_bek_in)) end = l->offset_bek_in - l->lookahead_bek_in;
  
  // entry = index of hashentry
  this = (*l).input_bekken + entry;
  that = (*l).input_bekken + l->lookahead_bek_in;
  othis = (w_int)this;  
  
  end = end + (w_int)this;

  while ((w_int)this < end && *this == *that) {
    this++;
    that++;
  }

  return ((w_int)this - othis);
}

/*
** Fixup the huffman so that no codelengths more than the given length occur
*/
static void fixHuff(w_int i, w_short *length_counts, w_zip_huff huff_table, w_int nr_symb) {
  w_int j, k;
  
  while (length_counts[i] != 0) {
    j = i - 2;
    while (length_counts[j] == 0 && j >= 0) j -= 1;

    length_counts[i] -= 2;
    length_counts[i - 1] += 1;
    length_counts[j + 1] += 2;
    length_counts[j] -= 1;

    k = 0;
    while (huff_table[k].code_length != i && k < nr_symb) k++;
    huff_table[k].code_length = i - 1;

    k = 0;
    while (huff_table[k].code_length != i && k < nr_symb) k++;
    huff_table[k].code_length = i - 1;

    k = 0;
    while (huff_table[k].code_length != (i - 1) && k < nr_symb) k++;
    huff_table[k].code_length = j + 1;

    k = nr_symb - 1;
    while (huff_table[k].code_length != j && k >= 0) k--;
    huff_table[k].code_length = j + 1;
  }
}

/*
** creates huffman table (code for each symbol)
**
** arguments: nr of symbols, array with frequencies
** returns: an allocated huffman table
*/
w_zip_huff createHuff(w_int nr_symb, w_short *freq, w_int *length) {
  w_int i, j, k, code;
  w_zip_huff huff_table;
  w_zip_huff_init huff_table_init;
  w_short *length_counts, *start_code; 
  w_int bi[2], bf[2], last_symb;

  // compute length of codes
  huff_table = allocMem(sizeof(w_Zip_Huff) * nr_symb);
  if (!huff_table) {
    return NULL;
  }
  
  huff_table_init = allocMem(sizeof(w_Zip_Huff_Init) * nr_symb);
  if (!huff_table_init) {
    releaseMem(huff_table);
    return NULL;
  }

  /*
  ** TODO : This below should be rewritten using something like in zlib
  */  
  k = 0;      
  for (i = 0; i < nr_symb; i++) {
      huff_table[i].next = -1;
      huff_table[i].code_length = 0;

    if (freq[i] != 0) {
      huff_table_init[k].freq = freq[i];
      huff_table_init[k].head = i;
      huff_table_init[k].tail = i;

      k++;
    }
  }

  // TODO : what if 0 ?????    
  if (k == 1){
    // pathological cases
    huff_table[huff_table_init[0].head].code_length = 1;
  }
  else {
    while (k > 1) {
      bf[0] = bf[1] = 4000000;
      bi[0] = bi[1] = 0;
      j = 0;
      // find smallest two entries
      for (i = 0; i < k; i++) {
        if (huff_table_init[i].freq < bf[0]) {
          if (huff_table_init[i].freq > bf[1]) {
            bf[0] = huff_table_init[i].freq;
            bi[0] = i;
          }
          else {
            if (bf[0] < bf[1]) {
              bf[1] = huff_table_init[i].freq;
              bi[1] = i;
            }
            else {
              bf[0] = huff_table_init[i].freq;
              bi[0] = i;
            }
          }
        }
        else {
          if (huff_table_init[i].freq < bf[1]) {
            bf[1] = huff_table_init[i].freq;
            bi[1] = i;
          }
        }
      }
      huff_table[huff_table_init[bi[0]].tail].next = huff_table_init[bi[1]].head;
      huff_table_init[bi[0]].tail = huff_table_init[bi[1]].tail;
      huff_table_init[bi[0]].freq += huff_table_init[bi[1]].freq;

      k--;

      huff_table_init[bi[1]].freq = huff_table_init[k].freq;
      huff_table_init[bi[1]].head = huff_table_init[k].head;
      huff_table_init[bi[1]].tail = huff_table_init[k].tail;

      i = huff_table_init[bi[0]].head;
      while (i != -1) {
        huff_table[i].code_length += 1;
        i = huff_table[i].next;
      }

    }
  }

  woempa(2, "Code lengths\n");
  for (i = 0; i < nr_symb; i += 1) woempa(2, "symbol %i code length %i\n", i, huff_table[i].code_length);

  // count the lengths
  length_counts = allocMem(sizeof(w_short) * 32);
  if (!length_counts) {
    releaseMem(huff_table_init);
    releaseMem(huff_table);
    return NULL;
  }
  
  memset(length_counts, 0, sizeof(w_short) * 32);
  for (i = 0; i < nr_symb; i += 1) {
    k = huff_table[i].code_length;
    length_counts[k] += 1;
  }
  length_counts[0] = 0;   // just forget the 0 lengths

  // limit length to 15 of 7 (3 bits for table huff) using the length counts 
  // TODO : according to zlib, can only happen with length 7, check this, and if so, remove case
  if (nr_symb == 19) {
    for (i = 32 - 1; i >= 8; i -= 1) {
      fixHuff(i, length_counts, huff_table, nr_symb);
    }
  }
  else {
    for (i = 32 - 1; i >= 16; i -= 1) {
      fixHuff(i, length_counts, huff_table, nr_symb);
    }
  }

  woempa(2, "Length counts\n");
  for (i = 0; i < 16; i += 1) woempa(2, "length %i has count %i\n", i, length_counts[i]);

  /*
  ** Step [2]
  **
  ** Find the numerical value of the smallest code for each code length.
  */
  start_code = allocMem(sizeof(w_short) * (MAX_BITS + 1));
  if (!start_code) {
    releaseMem(length_counts);
    releaseMem(huff_table_init);
    releaseMem(huff_table);
    return NULL;
  }

  code = 0;
  length_counts[0] = 0;
  for (i = 1; i < MAX_BITS + 1; i++) {
    code = (code + length_counts[i - 1]) << 1;
    start_code[i] = code;
  }

  /*
  ** Step [3]
  **
  ** Assign numerical values to all codes that have non-zero bit lengths, 
  ** using consecutive values for these codes of the same code length, with 
  ** the numerical base values determined in the previous loop. 
  */
  last_symb = -1;
  for (i = 0; i < nr_symb; i += 1) {
    if (huff_table[i].code_length != 0) {
      huff_table[i].code = start_code[huff_table[i].code_length];
      start_code[huff_table[i].code_length] += 1;
      last_symb = i;
    }
  }

  woempa(2, "The actual codes\n");
  for (i = 0; i < nr_symb; i += 1) {
    woempa(2, "symbol %i code ", i);
    for (j = huff_table[i].code_length - 1; j >= 0; j -= 1) {
      woempa(2, "%i", (huff_table[i].code & (1 << j)) != 0);
    }
    woempa(2, "\n");
  }

  releaseMem(huff_table_init);
  releaseMem(start_code);
  releaseMem(length_counts);

  *length = last_symb + 1;

  return huff_table;
}

/*
** analyse lengths and literals
** analyse distances
*/
static inline w_void freqAnal(w_ubyte *data, w_short *count, w_int data_amount) {
  w_int i;

  memset(count, 0x0, 330 * 2);
  for (i = 0; i < data_amount; i += 2) {
    if (data[i + 1] == 255) {
      count[data[i]] += 1;
    } 
    else {
      count[data[i] + 256] += 1;

      if (data[i] != 0) count[300 + data[i + 1]] += 1;
    }
  }
}

/*
** Write out a number of bits given by count, and max sizeof(w_short)
*/
static w_int writeBits(w_deflate_control bs, w_word obyte, w_int count) {
  w_word mask;

  mask = 1;
  while (mask != (unsigned)(1 << count)) {
    if (mask & obyte) bs->o_bits |= bs->o_mask;
    bs->o_mask <<= 1;
    if ((bs->o_mask & 0xff) == 0) {
      writeLiteralByte(bs, bs->o_bits);
    
      bs->o_bits = 0;
      bs->o_mask = 0x01;
    }
    mask <<= 1;
  }
  return 0;  
}

/*
** Write out a number of bits given by count, and max sizeof(w_short), but in inverse order
*/
static w_int writeBitsInverse(w_deflate_control bs, w_word obyte, w_int count) {
  w_word mask;

  mask = 1 << (count - 1);
  while (mask != 0) {
    if (mask & obyte) bs->o_bits |= bs->o_mask;
    bs->o_mask <<= 1;
    if ((bs->o_mask & 0xff) == 0) {
      writeLiteralByte(bs, bs->o_bits);
    
      bs->o_bits = 0;
      bs->o_mask = 0x01;
    }
    mask >>= 1;
  }
  return 0;  
}

static w_int writeSingleBit(w_deflate_control bs, w_word obyte) {
  if (obyte) bs->o_bits |= bs->o_mask;
  bs->o_mask <<= 1;
  if ((bs->o_mask & 0xff) == 0) {
    writeLiteralByte(bs, bs->o_bits);
    
    bs->o_bits = 0;
    bs->o_mask = 0x1;
  }
  return 0;  
}

/*
** take all info we have, do freq analysis, create huffman codes, and writes
** everything out
*/
w_int encode(w_deflate_control l, w_ubyte *data, w_short *data_extra, w_int data_amount, w_int last_block) {
  w_zip_huff dict_lit, dict_dist, dict_huff;
  w_int i, lit_length, dist_length, huff_length;

  freqAnal(data, l->zip_hash_table, data_amount);

  dict_lit = createHuff(286, l->zip_hash_table, &lit_length);
  if (!dict_lit) {
    return 0;
  }

  if (lit_length < 257 ) {
    // ajaj, we did not include the end-of-block marker
    releaseMem(dict_lit);
    return 0;
  }

  dict_dist = createHuff(30, l->zip_hash_table + 300, &dist_length);      
  if (!dict_dist) {
    releaseMem(dict_lit);
    return 0;
  }
  
  // no distance code, so make lenght 1 and set first to 0
  if (dist_length == 0) {
    dist_length++;
  }

  // TODO : we should run length encode the tables !

  // calculate frequencies of huffman tables
  memset(l->zip_hash_table, 0x0, 19 * 2);
  for (i = 0; i < lit_length; i += 1) {
    l->zip_hash_table[dict_lit[i].code_length] += 1;
  }
  for (i = 0; i < dist_length; i += 1) {
    l->zip_hash_table[dict_dist[i].code_length] += 1;
  }

//  printf("----------Huffman for the length tables\n");
  dict_huff = createHuff(19, l->zip_hash_table, &huff_length);
  if (!dict_huff) {
    releaseMem(dict_dist);
    releaseMem(dict_lit);
    return 0;
  }

  // TODO : if i remove this, decompression crashes because we don't store in normal
  // order, we need to walk through the bit_order table to determine the last symbol
  huff_length = 19;

  // write header
  // last-block
  writeSingleBit(l, (unsigned)last_block);
  // type (dynamic)
  writeBits(l, 2, 2);      

  // write out tables

  // nr of literal/length codes - 257
  writeBits(l, (unsigned)(lit_length - 257), 5);
  // nr of distance codes - 1
  writeBits(l, (unsigned)(dist_length - 1), 5);
  // nr of code-length codes - 4
  writeBits(l, (unsigned)(huff_length - 4), 4);

  for (i = 0; i < huff_length; i += 1) 
    writeBits(l, (unsigned)dict_huff[bit_order[i]].code_length, 3);

  for (i = 0; i < lit_length; i += 1) 
    writeBitsInverse(l, (unsigned)dict_huff[dict_lit[i].code_length].code, dict_huff[dict_lit[i].code_length].code_length);

  for (i = 0; i < dist_length; i += 1) 
    writeBitsInverse(l, (unsigned)dict_huff[dict_dist[i].code_length].code, dict_huff[dict_dist[i].code_length].code_length);

  // encode the data and interleave the extra bits

  for (i = 0; i < data_amount; i += 2) {
    if (data[i + 1] == 255) {
      // a literal
      writeBitsInverse(l, (unsigned)dict_lit[data[i]].code, dict_lit[data[i]].code_length);
    } 
    else {
      // a length or end-of-block
      writeBitsInverse(l, (unsigned)dict_lit[data[i] + 256].code, dict_lit[data[i] + 256].code_length);
      if (data[i] != 0) {
        // distance not 0, so a real distance, else it was an end-of-file

        // write extra bits for length            
        if (length_table[data[i] - 1].bits != 0) writeBits(l, (unsigned)data_extra[i], length_table[data[i] - 1].bits);

        // write distance
        writeBitsInverse(l, (unsigned)dict_dist[data[i + 1]].code, dict_dist[data[i + 1]].code_length);
        // write extra bits for distance
        if (distance_table[data[i + 1]].bits != 0) writeBits(l, (unsigned)data_extra[i + 1], distance_table[data[i + 1]].bits);
      }
    }
  }

  releaseMem(dict_lit);
  releaseMem(dict_dist);
  releaseMem(dict_huff);
  
  return 1;
}

/*
** The "algorithm" for writing stored files
*/
w_int store_stream(w_deflate_control l) {
  w_int check, count, end, size;
  w_ubyte b;

  // My non compressed blocks have a maximum size of 32k, which is not optimal
  // because 64k is allowed, but is have a buffer of 33k present, so I use it  

  count = 32768;
  end = 0;
  while (!end && l->resets_completed == l-> resets_requested) {
    // if full block, write a header
    if(count >= 32768) {
      writeLiteralByte(l, 0);   // header of non-comp, not last block

      size = 32768;
      check = ~size;

      writeLiteralByte(l, (unsigned)(size & 0x000000ff));  
      writeLiteralByte(l, (unsigned)(size & 0x0000ff00) >> 8);
      writeLiteralByte(l, (unsigned)(check & 0x000000ff));  
      writeLiteralByte(l, (unsigned)(check & 0x0000ff00) >> 8);
      
      count = 0;
    }

    b = readLiteralByte(l);
    // end of input
    if (l->par_in == NULL) {
      if (l->offset_in == -1){
        // Oeioei, and error occured, propagate it
        return 0;
      }
      // backtrack and change the last blockheader

      size = count;
      check = ~size;

      if (l->offset_bek_out - count - 5 < 0)
        (*l).output_bekken[33 * 1024 + l->offset_bek_out - count - 5] = 1;  // header of non-comp, last block
      else
        (*l).output_bekken[l->offset_bek_out - count - 5] = 1;  // header of non-comp, last block
      
      // change size
      if (l->offset_bek_out - count - 4 < 0)
        (*l).output_bekken[33 * 1024 + l->offset_bek_out - count - 4] = size & 0x000000ff;
      else
        (*l).output_bekken[l->offset_bek_out - count - 4] = size & 0x000000ff;
      if (l->offset_bek_out - count - 3 < 0)
        (*l).output_bekken[33 * 1024 + l->offset_bek_out - count - 3] = (size & 0x0000ff00) >> 8;
      else
        (*l).output_bekken[l->offset_bek_out - count - 3] = (size & 0x0000ff00) >> 8;
      if (l->offset_bek_out - count - 2 < 0)
        (*l).output_bekken[33 * 1024 + l->offset_bek_out - count - 2] = check & 0x000000ff;
      else
        (*l).output_bekken[l->offset_bek_out - count - 2] = check & 0x000000ff;
      if (l->offset_bek_out - count - 1 < 0)
        (*l).output_bekken[33 * 1024 + l->offset_bek_out - count - 1] = (check & 0x0000ff00) >> 8;
      else
        (*l).output_bekken[l->offset_bek_out - count - 1] = (check & 0x0000ff00) >> 8;
      
      end = 1;
    }
    else {
      writeLiteralByte(l, b);
    }
    
    count += 1;
  }

  // flush what we have left
  bekkenFlush(l);
  
  return 1;
}

/*
** These functions may only be called by the zipper.
** It maintains a 32k buffer with 512 bytes lookahead for deflating.
**
** returns 1 if ok
** returns 0 if eof or error detected
*/
static w_int fillWindow(w_deflate_control bs) {
  w_int size_lookahead;

  size_lookahead=(bs->offset_bek_in - bs->lookahead_bek_in);
  
  // we are allowed to maintain 512 bytes of lookup, but only 258 needed
  // keep trying till we have enough or end of file is detected (returns 0)
  while (size_lookahead < 300) {
    // fill up the lookahead buffer as much as possible

    if (bs->offset_in == 0 || bs->offset_in >= bs->par_in->size) {
      getNewBlock(bs);

      // once we have this, we can stop trying to get new blocks
      if (bs->par_in == NULL) 
        return 0;

      bs->offset_in = 0;
    }

    // ok, we have data available, now copy it
    if ((32768 + 512 - bs->offset_bek_in) > (bs->par_in->size - bs->offset_in)) {
      // everything we received can be put in the buffer     
      w_memcpy((*bs).input_bekken + bs->offset_bek_in, bs->par_in->data + bs->offset_in, (unsigned)(bs->par_in->size - bs->offset_in));

      bs->processed_size += bs->par_in->size - bs->offset_in;

      bs->offset_bek_in += bs->par_in->size - bs->offset_in;
      bs->offset_in += bs->par_in->size - bs->offset_in;
    }
    else if (bs->offset_bek_in < 32768 + 512) {    // not full
      // copy to the space  we have left
      w_memcpy((*bs).input_bekken + bs->offset_bek_in, bs->par_in->data + bs->offset_in, (unsigned)(32768 + 512 - bs->offset_bek_in));

      bs->processed_size += (32768 + 512 - bs->offset_bek_in);

      bs->offset_in += (32768 + 512 - bs->offset_bek_in);
      bs->offset_bek_in += (32768 + 512 - bs->offset_bek_in);
    }
    
    size_lookahead=(bs->offset_bek_in - bs->lookahead_bek_in);
  } 
  return 1;
}

/*
** Main loop of the deflater doing the dictionary creation and the lazy matching
*/
w_int deflate_stream(w_deflate_control l) {
  w_int size, end, max_chain, still_data, i, j, code, run, dist; 
  w_word key;
  w_short best_length, best_index, cur_length;
  w_ubyte *length_code, *temp_store, *dist_code;
  w_short *temp_store_extra;
  w_int tso, lazy_match, prev_match, total_won, tso_total;  

  total_won = 0;
  tso_total = 0;
  prev_match = 0;
  
  // actual hashtable, init all "pointers" to -1 (unsused)
  l->zip_hash_table = allocMem(sizeof(w_short) * HASH_LENGTH);
  if (!l->zip_hash_table) {
    return 0;
  }

  memset(l->zip_hash_table, 0xff, sizeof(w_short) * HASH_LENGTH);

  // allocate one hashentry per byte in the input block 
  // this way we don't have to represent the index, and we never need to alloc or free
  l->zip_hash_blocks = allocMem(sizeof(w_Zip_Hash) * 32768);
  if (!l->zip_hash_blocks) {
    woempa(9, "Unable to allocate zip_hash_blocks\n");
    releaseMem(l->zip_hash_table);
    return 0;
  }
  l->zip_hash_first = 0;

  // construct length table lookup length offset is 3
  // length code offset is 257
  length_code = allocMem(256);
  if (!length_code) {
    woempa(9, "Unable to allocate length_code\n");
    releaseMem(l->zip_hash_blocks);
    releaseMem(l->zip_hash_table);
    return 0;
  }

  for (i = 0; i < 29; i++) {
    for (j = 0; j < (1 << length_table[i].bits); j++) {
      length_code[length_table[i].base - 3 + j] = i;
    }
  }

  // temporary storage for almost compressed data
  // first is for <length, distance> and literals (have second byte set to 255) to be huffmann encoded
  // second is for extra bits
  temp_store = allocMem((32768 + 1) * 2);
  if (!temp_store) {
    woempa(9, "Unable to allocate temp_store\n");
    releaseMem(length_code);
    releaseMem(l->zip_hash_blocks);
    releaseMem(l->zip_hash_table);
    return 0;
  }

  temp_store_extra = allocMem(sizeof(w_short)* (32768 + 1) * 2);
  if (!temp_store_extra) {
    woempa(9, "Unable to allocate temp_store_extra\n");
    releaseMem(temp_store);
    releaseMem(length_code);
    releaseMem(l->zip_hash_blocks);
    releaseMem(l->zip_hash_table);
    return 0;
  }

  // initialise distance mapping
  dist_code = allocMem(512);
  if (!dist_code) {
    woempa(9, "Unable to allocate dist_code\n");
    releaseMem(temp_store_extra);
    releaseMem(temp_store);
    releaseMem(length_code);
    releaseMem(l->zip_hash_blocks);
    releaseMem(l->zip_hash_table);
    return 0;
  }

  dist = 0;
  for (i = 0 ; i < 16; i++) {
    for (j = 0; j < (1 << distance_table[i].bits); j++) {
      dist_code[dist++] = i;
    }
  }
  dist >>= 7;
  for ( ; i < 30; i++) {
    for (j = 0; j < (1 << (distance_table[i].bits - 7)); j++) {
      dist_code[256 + dist++] = i;
    }
  }

  tso = 0;
  lazy_match = 0;
  
  end = 0;
  still_data = 1;

  // prepare hashkey
  key = 0;
  still_data = fillWindow(l);
  if (l->offset_bek_in - l->lookahead_bek_in >= 3) {
    key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in]);
    key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in + 1]);
  }

  while (!end && l->resets_completed == l-> resets_requested) {
    if (l->lookahead_bek_in >= 32768) {

      // the end-of-block marker
      if (tso >= 32768 * 2) {
        // ajaj, we only had literals, just store block
        woempa(9, "You should better store the file, compression is bad.\n");
      }
      if (tso >= (32768 + 1) * 2) {
        woempa(9, "=========== AJAJ we cross the border\n");
        abort();
      }
      temp_store[tso] = 0;
      temp_store[tso + 1] = 0;
      tso += 2;

      tso_total += tso;

      // encode the data and write it out (not last block))
      if (encode(l, temp_store, temp_store_extra, tso, 0) == 0) {
        releaseMem(dist_code);
        releaseMem(temp_store_extra);
        releaseMem(temp_store);
        releaseMem(length_code);
        releaseMem(l->zip_hash_blocks);
        releaseMem(l->zip_hash_table);
        return 0;
      }

      // restarting for next block
     
      // clear hashtable
      memset(l->zip_hash_table, 0xff, sizeof(w_short) * HASH_LENGTH);

      l->zip_hash_first = 0;
      
      // to ease sliding window alot, at the end, we just copy remaining bytes to the beginning
      // this way we actually dont have a real sliding window because we will never pass
      // the end of the buffer. Very handy !!!
      // this way all indexes in a window start at 0 and just count up, so hashentries just have
      // a "pointer" in it and no index, because the index in hash_blocks is the same as in the
      // sliding window
      w_memcpy((*l).input_bekken, (*l).input_bekken + l->lookahead_bek_in, (unsigned)(l->offset_bek_in - l->lookahead_bek_in));
      
      l->offset_bek_in = l->offset_bek_in - l->lookahead_bek_in;
      l->lookahead_bek_in = 0;
      
      tso = 0;
      lazy_match = 0;

      key = 0;
      still_data = fillWindow(l);
      if (l->offset_bek_in - l->lookahead_bek_in >= 3) {
        key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in]);
        key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in + 1]);
      }
    }
    if (still_data)
      still_data = fillWindow(l);     // tries to fill lookahead as much as needed and returns 0 if at the end of file

    if (!still_data && l->offset_in == -1){
        // Oeioei, and error occured, propagate it
        releaseMem(dist_code);
        releaseMem(l->zip_hash_table);
        releaseMem(l->zip_hash_blocks);
        releaseMem(length_code);
        releaseMem(temp_store);
        releaseMem(temp_store_extra);
        return 0;
    }

    size = l->offset_bek_in - l->lookahead_bek_in;
    
    if (l->offset_bek_in >= 32768) { // buffer is full, so now size is remaining lookahead in window
      size = 32768 - l->lookahead_bek_in;
    }
    
    if (size >= 3) {
      // ok, at least 3 bytes left, so keep on processing

      // add current 3 bytes lookahead to the hashtable
      key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in + 2]);
      l->zip_hash_blocks[l->zip_hash_first].next = l->zip_hash_table[key];
      run = l->zip_hash_table[key];  // here we will start searching
      l->zip_hash_table[key] = l->zip_hash_first;
      l->zip_hash_first += 1;
      
      // look for earlier repetition 
      max_chain = 4096;
      best_length = 0;
      best_index = 0;
      if (lazy_match && prev_match > 32) max_chain >>= 2;
      // we can stop if our current best match is the shortest that will be presentable (if nr of bits for representation rise)
      while (max_chain > 0 && run != -1 && best_length < 258) {
        cur_length = find_longest_match(l, run);

        if (cur_length > best_length) {
          best_length = cur_length;
          best_index = run;
        }

        run = l->zip_hash_blocks[run].next;
        max_chain -= 1;
      }
          
      if (best_length >= 3) {
        /*
        ** This is the lazy matcher
        **
        ** match is better then 2 bytes, so compress
        **
        **
        ** if this match is better then the previous and we are lazy matching
        ** replace the previous with a literal and write out this match, lazy_match = true (re-lazy-match)
        **
        **
        ** else if this match is less then previous and we are lazy matching
        ** lazy_match = false, insert every intermediate entry to the hashtable
        **
        **
        ** else if we are not lazy matching
        ** write this match out, lazy_match = true, advance one step
        */

        dist = (l->lookahead_bek_in - best_index) - 1;
        if (dist < 256) { 
          code = dist_code[dist];
        }
        else {
          code = dist_code[256 + (dist >> 7)];
        }

        if (lazy_match) {
          // in this if i try to take into account the size of extra bits, thereby decreasing the length with the amount of extra bits
          if ((best_length*8 - distance_table[code].bits) > (prev_match*8 - distance_table[temp_store[tso - 1]].bits)) {
            total_won += best_length - prev_match;
            woempa(2, "-- Found lazy match: prev match %i, curr match %i, won %i\n", prev_match, best_length, total_won);

            // replace old by literal
            tso -= 2;
            temp_store[tso] = (*l).input_bekken[l->lookahead_bek_in - 1];
            temp_store[tso + 1] = 255;
            tso += 2;

            // insert new entry
            temp_store[tso] = length_code[best_length - 3] + 1;
            temp_store[tso + 1] = code;
            temp_store_extra[tso] = best_length - length_table[length_code[best_length - 3]].base;
            temp_store_extra[tso + 1] = l->lookahead_bek_in - best_index - distance_table[code].base;
            tso += 2;

            // scip the one we just inserted
            l->lookahead_bek_in += 1;

            // re-lazy-match
            lazy_match = 1;
            prev_match = best_length;
          }
          else {
            // scip the one we just inserted and the ones we are going to insert
            l->lookahead_bek_in += 1;

            // add intermediate into hashtable
            for (i = 0; i < prev_match - 1 - 1; i++) { 
              // if not at the end
              if (l->zip_hash_first < 32768 && (l->lookahead_bek_in + 2) <= l->offset_bek_in) {
                key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in + 2]);
                l->zip_hash_blocks[l->zip_hash_first].next = l->zip_hash_table[key];
                l->zip_hash_table[key] = l->zip_hash_first;
                l->zip_hash_first += 1;
              }
              l->lookahead_bek_in += 1;
            }
            lazy_match = 0;        
          }
        }
        else {
          temp_store[tso] = length_code[best_length - 3] + 1;
          temp_store[tso + 1] = code;
          temp_store_extra[tso] = best_length - length_table[length_code[best_length - 3]].base;
          temp_store_extra[tso + 1] = l->lookahead_bek_in - best_index - distance_table[code].base;
          tso += 2;

          // scip the one we just inserted
          l->lookahead_bek_in += 1;

          lazy_match = 1;
          prev_match = best_length;
        }
      }
      else {
        // match is smaller then 3 bytes so write literal symbol
        
        // if lazy matching, insert intermediate into hashtable and scip
        if (lazy_match) {
          // scip the one we just inserted and the ones we are going to insert
          l->lookahead_bek_in += 1;

          // add intermediate into hashtable
          for (i = 0; i < prev_match - 1 - 1; i++) { 
            // if not at the end
            if (l->zip_hash_first < 32768 && (l->lookahead_bek_in + 2) <= l->offset_bek_in) {
              key = running_hash_function(key, (*l).input_bekken[l->lookahead_bek_in + 2]);
              l->zip_hash_blocks[l->zip_hash_first].next = l->zip_hash_table[key];
              l->zip_hash_table[key] = l->zip_hash_first;
              l->zip_hash_first += 1;
            }
            l->lookahead_bek_in += 1;
          }
          lazy_match = 0;        
        }
        else {
          temp_store[tso] = (*l).input_bekken[l->lookahead_bek_in];
          temp_store[tso + 1] = 255;
          tso += 2;

          l->lookahead_bek_in += 1;
        }
      }
    }
    else {
      // less then 3 bytes in lookahead, so just store them (we do not check if we exeed 32k, because it are the last bytes)

      if (lazy_match) {
        size -= prev_match - 1;
        lazy_match = 0;        
        
        // scip those in the lazy-match, but don't insert them in the hashtable cause we don't need it anymore
        l->lookahead_bek_in += prev_match - 1;      
      }

      for (i = 0; i < size; i++) {
        temp_store[tso] = (*l).input_bekken[l->lookahead_bek_in];
        temp_store[tso + 1] = 255;
        tso += 2;
        l->lookahead_bek_in += 1;      
      }

      // TODO : what if buffer is full and we just received the last block ????????
      if (!still_data) end = 1;
    }
  }

  if (tso >= 32768 * 2) {
    // ajaj, we only had literals, just store block
    woempa(9, "You should better store the file, compression is bad.\n");
  }
  if (tso >= (32768 + 1) * 2) {
    woempa(9, "=========== AJAJ we cross the border\n");
    abort();
  }
  temp_store[tso] = 0;
  temp_store[tso + 1] = 0;
  tso += 2;

  tso_total += tso;

  // encode the data and write it out (last block))
  if (encode(l, temp_store, temp_store_extra, tso, 1) == 0) {
    releaseMem(dist_code);
    releaseMem(temp_store_extra);
    releaseMem(temp_store);
    releaseMem(length_code);
    releaseMem(l->zip_hash_blocks);
    releaseMem(l->zip_hash_table);
    return 0;
  }

  // flush possible remaining bits
  writeByteAlign(l);

  // flush buffer
  bekkenFlush(l);

  // clean up the hash table and blocks
  releaseMem(dist_code);
  releaseMem(l->zip_hash_table);
  releaseMem(l->zip_hash_blocks);
  releaseMem(length_code);
  releaseMem(temp_store);
  releaseMem(temp_store_extra);
  
  return 1;
}

/*
**
** The main deflate function
**
*/
w_void zzzdeflate(w_void *ll) {
  w_deflate_control l = ll;
  w_int err, no_auto, stop;
  x_status s;
  x_monitor_eternal(&l->ready);
  x_monitor_notify_all(&l->ready);
  l->state = COMPRESSION_THREAD_RUNNING;
  x_monitor_exit(&l->ready);

  err = stop = no_auto = 0;
  while (!err && !no_auto && !stop) {

    if (l->compression_level == 0) {
      // only store (fast)
      err = !store_stream(l);
    }
    else {
      // all other compression levels
      err = !deflate_stream(l);
    }

    if (err) {
      errorFlush(l);
    } 
    
    // reinit so we can keep on processing
    l->offset_in = l->offset_bek_out = 0;
    l->lookahead_bek_in = l->offset_bek_in = l->size_bek_out = 0;
    l->i_bits = 0x01;
    l->o_mask = 0x1;
    l->o_bits = 0;

    // try to get monitor
    woempa(INF_WOEMP_LEV_1, "Entering\n");
    woempa(INF_WOEMP_LEV_1, "State: err %i, stop %i, resets %i--%i, noauto %i\n", err, stop, l->resets_requested, l->resets_completed, no_auto);
    s = x_monitor_eternal(&l->ready);
    if (s == xs_success) {

      // if we need to stop or we had an error, try to synchonise with the other thread
      if (l->no_auto || err) {
      
        while (l->resets_completed == l->resets_requested) {
          woempa(INF_WOEMP_LEV_1, "Trying ...\n");
          s = x_monitor_wait(&l->ready, COMPRESSION_WAIT_TICKS);
        }
      }

      // set stop and reset
      no_auto = l->no_auto;
      stop = l->stop;

      woempa(INF_WOEMP_LEV_1, "State: err %i, stop %i, resets %i--%i, noauto %i\n", err, stop, l->resets_requested, l->resets_completed, no_auto);

      // if reset, clear all queues and partial data
      if (l->resets_completed != l->resets_requested) {
        woempa(INF_WOEMP_LEV_1, "Resetting\n");
        l->resets_completed = l->resets_requested;
        no_auto = 0;

        switch (x_mutex_lock(&l->mutx, x_eternal)) {
          case xs_success:
            break;
          default:
            err = 1;
            break;
        }

        x_queue_flush(&l->q_in, unzip_freeQueue);
        x_queue_flush(&l->q_out, unzip_freeQueue);

        if (l->par_in != NULL) {
          woempa(INF_WOEMP_LEV_1, "--in-- %p %p\n", l->par_in, l->par_in->data);
          if (l->par_in->data != NULL) releaseMem(l->par_in->data);
          releaseMem(l->par_in);
        }
        if (l->par_out != NULL && l->par_out != (w_void *)(-1)) {
          woempa(INF_WOEMP_LEV_1, "--out-- %p %p\n", l->par_out, l->par_out->data);
          if (l->par_out->data != NULL) releaseMem(l->par_out->data);
          releaseMem(l->par_out);
        }
        l->par_out = l->par_in = NULL;
        l->offset_out = 0;

        x_mutex_unlock(&l->mutx);
        
        // notify thread we are ready
        x_monitor_notify_all(&l->ready);
      }

      l->processed_size = 0;
      if (!l->stop) l->nomoreinput = 0;

      woempa(INF_WOEMP_LEV_1, "Exiting\n");

      x_monitor_exit(&l->ready);
    }
    else {
      woempa(9, "Monitor error !!!!\n");
      err = 1;
    }
  }
  x_monitor_eternal(&l->ready);
  x_monitor_notify_all(&l->ready);
  l->state = COMPRESSION_THREAD_STOPPED;
  x_monitor_exit(&l->ready);
}
