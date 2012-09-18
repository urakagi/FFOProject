package org.sais.chocolatsnote.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.sais.chocolat.core.Participant;
import org.sais.chocolat.core.Round;
import org.sais.chocolat.core.Table;
import org.sais.chocolat.core.Tournament;
import org.sais.chocolat.xml.XMLUtils;

/**
 *
 * @author Romulus
 */
public class ChocolatsNote {

    public static String DROPBOX_FOLDER = null;
    private ArrayList<Tournament> mTournaments = new ArrayList<Tournament>();
    private ArrayList<Target> mPlayers = new ArrayList<Target>();
    private ArrayList<Target> mDecks = new ArrayList<Target>();
    private ArrayList<Target> mDeckTypes = new ArrayList<Target>();
    private ArrayList<Target> mChars = new ArrayList<Target>();
    private ArrayList<Target> mCharsLevel = new ArrayList<Target>();
    private ArrayList<Target> mPrevPlayers = new ArrayList<Target>();
    private ArrayList<Target> mPrevDecks = new ArrayList<Target>();
    private ArrayList<Target> mPrevDeckTypes = new ArrayList<Target>();
    private ArrayList<Target> mPrevChars = new ArrayList<Target>();
    private ArrayList<Target> mPrevCharsLevel = new ArrayList<Target>();
    private final int K = 12;
    private boolean COUNTCHAMP = false;

