#ifndef _SYMBOLS_H
#define _SYMBOLS_H

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
** $Id: symbols.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** All functions and structures relating to management of identifiers (strings),
** symbols (they refer to an identifier) and symbol tables.
*/

//#include "modtypes.h"
#include <oswald.h>

typedef struct x_Ident {
  x_ident next;               /* Next free or occupied identifier. Occupied identifiers use 'next' in the hash chain. */
  x_symbol symbols;           /* Pointer to list of symbols using this identifier. */
  x_ubyte string[0];          /* The bytes containing the string. */
} x_Ident;

#define SYM_EXPORTED          0x80000000 /* This also implies that the x_symbol->value.address field is valid. */
#define SYM_IMPORTED          0x40000000 /* This also implies that the x_symbol->value.symbol field is valid. */

#define SYM_RESOLVED          0x20000000 /* This means that the symbol has been linked, it is valid. */
#define SYM_SPECIAL           0x10000000 /* Special symbols, they should not be used for linking up. E.g. init_module, clean_module */
#define SYM_INIT              0x08000000 
#define SYM_CLEAN             0x04000000

#define SYM_FUNCTION          0x02000000 /* The symbol is a function address */
#define SYM_VARIABLE          0x01000000 /* The symbol is the address of a variable */

#define SYM_JAVA              0x00800000 /* The symbol follows the Java Virtual Machine Naming convention. */

#define SYM_COMMON            0x00400000 /* Symbol is a common block symbol. When not resolved, the symbol->value.offset is valid. */
#define SYM_STATIC            0x00200000
#define SYM_PACKAGE           0x00100000
#define SYM_SPARE3            0x00080000
#define SYM_SPARE4            0x00040000
#define SYM_SPARE5            0x00020000
#define SYM_SPARE6            0x00010000

#define SYM_DEPMASK           0x0000ffff /* The mask to get at the number of referals to this symbols. */

typedef struct x_Symbol {
  x_symbol next;              /* Next symbol in this chain of symbols with the same 'ident'. */
  x_ident ident;              /* The identifier for this symbol. */
  x_flags flags;              /* The flags for this symbol and the number of dependencies on this symbol (lower bits). */
  union {
    x_address address;        /* When the symbol itself is a 'definition', this is the address where it is found. */
    x_symbol defines;         /* When the symbol is a 'declaration', the symbol that has the definition (element in 'next' list) */
    x_size offset;            /* Offset from the COMMON block area that will be allocated later and assigned to 'address'. */
  } value;
} x_Symbol;

#define SYMTAB_MADGIC         0x19650403

typedef struct x_Symtab {
  x_symtab next;              /* The linked list of all symbols tables, starts at 'symtabs' in symbols.c */
  x_module module;            /* The module this symbol table belongs to. */
  x_int num_symbols;          /* The number of imported and exported symbols in the 'symbols' field. */
  x_int capacity;             /* */
  x_uword madgic;             /* The madgic word used to find back the symbol table structure, given a symbol pointer. */
  x_Symbol symbols[0];        /* The table of full x_Symbol structures, the number of entries = num_import + num_export */
} x_Symtab;

void x_static_hash(x_symbol symbol);
x_symtab x_symbol2symtab(x_symbol symbol);
x_ident x_ident_search(const char * string, x_boolean create);
x_symtab x_symtab_create(x_int num_slots, x_malloc m_alloc);
void x_ident_iterate(x_boolean (*iterator)(x_ident ident, void * argument), void * argument);
x_boolean streq(const char * str1, const char * str2);
x_boolean streqn(const char * str1, const char * str2, x_size n);
signed int x_symbol_refs(x_symbol symbol, signed int addend);
x_int x_symbol_java(x_module module, x_symbol symbol, unsigned char * buffer, x_size num);
x_int x_symbol_flags2char(x_symbol symbol, char * buffer, x_int num);

#endif /* _SYMBOLS_H */
