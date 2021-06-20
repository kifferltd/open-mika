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
#include "mika_threads.h"

#include "vfs.h"
#include "e2fs_prototypes.h"

#include "buffercache.h"

#include <stdlib.h>
#include <stdio.h>
#include "oswald.h"

struct vfs_Filesystem_Type e2fs_filesystem_type = {
  (char *)"e2fs",       /* Name of the filesystem */
  e2fs_read_superblock, /* Read superblock function */
  NULL                  /* Pointer to next filesystemtype */
};

struct vfs_Super_Operations e2fs_super_operations = {
  e2fs_read_inode,      /* Read an inode */
  e2fs_write_inode,     /* Write an inode */
  e2fs_unlink_inode,    /* Unlink an inode */
  e2fs_write_superblock,/* Write back the superblock */
  e2fs_close_filesys,   /* Close the filesystem */
  e2fs_sync             /* Flush/Sync */
};

struct vfs_Inode_Operations e2fs_inode_operations = {
  e2fs_create_inode,    /* Create an inode */
  e2fs_mkdir,           /* Make directory */   
  e2fs_rmdir,           /* Remove directory */
  e2fs_rename,          /* Rename/move */
  e2fs_lookup_inode,    /* Lookup an inode */
  e2fs_get_block,       /* Get a block from the inode */
  e2fs_put_block,       /* Put a block in the inode */
  e2fs_inode_cleanup,   /* Clean up memory usage for this inode */
  e2fs_inode_truncate,  /* Truncate inode -> Release all the blocks used by this inode */
  e2fs_read_dir         /* Read in a directory */
};

