/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.irc;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.sais.chocolat.core.GameResult;
import org.sais.chocolat.core.Participant;
import org.sais.chocolat.core.Round;
import org.sais.chocolat.core.Table;
import org.sais.chocolat.core.Tournament;

/**
 *
 * @author Romulus
 */
public class IRCManager {

    public static final String[] COMMAND_PREFIX = new String[]{"＠", "@"};
    public static final String[] ORDER_PREFIX = new String[]{"！", "!"};

    public static int sRoundInterval = 35;

    private Tournament mTour;
    private IRCWatchFrame mFrame;
    private IRCAgent mAgent;
    private IIRCManagerCallback mCaller;
    private IIRCManagerResultSetCallback mResultHolder;
    private boolean mOnRecipent = true;
    private String mProxyIP = "";
    private ArrayList<String> mRule = new ArrayList<String>();
    private boolean mDebuggable;
    private boolean mDebugging;

    public IRCManager(IIRCManagerCallback caller, Tournament tour) {
        mTour = tour;
        mCaller = caller;
        mFrame = new IRCWatchFrame(this);
    }
    
    public void setTournament(Tournament tour) {
        mTour = tour;
    }

    public void setAgent(IRCAgent agent) {
        mAgent = agent;
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message, boolean isNotice) {
        int cmdprefix = isCommand(message);
        if (cmdprefix > 0) {
            mFrame.showMessage("<" + sender + "> " + message, new Color(0, 100, 0));
            dispatchCommand(channel, sender, login, hostname, message.substring(cmdprefix));
            return;
        }

        cmdprefix = isOrder(message);
        if (cmdprefix > 0) {
            mFrame.showMessage("<" + sender + "> " + message, new Color(100, 100, 0));
            dispatchOrder(channel, sender, login, hostname, message.substring(cmdprefix));
            return;
        }

        if (isNotice) {
            mFrame.showMessage("<" + sender + "> " + message, Color.RED);
        } else {
            mFrame.showMessage("<" + sender + "> " + message);
        }
    }

    public void onPrivateMessage(String sender, String login, String hostname, String message) {
        if (message.equals("debug") && mDebuggable) {
            showSystemMessage("らめぇ！" + sender + "の太くて硬いデバッガーが入って来ちゃうの！");
            mAgent.sendMessage(sender, "デバッグを開始します。");
            mDebugging = true;
        }
        if (!mDebugging) {
            return;
        }
        if (message.startsWith("info")) {
            String[] cmd = message.split(" ");
            String p1 = cmd[1];
            if (p1.equals("tour")) {
                mAgent.sendMessage(sender, mTour.name);
                mAgent.sendMessage(sender, mTour.round.size() + " rounds, " + mTour.participant.size() + " participants");
            } else if (p1.equals("round")) {
                for (Round r : mTour.round) {
                    mAgent.sendMessage(sender, "sn=" + r.sn + ", " + r.tables.size() + " tables, isResultSet=" + r.isResultSet());
                }
            } else if (p1.equals("table")) {
                int p2 = Integer.parseInt(cmd[2]);
                Round target = null;
                for (Round r : mTour.round) {
                    if (r.sn == p2) {
                        target = r;
                        break;
                    }
                }
                if (target == null) {
                    mAgent.sendMessage(sender, "No table with inputed sn.");
                } else {
                    mAgent.sendMessage(sender, "Tables in round sn " + target.sn);
                    for (Table t : target.tables) {
                        mAgent.sendMessage(sender, "Table " + t.sn + " " + t.player1.name + " VS " + t.player2.name + ", winner=" + t.winner);
                    }
                }
            }
        } else if (message.startsWith("delete")) {
            String[] cmd = message.split(" ");
            String p1 = cmd[1];
            int p2 = Integer.parseInt(cmd[2]);
            if (p1.equals("round")) {
                Round target = null;
                for (Round r : mTour.round) {
                    if (r.sn == p2) {
                        target = r;
                        break;
                    }
                }
                if (target == null) {
                    mAgent.sendMessage(sender, "No round with inputed sn.");
                } else {
                    mTour.round.remove(target);
                    mAgent.sendMessage(sender, "Removed round " + target.sn);
                }
            } else if (p1.equals("result")) {
                Round target = null;
                for (Round r : mTour.round) {
                    if (r.sn == p2) {
                        target = r;
                        break;
                    }
                }
                if (target == null) {
                    mAgent.sendMessage(sender, "No round with inputed sn.");
                } else {
                    for (Table t : target.tables) {
                        t.winner = -1;
                    }
                    mAgent.sendMessage(sender, "Removed all results of round " + target.sn);
                }
            }
            mCaller.invokeUpdate();
        }
    }

