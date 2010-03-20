/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
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

#ifndef _JDWP_H
#define _JDWP_H
#ifdef JDWP

/* 
** JavaTM Debug Wire Protocol 
*/

#include "oswald.h"
#include <string.h>
#include "wonka.h"

/* 
** Error Constants 
*/

typedef enum {
  jdwp_err_invalid_tag                  = 500, /* Object type id or class tag. */
  jdwp_err_already_invoking             = 502, /* Previous invoke not complete. */
  jdwp_err_invalid_index                = 503, /* Index is invalid. */
  jdwp_err_invalid_length               = 504, /* length is invalid. */
  jdwp_err_invalid_string               = 506, /* string is invalid. */
  jdwp_err_invalid_class_loader         = 507, /* classloader is invalid. */
  jdwp_err_invalid_array                = 508, /* array is invalid. */
  jdwp_err_transport_load               = 509,
  jdwp_err_transport_init               = 510,
  jdwp_err_native_method                = 511,
  jdwp_err_invalid_count                = 512, /* count is invalid. */
  jdwp_err_none                         =   0, /* no error. */ 
  jdwp_err_invalid_thread               =  10, /* not a valid thread or has exited. */
  jdwp_err_invalid_thread_group         =  11, /* threadgroup invalid. */
  jdwp_err_invalid_priority             =  12, /* invalid priority. */
  jdwp_err_thread_not_suspended         =  13, /* specified thread has not been suspended by an event. */
  jdwp_err_thread_suspended             =  14, /* thread is already suspended. */
  jdwp_err_invalid_object               =  20, /* reference type has been unloaded and garbage collected. */
  jdwp_err_invalid_class                =  21, /* invalid class. */
  jdwp_err_class_not_prepared           =  22, /* class is not yet prepared. */
  jdwp_err_invalid_methodid             =  23, /* invalid method. */
  jdwp_err_invalid_location             =  24, /* invalid location. */
  jdwp_err_invalid_fieldid              =  25, /* invalid field. */
  jdwp_err_invalid_frameid              =  30, /* invalid frame ID. */ 
  jdwp_err_no_more_frames               =  31, /* no more java or native frames on the call stack. */
  jdwp_err_opaque_frame                 =  32, /* no information about the frame available. */
  jdwp_err_not_current_frame            =  33, /* operation can only be performed on current frame. */
  jdwp_err_type_mismatch                =  34, /* invalid variable type. */
  jdwp_err_invalid_slot                 =  35, /* invalid slot. */
  jdwp_err_duplicate                    =  40, /* item already set. */
  jdwp_err_not_found                    =  41, /* not found. */
  jdwp_err_invalid_monitor              =  50, /* invalid monitor. */
  jdwp_err_not_monitor_owner            =  51, /* thread doesn't own the monitor. */
  jdwp_err_interrupt                    =  52, /* call has been interrupted */
  jdwp_err_invalid_class_format         =  60, /* the loaded classfile is invalid */
  jdwp_err_circular_class_definition    =  61, /* circularity has been detected while initializing a class. */
  jdwp_err_fails_verification           =  62, /* the loaded classdile fails verification */
  jdwp_err_add_method_not_implemented   =  63, /* add method is not implemented. */
  jdwp_err_schema_change_not_implemented = 64, /* schema change is not implemented. */
  jdwp_err_invalid_typestate            =  65, /* state of the thread has been modified, and is now inconsistent. */
  jdwp_err_not_implemented              =  99, /* not implemented. */
  jdwp_err_null_pointer                 = 100, /* invalid pointer. */
  jdwp_err_absent_information           = 101, /* information is not available. */
  jdwp_err_invalid_event_type           = 102, /* event type id is not valid. */
  jdwp_err_illegal_argument             = 103, /* illegal argument. */
  jdwp_err_out_of_memory                = 110, /* no more memory available */
  jdwp_err_access_denied                = 111, /* debugging isn't enabled and can't be used */ 
  jdwp_err_vm_dead                      = 112, /* the VM is not running. */
  jdwp_err_internal                     = 113, /* internal error has occurred. */
  jdwp_err_unattached_thread            = 115  /* thread being used to call this function is not attached to the VM */
} jdwp_error;

