#ifndef _MUTEX_TEST_
#define _MUTEX_TEST_
void mutex_test(void*);
cyg_handle_t mutex_pool_handle1;
cyg_handle_t mutex_pool_handle2;
cyg_mempool_fix mutex_pool1;
cyg_mempool_fix mutex_pool2;
void* mutex_stack[10];	// the pointers to the mem of our stacks of our threads
void* mutex_struct[10];	// the pointers to the mem of our structs for our threads
void create_mutex_mem(void);
void* mu_thread;
void* mu_stack;
x_mutex mutex;
int count;
void mutex_prog(void* arg);
#endif //_MUTEX_TEST_
