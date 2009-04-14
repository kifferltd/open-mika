/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import wonka.vm.ArrayUtil;
import wonka.decoders.UTF8Decoder;

/**
** TODO
**  - check out the story of serialPersitentFields !!! --> work to be done in ObjectStreamClass.c
**  - replacing objects: get rid of getDeclaredMethod !
**  - handle Exceptions. test handling.
**  - putFields & writeFields.
**  - useProtocolVersion.
**  - remove unneeded drain() or inline them
**  - optimize like hell
**  - test native array code and optimize were needed !
**  - handle proxy classes when available !
**  - optimize defaultWriteObject --> 1 native method ?
*/

public class ObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {

  private static final int BUFFERSIZE = 1024;

  private static final Class clazzObjectStreamClass = new ObjectStreamClass("").getClass();
  private static final Class clazzClass = clazzObjectStreamClass.getClass();
  private static final Class clazzString = "".getClass();
  private static final Class[] EMPTY = new Class[0];
  static final Object[] NO_ARGS = new Object[0];

  private Object[] args; //used when invoking writeObject.

  private OutputStream out;

  private final byte[] buffer;
  private int pointer;

  private byte[] databuffer;
  private int datapointer;

  private int handleCounter;

  /** internal hashtable for handle cache */
  private Object[] objectCache;
  private int [] handles;
  private int hthreshold;
  private int hcapacity;
  private int hoccupancy;
  /** end of internal hashtable */

  /** internal hashtable for replacement cache */
  private Object[] replacementCache;
  private Object[] replacements;
  private int rthreshold;
  private int rcapacity;
  private int roccupancy;
  /** end of internal hashtable */


  //internal state of the ObjectOutputStream
  private Object active; /** object currently being serialized */
  private ObjectStreamClass currentObjectStreamClass; /** ObjectStreamClass of the object currently being serialized */
  private boolean enableReplaceObject;
  private boolean override;

