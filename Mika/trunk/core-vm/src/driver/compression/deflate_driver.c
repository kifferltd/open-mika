/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2008 by Chris Gray, /k/ Embedded Java   *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "oswald.h"
#include "deflate_internals.h"

#define INF_WOEMP_LEV_1  7

static w_void unzip_initDevice(w_device device) {
  x_status status;
  w_deflate_control unzip;

  // do not initialise the mother device
  if(device->familyMember != 0 && device->familyMember != 20) {
    device->control = allocMem(sizeof(w_Deflate_Control));
    if(!device->control) {
      wprintf("Unable to allocate memory for deflate controller\n");
      return;
    }
    
    unzip = (w_deflate_control)device->control;
  
    unzip->mutx = allocMem(sizeof(x_Mutex));
    if(!unzip->mutx) {
      wprintf("Unable to allocate memory for deflater mutex\n");
      releaseMem(device->control);
      device->control = NULL;
      return;
    }
    
    unzip->output_bekken = allocMem(32 * 1024 + 1024);
    if(!unzip->output_bekken) {
      wprintf("Unable to allocate memory for deflater output buffer\n");
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }
    
    unzip->input_bekken = allocMem(32 * 1024 + 512);
    if(!unzip->input_bekken) {
      wprintf("Unable to allocate memory for deflater input buffer\n");
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }

    unzip->ready = allocMem(sizeof(x_Monitor));
    if(!unzip->ready) {
      wprintf("Unable to allocate memory for deflater monitor\n");
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }

    unzip->qmem_in = allocMem(4 * 512);
    if(!unzip->qmem_in) {
      wprintf("Unable to allocate memory for deflater input queue storage\n");
      releaseMem(unzip->ready);
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }
    
    unzip->qmem_out = allocMem(4 * 512);
    if(!unzip->qmem_out) {
      wprintf("Unable to allocate memory for deflater output queue storage\n");
      releaseMem(unzip->qmem_in);
      releaseMem(unzip->ready);
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }
  
    unzip->q_in = allocMem(sizeof(x_Queue));
    if(!unzip->q_in) {
      wprintf("Unable to allocate memory for deflater input queue\n");
      releaseMem(unzip->qmem_out);
      releaseMem(unzip->qmem_in);
      releaseMem(unzip->ready);
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }
  
    unzip->q_out = allocMem(sizeof(x_Queue));
    if(!unzip->q_out) {
      wprintf("Unable to allocate memory for deflater output queue\n");
      releaseMem(unzip->q_in);
      releaseMem(unzip->qmem_out);
      releaseMem(unzip->qmem_in);
      releaseMem(unzip->ready);
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }

    unzip->thread = allocClearedMem(sizeof(x_Thread));
    if(!unzip->thread) {
      wprintf("Unable to allocate memory for deflater thread\n");
      releaseMem(unzip->q_out);
      releaseMem(unzip->q_in);
      releaseMem(unzip->qmem_out);
      releaseMem(unzip->qmem_in);
      releaseMem(unzip->ready);
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }

#ifdef O4P
    unzip->stack = NULL;
#else
    unzip->stack = allocClearedMem_with_retries(driver_stack_size, 3);
    if(!unzip->stack) {
      wprintf("Unable to allocate memory for deflater stack\n");
      releaseMem(unzip->thread);
      releaseMem(unzip->q_out);
      releaseMem(unzip->q_in);
      releaseMem(unzip->qmem_out);
      releaseMem(unzip->qmem_in);
      releaseMem(unzip->ready);
      releaseMem(unzip->input_bekken);
      releaseMem(unzip->output_bekken);
      releaseMem(unzip->mutx);
      releaseMem(device->control);
      device->control = NULL;
      return;
    }
#endif

    x_mutex_create(unzip->mutx);

    unzip->offset_in = unzip->offset_out = unzip->offset_bek_out = 0;
    unzip->lookahead_bek_in = unzip->offset_bek_in = unzip->size_bek_out = 0;
    unzip->i_bits = 0x01;
    unzip->o_bits = 0;
    unzip->o_mask = 0x1;
    unzip->par_out = unzip->par_in = NULL;

    unzip->compression_level = 9;
    unzip->no_auto = 0;
    unzip->need_more_input = 1;             // we are starting up, so we need input
    unzip->processed_size = 0;

    unzip->reset = 0;
    unzip->stop = 0;
    unzip->state = COMPRESSION_THREAD_UNSTARTED;

    x_monitor_create(unzip->ready);

    unzip->dictionary = 0;

    // for the zipper
    unzip->nomoreinput = 0;

    x_queue_create(unzip->q_in, unzip->qmem_in, 512);
    woempa(1, "Created q_in at %p\n", unzip->q_in);
    x_queue_create(unzip->q_out, unzip->qmem_out, 512);
    woempa(1, "Created q_out at %p\n", unzip->q_out);


    x_monitor_eternal(unzip->ready);
    // Start thread with the current prio of the current thread, this to provide nice round-robinning
    if(device->familyMember < 200) {
      status = x_thread_create(unzip->thread, zzzinflate, unzip, unzip->stack, driver_stack_size, 20, TF_START);
    }
    else {
      status = x_thread_create(unzip->thread, zzzdeflate, unzip, unzip->stack, driver_stack_size, 20, TF_START);
    }
    if (status != xs_success) {
      wabort(ABORT_WONKA, "Hooooola, unable to start deflating thread: status = '%s'\n", x_status2char(status));
    }
    x_monitor_wait(unzip->ready, x_eternal);
    x_monitor_exit(unzip->ready);
  }
}

