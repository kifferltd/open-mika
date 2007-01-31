#ifndef _THREAD_TEST_
#define _THREAD_TEST_
void WaitForAll();
void sleepprog(void*);
void suspendprog(void*);
void stupidprog(void*);
void testprogram(void*);
cyg_handle_t mempool1;
cyg_handle_t mempool2;
cyg_mempool_fix pool1;
cyg_mempool_fix pool2;
void* stack_mem[20];	// the pointers to the mem of our stacks of our threads
void* struct_mem[20];	// the pointers to the mem of our structs for our threads
void create_mempools(void);
void alloc_mempools(void);
void free_mempools();
void* base1; 
void* base2; 
#endif //_THREAD_TEST_