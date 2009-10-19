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

#include "new_deflate_internals.h"
#include "debug.h"

/* ------------------------------------------------------------------------------------------------------- */
// inflating and destoring
/* ------------------------------------------------------------------------------------------------------- */

/*
** OK, here comes the crux of the matter. This is required reading!
**
** For *required* additional information, we refer
** to RFC1951 "DEFLATE Compressed Data Format Specification version 1.3". Also
** note that the official Appnote.txt has some errors in it! More specifically
** the definition of HLIT and HCLEN of the dynamic Huffman codes is wrong, use the
** RFC (or this source code) for the correct values.
** 
** If you read the zlib/unzip code and don't understand it, join the club;
** therefore I have put this information in this file and tried reconstructing
** the code for deflating blocks, myself in a IMHO more simpler way.
**
** All alphabet (code table) reconstruction starts with a counting of
** howmany times a certain codelength (in number of bits) occurs in the
** alphabet. This is for example a table that gives these statistics for the
** alphabet with which a in a dynamic inflated block (compression number 8) the
** same statistics are compressed for the end alphabet (with which the file is
** decompressed).
** 
** This is "Step [1]" and it is done in buildDynamicDictionary and
** buildFixedDictionary.
**
**    The total number of codes = 14 (the number of alphabet symbol values).
**
**     Code length  |   Number of times it occurs in the alphabet
**    --------------+---------------------------------------------
**            0     |   (per definition)
**            1     |          0
**            2     |          0
**            3     |          5   <-
**            4     |          5   <-
**            5     |          1   <-
**            6     |          1   <-
**            7     |          0
**            8     |          0
**            9     |          0
**           10     |          0
**           11     |          0
**           12     |          0
**           13     |          0
**           14     |          0
**           15     |          0
** 
** As shown in the RFC, the bit length statistics are enough to reconstruct
** the alphabet and corresponding codes. The assignment of codes is done in
** gen_tree and is done according to the RFC, we call this "Step [2]".
**
** Then, again according to the RFC, we assign the numerical code values to
** all non 0 length alphabet elements, and we assign the consecutive symbol
** values to each alphabet element (the elements with code length 0 are not
** used but we still have to assign them a symbol value to keep or symbol
** numbering correct). This is "Step [3]".
**
** Now we have to build a system with which we can lookup codes rather
** quickly (we are not to worried about performance, rather correctness) and
** that doesn't take to much time to build itself.
** 
** (Read the following a few times and go through the code afterwards,
** enlightment *will* come...)
**
** In this approach, we put all codes of a certain length in an array. As
** you notice, the code value itself can be used as an index. If the code is
** bigger then the number of elements in the array, we check for the next level
** of table. Each code length has thus an array and a pivot value; when the
** code is greater than the pivot value, we jump to the next level of table.
**
** For decoding, we start reading in bits as much as the first table length
** tells us, is the code read in, *minus* the offset of that level (more
** later), higher than the pivot value, we go to the next level. We have to
** substract the offset value since all our arrays in each level start at 0
** index. We proceed like this untill we have reached a true array index.
**
** This is a table as reconstructed from the code length statistics above.
** This table is built in a straightforward fashion (really!) in "Step [4]".
**
** ------ bit length 3, offset 0 --------------
**     0 code = 0x0000 symbol =   0
**     1 code = 0x0001 symbol =   6
**     2 code = 0x0002 symbol =   8
**     3 code = 0x0003 symbol =   9
**     4 code = 0x0004 symbol =  10
** 
** ------ bit length 4, offset 10 --------------
**     0 code = 0x000a symbol =   4
**     1 code = 0x000b symbol =   5
**     2 code = 0x000c symbol =   7
**     3 code = 0x000d symbol =  11
**     4 code = 0x000e symbol =  12
** 
** ------ bit length 5, offset 30 --------------
**     0 code = 0x001e symbol =  16
** 
** ------ bit length 6, offset 62 --------------
**     0 code = 0x003e symbol =  18
** 
** ------ bit length 7, offset 126 --------------
**     0 code = 0x007e symbol =   3
**     1 code = 0x007f symbol =  17
** 
*/

