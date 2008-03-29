/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2003 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

package java.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

public class File implements Comparable, Serializable {

  private static final long serialVersionUID = 301077366599181567L;

  static Vector toBeDeleted;
  
  public static final String separator;
  public static final char   separatorChar;
  public static final String pathSeparator;
  public static final char   pathSeparatorChar;

  private static String current_working_dir = null;
  private static String fsroot_dir = null;
  private static String fsrootedPrefix;

  /**
   ** Name by which the File object was created ('path' parameter of the constructor).
   */
  private transient  String     fullname;
  /*
  ** If the path begins with '/', this is stripped off and 'absolute' is set.
  ** If the path begins with '{}/', this is stripped off and 'fsrooted' is set.
  ** The remainder of the path is then split at the last '/':
  **  - everything up to the last '/' (exclusive) goes in 'dirpath'.
  **  - everything after the last '/' (exclusive) goes in 'filename'.
  */
  private transient  boolean    absolute;
  private transient  boolean    fsrooted;
  private transient  String     dirpath;
  private transient  String     filename;

  /**
   ** The absolute path used to open the file, after path mungeing.
   */
  private transient  String     absolutePath;
  /**
   ** Same as absolutePath, but after eliminating './' and '../' sequences.
   ** Generated on demand by toURL().
   */
  private transient String canonicalPath;

  /**
   ** Same as absolutePath. Used by the native functions to access the file.
   */
  final transient String absname;

  private static native String get_CWD();
  private static native String get_fsroot();
  private static native void init();
  
  static {
    init();
    
    separator = GetSystemProperty.FILE_SEPARATOR;
    pathSeparator = GetSystemProperty.PATH_SEPARATOR;
    separatorChar = separator.charAt(0);
    pathSeparatorChar = separator.charAt(0);
    fsrootedPrefix = "{}" + separator;

    current_working_dir = get_CWD();
    fsroot_dir = get_fsroot() + separator;
    current_working_dir = current_working_dir + separator;
    System.getProperties().setProperty("user.dir", current_working_dir);
  }

  public static File[] listRoots() {
    return new File[] { new File("/") };
  }

  public static File createTempFile(String pre, String suf) throws IOException {
    return createTempFile(pre, suf, null);
  }

  public static File createTempFile(String pre, String suf, File dir) throws IOException {
    if(dir == null) {
      dir = new File(System.getProperties().getProperty("java.io.tmpdir", "/tmp"));
    }
    
    if(suf == null) {
      suf = ".tmp";
    }

    if(pre == null) {
      pre = "";
    }

    File result = new File(dir, pre + (new Random()).nextLong() + suf);
    result.createNewFile();

    return result;
  }

  private String pack(String path) {
    String result = "";
    LinkedList list = new LinkedList();
    StringTokenizer st = new StringTokenizer(path, "/");

     try {
      while (true) {
        String token = st.nextToken();
        if(!token.equals(".")) { 
          if(!token.equals("..")) {
            list.add(token);
          } else {
            if(list.size() > 0)
              list.removeLast();
          }
        }
      }
    }
    catch(NoSuchElementException e) {
    }

    Iterator iter = list.iterator();
    while(iter.hasNext()) 
      result += "/" + (String)iter.next();
      
    if(!path.startsWith("/") && (result.length() > 0)) result = result.substring(1);
    //if(path.endsWith("/")) result += "/";

    return result; 
  }

  private String stripSlashes(String path) {
    String result = path;
    int i = result.indexOf("//");

    while (i >= 0) {
      result  = result.substring(0, i) + result.substring(i + 1);
      i = result.indexOf("//");
    }
    return result;
  }

  public File(String path) throws NullPointerException {
    String relpath;
    filename = "";

    if(path.length() == 0) path = current_working_dir;

    if(path.charAt(0) == separatorChar) {  // Absolute path
      relpath = path.substring(1);
      absolute = true;
    } else if(path.startsWith(fsrootedPrefix)) {  // fsrooted path
      relpath = path.substring(3);
      fsrooted = true;
    } else {
      relpath = path;
    }

    try {

      int dirpathlen = relpath.lastIndexOf(separatorChar);
      if (dirpathlen < 0) {
        dirpath = "";
        filename = relpath;
      } else {
        dirpath = relpath.substring(0, dirpathlen);
        filename = relpath.substring(dirpathlen + 1);
      }
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
    }

    fullname = stripSlashes(path);
    if (absolute) {
      absolutePath = fullname;
    }
    else if (fsrooted) {
      absolutePath = fsroot_dir + fullname.substring(3);
    }
    else {
      absolutePath = current_working_dir + fullname;
    }
    int len = absolutePath.length() - 1;
    if(len >= 0 && absolutePath.charAt(len) == '/') {
      absolutePath = absolutePath.substring(0, len);
    }

    
    absname = absolutePath;
  }

  public File(String dirname, String name) throws NullPointerException {
    this(name == null ? null : (dirname == null ? "" : (dirname.equals("") ? "" : dirname + separatorChar)) + name);
  }

  public File(File dir, String name) throws NullPointerException {
    this((dir == null ? "" : dir.getPath()), name);
  }

  public String toString() {
    return fullname;
  }

  public boolean equals(Object obj) {
    if ((obj != null) && obj instanceof File) {
      return (((File)obj).getPath().equals(getPath()));
    }
    return false;
  }

