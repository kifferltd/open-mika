export WONKA_INFO

ifdef CCLASSPATH
  WONKA_INFO += runtime classpath is $(CCLASSPATH);
endif

ifeq ($(FILESYSTEM), vfs)
  WONKA_INFO += using own virtual filesystem\;
endif
 
ifeq ($(FILESYSTEM), native)
  WONKA_INFO += using host OS filesystem, with virtual root at $(FSROOT)\;
endif

ifeq ($(JAVA5_SUPPORT), true)
  WONKA_INFO += with Java5 support\;
endif

ifeq ($(JAVA5_SUPPORT), false)
  WONKA_INFO += no Java5 support\;
endif

ifeq ($(JDWP), true)
  WONKA_INFO += with JDWP enabled\;
endif

ifeq ($(JDWP), false)
  WONKA_INFO += no JDWP\;
endif

ifeq ($(USE_LIBFFI), true)
  WONKA_INFO += using libffi to call native code\;
endif

ifeq ($(USE_LIBFFI), false)
  WONKA_INFO += using own hacks to call native code\;
endif

ifeq ($(BYTECODE_VERIFIER), true)
  WONKA_INFO += bytecode verification is enabled\;
endif

ifeq ($(BYTECODE_VERIFIER), false)
  WONKA_INFO += bytecode verification is disabled\;
endif

ifeq ($(NETWORK), none)
  WONKA_INFO += no network\;
endif

ifeq ($(NETWORK), native)
  WONKA_INFO += using the host OS network facilities\;
endif

ifeq ($(SECURITY), java2)
    WONKA_INFO += fine-grained (Java2) security\;
endif
ifeq ($(SECURITY), none)
    WONKA_INFO += no security\;
endif

ifeq ($(FLOATING_POINT), native)
  WONKA_INFO += using native floating-point\;
endif

ifeq ($(FLOATING_POINT), hauser)
  WONKA_INFO += using own floating-point after John Hauser\;
endif

ifeq ($(MATH), native)
  WONKA_INFO += using native math functions\;
endif

ifeq ($(MATH), java)
  WONKA_INFO += using all-java math functions\;
endif

ifeq ($(UNICODE_SUBSETS), 0)
  WONKA_INFO += minimal Unicode support\;
else
  ifeq ($(UNICODE_SUBSETS), 999)
    WONKA_INFO += full Unicode support\;
  else
    WONKA_INFO += support for Unicode subsets $(UNICODE_SUBSETS)\;
  endif
endif

ifeq ($(ENABLE_THREAD_RECYCLING), true)
  WONKA_INFO += with recycling of native threads\;
else
  WONKA_INFO += no recycling of native threads\;
endif

ifeq ($(JAVAX_COMM), true)
    WONKA_INFO += with javax.comm\;
endif

ifeq ($(JAVAX_COMM), false)
    WONKA_INFO += no javax.comm\;
endif

ifneq ($(UPTIME_LIMIT), none)
  WONKA_INFO += will exit automatically after $(UPTIME_LIMIT) seconds\;
endif