/*
char *printBits(w_int code, w_int length) {
  static char buffer[BUF_SIZE];
  w_word mask = 1 << (length - 1);
  w_int i = 0;
  
  memset(buffer, 0x00, BUF_SIZE);
  while (mask) {
    if ((w_word)code & mask) {
      buffer[i++] = '1';
    }
    else {
      buffer[i++] = '0';
    }
    mask >>= 1;
  }

  return buffer;
}
*/

static void releaseTable(w_ztable table) {
  w_ztable victim;

  // Break the circular list first.
  table->previous->next = NULL;

  // Now clean the list up...
  while (table) {
    victim = table;
    table = table->next;
    releaseMem(victim->table);
    releaseMem(victim);
  }
}


static w_ztable gen_tree(w_Hnode *tree, w_int max_code, w_size bl_count[]) {
  w_ztable first = NULL;
  w_ztable table;
  w_size current_count;
  w_int offset;
  w_int i;
  w_int j;
  w_word code = 0;
  w_word next_code[MAX_BITS + 1];
  w_int length;
  w_short symbol = 0;

  /*
  ** Step [2]
  **
  ** Find the numerical value of the smallest code for each code length.
  */
  bl_count[0] = 0;
  for (i = 1; i < MAX_BITS + 1; i++) {
    code = (code + bl_count[i - 1]) << 1;
    next_code[i] = code;
  }

  /*
  ** Step [3]
  **
  ** Assign numerical values to all codes that have non-zero bit lengths, 
  ** using consecutive values for these codes of the same code length, with 
  ** the numerical base values determined in the previous loop. Note that we
  ** assign a symbol value to all codes, even the zero-lengths which are not
  ** used. See the notes above for gen_tree.
  */
  for (i = 0; i < max_code; i++) {
    length = tree[i].length;
    if (length != 0) {
      tree[i].code = (w_short)next_code[length];
      next_code[length]++;
    }
    /*
    ** Assign a symbol to non used alphabet elements (length == 0) also !!
    */
    tree[i].symbol = symbol++;
  }

  // Check that we have an EOB code.
  for (i = 0; i < max_code; i++) {
    if (tree[i].symbol == END_BLOCK) {
      break;
    }
  }
  if (max_code > 32 && i == max_code) {
    woempa(10, "NO EOB symbol (number of codes is %d)!!\n", max_code);
    return NULL;
  }

  /*
  ** Step [4]
  **
  ** Now build a multi-level table lookup system.
  */
  for (i = 1; i < MAX_BITS + 2; i++) {
    current_count = bl_count[i - 1];
    if (current_count) {
      woempa(1, "Current length %d, count %d\n", i - 1, current_count);
      table = allocClearedMem(sizeof(w_Ztable));
      if (! table) {
        if (first) {
          releaseTable(first);
        }
        wprintf("gen_tree : could not allocate %d bytes for table\n", sizeof(w_Ztable));
        return NULL;
      }
      table->table = allocClearedMem(current_count * sizeof(w_Hnode));
      if (! table->table) {
        if (first) {
          releaseTable(first);
        }
        releaseMem(table);
        wprintf("gen_tree : could not allocate %d bytes for table->table\n", current_count *sizeof(w_Hnode));
        return NULL;
      }
      offset = 0;
      for (j = 0; j < max_code; j++) {
        if (tree[j].length == i - 1) {
          w_memcpy(&table->table[offset], &tree[j], sizeof(w_Hnode));
          offset += 1;
        }
      }
      table->length = (w_size)(i - 1);
      table->pivot = (w_size)(offset - 1);
      if (! first) {
        first = table;
        list_init(first);
      }
      else {
        list_insert(first, table);
      }
      table->offset = (w_size)table->table[0].code;
    }
  }

  return first;
}