    private void dispatchCommand(String channel, String sender, String login, String hostname, String command) {
        if (mTour.promoter.length() < 1) {
            showSystemMessage("大会はまだ始まって無いわ。ご主人様、IRCの方で「！大会開始」と発言して下さいな。");
            return;
        }
        command = command.replace("　", " ");

        if (command.startsWith("参加")) {
            handleParticipate(channel, sender, hostname);
            return;
        }
        if (command.startsWith("チーム参加")) {
            handleTeamParticipate(channel, sender, command.substring("チーム参加".length()), hostname);
            return;
        }
        if (command.startsWith("離脱")) {
            handleDrop(channel, sender);
            return;
        }
        if (command.startsWith("リスト")) {
            handleList(channel);
            return;
        }
        if (command.startsWith("ホスト")) {
            handleHostChange(channel, sender);
            return;
        }
        if (command.startsWith("報告")) {
            handleResultReport(channel, sender, command.substring(2).trim());
            return;
        }
        if (command.startsWith("勝") || command.startsWith("負") || command.startsWith("敗")) {
            handleResultReport(channel, sender, command);
            return;
        }
        if (command.startsWith("デッキ")) {
            handleRegisterDeck(channel, sender, command.substring(3).trim());
            return;
        }
        if (command.startsWith("結果")) {
            handleListResults(channel);
            return;
        }
        if (command.startsWith("残り")) {
            handleListLeftTables(channel);
            return;
        }
        if (command.startsWith("全勝者")) {
            handleListNotDefeated(channel);
        }
        if (command.startsWith("ルール")) {
            handleShowRule(channel);
            return;
        }
    }

    private void dispatchOrder(String channel, String sender, String login, String hostname, String order) {
        order = order.replace("　", " ");

        if (order.startsWith("大会開始")) {
            if (order.length() == 4) {
                showSystemMessage("大会名を忘れてるよー「！大会開始　第四十三回定例」みたいな感じでよろしくね。");
            }
            handleStartTournament(channel, sender, order.substring(4).trim());
            return;
        }
        if (order.startsWith("大会再開")) {
            handleRestartTournament(sender);
            return;
        }

        if (mTour.promoter.length() < 1) {
            showSystemMessage("大会はまだ始まって無いわ。ご主人様、IRCの方で「！大会開始」と発言して下さいな。");
            return;
        }

        if (order.startsWith("受付終了")) {
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleStopRecipent(channel);
        }
        if (order.startsWith("受付再開")) {
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleRestartRecipent(channel);
        }
        if (order.startsWith("プロキシ")) {
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            order = order.replace("　", "");
            mProxyIP = order.substring(4).trim();
            showSystemMessage("プロキシのIPを設定したわよ");
            sendSystemNotice(channel, "プロキシのIPを" + mProxyIP + "に設定しました。");
        }
        if (order.startsWith("次回戦")) {
            // Check privilege
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            mOnRecipent = false;
            handleNextRound(channel);
            return;
        }
        if (order.startsWith("結果発表")) {
            // Check privilege
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleShowResult(channel);
            return;
        }
        if (order.startsWith("ルール")) {
            // Check privilege
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleSetRule(order, channel);
            return;
        }
        if (order.startsWith("試合時間")) {
            // Check privilege
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleSetTime(order, channel);
            return;
        }
        if (order.startsWith("上位デッキ")) {
            // Check privilege
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleShowDecks(channel);
            return;
        }

        if (order.startsWith("大会終了")) {
            // Check privilege
            if (!sender.equals(mTour.promoter)) {
                showSystemMessage("ふぁ～・・・");
                sendSystemNotice(channel, "曲者ー！であえであえー！");
                return;
            }
            handleStopTournament(channel);
            return;
        }
    }

