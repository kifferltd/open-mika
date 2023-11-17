#include "stdbool.h"
#include "arrays.h"
#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "loading.h"
#include "locks.h"
#include "methods.h"
#include "mika_threads.h"
#include "mika_stack.h"
#include "wonka.h"

extern  x_mutex mutex64;

typedef struct IM4000_Frame {
  int32_t unused0;    // ConstantPool
  w_method method;
  w_instance unused2; // SyncObject
  u_int8_t *current;  // ThrowPC
  int32_t unused4;    // LSP
  int32_t unused5;    // ESP
  int32_t *locals;    // LMP
  int32_t unused7;    // LocalsCount
  int32_t unused8;    // RetAddress
  int32_t unused9;    // FMP
} IM4000_Frame;

/**
 * The 'locals' pointer in the IM4000_Frame actually points to local variable #8.
*/
#define LOCAL_VARIABLE_ARRAY(frame) ((frame)->locals-8)

typedef struct IM4000_Frame *im4000_frame;

extern void throw(w_instance objectref) __attribute__((noreturn));

#define CHECK_FOR_PENDING_EXCEPTION \
  { \
    w_instance exception = exceptionThrown(thread); \
    if (exception) { \
      woempa(7, "Throwing pending exception %e\n", exception); \
      throw(exception); \
    } \
  }

#define THROW_EXCEPTION(exclazz,...) throwException(thread, exclazz, __VA_ARGS__); throw(exceptionThrown(thread))


/*
** Seek a handler for an exception.
** @param frame the current execution frame .
** @param pc the current bytecode position in that frame.
** @param objectref the exception object.
** @return 0 if no handler is found, else the pc of the handler.
*/
int32_t throwExceptionAt(im4000_frame frame, int32_t pc, w_instance objectref) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;
  w_slot saved_auxstack_top;
  int32_t handler_pc = 0;

  if (!isSuperClass(clazzThrowable, instance2object(objectref)->clazz)) {
    wabort(ABORT_WONKA, "Malfeasance detected - %j is not an an instance of Throwable", objectref);
  }

  w_boolean was_unsafe = enterUnsafeRegion(thread);
  if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
    w_printf("Thrown: Seeking handler for %e at %M:%d in thread %t\n", objectref, calling_method, pc, thread);
  }
  woempa(7, "Seeking handler for %e, current frame is running %M:%d\n", objectref, calling_method, pc);

  saved_auxstack_top = (w_slot)thread->top->auxstack_top;
  pushLocalReference(thread->top, objectref);

  w_clazz caught_clazz; // the class of exception which is covered by this handler
  for (int32_t i = 0; i <  calling_method->exec.numExceptions; i++) {
    w_exception ex = &calling_method->exec.exceptions[i];
    if (ex->type_index) {
      enterSafeRegion(thread);
      caught_clazz = getClassConstant(calling_clazz, ex->type_index, thread);
      enterUnsafeRegion(thread);
    }
    else {
      caught_clazz = NULL; // means that this is a catch-all handler
    }
    if (pc >= ex->start_pc && pc < ex->end_pc) {
      if (caught_clazz == NULL || isSuperClass(caught_clazz, instance2object(objectref)->clazz)) {
        handler_pc = ex->handler_pc;
         if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
          w_printf("Thrown: Catching %e at %M:%d in thread %t, handler at pc %d\n", objectref, calling_method, pc, thread, handler_pc);
        }
       woempa(7, ">>>> Found a handler for %e (as %k) at pc = %d in %M <<<<\n",
            objectref, caught_clazz, handler_pc, calling_method);
#ifdef JDWP
        enterSafeRegion(thread);
        jdwp_event_exception(objectref, calling_method, ex->handler_pc);
        enterUnsafeRegion(thread);
#endif
        exceptionThrown(thread) = NULL;
        // TODO is this really needed?
        setReferenceField_unsafe(thread->Thread, NULL, F_Thread_thrown);

        break;
      }
    }
  }

  if (handler_pc == 0) {
    woempa(7, ">>>> Found no handler for %j in this frame <<<<\n", objectref);
    exceptionThrown(thread) = objectref;
    // TODO is this really needed?
    setReferenceField_unsafe(thread->Thread, objectref, F_Thread_thrown);
    if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
      w_printf("Thrown: Propagating %e at %M:%d in thread %t\n", objectref, calling_method, pc, thread);
    }
  }
  thread->top->auxstack_top = saved_auxstack_top; // clean up aux stack
  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
 
  return handler_pc;
}

