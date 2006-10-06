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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2005, 2006 by Chris Gray,             *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/

/*
** $Id: BigInteger.c,v 1.7 2006/10/04 14:24:16 cvsroot Exp $
*/

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"

static inline w_word htom32bit(w_word x) {

#if __BYTE_ORDER == __LITTLE_ENDIAN
  w_ubyte* bytes = (w_ubyte*)&x;
  w_word result = (bytes[0]<<24) | (bytes[1]<<16) | (bytes[2]<<8) | bytes[3];

  return result;

#endif

#if __BYTE_ORDER == __BIG_ENDIAN
  return x;
#endif

}

#define mtoh32bit(x) htom32bit(x)

static inline w_word BigInteger_bytesToWord(w_ubyte* bytes, w_int nr) {
  w_word result=0;
  w_int i = 0;

  for( ; i <nr ; i++){
    result = result<<8 | bytes[i];
  }
  return result;
}

static inline w_word BigInteger_bytesToSignedWord(w_ubyte* bytes, w_int nr, w_word result) {

  w_int i;

  for (i = 0; i < nr; i++){
    result = result << 8 | bytes[i];
  }

  return result;

}

static inline w_instance BigInterger_allocByteArray(w_thread thread, w_int size) {
  w_instance Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], size);
  if(Bytes) {
    addLocalReference(thread, Bytes);
  }
  return Bytes;
}

//native methods from BigInteger ...
w_instance BigInteger_add(JNIEnv *env, w_instance ThisBigInt, w_instance Value);
w_instance BigInteger_divide(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor);
w_instance BigInteger_divideAndRemainder(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor);
w_instance BigInteger_nativeMod(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor);
w_instance BigInteger_nativeMultiply(JNIEnv *env, w_instance ThisBigInt, w_instance Value);
w_instance BigInteger_negateBytes(JNIEnv *env, w_instance ThisBigInt);
w_instance BigInteger_remainder(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor);
w_instance BigInteger_nativeSubtract(JNIEnv *env, w_instance ThisBigInt, w_instance Value);

/**
** this will function will negate the BigInteger Object and cache the Operation;
** (this functions does the same as the java 'negate()'.
*/
w_instance BigInteger_negate(JNIEnv *env, w_instance bigInteger);

w_instance BigInteger_subtractBytes(JNIEnv *env, w_instance ThisBigInt, w_instance Value);


/**
** default constructor. Set the integer fields of the new instane to correct values ...
*/
w_instance BigInteger_allocInstance(w_thread thread);

/**
** returns a BigInteger instance of the w_ulong value with the given sign
*/
w_instance BigInteger_Long2BigInt(JNIEnv *env, w_ulong value, w_int sign);

/**
** returns the class constant ZERO.
*/
w_instance BigInteger_getConstant(JNIEnv *env);

/**
** this function will order the bytes to from host to BigEndian ordering
** and will remove leading zero's. Bytes.length must be a multiple of 4.
*/
w_int BigInteger_ReorderAndNormelizeArray(w_instance Bytes);

/**
** this functions clones the array but set the size to a multiple of 4, and orders the words
** according machine endianness.
*/
w_instance BigInteger_cloneArray(w_thread thread, w_instance Array){
  w_ubyte* temp  = (w_ubyte*)instance2Array_byte(Array);
  w_int length = instance2Array_length(Array);
  w_int rlength = ((length - 1)/ 4 + 1);
  w_int offset = rlength * 4;
  w_instance Bytes = BigInterger_allocByteArray(thread, offset);
  w_int count = 0;
  w_int i;
  w_word* bytes;

  if(Bytes == NULL){
    return NULL;
  }

  bytes =  (w_word*)instance2Array_byte(Bytes);
  offset = length % 4;

  if(offset){
    bytes[count++] = BigInteger_bytesToWord(temp, offset);
    temp = temp+offset;
  }

  length = length / 4;

  for(i = 0 ; i < length ; i++){
    bytes[count++] = BigInteger_bytesToWord(temp, 4);
    temp = temp+4;
  }
  return Bytes;
}
/**
** this functions clones the array but set the size to a multiple of 4, and orders the words
** according machine endianness.
*/
w_instance BigInteger_cloneSignedArray(w_thread thread, w_instance Array, w_word msw){
  w_ubyte* temp  = (w_ubyte*)instance2Array_byte(Array);
  w_int length = instance2Array_length(Array);
  w_int rlength = ((length - 1)/ 4 + 1);
  w_int offset = rlength * 4;
  w_instance Bytes = BigInterger_allocByteArray(thread, offset);
  w_int count = 0;
  w_int i;
  w_word* bytes;

  if(Bytes == NULL){
    return NULL;
  }

  bytes =  (w_word*)instance2Array_byte(Bytes);
  offset = length % 4;

  if(offset){
    bytes[count++] = BigInteger_bytesToSignedWord(temp, offset, msw);
    temp = temp+offset;
  }

  length = length / 4;

  for(i = 0 ; i < length ; i++){
    bytes[count++] = BigInteger_bytesToWord(temp, 4);
    temp = temp+4;
  }
  return Bytes;
}

/**
** this functions clones the array but set the size to a multiple of 4.
*/
w_instance BigInteger_cloneArrayToSize(w_thread thread, w_instance Array, w_word msw, w_int size){
  w_word* arrayWords = (w_word*)instance2Array_byte(Array);
  w_int length = instance2Array_length(Array) / 4;
  w_instance Bytes = BigInterger_allocByteArray(thread, size);
  w_int k= (size / 4) - length;
  w_int count = 0;
  w_int i;
  w_word* bytes;

  if(Bytes == NULL){
    return NULL;
  }

  bytes =  (w_word*)instance2Array_byte(Bytes);

  for(i=0 ; i < k ; i++){
    bytes[count++] = msw;
  }

  for(i = 0 ; i < length ; i++){
    bytes[count++] = arrayWords[i];

  }
  return Bytes;
}

/**
** this function will remove all leading zero's.
*/
inline w_int BigInteger_NormelizeArray(w_instance Bytes){
  w_word* bytes = (w_word*)instance2Array_byte(Bytes);
  /* we should normalize the byteArray (al leading zero bytes should removed).
  ** instead of creating a new byte array object we shorten the existing one by 1 byte
  */
  if(bytes[0] == 0){
    w_int newlength = Bytes[F_Array_length]/4;
    w_word* src;
    w_word* dst = bytes;
    w_int k;
    w_int zeros=1;
    while(zeros < newlength && bytes[zeros] == 0){
      zeros++;
    }

    src = bytes+zeros;
    newlength -= zeros;

    for(k=0 ; k < newlength ; k++){
      *(dst++) = *(src++);
    }
    Bytes[F_Array_length] = newlength*4;
    return newlength;
  }
  return 1;
}