/*
** EventKind Constants 
*/

typedef enum {
  jdwp_evt_vm_disconnected    = 100,
  jdwp_evt_single_step        =   1,
  jdwp_evt_breakpoint         =   2,
  jdwp_evt_frame_pop          =   3,
  jdwp_evt_exception          =   4,
  jdwp_evt_user_defined       =   5,
  jdwp_evt_thread_start       =   6,
  jdwp_evt_thread_end         =   7,
  jdwp_evt_class_prepare      =   8,
  jdwp_evt_class_unload       =   9,
  jdwp_evt_class_load         =  10,
  jdwp_evt_field_access       =  20,
  jdwp_evt_field_modification =  21,
  jdwp_evt_exception_catch    =  30,
  jdwp_evt_method_entry       =  40,
  jdwp_evt_method_exit        =  41,
  jdwp_evt_vm_init            =  90,
  jdwp_evt_vm_death           =  99,
  jdwp_evt_vm_start           = jdwp_evt_vm_init,
  jdwp_evt_thread_death       = jdwp_evt_thread_end
} jdwp_event_kind;

/*
** ThreadStatus Constants 
*/

typedef enum {
  jdwp_ts_zombie    = 0,
  jdwp_ts_running   = 1,
  jdwp_ts_sleeping  = 2,
  jdwp_ts_monitor   = 3,
  jdwp_ts_wait      = 4
} jdwp_thread_status;

/*
** SuspendStatus Constants 
*/

typedef enum {
  jdwp_ss_suspended = 0x1
} jdwp_suspend_status;

/*
** ClassStatus Constants
*/

typedef enum {
  jdwp_cs_verified     = 1,
  jdwp_cs_prepared     = 2,
  jdwp_cs_initialized  = 4,
  jdwp_cs_error        = 8
} jdwp_class_status;

w_word clazz2status(w_clazz clazz);
w_ubyte clazz2tag(w_clazz clazz);
w_ubyte clazz2sigbyte(w_clazz clazz);

/* 
** TypeTag Constants 
*/

typedef enum {
  jdwp_tt_class      = 1, /* ReferenceType is a class. */
  jdwp_tt_interface  = 2, /* ReferenceType is an interface. */
  jdwp_tt_array      = 3  /* ReferenceType is an array. */
} jdwp_type_tag;

/*
** Tag Constants 
*/

typedef enum {
  jdwp_tag_array        =  91, /* '[' - array object (objectID size). */
  jdwp_tag_byte         =  66, /* 'B' - byte value (1 byte). */
  jdwp_tag_char         =  67, /* 'C' - character value (2 bytes). */
  jdwp_tag_object       =  76, /* 'L' - object (objectID size). */
  jdwp_tag_float        =  70, /* 'F' - float value (4 bytes). */
  jdwp_tag_double       =  68, /* 'D' - double value (8 bytes). */
  jdwp_tag_int          =  73, /* 'I' - int value (4 bytes). */
  jdwp_tag_long         =  74, /* 'J' - long value (8 bytes). */
  jdwp_tag_short        =  83, /* 'S' - short value (2 bytes). */
  jdwp_tag_void         =  86, /* 'V' - void value (no bytes). */
  jdwp_tag_boolean      =  90, /* 'Z' - boolean value (1 byte). */
  jdwp_tag_string       = 115, /* 's' - String object (objectID size). */
  jdwp_tag_thread       = 116, /* 't' - Thread object (objectID size). */
  jdwp_tag_thread_group = 103, /* 'g' - ThreadGroup object (objectID size). */
  jdwp_tag_class_loader = 108, /* 'l' - ClassLoader object (objectID size). */
  jdwp_tag_class_object =  99  /* 'c' - class object object (objectID size). */
} jdwp_tag;

