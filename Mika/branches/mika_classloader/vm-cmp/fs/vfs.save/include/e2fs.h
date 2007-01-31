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
**************************************************************************/



#ifndef E2FS_INCLUDE
#define E2FS_INCLUDE 

#include <stdio.h>
#include <string.h>
#include "wonka.h"
#include "oswald.h"
#include "driver_blockrandom.h"

/* Superblock */

#define E2FS_SUPERBLOCK_OFFSET  1024    /* Location of the superblock */
#define E2FS_SUPERBLOCK_SIZE    1024    /* Size of the superblock */
#define E2FS_SUPERBLOCK_MAGIC   0xef53  /* Magic number of the superblock */

/* Block & fragment size */

#define E2FS_BLOCK_SIZE_MIN     1024    /* Minimum block size */
#define E2FS_BLOCK_SIZE_MAX     4096    /* Maximum block size */
#define E2FS_FRAG_SIZE_MIN      1024    /* Minimum fragment size */
#define E2FS_FRAG_SIZE_MAX      4096    /* Maximum fragment size */

/* OS Identification */

#define E2FS_OS_LINUX           0       /* Kinda obvious */
#define E2FS_OS_HURD            1
#define E2FS_OS_MASIX           2
#define E2FS_OS_FREEBSD         3
#define E2FS_OS_LITES           4

/* Error behaviour */

#define E2FS_ERROR_CONTINUE     1       /* Don't mind */
#define E2FS_ERROR_RO           2       /* Remount read-only */
#define E2FS_ERROR_PANIC        3       /* Panic */

/* Feature sets */

#define E2FS_FC_DIR_PREALLOC    0x0001
#define E2FS_FC_IMAGIC_INODES   0x0002
#define E2FS_FC_JOURNAL         0x0004

#define E2FS_FROC_SPARSE_SUPER  0x0001
#define E2FS_FROC_LARGE_FILE    0x0002
#define E2FS_FROC_BTREE_DIR     0x0004

#define E2FS_FIC_COMPRESSION    0x0001
#define E2FS_FIC_FILETYPE       0x0002
#define E2FS_FIC_RECOVER        0x0004

/* Special inodes */

#define E2FS_BAD_INODE          1       /* Bad blocks inode */
#define E2FS_ROOT_INODE         2       /* Root directory inode */
#define E2FS_ACL_IDX_INODE      3       /* ACL */
#define E2FS_ACL_DATA_INODE     4       /* ACL */
#define E2FS_BOOT_LOADER_INO    5       /* the bootloader */
#define E2FS_UNDEL_INODE        6       /* Undelete directory inode */

/* Inode block pointers */

#define E2FS_IB_NDIR            12      /* Number of direct block pointers */
#define E2FS_IB_IND_BLOCK       12      /* Indirect blocklist pointer */
#define E2FS_IB_DIND_BLOCK      13      /* Double indirect blocklist pointer */
#define E2FS_IB_TIND_BLOCK      14      /* Triple indirect blocklist pointer */
#define E2FS_IB_N_BLOCKS        15      /* Number of block pointers */

/* Inode flags */

#define E2FS_IF_SECURE_DEL      0x00000001      /* Secure deletion */
#define E2FS_IF_UNDELETE        0x00000002      /* Undelete */
#define E2FS_IF_COMPRESSION     0x00000004      /* Compressed file */
#define E2FS_IF_SYNC            0x00000008      /* Updates are synchrone */
#define E2FS_IF_IMMUTABLE       0x00000010      /* File is immutable (not allowed to change) */
#define E2FS_IF_APPEND          0x00000020      /* File is append onky */
#define E2FS_IF_NO_DUMP         0x00000040      /* Do not dump file */
#define E2FS_IF_NO_ATIME        0x00000080      /* Don't update the access time */
#define E2FS_IF_DIRTY           0x00000100      /* Compression: */
#define E2FS_IF_COMPRBLK        0x00000200      /* Compression: one or more compressed clusters */
#define E2FS_IF_NO_COMPR        0x00000400      /* Compression: access raw compressed data */
#define E2FS_IF_COMPR_ERROR     0x00000800      /* Compression: error */
#define E2FS_IF_BTREE           0x00001000      /* Binary tree format for dirs */
#define E2FS_IF_IMAGIC          0x00002000      /* ??? */

/* File stuff */

#define E2FS_FILE_NAME_LEN    255       /* Maximum length of a filename */

