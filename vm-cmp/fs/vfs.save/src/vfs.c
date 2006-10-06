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

#include "stdio.h"
#include "string.h"

#include "vfs.h"
#include "vfs_errno.h"

#include "e2fs_prototypes.h"
#include "vfs_fcntl.h"

#include <stdlib.h>

char *current_working_dir = "/" ;
char *current_root_dir = "/" ;

vfs_filesystem_type  filesystem_list = NULL;  /* The master list of the known (registered) filesystem types */
vfs_dir_entry        dir_entry_list = NULL;   /* The master list of the directory entries */
vfs_mount_table      mount_table = NULL;      /* The mount table */
vfs_file             file_list = NULL;        /* The master list of open files */
vfs_DIR              *DIR_list = NULL;        /* The master list of open directories */
vfs_FILE             *FILE_list = NULL;       /* The master list of open streams */ 

w_word               global_errno = 0;

x_mutex              mutex_vfs;
x_mutex              mutex_vfs_lock;
x_thread             vfs_lock_thread = NULL;
w_int                vfs_lock_count = 0;

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_set_errno(w_word err) {
  global_errno = err;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_get_errno() {
  return global_errno;
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_register_filesystem(const vfs_filesystem_type type) {

  if(filesystem_list == NULL) {         /* Check if there is already a filesystem list */
    filesystem_list = type;             /* Nope -> this type is the first one */
    filesystem_list->list_next = NULL;  /* There is no next filesystem type */
  } else {                              
    type->list_next = filesystem_list;  /* Yep -> Move the whole list up */
    filesystem_list = type;             /* and make this one the first of the list */
  }

  woempa(7, "%s registered as a known filesystem type\n", type->name);
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_unregister_filesystem(vfs_filesystem_type type) {
  vfs_filesystem_type temp;
  
  temp = filesystem_list;
  
  woempa(3, "%s\n", type->name);
  
  if(filesystem_list == type) {              /* Is the filesystem type the first of the list ? */
    filesystem_list = type->list_next;       /* Yep, make the list point to the second one */
    releaseMem(type);
  } else {                                   
    while(temp != NULL) {                    /* Nope, traverse the list */
      if(temp->list_next == type) {          /* Is the next filesystem type the one to unregister ? */
        temp->list_next = type->list_next;   /* Yes -> Remove it */
        releaseMem(type);
      }
      temp = temp->list_next;                /* Got to the next entry in the list */
    }
  }
}

/* --------------------------------------------------------------------------------------------------------*/

vfs_filesystem_type vfs_get_filesystem_type(const w_ubyte *name) { /* Get a pointer to a vfs filesystem */ 
	                                                           /* structure by name  */
  vfs_filesystem_type temp = filesystem_list;
  vfs_filesystem_type result = NULL;
  
  woempa(3, "%s\n", name);

  while(result == NULL && temp != NULL) {
    if(strcmp(temp->name, name)==0) result = temp;
    temp = temp->list_next;
  }

  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_flush_dir_entry(vfs_dir_entry dir_entry) {  /* Flush a part of the directory entries tree */

  vfs_dir_entry child = dir_entry->child_list;         /* First child of this entry */
  vfs_dir_entry temp;                                  /* Temporary */

  if((strcmp(dir_entry->name, ".") != 0) && (strcmp(dir_entry->name, "..") != 0)) { 

    woempa(3, "name: %s\n", dir_entry->name);

    while(child != NULL) {                             /* Keep going as long as there are childeren */
      vfs_flush_dir_entry(child);                      /* Flush the child -> recursive */

      temp = child;                                    /* Temporary */
      child = child->next_entry;                       /* Go to the next child */
      // dir_entry->child_list = child;                   /* Remove the previous child in the child list */
      /* This line seemed to be the root of all evil in multithreaded filesystem access.. Still don't know why 
         but I do know it was a giant memory leak ! */
      if((strcmp(temp->name, ".") != 0) && (strcmp(temp->name, "..") != 0)) 
        temp->inode->sb->super_ops->write_inode(temp->inode);  /* write the fs inode */

    }
  }

  if(dir_entry->inode != dir_entry->mount) {                 /* This entry has another fs mounted on it */
    woempa(3, "Mounted !\n");
    dir_entry->mount->sb->super_ops->write_inode(dir_entry->mount);/* Write the mounted inode */
  }
  
}

/* --------------------------------------------------------------------------------------------------------*/

w_void vfs_cleanup_dir_entry(vfs_dir_entry dir_entry) {  /* Cleanup a part of the directory entries tree */

  vfs_dir_entry child = dir_entry->child_list;         /* First child of this entry */
  vfs_dir_entry temp;                                  /* Temporary */

  if((strcmp(dir_entry->name, ".") != 0) && (strcmp(dir_entry->name, "..") != 0)) { 

    woempa(3, "name: %s\n", dir_entry->name);

    while(child != NULL) {                             /* Keep going as long as there are childeren */
      vfs_cleanup_dir_entry(child);                    /* Cleanup the child -> recursive */

      temp = child;                                    /* Temporary */
      child = child->next_entry;                       /* Go to the next child */
      dir_entry->child_list = child;                   /* Remove the previous child in the child list */
      if((strcmp(temp->name, ".") != 0) && (strcmp(temp->name, "..") != 0)) 
        temp->inode->inode_ops->cleanup(temp->inode);  /* Cleanup the fs inode */
      releaseMem(temp->inode);                         /* Cleanup the vfs inode */
      releaseMem(temp);                                /* Cleanup the child */
      dir_entry_list->total_entries--;                 /* One dir_entry less */

    }
  }

  if(dir_entry->inode != dir_entry->mount) {                 /* This entry has another fs mounted on it */
    woempa(3, "Mounted !\n");
    dir_entry->mount->inode_ops->cleanup(dir_entry->mount);  /* Loose the mounted inode */
    releaseMem(dir_entry->mount);

    dir_entry->mount = dir_entry->inode;
  }
  
}

/* --------------------------------------------------------------------------------------------------------*/

vfs_dir_entry vfs_lookup_entry(vfs_dir_entry parent, const char *name) {
  vfs_dir_entry child = NULL;
  vfs_dir_entry temp;

  woempa(3, "%s in %s\n", name, parent->name);
  
  /* First search the directory entries */

  temp = parent->child_list;                         /* Start with the first child */
  while(temp != NULL && child == NULL) {             /* Keep going as long as there are childeren and */
                                                     /* nothing is found */
    if(strcmp(name, temp->name)==0) child = temp;    /* Compare the names */
    temp = temp->next_entry;                         /* Go to the next child */
  }
  
  /* if nothing is found, search the underlying filesystem */
  
  if(child == NULL) {

    child = allocMem(sizeof(vfs_Dir_Entry));          /* Allocate memory for this new child */
    memset(child, 0, sizeof(vfs_Dir_Entry));
    child->name = allocMem((w_size)strlen(name) + 1); /* Allocate memory for the filename */
    child->inode = NULL;                                          /* No inode yet */

    strcpy(child->name, name);                                    /* Copy the filename to the dir_entry */

    if(parent->inode != NULL) {                                   /* Is there an inode ? e.g unmounted root */
      if(parent->inode == parent->mount) {
        parent->inode->inode_ops->lookup(parent->inode, child);   /* Lookup the name in the filesystem */
      } else {
        parent->mount->inode_ops->lookup(parent->mount, child);   /* Lookup the name in the mounted filesystem */
      }
    }

    if(child->inode != NULL) {                                    /* If inode is not NULL, then a file is found */ 
      child->parent = parent;                                     /* Make a reference to the parent */
      child->mount = child->inode;                                /* Make mount point to the same inode */
      child->next_entry = parent->child_list;                     /* Put the child in the childlist of the parent */
      parent->child_list = child;                            
      dir_entry_list->total_entries++;                            /* One more directory entry */
    } else { 
      releaseMem(child->name);
      releaseMem(child);                                                /* No file is found, release the memory */
      child = NULL;
    }

  }

  return child;  /* Return the found directory entry, or NULL if nothing was found */

}

/* --------------------------------------------------------------------------------------------------------*/

vfs_dir_entry vfs_lookup_fullpath(const vfs_dir_entry root_entry, const char *fullpath) {

  w_ubyte        c;
  w_ubyte        *name;
  vfs_dir_entry  result = root_entry;
  w_ubyte        *path;
  w_ubyte        *workpath;

  woempa(3, "%s in %s\n", fullpath, root_entry->name);

  path = allocMem((w_size)(strlen(fullpath) + 1));

  strcpy(path, fullpath);

  workpath = path;
    
  c = *path;                   /* Check if the path starts with a slash */
  if(c == '/') {               /* If so, start reading from the root directory */ 
    result = dir_entry_list;   /* instead of the given directory */
  };

  while(((name = strsep((char **)&workpath, "/")) != NULL) && (result != NULL)) { 
	                       /* Split the path in pieces, a slash as delimeter  */
    c = name[0];               /* if c, the first char of name == \0, then name is empty and ignored */
    if(c != '\0') result = vfs_lookup_entry(result, name);
    
    if(strcmp("..", name)==0) result = result->parent->parent;
    if(strcmp(".", name)==0) result = result->parent;
    
                               /* Result points now to the next piece of path, or NULL if it didn't exist */
  }

  releaseMem(path);            /* Release some memory */

  return result;               /* At the end, result will be the directory entry of */
                               /* last entry in the path, or NULL in case of an error */

}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_mount(const w_ubyte *device_id, const w_ubyte *dir, const w_ubyte *fs_name, w_word flags) {

  vfs_filesystem_type fs;     /* Get the filesystem type */
  vfs_superblock      sb;
  vfs_inode           inode;
  vfs_mount_table     mount;
  vfs_dir_entry       dir_entry;
                                                                 /* Get the dir_entry for this dir */
  int                 result = -1;                               /* Default to 'not successful' */
  
  vfs_lock();

  fs = vfs_get_filesystem_type(fs_name);                         /* Get the filesystem type */
  dir_entry = vfs_lookup_fullpath(dir_entry_list, dir);
  
  woempa(7, "Mounting %s (%s) on %s\n", device_id, fs_name, dir);
  
  if(dir_entry != NULL) {
    if(fs != NULL) {                                             /* Valid filesystem found ? */

      sb = allocMem(sizeof(vfs_Superblock));    /* Allocate memory for the superblock */
      inode = allocMem(sizeof(vfs_Inode));      /* Allocate memory for the root inode */

      memset(sb, 0, sizeof(vfs_Superblock));                     /* Clear the superblock */
      memset(inode, 0, sizeof(vfs_Inode));                       /* Clear the inode */

      sb->device = (w_ubyte *)device_id;                         /* Set device */
      sb->flags = flags;                                         /* Set flags */
      woempa(7,"Reading (%p) superblock from device %s, flags %x\n",fs->read_superblock,device_id,flags);
      fs->read_superblock(sb);                                   /* Read in the superblock */
      inode->sb = sb;                                            /* Make a reference in the inode to the sb */
      inode->nr = sb->root_inode_nr;                             /* Get the root inode number */
      woempa(7,"Reading root inode (%d)\n",inode->nr);
      sb->super_ops->read_inode(inode);                          /* Read in this inode */
    
      vfs_cleanup_dir_entry(dir_entry);                          /* Cleanup the mount point */

      dir_entry->mount = inode;                                  /* Put the inode in the dir_entry of the */
                                                                 /* mount point */

      if(dir_entry->inode==NULL) dir_entry->inode = dir_entry->mount;  /* in case dir_entry is for '/' */

      /* Update the mount table */

      mount = allocMem(sizeof(vfs_Mount_Table)); /* Allocate memory for a mount */
                                                                  /* table entry */
      memset(mount, 0, sizeof(vfs_Mount_Table));
      mount->mount_point = allocMem((w_size)strlen(dir)+1);    /* Allocate memory for the */
                                                                                /* mount point */
      strcpy(mount->mount_point, dir);                           /* Copy the mount point */
      mount->superblock = sb;                                    /* Store the superblock */
      mount->fs = fs;                                            /* Make link to the fs type */
      mount->list_next = mount_table;                            /* Add this entry to the list */
      mount_table = mount;

      result = 0;
    }
  }

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_word vfs_umount(const w_ubyte *dir) {

  vfs_mount_table     mount;
  vfs_mount_table     temp;
  vfs_dir_entry       dir_entry;
  int                 result = -1;                               /* Default to 'not successful' */
  
  vfs_lock();

  mount = mount_table;
  dir_entry = vfs_lookup_fullpath(dir_entry_list, dir);          /* Get the dir_entry for this dir */
  
  woempa(4, "%s\n", dir);

  /* OLA: What about open files ?? */

  while((mount != NULL) && (strcmp(mount->mount_point, dir) != 0)) mount = mount->list_next;
  
  if(mount != NULL) {

    vfs_cleanup_dir_entry(dir_entry);                          /* Cleanup the mount point */

    mount->superblock->super_ops->close_fs(mount->superblock);
    
    /* Update the mount table */

    temp = mount_table;
    if(mount_table == mount) {
      mount_table = mount_table->list_next;
      releaseMem(mount);
    } else {
      while(temp != NULL && temp->list_next != NULL) {
        if(temp->list_next == mount) {
          temp->list_next = temp->list_next->list_next;
          releaseMem(mount);
        }
        temp = temp->list_next;
      }
    }

    result = 0;
  }

  vfs_unlock();
  
  return result;
}

/* --------------------------------------------------------------------------------------------------------*/

w_void init_vfs() {
  mutex_vfs  = allocMem(sizeof(x_Mutex));
  mutex_vfs_lock  = allocMem(sizeof(x_Mutex));

  x_mutex_create(mutex_vfs);
  x_mutex_create(mutex_vfs_lock);
 
  /* Build the root directory entry */

  dir_entry_list = allocMem(sizeof(vfs_Dir_Entry));
 
  dir_entry_list->name = (char *)"/";
  dir_entry_list->parent = NULL;
  dir_entry_list->child_list = NULL;
  dir_entry_list->next_entry = NULL;
  dir_entry_list->total_entries = 1;
  dir_entry_list->inode = NULL;
  dir_entry_list->mount = NULL;

  woempa(7, "virtual filesystem initialized\n");
}

/* --------------------------------------------------------------------------------------------------------*/

w_void close_vfs() {
  	
  while(mount_table != NULL) {
    vfs_umount(mount_table->mount_point);
  }

  releaseMem(dir_entry_list);

  x_mutex_delete(mutex_vfs); 
  x_mutex_delete(mutex_vfs_lock); 
  
  releaseMem(mutex_vfs);
  releaseMem(mutex_vfs_lock);
  
  woempa(7, "virtual filesystem closed.\n");

}

