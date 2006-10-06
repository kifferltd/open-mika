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



#include "wonka.h"
#include "ts-mem.h"
#include "threads.h"
#include "oswald.h"

#include "vfs.h"
#include "vfs_errno.h"
#include "vfs_fcntl.h"

#include <stdlib.h>

// #include <e2fs_prototypes.h>      /* Shouldn't be here -> need to fix vfs_opendir */

/*   W = write     D = devices     S = symlinks     ~ = plus minus     C = context
   
   W   int link(const char *oldpath, const char *newpath);     > Only needed to make hard links
   
   W   int rename(const char *oldpath, const char *newpath);
       
   ?   int fcntl(int fd, int cmd);
   ?   int fcntl(int fd, int cmd, long arg);
   ?   int fcntl(int fd, int cmd, struct flock * lock);
       
   S   int symlink(const char *oldpath, const char *newpath);
   S   int readlink(const char *path, char *buf, size_t bufsiz);
   S   int lstat(const char *file_name, struct stat *buf);
       
       FILE *freopen (const char *path, const char *mode, FILE *stream);

       int fileno( FILE *stream);

       int getdents(unsigned int fd, struct dirent *dirp, unsigned int count);

   ~   int scandir(const char *dir, struct dirent ***namelist,
              int (*select)(const struct dirent *),
              int (*compar)(const struct dirent **, const struct dirent **));

       int ungetc(int c, FILE *stream);

// W   void setbuf(FILE *stream, char *buf);
// W   void setbuffer(FILE *stream, char *buf, size_tsize);
// W   void setlinebuf(FILE *stream);
// W   int setvbuf(FILE *stream, char *buf, int mode , size_t size);
 > No buffers yet, so no use for these functions...
 
 */

/* --------------------------------------------------------------------------------------------------------
 *
 * Here are the highlevel functions, which mimic the normal C functions
 * 
 * --------------------------------------------------------------------------------------------------------*/