    // Command Handling
    private void handleParticipate(String channel, String sender, String hostname) {
        if (!mOnRecipent) {
            return;
        }

        Participant player = mTour.getParticipant(sender);
        String ip;
        try {
            ip = InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException ex) {
            ip = hostname;
        }

        // Check IP
        Participant ip_p = mTour.checkIP(ip);
        if (ip_p != null) {
            showSystemMessage("548乙。");
            sendSystemMessage(channel, sender + "様と同じIPの方が既に" + ip_p.name + "の名義で参加しております。");
            return;
        }

        // Duplicate Check
        if (player != null) {
            showSystemMessage(sender + " は既にID " + player.id + "で参加しているわ。自分の参加も覚えてないなんて、大丈夫かしら。");
            sendSystemMessage(channel, sender + "様は既に参加されております。IDは" + player.id + "番です。");
            return;
        }
        player = new Participant();
        player.name = Tournament.deleteTailUnderline(sender);
        player.isHostable = true;
        player.ip = ip;
        mTour.enroll(player);
        showSystemMessage(sender + " の参加を受理したわよ。IDは" + player.id + "、IPは" + player.ip + "ね。");
        sendSystemNotice(channel, sender + "様の参加を受理しました。現在の参加者は" + mTour.countParticipants() + "人です。");
        mAgent.setTopic(channel, mTour.name + "（現在" + mTour.countParticipants() + "人）参加受付中です。参加者は「＠参加」と発言してくださいな。");
        mCaller.invokeUpdate();
    }

    private void handleTeamParticipate(String channel, String sender, String teammates, String hostname) {
        if (!mOnRecipent) {
            return;
        }

        for (String s : teammates.split(" ")) {
            if (s.length() > 0) {
                sender += "&" + Tournament.deleteTailUnderline(s);
            }
        }

        String ip;
        try {
            ip = InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException ex) {
            ip = hostname;
        }

        // Check IP
        Participant ip_p = mTour.checkIP(ip);
        if (ip_p != null) {
            showSystemMessage("548乙。");
            sendSystemMessage(channel, sender + "様と同じIPの方が既に" + ip_p.name + "の名義で参加しております。");
            return;
        }

        // Duplicate Check
        Participant player = mTour.getParticipant(sender);
        if (player != null) {
            showSystemMessage(sender + " は既にID " + player.id + "で参加しているわ。自分の参加も覚えてないなんて、大丈夫かしら。");
            sendSystemMessage(channel, sender + "様は既に参加されております。IDは" + player.id + "番です。");
            return;
        }

        player = new Participant();
        player.name = Tournament.deleteTailUnderline(sender);
        player.isHostable = true;
        player.ip = ip;
        mTour.enroll(player);
        showSystemMessage(sender + " の参加を受理したわよ。IDは" + player.id + "、IPは" + player.ip + "ね。");
        sendSystemNotice(channel, sender + "様の参加を受理しました。現在の参加者は" + mTour.countParticipants() + "人です。");
        mAgent.setTopic(channel, mTour.name + "（現在" + mTour.countParticipants() + "人）参加受付中です。参加者は「＠参加」と発言してくださいな。");
        mCaller.invokeUpdate();
    }

    private void handleDrop(String channel, String sender) {
        Participant player = mTour.getParticipant(sender);
        if (player == null) {
            showSystemMessage(sender + " は参加してないわよ。この歳でもうボケたのかしら。");
            sendSystemMessage(channel, sender + "様は大会に参加しておりません。よって離脱は不可能です。");
            return;
        }
        mTour.delete(player.id);
        showSystemMessage(sender + " の離脱を受理したわよ。まったく人騒がせね。");
        sendSystemMessage(channel, sender + "様の離脱手続を済ましました。現在の参加者は" + mTour.countParticipants() + "人です。");
        mAgent.setTopic(channel, mTour.name + "（現在" + mTour.countParticipants() + "人）参加受付中です。参加者は「＠参加」と発言してくださいな。");
        mCaller.invokeUpdate();
    }

    private void handleList(String channel) {
        String list = "";
        String noHostList = "";
        int noHostCount = 0;
        for (Participant p : mTour.participant.values()) {
            list += p.name + ", ";
            if (!p.isHostable) {
                noHostList += p.name + ", ";
                ++noHostCount;
            }
        }
        if (list.length() > 2) {
            list = list.substring(0, list.length() - 2);
        }
        if (noHostList.length() > 2) {
            noHostList = noHostList.substring(0, noHostList.length() - 2);
        }
        showSystemMessage("受付窓で確認してね。");
        sendSystemMessage(channel, mTour.name + "参加者リスト（" + mTour.countParticipants() + "名）：" + list);
        if (noHostCount > 0) {
            sendSystemMessage(channel, "ホスト不可（" + noHostCount + "名）：" + noHostList);
        }
        sendSystemMessage(channel, "ホスト不可の方は、「＠ホスト」で報告してくださいな。");
    }

