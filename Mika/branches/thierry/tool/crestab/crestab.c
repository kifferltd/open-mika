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
**************************************************************************/


/*
** $Id: crestab.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** This 'crestab' tool will take as input a file with symbols to
** put in a static table that it will generate. The second input
** is an ELF relocatable object file in which the symbols will be
** looked up.
*/

#include <stdlib.h>
#include <stdio.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdarg.h>
#include <string.h>

#include <getopt.h>

/*
** We reproduce the header for for the elf bits we use to avoid
** trouble building this tool in a cross compiled environment. Taking in
** the "elf.h" file of oswald would bring in inline assembly for ARM
** when we would compile the tool for a x86 host and that clashes...
** We only take in the bits we need.
*/

typedef unsigned int              e_address;
typedef unsigned int              e_offset;
typedef unsigned int              e_uword;
typedef signed int                e_sword;
typedef unsigned char             e_ubyte;
typedef unsigned short            e_ushort;

typedef struct e_Header         * e_header;
typedef struct e_Symbol         * e_symbol;
typedef struct e_Section        * e_section;

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

typedef enum {
  smt_notype  = 0,
  smt_object  = 1,
  smt_func    = 2,
  smt_section = 3,
  smt_file    = 4,
  smt_loproc  = 13,
  smt_hiproc  = 15,
} sm_type;

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

inline static unsigned int e_sym_type(e_symbol symbol) {
  return (symbol->info & 0x0000000f);
}

/*
** A symbol
*/

typedef struct Symbol_t * symbol_t;

typedef struct Symbol_t {
  symbol_t  next;
  char *    name;
  char *    comment;
  int       writte;              /* 1 means write this to the output file, 0 means don't. */
  e_symbol  elf_symbol;
} Symbol_t;

/*
** The list of symbols.
*/

static symbol_t symbols = NULL;

/*
** Our internal parameters and their default values.
*/

static struct {
  char * output;
  char * table;
  char * relocatable;
  char * function;
  int verbose;
  int extra;

  FILE * f_output;
} globals = {
  "symtab.c",
  "symtab.exp",
  NULL,
  "static",
  0,
  0,

  NULL,
};

/*
** The options array.
*/

static struct option options[] = {
  {      "output", 1, NULL, 'o' },
  {       "table", 1, NULL, 't' },
  { "relocatable", 1, NULL, 'r' },
  {    "function", 1, NULL, 'f' },
  {     "verbose", 0, NULL, 'v' },
  {       "extra", 1, NULL, 'e' },
  {          NULL, 0, NULL,  0  },
};

e_section get_section(e_header header, int indexx) {
 
  unsigned char * data = (unsigned char *)header;

  return (e_section)((data + header->sections) + (sizeof(e_Section) * indexx));

}

int num_entries(e_section section) {

  return (section->size / section->sizeof_entry);

}

e_symbol get_symbol(e_header header, e_section section, int indexx) {

  unsigned char * data = (unsigned char *)header;

  return (e_symbol)(data + section->offset + (section->sizeof_entry * indexx));
  
}

char * get_name(e_header header, e_section section, e_symbol symbol) {

  unsigned char * data = (unsigned char *)header;
  e_section strtab_section = get_section(header, (int)section->link);
  char * strtab = (data + strtab_section->offset);
  
  return & strtab[symbol->name];
  
}

int read_elf_symbols(unsigned char * elf) {

  e_header header = (e_header)elf;
  e_section section;
  e_symbol elf_symbol;
  symbol_t symbol;
  char * name;
  int i;
  int j;
  
  for (i = 0; i < header->num_sections; i++) {
    section = get_section(header, i);
    if (section->type == sc_symtab) {
      for (j = 1; j < num_entries(section); j++) {
        elf_symbol = get_symbol(header, section, j);
        name = get_name(header, section, elf_symbol);
        
        /*
        ** We can't do anything with empty symbol names (used for section symbols).
        */
        
        if (strlen(name)) {
          symbol = calloc(1, sizeof(Symbol_t));
          symbol->elf_symbol = elf_symbol;
          symbol->name = name;
          symbol->next = symbols;
          symbols = symbol;
        }
      }
    }
  }
  
  return 0;
  
}

int find_symbol(char * name, char * arguments, char * comment) {

  symbol_t symbol;
  
  for (symbol = symbols; symbol; symbol = symbol->next) {
    if (strcmp(name, symbol->name) == 0) {
      if (globals.verbose) {
        fprintf(stdout, "Found table symbol '%s' in ELF symbol table.\n", name);
      }
      if (symbol->writte) {
        fprintf(stdout, "WARNING: symbol '%s' appears more than once in file '%s'.\n", symbol->name, globals.table);
      }
      else {
        if (strlen(comment)) {
          symbol->comment = calloc(1, strlen(comment) + 1);
          strcpy(symbol->comment, comment);
        }
        symbol->writte = 1;
      }
      return 1;
    }
  }

  fprintf(stdout, "WARNING: did not find back symbol '%s' in the ELF file '%s'.\n", name, globals.relocatable);
  
  return 0;
  
}