w_bits new_readSingleBit(w_inflate_control bs) {
  w_bits bit;

  bit = (bs->i_bits & 0x01);

//woempa(7, "%d\n", bs->i_bits);
  bs->i_bits >>= 1;
  if (! bs->i_bits) {
//woempa(7, "%d\n", bs->offset_in);
    if (bs->offset_in == 0 || bs->offset_in >= bs->par_in->size) {
      new_getNewBlock(bs);
//woempa(7, "%p\n", bs->par_in);

      if (bs->par_in == NULL) return 0;

      bs->offset_in = 0;
    }

    bs->i_bits = bs->par_in->data[bs->offset_in];			// dit kan beter door van offset een pointer te maken
    bit = (bs->i_bits & 0x01);
    bs->i_bits = (bs->i_bits >> 1) | 0x80;

    bs->offset_in += 1;

    bs->processed_size += 1;
  }

//woempa(7, "%d\n", bit);
  return bit;
}

w_hnode new_decode(w_ztable table, w_inflate_control in) {
  w_ztable current = table;
  w_size j = 0;
  w_size delta_length = current->length;
  w_size i;
  w_hnode node;

  while (1) {
    for (i = 0; i < delta_length; i++) {
      j = (j << 1) | new_readSingleBit(in);
    }

    if (j - current->offset > current->pivot) {
      delta_length = current->length;
      current = current->next;
      delta_length = current->length - delta_length;
      continue;
    }
    else {
      node = &current->table[j - current->offset];
      break; 
    }
  }
  
  return node;
}

/*
** Return up to 8 bits in 1 call.
*/
w_bits new_readBits(w_inflate_control bs, w_size count) {
  w_bits bits = 0;
  w_size ruler = count;

  if (count > 8) {
    wabort(ABORT_WONKA, "No counts longer than 8, current is %d!\n", count);
  }
  
  while (ruler--) {
    bits = (bits << 1) | new_readSingleBit(bs);
  }
  
  if (count) {
    bits = new_bitReverse[bits] >> (8 - count);
  }

  return bits;
}

// only on aligned stream !!!
static w_int new_writeWindowBytes(w_inflate_control bs, w_int length, w_int distance) {
  w_int j;
  w_byte obyte;
  w_int off = bs->offset_bek_out - distance;

  if (off < 0) off = 33*1024 + off;
  
  for (j = 0; j < length; j++) {
    obyte = (*bs).output_bekken[off];
    if (new_writeLiteralByte(bs, obyte)) {
      return 1;
    }
    off += 1;
    if (off >= 33*1024) off = 0;
  }

  return 0;  
}

w_int new_inflateBlock(w_inflate_control bs, w_zdict dict) {
  w_word j;
  w_int len;
  w_int dist;
  w_hnode node;
  w_int status = 0;

  while (bs->resets_completed == bs->resets_requested) {
    node = new_decode(dict->lengths_literals, bs);
    j = node->symbol;

    if (j < 256) {
      // It is a literal byte, write it as it is...
      if (new_writeLiteralByte(bs, j)) {
        status = 1;
        goto hastalavista;
      }
    }
    else if (j == END_BLOCK) {
      // End of block reached; break out of decoding loop.
      break;
    }
    else {
      /*
      ** It is a length; substract the offset and find out the extra information. Note
      ** that the extra bits will never exceed 8 so we don't need to be able to read
      ** the extra bits in more then 2 passes as is done with the extra bits for the
      ** distances.
      */
      j -= 256 + 1;
      len = (w_int)new_readBits(bs, (w_size)new_length_table[j].bits);
      len += new_length_table[j].base;

      /*
      ** Now find the distance that should be used, note that the number of extra bits
      ** could exceed 8 bits, so we need to read in maybe 2 passes, an 8 bit pass and
      ** then the rest.
      */

      node = new_decode(dict->distances, bs);
      j = node->symbol;
      if (new_distance_table[j].bits > 8) {
        // Read extra bits in 2 passes...
        dist = (w_int)new_readBits(bs, 8);
        dist |= (w_int)(new_readBits(bs, (w_size)new_distance_table[j].bits - 8) << 8);
      }
      else {
        // Less then 8 bits, read in 1 pass...
        dist = (w_int)new_readBits(bs, (w_size)new_distance_table[j].bits);
      }
      dist += new_distance_table[j].base;

      /*
      ** Now copy over 'len' bytes from the output buffer, starting at 'dist'
      ** offset from the current byte.
      */
      
      if (new_writeWindowBytes(bs, len, dist)) {
        status = 1;
        goto hastalavista;
      }
    }
  }

hastalavista:

  woempa(1, "--> block decoded; status is: %s.\n", status ? "ERROR" : "OK");

  return status;
}

