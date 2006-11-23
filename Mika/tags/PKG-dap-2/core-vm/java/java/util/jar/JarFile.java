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


/**
 * $Id: JarFile.java,v 1.5 2006/04/05 09:20:06 cvs Exp $
 */
package java.util.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Important notes on JarSigning: 
 * - If no Manifest is available no data verification can be done
 *   The jarfile should be considered to be untrusted
 * - How can you tell your data was signed and valiate well or unsigned
 *   especially important for classloaders !  
 * 
 * @author ruelens
 */
public class JarFile extends ZipFile {

  public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

  private static final Manifest theEmptyManifest = new Manifest();

  private Manifest manifest;

  private boolean verify;
  boolean verificationFailed;

  public JarFile(File file) throws IOException {
   	this(file,true, ZipFile.OPEN_READ);
  }
  public JarFile(String fname) throws IOException {
  	this(new File(fname),true, ZipFile.OPEN_READ);
  }
  public JarFile(String fname, boolean verify) throws IOException {
  	this(new File(fname),verify, ZipFile.OPEN_READ);
  }

  public JarFile(File file, boolean verify) throws IOException {
    this(file, verify, ZipFile.OPEN_READ);
  }

  public JarFile(File file, boolean verify, int mode) throws IOException {
    super(file, mode);
    manifest = theEmptyManifest;
    ZipEntry ze = super.getEntry(MANIFEST_NAME);
    if (ze != null){
      manifest = new Manifest(super.getInputStream(ze));
      if (verify) {
        /* We have a manifest ==> verification makes sense */
        //TODO ...
        this.verify = JarVerifier.verifyManifest(this);           
      }
    }
  }

// this method should be implemented by JarFile itself but are inherited now from ZipFile ...
	//  public Enumeration entries() { super.entries(); }

  public ZipEntry getEntry(String name) {
    ZipEntry ze =  super.getEntry(name);
    if(ze != null) {
      JarEntry je = new JarEntry(ze);
      je.setAttributes(manifest.getAttributes(je.getName()));
      return je;
    }
    return null;
  }

/**
** @remark should add security features ... !
*/
  public InputStream getInputStream (ZipEntry ze) throws IOException {
    InputStream in = super.getInputStream(ze);
    if (in != null && verify) {      
      if (verificationFailed) {
        throw new SecurityException("Verification of file "+this+" Failed !!!");
      }
      try {
        verifyEntryData(ze);
      } catch (NoSuchAlgorithmException e) {
        throw new SecurityException(e.getMessage());
      }
    }
    return in;
  }

  public JarEntry getJarEntry(String name) {
    ZipEntry ze =  super.getEntry(name);
    if(ze != null) {
      JarEntry je = new JarEntry(ze);
      je.setAttributes(manifest.getAttributes(je.getName()));
      return je;
    }
    return null;
  }

  public Manifest getManifest() throws IOException {
    return this.manifest;
  }

  /**
   * additions for verifying signed jarfiles ...
   */
  private void verifyEntryData(ZipEntry je) throws IOException,
      NoSuchAlgorithmException {
    Manifest manifest = getManifest();
    Attributes attributes = manifest.getAttributes(je.getName());
    if (attributes != null) {
      String[] algorithm = JarVerifier.getDigestKeyAlgortihm(attributes);
      if (algorithm != null) {
        JarVerifier.verifyBytes(getEntryBytes(je), algorithm[0], algorithm[1]);
      } else {
        // System.out.println("Verifyer.verifyEntryData() unsigned entry"
        // +je.getName());
      }
    }
  }
  
  private byte[] getEntryBytes(ZipEntry je) throws IOException {
    byte[] bytes = new byte[(int)je.getSize()];
    InputStream in = super.getInputStream(je);
    int off = 0;
    int len = bytes.length;
    do {
      int rd = in.read(bytes, off, len);
      if (rd == -1) {
        break;
      }
      off += rd;
    } while (off < len);
    return bytes;
  }  
  