    private void handleHostChange(String channel, String sender) {
        Participant player = mTour.getParticipant(sender);
        if (player == null) {
            showSystemMessage(sender + " は参加してないわよ。この歳でもうボケたのかしら。");
            sendSystemMessage(channel, sender + "様は参加しておりません。");
            return;
        }
        player.isHostable = !player.isHostable;
        if (player.isHostable) {
            showSystemMessage(sender + " をホスト可能に変更したわよ。");
            sendSystemNotice(channel, sender + "様をホスト可能に設定致しました。");
        } else {
            showSystemMessage(sender + " をホスト不可に変更したわよ。ホスト不可は面倒くさいだからやめてほしいわね。");
            sendSystemNotice(channel, sender + "様をホスト不可に設定致しました。");
        }
        mCaller.invokeUpdate();
    }

    private void handleResultReport(String channel, String sender, String resultString) {
        Participant reporter = mTour.getParticipant(sender);
        Table t = mTour.getCurrentRound().findTableByParticipantId(reporter.id);
        if (t == null) {
            showSystemMessage(sender + "、参加して無いじゃない。面倒を掛けないで欲しいわ！");
            sendSystemMessage(channel, sender + "様は参加しておりません。");
            return;
        }

        int result = -1;
        if (resultString.startsWith("勝")) {
            result = GameResult.RESULT_WON;
        } else if (resultString.startsWith("敗") || resultString.startsWith("負")) {
            result = GameResult.RESULT_LOSS;
        } else if (resultString.startsWith("引")) {
            result = GameResult.RESULT_DRAW;
            t.setWinner(Table.PROXY_DRAW);
            showSystemMessage(t.getIRCResultString() + "を記録したわよ。");
            sendSystemNotice(channel, t.getIRCResultString() + "を記録しました。");
            afterResultInputed(channel);
            return;
        } else {
            showSystemMessage("イミフ・・・");
            sendSystemMessage(channel, "報告は、「＠報告　勝利／勝ち」か、「＠報告　敗北／負け」か、「＠報告　引き分け」、でお願い致します。");
            return;
        }

        int winner;
        if (t.getP1().id == reporter.id) {
            if (result == GameResult.RESULT_WON) {
                winner = Table.PLAYER1;
            } else if (result == GameResult.RESULT_LOSS) {
                winner = Table.PLAYER2;
            } else {
                winner = -1;
                System.out.println("Invalid report, id = p1");
            }
        } else if (t.getP2().id == reporter.id) {
            if (result == GameResult.RESULT_WON) {
                winner = Table.PLAYER2;
            } else if (result == GameResult.RESULT_LOSS) {
                winner = Table.PLAYER1;
            } else {
                winner = -1;
                System.out.println("Invalid report, id = p2");
            }
        } else {
            winner = -1;
            System.out.println("Invalid report, id = " + reporter.id);
        }

        t.setWinner(winner);
        if (winner == Table.PLAYER1) {
            showSystemMessage(t.getIRCResultString() + "を記録したわよ。");
            sendSystemNotice(channel, t.getIRCResultString() + "を記録しました。");
        } else {
            showSystemMessage(t.getIRCResultString() + "を記録したわよ。");
            sendSystemNotice(channel, t.getIRCResultString() + "を記録しました。");
        }
        afterResultInputed(channel);

    }

    private void afterResultInputed(String channel) {
        mResultHolder.invokeUpdate();
        Round r = mTour.getCurrentRound();
        if (r.isResultSet()) {
            mResultHolder.invokeSave();
            showSystemMessage(r.sn + "回戦終了ー");
            sendSystemNotice(channel, r.sn + "回戦の全試合が終了しました。");
            mAgent.setTopic(channel, r.sn + "回戦終了");
        }
    }

