/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: exception_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <tests.h>

static x_sleep second;

#define EXC_STACK_SIZE ((1024 * 1) + MARGIN)

static x_thread thread_1;
static x_thread thread_2;

/*
** This variable controls the recursion on the stack of the depth_charge function. Don't
** set it too high since you might corrupt your stack; for test 1.
*/

static x_int trigger_depth;

/*
** This variable tracks if callbacks are being called in the right order. Also for test 1.
*/

static x_int callback_number = 0;

/*
** The following functions checks a single exception on its behaviour with callbacks,
** if it is thrown properly or not (it toggles throwing behaviour with non throwing)
** and if has been popped properly from the thread control block. This test does not
** yet test try/catch blocks within other try/catch blocks.
*/

static void callback_1(void * arg) {

  oempa("Callback %d has been called with argument 0x%08x.\n", callback_number, arg);

  if (callback_number != 2) {
    oempa("Bad order of callbacks %d in stead of 2.\n", callback_number);
    exit(0);
  }

}

static void callback_2(void * arg) {

  oempa("Callback %d has been called with pointer 0x%08x, size %d.\n", callback_number, arg, x_mem_size(arg));
  x_mem_free(arg);

  if (callback_number != 1) {
    oempa("Bad order of callbacks %d in stead of 1.\n", callback_number);
    exit(0);
  }

  callback_number += 1;
  
}

static void callback_3(void * arg) {

  oempa("Callback %d has been called with pointer 0x%08x.\n", callback_number, arg);

  if (callback_number != 0) {
    oempa("Bad order of callbacks %d in stead of 0.\n", callback_number);
    exit(0);
  }

  callback_number += 1;
  
}

/*
** We declare the functions here and define them later so that no aggresive optimizing
** compiler will inline them.
*/

void depth_charge(x_int depth, x_ubyte * previous_stack, x_int diff);
x_int stack_pusher(x_int i);

static void exception_test_1(void * arg) {

  volatile void * caught = NULL;
  int dothrow = 1;
  int loop = 0;
  x_status status;

  /*
  ** Every stack variable used inside a x_Try MUST be declared volatile !
  */

  volatile x_int returned = 0;

  while (1) {
    x_assert(critical_status == 0);   

    /*
    ** Toggle dothrow...
    */
    
    if (dothrow == 1) {
      dothrow = 0;
    }
    else {
      dothrow = 1;
    }

    status = x_exception_callback(callback_1, NULL);
    if (status != xs_no_instance) {
      oempa("Callback should not succeed since no exception in scope.\n");
      exit(0);
    }

    x_Try {
      if (thread_current->l_exception == NULL) {
        oempa("Exception has not been pushed properly.\n");
        exit(0);
      }
      returned = 0;
      returned = stack_pusher(dothrow);
    }
    x_Catch (caught) {
      if (thread_current->l_exception != NULL) {
        oempa("Exception has not been popped properly.\n");
        exit(0);
      }
      oempa("OK, I have caught the exception %p\n", caught);
      if (returned != 0) {
        oempa("Return value should be 0, is now %d.\n", returned);
        exit(0);
      }
    }
  
    if (dothrow && caught == NULL) {
      oempa("Exception not thrown properly\n");
      exit(0);
    }
  
    if (! dothrow && caught != NULL) {
      oempa("Exception falsely thrown.\n");
      exit(0);
    }
  
    if (thread_current->l_exception != NULL) {
      oempa("Exception has not been popped properly...\n");
      exit(0);
    }
    
    loop += 1;
    oempa("------------- exception check %d done ------------------\n", loop);
    callback_number = 0;
    x_thread_sleep(second * 2 + x_random() % 20);
    
  }
  
}

void depth_charge(x_int depth, x_ubyte * previous_stack, x_int diff) {

  void * throw = (void *)0xcafebabe;
  x_ubyte * stack = (x_ubyte *)&depth;
  x_ubyte * mem;
  x_size size;
  x_size avail;
  x_status status;
  x_size stack_used;
  x_size stack_left;
  x_size stack_size;

  if (previous_stack == 0) {
    previous_stack = stack;
  }
    
  oempa("Countdown to %2d -> %2d, stack pointer 0x%08x (diff = %2d, total = %3d)\n", trigger_depth, depth, stack, previous_stack - stack, diff);
  x_thread_sleep(x_random() % 10 + 1);

  if (depth == 0) {
  
    /*
    ** Allocate some memory that will be released in callback_2.
    */
    
    size = x_random() % 100 + 1;
    avail = x_mem_avail();
    mem = x_mem_get(size);

    status = x_exception_callback(callback_2, mem);
    if (status != xs_success) {
      oempa("Bad status '%s'.\n", x_status2char(status));
      exit(0);
    }

    /*
    ** This should be the first callback that is executed.
    */
    
    status = x_exception_callback(callback_3, (void *)0xdeadbeef);
    if (status != xs_success) {
      oempa("Bad status '%s'.\n", x_status2char(status));
      exit(0);
    }

    x_stack_info(thread_current, &stack_size, &stack_used, &stack_left);
    oempa("Throwing at depth %2d, stack size = %4d, stack used = %4d (max), stack left = %4d (min).\n", trigger_depth, stack_size, stack_used, stack_left);
    oempa("Throwing 0x%08x, mem = 0x%08x, size = %3d, avail = %d bytes.\n", throw, mem, size, avail);
    x_Throw(throw);
  }
  else {
    depth_charge(depth - 1, stack, diff + previous_stack - stack);
  }

}

