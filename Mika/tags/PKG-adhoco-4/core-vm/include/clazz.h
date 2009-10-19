/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 by Chris Gray,   *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#ifndef _CLAZZ_H
#define _CLAZZ_H

#include "package.h"
#include "wonka.h"
#include "hashtable.h"
#include "oswald.h"
#include "threads.h"
#include "wordset.h"

/*
** If CLASSES_HAVE_INSTANCE_CACHE is defined each class will have a cache
** of instances which have become unreachable and can be recycled.
** Not currently usable: it justs wastes memory and runs slower, not faster. :-(
*/
//#define CLASSES_HAVE_INSTANCE_CACHE

typedef struct w_InnerClassInfo {
  w_ushort inner_class_info_index;
  w_ushort outer_class_info_index;
  w_ushort inner_name_index;	     
  w_ushort inner_class_access_flags;
} w_InnerClassInfo;

typedef struct w_Clazz {
/* Information available in all states, even CLAZZ_STATE_UNLOADED       */
  w_string dotified;     /* class name, e.g. java.lang.Object           */
  char   *label;         /* null-terminated C string beginning "class"  */
  w_flags flags;         /* class access and other flags, see below     */
  w_instance loader;     /* the ClassLoader that defined this class     */
  w_ubyte type;          /* JIFF VMTYPE, see below                      */
  w_ubyte dims;          /* Number of dimensions if an array type       */
  w_ubyte bits;
  w_ubyte spare;
/* Information available in states CLAZZ_STATE_LOADED and higher        */
/* (see also the 'temp' annex below)                                    */
  w_short cmajor;        /* Classfile format major version number       */
  w_short cminor;        /* Classfile format minor version number       */
  w_size  numConstants;  /* Number of constants in the constant pool    */
  w_ConstantType *tags;  /* Constant tags (see constant.h)              */
  w_ConstantValue* values; /* Constant values (see constant.h)          */
  w_size  numDeclaredMethods;
  w_method own_methods;
  w_ushort numFields;
  w_ushort numStaticFields;
  w_field own_fields;
  x_monitor  resolution_monitor; /* used to protect resolution of constants */
  w_thread   resolution_thread;  /* thread which is busy with this class    */
                                 /* (unstable class states only)            */
  w_package  package;    /* runtime package of which this class is a member */

/* Information available in states CLAZZ_STATE_SUPERS_LOADED and higher */
/* (see also the 'temp' annex below)                                    */
  w_ushort numSuperClasses;
  w_ushort numInterfaces;
  w_ushort numDirectInterfaces;
  w_clazz *supers;
  w_clazz *interfaces;
  
/* Information available in states CLAZZ_STATE_REFERENCED and higher    */
/* (see also the 'temp' annex below)                                    */

/* Note that the vmlt only contains inheritable methods, i.e. not       */
/* static, private, or <init> methods. Final methods are included, as   */
/* they are inherited (but cannot be overridden).                       */
  w_method *vmlt;                   /* Virtual method lookup table      */
                                    /* (not for interfaces) */
  w_size numInheritableMethods;     /* Number of methods in the vmlt    */

/* Currently all static variables are stored in one or two words, in no */
/* particular order. The packing of fields into an instance is more     */
/* complex:                                                             */
/*  boolean and byte fields each occupy one byte.                       */
/*  other primitive fields occupy either one or two words.              */
/*  reference fields follow all primitive fields.                       */
/* To implement this packing we need to know the next available byte    */
/* slot, next available word slot, and the number of reference fields   */
/* defined in this class and all its superclasses.                      */
  w_ushort numStaticWords;          /* The number of static field words */
  w_ushort instanceSize;            /* The size in words of an instance */
                                    /* of this class, excluding the     */
                                    /* object header.                   */
  w_ushort numReferenceFields;      /* The number of these words which  */
                                    /* occupied by reference fields.    */
  w_ushort nextByteSlot;            /* The next slot available for a    */
                                    /* byte-sized field. (The next slot */
                                    /* available for a word-sized field */
                                    /* is instanceSize - numReferenceFields, */
                                    /* which may be >= nextByteSlot).   */

  w_wordset references;             /* contains w_clazz pointers to classes */
                                    /* which are referenced by this class   */

/* Information available in state CLAZZ_STATE_INITIALIZED only        */
  w_word *staticFields;  /* the static data fields for this class */
  w_method defaultInit;  /*   <init>()V           default initializer    */
  w_method clinit;       /*   <clinit>            class initializer      */
  w_method runner;       /*   run()V              runnable entry point   */

/* not sorted yet ... */


  w_instance Class;      /* the Class instance for this clazz */
                         /* some commonly-occurring methods, if present: */

  w_long suid;          /* the cached SUID of this clazz or 0 */
  w_string filename;    /* filename as found in class file */

#ifdef JAVA_PROFILE
  w_long instances;
  w_long max_instances;
  w_long total_instances;
#endif 
  
  w_string   failure_message;
  w_size bytes_needed;     /* The number of bytes we have to allocate for an instance of this clazz. */
  w_clazz nextDimension;     /* Pointer to clazz with one more dimension if already defined */
  w_clazz previousDimension; /* Pointer to clazz with one less dimension (or null if dims == 0) */
  struct {
    w_ushort this_index;
    w_ushort super_index;
    w_ushort interface_index_count;
    w_ushort inner_class_info_count;
    w_ushort *interface_index;
    w_InnerClassInfo *inner_class_info;
  } temp;
#ifdef CLASSES_HAVE_INSTANCE_CACHE
  w_fifo cache_fifo;
#ifndef THREAD_SAFE_FIFOS
  x_mutex cache_mutex;
#endif
#endif
} w_Clazz;

