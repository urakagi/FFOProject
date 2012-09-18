/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.chocolat.xml.ChocolatXMLWritable;
import org.sais.chocolat.xml.XMLUtils;

/**
 *
 * @author Romulus
 */
public class Participant implements Comparable<Participant>, ChocolatXMLWritable {

    public int id;
    public String name = "";
    public String ip = "";
    public boolean isHostable = true;
    public String deck = "";
    public HashMap<Table, GameResult> games = new HashMap<Table, GameResult>();

    public int getPoint() {
        int pts = 0;
        for (GameResult g : games.values()) {
            if (g.result == GameResult.RESULT_WON) {
                pts += 3;
            } else if (g.result == GameResult.RESULT_DRAW) {
                pts += 1;
            }
        }
        return pts;
    }

    public float getOpp() {
        int bye = 0;
        float opp_pts = 0f;
        for (GameResult g : games.values()) {
            if (g.opp != null) {
                float temp = (g.opp.getPoint() * 1.0f) / (g.opp.games.size() * 3.0f);
                if (temp < 0.33f) {
                    temp = 0.33f;
                }
                opp_pts += temp;
            } else {
                bye += 1;
            }
        }
        if (games.size() - bye == 0) {
            return 0f;
        }
        return opp_pts / (games.size() - bye);
    }

    public int getWins() {
        int ret = 0;
        for (GameResult g : games.values()) {
            if (g.result == GameResult.RESULT_WON) {
                ++ret;
            }
        }
        return ret;
    }

    public int getLosses() {
        int ret = 0;
        for (GameResult g : games.values()) {
            if (g.result == GameResult.RESULT_LOSS) {
                ++ret;
            }
        }
        return ret;
    }

    public int getDraws() {
        int ret = 0;
        for (GameResult g : games.values()) {
            if (g.result == GameResult.RESULT_DRAW) {
                ++ret;
            }
        }
        return ret;
    }

    public boolean isMatchedWith(Participant opp) {
        boolean isMatched = false;
        for (GameResult g : games.values()) {
            if (g.opp == null) {
                continue;
            }
            if (g.opp.equals(opp)) {
                isMatched = true;
                break;
            }
        }
        return isMatched;
    }

    public int compareTo(Participant o) {
        if (this.getPoint() > o.getPoint()) {
            return 1;
        }
        if (this.getPoint() < o.getPoint()) {
            return -1;
        }
        if (this.getOpp() - o.getOpp() > 0.0001) {
            return 1;
        }
        if (this.getOpp() - o.getOpp() < -0.0001) {
            return -1;
        }
        return Math.random() > 0.5 ? 1 : -1;
    }

    @Override
    public String toString() {
        return name;
    }

    public void Bye() {
    }

    public void writeXML(BufferedWriter out) {
        try {
            XMLUtils.openTag(out, XMLUtils.XML_PARTICIPANT);
            XMLUtils.openTag(out, XMLUtils.XML_ID);
            out.write(String.valueOf(id));
            XMLUtils.closeTag(out, XMLUtils.XML_ID);
            XMLUtils.openTag(out, XMLUtils.XML_NAME);
            out.write(name);
            XMLUtils.closeTag(out, XMLUtils.XML_NAME);
            XMLUtils.openTag(out, XMLUtils.XML_DECK);
            if (deck == null) {
                deck = "";
            }
            out.write(deck);
            XMLUtils.closeTag(out, XMLUtils.XML_DECK);
            XMLUtils.openTag(out, XMLUtils.XML_ISHOSTABLE);
            out.write(String.valueOf(isHostable));
            XMLUtils.closeTag(out, XMLUtils.XML_ISHOSTABLE);
            XMLUtils.openTag(out, XMLUtils.XML_IP);
            out.write(encryptReversable(ip));
            XMLUtils.closeTag(out, XMLUtils.XML_IP);
            XMLUtils.closeTag(out, XMLUtils.XML_PARTICIPANT);
        } catch (IOException ex) {
            Logger.getLogger(Participant.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String encryptReversable(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        int d = 0;
        for (char c : s.toCharArray()) {
            b.append(Character.valueOf((char) (c + d - 10)));
            ++d;
        }
        return b.toString();
    }

    public static String unencrypt(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        int d = 0;
        for (char c : s.toCharArray()) {
            b.append(Character.valueOf((char) (c - d + 10)));
            ++d;
        }
        return b.toString();
    }
}