void write_opening(FILE * o) {

  fprintf(o, "\n");
  fprintf(o, "/*\n");
  fprintf(o, "** This file is generated automatically. Do not edit this file.\n");
  fprintf(o, "** Use the 'crestab' tool to generate it.\n");
  fprintf(o, "**\n");
  fprintf(o, "*/\n");
  fprintf(o, "\n");
  fprintf(o, "#define NULL ((void *)0)\n");
  fprintf(o, "\n");
  fprintf(o, "#include \"symbols.h\"\n");
  fprintf(o, "\n");

}

void write_identifiers(FILE * o) {

  symbol_t symbol;
  unsigned int i;
  
  /*
  ** Write out the identifiers of the symbols that have been found.
  */

  for (symbol = symbols; symbol; symbol = symbol->next) {
    if (symbol->writte) {
      fprintf(o, "static x_Ident Ident_%s = {\n", symbol->name);
      fprintf(o, "  NULL,\n");
      fprintf(o, "  NULL,\n");
      fprintf(o, "  { ");
      for (i = 0; i < strlen(symbol->name); i++) {
        fprintf(o, "'%c', ", symbol->name[i]);
      }
      fprintf(o, "'\\0' }, \n");
      fprintf(o, "};\n");
      fprintf(o, "\n");
    }
  }

}

void write_symbols(FILE * o, int num_symbols) {

  symbol_t symbol;
  int i;

  /*
  ** Write out the symbols that have been found.
  */

  fprintf(o, "static x_Symtab Symtab = {\n");
  fprintf(o, "  NULL,\n");
  fprintf(o, "  NULL,\n");
  fprintf(o, "  %d,\n", num_symbols);
  fprintf(o, "  %d,\n", num_symbols + globals.extra);
  fprintf(o, "  SYMTAB_MADGIC,\n");
  fprintf(o, "  {\n");
  for (symbol = symbols; symbol; symbol = symbol->next) {
    if (symbol->writte) {
      fprintf(o, "    { NULL, & Ident_%s, ", symbol->name);
      if (e_sym_type(symbol->elf_symbol) == smt_func) {
        fprintf(o, "(SYM_RESOLVED | SYM_FUNCTION | SYM_EXPORTED | SYM_STATIC), ");
        fprintf(o, "{ (x_address) %s } ", symbol->name);
      }
      else {
        fprintf(o, "(SYM_RESOLVED | SYM_VARIABLE | SYM_EXPORTED | SYM_STATIC), ");
        fprintf(o, "{ (x_address) & %s } ", symbol->name);
      }
      fprintf(o, "},\n");
    }
  }

  if (globals.extra) {
    for (i = 0; i < globals.extra; i++) {
      fprintf(o, "    { NULL, NULL, 0, { 0 } },\n");
    }
  }
  
  fprintf(o, "  }\n");
  fprintf(o, "};\n");
  fprintf(o, "\n");

}

void write_function(FILE *o, int num_symbols) {

  fprintf(o, "x_symtab x_symtab_%s (void) {\n", globals.function);
  fprintf(o, "\n");
  fprintf(o, "  int i;\n");
  fprintf(o, "\n");
  fprintf(o, "  for (i = 0; i < %d; i++) {\n", num_symbols);
  fprintf(o, "    x_static_hash(& Symtab.symbols[i]);\n");
  fprintf(o, "  }\n");
  fprintf(o, "\n");
  fprintf(o, "  return & Symtab;\n");
  fprintf(o, "\n");
  fprintf(o, "}\n");

}

void write_externals(FILE *o) {

  symbol_t symbol;

  fprintf(o, "/*\n");
  fprintf(o, "** The external declarations of function pointers and variables. Note that the\n");
  fprintf(o, "** functions are declared as taking void and returning void. This is OK since we\n");
  fprintf(o, "** only want the pointer. The same goes for variables, we only need the address of\n");
  fprintf(o, "** the variable, not it's true type.\n");
  fprintf(o, "*/\n");
  fprintf(o, "\n");

  for (symbol = symbols; symbol; symbol = symbol->next) {
    if (symbol->writte) {
      if (e_sym_type(symbol->elf_symbol) == smt_func) {
        fprintf(o, "extern void %s(void);\n", symbol->name);
      }
      else {
        fprintf(o, "extern unsigned int %s;\n", symbol->name);
      }
    }
  }
  
  fprintf(o, "\n");
  
}

#define skip_whitespace(cursor) while (*cursor == ' ' || *cursor == '\t') { cursor++; }

