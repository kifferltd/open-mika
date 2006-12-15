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
 * $Id: PolicyReader.java,v 1.3 2006/04/18 11:35:27 cvs Exp $
 */

package com.acunia.wonka.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.URL;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.StringTokenizer;

final class PolicyReader implements PrivilegedAction {

  private static final String TRUE = "true";
  private static final String URLSTRING = "policy.url.";
  private static final String PK = ";";
  private static final String K = ",";
  private static final String DACO = "${";
  private static final String ACO = "{";
  private static final char ACC = '}';

  private HashMap collections;
  private KeyStore store;
  private StreamTokenizer st;
  private boolean expand;
  private boolean validEntry = true;
  private String fileSep;
  private ClassLoader cl;

  public Object run(){
    try {
      fileSep = System.getProperty("file.Separator","/");
      cl = ClassLoader.getSystemClassLoader();
      collections = new HashMap();
      expand = TRUE.equals(Security.getProperty("policy.expandProperties"));
      int nr = 1;
      String url = Security.getProperty(URLSTRING+nr);

      if(TRUE.equals(Security.getProperty("policy.allowSystemProperty"))){
        String userURL = System.getProperty("java.security.policy");
        if(userURL != null){
          if(userURL.startsWith("=")){
            url = null;
          }
          loadPolicyFile(userURL);
        }
      }

      while (url != null){
        if(loadPolicyFile(url)){
          break;
        }
        nr++;
        url = Security.getProperty(URLSTRING+nr);
      }
      //IF no default CodeSource is there we make one with no Permissions ...
      if(collections.get(DefaultPolicy.DEFAULT_CS) == null){
        collections.put(DefaultPolicy.DEFAULT_CS,new PolicyPermissionCollection());
      }
      return collections;
    }
    catch(Exception e){
      //System.out.println("PolicyReader detected '"+e+"' -- Default Policy will not be updated");
      e.printStackTrace();
      return null;
    }
  }

  private boolean hasMoreEntryTokens() throws Exception {
    int type = st.nextToken();
    if(type == ','){
      return true;
    }
    if(type == ';'){
      return false;
    }
    throw new GeneralSecurityException("bad entry syntax encountered "+st);
  }

  private String expandString(String name, boolean inCodeBase){
    //TODO ... replace '\' by '/' in certain occasions
    int index = name.indexOf(DACO);
    if(index == -1){
      return name;
    }
    StringBuffer buf = new StringBuffer();
    int end = 0;
    do {
      buf.append(name.substring(end,index));
      index += 2;
      end = name.indexOf(ACC, index);
      if(end == -1){
        buf.append(name.substring(index-2));
        return buf.toString();
      }
      String key = name.substring(index,end);
      if(key == "/"){
        buf.append(fileSep);
      }
      else {
        String prop = System.getProperty(key);
        if(prop == null){
          validEntry = false;
          return name;
        }
        if(inCodeBase){
          prop = prop.replace(fileSep.charAt(0),'/');
        }
        buf.append(prop);
      }
      end++;
      index = name.indexOf(DACO, end);
    } while (index != -1);
    buf.append(name.substring(end));
    return buf.toString();
  }

  private Certificate[] getCertificates(String aliases) throws GeneralSecurityException, KeyStoreException {
    if(expand){
      aliases = expandString(aliases, false);
    }
    StringTokenizer str = new StringTokenizer(aliases, K);
    int count = str.countTokens();
    Certificate[] certs = new Certificate[count];
    for (int i = 0; i < count ; i++){
      //TODO check behaviour... what if the store doesn't contain the alias. Do we put in a 'null' certificate ...
      // or should we throw a GeneralSecurityException ...
      certs[i] = store.getCertificate(str.nextToken());
    }
    return certs;
  }

  /**
  ** while using getToken we don't always check if we get a non null value back and
  ** by donig so trigger NullPointerExceptions.  This is done on purpose because we expect a valid
  ** token and if we don't find one the file is corrupt and not trustworthy.
  */
  private String getToken()throws IOException {
    int type = st.nextToken();
    if(type == StreamTokenizer.TT_WORD || type == '"'){
      return st.sval;
    }
    if(type == StreamTokenizer.TT_EOF){
      return null;
    }
    return String.valueOf((char)type);
  }

  /**
  ** returns true if the url cannot be connected ...
  */
  private boolean loadPolicyFile(String url) throws Exception {
    if(expand){
      url = expandString(url, true);
    }
    URL loc = new URL(url);
    InputStream in = null;
    try {
      in = loc.openStream();
    }
    catch(IOException ioe){
      return true;
    }
    st = new StreamTokenizer(new InputStreamReader(in));
    st.slashSlashComments(true);
    //WE ARE SETUP NOW ...
    String token = getToken();
    //At this point token indicates the start of an entry (or null) ...
    while(token != null){
      if(token.equalsIgnoreCase("grant")){
        //System.out.println("PARSING 'grant' BLOCK");
        parseGrantBlock(loc);
      }
      else if(token.equalsIgnoreCase("keystore")){
        parseKeyStoreBlock(loc);
      }
      else {
        throw new GeneralSecurityException("unknown start of policy file entry '"+token+"'");
      }
      token = getToken();
    }
    return false;
  }

