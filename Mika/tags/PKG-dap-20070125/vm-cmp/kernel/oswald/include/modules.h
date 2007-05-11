#ifndef _MODULES_H
#define _MODULES_H

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
** $Id: modules.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** Support for loadable ELF modules (object files) and shared libraries.
*/

//#include "modtypes.h"
#include <oswald.h>
#include "elf.h"

/*
** What is the alignment of memory blocks that are allocted through the supplied memory allocator.
*/

#define ALLOC_ALIGNMENT 4

/*
** A helper structure during loading. Contains a modifiable runtime copy of the ELF section headers.
*/

typedef struct x_Elf {
  x_module module;        /* Which module this helper structure is for. */
  e_header header;        /* All the ELF data, it starts with the ELF header. */
  x_ubyte * data;         /* The ELF file data as a byte pointer. Is same as 'header' but we don't make as much casting errors :-[ */
  x_int oswald;           /* The *INDEX* of the special .oswald section of the module. */
  x_ubyte * oswald_data;  /* The temporary data space that we use to put the oswald section in, so relocation can take place. */
  x_size common_offset;   /* The next available position in the COMMON block. After resolving imports/exports, this is the size. */
  x_size num_sections;    /* The number of section entries there are in the 'sections' array. */
  e_Section sections[0];  /* The variable sized array that contains a modifiable runtime copy of the section header in 'data'. */
} x_Elf;

typedef struct x_Module {
  x_module   next;        /* The link to other modules, when the module is linked in the list. */
  x_malloc   f_alloc;
  x_free     f_free;
  x_elf      elf;         /* The link to the ELF helper structure during initialization. */
  x_ident    ident;       /* The name of the module. VERSION ?? In special variable ?? */
  x_ident    package;     /* A package name. */
  x_ubyte    o_major;
  x_ubyte    o_minor;
  x_ubyte    m_major;
  x_ubyte    m_minor;
  x_flags    flags;       /* The module flags that indicates it's state. */
  x_symtab   symtab;      /* The symbol table of the module, with imported and exported symbols. */
  x_ubyte *  area_1;      /* This is the memory that is allocated to contain TEXT - DATA - BSS. */
  x_ubyte *  area_2;      /* This is the memory area for common block data. Can be NULL. */
  void *     xref;        /* User definable cross reference link. */
} x_Module;

/*
** Module flags
*/

#define MOD_ELF_RELOC     0x80000000 /* Module is build from a relocatable ELF file (*.o) */
#define MOD_ELF_SHARED    0x40000000 /* Module is build from a shared ELF object file (*.so) */
#define MOD_OK            0x20000001 /* This flag must be set for any operation to succeed on a module. */
#define MOD_LOADED        0x10000000
#define MOD_SYMTAB        0x08000000 /* Module has processed ELF symbol tables allready. */
#define MOD_RESOLVED      0x04000000 /* All symbols have been resolved. */
#define MOD_RELOCATED     0x02000000
#define MOD_INITIALIZED   0x01000000
#define MOD_STRIPPED      0x00800000
#define MOD_UNLOADED      0x00400000

/*
** Function prototypes.
*/

x_status x_module_load(x_module module, unsigned char * buffer, x_malloc m, x_free f);
x_status x_module_resolve(x_module module);
x_status x_module_relocate(x_module module);
x_status x_module_search(x_module module, const char * name, void ** address);
x_status x_module_delete(x_module module);
x_status x_module_init(x_module module);
x_status x_module_strip(x_module module);
x_status x_module_unload(x_module module);
void x_module_iterate(x_boolean (*iterator)(x_module module, void * argument), void * argument);

/*
** Macros
*/

#define OSWALD_MAJOR ((unsigned char) 1)
#define OSWALD_MINOR ((unsigned char) 0)

#define module_ident(name)      static const char mxd_ident[] __attribute__((section(".oswald"))) = { name }
#define module_version(m1, m2)  static const int mxd_version __attribute__((section(".oswald"))) = ((OSWALD_MAJOR << 24) | (OSWALD_MINOR << 16) | ((unsigned char)m1 << 8) | (unsigned char)m2)
#define module_package(package) static const char mxd_package[] __attribute__((section(".oswald"))) = { package }

#endif /* _MODULES_H */