w_field getMethodConstant_unsafe(w_clazz c, uint32_t i) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_boolean was_unsafe = enterSafeRegion(thread);
  w_method m = getMethodConstant(c, i);
  if (isSet(m->flags, ACC_STATIC)) {
    mustBeInitialized(m->spec.declaring_clazz);
  }
  if (was_unsafe) {
    enterUnsafeRegion(thread);
  }
  return m;
}

w_method getIMethodConstant_unsafe(w_clazz c, uint32_t i) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_boolean was_unsafe = enterSafeRegion(thread);
  w_method m = getIMethodConstant(c, i);
  if (was_unsafe) {
    enterUnsafeRegion(thread);
  }
  return m;
 }

w_field getFieldConstant_unsafe(w_clazz c, uint32_t i) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_boolean was_unsafe = enterSafeRegion(thread);
  w_field f = getFieldConstant(c, i);
  if (isSet(f->flags, ACC_STATIC)) {
    // needed for getstatic, putstatic
    mustBeInitialized(f->declaring_clazz);
  }
  if (was_unsafe) {
    enterUnsafeRegion(thread);
  }
  return f;
 }

w_method emul_special_target(im4000_frame frame, w_method called_method) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;
  w_clazz target_clazz = called_method->spec.declaring_clazz;

  if (isSet(called_method->flags, ACC_STATIC)) {
    THROW_EXCEPTION(clazzIncompatibleClassChangeError, NULL);
  }

  /*
  ** The logic of this opcode is rather complex, see https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-6.html#jvms-6.5.invokespecial .
  ** In the end there are two main cases:
  ** - we should invoke exactly the instance method specified, without worrying about possible overrides ("nonvirtual" case)
  ** - we should invoke the corresponding method in the immediate superclass ("invokesuper" case).
  */
    if ((target_clazz->flags & (ACC_FINAL | ACC_SUPER)) != ACC_SUPER 
      || isSet(called_method->flags, ACC_PRIVATE) || isSet(called_method->flags, METHOD_IS_CONSTRUCTOR) 
      || !isSuperClass(target_clazz, getSuper(calling_clazz)
    )) {
      woempa(7, "nonvirtual case - just call %m\n", called_method);
      // "nonvirtual" case
      /* TODO see if we can implement this - requires access to current code position
      if (called_method->exec.arg_i < 4) {
        if (called_method->exec.code && called_method->exec.code[0] == aload_0 && called_method->exec.code[1] == areturn) {
          woempa(1, "zapping invokespecial %M at pc[%d] of %M (was: %d %d %d)\n", called_method, current - method->exec.code, method, current[0], current[1], current[2]);
          *current = called_method->exec.arg_i > 0 ? pop : nop;
          *(++current) = called_method->exec.arg_i > 1 ? pop : nop;
          *(++current) = called_method->exec.arg_i > 2 ? pop : nop;
          woempa(1, "zapped invokespecial %M at pc[%d] of %M (now: %d %d %d)\n", called_method, current - method->exec.code, method, current[0], current[1], current[2]);
          tos -= called_method->exec.arg_i - 1;
          do_next_opcode;
          // that's a goto, code below is not executed
        }
      }
      */
      // TODO - replace by fastcode
      // woempa(1, "Replacing invokespecial by invokenonvirtual for %M\n", x);
      // *current = in_invokenonvirtual;
      lowMemoryCheck;

      return called_method;
    }
    else {
      // TODO - replace by fastcode
      // woempa(1, "Replacing invokespecial by invokensuper for %M\n", x);
      // *current = in_invokesuper;

      w_clazz super = getSuper(calling_clazz);
      woempa(7, "super case - look up %m in vmlt of superclass %k\n, called_method, super");

      if (!super) {
        THROW_EXCEPTION(clazzIncompatibleClassChangeError, NULL);
      }

      w_method target_method = virtualLookup(called_method, super);

      if (isSet(target_method->flags, ACC_ABSTRACT)) {
        THROW_EXCEPTION(clazzAbstractMethodError, NULL);
      }

      woempa(7, "target method is %m\n", target_method);
      lowMemoryCheck;
      return target_method;
  }
}

