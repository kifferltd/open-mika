/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/



#ifndef VFS_INCLUDE
#define VFS_INCLUDE

#include <e2fs.h>
#include <oswald.h>
#include <vfs_fcntl.h>

/* File flags */

#define VFS_FF_R      0x01
#define VFS_FF_W      0x02
#define VFS_FF_DIR    0x04

/* Superblock flags -> Mount flags */

#define VFS_MOUNT_RW  0x01
#define VFS_MOUNT_RO  0x00

// extern w_word errno;
extern char *current_working_dir;
extern char *current_root_dir;

/* structure of the vfs superblock */

typedef struct vfs_Superblock {

  w_word                       block_size;    /* Size of the blocks */
  struct vfs_Filesystem_Type   *type;         /* Which kind of filesystem is this */
  struct vfs_Super_Operations  *super_ops;    /* Operations on the superblock */
  w_word                       root_inode_nr; /* The number of the root inode of this superblock */
  w_word                       flags;         /* flags e.g. read only */
  w_ubyte                      *device;       /* Representation of the device */

  union {
    e2fs_filesystem            e2fs;          /* Pointer to an e2fs filesystem */
  } sb;                                       /* union of "superblocks", one for each known filesystem */
  
} vfs_Superblock;

typedef vfs_Superblock  *vfs_superblock;

/* Structure of a vfs inode */

typedef struct vfs_Inode {

  w_word                      nr;                /* Inode number */
  w_word                      size;              /* Size in bytes */
  w_ushort                    flags;             /* File flags */
  
  vfs_superblock              sb;                /* Superblock */
  struct vfs_Inode_Operations *inode_ops;        /* Operations on the inode */

  union {
    e2fs_inode   e2fs;                           /* Pointer to an e2fs inode */
  } inode;                                       /* union of "inodes", one for each known filesystem */

} vfs_Inode;

typedef vfs_Inode  *vfs_inode;

/* File system type */

typedef struct vfs_Filesystem_Type {

  char    *name;                                           /* Name of the filesystem */
  w_void    (*read_superblock) (vfs_superblock superblock);  /* The function to read in the superblock */
  struct vfs_Filesystem_Type  *list_next;                  /* Next filesystem type */
  
} vfs_Filesystem_Type;

typedef vfs_Filesystem_Type *vfs_filesystem_type;

/* The directory entries */

typedef struct vfs_Dir_Entry {

  struct vfs_Dir_Entry  *parent;        /* Points to the parent (directory) of this directory entry */
  struct vfs_Dir_Entry  *child_list;    /* Points to the first entry in this directory (if it is a directory) */
  struct vfs_Dir_Entry  *next_entry;    /* Points to the next entry in the parent directory */

  w_ubyte               *name;          /* Name of this entry */
  vfs_inode             inode;          /* Inode corresponding with this entry */
  vfs_inode             mount;          /* Inode of the root of the fs mounted on this entry */ 
                                        /* Mostly not used and identical to inode */
                                     
  w_word                total_entries;  /* Used only in the first entry in the tree, used for statistics */
  
} vfs_Dir_Entry;

typedef vfs_Dir_Entry *vfs_dir_entry;

/* The superblock operations */

typedef struct vfs_Super_Operations {

  w_void (*read_inode)(vfs_inode inode);                            /* Read an inode */
  w_void (*write_inode)(vfs_inode inode);                           /* Write an inode */
  w_void (*unlink_inode)(vfs_inode inode, vfs_dir_entry dir_entry); /* Unlink an inode */
  w_void (*write_superblock)(vfs_superblock superblock);            /* Write back the superblock */
  w_void (*close_fs)(vfs_superblock superblock);                    /* Close this filesystem */
  w_void (*flush)(vfs_superblock superblock);                       /* Flush/sync */
  
} vfs_Super_Operations;

typedef vfs_Super_Operations *vfs_super_operations;

/* The inode operations */