x_int stack_pusher(x_int i) {

  x_int depth;
  x_status status;

  status = x_exception_callback(callback_1, (void *)0xbeafbabe);
  if (status != xs_success) {
    oempa("Status wrong '%s'.\n", x_status2char(status));
    exit(0);
  }
      
  if (i) {
    depth = x_random() % 15 + 3;
    trigger_depth = depth;
    depth_charge(depth, 0, 0);
  }

  return 10;
  
}

/*
** The following funnctions check for exceptions being thrown in nested try/catch blocks.
** Note that we make the nested_x functions non static and define them in reverse order
** later so that the compiler will not inline them.
*/

void nested_1(x_boolean throw, x_exception check);
void nested_2(x_boolean throw, x_exception check);
void nested_3(x_boolean throw);

void exception_test_2(void * arg) {

  volatile void * caught = NULL;
  int loop = 0;
  x_boolean throw = false;

  while (1) {
    x_assert(critical_status == 0);   

    /*
    ** Toggle throwing behaviour.
    */
  
    if (throw) {
      throw = false;
    }
    else {
      throw = true;
    }

    /*
    ** And do the test; note that in this function and the nested_1 function, we pass the
    ** address of the non-visible Exception structure from the macro to check against
    ** in the nested_1 and nested_2 functions. This is for testing only and should not be
    ** done inside normal program fragments...
    */    

    x_Try {
      nested_1(throw, &Exception);
    }
    x_Catch (caught) {
      oempa("OK, I caught exception 0x%08x from nested_1.\n", caught);
      if (caught != (void *)0xcafe0001) {
        oempa("Bad exception value 0x%08x.\n", caught);
        exit(0);
      }
    }

    if (throw && caught == NULL) {
      oempa("Exception not thrown properly\n");
      exit(0);
    }

    /*
    ** At this point, no exception block should be registered in the thread
    ** control block anymore.
    */
    
    if (thread_current->l_exception != NULL) {
      oempa("Exceptions have not been popped properly.\n");
      exit(0);
    }
  
    loop += 1;
    oempa("------------- nesting check %d done ------------------\n", loop);
    x_thread_sleep(second * 2 + x_random() % 30);
    
  }

}

void nested_1(x_boolean throw, x_exception check) {

  volatile void * caught = NULL;

  x_Try {
    nested_2(throw, &Exception);
  }
  x_Catch (caught) {
    oempa("OK, I caught exception 0x%08x from nested_2.\n", caught);
    if (caught != (void *)0xcafe0002) {
      oempa("Bad exception value 0x%08x.\n", caught);
      exit(0);
    }
  }
  
  if (throw && caught == NULL) {
    oempa("Exception not thrown properly\n");
    exit(0);
  }
  
  if (throw) {
    x_Throw((void *)0xcafe0001);
  }

  /*
  ** Check that the correct exception block is in scope. The exception has been
  ** passed as 'check' argument by exception_test_2.
  */

  if (thread_current->l_exception != check) {
    oempa("Bad exception in scope.\n");
    exit(0);
  }

}

void nested_2(x_boolean throw, x_exception check) {

  volatile void * caught = NULL;

  x_Try {
    nested_3(throw);
  }
  x_Catch (caught) {
    oempa("OK, I caught exception 0x%08x from nested_3.\n", caught);
    if (caught != (void *)0xcafe0003) {
      oempa("Bad exception value 0x%08x.\n", caught);
      exit(0);
    }
  }
  
  if (throw && caught == NULL) {
    oempa("Exception not thrown properly\n");
    exit(0);
  }

  if (throw) {
    x_Throw((void *)0xcafe0002);
  }

  /*
  ** Check that the correct exception block is in scope. The exception has been
  ** passed as 'check' argument by nested_1.
  */

  if (thread_current->l_exception != check) {
    oempa("Bad exception in scope.\n");
    exit(0);
  }

}

void nested_3(x_boolean throw) {

  x_size stack_size;
  x_size stack_used;
  x_size stack_left;

  /*
  ** We print out some stack info to see if it's running away. Note that
  ** when compiled in NON DEBUG mode, this information is useless.
  */
    
  x_stack_info(thread_current, &stack_size, &stack_used, &stack_left);
  oempa("Throwing ? %s : stack size = %4d, stack used = %4d (max), stack left = %4d (min).\n", throw ? "YES" : "NO", stack_size, stack_used, stack_left);
  if (throw) {
    x_Throw((void *)0xcafe0003);
  }

}

x_ubyte * exception_test(x_ubyte * memory) {

  x_status status;
  x_ubyte * stack;

  second = x_seconds2ticks(1);

  thread_1 = x_alloc_static_mem(memory, sizeof(x_Thread));
  stack = x_alloc_static_mem(memory, EXC_STACK_SIZE);
  status = x_thread_create(thread_1, exception_test_1, thread_1, stack, EXC_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  thread_2 = x_alloc_static_mem(memory, sizeof(x_Thread));
  stack = x_alloc_static_mem(memory, EXC_STACK_SIZE);
  status = x_thread_create(thread_2, exception_test_2, thread_2, stack, EXC_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
    
  return memory;
  
}