  private void parseGrantBlock(URL baseURL)throws Exception {
    URL url = null;
    Certificate[] signers = null;
    do {
      String token = getToken();
      //System.out.println("checking token '"+token+"' in 'grant' BLOCK");
      if(token.equals(ACO)){
        break;
      }
      if(token.equalsIgnoreCase("signedBy")){
        if(signers != null){
          throw new GeneralSecurityException("INVALID GRANT ENTRY: more then one signedBy token found");
        }
        signers = getCertificates(getToken());
      }
      else if (token.equalsIgnoreCase("codeBase")){
        if(url != null){
          throw new GeneralSecurityException("INVALID GRANT ENTRY: more then one codeBase token found");
        }
        String location = getToken();
        if(expand){
          location = expandString(location, true);
        }
        url = new URL(baseURL, location);
      }
      else {
        throw new GeneralSecurityException("unknown token '"+token+"' in 'grant' entry header");
      }

    } while(true);
    //System.out.println("PARSING 'grant' BLOCK: looking for permission entries");

    //At this point we've parsed the opening line of the grant entry.  It contains the information
    //to construct a CodeSource. The CodeSource might be encountered in previous parsed grant entries ...
    PermissionCollection pc;
    if(validEntry){

      CodeSource cs = new CodeSource(url, signers);
      pc = (PermissionCollection)collections.get(cs);
      if(pc == null){
        pc = new PolicyPermissionCollection();
        if(url != null){
          //read permission for  this URL ...
          pc.add(url.openConnection().getPermission());
        }
        collections.put(cs, pc);
      }
    }
    else {
      pc = new PolicyPermissionCollection();
    }
    //now we start reading the permission entries until we get '};'
    do {
      String token = getToken();
      //System.out.println("checking token '"+token+"' in 'grant' BODY BLOCK");
      if(token.equalsIgnoreCase("permission")){
        parsePermissionBlock(pc);
        continue;
      }
      if(token.equals("}") && st.nextToken() == ';'){
        break;
      }
      throw new GeneralSecurityException("unknown token '"+token+"' in 'grant' entry body");
    } while(true);
  }


  private void parseKeyStoreBlock(URL baseURL) throws Exception {
    //a keystore entry exist of two parts: URL and type
    String url = getToken();
    if(st.nextToken() != ','){
      throw new GeneralSecurityException("unknown token '"+((char)st.ttype)+"' in 'keystore' entry (,)");
    }
    String type = getToken();
    if(st.nextToken() != ';'){
      throw new GeneralSecurityException("unknown token '"+((char)st.ttype)+"' in 'keystore' entry (;)");
    }
    //if we already have a keystore we ignore this entry ...
    if(store == null){
      if(expand){
        type = expandString(type, false);
        url  = expandString(url, true);
      }
      if(validEntry){
        store = KeyStore.getInstance(type);
        store.load(new URL(baseURL, url).openStream(), null);
      }
      else {
        validEntry = true;
      }
    }
  }

  private void parsePermissionBlock(PermissionCollection pc) throws Exception {
    //TODO ... make sure we use the correct ClassLoader !!!
    String classname = getToken();
    String target = getToken();
    Class permission = Class.forName(classname,true, cl);
    if(target.equals(PK)){
      Permission perm = (Permission) permission.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
      pc.add(perm);
      return;
    }
    String[] args;
    if(hasMoreEntryTokens()){
      String action = getToken();
      if(hasMoreEntryTokens()){
        verifySigners(permission);
      }
      if(expand){
        action = expandString(action, false);
      }
      args = new String[2];
      args[1] = action;
    }
    else {
      args = new String[1];
    }
    if(expand){
      target = expandString(target, false);
    }
    if(validEntry){
      args[0] = target;
      Class[] params = new Class[args.length];
      params[0] = target.getClass();
      if(params.length == 2){
        params[1] = params[0];
      }
      //System.out.println("CONSTRUCTING "+permission+" with target '"+args[0]+"' and action "+(args.length == 2 ? args[1]:"no action"));
      Permission perm = (Permission) permission.getDeclaredConstructor(params).newInstance(args);
      pc.add(perm);
    }
    else {
      validEntry = true;
    }
  }

  private void verifySigners(Class cl) throws Exception {
    String sign = getToken();
    String aliases = getToken();
    if(!sign.equalsIgnoreCase("signedBy") || st.nextToken() != ';'){
      throw new GeneralSecurityException("INVALID PERMISSION ENTRY: signedBy = '"+sign+"' and aliases = '"+aliases+"'");
    }
    //if this fails we don't trust the line ... (a property might be missing then it means we must disregard this line)
    Certificate[] certs = getCertificates(aliases);

    //this is tricky business we should ignore all signers if it system permission...
    ClassLoader loader = cl.getClassLoader();

    //TODO make sure this is true for all System classes.
    if(loader != null){// && loader != ClassLoader.getSystemClassLoader()){
      Object[] signers = cl.getSigners();
      if(signers != null){
        for (int i = 0 ; i < signers.length ; i++){
          for(int j = 0 ; j < certs.length ; j++){
            //TODO verify this is safe way to make sure the class file was signed by one aliases mentioned
            if(signers[i].equals(certs[j])){
              return;
            }
          }
        }
      }
      validEntry = false;
    }
  }
}

