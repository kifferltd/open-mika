#ifndef _ELF_H
#define _ELF_H

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
**
** $Id: elf.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** ELF definitions. I decided to not blatantly copy over the ELF definitions of the
** well known document (TIS, Portable Formats Specification, v 1.1). It is a very concise
** and therefore difficult to understand, especially if your not native English speaking.
** Learning my way around ELF has happened by doing things my way. Therefore these new definitions.
**
** All ELF related structures get the prefix 'e_'. As usual, the structure itself
** proceeds with a capital, a pointer to such a structure, with a small character.
** E.g., the ELF header structure is defined as 'e_Header' while a pointer to a ELF header
** is typedefed as a 'e_header'. Note that the 'e_' types are only used within the
** modules code. It should not be included anywhere else.
*/

//#include "modtypes.h"
#include <oswald.h>

typedef struct e_Header         * e_header;
typedef struct e_Section        * e_section;
typedef struct e_Symbol         * e_symbol;
typedef struct e_Rel            * e_rel;
typedef struct e_Rela           * e_rela;
typedef struct e_Segment        * e_segment;

typedef unsigned int              e_address;
typedef unsigned int              e_offset;
typedef unsigned int              e_uword;
typedef signed int                e_sword;
typedef unsigned char             e_ubyte;
typedef unsigned short            e_ushort;

typedef enum {
  e_false = 0,
  e_true  = 1,
} e_boolean;

/*
** The ELF header of any file that is compatible with the TIS 1.1 standard.
**
** Note that we don't use the term 'program header' and than refer to 'segments' afterwards.
** We use the term 'segments' consistently.
*/

typedef struct e_Header {
  e_ubyte    ident[16];
  e_ushort   type;                /* */
  e_ushort   machine;
  e_uword    version;
  e_address  entry;
  e_offset   segments;            /* The offset in bytes where the segment table begins or 0 if there is no segment table. */
  e_offset   sections;            /* The offset in bytes where the sections table begins or 0 if there is no sections table. */
  e_uword    flags;
  e_ushort   sizeof_header;       /* The size of this header. Use this in stead of sizeof(e_Header) for reading in an ELF file. */
  e_ushort   sizeof_segment;      /* The size of a segment entry in the segment table. */
  e_ushort   num_segments;        /* The number of segment entries in the segment table. */
  e_ushort   sizeof_section;      /* The size of an section entry in the section table. */
  e_ushort   num_sections;        /* The number of section entries in the section table. */
  e_ushort   sc_names;            /* The index in the section table of the string table that holds the names of the sections. */
} e_Header;

/*
** Types of ELF files.
*/

typedef enum {
  ef_none   = 0,
  ef_rel    = 1,
  ef_exec   = 2,
  ef_dyn    = 3,
  ef_core   = 4,
  ef_loproc = 0xff00,
  ef_hiproc = 0xffff,
} ef_type;

/*
** A single section table entry. This structure gives all information
** about a single section in an ELF file.
*/

typedef struct e_Section {
  e_uword     name;
  e_uword     type;
  e_uword     flags;
  e_address   address;
  e_offset    offset;
  e_uword     size;
  e_uword     link;
  e_uword     info;
  e_uword     alignment;
  e_uword     sizeof_entry;
} e_Section;

typedef enum {
  sc_null     = 0,
  sc_progbits = 1,
  sc_symtab   = 2,
  sc_strtab   = 3,
  sc_rela     = 4,
  sc_hash     = 5,
  sc_dynamic  = 6,
  sc_note     = 7,
  sc_nobits   = 8,
  sc_rel      = 9,
  sc_shlib    = 10,
  sc_dynsym   = 11,
  sc_loproc   = 0x70000000,
  sc_hiproc   = 0x7fffffff,
  sc_louser   = 0x80000000,
  sc_hiuser   = 0xffffffff,
} sc_type;

/*
** Special section numbers.
*/

