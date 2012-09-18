/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fforeaplyanalyzer;

import fforeaplyanalyzer.Main.MyDeck.Stats;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Romulus
 */
public class Main {

    static final String PATH = ".";

    public static void main(String[] args) throws Exception {
        File dir = new File(PATH);
        File[] files = dir.listFiles();
        int count = 0;
        int nocount = 0;

        ArrayList<MyDeck> decks = new ArrayList<MyDeck>();
        SortableHashMap<String, ArrayList<DeckResult>> oppdecks = new SortableHashMap<String, ArrayList<DeckResult>>();
        SortableHashMap<String, Stats> opps = new SortableHashMap<String, Stats>();

        for (File f : files) {
            try {
                ++count;
                if (f.isFile() && f.getName().endsWith("txt")) {
                    String myname, oppname;
                    int dice = 0;  //0: No game; 1: First; -1: Last
                    int result = 0; //0: No game; 1: Wom ,n; -1: Lost
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "Unicode"));

                    String line = in.readLine();
                    String deck;
                    String oppdeck;
                    try {
                        myname = line.split("//")[0];
                        deck = line.split("//")[2];

                        line = in.readLine();
                        oppname = line.split("//")[0];
                        oppdeck = line.split("//")[2];
                    } catch (Exception e) {
                        continue;
                    }
                    if (oppname.equals(myname)) {
                        ++nocount;
                        continue;
                    }

                    deck = deck.replace("キモけーね", "上白沢 慧音");
                    deck = deck.replace("慧音（人間）", "上白沢 慧音");
                    deck = deck.replace("慧音（妖怪）", "上白沢 慧音");

                    oppdeck = oppdeck.replace("キモけーね", "上白沢 慧音");
                    oppdeck = oppdeck.replace("慧音（人間）", "上白沢 慧音");
                    oppdeck = oppdeck.replace("慧音（妖怪）", "上白沢 慧音");

                    oppname = fixName(oppname);

                    MyDeck myDeck = null;
                    boolean existed = false;
                    for (MyDeck d : decks) {
                        if (d.deckType.charstring.equals(deck)) {
                            existed = true;
                            myDeck = d;
                            break;
                        }
                    }

                    if (!existed) {
                        myDeck = new MyDeck(deck);
                        decks.add(myDeck);
                    }

                    if (!opps.containsKey(oppname)) {
                        opps.put(oppname, new Stats());
                    }

                    if (!oppdecks.containsKey(oppdeck)) {
                        oppdecks.put(oppdeck, new ArrayList<DeckResult>());
                    }


                    int atkhp = 999;
                    int icphp = 999;
                    int turn = -1;
                    int format = 2; //2: New, 3: Old
                    int whosturn = 0; //1: My, -1: Opp
                    int conceder = 0; //1: Opp concede, -1: Ich concede

                    while (line != null) {
                        if (line.startsWith("賽が投げられて、" + myname + "の先攻になった。")) {
                            dice = 1;
                        }
                        if (line.startsWith("賽が投げられて、" + oppname + "の先攻になった。")) {
                            dice = -1;
                        }

                        if (turn < 0 && line.contains("場に出しました")) {
                            if (line.contains(myname)) {
                                dice = 1;
                            } else {
                                dice = -1;
                            }
                        }

                        if (line.startsWith("Turn")) {
                            if (line.startsWith("Turn 2 - " + myname)) {
                                dice = -1;
                            } else if (line.startsWith("Turn 3 - " + myname)) {
                                dice = 1;
                            } else {
                                format = line.split("//").length;
                                if (format == 2) {
                                    //New Format
                                    turn = Integer.parseInt(line.split("-")[0].replace("Turn", "").trim());
                                } else if (format == 3) {
                                    //Old Format
                                    turn = Integer.parseInt(line.split("//")[0].replace("Turn", "").trim());
                                } else {
                                    assert false;
                                }
                            }

                            if (dice > 0) {
                                whosturn = (turn % 2 == 1) ? 1 : -1;
                            } else if (dice < 0) {
                                whosturn = (turn % 2 == 0) ? 1 : -1;
                            } else {
                                line = in.readLine();
                                continue;
                            }

                            if (format == 2) {
                                atkhp = Integer.parseInt(line.split("//")[1].split("\\(")[0].substring(2));
                                String subline = line.split("//")[1].split(":")[1];
                                icphp = Integer.parseInt(subline.substring(0, subline.indexOf(')')));
                            } else {
                                String[] s = line.split("//");
                                int myhp = Integer.parseInt(s[1].substring(s[1].indexOf("体力") + 2, s[1].indexOf("呪力")).trim());
                                int opphp = Integer.parseInt(s[2].substring(s[2].indexOf("体力") + 2, s[2].indexOf("呪力")).trim());
                                if (whosturn == 1) {
                                    atkhp = myhp;
                                    icphp = opphp;
                                } else if (whosturn == -1) {
                                    atkhp = opphp;
                                    icphp = myhp;
                                }
                            }


                        } // if starts with "Turn"
                        else if (line.startsWith("結果：")) {
                            //New Format combat result
                            line = line.replace("回避 :", "Dmg 0 :");
                            line = line.replace(": 回避", ": 0 Dmg");
                            line = line.replace("=== :", "Dmg 0 :");
                            line = line.replace(": ===", ": 0 Dmg");
                            line = line.replace("dmg", "Dmg");
                            String res = line.split("Dmg")[1];
                            int atkdmg = Integer.parseInt(res.split(":")[0].trim());
                            int icpdmg = Integer.parseInt(res.split(":")[1].trim());

                            if (icpdmg >= icphp) {
                                result = (whosturn == 1) ? 1 : -1;
                                break;
                            }
                            if (atkdmg >= atkhp) {
                                result = (whosturn == -1) ? 1 : -1;
                                break;
                            }
                        } else if (line.startsWith("戦闘の結果")) {
                            line = line.replace("。", "、");
                            line = line.replace("回避", "、0点のダメージ");
                            //Old Format combat result
                            int atkdmg = 0;
                            int icpdmg = 0;
                            String[] s = line.split("、");
                            switch (s.length) {
                                case 5:
                                    //Normal
                                    atkdmg = Integer.parseInt(s[2].substring(0, s[2].indexOf("点の")));
                                    icpdmg = Integer.parseInt(s[4].substring(0, s[4].indexOf("点の")));
                                    break;
                                case 2:
                                    //Through
                                    if (whosturn == 1) {
                                        if (s[1].startsWith(myname)) {
                                            break;
                                        }
                                        icpdmg = Integer.parseInt(s[1].substring(oppname.length() + 1, s[1].indexOf("点の")));
                                    } else if (whosturn == -1) {
                                        if (s[1].startsWith(oppname)) {
                                            break;
                                        }
                                        icpdmg = Integer.parseInt(s[1].substring(myname.length() + 1, s[1].indexOf("点の")));
                                    }
                                    break;
                            }
                            if (icpdmg >= icphp) {
                                result = (whosturn == 1) ? 1 : -1;
                                break;
                            }
                            if (atkdmg >= atkhp) {
                                result = (whosturn == -1) ? 1 : -1;
                                break;
                            }
                        } else if (line.contains("の体力が－")) {
                            String[] s = line.split("\\(");
                            int hp = Integer.parseInt(s[s.length - 1].substring(0, s[s.length - 1].lastIndexOf(')')).trim());
                            if (line.contains(myname)) {
                                if (whosturn == 1) {
                                    atkhp = hp;
                                } else {
                                    icphp = hp;
                                }
                            } else if (line.contains(oppname)) {
                                if (whosturn == -1) {
                                    atkhp = hp;
                                } else {
                                    icphp = hp;
                                }
                            }
                            if (hp <= 0) {
                                if (line.contains(myname)) {
                                    result = -1;
                                } else if (line.contains(oppname)) {
                                    result = 1;
                                }
                            }
                        } else if (line.contains("の体力は今")) {
                            String[] s = line.split("の体力は今");
                            int hp = Integer.parseInt(s[s.length - 1].substring(0, s[s.length - 1].lastIndexOf('(')).trim());
                            if (line.contains(myname)) {
                                if (whosturn == 1) {
                                    atkhp = hp;
                                } else {
                                    icphp = hp;
                                }
                            } else if (line.contains(oppname)) {
                                if (whosturn == -1) {
                                    atkhp = hp;
                                } else {
                                    icphp = hp;
                                }
                            }
                            if (hp <= 0) {
                                if (line.contains(myname)) {
                                    result = -1;
                                } else if (line.contains(oppname)) {
                                    result = 1;
                                }
                            }

                        } // end of result checking

                        //Concedes
                        if (conceder == 0 && (line.contains("参りました") || line.contains("まいりました") || (line.contains("投了") && !line.contains("？")) || line.contains("負け") || line.contains("まけ") || line.contains("無理") || line.contains("むり") || line.contains("降参")) || line.contains("輸") || line.contains("敗") || line.contains("白旗") || line.contains("詰み") || line.contains("詰んだ") || line.contains("無解")) {
                            if (line.contains(myname + ":")) {
                                conceder = -1;
                            } else if (line.contains(oppname + ":")) {
                                conceder = 1;
                            }
                        }

                        line = in.readLine();
                    } // of reading lines

                    if (result == 0 && conceder != 0) {
                        result = conceder;
                    }

                    //Set result
                    DeckResult deckresult = new DeckResult();
                    deckresult.dice = dice;
                    deckresult.result = result;

                    decks.get(decks.indexOf(myDeck)).addResult(oppname, oppdeck, deckresult);
                    oppdecks.get(oppdeck).add(deckresult);
                    opps.get(oppname).push(deckresult);

                    in.close();
                    dice = 0;
                    oppname = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        } // end of file lists

        //System.out.println(count);

        Collections.sort(decks);

        StringBuilder t = new StringBuilder();
        for (MyDeck d : decks) {
            d.stats.writeResult(d.getDisplayName(), t);

        /*
        //Opp
        for (DeckType op : d.oppDecksMerged.keySet()) {
        Stats s = new Stats();
        for (DeckResult g : d.oppDecksMerged.get(op)) {
        s.push(g);
        }
        first = s.getFirsts();
        last = s.getLasts();
        firstwon = s.firstWon;
        firstlost = s.firstLost;
        lastwon = s.lastWon;
        lastlost = s.lastLost;
        firstpercentage = firstwon + firstlost == 0 ? 0.0 : (firstwon * 10000 / (firstwon + firstlost)) / 100.0;
        lastpercentage = lastwon + lastlost == 0 ? 0.0 : (lastwon * 10000 / (lastwon + lastlost)) / 100.0;
        totalpercentage = lastwon + lastlost + firstwon + firstlost == 0 ? 0.0 : ((lastwon + firstwon) * 10000 / (lastwon + lastlost + firstwon + firstlost)) / 100.0;
        if (first+last > 4) {
        t.append("  "+op.getDisplayName() + "\n");
        t.append("  "+first + " firsts, " + firstwon + "-" + firstlost + " (" + firstpercentage + "%)\n");
        t.append("  "+last + " lasts, " + lastwon + "-" + lastlost + " (" + lastpercentage + "%)\n");
        t.append("  "+(first + last) + " totals, " + (lastwon + firstwon) + "-" + (lastlost + firstlost) + " (" + (totalpercentage) + "%)\n");
        t.append("\n");
        }
        }
         */
        }
        t.append("\n");


        ArrayList<String> entries2 = oppdecks.getSortedKeys();

        StringBuilder t_oppdeck = new StringBuilder();

        int p_first = 0;
        int p_last = 0;
        int p_firstwon = 0;
        int p_firstlost = 0;
        int p_lastwon = 0;
        int p_lastlost = 0;
        for (String m : entries2) {
            int first = 0;
            int last = 0;
            int firstwon = 0;
            int firstlost = 0;
            int lastwon = 0;
            int lastlost = 0;
            for (DeckResult r : oppdecks.get(m)) {
                if (r.dice > 0) {
                    first++;
                    if (r.result > 0) {
                        firstwon++;
                    } else if (r.result < 0) {
                        firstlost++;
                    }
                } else if (r.dice < 0) {
                    last++;
                    if (r.result > 0) {
                        lastwon++;
                    } else if (r.result < 0) {
                        lastlost++;
                    }
                }
            }
            double firstpercentage = firstwon + firstlost == 0 ? 0.0 : (firstwon * 10000 / (firstwon + firstlost)) / 100.0;
            double lastpercentage = lastwon + lastlost == 0 ? 0.0 : (lastwon * 10000 / (lastwon + lastlost)) / 100.0;
            double totalpercentage = lastwon + lastlost + firstwon + firstlost == 0 ? 0.0 : ((lastwon + firstwon) * 10000 / (lastwon + lastlost + firstwon + firstlost)) / 100.0;
            t_oppdeck.append(m + "\n");
            t_oppdeck.append(first + " firsts, " + firstwon + "-" + firstlost + " (" + firstpercentage + "%)\n");
            t_oppdeck.append(last + " lasts, " + lastwon + "-" + lastlost + " (" + lastpercentage + "%)\n");
            t_oppdeck.append((first + last) + " totals, " + (lastwon + firstwon) + "-" + (lastlost + firstlost) + " (" + (totalpercentage) + "%)\n");
            t_oppdeck.append("\n");

            p_first += first;
            p_last += last;
            p_firstwon += firstwon;
            p_firstlost += firstlost;
            p_lastwon += lastwon;
            p_lastlost += lastlost;
        }
        t_oppdeck.append("\n");

        StringBuilder t_opp = new StringBuilder();
        for (String oppname : opps.getSortedKeys()) {
            opps.get(oppname).writeResult(oppname, t_opp);
        }

        double p_firstpercentage = p_firstwon + p_firstlost == 0 ? 0.0 : (p_firstwon * 10000 / (p_firstwon + p_firstlost)) / 100.0;
        double p_lastpercentage = p_lastwon + p_lastlost == 0 ? 0.0 : (p_lastwon * 10000 / (p_lastwon + p_lastlost)) / 100.0;
        double p_totalpercantage = p_lastwon + p_lastlost + p_firstwon + p_firstlost == 0 ? 0.0 : ((p_lastwon + p_firstwon) * 10000 / (p_lastwon + p_lastlost + p_firstwon + p_firstlost)) / 100.0;
        t.append("Player total:\n");
        t.append("Replay Count=" + count + "\n");
        t.append("Illegal Replay Count=" + nocount + "\n");
        t.append(p_first + " firsts, " + p_firstwon + "-" + p_firstlost + " (" + p_firstpercentage + "%)\n");
        t.append(p_last + " lasts, " + p_lastwon + "-" + p_lastlost + " (" + p_lastpercentage + "%)\n");
        t.append((p_first + p_last) + " totals, " + (p_lastwon + p_firstwon) + "-" + (p_lastlost + p_firstlost) + " (" + (p_totalpercantage) + "%)\n");

        new NewJFrame(t.toString(), t_oppdeck.toString(), t_opp.toString()).setVisible(true);

    }

