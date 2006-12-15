#ifndef _QUEUE_TEST_
#define _QUEUE_TEST_
void queue_test(void*);
cyg_handle_t queue_pool_handle1;
cyg_handle_t queue_pool_handle2;
cyg_mempool_fix queue_pool1;
cyg_mempool_fix queue_pool2;
void* queue_stack[2];	// the pointers to the mem of our stacks of our threads
void* queue_struct[2];	// the pointers to the mem of our structs for our threads
void create_queue_mem(void);
void* q_thread;
void* q_stack;
x_queue queue;
int produced;
int stuurtext[5];
void queue_producer(void* arg);
void queue_consumer(void* arg);
#endif //_QUEUE_TEST_
