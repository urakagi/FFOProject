/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.deck;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.tool.FTool;

public class Deck {

    private String deckname = "";
    int[] characters = new int[4];
    public int[] cards = new int[40];
    public ArrayList<String> tags = new ArrayList<String>();
    public ArrayList<String> aliases = new ArrayList<String>();

    public Deck() {
        cards = new int[40];
        characters = new int[4];
    }

    public static Deck newNull() {
        return new Deck();
    }

    public String getDeckName() {
        return deckname;
    }

    public void setDeckName(String deckname) {
        this.deckname = deckname;
    }

    public String getCharsStringForSocket() {
        return characters[0] + " " + characters[1] + " " + characters[2] + " " + characters[3];
    }

    public String getCharsStringForDisplay() {
        String ret = "";
        CountHash<String> lvs = new CountHash<String>();
        for (int i = 0; i < 4; ++i) {
            lvs.increase(CardDatabase.getCharacterInfo(characters[i]).getSingleTextName(), 1);
        }
        for (int i = 0; i < 4; ++i) {
            int lv = lvs.get(CardDatabase.getCharacterInfo(characters[i]).getSingleTextName());
            if (lv > 0) {
                ret += CardDatabase.getCharacterInfo(characters[i]).getSingleTextName() + lv;
                lvs.mHashMap.put(CardDatabase.getCharacterInfo(characters[i]).getSingleTextName(), 0);
            }
        }
        return ret;
    }

    public String getCharsStringForReplay() {
        String ret = "";
        for (int i = 0; i < 4; ++i) {
            ret += CardDatabase.getCharacterInfo(characters[i]).getName().split(FTool.getLocale(81))[0] + "-";
        }
        return ret;
    }

    public int getLeader() {
        return characters[0];
    }

    /**
     * Get character's card number, sorted, ignoring leader.
     * @return
     */
    public ArrayList<Integer> getCharacters() {
        ArrayList<Integer> ret = new ArrayList<Integer>(4);
        for (int ch : characters) {
            ret.add(ch);
        }
        Collections.sort(ret);
        return ret;
    }

    public void setCharacters(String[] ss) {
        int i = 0;
        for (String s : ss) {
            characters[i] = Integer.parseInt(s);
            ++i;
        }
    }

    public String getCheckCode() {
        CRC32 alg = new CRC32();
        String s = "";
        for (int c : cards) {
            s += c;
        }
        for (int c : characters) {
            s += c;
        }
        try {
            s += new File("FantasyFesta.jar").lastModified();
        } catch (Exception e) {
        }
        alg.update(s.getBytes());
        long value = alg.getValue();
        return Long.toHexString(value);
    }