int main(int argc, char * argv[]) {

  FILE * file;
  const unsigned int size = 256;
  char buffer[size];
  char name[size];
  char comment[size];
  char arguments[size];
  char * cursor;
  char * writte;
  char * newline;
  unsigned char *elf;
  struct stat fdinfo;
  int ed;
  int unresolved = 0;
  int num_symbols;
  symbol_t symbol;
  e_header header;
  int c;

  if (argc < 4) {
    fprintf(stderr, "usage %s --table=<table> --reloc=<object file> --out=<output>\n", argv[0]);
    exit(0);
  }
  
  while (1) {
    c = getopt_long(argc, argv, "o:t:r:f:", options, NULL);

    if (c == -1) {
      break;
    }

    switch (c) {
      case 'o': globals.output = optarg; break;
      case 't': globals.table = optarg; break;
      case 'r': globals.relocatable = optarg; break;
      case 'f': globals.function = optarg; break;
      case 'v': globals.verbose = 1; break;
      case 'e': globals.extra = strtol(optarg, NULL, 0); break;
      default: printf("uh %d '%c'\n", c, c);
    }

  }

  if (globals.verbose) {
    fprintf(stdout, "function name extension = '%s'\n", globals.function);
    fprintf(stdout, "Will allocate space for %d empty symbol%s.\n", globals.extra, globals.extra == 1 ? "" : "s");
  }

  ed = open(globals.relocatable, O_RDONLY);
  if (ed < 0) {
    printf("could not open ELF file '%s'.\n", globals.relocatable);
    exit(0);
  }

  fstat(ed, &fdinfo);
  if (globals.verbose) {
    fprintf(stdout, "Opened object file '%s', size = %d bytes\n", globals.relocatable, (int)fdinfo.st_size);
  }
  elf = (unsigned char *)malloc((unsigned int)fdinfo.st_size);
  read(ed, elf, (unsigned int)fdinfo.st_size);
  close(ed);
  
  /*
  ** Read in all the ELF symbols from all the sections.
  */

  read_elf_symbols(elf);
  header = (e_header)elf;

  file = fopen(globals.table, "r");
  if (! file) {
    printf("Could not open file '%s'.\n", globals.table);
    exit(1);
  }

  if (globals.verbose) {
    fprintf(stdout, "Opened table file '%s'\n", globals.table);
  }

  while (fgets(buffer, (signed int)size, file)) {
    cursor = buffer;
    newline = buffer + strlen(buffer) - 2;

    /*
    ** Skip empty lines.
    */
    
    if (strlen(buffer) < 3) {
      continue;
    }
    
    skip_whitespace(cursor);
    
    /*
    ** Skip comment lines.
    */
    
    if (*cursor == '#') {
      continue;
    }

    /*
    ** Parse the name of the symbol out of the buffer up to the newline or the next white space.
    */
    
    memset(name, 0x00, size);
    writte = name;
    while (*cursor != '\n' && *cursor != ' ') {
      *writte++ = *cursor;
      cursor++;
    }
    
    skip_whitespace(cursor);
    
    memset(arguments, 0x00, size);
    if (cursor < newline) {
      writte = arguments;
      while (*cursor != '#' && *cursor != '\n') {
        *writte++ = *cursor;
        cursor++;
      }
    }

    skip_whitespace(cursor);
    
    memset(comment, 0x00, size);
    if (cursor < newline) {
      cursor += 1;
      skip_whitespace(cursor);
      writte = comment;
      while (*cursor != '\n' && *cursor != '\0') {
        *writte++ = *cursor;
        cursor++;
      }
    }
    
//    printf("label = '%s' arguments = '%s' comment = '%s'\n", name, arguments, comment);
    
    if (! find_symbol(name, arguments, comment)) {
      unresolved += 1;
    }
    
  }
  
  fclose(file);

  /*
  ** Open the output file.
  */
  
  globals.f_output = fopen(globals.output, "w");
  if (! globals.f_output) {
    fprintf(stderr, "Could not open output file '%s'.\n", globals.output);
    exit(1);
  }
  
  /*
  ** Do some checking and some counting.
  */

  num_symbols = 0;
  for (symbol = symbols; symbol; symbol = symbol->next) {
    if (symbol->writte) {
      if (e_sym_type(symbol->elf_symbol) == smt_notype && symbol->elf_symbol->section == shn_undef) {
        fprintf(stdout, "WARNING: symbol '%s' is not exported by '%s' and is ignored.\n", symbol->name, globals.relocatable);
        symbol->writte = 0;
      }
      else {
        num_symbols += 1;
      }
    }
  }

  write_opening(globals.f_output);

  write_externals(globals.f_output);

  write_identifiers(globals.f_output);

  write_symbols(globals.f_output, num_symbols);

  write_function(globals.f_output, num_symbols);

  if (globals.verbose) {
    fprintf(stdout, "Created %d static identifiers and symbols in '%s'\n", num_symbols, globals.output);
  }
      
  exit(0);

}
