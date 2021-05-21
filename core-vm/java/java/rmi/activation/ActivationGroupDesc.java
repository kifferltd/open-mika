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

package java.rmi.activation;

import java.io.Serializable;
import java.util.Properties;
import java.rmi.MarshalledObject;

public final class ActivationGroupDesc implements Serializable {

  private static final long serialVersionUID = -4936225423168276595L;

  private String className;
  private String location;
  private MarshalledObject data;
  private ActivationGroupDesc.CommandEnvironment env;
  private Properties props;

  public ActivationGroupDesc(Properties overrides, CommandEnvironment cmd) {
    env = cmd;
    props = overrides;
    //TODO ...
    //location = DEFAULT Location ...
    //data = DEFAULT data ...
  }

  public ActivationGroupDesc(String className, String location, MarshalledObject data, Properties overrides, CommandEnvironment cmd) {
    this.className = className;
    this.location = location;
    this.data = data;
    env = cmd;
    props = overrides;
  }

  public String getClassName() {
    return className;
  }

  public String getLocation() {
    return location;
  }

  public MarshalledObject getData() {
    return data;
  }

  public Properties getPropertyOverrides() {
    return props;
  }

  public CommandEnvironment getCommandEnvironment() {
    return env;
  }

  public boolean equals(Object obj) {
    if(getClass().isInstance(obj)){
      ActivationGroupDesc agd = (ActivationGroupDesc)obj;
      return (env == null ? agd.env == null : env.equals(agd.env))
          && (props == null ? agd.props == null : props.equals(agd.props))
          && (className == null ? agd.className == null : className.equals(agd.className))
          && data.equals(agd.data) && location.equals(location);
    }
    return false;
  }

  public int hashCode() {
    int hash = (env == null ? (int)serialVersionUID : env.hashCode());
    hash ^= (props == null ? (int)serialVersionUID : props.hashCode());
    hash ^= (className == null ? (int)serialVersionUID : className.hashCode());
    return data.hashCode() ^ location.hashCode() ^ hash;
  }


  public static class CommandEnvironment implements Serializable {

    private static final long serialVersionUID = 6165754737887770191L;

    private String command;
    private String[] options;


    public CommandEnvironment(String cmdpath, String[] argv){
      command = cmdpath;
      options = (argv == null ? new String[0] : argv);
    }
    
    public String getCommandPath() {
      return command;
    }
    
    public String[] getCommandOptions() {
      return options;
    }
    
    public boolean equals(Object obj) {
      if(getClass().isInstance(obj)){
        CommandEnvironment cenv = (CommandEnvironment)obj;
        return (command == null ? cenv.command == null : command.equals(cenv.command))
            && java.util.Arrays.equals(options, cenv.options);
      }
      return false;
    }
  
    public int hashCode() {
      return command == null ? (int)serialVersionUID : command.hashCode();
    }
  }
}

