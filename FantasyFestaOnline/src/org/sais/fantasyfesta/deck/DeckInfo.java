/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.deck;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.sais.chocolat.analyzer.data.CADeck;

/**
 *
 * @author Romulus
 */
public class DeckInfo implements Comparable<DeckInfo> {

    private static String[] CHARS = new String[]{"霊", "魔", "咲", "妖", "紫", "ア", "レ", "幽", "フ", "パ", "美", "輝", "永", "鈴", "藍", "橙", "虹", "妹", "慧", "萃", "諏", "神", "早", "文", "小", "に", "天", "衣", "空", "燐", "こ", "さ", "白", "星", "ぬ", "傘", "ナ", "水"};
    //private static String[] FULLNUMBER = new String[]{"０", "１", "２", "３", "４", "５", "６"};
    private File mFile;
    private Deck mDeck;
    private int[] mLvs = new int[CHARS.length];
    private long mLastModified;
    private ArrayList<CADeck> mCADecks = new ArrayList<CADeck>();

    public DeckInfo(File file) {
        try {
            this.mFile = file;
            mLastModified = file.lastModified();
            mDeck = Deck.Editor.load(file);
            for (int charnum : mDeck.characters) {
                mLvs[fixChar(charnum / 100) - 1]++;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public String getName() {
        return mDeck.getDeckName();
    }

    public String getCharConstruct() {
        try {
            int leader = fixChar(mDeck.characters[0] / 100);
            if (leader == 0) {
                return "不明";
            }
            StringBuffer b = new StringBuffer();
            b.append(CHARS[leader - 1] + mLvs[leader - 1]);
            for (int i = 0; i < mLvs.length; ++i) {
                if (i == leader - 1) {
                    continue;
                }
                if (mLvs[i] > 0) {
                    b.append(CHARS[i] + mLvs[i]);
                }
            }
            return b.toString();
        } catch (Exception ex) {
            // ex.printStackTrace();
            return "解析失敗";
        }
    }

    public ArrayList<String> getTags() {
        return mDeck.tags;
    }

    public String getTagString() {
        StringBuffer b = new StringBuffer();
        for (String s : mDeck.tags) {
            b.append(s + " ");
        }
        return b.toString().trim();
    }

    public String getLastModifiedString() {
        long diff = System.currentTimeMillis() - mLastModified;
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(diff);

        if (diff < 86400000L) {
            return "ほやほや";
        }
        long days = diff / 86400000L;
        if (days < 30) {
            return days + "日前";
        }
        long months = diff / 2592000000L;
        return months + "ヶ月前";
    }

    private int fixChar(int c) {
        if (c == 80) {
            return 17;
        } else if (c == 81) {
            return 19;
        }
        return c;
    }

    public int getCharLV(int charnumprefix) {
        try {
            return mLvs[charnumprefix - 1];
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean containsChar(int charnumprefix) {
        try {
            return mLvs[charnumprefix - 1] > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean containsTag(String tag) {
        return mDeck.tags.contains(tag);
    }

    public ArrayList<String> getAliases() {
        return mDeck.aliases;
    }

    public File getFile() {
        return mFile;
    }

    public void addCADeck(CADeck cadeck) {
        mCADecks.add(cadeck);
    }

    public void clearCADeck() {
        mCADecks.clear();
    }

    public boolean hasCA() {
        return mCADecks.size() > 0;
    }

    public ArrayList<CADeck> getCADecks() {
        return mCADecks;
    }

    public int getCAUseCount() {
        int ret = 0;
        for (CADeck d : mCADecks) {
            ret += d.getStats().getGameCount();
        }
        return ret;
    }

    public double getCAWinningPercentage() {
        int wins = 0;
        int losts = 0;
        for (CADeck d : mCADecks) {
            wins += d.getStats().getWins();
            losts += d.getStats().getLosts();
        }
        return 1.0 * wins / (wins + losts);
    }

    public int getCALosts() {
        int losts = 0;
        for (CADeck d : mCADecks) {
            losts += d.getStats().getLosts();
        }
        return losts;
    }

    public int getCAWins() {
        int wins = 0;
        for (CADeck d : mCADecks) {
            wins += d.getStats().getWins();
        }
        return wins;
    }

    public String getUsagePeriodString() {
        long first = Long.MAX_VALUE;
        long last = 0;
        for (CADeck d : mCADecks) {
            if (d.getFirstUsed() < first) {
                first = d.getFirstUsed();
            }
            if (d.getLastUsed() > last) {
                last = d.getLastUsed();
            }
        }
        SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd");
        return df.format(first) + "-" + df.format(last);
    }

    public long getCALastUsed() {
        long last = Long.MIN_VALUE;
        for (CADeck d : mCADecks) {
            if (d.getLastUsed() > last) {
                last = d.getLastUsed();
            }
        }
        return last;
    }

    @Override
    public int compareTo(DeckInfo o) {
        if (this.mLastModified < o.mLastModified) {
            return 1;
        }
        if (this.mLastModified > o.mLastModified) {
            return -1;
        }
        return 0;
    }
}
