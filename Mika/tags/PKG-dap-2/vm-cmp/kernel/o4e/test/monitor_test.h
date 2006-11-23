#ifndef _monitor_test_
#define _monitor_test_


void monitor_testprogram(void*);
int ERROR;
void prog1(void*);
void prog2(void*);
void prog3(void*);
void prog4(void*);
x_monitor monitor;
x_monitor monitor2;
x_monitor monitor3;
int teller;
cyg_handle_t monitorpool1;
cyg_handle_t monitorpool2;
cyg_mempool_fix mpool1;
cyg_mempool_fix mpool2;
void* mstack[10];	// the pointers to the mem of our stacks of our threads
void* mstruct[10];	// the pointers to the mem of our structs for our threads
void create_mem(void);
void* m_thread; 
void* m_stack; 





#endif // _monitor_test_