w_method emul_virtual_target(w_method called_method, w_instance objectref) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;

  if (isSet(called_method->flags, METHOD_NO_OVERRIDE) && called_method->exec.code) {
    woempa(7, "no override possible - just call %M\n", called_method);

    lowMemoryCheck;
    return called_method;
  }

  w_clazz target_clazz = instance2clazz(objectref);
  w_method target_method = virtualLookup(called_method, target_clazz);

  if (isSet(target_method->flags, ACC_STATIC | ACC_ABSTRACT)) {
   THROW_EXCEPTION(clazzIncompatibleClassChangeError, NULL);
  }

  woempa(7, "target method is %M\n", target_method);

  lowMemoryCheck;
  return target_method;
}

w_method emul_interface_target(w_method called_method, w_instance objectref) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;

  w_clazz target_clazz = instance2clazz(objectref);
  w_method target_method = interfaceLookup(called_method, target_clazz);
  woempa(7, "target method is %m\n", target_method);

  if (!target_method || isSet(target_method->flags, ACC_ABSTRACT)) {
    THROW_EXCEPTION(clazzAbstractMethodError, NULL);
  }

 if (isSet(target_method->flags, ACC_STATIC)) {
    THROW_EXCEPTION(clazzIncompatibleClassChangeError, NULL);
 }

  if (isNotSet(target_method->flags, ACC_PUBLIC)) {
    THROW_EXCEPTION(clazzIllegalAccessError, NULL);
  }
 
  lowMemoryCheck;

  return target_method;
}

w_method emul_static_target(im4000_frame frame, w_method called_method) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;

  if (!(isSet(called_method->flags, ACC_STATIC) && isNotSet(called_method->flags, METHOD_IS_CLINIT))) {
    THROW_EXCEPTION(clazzIncompatibleClassChangeError, NULL);
  }

  lowMemoryCheck;
  return called_method;
}

/**
 * Load the 32-bit value of an item in the constant pool onto the stack.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of the value to be loaded.
 * @return the 32-bit value.
 */
uint32_t emul_ldc(im4000_frame frame, uint32_t cpIndex) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  w_boolean was_unsafe = enterSafeRegion(thread);
  uint32_t constant = get32BitConstant(calling_clazz, cpIndex, 0, thread);
  if (was_unsafe) {
    enterUnsafeRegion(thread);
  }
  CHECK_FOR_PENDING_EXCEPTION

  lowMemoryCheck;
  return constant;
}

/**
 * Load the 64-bit value of an item in the constant pool onto the stack.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of the value to be loaded.
 * @return      the 64-bit value.
*/
uint64_t emul_ldc2_w(im4000_frame frame, uint32_t cpIndex) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  w_boolean was_unsafe = enterSafeRegion(thread);
  uint64_t constant = get64BitConstant(calling_clazz, cpIndex, 0, thread).u64;
  if (was_unsafe) {
    enterUnsafeRegion(thread);
  }
  CHECK_FOR_PENDING_EXCEPTION

  lowMemoryCheck;
  return constant;
}

