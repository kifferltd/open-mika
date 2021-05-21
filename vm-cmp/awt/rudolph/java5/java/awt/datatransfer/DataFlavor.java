/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package java.awt.datatransfer;

import java.io.*;

public class DataFlavor implements Externalizable, Cloneable{

  public static final DataFlavor javaFileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List","application/x-java-file-list");
  public static final DataFlavor plainTextFlavor = new DataFlavor("text/plain; charset=unicode","Plain Text");
  public static final DataFlavor stringFlavor = new DataFlavor(java.lang.String.class, "Unicode String");
  public static final String javaJVMLocalObjectMimeType = "application/x-java-jvm-local-objectref";
  public static final String javaRemoteObjectMimeType = "application/x-java-remote-object";
  public static final String javaSerializedObjectMimeType = "application/x-java-serialized-object";
  private Class defaultRepresentationClass = java.io.InputStream.class;
  private Class representationClass; //the class in witch the data must be represented
  private String charset;
  private String mimeType; //MIME content type: in witch format (MIME type like JPEG, text) the data is saved
  private String humanPresentableName; //the name of this DataFlavor in humans words

  public DataFlavor(){
    ;
  }

  public DataFlavor(String mimeType, String humanPresentableName) {
    if(mimeType.indexOf(';')>0)
      this.mimeType = mimeType.substring(0,mimeType.indexOf(';')).trim();
    else this.mimeType = mimeType;
    this.humanPresentableName = humanPresentableName;
    String givenClass;
    if(mimeType.indexOf("class=")>=0){
        givenClass = mimeType.substring(mimeType.lastIndexOf("class=")+6).trim();
      if(givenClass.indexOf(';')>=0){
        givenClass = givenClass.substring(0,givenClass.indexOf(';')).trim();
      }
    }
    else{
      givenClass = mimeType.trim();
      if(mimeType.indexOf("application")>=0)
        throw new IllegalArgumentException("no representation class specified for:application/x-java-serialized-object");
    }
    if(mimeType.indexOf("text/plain")>=0){
      if(mimeType.indexOf("charset=")>=0){
        charset = mimeType.substring(mimeType.lastIndexOf("charset=")+8).trim();
      }
      try{representationClass = Class.forName("java.io.InputStream");}
      catch(ClassNotFoundException e){System.out.println(e.getMessage());}
    } else{
      try{representationClass = Class.forName(givenClass);}
      catch(ClassNotFoundException e){throw new IllegalArgumentException("failed to parse:"+givenClass);}
    }
  }

  public DataFlavor(Class representationClass, String humanPresentableName) {
    this.representationClass = representationClass;
//ONLY IN THE BOOK
//    if(humanPresentableName == null) this.humanPresentableName = (""+representationClass.getClass());
//    else
      this.humanPresentableName = humanPresentableName;
    mimeType = "application/x-java-serialized-object";
  }

  public DataFlavor(String mimeType, String humanPresentableName, ClassLoader classLoader)
           throws ClassNotFoundException{
    if(mimeType.indexOf(';')>0)
      this.mimeType = mimeType.substring(0,mimeType.indexOf(';')).trim();
    else this.mimeType = mimeType;
    this.humanPresentableName = humanPresentableName;
    String givenClass;
    if(mimeType.indexOf("class=")>=0){
        givenClass = mimeType.substring(mimeType.lastIndexOf("class=")+6).trim();
      if(givenClass.indexOf(';')>=0){
        givenClass = givenClass.substring(0,givenClass.indexOf(';')).trim();
      }
    }
    else{
      givenClass = mimeType.trim();
      if(mimeType.indexOf("application")>=0)
        throw new IllegalArgumentException("no representation class specified for:application/x-java-serialized-object");
    }
    if(mimeType.indexOf("text/plain")>=0){
      if(mimeType.indexOf("charset=")>=0){
        charset = mimeType.substring(mimeType.lastIndexOf("charset=")+8).trim();
      }
      representationClass = classLoader.loadClass("java.io.InputStream");
    } else{
      representationClass = classLoader.loadClass(givenClass);
    }
  }

