#include "stdbool.h"
#include "arrays.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "loading.h"
#include "methods.h"
#include "mika_threads.h"
#include "mika_stack.h"
#include "wonka.h"

typedef struct IM4000_Frame {
  int32_t unused0;  // ConstantPool
  w_method method;
  w_instance unused2; // SyncObject
  int32_t unused3;  // ThrowPC
  int32_t unused4;  // LSP
  int32_t unused5;  // ESP
  int32_t unused6;  // LMP
  int32_t unused7;  // LocalsCount
  int32_t unused8;  // RetAddress
  int32_t unused9;  // FMP
} IM4000_Frame;

typedef struct IM4000_Frame *im4000_frame;

extern void throw(w_instance objectref) __attribute__((noreturn));

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
    w_printf("Thrown: Seeking handler for %e in %M, thread %t\n", thread->exception, calling_method, thread);
  }
  woempa(7, "Seeking handler for %k in %t, current frame is running %M\n", instance2clazz(thread->exception), thread, frame->method);

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
        if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
          w_printf("Thrown: Catching %e in %M, thread %t\n", objectref, calling_method, thread);
        }
        woempa(7, ">>>> Found a handler for %j (as %k) at pc = %d in method %M <<<<\n",
            objectref, caught_clazz, ex->handler_pc, calling_method);
#ifdef JDWP
        enterSafeRegion(thread);
        jdwp_event_exception(objectref, calling_method, ex->handler_pc);
        enterUnsafeRegion(thread);
#endif
        thread->exception = NULL;
        // TODO is this really needed?
        setReferenceField_unsafe(thread->Thread, NULL, F_Thread_thrown);
        handler_pc = ex->handler_pc;

        break;
      }
    }
  }

  if (handler_pc == -1) {
    woempa(7, ">>>> Found no handler for %j in this frame <<<<\n", objectref);
    thread->exception = objectref;
    // TODO is this really needed?
    setReferenceField_unsafe(thread->Thread, objectref, F_Thread_thrown);
    if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
      w_printf("Thrown: Propagating %e in %M, thread %t\n", thread->exception, calling_method, thread);
    }
  }
  thread->top->auxstack_top = saved_auxstack_top; // clean up aux stack
  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
 
  return handler_pc;
}


static void i_callMethod(w_frame caller, w_method method) {

#ifdef JAVA_PROFILE
  w_thread thread = caller->thread;
  x_long time_start;    
  x_long time_delta;    
#endif
#ifdef DEBUG_STACKS
  w_int depth = (char*)caller->thread->native_stack_base - (char*)&depth;
  if (depth > caller->thread->native_stack_max_depth) {
    if (isSet(verbose_flags, VERBOSE_FLAG_STACK)) {
      w_printf("%M: thread %t stack base %p, end %p, now at %p, used = %d%%\n", method, caller->thread, caller->thread->native_stack_base, (char*)caller->thread->native_stack_base - caller->thread->ksize, &depth, (depth * 100) / caller->thread->ksize);
    }
    caller->thread->native_stack_max_depth = depth;
  }
#endif

  woempa(1, "CALLING %M, dispatcher is %p\n", method, method->exec.dispatcher);
  if (caller->auxstack_top - (caller->jstack_top + method->exec.stack_i) > MIN_FREE_SLOTS
#ifdef DEBUG_STACKS
    && caller->thread->ksize - depth > 4096
#endif
  ) {
#ifdef JAVA_PROFILE
    if(method->exec.dispatcher) {
      updateProfileCalls(caller->method, method);
      time_start = x_systime_get();
      time_delta = caller->thread->kthread->time_delta;
      // w_dump(" --> %M\n", method);
   
      method->exec.dispatcher(caller, method);
      
      // w_dump(" <-- %8lld %M\n", x_systime_get() - time_start - (caller->thread->kthread->time_delta - time_delta), method);
      method->exec.runtime += x_systime_get() - time_start - (caller->thread->kthread->time_delta - time_delta);
      method->exec.totaltime += x_systime_get() - time_start;
    }
    else {
      method->exec.dispatcher(caller, method);
    }
#else
    method->exec.dispatcher(caller, method);
#endif
    woempa(1, "RETURNED from %M\n", method);
  }
  else {
    w_boolean unsafe = enterSafeRegion(caller->thread);

    throwException(caller->thread, clazzStackOverflowError, "unable to call %M: %d on aux stack, %d on java stack, need %d + %d free slots", method, caller->thread->slots + SLOTS_PER_THREAD - caller->auxstack_top, caller->jstack_top - caller->thread->slots, method->exec.stack_i, MIN_FREE_SLOTS);
    if (unsafe) {
      enterUnsafeRegion(caller->thread);
    }
    throw(caller->thread->exception);
  }
}

