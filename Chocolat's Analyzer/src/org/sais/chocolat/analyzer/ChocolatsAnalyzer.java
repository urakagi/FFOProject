package org.sais.chocolat.analyzer;

import org.sais.chocolat.analyzer.data.CountHash;
import org.sais.chocolat.analyzer.data.DeckType;
import org.sais.chocolat.analyzer.data.DeckResult;
import org.sais.chocolat.analyzer.data.AnalyzeParameter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sais.chocolat.analyzer.data.BattleLine;
import org.sais.chocolat.analyzer.data.BattleVector;
import org.sais.chocolat.analyzer.data.CADeck;
import org.sais.chocolat.analyzer.data.Stats;
import org.sais.chocolat.analyzer.data.TurnLine;
import org.sais.chocolat.atelier.ChocolatsAtelier;

/**
 *
 * @author Romulus
 */
public class ChocolatsAnalyzer {

    AnalyzeParameter param;
    String myname;
    String oppname;
    Set<String> hasDrawn = new HashSet<String>();
    int dice = 0;  //0: No game; 1: First; -1: Last
    int result = 0; //0: No game; positive: Won, negative: Lost, 1: Damage, 2: Deckout, 3: Resign
    int conceder = 0; //1: Opp concede, -1: Ich concede
    Continuous contWon = new Continuous();
    Continuous contLost = new Continuous();
    Continuous contCurrent = new Continuous();
    int cont = 0; //1: winning, -1: losing
    // Core
    ArrayList<CADeck> mDecks = new ArrayList<CADeck>();
    ArrayList<CADeck> mDecksByName = new ArrayList<CADeck>();
    SortableArrayHashMap<String, ArrayList<DeckResult>> mOppdecks = new SortableArrayHashMap<String, ArrayList<DeckResult>>();
    SortableArrayHashMap<String, Stats> mOpps = new SortableArrayHashMap<String, Stats>();
    ArrayList<Replay> replays;
    String deck;
    String deckname;
    String oppdeck;
    Replay rp;
    String fixed_oppname;
    CADeck myDeck;
    CADeck myDeckByName;
    int mTurn;
    // Game count
    int count = 0;
    int nocount = 0;
    int gamecount = 0;
    // Time Interval
    Calendar cal;
    Calendar firstCal = null;
    Calendar lastCal = null;
    Calendar fromCal;
    Calendar toCal;
    CountHash<String> mDrawn = new CountHash<String>();
    HashSet<String> mUsed = new HashSet<String>();
    MultipleCountHash<String> mDependency = new MultipleCountHash<String>(2);
    MultipleCountHash<String> mDrawnResult = new MultipleCountHash<String>(2);
    CountHash<String> mEvent = new CountHash<String>();
    CountHash<String> mActive = new CountHash<String>();
    CountHash<String> mBattle = new CountHash<String>();
    CountHash<String> mSupport = new CountHash<String>();
    CountHash<String> mEventWithHandInfo = new CountHash<String>();
    CountHash<String> mActiveWithHandInfo = new CountHash<String>();
    CountHash<String> mBattleWithHandInfo = new CountHash<String>();
    CountHash<String> mAtkDamage = new CountHash<String>();
    CountHash<String> mIcpDamage = new CountHash<String>();
    CountHash<String> mSupportWithHandInfo = new CountHash<String>();
    boolean handRecorded = false;
    MultipleCountHash<String> mHandRecordedGames = new MultipleCountHash<String>(2);
    int mGamesWithHand = 0;
    private static final DecimalFormat mat0 = new DecimalFormat("#0%");
    private static final DecimalFormat mat1 = new DecimalFormat("#0.0%");
    private static final DecimalFormat mat2 = new DecimalFormat("#0.00%");
    private static final DecimalFormat mat1signed = new DecimalFormat("+#0.0%;-#0.0%");
    private static final DecimalFormat dfavg = new DecimalFormat("##0.0");
    private IAnalyzerCallback mCaller;
    private String mVersion;
    
    private static Calendar[] VER_DATES = new Calendar[8];
    private static String[] VERS = new String[]{"一幕", "二幕", "三幕", "四幕", "五幕", "六幕", "七幕", "八幕"};
    
