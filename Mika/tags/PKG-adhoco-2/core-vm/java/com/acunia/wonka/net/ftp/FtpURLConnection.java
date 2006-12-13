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


package com.acunia.wonka.net.ftp;

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