w_method emul_special_target(im4000_frame frame, w_method called_method) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;
  w_clazz target_clazz = called_method->spec.declaring_clazz;

  if (thread->exception){
    throw(thread->exception);
  }
  // TODO can we make use of e_exception for this?
  if (isSet(called_method->flags, ACC_STATIC)) {
    throwException(thread, clazzIncompatibleClassChangeError, NULL);
    throw(thread->exception);
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
      /*
      if (x->exec.arg_i < 4) {
        if (x->exec.code && x->exec.code[0] == aload_0 && x->exec.code[1] == areturn) {
          woempa(1, "zapping invokespecial %M at pc[%d] of %M (was: %d %d %d)\n", x, current - method->exec.code, method, current[0], current[1], current[2]);
          *current = x->exec.arg_i > 0 ? pop : nop;
          *(++current) = x->exec.arg_i > 1 ? pop : nop;
          *(++current) = x->exec.arg_i > 2 ? pop : nop;
          woempa(1, "zapped invokespecial %M at pc[%d] of %M (now: %d %d %d)\n", x, current - method->exec.code, method, current[0], current[1], current[2]);
          tos -= x->exec.arg_i - 1;
          do_next_opcode;
          // that's a goto, code below is not executed
        }
      }
      */
      // TODO
      // woempa(1, "Replacing invokespecial by invokenonvirtual for %M\n", x);
      // *current = in_invokenonvirtual;

      return called_method;
    }
    else {
      // TODO
      // woempa(1, "Replacing invokespecial by invokensuper for %M\n", x);
      // *current = in_invokesuper;

      w_clazz super = getSuper(calling_clazz);
      woempa(7, "super case - look up %m in vmlt of superclass %k\n, called_method, super");

      // TODO can we make use of e_exception for this?
      if (!super) {
        throwException(thread, clazzIncompatibleClassChangeError, NULL);
        throw(thread->exception);
      }

      w_method target_method = virtualLookup(called_method, super);
      woempa(7, "target method is %m\n", target_method);
      return target_method;
  }
}

w_method emul_virtual_target(im4000_frame frame, w_method called_method) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;
  w_clazz target_clazz = called_method->spec.declaring_clazz;

  if (thread->exception){
    throw(thread->exception);
  }
  // TODO can we make use of e_exception for this?
  if (isSet(called_method->flags, ACC_STATIC)) {
    throwException(thread, clazzIncompatibleClassChangeError, NULL);
    throw(thread->exception);
  }

  if (isSet(called_method->flags, METHOD_NO_OVERRIDE) && called_method->exec.code) {
    woempa(7, "no override possible - just call %m\n", called_method);

    return called_method;
  }
  else {
    w_method target_method = virtualLookup(called_method, target_clazz);
    woempa(7, "target method is %m\n", target_method);

    return target_method;
  }

}

w_method emul_static_target(im4000_frame frame, w_method called_method) {
  w_thread thread = currentWonkaThread;

  if (thread->exception){
    throw(thread->exception);
  }
  // TODO can we make use of e_exception for this?
  if (!(isSet(called_method->flags, ACC_STATIC) && isNotSet(called_method->flags, METHOD_IS_CLINIT))) {
    throwException(thread, clazzIncompatibleClassChangeError, NULL);
    throw(thread->exception);
  }

  return called_method;
}

/**
 * Load the 32-bit value of an item in the constant pool onto the stack.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of the value to be loaded.
 * @return the 32-bit value.
 */
uint32_t emul_ldc(im4000_frame frame, uint16_t cpIndex) {
    w_thread thread = currentWonkaThread;
    w_method calling_method = frame->method;
    w_clazz calling_clazz = calling_method->spec.declaring_clazz;

    // enterSafeRegion(thread);
    return getClassConstant(calling_clazz, cpIndex, thread);
}

/**
 * Load the 32-bit value of an item in the constant pool onto the stack.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool of the value to be loaded.
 * @return      the 64-bit value.
*/
uint64_t emul_ldc2(im4000_frame frame, uint16_t index) {

    return 0;
}

// emul_jsr(offset)?
// emul_ret()?

/**
 * Fetch the value of a static field.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool where the target field is defined.
 * 
 * TODO figure out how we can return a 32- or 64-bit value!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 */
