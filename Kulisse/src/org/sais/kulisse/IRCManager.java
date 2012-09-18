/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import org.sais.kulisse.gradeleague.GradeManager;

/**
 *
 * @author Romulus
 */
public class IRCManager {

    public static final String[] COMMAND_PREFIX = new String[]{"＠", "@"};
    public static final String[] ORDER_PREFIX = new String[]{"！", "!"};
    private static final String CLIENT_ONLY = "クラ専";
    private IRCWatchFrame mFrame;
    private IRCAgent mAgent;
    private IIRCManagerCallback mCaller;
    private ArrayList<Recruit> mRecruits = new ArrayList<Recruit>();
    private String mChannel = null;
    private ArrayList<Object> mProxies = new ArrayList<Object>();
    private GradeManager mGradeManager;

    public IRCManager(IIRCManagerCallback caller) {
        mCaller = caller;
        mFrame = new IRCWatchFrame(this);
        try {
            mGradeManager = new GradeManager();
        } catch (IOException e) {
            mFrame.showMessage("Error loading grade manager!", Color.RED);
            e.printStackTrace();
        }
    }

    public void setAgent(IRCAgent agent) {
        mAgent = agent;
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message, boolean isNotice) {
        if (mChannel == null) {
            mChannel = mFrame.getChannel();
        }
        try {
            message = new String(message.getBytes("UTF-8"), "UTF-8").replace('\002', ' ').replace("  ", " ");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        if (isNotice && sender.startsWith("irc_utage")) {
            int index3 = message.indexOf("で対戦募集中");
            if (index3 >= 0) {
                int index1 = message.indexOf("さんが");
                String player = message.substring(0, index1);
                if (getRecruitByNick(player) != null) {
                    mRecruits.remove(getRecruitByNick(player));
                }
                int index2 = index1 + 4;
                String ip = message.substring(index2, index3 - 1);
                String extra = "";
                if (message.length() > index3 + 7) {
                    int index5 = message.indexOf("※");
                    if (index5 > 0) {
                        extra = message.substring(index3 + 7, index5);
                    } else {
                        extra = message.substring(index3 + 7);
                    }
                }
                mRecruits.add(new Recruit(ip, player, System.currentTimeMillis(), extra));
                showSystemMessage("ぼ、募集を確認しました！プレイヤーは" + player + "様、ＩＰは " + ip + " です！");
            }
            int index6 = message.indexOf("さんがホスト募集中");
            if (index6 > 0) {
                String player = message.substring(0, index6);
                if (getRecruitByNick(player) != null) {
                    mRecruits.remove(getRecruitByNick(player));
                }
                String ip = CLIENT_ONLY;
                String extra = "";
                if (message.length() > index6 + 10) {
                    int index7 = message.indexOf("※");
                    if (index7 > 0) {
                        extra = message.substring(index6 + 10, index7);
                    } else {
                        extra = message.substring(index6 + 10);
                    }
                }
                mRecruits.add(new Recruit(ip, player, System.currentTimeMillis(), extra));
                showSystemMessage("クライアント専門の募集を確認しました！プレイヤーは" + player + "様です！");
            }
            int index4 = message.indexOf("さんが対戦募集を締め切りました");
            if (index4 > 0) {
                String nick = message.substring(0, index4);
                Recruit rc = getRecruitByNick(nick);
                if (rc != null) {
                    mRecruits.remove(rc);
                }
            }
        }

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

    private void dispatchCommand(String channel, String sender, String login, String hostname, String command) {
        command = command.replace("　", " ");

        if (command.startsWith("卓リスト")) {
            confirmRaises(true);
            return;
        }
        if (command.startsWith("募集リスト")) {
            confirmRaises(false);
            return;
        }
        if (command.startsWith("クリプロ")) {
            try {
                int amount;
                try {
                    amount = Integer.parseInt(command.substring(4).trim());
                } catch (NumberFormatException ex) {
                    amount = 1;
                }

                InetAddress addr = InetAddress.getLocalHost();
                String myIP = addr.getHostAddress();

                for (int i = 0; i < amount; ++i) {
                    URL u = new File("FantasyFestaProxy.jar").toURI().toURL();
                    URLClassLoader cl = new URLClassLoader(new URL[]{u});
                    Class<?> proxy_clazz = Class.forName("org.sais.fantasyfestaproxy.ProxyMainFrame", true, cl);
                    Object proxy = proxy_clazz.getMethod("autoLaunch", (Class<?>[]) null).invoke(null, (Object[]) null);
                    Method getPortMethod = proxy_clazz.getMethod("getPorts", (Class<?>[]) null);
                    int[] port = (int[]) getPortMethod.invoke(proxy, (Object[]) null);

                    mProxies.add(proxy);
                    sendSystemNotice(channel, myIP + ":" + port[0] + "-" + port[1] + "/" + port[2] + " にてプロキシ貸し出し中");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            return;
        }
        if (command.startsWith("クリッセプロキシリスト")) {
            for (int index = 0; index < mProxies.size();) {
                try {
                    InetAddress addr = InetAddress.getLocalHost();
                    String myIP = addr.getHostAddress();
                    URL u = new File("FantasyFestaProxy.jar").toURI().toURL();
                    URLClassLoader cl = new URLClassLoader(new URL[]{u});
                    Class<?> proxy_clazz = Class.forName("org.sais.fantasyfestaproxy.ProxyMainFrame", true, cl);
                    Method getPortMethod = proxy_clazz.getMethod("getPorts", (Class<?>[]) null);
                    int[] port = (int[]) getPortMethod.invoke(mProxies.get(index), (Object[]) null);
                    sendSystemNotice(channel, myIP + ": " + port[0] + "-" + port[1] + "/" + port[2] + " にてプロキシ貸し出し中");
                } catch (Exception ex) {
                    mProxies.remove(index);
                    continue;
                }
                ++index;
            }
            return;
        }
        if (command.startsWith("段位戦")) {
            String[] s = command.split(" ");
            String name = sender.replaceAll("_*$", "");
            if (s.length == 1) {
                sendSystemNotice(channel, "現在の段位戦の情報は http://220.130.10.99/players.html を参照してください。");
                return;
            }
            if (s[1].contains("参加")) {
                try {
                    mGradeManager.newPlayer(name);
                    sendSystemNotice(channel, name + "さんの参加を受け付けました。御健闘を祈っております。");
                } catch (IOException e) {
                    sendSystemNotice(channel, "データの書き込みに問題が発生しました。もう一度参加してみてください。");
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    sendSystemNotice(channel, e.getMessage() + "さんはすでに参加しております。");
                }
                return;
            }
            if (s[1].contains("ルール")) {
                sendSystemNotice(channel, "段位戦のルールは http://www39.atwiki.jp/gensouutage_net/pages/10807.html を参照してください。");
                return;
            }
            if (s[1].contains("リロード")) {
                try {
                    mGradeManager = new GradeManager();
                    sendSystemNotice(channel, "データをリロードしました。");
                } catch (IOException ex) {
                    sendSystemNotice(channel, "データの書き込みに問題が発生しました。もう一度リロードしてみてください。");
                }
                return;
            }
            if (s[1].contains("優勝")) {
                try {
                    String winner = s[2];
                    String tour = s[3];
                    mGradeManager.addChampion(winner, tour);
                    sendSystemNotice(channel, winner + "さんの優勝を記録しました。");
                } catch (ArrayIndexOutOfBoundsException e) {
                    sendSystemNotice(channel, "「＠段位戦　優勝　nick　大会名」で入力して下さい。");
                } catch (IOException e) {
                    sendSystemNotice(channel, "データの書き込みに問題が発生しました。もう一度報告してみてください。");
                }
                return;
            }
            if (s[1].contains("成績")) {
                sendSystemNotice(channel, mGradeManager.getScoreString(name));
                return;
            }
            if (s.length < 3) {
                sendSystemNotice(channel, "報告は「＠段位戦 勝ち （相手のニック）」という形でお願いします。");
                return;
            }
            String opp = s[2];
            if (name.equals(opp)) {
                sendSystemNotice(channel, "報告は「＠段位戦 勝ち （相手のニック）」という形でお願いします。");
                return;
            }
            try {
                if (s[1].contains("勝")) {
                    report(channel, name, opp);
                } else if (s[1].contains("負")) {
                    report(channel, opp, name);
                } else if (s[1].contains("チェック")) {
                    mGradeManager.check(name, opp);
                    sendSystemNotice(channel, "段位戦：" + name + "さんと" + opp + "さんは対戦可能です。");
                } else if (s[1].contains("キャンセル")) {
                    mGradeManager.cancel(name, opp);
                    sendSystemNotice(channel, "段位戦：" + name + "さんと" + opp
                            + "さんの対戦記録を消しました。級位でポイントが０の場合、キャンセルに不具合が出るかもしれませんが、その時は主催者にご連絡下さい。");
                } else {
                    sendSystemNotice(channel, "報告は「＠段位戦　勝ち　（相手のニック）」という形でお願いします。勝負が書かれていません。");
                    return;
                }
            } catch (IOException e) {
                sendSystemNotice(channel, "データの書き込みに問題が発生しました。もう一度報告してみてください。");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                sendSystemNotice(channel, e.getMessage());
            }
        }
    }

    private void report(String channel, String winner, String loser) throws IOException {
        int winPreGrade = mGradeManager.getStandings().getPlayer(winner).getGrade();
        int losePreGrade = mGradeManager.getStandings().getPlayer(loser).getGrade();
        mGradeManager.addGame(winner, loser);
        sendSystemNotice(channel, "記録しました。"
                + "リプレイは " + mGradeManager.getRepalyPageName(winner, loser) + " に貼って下さい。");
        int winAfterGrade = mGradeManager.getStandings().getPlayer(winner).getGrade();
        int loseAfterGrade = mGradeManager.getStandings().getPlayer(loser).getGrade();
        String text = "";
        if (winAfterGrade > winPreGrade) {
            text += winner + "さん、" + mGradeManager.getStandings().getPlayer(winner).getGradeString()
                    + "への昇格おめでとうございます！";
        } else {
            text += mGradeManager.getScoreString(winner);
        }
        text += mGradeManager.getScoreString(loser);
        sendSystemNotice(channel, text);
    }

    private void dispatchOrder(String channel, String sender, String login, String hostname, String order) {
        order = order.replace("　", " ");

        if (order.startsWith("")) {
            return;
        }

    }

    // End of Order Handling
    // Command Handling
    private void confirmRaises(boolean showPlaying) {
        if (mRecruits.isEmpty()) {
            sendSystemNotice(mChannel, "募集及び進行中の卓はございません。");
            showSystemMessage("卓はありませんよー");
            return;
        }
        ArrayList<Recruit> confirm = new ArrayList<Recruit>();
        ArrayList<Recruit> remove = new ArrayList<Recruit>();
        for (Recruit rc : mRecruits) {
            if (rc.ip.equals(CLIENT_ONLY)) {
                if (System.currentTimeMillis() - rc.time > 1800000) {
                    remove.add(rc);
                } else {
                    success(rc, "募集中 (" + rc.nick + ") / ");
                }
                continue;
            }
            confirm.add(rc);
        }
        for (Recruit rc : confirm) {
            new ConfirmThread(rc, showPlaying).start();
        }
        Collections.synchronizedList(mRecruits).removeAll(remove);
        if (mRecruits.isEmpty()) {
            sendSystemNotice(mChannel, "募集及び進行中の卓はございません。");
            showSystemMessage("卓がなくなりましたよー");
        }
    }

    void success(Recruit recruit, String message) {
        // Difference in minutes
        long diff = (System.currentTimeMillis() - recruit.time) / 60000;
        sendSystemNotice(mChannel, message + recruit.ip + " / " + diff + "分前 / " + recruit.extraMessage);
        showSystemMessage(message + recruit.ip + " / " + diff + "分前 / " + recruit.extraMessage);
    }

    void fail(Recruit recruit) {
        Collections.synchronizedList(mRecruits).remove(recruit);
        showSystemMessage(recruit.nick + "様のIPを消去しま、ました！");
        if (mRecruits.isEmpty()) {
            sendSystemNotice(mChannel, "募集及び進行中の卓はございません。");
            showSystemMessage("卓がなくなりましたよー");
        }
    }

    // End of Command Handling
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

    private Recruit getRecruitByNick(String nick) {
        try {
            for (Recruit rc : mRecruits) {
                if (rc.nick.equals(nick)) {
                    return rc;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    class ConfirmThread extends Thread {

        Recruit recruit;
        boolean showPlaying;

        public ConfirmThread(Recruit rc, boolean showPlaying) {
            this.recruit = rc;
            this.showPlaying = showPlaying;
        }

        @Override
        @SuppressWarnings("empty-statement")
        public void run() {
            try {
                final Socket soc = new Socket(recruit.ip, 12700);
                Timer timeout = new Timer();
                timeout.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(), "Unicode"));
                            out.write("$IWANTTOWATCH:");
                            out.newLine();
                            out.flush();
                        } catch (SocketException ex) {
                            System.out.println(ex.getMessage());
                            return;
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, ex.getClass() + "\n" + ex.getMessage(), "send() in Sock", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }, 1000);
                BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream(), "Unicode"));
                String s;
                for (s = in.readLine(); !s.contains("$"); s = in.readLine());
                if (s.contains("GAMEYET")) {
                    String version = s.substring(s.indexOf(":") + 1);
                    if (version.length() == 0) {
                        version = "7.x.x";
                    }
                    success(recruit, "募集中 (" + recruit.nick + ") / V" + version + " / ");
                } else if (s.contains("$YOUAREWATCHER:")) {
                    String version = s.substring(s.indexOf(":") + 1);
                    String name0, name1;
                    if (version.length() == 0) {
                        version = "7.x.x";
                        name0 = "!NAME_0=->";
                        name1 = "!NAME_1=->";
                    } else {
                        name0 = "!NAME_ICH=->";
                        name1 = "!NAME_OPP=->";
                    }
                    if (showPlaying) {
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(), "Unicode"));
                        out.write("$GIVEMECURRENT:");
                        out.newLine();
                        out.flush();
                        for (s = in.readLine(); !s.contains("$CURRENT:"); s = in.readLine());
                        int index1 = s.indexOf(name1);
                        int index2 = s.indexOf("::!", index1);
                        String oppname = s.substring(index1 + name1.length(), index2);
                        int index3 = s.indexOf(name0);
                        int index4 = s.indexOf("::!", index3);
                        String plname = s.substring(index3 + name0.length(), index4);
                        success(recruit, "対戦中 (" + plname + " vs " + oppname + ") / V" + version + " / ");
                    }
                } else {
                    fail(recruit);
                }
                in.close();
                soc.close();
            } catch (UnknownHostException ex) {
                fail(recruit);
            } catch (IOException ex) {
                fail(recruit);
            }

        }
    }
}