    private void handleRegisterDeck(String channel, String sender, String deckString) {
        Participant p = mTour.getParticipant(sender);
        if (p == null) {
            showSystemMessage(sender + " は参加してないわよ。この歳でもうボケたのかしら。");
            sendSystemMessage(channel, sender + "様は参加しておりません。");
            return;
        }

        String deck = fixDeck(deckString);
        if (deck.length() > 1) {
            p.deck = deck;
            showSystemMessage(sender + " のデッキは、" + p.deck + "ね。");
            sendSystemNotice(channel, sender + "様のデッキ「Ｌ" + p.deck + "」を登録致しました。");
        } else {
            showSystemMessage(sender + "・・・まともに登録しなさいよ！");
            sendSystemNotice(channel, sender + "様のデッキを理解できません。もっと文明的な書き方をお願いします。");
        }
        mCaller.invokeUpdate();
    }

    private String fixDeck(String deck) {
        String ret = "";
        HashMap<String, Integer> map = new HashMap<String, Integer>(20);
        String leader = "";
        int leaderindex = 99;
        /*Pattern pattern = Pattern.compile("[0-9]");
        String[] chars = pattern.split(deck);*/
        for (String[] character : CharNames.chars) {
            for (String s : character) {
                if (map.get(character[0]) == null) {
                    map.put(character[0], 0);
                }
                int index = deck.indexOf(s);
                if (index >= 0) {
                    try {
                        map.put(character[0], map.get(character[0]) + Integer.parseInt(deck.substring(index + s.length(), index + s.length() + 1)));
                    } catch (NumberFormatException e) {
                        map.put(character[0], map.get(character[0]) + 1);
                    }
                    if (index < leaderindex) {
                        leader = character[0];
                        leaderindex = index;
                    }
                    break;
                }
            }
        }

        // Add Leader
        for (String s : map.keySet()) {
            if (s.equals(leader)) {
                ret += s + map.get(s) + "：";
            }
        }
        // Add others
        for (String[] character : CharNames.chars) {
            String s = character[0];
            if (!s.equals(leader) && map.get(s) > 0) {
                ret += s + map.get(s) + "：";
            }
        }

        if (ret.length() > 1) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    private void handleListResults(String channel) {
        Round r = mTour.getCurrentRound();
        if (r == null) {
            showSystemMessage("せっかちな人ね。それじゃ嫌われるわよ？");
            sendSystemMessage(channel, "まだ試合は始まっていません。");
            return;
        }

        int done = 0;
        int left = 0;
        showSystemMessage("＠結果によるログ流し始まるよー");
        for (Table t : r.getTables()) {
            if (t.isResultSet()) {
                sendSystemNotice(channel, t.getIRCResultString());
                ++done;
            } else {
                ++left;
            }
        }
        sendSystemNotice(channel, "完了 " + done + " 卓、進行中 " + left + " 卓です。");
    }

    private void handleListLeftTables(String channel) {
        Round r = mTour.getCurrentRound();
        if (r == null) {
            showSystemMessage("せっかちな人ね。それじゃ嫌われるわよ？");
            sendSystemMessage(channel, "まだ試合は始まっていません。");
            return;
        }

        int done = 0;
        int left = 0;
        showSystemMessage("＠残りによるログ流し始まるよー");
        for (Table t : r.getTables()) {
            if (t.isResultSet()) {
                ++done;
            } else {
                sendSystemNotice(channel, t.getIRCResultString() + " (" + t.getHostIP() + ")");
                ++left;
            }
        }
        sendSystemNotice(channel, "進行中 " + left + " 卓、完了 " + done + " 卓です。");
    }

    void handleListNotDefeated(String channel) {
        String message = "名）：";
        int cnt = 0;
        for (Participant p : mTour.participant.values()) {
            if (p.getLosses() == 0 && p.getDraws() == 0) {
                message += p.name + " ";
                cnt++;
            }
        }
        showSystemMessage("全勝者は" + cnt + "名よ。");
        sendSystemNotice(channel, "全勝者（" + cnt + message);
    }

    // End of Command Handling
    // Order Handling
    private void handleStartTournament(String channel, String sender, String tourname) {
        if (mTour.promoter.length() > 0) {
            showSystemMessage("今回のご主人様は" + mTour.promoter + " だわ。重婚は犯罪よ？");
            return;
        }

        mTour.start(tourname);
        if (mTour.name.length() < 1) {
            showSystemMessage("大会名を設定忘れてるわよー");
            sendSystemMessage(channel, "ご主人様、大会名をまだ設定しておりませんよ？");
            return;
        }
        mTour.promoter = sender;
        mCaller.invokeUpdate();
        showSystemMessage("今回のご主人様は" + sender + " ね。よろしくお願いするわ。");
        sendSystemMessage(channel, "これより、" + mTour.name + "の受付を開始致します。主催は" + mTour.promoter + "様です。皆様よろしくお願い致します。");
        mAgent.setTopic(channel, mTour.name + "（現在" + mTour.countParticipants() + "人）参加受付中です。参加者は「＠参加」と発言してくださいな。");
        mCaller.invokeUpdate();
    }

    private void handleStopTournament(String channel) {
        mOnRecipent = false;
        mTour.promoter = "";
        mProxyIP = "";
        showSystemMessage("お疲れ様ー");
        sendSystemNotice(channel, "これにて、" + mTour.name + "は無事終了致しました。皆様お疲れ様でした。");
        ArrayList<Participant> a = mTour.getStandingsArray();
        Collections.sort(a);
        Collections.reverse(a);
        mAgent.setTopic(channel, mTour.name + "は終了しました。優勝は" + a.get(0).name + "さんでした。");
    }

    private void handleSetRule(String order, String channel) {
        String[] s = order.split(" ");
        if (s.length <= 1) {
            return;
        }
        try {
            int index = Integer.parseInt(s[0].substring(3, 4));
            if (index > mRule.size()) {
                mRule.add(order.substring(order.indexOf(" ")));
                sendSystemNotice(channel, "ルール" + mRule.size() + "を設定しました。");
            } else {
                mRule.set(index, order.substring(order.indexOf(" ")));
                sendSystemNotice(channel, "ルール" + (index + 1) + "を変更しました。");
            }
        } catch (Exception e) {
            mRule.add(order.substring(order.indexOf(" ")));
            sendSystemNotice(channel, "ルール" + mRule.size() + "を設定しました。");
        }
        showSystemMessage(order.substring(order.indexOf(" ")) + "を設定したよー");
    }

    private void handleShowRule(String channel) {
        for (String s : mRule) {
            sendSystemNotice(channel, "・" + s);
        }
    }

    private void handleSetTime(String order, String channel) {
        String s = order.split(" ")[1];
        try {
            int time = Integer.parseInt(s);
            sRoundInterval = time;
            sendSystemNotice(channel, "試合時間を " + sRoundInterval + " 分に設定しました。");
            showSystemMessage("試合時間を " + sRoundInterval + " 分に設定しました。");
        } catch (NumberFormatException e) {
            sendSystemNotice(channel, "格式が違いますわ。「！試合時間　25」みたいに命令してくださいな。「分」は要りませんよ、強制ですからね。");
            showSystemMessage("我が主ながら呆れたわね～");
        }
    }

    private void handleShowResult(String channel) {
        ArrayList<Integer> ranking = new ArrayList<Integer>(mTour.countParticipants());
        ArrayList<Participant> a = mTour.getStandingsArray();
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
            nowpt = pt;
            nowopp = opp;
        }

        Collections.reverse(ranking);
        Collections.reverse(a);
        Iterator it = ranking.iterator();
        it = ranking.iterator();
        showSystemMessage("結果発表によるログ流し始まるよー");
        for (Participant p : a) {
            String sopp = String.valueOf(p.getOpp());
            if (sopp.length() > 5) {
                sopp = sopp.substring(0, 5);
            }
            sendSystemNotice(channel, it.next() + "位 " + p.name + " " + p.getPoint() + "pts Opp%:" + sopp);
        }

    }

