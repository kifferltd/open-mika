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

/*
** $Id: symbols.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** Code to manipulate the symbol tables and the identifiers used in symbols.
*/

#include <stddef.h> // for offsetof

#include "elf.h"
#include "symbols.h"
#include "modules.h"

/*
** All symbol tables are linked into a list. This list starts with 'symtabs'.
*/

static x_symtab symtabs = NULL;

/*
** NUM_BUCKETS defines how many slots our simple hash table has;
** 'hashtable' is an array of pointers to identifiers. The system is 
** based on 'separate chaining' and identifiers are chained through 
** their 'next' field.
*/

#define NUM_BUCKETS (97) 

static x_ident hashtable[NUM_BUCKETS];

/*
** Compare two strings, return true when equal, false otherwise. We don't use
** strcmp since it's semantics are strange IMHO and we don't need a +1 or
** -1 result, only false, when the strings aren't equal.
*/

x_boolean streq(const char * str1, const char * str2) {

  while (*str1 == *str2) {
    if (*str1 == '\0') {
      return true;
    }
    str1 += 1;
    str2 += 1;
  }
  
  return false;
  
}

/*
** Our own private strlen, used only once so we make it inline static.
*/

inline static x_size i_strlen(const char * str) {

  const char * cursor = str;
  
  while (*cursor != '\0') {
    cursor += 1;
  }
  
  return (cursor - str);
  
}

x_boolean streqn(const char * str1, const char * str2, x_size n) {

  while (*str1 == *str2 && n--) {
    if (*str1 == '\0') {
      return true;
    }
    str1 += 1;
    str2 += 1;
  }
  
  return (n == 0);
  
}

/*
** The ELF defined hashing function.
*/

x_uword x_elf_hash(const unsigned char * string) {

  x_uword h = 0;
  x_uword g;
  
  while (*string) {
    h = (h << 4) + *string++;
    g = h & 0xf0000000;
    if (g) {
      h ^= g >> 24;
    }
    h &= ~g;
  }

  return h;
  
}

/*
** Search for an identifier that already carries 'string'. When 
** the identifier is found, it is returned, when it is not
** found, it will be created when the 'create' flag is true,
** otherwise, NULL will be returned.
*/

x_ident x_ident_search(const char * string, x_boolean create) {

  x_ident ident;
  x_uword hash = x_elf_hash(string);
  x_size length;
  x_size indexx = hash % NUM_BUCKETS;
  x_size i;
  x_ubyte * c;

  /*
  ** See if the identifier exists in our hashtable, and if yes, return it.
  */
  
  for (ident = hashtable[indexx]; ident; ident = ident->next) {
    if (streq(ident->string, string)) {
      return ident;
    }
  }
  
  /*
  ** Since we arrived here, it doesn't exist, so we allocate a fresh one,
  ** and put it into our hashtable, when the create flag allows for it.
  */

  if (create) {
    length = i_strlen(string) + 1;  
    ident = malloc(sizeof(x_Ident) + length);
    if (ident == NULL) {
      return NULL;
    }
    ident->next = hashtable[indexx];
    hashtable[indexx] = ident;
//    memcpy(ident->string, string, length);
    c = ident->string;
    for (i = 0; i < length; i++) {
      *c++ = *string++;
    }
    ident->symbols = NULL;
  }

  return ident;
  
}

void x_static_hash(x_symbol symbol) {

  x_size indexx;

  indexx = x_elf_hash(symbol->ident->string) % NUM_BUCKETS;
  symbol->ident->next = hashtable[indexx];
  symbol->ident->symbols = symbol;
  hashtable[indexx] = symbol->ident;
  loempa(9, "Hashed static identifier '%s', symbol = 0x%08x\n", symbol->ident->string, symbol->ident->symbols);

}

/*
** Iterate over the identifier entries. If not used, we'd better remove it...
*/

void x_ident_iterate(x_boolean (*iterator)(x_ident ident, void * argument), void * argument) {

  x_ident ident;
  x_size i;

  for (i = 0; i < NUM_BUCKETS; i++) {
    for (ident = hashtable[i]; ident; ident = ident->next) {
      if (! ((*iterator)(ident, argument))) {
        return;
      }
    }
  }
  
}

// Add check for runaway behavior...
x_symtab x_symbol2symtab(x_symbol symbol) {

  while (*(x_uword *)((unsigned char *)symbol - sizeof(x_uword)) != SYMTAB_MADGIC) {
//    loempa(9, "Symbol is not first 0x%08x...\n", *(x_uword *)((unsigned char *)symbol - sizeof(x_uword)));
    symbol -= 1;
  }

  return (x_symtab)((unsigned char *)symbol - offsetof(x_Symtab, symbols));

}

x_symtab x_symtab_create(x_int num_slots, x_malloc m_malloc) {

  x_symtab symtab;

  symtab = m_malloc(sizeof(x_Symtab) + sizeof(x_Symbol) * num_slots);
  if (symtab) {
    symtab->module = NULL;
    symtab->next = NULL;
    symtab->num_symbols = 0;
    symtab->capacity = num_slots;
    symtab->madgic = SYMTAB_MADGIC;
    
    /*
    ** The identifier being NULL is a sign that the symbol has not been initialized yet,
    ** so we clear all identifier references.
    */
    
    while (num_slots--) {
      symtab->symbols[num_slots].ident = NULL;
    }

    symtab->next = symtabs;
    symtabs = symtab;    

  }
  
  return symtab;

}