    public ChocolatsNote() {
        DROPBOX_FOLDER = Tools.readConfig("dropbox_folder");
        COUNTCHAMP = Tools.readBooleanConfig("count_champ", false);

        ArrayList<String> folders = Tools.readListConfig("xml_folders");
        ArrayList<File> files = new ArrayList<File>();

        for (String folder : folders) {
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File[] raw_files = dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });
            if (raw_files == null) {
                continue;
            }
            files.addAll(Arrays.asList(raw_files));
        }

        for (File f : files) {
            if (!f.isFile()) {
                continue;
            }
            Tournament t = XMLUtils.parse(f);
            if (t != null) {
                t.deleteAllParticipantUnderlines();
                mTournaments.add(t);
            }
        }
        Collections.sort(mTournaments, new Comparator<Tournament>() {

            @Override
            public int compare(Tournament o1, Tournament o2) {
                if (o1.equals(o2)) {
                    return 0;
                }
                long l1, l2;
                l1 = getTourTime(o1);
                l2 = getTourTime(o2);
                return l1 > l2 ? 1 : -1;
            }
        });
    }

    public void exec() {
        calcRatings();

        try {
            if (COUNTCHAMP) {
                writeChampionCounts();
            } else {
                writePlayerRanking();
                writeCharRanking();
                writeDeckRanking();
                writeWinningDecks();
                writeResults();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    static final double[] CHAR_WEIGHT = new double[]{0.2, 0.6, 1, 1.1};

    private void writeChampionCounts() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DROPBOX_FOLDER + "WinnerCounts.html"), "UTF-8"));
        writeHTMLHeader(out);
        out.write("<table border=1 cellpadding=2>");
        out.write("<tr><th>大会名</th><th>優勝者</th><th>デッキ構成</th><th>規模</th><th>ラウンド数</th><th>開催日</th><th>主催者</th><th>大会名</th></tr>\r\n");
        String[] roundColors = new String[]{"black", "black", "black", "orange", "green", "red", "purple"};
        CountHash<String> winners = new CountHash<String>();
        final CountHash<String> kos = new CountHash<String>();
        SimpleDateFormat f = new SimpleDateFormat("yyyy年MM月dd日（E）", Locale.JAPAN);
        for (int i = mTournaments.size() - 1; i >= 0; --i) {
            Tournament t = mTournaments.get(i);
            Participant p = t.getStandingsArray().get(0);

            String bgcolor = getTourBackground(t);

            String suffix = "<td>" + t.countParticipants() + "人</td><td><font color=" + roundColors[t.round.size()] + ">" + t.round.size() + "ラウンド</font></td>";
            if (t.date.after(new GregorianCalendar(1995, 1, 1).getTime())) {
                suffix += "<td bgcolor=\"" + bgcolor + "\">" + f.format(t.date) + "</td>";
            } else {
                suffix += "<td bgcolor=\"" + bgcolor + "\"></td>";
            }

            if (t.promoter.length() > 0) {
                suffix += "<td bgcolor=\"" + bgcolor + "\"><font color=\"" + getPromoterColor(t.promoter) + "\">" + t.promoter + "</font></td>";
            } else {
                suffix += "<td></td>";
            }

            String tourlink = "<td bgcolor=\"" + bgcolor + "\"><a href=\"http://www39.atwiki.jp/gensouutage_net/?page=" + fixToKanjiExpression(t.name) + "\">" + fixToKanjiExpression(t.name) + "</a></td>";

            suffix += tourlink;

            out.write("<tr>");

            if (extractTournamentSerial(t.name) >= 35 || t.date.getTime() > 1000000) {
                out.write(tourlink + "<td>" + p.name + "</td><td><a href=\"http://www39.atwiki.jp/gensouutage_net/?page=" + t.name + " " + p.name + "\">"
                        + p.deck + "</a></td>" + suffix + "</tr>\r\n");
            } else {
                out.write(tourlink + "<td>" + p.name + "</td><td>" + p.deck + "</td>" + suffix + "</tr>\r\n");
            }
            String decktype = getDeckType(p.deck).name;
            for (String c : CHARS) {
                if (p.deck.matches(".*" + c + "[34]" + ".*")) {
                    decktype = c;
                    break;
                }
            }
            winners.increase(decktype, 1);
            kos.increase(decktype, t.countParticipants() - 1);
        }
        out.write("</table>");
        out.write("<br><h1>優勝デッキカウント</h1>\r\n");
        out.write("<table border=1 cellpadding=2>");
        out.write("<tr><th>優勝デッキタイプ</th><th>優勝回数</th><th>総撃破人数</th></tr>\r\n");
        for (Entry<String, Integer> e : winners.getSortedList(true, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return kos.get(o2.getKey()) - kos.get(o1.getKey());
            }
        })) {
            String p = e.getKey();
            out.write("<tr><td>" + p + "</td><td>" + e.getValue() + "</td><td>" + kos.get(p) + "</td></tr>\r\n");
            out.flush();
        }
        out.write("</table>");
        out.write("<br><h1>キャラ順</h1>\r\n");
        out.write("<table border=1 cellpadding=2>");
        out.write("<tr><th>優勝デッキタイプ</th><th>優勝回数</th><th>総撃破人数</th></tr>\r\n");
        for (String c : CHARS) {
            out.write("<tr><td>" + c + "</td><td>" + winners.get(c) + "</td><td>" + kos.get(c) + "</td></tr>\r\n");
            out.flush();
        }
        out.write("</table>");
        writeHTMLFooter(out);
        out.close();
        System.exit(0);
    }

    public void calcRatings() {
        for (Tournament tour : mTournaments) {
            // Last tournament, record current state for difference
            if (tour.equals(mTournaments.get(mTournaments.size() - 1))) {
                keepPrev();
            }

            for (Round round : tour.round) {
                for (Table table : round.tables) {
                    // Skip BYE
                    if (table.player2 == null) {
                        continue;
                    }

                    double p1pt;
                    double p2pt;
                    switch (table.getWinner()) {
                        case Table.PLAYER1:
                            p1pt = 1;
                            p2pt = 0;
                            break;
                        case Table.PLAYER2:
                            p1pt = 0;
                            p2pt = 1;
                            break;
                        case Table.PROXY_DRAW:
                            p1pt = 0.5;
                            p2pt = 0.5;
                            break;
                        default:
                            System.err.println("Table result error");
                            continue;
                    }

                    int plDelta = calc(mPlayers, table.player1.name, table.player2.name, p1pt, p2pt);
                    calc(mDecks, table.player1.deck, table.player2.deck, p1pt, p2pt);
                    calc(getDeckType(table.player1.deck), getDeckType(table.player2.deck), p1pt, p2pt);

                    // Characters
                    CountHash<String> hash1 = extractCharLevels(table.player1.deck);
                    CountHash<String> hash2 = extractCharLevels(table.player2.deck);
                    calcCharRatings(hash1, hash2, p1pt, p2pt, plDelta);

                    increaseStatByTable(table);
                }
            }
            increaseStatByTour(tour);
        }

        Collections.sort(mPlayers, new Comparator<Target>() {

            @Override
            public int compare(Target o1, Target o2) {
                if (o1.appear < 3) {
                    if (o2.appear < 3) {
                        return o1.compareTo(o2);
                    } else {
                        return 1;
                    }
                } else if (o2.appear < 3) {
                    return -1;
                }
                return o1.compareTo(o2);
            }
        });
        Collections.sort(mChars);
        Collections.sort(mCharsLevel);
        Collections.sort(mDecks);
        Collections.sort(mDeckTypes);
    }

    private int calc(Target tar1, Target tar2, double Sa, double Sb) {
        int delta = ero(tar1.rating, tar2.rating, Sa, 1);
        tar1.rating += delta;
        tar2.rating -= delta;
        return delta;
    }

    private int calc(ArrayList<Target> pool, String name1, String name2, double Sa, double Sb) {
        return calc(Target.getOrNew(pool, name1), Target.getOrNew(pool, name2), Sa, Sb);
    }

    private int ero(int Ra, int Rb, double Sa, double magnification) {
        double Qa = Math.pow(10, Ra / 400.);
        double Qb = Math.pow(10, Rb / 400.);
        double Ea = Qa / (Qa + Qb);
        // Test if <0 will appear, which is a bug
        if (Ea < 0) {
            System.out.println("Ea=" + Ea);
        }
        return (int) (K * (Sa - Ea) * magnification);
    }

    private void calcCharRatings(CountHash<String> h1, CountHash<String> h2, double p1pt, double p2pt, int playerDelta) {
        // Character rating
        int avgRating1 = 0;
        int avgRating2 = 0;
        int avgRating1_2 = 0;
        int avgRating2_2 = 0;
        // Calculate opponent's average rating for Rb
        double totalWeight = 0;
        for (Entry<String, Integer> e : h1.getSortedList(false)) {
            Target c = Target.getOrNew(mCharsLevel, e.getKey() + e.getValue());
            double weight = CHAR_WEIGHT[e.getValue() - 1];
            avgRating1 += (int) (c.rating * weight);
            avgRating1_2 += (int) (c.rating2 * weight);
            totalWeight += weight;
        }
        avgRating1 /= totalWeight;
        avgRating1_2 /= totalWeight;
        totalWeight = 0;
        for (Entry<String, Integer> e : h2.getSortedList(false)) {
            Target c = Target.getOrNew(mCharsLevel, e.getKey() + e.getValue());
            double weight = CHAR_WEIGHT[e.getValue() - 1];
            avgRating2 += (int) (c.rating * weight);
            avgRating2_2 += (int) (c.rating2 * weight);
            totalWeight += weight;
        }
        avgRating2 /= totalWeight;
        avgRating2_2 /= totalWeight;
        totalWeight = 0;

        // Character rating
        for (Entry<String, Integer> e : h1.getSortedList(false)) {
            Target target = Target.getOrNew(mChars, e.getKey());
            int delta = ero(target.rating, avgRating2, p1pt, CHAR_WEIGHT[e.getValue() - 1]);
            target.rating += delta;
            target.rating2 += ero(target.rating2, avgRating2_2, p1pt, CHAR_WEIGHT[e.getValue() - 1]) - (int) (playerDelta * CHAR_WEIGHT[e.getValue() - 1] / 2);
        }
        for (Entry<String, Integer> e : h2.getSortedList(false)) {
            Target target = Target.getOrNew(mChars, e.getKey());
            int delta = ero(target.rating, avgRating1, p2pt, CHAR_WEIGHT[e.getValue() - 1]);
            target.rating += delta;
            target.rating2 += ero(target.rating2, avgRating1_2, p2pt, CHAR_WEIGHT[e.getValue() - 1]) + (int) (playerDelta * CHAR_WEIGHT[e.getValue() - 1] / 2);
        }

        // Character level rating
        for (Entry<String, Integer> e : h1.getSortedList(false)) {
            Target target = Target.getOrNew(mCharsLevel, e.getKey() + e.getValue());
            target.rating += ero(target.rating, avgRating2, p1pt, CHAR_WEIGHT[e.getValue() - 1]);
        }
        for (Entry<String, Integer> e : h2.getSortedList(false)) {
            Target target = Target.getOrNew(mCharsLevel, e.getKey() + e.getValue());
            target.rating += ero(target.rating, avgRating1, p2pt, CHAR_WEIGHT[e.getValue() - 1]);
        }
    }

    private CountHash<String> extractCharLevels(String deck) {
        CountHash<String> ret = new CountHash<String>();

        for (String c : CHARS) {
            if (deck.contains(c)) {
                int lv;
                try {
                    lv = Integer.parseInt(String.valueOf(deck.charAt(deck.indexOf(c) + c.length())));
                } catch (Exception e) {
                    lv = 0;
                }
                ret.increase(c, lv);
            }
        }
        return ret;
    }

    private void increaseStatByTable(Table table) {
        int winner = table.getWinner();

        increaseWonLost(winner,
                Target.findByName(mPlayers, table.player1.name),
                Target.findByName(mPlayers, table.player2.name));

        increaseWonLost(winner,
                Target.findByName(mDecks, table.player1.deck),
                Target.findByName(mDecks, table.player2.deck));

        increaseWonLost(winner,
                getDeckType(table.player1.deck),
                getDeckType(table.player2.deck));
    }

    private void increaseWonLost(int winner, Target tar1, Target tar2) {
        switch (winner) {
            case Table.PLAYER1:
                tar1.won++;
                tar2.lost++;
                break;
            case Table.PLAYER2:
                tar1.lost++;
                tar2.won++;
                break;
            case Table.PROXY_DRAW:
                tar1.drawn++;
                tar2.drawn++;
                break;
        }
    }

    private void increaseStatByTour(Tournament tour) {
        for (Participant par : tour.getStandingsArray()) {
            Target tar;

            // Player tounament participant count
            tar = Target.findByName(mPlayers, par.name);
            tar.appear++;

            //  Character tournament count
            for (Entry<String, Integer> e : extractCharLevels(par.deck).getSortedList(false)) {
                tar = Target.findByName(mChars, e.getKey());
                tar.appear++;
                tar.totalLv += e.getValue();
                if (par.deck.startsWith(e.getKey())) {
                    tar.leaderCnt++;
                }
            }

            for (String s : par.deck.split("：")) {
                if (s.length() > 0) {
                    tar = Target.findByName(mCharsLevel, s);
                    tar.appear++;
                }
            }

        }
    }
    static private String[] CHARS = new String[]{"霊夢", "魔理沙", "咲夜", "妖夢", "紫", "アリス", "レミリア", "幽々子", "フランドール", "パチュリー",
        "美鈴", "輝夜", "永琳", "鈴仙", "藍", "橙", "プリズムリバー", "慧音",
        "妹紅", "萃香", "諏訪子", "神奈子", "早苗", "文", "小町", "にとり", "天子", "衣玖", "空", "燐", "こいし", "さとり", "白蓮", "星",
        "ぬえ", "小傘", "ナズーリン", "水蜜"};

    private void keepPrev() {
        try {
            for (Target t : mPlayers) {
                mPrevPlayers.add(t.clone());
            }
            for (Target t : mDecks) {
                mPrevDecks.add(t.clone());
            }
            for (Target t : mChars) {
                mPrevChars.add(t.clone());
            }
            for (Target t : mCharsLevel) {
                mPrevCharsLevel.add(t.clone());
            }
            for (Target t : mDeckTypes) {
                mPrevDeckTypes.add(t.clone());
            }
            Collections.sort(mPrevPlayers, new Comparator<Target>() {

                @Override
                public int compare(Target o1, Target o2) {
                    if (o1.appear < 3) {
                        if (o2.appear < 3) {
                            return o1.compareTo(o2);
                        } else {
                            return 1;
                        }
                    } else if (o2.appear < 3) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }
            });
            Collections.sort(mPrevChars);
            Collections.sort(mPrevCharsLevel);
            Collections.sort(mPrevDecks);
            Collections.sort(mPrevDeckTypes);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ChocolatsNote.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writePlayerRanking() throws IOException {
        DecimalFormat formatWithSign = new DecimalFormat("<font color=green>↑0</font>;<font color=red>↓0</font>");
        DecimalFormat formatWithPlus = new DecimalFormat("<font color=green>+0</font>");
        DecimalFormat formatPercentageDiff = new DecimalFormat("<font color=green>(+##0.0%)</font>;<font color=red>(-#00.0%)</font>");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DROPBOX_FOLDER + "PlayerRanking.html"), "UTF-8"));
        ChocolatsNote.writeHTMLHeader(out);
        out.write("プレイヤーリザルトリンクは固定ニックではない人には対応してません。<br>");
        out.write("<table border=1 cellpadding=2 style=\"text-align:center\">");
        out.write("<tr><th>順位</th><th></th><th>プレイヤー名</th><th>レーティング</th><th></th><th>大会参加</th><th>勝利</th><th></th><th>敗北</th><th></th>" + "<th>引き分け</th><th></th><th>勝率</th></tr>");
        out.write("<br>\r\n");

        for (Target player : mPlayers) {
            int rank = mPlayers.indexOf(player) + 1;
            int prevRank = -1;
            Target prevPlayer = Target.findByName(mPrevPlayers, player.name);
            if (prevPlayer == null) {
                prevPlayer = new Target(player.name);
            } else {
                prevRank = mPrevPlayers.indexOf(prevPlayer) + 1;
            }

            double perDiff = (1.0 * player.won / (player.won + player.lost)) - (1.0 * prevPlayer.won / (prevPlayer.won + prevPlayer.lost));
            out.write("<tr>");
            out.write("<td>" + rank + "</td>");
            out.write("<td>" + (prevRank > 0 ? (prevRank - rank == 0 ? "" : formatWithSign.format(prevRank - rank)) : "") + "</td>");
            out.write("<td><a href=\"http://dl.dropbox.com/u/5736424/FFOResult/" + player.name + ".html\">" + player.name + "</a></td>");
            out.write("<td>" + player.rating + "</td>");
            out.write("<td>" + ((player.rating - prevPlayer.rating) == 0 ? "" : formatWithSign.format(player.rating - prevPlayer.rating)) + "</td>");
            out.write("<td>" + player.appear + "</td>");
            out.write("<td>" + player.won + "</td>");
            out.write("<td>" + ((player.won - prevPlayer.won) == 0 ? "" : formatWithPlus.format(player.won - prevPlayer.won)) + "</td>");
            out.write("<td>" + player.lost + "</td>");
            out.write("<td>" + ((player.lost - prevPlayer.lost) == 0 ? "" : formatWithPlus.format(player.lost - prevPlayer.lost)) + "</td>");
            out.write("<td>" + player.drawn + "</td>");
            out.write("<td>" + ((player.drawn - prevPlayer.drawn) == 0 ? "" : formatWithPlus.format(player.drawn - prevPlayer.drawn)) + "</td>");
            out.write("<td>" + (new DecimalFormat("###.00")).format(100.0 * player.won / (player.won + player.lost)) + "%</td>");
            out.write("<td>" + (perDiff == 0 ? "" : formatPercentageDiff.format(perDiff)) + "</td>");
            out.write("</tr>\r\n");
            out.flush();
        }
        out.write("</table");
        writeHTMLFooter(out);
    }

    private void writeCharRanking() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DROPBOX_FOLDER + "CharRating.html"), "UTF-8"));
        writeHTMLHeader(out);

        out.write("キャラクターのレーティングです。エロ・・・もとい、Eroレーティングシステムによって算出。<br>\r\n" + "メインファクターは、このキャラクターのレベル傾向を表す。高いレベルでいい結果を残すと上がり、悪い結果を残すと下がる。逆に、低いレベルでいい結果を残すと下がり、悪い結果を残すと上がる。<br>\r\n" + "式は（レベル別レーティング－レベル平均レーティング）ｘウェイトｘ使用回数／総使用回数。ウェイトはレベル１から４で{-2, 1, 6, 8}。<br>\r\n<br>\r\n" + "<table border=1 cellpadding=2>" + "<tr><th>順位</th><th>キャラクター</th><th>レーティング</th><th>使用回数</th><th>Ｌ回数</th><th>Ｌ％</th><th>総レベル</th><th>平均レベル</th><th>ＭＦ</th><th>PL補正含</th></tr><br>\r\n");

        int rank = 1;
        DecimalFormat format = new DecimalFormat("0.00");
        DecimalFormat formatPercentage = new DecimalFormat("#00.0%");
        for (Target ch : mChars) {
            int[] weight_MF = new int[]{-2, 1, 6, 8};
            ch.ratingMF = 0;
            int avg = 0;
            int total = 0;
            for (int i = 1; i <= 4; ++i) {
                Target pp = Target.findByName(mCharsLevel, ch.name + i);
                if (pp != null) {
                    avg += pp.rating;
                    total += pp.appear;
                } else {
                    avg += 1600;
                }
            }
            if (total == 0) {
                continue;
            }
            avg /= 4;
            for (int i = 1; i <= 4; ++i) {
                Target pp = Target.getOrNew(mCharsLevel, ch.name + i);
                ch.ratingMF += (pp.rating - avg) * pp.appear * weight_MF[i - 1] / total;
            }
            ch.ratingMF /= 3;
            String diff = ch.ratingMF < 0 ? "<font color=red>" : "<font color=green>+";
            diff += ch.ratingMF + "</font>";
            Target oldch = Target.getOrNew(mPrevChars, ch.name);
            if (oldch != null) {
                int delta = ch.rating - oldch.rating;
                int delta2 = ch.rating2 - ch.rating;
                int useDelta = ch.appear - oldch.appear;
                int lvDelta = ch.totalLv - oldch.totalLv;
                int leaderDelta = ch.leaderCnt - oldch.leaderCnt;
                int rankdelta = rank - (mPrevChars.indexOf(oldch) + 1);
                String sDelta = ch.appear == oldch.appear ? "" : ("<font color=" + (delta >= 0 ? (delta == 0 ? "blue" : "green") : "red") + "> (" + (delta >= 0 ? "+" : "") + delta + ")</font>");
                String sDelta2 = "<font color=" + (delta2 >= 0 ? (delta2 == 0 ? "blue" : "green") : "red") + "> (" + (delta2 >= 0 ? "+" : "") + delta2 + ")</font>";
                String sUseDelta = "<font color=grey>" + (ch.appear == oldch.appear ? "" : " (+" + useDelta + ")") + "</font>";
                String sLvDelta = "<font color=grey>" + (ch.appear == oldch.appear ? "" : " (+" + lvDelta + ")") + "</font>";
                String sLeaderDelta = "<font color=grey>" + (ch.appear == oldch.appear ? "" : " (" + (leaderDelta == 0 ? "-" : "+" + leaderDelta) + ")") + "</font>";
                String sRankDelta = rankdelta == 0 ? "<font color=grey> (→)</font>" : ("<font color=" + (rankdelta >= 0 ? "red" : "green") + "> (" + (rankdelta >= 0 ? "↓" : "") + rankdelta + ")</font>");
                double avgLvData = 1.0 * ch.totalLv / ch.appear - 1.0 * oldch.totalLv / oldch.appear;
                DecimalFormat formatWithSign = new DecimalFormat("<font color=green> (+0.00)</font>;<font color=red> (-0.00)</font>");
                String sAvgLvDelta = ch.appear == oldch.appear ? "" : (Math.abs(avgLvData) < 0.01 ? "<font color=grey> (±0.00)</font>" : formatWithSign.format(avgLvData));
                sRankDelta = sRankDelta.replace('-', '↑');
                out.write("<tr><td>" + rank + sRankDelta + "</td><td>" + ch.name + "</td><td>" + ch.rating + sDelta + "</td><td>" + ch.appear + sUseDelta + "</td><td>" + ch.leaderCnt + sLeaderDelta + "</td><td>" + formatPercentage.format(1.0 * ch.leaderCnt / ch.appear) + "</td><td>" + ch.totalLv + sLvDelta + "</td><td>" + format.format(1.0 * ch.totalLv / ch.appear) + " " + sAvgLvDelta + "</td><td>" + diff + "</td><td>" + ch.rating2 + sDelta2 + "</td></tr>\r\n");
            } else {
                out.write("<tr><td>" + rank + "</td><td>" + ch.name + "</td><td>" + ch.rating + "</td><td>" + diff + "</td><td>" + ch.appear + "</td><td>" + formatPercentage.format(1.0 * ch.leaderCnt / ch.appear) + "</td><td>" + ch.totalLv + "</td><td>" + format.format(1.0 * ch.totalLv / ch.appear) + "</td><td>" + ch.leaderCnt + "</td><td>" + ch.rating2 + "</td></tr>\r\n");
            }
            out.flush();
            rank++;
        }
        out.write("</table>");
        writeHTMLFooter(out);
        out.close();

        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DROPBOX_FOLDER + "CharLVRating.html"), "UTF-8"));
        writeHTMLHeader(out);
        rank = 1;
        out.write("<h1>順位別</h1>\r\n");
        out.write("<table border=1 cellpadding=2>");
        out.write("<tr><th>順位</th><th>キャラクター＆レベル</th><th>レーティング</th><th>使用回数</th></tr>\r\n");
        for (Target p : mCharsLevel) {
            out.write("<tr><td>" + rank + "</td><td>" + p.name + "</td><td>" + p.rating + "</td><td>" + p.appear + "</td></tr>\r\n");
            p.ratingMF = rank;
            rank++;

        }
        out.write("</table>");
        out.write("<br>");
        out.write("<table border=1 cellpadding=2>");
        out.write("<h1>キャラ別</h1>\r\n");
        out.write("<tr><th>順位</th><th>キャラクター＆レベル</th><th>レーティング</th><th>使用回数</th></tr>\r\n");
        for (String s : CHARS) {
            for (int i = 1; i <= 4; ++i) {
                Target p = Target.findByName(mCharsLevel, s + i);
                if (p == null) {
                    out.write("<tr><td>---</td><td>" + (s + i)
                            + "</td><td>---</font></td><td>---</td></tr>\r\n");
                    continue;
                }
                String color = "";
                if (p.rating > 1600) {
                    color = "green";
                } else if (p.rating < 1600) {
                    color = "red";
                } else if (p.rating == 1600) {
                    color = "blue";
                }
                out.write("<tr><td>" + p.ratingMF + "</td><td>" + p.name + "</td><td><font color=" + color + ">" + p.rating + "</font></td><td>" + p.appear + "</td></tr>\r\n");
            }
            out.write("<tr><td colspan=4></td></tr>\r\n");
        }
        out.write("</table>");
        out.close();
    }

    private void writeWinningDecks() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DROPBOX_FOLDER + "Winners.html"), "UTF-8"));
        writeHTMLHeader(out);
        out.write("<table border=1 cellpadding=2>");
        out.write("<tr><th>大会名</th><th>優勝者</th><th>デッキ構成</th><th>規模</th><th>ラウンド数</th><th>開催日</th><th>主催者</th><th>大会名</th></tr>\r\n");
        String[] roundColors = new String[]{"black", "black", "black", "orange", "green", "red", "purple"};
        CountHash<String> winners = new CountHash<String>();
        final CountHash<String> kos = new CountHash<String>();
        SimpleDateFormat f = new SimpleDateFormat("yyyy年MM月dd日（E）", Locale.JAPAN);
        for (int i = mTournaments.size() - 1; i >= 0; --i) {
            Tournament t = mTournaments.get(i);
            Participant p = t.getStandingsArray().get(0);

            String bgcolor = getTourBackground(t);

            String suffix = "<td>" + t.countParticipants() + "人</td><td><font color=" + roundColors[t.round.size()] + ">" + t.round.size() + "ラウンド</font></td>";
            if (t.date.after(new GregorianCalendar(1995, 1, 1).getTime())) {
                suffix += "<td bgcolor=\"" + bgcolor + "\">" + f.format(t.date) + "</td>";
            } else {
                suffix += "<td bgcolor=\"" + bgcolor + "\"></td>";
            }

            if (t.promoter.length() > 0) {
                suffix += "<td bgcolor=\"" + bgcolor + "\"><font color=\"" + getPromoterColor(t.promoter) + "\">" + t.promoter + "</font></td>";
            } else {
                suffix += "<td></td>";
            }

            String tourlink = "<td bgcolor=\"" + bgcolor + "\"><a href=\"http://www39.atwiki.jp/gensouutage_net/?page=" + fixToKanjiExpression(t.name) + "\">" + fixToKanjiExpression(t.name) + "</a></td>";

            suffix += tourlink;

            out.write("<tr>");

            if (extractTournamentSerial(t.name) >= 35 || t.date.getTime() > 1000000) {
                out.write(tourlink + "<td>" + p.name + "</td><td><a href=\"http://www39.atwiki.jp/gensouutage_net/?page=" + t.name + " " + p.name + "\">"
                        + p.deck + "</a></td>" + suffix + "</tr>\r\n");
            } else {
                out.write(tourlink + "<td>" + p.name + "</td><td>" + p.deck + "</td>" + suffix + "</tr>\r\n");
            }
            winners.increase(p.name, 1);
            kos.increase(p.name, t.countParticipants() - 1);
        }
        out.write("</table>");
        out.write("<br><h1>優勝者カウント</h1>\r\n");
        out.write("<table border=1 cellpadding=2>");
        out.write("<tr><th>プレイヤー名</th><th>優勝回数</th><th>総撃破人数</th></tr>\r\n");
        for (Entry<String, Integer> e : winners.getSortedList(true, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return kos.get(o2.getKey()) - kos.get(o1.getKey());
            }
        })) {
            String p = e.getKey();
            out.write("<tr><td>" + p + "</td><td>" + e.getValue() + "</td><td>" + kos.get(p) + "</td></tr>\r\n");
            out.flush();
        }
        out.write("</table>");
        writeHTMLFooter(out);
        out.close();
    }

    private void writeDeckRanking() throws IOException {
        // Dropbox output
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ChocolatsNote.DROPBOX_FOLDER + "DeckRanking.html"), "UTF-8"));
        ChocolatsNote.writeHTMLHeader(out);
        out.write("<a href=\"#deckcon\">デッキ構成別</a>\r\n");
        out.write("<a name=\"decktype\"><h1>デッキタイプ別</h1>\r\n");
        out.write("<table border=1 cellpadding=2>\r\n");
        out.write("<tr><th>順位</th><th>デッキタイプ</th><th>レーティング</th><th>勝利</th><th>敗北</th><th>勝率</th><th>総試合</th></tr>\r\n");

        Target.sSortByPercentage = false;
        int i = 0;
        for (Target t : mDeckTypes) {
            int delta = 0;
            boolean hit = false;
            for (Target tt : mPrevDeckTypes) {
                if (tt.name.equalsIgnoreCase(t.name)) {
                    delta = t.rating - tt.rating;
                    if (tt.won + tt.lost + tt.drawn != t.won + t.lost + t.drawn) {
                        hit = true;
                    }
                    break;
                }
            }
            String sDelta = "";
            if (hit) {
                sDelta = "<font color=" + (delta >= 0 ? (delta == 0 ? "blue" : "green") : "red") + "> (" + (delta >= 0 ? "+" : "") + delta + ")</font>";
            }
            out.write("<tr>");
            out.write(("<td>" + (i + 1) + "</td><td>" + t.name + "</td><td>" + t.rating + sDelta + "</td><td>" + t.won + "</td><td>" + t.lost + "</td><td>" + new DecimalFormat("#00.0").format((t.won * 100.0) / (t.won + t.lost + t.drawn)) + "%" + "</td><td>" + (t.won + t.lost + t.drawn) + "</td>") + "\r\n");
            out.write("</tr>\r\n");
            ++i;
            out.flush();
        }
        out.write("</table>\r\n");
        out.write("<br>\r\n");
        out.write("註：２つ以上のデッキタイプに当てはまるデッキの判断の優先順位は以下である：<br>１・レベル３以上<br>２・２：２協力カード系統<br>３・１：１：１協力カード系統<br>４・１：１協力カード系統<br>５・レベル２軸系統<br>６・四人協力");
        out.write("<br>\r\n");
        out.write("<br>\r\n");
        i = 0;
        out.write("<a name=\"deckcon\"><h1>デッキ構成別</h1></a>\r\n");
        out.write("<table border=1 cellpadding=2>\r\n");
        out.write("<tr><th>順位</th><th>デッキ構成</th><th>レーティング</th><th>勝利</th><th>敗北</th><th>勝率</th><th>参加大会数</th></tr>\r\n");
        for (Target deck : mDecks) {
            out.write(("<tr><td>" + (i + 1) + "</td><td>" + deck.name + "</td><td>" + deck.rating + "</td><td>" + deck.won + "</td><td>" + deck.lost + "</td><td>" + new DecimalFormat("#00.0").format(deck.won * 1.0 / (deck.won + deck.lost)) + "%" + "</td><td>" + deck.appear + "</td></tr>") + "\r\n");
            ++i;
        }
        out.write("</table>\r\n");
        ChocolatsNote.writeHTMLFooter(out);
        out.close();
    }

    public ArrayList<String> listPlayers() {
        ArrayList<String> ret = new ArrayList<String>();

        for (Tournament t : mTournaments) {
            for (Integer i : t.participant.keySet()) {
                String name = t.participant.get(i).name;
                name = XMLUtils.fixName(Tournament.deleteTailUnderline(name));

                boolean hasName = false;
                for (String s : ret) {
                    if (name.equalsIgnoreCase(s)) {
                        hasName = true;
                        break;
                    }
                }
                if (!hasName) {
                    ret.add(name);
                }
            }
        }

        return ret;
    }

    private void writeResults() {
        ResultLister lister = new ResultLister();
        for (Target target : mPlayers) {
            lister.writeResult(target.name, true, false);
            lister.writeResult(target.name, true, true);
        }
        for (String s : lister.REGULAR_DECKS) {
            lister.writeResult(s, false, false);
            lister.writeResult(s, false, true);
        }
    }

    public ArrayList<String> listDecks() {
        ArrayList<String> ret = new ArrayList<String>();

        for (Tournament t : mTournaments) {
            if (isExcluded(t)) {
                continue;
            }
            for (Integer i : t.participant.keySet()) {
                if (!ret.contains(t.participant.get(i).deck)) {
                    ret.add(t.participant.get(i).deck);
                }
            }
        }

        return ret;
    }

    public ArrayList<Tournament> getParticipatedTours(String playerName) {
        ArrayList<Tournament> ret = new ArrayList<Tournament>();

        for (Tournament t : mTournaments) {
            boolean participated = false;
            for (Integer i : t.participant.keySet()) {
                if (Tournament.deleteTailUnderline(t.participant.get(i).name).equalsIgnoreCase(Tournament.deleteTailUnderline(playerName))) {
                    participated = true;
                    break;
                }
            }
            if (participated) {
                ret.add(t);
            }
        }

        return ret;
    }

    public ArrayList<Tournament> getDeckUsedTours(String deckName) {
        ArrayList<Tournament> ret = new ArrayList<Tournament>();

        for (Tournament t : mTournaments) {
            if (isExcluded(t)) {
                continue;
            }
            boolean participated = false;
            for (Integer i : t.participant.keySet()) {
                if (t.participant.get(i).deck.equalsIgnoreCase(deckName)) {
                    participated = true;
                    break;
                }
            }
            if (participated) {
                ret.add(t);
            }
        }

        return ret;
    }

    public static float extractTournamentSerial(String filename) {
        try {
            if (filename.contains("Teirei")) {
                return Integer.parseInt(filename.split(" ")[0]);
            }
            int index = filename.indexOf("回定例大会");
            if (index >= 0) {
                String core = filename.substring(1, index);
                if (core.length() == 3) {
                    core = core.replace("十", "");
                    return Integer.parseInt(replaceKanjiToArabic(core));
                } else if (core.length() == 2) {
                    if (core.charAt(0) == '十') {
                        core = core.replace("十", "1");
                    } else {
                        core = core.replace("十", "0");
                    }
                    return Integer.parseInt(replaceKanjiToArabic(core));
                } else {
                    return -1f;
                }
            }
            index = filename.indexOf("回FFO平日大会");
            if (index >= 0) {
                String core = filename.substring(1, index);
                switch (core.length()) {
                    case 3:
                        core = core.replace("十", "");
                        core = core.replace("拾", "");
                        break;
                    case 2:
                        if (core.startsWith("十") || core.startsWith("拾")) {
                            core = core.replace("十", "1");
                            core = core.replace("拾", "1");
                        } else {
                            core = core.replace("十", "0");
                            core = core.replace("拾", "0");
                        }
                        break;
                    case 1:
                        break;
                    default:
                        return -1f;
                }
                int num = Integer.parseInt(replaceKanjiToArabic(core));
                if (num <= 11) {
                    return num + 50.5f;
                } else {
                    return num + 49.6f;
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public static String replaceKanjiToArabic(String s) {
        String ret = s;
        ret = ret.replace("一", "1");
        ret = ret.replace("壱", "1");
        ret = ret.replace("二", "2");
        ret = ret.replace("弐", "2");
        ret = ret.replace("三", "3");
        ret = ret.replace("参", "3");
        ret = ret.replace("四", "4");
        ret = ret.replace("肆", "4");
        ret = ret.replace("五", "5");
        ret = ret.replace("伍", "5");
        ret = ret.replace("六", "6");
        ret = ret.replace("七", "7");
        ret = ret.replace("八", "8");
        ret = ret.replace("九", "9");
        return ret;
    }

    public static String getKanji(int digit) {
        switch (digit) {
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            case 7:
                return "七";
            case 8:
                return "八";
            case 9:
                return "九";
            default:
                return "";
        }
    }

    public static String replaceArabicToKanji(int number) {
        if (number == 10) {
            return "十";
        }
        if (number % 10 == 0) {
            return getKanji(number / 10) + "十";
        }
        if (number < 20) {
            return "十" + getKanji(number % 10);
        }
        return getKanji(number / 10) + "十" + getKanji(number % 10);
    }

    public static String fixToKanjiExpression(String filename) {
        if (!filename.contains("Teirei")) {
            return filename.replace(".xml", "");
        }
        int no = Integer.parseInt(filename.split(" ")[0]);
        return "第" + replaceArabicToKanji(no) + "回定例大会";
    }

    public static boolean isExcluded(Tournament tour) {
        if (ChocolatsNote.extractTournamentSerial(tour.name) < 38.5 && tour.date.getTime() < 1000000) {
            return true;
        }
        return false;
    }

    private long getTourTime(Tournament t) {
        long time = t.date.getTime();
        if (time > 0) {
            return time;
        }
        float sn = extractTournamentSerial(t.name);
        return sOldTourTable.get(sn);
    }

    public static void writeHTMLHeader(BufferedWriter out) throws IOException {
        out.write("<html><head><title>幻想ノ宴ネット大会ランキング</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>\r\n");
        out.write("<p>Last Update: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "</p>\r\n");
    }

    public static void writeHTMLFooter(BufferedWriter out) throws IOException {
        out.write("</body></html>\r\n");
    }

    public Target getDeckType(String deck) {
        for (String s : CHARS) {
            if (deck.contains(s + "3") || deck.contains(s + "4")) {
                return Target.getOrNew(mDeckTypes, s + "3+");
            }
        }

        if (getLevel(deck, "妖夢") == 2 && getLevel(deck, "幽々子") == 2) {
            return Target.getOrNew(mDeckTypes, "エタ斬");
        }

        if (getLevel(deck, "妖夢") == 2 && getLevel(deck, "鈴仙") == 2) {
            return Target.getOrNew(mDeckTypes, "LRE");
        }
        if (getLevel(deck, "神奈子") == 2 && getLevel(deck, "諏訪子") == 2) {
            return Target.getOrNew(mDeckTypes, "神の怒り");
        }
        if (getLevel(deck, "にとり") == 2 && getLevel(deck, "文") == 2) {
            return Target.getOrNew(mDeckTypes, "妖怪山颪");
        }
        if (getLevel(deck, "永琳") == 2 && getLevel(deck, "鈴仙") == 2) {
            return Target.getOrNew(mDeckTypes, "高草郡");
        }
        if (getLevel(deck, "レミリア") == 2 && getLevel(deck, "咲夜") == 2) {
            return Target.getOrNew(mDeckTypes, "約束");
        }
        if (getLevel(deck, "紫") == 2 && getLevel(deck, "藍") == 2) {
            return Target.getOrNew(mDeckTypes, "妖々跋扈");
        }
        if (getLevel(deck, "プリズムリバー") == 2 && getLevel(deck, "幽々子") == 2) {
            return Target.getOrNew(mDeckTypes, "花見");
        }
        if (getLevel(deck, "天子") == 2 && getLevel(deck, "衣玖") == 2) {
            return Target.getOrNew(mDeckTypes, "お仕置き");
        }
        if (getLevel(deck, "霊夢") == 2 && getLevel(deck, "早苗") == 2) {
            return Target.getOrNew(mDeckTypes, "かんなぎ");
        }
        if (getLevel(deck, "燐") == 2 && getLevel(deck, "空") == 2) {
            return Target.getOrNew(mDeckTypes, "灼熱地獄");
        }
        if (getLevel(deck, "こいし") == 2 && getLevel(deck, "さとり") == 2) {
            return Target.getOrNew(mDeckTypes, "ビジター");
        }
        if (getLevel(deck, "空") >= 1 && getLevel(deck, "神奈子") >= 1 && getLevel(deck, "諏訪子") >= 1 && getLevel(deck, "早苗") >= 1) {
            return Target.getOrNew(mDeckTypes, "守矢地獄異変");
        }
        if (getLevel(deck, "空") >= 1 && getLevel(deck, "神奈子") >= 1 && getLevel(deck, "諏訪子") >= 1) {
            return Target.getOrNew(mDeckTypes, "地獄異変（守矢除く）");
        }
        if (getLevel(deck, "早苗") >= 1 && getLevel(deck, "神奈子") >= 1 && getLevel(deck, "諏訪子") >= 1) {
            return Target.getOrNew(mDeckTypes, "守矢一家（異変除く）");
        }
        if (getLevel(deck, "輝夜") >= 1 && getLevel(deck, "永琳") >= 1 && getLevel(deck, "鈴仙") >= 1) {
            return Target.getOrNew(mDeckTypes, "隠遁／永夜異変");
        }

        if (getLevel(deck, "紫") >= 1 && getLevel(deck, "藍") >= 1 && getLevel(deck, "橙") >= 1) {
            return Target.getOrNew(mDeckTypes, "八雲一家");
        }
        if (getLevel(deck, "妖夢") >= 1 && getLevel(deck, "プリズムリバー") >= 1 && getLevel(deck, "幽々子") >= 1) {
            return Target.getOrNew(mDeckTypes, "春雪異変");
        }
        if (getLevel(deck, "レミリア") >= 1 && getLevel(deck, "咲夜") >= 1 && getLevel(deck, "パチュリー") >= 1) {
            return Target.getOrNew(mDeckTypes, "紅霧異変");
        }
        if (getLevel(deck, "魔理沙") >= 1 && getLevel(deck, "パチュリー") >= 1 && getLevel(deck, "アリス") >= 1) {
            return Target.getOrNew(mDeckTypes, "トリニティレイ");
        }

        if (getLevel(deck, "文") >= 1 && getLevel(deck, "橙") >= 1 && getLevel(deck, "魔理沙") >= 1) {
            return Target.getOrNew(mDeckTypes, "神速");
        }
        if (getLevel(deck, "咲夜") >= 1 && getLevel(deck, "永琳") >= 1 && getLevel(deck, "橙") >= 1) {
            return Target.getOrNew(mDeckTypes, "秘密鼠退治デッキアウト");
        }
        if (getLevel(deck, "輝夜") >= 1 && getLevel(deck, "レミリア") >= 1 && getLevel(deck, "幽々子") >= 1) {
            return Target.getOrNew(mDeckTypes, "カリスマ");
        }
        if (getLevel(deck, "咲夜") >= 1 && getLevel(deck, "妖夢") >= 1) {
            return Target.getOrNew(mDeckTypes, "低Ｌｖ最後の砦系統");
        }
        if (getLevel(deck, "諏訪子") >= 1 && getLevel(deck, "フランドール") >= 1) {
            return Target.getOrNew(mDeckTypes, "低Ｌｖ弾幕遊戯系統");
        }

        if (deck.contains("萃香2")) {
            return Target.getOrNew(mDeckTypes, "萃香2系統");
        }
        if (deck.contains("燐2")) {
            return Target.getOrNew(mDeckTypes, "燐2系統");
        }

        if (deck.contains("フランドール2")) {
            if (deck.split("：").length == 2) {
                return Target.getOrNew(mDeckTypes, "ＴＰフランドール系統");
            }
        }
        if (deck.contains("魔理沙2")) {
            if (deck.split("：").length == 2) {
                return Target.getOrNew(mDeckTypes, "ＴＰ魔理沙系統");
            } else {
                return Target.getOrNew(mDeckTypes, "低Ｌｖ魔理沙2系統");
            }
        }
        if (deck.startsWith("アリス2")) {
            return Target.getOrNew(mDeckTypes, "アリス2人形系統");
        }
        if (deck.contains("パチュリー2")) {
            return Target.getOrNew(mDeckTypes, "パチュリー2系統");
        }

        if (getLevel(deck, "早苗") == 1 && getLevel(deck, "神奈子") == 1 && getLevel(deck, "永琳") == 1 && getLevel(deck, "妹紅") == 1) {
            return Target.getOrNew(mDeckTypes, "風林火山");
        }
        if ((getLevel(deck, "橙") == 1 && getLevel(deck, "アリス") == 1 && getLevel(deck, "鈴仙") == 1 && getLevel(deck, "萃香") == 1)) {
            return Target.getOrNew(mDeckTypes, "大群");
        }
        if (getLevel(deck, "妹紅") == 1 && getLevel(deck, "フランドール") == 1 && getLevel(deck, "紫") == 1 && getLevel(deck, "萃香") == 1) {
            return Target.getOrNew(mDeckTypes, "EXTRAVANGAZA");
        }
        if (getLevel(deck, "天子") == 1 && getLevel(deck, "空") == 1 && getLevel(deck, "レミリア") == 1 && getLevel(deck, "萃香") == 1) {
            return Target.getOrNew(mDeckTypes, "驚天動地");
        }
        if (getLevel(deck, "空") >= 1 && getLevel(deck, "魔理沙") >= 1 && getLevel(deck, "白蓮") >= 1 && getLevel(deck, "さとり") >= 1) {
            return Target.getOrNew(mDeckTypes, "少女覚醒");
        }

        if (getLevel(deck, "早苗") >= 1 && getLevel(deck, "文") >= 1) {
            return Target.getOrNew(mDeckTypes, "低Ｌｖ疾風招来系統");
        }

        int num = deck.split("：").length;
        switch (num) {
            case 2:
                mListUpper.increase(deck, 1);
                return Target.getOrNew(mDeckTypes, "リストアップされていない2:2");
            case 3:
                mListUpper.increase(deck, 1);
                return Target.getOrNew(mDeckTypes, "リストアップされていない2:1:1");
            case 4:
                mListUpper.increase(deck, 1);
                return Target.getOrNew(mDeckTypes, "リストアップされていないオール1");
        }

        return Target.getOrNew(mDeckTypes, "記録なし");
    }
    public CountHash<String> mListUpper = new CountHash<String>();

    private int getLevel(String deck, String c) {
        if (deck.indexOf(c) < 0) {
            return 0;
        }
        return Integer.parseInt(deck.substring(deck.indexOf(c) + c.length(), deck.indexOf(c) + c.length() + 1));
    }
    public static final HashMap<Float, Long> sOldTourTable;

    static {

        sOldTourTable = new HashMap<Float, Long>();
        sOldTourTable.put(-1f, 1266179400000L);
        sOldTourTable.put(14f, 1219523400000L);
        sOldTourTable.put(15f, 1220733000000L);
        sOldTourTable.put(16f, 1221942600000L);
        sOldTourTable.put(17f, 1223152200000L);
        sOldTourTable.put(18f, 1224361800000L);
        sOldTourTable.put(19f, 1225571400000L);
        sOldTourTable.put(20f, 1226781000000L);
        sOldTourTable.put(21f, 1227990600000L);
        sOldTourTable.put(22f, 1229200200000L);
        sOldTourTable.put(23f, 1230409800000L);
        sOldTourTable.put(24f, 1232224200000L);
        sOldTourTable.put(25f, 1233433800000L);
        sOldTourTable.put(26f, 1234643400000L);
        sOldTourTable.put(27f, 1235853000000L);
        sOldTourTable.put(28f, 1237062600000L);
        sOldTourTable.put(29f, 1238272200000L);
        sOldTourTable.put(30f, 1239481800000L);
        sOldTourTable.put(31f, 1240691400000L);
        sOldTourTable.put(32f, 1241901000000L);
        sOldTourTable.put(33f, 1243110600000L);
        sOldTourTable.put(34f, 1244320200000L);
        sOldTourTable.put(35f, 1245529800000L);
        sOldTourTable.put(36f, 1246739400000L);
        sOldTourTable.put(37f, 1247949000000L);
        sOldTourTable.put(38f, 1249158600000L);
        sOldTourTable.put(39f, 1250368200000L);
        sOldTourTable.put(40f, 1251577800000L);
        sOldTourTable.put(41f, 1252787400000L);
        sOldTourTable.put(42f, 1253392200000L);
        sOldTourTable.put(43f, 1253997000000L);
        sOldTourTable.put(44f, 1255206600000L);
        sOldTourTable.put(45f, 1255811400000L);
        sOldTourTable.put(46f, 1256416200000L);
        sOldTourTable.put(47f, 1257625800000L);
        sOldTourTable.put(48f, 1258230600000L);
        sOldTourTable.put(49f, 1258835400000L);
        sOldTourTable.put(50f, 1260045000000L);
        sOldTourTable.put(51f, 1260649800000L);
        sOldTourTable.put(52f, 1261254600000L);
        sOldTourTable.put(53f, 1263069000000L);
        sOldTourTable.put(54f, 1263673800000L);
        sOldTourTable.put(55f, 1264278600000L);
        sOldTourTable.put(56f, 1265488200000L);
        sOldTourTable.put(57f, 1266093000000L);
        sOldTourTable.put(58f, 1266697800000L);
        sOldTourTable.put(59f, 1267907400000L);
        sOldTourTable.put(60f, 1268512200000L);
        sOldTourTable.put(61f, 1269117000000L);
        sOldTourTable.put(62f, 1270326600000L);
        sOldTourTable.put(63f, 1270931400000L);
        sOldTourTable.put(64f, 1271536200000L);
        sOldTourTable.put(51.5f, 1262809800000L);
        sOldTourTable.put(52.5f, 1263414600000L);
        sOldTourTable.put(53.5f, 1264019400000L);
        sOldTourTable.put(54.5f, 1264624200000L);
        sOldTourTable.put(55.5f, 1265229000000L);
        sOldTourTable.put(56.5f, 1265833800000L);
        sOldTourTable.put(57.5f, 1267043400000L);
        sOldTourTable.put(58.5f, 1267648200000L);
        sOldTourTable.put(59.5f, 1268253000000L);
        sOldTourTable.put(60.5f, 1268857800000L);
        sOldTourTable.put(61.5f, 1269462600000L);
        sOldTourTable.put(61.6f, 1270067400000L);
        sOldTourTable.put(62.6f, 1270672200000L);
        sOldTourTable.put(63.6f, 1271277000000L);
        sOldTourTable.put(64.6f, 1271881800000L);
    }

    private static String getTourBackground(Tournament t) {
        if (t.name.contains("定例大会")) {
            return "#F0FFF0";
        }
        if (t.name.contains("平日大会") || t.name.contains("小週末")) {
            return "#FFFFF0";
        }
        return "#FFFFFF";
    }

    private String getPromoterColor(String promoter) {
        if (promoter.startsWith("keychi")) {
            return "#007000";
        }
        if (promoter.startsWith("Urakagi")) {
            return "#A000A0";
        }
        if (promoter.startsWith("Spirit_K")) {
            return "#000070";
        }
        if (promoter.startsWith("moruga")) {
            return "#909000";
        }
        if (promoter.startsWith("shinonome")) {
            return "#700000";
        }
        if (promoter.startsWith("hai")) {
            return "#009090";
        }
        if (promoter.startsWith("seppei")) {
            return "#AAAAAA";
        }
        return "#000000";
    }

    class ResultLister {

        private final String WIKI_PREFIX = "http://www39.atwiki.jp/gensouutage_net/?page=";

        private void writeResult(String deck, boolean matchPlayer, boolean timeline) {
            exec(deck, matchPlayer, timeline ? mDateComparator : mScoreComparator);
        }

        private void exec(String request, boolean matchPlayer, Comparator<DataLine> comparator) {

            BufferedWriter out = null;
            try {
                String filename = request.replace('?', 'Q').replace('*', 'S').replace('|', 'I').replace('\\', 'B').replace('/', 'L');
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ChocolatsNote.DROPBOX_FOLDER + filename + (comparator == mDateComparator ? "_timeline" : "") + ".html"), "UTF-8"));
                out.write("<html><head><title>" + request + "のリザルト</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
                        + RESULT_HEADER_CSS
                        + "</head><body>\r\n");
                out.write("<p>Last Update: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "</p>\r\n");
                if (comparator == mScoreComparator) {
                    out.write("<a href=\"#stat\">統計結果へ</a><br><a href=\"" + request + "_timeline.html\">タイムライン</a><br><h2>大会結果</h2>\r\n");
                } else {
                    out.write("<a href=\"#stat\">統計結果へ</a><br><a href=\"" + request + ".html\">成績順</a><br><h2>大会結果</h2>\r\n");
                }
                out.write("<table border=1 cellpadding=4 style=\"text-align:center;\"><tbody>\r\n");
                out.write("<tr><th>大会名</th><th>プレイヤー</th><th>デッキ</th><th>勝-負</th><th>開催日</th></tr>\r\n");

                //結果統計
                int wins = 0;
                int rounds = 0;

                HashMap<String, WinRound> bd = new HashMap<String, WinRound>(); //デッキ別用
                HashMap<String, WinRound> bp = new HashMap<String, WinRound>(); //プレイヤー別用
                DecimalFormat numf = new DecimalFormat("#00.00%");

                Pattern pattern = Pattern.compile(request);
                ArrayList<DataLine> data = new ArrayList<DataLine>();

                for (Tournament tour : mTournaments) {
                    for (Participant par : tour.getStandingsArray()) {
                        if (pattern.matcher(matchPlayer ? par.name : par.deck).find()) {
                            DataLine line = new DataLine();
                            line.date = tour.date;
                            line.deck = par.deck;
                            line.player = par.name;
                            line.round = tour.round.size();
                            line.tour = tour.name;
                            line.win = par.getWins();
                            data.add(line);
                        }
                    }
                    out.flush();
                }
                Collections.sort(data, comparator);

                /* For graph, change all to 5 games by winning percentage.
                For example, one 3-1 game will become 0.75 4-1 and 0.25 3-2 game. */
                // 0 for 0-5, 5 for 5-0
                double[] distribution = new double[]{0, 0, 0, 0, 0, 0};

                for (DataLine line : data) {
                    String datestring = new SimpleDateFormat("yyyy年MM月dd日（E）", Locale.JAPAN).format(line.date);
                    String background = "#FFFFFF";
                    switch (line.round - line.win) {
                        case 0:
                            background = "#FFFFD0";
                            break;
                        case 1:
                            background = "#E5FFE5";
                            break;
                    }
                    if (line.win == 0) {
                        background = "#FFE5E5";
                    } else if (line.win == 1) {
                        background = "#E5E5FF";
                    }
                    out.write("<tr bgcolor=" + background + ">");
                    out.write("<td><a href=" + WIKI_PREFIX + line.tour + ">" + line.tour + "</a></td><td>" + line.player + "</td><td>" + line.deck + "</td><td>" + line.win + "-" + (line.round - line.win) + "</td><td>" + datestring + "</td>");
                    out.write("</tr>");
                    wins += line.win;
                    rounds += line.round;
                    if (!bp.containsKey(line.player)) {
                        WinRound wr = new WinRound();
                        bp.put(line.player, wr);
                    }
                    bp.get(line.player).win += line.win;
                    bp.get(line.player).round += line.round;
                    if (!bd.containsKey(line.deck)) {
                        WinRound wr = new WinRound();
                        bd.put(line.deck, wr);
                    }
                    bd.get(line.deck).win += line.win;
                    bd.get(line.deck).round += line.round;

                    // Graph
                    if (line.round != 5) {
                        double wp = line.win * 1.0 / line.round;
                        if (wp == 1.0) {
                            distribution[5] += 1;
                        } else {
                            int low = (int) (wp * 5);
                            distribution = calcGraph(distribution, low + 1, low, wp);
                        }
                    } else {
                        distribution[line.win] += 1;
                    }
                }
                out.write("</tbody></table>");

                DecimalFormat tdf = new DecimalFormat("###0.0");
                DecimalFormat pdf = new DecimalFormat("##0%");
                double sum = 0.0;
                for (int i = 0; i < 6; ++i) {
                    sum += distribution[i];
                }

                // Graph
                out.write("<p><table width=530 class=graph cellspacing=6 cellpadding=0>\r\n");
                out.write("<tbody>");
                for (int i = 5; i >= 0; --i) {
                    out.write("<tr><td width=40>" + i + "-" + (5 - i) + "</td><td width=400 class=\"bar bar" + i + "\"><div style=\"width: "
                            + ((distribution[i] / sum) * 100) + "%\"></div>"
                            + tdf.format(distribution[i]) + "</td><td>" + pdf.format(distribution[i] / sum) + "</td></tr>\r\n");
                }
                out.write("</tbody></table></p>\r\n");

                out.write("<h2><a name=\"stat\">統計結果</a></h2>");
                out.write("<h3>" + rounds + "戦 " + wins + "勝 " + (rounds - wins) + "負 (" + numf.format(wins * 1.0 / rounds) + ")</h3>");
                // プレイヤー別
                ArrayList<Entry<String, WinRound>> bpa = new ArrayList<Entry<String, WinRound>>();
                bpa.addAll(bp.entrySet());
                Collections.sort(bpa, sort_result);
                out.write("<h3>プレイヤー別</h3><table border=1 cellpadding=4 style=\"text-align:center;\"><tbody>");
                out.write("<tr><th>プレイヤー</th><th>総R</th><th>勝敗</th><th>勝率</th></tr>");
                for (Entry<String, WinRound> e : bpa) {
                    WinRound p = e.getValue();
                    double winper = p.win * 1.0 / p.round;
                    int red = 200 + (int) (55 * winper);
                    int green = 200 + (int) (55 * winper);
                    int blue = 200 + (int) (55 * winper);
                    String bgcolor = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
                    out.write("<tr bgcolor=" + bgcolor + "><td>" + e.getKey() + "</td><td>" + p.round + "</td><td>" + p.win + "-" + (p.round - p.win) + "</td><td>" + numf.format(winper) + "</td></tr>");
                }
                out.write("</tbody></table>");
                // 構成別
                ArrayList<Entry<String, WinRound>> bda = new ArrayList<Entry<String, WinRound>>();
                bda.addAll(bd.entrySet());
                Collections.sort(bda, sort_result);
                out.write("<h3>デッキ構成別</h3><table border=1 cellpadding=4 style=\"text-align:center;\"><tbody>");
                out.write("<tr><th>構成</th><th>総R</th><th>勝敗</th><th>勝率</th></tr>");
                for (Entry<String, WinRound> e : bda) {
                    WinRound p = e.getValue();
                    double winper = p.win * 1.0 / p.round;
                    int red = 200 + (int) (55 * winper);
                    int green = 200 + (int) (55 * winper);
                    int blue = 200 + (int) (55 * winper);
                    String bgcolor = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
                    out.write("<tr bgcolor=" + bgcolor + "><td>" + e.getKey() + "</td><td>" + p.round + "</td><td>" + p.win + "-" + (p.round - p.win) + "</td><td>" + numf.format(winper) + "</td></tr>");
                }
                out.write("</tbody></table>");
                out.write("</body></html>");
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(ChocolatsNote.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ChocolatsNote.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        private double[] calcGraph(double[] distribution, int high, int low, double wp) {
            double a = high * 0.2;
            double b = low * 0.2;
            double x = (b - wp) / (b - a);
            double y = (wp - a) / (b - a);
            distribution[high] += x;
            distribution[low] += y;
            return distribution;
        }
        Comparator<Entry<String, WinRound>> sort_result = new Comparator<Entry<String, WinRound>>() {

            @Override
            public int compare(Entry<String, WinRound> o1, Entry<String, WinRound> o2) {
                int comp = o2.getValue().round - o1.getValue().round;
                if (comp != 0) {
                    return comp;
                }
                return o2.getValue().win - o1.getValue().win;
            }
        };
        Comparator<DataLine> mScoreComparator = new Comparator<DataLine>() {

            @Override
            public int compare(DataLine o1, DataLine o2) {
                int comp = (int) (100.0 * o2.win / o2.round - 100.0 * o1.win / o1.round);
                if (comp != 0) {
                    return comp;
                }
                if (o1.win == 0) {
                    comp = (o1.round - o1.win) - (o2.round - o2.win);
                } else {
                    comp = o2.win - o1.win;
                }
                if (comp != 0) {
                    return comp;
                }
                return o2.date.compareTo(o1.date);
            }
        };
        Comparator<DataLine> mDateComparator = new Comparator<DataLine>() {

            @Override
            public int compare(DataLine o1, DataLine o2) {
                return o2.date.compareTo(o1.date);
            }
        };

        class DataLine {

            String tour;
            String player;
            String deck;
            Date date;
            int win;
            int round;
        }

        class WinRound {

            int win;
            int round;
        }
        private final String[] REGULAR_DECKS = new String[]{
            "霊夢[34]", "魔理沙[34]", "咲夜[34]", "妖夢[34]", "紫[34]", "アリス[34]", "レミリア[34]", "幽々子[34]", "フランドール[34]", "パチュリー[34]",
            "美鈴[34]", "輝夜[34]", "永琳[34]", "鈴仙[34]", "藍[34]", "橙[34]", "プリズムリバー[34]", "妹紅[34]", "慧音[34]", "萃香[34]",
            "諏訪子[34]", "神奈子[34]", "早苗[34]", "文[34]", "小町[34]", "にとり[34]", "天子[34]", "衣玖[34]", "空[34]", "燐[34]",
            "こいし[34]", "さとり[34]", "白蓮[34]", "星[34]", "ぬえ[34]", "小傘[34]", "ナズーリン[34]", "水蜜[34]"};
    }
    private final String RESULT_HEADER_CSS =
            "<style type=\"text/css\">      .graph { background-color: #C8C8C8; border: solid 1px black;      }"
            + ".graph td { font-family: verdana, arial, sans serif;      }      "
            + ".graph thead th { border-bottom: double 3px black; font-family: verdana, arial, sans serif; padding: 1em;      }"
            + ".graph tfoot td { border-top: solid 1px #999999; font-size: x-small; text-align: center; padding: 0.5em; color: #666666;      }"
            + ".bar { background-color: white; text-align: right; padding-right: 0.5em; width: 400px;      }"
            + ".bar div { text-align: right; color: white; float: left; padding-top: 0; height: 1em; border-left: solid 1px #CCCCCC;}"
            + ".bar5 div { background-color: #FFFFD0; border-top: solid 2px #FFFFE0; border-bottom: solid 2px #C0C099; border-right: solid 3px #E0E090}"
            + ".bar4 div { background-color: #D5FFD5; border-top: solid 2px #F5FFF5; border-bottom: solid 2px #C5DFC5; border-right: solid 3px #C5EFC5}"
            + ".bar3 div { background-color: #90D0D0; border-top: solid 2px #A0E0E0; border-bottom: solid 2px #70B0B0; border-right: solid 3px #80C0C0}"
            + ".bar2 div { background-color: #C0A8EE; border-top: solid 2px #D0B8FE; border-bottom: solid 2px #A088CE; border-right: solid 3px #B098DE}"
            + ".bar1 div { background-color: #D5D5FF; border-top: solid 2px #E5E5FF; border-bottom: solid 2px #B5B5E0; border-right: solid 3px #A5A5F0}"
            + ".bar0 div { background-color: #FFA5A5; border-top: solid 2px #FFB5B5; border-bottom: solid 2px #CF7575; border-right: solid 3px #D08585}"
            + "</style>";
}