void releaseDictionary(w_zdict dict);
/*
void releaseDictionary(w_zdict dict) {
  releaseTable(dict->lengths_literals);
  if (dict->distances != NULL) releaseTable(dict->distances);

  releaseMem(dict);
}
*/
/*
void dumpTable(w_ztable table) {
  w_ztable current;
  w_size i;
  w_int level = 0;
  
  current = table;
  do {
    level += 1;
    woempa(10, "---[%2d]--- bit length %d, offset %d, pivot %d  --------------\n", level, current->length, current->offset, current->pivot);
    for (i = 0; i <= current->pivot; i++) {
      woempa(10, "  %3d code = 0x%04x symbol = %3d (0x%04x)\n", i, current->table[i].code, current->table[i].symbol, current->table[i].symbol);
    }
    current = current->next;
  } while (current != table);
}
*/

w_zdict new_buildDynamicDictionary(w_inflate_control in) {
  w_zdict dict = NULL;
  w_int numLiteralCodes;
  w_int numDistanceCodes;
  w_int numLengthCodes;
  w_size *codeLength;
  w_int i;
  w_int j;
  w_int l;
  w_ztable tmptable;
  w_Hnode *tmpnodes;
  w_hnode node;
  w_int n;
  w_size bit_length_counts[MAX_BITS + 1];

  woempa(1, "Building dynamic dictionary\n");

  numLiteralCodes = 257 + (w_int)new_readBits(in, 5);
  numDistanceCodes = 1 + (w_int)new_readBits(in, 5);
  numLengthCodes = 4 + (w_int)new_readBits(in, 4);

  woempa(1, "Number of literal codes = %d\n", numLiteralCodes);
  woempa(1, "Number of distance codes = %d\n", numDistanceCodes);
  woempa(1, "Number of distance/length codes = %d\n", numLengthCodes);

  n = numLiteralCodes + numDistanceCodes;
  codeLength = allocClearedMem(n * sizeof(w_size));
  if (! codeLength) {
    wprintf("buildDynamicDictionary() : could not allocate %d bytes for codeLength table\n", n * sizeof(w_size));
    return NULL;
  }

  tmpnodes = allocClearedMem((19 + 2) * sizeof(w_Hnode));
  if (! tmpnodes) {
    wprintf("buildDynamicDictionary() : could not allocate %d bytes for tmpnodes\n", 21 * sizeof(w_Hnode));
    releaseMem(codeLength);
    return NULL;
  }

  /*
  ** Read in the code lengths for the 19 alphabet symbols.
  */
  for (i = 0; i < numLengthCodes; i++) {
    j = (w_int)new_readBits(in, 3);
    codeLength[new_bit_order[i]] = (w_size)j;
  }

  if (in->par_in == NULL) {
    wprintf("par_in is NULL!\n");
    return NULL;
  }

  /*
  ** Step [1]
  **
  ** What we have read in are the code lengths in bits for each of the 19 possible
  ** symbol values, maybe some are not used. We now go over these lengths and count
  ** for each length, how many times it occurs so that we can rebuild our huffman code table.
  */
  // Paranoid loop 'coz memset failed on Yair's box ...
  for (i = 0; i < MAX_BITS + 1; i++) {
    bit_length_counts[i] = 0;
  }
  for (i = 0; i < 19; i++) {
    if (codeLength[i]) {
      bit_length_counts[codeLength[i]]++;
      tmpnodes[i].length = (w_byte)codeLength[i];
    }
  }

  /*
  ** Generate a table to decode the Huffman codes for the lengths and the distances and then
  ** use this temporary table to read in and decode the lengths for the symbols that are used
  ** for the end dictionary tables. Note that the data for literal/length codes and the distance
  ** codes has to be read in as one block.
  **
  ** (Steps [2], [3] and [4] are done in gen_tree)
  */
  tmptable = gen_tree(tmpnodes, 19, bit_length_counts);
  if (!tmptable) {
    wprintf("gen_tree failed! (1)\n");
    // TODO : cleanup?
    return NULL;
  }
  releaseMem(tmpnodes);
//  dumpTable(tmptable);

  /*
  ** Now using the tmptable, decode the literal and distance code lengths; though
  ** reset the code length array first.
  */
  for (i = 0; i < n; ++i) {
    codeLength[i] = 0;
  }
  i = 0;
  while (i < n && in->resets_completed == in->resets_requested) {
    node = new_decode(tmptable, in);
    j = node->symbol;
    if (j < 16) {
      /*
      ** It is a literal length, just take it as it is...
      */
      codeLength[i++] = (w_size)j;
    }
    else if (j == 16) {
      /*
      ** Repeat the last given length 3 to 6 times, depending on the next 2 bits
      ** that are in the stream.
      */
      j = 3 + (w_int)new_readBits(in, 2);
      if (i + j > n) {
        woempa(10, "Wrong (i) %d + (j) %d > (n) %d\n", i, j, n);
        releaseMem(codeLength);
        releaseTable(tmptable);
        return NULL;
      }
      l = i ? (w_int)codeLength[i - 1] : 0;
      while (j--) {
        codeLength[i++] = (w_size)l;
      }
    }
    else {
      if (j == 17) {
        /*
        ** Repeat a 0 length for 3 to 10 times, the number of times to repeat is
        ** encoded in the next 3 bits in the stream.
        */
        j = 3 + (w_int)new_readBits(in, 3);
      }
      else {
        /*
        ** j must be 18, so we have to repeat a 0 bit length for 11 to 138 times,
        ** depending on the next 7 bits in the stream.
        */
        j = 11 + (w_int)new_readBits(in, 7);
      }
      if (i + j > n) {
        woempa(1, "Wrong (i) %d + (j) %d > (n) %d\n", i, j, n);
        releaseMem(codeLength);
        releaseTable(tmptable);
        return NULL;
      }
      while (j--) {
        codeLength[i++] = 0;
      }
    }
  }

  releaseTable(tmptable);

  /*
  ** OK, the same trick we did previously, namely counting the number of times a certain
  ** bit code length appears, we do again and use this information to reconstruct the 
  ** literal/length code table first...
  */
  dict = allocClearedMem(sizeof(w_Zdict));
  if (! dict) {
    wprintf("buildDynamicDictionary() : could not allocate %d bytes for dict\n", sizeof(w_Zdict));
    releaseMem(codeLength);
    return NULL;
  }

  tmpnodes = allocClearedMem((numLiteralCodes + 2) * sizeof(w_Hnode));
  if (! tmpnodes) {
    wprintf("buildDynamicDictionary() : could not allocate %d bytes for tmpnodes\n", (numLiteralCodes * 2) * sizeof(w_Hnode));
    releaseMem(dict);
    releaseMem(codeLength);
    return NULL;
  }

  for (i = 0; i < MAX_BITS + 1; i++) {
    bit_length_counts[i] = 0;
  }
  for (i = 0; i < numLiteralCodes; i++) {
    j = (w_int)codeLength[i];
    if (j) {
      bit_length_counts[j]++;
      tmpnodes[i].length = (w_byte)j;
    }
  }
  
  dict->lengths_literals = gen_tree(tmpnodes, numLiteralCodes, bit_length_counts);
  releaseMem(tmpnodes);
  if (! dict->lengths_literals) {
    releaseMem(dict);
    releaseMem(codeLength);
    return NULL;
  }
//  dumpTable(dict->lengths_literals);

  /*
  ** ... and then the distance code table.
  */
  tmpnodes = allocClearedMem((numDistanceCodes + 2) * sizeof(w_Hnode));
  if (! tmpnodes) {
    wprintf("buildDynamicDictionary() : could not allocate %d bytes for tmpnodes\n", (numDistanceCodes * 2) * sizeof(w_Hnode));
    releaseTable(dict->lengths_literals);
    releaseMem(dict);
    releaseMem(codeLength);
    return NULL;
  }

  for (i = 0; i < MAX_BITS + 1; i++) {
    bit_length_counts[i] = 0;
  }
  for (i = 0; i < numDistanceCodes; i++) {
    j = (w_int)codeLength[i + numLiteralCodes];
    if (j) {
      bit_length_counts[j]++;
      tmpnodes[i].length = (w_byte)j;
    }
  }

  dict->distances = gen_tree(tmpnodes, numDistanceCodes, bit_length_counts);
  releaseMem(tmpnodes);
  // if we have nr distances == 1 and first distance is 0, then we have only literals, so proceed
  if (dict->distances == NULL && !(numDistanceCodes == 1 && codeLength[numLiteralCodes] == 0)) {
    releaseTable(dict->lengths_literals);
    releaseMem(codeLength);
    releaseMem(dict);
    return NULL;
  }
//  dumpTable(dict->distances);

  releaseMem(codeLength);
  woempa(1, "Finished building dynamic dictionary\n");

  return dict;
}

