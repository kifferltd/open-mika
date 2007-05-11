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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>

void compile_module(const char * cc, const char * includes, const char * src, const char * obj) {

  char * syscmd;
  const unsigned int cmdlen = (1024 * 10);
  int result;
  
  syscmd = calloc(1, cmdlen);
  sprintf(syscmd, "%s %s -c %s -o %s", cc, includes, src, obj);
  result = system(syscmd);
  free(syscmd);
  
}

void write_module(FILE * o, const char * src, const char * obj, int i, int last) {

  struct stat fdinfo;
  int fd;
  unsigned int j;
  unsigned char * buffer;
  unsigned int size;

  fd = open(obj, O_RDONLY);
  if (fd == -1) {
    fprintf(stderr, "Could not open file '%s'\n", obj);
    exit(1);
  }
  
  fstat(fd, &fdinfo);
  size = (unsigned int)fdinfo.st_size;
  buffer = calloc(1, size);
  fprintf(stdout, "Reading data from %s -> %s %d bytes.\n", src, obj, size);
  read(fd, buffer, size);
  close(fd);

  fprintf(o, "/*\n** Data read from %s\n*/\n\n", obj);
  fprintf(o, "static Module Module_%d = {\n", i);
  if (last) {
    fprintf(o, "  (struct Module *)0,\n");
  }
  else {
    fprintf(o, "  & Module_%d,\n", i + 1);
  }
  fprintf(o, "  \"%s\",\n", src);
  fprintf(o, "  %d,\n", size);
  fprintf(o, "  {\n    ");
  
  for (j = 0; j < size; j += 1) {
    fprintf(o, "0x%02x, ", buffer[j]);
    if (j % 32 == 0 && j != 0) {
      fprintf(o, "\n    ");
    }
  }
  fprintf(o, "\n");
  
  fprintf(o, "  }\n");
  fprintf(o, "};\n");
  fprintf(o, "\n");
  fprintf(o, "\n");
  
  free(buffer);

}

int main(int argc, char * argv[]) {

  FILE * o;
  int i;
  char * o_name;
  char ** module_src;
  char ** module_obj;
  char * temp_1;
  char * temp_2;
  const unsigned int buffer_size = (1024 * 50); // Yes, I think that's enough...
  int num_module = 0;

  if (argc < 5) {
    for (i = 1; i < argc; i++) {
      fprintf(stderr, "arg %d = '%s'\n", i, argv[i]);
    }
    fprintf(stderr, "create_modules CPU HOST CC outfile.c module1.c [module2.c ...]\n");
    exit(1);
  }

  fprintf(stdout, "Creating module data with CPU = '%s' HOST = '%s' CC = '%s'\n", argv[1], argv[2], argv[3]);

  num_module = argc - 5;
  module_src = malloc(sizeof(char *) * num_module);
  module_obj = malloc(sizeof(char *) * num_module);

  temp_1 = malloc(buffer_size);
  temp_2 = malloc(buffer_size);
  for (i = 5; i < argc; i++) {
    module_src[i - 5] = argv[i];
    memset(temp_1, 0x00, buffer_size);
    sprintf(temp_1, "/tmp/module_XXXXXX");
    mkstemp(temp_1);
    sprintf(temp_1 + strlen(temp_1), ".o");
    module_obj[i - 5] = malloc(sizeof(char) * (strlen(temp_1) + 1));
    strcpy(module_obj[i - 5], temp_1);
  }

  for (i = 0; i < num_module; i++) {
    printf("Creating object code for '%s'.\n", module_src[i]);
  }

  /*
  ** Get working path and create the necessary include headers. Use the first
  ** given module source file as a reference.
  */

  memset(temp_1, 0x00, buffer_size);
  memcpy(temp_1, module_src[0], (unsigned int)(strrchr(module_src[0], '/') - module_src[0]));
  memset(temp_2, 0x00, buffer_size);
  memcpy(temp_2, temp_1, (unsigned int)(strrchr(temp_1, '/') - temp_1));
  memset(temp_1, 0x00, buffer_size);
  sprintf(temp_1, "-I%s/include -I%s/hal/cpu/%s/include -I%s/hal/host/%s/include", temp_2, temp_2, argv[1], temp_2, argv[2]);

  /*
  ** Compile all the module files.
  */

  for (i = 0; i < num_module; i++) {
    compile_module(argv[3], temp_1, module_src[i], module_obj[i]);
  }

  /*
  ** Generate the source header.
  */

  o_name = argv[4];
  o = fopen(o_name, "w");

  fprintf(o, "/*\n");
  fprintf(o, "** Object code of modules\n");
  for (i = 0; i < num_module; i++) {
    fprintf(o, "**   %s\n", module_src[i]);
  }
  fprintf(o, "*/\n\n");

  fprintf(o, "typedef struct Module {\n");
  fprintf(o, "  struct Module * next;\n");
  fprintf(o, "  const char * name;\n");
  fprintf(o, "  int size;\n");
  fprintf(o, "  unsigned char data[0];\n");
  fprintf(o, "} Module;\n");
  fprintf(o, "\n");

  for (i = num_module - 1; i >= 0; i--) {
    write_module(o, module_src[i], module_obj[i], i, i == (num_module - 1));
  }

  fprintf(o, "struct Module * first_Module = & Module_0;\n");

  fclose(o);
  
  free(temp_1);
  free(temp_2);
  for (i = 0; i < num_module; i++) {
    remove(module_obj[i]);
    free(module_obj[i]);
  }

  return 0;
  
}