    private void handleNextRound(String channel) {
        IIRCManagerResultSetCallback next = mCaller.nextRound();
        if (next == null) {
            showSystemMessage("結果入力はまだ終わって無いわよ。");
            sendSystemMessage(channel, "ご主人様、進行中ラウンドの結果入力がまだ終わっておりませんよ？");
            return;
        }

        mResultHolder = next;
        Round r = mTour.getCurrentRound();
        showSystemMessage("ペアリングによるログ流し始まるわよー");
        sendSystemNotice(channel, r.sn + "回戦のペアリングになります。");

        int table = 0;
        for (Table t : r.tables) {
            ++table;
            if (t.getP2() == null) {
                sendSystemNotice(channel, "Table" + table + " " + t.getP1().name + " BYE");
                continue;
            }
            String host;
            switch (t.host) {
                case Table.PLAYER1:
                    host = " Host:" + t.getP1().name + " (" + t.getP1().ip + ")";
                    break;
                case Table.PLAYER2:
                    host = " Host:" + t.getP2().name + " (" + t.getP2().ip + ")";
                    break;
                default:
                    try {
                        URL u = new File("FantasyFestaProxy.jar").toURI().toURL();
                        URLClassLoader cl = new URLClassLoader(new URL[]{u});
                        Class<?> proxy_clazz = Class.forName("org.sais.fantasyfestaproxy.ProxyMainFrame", true, cl);
                        Object proxy = proxy_clazz.getMethod("autoLaunch", (Class<?>[]) null).invoke(null, (Object[]) null);
                        Method getPortMethod = proxy_clazz.getMethod("getPorts", (Class<?>[]) null);
                        int[] port = (int[]) getPortMethod.invoke(proxy, (Object[]) null);

                        if (mProxyIP.length() > 0) {
                            host = " PROXY (" + mProxyIP + ": " + port[0] + "-" + port[1] + "/" + port[2] + ")";
                        } else {
                            host = " PROXY (" + mAgent.getInetAddress() + ": " + port[0] + "-" + port[1] + "/" + port[2] + ")";
                        }
                        break;
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "No proxy jar file found.", "Proxy Error", JOptionPane.ERROR_MESSAGE);
                        host = "Manual Proxy";
                        Logger.getLogger(IRCManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            sendSystemNotice(channel, "Table" + table + " " + t.getP1().name + " VS " + t.getP2().name + host);
        }

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, sRoundInterval);
        mAgent.setTopic(channel, r.sn + "回戦は" + (c.get(Calendar.HOUR_OF_DAY)) + ":" + c.get(Calendar.MINUTE) + "までです。");

    }

    private void handleShowDecks(String channel) {
        sendSystemNotice(channel, "上位陣のデッキリストです。");

        int cnt = 0;
        for (Participant par : mTour.getStandingsArray()) {
            ++cnt;
            if (par.deck.length() > 0) {
                sendSystemNotice(channel, par.name + " " + par.deck);
            } else {
                sendSystemNotice(channel, par.name + " 未登録");
            }
            if (cnt > mTour.getStandingsArray().size() / 3) {
                break;
            }
        }
    }

    private void handleStopRecipent(String channel) {
        mOnRecipent = false;
        sendSystemMessage(channel, "参加受付を締め切りました。");
        showSystemMessage("締め切ったわよ。さっさと始めましょ。");
        mAgent.setTopic(channel, mTour.name + "、間も無く開催いたします。");
    }

    private void handleRestartRecipent(String channel) {
        mOnRecipent = true;
        sendSystemMessage(channel, "参加受付を再開しました。");
        showSystemMessage("はいはい滑り込みお疲れ様。まぁ人が多いのはいいことだわ。");
        mAgent.setTopic(channel, mTour.name + "（現在" + mTour.countParticipants() + "人）参加受付中です。参加者は「＠参加」と発言してくださいな。");
    }

    // End of Order Handling
    private void showSystemMessage(String message) {
        mFrame.showMessage(message, new Color(0, 100, 140));
    }

    private void sendSystemMessage(String channel, String message) {
        mAgent.sendMessage(channel, message);
    }

    private void sendSystemNotice(String channel, String message) {
        mAgent.sendNotice(channel, message);
    }

    /**
     * Check if the message is a command.
     * @param message
     * @return the length of prefix. 0 if the message is not a command.
     */
    private int isCommand(String message) {
        for (String s : COMMAND_PREFIX) {
            if (message.startsWith(s)) {
                return s.length();
            }
        }
        return 0;
    }

    private int isOrder(String message) {
        for (String s : ORDER_PREFIX) {
            if (message.startsWith(s)) {
                return s.length();
            }
        }
        return 0;
    }

    public void setDebuggable(boolean debuggable) {
        mDebuggable = debuggable;
    }

    private void handleRestartTournament(String sender) {
        if (mTour.promoter.length() > 0) {
            showSystemMessage("今回のご主人様は" + mTour.promoter + " だわ。重婚は犯罪よ？");
            return;
        }
        mTour.start(mTour.name);
        mTour.promoter = sender;
        mCaller.invokeUpdate();
        return;
    }
}
