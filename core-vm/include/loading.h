#ifndef _LOADING_H
#define _LOADING_H

/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2005, 2006 by Chris Gray,             *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/

/*
** $Id: loading.h,v 1.5 2006/10/04 14:24:14 cvsroot Exp $ 
** 
** The Wonka kernel is software copyright by SmartMove NV (1999).
** Please see the file Copyright for information on its legal use.
** 
*/

#include "clazz.h"
#include "core-classes.h"
#include "wstrings.h"
#include "oswald.h"
#include "wonka.h"

/*
** Number of ticks for which to x_monitor_wait() when another thread is
** modifying the state of a class.
*/

#define CLASS_STATE_WAIT_TICKS 10

/*
** Some special methods: java.lang.Object/finalize() and 
java.lang/ClassLoader/loadClass(). These are set up during startLoading().
*/
w_method finalize_method;
w_method loadClass_method;

/*
** If a class with the given name has already been loaded by `loader',
** return a pointer to its w_Clazz structure; otherwise return a pointer
** to a w_UnloadedClazz which encapsulates the name and loader.
*/
w_clazz identifyClazz(w_string name, w_instance loader);

/*
** seekClazzByName looks for the named class in the loaded_classes hashtable
** of the given classLoader, and returns either a w_clazz (if it was found)
** or null.
*/
w_clazz seekClazzByName(w_string name, w_instance classLoader);

/*
** namedClassMustBeLoaded looks for the named class in the loaded_classes
** hashtable of the given classLoader, and if it is not found then it tries
** to load it (by calling the classLoader's loadClass() method).  If loading
** fails an exception is thrown and namedClassMustBeLoaded returns NULL.
*/
w_clazz namedClassMustBeLoaded(w_instance classLoader, w_string name);

/*
** namedArrayClassMustBeLoaded behaves the same as namedClassMustBeLoaded,
** except that the name must denote an array class (must begine with '[').
*/
w_clazz namedArrayClassMustBeLoaded(w_instance classLoader, w_string name);

/*
** getNextDimension returns the w_clazz which represents a 1-D array of the
** given clazz (so it the given clazz is itself an n-dimensional array the
** result will be (n+1)-dimensional). If the required array clazz does not
** exist then it will be created, with the given initiating loader. (The
** defining loader will be that of the array component).
*/
w_clazz getNextDimension(w_clazz, w_instance initiating_loader);

/*
** createNextDimension creates the w_clazz which represents a 1-D array of the
** given clazz.  The target class must not already exist!
*/
w_clazz createNextDimension(w_clazz, w_instance initiating_loader);

/*
** Convert the given string from descriptor form (e.g. "Ljava/lang/Class;", "I")
** to Class.getName() form ("java.lang.Class", "int").
** Returns a registered w_string.
*/
w_string undescriptifyClassName(w_string string);

/*
** Returns true iff the class name is one that may only be loaded by the
** system/bootstrap class loader (e.g. java.*, wonka.*).
*/
w_boolean namedClassIsSystemClass(w_string name);

extern w_hashtable fixup1_hashtable;
extern w_hashtable fixup2_hashtable;

/*
** Given an instance 'l' of java.lang.ClassLoader, return a pointer to the 
** w_Hashtable which holds a pointer to a w_Clazz structure for each class
** which was already loaded by this loader.
** If 'l' is null, system_loaded_class_hashtable is returned.
*/
#define loader2loaded_classes(l) ((l) ? getWotsitField((l), F_ClassLoader_loaded_classes) : system_loaded_class_hashtable)

/*
** Given an instance 'l' of java.lang.ClassLoader, return a pointer to the 
** w_Hashtable which holds a pointer to a w_UnloadedClazz structure for each
** class which has been marked as loadable by theis loader but has not yet
** been loaded.
** If 'l' is null, system_unloaded_class_hashtable is returned.
*/
#define loader2unloaded_classes(l) ((l) ? getWotsitField((l), F_ClassLoader_unloaded_classes) :system_unloaded_class_hashtable)