    static String fixName(String name) {
        if (name.equals("蟻酸泥棒") || name.equals("うらきー") || name.equals("HCOOH_Linux") || name.equals("裏鍵（外出中）")) {
            return "裏鍵";
        }
        if (name.equals("あげ") || name.equals("火世院") || name.equals("天条院揚") || name.equals("揚") || name.equals("白") || name.equals("tenjoin") || name.equals("リク") || name.equals("天条院")) {
            return "アゲ";
        }
        if (name.equals("ｸﾗｽﾄ") || name.equals("デンス")) {
            return "クラスト";
        }
        if (name.equals("tega-ru")) {
            return "テガーる";
        }
        if (name.equals("Tanoue")) {
            return "田之上";
        }
        if (name.equals("biku")) {
            return "びく";
        }
        if (name.equals("kkk")) {
            return "sak";
        }
        if (name.equals("nanasi_F") || name.equals("nanashi_F")) {
            return "F";
        }
        if (name.equals("弥十郎") || name.equals("emanon") || name.equals("yaiti")) {
            return "弥一郎";
        }
        if (name.equals("遥奈")) {
            return "観月＠ＣＶ：風音様";
        }
        if (name.equals("瀬智") || name.equals("瀬智・如月") || name.equals("sechi")) {
            return "如月";
        }
        if (name.equals("remiril")) {
            return "れみりる優曇華イナバ";
        }
        if (name.equals("手裏剣")) {
            return "アロンダイト";
        }
        if (name.equals("kotti2")) {
            return "こっち２";
        }
        if (name.equals("ProfileXP")) {
            return "XP";
        }
        if (name.equals("傭兵")) {
            return "kokurei";
        }
        if (name.equals("kb_")) {
            return "kb";
        }
        if (name.equals("南斗紅鶴拳ユダ") || name.equals("みおーん"))
            return "sanagi";
        if (name.equals("AtWikkii"))
            return "ウィッキー";
        if (name.equals("High"))
            return "Faith";
        if (name.equals("metalzoika"))
            return "zoika";
        if (name.startsWith("虚人(")) {
            return "ノナメクス先生";
        }
        return name;
    }

