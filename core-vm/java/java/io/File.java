/**************************************************************************
* Parts copyright (c) 2009, 2022, 2023 by Chris Gray, KIFFER Ltd.         *
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
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

package java.io;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import wonka.vm.Etc;
import wonka.vm.SecurityConfiguration;

/**
 * Representation of an abstract path name which can be resolved to a file.
 *
 * This implementation aims to be usable both on unixey filesystems and on
 * dossy systems such as FreeRTOS+FAT.
 */
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
  private static final String delimiter;

  /**
   * Pathname by which the File object was created. 
   * Compared to the pathname string used in the constructor, alternative
   * separators are replaced by {@link #separator} and repeated separator
   * characters are replaced by a single character, but canonicalisation
   * is not performed (so sequences such as "./" or "../" are left in place).
   */
  private transient  String     fullname;

  /**
   * The 'prefix' part of {@link #fullname}.
   * The prefix can be any of the following:
   *  - '/' for an absolute path in unices
   *  - one letter plus ':\\' for an absolute path in dosland
   *  - one letter plus ':' for a relative path in dosland
   *  - '//' for a UNC pathname in dosland
   *  - empty for all relative paths in unices, or a driveletter-free relative path in dosland.
  */
  private transient  String     prefix;

  private static final String UNIX_PREFIX = "/";
  private static final String UNC_PREFIX = "\\\\";
  private static final String ALT_UNC_PREFIX = "//";
  private static final String ROMAN_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * The name-list contains one String for each component of the path.
   * The prefix is NOT present in this list.
   * The last component may be a filename or a directory name.
   */
  private transient  List       name_list;

  /**
   * Set true iff the File was created with an empty path (and no parent).
   */
  private transient  boolean    empty;

  /**
   * Set true iff the File represents an absolute path.
   */
  private transient  boolean    absolute;

  /**
   * Set true iff the File represents a fsrooted path.
   */
  private transient  boolean    fsrooted;

  /**
   * The dosland drive letter if present, else '\0'.
   */
  private transient  char       driveletter;

  /**
   * The part of the path up to the last '/' character (DEPRECATED).
   */
  private transient  String     dirpath;

  /**
   * The part of the path after the last '/' character (DEPRECATED).
   */
  private transient  String     filename;

  /**
   * The hashcode of the File object, calculated on first use.
   */
  private transient  int        hashcode;

  /**
   ** The absolute path used to open the file, after path mungeing. (DEPRECATED)
   */
  private transient  String     absolutePath;

  /**
   ** Same as absolutePath, but after eliminating './' and '../' sequences.
   ** Generated on demand by toURL().
   */
  private transient String canonicalPath;

  /**
   ** Path used by the native functions to access the file.
   ** May or may not be the same as absolutePath.
   */
  final transient String hostpath;

  /**
   * Get the Current Working Directory from the OS.
   */
  private static native String get_CWD();

  /**
   * Get the path which is to be substituted for the fsroot prefix.
   */
  private static native String get_fsroot();
  
  /**
   * Extract the path element from a file: URI.
   *
   * @param  uri  URI from which the file path is to be extracted.
   *
   * @throws IllegalArgumentException if uri is not an absolute, hierarchical
   * URI with protocol "file", a non-empty path component, and no authority,
   * query, or fragment part.
   */
  private static String extractPathFromURI(URI uri) throws IllegalArgumentException {
    Etc.woempa(7, "Extracting Path from URI " +  uri);
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute: " + uri);
    }

    if (!uri.getRawSchemeSpecificPart().startsWith(UNIX_PREFIX)) {
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
    delimiter = separator == "\\" ? "\\/" : separator;

    current_working_dir = get_CWD();
    if (!current_working_dir.endsWith(separator)) {
      current_working_dir = current_working_dir + separator;
    }
    fsroot_dir = get_fsroot();
    if (!fsroot_dir.endsWith(separator)) {
      fsroot_dir = fsroot_dir + separator;
    }
    // TODO: shouldn't we have a GetSystemProperty-like mechanism here?
    System.getProperties().setProperty("user.dir", current_working_dir);
  }

  // TODO link to mount table when we have one
  public static File[] listRoots() {
    return new File[] { new File(UNIX_PREFIX) };
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
    Etc.woempa(7, "stripSlashes(" + path + ") => " + result);
 
    return result;
  }

  /**
   * Extract the prefix part from the given pathname string, and set zero
   * or more of the booleans {@link #absolute}, {@link #fsrooted}, {@link #empty}.
   * If the prefix contains a drive letter, store this in {@link #driveletter}.
   *
   * @param pathname the pathname string to be analysed
   * @return the length of the prefix part
   * @throw  NullPointerException if pathname is null
   */ 
  private int extractPrefix(String pathname) {

    Etc.woempa(7, "Extracting prefix from '" + pathname + "'");
    if (pathname.startsWith(UNIX_PREFIX)) {
      prefix = UNIX_PREFIX;
      absolute = true;
    }
    else if (pathname.startsWith(fsrootedPrefix)) {
      prefix = fsrootedPrefix;
      fsrooted = true;
      absolute = true;
    }
    // TODO should we also allow "//"?
    else if (pathname.startsWith(UNC_PREFIX) || pathname.startsWith(ALT_UNC_PREFIX)) {
      prefix = UNC_PREFIX;
      absolute = true;
    }
    else if (pathname.length() > 1
          && ROMAN_ALPHABET.indexOf(pathname.charAt(0)) >= 0
          && pathname.charAt(1) == ':'
            ) {
      driveletter = pathname.charAt(0);
      absolute = pathname.length() > 2 && pathname.charAt(2) ==  '\\';
      prefix = pathname.substring(0, absolute ? 3 : 2);
    }
    else {
      prefix = "";
    }
    Etc.woempa(7, "prefix = '" + prefix + "' absolute = " + absolute + " fsrooted = " + fsrooted);

    empty = pathname.length() == 0;

    return prefix.length();
  }

  public File(String name) throws NullPointerException {
    this((File)null, name);
  }

  public File(String dirname, String name) throws NullPointerException {
    this(dirname == null ? null : new File(dirname), name);
  }

  public File(File dir, String name) throws NullPointerException {
    if (dir == null) {
      Etc.woempa(7, "Parent path is null, file path is '" + name + "'");
      int pfxlen = extractPrefix(name);
      String name_seq = name.substring(pfxlen);
      Etc.woempa(7, "Prefix is '" + prefix + "', name_seq is '" + name_seq +"'");

      name_list = new ArrayList();
      StringTokenizer toks = new StringTokenizer(name_seq, delimiter);
      while (toks.hasMoreTokens()) {
        String element = toks.nextToken();
/* No - leave the little blighters in there
        if (".".equals(element)) {
          continue;
        }
        if ("..".equals(element)) {
          name_list.remove(name_list.size() - 1);
          continue;
        }
*/
        name_list.add(element);
      }

    }
    else {
      prefix = dir.prefix;
      name_list = dir.name_list;
      empty = dir.empty && name.length() == 0;
      absolute = dir.absolute;
      fsrooted = dir.fsrooted;
      Etc.woempa(7, "Parent path is '" + dir + "', prefix is '" + prefix + "'");
      File temp = new File(name);
      Iterator nameIter = temp.name_list.iterator();
      while (nameIter.hasNext()) {
        name_list.add(nameIter.next());
      }
    }

// compatibility code: generate fullname and relpath
    StringBuffer relpath_buffer = null;
    Iterator pathIter = name_list.iterator();
    while (pathIter.hasNext()) {
      String element = (String)pathIter.next();
      Etc.woempa(7, "  path element: " + element);
      if (relpath_buffer == null) {
        relpath_buffer = new StringBuffer(element);
      }
      else  {
        relpath_buffer.append(separator);
        relpath_buffer.append(element);
      }
    }
    String relpath = relpath_buffer.toString();
    String fullname = empty ? "" : prefix + relpath;
    Etc.woempa(7, "fullname is " + fullname + ", relpath is " + relpath);

    hashcode = fullname.hashCode() ^ 1234321;

    // derive DEPRECATED fields
    try {

      int dirpathlen = relpath.lastIndexOf(separatorChar);

      if (dirpathlen < 0) {
        dirpath = null;
        filename = relpath;
      } else {
        dirpath = relpath.substring(0, dirpathlen);
        filename = relpath.substring(dirpathlen + 1);
      }
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
    }

    // TODO resolve and construct absolute path on first use
    if (absolute) {
      absolutePath = fullname;
    }
    else if (fsrooted) {
      absolutePath = fsroot_dir + relpath;
    }
    else {
      absolutePath = current_working_dir + relpath;
    }
    int len = absolutePath.length() - 1;
    if(len > 0 && absolutePath.charAt(len) == '/') {
      absolutePath = absolutePath.substring(0, len);
    }

    Etc.woempa(7, "File " + fullname + " is " + (absolute ? "absolute" : fsrooted ? "fsrooted" : "relative") + ", absolute path = " + absolutePath);
    hostpath = absolutePath;
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
    return empty ? "" : (String)name_list.get(name_list.size() - 1);
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
    // Need to have at least two path elements (including the prefix, if any)
    // otherwise we should return null.
    int nelems = name_list.size();
    if (prefix != null) {
      ++nelems;
    }
    if (nelems < 2) {
      return null;
    }

    StringBuffer parentbuf = new StringBuffer();
    if (prefix != null) {
      parentbuf.append(prefix);
    }
    Iterator parentiter = name_list.iterator();
    if (parentiter.hasNext()) {
      parentbuf.append(parentiter.next());
    }
    while (parentiter.hasNext()) {
      parentbuf.append(separator);
      parentbuf.append(parentiter.next());
    }

    return parentbuf.toString();
/* WAS:
    if (dirpath == null || dirpath.length() + filename.length() == 0) {

      return null;

    }

    String prefix = absolute ? separator : fsrooted ? fsrootedPrefix : "";

    if (filename.length() == 0) {

      return new File(prefix + dirpath).getParent();
      
    }

    return prefix + dirpath;
*/
  }

  // TODO make this work at the abstract path level instead of constructing
  // a pathname string and re-analysing it.
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
    writeCheck();
    return _createNew();
  }

  public native boolean _createNew() throws IOException;

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