/*
** An unloaded class consists of a name-classloader pair.  The state subfield
** of the flags will always be zero (CLAZZ_STATE_UNLOADED).
*/
typedef struct w_UnloadedClazz {

  w_string dotified;     /* class name, e.g. java.lang.Object           */
  char   *label;         /* null-terminated C string beginning "class"  */
  w_flags flags;         /* class access and other flags, see below     */
  w_instance loader;     /* the ClassLoader that initiated this class   */
  w_ubyte type;          /* JIFF VMTYPE, see below                      */
  w_ubyte dims;          /* Number of dimensions if an array type       */
  w_ubyte bitz;          /* Not used                                    */
  w_ubyte spare;
                     /* Loading constraints :                           */
                         /* If This unloaded class is constrained to be */
                         /* the same as an already loaded class, then   */
                         /* constraint_parent points to this class.     */
  w_clazz constraint_parent;
  struct {               /* Alternatively, if this unloaded class is    */
    w_clazz next;        /* constrained to be the same as one or more   */
    w_clazz prev;        /* other unloaded classes, these are joined    */
  } constraint_siblings; /* into a linked list using constraint_siblings. */
} w_UnloadedClazz;

/*
** The low-order 16 bits of the `flags' field are the ACC_... flags as they
** appear in the class file format.  The remaining fields are defined below.
**
** +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
** | | | | | | | | | | | | | state |       ACC_xxxxx flags         |
** +-+^+^+^+^+^+^+^+^+^+^+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
**    | | | | | | | | | |
**    | | | | | | | | | +--- CLAZZ_IS_TRUSTED 
**    | | | | | | | | +----- CLAZZ_IS_SCRAMBLED
**    | | | | | | | +------- CLAZZ_IS_PRIMITIVE
**    | | | | | | +--------- CLAZZ_IS_PROXY
**    | | | | | +----------- CLAZZ_IS_THROWABLE
**    | | | | +------------- CLAZZ_IS_THREAD
**    | | | +--------------- CLAZZ_IS_REFERENCE
**    | | +----------------- CLAZZ_IS_CLASSLOADER
**    | +------------------- CLAZZ_IS_UDCL
**    +--------------------- CLAZZ_HAS_FINALIZER
*/
#define CLAZZ_STATE_MASK        0x000f0000
#define CLAZZ_STATE_SHIFT               16
#define CLAZZ_STATE_UNLOADED             0
#define CLAZZ_STATE_LOADING              1
#define CLAZZ_STATE_LOADED               2
#define CLAZZ_STATE_SUPERS_LOADING       3
#define CLAZZ_STATE_SUPERS_LOADED        4
#define CLAZZ_STATE_REFERENCING          5
#define CLAZZ_STATE_REFERENCED           6
#define CLAZZ_STATE_LINKING              7
#define CLAZZ_STATE_LINKED               8
#define CLAZZ_STATE_VERIFYING            9
#define CLAZZ_STATE_VERIFIED            10
#define CLAZZ_STATE_INITIALIZING        11
#define CLAZZ_STATE_INITIALIZED         12
// Cheating, this should really be a different value
#define CLAZZ_STATE_GARBAGE             15
#define CLAZZ_STATE_BROKEN              15

