/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolatsnote.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 *
 * @author Romulus
 */
public class Tools {

    public static void updateConfig(String prefix, String content) {  //Remember to use all lower case string in prefix
        try {
            if (content == null) {
                return;
            }
            String line;
            File org = new File("config.ini");
            File rpc = new File("configtemp.ini");
            if (!org.exists()) {
                if (rpc.exists()) {
                    rpc.renameTo(org);
                } else {
                    org.createNewFile();
                }
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(org), "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rpc), "UTF-8"));
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
            File tmp = new File("config.org.ini");
            if (tmp.exists()) {
                tmp.delete();
            }
            if (org.renameTo(tmp)) {
                if (rpc.renameTo(org)) {
                    tmp.delete();
                } else {
                    tmp.renameTo(org);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *
     * @param prefix
     * @return empty string if not exist.
     */
    public static String readConfig(String prefix) {  //Remember to use all lower case string in prefix
        try {
            String line;
            File configfile = new File("config.ini");
            File tempfile = new File("configtemp.ini");
            if (!configfile.exists()) {
                if (tempfile.exists()) {
                    tempfile.renameTo(configfile);
                } else {
                    configfile.createNewFile();
                }
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "UTF-8"));
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
            return "";
        }
    }

    public static ArrayList<String> readListConfig(String prefix) {  //Remember to use all lower case string in prefix
        ArrayList<String> ret = new ArrayList<String>();
        try {
            String line;
            File configfile = new File("config.ini");
            File tempfile = new File("configtemp.ini");
            if (!configfile.exists()) {
                if (tempfile.exists()) {
                    tempfile.renameTo(configfile);
                } else {
                    configfile.createNewFile();
                }
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "UTF-8"));
            line = fr.readLine();
            while (line != null) {
                if (line.contains("=")) {
                    String lineprefix = line.substring(0, line.indexOf("=")).trim().toLowerCase();
                    if (lineprefix.equals(prefix)) {
                        String sub = line.substring(prefix.length());
                        ret.add(sub.substring(sub.indexOf("=") + 1).trim());
                    }
                }
                line = fr.readLine();
            }
            fr.close();
            return ret;
        } catch (Exception ex) {
            return ret;
        }
    }

    public static boolean readBooleanConfig(String prefix, boolean defaultValue) {
        String conf = readConfig(prefix);
        if (conf.length() == 0) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(conf);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
