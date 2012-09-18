/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse.gradeleague;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chung-mingtsai
 */
public class Standings {

    private final static String FILE_NAME = "standings.txt";
    private ArrayList<Player> mPlayers = new ArrayList<Player>();

    public Standings() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            mPlayers.add(new Player(line));
        }
        in.close();
    }

    public void newPlayer(String name) throws IOException {
        if (!getPlayer(name).isNull()) {
            throw new IllegalStateException(name);
        }
        mPlayers.add(new Player(getNewId() + " " + name + " -3 0"));
        writeStandings();
    }

    private int getNewId() {
        int max = -1;
        for (Player p : mPlayers) {
            if (p.getId() > max) {
                max = p.getId();
            }
        }
        return max + 1;
    }

    public Player getPlayer(String name) {
        for (Player p : mPlayers) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return Player.newNull();
    }

    public void check(String p1Name, String p2Name) {
        Player p1 = getPlayer(p1Name);
        Player p2 = getPlayer(p2Name);

        // Check if players are registered
        if (p1.isNull()) {
            throw new IllegalStateException(p1Name + "さんはまだ段位戦に参加してません。「＠段位戦 参加」で参加しておいてください。");
        }
        if (p2.isNull()) {
            throw new IllegalStateException(p2Name + "さんはまだ段位戦に参加してません。「＠段位戦 参加」で参加しておいてください。");
        }

        // Check grade
        if (p1.getGrade() > 0 && p2.getGrade() > 0) {
            if (Math.abs(p1.getGrade() - p2.getGrade()) > 1) {
                throw new IllegalStateException("段位保持者が対戦できるのは前後一段の保持者までです。");
            }
        } else if (p1.getGrade() * p2.getGrade() < 0) {
            // 段位と級位、初段と一級＝商が-1のみが許される
            if (p1.getGrade() * p2.getGrade() != -1) {
                throw new IllegalStateException("段位保持者と級位保持者が対戦できるのは初段と一級だけです。");
            }
        }

        for (Scorable score : p1.getScores()) {
            if (score instanceof Game) {
                Game g = (Game) score;
                if (g.getLoserName().equals(p2.getName()) || g.getWinnerName().equals(p2.getName())) {
                    throw new IllegalStateException(p1.getName() + "さんと" + p2.getName() + "さんは今期中対戦済です。");
                }
            }
        }
    }

    public void cancel(String winnerName, String loserName) throws IOException {
        getPlayer(loserName).win(getPlayer(winnerName));
        Collections.sort(mPlayers);
        writeStandings();
    }

    public void addGame(String winner, String loser) throws IOException {
        getPlayer(winner).win(getPlayer(loser));
        Collections.sort(mPlayers);
        writeStandings();
    }

    public void addChamp(String winner, String tourName) throws IOException {
        getPlayer(winner).champ(tourName);
        Collections.sort(mPlayers);
        writeStandings();
    }

    public ArrayList<Player> getSortedPlayers() {
        return mPlayers;
    }

    private void writeStandings() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME), "UTF-8"));
        for (Player p : mPlayers) {
            out.write(p.getDataLine());
            out.newLine();
        }
        out.close();
    }

    public void backup(int serial) {
        FileOutputStream out = null;
        try {
            File dest = new File("backup/standingsAfter" + serial + ".txt");
            dest.getParentFile().mkdirs();
            out = new FileOutputStream(dest);
            FileInputStream in = new FileInputStream(FILE_NAME);
            while (true) {
                try {
                    int r = in.read();
                    if (r < 0) {
                        break;
                    }
                    out.write(r);
                } catch (IOException ex) {
                    Logger.getLogger(Standings.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Standings.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Standings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