w_zdict new_buildFixedDictionary(void) {
  w_zdict dict = allocClearedMem(sizeof(w_Zdict));
  w_Hnode *tmpnodes;
  w_ztable table;
  w_size bit_length_counts[MAX_BITS + 1];
  w_int n;
  w_int c;

  if (! dict) {
    return NULL;
  }

  woempa(1, "Building fixed dictionary %p...\n", dict);

  /*
  ** Create the literal/lengths code tree. In here, Step [1] of the reconstructing
  ** is also performed.
  */
  n = 0;
  c = 0;
  tmpnodes = allocClearedMem((L_CODES + 2) * sizeof(w_Hnode));
  if (! tmpnodes) {
    releaseMem(dict);
    return NULL;
  }

  while (c <= MAX_BITS) { bit_length_counts[c++] = 0; }
  while (n <= 143) { tmpnodes[n++].length = 8; bit_length_counts[8]++; }
  while (n <= 255) { tmpnodes[n++].length = 9; bit_length_counts[9]++; }
  while (n <= 279) { tmpnodes[n++].length = 7; bit_length_counts[7]++; }
  while (n <= 287) { tmpnodes[n++].length = 8; bit_length_counts[8]++; }
  table = gen_tree(tmpnodes, L_CODES, bit_length_counts);
  dict->lengths_literals = table;
  releaseMem(tmpnodes);
  if (! dict->lengths_literals) {
    releaseMem(dict);
    return NULL;
  }

  /*
  ** Create the distances code tree, this could be done by hand, but we leave it
  ** up to the gen_tree routine for consistency. Again, Step [1] is done here.
  */
  n = 0;
  c = 0;
  tmpnodes = allocClearedMem((D_CODES + 2) * sizeof(w_Hnode));
  if (! tmpnodes) {
    releaseTable(dict->lengths_literals);
    releaseMem(dict);
    return NULL;
  }
  while (c <= MAX_BITS) { bit_length_counts[c++] = 0; }
  while (n <= 31) { tmpnodes[n++].length = 5; bit_length_counts[5]++; }
  table = gen_tree(tmpnodes, D_CODES, bit_length_counts);
  dict->distances = table;
  releaseMem(tmpnodes);
  if (! dict->distances) {
    releaseTable(dict->lengths_literals);
    releaseMem(dict);
    return NULL;
  }

  return dict;
  
}