  public int compareTo(File pathname) {
    return (getPath().compareTo(pathname.getPath()));
  }

  public int compareTo(Object obj) {
    if (obj instanceof File) {
      return (absolutePath.compareTo(((File)obj).getAbsolutePath()));
    } else { throw new ClassCastException(); }
  }

  public int hashCode() {
    return getPath().hashCode() ^ 1234321;
  }

  public String getName() {
    String result = new String(filename);
    int snip = filename.lastIndexOf(separatorChar);
    if (snip >= 0) result = result.substring(snip+1);

    return result;
  }

  public String getPath() {
    return fullname;
  }

  public String getAbsolutePath() {
    return absolutePath;
  }

  public File getAbsoluteFile() {
    return new File(absolutePath);
  }

  public String getCanonicalPath() throws IOException {
    if(canonicalPath == null) {
      canonicalPath = pack(absolutePath);
    }
    return canonicalPath;
  }

  public File getCanonicalFile() throws IOException {
    return new File(getCanonicalPath());
  }

  public String getParent() {
    if (dirpath.length() + filename.length() == 0) {

      return null;

    }

    String prefix = absolute ? separator : fsrooted ? fsrootedPrefix : "";

    if (filename.length() == 0) {

      return new File(prefix + dirpath).getParent();
      
    }

    return prefix + dirpath;
  }

  public File getParentFile() {
    String parentName = getParent();
 
    if (parentName == null) {
      return null;
    }
 
    return new File(parentName);
  }

  public boolean isAbsolute() {
    return absolute;
  }

  public boolean isHidden() {
    return (filename.charAt(0) == '.');
  }
  
  public boolean createNewFile() throws IOException {
    if(!this.exists()) {
      try {
        FileOutputStream fo = new FileOutputStream(this);
        fo.close();
        return true;
      } catch(FileNotFoundException e) {
        throw new IOException("Unable to create " + this);
      }
    }
    return false;
  }

  public native boolean exists() throws SecurityException;

  public native String[] list() throws SecurityException;

  public String[] list(FilenameFilter filter) throws SecurityException {
    String[] files = this.list();
    if(files == null){
      return null;
    }

    ArrayList alist = new ArrayList(files.length);
    for(int i=0; i < files.length; i++){
      if(filter.accept(this, files[i])){
        alist.add(files[i]);
      }
    }
    String[] result = new String[alist.size()];
    return (String[])alist.toArray(result);
  }

  public File[] listFiles() throws SecurityException {
    String[] filenames = this.list();
    if(filenames == null){
      return null;
    }
    File[] files = new File[filenames.length];
    for(int i=0; i<filenames.length; i++) 
      files[i] = new File(this, filenames[i]);
    return files;
  }

  public File[] listFiles(FilenameFilter filter) throws SecurityException {
    String[] filenames = this.list(filter);
    if(filenames == null){
      return null;
    }
    File[] files = new File[filenames.length];
    for(int i=0; i<filenames.length; i++)
      files[i] = new File(this, filenames[i]);
    return files;
  }

  public File[] listFiles(FileFilter filter) throws SecurityException {
    File[] files = this.listFiles();
    if(files == null){
      return null;
    }
    ArrayList alist = new ArrayList(files.length);
    for(int i=0; i < files.length; i++){
      if(filter.accept(files[i])){
        alist.add(files[i]);
      }
    }
    File[] result = new File[alist.size()];
    return (File[])alist.toArray(result);
  }

  public native boolean canRead() throws SecurityException;

  public native boolean canWrite() throws SecurityException;

  public native boolean isFile() throws SecurityException;

  public native boolean isDirectory() throws SecurityException;

  public native long lastModified() throws SecurityException;

  public native boolean setLastModified(long time) throws SecurityException;

  public void deleteOnExit() throws SecurityException {
    if(toBeDeleted == null) {
      createShutdownHook();
    }
    toBeDeleted.add(this);
  }

  private synchronized static void  createShutdownHook(){
    if(toBeDeleted == null) {
      toBeDeleted = new Vector();
      Runtime.getRuntime().addShutdownHook(new Thread("File:deleteOnExit") {
        public void run() {
          Iterator iter = File.toBeDeleted.iterator();
          while(iter.hasNext()) {
            File f = (File)iter.next();
            try {
              if(f != null) f.delete();
            }
            catch(Exception e) {
            }
          }
        }
      });
    }
  }

  public boolean setReadOnly() throws SecurityException {
    return false;
  }

  public native long length() throws SecurityException;

  public native boolean mkdir() throws SecurityException;

  public boolean mkdirs() throws SecurityException {
    boolean result = false;
    if(!absolutePath.equals(separator) && !absolutePath.equals(fsrootedPrefix)) {
      File parent = getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      result = mkdir();
    }
    return result;
  }

  private native boolean rename(String src, String dest);
  
  public boolean renameTo(File dest) throws SecurityException {
    return rename(absolutePath, dest.getAbsolutePath());
  }

  public native boolean delete() throws SecurityException;

  public URL toURL() throws MalformedURLException {
    if(canonicalPath == null) {
      canonicalPath = pack(absolutePath);
    }
    return new URL("file", "", canonicalPath);
  }

}
