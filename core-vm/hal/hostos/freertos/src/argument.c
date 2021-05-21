/**************************************************************************
* Copyright (c) 2001, 2003 by Acunia N.V. All rights reserved.            *
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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: argument.c,v 1.2 2004/11/18 22:56:54 cvs Exp $
*/

#include <stdlib.h>
#include <sys/stat.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#include "ts-mem.h"
#include "oswald.h"
#include "jni.h"

#ifndef CLASSPATH
#define CLASSPATH "."
#endif

char *get_default_classpath(void) {
  char            *classpath;
  char            *default_classpath = CLASSPATH;
  char            *env_classpath;
  
  env_classpath = getenv("MIKA_CLASSPATH");
  if (env_classpath) {
    woempa(7, "Found MIKA_CLASSPATH=%s\n", env_classpath);
  }
  else {
    env_classpath = getenv("CLASSPATH");
  }

  if (env_classpath) {
    woempa(7, "Environmental classpath is %s\n", env_classpath);

    classpath = allocMem(strlen(env_classpath) + strlen(default_classpath) + 2);
    strcpy(classpath, env_classpath);
    classpath[strlen(env_classpath)] = ':';
    strcpy(classpath + strlen(env_classpath) + 1, default_classpath);
    classpath[strlen(env_classpath) +  strlen(default_classpath) + 1] = 0;

    woempa(7, "Default classpath is %s\n", classpath);
  }
  else {
    classpath = default_classpath;
  }

  return classpath;
}