void emul_getstatic(im4000_frame frame, uint16_t index) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  if (thread->exception){
    throw(thread->exception);
  }
  enterSafeRegion(thread);
  w_field source_field = getFieldConstant(calling_clazz, index);
  mustBeInitialized(source_field->declaring_clazz);
  enterUnsafeRegion(thread);

}

/**
 * Set the value of a static field.
 * 
 * Implementation Notes: 
 * See emul_new for how to extract the calling_clazz from the frame.
 * See also label c_putstatic in interpreter.c for the interpreted version.
 * To get the description of the field to be updated, call getFieldConstant().
 * We also need to call mustBeInitialized() on the fields's declaring_clazz.
 * Currently the above needs to be wrapped in enterSafeRegion/enterUnsafeRegion.
 * The address of the field in memory is field->declaring_clazz->staticFields[field->size_and_slot]
 * Check field->flags to see what kind of a field this is: if it is a LONG field, 
 * you need to copy two words from 'value' to the field address otherwise just 1 word.
 * For now we can skip the "current[0] = in_putstatic_double;" logic, that will be
 * for when we implement "fast" opcodes.
 * 
 * field->declaring_clazz->staticFields[field->size_and_slot]
 * @param frame the current stack frame.
 * @param index index into the constant pool where the target field is defined.
 * @param value pointer to the 32- or 64-bit value to be set.
*/
void emul_putstatic(im4000_frame frame, uint16_t index, void *value) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;
  w_field source_field;
  x_mutex mutex64;
  if (thread->exception){
    //do exception
  }
  enterSafeRegion(thread);
  source_field = getFieldConstant(calling_clazz, index);
  mustBeInitialized(source_field->declaring_clazz);
  enterUnsafeRegion(thread);

  if (isSet(source_field->flags, FIELD_IS_LONG)){
    w_dword *ptr = (w_dword *)&source_field->declaring_clazz->staticFields[source_field->size_and_slot];
    w_boolean isVolatile = isSet(source_field->flags, ACC_VOLATILE);

    if (isVolatile) {
      x_mutex_lock(mutex64, x_eternal);
    }
    *ptr = value;
  } else if (isSet(source_field->flags, FIELD_IS_REFERENCE)) {
    w_word *ptr = (w_word *)&source_field->declaring_clazz->staticFields[source_field->size_and_slot];
    *ptr = value;
  } else {
    w_word *ptr = (w_word *)&source_field->declaring_clazz->staticFields[source_field->size_and_slot];
    *ptr = value;
  }
}

/**
 * Fetch the value of an instance field.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool where the target field is defined.
 * @param objectref the instance from which the value should be fetched.
 * 
 * TODO figure out how we can return a 32- or 64-bit value!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 */
void emul_getfield(im4000_frame frame, uint16_t index, w_instance objectref) {

}

/**
 * Set the value of an instance field.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool where the target field is defined.
 * @param objectref the instance in which the value should be set.
 * @param value pointer to the 32- or 64-bit value to be set.
 */
