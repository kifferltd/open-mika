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
** $Id: modules.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** The code that implements the module functionality of Oswald. The only module format supported
** is the ELF format as specified by TIS Portable Format Specification, Version 1.1
*/

#include "elf.h"
#include "modules.h"
#include "symbols.h"

/*
** The list of modules loaded. Note that the first module loaded is
** the module of the kernel itself. It will not contain a separate
** text/data/bss segment but it will contain a symbol stable of exported
** symbols.
*/

static x_module modules;

/*
** Our own copy function.
*/

static void memcopy(void * dst, void * src, x_size num) {

  x_ubyte * d = dst;
  x_ubyte * s = src;
  
  while (num) {
    *d++ = *s++;
    num -= 1;
  }

}

/*
** Strip a module from the helper structures.
*/

x_status x_module_strip(x_module module) {

  if (isNotSet(module->flags, MOD_STRIPPED)) {
    if (module->elf) {
      module->f_free(module->elf);
      module->elf = NULL;
    }
  }

  return xs_success;
  
}

/*
** Utility functions to get at ELF data.
*/

inline static e_rel e_get_rel(x_module module, e_section rel_section, x_int indexx) {
  return (e_rel)(module->elf->data + rel_section->offset + (indexx * rel_section->sizeof_entry));
}

inline static e_symbol e_get_symbol(x_module module, e_section sym_section, x_int indexx) {
  return (e_symbol)(module->elf->data + sym_section->offset + (indexx * sym_section->sizeof_entry));
}

inline static x_int e_num_entries(e_section section) {
  return (section->size / section->sizeof_entry);
}

/*
** Round up a certain 'value' to the given 'rounding' factor.
*/ 