#define E2FS_FILE_UNKNOWN       0       /* Who knows... */
#define E2FS_FILE_REGULAR       1       /* Regular file */
#define E2FS_FILE_DIR           2       /* Directory */
#define E2FS_FILE_CHARDEV       3       /* Character device */
#define E2FS_FILE_BLOCKDEV      4       /* Block device */
#define E2FS_FILE_FIFO          5       /* FIFO */
#define E2FS_FILE_SOCKET        6       /* Socket */
#define E2FS_FILE_SYMLINK       7       /* Symbolic link */

/* File flags */
                                       
#define E2FS_FF_R               0x0100
#define E2FS_FF_W               0x0080
#define E2FS_FF_DIR             0x4000
#define E2FS_FF_REG             0x8000

/* File flags for inode creation */

#define E2FS_FC_R               0x0124
#define E2FS_FC_W               0x0092
#define E2FS_FC_DIR             0x4049

/* structure of the e2fs superblock */

typedef struct e2fs_Superblock {

  w_word    inodes_count;            /* Number of inodes */
  w_word    blocks_count;            /* Number of blocks */
  w_word    reserved_blocks_count;   /* Number of blocks reserved for superuser */
  w_word    free_blocks_count;       /* Number of free blocks */
  w_word    free_inodes_count;       /* Number of free inodes */
  w_word    first_data_block;        /* Location of first data block */
  w_word    block_size;              /* Size of the blocks */
  w_word    fragment_size;           /* Size of the fragments */
  w_word    blocks_per_group;        /* Number of blocks in a group */
  w_word    frags_per_group;         /* Number of fragments in a group */
  w_word    inodes_per_group;        /* Number of inodes in a  group */
  w_word    mount_time;              /* Last mount time */
  w_word    write_time;              /* Last write time */
  w_ushort  mount_count;             /* Counter for mounts */
  w_ushort  max_mount_count;         /* Max mounts for a fs check */
  w_ushort  magic;                   /* Magic number */
  w_ushort  state;                   /* File system state */
  w_ushort  error_behaviour;         /* What to do if an error is found */
  w_ushort  minor_rev_level;         /* Minor revision level */
  w_word    last_check;              /* Time of last fs check */
  w_word    check_interval;          /* Maximum time between checks */
  w_word    creator_os;              /* Os identification */
  w_word    major_rev_level;         /* Major revision level */
  w_ushort  default_uid_reserved;    /* Default uid for reserved blocks */
  w_ushort  default_gid_reserved;    /* Default gid for reserved blocks */

  w_word    first_inode;             /* Pointer to first non-reserved inode */
  w_ushort  inode_size;              /* Size of an inode */
  w_ushort  block_group_nr;          /* Block group number of this superblock */
  w_word    feat_compat_set;         /* Compatible feature set */
  w_word    feat_incompat_set;       /* Incompatible feature set */
  w_word    feat_ro_compat_set;      /* Compatible feature set for read-only */
  w_ubyte   uuid[16];                /* 128 bit id for this fs */
  w_ubyte   volume_label[16];        /* Volume label */
  w_ubyte   last_mounted[64];        /* Last mount point of this fs */
  w_word    algo_usage_bitmap;       /* For compression */

  /* Preallocation */

  w_ubyte   prealloc_blocks;         /* Nr of blocks to try to preallocate */
  w_ubyte   prealloc_dir_blocks;     /* Nr of blocks to try to preallocate for dirs */
  w_ushort  padding1;

  /* Journaling */

  w_ubyte    journal_uuid[16];        /* id of journal superblock */
  w_word    journal_inum;            /* inode number of the journal file */
  w_word    journal_dev;             /* device number of the journal file */
  w_word    last_orphan;             /* start of list of inodes to delete */
  w_word    reserved[197];           /* fill to the end of the block */
} e2fs_Superblock;

typedef e2fs_Superblock  *e2fs_superblock;

/* Structure of an inode */

