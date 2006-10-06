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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

#include "deflate_internals.h"

/* ------------------------------------------------------------------------------------------------------- */
// HELPERS
/* ------------------------------------------------------------------------------------------------------- */

/*
** TODO : Fix errorhandling
**        Check mem allocations
**        Improve speed
*/


/*
** This fucntion gets a new block from the input queue, and frees the previous block
** if one
** If nomoreinput is set, we don't block anymore on the queue
*/
w_void getNewBlock(w_deflate_control l) {
  w_deflate_queueelem qe;
  x_status s;

  // free old partial data
  if (l->par_in != NULL) {
    woempa(1, "-------Freeing.\n");

    if (l->par_in->data != NULL) releaseMem(l->par_in->data);
    releaseMem(l->par_in);
    l->par_in = NULL;
    
    // previous block was a dictionary, so set dictionary to 0
    if (l->dictionary) l->dictionary = 0;
  }
  else
    woempa(1, "-------Not Freeing.\n");

  // dont block if no-more-input
  if (l->nomoreinput) {
    woempa(1, "Try to receive new block NONBLOCKING.\n");
    s = x_queue_receive(l->q_in, (w_void **)&qe, x_no_wait);
    // TODO, moet dit
    l->need_more_input = 0;
  }
  else {
    woempa(1, "Try to receive new block BLOCKING.\n");
    
    // TODO : This is not a very SUPERDELUXE implementation, should actually ask oswald 
    // if thread is waiting on the queue-event
    l->need_more_input = 1;
    s = x_queue_receive(l->q_in, (w_void **)&qe, x_eternal);
    l->need_more_input = 0;
  }

  switch (s) {
    case xs_success:
      if (qe == NULL || l->reset) {
        woempa(7, "Got new BOGUS block.\n");
        // if reset or bogus message return ENDS
        l->par_in = NULL;
        l->offset_in = 0;      
        if (qe != NULL) {
          if (qe->data != NULL) releaseMem(qe->data);
          releaseMem(qe);
        }
      }
      else {
        switch (qe->errnum) {
          case WUNZIP_OK:
            woempa(1, "Got new OK block.\n");
            l->par_in = qe;
            l->offset_in = 0;      
            break;
          case WUNZIP_ENDS:
            woempa(1, "Got new ENDS block.\n");
            l->par_in = NULL;
            l->offset_in = 0;      
            releaseMem(qe);
            break;
          case WUNZIP_ERROR:
            woempa(7, "Got new ERROR block.\n");
            l->par_in = NULL;
            l->offset_in = -1;
            releaseMem(qe);
            break;
        }
      }
      break;
    case xs_no_instance:
      woempa(1, "Got new ENDS block (no_instance).\n");
      l->par_in =  NULL;
      l->offset_in = 0;      
      break;
    case xs_bad_context:
    case xs_deleted:
    case xs_bad_element:
    default:
      woempa(9, "AIAIAIAI, unable to receive blocks.\n");
      abort();
      break;    
  }  
}

/*
** The following call can only be done on a byte aligned stream !
*/
w_byte readLiteralByte(w_deflate_control bs) {
  w_byte dd;
  
  if (bs->offset_in == 0 || bs->offset_in >= bs->par_in->size) {
    getNewBlock(bs);
    
    // we had ends, just return 0
    if (bs->par_in == NULL) return 0;
    
    bs->offset_in = 0;
  }

  dd = bs->par_in->data[bs->offset_in];
  bs->offset_in += 1;

  bs->processed_size +=1;
    
  return dd;
}

w_void readByteAlign(w_deflate_control bs) {
  bs->i_bits = 0x01;
}

/*
** Sends an error message on the output queue
*/
w_void errorFlush(w_deflate_control bs) {
  x_status s;
  w_deflate_queueelem qe = allocMem(sizeof(w_Deflate_QueueElem));
  if (!qe) {
    wabort(ABORT_WONKA, "Unable to allocate qe\n");
  }

  woempa(1, "-=-- Flush an error.\n");

  qe->errnum = WUNZIP_ERROR;
  qe->data = NULL;
  qe->size = 0;

  s = x_queue_send(bs->q_out, qe, x_millis2ticks(5000));			// wait for a pretty long time, but not for ever, because space should come available

  switch (s) {
    case xs_success:
      break;
    case xs_no_instance:
    case xs_bad_context:
    case xs_deleted:
    case xs_bad_element:
    default:
      woempa(9, "AIAIAIAIAI, could not send buffer\n");
      releaseMem(qe);
      abort();
      break;    
  }

  bs->offset_bek_out = 0;
  bs->size_bek_out = 0;
}