#define getClazzState(c) (((c)->flags & CLAZZ_STATE_MASK) >> CLAZZ_STATE_SHIFT)
#define setClazzState(c,s) (c)->flags = (((c)->flags & ~CLAZZ_STATE_MASK) | ((s) << CLAZZ_STATE_SHIFT))

/*
 * CLAZZ_IS_TRUSTED is set if no verification is required for this class.
 */
#define CLAZZ_IS_TRUSTED 0x00200000

#ifdef SUPPORT_BYTECODE_SCRAMBLING
/*
** CLAZZ_IS_SCRAMBLED is set iff the bytecode of the class is encrypted.
*/
#define CLAZZ_IS_SCRAMBLED 0x00400000
#endif

/*
** CLAZZ_IS_PRIMITIVE is set iff the clazz represents a primitive type or null
** or void.  The flags CLAZZ_IS_THROWABLE, CLAZZ_IS_THREAD, CLAZZ_IS_THREADGROUP,
** CLAZZ_IS_REFERENCE, CLAZZ_IS_CLASSLOADER indicate that the class is (a
** subclass of) java.lang.Throwable, java.lang.Thread, java.lang.ThreadGroup,
** java.lang.ref.Reference, or java.lang.ClassLoader respectively; 
** CLAZZ_IS_UDCL indicates that the class is a user-defined class loader. These
** flags are inherited by all subclasses of this clazz.
*/
#define CLAZZ_IS_PRIMITIVE      0x00800000
#define CLAZZ_IS_PROXY          0x01000000
#define CLAZZ_IS_THROWABLE      0x02000000
#define CLAZZ_IS_THREAD         0x04000000
#define CLAZZ_IS_REFERENCE      0x08000000
#define CLAZZ_IS_CLASSLOADER    0x10000000
#define CLAZZ_IS_UDCL           0x20000000
#define CLAZZ_HAS_FINALIZER     0x40000000

#define CLAZZ_HERITABLE_FLAGS  (CLAZZ_IS_REFERENCE | CLAZZ_IS_THROWABLE | CLAZZ_IS_THREAD | CLAZZ_IS_CLASSLOADER | CLAZZ_IS_UDCL)

/*
** The "type" byte.  These definitions are congruent with JIFF.
**   Primitive types: note that the lowest 2 bits give an indication
**   of the size of an item (0 -> 1 byte, 1 -> 2, 2 -> 4, 3 -> 8).
*/
#define VM_TYPE_VOID            0x00 
#define VM_TYPE_SHORT           0x01
#define VM_TYPE_INT             0x02
#define VM_TYPE_LONG            0x03
#define VM_TYPE_BYTE            0x04
#define VM_TYPE_CHAR            0x05
#define VM_TYPE_FLOAT           0x06
#define VM_TYPE_DOUBLE          0x07
#define VM_TYPE_BOOLEAN         0x08
#define VM_TYPE_OBJECT          0x0A
/*
**   Flags.
*/
#define VM_TYPE_TWO_CELL        0x10  /* Needs 2 cells (not set for arrays) */
#define VM_TYPE_REF             0x20  /* Is a reference or array            */
#define VM_TYPE_MONO            0x40  /* Is a 1-D array                     */
#define VM_TYPE_MULTI           0x80  /* Is a n-D array, n>1                */

#define clazzIsPrimitive(c) isNotSet((c)->type, VM_TYPE_REF)

/*
** Get the superclass of a clazz (returns NULL if clazz is java.lang.Object).
*/
#define getSuper(clazz) (clazz->supers ? clazz->supers[0] : NULL)

/*
** Return true iff T_clazz is a strict superclass of S_clazz.  Will return
** WONKA_FALSE if T_clazz == S_clazz ! S_clazz and T_clazz must be at least 
** in state CLAZZ_SUPERS_LOADED.
*/
#define isStrictSuperClass(T_clazz,S_clazz) \
  (  (S_clazz)->supers \
  && (S_clazz)->numSuperClasses > (T_clazz)->numSuperClasses \
  && (S_clazz)->supers[(S_clazz)->numSuperClasses - (T_clazz)->numSuperClasses - 1] == (T_clazz) \
  )

