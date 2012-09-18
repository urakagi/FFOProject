/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse.gradeleague;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

/**
 *
 * @author chung-mingtsai
 */
public class GradeManager {

    private Segment mSegment;
    private Standings mStandings;

    public static String getSegmentPath(int serial) {
        return "segments/segment" + serial + ".txt";
    }

    public GradeManager() throws IOException {
        init();
    }

    private void init() throws IOException {
        int serial = getSerial();
        mStandings = new Standings();
        readSegmentFile(serial);
        for (Scorable score : mSegment.getScores()) {
            if (score instanceof Game) {
                Game g = (Game) score;
                mStandings.getPlayer(g.getWinnerName()).putScore(g);
                mStandings.getPlayer(g.getLoserName()).putScore(g);
            } else if (score instanceof Champion) {
                Champion c = (Champion) score;
                mStandings.getPlayer(c.getWinnerName()).putScore(score);
            }
        }
        updateHTML();
    }

    public void newPlayer(String name) throws IOException {
        mStandings.newPlayer(name);
        updateHTML();
    }

    public int getSerial() {
        Calendar now = Calendar.getInstance();
        int yeardiff = now.get(Calendar.YEAR) - 2011;
        int monthdiff = now.get(Calendar.MONTH) - 8;
        int dayindex = now.get(Calendar.DAY_OF_MONTH) > 15 ? 2 : 1;
        return yeardiff * 24 + monthdiff * 2 + dayindex;
    }

    private void readSegmentFile(int serial) throws IOException {
        File file = new File(getSegmentPath(serial));
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        mSegment = new Segment(file.getPath(), serial);
    }

    public String getRepalyPageName(String winner, String loser) throws UnsupportedEncodingException {
        Player wp = mStandings.getPlayer(winner);
        Player lp = mStandings.getPlayer(loser);
        return "http://www39.atwiki.jp/gensouutage_net/?page=" + URLEncoder.encode("g" + getSerial()
                + "-" + wp.getName() + "(" + wp.getGradeString() + ")"
                + "-" + lp.getName() + "(" + lp.getGradeString() + ")", "UTF-8");
    }

    public void addGame(String winner, String loser) throws IOException, IllegalStateException {
        // Check if is at correct serial
        if (mSegment.getSerial() != getSerial()) {
            mStandings.backup(mSegment.getSerial());
            init();
        }

        // Check duplicated games and duel limitation
        mStandings.check(winner, loser);

        // Update data
        mSegment.addGame(mStandings.getPlayer(winner), mStandings.getPlayer(loser));
        mStandings.addGame(winner, loser);

        // Write out html file
        updateHTML();
    }

    public void addChampion(String winner, String tourName) throws IOException, IllegalStateException {
        // Check if is at correct serial
        if (mSegment.getSerial() != getSerial()) {
            mStandings.backup(mSegment.getSerial());
            init();
        }

        mSegment.addChampion(winner, tourName);
        mStandings.addChamp(winner, tourName);

        updateHTML();
    }

    public void check(String player1, String player2) {
        mStandings.check(player1, player2);
    }

    public void cancel(String player1, String player2) throws IOException {
        Game game = mSegment.deleteGame(player1, player2);
        if (game == null) {
            throw new IllegalStateException(player1 + "さんと" + player2 + "さんは今期中、まだ対戦していません。");
        }
        mStandings.cancel(game.getWinnerName(), game.getLoserName());
        updateHTML();
    }
    
    public String getScoreString(String name) {
        StringBuilder ret = new StringBuilder();
        Player p = mStandings.getPlayer(name);
        ret.append(p.getName()).append("さんは ").append(p.getGradeString()).append(" です。")
                .append("次の位まで、").append(p.getForNextGrade()).append("点が必要になります。");
        return ret.toString();
    }
    
    public Standings getStandings() {
        return mStandings;
    }

    private void updateHTML() throws IOException {
        String dir = "/Applications/MAMP/htdocs/";
        File playerListFile = new File(dir + "players.html");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerListFile), "UTF-8"));
        out.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\"; charset=\"UTF-8\">"
                + "<title>FFO段位戦プレイヤー名簿</title></head><body>");
        out.newLine();
        out.write("<a href=\"http://www39.atwiki.jp/gensouutage_net/pages/10807.html\">ルールページ</a><br>");
        out.newLine();
        out.write("<p>第 " + getSerial() + " 期段位戦進行中</p>");
        out.newLine();
        out.write("<table border=1 cellpadding=3>");
        out.write("<tr><th>ID</th><th>登録名</th><th>段位</th><th>点数</th></tr>");
        out.newLine();
        for (Player p : mStandings.getSortedPlayers()) {
            out.write("<tr><td>" + p.getId() + "</td><td>" + p.getName() + "</td><td>" + p.getGradeString() + "</td><td>" + p.getPointString() + "</td></tr>");
            out.newLine();
        }
        out.write("</table>");
        out.newLine();
        out.write("<br>今期戦歴<br><table border=1>");
        for (Player p : mStandings.getSortedPlayers()) {
            out.write("<tr><td>" + p.getName() + "</td>");
            for (Scorable score : p.getScores()) {
                out.write("<td>" + score.getResultString(p.getName()) + "</td>");
            }
            out.write("</tr>");
            out.newLine();
        }
        out.write("</table>");
        out.write("</body></html>");
        out.close();
    }
}
