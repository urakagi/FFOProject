/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.chocolat.core.GameResult;
import org.sais.chocolat.core.Participant;
import org.sais.chocolat.core.Round;
import org.sais.chocolat.core.Table;
import org.sais.chocolat.core.Tournament;

/**
 *
 * @author Romulus
 */
public class XMLUtils {

    public final static String XML_TOURNAMENT = "Tournament";
    public final static String XML_NAME = "name";
    public final static String XML_PROMOTER = "Promoter";
    public final static String XML_DATE = "date";
    public final static String XML_PARTICIPANT = "Participant";
    public final static String XML_ID = "id";
    public final static String XML_ISDROPPED = "isDropped";
    public final static String XML_ISHOSTABLE = "isHostable";
    public final static String XML_IP = "IP";
    public final static String XML_DECK = "Deck";
    public final static String XML_ROUND = "Round";
    public final static String XML_TABLE = "Table";
    public final static String XML_PLAYER = "Player";
    public final static String XML_WINNER = "Winner";
    public final static String XML_BYE = "*BYE*";
    public final static String XML_DRAW = "*DRAW*";
    public final static String XML_GAME_RESULT = "GameResult";

    private static long sFileSize;

    public static Tournament parse(File xmlfile) {
        BufferedReader in = null;
        sFileSize = xmlfile.length();
        try {
            Tournament to;
            in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlfile), "UTF-8"), 8192);
            String s_tour = XMLUtils.readTag(in, XMLUtils.XML_TOURNAMENT);
            to = new Tournament();

            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s_tour.getBytes("UTF-8")), "UTF-8"), 8192);
            to.name = XMLUtils.readTag(in, XMLUtils.XML_NAME);
            to.promoter = XMLUtils.readTag(in, XML_PROMOTER);
            try {
                to.date = new Date(Long.parseLong(readTag(in, XML_DATE)));
            } catch (NumberFormatException ex) {
                to.date = new Date(0);
            }

            to.participant = new HashMap<Integer, Participant>();
            ArrayList<String> s_par = new ArrayList<String>();
            String tmp = XMLUtils.readTag(in, XMLUtils.XML_PARTICIPANT);
            while (tmp.length() > 0) {
                s_par.add(tmp);
                tmp = XMLUtils.readTag(in, XMLUtils.XML_PARTICIPANT);
            }

            for (String s : s_par) {
                BufferedReader local = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes("UTF-8")), "UTF-8"), 8192);
