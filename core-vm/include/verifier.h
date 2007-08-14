/**************************************************************************
* Copyright (c) 2006, 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef VERIFIER_H
#define VERIFIER_H

/*
** $id: verifier.h,v1.1.1.1 2001/07/06 07:30:25 ruelens Exp $
*/

//#define USE_BYTECODE_VERIFIER

#ifdef USE_BYTECODE_VERIFIER

#include "loading.h"

extern w_word verify_flags;

/*
** If USE_BYTECODE_VERIFIER is defined, following macros will enforce verification of:
**    VERIFY_BOOTSTRAP_CLASSES -> classes loaded by bootstrap/system classloader
**                                (rare, normally used only for testing)
**    VERIFY_EXTENSION_CLASSES -> classes loaded by extension classloader
**                                (rare, normally used only for testing)
**    VERIFY_APPLICATION_CLASSES -> classes loaded by application classloader
**    VERIFY_USERDEFINED_CLASSES -> classes loaded by user-defined classloader
*/

#define VERIFY_FLAG_BOOTSTRAP     1
#define VERIFY_FLAG_EXTENSION     2
#define VERIFY_FLAG_APPLICATION   4
#define VERIFY_FLAG_USERDEFINED   8

#define VERIFY_LEVEL_NONE    0
#define VERIFY_LEVEL_REMOTE  VERIFY_FLAG_USERDEFINED
#define VERIFY_LEVEL_ALL     15
#define VERIFY_LEVEL_DEFAULT (VERIFY_FLAG_APPLICATION | VERIFY_FLAG_USERDEFINED)

#ifndef VERIFY_BOOTSTRAP_CLASSES
#define VERIFY_BOOTSTRAP_CLASSES isSet(verify_flags, VERIFY_FLAG_BOOTSTRAP)
#endif
#ifndef VERIFY_EXTENSION_CLASSES
#define VERIFY_EXTENSION_CLASSES isSet(verify_flags, VERIFY_FLAG_EXTENSION)
#endif
#ifndef VERIFY_APPLICATION_CLASSES
#define VERIFY_APPLICATION_CLASSES isSet(verify_flags, VERIFY_FLAG_APPLICATION)
#endif
#ifndef VERIFY_USERDEFINED_CLASSES
#define VERIFY_USERDEFINED_CLASSES isSet(verify_flags, VERIFY_FLAG_USERDEFINED)
#endif

#define loaderIsTrusted(l) (((l) == systemClassLoader) ? !VERIFY_BOOTSTRAP_CLASSES : ((l) == extensionClassLoader) ? !VERIFY_EXTENSION_CLASSES : (instance2clazz(l) == clazzApplicationClassLoader) ? !VERIFY_APPLICATION_CLASSES : !VERIFY_USERDEFINED_CLASSES)
#define clazzShouldBeVerified(c) ((c)->loader ? !loaderIsTrusted((c)->loader) : VERIFY_BOOTSTRAP_CLASSES)

/*
** Called to perform bytecode verification.
** Returns CLASS_LOADING_SUCCEEDED or CLASS_LOADING_FAILED.
*/
w_int verifyClazz(w_clazz clazz);

#else // USE_BYTECODE_VERIFIER not defined

#define clazzShouldBeVerified(c) FALSE
#define loaderIsTrusted(l) TRUE
#define verifyClazz(c) CLASS_LOADING_SUCCEEDED

#endif
#endif
