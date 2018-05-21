/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005, 2008, 2009 by Chris Gray, /k/ Embedded Java   *
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

#include <signal.h>
#include <errno.h>
#include <stdlib.h>
#include <fcntl.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/signal.h>
#include <sys/types.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include "hashtable.h"
#include "wonka.h"
#include "ts-mem.h"
#include "oswald.h"
#include "exec.h"
#include "network.h"

typedef struct w_Pid_struct {
  w_int pid;
  w_int fd_in;
  w_int pipe_in;
  w_int fd_out;
  w_int pipe_out;
  w_int fd_err;
  w_int pipe_err;
  volatile w_int retval;
}  w_Pid_struct;

typedef w_Pid_struct* w_pid;

extern char *current_working_dir;
extern char **environ;

extern char *command_line_path;

char* cloneCommandLinePath(void) {
  int len = strlen(command_line_path);
  char* path = allocMem(len+1);
  memcpy(path, command_line_path, len);
  path[len] = 0;
  return path;
}

char *host_getCommandPath() {
  pid_t pid = getpid();
  char buffer[128];
  int result;
  char* path;
  int size = 256;
  int len = snprintf(buffer, 128, "/proc/%d/exe", pid);
  woempa(5,"formatted %d into '%s' (len = %d)\n",len);

  if(len == -1 || len >= 128) {
    return cloneCommandLinePath();
  }

  do {
     path = allocMem(size);
     if(path == NULL) {
       return cloneCommandLinePath();
     }
     result = readlink(buffer, path, size);
     if(result == -1) {
       return cloneCommandLinePath();
     } else if(result >= size) {
       releaseMem(path);
       size *= 2;
     } else {
       path[result] = 0;
       woempa(9,"Found mika binary: '%s'\n",path);
       return path;
     }
  } while (1);
}


