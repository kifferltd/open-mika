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
#include "oswald.h"

#include "vfs.h"
#include "e2fs_prototypes.h"
#include <stdlib.h>
#include "vfs_errno.h"

/* ------------------------------------------------------------------------------------------------------- */

static inline e2fs_inode_cache e2fs_search_inode_cache(e2fs_filesystem fs, e2fs_inode_nr inode_nr) {

  e2fs_inode_cache  cache;
  e2fs_inode_cache  result = NULL;

  cache = fs->ino_cache;
  while((cache != NULL) && (cache->nr != inode_nr)) cache = cache->next;
  if((cache != NULL) && (cache->nr == inode_nr)) result = cache;

  return result;
}


/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_read_inode_from_fs(e2fs_filesystem fs, w_word inode_nr, e2fs_inode e2fs_ino) {

  e2fs_block_nr     inode_block, block;
  w_long            group, offset;

  group = (inode_nr - 1) / fs->sb->inodes_per_group;            /* Group which contains the inode */
  offset = ((inode_nr - 1) % fs->sb->inodes_per_group) * fs->sb->inode_size;
                                                                   /* Location in the group */
  inode_block = offset / fs->block_size;                         /* block in the inode table */
  offset = offset % fs->block_size;                              /* offset inside the block */
  block = fs->group_desc[group].inode_table + inode_block;       /* block on device */

  woempa(2, "inode %d, group %d, offset %d, inode_block %d, block %d\n", 
         inode_nr, (w_word)group, (w_word)offset, inode_block, block);

  e2fs_read_block(fs, block, fs->block_size, fs->buffer_block);       /* Read a block into the buffer */
  
  memcpy(e2fs_ino, (fs->buffer_block + offset), sizeof(e2fs_Inode));  /* Copy the data to the inode struct */

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_read_inode(vfs_inode inode) {

  e2fs_filesystem   fs;
  e2fs_inode        e2fs_ino;  
  e2fs_inode_cache  ino_cache;

  e2fs_lock();

  fs = inode->sb->sb.e2fs;       /* Get a pointer to an e2fs filesystem structure out of the inode */

  ino_cache = e2fs_search_inode_cache(fs, inode->nr);

  if(ino_cache == NULL) {        /* Inode is not in the cache */
  
    e2fs_ino = allocMem(sizeof(e2fs_Inode));           /* Allocate memory to store the e2fs inode */
    e2fs_read_inode_from_fs(fs, inode->nr, e2fs_ino);         /* Read the inode from the filesystem */

    ino_cache = allocMem(sizeof(e2fs_Inode_Cache));    /* Allocate memory for a new inode cache entry */
    memset(ino_cache, 0, sizeof(e2fs_Inode_Cache));
    
    ino_cache->inode = e2fs_ino;       /* Add the necessary data to the inode_cache */
    ino_cache->nr = inode->nr;
    ino_cache->next = fs->ino_cache;
    fs->ino_cache = ino_cache;
    
  } else {
    woempa(2, "inode %d is in cache\n", inode->nr);
    e2fs_ino = ino_cache->inode;
    
  }
  
  inode->inode.e2fs = e2fs_ino;                                       /* Put the e2fs inode into the vfs inode */
  inode->size = e2fs_ino->size;

  inode->flags = 0;
  if((e2fs_ino->mode & E2FS_FF_R) == E2FS_FF_R) inode->flags |= VFS_FF_R;
  if((e2fs_ino->mode & E2FS_FF_W) == E2FS_FF_W) inode->flags |= VFS_FF_W;
  if((e2fs_ino->mode & E2FS_FF_DIR) == E2FS_FF_DIR) inode->flags |= VFS_FF_DIR;

  inode->inode_ops = &e2fs_inode_operations;

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_word e2fs_find_new_inode(e2fs_filesystem fs) {
                                   /* Find a new inode. OLA: This is not the way this should be done */
                                   /* We need a target to start from. The rules :  */
                                   /* 1.   Next inode free ?      */
                                   /* 2.   8 free inode nearby   */
                                   /* 3.   Any inode nearby       */
                                   /* 4.   8 free inodes far away */
                                   /* 5.   Any inode far away     */
                                  
  w_word i;

  e2fs_lock();

  i = fs->sb->first_inode;

  while(e2fs_bitmap_test_inode(fs, i) == 1) {
    i++;
  }
  
  e2fs_bitmap_set_inode(fs, i, 1);

  woempa(2, "inode: %d\n", i);

  e2fs_unlock();
  
  return i;
  
}
    
/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_write_ino_cache(e2fs_filesystem fs, e2fs_inode_cache ino_cache) {
  
  e2fs_block_nr     inode_block, block;
  w_long            group, offset;

  e2fs_lock();

  /* Write the block list first, because it might change the block pointers in the inode */

  if((ino_cache != NULL) && 
     ((ino_cache->flags & E2FS_INO_BDIRTY) == E2FS_INO_BDIRTY)) {
   
    e2fs_inode_write_block_list(fs, ino_cache);
    ino_cache->flags &= ~E2FS_INO_BDIRTY;       /* Clear the dirty flag */

  } else {
    woempa(2, "inode %d block list is not dirty\n", ino_cache->nr);
  }

  /* Write the inode */
  
  if((ino_cache != NULL) && 
     ((ino_cache->flags & E2FS_INO_DIRTY) == E2FS_INO_DIRTY)) {

    group = (ino_cache->nr - 1) / fs->sb->inodes_per_group;        /* Group which contains the inode */
    offset = ((ino_cache->nr - 1) % fs->sb->inodes_per_group) * fs->sb->inode_size;
                                                                   /* Location in the group */
    inode_block = offset / fs->block_size;                         /* block in the inode table */
    offset = offset % fs->block_size;                              /* offset inside the block */
    block = fs->group_desc[group].inode_table + inode_block;       /* block on device */

    woempa(2, "inode %d, group %d, offset %d, inode_block %d, block %d\n", 
           ino_cache->nr, (w_word)group, (w_word)offset, inode_block, block);
    woempa(3, "inode %d, size %d, blocks %d\n", ino_cache->nr, ino_cache->inode->size, ino_cache->inode->blocks_count);

    e2fs_read_block(fs, block, fs->block_size, fs->buffer_block);                /* Read a block into the buffer */

    memcpy((fs->buffer_block + offset), ino_cache->inode, sizeof(e2fs_Inode));   /* Copy the data from the inode struct */
    
    e2fs_write_block(fs, block, fs->block_size, fs->buffer_block);               /* Write the buffer back to the device */

    ino_cache->flags &= ~E2FS_INO_DIRTY;         /* Clear the dirty flag */
    
  } else {
    woempa(2, "inode %d is not dirty\n", ino_cache->nr);
  }

   e2fs_unlock();
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_write_inode(vfs_inode inode) {
  
  e2fs_inode_cache  ino_cache;
  w_word            mode = 0;

  e2fs_lock();

  ino_cache = inode->sb->sb.e2fs->ino_cache;

  woempa(3, "inode %d, size %d, e2fs size %d\n", inode->nr, inode->size, inode->inode.e2fs->size);

  while((ino_cache != NULL) && (ino_cache->inode != inode->inode.e2fs)) ino_cache = ino_cache->next;
  
  if(inode->size != inode->inode.e2fs->size) { 
    inode->inode.e2fs->size = inode->size; 
    ino_cache->flags |= E2FS_INO_DIRTY; 
  }

  mode = inode->inode.e2fs->mode & ~(E2FS_FC_R | E2FS_FC_W);
  if((inode->flags & VFS_FF_R) == VFS_FF_R) mode |= E2FS_FC_R;
  if((inode->flags & VFS_FF_W) == VFS_FF_W) mode |= E2FS_FC_W;

  if(mode != inode->inode.e2fs->mode) {
    ino_cache->flags |= E2FS_INO_DIRTY;
    ino_cache->inode->mode = mode;
  }

  e2fs_write_ino_cache(inode->sb->sb.e2fs, ino_cache);

  e2fs_unlock();
  
}

/* ------------------------------------------------------------------------------------------------------- */

vfs_dir_entry e2fs_lookup_inode(vfs_inode inode, vfs_dir_entry dir_entry) {

  w_ubyte         *buffer;
  w_ubyte         *ptr;
  w_word          blocks;
  e2fs_dir_entry  e2fs_dirent;
  vfs_inode       vfs_ino;
  w_word          i;

  e2fs_lock();

  woempa(2, "filename: %s\n", dir_entry->name);
  
  blocks = (inode->size + inode->sb->block_size - 1) / inode->sb->block_size;     /* Nr of blocks */
  
  buffer = allocMem((w_size)(blocks * inode->sb->block_size));   /* Allocate memory for the buffer */
  ptr = buffer;                                        
  
  for(i=0; i<blocks; i++) {               /* Loop through the blocks */
    e2fs_get_block(inode, ptr, i);        /* Load the blocks in the buffer */
    ptr += inode->sb->block_size;         /* Point to the next block */
  }

  ptr = buffer;                           /* Point to the beginning of the block */
  e2fs_dirent = (e2fs_dir_entry)ptr;      /* First entry */

  while(dir_entry->inode == NULL && ptr < buffer + inode->size) { /* Loop through the entries */
    if(strncmp(dir_entry->name, e2fs_dirent->name, strlen(dir_entry->name))==0) {  /* Compare the names */     
      vfs_ino = allocMem(sizeof(vfs_Inode));     /* Allocate memory for the vfs inode */
      memset(vfs_ino, 0, sizeof(vfs_Inode));
      vfs_ino->nr = e2fs_dirent->inode;                           /* Store the inode number in the vfs inode */
      vfs_ino->sb = inode->sb;                                    /* Pointer to the superblock for the new inode */
      e2fs_read_inode(vfs_ino);                                   /* Read the e2fs inode */
      dir_entry->inode = vfs_ino;                                 /* Store the resulting inode */
      dir_entry->parent = NULL;                                   /* Entry has no parent */
      dir_entry->child_list = NULL;                               /* Entry has no childeren */
      dir_entry->next_entry = NULL;                               /* Entry has no next entry */
    }
    ptr += e2fs_dirent->entry_length;                             /* Point to the next entry */
    e2fs_dirent = (e2fs_dir_entry)ptr;
  }

  releaseMem(buffer);

  e2fs_unlock();
   
  return NULL; 
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_inode_cleanup(vfs_inode inode) {
  e2fs_inode_cache  ino_cache;
  e2fs_inode_cache  temp;

  e2fs_lock();

  ino_cache = inode->sb->sb.e2fs->ino_cache;

  woempa(2, "\n");

  e2fs_write_inode(inode);

  if(ino_cache != NULL) {                       /* Is there an inode cache ? */
    if(ino_cache->inode == inode->inode.e2fs) { /* Is the inode the first of the list ? */
      releaseMem(ino_cache->blocks);                  /* Free the blocklist */
      releaseMem(ino_cache->inode);                   /* Free the inode */
      temp = ino_cache;                      
      inode->sb->sb.e2fs->ino_cache = ino_cache->next; 
                                                /* make the list point to the second one */
      releaseMem(temp);                               /* Free the inode cache entry */
    } else {                                               /* Nope, traverse the list */
      while((ino_cache != NULL) && (ino_cache->next != NULL)) {
        if(ino_cache->next->inode == inode->inode.e2fs) {  /* Is the next filesystem type the one to unregister ? */
          releaseMem(ino_cache->next->blocks);                   /* Free the blocklist */
          releaseMem(ino_cache->next->inode);                    /* Free the inode */
          temp = ino_cache->next;
          ino_cache->next = ino_cache->next->next;         /* Remove the entry from the list */
          releaseMem(temp);                                      /* Free the inode cache entry */
        }                                      
        ino_cache = ino_cache->next;                       /* Got to the next entry in the list */
      }
    }
  }

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_inode_truncate(vfs_inode inode) {

  w_word            i;
  e2fs_inode_cache  ino_cache;

  e2fs_lock();

  ino_cache = inode->sb->sb.e2fs->ino_cache;

  woempa(2, "%d\n", inode->nr);

  while((ino_cache != NULL) && (ino_cache->nr != inode->nr)) ino_cache = ino_cache->next;

  if(ino_cache != NULL) {
    if(ino_cache->blocks == NULL) { /* Read in the blocklist if needed */
      ino_cache->blocks = (w_word *)e2fs_inode_read_block_list(inode->sb->sb.e2fs, ino_cache->inode);
      ino_cache->blocks_count = ino_cache->inode->blocks_count / (inode->sb->sb.e2fs->block_size >> 9);
    }
    
    for(i=0; i < ino_cache->blocks_count; i++) {
      e2fs_bitmap_set_block(inode->sb->sb.e2fs, ino_cache->blocks[i], 0); 
      ino_cache->blocks[i] = 0; /* -> bad idea, makes recovering lost inodes impossible */
                                /* But is needed for fsck: It reports that the inode size is wrong */
                                /* And that the inode has still blocks */
    }
  }

  for(i=0; i <= E2FS_IB_NDIR; i++) ino_cache->inode->blocks[i] = 0; /* OLA: Makes undelete impossible */
  
  ino_cache->blocks_count = 0;
  ino_cache->inode->size = 0;
  ino_cache->inode->blocks_count = 0;
  
  ino_cache->flags |= E2FS_INO_BDIRTY;
  ino_cache->flags |= E2FS_INO_DIRTY;

  inode->size = 0;

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_add_dir_entry(e2fs_filesystem fs, e2fs_inode dir, e2fs_inode_nr ino_nr, const w_ubyte *name, w_word type) {
  
  w_ubyte           *buffer;              /* buffer */
  w_ubyte           *ptr, *last;          /* Pointers */
  w_word            i;                  
  w_word            position;             /* Position in the entry_list */
  e2fs_block_nr     block_nr;             
  e2fs_dir_entry    e2fs_dirent;          /* dir_entry structure for reading the list */
  w_word            final_length = 0;
  e2fs_inode_cache  ino_cache;

  e2fs_lock();

  /* OLA: Although this functions works, I'm not too happy how it works. This should be rewritten, */
  /* perhaps buffering whole directories and write them back to the device later. That way I can */
  /* use simple linked lists to represent directories, which are easier to handle */

  woempa(2, "name: %s\n", name);

  position = 0;                                             /* Start from the beginning */
  buffer = allocMem((w_size)(dir->size + fs->block_size));  /* Allocate memory */
  memset(buffer, 0, dir->size + fs->block_size);
  ptr = buffer;                                             /* Pointer to the buffer */
 
  /* Step 1 : Read in the blocks of the directory */
  
  for(i=0; i < dir->size / fs->block_size; i++) { 
    block_nr = e2fs_inode_get_phys_block(fs, dir, 0, i);
    e2fs_read_block(fs, block_nr, fs->block_size, ptr);
    ptr += fs->block_size;
  }

  /* Step 2 : Go over the entries to the last one */

  ptr = buffer;                                    /* Source pointer points to the beginning of the source */
  last = buffer;                                   /* If there are no blocks yet, last = the beginning */
  while(position < dir->size) {                    /* Loop over the entries */
    e2fs_dirent = (e2fs_dir_entry)ptr;             /* Map over a dir_entry structure */
    last = ptr;
    position += e2fs_dirent->entry_length;
    ptr += e2fs_dirent->entry_length;
  }

  e2fs_dirent = (e2fs_dir_entry)last; 

  if(dir->size > 0) {                              /* Skip this if the dir is size 0 */
  
    final_length = e2fs_dirent->entry_length;
    e2fs_dirent->entry_length = ((e2fs_dirent->name_length - 1) / 4) * 4 + 12;

    final_length -= e2fs_dirent->entry_length;

  }
	  
  /* Step 3 : Check if the dir need a new block */ 
  /* If so, the new dir entry has to start at the beginning of the new block, entries are not allowed to */
  /* overlap the border between blocks */
  
  if(final_length < ((strlen(name) - 1) / 4) * 4 + 12) { /* entry_length smaller than 0 -> get a block */
    e2fs_dirent->entry_length += final_length;  /* Make the entry point to the end of the block */
    final_length = fs->block_size;              /* Change the entry length */
    dir->size += fs->block_size;                /* Change the inode dir size */

    ino_cache = fs->ino_cache;
    while((ino_cache != NULL) && (ino_cache->inode != dir)) ino_cache = ino_cache->next;
    e2fs_alloc_block(fs, ino_cache, (dir->size / fs->block_size) - 1, 1);
    
  }
  
  last += e2fs_dirent->entry_length;            /* In case of a new first entry : entry_length = 0, so no problem */
  e2fs_dirent = (e2fs_dir_entry)last;

  e2fs_dirent->inode = ino_nr;
  e2fs_dirent->name_length = strlen(name);
  e2fs_dirent->file_type = type;

  strncpy(e2fs_dirent->name, name, E2FS_FILE_NAME_LEN);

  e2fs_dirent->entry_length = final_length;
  
  /* Step 4 : Write the blocks back to the device */

  ptr = buffer;  
  for(i=0; i < dir->size / fs->block_size; i++) { 
    block_nr = e2fs_inode_get_phys_block(fs, dir, 0, i);
    e2fs_write_block(fs, block_nr, fs->block_size, ptr);
    ptr += fs->block_size;
  }

  releaseMem(buffer);       /* Free memory */

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_delete_dir_entry(e2fs_filesystem fs, e2fs_inode dir, e2fs_inode_nr inode) {
 
  w_ubyte           *buffer;              /* buffer */
  w_ubyte           *buffer2;          
  w_ubyte           *ptr;                 /* Pointers */
  w_ubyte           *ptr2;
  w_word            i;                  
  e2fs_block_nr     block_nr;             
  e2fs_dir_entry    e2fs_dirent;          /* dir_entry structure for reading the list */
  e2fs_dir_entry    last = NULL;
  w_word            position = 0;
  w_word            position2 = 0;
  w_word            position3 = 0;
  w_word            final_length;
  w_word            entry_length;
  w_word            entry_length2;
  e2fs_inode_cache  ino_cache;

  e2fs_lock();

  ino_cache = fs->ino_cache;

  woempa(2, "inode: %d, dirsize: %d\n", inode, dir->size);

  buffer = allocMem((w_size)dir->size);     /* Allocate memory */
  ptr = buffer;                                              /* Pointer to the buffer */
  buffer2 = allocMem((w_size)dir->size);    /* Allocate memory */
  memset(buffer2, 0, dir->size);
  memset(buffer, 0, dir->size);
  ptr2 = buffer2;                                            /* Pointer to the buffer */

  final_length = dir->size;

  /* OLA: This function doesn't work and doens't make the dir smaller */
  /* OLA: Shouldn't work with inodes, but with names ! */
  
  /* Step 1 : Read in the blocks of the directory */
  
  for(i=0; i < dir->size / fs->block_size; i++) { 
    block_nr = e2fs_inode_get_phys_block(fs, dir, 0, i);
    e2fs_read_block(fs, block_nr, fs->block_size, ptr);
    ptr += fs->block_size;
  }

  /* Step 2 : Go over the entries and shoot the duck */

  ptr = buffer;                                    /* Source pointer points to the beginning of the source */
  while(position < dir->size) {                    /* Loop over the entries */
    e2fs_dirent = (e2fs_dir_entry)ptr;             /* Map over a dir_entry structure */
    entry_length = e2fs_dirent->entry_length; 
    entry_length2 = ((e2fs_dirent->name_length - 1) / 4) * 4 + 12;
 
    if(e2fs_dirent->inode == inode) {              /* entry matches the one to delete ? */
	    
    } else {
      
      e2fs_dirent->entry_length = entry_length2;
      
      if((position2 + entry_length2) > fs->block_size) {
          last->entry_length += (fs->block_size - position2);
          
          ptr2 += (fs->block_size - position2);
          position3 += (fs->block_size - position2);
          final_length -= (fs->block_size - position2);
          position2 = 0;
      }

      last = (e2fs_dir_entry)ptr2;

      memcpy(ptr2, ptr, entry_length2);
      ptr2 += entry_length2;
      position2 += entry_length2;
      position3 += entry_length2;
      final_length -= entry_length2;

    }
    position += entry_length;
    ptr += entry_length;
  }

  if(final_length > fs->block_size) {
    while((ino_cache != NULL) && (ino_cache->inode != dir)) ino_cache = ino_cache->next;
    e2fs_alloc_block(fs, ino_cache, (dir->size / fs->block_size), -1);
    final_length -= fs->block_size;
    dir->size -= fs->block_size;
  }

  last->entry_length += final_length;
 
  /* Step 3 : Write the blocks back to the device */

  ptr = buffer2;
  for(i=0; i < dir->size / fs->block_size; i++) { 
    block_nr = e2fs_inode_get_phys_block(fs, dir, 0, i);
    e2fs_write_block(fs, block_nr, fs->block_size, ptr);
    ptr += fs->block_size;
  }

  releaseMem(buffer);       /* Free memory */

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_delete_inode(vfs_inode inode) {
  
  e2fs_filesystem  fs;
  e2fs_inode_cache ino_cache;

  e2fs_lock();
  
  fs = inode->sb->sb.e2fs;
  ino_cache = fs->ino_cache;

  if(fs->flags && E2FS_FS_RW == 0) {
    e2fs_unlock();
    return;      /* If fs is not writable, this has no use */
  }

  while((ino_cache != NULL) && (ino_cache->inode != inode->inode.e2fs)) ino_cache = ino_cache->next;

  e2fs_inode_truncate(inode);                   /* Free the blocks used by this dir_entry and the inode itself */
  
  e2fs_bitmap_set_inode(fs, ino_cache->nr, 0);  /* Mark the inode as unused */

  if((ino_cache->inode->mode & E2FS_FF_DIR) == E2FS_FF_DIR) { /* Deleting a directory */
    inode->sb->sb.e2fs->group_desc[(inode->nr - 1) / inode->sb->sb.e2fs->sb->inodes_per_group].used_dirs--;
    inode->sb->sb.e2fs->flags |= E2FS_FS_GR_DIRTY;
  }
  
  ino_cache->inode->links_count = 0;            /* Set links count to 0 */
  ino_cache->inode->dtime = 1;                  /* Make deletion time something else than 0 -> marks inode as 'deleted'  */
  ino_cache->flags |= E2FS_INO_DIRTY;           /* Mark inode as dirty */      

  e2fs_unlock(); 

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_unlink_inode(vfs_inode dir_inode, vfs_dir_entry dir_entry) {

  e2fs_filesystem  fs;
  e2fs_inode_cache ino_cache;

  e2fs_lock();

  fs = dir_inode->sb->sb.e2fs;
  ino_cache = fs->ino_cache;

  woempa(3, "%s\n", dir_entry->name);

  if(fs->flags && E2FS_FS_RW == 0) {
    e2fs_unlock();
    return;       /* If fs is not writable, this has no use */
  }

  while((ino_cache != NULL) && (ino_cache->inode != dir_entry->inode->inode.e2fs)) ino_cache = ino_cache->next;

  e2fs_delete_dir_entry(fs, dir_inode->inode.e2fs, ino_cache->nr);
                                                 /* Delete the name from the dir list */
  
  dir_inode->size = dir_inode->inode.e2fs->size; /* Update the size of the directory */

  e2fs_delete_inode(dir_entry->inode);           /* Delete the inode */

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_word e2fs_create_inode(vfs_inode inode, vfs_dir_entry dir_entry) {
  
  e2fs_inode_nr    ino_nr;
  e2fs_inode_cache ino_cache;
  e2fs_filesystem  fs = inode->sb->sb.e2fs;
  e2fs_inode       e2fs_ino;
  w_word           type = 0;

  woempa(2, "%s\n", dir_entry->name);
  
  if(fs->flags && E2FS_FS_RW == 0) return EROFS;             /* If fs is not writable, this has no use */

  if((fs->sb->free_inodes_count == 0) || (fs->sb->free_blocks_count == 0)) return ENOSPC;
                                                             /* Check if there's enough room left */
  ino_nr = e2fs_find_new_inode(inode->sb->sb.e2fs);  

  ino_cache = allocMem(sizeof(e2fs_Inode_Cache));
  e2fs_ino = allocMem(sizeof(e2fs_Inode));

  ino_cache->inode = e2fs_ino;                               /* Put the inode in the inode_cache */
  ino_cache->nr = ino_nr;                                    /* Store the inode number */
  ino_cache->blocks = NULL;                                  /* No blocklist */
  ino_cache->blocks_count = 0;                               /* No blocks in the blocklist */
  ino_cache->list_size = 0;
  ino_cache->flags = E2FS_INO_DIRTY;                         /* Inode is dirty */

  ino_cache->next = inode->sb->sb.e2fs->ino_cache;           /* Add this inode cache entry to the list */
  inode->sb->sb.e2fs->ino_cache = ino_cache;
  
  memset(e2fs_ino, 0, sizeof(e2fs_Inode)); 
  
  e2fs_ino->atime = 1000; /* time(NULL); */
  e2fs_ino->ctime = 1000; /* time(NULL); */
  e2fs_ino->mtime = 1000; /* time(NULL); */
  e2fs_ino->links_count = 1;

  e2fs_ino->uid = 123;
  e2fs_ino->gid = 456;

  e2fs_ino->mode = 0;
  if((inode->flags & VFS_FF_R) == VFS_FF_R) e2fs_ino->mode |= E2FS_FC_R;
  if((inode->flags & VFS_FF_W) == VFS_FF_W) e2fs_ino->mode |= E2FS_FC_W;
  if((inode->flags & VFS_FF_DIR) == VFS_FF_DIR) {
    e2fs_ino->mode |= E2FS_FC_DIR;
    type = E2FS_FILE_DIR;
    inode->sb->sb.e2fs->group_desc[(ino_nr - 1) / inode->sb->sb.e2fs->sb->inodes_per_group].used_dirs++;
    inode->sb->sb.e2fs->flags |= E2FS_FS_GR_DIRTY;
  } else {
    e2fs_ino->mode |= E2FS_FF_REG;
    type = E2FS_FILE_REGULAR;
  }

  inode->inode.e2fs = e2fs_ino;
  inode->nr = ino_nr;
  inode->size = 0;

  e2fs_add_dir_entry(inode->sb->sb.e2fs, dir_entry->parent->inode->inode.e2fs, ino_nr, dir_entry->name, type);
  dir_entry->parent->inode->size = dir_entry->parent->inode->inode.e2fs->size;  
                                                             /* Update the size of the parent directory */
  ino_cache->flags |= E2FS_INO_DIRTY;
  
  return 0;
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_word e2fs_mkdir(vfs_inode inode, vfs_dir_entry dir_entry) {

  e2fs_inode_cache ino_cache = inode->sb->sb.e2fs->ino_cache;
  e2fs_filesystem  fs = inode->sb->sb.e2fs;

  
  if(inode->sb->sb.e2fs->flags && E2FS_FS_RW == 0) return EROFS;   
                                                             /* If fs is not writable, this has no use */

  if((fs->sb->free_inodes_count == 0) || (fs->sb->free_blocks_count == 0)) return ENOSPC;
                                                             /* Check for diskspace */

  woempa(3, "%s\n", dir_entry->name);
  
  inode->flags |= VFS_FF_DIR;                                /* Make sure the inode is a directory */
  
  e2fs_create_inode(inode, dir_entry);                       /* Create an inode for the directory */

  e2fs_add_dir_entry(inode->sb->sb.e2fs, inode->inode.e2fs, inode->nr, ".", E2FS_FILE_DIR);
                                                             /* Add the . directory */
  e2fs_add_dir_entry(inode->sb->sb.e2fs, inode->inode.e2fs, dir_entry->parent->inode->nr, "..", E2FS_FILE_DIR);
                                                             /* Add the .. directory */
  
  inode->inode.e2fs->links_count += 1;                       /* For the . */
  inode->size = inode->inode.e2fs->size;                     /* Update the size of the vfs inode */
  
  dir_entry->parent->inode->size = dir_entry->parent->inode->inode.e2fs->size;  

  while((ino_cache != NULL) && (ino_cache->nr != dir_entry->parent->inode->nr)) ino_cache = ino_cache->next;

  ino_cache->inode->links_count++;                           /* For the .. to the parent */
  ino_cache->flags |= E2FS_INO_DIRTY;

  e2fs_sync(inode->sb);

  return 0;
    
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_rmdir(vfs_inode inode, vfs_dir_entry dir_entry) {

  e2fs_inode_cache ino_cache = inode->sb->sb.e2fs->ino_cache;

  if(inode->sb->sb.e2fs->flags && E2FS_FS_RW == 0) return;   /* If fs is not writable, this has no use */

  woempa(2, "%s\n", dir_entry->name);
  
  e2fs_unlink_inode(inode, dir_entry);                       /* Unlink the directory -> free the inode */
                                                             /* and its blocks. Also remove the dir  */
                                                             /* entry */

  while((ino_cache != NULL) && (ino_cache->nr != dir_entry->parent->inode->nr)) ino_cache = ino_cache->next;
                                                             /* Look for the parent directory in the inode cache */

  ino_cache->inode->links_count--;                           /* For the .. to the parent */
  ino_cache->flags |= E2FS_INO_DIRTY;
    
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_rename(vfs_inode inode1, vfs_dir_entry dir_entry1, vfs_inode inode2, vfs_dir_entry dir_entry2) {

  e2fs_filesystem fs = inode1->sb->sb.e2fs;
  w_word          type;

  if(fs->flags && E2FS_FS_RW == 0) return;

  woempa(2, "%s to %s\n", dir_entry1->name, dir_entry2->name);

  if((inode1->flags & VFS_FF_DIR) == VFS_FF_DIR) {
    type = E2FS_FILE_DIR;
  } else {
    type = E2FS_FILE_REGULAR;
  }

  /* OLA: If entries are directories -> update the link_count of the inodes */

  e2fs_delete_dir_entry(fs, dir_entry1->parent->inode->inode.e2fs, inode1->nr);
                                                 /* Delete the name from the dir list */
  
  dir_entry1->parent->inode->size = dir_entry1->parent->inode->inode.e2fs->size; 
                                                 /* Update the size of the directory */

  e2fs_add_dir_entry(fs, dir_entry1->parent->inode->inode.e2fs, inode1->nr, dir_entry2->name, type);
                                                 /* Add the name to the dir list */
  
  dir_entry2->parent->inode->size = dir_entry2->parent->inode->inode.e2fs->size; 
                                                 /* Update the size of the directory */

  memcpy(inode2, inode1, sizeof(vfs_Inode));     /* Duplicate the vfs inode */
  
  dir_entry2->inode = inode2;
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_read_dir(vfs_inode inode, w_ubyte *buffer) {

  w_word  i;
  w_ubyte *ptr = buffer;

  /* OLA: Maybe check if inode is a directory ? */

  for(i=0; i<inode->size / inode->sb->block_size; i++) {
    e2fs_get_block(inode, ptr, i);
    ptr += inode->sb->block_size;
  }

}