/*
** StepDepth Constants 
*/

typedef enum {
  jdwp_sd_into = 0, /* step into any method calls that occur before the end of the step. */
  jdwp_sd_over = 1, /* step over any method calls that occur before the end of the step. */
  jdwp_sd_out  = 2  /* step out of the current method. */
} jdwp_step_depth;

/*
** StepSize Constants 
*/ 

typedef enum {
  jdwp_ss_min  = 0, /* step by the minimum possible amount. */
  jdwp_ss_line = 1  /* step to the next source line. */
} jdwp_step_size;

/*
** SuspendPolicy Constants 
*/

typedef enum {
  jdwp_sp_none         = 0, /* suspend no threads. */
  jdwp_sp_event_thread = 1, /* suspend the event thread. */
  jdwp_sp_all          = 2  /* suspend all threads. */
} jdwp_suspend_policy;

/*
** InvokeOptions Constants
*/

typedef enum {
  jdwp_inv_invoke_single_threaded = 0x01,
  jdwp_inv_invoke_nonvirtual      = 0x02 
} jdwp_invoke_options;


/*
** JDWP Command Sets
**    0 -  63 : Set of commands that are sent to the VM.
**   64 - 127 : Set of commands that are sent to the debugger.
**  128 - 255 : Vendor specific commands and extensions.
**              (In our case: Wonka specific stuff)
*/

typedef enum {
  jdwp_cmdset_virtualMachine       =  1,
  jdwp_cmdset_referenceType        =  2,
  jdwp_cmdset_classType            =  3,
  jdwp_cmdset_arrayType            =  4,
  jdwp_cmdset_interfaceType        =  5,
  jdwp_cmdset_method               =  6,
  jdwp_cmdset_field                =  8,
  jdwp_cmdset_objectReference      =  9,
  jdwp_cmdset_stringReference      = 10,
  jdwp_cmdset_threadReference      = 11,
  jdwp_cmdset_threadGroupReference = 12,
  jdwp_cmdset_arrayReference       = 13,
  jdwp_cmdset_classLoaderReference = 14,
  jdwp_cmdset_eventRequest         = 15,
  jdwp_cmdset_stackFrame           = 16,
  jdwp_cmdset_classObjectReference = 17,
  jdwp_cmdset_event                = 64,
  jdwp_cmdset_wonka                = 234
} jdwp_cmdset;

/*
** VirtualMachine Command Set (1)
*/

typedef enum {
  jdwp_vm_version              =  1,
  jdwp_vm_classesBySignature   =  2,
  jdwp_vm_allClasses           =  3,
  jdwp_vm_allThreads           =  4,
  jdwp_vm_topLevelThreadGroups =  5,
  jdwp_vm_dispose              =  6,
  jdwp_vm_idSizes              =  7,
  jdwp_vm_suspend              =  8,
  jdwp_vm_resume               =  9,
  jdwp_vm_exit                 = 10,
  jdwp_vm_createString         = 11,
  jdwp_vm_capabilities         = 12,
  jdwp_vm_classPaths           = 13,
  jdwp_vm_disposeObjects       = 14,
  jdwp_vm_holdEvents           = 15,
  jdwp_vm_releaseEvents        = 16,
  jdwp_vm_capabilitiesNew      = 17,
  jdwp_vm_redefineClasses      = 18,
  jdwp_vm_popObsoleteFrames    = 19
} jdwp_vm_cmd;

/*
**ReferenceType Command Set (2)
*/

