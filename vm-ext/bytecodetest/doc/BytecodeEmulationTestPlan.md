# Bytecode Emulation Test Plan



## Opcodes to be tested

- Certainly:
  
  - new, newarray, anewarray, multianewarray
  
  - invokespecial, invokevirtual, invokestatic, invokeinterface
  
  - getfield, putfield, getstatic, putstatic
  
  - ldc, ldc_w, ldc2_w
  
  - return, areturn, ireturn, freturn, lreturn, dreturn
  
  - checkcast, instanceof
  
  - monitorenter, monitorexit
  
  - athrow
  
  - "wide" version of some or all of the following opcodes:  
    iload, fload, aload, lload, dload, istore, fstore, astore, lstore, dstore,
    iinc.

- Possibly:
  
  - jsr, jsr_w, ret (incl. wide ret).

## Strategy

### Aims

- So far as possible, each test should cover just one use case of one opcode.
- No test should rely on an opcode which has not yet been tested.

### Constraints

- "Printing out" a result using System.out.println() causes quite a lot of opcodes to be executed -- so at least the early tests will have to either silently succeed or signal failure in some other way.
  Unfortunately we also cannot use the Java assert keyword until we have tested at least ldc_w, invokevirtual, putstatic, getstatic, new, invokespecial, and athrow. (These instructions are used in the bytecode which the Java compiler generates for an `assert` statement).
- The test suite will need to be launched from the `main() `method of some class, e.g. `wonka.bytecodetest.Main`. This `main()` method should not expect any parameters.

## Plan

### Non-instance opcodes : ldc\*, putstatic, getstatic, invokestatic

For these opcodes we do not need to create any java instances (objects). We run these tests within the `main()` method itself, so we do not need to execute any invoke\* opcode (other than invokestatic when we explicitly test this opcode).

#### Main flow

##### ldc -- putstatic - getstatic

Declare a static variable. Assign it a value, and check the value.

Should be tested for int, float, and String constants. 

###### ldc_w

Same as ****ldc**** but the constant must be at least no. 256 in the constant pool of the class. This will probably require us to develop a special "monster" class with many declarations.

Should be tested for int, float, and String constants.

###### ldc2_w

The constant must be a `double` or `long`: e.g. assign `Math.PI` to a `double`. Should be tested for both `long` and `double` cases.

###### invokestatic

Call a static method which updates the value of a static variable, and check that the variable has the new value.

######Java code

```
package wonka.bytecodetest;
import java.math.BigDecimal;
import java.lang.Math;

public class Main {**
 static String s;
 static double d;

  public static void main(String[] args) {
    s = "foo";
    if (s != "foo") System.exit(1);
    d = Math.PI;
    if (d != Math.PI) System.exit(1);
    int dummy = BigDecimal.ROUND_UP;
    test_invokestatic();
    if (s != "bar") System.exit(1);
  }

  private static void test_invokestatic() {
    s = "bar";
  }
}


```
######Compiled bytecode
```
 public static void main(java.lang.String[]);
  Code:
    0:	ldc	#2; //String foo
    2:	putstatic	#3; //Field s:Ljava/lang/String;
    5:	getstatic	#3; //Field s:Ljava/lang/String;
    8:	ldc	#2; //String foo
   10:	if_acmpeq	17
   13:	iconst_1
   14:	invokestatic	#4; //Method java/lang/System.exit:(I)V
   17:	ldc2_w	#5; //double 3.141592653589793d
   20:	putstatic	#7; //Field d:D
   23:	getstatic	#7; //Field d:D
   26:	ldc2_w	#5; //double 3.141592653589793d
   29:	dcmpl
   30:	ifeq	37
   33:	iconst_1
   34:	invokestatic	#4; //Method java/lang/System.exit:(I)V
   37:	iconst_0
   38:	istore_1
   39:	invokestatic	#8; //Method test_invokestatic:()V
   42:	getstatic	#3; //Field s:Ljava/lang/String;
   45:	ldc	#9; //String bar
   47:	if_acmpeq	54
   50:	iconst_1
   51:	invokestatic	#4; //Method java/lang/System.exit:(I)V
   54:	return

private static void test_invokestatic();
  Code:
   0:	ldc	#9; //String bar
   2:	putstatic	#3; //Field s:Ljava/lang/String;
   5:	return
```

