package wonka.vm.profile;

import java.io.*;
import java.util.*;

public class Profile {

  private class ClassData implements Comparable {
    public Hashtable methods = new Hashtable();
    public ClassLoaderData cld;
    public String name;

    public String toString() {
      return name;
    }

    public int compareTo(Object o) {
      return name.compareTo(((ClassData)o).name);
    }
  }

  private class ClassLoaderData implements Comparable {
    public int id;
    public String name;
    public Hashtable classes = new Hashtable();
    
    public String toString() {
      return "" + id + " -> " + name;
    }
    
    public int compareTo(Object o) {
      return (id < ((ClassLoaderData)o).id ? -1 : 1);
    }
  }

  private class ParameterData {
    public String type;
    public int array = 0;
    public ClassData cd;

    public ParameterData(String type, int array, ClassData cd) {
      this.type = type;
      this.array = array;
      this.cd = cd;
    }
  }
  
  private class MethodData implements Comparable {
    public int runs;
    public long bytecodes;
    public long acctime;
    public long runtime;
    public long time;
    public long instances;
    public char type;
    public String name;
    public String fullname;
    public ParameterData pd[];
    public ParameterData retType;
    public ClassData cd;

    public String toString() {
      return "" + runs + " " + bytecodes + " " + acctime + " " + runtime + " " + time + " " + instances + " " + type + " " + cd + "." + name;
    }
    
    public int compareTo(Object o) {
      return name.compareTo(((MethodData)o).fullname);
    }
  }

  private File logfile;
  private File resultDir;
  private Hashtable classloaders = new Hashtable();
  private MethodData[] allMethods;
  private Hashtable allClasses = new Hashtable();

  private Profile(String log, String results) throws Exception {
    logfile = new File(log);
    resultDir = new File(results);

    resultDir.mkdir();
    
    System.out.println("Logfile: " + logfile);
    System.out.println("Results: " + resultDir);
    
    readLogFile();

    outputClassLoaders();

    outputMethodTimings();
  }

  private ClassData getClassData(String name) {
    return (ClassData)allClasses.get(name);
  }
  
  private ClassData addClass(String name, int clid) {
    ClassData result = null;
    ClassLoaderData cld = null;
    if(clid != -1) {
      cld = (ClassLoaderData)classloaders.get(new Integer(clid));
      result = (ClassData)cld.classes.get(name);
    }
    if(result == null) {
      result = (ClassData)allClasses.get(name);
      if(result == null) {
        result = new ClassData();
        result.name = name;
        result.cld = cld;
      }
    }
    if(result.cld == null & cld != null) {
      result.cld = cld;
      cld.classes.put(name, result);
    }
    return result;
  }

