/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2003, 2009 by /k/ Embedded Java Solutions.          *
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import wonka.vm.SecurityConfiguration;

public class File implements Comparable, Serializable {

  private static final long serialVersionUID = 301077366599181567L;

  static Vector toBeDeleted;
  
  public static final String separator;
  public static final char   separatorChar;
  public static final String pathSeparator;
  public static final char   pathSeparatorChar;

  private static String current_working_dir;
  private static String fsroot_dir;
  private static String fsrootedPrefix;
  private static Random prng;

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
  private transient  boolean    empty;
  private transient  boolean    absolute;
  private transient  boolean    fsrooted;
  private transient  String     dirpath;
  private transient  String     filename;
  private transient  int        hashcode;

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
  
  private static String extractPathFromURI(URI uri) throws IllegalArgumentException {
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute: " + uri);
    }

    if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
      throw new IllegalArgumentException("URI is not hierarchical: " + uri);
    }

    if (!"file".equalsIgnoreCase(uri.getScheme())) {
      throw new IllegalArgumentException("URI has wrong scheme: " + uri);
    }

    if (uri.getRawPath() == null || uri.getRawPath().length() == 0) {
      throw new IllegalArgumentException("URI has no path: " + uri);
    }

    if (uri.getRawAuthority() != null) {
      throw new IllegalArgumentException("URI contains an authority: " + uri);
    }

    if (uri.getRawQuery() != null) {
      throw new IllegalArgumentException("URI contains a query: " + uri);
    }

    if (uri.getRawFragment() != null) {
      throw new IllegalArgumentException("URI contains a fragment: " + uri);
    }


    return uri.getPath();
  }

  static {
    separator = GetSystemProperty.FILE_SEPARATOR;
    pathSeparator = GetSystemProperty.PATH_SEPARATOR;
    separatorChar = separator.charAt(0);
    pathSeparatorChar = pathSeparator.charAt(0);
    fsrootedPrefix = "{}" + separator;

    current_working_dir = get_CWD();
    fsroot_dir = get_fsroot() + separator;
    current_working_dir = current_working_dir + separator;
    // TODO: shouldn't we have a GetSystemProperty-like mechanism here?
    System.getProperties().setProperty("user.dir", current_working_dir);
  }

  public static File[] listRoots() {
    return new File[] { new File("/") };
  }

  public static File createTempFile(String pre, String suf) throws IllegalArgumentException, IOException {
    return createTempFile(pre, suf, null);
  }

  public static synchronized File createTempFile(String pre, String suf, File dir) throws IllegalArgumentException, IOException, SecurityException {
    if(dir == null) {
      dir = new File(GetSystemProperty.TMPDIR);
    }
    
    if(suf == null) {
      suf = ".tmp";
    }

    if(pre == null || pre.length() < 3) {
      throw new IllegalArgumentException("prefix must be at least three chars: " + pre);
    }

    if (prng == null) {
      prng = new Random();
    }

    File result = new File(dir, pre + prng.nextLong() + suf);
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

    return result; 
  }

  private String stripSlashes(String path) {
    String result = path;
    int i = result.indexOf("//");

    while (i >= 0) {
      result  = result.substring(0, i) + result.substring(i + 1);
      i = result.indexOf("//");
    }

    i = result.lastIndexOf('/');
    if (i > 0 && i == result.length() - 1) {
      result = result.substring(0, i);
    }
 
    return result;
  }

  public File(String path) throws NullPointerException {
    String relpath;
    hashcode = path.hashCode() ^ 1234321;

    if(path.length() == 0) {
      path = current_working_dir;
      relpath = ".";
      empty = true;
    } else if(path.charAt(0) == separatorChar) {  // Absolute path
      relpath = path.substring(1);
      absolute = true;
    } else if(path.startsWith(fsrootedPrefix)) {  // fsrooted path
      relpath = path.substring(3);
      fsrooted = true;
    } else {
      relpath = path;
    }

    relpath = stripSlashes(relpath);

    try {

      int dirpathlen = relpath.lastIndexOf(separatorChar);

      if (dirpathlen < 0) {
        dirpath = null;
        filename = relpath;
      } else {
        dirpath = relpath.substring(0, dirpathlen);
        filename = relpath.substring(dirpathlen + 1);
      }
      //int l = filename.length();
      //while (l > 1 && filename.charAt(l - 1) == '/') {
      //  filename = filename.substring(0, --l);
      //}
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
    }

    fullname = empty ? "" : stripSlashes(path);
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
    if(len > 0 && absolutePath.charAt(len) == '/') {
      absolutePath = absolutePath.substring(0, len);
    }

    
    absname = absolutePath;
  }

  public File(String dirname, String name) throws NullPointerException {
    this(name == null ? null : (dirname == null ? "" : (dirname.equals("") ? "" : dirname.endsWith("/") ? dirname : dirname + separatorChar)) + name);
  }

  public File(File dir, String name) throws NullPointerException {
    this((dir == null ? "" : dir.getPath()), name);
  }

  public File(URI uri) throws NullPointerException, IllegalArgumentException {
    this(extractPathFromURI(uri));
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
    return hashcode;
  }

  public String getName() {
    if (empty) {
      return "";
    }

    String result = new String(filename);
    int snip = filename.lastIndexOf(separatorChar);
    if (snip >= 0) {
      result = result.substring(snip+1);
    }

    return result;
  }

  public String getPath() {
    return empty ? "" : fullname;
  }

  public String getAbsolutePath() {
    // bizarre but it seems to be what Sun is doing
    if (empty) {
      return "./";
    }
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
    if (dirpath == null || dirpath.length() + filename.length() == 0) {

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

  public boolean isHidden() throws SecurityException {
    readCheck(); 
    return (filename.charAt(0) == '.');
  }
  
  // TODO: this is supposed to be atomic wrt any other file ops!
  // Well at least we can make the method synchronized ...
  public synchronized boolean createNewFile() throws IOException, SecurityException {
    if(!this.exists()) {
      writeCheck();
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

  public boolean exists() throws SecurityException {
    readCheck();
    return _exists();
  }

  public native boolean _exists();

  public String[] list() throws SecurityException {
    readCheck();
    return _list();
  }

  public native String[] _list();

  public String[] list(FilenameFilter filter) throws SecurityException {
    String[] files = this.list();

    if (files == null){
      return null;
    }

    if (filter == null){
      return files;
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

    if (files == null){
      return null;
    }

    if (filter == null){
      return files;
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

  public boolean canRead() throws SecurityException {
    readCheck();
    return _canRead();
  }

  public native boolean _canRead();

  public boolean canWrite() throws SecurityException {
    writeCheck();
    return _canWrite();
  }

  public native boolean _canWrite();

  public boolean isFile() throws SecurityException {
    readCheck();
    return _isFile();
  }

  public native boolean _isFile();

  public boolean isDirectory() throws SecurityException {
    readCheck();
    return _isDirectory();
  }

  public native boolean _isDirectory();

  public long lastModified() throws SecurityException {
    readCheck();
    return _lastModified();
  }

  public native long _lastModified();

  public boolean setLastModified(long time) throws SecurityException {
    writeCheck();
    return _setLastModified(time);
  }

  public native boolean _setLastModified(long time);

  public void deleteOnExit() throws SecurityException {
    deleteCheck();
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
    writeCheck();
    if (empty) {
      // imitate Sun
      return false;
    }

    return _setReadOnly();
  }

  public native boolean _setReadOnly();

  public long length() throws SecurityException {
    readCheck();
    return _length();
  }

  public native long _length();

  public boolean mkdir() throws SecurityException {
    writeCheck();
    return _mkdir();
  }

  public native boolean _mkdir();

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

  private native boolean _rename(String src, String dest);
  
  public boolean renameTo(File dest) throws SecurityException {
    writeCheck();
    dest.writeCheck();
    return _rename(absolutePath, dest.getAbsolutePath());
  }

  public boolean delete() throws SecurityException {
    deleteCheck();
    return _delete();
  }

  public native boolean _delete();

  public URL toURL() throws MalformedURLException {
    if (empty) {
      return new URL("file:./");
    }

    if(canonicalPath == null) {
      canonicalPath = pack(absolutePath);
    }
    return new URL("file", "", isDirectory() ? canonicalPath + "/" : canonicalPath);
  }

  public URI toURI() {
    String uriPath = empty ? "./" : canonicalPath;

    if(uriPath == null) {
      canonicalPath = pack(absolutePath);
      uriPath = canonicalPath;
    }

    try {
      return new URI("file", null, uriPath, null, null);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private void readCheck() {
    if (SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkRead(fullname);
      }
    }
  }

  private void writeCheck() {
    if (SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkWrite(fullname);
      }
    }
  }

  private void deleteCheck() {
    if (SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkDelete(fullname);
      }
    }
  }
}