#### Exceptional flows

##### ldc, ldc\_w, ldc2\_w

**n/a**

##### putstatic

The search for a matching field is performed in the class or interface referenced by the method constant and in all its superclasses and superinterfaces. More precisely, the referenced class and all its superinterfaces are searched, and if no matching field is found then the procedure is repeated on the superclass (and so on, all the way up to
java,lang.Object). This should be tested for various cases: immediate and more distant superclasses and superinterfaces, interfaces inherited via a superclass, etc..

If no matching field is found, `NoSuchFieldError` is thrown.

If the matching field is not accessible to the caller, `IllegalAccessError` is thrown.

If the matching field is not a static field, `IncompatibleClassChangeError` is thrown.

##### getstatic

The search for a matching field is performed in the class or interface referenced by the method constant and in all its superclasses and superinterfaces. as described for **putstatic**. This should be tested for various cases: immediate and more distant superclasses and superinterfaces, interfaces inherited via a superclass, etc..

If no matching field is found, `NoSuchFieldError` will be thrown.

If the matching field is not accessible to the caller, `IllegalAccessError` will be thrown.

If the matching field is not a static field, `IncompatibleClassChangeError` will be thrown.

### Instance opcodes -- object creation (new, invokespecial)

#### Main flow

##### new

Create an instance of a Java class -- for the first test this could be a class with no fields and a default no-args constructor.

##### invokespecial

The Java compiler will generate both a **new** and an **invokespecial**
opcode, so these two will be tested together. The default constructor
will also generate an **invokespecial** opcode to call the constructor
of java.lang.Object.

If we add a constructor which takes a parameter (any parameter) and contains
`this();`
as its first statement, we test another, slightly different, use case for **invokespecial**. Another constructor could begin with 

`super();` 

to test yet another use case.

Note that this test also implicitly tests the **return** instruction, as this will be the last opcode of the constructor.

#### Exceptional flows

##### new

If the class file referenced by the constant cannot be found then `NoClassDefFound` is thrown. (Testing this requires the tests to be run against a different classpath to that used at compilation).

If the class referenced by the constant is not accessible from the test class (e.g. it is a private class, or package-protected in another package) then `IllegalAccessException` is thrown. (Testing this requires that the accessibility of the class is different at compilation time to runtime).

Theoretically a `LinkageError` can be thrown when resolving the class constant, but this is not easy to provoke.

##### invokespecial

This opcode is used for direct invocation of instance initialization
methods and methods of the current class and its supertypes.

The **invokespecial** instruction can function in either of two ways,
depending on the value of the method constant specified by its operand.
These both need to be tested.

Case I corresponds to super.method() in the Java source: it applies if

- The method constant does not resolve to a constructor (\<init\>),
  and
- The defining class of the method is a superclass of the current class.

(The Java VM spec adds that the class must have the ACC_SUPER flag set, but this will always be true.)

In this case the search for the matching method begins with the direct superclass of the current class.

Everything else is case II, corresponding to the use of super() in a constructor. In this case the search for the matching method begins with the class which is referenced by the method constant.

In order to fully test method lookup it will be necessary to compile the test code against a different class hierarchy to what it will encounter at runtime; specifically, the effect of inserting an intermediate subclass (or classes) of the class where the method was defined needs to be tested.

(The two cases and the ACC_SUPER flag exist because initially Sun did not properly support separate compilation.)

Both native and non-native methods should be tested.

If no matching method is found, `NoSuchMethodError` is thrown.

If the matching method is not accessible to the caller, `IllegalAccessError` is thrown.

If the objectref on the stack is null, `NullPointerException` isthrown.

### Array creation -- primitive array (newarray)

#### Main flow

##### newarray