/*
** Add the 'addend' number to the number of referrals to the symbol. The 'addend'
** can be positive or negative. This function returns the number of dependencies on this
** symbol, so if 0 is given as 'addend', this function returns the current number of dependencies.
*/

signed int x_symbol_refs(x_symbol symbol, signed int addend) {
  
  unsigned int dependents = (signed int)(symbol->flags & SYM_DEPMASK);
  
  dependents += addend;
  symbol->flags &= ~SYM_DEPMASK;
  symbol->flags = symbol->flags | dependents;

  return dependents;

}

/*
** Parse a JNI compliant function name into the Class name, method name and argument signature components.
** The result is a character array where the first 2 bytes are used as an index. E.g. the identifier of the
** symbol is parsed such that the Class name string begins at 'buffer[3]', the method name string begins at
** 'buffer [ buffer[1] ]' and the argument signature string begins at 'buffer[ buffer[2] ]'. Each string is
** terminated with a '\0' character.
**
** E.g. the Java class method:
**
**   Peer.destroy([BILjava/lang/String;)
**
** which is mangled in C into the function name:
**
**   Java_Peer_destroy___3BILjava_lang_String_2
**
** is parsed into the given buffer argument as follows:
**
** char buffer[] = {
**    6, = [0] class name index
**    4, = [1] class name length, excluding the nul character
**   11, = [2] method name index
**    7, = [3] method name length, excluding the nul character
**   19, = [4] argument signature index
**   23, = [5] argument signature length, excluding the nul character
**   'P', 'e', 'e', 'r', '\0',
**   'd', 'e', 's', 't', 'r', 'o', 'y', '\0',
**   '(', '[', 'B', 'I', 'L', 'j', 'a', 'v', 'a', '/', 'l', 'a', 'n', 'g', '/', 'S', 't', 'r', 'i', 'n', 'g', ';', ')', '\0',
**   ... remaining buffer space ...
** };
**
** This function returns the number of buffer characters that are used and will check for overflow, so if a call
** result = x_symbol_java(symbol, buffer, 100) returns the value 100, the parsing most probably failed.
** 
*/

x_int x_symbol_java(x_module module, x_symbol symbol, unsigned char * buffer, x_size num) {

  unsigned char * read;
  unsigned char * write;
  unsigned char * last;

  /*
  ** We set up a fencepost with 'last'. This 'last' is pointing to the character position
  ** in the buffer that we can not overwrite. In the while loops, there is checking for
  ** overflow of the buffer, but we sometimes do ' *write++ = '\0'; ' where we don't
  ** check for overflow. Therefore we decrease the last post with the number of unchecked
  ** writes; 6 positions for the length and indexes, 5 unchecked writes.
  */
  
  last = buffer + num - 1 - 6 - 5;

  if (streqn(symbol->ident->string, "Java_", 5)) {
    read = symbol->ident->string + 5;

    /*
    ** Extract the name of the Class, up to the next '-'; the combination of '_1' is replaced by a 
    ** normal underscore and processing continues.
    */

    buffer[0] = 6;
    write = buffer + 6;
    while (*read && write < last) {
      if (read[0] == '_' && read[1] == '1') {
        read++;
        *write++ = '_';
      }
      else if (read[0] == '_') {
        read++;
        break;
      }
      else {
        *write++ = *read;
      }
      read++;
    }

    buffer[1] = write - (buffer + buffer[0]);
    
    loempa(9, "CLASS NAME length is %d\n", buffer[1]);

    *write++ = '\0';

    /*
    ** Now extract the method name, up to the combination '__' which indicates the start
    ** of the argument signature; again '_1' is transformed into a single '_'.
    */

    buffer[2] = write - buffer;

    while (*read && write < last) {
      if (read[0] == '_' && read[1] == '1') {
        read++;
        *write++ = '_';
      }
      else if (read[0] == '_' && read[1] == '_') {
        read += 2;
        break;
      }
      else {
        *write++ = *read;
      }
      read++;
    }

    buffer[3] = write - (buffer + buffer[2]);

    loempa(9, "METHOD NAME length is %d\n", buffer[3]);
    
    *write++ = '\0';

    /*
    ** Now extract the signature, up to the end of the string; the combination '_2' is replaced by a ';' and
    ** the combination '_3' is replaced by a '['.
    */

    buffer[4] = write - buffer;

    *write++ = '(';
    while (*read && write < last) {
      if (read[0] == '_' && read[1] == '3') {
        read++;
        *write++ = '[';
      }
      else if (read[0] == '_' && read[1] == '2') {
        read++;
        *write++ = ';';
      }
      else if (read[0] == '_') {
        *write++ = '/';
      }
      else {
        *write++ = *read;
      }
      read++;
    }
    *write++ = ')';
    *write++ = '\0';

    buffer[5] = write - (buffer + buffer[4]) - 1;
    
    loempa(9, "SIGNATURE NAME length is %d\n", buffer[5]);

    loempa(9, "Java descriptor: %s %s %s %d characters\n", buffer + buffer[0], buffer + buffer[2], buffer + buffer[4], write - buffer);
//    loempa(9, "Java descriptor %d characters\n", write - buffer);

    return (write - buffer);
    
  }

  return 0;
  
}