    static class DeckResult {

        int dice = 0;
        int result = 0;
    }

    static class DeckType {

        String user;
        String charstring;
        String[] chars;

        DeckType(String user, String tooltext) {
            this.user = user;
            charstring = tooltext;
            chars = tooltext.split("-");
        }

        String getDisplayName() {
            String ret = "";
            String temp = "";
            ArrayList<String> done = new ArrayList<String>();

            for (int k = 0; k < 4; ++k) {
                int cnt = 1;
                if (done.contains(chars[k])) {
                    continue;
                }
                temp = chars[k];
                done.add(temp);
                for (int i = k + 1; i < 4; ++i) {
                    if (chars[i].equals(temp)) {
                        ++cnt;
                    }
                }
                ret += ((temp.split(" ").length == 2) ? temp.split(" ")[1] : temp) + cnt + "-";
            }

            return ret.substring(0, ret.length() - 1);
        }
    }

    static class MyDeck implements Comparable<MyDeck> {

        DeckType deckType;
        HashMap<DeckType, ArrayList<DeckResult>> oppDecks;
        HashMap<DeckType, ArrayList<DeckResult>> oppDecksMerged;
        int resultCount = 0;
        Stats stats = new Stats();

        MyDeck(String deckstring) {
            deckType = new DeckType("ICH", deckstring);
            oppDecks = new HashMap<DeckType, ArrayList<Main.DeckResult>>();
            oppDecksMerged = new HashMap<DeckType, ArrayList<Main.DeckResult>>();
        }

