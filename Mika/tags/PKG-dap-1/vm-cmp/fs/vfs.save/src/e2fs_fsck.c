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

#include "vfs.h"
#include "e2fs_prototypes.h"

#include "buffercache.h"

#include <stdlib.h>
#include <stdio.h>
#include "oswald.h"

w_word list_total = 0;
w_word hash_total = 0;

static inline w_void e2fs_fsck_bitmap_set(w_ubyte *bitmap, w_word nr, w_word bit) {
  w_word position = (nr- 1) / 8;
  w_word offset = (nr - 1) % 8;

  bitmap[position] = (bitmap[position] & ~(1 << offset)) | (bit << offset);
}
  
/* ------------------------------------------------------------------------------------------------------- */

static inline w_ubyte e2fs_fsck_bitmap_test(w_ubyte *bitmap, w_word nr) {
  w_word position = (nr - 1) / 8;
  w_word offset = (nr - 1) % 8;

  return ((bitmap[position] & (1 << offset)) == (1 << offset));
}

/* ------------------------------------------------------------------------------------------------------- */

static inline e2fs_fsck_dir_hash e2fs_fsck_search_hash(e2fs_filesystem fs, w_word inode_nr) {

  e2fs_fsck_dir_hash iter = fs->fsck->dirs[inode_nr % E2FS_FSCK_DIR_HASH_SIZE];
  e2fs_fsck_dir_hash result = NULL;

  while((iter != NULL) && (result == NULL)) {
    if(inode_nr == iter->inode_nr) result = iter;
    iter = iter->next;
  }
  return result;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_void e2fs_fsck_add_to_hash(e2fs_filesystem fs, e2fs_fsck_dir_hash dir) {

  dir->next = fs->fsck->dirs[dir->inode_nr % E2FS_FSCK_DIR_HASH_SIZE];
  if(dir->next != NULL) fs->fsck->dirs[dir->inode_nr % E2FS_FSCK_DIR_HASH_SIZE]->prev = dir;
  dir->prev = NULL;
  
  fs->fsck->dirs[dir->inode_nr % E2FS_FSCK_DIR_HASH_SIZE] = dir;

  hash_total++;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_void e2fs_fsck_remove_from_hash(e2fs_filesystem fs, e2fs_fsck_dir_hash dir) {

  if((dir->prev != NULL) && (dir->next != NULL)) {
    dir->prev->next = dir->next;
    dir->next->prev = dir->prev;
  } else { 
    if(dir->prev == NULL) {               /* First on the list */
      fs->fsck->dirs[dir->inode_nr % E2FS_FSCK_DIR_HASH_SIZE] = dir->next;
      fs->fsck->dirs[dir->inode_nr % E2FS_FSCK_DIR_HASH_SIZE]->prev = NULL;
    } else {                              /* Last on the list */
      dir->prev->next = NULL;
    }
  }

  hash_total--;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_void e2fs_fsck_remove_from_list(e2fs_filesystem fs, e2fs_fsck_dir_hash hash, e2fs_fsck_dir dir) {

  if((dir->prev != NULL) && (dir->next != NULL)) {
    dir->prev->next = dir->next;
    dir->next->prev = dir->prev;
  } else { 
    if(dir->prev == NULL) {               /* First on the list */
      hash->dir = dir->next;
      hash->dir->prev = NULL;
    } else {                              /* Last on the list */
      dir->prev->next = NULL;
      hash->tail = dir->prev;
    }
  }

  list_total--;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline w_void e2fs_fsck_add_to_list(e2fs_filesystem fs, e2fs_fsck_dir_hash hash, e2fs_fsck_dir dir) {

  if(hash->dir == NULL) {                 /* First one on the list */
    hash->dir = dir;                      /* Make dir & tail point to this entry */
    hash->tail = dir;                     /* The prev & next references are NULL because of the memset */
  } else {                                /* An other entry came in first.. */
    hash->tail->next = dir;               /* Put the entry on the end of the list */
    dir->prev = hash->tail;
    hash->tail = dir;
  }

  list_total++;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline e2fs_fsck_dir e2fs_fsck_search_list_name(e2fs_filesystem fs, e2fs_fsck_dir_hash hash, w_ubyte *name) {

  e2fs_fsck_dir iter = hash->dir;
  e2fs_fsck_dir result = NULL;

  while((iter != NULL) && (result == NULL)) {
    if(strcmp(name, iter->name) == 0) result = iter;
    iter = iter->next;
  }
  return result;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline e2fs_fsck_dir e2fs_fsck_search_list_inode(e2fs_filesystem fs, e2fs_fsck_dir_hash hash, w_word inode) {

  e2fs_fsck_dir iter = hash->dir;
  e2fs_fsck_dir result = NULL;

  while((iter != NULL) && (result == NULL)) {
    if(inode == iter->inode_nr) result = iter;
    iter = iter->next;
  }
  return result;
}

/* ------------------------------------------------------------------------------------------------------- */

static inline void e2fs_fsck_show_list(e2fs_filesystem fs, e2fs_fsck_dir_hash hash) {

  e2fs_fsck_dir iter = hash->dir;

  woempa(9, "Entries in directory inode %d :\n", hash->inode_nr);
  while(iter != NULL) {
    woempa(9, " '%s'\n", iter->name);
    iter = iter->next;
  }
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_cleanup_list(e2fs_filesystem fs, e2fs_fsck_dir_hash hash) {

  e2fs_fsck_dir iter = hash->dir;
  e2fs_fsck_dir temp;
  
  while(iter != NULL) {
    releaseMem(iter->name);
    temp = iter->next;
    releaseMem(iter);
    iter = temp;
    list_total--;
  }
  
  hash->dir  = NULL;
  hash->tail = NULL;
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_cleanup_hash(e2fs_filesystem fs) {
  w_word i;
  
  e2fs_fsck_dir_hash iter;
  e2fs_fsck_dir_hash temp;
  
  for(i = 0; i < E2FS_FSCK_DIR_HASH_SIZE; i++) {
    if(fs->fsck->dirs[i] != NULL) {
      iter = fs->fsck->dirs[i];
      while(iter != NULL) {
        e2fs_fsck_cleanup_list(fs, iter);
        temp = iter->next;
        releaseMem(iter);
        iter = temp;
        hash_total--;
      }        
      fs->fsck->dirs[i] = NULL;
    }
  }
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_pass1(e2fs_filesystem fs) {
  /* Check inodes, blocks & sizes :
   *
   * inodes: - Correct mode ?
   *         - Size and blockcount correct ?
   *         - Different inodes can not use the same blocks.
   * Generating bitmaps : - used inodes.
   *                      - inodes that are directories.
   *                      - inodes that are regular files.
   *                      - inodes with incorrect data.
   *                      - used blocks.
   *                      - blocks in use by more than one inode.
   * Also read in the blocks of directories for further checking.
   */

  e2fs_inode inode = fs->fsck->inode_list;
  w_word     i, j;
  w_word     *block_list;
  w_word     *iter;

  woempa(9, "Pass 1: Check inodes, blocks & sizes\n");

  /* Read in all the inodes. Every single one, even the ones not in use */ 

  inode = fs->fsck->inode_list;
  
  for(i = 1; i <= fs->sb->inodes_count; i++) {
    inode++;
    
    e2fs_read_inode_from_fs(fs, i, inode);

    if((i != E2FS_ROOT_INODE) && (i < fs->sb->first_inode)) continue;

    /* 1. Check if the inode is in use. */
    
    if(e2fs_bitmap_test_inode(fs, i)) {                   /* inode is in use */

      /* 2. Check if delete time is zero and other times different than zero */

      if(inode->ctime == 0) {
        woempa(9, "  ERROR: Used inode %d has zero creation time\n", i);
      }
      
      if(inode->dtime != 0) {
        woempa(9, "  ERROR: Used inode %d has non zero deletion time\n", i);
      }
      
      if(inode->atime == 0) {
        woempa(9, "WARNING: Used inode %d has zero access time\n", i);
      }
      
      if(inode->mtime == 0) {
        woempa(9, "WARNING: Used inode %d has zero modification time\n", i);
      }
      
      /* 3. Check the size and blockcount */

      if((inode->blocks_count * 512) % fs->block_size != 0) {
        woempa(9, "  ERROR: used inode %d has an invalid blocks_count (%d)\n", i, inode->blocks_count);
      }

      if(e2fs_block_to_blocklist(fs, ((inode->size + fs->block_size - 1) >> fs->block_size_2) - 1) + 1 != 
        (inode->blocks_count >> (fs->block_size_2 - 9)) && !(inode->blocks_count == 0 && inode->size == 0)) {
        woempa(9, "  ERROR: used inode %d is %d blocks and should be %d blocks\n", i, 
                 inode->blocks_count >> (fs->block_size_2 - 9), 
                 e2fs_block_to_blocklist(fs, ((inode->size + fs->block_size - 1) >> fs->block_size_2) - 1) + 1);
      }

      /* 4. iterate the blocklist and set bits in the bitmap */
      
      e2fs_fsck_bitmap_set(fs->fsck->inode_bitmap, i, 1);           /* Mark the inode as 'in use' */
      if((inode->mode & E2FS_FF_DIR) == E2FS_FF_DIR) 
        e2fs_fsck_bitmap_set(fs->fsck->dir_inode_bitmap, i, 1);     /* Mark the inode as a dir */

      block_list = iter = e2fs_inode_read_block_list(fs, inode);
      for(j = 0; j < (inode->blocks_count >> (fs->block_size_2 - 9)); j++) {
        if(e2fs_fsck_bitmap_test(fs->fsck->block_bitmap, *iter)) {  
          /* Oh boy, the bit was already set. This means that another inode is also using this block */
          e2fs_fsck_bitmap_set(fs->fsck->schizo_block_bitmap, *iter, 1);   
          woempa(9, "  ERROR: Multiple inodes are using block %d\n", *iter);
        }
        e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, *iter, 1);     /* Mark the block as 'in use' */
        iter++;
      }
      releaseMem(block_list);
      
    } else {                                                        /* inode is not in use */

      /* 2. Check the times */

      if(inode->ctime != 0 && inode->dtime == 0) {
        woempa(9, "  ERROR: Deleted inode %d has zero deletion time\n", i);
      }
      
    }
  }
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_pass2(e2fs_filesystem fs) {
  /* Check the directory structure :
   *
   * - length of dir_entries should be at least 8 bytes and not 
   *   exceed the remaining space in a block.
   * - length of a name should be less than rec_len - 8.
   * - valid inode number in the dir_entries ?
   * - first entry should always be '.' (with matching inode).
   * - second entry should always be '..' (with matching inode).
   * Also collect the inode numbers of the subdirectories for further
   * checking.
   */

  w_word               i, j;
  w_ubyte              *buffer, *iter;
  w_word               *block_list;
  e2fs_inode           inode = NULL;
  e2fs_dir_entry       entry = NULL;
  w_word               position;
  w_word               remaining;
  w_word               entry_length;
  w_word               entry_nr;
  w_byte               *name;
  e2fs_fsck_dir_hash   hash;
  e2fs_fsck_dir        dir_entry;
  
  woempa(9, "Pass 2: Check the directory structure\n");

  name = allocMem(255);

  for(i = 1; i <= fs->sb->inodes_count; i++) 
    if(e2fs_fsck_bitmap_test(fs->fsck->dir_inode_bitmap, i)) {

      hash = allocMem(sizeof(e2fs_Fsck_Dir_Hash));     /* Allocate memory for the hashtable entry */
      memset(hash, 0, sizeof(e2fs_Fsck_Dir_Hash));            /* Clear it, no surprises */
      hash->inode_nr = i;                                     /* Store the current inode number */
      e2fs_fsck_add_to_hash(fs, hash);                        /* Add the entry to the hashtable */

      if(i == E2FS_ROOT_INODE) hash->connected_up = 1;        /* The root is always connected to the root (itself) */
      if(i == E2FS_ROOT_INODE) hash->connected_down = 1;      
      
      inode = (e2fs_inode)(fs->fsck->inode_list + i);         /* Get the inode from the list which was saved during pass1. */

      block_list = e2fs_inode_read_block_list(fs, inode);     /* Read in the block list of the inode. */
      buffer = iter = allocMem((w_size)inode->size);   /* Allocate memory for the content of the directory. */

      for(j = 0; j < (inode->size / fs->block_size); j++) {   /* Read in the content of the directory. */
        e2fs_read_block(fs, block_list[e2fs_block_to_blocklist(fs, j)], fs->block_size, iter);
        iter += fs->block_size;
      }

      releaseMem(block_list);                                 /* The blocklist is no longer needed, so release memory. */
 
      iter = buffer;                                          /* Iterate over the directory entries in this directory. */
      position = 0;
      entry_nr = 1;
      while(position < inode->size) {
        entry = (e2fs_dir_entry)iter;                         /* Get the entry out of the directory content. */
        entry_length = entry->entry_length;                   /* The length of this entry */
        remaining = fs->block_size - (position % fs->block_size);  /* Remaining space in the current block */

        if(entry_length < 8 || entry_length > remaining) {    /* Entry length is out of range */
          woempa(9, "  ERROR: Length of entry nr %d in inode %d is out of range\n", entry_nr, i);
        }

        if(entry->name_length > entry_length - 8) {           /* Name length is out of range */
          woempa(9, "  ERROR: Name length of entry nr %d in inode %d is out of range\n", entry_nr, i);
        }
        
        memset(name, 0, 255);                                
        strncpy(name, entry->name, entry->name_length);       /* Get the name of the entry. */

        if(entry_nr == 1) {                                   /* This should be the '.' directory. */
          if(strcmp(name, ".") != 0) {
            woempa(9, "  ERROR: The first entry of directory inode %d is not '.'\n", i);
          }

          if(entry->inode != i) {                             /* . should point to this directory. */
            woempa(9, "  ERROR: '.' in directory inode %d does not point to itself\n", i);
          } 
        }

        if(entry_nr == 2) {                                   /* This should be the '..' directory. */
          if(strcmp(name, "..") != 0) {
            woempa(9, "  ERROR: The second entry of directory inode %d is not '..'\n", i);
          }

          if(!e2fs_fsck_bitmap_test(fs->fsck->dir_inode_bitmap, entry->inode)) {
            woempa(9, "  ERROR: '..' in directory inode %d does not point to a directory\n", i);
          } 
        }

        if((entry->inode < fs->sb->first_inode && entry->inode != E2FS_ROOT_INODE && entry->inode != 0) ||
            entry->inode > fs->sb->inodes_count) {           /* inode number is out of range */
          woempa(9, "  ERROR: The inode '%s' in directory inode %d refers to, is out of range\n", name, i);
        }
        
        if((entry->inode != 0) && 
            !e2fs_fsck_bitmap_test(fs->fsck->inode_bitmap, entry->inode)) {  /* Check if the inode number is in use */
          woempa(9, "  ERROR: The inode '%s' in directory inode %d refers to, is marked as 'not in use'\n", name, i);
        }  

        dir_entry = allocMem(sizeof(e2fs_Fsck_Dir));   /* Allocate memory for the directory entry */
        memset(dir_entry, 0, sizeof(e2fs_Fsck_Dir));          /* Clear it, no surprises */
        dir_entry->inode_nr = entry->inode;                   /* Store the inode number of the entry */   

        dir_entry->name = allocMem(strlen(name) + 1);  /* Allocate memory for the name */
        memset(dir_entry->name, 0, strlen(name) + 1);         /* Clear memory */ 
        strcpy(dir_entry->name, name);                        /* Copy the name */

        e2fs_fsck_add_to_list(fs, hash, dir_entry);
        
        position += entry_length;                             /* Go to the next entry in the list. */
        iter += entry_length;
        entry_nr++;
      }
      entry_nr--;
      
      if(entry_nr < 2) {                                      /* A directory should contain at least 2 entries : . and .. */
        woempa(9, "  ERROR: Directory inode %d should have at least 2 entries but contains %d entries\n", i, entry_nr);
      }
      
      releaseMem(buffer);                                     /* The content of the directory is no longer needed. */
      
    } 

  releaseMem(name);
}

/* ------------------------------------------------------------------------------------------------------- */

w_ubyte e2fs_fsck_check_connected(e2fs_filesystem fs, w_word inode_nr) {
  w_ubyte            result = 1;
  e2fs_fsck_dir_hash current_dir;
  e2fs_fsck_dir      parent_entry;

  current_dir  = e2fs_fsck_search_hash(fs, inode_nr);
  
  if(current_dir->loop_test == 1) { 
    /* If loop_test is already set, we've encountered a loop in the directory structure. This means that  
     * by starting at a certain directory and continue follow the '..' (parent) directory, we eventually 
     * end up in the directory where we've started. This is not a good thing ! */
    
    woempa(9, "  ERROR: loop in directory structure detected...\n");
    result = 0;                                               /* return 0. Since we are in a loop, the root */
                                                              /* directory can never be reached */
                                                                 
  } else {
  
    current_dir->loop_test = 1;                               /* Set loop_test to 1 so we know we've been here before. */
  
    if(current_dir->connected_down == 0) {                    /* The current directory is not yet marked as connected */
      parent_entry = e2fs_fsck_search_list_name(fs, current_dir, (w_ubyte *)"..");
                                                              /* Search the current directory for its parent directory */
      if(parent_entry == NULL) {
        woempa(9, "  ERROR: parent directory not found (which is weird because it was there in the previous pass...\n");
      } else {
        result = e2fs_fsck_check_connected(fs, parent_entry->inode_nr);
                                                              /* Check the parent directory */
        current_dir->connected_down = result;                 /* Store the result */

        if(e2fs_fsck_search_list_inode(fs,  e2fs_fsck_search_hash(fs, parent_entry->inode_nr), current_dir->inode_nr) == NULL) {
          /* In this case the parent directory does not contain any references to this directory. This means that 
           * the current directory is not accessible through the directory structure. Bad mojo. */
          woempa(9, "  ERROR: parent directory (inode %d) does not contain a reference to the current directory (inode %d)\n", 
                 parent_entry->inode_nr, current_dir->inode_nr);
        } else {
          current_dir->connected_up = 1;
        }
      }
    } else {
      woempa(1, "Current dir (inode %d) is already checked\n", inode_nr);
    }
    
    current_dir->loop_test = 0;                               /* After processing, set loop_test back to 0 */
    
  }

  return result;
}
  
/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_pass3(e2fs_filesystem fs) {
  /* Check directory connectivity :
   *
   * - Every directory inode should be in the directory structure.
   *   Check this by reading the dir_entries and jump to '..' until
   *   a previously checked directory or the root is reached.
   * - Check if a directory doesn't show up twice in the structure.
   *   This is impossible because '..' can only point to one parent
   *   directory.
   */

  w_word i;
  
  woempa(9, "Pass 3: Check directory connectivity\n");

  for(i = 1; i <= fs->sb->inodes_count; i++) 
    if(e2fs_fsck_bitmap_test(fs->fsck->dir_inode_bitmap, i)) {
      e2fs_fsck_check_connected(fs, i);
    }

  /* TODO: Check for dupe directories.. Probably the easiest with a bitmap... */
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_pass4(e2fs_filesystem fs) {
  /* Check the reference count of all the inodes */
  
  woempa(9, "Pass 4: Check reference count\n");

  /* TODO: Everything */

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_pass5(e2fs_filesystem fs) {
  /* Check the group summary information */

  w_word i;
  w_word free_blocks = 0;
  w_word free_inodes = 0;
  w_word free_blocks_total = 0;
  w_word free_inodes_total = 0;
  
  woempa(9, "Pass 5: Check the group summary information\n");

  /* Compare the bitmaps of used inodes & blocks which where build during checking with the bitmaps on disk */
  
  for(i = 1; i < fs->sb->blocks_count; i++) 
    if(e2fs_fsck_bitmap_test(fs->block_bitmap, i) != e2fs_fsck_bitmap_test(fs->fsck->block_bitmap, i)) {
      woempa(9, "  ERROR: Block bitmap difference %c%d\n", (e2fs_fsck_bitmap_test(fs->fsck->block_bitmap, i) ? '+' : '-'), i); 
    }
  
  for(i = 1; i <= fs->sb->inodes_count; i++) 
    if(e2fs_fsck_bitmap_test(fs->inode_bitmap, i) != e2fs_fsck_bitmap_test(fs->fsck->inode_bitmap, i)) {
      woempa(9, "  ERROR: Inode bitmap difference %c%d\n", (e2fs_fsck_bitmap_test(fs->fsck->inode_bitmap, i) ? '+' : '-'), i); 
    }
  
  /* Count free blocks per group and compare */

  for(i=1; i < fs->sb->blocks_count; i++) {
    if(!e2fs_fsck_bitmap_test(fs->fsck->block_bitmap, i)) {
      free_blocks++;
      free_blocks_total++;
    }
    if(i % fs->sb->blocks_per_group == 0) {
      if(fs->group_desc[(i / fs->sb->blocks_per_group) - 1].free_blocks != free_blocks) {
        woempa(9, "  ERROR: Free blocks count of group %d is wrong. (is %d, should be %d)\n", 
                 (i / fs->sb->blocks_per_group) - 1, 
                 fs->group_desc[(i / fs->sb->blocks_per_group) - 1].free_blocks,
                 free_blocks);
      }
      free_blocks = 0;
    }
  }

  /* Once more for the final group... Should find another way to do this... */
  
  if(fs->group_desc[fs->group_desc_nr - 1].free_blocks != free_blocks) {
    woempa(9, "  ERROR: Free blocks count of group %d is wrong. (is %d, should be %d)\n", 
             fs->group_desc_nr - 1, 
             fs->group_desc[fs->group_desc_nr - 1].free_blocks,
             free_blocks);
  }

  /* Count free inodes per group and compare */

  for(i=1; i <= fs->sb->inodes_count; i++) {
    if(!e2fs_fsck_bitmap_test(fs->fsck->inode_bitmap, i)) {
      free_inodes++;
      free_inodes_total++;
    }
    if(i % fs->sb->inodes_per_group == 0) {
      if(fs->group_desc[(i / fs->sb->inodes_per_group) - 1].free_inodes != free_inodes) {
        woempa(9, "  ERROR: Free inodes count of group %d is wrong. (is %d, should be %d)\n", 
                 (i / fs->sb->inodes_per_group) - 1, 
                 fs->group_desc[(i / fs->sb->inodes_per_group) - 1].free_inodes,
                 free_inodes);
      }
      free_inodes = 0;
    }
  }

  /* Check the counted free blocks & free inodes with the data in the superblock */

  if(fs->sb->free_blocks_count != free_blocks_total) {
    woempa(9, "  ERROR: Free blocks count of the superblockis wrong. (is %d, should be %d)\n", 
             fs->sb->free_blocks_count,
             free_blocks_total);
  }
  
  if(fs->sb->free_inodes_count != free_inodes_total) {
    woempa(9, "  ERROR: Free inodes count of the superblockis wrong. (is %d, should be %d)\n", 
             fs->sb->free_inodes_count,
             free_inodes_total);
  }
  
}


/* ------------------------------------------------------------------------------------------------------- */

w_ubyte e2fs_fsck_check_sparse(w_word i) {
  w_word test;

  /* Checks if the group with the given number holds copies of the superblock and the group descriptors.
   * Every group wich is a power of 3, 5 or 7 has such a copy (if the correct flag in the superblock is set.
   * These groups have copies : 1 (power 0), 3, 5, 7, 3^2, 5^2, 7^2, 3^3, 5^3, 7^3, ... 
   * If the filesystem grows exponentially, copies grow linear. 
   */

  if(i == 0) return 0;
  if(i == 1) return 1;

  test = i;
  while((test > 3) && (test % 3 == 0)) test /= 3;
  if(test == 0) return 1;
  
  test = i;
  while((test > 5) && (test % 5 == 0)) test /= 5;
  if(test == 0) return 1;
  
  test = i;
  while((test > 7) && (test % 7 == 0)) test /= 7;
  if(test == 0) return 1;

  return 0;
  
}
        
/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck_mark_blocks(e2fs_filesystem fs) {
  w_word i, j;

  /* This one marks all the blocks/inodes in fsck's bitmaps that are in use by the structure of
   * the filesystem itself. Among these are e.g the inodes before the 'first inode' from the superblock and
   * all the blocks in use by inode tables. These should be marked so we can compare our fabricated 
   * bitmap with the one that's on disk and look for differences. */

      
  for(i = 1; i < fs->sb->first_inode; i++) {                      /* Inodes before the 'first inode' are in use. */
    e2fs_fsck_bitmap_set(fs->fsck->inode_bitmap, i, 1);
  }

    
  for(i=0; i < fs->group_desc_nr; i++) {                          /* Blocks used by the bitmaps are in use. */
    e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, fs->group_desc[i].block_bitmap, 1);
    e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, fs->group_desc[i].inode_bitmap, 1);
  } 

  for(i=0; i < fs->desc_blocks; i++) {                            /* Blocks used by the group descriptors are in use. */
    e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, (fs->sb->first_data_block + 1 + i), 1);
  }

  for(i=0; i < fs->group_desc_nr; i++) {                          /* Go through the different groups */
    
    if((fs->sb->feat_ro_compat_set & E2FS_FROC_SPARSE_SUPER) == E2FS_FROC_SPARSE_SUPER) {
      /* The filesystem holds copies of the superblock & groupdescs. These are also in used and should be marked */
      if(e2fs_fsck_check_sparse(i)) {
        e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, fs->sb->blocks_per_group * i + 1, 1);         /* Superblock copy */
        for(j=0; j < fs->desc_blocks; j++) {
          e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, fs->sb->blocks_per_group * i + 2 + j, 1);   /* Groupdesc copy */
        }
      }
    }

    /* Blocks used by the inode tables are in use. */
    
    for(j=0; j < fs->sb->inodes_per_group / (fs->block_size / sizeof(e2fs_Inode)); j++) {
      e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, fs->group_desc[i].inode_table + j, 1);
    }

  }

  e2fs_fsck_bitmap_set(fs->fsck->block_bitmap, 1, 1);             /* The block used by the superblock is in use. */

}

/* ------------------------------------------------------------------------------------------------------- */

w_void e2fs_fsck(e2fs_filesystem fs) {

  e2fs_fsck_data  fsck = allocMem(sizeof(e2fs_Fsck_Data));
  w_word          block_bytes;   /* Number of bytes in a block bitmap */
  w_word          inode_bytes;   /* Number of bytes in an inode bitmap */
  
  memset(fsck, 0, sizeof(e2fs_Fsck_Data));

  woempa(9, "Start of e2fs_fsck\n");

  /* Prepare for battle */

  fs->fsck = fsck;

  if(fs->sb->blocks_per_group > fs->sb->blocks_count) 
    block_bytes = (fs->sb->blocks_count + 7) >> 3; 
  else 
    block_bytes = fs->sb->blocks_per_group >> 3;
  
  if(fs->sb->inodes_per_group > fs->sb->inodes_count) 
    inode_bytes = (fs->sb->inodes_count + 7) >> 3; 
  else 
    inode_bytes = fs->sb->inodes_per_group >> 3;

  fs->fsck->block_bitmap = allocMem((w_size)(block_bytes * fs->group_desc_nr));
  fs->fsck->inode_bitmap = allocMem((w_size)(inode_bytes * fs->group_desc_nr));
  fs->fsck->dir_inode_bitmap = allocMem((w_size)(inode_bytes * fs->group_desc_nr));
  fs->fsck->bad_inode_bitmap = allocMem((w_size)(inode_bytes * fs->group_desc_nr));
  fs->fsck->schizo_block_bitmap = allocMem((w_size)(block_bytes * fs->group_desc_nr));

  memset(fs->fsck->block_bitmap, 0, block_bytes * fs->group_desc_nr);
  memset(fs->fsck->inode_bitmap, 0, inode_bytes * fs->group_desc_nr);
  memset(fs->fsck->dir_inode_bitmap, 0, inode_bytes * fs->group_desc_nr);
  memset(fs->fsck->bad_inode_bitmap, 0, inode_bytes * fs->group_desc_nr);
  memset(fs->fsck->schizo_block_bitmap, 0, block_bytes * fs->group_desc_nr);

  fs->fsck->inode_list = allocMem(sizeof(e2fs_Inode) * (fs->sb->inodes_count + 1));
  memset(fs->fsck->inode_list, 0, (sizeof(e2fs_Inode) * fs->sb->inodes_count));

  e2fs_fsck_mark_blocks(fs);
  
  /* Check the filesystem */
   
  e2fs_fsck_pass1(fs);
  e2fs_fsck_pass2(fs);
  e2fs_fsck_pass3(fs);
  e2fs_fsck_pass4(fs);
  e2fs_fsck_pass5(fs);

  /* Clean up */

  e2fs_fsck_cleanup_hash(fs);

  releaseMem(fs->fsck->inode_list);
  releaseMem(fs->fsck->block_bitmap);
  releaseMem(fs->fsck->inode_bitmap);
  releaseMem(fs->fsck->dir_inode_bitmap);
  releaseMem(fs->fsck->bad_inode_bitmap);
  releaseMem(fs->fsck->schizo_block_bitmap);
  releaseMem(fs->fsck);
  fs->fsck = NULL;

  woempa(9, "e2fs_fsck done.\n");

}

