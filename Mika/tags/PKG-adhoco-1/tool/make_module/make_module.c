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
#include <unistd.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>

int main(int argc, char * argv[]) {

  char * tmpdir;
  char * syscmd;
  char * pwd;

  if (argc < 5) {
    fprintf(stderr, "%s generates a .o module file from a .a archive.\n", argv[0]);
    fprintf(stderr, "usage: %s ar ld output.o lib.a\n", argv[0]);
    return 0;
  }  

  tmpdir = (char *)malloc(L_tmpnam);
  syscmd = (char *)malloc(255);
  pwd = (char *)malloc(255);

  getcwd(pwd, 255); /* Store the current directory */

  if(tmpnam(tmpdir) == NULL) {
    fprintf(stderr, "memory troubles...\n");
    return -1;
  }

  /*
   * Make a temporary subdirectory
   */

  sprintf(syscmd, "mkdir %s", tmpdir);
  system(syscmd);

  /*
   * Copy the .a archive to the temporary dir
   */

  sprintf(syscmd, "cp %s %s", argv[4], tmpdir);
  system(syscmd);

  /*
   * Change to the temporary directory
   */

  chdir(tmpdir);

  /*
   * Unpack the lib.a file
   */

  sprintf(syscmd, "%s x %s", argv[1], (char *)(strrchr(argv[4], '/') + 1));
  system(syscmd);

  /*
   * Link the *.o files into one big .o file
   */

  sprintf(syscmd, "%s -X -x -i -o lib.o *.o", argv[2]);
  system(syscmd);

  /*
   * Jump back to the original directory
   */
  
  chdir(pwd);

  /* 
   * Copy the new .o file
   */
  
  sprintf(syscmd, "cp %s/lib.o %s", tmpdir, argv[3]);
  system(syscmd);
  
  /*    
   * Remove the temporary subdirectory
   */
  
  sprintf(syscmd, "rm -rf %s", tmpdir);
  // system(syscmd);

  free(tmpdir);
  free(syscmd);
  
  return 0;
}