/*
* allocates as w_inflate_control.
*/
w_inflate_control allocInflateControl(void) {
  w_inflate_control control = allocClearedMem(sizeof(w_Inflate_Control));
  if(control) {
    control->i_bits = 0x01;
    control->o_mask = 0x1;
    control->compression_level = 9;
    control->need_more_input = 1;             // we are starting up, so we need input
  }
  return control;
}

void deleteQueueElement(w_deflate_queueelem element) {
  if(element->data != NULL && element->release) {
    releaseMem(element->data);
  }
  releaseMem(element);
}

void releaseInflateControl(w_inflate_control control) {
  w_deflate_queueelem element;

  element = control->first_link_in;
  while(element != NULL) {
    w_deflate_queueelem next = element->next;
    deleteQueueElement(element);
    element = next;
  }

  element = control->first_link_out;
  while(element != NULL) {
    w_deflate_queueelem next = element->next;
    deleteQueueElement(element);
    element = next;
  }

  if(control->par_in) {
    deleteQueueElement(control->par_in);
  }
  if(control->par_out) {
    deleteQueueElement(control->par_out);
  }

  releaseMem(control);
}

void inflate_control_setInput(w_inflate_control control, w_ubyte* bytes, w_int size, int clonedata) {
  w_deflate_queueelem element;

  woempa(7, "data = %p size = %d clonedata = %d\n", bytes, size, clonedata);
  if(size <= 0) {
    return;
  }

  element = allocMem(sizeof(w_Deflate_QueueElem));
  woempa(7, "queue element = %p\n", element);
  if(element) {
    element->next = NULL;
    element->size = size;
    element->release = clonedata;
    if(clonedata) {
      w_ubyte* data = allocMem(sizeof(w_ubyte)*size);
      woempa(7, "cloning, copy = %p\n", data);
      if(data) {
        memcpy(data,bytes,sizeof(w_ubyte)*size);
        element->data = data;
      } else {
        releaseMem(element);
        return;
      }
    } else {
      element->data = bytes;
    }
    if(control->last_link_in) {
      woempa(7, "set %p->last_link_in->next to %p\n", control, element);
      control->last_link_in->next = element;
    } else {
      woempa(7, "set %p->first_link_in to %p\n", control, element);
      control->first_link_in = element;
    }
    control->last_link_in = element;
    control->need_more_input = 0;
  }
}

