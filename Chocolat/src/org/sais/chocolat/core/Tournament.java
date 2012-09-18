/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.sais.chocolat.xml.ChocolatXMLWritable;
import org.sais.chocolat.xml.XMLUtils;

/**
 *
 * @author Romulus
 */
public class Tournament implements ChocolatXMLWritable {

    public String name = "";
    public String promoter = "";
    public Date date;
    public HashMap<Integer, Participant> participant = new HashMap<Integer, Participant>();
    public ArrayList<Round> round = new ArrayList<Round>();

    public Tournament() {
        date = new Date(System.currentTimeMillis());
    }

    public void start(String name) {
        this.name = name;
        this.date = new Date(System.currentTimeMillis());
    }

    /**
     * Enroll an empty participant. ID will be assigned automatically.
     * @return The new paricipant's ID.
     */
    public int enroll() {
        Participant p = new Participant();
        p.id = getNextID();
        p.isHostable = true;
        participant.put(p.id, p);
        return p.id;
    }

    /**
     * Enroll a pre-set participant. ID will be assigned automatically.
     * @param p Enrolling participant.
     * @return The assigned ID.
     */
    public int enroll(Participant p) {
        p.id = getNextID();
        participant.put(p.id, p);
        return p.id;
    }

    public void delete(Integer id) {
        participant.remove(id);
    }

    private int getNextID() {
        int last = 0;
        for (Integer i : participant.keySet()) {
            if (i > last) {
                last = i;
            }
        }
        return last + 1;
    }

    /**
     * Find a participant by name. Tailing '_" are ignored.
     * @param name The participant's name.
     * @return The participant with indicated name.
     */
    public Participant getParticipant(String name) {
        for (Integer i : participant.keySet()) {
            if (participant.get(i).name.contains("&")) {
                if (deleteTailUnderline(participant.get(i).name).contains(deleteTailUnderline(name))) {
                    return participant.get(i);
                }
            } else {
                if (deleteTailUnderline(participant.get(i).name).equalsIgnoreCase(deleteTailUnderline(name))) {
                    return participant.get(i);
                }
            }
        }
        return null;
    }

    public Participant checkIP(String ip) {
        for (Participant p : participant.values()) {
            if (p.ip != null) {
                if (p.ip.equals(ip)) {
                    return p;
                }
            }
        }
        return null;
    }

    public static String deleteTailUnderline(String s) {
        for (int i = s.length() - 1; i >= 0; --i) {
            if (s.charAt(i) != '_') {
                return s.substring(0, i + 1);
            }
        }
        return s;
    }

    public void deleteAllParticipantUnderlines() {
        for (Participant p : participant.values()) {
            p.name = deleteTailUnderline(p.name);
        }
    }

    public void setParticipantName(Integer id, String name) {
        participant.get(id).name = name;
    }

    public void setParticipantDeck(Integer id, String deck) {
        participant.get(id).deck = deck;
    }

    public void setIsHostable(Integer id, Boolean isHostable) {
        participant.get(id).isHostable = isHostable;
    }

    public void setParticipantIP(Integer id, String ip) {
        participant.get(id).ip = ip;
    }

    public void dump() {
        for (Integer id : participant.keySet()) {
            System.out.println(participant.get(id).id + " " + participant.get(id).name + " " + participant.get(id).isHostable);
        }
        for (Table t : round.get(0).tables) {
            if (t.player2 == null) {
                System.out.println(t.player1.name + " vs *BYE*");
            } else {
                System.out.println(t.player1.name + " vs " + t.player2.name);
            }
        }
        System.out.println();
    }

    public ArrayList<Participant> getStandingsArray() {
        ArrayList<Participant> a = new ArrayList<Participant>();
        for (Integer id : participant.keySet()) {
            a.add(participant.get(id));
        }
        Collections.sort(a);
        Collections.reverse(a);

        return a;
    }

    public ArrayList<Participant> getPairingsArray() {
        ArrayList<Participant> a = new ArrayList<Participant>();
        for (Integer id : participant.keySet()) {
            a.add(participant.get(id));
        }

        ArrayList<Participant> ret = new ArrayList<Participant>();
        int nowpt = -1;
        ArrayList<Participant> group = new ArrayList<Participant>();
        Collections.sort(a);
        while (!a.isEmpty()) {
            Participant p = a.remove(0);
            if (p.getPoint() != nowpt) {
                Collections.shuffle(group);
                ret.addAll(group);
                group.clear();
                group.add(p);
                nowpt = p.getPoint();
            } else {
                group.add(p);
            }
        }
        Collections.shuffle(group);
        ret.addAll(group);

        Collections.reverse(ret);

        return ret;
    }

    public void pairLates() {
        Round r = round.get(round.size() - 1);

        ArrayList<Participant> c = new ArrayList<Participant>();
        for (Participant p : participant.values()) {
            c.add(p);
        }

        // Get BYE tables
        ArrayList<Table> rt = new ArrayList<Table>(1);
        for (Table t : r.getTables()) {
            if (t.getP2() != null) {
                c.remove(t.getP1());
                c.remove(t.getP2());
            } else {
                rt.add(t);
            }
        }
        r.getTables().removeAll(rt);

        Participant[] a = new Participant[c.size()];
        c.toArray(a);
        for (int i = 0; i < a.length; ++i) {
            if (i == a.length - 1) {
                r.add(new Table(r.getTables().size() + 1, a[i]));
                break;
            }
            r.add(new Table(r.getTables().size() + 1, a[i], a[i + 1]));
            i++;
        }
    }