  protected ObjectOutputStream() throws IOException, SecurityException {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
      }
    }

    args = new Object[1];
    args[0] = this;

    override = true;
    buffer = null;
  }

  public ObjectOutputStream(OutputStream out) throws IOException {
    this.out = out; /*
    this.out = new TeeOutputStream(out);
    //*/
    buffer = new byte[BUFFERSIZE];
    databuffer = new byte[BUFFERSIZE];

    args = new Object[1];
    args[0] = this;

    handleCounter = baseWireHandle;

    objectCache = new Object[101];
    handles = new int[101];
    hthreshold = 75;;
    hcapacity = 101;

    replacementCache = new Object[101];
    replacements = new Object[101];
    rthreshold = 75;;
    rcapacity = 101;

    writeStreamHeader();
  }

  protected void annotateClass(Class clazz) throws IOException {
    //default implementation does nothing
  }

  protected  void annotateProxyClass(Class clazz) throws IOException {
    //default implementation does nothing
  }

  public void close() throws IOException {
    //TODO try to find out if flush() or drain is needed. for now drain which is faster.
    drain();
    out.close();
  }

  public void defaultWriteObject() throws IOException {
    Object current = active;
    ObjectStreamClass osc = currentObjectStreamClass;
    if(current == null){
      throw new NotActiveException("Not serializing an object");
    }

    ObjectStreamField[] osfields = osc.getFields();
    int len = osfields.length;
    byte[] buf = buffer;
    try {
      for(int i = 0 ; i < len ; i++){
        ObjectStreamField osfield = osfields[i];
        Field f = osfield.field;

        //System.out.println("Writing field "+f+" ("+osfield+") to stream");

        if(f == null){
          InvalidClassException ice = new InvalidClassException("no field "+osfield.name+" of "+osfield.type+" in "+osc.name);
          ice.classname = osc.name;
          handleException(ice);
        }

        if(osfield.isPrimitive){
          if(datapointer > 0 || pointer + 8 > BUFFERSIZE){
            drain();
          }
          int p = pointer;
          switch(osfield.code){
            case 'I':
              ArrayUtil.iInBArray(f.getInt(current), buf, p);
              p += 4;
              break;
            case 'J':
              ArrayUtil.lInBArray(f.getLong(current), buf, p);
              p += 8;
              break;
            case 'F':
              ArrayUtil.fInBArray(f.getFloat(current), buf, p);
              p += 4;
              break;
            case 'D':
              ArrayUtil.dInBArray(f.getDouble(current), buf , p);
              p += 8;
              break;
            case 'B':
              buf[p++] = f.getByte(current);
              break;
            case 'Z':
              buf[p++] = (byte)(f.getBoolean(current) ? 1 : 0);
              break;
            case 'C':
              int ch = f.getChar(current);
              buf[p++] = (byte)(ch >> 8);
              buf[p++] = (byte) ch;
              break;
            case 'S':
              int s = f.getShort(current);
              buf[p++] = (byte)(s >> 8);
              buf[p++] = (byte) s;
              break;
            default:
              throw new StreamCorruptedException("trying to write primitive field with code '"+(osfield.code)+"'");
          }
          pointer = p;
        }
        else {
          writeObject(f.get(current));
        }
      }
    }
    catch(IllegalAccessException iae){
      handleException(iae);
    }
  }
  
  public void writeUnshared(Object obj) throws IOException {
    //TODO: do unshared thingy.
    writeObject(obj);
  }

  protected void drain() throws IOException {
    int p = pointer;
    byte[] buf = buffer;
    if(datapointer > 0){
      int dp = datapointer;
      if(p > BUFFERSIZE - 5){
        //lets hope don't get into this branch to much
        out.write(buf,0,p);
        p = 0;
      }

      if(dp < 256){
        buf[p++] = TC_BLOCKDATA;
        buf[p++] = (byte)dp;
      }
      else{
        buf[p++] = TC_BLOCKDATALONG;
        ArrayUtil.iInBArray(dp, buf, p);
        p += 4;
      }
      out.write(buf,0,p);
      pointer = 0;
      out.write(databuffer,0,dp);
      datapointer = 0;
    }
    else {
      out.write(buf,0,p);
      pointer = 0;
    }
  }

  protected  boolean enableReplaceObject(boolean enable) throws SecurityException {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(SUBSTITUTION_PERMISSION);
      }
    }
    boolean b = enableReplaceObject;
    enableReplaceObject = enable;
    return b;
  }

  public void flush() throws IOException {
    drain();
    out.flush();
  }

  public PutField putFields() throws IOException {
    //Big TODO ...
    return null;
  }

  protected Object replaceObject(Object obj) throws IOException {
    //the default behaviour is not to replace object hence we return obj.
    return obj;
  }

  public void reset() throws IOException {
    //check if we are currently serializing an object --> throw IOException
    if(active != null){
      throw new IOException("cannot reset while serializing an object");
    }
    if(datapointer > 0 || pointer >= BUFFERSIZE){
      drain();
    }
    buffer[pointer++] = TC_RESET;

    //clean object cache!!!
    objectCache = new Object[101];
    handles = new int[101];
    hthreshold = 75;;
    hcapacity = 101;
    hoccupancy = 0;
    replacementCache = new Object[101];
    replacements = new Object[101];
    rthreshold = 75;;
    rcapacity = 101;
    roccupancy = 0;
    handleCounter = baseWireHandle;
  }

  public void useProtocolVersion(int version) throws IOException {
    if(version == PROTOCOL_VERSION_1){
      //System.out.println("\n\nPROTOCOL_VERSION_1 is not supported using PROTOCOL_VERSION_2\n\n");
    }
    else if(version != PROTOCOL_VERSION_1){
      throw new IllegalArgumentException("protocol version "+version+" not supported");
    }
    //TODO did we start already ??? --> throw IllegalStateException
    //TODO ???
  }

  public void write(byte[] buf) throws IOException {
    write(buf, 0, buf.length);
  }

  public void write(byte[] buf, int off, int len) throws IOException {
    if(datapointer + len > databuffer.length){
      grow(len);
    }
    System.arraycopy(buf,off,databuffer,datapointer,len);
    datapointer += len;
  }

  public void write(int data) throws IOException {
    if(datapointer >= databuffer.length){
      grow(BUFFERSIZE);
    }
    databuffer[datapointer++] = (byte)data;
  }

  public void writeBoolean(boolean data) throws IOException {
    if(datapointer >= databuffer.length){
      grow(BUFFERSIZE);
    }
    databuffer[datapointer++] = (byte)(data ? 1 : 0);
  }

  public void writeByte(int data) throws IOException {
    if(datapointer >= databuffer.length){
      grow(BUFFERSIZE);
    }
    databuffer[datapointer++] = (byte)data;
  }

  public void writeBytes(String string) throws IOException {
    byte[] buf = string.getBytes("latin1");
    int len = buf.length;
    if(datapointer > databuffer.length - len){
      grow(len);
    }
    System.arraycopy(buf,0,databuffer,datapointer,len);
    datapointer += len;
  }

  public void writeChar(int data) throws IOException {
    if(datapointer + 2 > databuffer.length){
      grow(BUFFERSIZE);
    }
    databuffer[datapointer++] = (byte)(data>>8);
    databuffer[datapointer++] = (byte)data;
  }

  public void writeChars(String string) throws IOException {
    byte[] buf = string.getBytes("UTF-16BE");
    int len = buf.length;
    if(datapointer > databuffer.length - len){
      grow(len);
    }
    System.arraycopy(buf,0,databuffer,datapointer,len);
    datapointer += len;
  }

  protected void writeClassDescriptor(ObjectStreamClass osc) throws IOException {
    // we need space for  2 + len + 8 + 1 + 2 +??? + 1
    byte[] bytes = UTF8Decoder.stringToB(osc.name);
    int len = bytes.length;
    byte[] buf = buffer;

    int p;
    //lets hope we have enough space in our buffer.
    if(pointer + len >= BUFFERSIZE - 13){
      drain();  //TODO remove or replace ???
      if(len > BUFFERSIZE - 13){
        buf[0] = (byte)(len>>8);
        buf[1] = (byte) len;
        out.write(buf,0,2);
        out.write(bytes,0,len);
        p = 0;
      }
      else {
        p = pointer;
        buf[p++] = (byte)(len>>8);
        buf[p++] = (byte) len;
        System.arraycopy(bytes, 0, buf, p, len);
        p += len;
      }
    }
    else {
      p = pointer;
      buf[p++] = (byte)(len>>8);
      buf[p++] = (byte) len;
      System.arraycopy(bytes, 0, buf, p, len);
      p += len;
    }

    ArrayUtil.lInBArray(osc.suid, buf , p);
    p += 8;

    buf[p++] = (byte) osc.flags;

    ObjectStreamField[] fields = osc.getFields();
    len = fields.length;
    buf[p++] = (byte)(len>>8);
    buf[p++] = (byte) len;

    for(int i = 0 ; i < len ; i++){
      ObjectStreamField osf = fields[i];
      bytes = UTF8Decoder.stringToB(osf.name);
      int l = bytes.length;
      if(p + l > BUFFERSIZE - 3){
        out.write(buf,0,p);
        p = 0;
        if(l > BUFFERSIZE - 3){
          buf[0] = (byte) osf.code;
          buf[1] = (byte)(l>>8);
          buf[2] = (byte) l;
          out.write(buf,0,3);
          out.write(bytes,0,l);
        }
        else {
          buf[p++] = (byte) osf.code;
          buf[p++] = (byte)(l>>8);
          buf[p++] = (byte) l;
          System.arraycopy(bytes, 0, buf, p, l);
          p += l;
        }
      }
      else {
        buf[p++] = (byte) osf.code;
        buf[p++] = (byte)(l>>8);
        buf[p++] = (byte) l;
        System.arraycopy(bytes, 0, buf, p, l);
        p += l;

      }
      if(!osf.isPrimitive){
        pointer = p;
        writeObject(osf.typeString);
        p = pointer;
      }
    }

    pointer = p;
    annotateClass(osc.clazz);
    p = pointer;

    if(p >= BUFFERSIZE){
      out.write(buf,0,p);
      p = 0;
    }
    buf[p++] = TC_ENDBLOCKDATA;


    ObjectStreamClass tmp = osc.parent;
    if(tmp == null){
      if(p >= BUFFERSIZE){
        out.write(buf,0,p);
        p = 0;
      }
      buf[p++] = TC_NULL;
      pointer = p;
    }
    else {
      pointer = p;
      writeObject(tmp);
    }
  }

  public void writeDouble(double d)  throws IOException {
    int dp = datapointer;
    if(dp + 8 > databuffer.length){
      grow(BUFFERSIZE);
    }
    ArrayUtil.dInBArray(d, databuffer, dp);
    datapointer =  dp + 8;
  }

  public void writeFields() throws IOException {
    //big TODO ...
  }

  public void writeFloat(float f) throws IOException {
    int dp = datapointer;
    if(dp + 4 > databuffer.length){
      grow(BUFFERSIZE);
    }
    ArrayUtil.fInBArray(f, databuffer, dp);
    datapointer =  dp + 4;
  }

  public void writeInt(int data) throws IOException {
    int dp = datapointer;
    if(dp + 4 > databuffer.length){
      grow(BUFFERSIZE);
    }
    ArrayUtil.iInBArray(data, databuffer, dp);
    datapointer =  dp + 4;
  }

  public void writeLong(long data) throws IOException {
    int dp = datapointer;
    if(dp + 8 > databuffer.length){
      grow(BUFFERSIZE);
    }
    ArrayUtil.lInBArray(data, databuffer, dp);
    datapointer =  dp + 8;
  }

  public final void writeObject(Object obj) throws IOException {
    //step 1.
    if(override){
      writeObjectOverride(obj);
      return;
    }
    //step 2. clear data buffer.

    if(datapointer > 0 || pointer > BUFFERSIZE -9){
      drain();
    }

    ObjectStreamClass osc;
    Class objClass;
    byte[] buf = this.buffer;

    do {
      //step 3. if obj == null; write null reference.
      if(obj == null){
        buf[pointer++] = TC_NULL;
        if(active == null){
          drain();
        }
        return;
      }

      //step 4 If the object has been previously replaced, as described in Step 8,
      //write the handle of the replacement to the stream and writeObject returns.

      int cap = rcapacity;
      int ihash = System.identityHashCode(obj);
      int hash = ihash % cap;
      Object k[] = replacementCache;
      do {
        if(hash < 0) hash += cap;
        Object k2 = k[hash];
        if(k2 == null) {
          break;
        } else if(obj == k2) {
          obj = replacements[hash];
          if(obj == null){
            buf[pointer++] = TC_NULL;
            if(active == null){
              drain();
            }
            return;
          }
          ihash = System.identityHashCode(obj);
          break;
        }
        hash--;
      } while(true);


      //5.If the object has already been written to the stream, its handle is written to the stream and writeObject returns.

      int handle = getHandle(obj,ihash);
      if(handle != -1){
        int p = pointer;
        buf[p++] = TC_REFERENCE;
        ArrayUtil.iInBArray(handle, buf, p);
        pointer = p + 4;
        if(active == null){
          drain();
        }
        return;
      }


      //6.If the object is a Class, the corresponding ObjectStreamClass is written to the stream, a handle is assigned for the class,
      //and writeObject returns.

      objClass = obj.getClass();

      if(objClass == clazzClass){
        buf[pointer++] = TC_CLASS;

        Class clazz = (Class)obj;
        osc = ObjectStreamClass.lookup(clazz);
        if(osc == null){
          put(obj,handleCounter++);
          byte[] bytes = UTF8Decoder.stringToB(clazz.getName());
          int l = bytes.length;
          int p = pointer;
          buf[p++] = TC_CLASSDESC;
          buf[p++] = (byte)(l>>8);
          buf[p++] = (byte) l;
          if(l + p > BUFFERSIZE - 13){
            out.write(buf,0,p);
            p = 0;
            if(l > BUFFERSIZE - 13){
              out.write(bytes,0,l);
            }
            else {
              System.arraycopy(bytes,0,buf,p,l);
              p += l;
            }
          }
          else {
            System.arraycopy(bytes,0,buf,p,l);
            p += l;

          }
          //suid is 0 (8)and flag are 0 (1) and no fields (2)
          ArrayUtil.lInBArray(0L, buf, p);
          p += 8;
          buf[p++] = TC_ENDBLOCKDATA;
          buf[p++] = TC_NULL;
          pointer = p;
        }
        else {
          writeObject(osc);
        }
        put(obj, handleCounter++);

        if(active == null){
          drain();
        }
        return;
      }

      //7.If the object is an ObjectStreamClass, a descriptor for the class is written to the stream including its name,
      //serialVersionUID, and the list of fields by name and type. A handle is assigned for the descriptor.
      //The annotateClass subclass method is called before writeObject returns.

      if(clazzObjectStreamClass.isInstance(obj)){
        put(obj, handleCounter++);
        osc = (ObjectStreamClass)obj;
        buf[pointer++] = TC_CLASSDESC;
        writeClassDescriptor(osc);
        if(active == null){
          drain();
        }
        return;
      }

      //8.Process potential substitutions by the class of the object and/or by a subclass of ObjectInputStream.
      /* a.If the class of an object defines the appropriate writeReplace method, the method is called.
           Optionally, it can return a substitute object to be serialized.
      */

      boolean same = true;
      Object replace = obj;
      try {
        //TODO do we really need to do this by calling getDeclaredMethod ???
        Method writeReplace = objClass.getDeclaredMethod("writeReplace",EMPTY);
        replace = writeReplace.invoke(obj,NO_ARGS);
        same = (replace == obj);
      }
      catch(NoSuchMethodException nsme){/** ignore */}
      catch(InvocationTargetException ite){
        handleException(ite.getTargetException());
      }
      catch(IllegalAccessException iae){
        /** should never occur*/
        handleException(iae);
      }

      if(enableReplaceObject){
        replace = replaceObject(replace);
        same = (replace == obj);
      }

      if(same){
        break;
      }

      putReplacement(obj,replace);
      obj = replace;

    } while(true);

    if(objClass == clazzString){
      //we have at least 9 bytes available.
      String s = (String)obj;
      byte[] bytes = UTF8Decoder.stringToB(s);
      int len = bytes.length;
      int p = pointer;
      if(len < 65536) {
        buf[p++] = TC_STRING;
        buf[p++] = (byte)(len>>8);
        buf[p++] = (byte)len;
      }
      else {
        buf[p++] = TC_LONGSTRING;
        ArrayUtil.lInBArray(len & 0x0ffffffffL, buf, p);
        p += 8;
      }

      if(len > BUFFERSIZE - p){
        out.write(buf,0,p);
        out.write(bytes,0,len);
        pointer = 0;
      }
      else {
        System.arraycopy(bytes,0,buf,p,len);
        pointer = p + len;
      }
      put(obj, handleCounter++);
      if(active == null){
        drain();
      }
      return;
    }

    osc = ObjectStreamClass.lookup(objClass);
    if(osc == null){
      throw new NotSerializableException(obj+" of "+objClass+" is not Serializable");
    }

    Object current = active;
    active = obj;

    if((osc.flags & ObjectStreamClass.IS_ARRAY) > 0){
      buf[pointer++] = TC_ARRAY;
      writeObject(osc);

      put(obj, handleCounter++);

      Class ctype = objClass.getComponentType();
      if(ctype.isPrimitive()){
        int p = pointer;
        byte[] bytes = primitiveArrayToBytes(obj);
        ArrayUtil.iInBArray(pointer, buf, p);
        p += 4;
        int l = bytes.length;
        if(p + l > BUFFERSIZE){
          out.write(buf,0,p);
          out.write(bytes,0,l);
          pointer = 0;
        }
        else {
          //System.out.println("copying array "+bytes.length+", 0, "+buf.length+", "+p+", "+l);
          System.arraycopy(bytes, 0, buf, p, l);
          pointer = p + l;
        }
      }
      else {
        Object[] objectArray = (Object[])obj;
        int p = pointer;
        int arrayLength = objectArray.length;
        ArrayUtil.iInBArray(arrayLength, buf, p);
        pointer = p + 4;

        for (int i = 0; i < arrayLength; i++) {
          writeObject(objectArray[i]);
        }
      }
      if(current == null){
        drain();
      }
      active = current;
      return;
    }

    buf[pointer++] = TC_OBJECT;

    writeObject(osc);

    put(obj, handleCounter++);

    ObjectStreamClass curOsc = currentObjectStreamClass;
    if((osc.flags & SC_EXTERNALIZABLE) > 0){
      currentObjectStreamClass = osc;
      ((Externalizable)obj).writeExternal(this);
      //TODO ... verify if this check makes sense, it might be better to drain immediatly
      if(datapointer > 0 || pointer >= BUFFERSIZE){
        drain();
      }
      buf[pointer++] = TC_ENDBLOCKDATA;
    }
    else {
      //object is only serializable
      int size = 128;
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
        currentObjectStreamClass = osc;
        Method writeObject = osc.writeObject;
        if(writeObject == null){
          defaultWriteObject();
        }
        else{
          try {
           writeObject.invoke(obj,args);
           //does this check makes sense or do we drain right away ...
           if(datapointer > 0 || pointer >= BUFFERSIZE){
             drain();
           }
           buf[pointer++] = TC_ENDBLOCKDATA;
          }
          catch(InvocationTargetException ite){
            handleException(ite.getTargetException());
          }
          catch(IllegalAccessException iae){
            handleException(iae);
          }
        }
      } while(i > 0);
    }
    active = current;

    if(current == null){
      drain();
    }

    currentObjectStreamClass = curOsc;
  }

  protected void writeObjectOverride(Object obj) throws IOException {
    /** this one is easy.  The default implementation does nothing. */
  }

  public void writeShort(int data) throws IOException {
    if(datapointer + 2 > databuffer.length){
      grow(BUFFERSIZE);
    }
    databuffer[datapointer++] = (byte)(data>>8);
    databuffer[datapointer++] = (byte)data;
  }

  protected void writeStreamHeader() throws IOException {
    byte[] buf = buffer;
    buf[0] = (byte)(STREAM_MAGIC>>8);
    buf[1] = (byte)STREAM_MAGIC;
    buf[2] = (byte)(STREAM_VERSION>>8);
    buf[3] = (byte)STREAM_VERSION;
    out.write(buf, 0, 4);
  }

  public void writeUTF(String s) throws IOException {
    byte[] bytes = UTF8Decoder.stringToB(s);
    int len = bytes.length;
    if(len > 65535){
      throw new UTFDataFormatException("to much bytes in string");
    }

    if(datapointer + 2 +len > databuffer.length){
      grow(len+2);
    }
    databuffer[datapointer++] = (byte)(len>>8);
    databuffer[datapointer++] = (byte)len;
    System.arraycopy(bytes,0,databuffer,datapointer,len);
    datapointer += len;
  }

  /**
  ** this method make the internal dataBuffer larger. Should not be called very often though,
  ** so no need to inline it.
  */
  private void grow(int size) {
    byte[] buf = databuffer;
    if(size < BUFFERSIZE){
      size = BUFFERSIZE;
    }
    databuffer = new byte[size+buf.length];
    System.arraycopy(buf,0,databuffer,0,datapointer);
  }

  /**
  ** when this method is called something is going wrong. Again we don't need to inline
  ** performance in not an issue here.
  */
  private void handleException(Throwable e) throws IOException {
    //TODO notify peer ... see docs
    if(e instanceof IOException){
      if(datapointer > 0 || pointer >= BUFFERSIZE){
        drain();
      }
      buffer[pointer++] = TC_EXCEPTION;
      objectCache = new Object[101];
      handles = new int[101];
      hthreshold = 75;;
      hcapacity = 101;
      hoccupancy = 0;
      replacementCache = new Object[101];
      replacements = new Object[101];
      rthreshold = 75;;
      rcapacity = 101;
      roccupancy = 0;
      handleCounter = baseWireHandle;
      writeObject(e);
      drain();

      throw((IOException)e);
    }
    throw new StreamCorruptedException("stream got corrupted by "+e);
  }

  /**
  ** Take care: This method will change pointer. (pointer will hold the length of the array).
  **
  */
  private native byte[] primitiveArrayToBytes(Object pArray);