int inflate_control_inflate(w_inflate_control l) {
  w_bits lastblock;
  w_bits type;
  w_zdict fixed_dict = NULL;
  w_zdict dict = NULL;
  w_int num = 1;
  w_int size, check;
  w_int err = 0;

  do {
woempa(7, "l->i_bits = %d\n", l->i_bits);
    lastblock = new_readSingleBit(l);
    if (l->par_in == NULL) {
      woempa(7, "A no more input: baling out\n");
      goto hastalavista;
    }

    type = new_readBits(l, 2);
    if (l->par_in == NULL) {
      woempa(7, "B no more input: baling out\n");
      goto hastalavista;
    }

    switch (type) {
      case 0:
        new_readByteAlign(l);
        size = 0;
        size = new_readLiteralByte(l);
        size |= (new_readLiteralByte(l) << 8);
        check = 0;
        check = new_readLiteralByte(l);
        check |= (new_readLiteralByte(l) << 8);

        if (l->par_in == NULL) {
          woempa(7, "C no more input: baling out\n");
          goto hastalavista;
        }

        woempa(7,"--> block %d is of the 'stored' type. %d bytes (0x%04x == 0x%04x)\n", num, size, size & 0x0000ffff, ~check & 0x0000ffff);
        if ((size & 0x0000ffff) != (~check & 0x0000ffff)) {
          woempa(9,"Wrong block check 0x%04x != 0x%04x.\n", size & 0x0000ffff, ~check & 0x0000ffff);
          err = 1;
          goto hastalavista;
        }
        while (size--) {
          if (new_writeLiteralByte(l, new_readLiteralByte(l))) {
            wprintf("writeLiteralFoo\n");
            err = 1;
            goto hastalavista;
          }
        }
        break;

      case 1:
        if (! fixed_dict ) {
          fixed_dict = new_buildFixedDictionary();
          if (!fixed_dict) {
            wabort(ABORT_WONKA, "Unable to build fixed dictionary\n");
          }
        }
        woempa(7,"--> block %d is of the 'fixed huffman code' type.\n", num);
        if (new_inflateBlock(l, fixed_dict)) {
          woempa(9,"inflateFoo\n");
          err = 1;
          goto hastalavista;
        }
        break;

      case 2:
        woempa(7,"--> block %d is of the 'dynamic huffman code' type.\n", num);
        dict = new_buildDynamicDictionary(l);
        if (! dict) {
          woempa(9,"dictFoo\n");
          err = 1;
          goto hastalavista;
        }
        if (new_inflateBlock(l, dict) != 0) {
          wprintf("inflateBar\n");
          err = 1;
          goto hastalavista;
        }
        releaseDictionary(dict);
        dict = NULL;
        break;

      default:
        woempa(9,"Block %d has an unknown type (0x%08x) or has an error in it!\n", num, type);
        err = 1;
        goto hastalavista;
    }
    num += 1;
  } while (!lastblock);

hastalavista:

  if (!err) {
    if(l->size_bek_out > 0) {
      woempa(7, "flushing %d output bytes\n", l->size_bek_out);
      new_bekkenFlush(l);
    }
  }
  else {
    woempa(7, "HOOLA, an error occured while decompressing...\n");
    new_errorFlush(l);
  }

  if (fixed_dict) {
   releaseDictionary(fixed_dict);
  }

  if (dict) {
    releaseDictionary(dict);
  }
  return err;
}

