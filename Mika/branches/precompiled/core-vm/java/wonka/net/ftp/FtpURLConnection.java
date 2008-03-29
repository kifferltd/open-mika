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

package wonka.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;

import java.security.Permission;

public class FtpURLConnection extends URLConnection {

  private static int clientport = 64000;
  private static final byte[] USER_BYTES = "USER ftp\r\n".getBytes();
  private static final byte[] PASS_BYTES = "PASS wonka\r\n".getBytes();
  private static final byte[] PORT_BYTES = "PORT ".getBytes();
  private static final byte[] RETR_BYTES = "RETR ".getBytes();
  private static final byte[] QUIT_BYTES = "QUIT \r\n".getBytes();
  private static final byte[] CRLF = {13,10};


  private Socket control;
  private ServerSocket acceptor;
  private Socket datastream;


  public FtpURLConnection(URL url) {
    super(url);
  }

  public synchronized void connect() throws IOException {
    if(!connected){
      int port = url.getPort();
      if(port == -1){
        port = 21;
      }
      control = new Socket(url.getHost(),port);
      control.setSoTimeout(5000);
      if(clientport < 1025){
        clientport = 64000;
      }
      port = clientport;
      while (true){
        try {
          acceptor = new ServerSocket(port);
          clientport = port - 1;
          break;
        }
        catch(IOException ioe){
          if(port < 1025){
            port = 65000;
          }
          else {
            port--;
          }
        }
      }
      OutputStream out = control.getOutputStream();
      DataInputStream in = new DataInputStream(control.getInputStream());
      String code = checkReply(in);
      if(!code.equals("220")){
        closeWithException("ftp service is unavailable");
      }

      out.write(USER_BYTES);
      code = checkReply(in);
      if(code.equals("331")){
        out.write(PASS_BYTES);
        code = checkReply(in);
        if(!code.equals("230") && !code.equals("202")){
          closeWithException("Password needed to login on "+url.getHost()+" as user ftp");
        }
      }
      else if(!code.equals("230")){
        closeWithException("Unable to login on "+url.getHost()+" as user ftp");
      }

      out.write("TYPE I\r\n".getBytes());
      code = checkReply(in);
      if(!code.equals("200") && !code.equals("502")){
        closeWithException("Failed to set type");
      }

      out.write(PORT_BYTES);
      out.write(writePort(InetAddress.getLocalHost().getAddress(),port));
      out.write(CRLF);
      code = checkReply(in);
      if(!code.equals("200")){
        closeWithException("Failed to specify port");
      }

      out.write(RETR_BYTES);
      out.write(url.getFile().getBytes());
      out.write(CRLF);
      code = checkReply(in);
      if(!code.equals("150") && !code.equals("125")){
        closeWithException("RETR request failed for "+url.getFile());
      }

      acceptor.setSoTimeout(5000);
      datastream = acceptor.accept();
      connected = true;
    }
  }

  public Permission getPermission() throws IOException {
   	return new SocketPermission(url.getHost()+":"+(url.getPort()== -1 ? 21 : url.getPort()) , "connect");
  }

  public InputStream getInputStream() throws IOException {
    if (!connected) {
      connect();
    }
    return datastream.getInputStream();
  }

  private byte[] writePort(byte[] address, int port) throws IOException {
    StringBuffer buf = new StringBuffer(128);
    for(int i = 0 ; i < address.length ; i++){
      buf.append((0xff &(char)address[i]));
      buf.append(',');
    }
    buf.append(port>>8);
    buf.append(',');
    buf.append(port & 0xff);
    return buf.toString().getBytes();
  }

  private String checkReply(DataInputStream in) throws IOException {
    String reply = null;
    do {
      reply = in.readLine();
      if(reply == null || reply.length() < 3){
        control.getOutputStream().write(QUIT_BYTES);
        control.close();
        throw new IOException("Invalid Response");
      }
      if(reply.length() == 3){
        break;
      }
      //System.out.println(reply);
    }
    while(reply.charAt(3) == '-');

    //System.out.println("RETURNING REPLY CODE "+reply.substring(0,3));

    return reply.substring(0,3);
  }

  private void closeWithException(String message) throws IOException {
    control.getOutputStream().write(QUIT_BYTES);
    control.close();
    throw new IOException(message);
  }
}
