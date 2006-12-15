
#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "jni.h"
#include "methods.h"
#include "threads.h"
#include "wonka.h"

typedef w_long (w_fun)(JNIEnv*, w_instance, ...);

w_long _call_static(JNIEnv* env, w_instance theClass, w_slot top, w_methodExec exec) {
  w_fun *f = (w_fun*)exec->function.long_fun;

  switch (exec->arg_i) {
  case 0: 
      return f(env, theClass);

  case 1: 
      return f(env, theClass, top[-1].c);

  case 2: 
      return f(env, theClass, top[-2].c, top[-1].c);

  case 3: 
      return f(env, theClass, top[-3].c, top[-2].c, top[-1].c);

  case 4: 
      return f(env, theClass, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 5: 
      return f(env, theClass, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 6: 
      return f(env, theClass, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 7: 
      return f(env, theClass, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 8: 
      return f(env, theClass, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 9: 
      return f(env, theClass, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 10: 
      return f(env, theClass, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 11: 
      return f(env, theClass, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 12: 
      return f(env, theClass, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 13: 
      return f(env, theClass, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 14: 
      return f(env, theClass, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 15: 
      return f(env, theClass, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 16: 
      return f(env, theClass, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 17: 
      return f(env, theClass, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 18: 
      return f(env, theClass, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 19: 
      return f(env, theClass, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 20: 
      return f(env, theClass, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 21: 
      return f(env, theClass, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 22: 
      return f(env, theClass, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 23: 
      return f(env, theClass, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 24: 
      return f(env, theClass, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 25: 
      return f(env, theClass, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 26: 
      return f(env, theClass, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 27: 
      return f(env, theClass, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 28: 
      return f(env, theClass, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 29: 
      return f(env, theClass, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 30: 
      return f(env, theClass, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 31: 
      return f(env, theClass, top[-31].c, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  default: 
    {
      w_long dummy;

      throwException(JNIEnv2w_thread(env), clazzVirtualMachineError, "Too many parameters in call to native static method (max. 31 in this build)");
      dummy = 0LL;

      return dummy;
    }
  }
}

w_long _call_instance(JNIEnv* env, w_slot top, w_methodExec exec) {
  w_fun *f = (w_fun*)exec->function.long_fun;

  switch (exec->arg_i) {
  case 1: 
      return f(env, (w_instance)top[-1].c);

  case 2: 
      return f(env, (w_instance)top[-2].c, top[-1].c);

  case 3: 
      return f(env, (w_instance)top[-3].c, top[-2].c, top[-1].c);

  case 4: 
      return f(env, (w_instance)top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 5: 
      return f(env, (w_instance)top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 6: 
      return f(env, (w_instance)top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 7: 
      return f(env, (w_instance)top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 8: 
      return f(env, (w_instance)top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 9: 
      return f(env, (w_instance)top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 10: 
      return f(env, (w_instance)top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 11: 
      return f(env, (w_instance)top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 12: 
      return f(env, (w_instance)top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 13: 
      return f(env, (w_instance)top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 14: 
      return f(env, (w_instance)top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 15: 
      return f(env, (w_instance)top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 16: 
      return f(env, (w_instance)top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 17: 
      return f(env, (w_instance)top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 18: 
      return f(env, (w_instance)top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 19: 
      return f(env, (w_instance)top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 20: 
      return f(env, (w_instance)top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 21: 
      return f(env, (w_instance)top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 22: 
      return f(env, (w_instance)top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 23: 
      return f(env, (w_instance)top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 24: 
      return f(env, (w_instance)top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 25: 
      return f(env, (w_instance)top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 26: 
      return f(env, (w_instance)top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 27: 
      return f(env, (w_instance)top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 28: 
      return f(env, (w_instance)top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 29: 
      return f(env, (w_instance)top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 30: 
      return f(env, (w_instance)top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 31: 
      return f(env, (w_instance)top[-31].c, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  default: 
    {
      w_long dummy;

      throwException(JNIEnv2w_thread(env), clazzVirtualMachineError, "Too many parameters in call to native instance method (max. 30 in this build)");
      dummy = 0LL;

      return dummy;

    }
  }
}