  void verifyManifest(JarEntry je) throws IOException {
    Manifest mf = new Manifest(super.getInputStream(je));
    String[] result = JarVerifier.getDigestKeyAlgortihm(mf.getMainAttributes(),
        "-Digest-Manifest");
    byte[] manifestBytes = getEntryBytes(getJarEntry(JarFile.MANIFEST_NAME));
    if (result != null) {
      try {
        JarVerifier.verifyBytes(manifestBytes, result[0], result[1]);
        return;
      } catch (NoSuchAlgorithmException e) {
        throw new ZipException(e.getMessage());
      } catch (SecurityException se) {
        // continue parsing each individual header ...
      }
    }
    //System.out.println("Verifyer.verifyManifest() parsing each individual header ...");
    // parse each individual header ...
    int idx = 0;
    int len = manifestBytes.length;
    int prev = 0;
    int linelength = 2;
    while (idx < len) {
      int ch = manifestBytes[idx++];
      if (ch == '\n') {
        // linelength == 0 or linelength == 1 and prev == '\r'
        if (linelength == 0 || (linelength == 1 && prev == '\r')) {
          //System.out.println("Verifyer.verifyManifest() Empty Line !");
          break;
        }
      } else if (prev == '\r') {
        if (linelength == 1) {
          idx--;
          break;
        }
      } else {
        linelength++;
        prev = ch;
        continue;
      }
      prev = 0;
      linelength = 0;
    }

    //System.out.println("Verifyer.verifyManifest() looking for entry data "+idx+", len "+len);
    int start = idx;
    while (idx < (len - 5)) {
      if ("Name:".equals(new String(manifestBytes, idx, 5))) {
        idx += 5;
        int startOfName = idx;
        while(idx < len) {
          int ch = manifestBytes[idx++];
          if (ch == '\n' || ch == '\r') {
            idx--;
            break;
          }
        }
        String name = new String(manifestBytes, startOfName, idx - startOfName).trim();
        // each entry found here, should have an entry in the manifest.
        //System.out.println("Verifyer.verifyManifest()found name '"+name+"'");
        Attributes attr = mf.getAttributes(name);
        if (attr != null) {
          result = JarVerifier.getDigestKeyAlgortihm(attr);
          if (result == null) {
            throw new SecurityException("invalid Signature File " + name);
         }
        }
        //System.out.println("Verifyer.verifyManifest()key = " + name);
        linelength = 5;
        prev = 0;
        while (idx < len) {
          int ch = manifestBytes[idx++];
          if (ch == '\n') {
            // linelength == 0 or linelength == 1 and prev == '\r'
            if (linelength == 0 || (linelength == 1 && prev == '\r')) {              
              break;
            }
          } else if (prev == '\r') {
            if (linelength == 1) {
              idx--;
              break;
            }
          } else {
            linelength++;
            prev = ch;
            continue;
          }
          prev = 0;
          linelength = 0;
        }        
        if (result != null) {
          //TODO check where we need to stop collecting bytes.
          int l = idx - start;
          byte[] entryBytes = new byte[l];
          //System.out.println("Verifyer.verifyManifest()"+l+", "+start+", "+manifestBytes.length);
          System.arraycopy(manifestBytes, start, entryBytes, 0, l);
          //System.out.println("Verifyer.verifyManifest() entra data '"+new String(entryBytes)+"'");
          try {
            JarVerifier.verifyBytes(entryBytes, result[0], result[1]);
          } catch (NoSuchAlgorithmException e) {
            throw new ZipException(e.getMessage());
          }
        }
        start = idx;
      } else {
        //System.out.println("Verifyer.verifyManifest() 'Name:' != '"+new String(manifestBytes, idx, 5)+"'");
        new ZipException("failed to reparse manifest 'Name:' != '"+new String(manifestBytes, idx, 5)+"'");
      }
    } 
  }
}