/**
** internal hashtable methods
*/
  private int getHandle(Object key, int hash) {
    int cap = hcapacity;
    Object k[] = objectCache;

    hash = hash % cap;

    do {
      if(hash < 0) hash += cap;

      Object k2 = k[hash];

      if(k2 == null) {
        return -1;
      } else if(key == k2) {
        return this.handles[hash];
      }

      hash--;
    } while(true);
  }

  private void put(Object key, int handle){
    int cap = hcapacity;
    int hash = System.identityHashCode(key) % cap;
    Object k[] = objectCache;
    do {
      if(hash < 0) hash += cap;

      Object k2 = k[hash];

      if(k2 == null) {
        k[hash] = key;
        this.handles[hash] = handle;

        if (++hoccupancy >= hthreshold) {
          hRehash();
        }
        return;

      }
      hash--;
    } while(true);
  }

  private void hRehash() {
    int oldsize = this.hcapacity;
    int newsize = oldsize * 2 + 1;
    Object[] oldkeys = this.objectCache;
    int[] oldvalues = this.handles;
    objectCache = new Object[newsize];
    handles = new int[newsize];

    this.hcapacity = newsize;
    this.hthreshold = (int)(newsize * 0.75f);
    this.hoccupancy = 0;

    for (int oldindex = 0; oldindex < oldsize; ++oldindex) {
      Object key = oldkeys[oldindex];
      if (key != null)
        put(key, oldvalues[oldindex]);
    }
  }

  private void putReplacement(Object key, Object handle){
    int cap = rcapacity;
    int hash = System.identityHashCode(key) % cap;
    Object k[] = replacementCache;
    do {
      if(hash < 0) hash += cap;

      Object k2 = k[hash];

      if(k2 == null) {
        k[hash] = key;
        this.replacements[hash] = handle;

        roccupancy++;

        if (roccupancy >= rthreshold) {
          rRehash();
        }
        return;

      }
      hash--;
    } while(true);
  }

  private void rRehash() {
    int oldsize = this.rcapacity;
    int newsize = oldsize * 2 + 1;
    Object[] oldkeys = this.replacementCache;
    Object[] oldvalues = this.replacements;
    replacementCache = new Object[newsize];
    replacements = new Object[newsize];

    this.rcapacity = newsize;
    this.rthreshold = (int)(newsize * 0.75f);
    this.roccupancy = 0;

    for (int oldindex = 0; oldindex < oldsize; ++oldindex) {
      Object key = oldkeys[oldindex];
      if (key != null)
        putReplacement(key, oldvalues[oldindex]);
    }
  }