typedef struct e2fs_Inode {

  w_ushort  mode;              /* File mode */
  w_ushort  uid;               /* Low 16 bits of owner uid */
  w_word    size;              /* Size in bytes */
  w_word    atime;             /* Last access time */
  w_word    ctime;             /* Creation time */
  w_word    mtime;             /* Modification time */
  w_word    dtime;             /* Deletion time */
  w_ushort  gid;               /* Low 16 bits of owner gid */
  w_ushort  links_count;       /* Links pointing to this inode */
  w_word    blocks_count;      /* Number of blocks used */
  w_word    flags;             /* File flags */

  union {
    struct { w_word reserved1; } linux1;
    struct { w_word translator; } hurd1;
    struct { w_word reserved1; } masix1;
  } os_dep1;                   /* OS dependent */

  w_word    blocks[E2FS_IB_N_BLOCKS];  /* Pointer to blocks */
  w_word    generation;                /* File version (nfs) */
  w_word    file_acl;                  /* ACL */
  w_word    dir_acl;                   /* ACL */
  w_word    frag_address;              /* Fragment address */

  union {
    struct {
      w_ubyte     frag_nr;       /* Fragment number */
      w_ubyte     frag_size;     /* Fragment size */
      w_ushort    pad1;          /* Unused */
      w_ushort    uid_high;      /* High 16 bits of owner uid */
      w_ushort    gid_high;      /* High 16 bits of owner gid */
      w_word      reserved2;     /* Unused */
    } linux2;
    struct {
      w_ubyte     frag_nr;       /* Fragment number */
      w_ubyte     frag_size;     /* Fragment size */
      w_ushort    mode_high;     /* High bits of mode */
      w_ushort    uid_high;      /* High 16 bits of owner uid */
      w_ushort    gid_high;      /* High 16 bits of owner gid */
      w_word      author;        /* author id */
    } hurd2;
    struct {
      w_ubyte     frag_nr;       /* Fragment number */
      w_ubyte     frag_size;     /* Fragment size */
      w_ushort    pad1;          /* Unused */
      w_word      reserved2[2];  /* Unused */
    } masix2;
  } os_dep2;                   /* OS dependent stuff */

} e2fs_Inode;

typedef e2fs_Inode  *e2fs_inode;

/* Structure of a  directory entry */

typedef struct e2fs_Dir_Entry {

  w_word    inode;                      /* Inode pointer to file */
  w_ushort  entry_length;               /* Length of this entry */
  w_ubyte   name_length;                /* Length of the name */
  w_ubyte   file_type;                  /* Type of the file */
  w_ubyte    name[E2FS_FILE_NAME_LEN];   /* File name */

} e2fs_Dir_Entry;

typedef e2fs_Dir_Entry  *e2fs_dir_entry;

/* Structure of a group descriptor */

typedef struct e2fs_Group_Desc {

  w_word    block_bitmap;       /* Pointer to the blocks bitmap block*/
  w_word    inode_bitmap;       /* Pointer to the inodes bitmap block*/
  w_word    inode_table;        /* Pointer to the inode table block */
  w_ushort  free_blocks;        /* Number of free blocks */
  w_ushort  free_inodes;        /* Number of free inodes */
  w_ushort  used_dirs;          /* Number of used directories */
  w_ushort  pad;                /* Unused */
  w_word    reserved[3];        /* Unused */

} e2fs_Group_Desc;

typedef e2fs_Group_Desc  *e2fs_group_desc;

/* Structure of the inode cache */

typedef struct e2fs_Inode_Cache {
  
  e2fs_inode                inode;            /* The inode itself */
  w_word                    nr;               /* Number of this inode */
  w_word                    *blocks;          /* Pointer to the blocklist */
  w_word                    blocks_count;     /* Number of blocks in the blocklist */
  w_word                    list_size;        /* Number of max blocks in the blocklist */
  w_word                    flags;            /* Flags */
  struct e2fs_Inode_Cache   *next;            /* Pointer to the next entry */
  
} e2fs_Inode_Cache;

typedef e2fs_Inode_Cache  *e2fs_inode_cache;

#define E2FS_INO_DIRTY   0x01
#define E2FS_INO_BDIRTY  0x02

/* Structures which contains extra data needed by e2fs_fsck */

#define E2FS_FSCK_DIR_HASH_SIZE  32

typedef struct e2fs_Fsck_Dir {
  w_word                     inode_nr;
  w_ubyte                    *name;
  struct e2fs_Fsck_Dir       *next;
  struct e2fs_Fsck_Dir       *prev;
} e2fs_Fsck_Dir;

typedef e2fs_Fsck_Dir        *e2fs_fsck_dir;

typedef struct e2fs_Fsck_Dir_Hash {
  w_word                     inode_nr;
  w_ubyte                    connected_up;
  w_ubyte                    connected_down;
  w_ubyte                    loop_test;
  struct e2fs_Fsck_Dir       *dir;
  struct e2fs_Fsck_Dir       *tail;
  struct e2fs_Fsck_Dir_Hash  *next;
  struct e2fs_Fsck_Dir_Hash  *prev;
} e2fs_Fsck_Dir_Hash;