  private MethodData parseMethod(String method, ClassLoaderData cld) {
    StringTokenizer st;
    MethodData md = new MethodData();

    int idx = method.indexOf('<');
    if(idx == -1) idx = method.indexOf('(');
    idx = method.lastIndexOf('.', idx);

    String clazz = method.substring(0, idx);
    md.fullname = method.substring(idx + 1, method.length());

    ClassData cd = (ClassData)allClasses.get(clazz);
    if(cd == null) {
      cd = new ClassData();
      cd.name = clazz;
      allClasses.put(clazz, cd);
      if(cld != null) {
        cld.classes.put(clazz, cd);
      }
    }

    MethodData md2 = (MethodData)cd.methods.get(md.fullname);

    if(md2 != null) return md2;

    md.cd = cd;
    cd.methods.put(md.fullname, md);
    
    if((idx = md.fullname.indexOf('<')) != -1) {
      md.fullname = md.fullname.substring(0, idx) + "&lt;" + md.fullname.substring(idx + 1, md.fullname.length());
    }

    idx = md.fullname.indexOf('(');
    String parameters = md.fullname.substring(idx + 1, md.fullname.indexOf(')'));
    String retType = md.fullname.substring(md.fullname.indexOf(')') + 1, md.fullname.length());
    md.name = md.fullname.substring(0, idx);

    int ac = 0;
    idx = 0;
    Vector params = new Vector();

    while(idx < parameters.length()) {
      switch(parameters.charAt(idx)) {
        case 'Z': params.add(new ParameterData("boolean", ac, null)); ac = 0; idx++; break;
        case 'B': params.add(new ParameterData("byte", ac, null));    ac = 0; idx++; break;
        case 'C': params.add(new ParameterData("char", ac, null));    ac = 0; idx++; break;
        case 'S': params.add(new ParameterData("short", ac, null));   ac = 0; idx++; break;
        case 'I': params.add(new ParameterData("int", ac, null));     ac = 0; idx++; break;
        case 'F': params.add(new ParameterData("float", ac, null));   ac = 0; idx++; break;
        case 'J': params.add(new ParameterData("long", ac, null));    ac = 0; idx++; break;
        case 'D': params.add(new ParameterData("double", ac, null));  ac = 0; idx++; break;
        case 'L':
                  String clazz2 = parameters.substring(idx + 1, parameters.indexOf(';', idx));
                  String result = "";
                  st = new StringTokenizer(clazz2, "/");
                  while(st.hasMoreTokens()) {
                    result += st.nextToken() + ".";
                  }
                  result = result.substring(0, result.length() - 1);
                  params.add(new ParameterData(null, ac, addClass(result, (cld != null ? cld.id : -1))));
                  ac = 0;
                  idx = parameters.indexOf(';', idx) + 1;
                  break;
        case '[': ac++; idx++; break;
      }
    }

    md.pd = (ParameterData[])params.toArray(new ParameterData[0]);

    idx = 0;
    ac = 0;

    while(idx < retType.length()) {
      switch(retType.charAt(idx)) {
        case 'V': md.retType = new ParameterData("void", ac, null);    idx++; break;
        case 'Z': md.retType = new ParameterData("boolean", ac, null); idx++; break;
        case 'B': md.retType = new ParameterData("byte", ac, null);    idx++; break;
        case 'S': md.retType = new ParameterData("short", ac, null);   idx++; break;
        case 'C': md.retType = new ParameterData("char", ac, null);    idx++; break;
        case 'I': md.retType = new ParameterData("int", ac, null);     idx++; break;
        case 'F': md.retType = new ParameterData("float", ac, null);   idx++; break;
        case 'J': md.retType = new ParameterData("long", ac, null);    idx++; break;
        case 'D': md.retType = new ParameterData("double", ac, null);  idx++; break;
        case 'L':
                  String clazz2 = retType.substring(idx + 1, retType.indexOf(';', idx));
                  String result = "";
                  st = new StringTokenizer(clazz2, "/");
                  while(st.hasMoreTokens()) {
                    result += st.nextToken() + ".";
                  }
                  result = result.substring(0, result.length() - 1);
                  md.retType = new ParameterData(null, ac, addClass(result, (cld != null ? cld.id : -1)));
                  ac = 0;
                  idx = retType.indexOf(';', idx) + 1;
                  break;
        case '[': ac++; idx++; break;
      }
    }

    return md;
  }