x_mutex   mutex_e2fs;
x_mutex   mutex_e2fs_lock;
x_thread  e2fs_lock_thread;
w_int     e2fs_lock_count;

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_read_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word size, w_ubyte *buffer) {
  fs_buffer read_buffer;

  e2fs_lock();
  
  woempa(2, "block %d   size %d\n", phys_block, size);
  
  if(size != fs->block_size) woempa(9, "OOPS: size is a no-no, block %d, size %d\n", phys_block, size);
  
//  ((w_diskDriver)fs->disk_device->driver)->read(fs->disk_device->cb, (phys_block * fs->block_size) / 512, size, buffer); 

  read_buffer = fs_allocate_buffer(phys_block, size, fs->disk_device);
  memcpy(buffer, read_buffer->data, size);
  fs_release_buffer(read_buffer, 0);

  e2fs_unlock();
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_write_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word size, w_ubyte *buffer) {
  fs_buffer write_buffer;

  e2fs_lock();
  
  woempa(2, "block %d   size %d\n", phys_block, size);
  
  if(size != fs->block_size) woempa(9, "OOPS: size is a no-no, block %d, size %d\n", phys_block, size);

//  ((w_diskDriver)fs->disk_device->driver)->write(fs->disk_device->cb, (phys_block * fs->block_size) / 512, size, buffer);
    
  write_buffer = fs_allocate_buffer(phys_block, size, fs->disk_device);
  memcpy(write_buffer->data, buffer, size);
  fs_release_buffer(write_buffer, FS_BUFFER_DIRTY); 

  e2fs_unlock();
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_read_bitmaps(e2fs_filesystem fs) {
  e2fs_block_nr  block;
  w_word         block_bytes;
  w_word         inode_bytes;
  w_word         i;
  w_ubyte        *block_bitmap;
  w_ubyte        *inode_bitmap;

  e2fs_lock();

  woempa(2, "\n");
  
  if(fs->sb->blocks_per_group > fs->sb->blocks_count) 
    block_bytes = (fs->sb->blocks_count + 7) >> 3; 
  else 
    block_bytes = fs->sb->blocks_per_group >> 3;
  
  if(fs->sb->inodes_per_group > fs->sb->inodes_count) 
    inode_bytes = (fs->sb->inodes_count + 7) >> 3; 
  else 
    inode_bytes = fs->sb->inodes_per_group >> 3;

  block_bitmap = allocMem((w_size)(block_bytes * fs->group_desc_nr));
  inode_bitmap = allocMem((w_size)(inode_bytes * fs->group_desc_nr));

  memset(block_bitmap, 0, block_bytes * fs->group_desc_nr);
  memset(inode_bitmap, 0, inode_bytes * fs->group_desc_nr);

  fs->block_bitmap = block_bitmap;
  fs->inode_bitmap = inode_bitmap;

  /* OLA: This wil fail big time if one the bitmaps is more than one block */
  /* I need to figure out if that is possible */
  
  for(i=0; i < fs->group_desc_nr; i++) {
    block = fs->group_desc[i].block_bitmap;
    e2fs_read_block(fs, block, fs->block_size, fs->buffer_block);
    memcpy(block_bitmap, fs->buffer_block, ((i+1) * block_bytes > ((fs->sb->blocks_count + 7) >> 3) ? 
			    ((fs->sb->blocks_count + 7) >> 3) % block_bytes : block_bytes));

    block = fs->group_desc[i].inode_bitmap;
    e2fs_read_block(fs, block, fs->block_size, fs->buffer_block);
    memcpy(inode_bitmap, fs->buffer_block, ((i+1) * inode_bytes > ((fs->sb->inodes_count + 7 ) >> 3) ? 
			    ((fs->sb->inodes_count + 7) >> 3) % inode_bytes : inode_bytes));
    
    inode_bitmap += inode_bytes;
    block_bitmap += block_bytes;
   
  } 

  e2fs_unlock();
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_write_bitmaps(e2fs_filesystem fs) {
  e2fs_block_nr  block;
  w_word         block_bytes;
  w_word         inode_bytes;
  w_word         i;
  w_ubyte        *block_bitmap;
  w_ubyte        *inode_bitmap;

  e2fs_lock();

  woempa(2, "\n");

  if(((fs->flags & E2FS_FS_BB_DIRTY) == E2FS_FS_BB_DIRTY) || ((fs->flags & E2FS_FS_IB_DIRTY) == E2FS_FS_IB_DIRTY)) {
  
    if(fs->sb->blocks_per_group > fs->sb->blocks_count) 
       block_bytes = (fs->sb->blocks_count + 7) >> 3; 
    else 
      block_bytes = fs->sb->blocks_per_group >> 3;
    
    if(fs->sb->inodes_per_group > fs->sb->inodes_count) 
      inode_bytes = (fs->sb->inodes_count + 7) >> 3; 
    else 
      inode_bytes = fs->sb->inodes_per_group >> 3;

    block_bitmap = fs->block_bitmap;
    inode_bitmap = fs->inode_bitmap;
  
    for(i=0; i < fs->group_desc_nr; i++) {
      
      if((fs->flags & E2FS_FS_BB_DIRTY) == E2FS_FS_BB_DIRTY) {       /* Is the block bitmap dirty ? */
        
        block = fs->group_desc[i].block_bitmap;
	memset(fs->buffer_block, 0xff, fs->block_size);
        memcpy(fs->buffer_block, block_bitmap, ((i+1) * block_bytes > ((fs->sb->blocks_count + 7) >> 3) ? 
				((fs->sb->blocks_count + 7) >> 3) % block_bytes : block_bytes));
        e2fs_write_block(fs, block, fs->block_size, fs->buffer_block);
        block_bitmap += block_bytes;
      }

      if((fs->flags & E2FS_FS_IB_DIRTY) == E2FS_FS_IB_DIRTY) {       /* Is the inode bitmap dirty ? */
        
        block = fs->group_desc[i].inode_bitmap;
        memset(fs->buffer_block, 0xff, fs->block_size);
        memcpy(fs->buffer_block, inode_bitmap, ((i+1) * inode_bytes > ((fs->sb->inodes_count + 7) >> 3) ? 
				((fs->sb->inodes_count + 7) >> 3) % inode_bytes : inode_bytes));
        e2fs_write_block(fs, block, fs->block_size, fs->buffer_block);        
        inode_bitmap += inode_bytes;
      }
      
    }

    fs->flags &= ~E2FS_FS_BB_DIRTY;   /* Clear the block bitmap dirty flag */
    fs->flags &= ~E2FS_FS_IB_DIRTY;   /* Clear the inode bitmap dirty flag */
  }

  e2fs_unlock();
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_bitmap_set_inode(e2fs_filesystem fs, e2fs_inode_nr inode, w_word bit) {

  w_word position = (inode - 1) / 8;
  w_word offset = (inode - 1) % 8;

  e2fs_lock();

  fs->inode_bitmap[position] = (fs->inode_bitmap[position] & ~(1 << offset)) | (bit << offset);
  fs->group_desc[(inode - 1) / fs->sb->inodes_per_group].free_inodes += (bit == 0 ? 1 : -1);
  fs->sb->free_inodes_count += (bit == 0 ? 1 : -1);

  fs->flags |= E2FS_FS_IB_DIRTY;   /* Mark the inode bitmap as dirty */
  fs->flags |= E2FS_FS_SB_DIRTY;   /* Mark the superblock as dirty */
  fs->flags |= E2FS_FS_GR_DIRTY;   /* Mark the group descriptors as dirty */

  e2fs_unlock();
  
}
  
/* ------------------------------------------------------------------------------------------------------- */

w_ubyte e2fs_bitmap_test_inode(e2fs_filesystem fs, e2fs_inode_nr inode) {

  w_word position = (inode - 1) / 8;
  w_word offset = (inode - 1) % 8;
  w_ubyte result;

  e2fs_lock();

  result = ((fs->inode_bitmap[position] & (1 << offset)) == (1 << offset));

  e2fs_unlock();

  return result;
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_bitmap_set_block(e2fs_filesystem fs, e2fs_block_nr block, w_word bit) {

  w_word position = (block - 1) / 8;
  w_word offset = (block - 1) % 8;

  e2fs_lock();

  fs->block_bitmap[position] = (fs->block_bitmap[position] & ~(1 << offset)) | (bit << offset);
  fs->group_desc[(block - 1 ) / fs->sb->blocks_per_group].free_blocks += (bit == 0 ? 1 : -1); 
  fs->sb->free_blocks_count += (bit == 0 ? 1 : -1);

  fs->flags |= E2FS_FS_BB_DIRTY;   /* Mark the block bitmap as dirty */
  fs->flags |= E2FS_FS_SB_DIRTY;   /* Mark the superblock as dirty */
  fs->flags |= E2FS_FS_GR_DIRTY;   /* Mark the group descriptors as dirty */

  e2fs_unlock();

}
  
/* ------------------------------------------------------------------------------------------------------- */

w_ubyte e2fs_bitmap_test_block(e2fs_filesystem fs, e2fs_block_nr block) {

  w_word position = (block - 1)/ 8;
  w_word offset = (block - 1) % 8;
  w_ubyte result;

  e2fs_lock();

  result = ((fs->block_bitmap[position] & (1 << offset)) == (1 << offset));

  e2fs_unlock();

  return result;
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_show_fs(e2fs_filesystem fs) {
  w_ubyte         uuid[39];
  w_int           i;
  
  memset((w_void *)&uuid, 0, sizeof(uuid));

  for(i=0; i<16; i++) {
    if(i == 4 || i == 6 || i == 8 || i == 10) sprintf(uuid, "%s-", (char *)&uuid);
    sprintf(uuid, "%s%02x", (char *)&uuid, fs->sb->uuid[i]);
  }

  woempa(7,"--- e2fs info ---------------------------------------------\n");
  
  woempa(7, "label: %s  UUID: %s\n", fs->sb->volume_label, (char *)&uuid);
  woempa(7, "inodes: %d (%d free)  blocks: %d (%d free)  (%s)\n", 
      fs->sb->inodes_count, fs->sb->free_inodes_count, fs->sb->blocks_count, fs->sb->free_blocks_count,
      (fs->flags && E2FS_FS_RW == E2FS_FS_RW ? "RW" : "RO"));
  woempa(7, "blocksize: %d, fragsize: %d, inosize: %d\n", 
         fs->block_size, fs->fragment_size, fs->sb->inode_size);
  woempa(7, "%d blocks, %d frags and %d inos per group\n", fs->sb->blocks_per_group, 
                                                           fs->sb->frags_per_group, 
                                                           fs->sb->inodes_per_group);
  woempa(7, "groupdesc: %d, descblocks: %d\n", fs->group_desc_nr, fs->desc_blocks); 

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_open_filesys(e2fs_filesystem fs) {

  w_word   i;
  w_word   group_block;
  w_ubyte  *pointer;

  e2fs_lock();
  
  /* open the device */

  woempa(7,"Opening device %s\n",fs->dev_id);
  
  fs->disk_device = deviceBROpen(fs->dev_id, wdp_read_write);

  /* Allocate memory for the superblock */

  fs->sb = allocMem(sizeof(e2fs_Superblock));

  deviceBRRead(fs->disk_device, (w_ubyte *)fs->sb, E2FS_SUPERBLOCK_OFFSET / 512, E2FS_SUPERBLOCK_SIZE, x_eternal); 
                                                                    /* Read in the superblock */
  /* Do some math */

  fs->block_size = E2FS_BLOCK_SIZE_MIN << fs->sb->block_size;       /* Get the blocksize */
  fs->fragment_size = E2FS_FRAG_SIZE_MIN << fs->sb->fragment_size;  /* Get the fragment size */

  fs->group_desc_nr = (fs->sb->blocks_count                         /* Calculate the number of */
                       - fs->sb->first_data_block                   /* group descriptors */
                       + fs->sb->blocks_per_group - 1) / 
                       fs->sb->blocks_per_group;

  fs->desc_per_block = fs->block_size /                             /* How many group descriptors fit */
                       sizeof(e2fs_Group_Desc);                     /* in one block */

  fs->desc_blocks = (fs->group_desc_nr + fs->desc_per_block - 1)    /* Calculate the number of */
                     / fs->desc_per_block;                          /* group descriptor blocks */

  group_block = fs->sb->first_data_block + 1;                       /* Pointer to the group block */

  /* Allocate some memory for the group descriptors */

  fs->group_desc = allocMem((w_size)(fs->desc_blocks * fs->block_size));

  /* Read in the group descriptors */

  pointer = (w_ubyte *)fs->group_desc;                              /* Start of the group desc. */
  for(i=0; i < fs->desc_blocks; i++) {
    deviceBRRead(fs->disk_device, (w_ubyte *)pointer, (w_int)((group_block * fs->block_size) / 512), (w_int)fs->block_size, x_eternal); 
                                                                    /* Read from device */
    group_block++;                                                  /* Next group block */
    pointer += fs->block_size;                                      /* Point to the next block */
  }

  fs->buffer_block = allocMem((w_size)fs->block_size);            
                                                                    /* Allocate memory for the buffer */

  if(fs->flags && E2FS_FS_RW == E2FS_FS_RW) {                       /* Read/Write access -> Read in the bitmaps */
    e2fs_read_bitmaps(fs);                                          /* Read in used inodes/blocks bitmap */ 
  } else {
    fs->inode_bitmap = NULL;
    fs->block_bitmap = NULL;
  }
  
  fs->sb->mount_count++;                                            /* Increment mount count */

  for(i=1; (fs->block_size >> i) != 1; i++);                        /* Get the log 2 of block_size */
  fs->block_size_2 = i;

  fs->addr_per_block = fs->block_size >> 2;                         /* block_size / 4 */

  for(i=1; (fs->addr_per_block >> i) != 1; i++);                    /* Get the log 2 of addr_per_block */
  fs->addr_per_block_2 = i;

/* #if(7 >= DEBUG_LEVEL) */
  e2fs_show_fs(fs);
/* #endif */

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_sync(vfs_superblock sb) {
  e2fs_filesystem   fs;
  w_word            group_block;
  e2fs_inode_cache  ino_cache;

  e2fs_lock();

  fs = sb->sb.e2fs;
  ino_cache = fs->ino_cache;

  woempa(3, "\n");

  if(fs->flags && E2FS_FS_RW == E2FS_FS_RW) { /* Filesystem is read/write */

    if(((fs->flags & E2FS_FS_BB_DIRTY) == E2FS_FS_BB_DIRTY) || ((fs->flags & E2FS_FS_IB_DIRTY) == E2FS_FS_IB_DIRTY)) {
      woempa(1, "Write bitmaps\n");
      e2fs_write_bitmaps(fs);
    }

    if((fs->flags & E2FS_FS_SB_DIRTY) == E2FS_FS_SB_DIRTY) {
      woempa(1, "Write superblock\n");
      e2fs_write_block(fs, 1, sizeof(e2fs_Superblock), (w_ubyte *)fs->sb);
    }
    
    woempa(1, "%d free inodes, %d free blocks\n", 
      fs->group_desc[0].free_inodes,
      fs->group_desc[0].free_blocks);
      
    
    
    if((fs->flags & E2FS_FS_GR_DIRTY) == E2FS_FS_GR_DIRTY) {
      woempa(1, "Write group descriptors\n");

      group_block = fs->sb->first_data_block + 1;                       /* Pointer to the group block */
      e2fs_write_block(fs, group_block, fs->block_size * fs->desc_blocks, (w_ubyte *)fs->group_desc);

    }

    woempa(1, "Flushing the inode cache\n");
    
    while(ino_cache != NULL) {
      e2fs_write_ino_cache(fs, ino_cache);
      ino_cache = ino_cache->next;
    }
      
  }

  fs_write_all_buffers();

  e2fs_unlock();
 
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_close_filesys(vfs_superblock sb) {

  e2fs_filesystem   fs;

  e2fs_lock();

  fs = sb->sb.e2fs;

  e2fs_sync(sb);

  if(fs->inode_bitmap != NULL) releaseMem(fs->inode_bitmap);
  if(fs->block_bitmap != NULL) releaseMem(fs->block_bitmap);

  deviceBRClose(fs->disk_device);
  
  releaseMem(fs->group_desc);
  releaseMem(fs->sb);
  releaseMem(fs->buffer_block);
  releaseMem(fs);

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_write_superblock(vfs_superblock superblock) {

  e2fs_lock();

  woempa(2, "\n");

  e2fs_write_block(superblock->sb.e2fs, 1, sizeof(e2fs_Superblock), (w_ubyte *)superblock->sb.e2fs->sb);
  
  superblock->sb.e2fs->flags &= ~E2FS_FS_SB_DIRTY;  /* Superblock is no longer dirty */

  e2fs_unlock();
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_read_superblock(vfs_superblock superblock) {

  e2fs_filesystem fs = allocMem(sizeof(e2fs_Filesystem));
  memset(fs, 0, sizeof(e2fs_Filesystem));

  e2fs_lock();

  woempa(2, "\n");
  superblock->type = &e2fs_filesystem_type;
  superblock->super_ops = &e2fs_super_operations;

  fs->dev_id = superblock->device;
  fs->flags  = superblock->flags;
  
  superblock->sb.e2fs = fs;
  woempa(7,"Allocated e2fs_filesystem @ %p, time to open it\n",fs);
  e2fs_open_filesys(fs);
  superblock->block_size = fs->block_size;
  superblock->root_inode_nr = E2FS_ROOT_INODE;

  e2fs_fsck(fs);

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void init_e2fs() {
  woempa(2, "\n");

  mutex_e2fs  = allocMem(sizeof(x_Mutex));
  mutex_e2fs_lock  = allocMem(sizeof(x_Mutex));

  x_mutex_create(mutex_e2fs);
  x_mutex_create(mutex_e2fs_lock);
 
  vfs_register_filesystem(&e2fs_filesystem_type);
}