/*
** Possible outcomes for mustBeXxxx calls: no action was needed, action was
** successful, action was unsuccessful.
*/
#define CLASS_LOADING_DID_NOTHING 0
#define CLASS_LOADING_SUCCEEDED   1
#define CLASS_LOADING_FAILED     -1

w_int mustBeLoaded(volatile w_clazz *clazzptr);
w_int mustBeSupersLoaded(w_clazz clazz);
w_int mustBeReferenced(w_clazz clazz);
w_int mustBeLinked(w_clazz clazz);
w_int mustBeInitialized(w_clazz clazz);

static inline w_boolean 
classIsInitialized(w_clazz clazz) {
  return getClazzState(clazz) == CLAZZ_STATE_INITIALIZED;
}

w_clazz loadBootstrapClass(w_string name);
w_clazz loadNonBootstrapClass(w_instance classLoader, w_string name);

/*
** Function to be called during VM startup, to initialise some data structures.
*/
void startLoading(void);

/*
** Name of the zipfile which contains the bootstrap classes, as a C string.
*/
char     *zipname;

/*
** Set the system class loader to 'scl'.  From this point on classes on
** the bootstrap class path will be loaded by scl, not by loadBootstrapClass.
*/
void setSystemClassLoader(w_instance scl);

/*
** Hashtable mapping clazz x imethod -> method
*/
extern w_hashtable2k interface_hashtable;

/*
** The unique instance of SystemClassLoader (or NULL if not yet installed).
** Must only be set once, using setSystemClassLoader()!
*/
extern w_instance systemClassLoader;

/*
** The unique instance of ExtensionClassLoader, or NULL if none exists.
** Set up by static method installExtensionClassLoader() of java.lang.ClassLoader.
*/
extern w_instance extensionClassLoader;

/*
** Function to compare two w_[Unloaded}Clazz pointers.
** Returns WONKA_TRUE if they are either both unloaded (state == 0) or both
** loading or loaded (state != 0), and both refer to the same class name
** and loader. Otherwise, returns WONKA_FALSE.
*/
w_boolean sameClazz(w_clazz, w_clazz);

/*
** Ditto but taking w_word parameters, for use as a hashtable comparator.
*/
w_boolean clazz_comparator(w_word clazz1_word, w_word clazz2_word);

/*
** Function to compare two pointers to w_[Unloaded}Clazz pointers.
** If the two pointers point to w_[Unloaded}Clazz pointers with different
** names, returns WONKA_FALSE without further checking.
** If both pointers are to w_UnloadedClazz pointers with the same name and
** same (originating) class loader, returns WONKA_TRUE. Also returns
** WONKA_TRUE if both pointers are to (loaded) w_Clazz pointers with the
** same name and (defining) class loader. Otherwise (one is loaded and
** the other is not, or both are unloaded and the initiating loaders are
** different), both classes will be loaded in order to compare them.
**
** Because this function can call mustBeLoaded(), the caller must ensure
** that for each of the two pointers at least one of the following is
** true:
**   - the class is already loaded
**   - the pointer-to-a-pointer is a local variable of a function
**   - the pointer-to-a-pointer is a field of a w_Method or w_Field, and
**     the caller holds the resolution_monitor for the clazz in which the
**     field or method is declared.
** Failure to do so may result in race situations in which two threads load
** the same class and deregister the same w_UnloadedClazz (ouch).
*/
w_boolean sameClassReference(w_clazz *clazzptr1, w_clazz *clazzptr2);

/*
** Hashcode for a w_[Unloaded]Class.
*/
w_word clazz_hashcode(w_word clazz_word);

/*
** Register a w_UnloadedClazz. The result returned is a w_UnloadedClazz*
** which may or not be the same as the one passed as input: if it is
** different, then the one passed as input has been released.
*/
w_clazz registerUnloadedClazz(w_clazz clazz);

/*
** Deregister a w_UnloadedClazz. When we see that the registered count drops
** to 0, we release the w_UnloadeClazz structure.
*/

void deregisterUnloadedClazz(w_clazz clazz);


/*
** Perform a given function for each ClassLoader registered with the system.
** Returns the number of class loaders processed.
*/
w_fifo forEachClassLoader(void* (*fun)(w_instance));

#endif /* _LOADING_H */