    static {
        try {
            DateFormat formatter;
            formatter = new SimpleDateFormat("yy-MM-dd");
            Calendar cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2006-05-21"));
            VER_DATES[0] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2006-12-29"));
            VER_DATES[1] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2007-08-17"));
            VER_DATES[2] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2008-08-16"));
            VER_DATES[3] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2009-08-15"));
            VER_DATES[4] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2010-08-14"));
            VER_DATES[5] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2011-08-13"));
            VER_DATES[6] = cal;
            cal = Calendar.getInstance();
            cal.setTime(formatter.parse("2012-08-11"));
            VER_DATES[7] = cal;
        } catch (ParseException ex) {
            Logger.getLogger(ChocolatsAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exec(String[] args, final AnalyzeParameter param, String rootFolder, boolean writeOut) {
        this.param = param;
        boolean moveFiles = false;
        String moveTarget = "";
        File moveTo = new File("moved");
        replays = new ArrayList<Replay>();
        if (args.length >= 3) {
            if (args[0].toUpperCase().equals("MOVE")) {
                moveFiles = true;
                moveTarget = args[1].trim();
                moveTo = new File(args[2].trim());
                moveTo.mkdirs();
            }
        }

        mDecks = new ArrayList<CADeck>();
        mDecksByName = new ArrayList<CADeck>();
        mOppdecks = new SortableArrayHashMap<String, ArrayList<DeckResult>>();
        mOpps = new SortableArrayHashMap<String, Stats>();

        int loops = 1;
        if (param.addreverse) {
            loops = 2;
        }
        for (int loop = 0; loop < loops; ++loop) {
            ArrayList<File> files = new ArrayList<File>();
            File rootDir = new File(rootFolder);
            for (File f : rootDir.listFiles()) {
                if (f.isFile() && f.getName().endsWith("txt") && f.getName().startsWith("Replay")) {
                    files.add(f);
                }
            }
            File ini = new File(rootDir.getPath() + "/dirs.ini");
            try {
                if (ini.exists()) {
                    BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(ini), "UTF-8"));
                    for (String line = fin.readLine(); line != null; line = fin.readLine()) {
                        File dir;
                        if (line.startsWith("/") || line.contains(":")) {
                            dir = new File(line);
                        } else {
                            dir = new File(rootDir.getPath() + "/" + line);
                        }
                        if (dir.exists()) {
                            for (File f : dir.listFiles()) {
                                if (f.isFile() && f.getName().endsWith("txt") && f.getName().startsWith("Replay")) {
                                    files.add(f);
                                }
                            }
                        }
                    }
                    fin.close();
                } else {
                    ini.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Sort by time
            Collections.sort(files, new Comparator<File>() {

                public int compare(File o1, File o2) {
                    Calendar c1 = CADeck.convertToCalendar(o1.getName());
                    Calendar c2 = CADeck.convertToCalendar(o2.getName());
                    if (c1 == null || c2 == null) {
                        return -1;
                    }
                    if (param.lasts.length() > 0) {
                        return c2.compareTo(c1);
                    }
                    return c1.compareTo(c2);
                }
            });

            count = 0;
            nocount = 0;
            gamecount = 0;
            int maxcount = Integer.MAX_VALUE;
            if (param.lasts.length() > 0) {
                maxcount = Integer.parseInt(param.lasts);
            } else if (param.firsts.length() > 0) {
                maxcount = Integer.parseInt(param.firsts);
            }

            fromCal = Calendar.getInstance();
            toCal = Calendar.getInstance();
            if (param.from.length() > 0) {
                String[] s = param.from.split("-");
                if (s.length == 3) {
                    fromCal.set(Calendar.YEAR, Integer.parseInt(s[0]));
                    fromCal.set(Calendar.MONTH, Integer.parseInt(s[1]) - 1);
                    fromCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s[2]));
                    fromCal.set(Calendar.HOUR_OF_DAY, 0);
                    fromCal.set(Calendar.MINUTE, 0);
                    fromCal.set(Calendar.SECOND, 0);
                } else {
                    fromCal.set(Calendar.YEAR, 1970);
                }

            } else {
                fromCal.set(Calendar.YEAR, 1970);
            }
            
            Calendar vstart = Calendar.getInstance();
            vstart.setTimeInMillis(0);
            Calendar vend = Calendar.getInstance();
            vend.setTimeInMillis(Long.MAX_VALUE);
            if (param.startVerIndex >= 0) {
                vstart = VER_DATES[param.startVerIndex];
            }
            if (param.endVerIndex > 0) {
                vend = VER_DATES[VER_DATES.length - param.endVerIndex];
                vend.roll(Calendar.DAY_OF_YEAR, false);
            }

            if (param.to.length() > 0) {
                String[] s = param.to.split("-");
                if (s.length == 3) {
                    toCal.set(Calendar.YEAR, Integer.parseInt(s[0]));
                    toCal.set(Calendar.MONTH, Integer.parseInt(s[1]) - 1);
                    toCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s[2]));
                    toCal.set(Calendar.HOUR_OF_DAY, 23);
                    toCal.set(Calendar.MINUTE, 59);
                    toCal.set(Calendar.SECOND, 59);
                } else {
                    toCal.set(Calendar.YEAR, 2999);
                }
            } else {
                toCal.set(Calendar.YEAR, 2999);
            }
            
            if (vstart.after(fromCal)) {
                fromCal = vstart;
            }
            if (vend.before(toCal)) {
                toCal = vend;
            }
            
            mCaller.setLinkTotal(files.size());

            for (int i = 0; i < files.size(); ++i) {
                File f = files.get(i);
                if (i % 37 == 0) {
                    mCaller.setLinkProgress(i);
                }

                try {
                    // Filters
                    if (gamecount >= maxcount) {
                        break;
                    }

                    if (f.length() <= param.minSize * 1024) {
                        continue;
                    }

                    cal = CADeck.convertToCalendar(f.getName());
                    if (cal == null) {
                        continue;
                    }

                    if (cal.before(fromCal) || cal.after(toCal)) {
                        continue;
                    }

                    ++count;
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "Unicode"));

                    String line = in.readLine();

                    mVersion = "1.0";
                    if (line.startsWith("FFO Replay Version ")) {
                        mVersion = line.substring("FFO Replay Version ".length());
                        in.readLine();
                        line = in.readLine();
                    }

                    if (line.contains("???")) {
                        continue;
                    }

                    // Check my deck for files to move
                    if (moveFiles && line.contains(moveTarget)) {
                        in.close();
                        f.renameTo(new File(moveTo.getPath() + "/" + f.getName()));
                        continue;

                    }

                    {
                        Pattern pattern = Pattern.compile("^(.*)//(.*)//(.*)");
                        Matcher matcher;
                        try {
                            switch (loop) {
                                case 0:
                                    matcher = pattern.matcher(line);
                                    matcher.matches();
                                    myname = matcher.group(1);
                                    deckname = matcher.group(2);
                                    deck = matcher.group(3);
                                    line = in.readLine();
                                    matcher = pattern.matcher(line);
                                    matcher.matches();
                                    oppname = matcher.group(1);
                                    oppdeck = matcher.group(3);
                                    break;
                                case 1:
                                    matcher = pattern.matcher(line);
                                    matcher.matches();
                                    oppname = matcher.group(1);
                                    oppdeck = matcher.group(3);
                                    line = in.readLine();
                                    matcher = pattern.matcher(line);
                                    matcher.matches();
                                    myname = matcher.group(1);
                                    deckname = matcher.group(2);
                                    deck = matcher.group(3);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            in.close();
                            continue;

                        }
                    }

                    if (oppname.equals(myname)) {
                        ++nocount;
                        in.close();
                        continue;

                    }

                    if (oppdeck.contains("???")) {
                        ++nocount;
                        in.close();
                        continue;
                    }

                    // Deck name filter
                    if (param.deckNameFilter.length() > 0 && !deckname.contains(param.deckNameFilter)) {
                        continue;
                    }

                    // Check opp's deck for files to move
                    if (moveFiles && line.contains(moveTarget)) {
                        in.close();
                        f.renameTo(new File(moveTo.getPath() + "/" + f.getName()));
                        continue;
                    }

                    if (moveFiles) {
                        continue;
                    }

                    deck = fixDeckStructure(deck);
                    oppdeck = fixDeckStructure(oppdeck);

                    String deckdisp = new CADeck(oppname, oppdeck).getDisplayName();
                    if (!filterLevel(deckdisp, param.oppDeckFilter)) {
                        continue;
                    }

                    fixed_oppname = fixName(oppname);

                    if (param.oppNameFilter.length() > 0 && !fixed_oppname.contains(param.oppNameFilter)) {
                        continue;
                    }

                    myDeck = null;
                    boolean existed = false;
                    for (CADeck d : mDecks) {
                        if (d.deckType.charstring.equals(deck)) {
                            existed = true;
                            myDeck = d;
                            myDeck.setDate(f);
                            break;

                        }
                    }

                    if (!existed) {
                        myDeck = new CADeck(myname, deck);
                        if (!filterLevel(myDeck.deckType.getDisplayName(), param.deckFilter)) {
                            continue;
                        }
                        myDeck.setDate(f);
                        mDecks.add(myDeck);
                    }

                    existed = false;

                    myDeckByName = null;
                    for (CADeck d : mDecksByName) {
                        if (d.deckType.charstring.equals(deckname)) {
                            existed = true;
                            myDeckByName = d;
                            myDeckByName.setDate(f);
                            break;
                        }
                    }

                    if (!existed) {
                        myDeckByName = new CADeck(myname, deckname);
                        myDeckByName.setDate(f);
                        mDecksByName.add(myDeckByName);
                    }

                    oppdeck = "対：" + new DeckType("OPP", oppdeck).getDisplayName();

                    if (!mOpps.containsKey(fixed_oppname)) {
                        mOpps.put(fixed_oppname, new Stats());
                    }

                    if (!mOppdecks.containsKey(oppdeck)) {
                        mOppdecks.put(oppdeck, new ArrayList<DeckResult>());
                    }

                    //This is a legal file
                    rp = null;
                    if (param.listReplay) {
                        rp = new Replay();
                        rp.file = f;
                        rp.mydeck = myDeck.deckType.getDisplayName();
                        rp.oppdeck = oppdeck;
                        rp.oppname = fixed_oppname;
                        replays.add(rp);
                    }

                    parseFile(in, f);
                    decideResult();

                    in.close();
                    dice = 0;
                    oppname = "";
                    fixed_oppname = "";
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(f.getName());
                    continue;
                }
            } // end of file lists
        } // end of loops

        if (moveFiles) {
            return;
        }

        if (writeOut) {
            try {
                writeOut();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static String fixName(String name) {
        if (name.contains("蟻酸") || name.contains("砂井裏鍵") || name.equals("うらきー") || name.equals("HCOOH_Linux") || name.equals("裏鍵（外出中）")) {
            return "裏鍵";
        }

        if (name.equals("あげ") || name.equals("火世院") || name.equals("天条院揚") || name.equals("揚") || name.equals("白") || name.equals("tenjoin") || name.equals("リク") || name.equals("天条院")) {
            return "アゲ";
        }

        if (name.equals("ｸﾗｽﾄ") || name.equals("デンス") || name.equals("クラスト")) {
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

        if (name.equals("南斗紅鶴拳ユダ") || name.equals("みおーん")) {
            return "sanagi";
        }

        if (name.equals("AtWikkii")) {
            return "ウィッキー";
        }

        if (name.equals("High")) {
            return "Faith";
        }

        if (name.equals("metalzoika")) {
            return "zoika";
        }

        if (name.startsWith("虚人(")) {
            return "ノナメクス先生";
        }

        return name;
    }
    static long T = 0l;

    static public void countTime(String tag) {
        long N = Calendar.getInstance().getTimeInMillis();
        if (T != 0) {
            System.out.println(tag + ": " + (N - T) + " miliseconds elipsed.");
        }

        T = N;
    }

    private String fixDeckStructure(String deck) {
        Pattern pattern = Pattern.compile("慧音（妖怪）|慧音（人間）|キモけーね");
        Matcher matcher = pattern.matcher(deck);
        deck = matcher.replaceAll("上白沢 慧音");
        return deck;
    }

    private boolean filterLevel(String deck, String filter) {
        if (filter.length() > 0) {
            if (filter.contains("プリズムリバー") || filter.contains("プリバー") || filter.contains("虹川")) {
                deck = fixPrismRiverDeckStructure(deck);
                filter = filter.replace("プリバー", "プリズムリバー");
                filter = filter.replace("虹川", "プリズムリバー");
            }

            if (deck.contains(filter)) {
                return true;
            }

            if (filter.contains("3+")) {
                String deckFilter1 = filter.replace("3+", "4");
                String deckFilter2 = filter.replace("3+", "3");
                if (!deck.contains(deckFilter1) && !deck.contains(deckFilter2)) {
                    return false;
                }

            } else if (filter.contains("2+")) {
                String deckFilter1 = filter.replace("2+", "4");
                String deckFilter2 = filter.replace("2+", "3");
                String deckFilter3 = filter.replace("2+", "2");
                if (!deck.contains(deckFilter1) && !deck.contains(deckFilter2) && !deck.contains(deckFilter3)) {
                    return false;
                }

            } else {
                return false;
            }

        }
        return true;
    }

    private String fixPrismRiverDeckStructure(String deck) {
        if (!deck.contains("ルナサ") && !deck.contains("メルラン") && !deck.contains("リリカ")) {
            return deck;
        }

        boolean isLeader = deck.startsWith("Lルナサ") || deck.startsWith("Lメルラン") || deck.startsWith("Lリリカ");
        int totallv = 0;
        String[] charlvs = deck.split("-");
        ArrayList<String> others = new ArrayList<String>();
        String otherLeader = null;
        for (String s : charlvs) {
            int index = s.indexOf("ルナサ");
            if (index >= 0) {
                totallv += Integer.parseInt(s.substring(index + 3, index + 4));
                continue;
            }

            index = s.indexOf("メルラン");
            if (index >= 0) {
                totallv += Integer.parseInt(s.substring(index + 4, index + 5));
                continue;
            }

            index = s.indexOf("リリカ");
            if (index >= 0) {
                totallv += Integer.parseInt(s.substring(index + 3, index + 4));
                continue;

            }

            if (s.startsWith("L")) {
                otherLeader = s;
            } else {
                others.add(s);
            }

        }
        deck = "";
        if (isLeader) {
            deck += "Lプリズムリバー" + totallv;
            for (String s : others) {
                deck += "-" + s;
            }

        } else {
            deck += otherLeader + "-";
            deck +=
                    "プリズムリバー" + totallv;
            for (String s : others) {
                deck += "-" + s;
            }

        }
        return deck;
    }
    //
    private final static int FORMAT_V2 = 3;
    private final static int FORMAT_V4 = 2;
    private final static int FORMAT_V8 = 4;
    //
    private final static int TURN_ICH = 1;
    private final static int TURN_OPP = -1;
    //
    private final static int DICE_NOGAME = 0;
    private final static int DICE_FIRST = 1;
    private final static int DICE_LAST = -1;
    //
    private final static int RESULT_NOGAME = 0;
    private final static int RESULT_WIN_DAMAGE = 1;
    private final static int RESULT_WIN_DECKOUT = 2;
    private final static int RESULT_WIN_RESIGN = 3;
    //
    private final static int RESULT_LOSE_DAMAGE = -1;
    private final static int RESULT_LOSE_DECKOUT = -2;
    private final static int RESULT_LOSE_RESIGN = -3;
    //
    private final static int CONCEDE_NONE = 0;
    private final static int CONCEDE_ICH = -1;
    private final static int CONCEDE_OPP = 1;

    private void parseFile(BufferedReader in, File file) throws IOException {
        mTurn = -1;
        TurnLine tl = null;
        int format = FORMAT_V8; //2: New, 3: Old
        int atkTurn = TURN_ICH;
        String spell = null; // Battling spell
        dice = DICE_NOGAME;
        result = RESULT_NOGAME;
        conceder = CONCEDE_NONE;
        hasDrawn.clear();
        boolean hasHandInfo = false;
        BattleVector attackSpells = new BattleVector();
        BattleVector interceptSpells = new BattleVector();

        // Hard parsing
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (dice == DICE_NOGAME) {
                if (line.startsWith("賽が投げられて、" + myname + "の先攻になった。")) {
                    dice = DICE_FIRST;
                }
                if (line.startsWith("賽が投げられて、" + oppname + "の先攻になった。")) {
                    dice = DICE_LAST;
                }
            }

            if (mTurn < 0 && line.contains("場に出しました")) {
                if (line.contains(myname)) {
                    dice = DICE_FIRST;
                } else {
                    dice = DICE_LAST;
                }
            }

            boolean isTurnLine = false;

            Matcher V4TurnMatcher = Pattern.compile("Turn ([0-9０-９]+) - (.+)//体力([0-9０-９]+)\\(:([0-9０-９]+)\\)"
                    + " 呪力[0-9０-９]+\\(:[0-9０-９]+\\) 手札.* 山([0-9０-９]+)\\(:([0-9０-９]+)\\).*$").matcher(line);
            if (V4TurnMatcher.matches()) {
                tl = new TurnLine();
                isTurnLine = true;
                // Ver4 replay
                format = FORMAT_V4;
                tl.turn = Integer.parseInt(V4TurnMatcher.group(1));
                tl.atkName = V4TurnMatcher.group(2);
                tl.atkHP = Integer.parseInt(V4TurnMatcher.group(3));
                tl.icpHP = Integer.parseInt(V4TurnMatcher.group(4));
                tl.atkDeck = Integer.parseInt(V4TurnMatcher.group(5));
                tl.icpDeck = Integer.parseInt(V4TurnMatcher.group(6));

                if (tl.turn == 2 && tl.atkName.equals(myname)) {
                    dice = DICE_LAST;
                } else if (tl.turn == 3 && tl.atkName.equals(myname)) {
                    dice = DICE_FIRST;
                }
            } else {
                Matcher V2TurnMatcher = Pattern.compile("Turn ([0-9０-９]+)//([^/]*) 体力([0-9０-９]+) "
                        + "呪力[0-9０-９]+ // ([^/]*) 体力([0-9０-９]+) 呪力[0-9０-９]+.*$").matcher(line);
                if (V2TurnMatcher.matches()) {
                    tl = new TurnLine();
                    isTurnLine = true;
                    // Ver2 replay
                    format = FORMAT_V2;
                    tl.turn = Integer.parseInt(V2TurnMatcher.group(1));
                    if (atkTurn == TURN_ICH) {
                        tl.atkHP = Integer.parseInt(V2TurnMatcher.group(3));
                        tl.icpHP = Integer.parseInt(V2TurnMatcher.group(5));
                        tl.atkDeck = 40;
                        tl.icpDeck = 40;
                    } else {
                        tl.atkHP = Integer.parseInt(V2TurnMatcher.group(5));
                        tl.icpHP = Integer.parseInt(V2TurnMatcher.group(3));
                        tl.atkDeck = 40;
                        tl.icpDeck = 40;
                    }
                }
            }

            if (isTurnLine) {
                mTurn = tl.turn;
                spell = null;
                conceder = CONCEDE_NONE;

                if (dice == DICE_FIRST) {
                    atkTurn = (mTurn % 2 == 1) ? TURN_ICH : TURN_OPP;
                } else if (dice == DICE_LAST) {
                    atkTurn = (mTurn % 2 == 0) ? TURN_ICH : TURN_OPP;
                }

                if (atkTurn == TURN_OPP && mVersion.equals("2.0")) {
                    // In replay ver 2.0, opponent's deck is drawn by 1
                    tl.atkDeck -= 1;
                }

                if (tl.atkDeck <= 0) {
                    result = -atkTurn * 2;
                    break;
                } else if (tl.icpDeck <= 0) {
                    result = atkTurn * 2;
                    break;
                }

            } else if (Pattern.compile("^(★戦闘)?結果：").matcher(line).find()) {
                BattleLine bl = new BattleLine();
                Matcher atkHitMatcher = Pattern.compile("(【回避】|回避) :").matcher(line);
                if (atkHitMatcher.find()) {
                    line = atkHitMatcher.replaceAll("dmg 0 :");
                    bl.atkhit = false;
                } else {
                    bl.atkhit = true;
                }
                Matcher icpHitMatcher = Pattern.compile(": (【回避】|回避)").matcher(line);
                if (icpHitMatcher.find()) {
                    line = icpHitMatcher.replaceAll(": 0 dmg");
                    bl.icphit = false;
                } else {
                    bl.icphit = true;
                }
                line = line.replaceAll("(===|【信仰遮断】) :", "dmg 0 :");
                line = line.replaceAll(": (===|【信仰遮断】)", ": 0 dmg");
                Matcher battleMatcher = Pattern.compile("^.*結果：.* - dmg ([0-9０-９]+) : ([0-9０-９]+) dmg.*$", Pattern.CASE_INSENSITIVE).matcher(line);
                if (battleMatcher.matches()) {
                    bl.atkdmg = Integer.parseInt(battleMatcher.group(1));
                    bl.icpdmg = Integer.parseInt(battleMatcher.group(2));
                }

                if (atkTurn == TURN_ICH) {
                    mAtkDamage.increase(spell, bl.icpdmg);
                } else if (atkTurn == TURN_OPP) {
                    mIcpDamage.increase(spell, bl.atkdmg);
                }

                if (bl.icpdmg >= tl.icpHP) {
                    result = (atkTurn == TURN_ICH) ? RESULT_WIN_DAMAGE : RESULT_LOSE_DAMAGE;
                    conceder = CONCEDE_NONE;
                    break;
                }

                if (bl.atkdmg >= tl.atkHP) {
                    result = (atkTurn == TURN_OPP) ? RESULT_WIN_DAMAGE : RESULT_LOSE_DAMAGE;
                    conceder = CONCEDE_NONE;
                    break;
                }
            } else if (line.startsWith("戦闘の結果") && tl != null) {
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
                        icpdmg =
                                Integer.parseInt(s[4].substring(0, s[4].indexOf("点の")));
                        break;

                    case 2:
                        //Through
                        if (atkTurn == 1) {
                            if (s[1].startsWith(myname)) {
                                break;
                            }

                            icpdmg = Integer.parseInt(s[1].substring(oppname.length() + 1, s[1].indexOf("点の")));
                        } else if (atkTurn == -1) {
                            if (s[1].startsWith(oppname)) {
                                break;
                            }

                            icpdmg = Integer.parseInt(s[1].substring(myname.length() + 1, s[1].indexOf("点の")));
                        }

                        break;
                }

                if (icpdmg >= tl.icpHP) {
                    result = (atkTurn == 1) ? 1 : -1;
                    conceder = 0;
                    break;
                }

                if (atkdmg >= tl.atkHP) {
                    result = (atkTurn == -1) ? 1 : -1;
                    conceder = 0;
                    break;
                }

            } else if (tl != null && (line.contains("の体力が－") || line.contains("の体力が＋"))) {
                Matcher m = Pattern.compile("^(.+)の体力が(－|＋).*\\((-?[0-9０-９]+)\\)").matcher(line);
                m.find();
                int hp = Integer.parseInt(m.group(3));
                if (hp <= 0) {
                    if (m.group(1).equals(myname)) {
                        result = RESULT_LOSE_DAMAGE;
                        conceder = CONCEDE_NONE;
                    } else if (m.group(1).equals(oppname)) {
                        result = RESULT_WIN_DAMAGE;
                        conceder = CONCEDE_NONE;
                    }
                    break;
                }
                if (m.group(1).equals(myname)) {
                    if (atkTurn == TURN_ICH) {
                        tl.atkHP = hp;
                    } else {
                        tl.icpHP = hp;
                    }
                } else if (m.group(1).equals(oppname)) {
                    if (atkTurn == TURN_OPP) {
                        tl.atkHP = hp;
                    } else {
                        tl.icpHP = hp;
                    }
                }
            } else if (line.contains("の体力は今") && tl != null) {
                String[] s = line.split("の体力は今");
                int hp = Integer.parseInt(s[s.length - 1].substring(0, s[s.length - 1].lastIndexOf('(')).trim());
                if (line.contains(myname)) {
                    if (atkTurn == 1) {
                        tl.atkHP = hp;
                    } else {
                        tl.icpHP = hp;
                    }

                } else if (line.contains(oppname)) {
                    if (atkTurn == -1) {
                        tl.atkHP = hp;
                    } else {
                        tl.icpHP = hp;
                    }

                }
                if (hp <= 0) {
                    if (line.contains(myname)) {
                        result = -1;
                        conceder = 0;
                        break;
                    } else if (line.contains(oppname)) {
                        result = 1;
                        conceder = 0;
                        break;
                    }

                }

            } else if (tl != null && (line.matches(".*(を山札の一番上から捨札に|を山札の一番上から捨て札に|カードを１枚引きました).*"))) {
                if ((line.startsWith(myname) && atkTurn == 1) || (line.startsWith(oppname) && atkTurn == -1)) {
                    tl.atkDeck -= 1;
                } else if ((line.startsWith(myname) && atkTurn == -1) || (line.startsWith(oppname) && atkTurn == 1)) {
                    tl.icpDeck -= 1;
                }

                if (tl.atkDeck <= 0) {
                    result = -atkTurn * 2;
                    conceder = 0;
                    break;
                }

                if (tl.icpDeck <= 0) {
                    result = atkTurn * 2;
                    conceder = 0;
                    break;
                }

            } // end of result checking

            if (param.calcCards) {
                String result = statCard(line, hasHandInfo, atkTurn, attackSpells, interceptSpells);
                if (result != null) {
                    spell = result;
                }
            }

            //Concedes
            if (conceder == 0 && (line.contains("参りました") || line.contains("まいりました") || (line.contains("投了")) || line.contains("負け") || line.contains("まけ") || line.contains("無理") || line.contains("むり") || line.contains("降参")) || line.contains("輸") || line.contains("敗") || line.contains("白旗") || line.contains("詰み") || line.contains("詰んだ") || line.contains("無解") || line.contains("lost") || line.contains("Lost") || line.equalsIgnoreCase("lost") || line.contains("ｵﾜﾀ") || line.contains("オワタ") || line.contains("降参") || line.contains("しゅーりょー")) {
                if (line.contains(myname + ":")) {
                    conceder = -1;
                } else if (line.contains(oppname + ":")) {
                    conceder = 1;
                }

                if (conceder == 0 && (line.contains("参りました") || line.contains("まいりました") || (line.contains("投了")) || line.contains("負け") || line.contains("まけ") || line.contains("無理") || line.contains("むり") || line.contains("降参")) || line.contains("輸") || line.contains("敗") || line.contains("白旗") || line.contains("詰み") || line.contains("詰んだ") || line.contains("無解") || line.contains("lost") || line.contains("Lost") || line.equalsIgnoreCase("lost") || line.contains("ｵﾜﾀ") || line.contains("オワタ") || line.contains("しゅーりょー")) {
                    if (line.contains(myname + ":")) {
                        conceder = -1;
                    } else if (line.contains(oppname + ":")) {
                        conceder = 1;
                    }

                }
            }
        } // End of hard parsing

    }

    private void decideResult() {
        if (result == 0 && conceder != 0) {
            result = conceder * 3;
        }

//Continuouses
        if (result > 0) {
            if (cont > 0) {
                ++contCurrent.length;
                contCurrent.to = cal;
            } else {
                if (contCurrent.length > contLost.length) {
                    contLost = contCurrent;
                }

                contCurrent = new Continuous();
                contCurrent.from = cal;
                contCurrent.to = cal;
                contCurrent.length = 1;
                cont = 1;
            }

        } else if (result < 0) {
            if (cont < 0) {
                ++contCurrent.length;
                contCurrent.to = cal;
            } else {
                if (contCurrent.length > contWon.length) {
                    contWon = contCurrent;
                }

                contCurrent = new Continuous();
                contCurrent.from = cal;
                contCurrent.to = cal;
                contCurrent.length = 1;
                cont =
                        -1;
            }

        }

        if (result != 0) {
            if (gamecount == 0) {
                firstCal = cal;
            }

            lastCal = cal;
            gamecount++;

        }

        //Set result

        DeckResult deckresult = new DeckResult();
        deckresult.dice = dice;
        deckresult.result = result;
        deckresult.turn = mTurn;
        if (conceder != 0) {
            deckresult.concede = true;
        }

        if (param.listReplay) {
            switch (result) {
                case 0:
                    rp.result = "NG";
                    break;

                case 1:
                    rp.result = "体力 勝";
                    break;

                case 2:
                    rp.result = "山焼 勝";
                    break;

                case 3:
                    rp.result = "中押 勝";
                    break;

                case -1:
                    rp.result = "体力 敗";
                    break;

                case -2:
                    rp.result = "山焼 敗";
                    break;

                case -3:
                    rp.result = "中押 敗";
                    break;

            }


            if (dice > 0) {
                rp.firstlast = "先";
            } else if (dice < 0) {
                rp.firstlast = "後";
            } else {
                rp.firstlast = "";
            }

            rp.turn = mTurn;
        }

// Card counts
        if (result != 0 && param.calcCards && handRecorded) {
            mGamesWithHand++;
            if (result > 0) {
                mHandRecordedGames.increase("", 0, 1);
            } else if (result < 0) {
                mHandRecordedGames.increase("", 1, 1);
            }

            for (String s : hasDrawn) {
                mDrawn.increase(s, 1);
                if (result > 0) {
                    mDrawnResult.increase(s, 0, 1);
                } else if (result < 0) {
                    mDrawnResult.increase(s, 1, 1);
                }

            }
            for (String s : mUsed) {
                if (result > 0) {
                    mDependency.increase(s, 0, 1);
                } else if (result < 0) {
                    mDependency.increase(s, 1, 1);
                }

            }
            handRecorded = false;
        }

        mUsed.clear();

        mDecks.get(mDecks.indexOf(myDeck)).addResult(fixed_oppname, oppdeck, deckresult);
        mDecksByName.get(mDecksByName.indexOf(myDeckByName)).addResult(fixed_oppname, oppdeck, deckresult);
        mOppdecks.get(oppdeck).add(deckresult);
        mOpps.get(fixed_oppname).push(deckresult);
    }

    /**
     * Calculate statistics of indivisual cards
     * @param line
     * @param hasHandInfo
     * @param atkTurn
     * @param attackSpells
     * @param interceptSpells
     * @return The battled spell
     */
    private String statCard(String line, boolean hasHandInfo, int atkTurn, BattleVector attackSpells, BattleVector interceptSpells) {
        Matcher m;
        m = Pattern.compile("^手札：.*$").matcher(line);
        if (m.matches()) {
            handRecorded = true;
            String[] cards = line.substring(3).split("//");
            for (String c : cards) {
                String cc = quoteCardname(c);
                if (!hasDrawn.contains(cc)) {
                    hasDrawn.add(cc);
                }
            }
            hasHandInfo = true;
            return null;
        }
        m = Pattern.compile("^イベント[（(](.+)[）)]：《?([^》]+)》?$").matcher(line);
        if (m.find()) {
            String user = m.group(1);
            String cardname = quoteCardname(m.group(2));
            if (user.equals(myname)) {
                mEvent.increase(cardname, 1);
                if (hasHandInfo) {
                    mEventWithHandInfo.increase(cardname, 1);
                }

                mUsed.add(cardname);
                if (!hasDrawn.contains(cardname)) {
                    hasDrawn.add(cardname);
                }
            }
            return null;
        }
        m = Pattern.compile("^↑?起動：《?([^》]+)》?$").matcher(line);
        if (m.find() && atkTurn > 0) {
            String cardname = quoteCardname(m.group(1));
            mActive.increase(cardname, 1);
            if (hasHandInfo) {
                mActiveWithHandInfo.increase(cardname, 1);
            }

            mUsed.add(cardname);
            if (!hasDrawn.contains(cardname)) {
                hasDrawn.add(cardname);
            }
            return null;
        }
        m = Pattern.compile("☆?戦闘：.+ - 《?([^》]+)》?（相手スルー）").matcher(line);
        if (m.find()) {
            String spell = quoteCardname(m.group(1));
            mBattle.increase(spell, 1);
            if (hasHandInfo) {
                mBattleWithHandInfo.increase(spell, 1);
            }

            if (atkTurn > 0) {
                attackSpells.add(spell);
            } else if (atkTurn < 0) {
                interceptSpells.add(spell);
            }

            if (!hasDrawn.contains(spell)) {
                hasDrawn.add(spell);
            }
            mUsed.add(spell);
            return spell;
        }
        m = Pattern.compile("^.?戦闘：.+ - 《?([^》]+)》? vs 《?([^》]+)》? - .+$").matcher(line);
        if (m.find()) {
            String spell;
            if (atkTurn > 0) {
                spell = quoteCardname(m.group(1));
            } else {
                spell = quoteCardname(m.group(2));
            }

            mBattle.increase(spell, 1);
            if (hasHandInfo) {
                mBattleWithHandInfo.increase(spell, 1);
            }

            if (atkTurn > 0) {
                attackSpells.add(spell);
            } else if (atkTurn < 0) {
                interceptSpells.add(spell);
            }

            if (!hasDrawn.contains(spell)) {
                hasDrawn.add(spell);
            }

            mUsed.add(spell);
            return spell;
        }
        m = Pattern.compile("^(.+)は《?([^》]+)》?を.+の《?[^》]+》?に(配置しました|つけました)。$").matcher(line);
        if (m.find() && m.group(1).equals(myname)) {
            String support = quoteCardname(m.group(2));
            mSupport.increase(support, 1);
            if (hasHandInfo) {
                mSupportWithHandInfo.increase(support, 1);
            }

            mUsed.add(support);
            if (!hasDrawn.contains(support)) {
                hasDrawn.add(support);
            }
            return null;
        }
        m = Pattern.compile("^シーン：《?([^》]+)》?$").matcher(line);
        if (m.matches()) {
            String cardname = quoteCardname(m.group(1));
            if (atkTurn > 0) {
                mSupport.increase(cardname, 1);
                if (hasHandInfo) {
                    mSupportWithHandInfo.increase(cardname, 1);
                }

            }
            mUsed.add(cardname);
            if (!hasDrawn.contains(cardname)) {
                hasDrawn.add(cardname);
            }
            return null;
        }
        return null;
    }

    private String quoteCardname(String cardname) {
        return "《" + cardname + "》";
    }

    private void writeOut() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        // Decks
        Collections.sort(mDecks);
        Collections.sort(mDecksByName);

        StringBuilder t = new StringBuilder();
        for (CADeck d : mDecks) {
            d.stats.writeResult(d.getDisplayName(), t);
        }

        StringBuilder t_name = new StringBuilder();
        for (CADeck d : mDecksByName) {
            d.stats.writeResult(d.getDisplayName(), t_name);
        }

        t_name.append("\r\n");

        // Opp decks
        ArrayList<String> entries2 = mOppdecks.getSortedKeys();

        StringBuilder t_oppdeck = new StringBuilder();

        int p_first = 0;
        int p_last = 0;
        int p_firstwon = 0;
        int p_firstlost = 0;
        int p_lastwon = 0;
        int p_lastlost = 0;
        int p_notdecided = 0;
        int[] totalTurnWon = new int[]{0, 0};
        int[] cntTurnWon = new int[]{0, 0};
        int[] totalTurnLost = new int[]{0, 0};
        int[] cntTurnLost = new int[]{0, 0};
        int concede = 0;
        int surrendered = 0;
        for (String m : entries2) {
            int first = 0;
            int last = 0;
            int firstwon = 0;
            int firstlost = 0;
            int lastwon = 0;
            int lastlost = 0;
            int notdecided = 0;
            for (DeckResult r : mOppdecks.get(m)) {
                if (r.result == 0) {
                    notdecided++;
                    continue;

                }


                if (r.result > 0) {
                    totalTurnWon[0] += r.turn;
                    cntTurnWon[0]++;
                    if (r.concede) {
                        surrendered++;
                    } else {
                        totalTurnWon[1] += r.turn;
                        cntTurnWon[1]++;
                    }

                } else if (r.result < 0) {
                    totalTurnLost[0] += r.turn;
                    cntTurnLost[0]++;
                    if (r.concede) {
                        concede++;
                    } else {
                        totalTurnLost[1] += r.turn;
                        cntTurnLost[1]++;
                    }

                }
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
            t_oppdeck.append(m + "\r\n");
            t_oppdeck.append((firstwon + firstlost) + " firsts, " + firstwon + "-" + firstlost + " (" + firstpercentage + "%)\r\n");
            t_oppdeck.append((lastwon + lastlost) + " lasts, " + lastwon + "-" + lastlost + " (" + lastpercentage + "%)\r\n");
            t_oppdeck.append((firstwon + firstlost + lastwon + lastlost) + " totals, " + (lastwon + firstwon) + "-" + (lastlost + firstlost) + " (" + (totalpercentage) + "%)" + (notdecided > 0 ? " - " + notdecided + " NG\r\n" : "\r\n"));
            t_oppdeck.append("\r\n");

            p_first +=
                    first;
            p_last +=
                    last;
            p_firstwon +=
                    firstwon;
            p_firstlost +=
                    firstlost;
            p_lastwon +=
                    lastwon;
            p_lastlost +=
                    lastlost;
            p_notdecided +=
                    notdecided;
        }

        t_oppdeck.append("\r\n");

        StringBuilder t_opp = new StringBuilder();
        for (String oppname_ : mOpps.getSortedKeys()) {
            mOpps.get(oppname_).writeResult(oppname_, t_opp);
        }

// Continious
        if (cont > 0) {
            if (contCurrent.length >= contWon.length) {
                contWon = contCurrent;
            }

        } else if (cont < 0) {
            if (contCurrent.length >= contLost.length) {
                contLost = contCurrent;
            }

        }
        t.append("最高連勝：" + contWon.getDisplayMessage() + "\r\n");
        t.append("最高連敗：" + contLost.getDisplayMessage() + "\r\n");
        t.append("\r\n");

        StringBuilder t_filter = new StringBuilder();

        // Total
        double p_firstpercentage = p_firstwon + p_firstlost == 0 ? 0.0 : (p_firstwon * 10000 / (p_firstwon + p_firstlost)) / 100.0;
        double p_lastpercentage = p_lastwon + p_lastlost == 0 ? 0.0 : (p_lastwon * 10000 / (p_lastwon + p_lastlost)) / 100.0;
        double p_totalpercantage = p_lastwon + p_lastlost + p_firstwon + p_firstlost == 0 ? 0.0 : ((p_lastwon + p_firstwon) * 1.0 / (p_lastwon + p_lastlost + p_firstwon + p_firstlost));
        p_first =
                p_firstwon + p_firstlost;
        p_last =
                p_lastwon + p_lastlost;
        t.append("Player total:\r\n");
        t.append("File Count=" + count + "\r\n");
        t.append("Non-Replay Count=" + nocount + "\r\n");
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        if (param.lasts.length() > 0) {
            if (lastCal != null && firstCal != null) {
                t.append(df.format(lastCal.getTime()) + "-" + df.format(firstCal.getTime()) + "\r\n");
            }

        } else {
            if (lastCal != null && firstCal != null) {
                t.append(df.format(firstCal.getTime()) + "-" + df.format(lastCal.getTime()) + "\r\n");
            }

        }
        t.append("\r\n");

        if (param.startVerIndex == VERS.length - param.endVerIndex - 1) {
            t_filter.append(VERS[param.startVerIndex] + "を集計\r\n");
        } else {
            t_filter.append(VERS[param.startVerIndex] + "から" + VERS[VERS.length - param.endVerIndex - 1] + "を集計\r\n");
        }

        if (param.deckFilter.length() > 0) {
            t_filter.append(param.deckFilter + "  ");
        }

        if (param.oppDeckFilter.length() > 0) {
            t_filter.append("対：" + param.oppDeckFilter + "  ");
        }

        if (param.oppNameFilter.length() > 0) {
            t_filter.append("対：" + param.oppNameFilter + "  ");
        }

        if (param.deckFilter.length() > 0 || param.oppDeckFilter.length() > 0 || param.oppNameFilter.length() > 0) {
            t_filter.append("\r\n");
        }

        if (param.from.length() > 0) {
            t_filter.append("区間：" + df.format(fromCal.getTime()));
            if (param.to.length() == 0) {
                t_filter.append("-");
            }

        }
        if (param.to.length() > 0) {
            if (param.from.length() == 0) {
                t_filter.append("区間：");
            }

            t_filter.append("-" + df.format(toCal.getTime()));
        }

        if (param.from.length() > 0 || param.to.length() > 0) {
            t_filter.append("\r\n");
        }
        
        if (param.lasts.length() > 0) {
            t_filter.append("最近の" + param.lasts + "戦\r\n");
        } else if (param.firsts.length() > 0) {
            t_filter.append("最初の" + param.firsts + "戦\r\n");
        }

// Everything totals
        int p_total = p_first + p_last;
        t.append(t_filter);
        t.append(p_first + " firsts, " + p_firstwon + "-" + p_firstlost + " (" + p_firstpercentage + "%)\r\n");
        t.append(p_last + " lasts, " + p_lastwon + "-" + p_lastlost + " (" + p_lastpercentage + "%)\r\n");
        t.append(
                p_total + " totals, " + (p_lastwon + p_firstwon) + "-" + (p_lastlost + p_firstlost) + " (" + mat2.format(p_totalpercantage) + ")" + (p_notdecided > 0 ? " - " + p_notdecided + " NG\r\n" : "\r\n"));

        // Concedes
        t.append("\r\n");
        t.append(concede + " concedes. (" + mat2.format(concede * 1.0 / (p_firstlost + p_lastlost)) + ")\r\n");
        t.append(surrendered + " surrendered. (" + mat2.format(surrendered * 1.0 / (p_firstwon + p_lastwon)) + ")\r\n");

        // Average turns
        t.append("\r\n");
        t.append(t_filter);
        t.append("Winning in avarage " + dfavg.format(1.0 * totalTurnWon[0] / cntTurnWon[0]) + " turns. ("
                + dfavg.format(1.0 * totalTurnWon[1] / cntTurnWon[1]) + " w/o concedes)\r\n");
        t.append("Losing in avarage " + dfavg.format(1.0 * totalTurnLost[0] / cntTurnLost[0]) + " turns. ("
                + dfavg.format(1.0 * totalTurnLost[1] / cntTurnLost[1]) + " w/o surrenders)\r\n");


        // Character count
        StringBuilder t_characters = new StringBuilder();
        CountHash<String> level_deck = new CountHash<String>();
        CountHash<String> level_replay = new CountHash<String>();
        CountHash<String> used_deck_index = new CountHash<String>();
        CountHash<String> used_replay_index = new CountHash<String>();
        CountHash<String> wins_level = new CountHash<String>();
        CountHash<String> loses_level = new CountHash<String>();
        CountHash<String> wins_nolevel = new CountHash<String>();
        CountHash<String> loses_nolevel = new CountHash<String>();
        ArrayList<CountHash<String>> used_deck = new ArrayList<CountHash<String>>();
        ArrayList<CountHash<String>> used_replay = new ArrayList<CountHash<String>>();

        // index 0=lv1, 1=lv2, 2=lv3, 3=lv4

        for (int i = 0; i
                < 4; ++i) {
            used_deck.add(new CountHash<String>());
        }

        for (int i = 0; i
                < 4; ++i) {
            used_replay.add(new CountHash<String>());
        }

        for (CADeck d : mDecks) {
            // Total levels by replay
            CountHash<String> local_lv = new CountHash<String>();
            for (String c : d.deckType.getDisplayChars()) {
                if (c.length() == 6) {
                    c = c.substring(0, 3);
                }

                if (c.length() == 7) {
                    c = c.substring(0, 5);
                }

                level_deck.increase(c, 1);
                level_replay.increase(c, d.resultCount);
                local_lv.increase(c, 1);
                wins_level.increase(c, d.stats.firstWon + d.stats.firstLost);
                loses_level.increase(c, d.stats.firstLost + d.stats.lastLost);
            }

            for (String c : local_lv.getHash().keySet()) {
                used_deck.get(local_lv.get(c) - 1).increase(c, 1);
                used_replay.get(local_lv.get(c) - 1).increase(c, d.resultCount);
                used_deck_index.increase(c, 1);
                used_replay_index.increase(c, d.resultCount);
                wins_nolevel.increase(c, d.stats.firstWon + d.stats.firstLost);
                loses_nolevel.increase(c, d.stats.firstLost + d.stats.lastLost);
            }

        }
        int ranking = 1;
        int[] lv_count = new int[4];

        t_characters.append("総使用回数順（リプレイごとに累計）（レベル別１から４）\r\n");
        // Borrow character lists from level_replay
        for (Entry e : used_replay_index.getSortedList(
                true)) {
            for (int i = 0; i
                    < 4; ++i) {
                lv_count[i] = used_replay.get(i).get((String) e.getKey());
            }

            t_characters.append(ranking + "." + e.getKey() + "\t" + e.getValue() + "（" + lv_count[0] + "-" + lv_count[1] + "-" + lv_count[2] + "-" + lv_count[3] + "）\r\n");
            ++ranking;
        }

        t_characters.append("\r\n");
        ranking =
                1;
        lv_count =
                new int[4];

        t_characters.append("総使用回数順（デッキタイプごとに累計）（レベル別１から４）\r\n");
        for (Entry e : used_deck_index.getSortedList(
                true)) {
            for (int i = 0; i
                    < 4; ++i) {
                lv_count[i] = used_deck.get(i).get((String) e.getKey());
            }

            t_characters.append(ranking + "." + e.getKey() + "\t" + e.getValue() + "（" + lv_count[0] + "-" + lv_count[1] + "-" + lv_count[2] + "-" + lv_count[3] + "）\r\n");
            ++ranking;
        }

        t_characters.append("\r\n");
        ranking =
                1;
        HashMap<String, Double> percentage_level = new HashMap<String, Double>();
        HashMap<String, Double> percentage_nolevel = new HashMap<String, Double>();
        for (Entry e : wins_level.getSortedList(
                true)) {
            int win = (Integer) e.getValue();
            int lose = (Integer) loses_level.get((String) e.getKey());
            double percentage = (1.0 * win / (win + lose));
            percentage_level.put((String) e.getKey(), percentage);
        }

        for (Entry e : wins_nolevel.getSortedList(
                true)) {
            int win = (Integer) e.getValue();
            int lose = (Integer) loses_nolevel.get((String) e.getKey());
            double percentage = (1.0 * win / (win + lose));
            percentage_nolevel.put((String) e.getKey(), percentage);
        }

        ArrayList<Entry> percentage_array_level = new ArrayList<Entry>();

        percentage_array_level.addAll(percentage_level.entrySet());
        Collections.sort(percentage_array_level,
                new Comparator<Entry>() {

                    public int compare(Entry o1, Entry o2) {
                        double ret = ((Double) o2.getValue()) - ((Double) o1.getValue());
                        if (ret > 0) {
                            return 1;
                        }

                        if (ret < 0) {
                            return -1;
                        }

                        return 0;
                    }
                });

        ArrayList<Entry> perventage_array_nolevel = new ArrayList<Entry>();
        perventage_array_nolevel.addAll(percentage_nolevel.entrySet());
        Collections.sort(perventage_array_nolevel, new Comparator<Entry>() {

            public int compare(Entry o1, Entry o2) {
                double ret = ((Double) o2.getValue()) - ((Double) o1.getValue());
                if (ret > 0) {
                    return 1;
                }

                if (ret < 0) {
                    return -1;
                }

                return 0;
            }
        });

        t_characters.append("総レベル順（リプレイごとに累計）\r\n");
        for (Entry e : level_replay.getSortedList(true)) {
            t_characters.append(ranking + "." + e.getKey() + "\t" + e.getValue() + "\r\n");
            ++ranking;
        }

        t_characters.append("\r\n");
        ranking =
                1;

        t_characters.append("総レベル順（デッキタイプごとに累計）\r\n");
        for (Entry e : level_deck.getSortedList(true)) {
            t_characters.append(ranking + "." + e.getKey() + "\t" + e.getValue() + "\r\n");
            ++ranking;
        }

        t_characters.append("\r\n");
        ranking =
                1;

        t_characters.append("勝率順（レベル補正あり）\r\n");
        for (Entry e : percentage_array_level) {
            t_characters.append(ranking + "." + e.getKey() + "\t" + mat2.format((Double) e.getValue()) + "\r\n");
            ++ranking;
        }

        t_characters.append("\r\n");
        ranking =
                1;

        t_characters.append("勝率順（レベル補正なし）\r\n");
        for (Entry e : perventage_array_nolevel) {
            t_characters.append(ranking + "." + e.getKey() + "\t" + mat2.format((Double) e.getValue()) + "\r\n");
            ++ranking;
        }

        t_characters.append("\r\n");
        ranking =
                1;

        // Cards count
        StringBuffer t_cards = new StringBuffer();
        StringBuilder t_ana = new StringBuilder();
        if (param.calcCards) {
            t_cards.append("シングルカード統計の上位２０枚（新リプレイのみカウント）\r\nドローの後の数字は使用比（スペルは起動基準）\r\n\r\n");

            DecimalFormat mat = new DecimalFormat("#0.00");
            SortableHashMap<String, Double> mUseRate = new SortableHashMap<String, Double>();

            int cc = 1;
            t_cards.append("ドロー回数（" + mGamesWithHand + "ゲーム中）１ゲーム中に何枚引いても１回、手札を記録したリプレイのみカウント）\r\n");
            for (Entry e : mDrawn.getSortedList(true)) {
                String rate = "";
                int ac = mActiveWithHandInfo.get((String) e.getKey());
                int bt = mBattleWithHandInfo.get((String) e.getKey());
                if (ac > 0 || bt > 0) {
                    rate = " (" + mat.format(1.0 * (ac + bt) / 2 / (Integer) e.getValue()) + ") ";
                    mUseRate.put((String) e.getKey(), 1.0 * (ac + bt) / 2 / (Integer) e.getValue());
                }

                if (mSupportWithHandInfo.get((String) e.getKey()) > 0) {
                    rate = " (" + mat.format(1.0 * mSupportWithHandInfo.get((String) e.getKey()) / (Integer) e.getValue()) + ") ";
                    mUseRate.put((String) e.getKey(), 1.0 * mSupportWithHandInfo.get((String) e.getKey()) / (Integer) e.getValue());
                }

                if (mEventWithHandInfo.get((String) e.getKey()) > 0) {
                    rate = " (" + mat.format(1.0 * mEventWithHandInfo.get((String) e.getKey()) / (Integer) e.getValue()) + ") ";
                    mUseRate.put((String) e.getKey(), 1.0 * mEventWithHandInfo.get((String) e.getKey()) / (Integer) e.getValue());
                }

                t_cards.append("(" + cc + ") " + e.getValue() + rate + "\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20 && !param.listAllCards) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("使用比順\r\n");
            for (String k : mUseRate.getSortedKeys()) {
                t_cards.append("(" + cc + ") " + mat.format(mUseRate.get(k)) + "\t" + k + "\r\n");
                ++cc;
                if (cc > 20 && !param.listAllCards) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("スペル起動回数\r\n");
            for (Entry e : mActive.getSortedList(true)) {
                t_cards.append("(" + cc + ") " + e.getValue() + "\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("スペル戦闘回数\r\n");
            for (Entry e : mBattle.getSortedList(true)) {
                t_cards.append("(" + cc + ") " + e.getValue() + "\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("スペル攻撃ダメージ\r\n");
            for (Entry e : mAtkDamage.getSortedList(true)) {
                t_cards.append("(" + cc + ") " + e.getValue() + " (" + dfavg.format(mAtkDamage.getAverage((String) e.getKey())) + ")\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("スペル迎撃ダメージ\r\n");
            for (Entry e : mIcpDamage.getSortedList(true)) {
                t_cards.append("(" + cc + ") " + e.getValue() + " (" + dfavg.format(mIcpDamage.getAverage((String) e.getKey())) + ")\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("サポート配置枚数\r\n");
            for (Entry e : mSupport.getSortedList(true)) {
                t_cards.append("(" + cc + ") " + e.getValue() + "\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("イベント使用枚数\r\n");
            for (Entry e : mEvent.getSortedList(true)) {
                t_cards.append("(" + cc + ") " + e.getValue() + "\t" + e.getKey() + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("１回でも使用したゲームの勝敗状況（母体平均勝率" + mat2.format(p_totalpercantage) + "）\r\n");
            for (String s : mDependency.getSortedKeys()) {
                t_cards.append("<" + cc + "> " + getMCHWinLoseString(mDependency, s) + "\t" + s + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            cc =
                    1;
            t_cards.append("引かなかったゲームでの勝敗状況（母体平均勝率" + mat2.format(p_totalpercantage) + "）\r\n");
            for (String s : mDrawnResult.getSortedKeys()) {
                int wins = mHandRecordedGames.get("").get(0) - mDrawnResult.get(s).get(0);
                int losses = mHandRecordedGames.get("").get(1) - mDrawnResult.get(s).get(1);
                double per = 1.0 * wins / (wins + losses);
                String percentage = mat2.format(per);
                t_cards.append(">" + cc + "< " + wins + "-" + losses + " (" + percentage + ")\t" + s + "\r\n");
                ++cc;
                if (cc > 20) {
                    break;
                }

            }
            t_cards.append("\r\n");

            for (Entry e : mDrawn.getSortedList(true)) {
                String s = (String) e.getKey();
                if (s == null) {
                    continue;
                }

                t_ana.append(s + "\r\n");
                int wins;
                int losses;
                wins =
                        mHandRecordedGames.get("").get(0);
                losses =
                        mHandRecordedGames.get("").get(1);
                double basePer = 1.0 * wins / (wins + losses);
                t_ana.append("母体：" + wins + "-" + losses + " (" + mat1.format(basePer) + ")\r\n");
                t_ana.append("引いたゲーム：" + mDrawn.get(s) + "（" + mat0.format(mDrawn.get(s) * 1.0 / mGamesWithHand) + "）、");
                Double rate = mUseRate.get(s);
                if (rate == null) {
                    t_ana.append("未使用\r\n");
                } else {
                    t_ana.append("使用比：" + mat.format(rate) + "\r\n");
                }

                try {
                    wins = mDependency.get(s).get(0);
                    losses =
                            mDependency.get(s).get(1);
                    t_ana.append("使ったゲーム" + getWinLoseString(wins, losses, basePer) + "\r\n");
                } catch (NullPointerException ex) {
                    t_ana.append("未使用\r\n");
                }

                try {
                    wins = mDrawnResult.get(s).get(0) - mDependency.get(s).get(0);
                    losses =
                            mDrawnResult.get(s).get(1) - mDependency.get(s).get(1);
                    t_ana.append("腐ったゲーム" + getWinLoseString(wins, losses, basePer) + "\r\n");
                } catch (NullPointerException ex) {
                    t_ana.append("全ゲーム使用\r\n");
                }

                try {
                    wins = mHandRecordedGames.get("").get(0) - mDrawnResult.get(s).get(0);
                    losses =
                            mHandRecordedGames.get("").get(1) - mDrawnResult.get(s).get(1);
                    t_ana.append("来ないゲーム" + getWinLoseString(wins, losses, basePer) + "\r\n");
                } catch (NullPointerException ex) {
                    t_ana.append("全ゲーム引いた\r\n");
                }

                t_ana.append("\r\n");
            }

        }

        if (param.writeFile) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_deck.txt"), "UTF-8"), 8192);
            out.write(t.toString());
            out.close();

            out =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_opp_deck.txt"), "UTF-8"), 8192);
            out.write(t_oppdeck.toString());
            out.close();

            out =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_opp.txt"), "UTF-8"), 8192);
            out.write(t_opp.toString());
            out.close();

            out =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_deckname.txt"), "UTF-8"), 8192);
            out.write(t_name.toString());
            out.close();

            out =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_character.txt"), "UTF-8"), 8192);
            out.write(t_characters.toString());
            out.close();

            if (param.calcCards) {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_cards.txt"), "UTF-8"), 8192);
                out.write(t_cards.toString());
                out.close();

                out =
                        new BufferedWriter(new OutputStreamWriter(new FileOutputStream("analysis_cardsAnalysis.txt"), "UTF-8"), 8192);
                out.write(t_ana.toString());
                out.close();
            }

        }

        AnalyzeResult a_result = new AnalyzeResult();
        a_result.useDecks = t.toString();
        a_result.meetDecks = t_oppdeck.toString();
        a_result.meetPlayers = t_opp.toString();
        a_result.deckNames = t_name.toString();
        a_result.characters = t_characters.toString();
        a_result.cards = t_cards.toString();
        a_result.cardsAnalyze = t_filter.toString() + "\r\n" + t_ana.toString();
        a_result.writeReplay = param.listReplay;
        a_result.replays = replays;
        new ResultFrame(a_result).setVisible(true);

        if (param.useAtelier) {
            new ChocolatsAtelier(a_result).setVisible(true);
        }

    }

    private String getMCHWinLoseString(MultipleCountHash<String> h, String s) {
        int wins = h.get(s).get(0);
        int losses = h.get(s).get(1);
        String percentage = mat2.format(1.0 * wins / (wins + losses));
        if (wins + losses == 0) {
            percentage = "0.0%";
        }

        return wins + "-" + losses + " (" + percentage + ")";
    }

    private String getWinLoseString(int wins, int losses, double basePer) {
        if (wins + losses != 0) {
            double per = 1.0 * wins / (wins + losses);
            String percentage = mat1.format(per);
            return "（" + mat0.format(1.0 * (wins + losses) / mGamesWithHand) + "）：" + wins + "-" + losses + " (" + percentage + " / " + mat1signed.format(per - basePer) + ")";
        } else {
            return "（" + mat0.format(1.0 * (wins + losses) / mGamesWithHand) + "）：" + wins + "-" + losses + " (0.0%)";
        }

    }

    public ArrayList<CADeck> getStatsByDeckName() {
        return mDecksByName;
    }

    public void setCallback(IAnalyzerCallback caller) {
        mCaller = caller;


    }

    public interface IAnalyzerCallback {

        void setLinkTotal(int max);

        void setLinkProgress(int progress);
    }
}
