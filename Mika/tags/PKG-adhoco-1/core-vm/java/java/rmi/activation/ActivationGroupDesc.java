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