typedef enum {
  shn_undef     = 0x0000,
  shn_loreserve = 0xff00,
  shn_loproc    = 0xff00,
  shn_hiproc    = 0xff1f,
  shn_abs       = 0xfff1,
  shn_common    = 0xfff2,
  shn_hireserve = 0xffff,
} sh_number;

/*
** Section flags for the e_section->flags field.
*/

#define SHF_WRITE                 0x00000001
#define SHF_ALLOC                 0x00000002
#define SHF_EXECINSTR             0x00000004

/*
** An entry in the symbol table.
*/

typedef struct e_Symbol {
  e_uword     name;
  e_address   value;
  e_uword     size;
  e_ubyte     info;
  e_ubyte     other;
  e_ushort    section;
} e_Symbol;

inline static unsigned int e_sym_bind(e_symbol symbol) {
  return (symbol->info >> 4);
}

typedef enum {
  smb_local   = 0,
  smb_global  = 1,
  smb_weak    = 2,
  smb_loproc  = 13,
  smb_hiproc  = 15,
} sm_bind;

typedef enum {
  smt_notype  = 0,
  smt_object  = 1,
  smt_func    = 2,
  smt_section = 3,
  smt_file    = 4,
  smt_loproc  = 13,
  smt_hiproc  = 15,
} sm_type;

inline static unsigned int e_sym_type(e_symbol symbol) {
  return (symbol->info & 0x0000000f);
}

inline static unsigned int e_sym_info(unsigned int bind, unsigned int type) {
  return ((bind << 4) + (type & 0x0000000f));
}

/*
** Relocation entries.
*/

typedef struct e_Rel {
  e_offset    offset;
  e_uword     info;
} e_Rel;

typedef struct e_Rela {
  e_offset    offset;
  e_uword     info;
  e_sword     addend;
} e_Rela;

typedef enum {
  rel_none     = 0,
  rel_32       = 1,
  rel_pc32     = 2,
  rel_got32    = 3,
  rel_plt32    = 4,
  rel_copy     = 5,
  rel_glob_dat = 6,
  rel_jmp_slot = 7,
  rel_relative = 8,
  rel_gotoff   = 9,
  rel_gotpc    = 10,
} rel_type;

inline static x_int e_rel_sym(e_rel rel) {
  return (rel->info >> 8);
}

inline static x_int e_rel_type(e_rel rel) {
  return (rel->info & 0x000000ff);
}

typedef struct e_Segment {
  void * dummy;
} e_Segment;

/*
** CPU and handler descriptor structure for ELF operations. Note that we also record the structure sizes at
** compilation time since we will not handle ELF files that don't comply with the sizes of the standard; i.e.
** we will NOT handle files where e.g. e_header->sizeof_section != sizeof(e_Section).
*/

typedef struct e_Handler {
  x_ubyte e_machine;
  const char *name;
  x_size header_size;  /* Size of an ELF header structure. */
  x_size section_size; /* Size of an ELF section structure. */
  x_size segment_size; /* Size of an ELF segment structure. */
  void * relocators; // function pointers for relocation functions
} e_Handler;

/* 
** Values for e_machine (architecture).  
*/

#define EM_NONE         0               /* No machine */
#define EM_SPARC        2               /* SUN SPARC */
#define EM_386          3               /* Intel 80386 */
#define EM_68K          4               /* Motorola m68k family */
#define EM_88K          5               /* Motorola m88k family */
#define EM_486          6               /* Intel 80486 */
#define EM_PARISC      15               /* HPPA */
#define EM_PPC         20               /* PowerPC */

/*
** Function prototypes.
*/

const char * rel2char(unsigned int type);
const char * sct2char(sc_type type);
void sc_dump(e_section section, char * strtab, int i, e_boolean header, e_boolean footer);
void sym_dump(e_symbol symbol, char * strtab, int i, e_boolean header, e_boolean footer);
void rel_dump(e_rel rel, int i, e_boolean header, e_boolean footer);

#endif /* _ELF_H */