static w_void unzip_termDevice(w_device device) {
  x_status status, s;
  w_deflate_control unzip = (w_deflate_control)device->control;
  void *join_result;

  if(!device->control) {
    return;
  }

  // do not destuct the mother device
  if(device->familyMember != 0 && device->familyMember != 20) {

    // trying to enter monitor
    woempa(INF_WOEMP_LEV_1, "Entering\n");
    s = x_monitor_eternal(unzip->ready);
    if (s != xs_success) {
      wabort(ABORT_WONKA, "entering unzip->ready, error %d in monitor\n", s);
    }

    unzip->reset = 1;
    unzip->stop = 1;
    unzip->nomoreinput = 1;

    woempa(INF_WOEMP_LEV_1, "Sending on q_in %p\n", unzip->q_in);
    // send a bogus message to make sure blocking thread gets revived, but 
    // don't try to hard, it could be possible that the queue is full
    s = x_queue_send(unzip->q_in, NULL, x_millis2ticks(500));
    switch (s) {
      case xs_success:
        break;
      default:
        wabort(ABORT_WONKA, "Couldn't send bogus message, error %d in monitor\n", s);
    }

    woempa(INF_WOEMP_LEV_1, "Notifying\n");
    // notify the thread we changed something
    x_monitor_notify_all(unzip->ready);

    woempa(INF_WOEMP_LEV_1, "Waiting\n");
    // wait till thread notifies us
    while (unzip->state == COMPRESSION_THREAD_RUNNING) {
      s = x_monitor_wait(unzip->ready, COMPRESSION_WAIT_TICKS);
      if (s == xs_interrupted) {
        s = x_monitor_eternal(unzip->ready);
        if (s != xs_success) {
           wabort(ABORT_WONKA, "re-entering unzip->ready, error %d in monitor\n", s);
        }
      }
      else if (s != xs_success && s != xs_no_instance) {
        wabort(ABORT_WONKA, "waiting for notify, error %d in monitor\n", s);
      }
    }
    x_monitor_exit(unzip->ready);
    woempa(INF_WOEMP_LEV_1, "Exit\n");

    status = x_thread_join(unzip->thread, &join_result, 100);
    if (status != xs_success /* && status != xs_no_instance */) {
      wabort(ABORT_WONKA, "Hooooola, unable to join deflating thread: status = '%s'\n", x_status2char(status));
    }

    status = x_thread_delete(unzip->thread);
    if (status != xs_success) {
      wabort(ABORT_WONKA, "Hooooola, unable to delete deflating thread: status = '%s'\n", x_status2char(status));
    }
    if (unzip->stack) {
      releaseMem(unzip->stack);
    }
    releaseMem(unzip->thread);

    woempa(1, "Deleting q_in at %p\n", unzip->q_in);
    x_queue_delete(unzip->q_in);
    releaseMem(unzip->qmem_in);
    releaseMem(unzip->q_in);

    woempa(1, "Deleting q_out at %p\n", unzip->q_out);
    x_queue_delete(unzip->q_out);
    releaseMem(unzip->qmem_out);
    releaseMem(unzip->q_out);

    x_mutex_delete(unzip->mutx);
    releaseMem(unzip->mutx);

    releaseMem(unzip->output_bekken);
    releaseMem(unzip->input_bekken);

    x_monitor_delete(unzip->ready);
    releaseMem(unzip->ready);

    releaseMem(unzip);
  }
}

