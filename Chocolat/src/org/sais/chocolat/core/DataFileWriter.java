/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.core;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Romulus
 */
public class DataFileWriter {

    public static void flushStanding(Tournament tour) {
        int round = tour.round.size();
        ArrayList<Integer> ranking = new ArrayList<Integer>(tour.participant.size());

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Standings_round" + round + ".txt"), "UTF-8"));

            out.write("|Rank|Name|Points|Opp%|Deck|\r\n");
            ArrayList<Participant> a = tour.getStandingsArray();
            int order = 0;
            int sameorder = 0;
            int nowpt = -1;
            float nowopp = -1f;
            for (Participant p : a) {
                int pt = p.getPoint();
                float opp = p.getOpp();
                if (pt == nowpt && Math.abs(opp - nowopp) < 0.0001) {
                    sameorder++;
                } else {
                    order += sameorder + 1;
                    sameorder = 0;
                }
                ranking.add(order);

                String sopp = String.valueOf(opp);
                if (sopp.length() > 5) {
                    sopp = sopp.substring(0, 5);
                }
                out.write("|" + order + "|" + p.name + "|" + pt + "|" + sopp + "|[[L" + p.deck + ">" + tour.name + " " + p.name + "]]|\r\n");
                nowpt = pt;
                nowopp = opp;
            }

            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");

            Collections.reverse(ranking);
            Collections.reverse(a);
            Iterator it = ranking.iterator();
            for (Participant p : a) {
                String sopp = String.valueOf(p.getOpp());
                if (sopp.length() > 5) {
                    sopp = sopp.substring(0, 5);
                }
                out.write(it.next() + "位 " + p.name + " " + p.getPoint() + "pts Opp%:" + sopp + "\r\n");
            }

            out.close();
        } catch (IOException ex) {
            Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void flushResult(Tournament tour) {
        int round = tour.round.size();
        if (round == 0) {
            return;
        }
        Round r = tour.getCurrentRound();

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Results_round" + round + ".txt"), "UTF-8"));

            out.write("|>|>|>|&italic(){Round " + round + "}|\r\n");
            for (Table t : r.tables) {
                String p2name = t.getP2() == null ? "BYE" : t.getP2().name;
                String winner;
                if (t.getWinner() == Table.PLAYER1) {
                    winner = "O-X";
                } else if (t.getWinner() == Table.PLAYER2) {
                    winner = "X-O";
                } else if (t.getWinner() == Table.PROXY_DRAW) {
                    winner = "Draw";
                } else {
                    winner = "No Result";
                }
                out.write("|[[" + tour.name + " " + t.getP1().name + " VS " + p2name + "]]|" + winner + "|\r\n");
            }

            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }

    private static String getShortDeckType(String deck) {
        if (deck.length() < 1) {
            return "";
        }
        String ret = "";
        String[] ss = deck.split("：");
        for (String s : ss) {
            ret += s.charAt(0);
            ret += s.charAt(s.length() - 1);
        }
        return ret;
    }

    public static void reflushAllResults(Tournament tour) {
        int round = tour.round.size();
        if (round == 0) {
            return;
        }
        try {
            BufferedWriter allout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Results_all.txt"), "UTF-8"));
            for (Round r : tour.round) {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Results_round" + r.sn + ".txt"), "UTF-8"));

                out.write("|>|&italic(){Round " + r.sn + "}|\r\n");
                allout.write("|>|&italic(){Round " + r.sn + "}|\r\n");
                for (Table t : r.tables) {
                    String p2name = t.getP2() == null ? "BYE" : t.getP2().name;
                    String winner;
                    if (t.getWinner() == Table.PLAYER1) {
                        winner = "O-X";
                    } else if (t.getWinner() == Table.PLAYER2) {
                        winner = "X-O";
                    } else if (t.getWinner() == Table.PROXY_DRAW) {
                        winner = "Draw";
                    } else {
                        winner = "No Result";
                    }
                    if (t.getP2() == null) {
                        continue;
                    }
                    out.write("|[[リプレイ>" + tour.name + " " + t.getP1().name + " VS " + p2name + "]]|" + t.getP1().name + "(" + getShortDeckType(t.getP1().deck) + ")|" + winner + "|" + p2name + "(" + getShortDeckType(t.getP2().deck) + ")|\r\n");
                    allout.write("|[[リプレイ>" + tour.name + " " + t.getP1().name + " VS " + p2name + "]]|" + t.getP1().name + "(" + getShortDeckType(t.getP1().deck) + ")|" + winner + "|" + p2name + "(" + getShortDeckType(t.getP2().deck) + ")|\r\n");
                }
                out.close();
                allout.newLine();
            }
            allout.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void flushPairing(Tournament tour) {
        int round = tour.round.size();
        Round r = tour.getCurrentRound();

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Pairings_round" + round + ".txt"), "UTF-8"));

            int table = 0;
            for (Table t : r.tables) {
                ++table;
                String hoster;

                switch (t.host) {
                    case Table.PLAYER1:
                        hoster = t.getP1().name;
                        break;
                    case Table.PLAYER2:
                        hoster = t.getP2() == null ? "BYE" : t.getP2().name;
                        break;
                    default:
                        hoster = "PROXY";
                }
                String p2name = t.getP2() == null ? "BYE" : t.getP2().name;
                out.write("Table" + table + " " + t.getP1().name + " VS " + p2name + " Host:" + hoster + "\r\n");
            }

            out.close();
        } catch (IOException ex) {
            Logger.getLogger(DataFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