  public String toString(){
    String data = DataFlavor.class.getName()+"[mimetype="+mimeType+";representationclass="+representationClass.getName();
    if(charset != null) data+=";charset="+charset;
    return data+"]";
  }

  public static final DataFlavor getTextPlainUnicodeFlavor(){
    return plainTextFlavor;
  }

  public static final DataFlavor selectBestTextFlavor(DataFlavor[] availableFlavors){
    for(int i = 0; i<availableFlavors.length;i++){
      if(availableFlavors[i]==DataFlavor.stringFlavor)return availableFlavors[i];
    }
    for(int i = 0; i<availableFlavors.length;i++){
      if(availableFlavors[i]==DataFlavor.plainTextFlavor)return availableFlavors[i];
    }
    return null;
  }

  public Reader getReaderForText(Transferable transferable)
                        throws UnsupportedFlavorException,
                               IOException{
    if(transferable==null) throw new NullPointerException();
    if(representationClass == java.io.Reader.class){
      try{return (Reader)representationClass.newInstance();}
      catch(InstantiationException ie){throw new UnsupportedFlavorException(this);}
      catch(IllegalAccessException iae){throw new UnsupportedFlavorException(this);}
    }
    if(!transferable.isDataFlavorSupported(this))throw new UnsupportedFlavorException(this);
    if(transferable.getTransferData(this) == null) throw new IllegalArgumentException("data of transferable is null");
    if(representationClass != java.lang.String.class && representationClass != java.io.InputStream.class) throw new IllegalArgumentException();
    else{
      try{
        if(representationClass == java.lang.String.class) return (new StringReader((String)transferable.getTransferData(DataFlavor.stringFlavor)));
        else return (new InputStreamReader((InputStream)transferable.getTransferData(this),this.getParameter("charset")));
//MAYBE A FAULT IN ELSE
      }catch(ClassCastException cce){throw new UnsupportedFlavorException(this);}
    }
  }

  public String getMimeType() {
    if(mimeType.indexOf("application")>=0)
      return mimeType+"; class="+representationClass.getName();
    else return mimeType+"; class="+representationClass.getName()+"; charset="+charset;
  }

  public Class getRepresentationClass() {
    return representationClass;
  }

  public String getHumanPresentableName() {
    return humanPresentableName;
  }

  public String getPrimaryType(){
    String primaryMimeType = mimeType;
    if(primaryMimeType.indexOf('/')>0) primaryMimeType=primaryMimeType.substring(0,primaryMimeType.indexOf('/'));
    return primaryMimeType;
  }

  public String getSubType(){
    String subMimeType = mimeType;
    if(subMimeType.indexOf('/')>0) subMimeType=subMimeType.substring(subMimeType.indexOf('/')+1);
    return subMimeType;
  }

  public String getParameter(String paramName){
    if(paramName.equalsIgnoreCase("class")) return representationClass.getName();
    if(paramName.equalsIgnoreCase("charset")) return charset;
    return null;
  }

  public void setHumanPresentableName(String humanPresentableName) {
    this.humanPresentableName = humanPresentableName;
  }

  public boolean equals(Object o) {
    DataFlavor object;
    try{ object = (DataFlavor) o;}
    catch(Exception e){return false;}
    return (this.mimeType.equalsIgnoreCase(object.mimeType) && this.representationClass==object.representationClass);
  }

  public boolean equals(DataFlavor dataFlavor) {
    return (this.mimeType.equalsIgnoreCase(dataFlavor.mimeType) && this.representationClass.equals(dataFlavor.representationClass));
  }

  public boolean equals(String s){
    return (this.isMimeTypeEqual(s));
  }

  public int hashCode(){
    return this.mimeType.hashCode()+this.representationClass.hashCode();
  }

  public boolean match(DataFlavor that){
    if(mimeType.toLowerCase().indexOf("text")>=0){
      return (this.mimeType.equalsIgnoreCase(that.mimeType)&&this.representationClass==that.representationClass&&this.charset.equalsIgnoreCase(that.charset));
    }
    else return (this.mimeType.equalsIgnoreCase(that.mimeType)&&this.representationClass==that.representationClass);
  }