static w_ubyte _devnr1;
static w_ubyte _devnr2;

static w_device unzip_open(w_device device, w_driver_perm mode) {
  char name1[] = "unzip__";
  char name2[] = "zip__";
  w_device dev = NULL;

  // TODO : now devices are just increased, but if for example the first device is never closed, we get an
  // error when we wrap around, we should just look for a free device number
  
  // only mother thread will generate new devices on opening
  if(device->familyMember == 0){
    int loop = 0;
    while(dev == NULL) {
      // maximum 32 unzip devices present
      //WAS:_devnr1 = _devnr1 & 31;
      ++_devnr1;
      if (_devnr1 > 31) {
        _devnr1 = 1;
      }
 
      name1[5] = (_devnr1 / 10) + '0';
      name1[6] = (_devnr1 % 10) + '0';
  
      dev = registerDevice(name1, "zip", 100 + _devnr1, wdt_byte_serial);

      //WAS:_devnr1 += 1;

      if (dev != NULL) {
        device->driver->usage += 1;
        dev->usage = 1;
      }
      if(_devnr1 == 31 && loop++ >= 2) {
        x_thread_sleep(5);
      } 
    }
    return dev;
  }
  else if(device->familyMember == 20){
    while(dev == NULL) {
      // maximum 32 zip devices present
      //WAS:_devnr2 = _devnr2 & 31;
      ++_devnr2;
      if (_devnr2 > 31) {
        _devnr2 = 1;
      }
  
      name2[3] = (_devnr2 / 10) + '0';
      name2[4] = (_devnr2 % 10) + '0';
  
      dev = registerDevice(name2, "zip", 200 + _devnr2, wdt_byte_serial);

      //WAS:_devnr2 += 1;

      if (dev != NULL) {
        device->driver->usage += 1;
        dev->usage = 1;
      }
    }
  }
  else {
    dev = device;
  }

  return dev->control ? dev : NULL;
}

static w_void unzip_close(w_device device) {
  device->usage -= 1;
  device->driver->usage -= 1;

  deregisterDevice(device->name);
}

static w_driver_status unzip_write(w_device device, w_ubyte *bytes, w_int length, w_int *lwritn, x_sleep timeout) {
  w_deflate_control l = (w_deflate_control)device->control;
  x_status s;
  w_deflate_queueelem qe = allocMem(sizeof(w_Deflate_QueueElem));

  if(!device->control) {
    wprintf("Null deflate control\n");
    return wds_internal_error;
  }

  if (!qe) {
    wprintf("Unable to allocate queue element\n");
    return wds_internal_error;
  }

  woempa(INF_WOEMP_LEV_1, "writing block to device, size = %i.\n", length);

  qe->data = allocMem((unsigned)length);
  if (!qe->data) {
    wprintf("Unable to allocate queue element data\n");
    releaseMem(qe);
    return wds_internal_error;
  }

  w_memcpy(qe->data, bytes, (unsigned)length);
  qe->size = length;
  qe->errnum = WUNZIP_OK;

  woempa(7, "Sending on q_in at %p\n", l->q_in);
  s = x_queue_send(l->q_in, qe, timeout);
// this is unsafe, but we might need something like this (if the other thread 
// is waiting on the queue, already setting need_more_input = 0)
//  l->need_more_input = 0;
  woempa(7, "Result = %d\n", s);
  switch (s) {
    case xs_success:
      *lwritn = length;
      return wds_success;
    case xs_no_instance:
      *lwritn = 0;
      releaseMem(qe->data);
      releaseMem(qe);
      return wds_no_instance;    
    default:
      wprintf("Unable to send queue element\n");
      *lwritn = 0;
      releaseMem(qe->data);
      releaseMem(qe);
      return wds_internal_error;    
  }
}