        void addResult(String oppname, String oppdeckstring, DeckResult result) {
            DeckType index = null;
            for (DeckType d : oppDecks.keySet()) {
                if (d.user.equals(oppname) && d.charstring.equals(oppdeckstring)) {
                    index = d;
                    break;
                }
            }
            if (index == null) {
                ArrayList<DeckResult> temp = new ArrayList<DeckResult>();
                temp.add(result);
                oppDecks.put(new DeckType(oppname, oppdeckstring), temp);
            } else {
                oppDecks.get(index).add(result);
            }
            stats.push(result);
            ++resultCount;

        /*
        for (DeckType d : oppDecksMerged.keySet()) {
        if (d.charstring.equals(oppdeckstring)) {
        index = d;
        break;
        }
        }
        if (index == null) {
        ArrayList<DeckResult> temp = new ArrayList<DeckResult>();
        temp.add(result);
        oppDecksMerged.put(new DeckType("", oppdeckstring), temp);
        } else {
        oppDecksMerged.get(index).add(result);
        }
         * */

        }

        String getDisplayName() {
            return deckType.getDisplayName();
        }

        public int compareTo(MyDeck o) {
            return -this.resultCount + o.resultCount;
        }

        static class Stats extends ArrayList {

            int firstWon = 0;
            int firstLost = 0;
            int lastWon = 0;
            int lastLost = 0;
            int notDecided = 0;

