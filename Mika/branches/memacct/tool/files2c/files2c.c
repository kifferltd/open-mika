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
** $Id: files2c.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>
#include <assert.h>

#define CHUNK_BUF_SIZE	1024

typedef struct fileChunk {
  struct fileChunk *next;
  int size;
  char chunk[CHUNK_BUF_SIZE];
} fileChunk;

#define BUF_SIZE 1024

/** Remove zero or one '.' followed by one or more '/' from the beginning of a filename.
*/
char *strip_leading_fluff(const char *name) {
  char *stripped = (char *)name;
  while(*stripped == '.' && *(stripped+1) == '/') {
    stripped++;
  }
  while(*stripped == '/') {
    stripped++;
  }

  return stripped;
}

/** Replace '.', '/', '$' by '_dot_', '_slash_', '_dollar_' respectively.
*/
char *munge(const char *name) {

  static char buffer[BUF_SIZE];
  int i;
  char *proper = buffer;
  char *cursor = (char *)name;

  memset(buffer, '\0', BUF_SIZE);

  i = 0;
  while (*cursor) {
    if (*cursor == '.') {
      strcpy(proper + i, "_dot_");
      i += strlen("_dot_") - 1;
    }
    else if (*cursor == '/') {
      strcpy(proper + i, "_slash_");
      i += strlen("_slash_") - 1;
    }
    else {
      proper[i] = *cursor;
    }
    cursor++;
    i++;
  }

  return proper;
  
}

int dumpSingleFile(FILE *out, char *name) {

  int fd;
  fileChunk *firstChunk;
  fileChunk *current;
  fileChunk *previous;
  int chunksRead;
  int bytesRead;
  char *dataArray;
//  char *className;
//  char *dot;
  int idx;
  int i;
  int j;

  fd = open(name, O_RDONLY);
  if (fd == -1) {
    fprintf(stderr, "Could not open file '%s'\n", name);
    perror("reason");
    return -1;
  }

  firstChunk = (fileChunk *)calloc(1, sizeof(fileChunk));
  assert(firstChunk != NULL);
  current = firstChunk;

//  className = slashes2underscore(name);
//  dot = strrchr(className, '.');
//  if (dot) {
//    *dot = '\0';
//  }

  chunksRead = 1;
  current->next = NULL;
  while (1) {
    current->size = read(fd, current->chunk, CHUNK_BUF_SIZE);
    if (current->size == CHUNK_BUF_SIZE) {
      current->next = (fileChunk *)malloc(sizeof(fileChunk));
      current = current->next;
      current->next = NULL;
      chunksRead += 1;
    }
    else {
      break;
    }
  }

  /*
  ** Bytes read is all chunks - 1 * buffer size + the size of the
  ** last chunk.
  */

  bytesRead = (CHUNK_BUF_SIZE * (chunksRead - 1)) + current->size;

  dataArray = (char *)malloc((unsigned int)bytesRead);
  idx = 0;

  previous = NULL;
  for (current = firstChunk; current; current = current->next) {
    memcpy(dataArray + idx, current->chunk, (unsigned int)current->size);
    idx += CHUNK_BUF_SIZE;
    if (previous) free(previous);
    previous = current;
  }
  free(previous);

  /*
  ** Now dump the file to the output
  */

  i = 0;
  fprintf(out, "/*\n** Data for %s\n*/\n\n", name);
  fprintf(out, "w_byte %s_data[] = {\n", munge(name));
  while (i <= bytesRead) {
    fprintf(out, "  ");
    for (j = 0; j < 24; j++) {
      fprintf(out, "0x%02x, ", (unsigned char)dataArray[i++]);
      if (i > bytesRead) {
        break;
      }
    }
    fprintf(out, "\n");
  }
  fprintf(out, "};\n\n"); fflush(out);

  fprintf(out, "w_FileData %s = {\n", munge(name));
  fprintf(out, "  NULL,\n");
  fprintf(out, "  \"file:///%s\",\n", name);
  fprintf(out, "  %d,\n", bytesRead);
  fprintf(out, "  %s_data\n};\n\n", munge(name));

  free(dataArray);
  close(fd);
  
  return 0;
  
}

int main(int argc, char *argv[]) {

  int numfiles;
  char **files;
  int i;

  char *outputFile;
  FILE *out;
  
  if (argc < 3) {
    fprintf(stderr, "%s <output file> <data files...>\n", argv[0]);
    fprintf(stderr, "Dumps files in C source code to output file.\n");
    exit(1);
  }
 
  outputFile = argv[1];
  
  out = fopen(outputFile, "w");
  if (out == NULL) {
    fprintf(stderr, "Could not open file '%s': %s.\n", outputFile, strerror(errno));
    exit(0);
  }

  numfiles = argc - 2;
  files = (char **)calloc((unsigned int)numfiles, sizeof(char *));

  for (i = 0; i < numfiles; i++) {
    files[i] = strip_leading_fluff(argv[i+2]);
  }

  fprintf(out, "\n\n#include \"files.h\"\n\n");

  fprintf(stderr, "Dumping %3d files to '%s'\n", numfiles, outputFile);
  for (i = 0; i < numfiles; i++) {
    dumpSingleFile(out, files[i]);
  }

  /*
  ** Print the file directory data
  */
  
  fprintf(out, "w_FileData *w_Files[] = {\n");
  for (i = 0; i < numfiles; i++) {
    fprintf(out, "  &%s,\n", munge(files[i]));
  }
  fprintf(out, "  (w_FileData *)0\n");
  fprintf(out, "};\n");

  fflush(out);
  fclose(out);

  exit(0);
  
}
