/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.SupportCardInfo;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.ESupportType;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class BackgroundManager {

    private static HashMap<String, String> mImagePaths = new HashMap<String, String>();
    private static HashMap<String, String> mMusicPaths = new HashMap<String, String>();

    public static ArrayList<String> listScene() {
        ArrayList<String> ret = new ArrayList<String>();
        ret.add(java.util.ResourceBundle.getBundle("Global").getString("defaultscene.name"));
        for (CardInfo info : CardDatabase.listCards()) {
            if (!info.isCardType(ECardType.SUPPORT)) {
                continue;
            }
            if (((SupportCardInfo) info).getSupportType() != ESupportType.SCENE) {
                continue;
            }
            ret.add(info.getName());
        }
        return ret;
    }

    public static void loadImagePaths() {
        try {
            File configfile = new File("background.txt");
            if (!configfile.exists()) {
                configfile.createNewFile();
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "Unicode"));
            for (String line = fr.readLine(); line != null; line = fr.readLine()) {
                String name = line;
                line = fr.readLine();
                String path = line;
                if (path.length() > 0) {
                    mImagePaths.put(name, path);
                }
            }
            fr.close();
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Invalid background.txt");
        }
    }

    public static void writeImagePaths() {
        try {
            File configfile = new File("background.txt");
            if (!configfile.exists()) {
                new FileOutputStream(configfile).write(0);
            }
            BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configfile), "Unicode"));
            for (Entry<String, String> e : mImagePaths.entrySet()) {
                fw.write(e.getKey());
                fw.newLine();
                fw.write(e.getValue());
                fw.newLine();
            }
            fw.close();
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setImagePath(String name, String path) {
        mImagePaths.put(name, path);
    }

    public static String getImagePath(String name) {
        String ret = mImagePaths.get(name);
        return ret == null ? "" : ret;
    }

    public static void loadMusicPaths() {
        try {
            File configfile = new File("SceneBGM.txt");
            if (!configfile.exists()) {
                configfile.createNewFile();
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(configfile), "Unicode"));
            for (String line = fr.readLine(); line != null; line = fr.readLine()) {
                String name = line;
                line = fr.readLine();
                String path = line;
                if (path.length() > 0) {
                    mMusicPaths.put(name, path);
                }
            }
            fr.close();
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Invalid SceneBGM.txt");
        }
    }

    public static void writeMusicPaths() {
        try {
            File configfile = new File("SceneBGM.txt");
            if (!configfile.exists()) {
                new FileOutputStream(configfile).write(0);
            }
            BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configfile), "Unicode"));
            for (Entry<String, String> e : mMusicPaths.entrySet()) {
                fw.write(e.getKey());
                fw.newLine();
                fw.write(e.getValue());
                fw.newLine();
            }
            fw.close();
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getMusicPath(String name) {
        String ret = mMusicPaths.get(name);
        return ret == null ? "" : ret;
    }

    public static void setMusicPath(String name, String path) {
        mMusicPaths.put(name, path);
    }
}