/*
** Spawn a child process used to execute 'cmd', using path 'path'. Note that
** currently 'env' is not used (bug???). The child process's stdin, stdout,
** and stderr are redirected to new fd's, which are returned via fd_in, fd_out,
** and fd_err. Returns the pid of the child process, or <0 if fork/exec failed.
*/
w_void* host_exec(char **cmd, char **env, char *path, w_int* retpid) {
  w_int pid;
  w_int socks_in[2];
  w_int socks_out[2];
  w_int socks_err[2];
  w_pid wpid = (w_pid) allocMem(sizeof(w_Pid_struct));
  memset(wpid,-1,sizeof(w_Pid_struct));
  *retpid = -1;

  if(wpid == NULL) {
    return NULL;
  }

  if(pipe(socks_in)) {
    host_close(wpid);
    return NULL;
  }
  wpid->fd_in = socks_in[0];
  wpid->pipe_in = socks_in[1];

  if(pipe(socks_out)) {
    host_close(wpid);
    return NULL;
  }
  wpid->fd_out = socks_out[1];
  wpid->pipe_out = socks_out[0];

  if (pipe(socks_err)) {
    host_close(wpid);
    return NULL;
  }
  wpid->fd_err = socks_err[0];
  wpid->pipe_err = socks_err[1];

  fcntl(wpid->fd_in, F_SETFL, O_NONBLOCK);
  fcntl(wpid->fd_out, F_SETFL, O_NONBLOCK);
  fcntl(wpid->fd_err, F_SETFL, O_NONBLOCK);

  pid = vfork();

  if (pid < 0) {
    perror("mika: Runtime.exec(): vfork");
    host_close(wpid);
    return NULL;
  }
  else if (pid == 0) {
    w_int i;
    /*
    ** Close stdin, stdout and stderr.
    */

    close(0);
    close(1);
    close(2);

    /*
    ** Reassign stdin, stdout and stderr to the socketpairs.
    */

    dup2(socks_out[0], 0);
    dup2(socks_in[1], 1);
    dup2(socks_err[1], 2);

    /*
    ** close all the other file descriptors...
    */
    for(i=3; i < 1024; i++) {
      close(i);
    }

    /*
    ** Change path 
    */

    chdir(current_working_dir);

    if(path) chdir(path);

    /*
    ** Execute program.
    */ 
    if(env) {

      char **newenv = NULL;
      char **env1;
      char **env2;
      char **env3;

      for(env1 = environ; *env1; env1++);
      for(env2 = (char **)env; *env2; env2++);

      newenv = alloca(((int)(env1 - environ + env2 - (char **)env + 2)) * sizeof(char *));
      memset(newenv, 0, ((int)(env1 - environ + env2 - (char **)env + 2)) * sizeof(char *));

      env3 = alloca((int)(env2 - (char **)env + 1) * sizeof(char *));
      memcpy(env3, env, (int)(env2 - (char **)env + 1) * sizeof(char *));
      env = env3;

      env3 = newenv;

      for(env1 = environ; *env1; env1++) {
        *env3 = *env1;
        for(env2 = (char **)env; *env2; env2++, env3++) {
          if((int)*env2 != 0x01) {
            int j = (int)(strchr(*(char **)env1, '=') - *env1);
            int k = (int)(strchr(*(char **)env2, '=') - *env2);
            if(j == k && strncmp(*env1, *env2, j) == 0) {
              *env3 = *env2;
              *(char **)env2 = (char *)0x1;
            }
          }
        }
      }

      for(env2 = (char **)env; *env2; env2++) {
        if(*env2 != (char *)0x1) {
          *env3++ = *env2;
        }
      }

      if (isSet(verbose_flags, VERBOSE_FLAG_EXEC)) {
        w_printf("Execute command: host_exec: child executing\n");
      }

      if(strchr(cmd[0], '/')) {
        execve(cmd[0], (char **)cmd, (char **)newenv);
      }
      else {
        char *envpath = getenv("PATH");
        char *envpath2;
        char *tstpath = NULL;

        if(!envpath) {
          envpath = ":/bin:/usr/bin";
        }

        envpath2 = alloca(strlen(envpath) + 1);
        strcpy(envpath2, envpath);

        while((tstpath = strsep((char **)&envpath2, ":")) != NULL) {
          char *command = alloca(strlen(tstpath) + strlen(cmd[0]) + 5);
          strcpy(command, tstpath);
          strcat(command, "/");
          strcat(command, cmd[0]);
  /*
  * vfork CAN suspend program execution until _exit or execve is called.
  * There is no guarantee however !!! but it can't be harmfull to check the return status ;)
  */
          execve(command, (char **)cmd, (char **)newenv);
        }
      }
    }
    else {
      execvp(cmd[0], (char **)cmd);
    }
    woempa(9,"Execution of child process failed...\n");
    wpid->retval = EXECUTION_ERROR;
    _exit(123);
  }
  wpid->pid = pid;
  *retpid = pid;
  woempa(7, "child process has pid %d and fd = %d,%d,%d...\n",pid,wpid->fd_in,wpid->fd_out,wpid->fd_err);
  if (isSet(verbose_flags, VERBOSE_FLAG_EXEC)) {
    w_printf("Execute command: host_exec: child process has pid %d and fd = %d,%d,%d...\n",pid,wpid->fd_in,wpid->fd_out,wpid->fd_err);
  }

  /*
  * vfork CAN suspend program execution until _exit or execve is called.
  * There is no guarantee however !!! but it can't be harmfull to check the return status ;)
  */

  if(wpid->retval == EXECUTION_ERROR) {
    int result;
    woempa(9, "Execution of child process %d failed...\n",pid);
    if (isSet(verbose_flags, VERBOSE_FLAG_EXEC)) {
      w_printf("Execute command: host_exec: child process %d failed.\n",pid);
    }
    result = waitpid(pid, NULL, 0);
    if(result != pid) {
      woempa(9, "waitpid on child process %d returned %d\n",pid);
    }
    host_close(wpid);
    return NULL;
  }

  return wpid;
}
/**
 * Kill a child process. This is called by Process.destroy().
 */
w_void host_destroy(w_void* vpid) {
  w_pid pid = (w_pid) vpid;

  if(pid && pid->pid != -1) {
    kill(pid->pid, 15);
    pid->pid = -1;
  }
}

w_int host_getretval(w_void* pid) {
  return ((w_pid)pid)->retval;
}

w_void  host_setreturnvalue(w_void* pid, int retval) {
  ((w_pid)pid)->retval = retval;
}

#define ERESTARTSYS 512
w_int host_wait_for_all(w_int* retval) {
  int status;
  int pid;

  pid = waitpid(-1, &status, WNOHANG);

  if (pid == -1) {
    woempa(7, "[GRU] waitpid(-1,&status, 0) returned -1 (%d, %s)\n", errno, strerror(errno)); 
    if (isSet(verbose_flags, VERBOSE_FLAG_EXEC)) {
      w_printf("Execute command: host_wait_for_all: waitpid() returned -1 (%d, %s).\n", errno, strerror(errno));
    }
  }

  if(pid > 0) {
    if(WIFEXITED(status)) {
      *retval = WEXITSTATUS(status);
      if (isSet(verbose_flags, VERBOSE_FLAG_EXEC)) {
        w_printf("Execute command: host_wait_for_all: child return value = %d.\n",*retval);
      }
    } else {
      *retval = EXECUTION_ENDED;
      if (isSet(verbose_flags, VERBOSE_FLAG_EXEC)) {
        w_printf("Execute command: host_wait_for_all: child execution ended, return value lost.\n");
      }
    }
    woempa(9,"[GRU] waitpid(-1,&status, 0) returned %d (status = %d, retval = %d)\n",pid, status, *retval); 
  }
  woempa(1,"[GRU] waitpid(-1,&status, 0) returned %d (status = %d, retval = %d)\n",pid, status, *retval); 

  return pid;
}