/*
** Send all available data we still have and then send an ENDS message
*/
w_void bekkenFlush(w_deflate_control bs) {
  x_status s;
  w_deflate_queueelem qe = allocMem(sizeof(w_Deflate_QueueElem));
  if (!qe) {
    wabort(ABORT_WONKA, "Unable to allocate qe\n");
  }

  woempa(1, "-=-- Flush bekken.\n");

  qe->data = allocMem((unsigned)bs->size_bek_out);
  if (!qe->data) {
    wabort(ABORT_WONKA, "Unable to allocate qe->data\n");
  }
  qe->size = bs->size_bek_out;
  qe->errnum = WUNZIP_OK;

  woempa(1, "Block size %i and offset %i\n", bs->size_bek_out, bs->offset_bek_out);

  if (bs->offset_bek_out - bs->size_bek_out >= 0) {
    w_memcpy(qe->data, bs->output_bekken + (bs->offset_bek_out - bs->size_bek_out), (unsigned)bs->size_bek_out);
  } else {
    //  --------------------------
    //  |      |            | |  |
    //  --------------------------
    //  0      offset       ^ ^  33*1024
    //                      | |
    //                      | 1024+offset
    //               33*1024+offset-size
    //
    // beginnen bij 33*1024 + (offset - size)
    // eindigen bij 33*1024
    // dus lengte size - offset
    w_memcpy(qe->data, bs->output_bekken + 33 * 1024 + bs->offset_bek_out - bs->size_bek_out, (unsigned)(bs->size_bek_out - bs->offset_bek_out));
    w_memcpy(qe->data + (bs->size_bek_out - bs->offset_bek_out), bs->output_bekken, (unsigned)bs->offset_bek_out);
  }
  s = x_queue_send(bs->q_out, qe, x_millis2ticks(5000000));			// wait for a pretty long time, but not for ever, because space should come available

  switch (s) {
    case xs_success:
      break;
    case xs_no_instance:
    case xs_bad_context:
    case xs_deleted:
    case xs_bad_element:
    default:
      woempa(9, "AIAIAIAIAI, could not send buffer\n");
      releaseMem(qe->data);
      releaseMem(qe);
      abort();
      break;    
  }

  qe = allocMem(sizeof(w_Deflate_QueueElem));
  if (!qe) {
    wabort(ABORT_WONKA, "Unable to allocate qe\n");
  }
  qe->errnum = WUNZIP_ENDS;
  qe->data = NULL;
  qe->size = 0;
  s = x_queue_send(bs->q_out, qe, x_millis2ticks(5000000));			// wait for a pretty long time, but not for ever, because space should come available

  switch (s) {
    case xs_success:
      break;
    case xs_no_instance:
    case xs_bad_context:
    case xs_deleted:
    case xs_bad_element:
    default:
      woempa(9, "AIAIAIAIAI, could not send buffer\n");
      releaseMem(qe);
      abort();
      break;    
  }

  bs->offset_bek_out = 0;
  bs->size_bek_out = 0;
}

/*
** This is called when the output block is full, and sends it to the output queue
*/
w_void bekkenSendBlock(w_deflate_control bs) {
  x_status s;
  w_deflate_queueelem qe = allocMem(sizeof(w_Deflate_QueueElem));
  if (!qe) {
    wabort(ABORT_WONKA, "Unable to allocate qe\n");
  }

  woempa(1, "-=-- Flush send block.\n");

  qe->data = allocMem(1024);
  if (!qe->data) {
    wabort(ABORT_WONKA, "Unable to allocate qe->data\n");
  }
  qe->size = 1024;
  qe->errnum = WUNZIP_OK;

  woempa(1, "Block size %i and offset %i\n", bs->size_bek_out, bs->offset_bek_out);
  if (bs->offset_bek_out + 1024 <= 33*1024) {
    w_memcpy(qe->data, bs->output_bekken + bs->offset_bek_out, 1024);
  } 
  else {
    w_memcpy(qe->data, bs->output_bekken + bs->offset_bek_out, (unsigned)(33 * 1024 - bs->offset_bek_out));
    w_memcpy(qe->data + (33 * 1024 - bs->offset_bek_out), bs->output_bekken, (unsigned)(bs->offset_bek_out - 32 * 1024));
  }

  s = x_queue_send(bs->q_out, qe, x_millis2ticks(5000000));			// wait for a pretty long time, but not for ever, because space should come available

  switch (s) {
    case xs_success:
      break;
    case xs_no_instance:
    case xs_bad_context:
    case xs_deleted:
    case xs_bad_element:
    default:
      woempa(9, "AIAIAIAIAI, could not send buffer\n");
      releaseMem(qe->data);
      releaseMem(qe);
      abort();
      break;    
  }
  bs->size_bek_out -= 1024;
}

/*
** Byte-aligns the output stream, and thus flushes the remaining bits, padding with zero's
**
*/
w_void writeByteAlign(w_deflate_control bs) {
  if (bs->o_mask != 0x1) {
    writeLiteralByte(bs, bs->o_bits);
    
    bs->o_bits = 0;
    bs->o_mask = 0x1;
  }
}

/*
** Used to flush the queue
*/
w_void unzip_freeQueue(void *ee) {
  w_deflate_queueelem e = (w_deflate_queueelem)ee;

  if (e != NULL) {
    if (e->data != NULL) releaseMem(e->data);
    releaseMem(e);
  }
}