    public void setTags(String tagString) {
        tags.clear();
        tagString = tagString.replace("　", " ");
        for (String s : tagString.split(" ")) {
            if (s.trim().length() > 0) {
                tags.add(s);
            }
        }
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public void removeAlias(String alias) {
        aliases.remove(alias);
    }

    public static class Editor {

        public Editor(Deck inputdeck) throws UnsupportedEncodingException {
            inputdeck.deckname = "New Deck";
        }

        static void TestDeck() {
            try {
                Deck newdec = new Deck();
                newdec.deckname = "Test Deck";
                for (int i = 0; i < 4; ++i) {
                    newdec.characters[i] = 100;
                }
                for (int i = 0; i < 39; ++i) {
                    newdec.cards[i] = 101 + (int) (i / 3);
                }
                newdec.cards[39] = 1;
                save(new File("test.dec"), newdec);
            } catch (Exception e) {
                System.out.print(e);
            }
        }

        /**
         * Deck file structure:
         * int - deckname's length
         * x bytes - deckname
         * 4 ints - characters
         * 40 ints - cards
         * (int + x bytes - tag's length, then tag) * n
         * If deckname length is -1, then there's aliases, and the first one is current deckname
         *
         * @param file
         * @param deck
         * @return
         */
        static int save(File file, Deck deck) {
            try {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buff;
                if (deck.aliases.size() > 0) {
                    out.write(FTool.itoba(-1));
                    out.write(FTool.itoba(deck.aliases.size()));
                    buff = deck.deckname.getBytes("Unicode");
                    out.write(FTool.itoba(buff.length));
                    out.write(buff);
                    for (String alias : deck.aliases) {
                        buff = alias.getBytes("Unicode");
                        out.write(FTool.itoba(buff.length));
                        out.write(buff);
                    }
                } else {
                    buff = deck.deckname.getBytes("Unicode");
                    out.write(FTool.itoba(buff.length));
                    out.write(buff);
                }
                for (int i = 0; i < 4; ++i) {
                    out.write(FTool.itoba(deck.characters[i]));
                }
                for (int i = 0; i < 40; ++i) {
                    out.write(FTool.itoba(deck.cards[i]));
                }
                for (String tag : deck.tags) {
                    buff = tag.getBytes("Unicode");
                    out.write(FTool.itoba(buff.length));
                    out.write(buff);
                }
                out.flush();
                out.close();
                return 0;
            } catch (IOException e) {
                System.out.println(e);
                return -1;
            }
        }

        public static Deck load(File file) throws FileNotFoundException, IOException {
            Deck deck = new Deck();
            load(file, deck);
            return deck;
        }

        public static int load(File file, Deck deck) throws FileNotFoundException, IOException {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            byte[] buff = new byte[4];
            byte[] buff2;
            in.read(buff);
            int len = FTool.batoi(buff);
            if (len > 0) {
                buff2 = new byte[len];
                in.read(buff2);
                deck.deckname = new String(buff2, "Unicode");
            } else {
                in.read(buff);
                int count = FTool.batoi(buff);
                in.read(buff);
                len = FTool.batoi(buff);
                buff2 = new byte[len];
                in.read(buff2);
                deck.deckname = new String(buff2, "Unicode");
                for (int i = 0; i < count; ++i) {
                    in.read(buff);
                    len = FTool.batoi(buff);
                    buff2 = new byte[len];
                    in.read(buff2);
                    deck.aliases.add(new String(buff2, "Unicode"));
                }
            }
            for (int i = 0; i < 4; ++i) {
                in.read(buff);
                deck.characters[i] = FTool.batoi(buff);
            }
            for (int i = 0; i < 40; ++i) {
                in.read(buff);
                int card = FTool.batoi(buff);
                if (card / 100 == 99) {
                    deck.cards[i] = card - 900;
                } else {
                    deck.cards[i] = card;
                }
            }
            deck.tags.clear();
            for (int c = in.read(buff); c >= 0; c = in.read(buff)) {
                buff2 = new byte[FTool.batoi(buff)];
                in.read(buff2);
                deck.tags.add(new String(buff2, "Unicode"));
            }
            in.close();
            return 0;
        }
    }

    public String getTagString() {
        StringBuilder b = new StringBuilder();
        for (String s : tags) {
            b.append(s);
            b.append(" ");
        }
        return b.toString().trim();
    }

    static class CountHash<T> {

        HashMap<T, Integer> mHashMap = new HashMap<T, Integer>();

        public void increase(T key, int amount) {
            if (key == null) {
                return;
            }
            if (mHashMap.containsKey(key)) {
                mHashMap.put(key, mHashMap.get(key) + amount);
            } else {
                mHashMap.put(key, amount);
            }
        }

        public ArrayList<Entry<T, Integer>> getSortedList(final boolean decremental) {
            ArrayList<Entry<T, Integer>> ret = new ArrayList<Entry<T, Integer>>();
            ret.addAll(mHashMap.entrySet());
            Collections.sort(ret, new Comparator<Entry<T, Integer>>() {

                @Override
                public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
                    if (decremental) {
                        return ((Integer) o2.getValue()) - ((Integer) o1.getValue());
                    } else {
                        return ((Integer) o1.getValue()) - ((Integer) o2.getValue());
                    }
                }
            });
            return ret;
        }

        public ArrayList<Entry<T, Integer>> getSortedList(final boolean decremental, final Comparator<Entry<T, Integer>> subComparator) {
            ArrayList<Entry<T, Integer>> ret = new ArrayList<Entry<T, Integer>>();
            ret.addAll(mHashMap.entrySet());
            Collections.sort(ret, new Comparator<Entry<T, Integer>>() {

                @Override
                public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
                    int value = ((Integer) o2.getValue()) - ((Integer) o1.getValue());
                    if (value == 0) {
                        return subComparator.compare(o1, o2);
                    }
                    return decremental ? value : -value;
                }
            });
            return ret;
        }

        public int get(T key) {
            return mHashMap.get(key) == null ? 0 : mHashMap.get(key);
        }
    }
}