void emul_putfield(im4000_frame frame, uint16_t index, void *value, w_instance objectref) {

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
w_instance emul_new(im4000_frame frame, uint16_t cpIndex) 
{
    w_thread thread = currentWonkaThread;
    w_method calling_method = frame->method;
    w_clazz calling_clazz = calling_method->spec.declaring_clazz;

    enterSafeRegion(thread);
    w_clazz target_clazz = getClassConstant(calling_clazz, cpIndex, thread);
    woempa(7, "target clazz is %k\n", target_clazz);
    if (target_clazz) {
      mustBeInitialized(target_clazz);
      if(!thread->exception && isSet(target_clazz->flags, ACC_ABSTRACT | ACC_INTERFACE)) {
        throwException(thread, clazzInstantiationError, "%k", target_clazz);
        throw(thread->exception);
      }
    }

    enterUnsafeRegion(thread);
    if (thread->exception){
      throw(thread->exception);
    }
    w_instance o = allocInstance(thread, target_clazz);
    woempa(7, "created object %j\n", o);
    if (thread->exception){
      throw(thread->exception);
    }
 
    if (o) {
      removeLocalReference(thread, o);
    }
    enterSafeRegion(thread);

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
  w_thread thread = currentWonkaThread;

  if (count < 0) {
    // TODO  can we make use of e_exception for this?
    throwException(thread, clazzNegativeArraySizeException, NULL);
    throw(thread->exception);
  }

  w_clazz array_clazz = atype2clazz[atype];
  void *bytes = (F_Array_data + roundBitsToWords(array_clazz->previousDimension->bits * count)) * sizeof(w_word);

  enterSafeRegion(thread);
  mustBeInitialized(array_clazz);
  enterUnsafeRegion(thread);
  if (thread->exception) {
    throw(thread->exception);
  }
  woempa(1, "Allocating array of %d %k\n", count, array_clazz->previousDimension);
  w_instance a = allocArrayInstance_1d(thread, array_clazz, (w_int)count);

  if (!a) {
    throw(thread->exception);
  }
}

/**
 * Create a new one-dimensional array of a non-primitive type.
 * 
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param count the number of elements in the array.
 * @return      the created array instance.
 */
w_instance emul_anewarray(im4000_frame frame, uint16_t cpIndex, int32_t count) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  // TODO can we make use of e_exception for this?
  if (count < 0) {
    throwException(thread, clazzNegativeArraySizeException, NULL);
    throw(thread->exception);
  }

  w_boolean was_unsafe = enterSafeRegion(thread);
  w_clazz array_clazz = getClassConstant(calling_clazz, cpIndex, thread);

  if (array_clazz) {
    array_clazz = getNextDimension(array_clazz, clazz2loader(frame->method->spec.declaring_clazz));
    if (array_clazz) {
      mustBeInitialized(array_clazz);
    }
  }
  else if (isAssignmentCompatible(instance2clazz(thread->exception), clazzException)) {
    wrapException(thread, clazzNoClassDefFoundError, F_Throwable_cause);
  }

  enterUnsafeRegion(thread);

  if (thread->exception) {
    throw(thread->exception);
  }

  woempa(1, "Allocating array of %d %k\n", count, array_clazz->previousDimension);
  w_instance a = allocArrayInstance_1d(thread, array_clazz, count);
  if (thread->exception) {
    throw(thread->exception);
  }

  if (!was_unsafe) {
    enterSafeRegion(thread);
  }

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
  // TODO who checks for null instance?
  w_int len = instance2Array_length(arrayref);
  return len;
}

// emul_athrow(???)

/**
 * Check whether an object is of given type, and if it is not then throw a ClassCastException.
 * Note: no exception will be thrown if 'objectref' is null.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the target class is defined.
 * @param objectref the object to be checked.
 */
void emul_checkcast(im4000_frame frame, uint16_t index, w_instance objectref) {


}

/**
 * Check whether an object is of given type, returning a boolean result.
 * Note: returns true if 'objectref' is null.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the target class is defined.
 * @param objectref the object to be checked.
 * @return      true if 'objectref' is an instance of the class, false otherwise.
 */
bool emul_instanceof(im4000_frame frame, uint16_t index, w_instance objectref) {

}

/**
 * Enter the monitor which is implicitly associated with an object.
 * 
 * @param objectref the object whose monitor is to be entered.
 */
void emul_monitorenter(w_instance objectref) {

}

/**
 * Leave the monitor which is implicitly associated with an object.
 * 
 * @param objectref the object whose monitor is to be left.
 */
void emul_monitorexit(w_instance objectref) {

}

/**
 * Create a new multi-dimensional array of a non-primitive type.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param dimensions the number of dimensions.
 * @param ...   the number of elements in each dimension of the array.
 * @return      the created array instance.
 */
w_instance emul_multianewarray(im4000_frame frame, uint16_t index, uint8_t dimensions, ...) {

}

/**
 * Invoke a virtual method.
 * 
 * TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
 */
void emul_invokevirtual(im4000_frame frame, uint16_t index, w_instance objectref, ...) {

}

/**
// TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * @param frame the current stack frame.
 * @param cpIndex index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
 */
void emul_invokespecial(im4000_frame frame, uint16_t cpIndex, w_instance objectref, ...) {

}

/**
 * Invoke a special method, such as a constructor.
 * 
 * TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
 */
void  emul_invokestatic(im4000_frame frame, uint16_t index, ...) {

}

/**
 * Invoke an interface method.
 * 
 * TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
*/
void emul_invokeinterface(im4000_frame frame, uint16_t index, w_instance objectref, ...) {

}

/**
 * Convenience function to report an exection an abort the VM.
 * Use e.g. if an exception is thrown before we have entered wonka.Vvm.Init.main()
 * (which provides a catch-all exception handler).
 * @param objectref the exception to be reported
*/
void emul_unhandled_exception(w_instance objectref) {
  wabort(ABORT_WONKA, "Unhandled exception %e - game over!", objectref);
}