typedef enum {
  jdwp_reftype_signature            =  1,
  jdwp_reftype_classLoader          =  2,
  jdwp_reftype_modifiers            =  3,
  jdwp_reftype_fields               =  4,
  jdwp_reftype_methods              =  5,
  jdwp_reftype_getValues            =  6,
  jdwp_reftype_sourceFile           =  7,
  jdwp_reftype_nestedTypes          =  8,
  jdwp_reftype_status               =  9,
  jdwp_reftype_interfaces           = 10,
  jdwp_reftype_classObject          = 11,
  jdwp_reftype_sourceDebugExtension = 12
} jdwp_reftype_cmd;

/*
** ClassType Command Set (3)
*/

typedef enum {
  jdwp_classtype_superclass   = 1,
  jdwp_classtype_setValues    = 2,
  jdwp_classtype_invokeMethod = 3,
  jdwp_classtype_newInstance  = 4
} jdwp_classtype_cmd;

/*
** ArrayType Command Set (4)
*/

typedef enum {
  jdwp_arraytype_newInstance = 1
} jdwp_arraytype_cmd;

/*
** InterfaceType Command Set (5)
*/

/*
** Method Command Set (6)
*/

typedef enum {
  jdwp_method_lineTable     = 1,
  jdwp_method_variableTable = 2,
  jdwp_method_bytecodes     = 3
} jdwp_method_cmd;

/*
** Field Command Set (8)
*/

/*
** ObjectReference Command Set (9)
*/

typedef enum {
  jdwp_objref_referenceType     = 1,
  jdwp_objref_getValues         = 2,
  jdwp_objref_setValues         = 3,
  jdwp_objref_monitorInfo       = 5,
  jdwp_objref_invokeMethod      = 6,
  jdwp_objref_disableCollection = 7,
  jdwp_objref_enableCollection  = 8,
  jdwp_objref_isCollected       = 9
} jdwp_objref_cmd;

/*
** StringReference Command Set (10)
*/

typedef enum {
  jdwp_strref_value = 1
} jdwp_strref_cmd;

/*
** ThreadReference Command Set (11)
*/

typedef enum {
  jdwp_threadref_name                    =  1,
  jdwp_threadref_suspend                 =  2,
  jdwp_threadref_resume                  =  3,
  jdwp_threadref_status                  =  4,
  jdwp_threadref_threadGroup             =  5,
  jdwp_threadref_frames                  =  6,
  jdwp_threadref_frameCount              =  7,
  jdwp_threadref_ownedMonitors           =  8,
  jdwp_threadref_currentContendedMonitor =  9,
  jdwp_threadref_stop                    = 10,
  jdwp_threadref_interrupt               = 11,
  jdwp_threadref_suspendCount            = 12,
  jdwp_threadref_popTopFrame             = 13
} jdwp_threadref_cmd;

/*
** ThreadGroupReference Command Set (12)
*/

typedef enum {
  jdwp_threadgrpref_name     = 1,
  jdwp_threadgrpref_parent   = 2,
  jdwp_threadgrpref_children = 3
} jdwp_threadgrpref_cmd;

/*
** ArrayReference Command Set (13)
*/

typedef enum {
  jdwp_arrayref_length    = 1,
  jdwp_arrayref_getValues = 2,
  jdwp_arrayref_setValues = 3
} jdwp_arrayref_cmd;

/*
** ClassLoaderReference Command Set (14)
*/

typedef enum {
  jdwp_classloaderref_visibleClasses = 1
} jdwp_classloaderref_cmd;

/*
** EventRequest Command Set (15)
*/

typedef enum {
  jdwp_eventreq_set                 = 1,
  jdwp_eventreq_clear               = 2,
  jdwp_eventreq_clearAllBreakpoints = 3
} jdwp_eventreq_cmd;

/*
** StackFrame Command Set (16)
*/

typedef enum {
  jdwp_stack_getValues  = 1,
  jdwp_stack_setValues  = 2,
  jdwp_stack_thisObject = 3,
  jdwp_stack_popFrames  = 4
} jdwp_stack_cmd;

