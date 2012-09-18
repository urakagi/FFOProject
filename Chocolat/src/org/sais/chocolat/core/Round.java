/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.chocolat.xml.ChocolatXMLWritable;
import org.sais.chocolat.xml.XMLUtils;

/**
 *
 * @author Romulus
 */
public class Round implements ChocolatXMLWritable {

    public int sn;
    public ArrayList<Table> tables = new ArrayList<Table>();

    public void add(Table t) {
        tables.add(t);
    }

    public ArrayList<Table> getTables() {
        return tables;
    }

    public Table findTableByParticipantId(int id) {
        for (Table t : tables) {
            if (t.player1.id == id || t.player2.id == id) {
                return t;
            }
        }
        return null;
    }

    public boolean isResultSet() {
        for (Table t : tables) {
            if (t.winner < 0) {
                return false;
            }
        }
        return true;
    }

    public void writeXML(BufferedWriter out) {
        try {
            XMLUtils.openTag(out, XMLUtils.XML_ROUND);
            XMLUtils.openTag(out, XMLUtils.XML_ID);
            out.write(String.valueOf(sn));
            XMLUtils.closeTag(out, XMLUtils.XML_ID);
            for (Table t : tables) {
                t.writeXML(out);
            }
            XMLUtils.closeTag(out, XMLUtils.XML_ROUND);
        } catch (IOException ex) {
            Logger.getLogger(Round.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
