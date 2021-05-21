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


package java.security;

import java.security.spec.AlgorithmParameterSpec;

public class  AlgorithmParameterGenerator {

  public static AlgorithmParameterGenerator getInstance(String algorithm) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, "AlgorithmParameterGenerator.");
    return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) action.spi, action.provider, algorithm);
  }

  public static AlgorithmParameterGenerator getInstance(String algorithm, String provider)
  throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "AlgorithmParameterGenerator.");
    return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) action.spi, action.provider, algorithm);
  }

  public static AlgorithmParameterGenerator getInstance(String algorithm, Provider provider)
  throws NoSuchAlgorithmException {

  SecurityAction action = new SecurityAction(algorithm, provider, "AlgorithmParameterGenerator.");
  return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) action.spi, provider, algorithm);
}

  private AlgorithmParameterGeneratorSpi paramGenSpi;
  private Provider provider;
  private String algorithm;

  protected AlgorithmParameterGenerator(AlgorithmParameterGeneratorSpi paramGenSpi, Provider provider, String algorithm){
    this.paramGenSpi = paramGenSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final AlgorithmParameters generateParameters(){
    return paramGenSpi.engineGenerateParameters();
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final void init(AlgorithmParameterSpec genParamSpec) throws InvalidAlgorithmParameterException{
    init(genParamSpec, new SecureRandom());
  }

  public final void init(AlgorithmParameterSpec genParamSpec, SecureRandom random) throws InvalidAlgorithmParameterException{
    paramGenSpi.engineInit(genParamSpec, random);
  }

  public final void init(int size){
    init(size, new SecureRandom());
  }

  public final void init(int size, SecureRandom random){
    paramGenSpi. engineInit(size, random);
  }
}