    public void repair() {
        round.remove(round.size() - 1);
        pair();
    }

    public void pair() {
        ArrayList<Participant> a = getPairingsArray();

        Round r = new Round();
        while (!a.isEmpty()) {
            // Pick the player with highest rank, then remove it from the remaining array (RA).
            Participant par = a.remove(0);
            // The last player gets a BYE.
            if (a.isEmpty()) {
                r.add(new Table(r.getTables().size() + 1, par));
                break;
            }

            // Select the picked player's opponent
            ArrayList<Participant> repick = new ArrayList<Participant>();
            for (Participant opp : a) {
                // Check if is already matched
                if (par.isMatchedWith(opp)) {
                    System.out.println(par.name + " & " + opp.name + " table " + (r.tables.size() + 1));
                    // Check if paring system is in a dead status
                    if (a.indexOf(opp) == a.size() - 1) {
                        System.out.println("No more player available.");
                        // The last oppo has matched picked player, release last table and try to pair
                        ArrayList<Participant> leftParticipants = new ArrayList<Participant>();
                        ArrayList<Table> nowTables = new ArrayList<Table>();
                        leftParticipants.addAll(a);
                        leftParticipants.add(par);
                        nowTables.addAll(r.getTables());
                        if (nowTables.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Can't pair");
                            return;
                        }
                        Table t = nowTables.remove(nowTables.size() - 1);
                        leftParticipants.add(t.getP1());
                        if (t.getP2() != null) {
                            leftParticipants.add(t.getP2());
                        }
                        while (!pairPart(leftParticipants, nowTables)) {
                            if (nowTables.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Can't pair");
                                return;
                            }
                            t = nowTables.remove(nowTables.size() - 1);
                            leftParticipants.add(t.getP1());
                            if (t.getP2() != null) {
                                leftParticipants.add(t.getP2());
                            }
                        }
                        r.tables = nowTables;
                        round.add(r);
                        r.sn = round.size();
                        return;
                    }
                    System.out.println("Move to next opponent.");
                    continue;
                }
                // Current oppo has not matched with the picked player, check stairs
                if (par.getPoint() == opp.getPoint()) {
                    // Disable proxy preventing mechanism since auto-proxy is avilable now
                    // Not a stair, check if both unhostable
                    /*if (!par.isHostable && !opp.isHostable) {
                                        if (a.indexOf(opp) != a.size() - 1) {
                                        // Both hostable and is not the last one, send oppo to repick array and try others first
                                        repick.add(opp);
                                        continue;
                                        }
                                        }*/
                } else {
                    // A stair or is purged by unhostability
                    if (repick.size() > 0) {
                        // Unhostabliliy purging, pair PROXY
                        Participant reo = repick.get(0);
                        r.add(new Table(r.getTables().size() + 1, par, reo));
                        a.remove(reo);
                        break;
                    }
                    // A stair falls down
                }

                // Everything is good, pair them
                System.out.println("Paired " + par + " & " + opp + " table " + (r.tables.size() + 1));
                r.add(new Table(r.getTables().size() + 1, par, opp));
                a.remove(opp);
                break;
            }

        }

        round.add(r);
        r.sn = round.size();

        System.out.println();
    }

    private boolean pairPart(ArrayList<Participant> arr, ArrayList<Table> tables) {
        ArrayList<Participant> rem = new ArrayList<Participant>();
        int anchor = tables.size();
        rem.addAll(arr);
        if (rem.size() == 1) {
            tables.add(new Table(tables.size(), rem.remove(0)));
            return true;
        }
        if (rem.size() == 0) {
            return true;
        }
        Participant p1 = rem.remove(0);
        for (int i = 0; i < rem.size(); ++i) {
            Participant p2 = rem.get(i);
            if (p1.isMatchedWith(p2)) {
                continue;
            }
            rem.remove(p2);
            if (pairPart(rem, tables)) {
                tables.add(new Table(tables.size(), p1, p2));
                return true;
            } else {
                rem.add(p2);
                for (int j = anchor; j < tables.size(); ++j) {
                    tables.remove(j);
                }
                continue;
            }
        }
        // All opponents can't be paired
        return false;
    }

    public Round getCurrentRound() {
        if (round.size() == 0) {
            return null;
        }
        return round.get(round.size() - 1);
    }

    public ArrayList<Participant> getParticipantsForDeck(String deckName) {
        ArrayList<Participant> ret = new ArrayList<Participant>();
        for (Integer id : participant.keySet()) {
            if (participant.get(id).deck.equals(deckName)) {
                ret.add(participant.get(id));
            }
        }
        return ret;
    }

    public int countParticipants() {
        return participant.size();
    }

    public void writeXML(BufferedWriter out) {
        try {
            XMLUtils.openTag(out, XMLUtils.XML_TOURNAMENT);

            XMLUtils.openTag(out, XMLUtils.XML_NAME);
            out.write(name);
            XMLUtils.closeTag(out, XMLUtils.XML_NAME);

            XMLUtils.openTag(out, XMLUtils.XML_PROMOTER);
            out.write(promoter);
            XMLUtils.closeTag(out, XMLUtils.XML_PROMOTER);

            XMLUtils.openTag(out, XMLUtils.XML_DATE);
            out.write(String.valueOf(date.getTime()));
            XMLUtils.closeTag(out, XMLUtils.XML_DATE);

            for (Integer i : participant.keySet()) {
                participant.get(i).writeXML(out);
            }

            for (Round r : round) {
                r.writeXML(out);
            }

            XMLUtils.closeTag(out, XMLUtils.XML_TOURNAMENT);
        } catch (IOException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
