#ifndef VERIFIER_H
#define VERIFIER_H

/*
** $id: verifier.h,v1.1.1.1 2001/07/06 07:30:25 ruelens Exp $
*/

//#define USE_BYTECODE_VERIFIER

#ifdef USE_BYTECODE_VERIFIER

#include "loading.h"

/*
** If defines TRUE, following macros will enforce verification of:
**    VERIFY_BOOTSTRAP_CLASSES -> classes loaded by bootstrap/system classloader
**                                (rare, normally used only for testing)
**    VERIFY_EXTENSION_CLASSES -> classes loaded by extension classloader
**                                (rare, normally used only for testing)
**    VERIFY_APPLICATION_CLASSES -> classes loaded by application classloader
**    VERIFY_USERDEFINED_CLASSES -> classes loaded by user-defined classloader
*/
#ifndef VERIFY_BOOTSTRAP_CLASSES
#define VERIFY_BOOTSTRAP_CLASSES FALSE
#endif
#ifndef VERIFY_EXTENSION_CLASSES
#define VERIFY_EXTENSION_CLASSES FALSE
#endif
#ifndef VERIFY_APPLICATION_CLASSES
#define VERIFY_APPLICATION_CLASSES TRUE
#endif
#ifndef VERIFY_USERDEFINED_CLASSES
#define VERIFY_USERDEFINED_CLASSES TRUE
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
