/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import org.sais.fantasyfesta.autos.AutoMech;
import org.sais.fantasyfesta.autos.AutoMech.Limit;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.autos.AutoMech.Turn_Type;
import org.sais.fantasyfesta.core.ReadInnerFile;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class CardDatabase {

    private static HashMap<Integer, CardInfo> sCardData = new HashMap<Integer, CardInfo>();
    private static HashMap<Integer, String> sPronuciation = new HashMap<Integer, String>();
    private static Integer sCacheVersion = 0;

    static {
        readPronunciation();
    }

    static public CardInfo getInfo(int cardNo) {
        CardInfo info = sCardData.get(cardNo);
        return info == null ? CardInfo.newNull() : info;
    }

    static public CharacterCardInfo getCharacterInfo(int cardNo) {
        CardInfo info = sCardData.get(cardNo);
        return info == null ? CharacterCardInfo.newNull() : (CharacterCardInfo) sCardData.get(cardNo);
    }

    static public SpellCardInfo getSpellInfo(int cardNo) {
        CardInfo info = sCardData.get(cardNo);
        return info == null ? SpellCardInfo.newNull() : (SpellCardInfo) sCardData.get(cardNo);
    }

    static public SupportCardInfo getSupportInfo(int cardNo) {
        CardInfo info = sCardData.get(cardNo);
        return info == null ? SupportCardInfo.newNull() : (SupportCardInfo) sCardData.get(cardNo);
    }

    static public EventCardInfo getEventInfo(int cardNo) {
        CardInfo info = sCardData.get(cardNo);
        return info == null ? EventCardInfo.newNull() : (EventCardInfo) sCardData.get(cardNo);
    }

    static public CardInfo getInfo(String cardName) {
        for (CardInfo info : sCardData.values()) {
            if (info.getName().equals(cardName)) {
                return info;
            }
        }
        return null;
    }

    static public Collection<CardInfo> listCards() {
        ArrayList<CardInfo> ret = new ArrayList<CardInfo>(sCardData.values());
        Collections.sort(ret, new Comparator<CardInfo>() {

            @Override
            public int compare(CardInfo o1, CardInfo o2) {
                return o1.mNo - o2.mNo;
            }
        });
        return ret;
    }

    public static void readCardBase() throws FileNotFoundException, IOException {      //Read Card Data Base
        // Disable originalcard
        /*if (FTool.readBooleanConfig("originalcard", false)) {
            readOriginalCard();
        }*/

        InputStreamReader cardlistfile = null;
        //System.out.println("Read Carddatabase...");
        Locale l = Locale.getDefault();
        String language = FTool.readConfig("carddatalanguage");
        String country = FTool.readConfig("carddatacountry");
        if (!(language.equals("") || country.equals(""))) {
            l = new Locale(language, country);
        } else {
            language = FTool.readConfig("language");
            country = FTool.readConfig("country");
            if (!(language.equals("") || country.equals(""))) {
                l = new Locale(language, country);
            }
        }
        String cdname = java.util.ResourceBundle.getBundle("Global", l).getString("carddatabase.txt");
        File file = new File("locale\\" + cdname);
        // System.out.println(file.getAbsolutePath());
        if (file.exists()) {
            cardlistfile = new InputStreamReader(new FileInputStream(file), "Unicode");
        } else if (new ReadInnerFile(cdname).stream != null) {
            cardlistfile = new ReadInnerFile(cdname).stream;
        } else {
            cardlistfile = new ReadInnerFile("carddatabase.txt").stream;
        }

        execReadCardDataBase(new BufferedReader(cardlistfile), cdname);
    }

    private static void execReadCardDataBase(BufferedReader dataFile, String filename) {
        CardInfo info = null;
        AutoMech mech = null;
        ChoiceEffect eff = null;

        char[] buf = new char[1];
        String pp = new String();  //Buffer String for parse
        int step = 0;
        int lastcardnum = -1;
        int cardNo = -1;
        String cardName = "Error";

        final int STEPBASE_AUTOMECH = 100;
        final int STEPBASE_CHOICE_EFFECT = 1000;

        try {
            // Version of database, for caching
            int fileVersion = Integer.parseInt(dataFile.readLine());
            int cacheVersion;
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("cardCache_" + filename));
                cacheVersion = (Integer) in.readObject();
                in.close();
            } catch (FileNotFoundException ex) {
                cacheVersion = -1;
            }

            if (fileVersion <= cacheVersion && fileVersion > 0) {
                // Use cache is fine
                loadInfoCache(filename);
                return;
            }

            // Cache does not exists or old
            sCacheVersion = fileVersion;

            while (dataFile.read(buf, 0, 1) > 0) {  //Read database
                switch (buf[0]) {  //parse txtfile into objects
                    case '\n':
                        pp = pp.substring(0, pp.length() - 1);  //deletee tail \r
                        switch (step) {
                            case 0:
                                cardNo = FTool.safeParseInt(pp);
                                pp = "";
                                step++;
                                break;
                            case 1:
                                cardName = pp;
                                pp = "";
                                step++;
                                break;
                            case 2:
                                int type = FTool.safeParseInt(pp);
                                info = CardInfo.createCardInfo(cardNo, cardName, type);
                                pp = "";
                                step++;
                                break;
                            case 3:
                                info.setFifthLine(pp);
                                pp = "";
                                step++;
                                break;
                            case 4:
                                info.setCharacterRequirement(pp);
                                pp = "";
                                step++;
                                break;
                            case 5:
                                info.setSPRequirement(pp);
                                pp = "";
                                step++;
                                break;
                            case 6:
                                // All are card rule text
                                if (pp != null) {
                                    info.addRuleText(pp + "\r\n");
                                }
                                pp = "";
                                lastcardnum = cardNo;
                                break;
                            case STEPBASE_AUTOMECH + 0:
                                mech = new AutoMech(info);
                                mech.effect_index = FTool.safeParseInt(pp);
                                pp = "";
                                step++;
                                break;
                            case STEPBASE_AUTOMECH + 1:
                                String[] s = pp.split("/");
                                mech.timing = Timing.valueOf(s[0]);
                                mech.turn = Turn_Type.valueOf(s[1]);
                                if (s[2].length() == 1) {
                                    mech.target_player = EPlayer.values()[FTool.safeParseInt(s[2])];
                                } else {
                                    mech.target_player = EPlayer.valueOf(s[2]);
                                }
                                for (int i = 3; i < s.length - 1; i += 2) {
                                    mech.addEffect(s[i], FTool.safeParseInt(s[i + 1]));
                                }
                                pp = "";
                                step++;
                                break;
                            case STEPBASE_AUTOMECH + 2:
                                s = pp.split(" ");
                                if (s.length > 0) {
                                    mech.targets = new ArrayList<AutoMech.Target_Type>();
                                }
                                for (String target : s) {
                                    if (target.length() > 0) {
                                        mech.targets.add(AutoMech.Target_Type.valueOf(target));
                                    }
                                }
                                pp = "";
                                step++;
                                break;
                            case STEPBASE_AUTOMECH + 3:
                                s = pp.split(" ");
                                if (s.length > 0) {
                                    mech.limits = new ArrayList<AutoMech.Limit>();
                                }
                                for (String target : s) {
                                    if (target.length() > 0) {
                                        String[] s2 = target.split("/");
                                        switch (s2.length) {
                                            case 1:
                                                mech.limits.add(new Limit(AutoMech.Limit_Type.valueOf(s2[0]), "", ""));
                                                break;
                                            case 2:
                                                mech.limits.add(new Limit(AutoMech.Limit_Type.valueOf(s2[0]), s2[1], ""));
                                                break;
                                            case 3:
                                                mech.limits.add(new Limit(AutoMech.Limit_Type.valueOf(s2[0]), s2[1], s2[2]));
                                                break;
                                        }
                                    }
                                }
                                pp = "";
                                step++;
                                // End of automech parsing
                                info.addAutoMech(mech);
                                break;
                            case STEPBASE_CHOICE_EFFECT:
                                eff = new ChoiceEffect();
                                eff.setEffectIndex(FTool.safeParseInt(pp));
                                pp = "";
                                step++;
                                break;
                            case STEPBASE_CHOICE_EFFECT + 1:
                                eff.setMenuText(pp);
                                pp = "";
                                step++;
                                // End of choice effects parsing
                                info.addChoiceEffect(eff);
                                break;
                        }
                        break;
                    case '#':
                        while (dataFile.read(buf, 0, 1) > 0) {
                            if (buf[0] == '\n') {
                                break;
                            }
                        }
                        step = 0;
                        pp = "";
                        sCardData.put(info.getCardNo(), info);
                        break;
                    case '~':
                        boolean auto_in_text = true;
                        while (dataFile.read(buf, 0, 1) > 0) {
                            if (buf[0] == '\n') {
                                break;
                            }
                            if (buf[0] == 'c') {
                                auto_in_text = false;
                            }
                        }
                        pp = "";
                        if (auto_in_text) {
                            step = STEPBASE_AUTOMECH;
                        }
                        break;
                    case '*':
                        while (dataFile.read(buf, 0, 1) > 0) {
                            if (buf[0] == '\n') {
                                break;
                            }
                        }
                        pp = "";
                        step = STEPBASE_CHOICE_EFFECT;
                        break;
                    default:
                        pp = pp + buf[0];
                        break;
                }
            }
            saveInfoCache(filename);
        } catch (Exception ex) {
            System.err.println("step=" + step + ", lastcarnum=" + lastcardnum);
            System.err.println("pp=" + pp);
            ex.printStackTrace();
        }
    }

    private static void saveInfoCache(String filename) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("cardCache_" + filename));
            out.writeObject(sCacheVersion);
            for (CardInfo info : sCardData.values()) {
                out.writeObject(info);
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void loadInfoCache(String filename) {
        try {
            sCardData.clear();
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("cardCache_" + filename));
            sCacheVersion = (Integer) in.readObject();
            try {
                for (CardInfo info = (CardInfo) in.readObject(); in != null; info = (CardInfo) in.readObject()) {
                    sCardData.put(info.getCardNo(), info);
                }
            } catch (EOFException e) {
            }
            in.close();
        } catch (FileNotFoundException ex) {
            // No cache yet
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void readOriginalCard() {
        InputStreamReader cardlistfile = null;

        File file = new File("originalcarddatabase.txt");
        if (file.exists()) {
            // Check if too old
            Calendar renew = Calendar.getInstance();
            renew.set(2011, 6, 30);
            if (renew.before(file.lastModified())) {
                try {
                    cardlistfile = new InputStreamReader(new FileInputStream(file), "Unicode");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            } else {
                file.delete();
            }
        } else {
            cardlistfile = new ReadInnerFile("originalcarddatabase.txt").stream;
        }

        execReadCardDataBase(new BufferedReader(cardlistfile), "originalcarddatabase.txt");
    }

    public static void readPronunciation() {
        try {
            sPronuciation.clear();
            BufferedReader in = new BufferedReader(new ReadInnerFile("pronunciation.txt").stream);
            String line = in.readLine();
            while (line != null) {
                String[] s = line.split("<>");
                if (s.length < 2) {
                    break;
                }
                sPronuciation.put(FTool.safeParseInt(s[0]), s[1]);
                line = in.readLine();
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getPronunciation(Integer cardNo) {
        return sPronuciation.get(cardNo) != null ? sPronuciation.get(cardNo) : "";
    }

    public static String getCardName(int cardNo) {
        return getInfo(cardNo).getName();
    }
}