static w_driver_status unzip_read(w_device device, w_ubyte *bytes, w_int length, w_int *lread, x_sleep timeout) {
  w_deflate_control l = (w_deflate_control)device->control;
  w_deflate_queueelem qe;
  x_status s;
  w_int offset;

  if(!device->control) {
    wprintf("Null deflate control\n");
    return wds_internal_error;
  }

  // Get lock
  switch (x_mutex_lock(l->mutx, timeout)) {
    case xs_success:
      break;
    default:
      return wds_internal_error;
  }

  woempa(INF_WOEMP_LEV_1, "trying to read a block from device.\n");

  // TODO : decrease timeout if we waited in the x_mutex_lock

  // Here we try to return as much as possible of the requested size, if necesarry reading
  // multiple blocks from the queue
  *lread = 0;
  offset = 0;
  while (1) {
    if (l->par_out != NULL) {
      // the previous time we had an ENDS and data, so now return the ENDS
      if (l->offset_out < 0) {

        l->par_out = NULL;
        l->offset_out = 0;

        woempa(INF_WOEMP_LEV_1, "returning ENDS\n");

        x_mutex_unlock(l->mutx);
        return wds_data_exhausted;    
      }

      // we already have what we need, just return
      else if (offset == length){

        woempa(INF_WOEMP_LEV_1, "returning full block\n");

        x_mutex_unlock(l->mutx);
        return wds_success;
      }
      
      // we have partial data, copy it
      else if ((l->par_out->size - l->offset_out) <= (length - offset)) {
        // more data requested than we have, give everything
        w_memcpy(bytes + offset, l->par_out->data + l->offset_out, (unsigned)(l->par_out->size - l->offset_out));

        *lread += l->par_out->size - l->offset_out;
        offset += l->par_out->size - l->offset_out;

        if (l->par_out->data != NULL) releaseMem(l->par_out->data);
        releaseMem(l->par_out);
        l->par_out = NULL;
        l->offset_out = 0;
      }
      else {
        // give what they ask 
        w_memcpy(bytes + offset, l->par_out->data + l->offset_out, (unsigned)(length - offset));
        l->offset_out += length - offset;
        *lread += length - offset;
        offset += length - offset;

        woempa(INF_WOEMP_LEV_1, "returning block. size %i\n", offset);

        // buffer is now full, so move out
        x_mutex_unlock(l->mutx);
        return wds_success;
      }
    }
    else {
      // no data, get some
      s = x_queue_receive(l->q_out, (w_void **)&qe, timeout);

      // TODO : decrease timeout if we had to wait

      switch (s) {
        case xs_success:
          if (qe == NULL) {
            *lread = 0;

            woempa(7, "AIAIAI, not good, allocation failure somewhere.\n");
            wprintf("AIAIAI, not good, allocation failure somewhere.\n");

            x_mutex_unlock(l->mutx);
	    wprintf("read null queue element\n");
            return wds_internal_error;
          } 
          else {
            switch (qe->errnum) {
              case WUNZIP_OK:
                woempa(INF_WOEMP_LEV_1, "WUNZIP_OK\n");
                // OK, received new block
                l->par_out = qe;
                l->offset_out = 0;
                break;
              case WUNZIP_ENDS:
                woempa(INF_WOEMP_LEV_1, "WUNZIP_ENDS\n");
                // aha, end of stream marker
                // if we had data, return it and remember ENDS
                // else return ENDS

                if (offset > 0) {
                  // remember it
                  l->par_out = (w_void *)(-1);
                  l->offset_out = -1;

                  woempa(INF_WOEMP_LEV_1, "returning block and remembring ENDS. size %i\n", offset);

                  releaseMem(qe);
                  x_mutex_unlock(l->mutx);
                  return wds_success;
                }
                else {
                  l->par_out = NULL;
                  l->offset_out = 0;

                  woempa(INF_WOEMP_LEV_1, "returning ENDS\n");

                  releaseMem(qe);
                  x_mutex_unlock(l->mutx);
                  return wds_data_exhausted;    
                }
              case WUNZIP_ERROR:
              default:
                woempa(INF_WOEMP_LEV_1, "WUNZIP_ERROR\n");
                // AIAI, an error occured, throw away all data and return error
                
                *lread = 0;

                releaseMem(qe);
                // Release lock
                x_mutex_unlock(l->mutx);
	        wprintf("Unzipper returned error code %d\n", qe->errnum);
                return wds_internal_error;
            }
          }
          break;
        case xs_no_instance:
          // no data received during given timeout
          // if we had data, return it, else return lread 0 and wds_no_instance

          woempa(INF_WOEMP_LEV_1, "queue : no_instance\n");

          if (offset > 0) {

            woempa(INF_WOEMP_LEV_1, "returning block. size %i\n", offset);

            x_mutex_unlock(l->mutx);
            return wds_success;
          }
          else {

            woempa(INF_WOEMP_LEV_1, "returning no_instance\n");

            x_mutex_unlock(l->mutx);
            return wds_no_instance;    
          }
        default:
          // AIAI, an error occured, throw away all data and return error

          woempa(INF_WOEMP_LEV_1, "queue : error\n");
          wprintf("queue : error\n");
          
          *lread = 0;

          // Release lock (no error checking necessary)
          x_mutex_unlock(l->mutx);
	  wprintf("Error receiving from queue\n");
          return wds_internal_error;    
      }  
    }
  }

  // Release lock (no error checking necessary) -- Redundant code, only for safety
  x_mutex_unlock(l->mutx);
	  wprintf("Should not get here\n");
  return wds_internal_error;    
}
 
