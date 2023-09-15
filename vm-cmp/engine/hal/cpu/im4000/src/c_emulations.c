#include "stdbool.h"
#include "arrays.h"
#include "methods.h"
#include "mika_threads.h"
#include "wonka.h"

static w_frame getFrame() {
  return  currentWonkaThread->top;
}

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
  int32_t unused10; // RAR
} IM4000_Frame;

typedef struct IM4000_Frame *im4000_frame;

/**
 * Load the 32-bit value of an item in the constant pool onto the stack.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @return the 32-bit value.
 */
uint32_t emul_ldc(uint16_t cpIndex) {
    // JUNK CODE
    w_thread thread = currentWonkaThread;
    w_frame frame = getFrame();
    w_clazz calling_clazz = frame->method->spec.declaring_clazz;
    woempa(7, "in thread %t, calling clazz is %k\n", thread, calling_clazz);
    // enterSafeRegion(thread);
    return getClassConstant(calling_clazz, cpIndex, thread);
}

/**
 * Load the 32-bit value of an item in the constant pool onto the stack.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool of the value to be loaded.
 * @return      the 64-bit value.
*/
uint64_t emul_ldc2(w_clazz clazz, uint16_t index) {

    return 0;
}

// emul_jsr(offset)?
// emul_ret()?

/**
 * Fetch the value of a static field.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool where the target field is defined.
 * 
 * TODO figure out how we can return a 32- or 64-bit value!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 */
void emul_getstatic(w_clazz clazz, uint16_t index) {

}

/**
 * Set the value of a static field.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool where the target field is defined.
 * @param value pointer to the 32- or 64-bit value to be set.
*/
void emul_putstatic(w_clazz clazz, uint16_t index, void *value) {

}

/**
 * Fetch the value of an instance field.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool where the target field is defined.
 * @param objectref the instance from which the value should be fetched.
 * 
 * TODO figure out how we can return a 32- or 64-bit value!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 */
void emul_getfield(w_clazz clazz, uint16_t index, w_instance objectref) {

}

/**
 * Set the value of an instance field.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool where the target field is defined.
 * @param objectref the instance in which the value should be set.
 * @param value pointer to the 32- or 64-bit value to be set.
 */
void emul_putfield(w_clazz clazz, uint16_t index, void *value, w_instance objectref) {

}

/**
 * Create a new instance of the class described by entry 'index' in the constant pool of class 'clazz',
 * If this is an interface type or an abstract class, an InstantiationError will be thrown. If the class
 * has not yet been linked and initalised then this will be done, possible throwing an exception in case
 * of failure.
 * @param cpIndex index into the constant pool of 'clazz' where the target class is defined.
 * @return the created instance.
 */
w_instance emul_new(im4000_frame frame, uint16_t cpIndex) 
{
    lowMemoryCheck;
    w_thread thread = currentWonkaThread;
    w_method calling_method = frame->method;
    w_clazz calling_clazz = calling_method->spec.declaring_clazz;
    // woempa(7, "in thread %t, calling clazz is %k\n", thread, calling_clazz);
    // enterSafeRegion(thread);
    w_clazz target_clazz = getClassConstant(calling_clazz, cpIndex, thread);
    // woempa(7, "target clazz is %k\n", target_clazz);
    if (target_clazz) {
      mustBeInitialized(target_clazz);
    //  if(!thread->exception && isSet(target_clazz->flags, ACC_ABSTRACT | ACC_INTERFACE)) {
    //    throwException(thread, clazzInstantiationError, "%k", target_clazz);
    //  }
    }

    // TODO
    // enterUnsafeRegion(thread);
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
 * @param clazz the Java class in which the method currently executing is defined (not the target class!).
 * @param index index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param count the number of elements in the array.
 * @return      the created array instance.
 */
w_instance emul_anewarray(w_clazz clazz, uint16_t index, int32_t count) {

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
 * @param clazz the Java class in which the method currently executing is defined (not the target class!).
 * @param index index into the constant pool of 'clazz' where the target class is defined.
 * @param objectref the object to be checked.
 */
void emul_checkcast(w_clazz clazz, uint16_t index, w_instance objectref) {


}

/**
 * Check whether an object is of given type, returning a boolean result.
 * Note: returns true if 'objectref' is null.
 * 
 * @param clazz the Java class in which the method currently executing is defined (not the target class!).
 * @param index index into the constant pool of 'clazz' where the target class is defined.
 * @param objectref the object to be checked.
 * @return      true if 'objectref' is an instance of the class, false otherwise.
 */
bool emul_instanceof(w_clazz clazz, uint16_t index, w_instance objectref) {

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
 * @param clazz the Java class in which the method currently executing is defined (not the target class!).
 * @param index index into the constant pool of 'clazz' where the type of the array elements is defined.
 * @param dimensions the number of dimensions.
 * @param ...   the number of elements in each dimension of the array.
 * @return      the created array instance.
 */
w_instance emul_multianewarray(w_clazz clazz, uint16_t index, uint8_t dimensions, ...) {

}

/**
 * Invoke a virtual method.
 * 
 * TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
 */
void emul_invokevirtual(w_clazz clazz, uint16_t index, w_instance objectref, ...) {

}

/**
// TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
// Maybe we should pass a frame pointer instead of the clazz?
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
 */
void emul_invokespecial(w_clazz clazz, uint16_t index, w_instance objectref, ...) {

}

/**
 * Invoke a special method, such as a constructor.
 * 
 * TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
 */
void  emul_invokestatic(w_clazz clazz, uint16_t index, ...) {

}

/**
 * Invoke an interface method.
 * 
 * TODO figure out how we can return a 32- or 64-bit value, or no value at all!!!
 * Maybe we should pass a frame pointer instead of the clazz?
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool of 'clazz' where the method to be executed is defined.
 * @param ...   arguments to be passed to the method.
*/
void emul_invokeinterface(w_clazz clazz, uint16_t index, w_instance objectref, ...) {

}
