/*
 * Tools.java
 *
 * Created on 2007/1/3 10:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.tool;

import org.sais.fantasyfesta.card.CardDatabase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.ui.CardViewer;
import org.sais.fantasyfesta.core.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.*;
import org.sais.fantasyfesta.card.CardInfo;
import sun.audio.AudioPlayer;

/**
 *
 * @author Romulus
 * @last edited by Julius on 2008/12/14
 */
public class FTool {

    private static String[] loc = new String[1000];
    public static CardViewer cardviewer;
    public static CardViewer deckeditorcardviewer;
    public static Object sLock = new Object();
    public static boolean mute = false;

    static public String getLocale(int index) {
        return loc[index];
    }

    /**
     * Get the character index. Reimu=0.
     * @param charCardNum The card number of character card.
     * @return The character index.
     */
    static public int getCharIndex(int charCardNum) {
        int ret = charCardNum / 100;
        if (ret == 80) {
            return 16;
        }
        if (ret == 81) {
            return 18;
        }
        return ret - 1;
    }

    public static byte[] itoba(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff), (byte) (value & 0xff)};
    }

    public static int batoi(byte[] value) {
        return (value[0] & 0xff) * 0x1000000 + (value[1] & 0xff) * 0x10000 + (value[2] & 0xff) * 0x100 + (value[3] & 0xff);
    }

    static {
        try {
            char[] buf = new char[1];
            String bs = "";
            int loccnt = 0;
            InputStreamReader localefile = null;
						Locale l = Locale.getDefault();
						String language = FTool.readConfig("carddatalanguage");
						String country = FTool.readConfig("carddatacountry");
						if (!(language.equals("") || country.equals("")))
						{
							l = new Locale(language, country);
						}
						else
						{
							language = FTool.readConfig("language");
							country = FTool.readConfig("country");
							if (!(language.equals("") || country.equals("")))
							{
                l = new Locale(language, country);
							}
						}
						String localname = java.util.ResourceBundle.getBundle("Global",l).getString("locale.txt");
            File file = new File("locale\\" + localname);
            if (file.exists()) {
                localefile = new InputStreamReader(new FileInputStream(file), "Unicode");
            } else if (new ReadInnerFile(localname).stream != null){
								localefile = new ReadInnerFile(localname).stream;
						}	else {
                localefile = new ReadInnerFile("locale.txt").stream;
            }
            while (localefile.read(buf, 0, 1) > 0) {
                bs += buf[0];
                if (buf[0] == '\n') {
                    loc[loccnt] = bs.substring(0, bs.length() - 2);
                    bs = "";
                    ++loccnt;
                }
            }
            localefile.close();
        } catch (IOException ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void showCard(CardInfo info, JTextPane SCArea) {
        try {
            SCArea.setText("");
            if (info == null) {
                SCArea.setBackground(Color.white);
                return;
            }
            String dp = "";
            SimpleAttributeSet set = new SimpleAttributeSet();
            SCArea.setCharacterAttributes(set, true);
            Document doc = SCArea.getStyledDocument();

            String pronunciation = CardDatabase.getPronunciation(info.getCardNo());
            doc.insertString(doc.getLength(), pronunciation + "\n", set);

            StyleConstants.setFontSize(set, 16);
            StyleConstants.setBold(set, true);
            doc.insertString(doc.getLength(), info.getName() + "\n", set);
            StyleConstants.setFontSize(set, 12);
            StyleConstants.setBold(set, false);
            doc.insertString(doc.getLength(), info.dump(loc) + "\n\n", set);
            SCArea.setBackground(info.getBackgroundColor());

            String source = info.getRuleText();
            for (int idx = 0; idx < source.length(); ++idx) {
                switch (source.charAt(idx)) {
                    case '$':
                        doc.insertString(doc.getLength(), dp, set);
                        dp = "";
                        StyleConstants.setForeground(set, Color.red);
                        break;
                    case '%':
                        doc.insertString(doc.getLength(), dp, set);
                        dp = "";
                        StyleConstants.setForeground(set, new Color(0x209020));
                        break;
                    case '^':
                        doc.insertString(doc.getLength(), dp, set);
                        dp = "";
                        StyleConstants.setForeground(set, Color.blue);
                        break;
                    case '@':
                        doc.insertString(doc.getLength(), dp, set);
                        dp = "";
                        StyleConstants.setForeground(set, Color.magenta);
                        break;
                    case '*':
                        doc.insertString(doc.getLength(), dp, set);
                        dp = "";
                        StyleConstants.setForeground(set, new Color(0x902000));
                        break;
                    case '&':
                        doc.insertString(doc.getLength(), dp, set);
                        dp = "";
                        StyleConstants.setForeground(set, Color.black);
                        break;
                    case '\\':
                        idx++;
                        dp = dp + source.charAt(idx);
                        break;
                    default:
                        dp = dp + source.charAt(idx);
                }
            }
            doc.insertString(doc.getLength(), dp + "\n", set);

            if (info.getCardNo() == 8000) {
                doc.insertString(doc.getLength(), "#1700/1\n", set);
            } else if (info.getCardNo() == 8001) {
                doc.insertString(doc.getLength(), "#1700/2\n", set);
            } else if (info.getCardNo() == 8002) {
                doc.insertString(doc.getLength(), "#1700/3\n", set);
            } else if (info.getCardNo() == 8100) {
                doc.insertString(doc.getLength(), "#1900/1\n", set);
            } else if (info.getCardNo() == 8101) {
                doc.insertString(doc.getLength(), "#1900/2\n", set);
            } else if (info.getCardNo() > 0) {
                doc.insertString(doc.getLength(), "No." + info.getCardNo() + "\n", set);
            }

            SCArea.setCaretPosition(0);
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String[] getLatest() {
        URL ver;
        try {
            ver = new URL("http://dl.dropbox.com/u/5736424/FFO/version.txt");
            InputStreamReader isr = new InputStreamReader(ver.openStream());
            char[] buff = new char[30];
            int cnt = isr.read(buff);
            String latest = "";
            for (int i = 0; i < cnt; ++i) {    //Be sure to put newline on version.txt
                latest += buff[i];
            }
            latest = latest.trim();
            String[] rtn = latest.split(" ");
            return rtn;
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String getKLatest() {
        URL ver;
        try {
            ver = new URL("http://dl.dropbox.com/u/5736424/kver.txt");
            InputStreamReader isr = new InputStreamReader(ver.openStream());
            char[] buff = new char[30];
            int cnt = isr.read(buff);
            String latest = "";
            for (int i = 0; i < cnt; ++i) {    //Be sure to put newline on kver.txt
                latest += buff[i];
            }
            latest = latest.trim();
            return latest;
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    synchronized public static void updateConfig(String prefix, int content) {  //Remember to use all lower case string in prefix
        updateConfig(prefix, String.valueOf(content));
    }

    synchronized public static void updateConfig(String prefix, String content) {  //Remember to use all lower case string in prefix
        File org = new File("config.ini");
        File rpc = new File("config.temp." + System.currentTimeMillis() + ".ini");

        try {
            if (content == null) {
                return;
            }
            String line;
            if (!org.exists()) {
                if (rpc.exists()) {
                    rpc.renameTo(org);
                } else {
                    org.createNewFile();
                }
            }
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(org), "Unicode"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rpc), "Unicode"));
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
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        File tmp = new File("config.org." + System.currentTimeMillis() + ".ini");
        if (org.renameTo(tmp)) {
            if (rpc.renameTo(org)) {
                tmp.delete();
            } else {
                tmp.renameTo(org);
            }
        }
    }

    /**
     *
     * @param prefix
     * @return empty string if not exist.
     */
    synchronized public static String readConfig(String prefix) {  //Remember to use all lower case string in prefix
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
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
            return "Error";
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

    public static int readIntegerConfig(String prefix, int defaultValue) {
        String conf = readConfig(prefix);
        if (conf.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(conf);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void playSound(String filename) {
        if (mute) {
            return;
        }

        try {
            BufferedInputStream in = new BufferedInputStream(new ReadInnerFile(filename).u.openStream());
            AudioInputStream stream = AudioSystem.getAudioInputStream(in);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            final Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);

            clip.addLineListener(new LineListener() {

                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.stop();
                        clip.close();
                    }
                }
            });

            clip.start();
        } catch (Exception ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getNowTimeString(String format) {
        SimpleDateFormat D = new SimpleDateFormat(format);
        Date Now = new Date();
        long Time = Now.getTime();
        Time = Time + 100000L;
        return D.format(new Date(Time));
    }

    public static boolean XOR(boolean a, boolean b) {
        if ((a && b) || (!a && !b)) {
            return false;
        } else {
            return true;
        }
    }

    public static int safeParseInt(String s) {
        s = s.replace(" ", "");
        if (s.length() == 0) {
            return 0;
        }
        return Integer.parseInt(s);
/*        try {
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }*/
    }
    static long[] timer = new long[2];
    static int nowtimer = 0;

    static {
        timer[0] = 0;
        timer[1] = 0;
    }

    static void startTimer() {
        timer[nowtimer] = new Date().getTime();
    }

    static void switchTimer() {
        long interval = new Date().getTime() - timer[nowtimer];
        System.out.println("Interval is " + interval + " miliseconds.");
        nowtimer = nowtimer == 0 ? 1 : 0;
        timer[nowtimer] = new Date().getTime();
    }
    static long intervaltimer;

    public static long interval() {
        long now = new Date().getTime();
        long ret = now - intervaltimer;
        intervaltimer = now;
        return ret;
    }

    public static String parseLocale(int index, Object... args) {
        String ret = loc[index];
        for (int i = args.length; i > 0; --i) {
            ret = ret.replace("%" + i, args[i - 1].toString());
        }
        return ret;
    }

    public static void setMyFont(Component Comp, Font myFont) {
        //  Font myFont = new Font(java.util.ResourceBundle.getBundle("fantasyfestafront/Bundle").getString("FrontFrame.default.font"), Font.PLAIN, 12);
        if (Comp != null) {
            try {
                Comp.setFont(myFont);
            } catch (Exception e) {
                return;
            }
        }
        if (Comp instanceof Container) {
            Component[] components = ((Container) Comp).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component child = components[i];
                if (child != null) {
                    //System.out.println(child.getClass().getName());
                    setMyFont(child, myFont);
                }
            }
        }
        return;
    }

    public static void setLocale() {

        String language = FTool.readConfig("language");
        String country = FTool.readConfig("country");

        if (!language.equals("")) {
            Locale locale;
            if (!country.equals("")) {
                locale = new Locale(language, country);
            } else {
                locale = new Locale(language);
            }
            Locale.setDefault(locale);
            System.out.println("Locale: " + language + country + locale);
        } else {
            Locale.setDefault(Locale.JAPAN);
        }


    }

    public static boolean isURLAvailable(String url) {
        boolean result = false;
        int count = 0;
        int state = -1;

        try {

            if (url == null || url.length() < 0) {
                result = false;
            }
            while (count < 5) {
                URL urlStr = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlStr.openConnection();
                state = connection.getResponseCode();
                if (state == 200) {
                    result = true;
                    break;
                } else {
                    count++;
                }
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FTool.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static void setMyOpaque(JComponent con) {
        for (Component comp : con.getComponents()) {
            if (comp instanceof JComponent) {
                if (comp instanceof JScrollPane) {
                    ((JScrollPane) comp).getViewport().setOpaque(false);
                    ((JScrollPane) comp).getHorizontalScrollBar().setOpaque(false);
                    ((JScrollPane) comp).getVerticalScrollBar().setOpaque(false);
                }
                if (!(comp instanceof JTextComponent)) {
                    ((JComponent) comp).setOpaque(false);

                }
                //  ((JComponent)comp).setOpaque(false);
                setMyOpaque((JComponent) comp);
            }
        }
    }

    public static EPlayer rev(EPlayer ePlayer) {
        return ePlayer == EPlayer.ICH ? EPlayer.OPP : EPlayer.ICH;
    }
}

interface CardGameDisplayInterface {

    abstract void ShowTextinJTextPane();
}
