/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.chocolat.xml.ChocolatXMLWritable;
import org.sais.chocolat.xml.XMLUtils;

/**
 *
 * @author Romulus
 */
public class Table implements ChocolatXMLWritable {

    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static final int PROXY_DRAW = 3;
    public Participant player1;
    public Participant player2;
    public int sn;
    public int host;
    public int winner = -1;

    public Table(int no) {
        this.sn = no;
    }

    public Table(int no, Participant p1, Participant p2) {
        this.sn = no;
        player1 = p1;
        player2 = p2;
        if (p1.isHostable) {
            host = PLAYER1;
        } else if (p2.isHostable) {
            host = PLAYER2;
        } else {
            host = PROXY_DRAW;
        }
    }

    public Table(int no, Participant p1) {
        this.sn = no;
        player1 = p1;
        player2 = null;
    }

    public Participant getP1() {
        return player1;
    }

    public Participant getP2() {
        return player2;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int w) {
        winner = w;
        int w1;
        int w2;
        switch (w) {
            case PLAYER1:
                w1 = 3;
                w2 = 0;
                break;
            case PLAYER2:
                w1 = 0;
                w2 = 3;
                break;
            case PROXY_DRAW:
                w1 = 1;
                w2 = 1;
                break;
            default:
                System.out.println("Result Error, table = " + sn);
                return;
        }
        player1.games.put(this, new GameResult(player2, w1));
        if (player2 != null) {
            player2.games.put(this, new GameResult(player1, w2));
        }
    }

    public boolean isResultSet() {
        return winner > 0;
    }

    public String getIRCResultString() {
        if (getP2() == null) {
            return "Table" + sn + " " + getP1().name + " *BYE*";
        }
        if (winner == PLAYER1) {
            return "Table" + sn + " " + getP1().name + " O-X " + getP2().name;
        } else if (winner == PLAYER2) {
            return "Table" + sn + " " + getP1().name + " X-O " + getP2().name;
        } else if (winner == PROXY_DRAW) {
            // Draw
            return "Table" + sn + " " + getP1().name + " △-△ " + getP2().name;
        } else {
            return "Table" + sn + " " + getP1().name + " VS " + getP2().name;
        }
    }

    public String getHostIP() {
        if (host == PLAYER1) {
            return player1.ip;
        }
        if (host == PLAYER2) {
            return player2.ip;
        }
        // Proxy or BYE
        if (player2 == null) {
            return "";
        }
        return "PROXY";
    }

    public void writeXML(BufferedWriter out) {
        try {
            XMLUtils.openTag(out, XMLUtils.XML_TABLE);
            XMLUtils.openTag(out, XMLUtils.XML_PLAYER);
            out.write(player1.name);
            XMLUtils.closeTag(out, XMLUtils.XML_PLAYER);
            XMLUtils.openTag(out, XMLUtils.XML_PLAYER);
            if (player2 == null) {
                out.write(XMLUtils.XML_BYE);
            } else {
                out.write(player2.name);
            }
            XMLUtils.closeTag(out, XMLUtils.XML_PLAYER);
            XMLUtils.openTag(out, XMLUtils.XML_WINNER);
            switch (winner) {
                case PLAYER1:
                    out.write(player1.name);
                    break;
                case PLAYER2:
                    out.write(player2.name);
                    break;
                case PROXY_DRAW:
                    out.write(XMLUtils.XML_DRAW);
                    break;
            }
            XMLUtils.closeTag(out, XMLUtils.XML_WINNER);
            XMLUtils.closeTag(out, XMLUtils.XML_TABLE);
        } catch (IOException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
