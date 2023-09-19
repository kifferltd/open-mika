#include "stdbool.h"
#include "arrays.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "methods.h"
#include "mika_threads.h"
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

/*
** Seek a handler for an exception which has been thrown in the current thread.
** @param frame the current execution frame of the thread.
** @param pc the current bytecode position in that frame
** @return 0 if no exception is pending, -1 if no handler is found, else the pc of the handler.
*/
int32_t seekHandler(im4000_frame frame, int32_t pc) {
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;
  w_slot auxs;
  w_instance pending;

  if (!thread->exception) {
    return 0;
  }
  
  w_boolean was_unsafe = enterUnsafeRegion(thread);
  if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
    w_printf("Thrown: Seeking handler for %e in %M, thread %t\n", thread->exception, calling_method, thread);
  }
  woempa(7, "Seeking handler for %k in %t, current frame is running %M\n", instance2clazz(thread->exception), thread, frame->method);
   auxs = (w_slot)thread->top->auxstack_top;
  /*
  ** Store the pending exception locally and clear the thread exception, since
  ** resolving the class constant could result in loading/initializing etc. and
  ** that will check for pending exceptions. 
  */
  pending = thread->exception;
  pushLocalReference(thread->top, pending);
  thread->exception = NULL;

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
      if (caught_clazz == NULL || isSuperClass(caught_clazz, instance2object(pending)->clazz)) {
        if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
          w_printf("Thrown: Catching %e in %M, thread %t\n", pending, calling_method, thread);
        }
        woempa(7, ">>>> Found a handler for %j (as %k) at pc = %d in method %M <<<<\n",
            pending, caught_clazz, ex->handler_pc, calling_method);
#ifdef JDWP
        enterSafeRegion(thread);
        jdwp_event_exception(pending, calling_method, ex->handler_pc);
        enterUnsafeRegion(thread);
#endif
        // TODO is this really needed?
        setReferenceField_unsafe(thread->Thread, NULL, F_Thread_thrown);
        thread->exception = NULL;
        thread->top->auxstack_top = auxs;
        if (!was_unsafe) {
          enterSafeRegion(thread);
        }
        return ex->handler_pc;
      }
    }
  }

  /* We didn't find a handler, so we put the pending exception back in the thread and
  ** return -1 so that it will be propagated upward.
  */

  thread->exception = pending;
  // TODO is this really needed?
  setReferenceField_unsafe(thread->Thread, pending, F_Thread_thrown);
  if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
    w_printf("Thrown: Propagating %e in %M, thread %t\n", thread->exception, calling_method, thread);
  }
  thread->top->auxstack_top = auxs;
  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
 
  return -1;
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
  }
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

}

/**
 * Set the value of a static field.
 * 
 * Implementation Notes: 
 * See emul_new for how to extract the callin_clazz from the frame.
 * See also label c_putstatic in interpreter.c for the interpreted version.
 * To get the description of the field to be updated, call getFieldConstant().
 * We also need to call mustBeInitialized() on the fields's declaring_clazz.
 * Currently the above needs to be wrapped in enterSafeRegion/enterUndafeRegion.
 * The address of the field in memory is field->declaring_clazz->staticFields[field->size_and_slot]
 * Check field->flags to see what kind of a field this is: if it is a LONG field, 
 * you need to copy two words from 'value' to the field address otherwise just 1 word.
 * 
 * field->declaring_clazz->staticFields[field->size_and_slot]
 * @param frame the current stack frame.
 * @param index index into the constant pool where the target field is defined.
 * @param value pointer to the 32- or 64-bit value to be set.
*/
void emul_putstatic(im4000_frame frame, uint16_t index, void *value) {

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
      }
    }

    enterUnsafeRegion(thread);
    // TODO
    // if (thread->exception) {
    //   do_the_exception;
    // }

    // *current = in_new;

    // TODO
    // if (!enough_free_memory(thread, clazz->bytes_needed)) {
    //   do_OutOfMemoryError(clazz->bytes_needed);
    // }
    // if (thread->exception) {
    //   do_the_exception;
    // }
    w_instance o = allocInstance(thread, target_clazz);
    woempa(7, "created object %j\n", o);
    // TODO
    // if (thread->exception) {
    //   do_the_exception;
    // }

    if (o) {
      removeLocalReference(thread, o);
    }
    enterSafeRegion(thread);
  // TODO
    // current += 2;
    // goto check_async_exception;

    return o;
}