// CG HACK
#define x_read read
#define x_write write

w_int host_write(w_void* pid, w_ubyte* bytes, w_int len) {
  w_int fd = ((w_pid)pid)->fd_out;
  w_int result = x_write(fd, bytes, len);
  if(result != len) {
    //this should not happen to often, but still ...
    w_int total = result;
    if(result == -1) {
      if(errno != EAGAIN) {
        return EXECUTION_ERROR;
      }
      total = 0;
    } 
    while(total < len) {
      result = x_write(fd, bytes+total, len-total);
      if(result == -1) {
        if(errno != EAGAIN) {
          return EXECUTION_ERROR;
        }
      } else {
        total += result;
      }
    }
  }
  return 0;
}

w_void host_close(w_void* vpid) {
  w_pid pid = ((w_pid)vpid);
  woempa(6, "freeing resources from pid %d\n",pid->pid);
  woempa(6, "\tfd_in  = %d, pipe_in  = %d\n",pid->fd_in, pid->pipe_in);
  woempa(6, "\tfd_err = %d, pipe_err = %d\n",pid->fd_err, pid->pipe_err);
  woempa(6, "\tfd_out = %d, pipe_out = %d\n",pid->fd_out, pid->pipe_out);

  if(pid->fd_in != -1) {
    close(pid->fd_in);
    close(pid->pipe_in);
  }
  if(pid->fd_err != -1) {
    close(pid->fd_err);
    close(pid->pipe_err);
  }
  if(pid->fd_out != -1) {
    close(pid->fd_out);
    close(pid->pipe_out);
  }
  releaseMem(pid);
}

w_void host_close_in(w_void* vpid) {
  w_pid pid = ((w_pid)vpid);
  if(pid->fd_in != -1) {
    close(pid->fd_in);
    close(pid->pipe_in);
    pid->fd_in = -1;
  }
}

w_void host_close_err(w_void* vpid) {
  w_pid pid = ((w_pid)vpid);
  if(pid->fd_err != -1) {
    close(pid->fd_err);
    close(pid->pipe_err);
    pid->fd_err = -1;
  }
}

w_void host_close_out(w_void* vpid) {
  w_pid pid = ((w_pid)vpid);
  if(pid->fd_out != -1) {
    close(pid->fd_out);
    close(pid->pipe_out);
    pid->fd_out = -1;
  }
}

w_int static host_available(w_int fd) {
  w_int arg=0;
  w_int res = ioctl(fd, FIONREAD, &arg);
  if(res == -1) {
    woempa(9, "Error in Available 'ioctl' failed\n");
    return EXECUTION_ERROR;
  }
  woempa(6, "Available bytes %x\n",arg);
  return arg;
}

w_int host_available_in(w_void* pid){
  return host_available(((w_pid)pid)->fd_in);
}

w_int host_available_err(w_void* pid){
  return host_available(((w_pid)pid)->fd_err);
}

static w_int host_read(w_pid wpid, w_int fd, char* bytes, w_int len) {
  w_int result;
  result = x_read(fd, bytes, (size_t)len);
  while(result == -1 && errno == EAGAIN) {
    if(wpid->retval != -1) {
      return -1;
    }
    x_thread_sleep(x_millis2ticks(200));
    result = x_read(fd, bytes, (size_t)len);
  }
  if(result == 0) {
    woempa(7, "READ: reached EOF stream. returning -1\n");
    return -1;
  }
  if(result == -1) {
    woempa(9, "READ: error while reading from stream %s (%d) (fd = %d)\n",strerror(errno),errno, fd);
    return EXECUTION_ERROR;
  }
  woempa(6, "READ: read %d byte(s). returning %d\n",result, result);
  return result;
}

w_int host_read_in(w_void* pid, char* bytes, w_int len) {
  return host_read(((w_pid)pid), ((w_pid)pid)->fd_in, bytes, len);
}

w_int host_read_err(w_void* pid, char* bytes, w_int len) {
  return host_read(((w_pid)pid), ((w_pid)pid)->fd_err, bytes, len);
}