/**
 * Load the 64-bit value of an item in the constant pool onto the stack.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of the value to be loaded.
 * @return      the 64-bit value.
*/
uint64_t emul_ldc_w(im4000_frame frame, uint32_t cpIndex) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  w_boolean was_unsafe = enterSafeRegion(thread);
  uint64_t constant = get64BitConstant(calling_clazz, cpIndex, 0, thread).u64;
  if (was_unsafe) {
    enterUnsafeRegion(thread);
  }
  CHECK_FOR_PENDING_EXCEPTION

  lowMemoryCheck;
  return constant;
}

/// emul_jsr(offset)?
// emul_ret()?

/**
 * Fetch the value of a 32-bit static field.
 * 
 * @param field the static field from which the value is to be fetched.
 * @return the 32-bit value
 * 
 */
w_word emul_getstatic_single(w_field field) {
  lowMemoryCheck;
  return field->declaring_clazz->staticFields[field->size_and_slot];
}

/**
 * Fetch the value of a 64-bit static field.
 * 
 * @param field the static field from which the value is to be fetched.
 * @return the 64-bit value
 * 
 */
w_dword emul_getstatic_double(w_field field) {
  lowMemoryCheck;
  void *ptr = field->declaring_clazz->staticFields + field->size_and_slot;
  return *(w_dword*)ptr;
}

static inline w_word* field_address(w_instance objectref, w_field field) {
  w_int offset = (w_int)FIELD_OFFSET(field->size_and_slot);
  if (offset < 0) {
    offset += instance2clazz(objectref)->instanceSize;
  }
  return objectref + offset;
}

/**
 * Fetch the value of a 32-bit field of an object.
 * 
 * @param field the field description.
 * @param objectref the object from which the value is to be fetched.
 * @return the 32-bit value
 * 
 */
w_word emul_getfield_single(w_instance objectref, w_field field) {
  lowMemoryCheck;
  return *field_address(objectref, field);
}

/**
 * Fetch the value of a 64-bit field of an object.
 * 
 * @param field the field description.
 * @param objectref the object from which the value is to be fetched.
 * @return the 64-bit value
 * 
 */
w_dword emul_getfield_double(w_instance objectref, w_field field) {
  lowMemoryCheck;
  void *ptr = field_address(objectref, field);
  return *(w_dword*)ptr;
}

/**
 * Set the value of a 32-bitstatic field.
 * 
 * @param value the 32-bit value to be set.
 * @param field he field description.
*/
void emul_putstatic_single(w_word value, w_field field) {
  lowMemoryCheck;
  w_word *ptr = (w_word *)&field->declaring_clazz->staticFields[FIELD_OFFSET(field->size_and_slot)];
  *ptr = value;
  lowMemoryCheck;
}

/**
 * Set the value of a 64-bitstatic field.
 * 
 * @param value_high the high half of the 64-bit value to be set.
 * @param value_low the low half of the 64-bit value to be set.
 * @param field he field description.
*/
void emul_putstatic_double(w_word value_high, w_word value_low, w_field field) {
  lowMemoryCheck;
  w_word *ptr = (w_word *)&field->declaring_clazz->staticFields[FIELD_OFFSET(field->size_and_slot)];
  ptr[0] = value_high;
  ptr[1] = value_low;
  lowMemoryCheck;
}

/**
 * Set the value of a 32-bit instance field.
 * 
 * @param objectref the instance in which the value should be set.
 * @param value the 32-bit value to be set.
 * @param field the field description.
 */
void emul_putfield_single( w_instance objectref, w_word value, w_field field) {
  lowMemoryCheck;
  *field_address(objectref, field) = value;
  lowMemoryCheck;
}

/**
 * Set the value of a 64-bit instance field.
 * 
 * @param objectref the instance in which the value should be set.
 * @param value_high high half of the 64-bit value to be set.
 * @param value_low low half of the 64-bit value to be set.
 * @param field the field description.
 */
void emul_putfield_double( w_instance objectref, w_word value_high, w_word value_low, w_field field) {
  lowMemoryCheck;
  field_address(objectref, field)[0] = value_high;
  field_address(objectref, field)[1] = value_low;
  lowMemoryCheck;
}