typedef struct vfs_Inode_Operations {

  w_word (*create)(vfs_inode inode, vfs_dir_entry dir_entry);
  w_word (*mkdir)(vfs_inode inode, vfs_dir_entry dir_entry);
  w_void (*rmdir)(vfs_inode inode, vfs_dir_entry dir_entry);
  w_void (*rename)(vfs_inode inode1, vfs_dir_entry dir_entry1, vfs_inode inode2, vfs_dir_entry dir_entry2);
  vfs_dir_entry (*lookup)(vfs_inode inode, vfs_dir_entry dir_entry);
  w_void (*get_block)(vfs_inode inode, w_ubyte *buffer, w_word block_nr);
  w_word (*put_block)(vfs_inode inode, w_ubyte *buffer, w_word block_nr);
  w_void (*cleanup)(vfs_inode inode);
  w_void (*truncate)(vfs_inode inode);
  w_void (*read_dir)(vfs_inode inode, w_ubyte *buffer);             /* Read in a directory */

} vfs_Inode_Operations;

typedef vfs_Inode_Operations *vfs_inode_operations;

/* The mount table */

typedef struct vfs_Mount_Table {

  struct vfs_Mount_Table  *list_next;    /* Next entry of the mount table */
  w_ubyte                 *mount_point;  /* Location of the mounted device */
  vfs_superblock          superblock;    /* superblock of the device */
  vfs_filesystem_type     fs;            /* filesystem type of this mount entry */
  
} vfs_Mount_Table;

typedef vfs_Mount_Table *vfs_mount_table;

/* The file structure */

typedef struct vfs_File {
  
  int                    file_desc;     /* File descriptor */
  vfs_dir_entry          dir_entry;     /* Directory entry of this file */
  w_word                 position;      /* position counter */
  w_word                 flags;         /* Flags of this file */
  struct vfs_File        *next;         /* Next file entry */
     
} vfs_File;

typedef vfs_File *vfs_file;

/* Lowlevel dir_entry structure for communication between vfs & lower fs */

typedef struct vfs_Low_Dir_Entry {

  w_word    inode;                      /* Inode pointer to file */
  w_ushort  entry_length;               /* Length of this entry */
  w_ubyte   name_length;                /* Length of the name */
  w_ubyte   file_type;                  /* Type of the file */
  w_ubyte   name[255];                  /* File name (255 should be replaced with a define...) */

} vfs_Low_Dir_Entry;

typedef vfs_Low_Dir_Entry  *vfs_low_dir_entry;

/*-----------------------------------------------------------------------------------------------*/
 
/* The FILE Structure, for C compatibility */

typedef struct vfs_FILE {
  int             file_desc;            /* file descriptor of this stream */
  w_ubyte         *mode;                /* File mode, e.g. r+ */
  w_word          error;                /* Error flags */
  struct vfs_FILE *next;                /* next FILE entry */
} vfs_FILE;

#define VFS_STREAM_EOF     0x01         /* End of file flag */
#define VFS_STREAM_ERROR   0x02         /* Error flag */

/* The dirent structure, for C compatitibility */

typedef struct vfs_dirent {
  w_ubyte         d_name[255];
} vfs_dirent;

/* The DIR structure, for C compatibility */

typedef struct vfs_DIR {
  vfs_dir_entry       head;
  vfs_dir_entry       current;
  struct vfs_DIR      *next;
  struct vfs_dirent   dir_ent;
} vfs_DIR;

/* The stat structure, for C compatibility */

struct vfs_STAT {
  w_word        st_dev;      /* device */
  w_word        st_ino;      /* inode */
  w_word        st_mode;     /* protection */
  w_word        st_size;     /* total size, in bytes */
  w_word        st_blksize;  /* blocksize for filesystem I/O */
  w_long        st_atime;    /* time of last access */
  w_long        st_mtime;    /* time of last modification */
  w_long        st_ctime;    /* time of last change */
};

typedef int vfs_fpos_t;      /* For fgetpos(...) and fsetpos(...) */

/* Flags for the stat->st_mode field */

#define VFS_S_IFMT     0x0004   /* bitmask for the file type bitfields */
#define VFS_S_IFDIR    0x0004   /* directory */ 
#define VFS_S_IFREG    0x0000   /* regular file */

/* Since the vfs supports only one set of read/write flags, all the */
/* following point to the same set of bits */