This instruction is used for 1-dimensional arrays of primitive types, i.e. `boolean`, `char`, float, `double`, `byte`, `short`, `int`, and `long`.

##### multianewarray

This instruction is used for multi-dimensional arrays of both primitive and reference types.

#### Exceptional flows

##### newarray

If count is less than zero, $NegativeArraySizeException$ is thrown.

### Array creation -- instance array (anewarray)

#### Main flow

##### anewarray

This instruction is used for 1-dimensional arrays of reference types.

#### Exceptional flows

##### anewarray

Resolving the class constant may result in a `LinkageError`, `NoClassDefFoundError`, or `IllegalAccessError`, as described for **new**.

If count is less than zero, $NegativeArraySizeException$ is thrown.

### Array creation -- multi-dimensional array (multianewarray)

#### Main flow

##### multianewarray

This instruction is used for multi-dimensional arrays of both primitive and reference types -- both should be tested.

#### Exceptional flows

##### multianewarray

Resolving the class constant may result in a `LinkageError`, `NoClassDefFoundError`, or `IllegalAccessError`, as described for **new**.

If the size specified for any dimension is less than zero, `NegativeArraySizeException` is thrown.

If the size specified for any dimension is **equal to zero, none of the subsequent dimensions will be created.

### Instance opcodes -- field access (putfield, getfield)

#### Main flow

##### putfield

Stores a value in a field of an instance.

##### getfield

Fetches the value of a field of an instance.

#### Exceptional flows

##### putfield

The search for a matching field is performed in the class or interface referenced by the method constant and in all its superclasses and superinterfaces. More precisely, the referenced class and all its superinterfaces are searched, and if no matching field is found then theprocedure is repeated on the superclass (and so on, all the way up to `java,lang.Object`). This should be tested for various cases: immediate and more distant superclasses and superinterfaces, interfaces inherited via a superclass, etc..

If no matching field is found, `NoSuchFieldError` is thrown.

If the matching field is not accessible to the caller, `IllegalAccessError` is thrown.

If the matching field is a static field, `IncompatibleClassChangeError` is thrown.

If the objectref on the stack is null, `NullPointerException` is thrown.

##### getfield

Fetches the value of a field of an instance. The search for a matching field is performed in the class or interface referenced by the method constant and in all its superclasses and superinterfaces, as described for **putfield**.

If no matching field is found, `NoSuchFieldError` is thrown.

If the matching field is not accessible to the caller, `IllegalAccessError` is thrown.

If the matching field is a static field, `IncompatibleClassChangeError` is thrown.

If the objectref on the stack is null, `NullPointerException` is thrown.

### Instance opcodes -- type checks (checkcast, instanceof)

#### Main flow

##### checkcast

Used when a reference is cast to a particular class, array, or
interface type in Java.

##### instanceof

Used to implement the Java instanceof keyword `instanceof`.

#### Exceptional flows

##### checkcast

If the objectref at the top of the stack is null, this opcode behaves as
a no-op. Otherwise:

- If objectref is of scalar (non-array) type _S_ and the cast is to a class type _T_, then _T_ must be _S_ or a superclass of _S_.
- If objectref is of scalar (non-array) type _S_ and the cast is to a interface type _I_, then _S_ must implement _I_ (directly, or indirectly via a superclass or superinterface).
- If objectref is of array type _S\[\]_ and the cast is to a class type _T_, then _T_ must be java.lang.Object.
- If objectref is of array type _S\[\]_ and the cast is to a interface type _I_, then _T_ must be `java.lang.Cloneable` or `java.io.Serializable`.
- If objectref is of array type _S\[\]_ and the cast is to an array type _T\[\]_, then _T_ must be the same as _S_ or else these rules must be applied recursively to _S_ and _T_.

If applying these rules results in failure then ClassCastException is
thrown.

##### instanceof

If the objectref on the stack is null, this opcode pushes 0 (false) onto
the stack. Otherwise:

- If objectref is of scalar (non-array) type _S_ and the cast is to a class type _T_, then 1 (true) is pushed onto the stack if _T_ is _S_ or a superclass of _S_, otherwise 0 (false) is pushed.
- If objectref is of scalar (non-array) type _S_ and the cast is to a interface type _I_, then 1 (true) is pushed onto the stack if _S_ implements _I_ (directly or indirectly), otherwise 0 (false) is pushed.
- If objectref is of array type _S\[\]_ and the cast is to a class type _T_, then 1 (true) is pushed onto the stack if T is `java.lang.Object`, otherwise 0 (false) is pushed.
- If objectref is of array type _S\[\]_ and the cast is to a interface type _I_, then 1 (true) is pushed onto the stack if _T_ is `java.lang.Cloneable` or `java.io.Serializable`, otherwise 0 (false) is pushed.
- If objectref is of array type _S\[\]_ and the cast is to the same array type _S\[\]_, then 1 (true) is pushed onto the stack.
- If objectref is of array type _S\[\]_ and the cast is to a different array type _T\[\]_, then these rules must be applied recursively to _S_ and _T_.

### Instance opcodes -- virtual method (invokevirtual)

#### Main flow

Used to invoke instant methods which are not private or final.

#### Exceptional flows

The search for a matching method is performed in the class referenced by
the method constant and in all its superclasses.

In order to fully test method lookup it will be necessary to compile the
test code against a different class hierarchy to what it will encounter
at runtime; specifically, the effect of inserting an intermediate
subclass (or classes) of the class where the method was defined needs to
be tested.

Both native and non-native methods should be tested.

If no matching method is found in the target class or its superclasses, `NoSuchMethodError` is thrown.

If the matching method is not accessible to the caller, `IllegalAccessError` is thrown.

If the matching method is a static method, `IncompatibleClassChangeError` is thrown.

If the matching method is abstract, `AbstractMethodError` is thrown.

If the objectref on the stack is null, `NullPointerException` is thrown.

## Method return (return, areturn, ireturn, freturn, lreturn, dreturn)

#### Main flow

Return from a method, possibly returning a value as indicated by the prefix letter (or no value in the case of **return**). Each return type (a=reference, i=int, f=float, l=long, d=double) should be tested.

#### Exceptional flows

n/a

### Instance opcodes -- interface method (invokeinterface)

#### Main flow

##### invokeinterface

Invoke a method via an interface. This is used when the field or variable which is used to call the method is declared as being of type *I*, and at runtime that field or variable holds an instance of class *C* where *C* implements *I*.

#### Exceptional flows

##### invokeinterface

The search for a matching method is performed in the interface referenced by the method constant and in all its superinterfaces. This should be tested for various cases: immediate and more distant superinterfaces, interfaces inherited via a superclass, etc..

Both native and non-native methods should be tested.

If the operand does not resolve to an interface method, `IncompatibleClassChangeError` is thrown.

If no matching method is found in the target class, `NoSuchMethodError` is thrown.

If the matching method is not accessible to the caller, `IllegalAccessError` is thrown.

If the matching method is abstract, `AbstractMethodError` is thrown.

If the matching method is a static method, `IncompatibleClassChangeError`is thrown.

If the objectref on the stack is null, `NullPointerException` is thrown.

### Instance opcodes -- monitors (monitorenter, monitorexit)

#### Main flow

##### monitorenter, monitorexit

These opcodes are used to implement synchronized blocks. Note that a block may be synchronized on a class, as in `synchronized(Foo.class) { ... }`, and this special case should also be tested.

#### Exceptional flows

##### monitorenter, monitorexit

If the objectref at the top of the stack is null, `NullPointerException` is thrown. This will be the case for **monitorenter** if the reference in the synchronized clause is null. (There is no way to create this situation for **monitorexit** by compiling Java code).

### Exception handling -- athrow

#### Main flow

Used to implement a throw statement.

#### Exceptional flows

If the objectref at the top of the stack is null, `NullPointerException`
is thrown.

In theory `IllegalMonitorStateException` can be thrown, but there is no
way to produce this situation by compiling Java code.