  public boolean isMimeTypeEqual(String mimeType) {
    String thisMimeType = this.mimeType.toLowerCase().trim();
    String comparedMimeType = mimeType.toLowerCase().trim();
    if(thisMimeType.indexOf(';')>=0) thisMimeType = thisMimeType.substring(0,thisMimeType.indexOf(';')).trim();
    if(comparedMimeType.indexOf(';')>=0) comparedMimeType = comparedMimeType.substring(0,comparedMimeType.indexOf(';')).trim();
    System.out.println("thisMimeType = "+thisMimeType);
    System.out.println("comparedMimeType = "+comparedMimeType);
    return (thisMimeType.equals(comparedMimeType));
   }

  public final boolean isMimeTypeEqual(DataFlavor dataFlavor) {
    return isMimeTypeEqual(dataFlavor.mimeType);
  }

  public boolean isMimeTypeSerializedObject(){
    if(mimeType.indexOf("serialized")>=0) return true;
    return false;
  }

  public final Class getDefaultRepresentationClass(){
    return defaultRepresentationClass;
  }

  public final String getDefaultRepresentationClassAsString(){
    return defaultRepresentationClass.getName();
  }
  
  public boolean isRepresentationClassInputStream(){
    boolean isInputStream = false;
    for(Class superclass = this.representationClass; !superclass.equals(Object.class);superclass=superclass.getSuperclass()){
//      System.out.println("superclass = "+superclass.getName());
//      System.out.println("superclass == InputStream.class = "+(superclass == InputStream.class)+'\n');
      if(superclass == InputStream.class)isInputStream=true;
    }
    return isInputStream;
  }

  public boolean isRepresentationClassSerializable(){
    boolean isSerializable = false;
    Class[] classes = this.representationClass.getInterfaces();
    for(int i = 0; i < classes.length; i++){
//      System.out.println("classes["+i+"] = "+classes[i].getName());
//      System.out.println("classes["+i+"] == java.io.Serializable.class = "+(classes[i] == java.io.Serializable.class)+'\n');
      if(classes[i] == java.io.Serializable.class)isSerializable=true;
    }
    return isSerializable;
  }

  public boolean isRepresentationClassRemote(){
    boolean isRemote = false;
    Class[] classes = this.representationClass.getInterfaces();
    for(int i = 0; i < classes.length; i++){
//      System.out.println("classes["+i+"] = "+classes[i].getName());
//      System.out.println("classes["+i+"] == java.rmi.Remote.class = "+(classes[i] == java.rmi.Remote.class)+'\n');
      if(classes[i] == java.rmi.Remote.class)isRemote=true;
    }
    return isRemote;
  }

  public boolean isFlavorSerializedObjectType(){
    return isRepresentationClassSerializable();
  }

  public boolean isFlavorRemoteObjectType(){
    return isRepresentationClassRemote();
  }

//NOT SURE OF THIS FUNCTION
  public boolean isFlavorJavaFileListType(){  
    return javaFileListFlavor.getMimeType().equalsIgnoreCase(this.getMimeType());
  }

  public void readExternal(ObjectInput is)
                    throws IOException,
                         ClassNotFoundException{
    this.mimeType = (String)is.readObject();
    this.representationClass = (Class)is.readObject();
    this.humanPresentableName = (String)is.readObject();
      this.charset = (String)is.readObject();
    }

  public void writeExternal(ObjectOutput os)
                   throws IOException{
    os.writeObject(this.mimeType);
    os.writeObject(this.representationClass);
    os.writeObject(this.humanPresentableName);
    os.writeObject(this.charset);
  }

  public Object clone()
      throws CloneNotSupportedException{
    DataFlavor result = new DataFlavor();
    result.charset = this.charset;
    result.humanPresentableName = this.humanPresentableName;
    result.mimeType = this.mimeType;
    result.representationClass = this.representationClass;
    return result;
  }


  protected static final Class tryToLoadClass(String className, ClassLoader fallback)
      throws ClassNotFoundException{
//MUST BE IMPLEMENTED
    return null;
  }

  protected String normalizeMimeType(String parameterName, String parameterValue) {
    return (parameterName.toLowerCase().trim()+'='+parameterValue.toLowerCase().trim());
  }

  protected String normalizeMimeType(String mimeType) {
    return mimeType;
  }
}