int inflate_control_getbytes_from_queue(w_inflate_control control, w_ubyte* bytes, w_int len) {
  int result = 0;
  int need = len;
  w_deflate_queueelem element = control->first_link_out;
  woempa(7,"filling array. want %d bytes: first element = %p\n", len, element);
  while(element && need) {
    int have = element->size - element->index;

    woempa(7,"size = %d, index = %d, release = %d, len = %d, result = %d\n",element->size, element->index,
      element->release, len, result);
    if(have <= need) {
      woempa(7, "I have no more than I need, so I copy all I have\n");
      woempa(7,"memcpy 1 (%p,%p,%d)\n",bytes+result,element->data+element->index, have);
      memcpy(bytes+result,element->data+element->index, have);
      result += have;
      need -= have;
      have = 0;
    } else if (need) {
      woempa(7, "I have more than I need, so I copy all I need\n");
      woempa(7,"memcpy 2 (%p,%p,%d)\n",bytes+result,element->data+element->index, need);
      memcpy(bytes+result,element->data+element->index, need);
      element->index += need;
      result = len;
      have -= need;
      need = 0;
    }

    if (need && !have) {
      w_deflate_queueelem deleteMe = element;
      woempa(7, "I have nothing so I take the next element from the queue\n");
      element = element->next;
      deleteQueueElement(deleteMe);
      woempa(7,"still need %d bytes: next element = %p\n", need, element);
    }
  }
  control->first_link_out = element;
  woempa(7, "set %p->first_link_out to %p\n", control, element);
  if(!element) {
    woempa(7, "set %p->last_link_out to %p\n", control, element);
    control->last_link_out = NULL;
  }
  woempa(7,"Done filling array. got %d bytes\n",result);
  return result;
}
