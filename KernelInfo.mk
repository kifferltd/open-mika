ifeq ($(SCHEDULER), o4p)
  ifeq ($(USE_NANOSLEEP), true)
    O4P_INFO = using nanosleep(2) for internal timing loop\;
  else
    O4P_INFO = using usleep(3) for internal timing loop\;
  endif

  ifeq ($(HAVE_TIMEDWAIT), true)
    O4P_INFO += using pthread_cond_timedwait\;
  else
    O4P_INFO += not using pthread_cond_timedwait\;
  endif

  ifeq ($(USE_NATIVE_MALLOC), true)
    O4P_INFO += using native malloc\;
  else
    O4P_INFO += using own memory management routines\;
  endif

  ifdef HOST_TIMER_GRANULARITY
    O4P_INFO += host timer granularity = $(HOST_TIMER_GRANULARITY) usec\;
  endif
  export O4P_INFO
else ifeq ($(SCHEDULER), o4f)
  O4F_INFO += hello world
  export O4F_INFO
else
  ifdef CPU_MIPS
    OSWALD_INFO += estimated CPU speed = $(CPU_MIPS) MIPS\;
  endif

  ifdef HOST_TIMER_GRANULARITY
    OSWALD_INFO += host timer granularity = $(HOST_TIMER_GRANULARITY) usec\;
  endif

  ifeq ($(SHARED_HEAP), true)
    OSWALD_INFO += exporting own version of malloc and friends\;
  endif
  export OSWALD_INFO
endif