  private void readLogFile() throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(logfile));
    String line;
    String clazz;
    MethodData md;

    while((line = br.readLine()) != null) {
      
      if(line.startsWith("TL")) {
        ClassLoaderData cld = new ClassLoaderData();
        cld.id = Integer.parseInt(line.substring(2,5));
        cld.name = line.substring(7, line.length());
        classloaders.put(new Integer(cld.id), cld);
        System.out.println("Classloader: " + cld.id + " - " + cld.name);
      }
    
      else if(line.startsWith("TD")) {
        if(line.charAt(5) == ':') {
          StringTokenizer st = new StringTokenizer(line, " |", false);
          
          String tmp = st.nextToken();
          ClassLoaderData cld = (ClassLoaderData)classloaders.get(new Integer(Integer.parseInt(tmp.substring(2,5)))); // Classloader
          
          int runs = Integer.parseInt(st.nextToken());
          long bytecodes = Long.parseLong(st.nextToken());
          st.nextToken(); // avg
          long acctime = Long.parseLong(st.nextToken());
          st.nextToken(); // avg
          long runtime = Long.parseLong(st.nextToken());
          st.nextToken(); // avg
          long time = Long.parseLong(st.nextToken());
          st.nextToken(); // avg
          long instances = Long.parseLong(st.nextToken());
          st.nextToken(); // avg
          char type = st.nextToken().charAt(0);
          String method = st.nextToken();
            
          md = parseMethod(method, cld);
          md.runs = runs;
          md.bytecodes = bytecodes;
          md.acctime = acctime;
          md.runtime = runtime;
          md.time = time;
          md.instances = instances;
          md.type = type;

        }
        else if(line.charAt(5) == 'c') {
          StringTokenizer st = new StringTokenizer(line, " |");
          
          st.nextToken();
          int count = Integer.parseInt(st.nextToken());
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          String method = st.nextToken();
          System.out.println(" " + count + " " + method);
          // MethodData md2 = parseMethod(method, null);

        }
        else if(line.charAt(5) == 'i') {
        }
      }
    
    }
  }

  private void htmlHeader(PrintWriter out) {
    out.println("<html>");
    out.println("<header>");
    out.println("</header>");
    out.println("<body>");
  }
  
  private void htmlFooter(PrintWriter out) {
    out.println("</body>");
    out.println("</html>");
  }

  private void outputClassLoaders() throws Exception {
    PrintWriter out = new PrintWriter(new FileWriter(new File(resultDir, "classloaders.html")));
    htmlHeader(out);
    out.println("<h1>ClassLoaders</h1>");
    out.println("<br/>");

    ClassLoaderData clds[] = (ClassLoaderData[])classloaders.values().toArray(new ClassLoaderData[0]);
    Arrays.sort(clds);
    for(int i=0; i<clds.length; i++) {
      out.println("" + clds[i].id + ". <a href=\"classloader_" + clds[i].id + "_"+ clds[i].name + ".html\">" + clds[i].name + "</a><br/>");
      outputClasses(clds[i]);
    }
    htmlFooter(out);
    out.close();
  }

  private void outputClasses(ClassLoaderData cld) throws Exception {
    PrintWriter out = new PrintWriter(new FileWriter(new File(resultDir, "classloader_" + cld.id + "_" + cld.name + ".html")));
    htmlHeader(out);

    out.println("<h1>" + cld.name + "</h1>");
    out.println("<br/>");

    ClassData cds[] = (ClassData[])cld.classes.values().toArray(new ClassData[0]);
    Arrays.sort(cds);
    for(int i=0; i<cds.length; i++) {
      out.println("<a href=\"class_" + cds[i].name + ".html\">" + cds[i].name + "</a><br/>");
      outputMethods(cds[i]);
    }
    
    htmlFooter(out);
    out.close();
  }

  private void outputMethods(ClassData cd) throws Exception {
    PrintWriter out = new PrintWriter(new FileWriter(new File(resultDir, "class_" + cd.name + ".html")));
    htmlHeader(out);
    
    out.println("<h1>" + cd.name + "</h1>");
    out.println("<br/>");

    out.println("<table>");

    MethodData mds[] = (MethodData[])cd.methods.values().toArray(new MethodData[0]);
    Arrays.sort(mds);
    for(int i=0; i<mds.length; i++) {
      out.println("<tr>");
      
      writeMethodHtml(out, mds[i], false, true);
      
      out.println("<tr>");
    }

    out.println("</table>");
    htmlFooter(out);
    out.close();
  }
  
  private void writeMethodHtml(PrintWriter out, MethodData md, boolean classname, boolean retType) throws Exception {

    if(retType) {
      out.println("<td align=\"right\">");

      if(md.retType.type != null) {
        out.print(md.retType.type);
      }
      else {
        out.print("<a href=\"class_" + md.retType.cd.name + ".html\">" + md.retType.cd.name + "</a>");
      }
      for(int j=0; j<md.retType.array; j++) {
        out.print("[]");
      }

      out.println("</td>");
    }


    out.print("<td>");
    if(classname) {
      out.print("<a href=\"class_" + md.cd.name + ".html\">" + md.cd.name + "</a>.");
    }
    
    out.print("<a href=\"method_" + md.fullname + ".html\">" + md.name + "</a>(");

    for(int j=0; j<md.pd.length; j++) {
      if(md.pd[j].type != null) {
        out.print(md.pd[j].type);
      }
      else {
        out.print("<a href=\"class_" + md.pd[j].cd.name + ".html\">" + md.pd[j].cd.name + "</a>");
      }
      for(int k=0; k<md.pd[j].array; k++) {
        out.print("[]");
      }
      if(j < md.pd.length - 1) {
        out.print(", ");
      }
    }

    out.print(")");
    out.println("</td>");
  }

  private void outputMethodTimings() throws Exception {
    Vector methods = new Vector();
    Iterator clds = classloaders.values().iterator();
    while(clds.hasNext()) {
      ClassLoaderData cld = (ClassLoaderData)clds.next();

      Iterator cds = cld.classes.values().iterator();
      while(cds.hasNext()) {
        ClassData cd = (ClassData)cds.next();
        methods.addAll(cd.methods.values());
      }
    }
    allMethods = (MethodData[])methods.toArray(new MethodData[0]);

    outputMethodTimingsDetail("Runs", "runs", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        return (aa.runs < bb.runs ? 1 : aa.runs == bb.runs ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Total Bytecodes", "bytecodes", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        return (aa.bytecodes < bb.bytecodes ? 1 : aa.bytecodes == bb.bytecodes ? 0 : -1); 
      } 
    });

    outputMethodTimingsDetail("Avg Bytecodes", "bytecodes_avg", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        if(aa.runs == 0) return 1;
        if(bb.runs == 0) return -1;
        return (aa.bytecodes / aa.runs < bb.bytecodes / bb.runs ? 1 : aa.bytecodes / aa.runs == bb.bytecodes / bb.runs ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Total Time", "ttime", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        return (aa.acctime < bb.acctime ? 1 : aa.acctime == bb.acctime ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Avg Total Time", "ttime_avg", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        if(aa.runs == 0) return 1;
        if(bb.runs == 0) return -1;
        return (aa.acctime / aa.runs < bb.acctime / bb.runs ? 1 : aa.acctime / aa.runs == bb.acctime / bb.runs ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Run Time", "rtime", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        return (aa.runtime < bb.runtime ? 1 : aa.runtime == bb.runtime ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Avg Run Time", "rtime_avg", new Comparator() {
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        if(aa.runs == 0) return 1;
        if(bb.runs == 0) return -1;
        return (aa.runtime / aa.runs < bb.runtime / bb.runs ? 1 : aa.runtime / aa.runs == bb.runtime / bb.runs ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Non-Acc Time", "natime", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        return (aa.time < bb.time ? 1 : aa.time == bb.time ? 0 : -1); 
      } 
    });
    
    outputMethodTimingsDetail("Avg Non-Acc Time", "natime_avg", new Comparator() { 
      public int compare(Object a, Object b) { 
        MethodData aa = (MethodData)a; MethodData bb = (MethodData)b; 
        if(aa.runs == 0) return 1;
        if(bb.runs == 0) return -1;
        return (aa.time / aa.runs < bb.time / bb.runs ? 1 : aa.time / aa.runs == bb.time / bb.runs ? 0 : -1); 
      } 
    });
  }
 
  private void outputMethodTimingsDetail(String type, String file, Comparator comp) throws Exception {
    PrintWriter out = new PrintWriter(new FileWriter(new File(resultDir, "methodtimings_" + file + ".html")));
    htmlHeader(out);
    
    out.println("<h1>" + type + "</h1>");
    out.println("<br/><table>");

    out.print("<tr>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_runs.html\">Runs</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_bytecodes.html\">Total Bytecodes</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_bytecodes_avg.html\">Avg</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_ttime.html\">Total Time</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_ttime_avg.html\">Avg</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_rtime.html\">Run Time</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_rtime_avg.html\">Avg</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_natime.html\">Non-Acc Time</a></b></td>");
    out.print("<td align=\"right\"><b><a href=\"methodtimings_natime_avg.html\">Avg</a></b></td>");
    out.print("<td align=\"center\"><b>Type</b></td>");
    out.print("<td align=\"left\"><b>Method</b></td></tr>");
    Arrays.sort(allMethods, comp);

    for(int i=0; i<allMethods.length; i++) {
      if(allMethods[i].runs != 0) {
        out.print("<tr>");
        out.print("<td align=\"right\">" + allMethods[i].runs + "</td>");
        out.print("<td align=\"right\">" + allMethods[i].bytecodes + "</td>");
        out.print("<td align=\"right\">" + (allMethods[i].bytecodes / allMethods[i].runs) + "</td>");
        out.print("<td align=\"right\">" + allMethods[i].acctime + "</td>");
        out.print("<td align=\"right\">" + (allMethods[i].acctime / allMethods[i].runs) + "</td>");
        out.print("<td align=\"right\">" + allMethods[i].runtime + "</td>");
        out.print("<td align=\"right\">" + (allMethods[i].runtime / allMethods[i].runs) + "</td>");
        out.print("<td align=\"right\">" + allMethods[i].time + "</td>");
        out.print("<td align=\"right\">" + (allMethods[i].time / allMethods[i].runs) + "</td>");
        out.print("<td align=\"center\">" + allMethods[i].type + "</td>");

        writeMethodHtml(out, allMethods[i], true, false);
        out.println("</tr>");
      }
    }

    out.println("</table>");
    htmlFooter(out);
    out.close();
  }
  
  public static void main(String args[]) throws Exception {
    if(args.length != 2) {
      System.out.println("Use: Profile <logfile> <resultdir>");
      System.exit(1);
    }
    Profile p = new Profile(args[0], args[1]);
  }
  
}
