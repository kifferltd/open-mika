/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: ObjectInputStream.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.io;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Vector;

import wonka.decoders.UTF8Decoder;
import wonka.vm.ArrayUtil;

/**
** TODO:
**  - optimize, optimize and then more optimizations ... (defaultReadObject !!!).
**  - sort the ObjectStreamClass mess.
**  - handle Exceptions
**  - readFields
**  - close !!!
**  - prioryties in objectValidation.
**  - readClassDescripter (order of steps).
**  - readLine
*/
public class ObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants {

  private static final int BUFFERSIZE = 1024;

  //private int dbCount;

  /** internal buffer to hold blockData */
  private int dataPointer;
  private byte[] buffer;
  private int available;

  private InputStream in;
  private Vector objectCache;
  private Vector objectValidaters;

  private boolean enableReplaceObject;
  private boolean override;

  private final Object[] args; //used when invoking readObject.

  private Object current;
  private ObjectStreamClass currentOSC;

  private ClassLoader currentLoader; //if not null we are active !

  protected ObjectInputStream() throws IOException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
      }
    }

    args = new Object[1];
    args[0] = this;

    override = true;
  }

  public ObjectInputStream(InputStream in) throws IOException, StreamCorruptedException {
    this.in = in;/*
    this.in = new TeeInputStream(in);//*/

    buffer = new byte[BUFFERSIZE];
    objectCache = new Vector();

    args = new Object[1];
    args[0] = this;

    readStreamHeader();
  }


  public int available() throws IOException {
    if(dataPointer >= available){
      return 0;
    }
    return available - dataPointer;
  }

  public void close() throws IOException {
    //TODO ...
    in.close();
  }

  public void defaultReadObject() throws IOException, NotActiveException, ClassNotFoundException {
    ObjectStreamClass osc = currentOSC;
    Object obj = current;
    ObjectStreamField[] fields = osc.osFields; // these should be set when this osc got deserialized.
    int length = fields.length;

    byte[] buf = buffer;

    for(int i = 0 ; i < length ; i++){
      ObjectStreamField field = fields[i];

      Object value;
      switch(field.code){
        case 'L':
        case '[':
          value = readObject();
          break;
        case 'I':
          available = 0;
          getBytes(buf, 4);
          value = new Integer(ArrayUtil.bArrayToI(buf,0));
          break;
        case 'Z':
          available = 0;
          int iv = in.read();
          //dbCount++;
          if(iv == -1){
            throw new EOFException();
          }
          value = new Boolean(iv != 0);
          break;
        case 'F':
          available = 0;
          getBytes(buf, 4);
          value = new Float(ArrayUtil.bArrayToF(buf,0));
          break;
        case 'B':
          available = 0;
          iv = in.read();
          //dbCount++;
          if(iv == -1){
            throw new EOFException();
          }
          value = new Byte((byte)iv);
          break;
        case 'J':
          available = 0;
          getBytes(buf, 8);
          value = new Long(ArrayUtil.bArrayToL(buf,0));
          break;
        case 'C':
          available = 0;
          getBytes(buf,2);
          iv = buf[0]<<8 | (buf[1] & 0xff);
          value = new Character((char)iv);
          break;
        case 'S':
          available = 0;
          getBytes(buf,2);
          iv = buf[0]<<8 | (buf[1] & 0xff);
          value = new Short((short)iv);
          break;
        case 'D':
          available = 0;
          getBytes(buf,8);
          value = new Double(ArrayUtil.bArrayToD(buf,0));
          break;
        default:          
          throw new IOException("unexpected field code: '"+field.code+"'");
      }
      Field f = field.field;
      if(f != null){
        try {
          f.set(obj,value);
        }
        catch(Exception e){
          handleException(e);
        }
      }
      //else discard data ...
    }
  }

  public Object readUnshared() throws ClassNotFoundException, IOException {
    //TODO: to unshared thingy ...
    return this.readObject();
  }
  
  protected  boolean enableResolveObject(boolean enable) throws SecurityException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(SUBSTITUTION_PERMISSION);
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(SUBSTITUTION_PERMISSION);
      }
    }
    boolean b = enableReplaceObject;
    enableReplaceObject = enable;
    return b;
  }

  public int read() throws IOException {
    if(available <= dataPointer){
      try {
        fill();
      }
      catch(EOFException eof){
        return -1;
      }
    }
    int dp = dataPointer++;
    if(available <= dp){
      return -1;
    }
    return (buffer[dp] & 0xff);

  }

  public int read(byte[] b, int off, int size) throws IOException {
    if(available <= dataPointer){
      try {
        fill();
      }
      catch(EOFException eof){
        return -1;
      }
    }
    int dp = dataPointer;
    int total = available - dp;
    if(size > total){
      size = total;
    }
    System.arraycopy(buffer, dp, b, off, size);
    dataPointer += size;
    return size;
  }

  public boolean readBoolean() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer++;
    if(dp >= available){
      throw new EOFException();
    }
    return (buffer[dp] == 0 ? false : true);
  }

  public byte readByte() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer++;
    if(dp >= available){
      throw new EOFException();
    }

    return buffer[dp];
  }

  public char readChar() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    byte[] buf = buffer;
    int total = available - dp;
    dataPointer += 2;
    if(total < 2){
      throw new EOFException();
    }
    int ch = buf[dp++]<<8;
    return (char)(ch | (buf[dp] & 0xff));
  }

  protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
    //System.out.println("READING CLASSDESCRIPTOR");
    byte[] buf = buffer;
    getBytes(buf,2);
    int len = ((buf[0] & 0xff)<<8) | (buf[1] & 0xff);
    if(len + 11 > buf.length){
      buf = new byte[len+11];
      buffer = buf;
    }
    getBytes(buf, len + 11);
    String name = UTF8Decoder.bToString(buf,0,len);

    long suid = ArrayUtil.bArrayToL(buf,len);
    len+=8;


    //System.out.println("read SUID "+suid+" from Stream for "+name);

    ObjectStreamClass osc = new ObjectStreamClass(name,suid,buf[len++]);
    objectCache.addElement(osc);
    int mask = 0xff;
    int nrFields = (buf[len++] & mask)<<8;
    nrFields |= (buf[len] & mask);
    ObjectStreamField[] fields = new ObjectStreamField[nrFields];
    for (int i = 0 ; i < nrFields ; i++){
      getBytes(buf, 3);
      int b = buf[0];
      len = ((buf[1] & mask)<<8) |(buf[2] & mask);
      if(len > buf.length){
        buf = new byte[len];
        buffer = buf;
      }
      getBytes(buf, len);
      String fname = UTF8Decoder.bToString(buf, 0, len);
      //System.out.println("constructing field named "+fname);
      String desc;
      switch(b){
        case '[':
        case 'L':
          desc = desc2Name((String)readObject());
          break;
        case 'I':
          desc = "int";
          break;
        case 'J':
          desc = "long";
          break;
        case 'Z':
          desc = "boolean";
          break;
        case 'F':
          desc = "float";
          break;
        case 'B':
          desc = "byte";
          break;
        case 'C':
          desc = "char";
          break;
        case 'S':
          desc = "short";
          break;
        case 'D':
          desc = "double";
          break;
        default:
          throw new StreamCorruptedException("bad field type "+Integer.toHexString(b));

      }

      //System.out.println("FIELD ["+i+"] = "+fname+" of type "+desc);

      fields[i] = new ObjectStreamField(fname,desc,(char)b);
    }

    osc.osFields = fields;
    osc.clazz = resolveClass(osc);

    int rd = in.read();
    //dbCount++;
    if(rd != TC_ENDBLOCKDATA){
      throw new StreamCorruptedException("TC_ENDBLOCKDATA not found got "+Integer.toHexString(rd));
    }

    /** TODO determine the correct order of these steps ...*/
    osc.verifyInput();
    osc.parent = (ObjectStreamClass)readObject();
    /** end TODO */
    return osc;
  }

  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public GetField readFields() throws NotActiveException, IOException, ClassNotFoundException {
    if(this.currentLoader == null) {
      throw new NotActiveException();
    }
    // big TODO ...
    return null;
  }

  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public void readFully(byte[] data)  throws IOException {
    readFully(data, 0, data.length);
  }

  public void readFully(byte[] data, int offset, int size) throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    if(size > available - dp){
      throw new EOFException();
    }
    System.arraycopy(buffer, dp, data, offset, size);
    dataPointer += size;
  }

  public int readInt() throws IOException {
    //System.out.println("readInt start : "+dataPointer+" of "+available);
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    byte[] buf = buffer;
    int total = available - dp;
    dataPointer += 4;
    if(total < 4){
      throw new EOFException();
    }
    return ArrayUtil.bArrayToI(buf,dp);
  }

  /** @deprecated: don't use this method it's not implemented */
  public String readLine() throws IOException {
    //TODO ...
    return null;
  }

  public long readLong() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    byte[] buf = buffer;
    int total = available - dp;
    dataPointer += 8;
    if(total < 8){
      throw new EOFException();
    }
    return ArrayUtil.bArrayToL(buf,dp);
  }

  public final Object readObject() throws OptionalDataException, ClassNotFoundException, IOException {
    if(override){
      return readObjectOverride();
    }

    ClassLoader cl;

    int rd = in.read();
    //dbCount++;
    //discard data in buffer
    available = 0;

    ObjectStreamClass osc;
    Method readResolve;
    Vector cache;
    int place;
    Object o;

    switch(rd){
      case TC_NULL:
        //System.out.println("RETURNING NULL");
        return null;
      case TC_REFERENCE:
        byte[] buf = buffer;
        getBytes(buf, 4);
        int handle = ArrayUtil.bArrayToI(buf,0);
        return objectCache.get(handle - baseWireHandle);
      case TC_CLASSDESC:
        cl = currentLoader;
        if(cl == null){
          currentLoader = getCallingClassLoader();
        }
        osc = readClassDescriptor();
        currentLoader = cl;
        return osc;

      case TC_ARRAY:
        cl = currentLoader;
        if(cl == null){
          currentLoader = getCallingClassLoader();
        }
        osc = (ObjectStreamClass)readObject();
        available = 0;
        buf = buffer;
        getBytes(buf, 4);
        int length = ArrayUtil.bArrayToI(buf,0);

        Class clz = osc.clazz.getComponentType();

        if(clz == null){
          throw new StreamCorruptedException("TC_ARRAY encountered but no Array on the stream");
        }

        //System.out.println("Deserializing array from stream "+clz+" length = "+length+" clz isPrimitive ? "+clz.isPrimitive());

        cache = objectCache;
        place = cache.size();
        readResolve = null;

        if(clz.isPrimitive()){
          o = createPrimitiveArray(clz,length);
          cache.addElement(o);
        }
        else {
          o = Array.newInstance(clz,length);
          cache.addElement(o);
          Object[] oa = (Object[])o;
          for(int i = 0 ; i < length ; i++){
            oa[i] = readObject();
          }
        }
        currentLoader = cl;
        break;
      case TC_LONGSTRING:
        buf = buffer;
        cl = currentLoader;
        getBytes(buf, 8);
        int size = ArrayUtil.bArrayToI(buf,4);
        byte[] bytes = new byte[size];
        getBytes(bytes, size);
        o = UTF8Decoder.bToString(bytes, 0, size);
        cache = objectCache;
        place = cache.size();
        cache.addElement(o);
        readResolve = null;
        break;
      case TC_STRING:
        buf = buffer;
        cl = currentLoader;
        getBytes(buf, 2);
        size = ((buf[0] & 0xff)<<8) | (buf[1] & 0xff);
        if(size > buf.length){
          buf = new byte[size];
          buffer = buf;
        }
        getBytes(buf, size);
        o = UTF8Decoder.bToString(buf, 0, size);
        cache = objectCache;
        place = cache.size();
        cache.addElement(o);
        readResolve = null;
        break;
      case TC_OBJECT:
        cl = currentLoader;
        if(cl == null){
          currentLoader = getCallingClassLoader();
        }
        osc = (ObjectStreamClass)readObject();
        o = allocNewInstance(osc.clazz);

        //System.out.println("created object of type "+osc);

        readResolve = osc.readResolve;
        cache = objectCache;
        place = cache.size();
        cache.addElement(o);
        Object tmp = current;
        ObjectStreamClass tmpOSC = currentOSC;
        currentOSC = osc;
        current = o;
        if((osc.flags & SC_EXTERNALIZABLE) > 0){
          ((Externalizable)o).readExternal(this);
        }
        else {
          size = 128;
          ObjectStreamClass[] array = new ObjectStreamClass[size];
          int i=0;
          do {
            if(i == size){
              size *= 2;
              ObjectStreamClass[] newArray = new ObjectStreamClass[size];
              System.arraycopy(array,0,newArray,0,i);
              array = newArray;
            }
            array[i++] = osc;
            osc = osc.parent;
          } while(osc != null);

          do {
            osc = array[--i];
            currentOSC = osc;
            Method readObject = osc.readObject;
            currentOSC = osc;

            if(readObject == null){
              defaultReadObject();
            }
            else{
              try {
                readObject.invoke(o,args);

              }catch(InvocationTargetException ite){
                handleException(ite.getTargetException());
              }catch(IllegalAccessException iae){
                handleException(iae);
              }
            }
            if((osc.flags & SC_WRITE_METHOD) > 0){
              if(in.read() != TC_ENDBLOCKDATA){
                throw new StreamCorruptedException("TC_ENDBLOCKDATA not found");
              }
            }
          } while(i > 0);
        }
        currentLoader = cl;
        current = tmp;
        currentOSC = tmpOSC;
        break;
      case TC_CLASS:
        cl = currentLoader;
        if(cl == null){
          currentLoader = getCallingClassLoader();
        }
        osc = (ObjectStreamClass)readObject();
        Class clazz = osc.clazz;
        objectCache.addElement(clazz);
        currentLoader = cl;
        return clazz;
      case TC_BLOCKDATA:
        rd = in.read();
        //dbCount++;
        if(rd == -1){
          throw new EOFException();
        }
        throw new OptionalDataException(rd);
      case TC_BLOCKDATALONG:
        buf = buffer;
        getBytes(buf, 4);
        throw new OptionalDataException(ArrayUtil.bArrayToI(buf,0));
      case TC_RESET:
        cl = currentLoader;
        if(cl == null){
          currentLoader = getCallingClassLoader();
        }
        objectCache.clear();
        return readObject(); 
      case TC_EXCEPTION:
        objectCache.clear();
        Exception e = (Exception) readObject();
        throw new WriteAbortedException("Aborting serialization ",e);
      case TC_ENDBLOCKDATA:
        return readObject();
      case -1:
        throw new EOFException("no objects on stream");
      default:
        throw new StreamCorruptedException("unkown case in readObject: byte "+Integer.toHexString(rd));
    }

    Object rplc = o;
    if(readResolve != null){
      try {
        rplc = readResolve.invoke(o, ObjectOutputStream.NO_ARGS);
      }
      catch(Exception ite){
        //what TODO with it ...
      }
    }
    if(enableReplaceObject){
      rplc = resolveObject(rplc);
    }

    if(rplc != o){
      cache.setElementAt(rplc,place);
    }

    if(cl == null && objectValidaters != null){
      Vector list = objectValidaters;
      int l = list.size();
      ObjectInputValidation[] array = new ObjectInputValidation[l];
      list.toArray(array);
      for(int i = 0 ; i < l ; i++){
        array[i].validateObject();
      }
    }

    return rplc;
  }

  /** default implementation returns null */
  protected Object readObjectOverride() throws OptionalDataException, ClassNotFoundException, IOException {
    return null;
  }

  public short readShort() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    byte[] buf = buffer;
    int total = available - dp;
    dataPointer += 2;
    if(total < 2){
      throw new EOFException();
    }
    int sh = buf[dp++]<<8;
    return (short)(sh | (buf[dp] & 0xff));
  }

  protected  void readStreamHeader() throws IOException, StreamCorruptedException {
    byte[] buf = buffer;
    getBytes(buf,4);
    if((buf[0] != 0xffffffac) || (buf[1]!= 0xffffffed) || buf[2] != 0 || buf[3] != 5){
      throw new StreamCorruptedException("bad stream header encountered");
    }
  }

  public int readUnsignedByte() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer++;
    byte[] buf = buffer;
    if(available <= dp){
      throw new EOFException();
    }
    return (buf[dp] & 0xff);
  }

  public int readUnsignedShort() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    byte[] buf = buffer;
    int total = available - dp;
    dataPointer += 2;
    if(total < 2){
      throw new EOFException();
    }
    int sh = (buf[dp++] & 0xff)<<8;
    return sh | (buf[dp] & 0xff);
  }

  public String readUTF() throws IOException {
    if(available <= dataPointer){
      fill();
    }
    int dp = dataPointer;
    byte[] buf = buffer;
    int total = available - dp;
    if(total < 2){
      available = 0;
      throw new EOFException();
    }
    int length = ((buf[dp++] & 0xff)<<8);
    length |= (buf[dp++] & 0xff);
    if(total - 2 < length){
      available = 0;
      throw new EOFException();
    }
    dataPointer = dp + length;
    return UTF8Decoder.bToString(buf, dp, length);
  }

  public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
    if(currentLoader == null){
      throw new NotActiveException();
    }
    if(obj == null){
      throw new InvalidObjectException("null object is not allowed as ObjectInputValidation");
    }
    if(objectValidaters == null){
      objectValidaters = new Vector();
    }
    //TODO ... do something with priorities !!!
    objectValidaters.addElement(obj);
  }

  protected  Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
    //System.out.println("Default resolveClass: using "+currentLoader+" to load "+osc);
    return Class.forName(osc.name, false, currentLoader);
  }

  /** the default behaviour is to return the same object */
  protected  Object resolveObject(Object obj) throws IOException {
    return obj;
  }

  protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
    int length = interfaces.length;
    Class[] classes = new Class[length];
    ClassLoader cl = currentLoader;
    for (int i = 0 ; i < length ; i++){
      Class.forName(interfaces[i], false, cl);
    }
    return Proxy.getProxyClass(cl, classes);
  }

  public int skipBytes(int len) throws IOException {
    if(available <= dataPointer){
      try {
        fill();
      }
      catch(EOFException eof){
        return 0;
      }
    }
    int skip = available - dataPointer;
    if(len < skip){
      skip = len;
    }
    dataPointer += skip;
    return skip;
  }

  /** read and fill the dataBuffer */
  private void fill() throws IOException {
    dataPointer = 0;
    int rd = in.read();
    //dbCount++;
    switch(rd){
      case TC_BLOCKDATA:
        rd = in.read();
        //dbCount++;
        if(rd == -1){
          throw new EOFException();
        }
        //System.out.println("FILL: TC_BLOCKDATA size "+rd+" @"+dbCount);

        getBytes(buffer, rd);
        available = rd;
        break;
      case TC_BLOCKDATALONG:
        byte[] buf = buffer;
        getBytes(buf, 4);
        int size = ArrayUtil.bArrayToI(buf,0);
        if(size > buf.length){
          buf = new byte[size];
          buffer = buf;
        }
        getBytes(buf,size);
        available = size;
        break;
      default:
        throw new StreamCorruptedException("expected TC_BLOCKDATA or TC_BLOCKDATALONG but got "+rd);
    }
  }

  /** serves as an internal readFully , check if parameter offset can be discarded and replaced by 0 */
  private void getBytes(byte[] buf, int len) throws IOException {
    int off = 0;
    int rd = in.read(buf, off, len);

    //System.out.println("getBytes needs to load "+len+" bytes and got "+rd);

    while(rd < len){
      if(rd == -1){
        throw new EOFException();
      }
      off += rd;
      len -= rd;
      rd = in.read(buf, off, len);
    }
    //dbCount+=len;
  }

  /** creates and fill the primitive array and fills it with data from the byte array */
  private native Object createPrimitiveArray(Class clazz, int length);

  /** get the calling classloader called to resolve classes by the default resolveClass */
  private native ClassLoader getCallingClassLoader();

  /** creates an object of Class clazz and runs a no-arg constructor */
  private native Object allocNewInstance(Class clazz) throws IOException;

  private void handleException(Throwable e) throws IOException {
    //TODO ...
    throw new StreamCorruptedException("stream got corrupted by "+e);
  }


  private String desc2Name(String desc){
    if(desc.charAt(0) != '['){
      desc = desc.substring(1,desc.length()-1);
    }
    String name = desc.replace('/','.');

    //System.out.println("changed '"+desc+"' to '"+name+"'");

    return name;
  }

  public abstract static class GetField {
    public abstract boolean defaulted(String name) throws IOException, IllegalArgumentException;
    public abstract boolean get(String name, boolean defvalue) throws IOException, IllegalArgumentException;
    public abstract byte get(String name, byte defvalue) throws IOException, IllegalArgumentException;
    public abstract char get(String name, char defvalue) throws IOException, IllegalArgumentException;
    public abstract double get(String name, double defvalue) throws IOException, IllegalArgumentException;
    public abstract float get(String name, float defvalue) throws IOException, IllegalArgumentException;
    public abstract int get(String name, int defvalue) throws IOException, IllegalArgumentException;
    public abstract long get(String name, long defvalue) throws IOException, IllegalArgumentException;
    public abstract Object get(String name, Object defvalue) throws IOException, IllegalArgumentException;
    public abstract short get(String name, short defvalue) throws IOException, IllegalArgumentException;
    public abstract ObjectStreamClass getObjectStreamClass();
  }

/* simple class to test to check bytes read !!!
  private static class TeeInputStream extends InputStream {
    static int counter;

    private InputStream one;
    private OutputStream two;

    TeeInputStream(InputStream out) throws IOException{
      one = out;
      two = new FileOutputStream("INSER"+(counter++));
    }

    public int read() throws IOException {
      int rd = one.read();
      if(rd != -1){
        two.write(rd);
        two.flush();
      }
      else {
        two.close();
      }
      return rd;
    }

    public int read(byte[] b,int o, int l) throws IOException {
      int rd = one.read(b,o,l);
      if(rd != -1){
        two.write(b,o,rd);
        two.flush();
      }
      else {
        two.close();
      }
      return rd;
    }

    public int read(byte[] b) throws IOException{
      int rd = one.read(b,0,b.length);
      if(rd != -1){
        two.write(b,0,rd);
        two.flush();
      }
      else {
        two.close();
      }
      return rd;
    }

    public int available() throws IOException {
      two.flush();
      return one.available();
    }

    public void close() throws IOException {
      one.close();
      two.close();
    }

  }
//*/

}