int vfs_open(const w_ubyte *filename, const w_word flags, const w_word mode) {

  vfs_dir_entry dir_entry = NULL;
  vfs_dir_entry dir;
  vfs_inode     inode;
  vfs_file      file;
  int           result = 0;
  w_ubyte       *name, *path;

  vfs_lock();

  path = allocMem((w_size)(strlen(filename) + 1));
  strcpy(path, filename);
  
  name = strrchr(path, '/');

  if(name != NULL) {
    *name = '\0'; 
    name++;
  } 

  woempa(4, "%s in %s\n", name, path);

  vfs_set_errno(0);

  if((vfs_lookup_fullpath(dir_entry_list, filename) != NULL) && ((flags & (VFS_O_CREAT & VFS_O_EXCL)) != 0)) {  
    vfs_set_errno(EEXIST);                                     /* Check if this name isn't already in use */
    result = -1;                                               /* While VFS_O_CREAT and VFS_O_EXCL are used */
  }
  
  if((result == 0) && ((dir = vfs_lookup_fullpath(dir_entry_list, path)) == NULL)) {
    vfs_set_errno(ENOENT);                                     /* Check if the path exists */
    result = -1;
  }

  if((result == 0) && !(VFS_S_ISDIR(dir->inode->flags))) {         /* Check if path is a directory */
    vfs_set_errno(ENOTDIR);
    result = -1;
  }

  if((result == 0) && ((dir->inode->sb->flags & VFS_MOUNT_RW) == 0) && ((flags & VFS_O_WRONLY) != 0)) {
    vfs_set_errno(EROFS);                                      /* Check if fs is read/write while file is */
    result = -1;                                               /* being opened for writing */
  }

  if((result == 0) && ((dir->inode->flags & VFS_FF_W) == 0) && ((flags & (VFS_O_CREAT | VFS_O_WRONLY | VFS_O_RDWR)) != 0)) {
    vfs_set_errno(EACCES);                                     /* Check if directory is writeable while */
    result = -1;                                               /* file is being opened for writing */
  }

  if((result == 0) && ((dir->inode->flags & VFS_FF_R) == 0) && ((flags & (VFS_O_RDONLY | VFS_O_RDWR)) != 0)) {
    vfs_set_errno(EACCES);                                     /* Check if directory is readable while */
    result = -1;                                               /* file is being opened for reading */
  }

  if(result == 0) {
    dir_entry = vfs_lookup_fullpath(dir_entry_list, filename); /* Lookup the dir_entry for the name */
    
    if(dir_entry != NULL) {                                    /* Is a directory entry found ? */
      
      if(((dir_entry->inode->flags & VFS_FF_W) == 0) && (flags & (VFS_O_WRONLY | VFS_O_RDWR))) { 
        vfs_set_errno(EACCES);
        result = -1;
      }
      
      if(((dir_entry->inode->flags & VFS_FF_R) == 0) && (flags & (VFS_O_RDONLY | VFS_O_RDWR))) { 
        vfs_set_errno(EACCES);
        result = -1;
      }

      if((result == 0) && ((flags & VFS_O_TRUNC) == VFS_O_TRUNC))
        dir_entry->inode->inode_ops->truncate(dir_entry->inode);
      
    } else {                                                   /* No directory entry found -> create one */
      if((flags & VFS_O_CREAT) == VFS_O_CREAT) {
        
        dir_entry = allocMem(sizeof(vfs_Dir_Entry));
        inode = allocMem(sizeof(vfs_Inode));
        memset(dir_entry, 0, sizeof(vfs_Dir_Entry));
        memset(inode, 0, sizeof(inode));

        dir_entry->name = allocMem((w_size)(strlen(name) + 1)); /* Allocate memory for the name */
        strcpy(dir_entry->name, name);                         /* Copy the name */
        dir_entry->inode = inode;                              /* Put the inode in the dir_entry */
        dir_entry->mount = inode;
      
        dir_entry->parent = dir;                               /* Add the parent */
        dir_entry->child_list = NULL;
        dir_entry->next_entry = dir->child_list;               /* Add the dir_entry to the childlist */

        dir->child_list = dir_entry;                           /* of the parent (dir) */
        inode->flags = mode;
        inode->sb = dir->inode->sb;
        inode->inode_ops = dir->inode->inode_ops;
        vfs_set_errno(inode->inode_ops->create(inode, dir_entry));
        
        if(vfs_get_errno() != 0) {                                       /* Creation failed, cleanup a bit */
          result = -1;
          dir->child_list = dir_entry->child_list;
          releaseMem(inode);
          releaseMem(dir_entry);
        }

      } else {
	result = -1;
        vfs_set_errno(ENOENT);                                     /* Check if the path exists */
      }
    }
  }

  if(result == 0) {
    
    file = allocMem(sizeof(vfs_File));        /* Allocate some memory */

    file->file_desc = (file_list == NULL ? 1 : file_list->file_desc + 1);
                                                               /* File descriptor is the next in line, or 1 */
                                                               /* if there wasn't already an open file */
    file->dir_entry = dir_entry;                               /* Store the directory entry */
    if((flags & VFS_O_APPEND) == VFS_O_APPEND)
      file->position = file->dir_entry->inode->size;           /* Positioning at the end */
    else
      file->position = 0;                                      /* Start reading/writing at position 0 */
    
    file->flags = flags;

    file->next = file_list;                                    /* Add the file to the list of open files */
    file_list = file;

    result = file->file_desc;

  }

  vfs_unlock();
 
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

int vfs_creat(const w_ubyte *pathname, w_word mode) {
  return vfs_open(pathname, VFS_O_CREAT | VFS_O_WRONLY | VFS_O_TRUNC, mode);
}

/* --------------------------------------------------------------------------------------------------------*/

int vfs_close(const int file_desc) {
   
  vfs_file      file;
  vfs_file      temp;
  int           result = -1;                                  /* Default to 'not successful' */

  vfs_lock();

  file = file_list;
  
  woempa(4, "%d\n", file_desc);

  /* OLA: Should flush buffers to the device for writable files */

  vfs_set_errno(0);                                           /* Clear errno */

  if(file_list->file_desc == file_desc) {                     /* Is the first entry the one to be closed ? */
    file_list = file_list->next;                              /* yep, remove it from the list */
    releaseMem(file);                                               /* Release memory */
    result = 0;                                               /* No errors */
  } else {
    while((file != NULL) && (file->next != NULL)) {               /* Is there a next file ? */
      if(file->next->file_desc == file_desc) {                /* Found it */
        temp = file->next;
        file->next = (file->next == NULL ? NULL : file->next->next);
        releaseMem(temp);                                           /* Release memory */
        result = 0;                                           /* No errors */
      }
      file = file->next;                                      /* Go to the next file */
    }
  }

  if(result == -1) vfs_set_errno(EBADF);                      /* If result = -1, the descriptor was wrong */

  vfs_unlock();
  
  return result;
  
}

/* --------------------------------------------------------------------------------------------------------*/

int vfs_read(const int file_desc, w_void *buffer, const w_word count) {
 
  int             result = -1;                                /* Default to 'not successful' */
  vfs_file        file;                                       /* The open file list */
  vfs_dir_entry   dir_entry;                                   
  vfs_inode       inode;
  w_word          begin, end, pos, block_size;
  w_word          block_nr;
  w_word          dest_offset, src_offset, size;
  w_ubyte         *block_buffer;

  vfs_lock(); 

  file = file_list;
  
  woempa(4, "fd:%d, %d bytes\n", file_desc, count);

  vfs_set_errno(0);                                           /* Clear errno */

  while(file != NULL && file->file_desc != file_desc) file = file->next;

  if((file != NULL) && ((file->flags & (VFS_O_RDONLY | VFS_O_RDWR)) != 0)) {
    
    dir_entry = file->dir_entry;
    inode = dir_entry->inode;
    
    if((inode->flags & VFS_FF_DIR) == VFS_FF_DIR) {
      vfs_set_errno(EISDIR);
    } else {

      memset(buffer, 0, count);
      
      block_size = inode->sb->block_size;
      block_buffer = allocMem((w_size)block_size);
    
      begin = file->position;
      end   = begin + count;

      if(end > inode->size) end = inode->size;
      
      woempa(3, "begin: %d   end: %d\n", begin, end);
      
      block_nr = begin / block_size;
      pos      = block_nr * block_size;

      dest_offset = 0;
      src_offset = 0;

      while(pos < end) {

        /* OLA: Now a block of data is read into a seperate buffer than the destination */
        /* Speed can be improved by writing directly to the destination buffer. Tricky part */
        /* is that getblock() fetches a whole block regardless if we need the beginning of */
        /* that block -> possible by reading the whole block and then copy the needed part */
        /* over the beginning. Same problem arises with the end... Don't know a good solution (yet) */

        /* Also possible way to improve speed : Add a certain amount of blockbuffers into the */
        /* the open file structure, and read in blocks ahead */
      
        inode->inode_ops->get_block(inode, block_buffer, block_nr);

        size = block_size;

        if(pos < begin) { 
          src_offset = begin % block_size; 
          size = block_size - src_offset;
        } else {
          src_offset = 0;
        }  
        
        if(end < pos + block_size) {
          size = end % block_size;
          if(size > count) size = count;
        }
        
        woempa(1, "block_nr: %6d   src: %6d   dest: %6d   size: %6d\n", block_nr, src_offset, dest_offset, size);
      
        memcpy((w_ubyte *)((w_word)buffer + dest_offset), (w_ubyte *)((w_word)block_buffer + src_offset), size);
        
        dest_offset += size;
        
        block_nr++;
        
        pos += block_size;
      }

      result = end - begin;
      file->position = end;
      releaseMem(block_buffer);
    }
  } else {
    vfs_set_errno(EBADF);
  }

  vfs_unlock();

  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

int vfs_write(const int file_desc, const w_void *buffer, const w_word count) {
 
  int             result = -1;                                /* Default to 'not successful' */
  vfs_file        file;                                       /* The open file list */
  vfs_dir_entry   dir_entry;                                   
  vfs_inode       inode;
  w_word          begin, end, pos, block_size;
  w_word          block_nr;
  w_word          dest_offset, src_offset, size;
  w_ubyte         *block_buffer;

  vfs_lock();  

  result = -1;                                /* Default to 'not successful' */

  file = file_list;
  
  woempa(4, "%p fd:%d, %d bytes\n", currentWonkaThread, file_desc, count);

  vfs_set_errno(0);                                           /* Clear errno */
  
  while(file != NULL && file->file_desc != file_desc) file = file->next;

  if((file != NULL) && ((file->flags & (VFS_O_WRONLY | VFS_O_RDWR)) != 0)) {
    
    dir_entry = file->dir_entry;
    inode = dir_entry->inode;
    
    if((inode->flags & VFS_FF_DIR) == VFS_FF_DIR) {
      vfs_set_errno(EISDIR);
    } else {

      block_size = inode->sb->block_size;
      block_buffer = allocMem((w_size)block_size);

      if((file->flags & VFS_O_APPEND) == VFS_O_APPEND) file->position = inode->size; 
                                                              /* If file is open with append, make sure we write */
                                                              /* at the end of the file */
    
      begin = file->position;
      end   = begin + count;

      if(end > inode->size) inode->size = end; /* File can grow */
      
      woempa(2, "inode %d, begin %d, end %d\n", inode->nr, begin, end);
      
      block_nr = begin / block_size;
      pos      = block_nr * block_size;

      dest_offset = 0;
      src_offset = 0;

      while((pos < end) && (vfs_get_errno() == 0)) {

        size = block_size;

        if(pos < begin) { 
          src_offset = begin % block_size; 
          size = block_size - src_offset;
        } else {
          src_offset = 0;
        }  
        
        if(end < pos + block_size) {
          size = end % block_size;
          if(size > count) size = count;
        }

        if(src_offset != 0) {  /* If offset is different than zero, read in the buffer first */
          inode->inode_ops->get_block(inode, block_buffer, block_nr);
        }
        
        woempa(3, "block_nr: %6d   src: %6d   dest: %6d   size: %6d\n", block_nr, src_offset, dest_offset, size);
      
        memcpy((w_ubyte*)((w_word)block_buffer + src_offset), (w_ubyte *)((w_word)buffer + dest_offset), size);

        vfs_set_errno(inode->inode_ops->put_block(inode, block_buffer, block_nr));

        /* OLA: Should return the number of bytes actually written */

        dest_offset += size;
        
        block_nr++;
        
        pos += block_size;
      }

      result = end - begin;

      file->position = end;
  
      releaseMem(block_buffer);
    }
  } else {
    vfs_set_errno(EBADF);
  }

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

long vfs_lseek(const int file_desc, const long offset, const w_word whence) {
  vfs_file    file;
  w_long      result = -1;

  vfs_lock();

  file = file_list;
  
  woempa(4, "fd:%d, offset:%d, whence:%d\n", file_desc, offset, whence);

  vfs_set_errno(0);                                          /* Clear errno */

  while(file != NULL && file->file_desc != file_desc) file = file->next;
                                                             /* Locate the file structure */
  if(file != NULL) {                                         /* File structure found ? */
    switch(whence) {                                         /* Which kind of position ? */
      case SEEK_SET: result = 0;                            
                     file->position = offset;                /* Set to offset */
                     break;
      case SEEK_CUR: result = 0;
                     file->position += offset;               /* Go to position + offset */
                     break;
      case SEEK_END: result = 0;                              
                     file->position += offset + file->dir_entry->inode->size;
                                                             /* Go to EOF + offset */
                     break;
      default:       vfs_set_errno(EINVAL);                  /* Default: Bad interval */
                     break;
    }
  } else {
    vfs_set_errno(EBADF);                                    /* Filedescriptor not found... */
  }

  vfs_unlock();  
  
  return result;
}
 

/* --------------------------------------------------------------------------------------------------------*/

vfs_DIR *vfs_opendir(const w_ubyte *name) {
  vfs_dir_entry      dir_entry = NULL;
  vfs_DIR            *result = NULL;

  w_ubyte            *buffer;    
  w_ubyte            *ptr;      
  vfs_low_dir_entry  lowlevel_dirent;
  char               *ename; 
  vfs_inode          inode;    
 
  vfs_lock(); 
  
  woempa(4, "%s\n", name);

  ename = allocMem(255); /* OLA: Fixed size.... */

  vfs_set_errno(0);
  
  dir_entry = vfs_lookup_fullpath(dir_entry_list, name);     /* Lookup the dir_entry for the name */

  if(dir_entry != NULL)                                      /* Is a directory entry found ? */
    if((dir_entry->inode->flags & VFS_FF_DIR) != VFS_FF_DIR) 
      vfs_set_errno(ENOTDIR);
    else {                                                   /* Not found, read in from the fs */

      if(dir_entry->inode != dir_entry->mount) { 
        inode = dir_entry->mount;
      } else {
        inode = dir_entry->inode;
      }

      buffer = allocMem((w_size)inode->size);
      ptr = buffer;

      inode->inode_ops->read_dir(inode, buffer);
  
      lowlevel_dirent = (vfs_low_dir_entry)ptr;
      while(lowlevel_dirent->inode != 0 && lowlevel_dirent->name_length != 0) {
        memset(ename, 0, 255); /* OLA: Fixed length -> danger Will Robinson ! danger ! */
        strncpy(ename, lowlevel_dirent->name, lowlevel_dirent->name_length);
        vfs_lookup_entry(dir_entry, ename);
        ptr += lowlevel_dirent->entry_length;
        lowlevel_dirent = (vfs_low_dir_entry)ptr;
      }

      releaseMem(buffer);

      /* Continue */

      result = allocMem(sizeof(vfs_DIR));  
                                                    /* Allocate memory for the dir descriptor */
      result->head = dir_entry;                     /* Make head point to the parent directory */
      result->current = dir_entry->child_list;      /* Current = the first child */
      result->next = DIR_list;                      /* Add this descriptor to the list of open dirs */
      DIR_list = result;                                         

    }
  else 
    vfs_set_errno(ENOENT);                          /* No directory entry -> file does not exist */
  
  releaseMem(ename);
 
  vfs_unlock(); 
  
  return result;
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_closedir(vfs_DIR *dir) {
  vfs_DIR       *dir_list;
  int           result = -1;                        /* Default to 'not successful' */

  vfs_lock();

  dir_list = DIR_list;
  
  woempa(4, "%s\n", dir->head->name);

  vfs_set_errno(0);                                 /* Clear errno */

  if(DIR_list == dir) {                             /* Is dir the first entry in the dir list ? */
    DIR_list = DIR_list->next;                      /* Remove it from the list */
    releaseMem(dir);                                      /* Release memory */
    result = 0;                                     /* Mission accomplished */
  } else {
    while(dir_list != NULL && dir_list->next != NULL) {
                                                    /* Is there a next DIR ? */
      if(dir_list->next == dir) {                   /* Found it */
        dir_list->next = dir->next;                 /* Remove it from the list */
        releaseMem(dir);                                  /* Release memory */
        result = 0;                                 /* Mission accomplished */
      }
      dir_list = dir_list->next;                    /* Go to the next entry in the list */
    }
  }
 
  if(result == -1) vfs_set_errno(EBADF);            /* Mission failed ? -> Bad descriptor */

  vfs_unlock();  
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

vfs_dirent *vfs_readdir(vfs_DIR *dir) {
  vfs_dirent *result = NULL;                          /* Default to 'no more entries' */       

  vfs_lock();
  
  woempa(4, "%s\n", dir->head->name);

  if(dir->current != NULL) {                          /* See if dir is valid */
    strcpy(dir->dir_ent.d_name, dir->current->name);  /* Get the name from current and put it in the dirent */
    dir->current = dir->current->next_entry;          /* Current points to the next entry */
    result = &dir->dir_ent;                           /* Return the dirent */
  }

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_rewinddir(vfs_DIR *dir) {
  vfs_lock();
  dir->current = dir->head->child_list;             /* Current points back to the first child (entry) */
  vfs_unlock();
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_telldir(vfs_DIR *dir) {
  vfs_dir_entry dir_entry;
  int           result = -1;
  w_word        offset = 0;

  vfs_lock();

  dir_entry = dir->head->child_list;

  while((dir->current != dir_entry) && (dir_entry->next_entry != NULL)) {
    dir_entry = dir_entry->next_entry;
    offset++;
  }
  
  if(dir_entry == dir->current) result = offset;

  vfs_unlock();  
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_seekdir(vfs_DIR *dir, w_word offset) {
  vfs_dir_entry dir_entry;
  w_word        count;

  vfs_lock();

  dir_entry = dir->head->child_list;

  for(count=0; count < offset && dir_entry->next_entry != NULL; count++) dir_entry = dir_entry->next_entry;

  dir->current = dir_entry;

  vfs_unlock();  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_alphasort(struct vfs_dirent **a, struct vfs_dirent **b) {
  return strcmp((char *)((struct vfs_dirent *)(*a)->d_name), (char *)((struct vfs_dirent *)(*b)->d_name));
}

/* --------------------------------------------------------------------------------------------------------*/

// w_word vfs_scandir(const w_ubyte *dir, struct vfs_dirent ***namelist,
//    w_word (*select)(const struct vfs_dirent *),
//    w_word (*compar)(const struct vfs_dirent **, const struct vfs_dirent **)) {

  /* OLA: Still need to fill this is... */

//  return 0;  
//}

/* --------------------------------------------------------------------------------------------------------*/

int vfs_stat(const w_ubyte *filename, struct vfs_STAT *buf) {
  
  vfs_dir_entry dir_entry;
  int           result = -1;                                  /* Default to 'not successful' */

  vfs_lock();
  
  woempa(4, "%s\n", filename);

  vfs_set_errno(0);                                           /* Clear errno */
  
  dir_entry = vfs_lookup_fullpath(dir_entry_list, filename);  /* Lookup the dir_entry for the name */

  if(dir_entry != NULL) {                                     /* Is a directory entry found ? */
    result = 0;
    buf->st_dev = 0;                                          /* device */
    buf->st_ino = dir_entry->inode->nr;                       /* inode */
    buf->st_mode = dir_entry->inode->flags;                   /* protection */
    buf->st_size = dir_entry->inode->size;                    /* total size, in bytes */
    buf->st_blksize = dir_entry->inode->sb->block_size;       /* blocksize for filesystem I/O */
  } else {
    vfs_set_errno(ENOENT);                                    /* No directory entry -> file does not exist */
  }

  vfs_unlock(); 
  
  return result;

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fstat(int file_desc, struct vfs_STAT *buf) {
  
  vfs_dir_entry dir_entry;
  vfs_file      file;
  int           result = -1;                                  /* Default to 'not successful' */

  vfs_lock();

  file = file_list;

  woempa(4, "%d\n", file_desc);

  vfs_set_errno(0);                                           /* Clear errno */
  
  while(file != NULL && file->file_desc != file_desc) file = file->next;
                                                              /* Look for the right file structure */

  if(file != NULL) {                                          /* file stucture found ? */
    dir_entry = file->dir_entry;

    result = 0;
    buf->st_dev = 0;                                          /* device */
    buf->st_ino = dir_entry->inode->nr;                       /* inode */
    buf->st_mode = dir_entry->inode->flags;                   /* protection */
    buf->st_size = dir_entry->inode->size;                    /* total size, in bytes */
    buf->st_blksize = dir_entry->inode->sb->block_size;       /* blocksize for filesystem I/O */
  } else {
    vfs_set_errno(EBADF);                                     /* No file found ?-> Bad file descriptor */
  }

  vfs_unlock();  
  
  return result;

}

/* --------------------------------------------------------------------------------------------------------*/

vfs_FILE *vfs_fopen (const w_ubyte *path, const w_ubyte *mode) {
  
  vfs_FILE   *stream = NULL;
  int        file_desc;
  w_word     flags = 0;
 
  vfs_lock(); 

  woempa(4, "start path: %s, mode: %s\n", path, mode);  
  
  if(strcmp(mode, "r") == 0) flags = VFS_O_RDONLY;
  if(strcmp(mode, "r+") == 0) flags = VFS_O_RDWR;
  if(strcmp(mode, "w") == 0) flags = VFS_O_CREAT | VFS_O_TRUNC | VFS_O_WRONLY;
  if(strcmp(mode, "w+") == 0) flags = VFS_O_CREAT | VFS_O_TRUNC | VFS_O_RDWR;
  if(strcmp(mode, "a") == 0) flags = VFS_O_CREAT | VFS_O_APPEND | VFS_O_WRONLY;
  if(strcmp(mode, "a+") == 0) flags = VFS_O_CREAT | VFS_O_APPEND | VFS_O_RDWR;

  file_desc = vfs_open(path, flags, VFS_FF_R | VFS_FF_W);

  if(file_desc != -1) {                                 /* Valid filedes ? */
  
    stream = allocMem(sizeof(vfs_FILE));  /* Allocate memory */
    stream->file_desc = file_desc;                      /* Store the file descriptor */
    stream->mode = allocMem((w_size)strlen(mode)+1);  /* Allocate memory to store the mode */
    strcpy(stream->mode, mode);                         /* Copy the mode to the stream */
    stream->error = 0;
    stream->next = FILE_list;                           /* Add this stream to the open streams list */

  }

  vfs_unlock();

  return stream;
  
}

/* --------------------------------------------------------------------------------------------------------*/

vfs_FILE *vfs_fdopen (int file_desc, const w_ubyte *mode) {
  
  vfs_file   file;
  vfs_FILE   *stream = NULL;
  w_word     flags = 0;

  vfs_lock();

  file = file_list;

  woempa(4, "fd: %d, mode: %s\n", file_desc, mode);

  if(strcmp(mode, "r") == 0) flags = VFS_O_RDONLY;
  if(strcmp(mode, "r+") == 0) flags = VFS_O_RDWR;
  if(strcmp(mode, "w") == 0) flags = VFS_O_CREAT | VFS_O_TRUNC | VFS_O_WRONLY;
  if(strcmp(mode, "w+") == 0) flags = VFS_O_CREAT | VFS_O_TRUNC | VFS_O_RDWR;
  if(strcmp(mode, "a") == 0) flags = VFS_O_CREAT | VFS_O_APPEND | VFS_O_WRONLY;
  if(strcmp(mode, "a+") == 0) flags = VFS_O_CREAT | VFS_O_APPEND | VFS_O_RDWR;

  vfs_set_errno(0);

  while(file != NULL && file->file_desc != file_desc) file = file->next;

  file->flags = flags;
  
  if(file != NULL) {                                         /* File structure found ? */
    
    stream = allocMem(sizeof(vfs_FILE));    /* Allocate memory */
    stream->file_desc = file_desc;                           /* Store the file descriptor */
    stream->mode = allocMem((w_size)strlen(mode)+1);  /* Allocate memory to store the mode */
    strcpy(stream->mode, mode);                              /* Copy the mode to the stream */
    stream->error = 0;
    stream->next = FILE_list;                                /* Add this stream to the open streams list */   
  } else {
    vfs_set_errno(EBADF);                                    /* Filedescriptor not found... */
  }

  vfs_unlock();  
  
  return stream;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fclose(vfs_FILE *stream) {
  vfs_FILE      *streams;
  int           result = -1;                                 /* Default to 'not successful' */

  vfs_lock();

  woempa(4, "\n");

  streams = FILE_list;
  
  vfs_fflush(stream);                                        /* Flush unwritten buffers to the device */
 
  vfs_set_errno(0);

  if(streams == stream) {                                    /* First entry is to be closed */
    streams = streams->next;
    releaseMem(stream);
    result = 0;
  } else {
    while(streams != NULL && streams->next != NULL) {        /* Is there a next stream ? */
      if(streams->next == stream) {                          /* Found it */
        streams->next = (streams->next == NULL ? NULL : streams->next->next);
        // vfs_close(stream->file_desc);                        /* Close the file */
        releaseMem(stream);                                  /* Release the memory */
        result = 0;
      }
      streams = streams->next;
    }
  }
  
  vfs_unlock(); 

  return result;
  
}

/* --------------------------------------------------------------------------------------------------------*/

int vfs_fseek(vfs_FILE *stream, long offset, w_word whence) {
 
  vfs_file file;
  int      result = -1;

  vfs_lock();

  file = file_list;
  
  woempa(4, "offset: %d, whence: %d\n", offset, whence);

  vfs_set_errno(0);                                          /* Clear errno */

  while(file != NULL && file->file_desc != stream->file_desc) file = file->next;
                                                             /* Locate the file structure */
  if(file != NULL) {                                         /* File structure found ? */
    switch(whence) {                                         /* Which kind of position ? */
      case SEEK_SET: result = 0;                            
                     file->position = offset;                /* Set to offset */
                     break;
      case SEEK_CUR: result = 0;
                     file->position += offset;               /* Go to position + offset */
                     break;
      case SEEK_END: result = 0;                              
                     file->position += offset + file->dir_entry->inode->size;
                                                             /* Go to EOF + offset */
                     break;
      default:       vfs_set_errno(EINVAL);                  /* Default: Bad interval */
                     break;
    }
  } else {
    vfs_set_errno(EBADF);                                    /* Filedescriptor not found... */
  }

  if(file->position >= file->dir_entry->inode->size) {
    stream->error = VFS_STREAM_EOF;
  }
                                                             /* Set the eof flag if needed */
  vfs_unlock();
  
  return result;
 
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_rewind(vfs_FILE *stream) {
  woempa(4, "\n");

  return vfs_fseek(stream, 0, SEEK_SET);
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_ftell(vfs_FILE *stream) {

  vfs_file   file;
  int        result = -1;

  vfs_lock();
  
  file = file_list;
  
  woempa(4, "\n");

  while(file != NULL && file->file_desc != stream->file_desc) file = file->next;
                                                             /* Locate the file structure */
  if(file != NULL) {                                         /* File structure found ? */
    result = file->position;
  } else {
    vfs_set_errno(EBADF);                                    /* Filedescriptor not found... */
  }

  vfs_unlock(); 
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fgetpos(vfs_FILE *stream, vfs_fpos_t *pos) {
 
  int      result = -1;

  vfs_lock();

  woempa(4, "\n");
  
  *pos = vfs_ftell(stream);
  if(*pos != -1) result = 0;

  vfs_unlock();

  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fsetpos(vfs_FILE *stream, vfs_fpos_t *pos) {

  woempa(4, "\n");

  return vfs_fseek(stream, *pos, SEEK_SET);
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fread(w_void *ptr, w_word size, w_word nmemb, vfs_FILE *stream) {
  
  w_word   result = 0; 

  vfs_lock();

  woempa(4, "\n");
  
  if(!(stream->error && VFS_STREAM_EOF)) { 
    result = vfs_read(stream->file_desc, ptr, size * nmemb);
    if(result < nmemb * size) stream->error |= VFS_STREAM_EOF;     /* If we get less than we asked, */
                                                                    /* end-of-file is reached */
  }

  vfs_unlock();
  return result / size;

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fwrite(const w_void *ptr, w_word size, w_word nmemb, vfs_FILE *stream) {
  
  w_word   result = 0; 

  vfs_lock();

  woempa(4, "\n");

  /* flags are checked by the vfs_write function */
  result = vfs_write(stream->file_desc, ptr, size * nmemb);

  vfs_unlock();

  return result / size;

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_feof(vfs_FILE *stream) {
  woempa(4, "\n");

  vfs_lock();  
  vfs_unlock();

  return (stream->error && VFS_STREAM_EOF);
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_ferror(vfs_FILE *stream) {
  woempa(4, "\n");
  
  vfs_lock();  
  vfs_unlock();

  return (stream->error & VFS_STREAM_ERROR);
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_clearerr(vfs_FILE *stream) {
  woempa(4, "\n");
  
  vfs_lock();  
  stream->error = 0;
  vfs_unlock();

}


/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fgetc(vfs_FILE *stream) {
  
  w_ubyte result;
  
  woempa(4, "\n");

  vfs_lock();

  vfs_fread((w_void *)&result, 1, 1, stream);

  vfs_unlock();

  return (w_word)result;

}

/* --------------------------------------------------------------------------------------------------------*/

w_ubyte *vfs_fgets(w_ubyte *s, w_word size, vfs_FILE *stream) {
  w_word   count = 0;
  w_ubyte  *ptr;
  w_ubyte  c = 0;

  woempa(4, "\n");

  vfs_lock();

  ptr = s;

  while((count < size) && (!vfs_feof(stream)) && (c != '\n') && (c != '\0')) {
    vfs_fread(ptr, 1, 1, stream);
    c = *ptr;
    count++;
    ptr++;
  }

  ptr--;
  memcpy(ptr, "\0", 1);

  vfs_unlock();

  if(count != 0) return s; else return NULL;
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_getc(vfs_FILE *stream) {                 /* Copy of vfs_fgetc */

  w_ubyte result;
  
  woempa(4, "\n");

  vfs_lock();

  vfs_fread((w_void *)&result, 1, 1, stream);

  vfs_unlock();

  return (w_word)result;
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fputc(w_int c, vfs_FILE *stream) {

  w_ubyte result = c;
  
  /* OLA: EOF ? */

  woempa(4, "%c\n", c);

  vfs_lock();

  vfs_fwrite((w_ubyte *)&result, 1, 1, stream);

  vfs_unlock();
  
  return (w_word)result; /* OLA: Should check if the char is indeed written */

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fputs(const w_ubyte *s, vfs_FILE *stream) {

  woempa(4, "%s\n", s);

  return vfs_fwrite(s, 1, strlen(s), stream);
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_putc(w_word c, vfs_FILE *stream) {       /* Copy of vfs_fputc */

  w_ubyte result = c;
  
  /* OLA: EOF ? */

  woempa(4, "%c\n", c);
 
  vfs_lock();

  vfs_fwrite((w_ubyte *)&result, 1, 1, stream);

  vfs_unlock();
  
  return (w_word)result; /* OLA: Should check if the char is indeed written */

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fflush(vfs_FILE *stream) {

  vfs_file        file;                                       /* The open file list */
  vfs_dir_entry   dir_entry;                                   
  vfs_inode       inode;

  vfs_lock(); 

  file = file_list;
  
  woempa(4, "\n");

  vfs_set_errno(0);                                           /* Clear errno */

  while(file != NULL && file->file_desc != stream->file_desc) file = file->next;

  if(file != NULL) {
    
    dir_entry = file->dir_entry;
    inode = dir_entry->inode;

    vfs_flush_dir_entry(dir_entry_list); // Flush the entire filesystem

    inode->sb->super_ops->flush(inode->sb);
    
  }

  vfs_unlock();
    
  return 0;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_mkdir(const w_ubyte *pathname, w_word mode) {
  
  w_ubyte        *name;
  w_ubyte        *path;
  vfs_dir_entry  dir_entry = NULL;
  vfs_dir_entry  new_dir;
  vfs_inode      inode;
  int            result = 0;

  vfs_lock();

  path = allocMem((w_size)(strlen(pathname) + 1));
  strcpy(path, pathname);
  
  name = strrchr(path, '/');

  if(name != NULL) {
    *name = '\0'; 
    name++;
  } 

  woempa(4, "%s in %s\n", name, path);

  if(vfs_lookup_fullpath(dir_entry_list, pathname) != NULL) {  /* Check if this name isn't already in use */
    vfs_set_errno(EEXIST);
    result = -1;
  }
  
  if((result == 0) && ((dir_entry = vfs_lookup_fullpath(dir_entry_list, path)) == NULL)) {
                                                               /* Check if the path exists */
    vfs_set_errno(ENOENT);
    result = -1;
  }

  if((result == 0) && !(VFS_S_ISDIR(dir_entry->inode->flags))) {   /* Check if path is a directory */
    vfs_set_errno(ENOTDIR);
    result = -1;
  }

  if((result == 0) && ((dir_entry->inode->sb->flags & VFS_MOUNT_RW) == 0)) {
                                                               /* Check if fs is read/write */
    vfs_set_errno(EROFS);
    result = -1;
  }

  if((result == 0) && ((dir_entry->inode->flags & VFS_FF_W) == 0)) {
                                                               /* Check if directory is writeable */
    vfs_set_errno(EACCES);
    result = -1;
  }

  if(result == 0) {                                            /* All oki, make a directory */
    new_dir = allocMem(sizeof(vfs_Dir_Entry));
    inode = allocMem(sizeof(vfs_Inode));
    memset(new_dir, 0, sizeof(vfs_Dir_Entry));
    memset(inode, 0, sizeof(inode));
    
    new_dir->name = allocMem((w_size)(strlen(name) + 1));
    strcpy(new_dir->name, name);
    new_dir->inode = inode;
    new_dir->mount = inode;
    new_dir->parent = dir_entry;

    new_dir->child_list = dir_entry->child_list;
    dir_entry->child_list = new_dir;

    inode->flags = mode;
    inode->sb = dir_entry->inode->sb;
    inode->inode_ops = dir_entry->inode->inode_ops;
    
    inode->inode_ops->mkdir(inode, new_dir);
    
  }

  releaseMem(path);

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_rmdir(const w_ubyte *pathname){
  
  w_ubyte         *name;
  w_ubyte         *path;
  vfs_dir_entry   dir_entry;
  vfs_dir_entry   temp;
  int             result = 0;
  w_word          i;
  vfs_DIR         *DIR;
 
  vfs_lock(); 
  
  path = allocMem((w_size)(strlen(pathname) + 1));
  strcpy(path, pathname);
  
  name = strrchr(path, '/');

  if(name != NULL) {
    *name = '\0'; 
    name++;
  } 

  woempa(4, "%s in %s\n", name, path);

  if((dir_entry = vfs_lookup_fullpath(dir_entry_list, pathname)) == NULL) {  
	                                                             /* Check if this name is in use */
    vfs_set_errno(ENOENT);
    result = -1;
  }

  if((result == 0) && !(VFS_S_ISDIR(dir_entry->inode->flags))) {   /* Check if name is a directory */
    vfs_set_errno(ENOTDIR);
    result = -1;
  }

  if((result == 0) && ((dir_entry->inode->sb->flags & VFS_MOUNT_RW) == 0)) {
                                                               /* Check if fs is read/write */
    vfs_set_errno(EROFS);
    result = -1;
  }

  if((result == 0) && ((dir_entry->parent->inode->flags & VFS_FF_W) == 0)) {
                                                               /* Check if parent directory is writeable */
    vfs_set_errno(EACCES);
    result = -1;
  }

  if(result == 0) {                                            /* Check if dir is empty */

    if(dir_entry->child_list == NULL) {                        /* Childeren are not known -> read them */
      DIR = vfs_opendir(pathname);
      vfs_closedir(DIR);
    }

    i = 0;
    temp = dir_entry->child_list;
    while(temp != NULL) { temp = temp->next_entry; i++; }
   
    if(i != 2) {
      vfs_set_errno(ENOTEMPTY);      
      result = -1;
      woempa(3, "directory is not empty.\n");
    }
  }

  if(result == 0) {                                            /* All oki, remove a directory */
  
    dir_entry->inode->inode_ops->rmdir(dir_entry->parent->inode, dir_entry);
    dir_entry->inode->sb->super_ops->flush(dir_entry->parent->inode->sb);

    vfs_cleanup_dir_entry(dir_entry);                          /* Clear the childeren */

    dir_entry_list->total_entries--;                      
    
    /* Remove this dir_entry from the parents child list */
    
    temp = dir_entry->parent->child_list;
    if(temp == dir_entry) {
      dir_entry->parent->child_list = dir_entry->next_entry;
      releaseMem(dir_entry);
    } else {
      while(temp != NULL && temp->next_entry != NULL) {
        if(temp->next_entry == dir_entry) {
          temp->next_entry = dir_entry->next_entry;
          releaseMem(dir_entry);
        }
        temp = temp->next_entry;
      }
    }

  }

  releaseMem(path);

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_unlink(const w_ubyte *pathname) {

  w_ubyte        *name;
  w_ubyte        *path;
  vfs_dir_entry  dir_entry;
  vfs_dir_entry  temp;
  int            result = 0;

  vfs_lock();
  
  path = allocMem((w_size)(strlen(pathname) + 1));
  strcpy(path, pathname);
  
  name = strrchr(path, '/');

  if(name != NULL) {
    *name = '\0'; 
    name++;
  } 

  woempa(4, "%s in %s\n", name, path);

  if((dir_entry = vfs_lookup_fullpath(dir_entry_list, pathname)) == NULL) {  
	                                                             /* Check if this name is in use */
    vfs_set_errno(ENOENT);
    result = -1;
  }

  if((result == 0) && (VFS_S_ISDIR(dir_entry->inode->flags))) {    /* Check if name is a directory */
    vfs_set_errno(EPERM);
    result = -1;
  }

  if((result == 0) && ((dir_entry->inode->sb->flags & VFS_MOUNT_RW) == 0)) {
                                                               /* Check if fs is read/write */
    vfs_set_errno(EROFS);
    result = -1;
  }

  if((result == 0) && ((dir_entry->parent->inode->flags & VFS_FF_W) == 0)) {
                                                               /* Check if parent directory is writeable */
    vfs_set_errno(EACCES);
    result = -1;
  }

  if(result == 0) {                                            /* All oki, remove a directory */

    dir_entry->inode->sb->super_ops->unlink_inode(dir_entry->parent->inode, dir_entry);    
    dir_entry->inode->sb->super_ops->flush(dir_entry->parent->inode->sb);

    dir_entry_list->total_entries--;

    /* Remove this dir_entry from the parents child list */
    
    temp = dir_entry->parent->child_list;
    if(temp == dir_entry) {
      dir_entry->parent->child_list = dir_entry->next_entry;
      releaseMem(dir_entry);
    } else {
      while(temp != NULL && temp->next_entry != NULL) {
        if(temp->next_entry == dir_entry) {
          temp->next_entry = dir_entry->next_entry;
          releaseMem(dir_entry);
        }
        temp = temp->next_entry;
      }
    }

  }

  releaseMem(path);

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_chmod(const w_ubyte *path, w_word mode) {
  
  int           result = -1;
  vfs_dir_entry dir_entry;

  vfs_lock();
  
  dir_entry = vfs_lookup_fullpath(dir_entry_list, path);
  
  woempa(4, "path: %s, mode: %d\n", path, mode);
      
  vfs_set_errno(0);
  
  if(dir_entry != NULL) {

    if((dir_entry->inode->sb->flags & VFS_MOUNT_RW) == VFS_MOUNT_RW) {
      
      dir_entry->inode->flags = mode;
      result = 0;
    } else {
      vfs_set_errno(EROFS);
    }
    
  } else {
    vfs_set_errno(ENOENT);
  }

  vfs_unlock();
  
  return result;

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_fchmod(int file_desc, w_word mode) {

  int        result = -1;
  vfs_file   file;                   /* The open file list */

  vfs_lock();
  
  file = file_list;
  woempa(4, "fd: %d, mode: %d\n", file_desc, mode);

  vfs_set_errno(0);                              /* Clear errno */
  
  while(file != NULL && file->file_desc != file_desc) file = file->next;      
  
  if(file != NULL) {

    if((file->dir_entry->inode->sb->flags & VFS_MOUNT_RW) == VFS_MOUNT_RW) {
  
      file->dir_entry->inode->flags = mode;
      
      result = 0;
    } else {
      vfs_set_errno(EROFS);
    }
    
  } else {
    vfs_set_errno(EBADF);
  }

  vfs_unlock();
  
  return result;

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_rename(const w_ubyte *oldpath, const w_ubyte *newpath) {

#ifdef CRAP
	
  vfs_dir_entry dir_entry;
  vfs_dir_entry dir;
  vfs_inode     inode;
  vfs_file      file;
  w_word        result = 0;
  w_ubyte       *name, *path;

  path = allocMem((w_size)(strlen(filename) + 1));
  strcpy(path, filename);
  
  name = strrchr(path, '/');

  if(name != NULL) {
    *name = '\0'; 
    name++;
  } 
 
  woempa(4, "%s in %s\n", name, path);

  vfs_set_errno(0);
 
  if((vfs_lookup_fullpath(dir_entry_list, filename) != NULL) && ((flags & (VFS_O_CREAT & VFS_O_EXCL)) != 0)) {  
    vfs_set_errno(EEXIST);                                     /* Check if this name isn't already in use */
    result = -1;                                               /* While VFS_O_CREAT and VFS_O_EXCL are used */
  }
  
  if((result == 0) && ((dir = vfs_lookup_fullpath(dir_entry_list, path)) == NULL)) {
    vfs_set_errno(ENOENT);                                     /* Check if the path exists */
    result = -1;
  }

 if((result == 0) && !(VFS_S_ISDIR(dir->inode->flags))) {          /* Check if path is a directory */
    vfs_set_errno(ENOTDIR);
    result = -1;
  }

  if((result == 0) && ((dir->inode->sb->flags & VFS_MOUNT_RW) == 0) && ((flags & VFS_O_WRONLY) != 0)) {
    vfs_set_errno(EROFS);                                      /* Check if fs is read/write while file is */
    result = -1;                                               /* being opened for writing */
  }

  if((result == 0) && ((dir->inode->flags & VFS_FF_W) == 0) && ((flags & (VFS_O_CREAT | VFS_O_WRONLY | VFS_O_RDWR)) != 0)) {
    vfs_set_errno(EACCES);                                     /* Check if directory is writeable while */
    result = -1;                                               /* file is being opened for writing */
  }

  if((result == 0) && ((dir->inode->flags & VFS_FF_R) == 0) && ((flags & (VFS_O_RDONLY | VFS_O_RDWR)) != 0)) {
    vfs_set_errno(EACCES);                                     /* Check if directory is readable while */
    result = -1;                                               /* file is being opened for reading */
  }
  
  if(result == 0) {
    dir_entry = vfs_lookup_fullpath(dir_entry_list, filename); /* Lookup the dir_entry for the name */
    
    if(dir_entry != NULL) {                                    /* Is a directory entry found ? */
      
      if(((dir_entry->inode->flags & VFS_FF_W) == 0) && (flags & (VFS_O_WRONLY | VFS_O_RDWR))) { 
        vfs_set_errno(EACCES);
        result = -1;
      }
      
      if(((dir_entry->inode->flags & VFS_FF_R) == 0) && (flags & (VFS_O_RDONLY | VFS_O_RDWR))) { 
        vfs_set_errno(EACCES);
        result = -1;
      }
      
      if((flags & VFS_O_TRUNC) == VFS_O_TRUNC)
        dir_entry->inode->inode_ops->truncate(dir_entry->inode);
      
    } else {                                                   /* No directory entry found -> create one */
      if((flags & VFS_O_CREAT) == VFS_O_CREAT) {
        
        dir_entry = allocMem(sizeof(vfs_Dir_Entry));
        inode = allocMem(sizeof(vfs_Inode));
        memset(dir_entry, 0, sizeof(vfs_Dir_Entry));
        memset(inode, 0, sizeof(inode));

        dir_entry->name = allocMem((w_size)(strlen(name) + 1)); 
	                                                       /* Allocate memory for the name */
	
        strcpy(dir_entry->name, name);                         /* Copy the name */
        dir_entry->inode = inode;                              /* Put the inode in the dir_entry */
      
        dir_entry->parent = dir;                               /* Add the parent */
        dir_entry->child_list = dir->child_list;               /* Add the dir_entry to the childlist */
        dir->child_list = dir_entry;                           /* of the parent (dir) */

        inode->flags = mode;
        inode->sb = dir->inode->sb;
        inode->inode_ops = dir->inode->inode_ops;

        inode->inode_ops->create(inode, dir_entry);

      }
    }
  }

  if(result == 0) {
    
    file = allocMem(sizeof(vfs_File));        /* Allocate some memory */
    file->file_desc = (file_list == NULL ? 1 : file_list->file_desc + 1);
                                                               /* File descriptor is the next in line, or 1 */
                                                               /* if there wasn't already an open file */
    file->dir_entry = dir_entry;                               /* Store the directory entry */
    if((flags & VFS_O_APPEND) == VFS_O_APPEND)
      file->position = file->dir_entry->inode->size;           /* Positioning at the end */
    else
      file->position = 0;                                      /* Start reading/writing at position 0 */
    
    file->flags = flags;
    
    file->next = file_list;                                    /* Add the file to the list of open files */
    file_list = file;

    result = file->file_desc;
  }

  return result;

  w_ubyte        *name;
  w_ubyte        *path;
  vfs_dir_entry  dir_entry;
  vfs_dir_entry  temp;
  w_word         result = 0;

  path = allocMem((w_size)(strlen(pathname) + 1));
  strcpy(path, pathname);
  
  name = strrchr(path, '/');

  if(name != NULL) {
    *name = '\0'; 
    name++;
  } 

  woempa(3, "%s in %s\n", name, path);

  if((dir_entry = vfs_lookup_fullpath(dir_entry_list, pathname)) == NULL) {  
	                                                             /* Check if this name is in use */
    errno = ENOENT;
    result = -1;
  }
  
  if((result == 0) && (S_ISDIR(dir_entry->inode->flags))) {    /* Check if name is a directory */
    errno = EPERM;
    result = -1;
  }

  if((result == 0) && ((dir_entry->inode->sb->flags & VFS_MOUNT_RW) == 0)) {
                                                               /* Check if fs is read/write */
    errno = EROFS;
    result = -1;
  }

  if((result == 0) && ((dir_entry->parent->inode->flags & VFS_FF_W) == 0)) {
                                                               /* Check if parent directory is writeable */
    errno = EACCES;
    result = -1;
  }

  if(result == 0) {                                            /* All oki, remove a directory */
  
    dir_entry->inode->sb->super_ops->unlink_inode(dir_entry->parent->inode, dir_entry);

    dir_entry_list->total_entries--;

    /* Remove this dir_entry from the parents child list */
    
    temp = dir_entry->parent->child_list;
    if(temp == dir_entry) {
      dir_entry->parent->child_list = dir_entry->next_entry;
      releaseMem(dir_entry);
    } else {
      while(temp != NULL && temp->next_entry != NULL) {
        if(temp->next_entry == dir_entry) {
          temp->next_entry = dir_entry->next_entry;
          releaseMem(dir_entry);
        }
        temp = temp->next_entry;
      }
    }

  }

  releaseMem(path);

  return result;

#endif
}