/**
 * Create a new instance of the class described by entry 'index' in the constant pool of class 'clazz',
 * If this is an interface type or an abstract class, an InstantiationError will be thrown. If the class
 * has not yet been linked and initalised then this will be done, possible throwing an exception in case
 * of failure.
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the target class is defined.
 * @return the created instance.
 */
w_instance emul_new(im4000_frame frame, uint32_t cpIndex) 
{
  lowMemoryCheck;
    w_thread thread = currentWonkaThread;
    w_method calling_method = frame->method;
    w_clazz calling_clazz = calling_method->spec.declaring_clazz;
    w_boolean was_unsafe = enterSafeRegion(thread);
    w_clazz target_clazz = getClassConstant(calling_clazz, cpIndex, thread);
    woempa(7, "target clazz is %k\n", target_clazz);
    if (target_clazz) {
      woempa(7, "need to initialise %k\n", target_clazz);
      mustBeInitialized(target_clazz);
    }

    enterUnsafeRegion(thread);
    CHECK_FOR_PENDING_EXCEPTION

    if(isSet(target_clazz->flags, ACC_ABSTRACT | ACC_INTERFACE)) {
      THROW_EXCEPTION(clazzInstantiationError, "%k", target_clazz);
    }

    w_instance o = allocInstance(thread, target_clazz);
    CHECK_FOR_PENDING_EXCEPTION
    woempa(7, "created object %j\n", o); 
 
    if (o) {
      removeLocalReference(thread, o);
    }
    if (!was_unsafe) {
      enterSafeRegion(thread);
    }

  lowMemoryCheck;
    return o;
}

/**
 * Create a new one-dimensional array of a primitive type.
 * 
 * @param count the number of elements in the array.
 * @param atype the type of the array elements, encoded as follows:
 * 4 = boolean, 5 = char, 6 = float, 7 = double, 8 = byte, 9 = short, 10 - int, 11 = long.
 * @return      the created array instance.
 */
w_instance emul_newarray(int32_t count, uint8_t atype) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;

  if (count < 0) {
    THROW_EXCEPTION(clazzNegativeArraySizeException, NULL);
  }

  w_clazz array_clazz = atype2clazz[atype];
  enterSafeRegion(thread);
  mustBeInitialized(array_clazz);
  enterUnsafeRegion(thread);
  CHECK_FOR_PENDING_EXCEPTION

  woempa(1, "Allocating array of %d %k\n", count, array_clazz->previousDimension);
  w_instance a = allocArrayInstance_1d(thread, array_clazz, (w_int)count);

  CHECK_FOR_PENDING_EXCEPTION
  if (!a) {
    wabort(ABORT_WONKA, "Tonnerre et mille sabots! allocArrayInstance returned NULL but did not set exceptionThrown");
  }
  lowMemoryCheck;
  return a;
}

/**
 * Create a new one-dimensional array of a non-primitive type.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param count the number of elements in the array.
 * @return      the created array instance.
 */
w_instance emul_anewarray(im4000_frame frame, uint32_t cpIndex, int32_t count) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

   if (count < 0) {
    THROW_EXCEPTION(clazzNegativeArraySizeException, NULL);
  }

  w_boolean was_unsafe = enterSafeRegion(thread);
  w_clazz array_clazz = getClassConstant(calling_clazz, cpIndex, thread);

  if (array_clazz) {
    array_clazz = getNextDimension(array_clazz, clazz2loader(frame->method->spec.declaring_clazz));
    if (array_clazz) {
      mustBeInitialized(array_clazz);
    }
  }
  else if (isAssignmentCompatible(instance2clazz(exceptionThrown(thread)), clazzException)) {
    wrapException(thread, clazzNoClassDefFoundError, F_Throwable_cause);
  }

  enterUnsafeRegion(thread);

  w_instance a = NULL;
  if (!exceptionThrown(thread)) {
    woempa(1, "Allocating array of %d %k\n", count, array_clazz->previousDimension);
    a = allocArrayInstance_1d(thread, array_clazz, count);
  }

  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
  CHECK_FOR_PENDING_EXCEPTION


  lowMemoryCheck;
  return a;
}