#define VFS_S_IRWXU    0x0003   /* mask for file owner permissions */
#define VFS_S_IRUSR    0x0001   /* owner has read permission */
#define VFS_S_IWUSR    0x0002   /* owner has write permission */
#define VFS_S_IXUSR    0x0000   /* owner has execute permission */
#define VFS_S_IRWXG    0x0003   /* mask for group permissions */
#define VFS_S_IRGRP    0x0001   /* group has read permission */
#define VFS_S_IWGRP    0x0002   /* group has write permission */
#define VFS_S_IXGRP    0x0000   /* group has execute permission */
#define VFS_S_IRWXO    0x0003   /* mask for permissions for others (not in group) */
#define VFS_S_IROTH    0x0001   /* others have read permission */
#define VFS_S_IWOTH    0x0002   /* others have write permisson */
#define VFS_S_IXOTH    0x0000   /* others have execute permission */

/* Not implemented */

#define VFS_S_IFSOCK   0140000   /* socket */
#define VFS_S_IFLNK    0120000   /* symbolic link */ 
#define VFS_S_IFBLK    0060000   /* block device */ 
#define VFS_S_IFCHR    0020000   /* character device */
#define VFS_S_IFIFO    0010000   /* fifo */
#define VFS_S_ISUID    0004000   /* set UID bit */
#define VFS_S_ISGID    0002000   /* set GID bit (see below) */
#define VFS_S_ISVTX    0001000   /* sticky bit (see below) */ 

/* Macros for mode testing */

#define VFS_S_ISREG(m)   (((m) & VFS_S_IFMT) == VFS_S_IFREG)
#define VFS_S_ISDIR(m)   (((m) & VFS_S_IFMT) == VFS_S_IFDIR)

/* Not implemented */

#define VFS_S_ISCHR(m)   0
#define VFS_S_ISBLK(m)   0
#define VFS_S_ISFIFO(m)  0
#define VFS_S_ISSOCK(m)  0

/* Flags for lseek */

/* #define SEEK_SET     1
 * #define SEEK_CUR     2
 * #define SEEK_END     3
 */

/* Prototypes */

extern w_void init_vfs(void);
extern w_void close_vfs(void);

extern w_void vfs_register_filesystem(const vfs_filesystem_type type);
extern w_void vfs_unregister_filesystem(vfs_filesystem_type type);
extern vfs_filesystem_type vfs_get_filesystem_type(const w_ubyte *name);

extern w_void vfs_flush_dir_entry(vfs_dir_entry dir_entry);
extern w_void vfs_cleanup_dir_entry(vfs_dir_entry dir_entry);
extern vfs_dir_entry vfs_lookup_entry(vfs_dir_entry parent, const char *name);
extern vfs_dir_entry vfs_lookup_fullpath(const vfs_dir_entry root_entry, const char *fullpath);

/* Lists and trees to keep track of open files, known filesystems, mounts, ... */

extern vfs_filesystem_type  filesystem_list; /* The master list of the known (registered) filesystem types */
extern vfs_dir_entry        dir_entry_list;  /* The master list of the directory entries */
extern vfs_mount_table      mount_table;     /* The mount table */
extern vfs_file             file_list;       /* The master list of open files (with descriptors) */
extern vfs_FILE             *FILE_list;      /* The master list of open streams */
extern vfs_DIR              *DIR_list;       /* The master list of open directories */
extern w_word               global_errno;

extern x_mutex             mutex_vfs;
extern x_mutex             mutex_vfs_lock;
extern x_thread            vfs_lock_thread;
extern w_int               vfs_lock_count;

extern w_void vfs_set_errno(w_word err);
extern w_word vfs_get_errno(void);

static inline void vfs_lock(void) {
  x_mutex_lock(mutex_vfs_lock, x_eternal);
  if(vfs_lock_thread != x_thread_current()) {
    x_mutex_unlock(mutex_vfs_lock);
    x_mutex_lock(mutex_vfs, x_eternal);
    x_mutex_lock(mutex_vfs_lock, x_eternal);
    vfs_lock_thread = x_thread_current();
  } 
  vfs_lock_count++;
  x_mutex_unlock(mutex_vfs_lock);
}