/*
** Return WONKA_TRUE if T_clazz and S_clazz are identical or T_clazz is a
** superclass of S_clazz, WONKA_FALSE otherwise. S_clazz and T_clazz must
** be at least in state CLAZZ_SUPERS_LOADED.
*/
#define isSuperClass(T_clazz,S_clazz) ((T_clazz) == (S_clazz) || isStrictSuperClass((T_clazz), (S_clazz)))


/** Attach an instance of java.lang.Class to this clazz.
 ** Note that this is not currently used for array clazzes
 ** (createSingleArrayClazzFromDesc has its own code to do
 ** the equivalent thing).
 */
w_instance attachClassInstance(w_clazz, w_thread thread);

/*
** clazz2Class returns the w_instance of Class associated with this w_clazz.
*/
#ifdef RUNTIME_CHECKS
w_instance clazz2Class(w_clazz);
#else
#define clazz2Class(c) ((c)->Class)
#endif

/*
** Get the class loader of this class: the initiating class loader if class
** state is CLAZZ_STATE_UNLOADED, the defining class loader otherwise.
*/
static inline w_instance clazz2loader(w_clazz clazz) {
  return clazz->loader;
}

#include "heap.h"
#include "fields.h"

extern w_int F_Class_wotsit;

/*
** Get a pointer to the w_Clazz structure associated with an instance of Class.
*/
static inline w_clazz Class2clazz(w_instance Class) {
  return getWotsitField(Class, F_Class_wotsit);
}

/*
** The hashtables for unloaded and loaded classes used by the bootstrap class
** loader, and inherited by the system class loader when this is created.
*/
extern w_hashtable system_loaded_class_hashtable;

extern w_hashtable system_unloaded_class_hashtable;

extern w_hashtable system_package_hashtable;

/*
** Get a copy of a reference field of a class
*/
w_instance getStaticReferenceField(w_clazz clazz, w_int slot);

/*
** Set a reference field of a class.
*/
void setStaticReferenceField(w_clazz clazz, w_int slot, w_instance child, w_thread thread);

/*
** Set a reference field of a class, when the context is known to be 'unsafe'.
*/
void setStaticReferenceField_unsafe(w_clazz clazz, w_int slot, w_instance child, w_thread thread);

/**
 * Allocate memory for a w_Clazz, without initialising it.
 */
w_clazz allocClazz(void);

w_clazz createClazz(w_thread, w_string name, w_bar source, w_instance loader, w_boolean trusted);
void startClasses(void);

/**
** Register clazz 'clazz' with the loaded_classes_hashtable of 'loader'.
** If an entry already exists in state CLAZZ_STATE_LOADING, we copy the
** contents of 'clazz' over the existing entry, release the memory of
** 'clazz', and return the existing entry as result. Otherwise the
** result returned is 'clazz'.
** A typical calling pattern is
**   ...
**   clazz = allocClazz();
**   ...
**   clazz = registerClazz(thread, clazz, loader);
**   ...
**
** The caller must own the instance lock on 'loader'.
*/
w_clazz registerClazz(w_thread thread, w_clazz clazz, w_instance loader);

/*
** Destroy a w_clazz structure, cleaning up all its ramifications.
** The value returned is (a conservative estimate of) the number of bytes reclaimed.
*/
w_int destroyClazz(w_clazz clazz);

/*
** getField retrieves the field of `clazz' with a given name.
*/
w_field getField(w_clazz, w_string);

extern w_clazz clazz_boolean;
extern w_clazz clazz_char;
extern w_clazz clazz_float;
extern w_clazz clazz_double;
extern w_clazz clazz_byte;
extern w_clazz clazz_short;
extern w_clazz clazz_int;
extern w_clazz clazz_long;
extern w_clazz clazz_void;

extern w_clazz clazzArrayOf_Object;
extern w_clazz clazzArrayOf_Class;
extern w_clazz clazzArrayOf_String;

extern char * print_clazz_short(char*, int*, void*, int w, int p, unsigned int f);
extern char * print_clazz_long(char*, int*, void*, int w, int p, unsigned int f);

#endif /* _CLAZZ_H */
