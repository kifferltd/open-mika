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
import java.security.spec.InvalidParameterSpecException;
import java.io.IOException;

public class AlgorithmParameters {

  public static AlgorithmParameters getInstance(String algorithm) throws NoSuchAlgorithmException {
    SecurityAction action = new SecurityAction(algorithm, "AlgorithmParameters.");
    return new AlgorithmParameters((AlgorithmParametersSpi)action.spi, action.provider, algorithm);
  }

  public static AlgorithmParameters getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "AlgorithmParameters.");
    return new AlgorithmParameters((AlgorithmParametersSpi)action.spi, action.provider, algorithm);
  }

  public static AlgorithmParameters getInstance(String algorithm, Provider provider)
    throws NoSuchAlgorithmException {

    SecurityAction action = new SecurityAction(algorithm, provider, "AlgorithmParameters.");
    return new AlgorithmParameters((AlgorithmParametersSpi)action.spi, provider, algorithm);
  }

  private AlgorithmParametersSpi paramSpi;
  private Provider provider;
  private String algorithm;

  protected AlgorithmParameters(AlgorithmParametersSpi paramSpi, Provider provider, String algorithm) {
    this.paramSpi = paramSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final byte[] getEncoded() throws IOException {
    return paramSpi.engineGetEncoded();
  }

  public final byte[] getEncoded(String format) throws IOException {
    return paramSpi.engineGetEncoded(format);
  }

  public final AlgorithmParameterSpec getParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
    return paramSpi.engineGetParameterSpec(paramSpec);
  }

  public final Provider getProvider(){
    return provider;
  }

  public final void init(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    paramSpi.engineInit(paramSpec);
  }

  public final void init(byte[] params) throws IOException {
    paramSpi.engineInit(params);
  }

  public final void init(byte[] params, String format) throws IOException {
    paramSpi.engineInit(params, format);
  }

  public final String toString(){
    return paramSpi.engineToString();
  }
}