            void push(DeckResult result) {
                switch (result.dice) {
                    case -1:
                        switch (result.result) {
                            case -1:
                                lastLost++;
                                break;
                            case 1:
                                lastWon++;
                                break;
                            case 0:
                                notDecided++;
                                break;
                        }
                        break;
                    case 1:
                        switch (result.result) {
                            case -1:
                                firstLost++;
                                break;
                            case 1:
                                firstWon++;
                                break;
                            case 0:
                                notDecided++;
                                break;
                        }
                        break;
                    case 0:
                        notDecided++;
                        break;
                    default:
                        assert false;
                }
            }

            int getFirsts() {
                return firstWon + firstLost;
            }

            int getLasts() {
                return lastLost + lastWon;
            }

            double getFirstPercentage() {
                return firstWon + firstLost == 0 ? 0.0 : (firstWon * 10000 / (firstWon + firstLost)) / 100.0;
            }

            double getLastPercentage() {
                return lastWon + lastLost == 0 ? 0.0 : (lastWon * 10000 / (lastWon + lastLost)) / 100.0;
            }

            double getTotalPercentage() {
                return lastWon + lastLost + firstWon + firstLost == 0 ? 0.0 : ((lastWon + firstWon) * 10000 / (lastWon + lastLost + firstWon + firstLost)) / 100.0;
            }

            void writeResult(String title, StringBuilder builder) {
                builder.append(title + "\n");
                builder.append(getFirsts() + " firsts, " + firstWon + "-" + firstLost + " (" + getFirstPercentage() + "%)\n");
                builder.append(getLasts() + " lasts, " + lastWon + "-" + lastLost + " (" + getLastPercentage() + "%)\n");
                builder.append((getFirsts() + getLasts()) + " totals, " + (lastWon + firstWon) + "-" + (lastLost + firstLost) + " (" + (getTotalPercentage()) + "%)\n");
                builder.append("\n");
            }

            @Override
            public int size() {
                return getFirsts()+getLasts();
            }
        }
    }

    static class SortableHashMap<K, V extends ArrayList> extends HashMap<K, V> {

        public ArrayList<K> getSortedKeys() {
            ArrayList<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(this.entrySet());
            Collections.sort(entries, new Comparator<Entry<K, V>>() {

                public int compare(Entry<K, V> obj1, Entry<K, V> obj2) {
                    Map.Entry ent1 = (Map.Entry) obj1;
                    Map.Entry ent2 = (Map.Entry) obj2;
                    V val1 = (V) ent1.getValue();
                    V val2 = (V) ent2.getValue();
                    return val2.size() - val1.size();
                }
            });
            ArrayList<K> ret = new ArrayList<K>();
            for (Entry<K, V> e : entries) {
                ret.add(e.getKey());
            }
            return ret;
        }
    }
}