/**
 * Create a new one-dimensional array of a primitive type.
 * 
 * @param atype the type of the array elements, encoded as follows:
 * 4 = boolean, 5 = char, 6 = float, 7 = double, 8 = byte, 9 = short, 10 - int, 11 = long.
 * @param count the number of elements in the array.
 * @return      the created array instance.
 */
w_instance emul_newarray(uint8_t atype, int32_t count) {

}

/**
 * Create a new one-dimensional array of a non-primitive type.
 * 
 * @param frame the current stack frame.
 * @param index index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param count the number of elements in the array.
 * @return      the created array instance.
 */
w_instance emul_anewarray(im4000_frame frame, uint16_t index, int32_t count) {

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
  w_thread thread = currentWonkaThread;
  w_method calling_method = frame->method;
  w_clazz calling_clazz = calling_method->spec.declaring_clazz;

  enterSafeRegion(thread);
  w_method called_method = getMethodConstant(calling_clazz, cpIndex);
  enterUnsafeRegion(thread);
  // TODO
  // if (thread->exception) {
  //  do_the_exception;
  // }
  // if (isSet(x->flags, ACC_STATIC)) {
  //   do_throw_clazz(clazzIncompatibleClassChangeError);
  // }

  /*
  ** The logic of this opcode is rather complex, see https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-6.html#jvms-6.5.invokespecial .
  ** In the end there are two main cases:
  ** - we should invoke exactly the instance method specified, without worrying about possible overrides ("nonvirtual" case)
  ** - we should invoke the corresponding method in the immediate superclass ("invokesuper" case).
  */
    if ((called_method->spec.declaring_clazz->flags & (ACC_FINAL | ACC_SUPER)) != ACC_SUPER 
      || isSet(called_method->flags, ACC_PRIVATE) || isSet(called_method->flags, METHOD_IS_CONSTRUCTOR) 
      || !isSuperClass(called_method->spec.declaring_clazz, getSuper(frame->method->spec.declaring_clazz)
    )) {
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

      // TODO - is this separate step really needed?
      called_method = getResolvedMethodConstant(calling_clazz, cpIndex);
 
      // TODO - will the microcode even call us in this case?
      // if (! GET_SLOT_CONTENTS(tos - x->exec.arg_i)) {
      //   do_throw_clazz(clazzNullPointerException);
      // }

      if (isNotSet(called_method->flags, METHOD_UNSAFE_DISPATCH)) {
        enterSafeRegion(thread);
        i_callMethod(frame, called_method);
        enterUnsafeRegion(thread);
     }
      else {
        i_callMethod(frame, called_method);
      }
       // TODO
       // goto check_async_exception;
    }
    else {
      // TODO
      // woempa(1, "Replacing invokespecial by invokensuper for %M\n", x);
      // *current = in_invokesuper;

      w_clazz super = getSuper(frame->method->spec.declaring_clazz);

      // TODO
      // if (!super) {
      //   do_throw_clazz(clazzIncompatibleClassChangeError);
      // }

      // TODO - is this separate step really needed?
      called_method  = getResolvedMethodConstant(calling_clazz, cpIndex);
      called_method = virtualLookup(called_method, super);

      // TODO
      // if (!x) {
      //   do_the_exception;
      // }
      // if (isSet(x->flags, ACC_ABSTRACT)) {
      //   clazz = super;
      //   do_AbstractMethodError;
      // }

      // TODO - will the microcode even call us in this case?
      // if (! GET_SLOT_CONTENTS(tos - x->exec.arg_i)) {
      //   do_throw_clazz(clazzNullPointerException);
      // }

      if (isNotSet(called_method->flags, METHOD_UNSAFE_DISPATCH)) {
       enterSafeRegion(thread);
       i_callMethod(frame, called_method);
       enterUnsafeRegion(thread);
      }
     else {
        i_callMethod(frame, called_method);
      }
      // TODO
      // goto check_async_exception;
  }
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
