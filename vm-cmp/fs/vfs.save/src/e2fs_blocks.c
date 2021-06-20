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
#include "vfs_errno.h"

#include <stdlib.h>

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word e2fs_find_new_block(e2fs_filesystem fs) {
                                   /* Find a new block. OLA: This is not the way this should be done */
                                   /* We need a target to start from. The rules :  */
                                   /* 1.   Next block free ?      */
                                   /* 2.   8 free blocks nearby   */
                                   /* 3.   Any block nearby       */
                                   /* 4.   8 free blocks far away */
                                   /* 5.   Any block far away     */
                                      
  w_word i = 1;
  
  while(e2fs_bitmap_test_block(fs, i) == 1) i++;
  e2fs_bitmap_set_block(fs, i, 1);
  woempa(2, "block: %d\n", i);

  return i;
  
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word e2fs_clear_block(e2fs_filesystem fs, e2fs_block_nr block) {
  e2fs_bitmap_set_block(fs, block, 0);
  return 0;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word *e2fs_inode_write_ind_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word *source, w_word count) {
  
  w_ubyte        *buffer;

  buffer = allocMem((w_size)fs->block_size);   /* Allocate memory for the buffer */
  memset(buffer, 0, fs->block_size);
 
  memcpy(buffer, source, count * sizeof(w_word));

  e2fs_write_block(fs, phys_block, fs->block_size, buffer);    /* Write the block to the device */
  
  source += count;

  releaseMem(buffer);

  return source;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word  *e2fs_inode_write_dind_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word *source,
                                    w_word count) {
  
  e2fs_block_nr  *d_block_nr;
  w_ubyte        *buffer;
  w_word	i;

  buffer = allocMem((w_size)fs->block_size);  /* Allocate memory for the buffer */
  memset(buffer, 0, fs->block_size);

  for(i=0; i <= count >> fs->addr_per_block_2; i++) {
    
    d_block_nr = (w_word *)buffer + i;
    
    memcpy(d_block_nr, source, sizeof(w_word));

    source += 1;
    
    source = e2fs_inode_write_ind_block(fs, *d_block_nr, source, 
                                        (i < (count >> fs->addr_per_block_2) ? fs->addr_per_block : (count % fs->addr_per_block)));
  }
  
  e2fs_write_block(fs, phys_block, fs->block_size, buffer);    /* Write the block to the device */
  
  releaseMem(buffer);

  return source;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word *e2fs_inode_write_tind_block(e2fs_filesystem fs, w_word phys_block, w_word *source, 
                                   w_word count) {

  e2fs_block_nr  *t_block_nr;
  w_ubyte        *buffer;
  w_word         i;

  buffer = allocMem((w_size)fs->block_size);                 /* Allocate memory for the buffer */
  memset(buffer, 0, fs->block_size);
  
  for(i=0; i <= count >> (fs->addr_per_block_2 << 1); i++) {

    t_block_nr = (w_word *)buffer + i;
    memcpy(t_block_nr, source, sizeof(w_word));
    source += 1;

    source = e2fs_inode_write_dind_block(fs, *t_block_nr, source, (i < count >> (fs->addr_per_block_2 << 1) ? 
                                        (fs->addr_per_block << fs->addr_per_block_2) : 
					(count % (fs->addr_per_block << fs->addr_per_block_2))));
  }

  e2fs_write_block(fs, phys_block, fs->block_size, buffer);    /* Write the block to the device */
  
  releaseMem(buffer);

  return source;
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_inode_write_block_list(e2fs_filesystem fs, e2fs_inode_cache ino_cache) {
  
  w_word            block;               /* Number of blocks in the inode */
  w_word            *source;         
  w_word            i;
  
  e2fs_lock();

  woempa(2, "inode %d\n", ino_cache->nr);
  
  block = ino_cache->blocks_count;       /* Get the number of blocks */
  i = 0;
  
  if((block > 0) && (ino_cache != NULL)) {

    source = ino_cache->blocks;

    while((i < block) && (i < E2FS_IB_NDIR)) {            /* Skip the direct blocks */
      memcpy(&ino_cache->inode->blocks[i], source, sizeof(w_word));
      source++;
      i++;
    } 

    if(i < block) {                                       /* Write the indirect blocks */
      block -=  E2FS_IB_NDIR;
      block--;
      memcpy(&ino_cache->inode->blocks[E2FS_IB_IND_BLOCK], source, sizeof(w_word)); 
      source++;                                            /* Skip the inode indirect block */
      
      source = e2fs_inode_write_ind_block(fs, ino_cache->inode->blocks[E2FS_IB_IND_BLOCK], source, 
                                          (block <= fs->addr_per_block ? block : fs->addr_per_block));
      i += fs->addr_per_block + 1;

    }

    if(i < block) {                                       /* Write the double indirect blocks */
      block -= fs->addr_per_block;
      block--;
      memcpy(&ino_cache->inode->blocks[E2FS_IB_DIND_BLOCK], source, sizeof(w_word)); 
      source++;                                            /* Skip the inode double indirect block */ 
      
      source = e2fs_inode_write_dind_block(fs, ino_cache->inode->blocks[E2FS_IB_DIND_BLOCK], source, 
                                          (block <= fs->addr_per_block << fs->addr_per_block_2 ? 
                                           block : fs->addr_per_block << fs->addr_per_block_2));
      
      i += (fs->addr_per_block << fs->addr_per_block_2) + 1;
    }

    if(i < block) {                                       /* Write the triple indirect blocks */
      block -= (fs->addr_per_block << fs->addr_per_block_2);
      block--;
      memcpy(&ino_cache->inode->blocks[E2FS_IB_TIND_BLOCK], source, sizeof(w_word)); 
      source++;                                            /* Skip the inode triple indirect block */ 
      source = e2fs_inode_write_tind_block(fs, ino_cache->inode->blocks[E2FS_IB_TIND_BLOCK], source, block);
    }

  }

  e2fs_unlock();

}


/* ------------------------------------------------------------------------------------------------------- */

static inline w_word *e2fs_inode_read_ind_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word *target, w_word count) {
  
  w_ubyte        *buffer;

  buffer = allocMem((w_size)fs->block_size); /* Allocate memory for the buffer */
  e2fs_read_block(fs, phys_block, fs->block_size, buffer);    /* Read in the block from the device */

  memcpy(target, buffer, count * sizeof(w_word));

  target += count;

  releaseMem(buffer);

  return target;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word *e2fs_inode_read_dind_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word *target,
                                    w_word count) {
  
  e2fs_block_nr  *d_block_nr;
  w_ubyte        *buffer;
  w_word	i;

  buffer = allocMem((w_size)fs->block_size); /* Allocate memory for the buffer */
  e2fs_read_block(fs, phys_block, fs->block_size, buffer);    /* Read in the  block from the device */
  
  for(i=0; i <= count >> fs->addr_per_block_2; i++) {
    
    d_block_nr = (w_word *)buffer + i;
    memcpy(target, d_block_nr, sizeof(w_word));
    target += 1;
    
    target = e2fs_inode_read_ind_block(fs, *d_block_nr, target, (i < count >> fs->addr_per_block_2 ? 
                                                                fs->addr_per_block : (count % fs->addr_per_block)));
  }
  
  releaseMem(buffer);

  return target;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_word *e2fs_inode_read_tind_block(e2fs_filesystem fs, w_word phys_block, w_word *target, 
                                   w_word count) {

  e2fs_block_nr  *t_block_nr;
  w_ubyte        *buffer;
  w_word         i;

  buffer = allocMem((w_size)fs->block_size);  /* Allocate memory for the buffer */
  e2fs_read_block(fs, phys_block, fs->block_size, buffer);     /* Read in the  block from the device */
  
  for(i=0; i <= count >> (fs->addr_per_block_2 << 1); i++) {

    t_block_nr = (w_word *)buffer + i;
    memcpy(target, t_block_nr, sizeof(w_word));
    target += 1;

    target = e2fs_inode_read_dind_block(fs, *t_block_nr, target, (i < count >> (fs->addr_per_block_2 << 1) ? 
                                        (fs->addr_per_block << fs->addr_per_block_2) : 
					(count % (fs->addr_per_block << fs->addr_per_block_2))));
  }

  releaseMem(buffer);

  return target;
}

/* ------------------------------------------------------------------------------------------------------- */

w_word *e2fs_inode_read_block_list(e2fs_filesystem fs, e2fs_inode inode) {
  
  w_word            *block_list = NULL;  /* Block list */
  w_word            block;               /* Number of blocks in the inode */
  w_word            *target;
  w_word            i;
  w_word            list_size;

  e2fs_lock();

  woempa(2, "\n");
  
  block = inode->blocks_count / (fs->block_size >> 9);       /* Get the number of blocks */
  i = 0;

  list_size = (((block + 1024) / 1024) * 1024);
  
  block_list = allocMem((w_size)(list_size * sizeof(w_word))); 
  memset(block_list, 0, list_size * sizeof(w_word));
      
      /* OLA: Need to fix this to a more correct and usuable size ! */
      /* Moved this line out of the if(block...) because otherwise no memory is allocated */
      /* for inodes which have initially 0 blocks */

  if(block > 0) {

    target = block_list;

    while((i <= block) && (i < E2FS_IB_NDIR)) {            /* Read the indirect blocks */
      memcpy(target, &inode->blocks[i], sizeof(w_word));
      target++;
      i++;
    } 

    if(i < block) {                                       /* Read the indirect blocks */
      block -=  E2FS_IB_NDIR;
      block--;
      memcpy(target, &inode->blocks[E2FS_IB_IND_BLOCK], sizeof(w_word)); 
      target++;
      
      target = e2fs_inode_read_ind_block(fs, inode->blocks[E2FS_IB_IND_BLOCK], target, 
                                         (block <= fs->addr_per_block ? block : fs->addr_per_block));
      i += fs->addr_per_block + 1; 
    }

    if(i < block) {                                       /* Read the double indirect blocks */
      block -= fs->addr_per_block;
      memcpy(target, &inode->blocks[E2FS_IB_DIND_BLOCK], sizeof(w_word));      
      target++;                            
      block--;
      
      target = e2fs_inode_read_dind_block(fs, inode->blocks[E2FS_IB_DIND_BLOCK], target, 
                                           (block <= fs->addr_per_block << fs->addr_per_block_2 ? 
                                            block : fs->addr_per_block << fs->addr_per_block_2));
      
      i += (fs->addr_per_block << fs->addr_per_block_2) + 1; 
    }

    if(i < block) {                                       /* Read the triple indirect blocks */
      block -= (fs->addr_per_block << fs->addr_per_block_2);
      memcpy(target, &inode->blocks[E2FS_IB_TIND_BLOCK], sizeof(w_word));        
      target++;                                     
      block--; 
      target = e2fs_inode_write_tind_block(fs, inode->blocks[E2FS_IB_TIND_BLOCK], target, block);
    }

  }

  e2fs_unlock();

  return block_list;

}

/* ------------------------------------------------------------------------------------------------------- */

w_word e2fs_block_to_blocklist(e2fs_filesystem fs, w_word block) {
  w_word   i = block;  
  w_word   result;
  w_word   ind_blocks;
  w_word   ind_dub;
  w_word   ind_ind;

  e2fs_lock();
  
  if(block < E2FS_IB_NDIR) {                           /* Block has a direct pointer in the inode */
    result = block;
  } else {
    block -= E2FS_IB_NDIR;
    ind_blocks = 1;                                    /* Pass over the inode indirect block */
    if(block < fs->addr_per_block) {                   /* Block is in an indirect block */
      result = i + ind_blocks;
    } else {
      block -= fs->addr_per_block;
      ind_blocks += 1;                                 /* Pass over the inode double indirect block */
      if(block < (fs->addr_per_block << fs->addr_per_block_2)) {    /* Block is in a double indirect block */
        result = i + ind_blocks + (block >> fs->addr_per_block_2) + 1;
      } else {                                         /* Block is in a triple indirect block */
        block -= fs->addr_per_block << fs->addr_per_block_2;
        ind_blocks += 1;                               /* Pass over the inode triple indirect block */
        ind_blocks += fs->addr_per_block;              /* Pass over the double indirect blocks */

        ind_dub = (block / (fs->addr_per_block << fs->addr_per_block_2));                         /* nr of dinds */
        ind_ind = (block % (fs->addr_per_block << fs->addr_per_block_2)) >> fs->addr_per_block_2; /* nr of inds */
        
        /* OLA: Some optimalization is in order */
        
        ind_blocks += (ind_dub << fs->addr_per_block_2) + ind_ind + 3;  /* Jump over double indirects */

        result = i + ind_blocks;
          
      }
    }     
  }

  e2fs_unlock();

  return result;
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_word e2fs_inode_get_phys_block(e2fs_filesystem fs, e2fs_inode inode, e2fs_inode_nr inode_nr, e2fs_block_nr block_nr) {

  e2fs_inode_cache  ino_cache;
  w_word result;

  e2fs_lock();

  ino_cache = fs->ino_cache;

  while((ino_cache != NULL) && (ino_cache->inode != inode)) ino_cache = ino_cache->next;
  
  if(ino_cache->blocks == NULL) { /* No block list -> Load it */

    woempa(2, "blocks are not in cache -> Read in the block list\n");
    ino_cache->blocks = e2fs_inode_read_block_list(fs, inode);
    ino_cache->blocks_count = inode->blocks_count / (fs->block_size >> 9);
    ino_cache->list_size = (((inode->blocks_count / (fs->block_size >> 9) + 1024) / 1024) * 1024);
    
  }

  result = ino_cache->blocks[e2fs_block_to_blocklist(fs, block_nr)];

  e2fs_unlock();

  return result;

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_alloc_block(e2fs_filesystem fs, e2fs_inode_cache ino_cache, e2fs_block_nr block_nr, int alloc) {  
   
  /* Allocate a new block and add it to the blocklist (alloc = 1) or release the last block on the blocklist (alloc = -1) */

  w_word            *temp;
  int               count;
  int               i;

  e2fs_lock();

  if(ino_cache->blocks == NULL) {                                   /* No block list -> Load it */
    woempa(2, "blocks are not in cache -> Read in the block list\n");
    ino_cache->blocks = e2fs_inode_read_block_list(fs, ino_cache->inode);
    ino_cache->blocks_count = ino_cache->inode->blocks_count / (fs->block_size >> 9);
    /* OLA: What about the list_size of the ino_cache ?? */
    /* OLA: Put this functionality in read_block_list */
  }
  
  count = e2fs_block_to_blocklist(fs, block_nr) - ino_cache->blocks_count + 1;
                                                                    /* Calculate the difference */

  /* Add blocks ? */
  
  if((count > 0) && (alloc == 1)) {

    if(ino_cache->list_size < ino_cache->blocks_count + count) {    /* More blocks ? */
      temp = allocMem((w_size)((ino_cache->list_size + 1024) * sizeof(w_word)));
      memset(temp, 0, (w_size)((ino_cache->list_size + 1024) * sizeof(w_word)));
      memcpy(temp, ino_cache->blocks, ino_cache->blocks_count * sizeof(w_word));
      releaseMem(ino_cache->blocks);
      ino_cache->blocks = temp;
      ino_cache->list_size += 1024;
    }

    for(i=0; i < count; i++) {
      ino_cache->blocks[ino_cache->blocks_count] = e2fs_find_new_block(fs);
      ino_cache->blocks_count++;
    }
    
    ino_cache->inode->blocks_count = ino_cache->blocks_count * (fs->block_size >> 9);
    ino_cache->flags |= E2FS_INO_DIRTY;
    ino_cache->flags |= E2FS_INO_BDIRTY;
  
  }

  /* Remove blocks ? */

  if((count < 0) && (alloc == -1)) {

    if(ino_cache->list_size > ino_cache->blocks_count - count + 1024) {  /* Less blocks ? */
      temp = allocMem((w_size)((ino_cache->list_size - 1024) * sizeof(w_word)));
      memcpy(temp, ino_cache->blocks, ino_cache->blocks_count * sizeof(w_word));
      releaseMem(ino_cache->blocks);
      ino_cache->blocks = temp;
      ino_cache->list_size -= 1024;
    }
  
    for(i=0; i > count; i--) {
      ino_cache->blocks[ino_cache->blocks_count - 1] = e2fs_clear_block(fs, ino_cache->blocks[i]);
      ino_cache->blocks_count--;
    }

    ino_cache->inode->blocks_count = ino_cache->blocks_count * (fs->block_size >> 9);
    ino_cache->flags |= E2FS_INO_DIRTY;
    ino_cache->flags |= E2FS_INO_BDIRTY;
  
  }

  e2fs_unlock();

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_get_block(vfs_inode inode, w_ubyte *buffer, w_word block_nr) {

  e2fs_lock();
  
  woempa(2, "inode: %i  block: %i\n", inode->nr, block_nr);
  
  block_nr = e2fs_inode_get_phys_block(inode->sb->sb.e2fs, inode->inode.e2fs, inode->nr, block_nr);
  e2fs_read_block(inode->sb->sb.e2fs, block_nr, inode->sb->sb.e2fs->block_size, buffer);

  e2fs_unlock();
}

/* ------------------------------------------------------------------------------------------------------- */

w_word e2fs_put_block(vfs_inode inode, w_ubyte *buffer, w_word block_nr) {
  e2fs_inode_cache ino_cache;
  e2fs_filesystem  fs;

  e2fs_lock();
  
  ino_cache = inode->sb->sb.e2fs->ino_cache;
  fs = inode->sb->sb.e2fs;

  woempa(2, "inode: %i  block: %i\n", inode->nr, block_nr);

  while((ino_cache != NULL) && (ino_cache->inode != inode->inode.e2fs)) ino_cache = ino_cache->next;
                                                             /* Search the inode cache */
  if((e2fs_block_to_blocklist(fs, block_nr) - ino_cache->blocks_count + 1) > fs->sb->free_blocks_count) {
    e2fs_unlock();
    return ENOSPC;
  }
  
  e2fs_alloc_block(inode->sb->sb.e2fs, ino_cache, block_nr, 1);      /* If the requested block is not */
                                                                     /* allocated, allocate it */

  block_nr = e2fs_inode_get_phys_block(inode->sb->sb.e2fs, inode->inode.e2fs, inode->nr, block_nr);
                                                             /* Get the block number */

  e2fs_write_block(inode->sb->sb.e2fs, block_nr, inode->sb->sb.e2fs->block_size, buffer);
                                                             /* Write the block */
  e2fs_unlock();

  return 0;
}