/**
 * Get the length of an array instance.
 * 
 * @param arrayref the array.
 * @return         its length.
 */
w_int emul_arraylength(w_instance arrayref) 
{
  w_int len = instance2Array_length(arrayref);
  return len;
}

/**
 * Check whether an object is of given type, and if it is not then throw a ClassCastException.
 * Note: no exception will be thrown if 'objectref' is null.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the target class is defined.
 * @param objectref the object to be checked.
 */
w_instance emul_checkcast(im4000_frame frame, uint32_t cpIndex, w_instance objectref) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  w_boolean was_unsafe = enterSafeRegion(thread);
  w_clazz subject_clazz = getClassConstant(calling_clazz, (w_ushort) cpIndex, thread);
    
  if (!exceptionThrown(thread) && objectref) {
    w_boolean compatible;

    // TODO: make isAssignmentCompatible() GC-safe (means using constraints)
    enterUnsafeRegion(thread);
    compatible = isAssignmentCompatible(instance2object(objectref)->clazz, subject_clazz);

    if (!compatible) {
      THROW_EXCEPTION(clazzClassCastException, NULL);
    }

  }

  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
  CHECK_FOR_PENDING_EXCEPTION
  lowMemoryCheck;
  return objectref;
}

/**
 * Check whether an object is of given type, returning a boolean result.
 * Note: returns true if 'objectref' is null.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the target class is defined.
 * @param objectref the object to be checked.
 * @return      true if 'objectref' is an instance of the class, false otherwise.
 */
bool emul_instanceof(im4000_frame frame, w_instance objectref , uint32_t cpIndex) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  if(objectref == NULL){
    lowMemoryCheck;
    return true;
  }

  if(instance2object(objectref)->clazz->dotified == calling_clazz->dotified){
    lowMemoryCheck;
    return true;
  } else {
    lowMemoryCheck;
    return false;
  }
}

/**
 * Enter the monitor which is implicitly associated with an object.
 * 
 * @param objectref the object whose monitor is to be entered.
 */
void emul_monitorenter(w_instance objectref) {
  lowMemoryCheck;
  x_monitor m = getMonitor(objectref);
  x_monitor_eternal(m);
  lowMemoryCheck;
}

/**
 * Leave the monitor which is implicitly associated with an object.
 * 
 * @param objectref the object whose monitor is to be left.
 */
void emul_monitorexit(w_instance objectref) {
  lowMemoryCheck;
  x_monitor m = getMonitor(objectref);
  w_thread thread = currentWonkaThread;

  if (x_monitor_exit(m) != xs_success) {
    THROW_EXCEPTION(clazzIllegalMonitorStateException, NULL);
  }
  lowMemoryCheck;
}

/**
 * Create a new multi-dimensional array of a non-primitive type.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param nbrDimensions the number of dimensions.
 * @param dimensions the number of elements in each dimension of the array.
 * @return      the created array instance.
 */
w_instance emul_multianewarray(im4000_frame frame, uint32_t cpIndex, uint32_t nbrDimensions, uint32_t* dimensions) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  w_boolean was_unsafe = enterSafeRegion(thread);
  w_clazz array_clazz = getClassConstant(calling_clazz, cpIndex, thread);

if (array_clazz) {
    array_clazz = getNextDimension(array_clazz, clazz2loader(frame->method->spec.declaring_clazz));
    if (!exceptionThrown(thread) && array_clazz) {
      mustBeInitialized(array_clazz);
    }
  }
  else if (isAssignmentCompatible(instance2clazz(exceptionThrown(thread)), clazzException)) {
    wrapException(thread, clazzNoClassDefFoundError, F_Throwable_cause);
  }

  enterUnsafeRegion(thread);

  w_instance a = NULL;
  if (!exceptionThrown(thread)) {
    allocArrayInstance(thread, array_clazz, nbrDimensions, dimensions);
  }

  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
  CHECK_FOR_PENDING_EXCEPTION
  lowMemoryCheck;
  return a;  
}