inline static x_size round_up(x_size value, x_size rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

/*
** Starting from an ELF symbol section and the ELF symbol itself, 
** return the name for it as a C string.
*/

const char * symbol2char(x_module module, e_section sym_section, e_symbol symbol) {

  char * strtab = (module->elf->data + module->elf->sections[sym_section->link].offset);
  const char * name = &strtab[symbol->name];

  if (name[0] == '\0') {
    name = "<no name>";
  }
  
  return name;
  
}

/*
** Starting from the section index, return a character string with its name.
*/

char * section2char(x_module module, x_int indexx) {

  e_offset offset = module->elf->sections[module->elf->header->sc_names].offset;
  char * strtab = (module->elf->data + offset);

  return & strtab[module->elf->sections[indexx].name];
    
}

/*
** Get the value of an ELF symbol. This depends on the type of ELF file; see the TIS document
** on page 1-20 "Symbol Values" for more information.
** TODO, make it work for shared objects....
*/

static x_address e_symbol_value(x_module module, e_symbol elf_symbol) {

  x_address address = 0;
  
  if (elf_symbol->section < shn_loreserve) {
    address = module->elf->sections[elf_symbol->section].address + elf_symbol->value;
  }
  
  return address;
  
}

static x_status x_section_relocate(x_module module, e_section rel_section) {

  x_int num_rel = e_num_entries(rel_section);
  e_rel rel;
  e_section sym_section;
  e_section target_section;
  e_symbol elf_symbol;
  x_int i;
  x_int k;
  x_int e;
  x_int type;
  x_address S;
  x_address P;
  x_address A;

  sym_section = &module->elf->sections[rel_section->link];
  // ^ should check that sym_section->type = sc_symtab and complain if not
  target_section = &module->elf->sections[rel_section->info];
  // ^ should check that section flag SHF_ALLOC is set or something

  loempa(9, "Symbol section index %d, %d symtab start at 0x%08x\n", rel_section->link, e_num_entries(sym_section), module->elf->data + sym_section->offset);
  loempa(9, "Target section index %d '%s', type = %d, start = 0x%08x, %d relocations\n", rel_section->info, section2char(module, (x_int)rel_section->info), target_section->type, target_section->address, num_rel);
  
  for (i = 0; i < num_rel; i++) {
    S = 0;
    rel = e_get_rel(module, rel_section, i);
    elf_symbol = e_get_symbol(module, sym_section, e_rel_sym(rel));
    
    /*
    ** See if the symbol is of type global; if yes, then it is either imported or exported, but it
    ** will be in the symbol table of the module already, and we know it is resolved. In any case, we
    ** pass the relocator functions the value of the symbol in 'S'. In case of and non imported/exported
    ** symbol, it's the value of the ELF symbol (see TIS page 1-20) or the value of the symbol table symbol;
    ** in the latter case, we need to check if it's imported (value is pointer to symbol defining the value)
    ** or exported (value in the symbol itself).
    */

//    loempa(9, "+- Symbol '%s' it points to memory in section %d\n", symbol2char(module, sym_section, elf_symbol), elf_symbol->section);
    if (e_sym_bind(elf_symbol) == smb_local) {
      S = e_symbol_value(module, elf_symbol);
    }
    else {
      for (k = 0; k < module->symtab->num_symbols; k++) {
        if (streq(symbol2char(module, sym_section, elf_symbol), module->symtab->symbols[k].ident->string)) {
          if (isSet(module->symtab->symbols[k].flags, SYM_IMPORTED)) {
            // TODO Check if the 'defines' symbol is resolved !!
            S = module->symtab->symbols[k].value.defines->value.address;
          }
          else {
            S = module->symtab->symbols[k].value.address;
          }
          break;
        }
      }

      if (k == module->symtab->num_symbols) {
        loempa(9, "Oops, could not find back symbol '%s' in our own symbol table.\n", symbol2char(module, sym_section, elf_symbol));
        return xs_unresolved;
      }

    }

    /*
    ** We pass the addend 'A' explicitly to the relocator functions; so based on the relocation section type
    ** we get the addend explicitly from the relocation record from the e_rela->addend field or we get it from the
    ** word we need to patch up, in the target section (e_Rel). The variable 'P' is a pointer to the 32 bit
    ** word we need to patch, and in the case of an e_Rel relocation record, it also contains our addend.
    */
        
    P = target_section->address + rel->offset;

    if (rel_section->type == sc_rela) {
      A = ((e_rela)rel)->addend;
    }
    else {
      A = *(x_address *)P;
    }
    
    /*
    ** Address is the ELF symbol value (see page 1-20 of the ELF standard). It's meaning
    ** changes with the type of file. We call a cpu specific relocation function which we
    ** pass all necessary ingredients to cook us a nice relocation value and patch it into
    ** the P position. Note that we don't bother calling the relocator function when the type
    ** is 0 or NONE.
    */

    type = e_rel_type(rel);
    if (type > 0) {
      e = x_cpu_relocate(type, S, A, P);
      if (e != 0) {
        return xs_rel_error;
      }
    }
  
  }

  return xs_success;
  
}

/*
** Count the number of symbols exported or imported, for a certain symbol section. The passed counter
** is updated when we walk over the ELF symbol table entries. Only the symbols
** that are imported or exported by this module, will end up in the symbol table of the module. All other
** remaining ELF symbols are used in the relocation process only.
*/

inline static void x_symbols_count(x_module module, e_section sym_section, x_size * num_symbols) {

  e_symbol symbol;
  x_int i;


  /*
  ** Walk over all the symbols of this section. Note that entry index 0 is reserved and we should not look
  ** at it. When a symbols scope is global, it is accounted for; when its type and section are undefined,
  ** it is imported, otherwise it's exported, but ...
  **
  ** ... don't count symbols that are defined relative to the .oswald section as they are special module symbols
  ** that don't wind up in our module symbol table.
  */
  
  for (i = 1; i < e_num_entries(sym_section); i++) {
    symbol = e_get_symbol(module, sym_section, i);
    if (e_sym_bind(symbol) == smb_global && module->elf->oswald != symbol->section) {
      *num_symbols += 1;
    }
  }
    
}

/*
** Try to link up an unresolved symbol with a symbol that is exported and defines the
** address of the symbol. Returns the symbol that defines the unresolved symbol and will
** do the linkage process.
*/

inline static x_symbol x_symbol_resolve(x_symbol unresolved) {

  x_symbol defines;
  
  for (defines = unresolved->ident->symbols; defines; defines = defines->next) {
    if (isSet(defines->flags, SYM_EXPORTED) && isNotSet(defines->flags, SYM_SPECIAL)) {
      x_symbol_refs(defines, +1);
      unresolved->value.defines = defines;
      setFlag(unresolved->flags, SYM_RESOLVED);
      loempa(9, "Resolved %30s: address = 0x%08x, %2d referrals\n", unresolved->ident->string, defines->value.address, x_symbol_refs(defines, 0));
      break;
    }
  }

  return defines;
  
}

/*
** Fill in the module symbol table with information from the ELF symbol table, only for these
** symbols we either import or export. See also the x_symbols_count function. For exported 
** symbols, we can already give the address at which they can be found.
*/

inline static x_status x_symtab_resolve(x_module module, e_section sym_section) {

  x_symtab symtab = module->symtab;
  e_symbol elf_symbol;
  x_symbol symbol = symtab->symbols;
  x_symbol check;
  x_int i;
  x_ident ident;
  x_uword version;

  /*
  ** Start at the point where we left of the previous time this function was called. We use
  ** the symbols[x].ident == NULL comparison as an indicator that entry 'x' has not been bound
  ** to an ELF symbol yet and is thus the first uninitialized symbol.
  */
  
  for (i = 0; i < symtab->num_symbols; i++) {
    if (symtab->symbols[i].ident == NULL) {
      symbol = & symtab->symbols[i];
      break;
    }
  }

  /*
  ** See also the notes in the previous function x_symbols_count. 
  ** When an ELF symbol is either imported or exported, we search for or possibly create 
  ** the correct identifier and link in the symbol in the list of symbols of this identifier 
  ** and set the correct set of flags.
  */

  for (i = 1; i < e_num_entries(sym_section); i++) {
    elf_symbol = e_get_symbol(module, sym_section, i);
    if (e_sym_bind(elf_symbol) == smb_global) {
      ident = x_ident_search(symbol2char(module, sym_section, elf_symbol), true);
      if (ident == NULL) {
        return xs_no_mem;
      }
 
      /*
      ** See if it concerns 'special' symbols from the .oswald section first. Note that we check upon 
      ** the type of 'mxd_' label by means of checking a single character only.
      */
 
      if (module->elf->oswald == elf_symbol->section) {
        if (ident->string[4] == 'i') {
          loempa(9, "mxd_ident '%s'\n", (char *)(module->elf->oswald_data + elf_symbol->value));
          module->ident = x_ident_search(module->elf->oswald_data + elf_symbol->value, true);
        }
        else if (ident->string[4] == 'v') {
          version = *(x_uword *)(module->elf->oswald_data + elf_symbol->value);
          module->m_minor = (x_ubyte) (version >>  0);
          module->m_major = (x_ubyte) (version >>  8);
          module->o_minor = (x_ubyte) (version >> 16);
          module->o_major = (x_ubyte) (version >> 24);
          loempa(9, "Module version numbers: Oswald %u.%u   Module %u.%u\n", module->o_major, module->o_minor, module->m_major, module->m_minor);
        }
        else if (ident->string[4] == 'p') {
          loempa(9, "mxd_package '%s'\n", (char *)(module->elf->oswald_data + elf_symbol->value));
          module->package = x_ident_search(module->elf->oswald_data + elf_symbol->value, true);
        }
        
        continue;
      }
      
      /*
      ** Not a symbol from the .oswald section, proceed further...
      */
      
      if (streq(ident->string, "init_module") && e_sym_type(elf_symbol) == smt_func) {
        symbol->flags = (SYM_SPECIAL | SYM_INIT);
      }
      else if (streq(ident->string, "clean_module") && e_sym_type(elf_symbol) == smt_func) {
        symbol->flags = (SYM_SPECIAL | SYM_CLEAN);
      }
      else {
        symbol->flags = 0;
      }

      // this is wrong, doesn't take COMMON sections into account, they are imported!! fix this !!
      if (e_sym_type(elf_symbol) == smt_notype && elf_symbol->section == shn_undef) {
        loempa(9, "  imports '%s'\n", ident->string);
        symbol->flags |= SYM_IMPORTED;
      }
      else {
        loempa(9, "  exports '%s'\n", ident->string);
        
        /*
        ** Check if this is a COMMON symbol by looking at the section. If it is, we see if this
        ** symbol is already defined somewhere and is exported. If that is the case, we make
        ** it an imported symbol and set the value to the found value. If not, we increase the size
        ** of the COMMON area so that we account for the size we will allocate later and assign an
        ** offset in this common area in the symbol->value.offset field. 
        **
        ** If it is not a common symbol, we can already resolve it.
        */
        
        if (elf_symbol->section == shn_common) {

          symbol->flags |= SYM_COMMON;
          for (check = ident->symbols; check; check = check->next) {
            if (isSet(check->flags, SYM_EXPORTED)) {
              symbol->value.defines = check;
              x_symbol_refs(check, +1);
              symbol->flags |= (SYM_IMPORTED | SYM_RESOLVED);
              loempa(9, "Found common symbol back...\n");
              break;
            }
          }
          
          /*
          ** Did we find the common symbol back in the symbol tables? If not, we need to add the required
          ** space for it into our elf block and store the offset in the symbol itself for later; when we
          ** have allocated space for we will assign the correct address. Note that the ELF symbol 'value'
          ** field contains the alignment requirements and 'size' the number of bytes to allocate.
          */
          
          if (check == NULL) {
            symbol->value.offset = round_up(module->elf->common_offset, elf_symbol->value);
            module->elf->common_offset += elf_symbol->size;
            symbol->flags |= SYM_EXPORTED;
            loempa(9, "Exported common %20s: %5d bytes, %2d alignment, offset = %5d, total common = %6d bytes\n", ident->string, elf_symbol->size, elf_symbol->value, symbol->value.offset, module->elf->common_offset);
          }
          
        }
        else {
        
          /*
          ** Check if the symbol is defined already and bark if this is the case.
          */
        
          for (check = ident->symbols; check; check = check->next) {
            if (isSet(check->flags, SYM_EXPORTED) && isNotSet(symbol->flags, SYM_SPECIAL)) {
              loempa(9, "Symbol '%s' already defined: address = 0x%08x, module = '%s'\n", ident->string, check->value.address, x_symbol2symtab(check)->module->ident->string);
              return xs_sym_defined;
            }
          }
        
          /*
          ** Non common symbols that this module exports, can already be resolved since
          ** the appropriate sections have been transferred to memory, and 
          ** the section->address fields for these sections have been updated.
          */
        
          symbol->value.address = e_symbol_value(module, elf_symbol);
          symbol->flags |= (SYM_EXPORTED | SYM_RESOLVED);
          
        }

        if (e_sym_type(elf_symbol) == smt_func) {
          symbol->flags |= SYM_FUNCTION;
        }
        else {
          symbol->flags |= SYM_VARIABLE;
        }
        
      }
      
      symbol->ident = ident;
      symbol->next = ident->symbols;
      ident->symbols = symbol;
      symbol += 1;
    }
  }

  return xs_success;
    
}

/*
** Load a module. Allocate the ELF helper structures, check the ELF data and copy over the
** runtime structures from the ELF data which is presumed as read-only.
*/

x_status x_module_load(x_module module, unsigned char * data, x_malloc f_alloc, x_free f_free) {

  e_header header;
  e_section s;
  x_elf elf;
  x_ubyte * address;
  x_int i;
  x_symtab symtab;
  x_int num_symbols;

  x_size size;
  x_size offset;
  x_address aligned;
  x_size slack;
  x_ubyte * fence;

  module->ident = x_ident_search("", true);
  module->f_alloc = f_alloc;
  module->f_free = f_free;
  module->flags = 0x00000000;
  module->area_2 = NULL;
  
  /*
  ** Do some checks on the header and structure sizes.
  */
  
  header = (e_header)data;
  
  /*
  ** Allocate an x_Elf helper structure of the appropriate size and copy over the section 
  ** header entries. Note that the oswald section number is set to the impossible number
  ** of -1 as initialization.
  */
  
  elf = f_alloc(sizeof(x_Elf) + (sizeof(e_Section) * header->num_sections));
  if (elf == NULL) {
    return xs_no_mem;
  }
  
  elf->num_sections = header->num_sections;
  elf->oswald = -1;
  elf->oswald_data = NULL;
  elf->header = header;
  elf->data = data;
  elf->module = module;
  elf->common_offset = 0;
  module->elf = elf;
loempa(9, "Here 0x%08x to 0x%08x %d bytes\n", data + header->sections, elf->sections, sizeof(e_Section) * header->num_sections);
  memcopy(elf->sections, data + header->sections, sizeof(e_Section) * header->num_sections);
loempa(9, "Here\n");

  /*
  ** Determine the total size of the sections for which we need to allocate
  ** memory. 
  */

  size = 32 - ALLOC_ALIGNMENT;
  offset = 0;
  slack = 0;
  for (i = 0; i < header->num_sections; i++) {
    s = & elf->sections[i];
    if (isSet(s->flags, SHF_ALLOC)) {
      if (streq(section2char(module, i), ".oswald")) {
        module->elf->oswald = i;
        module->elf->oswald_data = f_alloc(s->size);
        loempa(9, "Found .oswald section as section number %d.\n", i);
      }
      else {
        aligned = round_up(offset, s->alignment);
        slack = aligned - offset;
        size += s->size + slack;
        offset = aligned;
        loempa(9, "Section %2d : %4d bytes, cumulative %4d bytes, alignment waste = %d bytes\n", i, s->size, size, s->alignment, slack);
        offset += s->size;
      }
    }
  }

  loempa(9, "Total of %d bytes required.\n", size);

  /*
  ** Allocate a block of memory and copy over section contents while
  ** setting the section->address to the correct value. We watch out for the address
  ** alignment requirements.
  */

  module->area_1 = f_alloc(size);
  address = module->area_1;
  if (address == NULL) {
    return xs_no_mem;
  }

  fence = address + size;
  loempa(9, "Area starts at 0x%08x (%d)\n", address, address);
  for (i = 0; i < header->num_sections; i++) {
    s = & elf->sections[i];
    if (isSet(s->flags, SHF_ALLOC)) {
      if (i == module->elf->oswald) {
        memcopy(module->elf->oswald_data, data + s->offset, s->size);
        s->address = (e_address)module->elf->oswald_data;
        loempa(9, "%-10s : %4d bytes, start = 0x%08x.\n", section2char(module, i), s->size, module->elf->oswald_data);
      }
      else {
        address = (x_ubyte *)round_up((x_size)address, s->alignment);
        memcopy(address, data + s->offset, s->size);
        s->address = (e_address)address;
        address += s->size;
        if (address > fence) {
          loempa(9, "ERROR %p - %p = 0x%08x (%d)\n", address, fence, address - fence, address - fence);
          return xs_mod_error;
        }
        loempa(9, "%-10s : %4d bytes, start = 0x%08x, next start = 0x%08x, alignment = %2d %sOK\n", section2char(module, i), s->size, s->address, s->address + s->size, s->alignment, (s->address % s->alignment == 0) ? "" : "NOT ");
      }
    }
  }

  loempa(9, "CHECK %p - %p = 0x%08x (%d) <- should be negative or 0\n", address, fence, address - fence, address - fence);

  /*
  ** For each symbol table section, count the imports and exports, allocate the symbol table 
  ** and set it up.
  */
  
  num_symbols = 0;
  for (i = 0; i < header->num_sections; i++) {
    s = & elf->sections[i];
    if (s->type == sc_symtab) {
      x_symbols_count(module, s, &num_symbols);
    }
  }
  
  loempa(9, "Found %d symbols to create a table for.\n", num_symbols);

  symtab = x_symtab_create(num_symbols, f_alloc);
  if (symtab == NULL) {
    return xs_no_mem;
  }
  
  symtab->num_symbols = num_symbols;
  symtab->module = module;
  module->symtab = symtab;

  /*
  ** All OK, the module is now ready to resolve the internal and external
  ** symbols.
  */
  
  setFlag(module->flags, MOD_LOADED);

  module->next = modules;
  modules = module;
  
  return xs_success;
  
}

x_status x_module_resolve(x_module module) {

  x_status status;
  x_int i;
  e_section s;
  x_symbol symbol;
  x_ubyte * address;
  x_size unresolved = 0;

  if (isNotSet(module->flags, MOD_LOADED)) {
    return xs_seq_error;
  }

  if (isSet(module->flags, MOD_RESOLVED)) {
    loempa(9, "Module 0x%08x has been resolved already (flags = 0x%08x).\n", module, module->flags);
    return xs_success;
  }  

  loempa(9, "*** Resolving module 0x%08x\n", module);

  if (isNotSet(module->flags, MOD_SYMTAB)) {

    for (i = 0; i < module->elf->header->num_sections; i++) {
      s = & module->elf->sections[i];
      if (s->type == sc_symtab) {
        status = x_symtab_resolve(module, s);
        if (status != xs_success) {
          return status;
        }
      }
    }
    
    /*
    ** Now see if we have any common block allocation to do and eventually
    ** resolving of exported common symbols. elf->common_offset will contain
    ** the size of the common memory to allocate. Alignment requirements have
    ** been taken in account in x_symtab_resolve, so we only have to make sure that
    ** the first address we start from is aligned to the highest requirements.
    */

    if (module->elf->common_offset) {
      loempa(9, "Total of %d bytes for exported common block symbols.\n", module->elf->common_offset);
      address = module->f_alloc(module->elf->common_offset + (32 - ALLOC_ALIGNMENT));
      if (! address) {
        return xs_no_mem;
      }
      module->area_2 = address;
      address = (x_ubyte *)round_up((x_size)address, 32);
      for (i = 0; i < module->symtab->num_symbols; i++) {
        symbol = & module->symtab->symbols[i];
        if (isSet(symbol->flags, SYM_COMMON) && isSet(symbol->flags, SYM_EXPORTED)) {
          if (isNotSet(symbol->flags, SYM_RESOLVED)) {
            symbol->value.address = (x_address)address + symbol->value.offset;
            setFlag(symbol->flags, SYM_RESOLVED);
            loempa(9, "Exported common %20s: address = 0x%08x\n", symbol->ident->string, symbol->value.address);
          }
        }
      }
    }

    setFlag(module->flags, MOD_SYMTAB);

  }

  /*
  ** Link up the 'imported' symbols by looking for 'exported' symbols with the
  ** same identifier. If this fails, because we can not find back a required symbol, 
  ** we quit.
  */

  for (i = 0; i < module->symtab->num_symbols; i++) {
    symbol = & module->symtab->symbols[i];
    if (isSet(symbol->flags, SYM_IMPORTED) && isNotSet(symbol->flags, SYM_RESOLVED)) {
      if (! x_symbol_resolve(symbol)) {
        unresolved += 1;
        loempa(9, "Unresolved symbol '%s', running total of %d unresolved symbols.\n", symbol->ident->string, unresolved);
      }
    }
  }

  if (! unresolved) {
    setFlag(module->flags, MOD_RESOLVED);
    return xs_success;
  }
  else {
    loempa(9, "%d of %d imported and exported symbols remain unresolved.\n", unresolved, module->symtab->num_symbols);
    return xs_unresolved;
  }
  
}

x_status x_module_relocate(x_module module) {

  x_status status;
  x_int i;
  e_section s;

  if (isNotSet(module->flags, MOD_RESOLVED)) {
    return xs_seq_error;
  }

  if (isSet(module->flags, MOD_RELOCATED)) {
    loempa(9, "Module has been relocated already.\n");
    return xs_success;
  }  

  /*
  ** OK, we have now all symbols in our own symbol table resolved, i.e. they have valid addresses;
  ** this has been done in the previous step for imported symbols and in x_symtab_resolve for
  ** exported symbols. We now try to do the relocations; patience my dear, we're nearly there...
  */
  
  for (i = 0; i < module->elf->header->num_sections; i++) {
    s = & module->elf->sections[i];
    if (s->type == sc_rel) {
      loempa(9, "*** Processing relocation section '%s', index %d.\n", section2char(module, i), i);
      status = x_section_relocate(module, s);
      if (status != xs_success) {
        return status;
      }
    }
  }

  setFlag(module->flags, MOD_RELOCATED);

  return xs_success;
  
}

/*
** Run a special function of a module (initializer, cleaner, translator)
*/

static x_status x_module_run(x_module module, x_flags type) {

  x_int i;
  x_mod_special special = NULL;

  for (i = 0; i < module->symtab->num_symbols; i++) {
    if (isSet(module->symtab->symbols[i].flags, type)) {
      special = (x_mod_special)module->symtab->symbols[i].value.address;
      (*special)();
      return xs_success;
    }
  }

  return xs_no_instance;

}

/*
** Run the initializer of a module. This is the last step before a module can be declared
** OK. This function has no effect if the initializer ran already. If the initializer ran
** OK, the module is declared OK and normal operation can start.
*/

x_status x_module_init(x_module module) {
  
  x_status status;

  if (isNotSet(module->flags, MOD_RELOCATED)) {
    return xs_seq_error;
  }

  if (isSet(module->flags, MOD_INITIALIZED)) {
    loempa(9, "Module has been initialized already.\n");
    return xs_success;
  }  
  
  status = x_module_run(module, SYM_INIT);

  if (status == xs_success) {
    setFlag(module->flags, MOD_OK | MOD_INITIALIZED);
  }


  return status;

}

/*
** Lookup the address of an exported function or variable
*/

x_status x_module_search(x_module module, const char * name, void ** address) {

  x_int i;

  if (isNotSet(module->flags, MOD_RESOLVED)) {
    loempa(9, "Module error, flags = 0x%08x\n", module->flags);
    return xs_seq_error;
  }
    
  for (i = 0; i < module->symtab->num_symbols; i++) {
    if (streq(name, module->symtab->symbols[i].ident->string)) {
      *address = (void *)module->symtab->symbols[i].value.address;
      loempa(9, "Found symbol for '%s:%s' address = 0x%08x\n", module->ident->string, name, address);
      return xs_success;
    }
  }
  
  return xs_no_instance;
}

/*
** Run the cleaner function of a module. The module must be declared OK, before this
** function can start. After successful operation of the cleaner, the module is declared to be
** in an non operational state, by unsetting the OK flag and setting the MOD_UNLOADED flag.
*/

x_status x_module_unload(x_module module) {

  x_status status;
  
  if (isNotSet(module->flags, MOD_OK)) {
    loempa(9, "Module error, flags = 0x%08x\n", module->flags);
    return xs_mod_error;
  }

  status = x_module_run(module, SYM_CLEAN);

  if (status == xs_success) {
    unsetFlag(module->flags, MOD_OK);
    setFlag(module->flags, MOD_UNLOADED);
  }

  return status;

}

x_status x_module_delete(x_module module) {
  return 0;
}

void x_module_iterate(x_boolean (*iterator)(x_module module, void * argument), void * argument) {

  x_module module;
  
  for (module = modules; module; module = module->next) {
    if (! ((*iterator)(module, argument))) {
      break;
    }
  }

}
