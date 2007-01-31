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

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include "tests.h"
#include "modules.h"
#include "elf.h"
#include "symbols.h"

#define MODT_STACK_SIZE (1024 * 4)

static x_thread mod_thread;

/*
** Note that this is a redeclaration of the
** same structure as generated in the modules_data.c file.
** We redeclare it here to spare us the hassle with header files.
*/

typedef struct Module {
  struct Module * next;
  const char * name;
  int size;
  unsigned char data[0];
} Module;

extern Module * first_Module;

static x_Module Static_module;
static x_module static_module = &Static_module;

int c = 10;
extern int is_an_exported_function(int a);

extern x_symtab x_symtab_sample(void);

x_status x_symtab_insert(x_symtab symtab, const char * label, void * address, x_flags flags) {

  x_ident ident;

  if (symtab->num_symbols < symtab->capacity) {
    ident = x_ident_search(label, true);
    symtab->symbols[ symtab->num_symbols ].next = NULL;
    symtab->symbols[ symtab->num_symbols ].flags = (SYM_EXPORTED | SYM_RESOLVED | flags);
    symtab->symbols[ symtab->num_symbols ].value.address = (x_address)address;
    symtab->symbols[ symtab->num_symbols ].ident = ident;
    ident->symbols = & symtab->symbols[ symtab->num_symbols ];
    
    oempa("%2d : address 0x%08x -> '%-s'\n", symtab->num_symbols, symtab->symbols[symtab->num_symbols].value.address, label);

    symtab->num_symbols += 1;

    return xs_success;
    
  }
  
  return xs_no_instance;
  
}

x_symtab symtab;

void x_symtab_kernel(void) {

   symtab = x_symtab_sample();

   x_symtab_insert(symtab, "printf",                    printf,                    SYM_FUNCTION);

   static_module->ident = x_ident_search("kernel", true);     
   static_module->symtab = symtab;
   symtab->module = static_module;

}

int is_an_exported_function(int a) {
  oempa("Called '%s' in = %d out = %d.\n", __FUNCTION__, a, a + 100);
  return a + 100;
}

x_boolean ident_iterator(x_ident ident, void * argument) {

  x_size num = 0;
  x_symbol symbol;
  char buffer[100];
  
  if (ident->symbols == NULL) {
    oempa("Identifier '%s' no symbols attached.\n", ident->string);
  }
  else {
    for (symbol = ident->symbols; symbol; symbol = symbol->next) {
      num += 1;
    }
    oempa("Identifier '%s' has %d symbols attached:\n", ident->string, num);
    for (symbol = ident->symbols; symbol; symbol = symbol->next) {
      oempa("  0x%08x : flags = 0x%08x, deps = %d, value = 0x%08x '%s:%s'\n", symbol, symbol->flags, x_symbol_refs(symbol, 0), symbol->value.address, x_symbol2symtab(symbol)->module->ident->string, symbol->ident->string);
      if (x_symbol_java((x_module)argument, symbol, buffer, 100)) {
        oempa(" java symbol : %s.%s%s\n", buffer + buffer[0], buffer + buffer[2], buffer + buffer[4]);
      }
    }
  }
  
  return true;
  
}

x_boolean module_iterator(x_module module, void * argument) {

  x_int num = *(x_int *)argument;
  x_int i;
  x_symbol s;

  if (isNotSet(module->flags, MOD_SYMTAB)) {
    oempa("Module not resolved yet...\n");
    return true;
  }
    
  oempa("Module %2d, %15s, 0x%08x, flags = 0x%08x\n", num, module->ident->string, module, module->flags);
  for (i = 0; i < module->symtab->num_symbols; i++) {
    s = & module->symtab->symbols[i];
  }
  
  num++;
  *(x_int *)argument = num;
  
  return true;
  
}

typedef int (*adder)(int a, int b);

void modules_entry(void * t) {

  x_status status;
  void * address;
  adder thefunc;
  int * parameter;

  x_Module Module_1;
  x_Module Module_2;

  x_module module_1 = &Module_1;
  x_module module_2 = &Module_2;

  /*
  ** For paranoia
  */
  
  memset(module_1, 0xff, sizeof(x_Module));
  memset(module_2, 0xff, sizeof(x_Module));

  x_symtab_kernel();
  
  status = x_module_load(module_1, first_Module->data, x_mem_alloc, x_mem_free);
  if (status != xs_success) {
    oempa("Loading 1 failed %d\n", status);
    exit(0);
  }
  
  status = x_module_load(module_2, first_Module->next->data, x_mem_alloc, x_mem_free);
  if (status != xs_success) {
    oempa("Loading 2 failed %d\n", status);
    exit(0);
  }

  status = x_module_resolve(module_1);
  if (status != xs_success) {
    oempa("Resolving failed %d for the first time !! NORMAL !!\n", status);
  }

  status = x_module_resolve(module_2);
  if (status != xs_success) {
    oempa("Resolving failed %d\n", status);
    exit(0);
  }

  status = x_module_resolve(module_1);
  if (status != xs_success) {
    oempa("Resolving failed %d for the second time !! NOT NORMAL !!\n", status);
    exit(0);
  }

  status = x_module_relocate(module_1);
  if (status != xs_success) {
    oempa("Relocation failed %d\n", status);
    exit(0);
  }

  status = x_module_relocate(module_2);
  if (status != xs_success) {
    oempa("Relocation failed %d\n", status);
    exit(0);
  }

  status = x_module_search(module_1, "parameter", (void **)&parameter);
  if (status == xs_success) {
    oempa("(1) Old parameter is %d (0x%08x), setting new value 200.\n", *parameter, *parameter);
    *parameter = 200;
  }
  else {
    oempa("Not found.\n");
    exit(0);
  }
    
  status = x_module_init(module_1);
  if (status != xs_success) {
    oempa("Initialisation failed %d\n", status);
    exit(0);
  }

  status = x_module_init(module_2);
  if (status != xs_success) {
    oempa("Initialisation failed %d\n", status);
    exit(0);
  }

  oempa("(2) Old parameter is %d (0x%08x), setting new value 300.\n", *parameter, *parameter);
  *parameter = 300;

  status = x_module_search(module_1, "do_add", &address);
  if (status == xs_success) {  
    thefunc = (adder)address;
    oempa("Address is %p\n", address);
    oempa("result = %d\n", (*thefunc)(10, 20));
  }
  else {
    oempa("Not found.\n");
  }

  status = x_module_search(module_2, "second_add", &address);
  if (status == xs_success) {  
    thefunc = (adder)address;
    oempa("2 Address is %p\n", address);
    oempa("2 result = %d\n", (*thefunc)(10, 20));
  }
  else {
    oempa("Not found.\n");
  }

  status = x_module_strip(module_1);
  if (status != xs_success) {
    oempa("Stripping failed %d\n", status);
    exit(0);
  }

  status = x_module_strip(module_2);
  if (status != xs_success) {
    oempa("Stripping failed %d\n", status);
    exit(0);
  }

  status = x_module_unload(module_1);
  if (status != xs_success) {
    oempa("Unloading failed %d\n", status);
    exit(0);
  }

  status = x_module_unload(module_2);
  if (status != xs_success) {
    oempa("Unloading failed %d\n", status);
    exit(0);
  }

  oempa("Testing modules done...\n");
  fflush(stdout);

}

x_ubyte * modules_test(x_ubyte * memory) {

  x_status status;
  
  mod_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(mod_thread, modules_entry, mod_thread, x_alloc_static_mem(memory, MODT_STACK_SIZE), MODT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("Status is '%s'\n", x_status2char(status));
    exit(0);
  }

  return memory;

}