/** end of internal hashtable methods */


  public abstract static class PutField {

    public PutField(){}

    public abstract void put(String name, boolean value);
    public abstract void put(String name, byte value);
    public abstract void put(String name, char value);
    public abstract void put(String name, double value);
    public abstract void put(String name, float value);
    public abstract void put(String name, int value);
    public abstract void put(String name, long value);
    public abstract void put(String name, Object value);
    public abstract void put(String name, short value);
    public abstract void write(ObjectOutput out) throws IOException;
  }
/* just to be able to write Serialized data to a file !!!.
  private static class TeeOutputStream extends OutputStream {
    private static int counter;

    private OutputStream one;
    private OutputStream two;

    TeeOutputStream(OutputStream out) throws IOException{
      one = out;
      two = new FileOutputStream("SER"+(counter++));
    }

    public void write(int ch) throws IOException {
      one.write(ch);
      two.write(ch);
    }

    public void write(byte[] b,int o, int l) throws IOException {
      one.write(b,o,l);
      two.write(b,o,l);
    }

    public void write(byte[] b) throws IOException{
      one.write(b,0,b.length);
      two.write(b,0,b.length);
    }

    public void flush() throws IOException {
      one.flush();
      two.flush();
    }

    public void close() throws IOException {
      one.close();
      two.close();
    }

  }
//*/
}
