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