inline void BigInteger_NormelizeNegativeArray(w_instance Bytes){
  w_word* bytes = (w_word*)instance2Array_byte(Bytes);
  /* we should normalize the byteArray (al leading zero bytes should removed).
  ** instead of creating a new byte array object we shorten the existing one by 1 byte
  */
  if(bytes[0] == 0xffffffff){
    w_int newlength = Bytes[F_Array_length]/4;
    w_word* src;
    w_word* dst = bytes;
    w_int k;
    w_int ff = 0;
    while(ff < newlength-1 && bytes[ff] == 0xffffffff){
      ff++;
    }

    src = bytes+ff;
    newlength -= ff;

    for(k=0 ; k < newlength ; k++){
      *(dst++) = *(src++);
    }
    Bytes[F_Array_length] = newlength*4;
  }
}

/**
** returns the place of the highest set bit in the byte starting from the offset.
*/
inline w_int BigIntiger_HighestBit(w_word byte){
  w_int place = 1;

  //woempa(10, "Highest Bit set start %x on %i, %i\n",byte,place,offset);
  while ((byte>>place)&& place<32){
    //woempa(10, "Highest Bit set shifting %x on %i\n",(byte>>place),place);
    place++;
  }
  //woempa(10, "Highest Bit set in %x is %i\n",byte,place);
  return place;
}

/**
** will calculate 'bits' bits of the result. The divisor will be shifted 'bits' bits up.
*/
w_word BigInteger_UpShift(w_int bits,w_word* dividend, w_word* divisor, w_int dslength, w_word msw){
  w_int loop = bits;
  w_word result = 0;

  for( ; loop > 0 ; loop--){
    w_int gte = 1;
    w_int gteloop = 0;

    if(!msw){
      //let's find out if our divisor is greater or equal then the shifted divisor
      for( ; gteloop < dslength-1 ; gteloop++){
        w_word byte = ( ((divisor[gteloop]<<loop) | (divisor[gteloop+1]>>(32-loop))) & 0xffffffff);
        //woempa(10,"COMPARING dividend %x with byte %x (loop = %i)\n",*dividend,byte,loop);
        if(dividend[gteloop] < byte){
          gte = 0;
          break;
        }
        if(dividend[gteloop] > byte){
          gte = -1;
          break;
        }
      }
    }
    else {
      //woempa(10,"MSB was set: dividend[-1] = %x <--> msw %x \n",dividend[-1],msw);
      *(dividend-1) = 0;
      msw = 0;
      gte = -1;
    }
    result = result<<1;

    //woempa(10,"GTE %i if needed COMPARING dividend %x with byte %x (%i)\n",gte, *dividend,((divisor[gteloop]<<loop)&0xffffffff),gteloop);
    if(gte && ((gte == -1) ||(dividend[gteloop] >= ((divisor[gteloop]<<loop)&0xffffffff)))){
      //substract ...
      w_int i;
      w_ulong borrow = 0;
      w_word prevByte = 0;

      for(i=dslength-1 ; i >= 0 ; i--){
        w_ulong overflow = ((((w_word)divisor[i])<<loop) + (prevByte>>(32-loop))) & 0xffffffff;

        //woempa(10,"subtracting:  %x - %x - %i)\n",dividend[i],overflow, borrow);
        overflow = dividend[i] - overflow - borrow;
        dividend[i] =  (w_word)(overflow & 0xffffffff);
        prevByte = divisor[i];
        borrow = (overflow>>32) & 0x01;
        //woempa(10,"SETTING BYTE TO %x (i = %i, prevByte = %x, newBorrow = %x)\n",dividend[i],i,prevByte, borrow);

      }
      result = result | 1;
    }
  }

  {
    //loop == 0 no shifting needed ...
    w_int gte = 1;
    w_int gteloop = 0;

    if(!msw){
      //let's find out if our divisor is greater or equal then the shifted divisor
      for( ; gteloop < dslength-1 ; gteloop++){
        w_word byte = divisor[gteloop];
        //woempa(10,"COMPARING dividend %x with byte %x (loop = %i)\n",*dividend,byte,loop);
        if(dividend[gteloop] < byte){
          gte = 0;
          break;
        }
        if(dividend[gteloop] > byte){
          gte = -1;
          break;
        }
      }
    }
    else {
      //woempa(10,"MSB was set: dividend[-1] = %x <--> msw %x \n",dividend[-1],msw);
      *(dividend-1) = 0;
      msw = 0;
      gte = -1;
    }
    result = result<<1;

    //woempa(10,"GTE %i if needed COMPARING dividend %x with byte %x (%i)\n",gte, *dividend,((divisor[gteloop])&0xffffffff),gteloop);
    if(gte && ((gte == -1) ||(dividend[gteloop] >= ((divisor[gteloop])&0xffffffff)))){
      //substract ...
      w_int i;
      w_ulong borrow = 0;

      for(i=dslength-1 ; i >= 0 ; i--){
        w_ulong overflow = divisor[i];

        //woempa(10,"subtracting:  %x - %x - %i)\n",dividend[i],overflow, borrow);
        overflow = dividend[i] - overflow - borrow;
        dividend[i] =  (w_word)(overflow & 0xffffffff);
        borrow = (overflow>>32) & 0x01;
        //woempa(10,"SETTING BYTE TO %x (i = %i = %x, newBorrow = %x)\n",dividend[i],i, borrow);

      }
      result = result | 1;
    }
  }
  //woempa(10, "RETURNING %x as result of %i-divide step (MSB of dividend = %x)\n",result, bits,*dividend);
  return result;
}