static inline void vfs_unlock(void) {
  x_mutex_lock(mutex_vfs_lock, x_eternal);
  vfs_lock_count--; 
  if(vfs_lock_count == 0) {
    vfs_lock_thread = NULL;
    x_mutex_unlock(mutex_vfs);
  }
  x_mutex_unlock(mutex_vfs_lock);
}

/* --------------------------------------------------------------------------------------------------------
 *
 * Here are the highlevel functions, which mimic the normal C functions
 * 
 * --------------------------------------------------------------------------------------------------------*/

w_word vfs_mount(const w_ubyte *device, const w_ubyte *dir, const w_ubyte *fs_name, w_word flags);

int vfs_open(const w_ubyte *filename, const w_word flags, const w_word mode);
int vfs_creat(const w_ubyte *pathname, w_word mode);
int vfs_close(const int file_desc);
int vfs_read(const int file_desc, w_void *buffer, const w_word count);
int vfs_write(const int file_desc, const w_void *buffer, const w_word count);
long vfs_lseek(const int file_desc, const long offset, const w_word whence);
w_word vfs_rename(const w_ubyte *oldpath, const w_ubyte *newpath);

vfs_DIR *vfs_opendir(const w_ubyte *name);
w_word vfs_closedir(vfs_DIR *dir);
vfs_dirent *vfs_readdir(vfs_DIR *dir);
w_void vfs_rewinddir(vfs_DIR *dir);
w_word vfs_telldir(vfs_DIR *dir);
w_void vfs_seekdir(vfs_DIR *dir, w_word offset);
w_word vfs_alphasort(struct vfs_dirent **a, struct vfs_dirent **b);

//w_word vfs_scandir(const w_ubyte *dir, struct vfs_dirent ***namelist,
//    w_word (*select)(const struct vfs_dirent *),
//    w_word (*compar)(const struct vfs_dirent **, const struct vfs_dirent **));

int vfs_stat(const w_ubyte *filename, struct vfs_STAT *buf);
w_word vfs_fstat(int file_desc, struct vfs_STAT *buf);

vfs_FILE *vfs_fopen(const w_ubyte *path, const w_ubyte *mode);
vfs_FILE *vfs_fdopen(int fildes, const w_ubyte *mode);
w_word vfs_fclose(vfs_FILE *stream);
int vfs_fseek(vfs_FILE *stream, long offset, w_word whence);
w_word vfs_ftell(vfs_FILE *stream);
w_word vfs_rewind(vfs_FILE *stream);
w_word vfs_fgetpos(vfs_FILE *stream, vfs_fpos_t *pos);
w_word vfs_fsetpos(vfs_FILE *stream, vfs_fpos_t *pos);

w_word vfs_fread(w_void *ptr, w_word size, w_word nmemb, vfs_FILE *stream); 
w_word vfs_fwrite(const w_void *ptr, w_word size, w_word nmemb, vfs_FILE *stream); 

w_word vfs_feof(vfs_FILE *stream);
w_word vfs_ferror(vfs_FILE *stream);
w_void vfs_clearerr(vfs_FILE *stream);

w_word vfs_fgetc(vfs_FILE *stream);
w_word vfs_getc(vfs_FILE *stream);
w_ubyte *vfs_fgets(w_ubyte *s, w_word size, vfs_FILE *stream);
w_word vfs_fputc(w_int c, vfs_FILE *stream);
w_word vfs_fputs(const w_ubyte *s, vfs_FILE *stream);
w_word vfs_putc(w_word c, vfs_FILE *stream);

w_word vfs_fflush(vfs_FILE *stream);

w_word vfs_mkdir(const w_ubyte *pathname, w_word mode);
w_word vfs_rmdir(const w_ubyte *pathname);
w_word vfs_unlink(const w_ubyte *pathname);
w_word vfs_chmod(const w_ubyte *path, w_word mode);
w_word vfs_fchmod(int file_desc, w_word mode);


char *strsep(char **stringp, const char *delim);

void printdir(char *dir, int depth);

#include <e2fs_prototypes.h>

#endif
