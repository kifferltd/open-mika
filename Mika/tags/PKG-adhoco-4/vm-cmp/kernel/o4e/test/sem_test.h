#ifndef _SEM_TEST_
#define _SEM_TEST_
void sem_test(void*);
cyg_handle_t sem_pool_handle1;
cyg_handle_t sem_pool_handle2;
cyg_mempool_fix sem_pool1;
cyg_mempool_fix sem_pool2;
void* sem_stack[2];	// the pointers to the mem of our stacks of our threads
void* sem_struct[2];	// the pointers to the mem of our structs for our threads
void create_sem_mem(void);
void* s_thread;
void* s_stack;
x_sem sem;
int stuurtext[5];
void sem_put_prog(void* arg);
void sem_get_prog(void* arg);
#endif //_SEM_TEST_