static w_driver_status unzip_seek(w_device device, int posn, x_sleep timeout) {
  return wds_not_implemented;
}

static w_driver_status unzip_set(w_device device, w_word command, w_word param, x_sleep timeout) {
  x_status s;
  w_deflate_control zip = (w_deflate_control)device->control;
  w_deflate_queueelem qe;

  if(!device->control) {
	  wprintf("Null deflate control\n");
    woempa(9, "Device %p has no control\n", device);
    return wds_internal_error;
  }

  switch (command) {
    case wdi_end_of_input:
      woempa(7, "wdi_end_of_input, family member = %d\n", device->familyMember);
      if (device->familyMember >= 200) {
        woempa(7, "zip = %p\n", zip);
        zip->nomoreinput = 1;

        // send ENDS in case the thread is waiting on the queue
        // thereby the devices are snakeable (bite your tail)

        qe = allocMem(sizeof(w_Deflate_QueueElem));
        woempa(7, "qe = %p\n", qe);
        if (!qe) {
          woempa(9, "Unable to allocate qe\n");
	  wprintf("Unable to allocate queue element\n");
          return wds_internal_error;
        }
        
        qe->data = NULL;
        qe->size = 0;
        qe->errnum = WUNZIP_ENDS;

        woempa(7, "Sending on zip->q_in = %p, timeout = %d\n", zip->q_in, timeout);
        s = x_queue_send(zip->q_in, qe, timeout);
        woempa(7, "s = %p\n", s);
        switch (s) {
          case xs_success:
            return wds_success;
          case xs_no_instance:
            return wds_no_instance;    
          default:
	  wprintf("Unable to send queue element\n");
            return wds_internal_error;    
        }
      }
      else return wds_not_implemented;

    case wdi_set_compression_level:
      woempa(7, "wdi_set_compression_level\n");
      if (param > 9) return wds_illegal_value;
      zip->compression_level = param;
      return wds_success;

    case wdi_set_no_auto:
      woempa(7, "wdi_set_no_auto\n");
      if (param > 1) return wds_illegal_value;
      zip->no_auto = param;
      return wds_success;

    case wdi_reset:
      woempa(7, "wdi_reset\n");
      // here same as in termDevice, only don't stop
      woempa(INF_WOEMP_LEV_1, "Entering\n");
      s = x_monitor_eternal(zip->ready);
      if (s == xs_success) {
        zip->reset = 1;

        zip->nomoreinput = 1;

        woempa(INF_WOEMP_LEV_1, "Sending on q_in at %p\n", zip->q_in);
        s = x_queue_send(zip->q_in, NULL, timeout);
        switch (s) {
          case xs_success:
            break;
          default:
            wprintf("Unable to send queue element\n");
            return wds_internal_error;
        }

        woempa(INF_WOEMP_LEV_1, "Notifying\n");
        x_monitor_notify_all(zip->ready);

        woempa(INF_WOEMP_LEV_1, "Waiting\n");

        /*
        ** If the the deflater thread is running, we give it a chance
        ** to process the reset and notify us that it has done so.
        ** TODO: hox can we know for sure that the reset has completed?
        */
        if (zip->state == COMPRESSION_THREAD_RUNNING) {
          s = x_monitor_wait(zip->ready, COMPRESSION_WAIT_TICKS);
          if (s == xs_interrupted) {
            wprintf("x_monitor_wait returned xs_interrupted\n");
            return wds_internal_error;
          }
          else if (s != xs_success && s != xs_no_instance) {
            woempa(INF_WOEMP_LEV_1, "Exit ERROR\n");
            x_monitor_exit(zip->ready);
            wprintf("x_monitor_wait returned error code %d\n", s);
            return wds_internal_error;
          }
        }
        woempa(INF_WOEMP_LEV_1, "Exit OK\n");
        x_monitor_exit(zip->ready);
        return wds_success;
      }
      else {
        wprintf("Unable to enter zip->ready\n");
        return wds_internal_error;
      }

    case wdi_send_dictionary:
      woempa(9, "This does NOT work, yet\n");
      wprintf("This does NOT work, yet\n");
      return wds_internal_error;
      
      /*
      ** What this should do.
      **
      ** deflater : just read the dictionary like all the rest of the incomming data, 
      **            but don't write the compressed data out.
      ** inflater : at the beginning of the inflater loop, detect after the first readSingleBit 
      **            if we need to handle a dictionary. If so, copy it to the sliding window, and 
      **            just proceed like normal.
      */
//      zip->dictionary = 1;
//      return wds_success;

    default:
      woempa(7, "???\n");
      return wds_no_such_command;
  }
}
 
static w_driver_status unzip_query(w_device device, w_word query, w_word *reply, x_sleep timeout) {
  w_deflate_control zip = (w_deflate_control)device->control;

  if(!device->control) {
    wprintf("No deflate control block!\n");
    return wds_internal_error;
  }

  switch (query) {
    case wdi_get_need_more_input:
      // TODO : This is not a very SUPERDELUXE implementation, should actually ask oswald 
      // if thread is waiting on the queue-event
      *reply = zip->need_more_input;
      woempa(INF_WOEMP_LEV_1, "need_more_input %i\n", *reply);
      return wds_success;
    
    case wdi_get_processed_size:
      *reply = zip->processed_size;
      woempa(INF_WOEMP_LEV_1, "processed_size %i\n", *reply);
      return wds_success;

    default:
      return wds_no_such_command;
  }
}
 
/* ------------------------------------------------------------------------- */

w_Driver_ByteSerial deflate_driver = {
  "deflate-driver-v0.2",
  "zip",
  0,
  NULL,
  unzip_initDevice,
  unzip_termDevice,
  unzip_open,
  unzip_close,
  unzip_read,
  unzip_write,
  unzip_seek,
  unzip_set,
  unzip_query
};
