/*
 * Tools.java
 *
 * Created on 2007/1/3 10:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.sais.kulisse;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Romulus
 * @last edited by Julius on 2008/12/14
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
                String lineprefix = "";
                if (line.contains("=")) {
                    lineprefix = line.substring(0, line.indexOf("=")).trim().toLowerCase();
                }
                if (lineprefix.equals(prefix)) {
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
                new FileOutputStream(configfile).write(0);
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "Unicode"));
            line = fr.readLine();
            while (line != null) {
                if (line.contains("=")) {
                    String lineprefix = line.substring(0, line.indexOf("=")).trim().toLowerCase();
                    if (lineprefix.equals(prefix)) {
                        fr.close();
                        String sub = line.substring(prefix.length());
                        return sub.substring(sub.indexOf("=") + 1).trim();
                    }
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

    public static String getNowTimeString(String format) {
        SimpleDateFormat D = new SimpleDateFormat(format);
        Date Now = new Date();
        long Time = Now.getTime();
        Time = Time + 100000L;
        return D.format(new Date(Time));
    }
}