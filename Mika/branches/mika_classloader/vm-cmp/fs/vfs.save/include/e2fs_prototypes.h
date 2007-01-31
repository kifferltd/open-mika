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


w_void e2fs_read_superblock(vfs_superblock superblock);
w_void e2fs_read_inode_from_fs(e2fs_filesystem fs, w_word inode_nr, e2fs_inode e2fs_ino);
w_void e2fs_read_inode(vfs_inode inode);
w_void e2fs_delete_inode(vfs_inode inode);
w_void e2fs_write_inode(vfs_inode inode);
w_word e2fs_create_inode(vfs_inode inode, vfs_dir_entry dir_entry);
w_word e2fs_mkdir(vfs_inode inode, vfs_dir_entry dir_entry);
w_void e2fs_rmdir(vfs_inode inode, vfs_dir_entry dir_entry);
w_void e2fs_rename(vfs_inode inode1, vfs_dir_entry dir_entry1, vfs_inode inode2, vfs_dir_entry dir_entry2);
w_void e2fs_inode_truncate(vfs_inode inode);
w_void e2fs_unlink_inode(vfs_inode dir_inode, vfs_dir_entry dir_entry);
w_void e2fs_write_superblock(vfs_superblock superblock);
w_void e2fs_close_filesys(vfs_superblock sb);
w_void e2fs_sync(vfs_superblock sb);

w_void e2fs_get_block(vfs_inode inode, w_ubyte *buffer, w_word block_nr);
w_word e2fs_put_block(vfs_inode inode, w_ubyte *buffer, w_word block_nr);
vfs_dir_entry e2fs_lookup_inode(vfs_inode inode, vfs_dir_entry dir_entry);
w_void e2fs_inode_cleanup(vfs_inode inode);

struct vfs_Filesystem_Type e2fs_filesystem_type;
struct vfs_Super_Operations e2fs_super_operations;
struct vfs_Inode_Operations e2fs_inode_operations;

w_void e2fs_write_ino_cache(e2fs_filesystem fs, e2fs_inode_cache ino_cache);
w_void init_e2fs(void);

w_void e2fs_read_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word size, w_ubyte *buffer);
w_void e2fs_write_block(e2fs_filesystem fs, e2fs_block_nr phys_block, w_word size, w_ubyte *buffer);

w_void e2fs_inode_write_block_list(e2fs_filesystem fs, e2fs_inode_cache ino_cache);
w_word *e2fs_inode_read_block_list(e2fs_filesystem fs, e2fs_inode inode);

w_void e2fs_read_bitmaps(e2fs_filesystem fs);
w_void e2fs_write_bitmaps(e2fs_filesystem fs);

w_void e2fs_bitmap_set_inode(e2fs_filesystem fs, e2fs_inode_nr inode, w_word bit);
w_ubyte e2fs_bitmap_test_inode(e2fs_filesystem fs, e2fs_inode_nr inode);
w_void e2fs_bitmap_set_block(e2fs_filesystem fs, e2fs_block_nr block, w_word bit);
w_ubyte e2fs_bitmap_test_block(e2fs_filesystem fs, e2fs_block_nr block);

w_word e2fs_inode_get_phys_block(e2fs_filesystem fs, e2fs_inode inode, e2fs_inode_nr inode_nr, e2fs_block_nr block_nr); 
w_void e2fs_alloc_block(e2fs_filesystem fs, e2fs_inode_cache ino_cache, e2fs_block_nr block_nr, int alloc);
w_word e2fs_block_to_blocklist(e2fs_filesystem fs, w_word block);

w_void e2fs_read_dir(vfs_inode inode, w_ubyte *buffer);

w_void e2fs_fsck(e2fs_filesystem fs);