/**
 * Store a reference, int or float value into a local variable.
 * @param frame the current stack frame.
 * @param value the value to be stored.
 * @param idx   index of the variable within the stack frame.
 * 
*/
void emul_istore(im4000_frame frame, uint32_t value, uint32_t idx){
  lowMemoryCheck;
   LOCAL_VARIABLE_ARRAY(frame)[idx] = value;
  lowMemoryCheck;
}

/**
 * Store a long or double value into a local variable.
 * @param frame the current stack frame.
 * @param value1 the first word of the value to be stored.
 * @param value2 the second word of the the value to be stored.
 * @param idx   index of the variable within the stack frame.
*/
void emul_lstore(im4000_frame frame, uint32_t value1, uint32_t value2, uint32_t idx){
  lowMemoryCheck;
  LOCAL_VARIABLE_ARRAY(frame)[idx] = value1;
  LOCAL_VARIABLE_ARRAY(frame)[idx + 1] = value2;
  lowMemoryCheck;
}

/**
 * Load a reference, int or float value from a local variable.
 * @param frame the current stack frame.
 * @param idx   index of the variable within the stack frame.
 * @return the value of the variable.
* 
*/
w_word emul_iload(im4000_frame frame, uint32_t idx){
  lowMemoryCheck;
  w_word value = LOCAL_VARIABLE_ARRAY(frame)[idx];
  lowMemoryCheck;
  return value;
}

/**
 * Load a long or double value from a local variable.
 * @param frame the current stack frame.
 * @param idx   index of the variable within the stack frame.
 * @return the value of the variable.
* 
*/
w_word emul_lload(im4000_frame frame, uint32_t idx){
  lowMemoryCheck;
  void *ptr = LOCAL_VARIABLE_ARRAY(frame) + idx;
  lowMemoryCheck;
  return *(w_dword*)ptr;
}

/**
 * Complete invocation of a native method and throw exception if any left in the thread
 *
 * @param frame the caller stack frame.
 * @param method the method the invoke, which has a native implementation
 * @param args base address of array containing arguments for method
 * @param return_buf buffer for return value if any
*/
void emul_invoke_native(im4000_frame frame, w_method method, const uint32_t *args, w_u64 *return_buf) {
  lowMemoryCheck;
  w_thread thread = currentWonkaThread;

  // The following code is basically activateFrame(), adapted to use an array of contents instead of a
  // varargs list of contents/scanning pairs.

  w_frame mika_frame = pushFrame(thread, method);
  w_size i = 0;
  w_instance protected = NULL;

  threadMustBeSafe(thread);
  if (!mika_frame) {
    // TODO - raise stack overflow error
  }

  while (i < method->exec.arg_i) {
    // [CG 20230531] Doing it this way so that the slot-type arguments will be consumed even if they are not used.
    w_word contents = args[i];
    SET_SLOT_CONTENTS(mika_frame->jstack_top++, contents);
    i += 1;
  }

  mika_frame->flags |= FRAME_NATIVE;
  callMethod(mika_frame, method);
  CHECK_FOR_PENDING_EXCEPTION

  switch(method->exec.return_i) {
  case 2:
    return_buf->words[1] = GET_SLOT_CONTENTS(--mika_frame->jstack_top);
    // fall through

    case 1:
      return_buf->words[0] = GET_SLOT_CONTENTS(--mika_frame->jstack_top);
      if (isNotSet(method->spec.return_type->flags, CLAZZ_IS_PRIMITIVE)) {
        protected = return_buf->words[0];
      }
       // fall through

    case 0:
      break;

    default:
      wabort(ABORT_WONKA, "Impossible exec.return_i value : %d\n", method->exec.return_i);
  }


  deactivateFrame(mika_frame, protected);

  // NOTE: Put whatever return value AT return_buf, that is for single-word return like
  // *(uint32_t*)return_buf = value;

  CHECK_FOR_PENDING_EXCEPTION
  lowMemoryCheck;
}
