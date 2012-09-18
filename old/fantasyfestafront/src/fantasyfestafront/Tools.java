/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fantasyfestafront;


import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class Tools {

      public static void updateConfig(String prefix, String content) {  //Remember to use all lower case string in prefix
    try {
      if (content == null) {
        return;
      }
      String line;
      BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("config.ini")), "Unicode"));
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("configtemp.ini")), "UnicodeLittle"));
      boolean found = false;
      line = fr.readLine();
      while (line != null) {
        if (line.toLowerCase().startsWith(prefix)) {
          bw.write(prefix + " = " + content + "\r\n");
          found = true;
        } else {
          if (line.endsWith("\n")) {
            bw.write(line);
          } else {
            bw.write(line + "\r\n");
          }
        }
        line = fr.readLine();
      }
      if (!found) {
        bw.write(prefix + " = " + content + "\r\n");
      }
      fr.close();
      bw.close();
      new File("config.ini").delete();
      new File("configtemp.ini").renameTo(new File("config.ini"));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static String readConfig(String prefix) {  //Remember to use all lower case string in prefix
    try {
      String line;
      File configfile = new File("config.ini");
      if (!configfile.exists()) {
        configfile.createNewFile();
      }
      BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "Unicode"));
      line = fr.readLine();
      while (line != null) {
        if (line.toLowerCase().startsWith(prefix)) {
          fr.close();
          String sub = line.substring(prefix.length());
          return sub.substring(sub.indexOf("=") + 1).trim();
        }
        line = fr.readLine();
      }
      fr.close();
      return "";
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static void loadBGM(ArrayList<String> sceneName,ArrayList<String> BGMPath)
  {


      try {
      String line;
      File configfile = new File("SceneBGM.txt");
      if (!configfile.exists()) {
        configfile.createNewFile();
      }
      BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "Unicode"));
      line = fr.readLine();
      while (line != null) {
        sceneName.add(line);
        line = fr.readLine();
        BGMPath.add(line);
        line = fr.readLine();
      }
      fr.close();

    } catch (Exception ex) {
      ex.printStackTrace();
      
    }
  }

    public static void updateBGM(ArrayList<String> sceneName,ArrayList<String> BGMPath)
  {


      try {
      String line;
      File configfile = new File("SceneBGM.txt");
      if (!configfile.exists()) {
        new FileOutputStream(configfile).write(0);
      }
      BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configfile), "Unicode"));
      for(int i = 0;i<sceneName.size();i++)
      {
          fw.write(sceneName.get(i));
          fw.newLine();
          if(BGMPath.get(i)!=null)
          {
               fw.write(BGMPath.get(i));
          }
          else
          {
              fw.write("");
          }

          fw.newLine();
      }
      fw.close();

    } catch (Exception ex) {
      ex.printStackTrace();

    }
  }




}