/**
** one part of the division. the function will calculate 'bits' bits of the result while shifting the divisor
** shift bits down ...
*/
w_word BigInteger_DownShift(w_int bits, w_int shift, w_word* dividend, w_word* divisor, w_int dslength){
  w_word result = 0;
  w_int loop;
  bits += shift;

  for(loop=shift ; loop <= bits ; loop++){
    w_int gte = 1;
    w_int gteloop = 1;
    w_word dsbyte = (divisor[0])>>(loop);

    result = result<<1;

    //woempa(10,"COMPARING dividend %x with byte %x\n",*dividend,dsbyte);
    if(dividend[0] < dsbyte){
      continue;
      //gte = 0;
    }
    else if(dividend[0] == dsbyte){
      //let's find out if our divisor is greater or equal then the shifted divisor
      for( ; gteloop < dslength ; gteloop++){
        w_ulong byte = (((divisor[gteloop-1]<<(32-loop)) | (divisor[gteloop]>>loop)) & 0xffffffff);
        //woempa(10,"COMPARING dividend %x with byte %x\n",dividend[gteloop],byte);
        if(dividend[gteloop] < byte){
          gte = 0;
          break;
        }
        if(dividend[gteloop] > byte){
          gte = -1;
          break;
        }
      }
    }
    else {
      gte = -1;
    }

    //woempa(10,"GTE %i if needed COMPARING dividend %x with byte %x\n",gte, dividend[dslength],((divisor[dslength-1]<<(32-loop))&0xffffffff));
    if(gte && ((gte == -1) || (dividend[dslength] >= ((divisor[dslength-1]<<(32-loop))&0xffffffff)))){
      //substract ...
      w_int k;
      w_int borrow = 0;
      w_word prevByte = 0;
      for(k=dslength-1 ; k >= 0 ; k--){
        w_ulong overflow = ( (divisor[k]<<(32-loop)) | (prevByte>>loop) ) & 0xffffffff;

        overflow = dividend[k+1] - overflow - borrow;
        dividend[k+1] =  (w_word)(overflow & 0xffffffff);
        prevByte = divisor[k];
        borrow = (overflow>>32) & 0x01;
        //woempa(10,"SETTING BYTE TO %x (i = %i, prevByte = %x, newBorrow = %i)\n",dividend[i+1],i+1,prevByte, borrow);
      }
      //woempa(10,"subtracting:  %x - %x - %i)\n",dividend[0],((divisor[0])>>(loop)), borrow);
      dividend[0] = dividend[0] - ((divisor[0])>>(loop)) - borrow;
      //woempa(10,"SETTING BYTE TO %x (i = %i)\n",dividend[0],0);
      result = result | 1;
    }
  }

  //woempa(10, "RETURNING %x step (MSB of dividend = %x)\n",result,*dividend);
  return result;
}

/**
** the core Engine of the divide, remainder, mod and divideAndRemainder operation ...
** returns a byte array containing the modulus/remainder of the division.
*/
w_instance BigInteger_division(JNIEnv *env, w_instance Mod, w_instance Bytes, w_instance Result){

  w_thread thread = JNIEnv2w_thread(env);
  w_word* resultWords = (w_word*)instance2Array_byte(Result);
  w_word* bytes  = (w_word*)instance2Array_byte(Bytes);
  w_int dslength = instance2Array_length(Bytes);

  if(dslength <= 4){
    //case if we divide a large number by an int (unsigned 32 bit number)...
    w_word dvsr = bytes[0];
    w_int length;
    w_word* modWords;
    w_ulong mod=0;
    w_int idx = 0;
    w_int i=4;
    w_instance Array = BigInterger_allocByteArray(thread, i);

    if(Array == NULL){
      return NULL;
    }

    modWords = (w_word*)instance2Array_byte(Mod);
    length = instance2Array_length(Mod) / 4;

    mod = modWords[0] / dvsr;
    if(mod){
      resultWords[idx++] = (w_word)mod;
    }
    mod = (w_ulong)modWords[0] % dvsr;

    for(i=1 ; i < length ; i++){
      mod = (mod << 32) | modWords[i];
      resultWords[idx++] = ((w_word)(mod/dvsr));
      mod = mod % dvsr;
    }

    modWords = (w_word*)instance2Array_byte(Array);
    modWords[0] = (w_word)mod;
    if((idx * 4) < instance2Array_length(Result)){
      Result[F_Array_length] = idx * 4;
    }
    return Array;
  }
  else{
    //worst case... we divede a large Number by another large number ...
    //step 1 transform all w_ubyte arrays to w_word arrays (ordered properly)...
    w_word * modWords;

    if(bytes == NULL){
      return NULL;
    }

    //woempa(10,"length = %i\n",instance2Array_length(Mod)/4);
    Mod = cloneArray(thread, Mod);

    if(Mod != NULL){
      w_int rlength = instance2Array_length(Result)/4;
      w_int loop=0;
      w_int dvhigh;
      w_int dshigh;
      w_word result = 0;
      w_word msw=0;

      addLocalReference(thread, Mod);

      //woempa(10,"rlength = %i\n",rlength);

      modWords = (w_word*)instance2Array_byte(Mod);
      dslength = (dslength-1)/ 4 + 1;
      //woempa(10,"dslength = %i\n",dslength);
      //all pointers are set all w_word* are multiples of 4 ...
      dvhigh = BigIntiger_HighestBit(modWords[0]);
      dshigh = BigIntiger_HighestBit(bytes[0]);

      //woempa(10,"dvhigh = %i, dshigh %i\n",dvhigh,dshigh);

      if(dvhigh < dshigh){
        if(dslength >= (instance2Array_length(Mod)/4)){
          return Mod;
        }
        result = BigInteger_DownShift(dvhigh-1, dshigh-dvhigh, modWords, bytes, dslength);
        //woempa(10, "shifting result %x to %x\n",result,result<<(33-dvhigh));
        result = result<<(33-dshigh);
        dvhigh = 32;
        msw = modWords[0];
        modWords++;
        if(dvhigh/8==dvhigh/8){
          loop++;
        }
      }
      result = result | BigInteger_UpShift(dvhigh-dshigh, modWords, bytes, dslength, msw);
      //woempa(10, "setting result[%i] word to %x\n",loop,result);
      resultWords[loop++] = result;

      for( ; loop < rlength ; loop++){
        w_word step = BigInteger_DownShift(dshigh-2, 1, modWords, bytes, dslength);
        modWords++;
        result =  BigInteger_UpShift(32-dshigh, modWords, bytes, dslength, modWords[-1]);
        //woempa(10,"combining %x | %x\n",(step<<(33-dshigh)), result);
        result = (step<<(33-dshigh)) | result;
        //woempa(10, "setting result[%i] word to %x\n",loop,result);
        resultWords[loop] = result;
      }
    }
  }
  return Mod;
}