typedef e2fs_Fsck_Dir_Hash   *e2fs_fsck_dir_hash;

typedef struct e2fs_Fsck_Data {

  w_ubyte             *block_bitmap;        /* Bitmap of used blocks (to compare with the one on disk) */
  w_ubyte             *inode_bitmap;        /* Bitmap of used inodes (to compare with the one on disk) */
  w_ubyte             *dir_inode_bitmap;    /* Bitmap of directory inodes */
  w_ubyte             *bad_inode_bitmap;    /* Bitmap of directory inodes */
  w_ubyte             *schizo_block_bitmap; /* Bitmap of blocks in use by multiple inodes */

  e2fs_inode          inode_list;           /* List of all the inodes */

  e2fs_fsck_dir_hash  dirs[E2FS_FSCK_DIR_HASH_SIZE];
                                            /* Hashtable to store the directory structure */
  
} e2fs_Fsck_Data;

typedef e2fs_Fsck_Data  *e2fs_fsck_data;

/* Structure of a file system */

typedef struct e2fs_Filesystem {

  e2fs_superblock  sb;               /* The superblock */

  /* NOTE : These fields are too big ! I need to calculate the maximum bits required */

  w_word           block_size;        /* Block size */
  w_word           block_size_2;      /* Block size as a power of 2 */  
  w_word           fragment_size;     /* Fragment size */
  w_word           group_desc_nr;     /* Number of group descriptors */
  w_word           desc_per_block;    /* Descriptors per block */
  w_word           desc_blocks;       /* Number of descriptor blocks */

  e2fs_group_desc  group_desc;        /* The group descriptors */

  w_ubyte          *block_bitmap;     /* Bitmap of used blocks */
  w_ubyte          *inode_bitmap;     /* Bitmap of used inodes */

  w_word           flags;             /* Flags of the filesystem (Read only, dirty, ...) */

  w_device       disk_device;       /* The underlying device */
  
  w_ubyte          *dev_id;           /* Device ID */

  /* A buffer for just one block. This is used as a common block buffer to reduce */
  /* the massive allocation en dealloction of memory when e.g. traversing through the blocks */
  /* of an inode */
  
  w_ubyte          *buffer_block;     /* Buffer for just one block */
  w_word           buffer_block_nr;   /* Which block is in the buffer */

  e2fs_inode_cache ino_cache;         /* Inode cache */

  w_word           addr_per_block;    /* Addresses per block */
  w_word           addr_per_block_2;  /* Addresses per block as a power of 2 */

  e2fs_fsck_data   fsck;              /* Extra data for e2fs_fsck */
  
} e2fs_Filesystem;

typedef e2fs_Filesystem  *e2fs_filesystem;

#define E2FS_FS_RW        0x01       /* Filesystem is read/write */
#define E2FS_FS_DIRTY     0x02       /* Filesystem is dirty */
#define E2FS_FS_BB_DIRTY  0x04       /* Block bitmap is dirty */
#define E2FS_FS_IB_DIRTY  0x08       /* Inode bitmap is dirty */
#define E2FS_FS_SB_DIRTY  0x10       /* Superblock is dirty */
#define E2FS_FS_GR_DIRTY  0x20       /* Group descriptors are dirty */

typedef w_word           e2fs_block_nr;
typedef w_word           e2fs_inode_nr;

extern x_mutex           mutex_e2fs;
extern x_mutex           mutex_e2fs_lock;
extern x_thread          e2fs_lock_thread;
extern w_int             e2fs_lock_count;

static inline void e2fs_lock(void) {
  x_mutex_lock(mutex_e2fs_lock, x_eternal);
  if(e2fs_lock_thread != x_thread_current()) {
    x_mutex_unlock(mutex_e2fs_lock);
    x_mutex_lock(mutex_e2fs, x_eternal);
    x_mutex_lock(mutex_e2fs_lock, x_eternal);
    e2fs_lock_thread = x_thread_current();
  } 
  e2fs_lock_count++;
  x_mutex_unlock(mutex_e2fs_lock);
}

static inline void e2fs_unlock(void) {
  x_mutex_lock(mutex_e2fs_lock, x_eternal);
  e2fs_lock_count--; 
  if(e2fs_lock_count == 0) {
    e2fs_lock_thread = NULL;
    x_mutex_unlock(mutex_e2fs);
  }
  x_mutex_unlock(mutex_e2fs_lock);
}

#endif