/*
** ClassObjectReference Command Set (17)
*/

typedef enum {
  jdwp_classobjref_reflectedType = 1
} jdwp_classobjref_cmd;

/* 
** Event Command Set (64)
*/

typedef enum {
  jdwp_event_composite = 100
} jdwp_event_cmd;

/* 
** Wonka Command Set (234)
*/

typedef enum {
  jdwp_wonka_blah = 1
} jdwp_wonka_cmd;


/*
** The command packet header.
*/

typedef struct jdwp_Command_Packet {
  w_int    length;       /* length of the packet in bytes, including this header. */
  w_int    id;           /* unique identifier of a command/reply packet. */
  w_ubyte  flags;        /* set to 0 for command packets. */
  w_ubyte  command_set;  /* identifier for the command group. */
  w_ubyte  command;      /* the actual command. */
  char     data[0];      /* packet data. */
} jdwp_Command_Packet;

typedef jdwp_Command_Packet  *jdwp_command_packet;

/*
** The reply packet header.
*/

typedef struct jdwp_Reply_Packet {
  w_int    length;       /* length of the packet in bytes, including this header. */
  w_int    id;           /* unique identifier of a command/reply packet. */
  w_ubyte  flags;        /* set to 0 for command packets. */
  w_ubyte  err1;         /* possible error that occured when processing the command. */
  w_ubyte  err2;         /* (Split these 2 because of aligning troubles) */
  w_ubyte  data[0];      /* packet data. */
} jdwp_Reply_Packet;

typedef jdwp_Reply_Packet  *jdwp_reply_packet;


/*
** Everything numerical has to be in a big endian format. If Wonka is running on a 
** little endian platform, then the bytes of all the numbers have to be swapped 
*/

#if __BYTE_ORDER == __BIG_ENDIAN

#define swap_byte(b) (b)
#define swap_bool(b) (b)
#define swap_short(s) (s)
#define swap_int(i) (i)
#define swap_long(l) (l)

#else // sorry, PDP endianness is not supported :)

#define swap_byte(b)  (b)
#define swap_bool(b)  (b)
#define swap_short(s) (((s & 0x00FF) << 8) | \
                       ((s & 0xFF00) >> 8))
#define swap_int(i)   (((i & 0x000000FF) << 24) | \
                       ((i & 0x0000FF00) << 8) | \
                       ((i & 0x00FF0000) >> 8) | \
                       ((i & 0xFF000000) >> 24))
#define swap_long(l)  (((l & 0x00000000000000FFLL) << 56) | \
                       ((l & 0x000000000000FF00LL) << 40) | \
                       ((l & 0x0000000000FF0000LL) << 24) | \
                       ((l & 0x00000000FF000000LL) << 8) | \
                       ((l & 0x000000FF00000000LL) >> 8) | \
                       ((l & 0x0000FF0000000000LL) >> 24) | \
                       ((l & 0x00FF000000000000LL) >> 40) | \
                       ((l & 0xFF00000000000000LL) >> 56))
#endif /* _BYTE_ORDER */

typedef enum {
  jdwp_state_unstarted,
  jdwp_state_initialised,
  jdwp_state_connected,
  jdwp_state_terminated,
} jdwp_state_enum;

extern jdwp_state_enum jdwp_state;

extern void jdwp_dispatcher(void);

/* 
** Prototypes for the dispatchers each commandset has.
*/

