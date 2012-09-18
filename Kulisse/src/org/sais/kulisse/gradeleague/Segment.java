/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse.gradeleague;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 *
 * @author chung-mingtsai
 */
public class Segment {

    private ArrayList<Scorable> mScorables = new ArrayList<Scorable>();
    private int mSerial;

    public Segment(String path, int serial) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        mSerial = serial;
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            Scorable score;
            if (line.contains("O-X") || line.contains("X-O")) {
                score = new Game(line);
            } else if (line.contains("*^*")) {
                score = new Champion(line);
            } else {
                continue;
            }
            mScorables.add(score);
        }
        in.close();
    }

    public ArrayList<Scorable> getScores() {
        return mScorables;

    }

    public void addGame(Player winner, Player loser) throws IOException {
        Game game = new Game(winner.getName(), loser.getName());
        mScorables.add(game);
        writeScores();
    }

    public void addChampion(String winner, String tourName) throws IOException {
        Champion champ = new Champion(winner, tourName);
        mScorables.add(champ);
        writeScores();
    }
    
    public int getSerial() {
        return mSerial;
    }

    public Game deleteGame(String player1, String player2) {
        Game ret = null;
        for (Scorable score : mScorables) {
            if (!(score instanceof Game)) {
                continue;
            }
            Game game = (Game) score;
            if ((game.getWinnerName().equalsIgnoreCase(player1) && game.getLoserName().equalsIgnoreCase(player2))
                    || (game.getWinnerName().equalsIgnoreCase(player2) && game.getLoserName().equalsIgnoreCase(player1))) {
                ret = game;
                break;
            }
        }
        if (ret != null) {
            mScorables.remove(ret);
        }
        return ret;
    }

    private void writeScores() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(GradeManager.getSegmentPath(mSerial)), "UTF-8"));
        for (Scorable score : mScorables) {
            out.write(score.getSegmentLine());
            out.newLine();
        }
        out.close();
    }

}