/*                int a = local.read();
                while (a > 0) {
                    System.out.print(Integer.toHexString(a) + " ");
                    a = local.read();
                }*/
                int id = Integer.parseInt(XMLUtils.readTag(local, XMLUtils.XML_ID));
                String name = XMLUtils.readTag(local, XMLUtils.XML_NAME);
                String deck = XMLUtils.readTag(local, XMLUtils.XML_DECK);
                boolean isHostable = Boolean.parseBoolean(XMLUtils.readTag(local, XMLUtils.XML_ISHOSTABLE));
                String ip = XMLUtils.readTag(local, XMLUtils.XML_IP);

                Participant p = new Participant();
                p.id = id;
                name = fixName(name);
                p.name = name;
                p.deck = deck;
                p.isHostable = isHostable;
                if (ip.length() > 0) {
                    p.ip = Participant.unencrypt(ip);
                }

                to.participant.put(id, p);
            }

            to.round = new ArrayList<Round>();
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s_tour.getBytes("UTF-8")), "UTF-8"), 8192);
            ArrayList<String> s_round = new ArrayList<String>();
            tmp = XMLUtils.readTag(in, XMLUtils.XML_ROUND);
            while (tmp.length() > 0) {
                s_round.add(tmp);
                tmp = XMLUtils.readTag(in, XMLUtils.XML_ROUND);
            }

            // Parse Round
            for (String s : s_round) {
                BufferedReader local = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes("UTF-8")), "UTF-8"), 8192);
                int id = Integer.parseInt(XMLUtils.readTag(local, XMLUtils.XML_ID));

                Round r = new Round();
                r.sn = id;
                r.tables = new ArrayList<Table>();

                String tp2 = XMLUtils.readTag(local, XMLUtils.XML_TABLE);
                ArrayList<String> s_tab = new ArrayList<String>();
                while (tp2.length() > 0) {
                    s_tab.add(tp2);
                    tp2 = XMLUtils.readTag(local, XMLUtils.XML_TABLE);
                }

                for (String s2 : s_tab) {
                    BufferedReader local2 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s2.getBytes("UTF-8")), "UTF-8"), 8192);
                    String p1 = fixName(XMLUtils.readTag(local2, XMLUtils.XML_PLAYER));
                    String p2 = fixName(XMLUtils.readTag(local2, XMLUtils.XML_PLAYER));
                    String winner = fixName(XMLUtils.readTag(local2, XMLUtils.XML_WINNER));

                    Table t = new Table(r.getTables().size() + 1);
                    if (p2.equals(XMLUtils.XML_BYE)) {
                        t.player2 = null;
                    }
                    for (Integer i : to.participant.keySet()) {
                        if (to.participant.get(i).name.toLowerCase().equals(p1.toLowerCase())) {
                            t.player1 = to.participant.get(i);
                        } else if (to.participant.get(i).name.toLowerCase().equals(p2.toLowerCase())) {
                            t.player2 = to.participant.get(i);
                        }
                    }
                    // Check if a player is dropped
                    if (t.player1 == null) {
                        Participant p = new Participant();
                        p.name = p1;
                        to.enroll(p);
                        t.player1 = p;
                    }
                    if (t.player2 == null && !p2.equals(XMLUtils.XML_BYE)) {
                        Participant p = new Participant();
                        p.name = p2;
                        to.enroll(p);
                        t.player2 = p;
                    }

                    if (p1.toLowerCase().equals(winner.toLowerCase())) {
                        t.winner = Table.PLAYER1;
                    } else if (p2.toLowerCase().equals(winner.toLowerCase())) {
                        t.winner = Table.PLAYER2;
                    } else {
                        t.winner = Table.PROXY_DRAW;
                    }
                    t.host = Table.PROXY_DRAW;

                    r.add(t);
                }

                to.round.add(r);
            }

            //Calculate GameResult
            for (Round r : to.round) {
                for (Table t : r.getTables()) {
                    int result1 = -1;
                    int result2 = -1;
                    switch (t.winner) {
                        case Table.PLAYER1:
                            result1 = GameResult.RESULT_WON;
                            result2 = GameResult.RESULT_LOSS;
                            break;
                        case Table.PLAYER2:
                            result1 = GameResult.RESULT_LOSS;
                            result2 = GameResult.RESULT_WON;
                            break;
                        case Table.PROXY_DRAW:
                            result1 = GameResult.RESULT_DRAW;
                            result2 = GameResult.RESULT_DRAW;
                            break;
                    }

                    t.player1.games.put(t, new GameResult(t.player2, result1));
                    if (t.player2 != null) {
                        t.player2.games.put(t, new GameResult(t.player1, result2));
                    }
                }
            }

            return to;

        } catch (IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static String fixName(String name) {
        if (name.equals("KRAST")) return "krast_D";
        if (name.startsWith("krast")) return "krast_D";
        if (name.equals("denncu")) return "krast_D";
        if (name.equals("dence")) return "krast_D";
        if (name.equals("nanashi_F")) return "F";
        if (name.equals("Nanashi_F")) return "F";
        if (name.equals("F_hiro")) return "F";
        if (name.equals("flute")) return "F";
        if (name.equals("|_|")) return "F";
        if (name.equals("FFFFFF")) return "F";
        if (name.equals("MOK_now")) return "MOK";
        if (name.equals("^w^")) return "tenjoin";
        if (name.equals("kaname")) return "tenjoin";
        if (name.equals("riku")) return "tenjoin";
        if (name.equals("bookm")) return "bukkun";
        if (name.equals("inberu")) return "seomasaki";
        if (name.equals("bukkun_AC")) return "bukkun";
        if (name.equals("kok_ss")) return "kokurei_ss";
        if (name.equals("kokurei")) return "kokurei_ss";
        if (name.equals("kiwamuRX")) return "kiwamu";
        if (name.startsWith("chrono")) return "chrono";
        if (name.startsWith("shosho")) return "shosho";
        if (name.startsWith("yuki") && name.length() == 5) return "yuki_hirano";
        if (name.equals("yuki")) return "yuki_hirano";
        if (name.equals("mikage1")) return "t_mikage";
        if (name.startsWith("hai_mk")) return "hai";
        if (name.startsWith("kotti")) return "kotti2";
        if (name.contains("Layla") || name.contains("LaYLa")) return "YukiRera";
        if (name.equals("kogasa") || name.equals("ShiraiKuroko") || name.equals("little-YKRR")) return "YukiRera";
        if (name.equals("kagurasos")) return "kagurazaka";
        if ((name.startsWith("shino") || name.startsWith("Shino")) && !name.startsWith("Shinonome") && !name.startsWith("shinonome")) return "Shino";
        if (name.startsWith("sion")) return "sion";
        if (name.equals("illmood_wahuu")) return "wahuu";
        if (name.startsWith("mituki")) return "mitukihime";
        if (name.equals("hrsts")) return "krast_D";
        if (name.equals("akaitori")) return "krast_D";
        if (name.equals("nomean03")) return "yaiti";
        if (name.equals("yaiti")) return "ichi";
        if (name.equals("Chabu-")) return "Chabu";
        if (name.startsWith("shuriken")) return "alondite";
        return name;
    }

    public static void openTag(BufferedWriter out, String tag) {
        try {
            out.write("<" + tag + ">");
        } catch (IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void closeTag(BufferedWriter out, String tag) {
        try {
            out.write("</" + tag + ">\n");
        } catch (IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String readTag(BufferedReader in, String tag) {
        try {
            in.mark((int) sFileSize);
        } catch (IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean isComplete = false;
        try {
            StringBuilder ret = new StringBuilder();
            int c = in.read();
            out:
            while (c >= 0) {
                if (c == '<') {
                    TagIdent tagi = identifyTag(in);
                    if (tagi.equals(tag)) {
                        if (tagi.isOpen) {
                            while (c >= 0) {
                                c = in.read();
                                if (c == '<') {
                                    TagIdent itagi = identifyTag(in);
                                    if (tag.equals(itagi.tagname) && !itagi.isOpen) {
                                        isComplete = true;
                                        break out;
                                    } else {
                                        ret.append("<" + (itagi.isOpen ? "" : "/") + itagi.tagname + ">");
                                    }
                                } else {
                                    ret.append((char) c);
                                }
                            }
                        } else {
                            isComplete = true;
                            break out;
                        }
                    }
                }
                c = in.read();
            }

            if (isComplete) {
                return ret.toString();
            } else {
                in.reset();
                return "";
            }

        } catch (IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     *
     * @param in
     * @return Tag name.
     * null if it's an ending tag
     */
    private static TagIdent identifyTag(BufferedReader in) {
        try {
            boolean isOpen = true;
            StringBuilder ret = new StringBuilder();
            char c = (char) in.read();
            if (c == '/') {
                isOpen = false;
                c = (char) in.read();
            }
            while (c != '>') {
                ret.append(c);
                c = (char) in.read();
            }
            return new TagIdent(isOpen, ret.toString());
        } catch (IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static class TagIdent {

        public boolean isOpen;
        public String tagname;

        public TagIdent(boolean isOpen, String tagname) {
            this.isOpen = isOpen;
            this.tagname = tagname;
        }

        public boolean equals(String tag) {
            return tagname.equals(tag);
        }
    }
}