extern void dispatch_vm(jdwp_command_packet);       
extern void dispatch_reftype(jdwp_command_packet);  
extern void dispatch_classtype(jdwp_command_packet);
extern void dispatch_arraytype(jdwp_command_packet);
extern void dispatch_inttype(jdwp_command_packet);  
extern void dispatch_method(jdwp_command_packet);   
extern void dispatch_field(jdwp_command_packet);    
extern void dispatch_objref(jdwp_command_packet);   
extern void dispatch_strref(jdwp_command_packet);   
extern void dispatch_threadref(jdwp_command_packet);
extern void dispatch_threadgrpref(jdwp_command_packet);
extern void dispatch_arrayref(jdwp_command_packet); 
extern void dispatch_classloaderref(jdwp_command_packet);
extern void dispatch_eventreq(jdwp_command_packet); 
extern void dispatch_stack(jdwp_command_packet);    
extern void dispatch_classobjref(jdwp_command_packet); 
extern void dispatch_event(jdwp_command_packet);    
extern void dispatch_wonka(jdwp_command_packet);   

/*
** String magic. This should be moved to strings.c in the wonka subdir.
*/

extern w_ubyte *jdwp_string2UTF8(w_string string, w_int *length);
extern w_ubyte *jdwp_cstring2UTF8(char *string, w_int *length);
extern w_ubyte *jdwp_string2JNI(w_string string);
extern w_ubyte *jdwp_cstring2JNI(w_ubyte *string);
extern w_ubyte *jdwp_UTF82cstring(w_ubyte *string, w_int *length);

/*
** Locking. 
*/

extern  x_mutex  jdwp_mutex;
extern  w_int    jdwp_events_enabled;

/*
** The JDWP thread
*/

extern  w_thread jdwp_thread;

extern char *jdwp_base_directory;

extern w_int jdwp_decrement_object_id_refcount(w_word objectID, w_int amount);
 
extern w_instance jdwp_objectID2instance(w_word objectID);

extern w_word jdwp_instance2objectID(w_instance instance);

extern void jdwp_internal_suspend_one(w_thread);

extern void jdwp_internal_resume_one(w_thread);

extern void jdwp_internal_suspend_all(void);

extern void jdwp_internal_resume_all(void);

extern w_ubyte jdwp_breakpoint_get_original(w_ubyte *code);

w_ubyte jdwp_event_breakpoint(w_ubyte *code);

void jdwp_event_step(w_thread thread);

// TODO: create a plug-in mechanism for different transports (e.g. a struct
// of function pointers to connect, disconnect, read, write functions).

extern w_boolean jdwp_connect_dt_socket(const char *jdwp_address_host, const char *jdwp_address_port, const w_boolean jdwp_config_server);

extern void *jdwp_recv_packet_dt_socket(void);

extern void jdwp_send_packet_dt_socket(void *packet);

extern void jdwp_disconnect_dt_socket(void);

/*
** Convert an array of bytes into a C string of the format " xx xx xx ..."
** where the 'x' are hex digits. The memory for the string is allocated
** using allocMem, and the caller should free it using releaseMem.
*/
extern char *bytes2hex(char *bytes, w_size length);

/*
** The following static const indicates if JDWP was enabled or not. 
*/

static const w_word jpda_hooks = 1;

#include "methods.h"

/*
** Detect 'bogus' frames, i.e. those which are not interpreted and therefore
** cannot contain a breakpoint or steppoint. Returns true iff frame is bogus.
*/
#define frameIsBogus(f) (isSet((f)->flags, FRAME_JNI | FRAME_LOADING | FRAME_CLINIT | FRAME_REFLECTION) || isSet((f)->method->flags, ACC_NATIVE))

/*
** Starting with frame f, go back up the stack until we find an interpretable
** frame, i.e. one which is not marked FRAME_JNI or FRAME_LOADING or 
** FRAME_CLINIT or FRAME_REFLECTION and which is not running a native method.
** The result can be f itself, or NULL if no such frame is found.
*/

static inline w_frame skipBogusFrames(w_frame f) {
  w_frame frame = f;

  while (frame && frameIsBogus(frame)) {
    woempa(7, "Skipping a frame because it's not interpretable\n");
    frame = frame->previous;
  }

  return frame;
}

#else  /* JDWP */

static const w_word jpda_hooks = 0;

#endif /* JDWP */

#endif /* _JDWP_H */