w_instance BigInteger_divide(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor){

  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude = getReferenceField(ThisBigInt, F_BigInteger_magnitude);
  w_instance ValueMagnitude;
  w_int thisSign = getIntegerField(ThisBigInt, F_BigInteger_signum);
  w_int valSign;
  w_int length;
  w_int vlength;
  w_ubyte* val;

  if(Divisor == NULL){
    throwException(thread,clazzNullPointerException,NULL);
    return NULL;
  }

  valSign = getIntegerField(Divisor, F_BigInteger_signum);

  if(valSign == 0){
    throwException(thread,clazzArithmeticException,NULL);
    return NULL;
  }

  if(thisSign == 0){
    return ThisBigInt;
  }

  if(valSign == -1){
    Divisor = BigInteger_negate(env, Divisor);
    if(Divisor == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }

  ValueMagnitude = getReferenceField(Divisor, F_BigInteger_valueCache);

  if(ValueMagnitude == NULL){
    ValueMagnitude = getReferenceField(Divisor, F_BigInteger_magnitude);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    ValueMagnitude = BigInteger_cloneArray(thread, ValueMagnitude);
    setReferenceField(Divisor, ValueMagnitude, F_BigInteger_valueCache);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  val = (w_ubyte*)instance2Array_byte(ValueMagnitude);
  vlength = instance2Array_length(ValueMagnitude);

  if(vlength == 4 && ((w_word*)val)[0] == 1){
    return (valSign == 1 ? ThisBigInt : BigInteger_negate(env, ThisBigInt));
  }

  if(thisSign == -1){
    ThisBigInt = BigInteger_negate(env, ThisBigInt);
    if(ThisBigInt == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }

  Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);
  if(Magnitude == NULL){
     w_instance Array = getReferenceField(ThisBigInt, F_BigInteger_magnitude);
    if(Array == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    Magnitude = BigInteger_cloneArray(thread, Array);
    setReferenceField(ThisBigInt, Magnitude, F_BigInteger_valueCache);

    if(Magnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  length = instance2Array_length(Magnitude);

  if(length < vlength){
    //woempa(10,"this length %i < other length %i\n",length, vlength);
    return BigInteger_getConstant(env);
  }

  if(length <= 8){
    w_word * magWords = (w_word*)instance2Array_byte(Magnitude);
    w_word * valWords = (w_word*)val;
    w_ulong dividend = magWords[0];
    w_ulong divisor = valWords[0];

    if(length == 8){
      dividend = (dividend<<32) | magWords[1];
      if(vlength == 8){
        divisor = (divisor<<32) | valWords[1];
      }
    }

    return BigInteger_Long2BigInt(env, dividend / divisor, valSign*thisSign);
  }

  {
    w_int rlength = length - vlength + 4;
    w_instance Bytes;
    w_instance Result;

    Bytes = BigInterger_allocByteArray(thread, rlength);
    if (Bytes == NULL){
      return NULL;
    }

    Result = BigInteger_allocInstance(thread);
    if (Result == NULL){
      return NULL;
    }

    BigInteger_division(env, Magnitude, ValueMagnitude, Bytes);

    setReferenceField(Result, Bytes, F_BigInteger_valueCache);
    setIntegerField(Result, F_BigInteger_signum, BigInteger_NormelizeArray(Bytes) ? 1 : 0);

    Bytes = cloneArray(thread,Bytes);

    if(Bytes == NULL){
      return NULL;
    }
    addLocalReference(thread, Bytes);
    BigInteger_ReorderAndNormelizeArray(Bytes);

    //woempa(10,"setting result magnitude to %p\n",Bytes);

    setReferenceField(Result, Bytes, F_BigInteger_magnitude);

    return (thisSign*valSign == -1 ? BigInteger_negate(env, Result) : Result);
  }
}

w_instance BigInteger_nativeMultiply(JNIEnv *env, w_instance ThisBigInt, w_instance Value){

  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude;
  w_instance ValueMagnitude;
  w_int thisSign = getIntegerField(ThisBigInt, F_BigInteger_signum);
  w_int valSign;
  w_int length;
  w_int vlength;
  w_ubyte* magnitude;
  w_ubyte* val;

  if(Value == NULL){
    throwException(thread,clazzNullPointerException,NULL);
    return NULL;
  }

  if(thisSign == 0){
    return ThisBigInt;
  }

  valSign = getIntegerField(Value, F_BigInteger_signum);

  if(valSign == 0){
    return Value;
  }
  //check this sign (if negative we negate it, else we make sure Magnitude is non NULL)
  if(thisSign == -1){
    ThisBigInt = BigInteger_negate(env, ThisBigInt);
    if(ThisBigInt == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }
  Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);
  if(Magnitude == NULL){
     w_instance Array = getReferenceField(ThisBigInt, F_BigInteger_magnitude);

    if(Array == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    Magnitude = BigInteger_cloneArray(thread, Array);
    setReferenceField(ThisBigInt, Magnitude, F_BigInteger_valueCache);

    if(Magnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  } 

  length = instance2Array_length(Magnitude);
  magnitude = (w_ubyte*)instance2Array_byte(Magnitude);

  if(length == 4 && ((w_word*)magnitude)[0] == 1){
    //woempa(10,"RETURN THIS RIGHT AWAY\n");
    return (thisSign == 1 ? Value : BigInteger_negate(env, Value));
  }

  //check the sign of Value (if negative we negate it, else we make sure Magnitude is non NULL)
  if(valSign == -1){
    Value = BigInteger_negate(env, Value);
    if(Value == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }

  ValueMagnitude = getReferenceField(Value, F_BigInteger_valueCache);
  if(ValueMagnitude == NULL){
    ValueMagnitude = getReferenceField(Value, F_BigInteger_magnitude);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    ValueMagnitude = BigInteger_cloneArray(thread, ValueMagnitude);
    setReferenceField(Value, ValueMagnitude, F_BigInteger_valueCache);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  val = (w_ubyte*)instance2Array_byte(ValueMagnitude);
  vlength = instance2Array_length(ValueMagnitude);

  if(vlength == 4 && ((w_word*)val)[0] == 1){
    //woempa(10,"RETURN THIS RIGHT AWAY\n");
    return (valSign*thisSign == 1 ? ThisBigInt : BigInteger_negate(env, ThisBigInt));
  }

  {
    //DONE checking ... lets calculate
    w_instance Result = BigInteger_allocInstance(thread);
    w_int pos = length + vlength;
    w_instance Bytes = BigInterger_allocByteArray(thread, pos);
    w_word * magWords = (w_word*) allocMem(length);
    w_word * valWords = (w_word*) (val);
    w_ubyte* bytes;
    w_word* resultBytes;
    w_int wordlength = length / 4;
    w_int i;

    if(Bytes == NULL || Result == NULL || magWords == NULL){
      if(magWords){
        releaseMem(magWords);
      }
      if(Result) {
        addLocalReference(thread, Result);
      }
      return NULL;
    }

    setIntegerField(Result, F_BigInteger_signum, 1);
    setReferenceField(Result, Bytes, F_BigInteger_valueCache);

    bytes = (w_ubyte*)instance2Array_byte(Bytes);
    resultBytes =  (w_word*)bytes;

    pos = (pos / 4) -1;

    {
      w_word* word = (w_word*)(magnitude);
      w_int l = length / 4 - 1;
      w_int count=0;

      for( ; l >= 0 ; l--){
        magWords[count++] = word[l];
      }
    }

    for(i = vlength/4 - 1 ; i >= 0 ; i--){
      w_ulong carry = 0;
      w_ulong valByte = valWords[i];
      w_int j;

      for(j = 0 ; j < wordlength; j++){
        w_int idx = pos - j;

        carry = (carry>>32);
        carry += valByte * magWords[j];
        carry += (resultBytes[idx]);
        resultBytes[idx] = ((w_word)(carry & 0xffffffff));
      }
      resultBytes[pos - wordlength] = ((w_word)(carry>>32 & 0xffffffff));
      pos--;
    }
    releaseMem(magWords);

    BigInteger_NormelizeArray(Bytes);
    Bytes = cloneArray(thread,Bytes);

    if(Bytes == NULL){
      return NULL;
    }

    setReferenceField(Result, Bytes, F_BigInteger_magnitude);
    BigInteger_ReorderAndNormelizeArray(Bytes);

    return (thisSign != valSign ? BigInteger_negate(env, Result) : Result);
  }
}

w_instance BigInteger_negateBytes(JNIEnv *env, w_instance ThisBigInt){

  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude =  getReferenceField(ThisBigInt, F_BigInteger_magnitude);
  w_instance Bytes;
  w_int length;

  if(Magnitude == NULL){
    throwException(thread,clazzNullPointerException,NULL);
    return NULL;
  }

  length = instance2Array_length(Magnitude);
  Bytes = BigInterger_allocByteArray(thread, length);

  if(Bytes){
    w_int j=1;
    w_ubyte* magnitude = (w_ubyte*)instance2Array_byte(Magnitude);
    w_ubyte* bytes = (w_ubyte*)instance2Array_byte(Bytes);
    w_int i;

    for (i = length-1 ; i>=0 ; i--){
      w_int hlp = (0xff ^ magnitude[i]) + j;
      j = (hlp & 0x100)>>8;
      bytes[i] = hlp & 0xff;		
    }
  }
  return Bytes;
}

w_instance BigInteger_negate(JNIEnv *env, w_instance bigInteger){

  w_instance Negated = getReferenceField(bigInteger, F_BigInteger_negateCache);

  if(Negated == NULL){
    w_thread thread = JNIEnv2w_thread(env);
    w_instance Bytes = BigInteger_negateBytes(env, bigInteger);

    if(Bytes == NULL){
      return NULL;
    }

    Negated = BigInteger_allocInstance(thread);

    if(Negated == NULL){
      return NULL;
    }

    setIntegerField(Negated, F_BigInteger_signum, - getIntegerField(bigInteger, F_BigInteger_signum));
    setReferenceField(Negated, Bytes, F_BigInteger_magnitude);
    setReferenceField(Negated, bigInteger, F_BigInteger_negateCache);
    setReferenceField(bigInteger, Negated, F_BigInteger_negateCache);
  }

  return Negated;
}

w_instance BigInteger_Long2BigInt(JNIEnv *env, w_ulong value, w_int sign){

  w_thread thread = JNIEnv2w_thread(env);
  w_instance Result = BigInteger_allocInstance(thread);
  w_int length = 8;
  w_instance Bytes = BigInterger_allocByteArray(thread,length);
  w_word * word = (w_word*)&value;
  w_word * arrayWords;
  w_ubyte * bytes;

  if(Result == NULL || Bytes == NULL){
    if(Result) {
      addLocalReference(thread, Result);
    }
    return NULL;
  }

  setIntegerField(Result, F_BigInteger_signum, 1);
  setReferenceField(Result, Bytes, F_BigInteger_magnitude);

  bytes = (w_ubyte*)instance2Array_byte(Bytes);
  arrayWords = (w_word*)bytes;
  arrayWords[0] = mtoh32bit(word[WORD_MSW]);
  arrayWords[1] = mtoh32bit(word[WORD_LSW]);

  {
    w_int zeros = 0;
    w_ubyte * src;


    while(zeros < length && bytes[zeros] == 0){
      zeros++;
    }

    src = bytes+zeros;
    length -= zeros;

    for(zeros=0 ; zeros < length ; zeros++){
      *(bytes++) = *(src++);
    }
    Bytes[F_Array_length] = length;
    if(length == 0){
      setIntegerField(Result, F_BigInteger_signum, 0);
      return Result;
    }
  }


  return (sign == 1 ? Result : BigInteger_negate(env, Result));
}

w_instance BigInteger_getConstant(JNIEnv *env){
  jclass jclazz = (*env)->FindClass(env, "java/math/BigInteger");
  if(jclazz){
    jfieldID id = (*env)->GetStaticFieldID(env, jclazz, "ZERO", "Ljava/math/BigInteger;");
    if(id){
      return (*env)->GetStaticObjectField(env, jclazz, id);
    }
  }
  return NULL;
}

w_int BigInteger_ReorderAndNormelizeArray(w_instance Bytes){
  w_ubyte* bytes = (w_ubyte*)instance2Array_byte(Bytes);

#if __BYTE_ORDER == __LITTLE_ENDIAN
  {
    w_word* byteWords = (w_word*)instance2Array_byte(Bytes);

    w_int stop = instance2Array_length(Bytes) / 4;
    w_int l;

    for(l=0 ; l < stop ; l++){
      //woempa(10,"normelizing word[%i] %x to %x\n",l,byteWords[l] ,htom32bit(byteWords[l]));
      byteWords[l]  = htom32bit(byteWords[l]);
    }
  }
#endif

  /* we should normalize the byteArray (al leading zero bytes should removed).
  ** instead of creating a new byte array object we shorten the existing one by 1 byte
  */
  if(bytes[0] == 0){
    w_int newlength = Bytes[F_Array_length];
    w_ubyte* src;
    w_ubyte* dst = bytes;
    w_int k;
    w_int zeros=1;
    while(zeros < newlength && bytes[zeros] == 0){
      zeros++;
    }

    src = bytes+zeros;
    newlength -= zeros;

    for(k=0 ; k < newlength ; k++){
      *(dst++) = *(src++);
    }
    Bytes[F_Array_length] = newlength;
    return newlength;
  }
  return 1;
}

w_int BigInteger_ReorderAndNormelizeNegativeArray(w_instance Bytes){
  w_ubyte* bytes = (w_ubyte*)instance2Array_byte(Bytes);

#if __BYTE_ORDER == __LITTLE_ENDIAN
  {
    w_word* byteWords = (w_word*)instance2Array_byte(Bytes);

    w_int stop = instance2Array_length(Bytes) / 4;
    w_int l;

    for(l=0 ; l < stop ; l++){
      //woempa(10,"normelizing word[%i] %x to %x\n",l,byteWords[l] ,htom32bit(byteWords[l]));
      byteWords[l]  = htom32bit(byteWords[l]);
    }
  }
#endif

  /* we should normalize the byteArray (al leading zero bytes should removed).
  ** instead of creating a new byte array object we shorten the existing one by 1 byte
  */
  if(bytes[0] == 0xff){
    w_int newlength = Bytes[F_Array_length]-1;
    w_ubyte* src;
    w_ubyte* dst = bytes;
    w_int k;
    w_int zeros=1;
    while(zeros < newlength && bytes[zeros] == 0xff){
      zeros++;
    }

    src = bytes+zeros;
    newlength = newlength + 1 - zeros;

    //woempa(10,"cutting array to length %i ff bytes %i from %i\n",newlength, zeros,Bytes[F_Array_length]);

    for(k=0 ; k < newlength ; k++){
      *(dst++) = *(src++);
    }
    Bytes[F_Array_length] = newlength;
    return newlength;
  }
  return 1;
}


w_instance BigInteger_remainder(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor){
  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);
  w_instance ValueMagnitude;
  w_int thisSign = getIntegerField(ThisBigInt, F_BigInteger_signum);
  w_int valSign;
  w_int length;
  w_int vlength;
  w_ubyte* val;

  if(Divisor == NULL){
    throwException(thread,clazzNullPointerException,NULL);
    return NULL;
  }

  valSign = getIntegerField(Divisor, F_BigInteger_signum);

  if(valSign == 0){
    throwException(thread,clazzArithmeticException,NULL);
    return NULL;
  }

  if(thisSign == 0){
    return ThisBigInt;
  }

  if(valSign == -1){
    Divisor = BigInteger_negate(env, Divisor);
    if(Divisor == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }

  ValueMagnitude = getReferenceField(Divisor, F_BigInteger_valueCache);

  if(ValueMagnitude == NULL){
    ValueMagnitude = getReferenceField(Divisor, F_BigInteger_magnitude);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    ValueMagnitude = BigInteger_cloneArray(thread, ValueMagnitude);
    setReferenceField(Divisor, ValueMagnitude, F_BigInteger_valueCache);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  val = (w_ubyte*)instance2Array_byte(ValueMagnitude);
  vlength = instance2Array_length(ValueMagnitude);

  if(vlength == 4 && ((w_word*)val)[0] == 1){
    return BigInteger_getConstant(env);
  }

  if(thisSign == -1){
    ThisBigInt = BigInteger_negate(env, ThisBigInt);
    if(ThisBigInt == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }

  if(Magnitude == NULL){
     w_instance Array = getReferenceField(ThisBigInt, F_BigInteger_magnitude);

    if(Array == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    Magnitude = BigInteger_cloneArray(thread, Array);
    setReferenceField(ThisBigInt, Magnitude, F_BigInteger_valueCache);

    if(Magnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  length = instance2Array_length(Magnitude);

  if(length < vlength){
    return thisSign == -1 ? getReferenceField(ThisBigInt, F_BigInteger_negateCache) : ThisBigInt;
  }

  if(length <= 8){
    w_word * magWords = (w_word*)instance2Array_byte(Magnitude);
    w_word * valWords = (w_word*)val;
    w_ulong dividend = magWords[0];
    w_ulong divisor = valWords[0];

    if(length == 8){
      dividend = (dividend<<32) | magWords[1];
      if(vlength == 8){
        divisor = (divisor<<32) | valWords[1];
      }
    }
    //woempa(10,"calculating '%x %x' % '%x %x' = '%x %x'\n",dividend,divisor, dividend % divisor);
    return BigInteger_Long2BigInt(env, dividend % divisor, thisSign);
  }
  else {
    w_int rlength = length - vlength + 4;
    w_instance Bytes = BigInterger_allocByteArray(thread, rlength);
    w_instance Result = BigInteger_allocInstance(thread);

    if(Bytes == NULL || Result == NULL){
      if(Result) {
        addLocalReference(thread, Result);
      }
      return NULL;
    }

    Bytes = BigInteger_division(env, Magnitude, ValueMagnitude, Bytes);

    setIntegerField(Result, F_BigInteger_signum, BigInteger_NormelizeArray(Bytes) ? 1 : 0);
    setReferenceField(Result, Bytes, F_BigInteger_magnitude);

    Bytes = cloneArray(thread,Bytes);

    if(Bytes == NULL){
      return NULL;
    }

    BigInteger_ReorderAndNormelizeArray(Bytes);
    setReferenceField(Result, Bytes, F_BigInteger_magnitude);

    return (thisSign == -1 ? BigInteger_negate(env, Result) : Result);
  }
}

w_instance BigInteger_nativeMod(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor){
  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);
  w_instance ValueMagnitude;
  w_int thisSign = getIntegerField(ThisBigInt, F_BigInteger_signum);
  w_int valSign;
  w_int length;
  w_int vlength;
  w_ubyte* val;

  if(Divisor == NULL){
    throwException(thread,clazzNullPointerException,NULL);
    return NULL;
  }

  valSign = getIntegerField(Divisor, F_BigInteger_signum);

  if(valSign != 1){
    throwException(thread,clazzArithmeticException,NULL);
    return NULL;
  }

  if(thisSign == 0){
    return ThisBigInt;
  }

  ValueMagnitude = getReferenceField(Divisor, F_BigInteger_valueCache);

  if(ValueMagnitude == NULL){
    ValueMagnitude = getReferenceField(Divisor, F_BigInteger_magnitude);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    ValueMagnitude = BigInteger_cloneArray(thread, ValueMagnitude);
    setReferenceField(Divisor, ValueMagnitude, F_BigInteger_valueCache);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  BigInteger_NormelizeArray(ValueMagnitude);
  val = (w_ubyte*)instance2Array_byte(ValueMagnitude);
  vlength = instance2Array_length(ValueMagnitude);

  if(vlength == 4 && ((w_word*)val)[0] == 1){
    return BigInteger_getConstant(env);
  }

  if(thisSign == -1){
    ThisBigInt = BigInteger_negate(env, ThisBigInt);
    if(ThisBigInt == NULL){
      //Something went wrong (exception is alreday set ...)
      return NULL;
    }
  }

  Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);

  if(Magnitude == NULL){
     w_instance Array = getReferenceField(ThisBigInt, F_BigInteger_magnitude);

    if(Array == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    Magnitude = BigInteger_cloneArray(thread, Array);
    setReferenceField(ThisBigInt, Magnitude, F_BigInteger_valueCache);

    if(Magnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }


  length = instance2Array_length(Magnitude);

  if(length < vlength){
    if(thisSign == -1){
      return BigInteger_subtractBytes(env, Divisor, ThisBigInt);
    }
    return ThisBigInt;
  }

  if(length <= 8){
    w_word * magWords = (w_word*)instance2Array_byte(Magnitude);
    w_word * valWords = (w_word*)val;
    w_ulong dividend = magWords[0];
    w_ulong divisor = valWords[0];
    w_ulong result;

    if(length == 8){
      dividend = (dividend<<32) | magWords[1];
      if(vlength == 8){
        divisor = (divisor<<32) | valWords[1];
      }
    }

    result = dividend % divisor;

    if(thisSign == -1){
       //woempa(10,"divisor LSW %i MSW %x - result LSW %i MSW %x = LSW %is MSW %x\n",divisor, result, divisor - result);
      result = divisor - result;
    }
    //woempa(10,"calculating mod '%x %x' % '%x %x' = '%x %x'\n",dividend,divisor, result);

    return BigInteger_Long2BigInt(env, result, 1);
  }
  else {
    w_int rlength = length - vlength + 4;
    w_instance Bytes = BigInterger_allocByteArray(thread, rlength);
    w_instance Result = BigInteger_allocInstance(thread);

    if(Bytes == NULL || Result == NULL){
      if(Result) {
        addLocalReference(thread, Result);
      }
      return NULL;
    }

    Bytes = BigInteger_division(env, Magnitude, ValueMagnitude, Bytes);

    setIntegerField(Result, F_BigInteger_signum, BigInteger_NormelizeArray(Bytes) ? 1 : 0);
    setReferenceField(Result, Bytes, F_BigInteger_valueCache);

    Bytes = cloneArray(thread,Bytes);

    if(Bytes == NULL){
      return NULL;
    }

    addLocalReference(thread, Bytes);

    BigInteger_ReorderAndNormelizeArray(Bytes);
    setReferenceField(Result, Bytes, F_BigInteger_magnitude);

    if(thisSign == -1){
      //woempa(10,"returning subtracted mod ...\n");
      return BigInteger_subtractBytes(env, Divisor, Result);
    }

    return Result;
  }
}

w_instance BigInteger_divideAndRemainder(JNIEnv *env, w_instance ThisBigInt, w_instance Divisor){
  return NULL;
}

w_instance BigInteger_subtractBytes(JNIEnv *env, w_instance ThisBigInt, w_instance Value){
  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude = getReferenceField(ThisBigInt, F_BigInteger_magnitude);
  w_word msw = getIntegerField(ThisBigInt, F_BigInteger_signum) == 1 ? 0 : 0xffffffff;
  w_instance ValueMagnitude;
  w_instance Result;
  w_int length;
  w_int vlength;
  w_int size;

  ValueMagnitude = getReferenceField(Value, F_BigInteger_valueCache);

  if(ValueMagnitude == NULL){
    ValueMagnitude = getReferenceField(Value, F_BigInteger_magnitude);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    ValueMagnitude = BigInteger_cloneSignedArray(thread, ValueMagnitude, msw);
    setReferenceField(Value, ValueMagnitude, F_BigInteger_valueCache);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);

  if(Magnitude == NULL){
     w_instance Array = getReferenceField(ThisBigInt, F_BigInteger_magnitude);

    if(Array == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    Magnitude = BigInteger_cloneSignedArray(thread, Array, msw);
    setReferenceField(ThisBigInt, Magnitude, F_BigInteger_valueCache);

    if(Magnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  vlength = instance2Array_length(ValueMagnitude);
  length = instance2Array_length(Magnitude);

  size = length > vlength ? length : vlength;

  //woempa(10,"cloning array to size %i (%i <--> %i)\n",size*4,length,vlength);

  Magnitude = BigInteger_cloneArrayToSize(thread, Magnitude, msw, size*4);
  Result = BigInteger_allocInstance(thread);

  if(Magnitude == NULL || Result == NULL){
    return NULL;
  }
  else {
    w_word* magWords = (w_word*)instance2Array_byte(Magnitude);
    w_word* valWords = (w_word*)instance2Array_byte(ValueMagnitude);
    w_int i;
    w_word borrow = 0;

    size--;

    for(i = vlength/4 - 1 ; i >= 0; i--){
      w_ulong word = (w_ulong)magWords[size] - (w_ulong)valWords[i] - (w_ulong)borrow;
      magWords[size--] = (w_word)word;
      borrow = (word & 0x100000000LL)>>32;
      //woempa(10,"SETTING WORD[%i] to %x (borrow %x, %x %x)\n",size+1, magWords[size+1],borrow,word);
    }

    for (; size >= 0;){
      w_ulong word = (w_ulong)magWords[size] - (w_ulong)msw - (w_ulong)borrow;
      magWords[size--] = (w_word)word;
      borrow = (word & 0x100000000LL)>>32;
      //woempa(10,"SETTING WORD[%i] to %x (borrow %x, %x %x)\n",size+1, magWords[size+1],borrow,word);
    }

    setReferenceField(Result, Magnitude, F_BigInteger_valueCache);

    if(instance2Array_byte(Magnitude)[0]  < 0){
      BigInteger_NormelizeNegativeArray(Magnitude);
      setIntegerField(Result, F_BigInteger_signum,  -1);
    }
    else {
      setIntegerField(Result, F_BigInteger_signum,  (BigInteger_NormelizeArray(Magnitude) ? 1 : 0));
    }

    Magnitude = cloneArray(thread,Magnitude);

    if(Magnitude == NULL){
      throwOutOfMemoryError(thread);
      return NULL;
    }

    addLocalReference(thread, Magnitude);

    if(getIntegerField(Result, F_BigInteger_signum) == -1){
      //woempa(10,"Normelizing negative Array\n");
      BigInteger_ReorderAndNormelizeNegativeArray(Magnitude);
    }
    else {
      BigInteger_ReorderAndNormelizeArray(Magnitude);
    }

    setReferenceField(Result, Magnitude, F_BigInteger_magnitude);

    return Result;
  }
}

/**
** represents the default constructor ..
*/
w_instance BigInteger_allocInstance(w_thread thread){
  w_instance newBig = allocInstance(thread,clazzBigInteger);

  if(newBig){
    setIntegerField(newBig, F_BigInteger_lowestSetBit, -2);
    setIntegerField(newBig, F_BigInteger_firstNonzeroByteNum, -2);
    setIntegerField(newBig, F_BigInteger_bitCount, -1);
    setIntegerField(newBig, F_BigInteger_bitLength, -1);
  }

  return newBig;
}

w_instance BigInteger_addBytes(JNIEnv *env, w_instance ThisBigInt, w_instance Value){
  w_thread thread = JNIEnv2w_thread(env);
  w_instance Magnitude = getReferenceField(ThisBigInt, F_BigInteger_magnitude);
  w_word msw = getIntegerField(ThisBigInt, F_BigInteger_signum) == 1 ? 0 : 0xffffffff;
  w_instance ValueMagnitude;
  w_instance Result;
  w_int length;
  w_int vlength;
  w_int size;

  ValueMagnitude = getReferenceField(Value, F_BigInteger_valueCache);

  if(ValueMagnitude == NULL){
    ValueMagnitude = getReferenceField(Value, F_BigInteger_magnitude);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    ValueMagnitude = BigInteger_cloneSignedArray(thread, ValueMagnitude, msw);
    setReferenceField(Value, ValueMagnitude, F_BigInteger_valueCache);

    if(ValueMagnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  Magnitude = getReferenceField(ThisBigInt, F_BigInteger_valueCache);

  if(Magnitude == NULL){
     w_instance Array = getReferenceField(ThisBigInt, F_BigInteger_magnitude);

    if(Array == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }

    Magnitude = BigInteger_cloneSignedArray(thread, Array, msw);
    setReferenceField(ThisBigInt, Magnitude, F_BigInteger_valueCache);

    if(Magnitude == NULL){
      throwException(thread,clazzNullPointerException,NULL);
      return NULL;
    }
  }

  vlength = instance2Array_length(ValueMagnitude);
  length = instance2Array_length(Magnitude);

  size = length > vlength ? length : vlength;

  //woempa(10,"cloning array to size %i (%i <--> %i)\n",size*4,length,vlength);

  Magnitude = BigInteger_cloneArrayToSize(thread, Magnitude, msw, size*4);
  Result = BigInteger_allocInstance(thread);

  if(Magnitude == NULL || Result == NULL){
    return NULL;
  }
  else {
    w_word* magWords = (w_word*)instance2Array_byte(Magnitude);
    w_word* valWords = (w_word*)instance2Array_byte(ValueMagnitude);
    w_int i;
    w_word carry = 0;

    size--;

    for(i = vlength/4 - 1 ; i >= 0; i--){
      w_ulong word = (w_ulong)magWords[size] + (w_ulong)valWords[i] + (w_ulong)carry;
      magWords[size--] = (w_word)word;
      carry = (word & 0x100000000LL)>>32;
      //woempa(10,"SETTING WORD[%i] to %x (borrow %x, %x %x)\n",size+1, magWords[size+1],borrow,word);
    }

    for (; size >= 0 && carry;){
      w_ulong word = (w_ulong)magWords[size] + (w_ulong)msw + (w_ulong)carry;
      magWords[size--] = (w_word)word;
      carry = (word & 0x100000000LL)>>32;
      //woempa(10,"SETTING WORD[%i] to %x (borrow %x, %x %x)\n",size+1, magWords[size+1],borrow,word);
    }

    setReferenceField(Result, Magnitude, F_BigInteger_valueCache);

    if(msw){
      BigInteger_NormelizeNegativeArray(Magnitude);
      setIntegerField(Result, F_BigInteger_signum, -1);
    }
    else {
      setIntegerField(Result, F_BigInteger_signum, (BigInteger_NormelizeArray(Magnitude) ? 1 : 0));
    }

    Magnitude = cloneArray(thread,Magnitude);

    if(Magnitude == NULL){
      throwOutOfMemoryError(thread);
      return NULL;
    }
    addLocalReference(thread, Magnitude);

    if(msw){
      BigInteger_ReorderAndNormelizeNegativeArray(Magnitude);
    }
    else {
      BigInteger_ReorderAndNormelizeArray(Magnitude);
    }

    setReferenceField(Result, Magnitude, F_BigInteger_magnitude);

    return Result;
  }
}

w_instance BigInteger_add(JNIEnv *env, w_instance ThisBigInt, w_instance Value){
  w_int thisSign = getIntegerField(ThisBigInt, F_BigInteger_signum);

  if(Value == NULL){
    throwException(JNIEnv2w_thread(env),clazzNullPointerException,NULL);
    return NULL;
  }

  if(thisSign == 0){
    return Value;
  }
  else {
    w_int valSign = getIntegerField(Value, F_BigInteger_signum);

    if(valSign == 0){
      return ThisBigInt;
    }

    if(thisSign == valSign){
      return BigInteger_addBytes(env, ThisBigInt, Value);
    }
    Value = BigInteger_negate(env, Value);

    return BigInteger_subtractBytes(env, ThisBigInt, Value);
  }
}

w_instance BigInteger_nativeSubtract(JNIEnv *env, w_instance ThisBigInt, w_instance Value){
  w_int thisSign = getIntegerField(ThisBigInt, F_BigInteger_signum);

  if(Value == NULL){
    throwException(JNIEnv2w_thread(env),clazzNullPointerException,NULL);
    return NULL;
  }

  if(thisSign == 0){
    return BigInteger_negate(env, Value);
  }
  else {
    w_int valSign = getIntegerField(Value, F_BigInteger_signum);

    if(valSign == 0){
      return ThisBigInt;
    }

    if(thisSign == valSign){
      return BigInteger_subtractBytes(env, ThisBigInt, Value);
    }
    Value = BigInteger_negate(env, Value);

    return BigInteger_addBytes(env, ThisBigInt, Value);
  }
}



