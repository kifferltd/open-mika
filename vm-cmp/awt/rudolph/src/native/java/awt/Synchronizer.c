/**************************************************************************
* Copyright (c) 2012 by Chris Gray, Kiffer Ltd.  All rights reserved.     *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Kiffer Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include "oswald.h"
#include "rudolph.h"
#include "mika_threads.h"

x_Monitor Tree_lock;
x_monitor tree_lock;

void Synchronizer_clinit(JNIEnv *env, jobject classSynchronizer) {
    tree_lock = &Tree_lock;
    x_status status = x_monitor_create(tree_lock);
}

void Synchronizer_staticLockAWT(JNIEnv *env, jobject classSynchronizer) {
    x_status status = x_monitor_eternal(tree_lock);
}
    
void Synchronizer_staticUnlockAWT(JNIEnv *env, jobject classSynchronizer) {
    x_status status = x_monitor_exit(tree_lock);
}
    

