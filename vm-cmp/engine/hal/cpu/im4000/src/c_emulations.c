#include "stdbool.h"
#include "arrays.h"
#include "wonka.h"

/**
 * Load the 32-bit value of an item in the constant pool onto the stack.
 * 
 * @param clazz the Java class in which the method currently executing is defined.
 * @param index index into the constant pool of the value to be loaded.
 * @return      the 32-bit value.
 */
uint32_t emul_ldc(w_clazz clazz, uint16_t offset) {

    return 0;
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
 * @param clazz the Java class in which the method currently executing is defined (not the target class!).
 * @param index index into the constant pool of 'clazz' where the target class is defined.
 * @return      the created instance.
 */
w_instance new_emul(w_clazz clazz, uint16_t index) 
{


    return NULL;
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
