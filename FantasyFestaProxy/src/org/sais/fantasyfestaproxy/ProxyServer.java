/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfestaproxy;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyServer extends Thread {

    public static final int DEBUG_LEVEL = 1;
    private ProxyMainFrame mParent;
    private SocketThread mVHostHSock;
    private SocketThread mVClientHSock;
    private ServerSocket mWatchersServerSocket;
    //private ArrayList<ToWatcherTSock> mToWatcherSocket = new ArrayList<ToWatcherTSock>();
    private ArrayList<Watcher> mWatchers = new ArrayList<Watcher>();
    private boolean mAccept = true;
    private Timer mTimeout;
    private boolean mGrade = false;

    public ProxyServer(ProxyMainFrame parent, int VHostPort, int VClientPort, int watcherPort, boolean grade) {
        mParent = parent;
        mGrade = grade;
        mVHostHSock = new SocketThread(true, VHostPort);
        mVClientHSock = new SocketThread(false, VClientPort);
        int shift = 0;
        while (shift < 1000) {
            try {
                mWatchersServerSocket = new ServerSocket(watcherPort + shift);
                mParent.setWatcherPort(watcherPort + shift);
                break;
            } catch (IOException ex) {
                ++shift;
            }
        }
        StringSelection ss = new StringSelection(System.currentTimeMillis() + " " + mVHostHSock.getPort() + " " + mVClientHSock.getPort() + " " + (watcherPort + shift));
        Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
        cp.setContents(ss, null);
        
        // 10 minutes proxy timeout
        mTimeout = new Timer();
        mTimeout.schedule(new TimerTask() {

            @Override
            public void run() {
                mParent.dispose();
            }
        }, 10 * 60000);
        
        mVHostHSock.start();
        mVClientHSock.start();
        start();
    }

    /**
     * Acts as a WatcherAccepter.
     */
    @Override
    public void run() {
        try {
            while (mAccept) {
                Socket incomingSoc;
                incomingSoc = mWatchersServerSocket.accept();
                if (!mVClientHSock.isConnected() || !mVHostHSock.isConnected()) {
                    send(incomingSoc, "$NOGAMEYET:");
                    continue;
                }
                int id = 0;
                if (mWatchers.size() > 0) {
                    id = mWatchers.get(mWatchers.size() - 1).getId() + 1;
                }
                ToWatcherTSock newWatcherSock = new ToWatcherTSock(incomingSoc, id);
                Watcher newWatcher = new Watcher("", id, newWatcherSock);
                mParent.setWatcherState(mWatchers.size() + " watchers.");
                mWatchers.add(newWatcher);
                send(incomingSoc, "$YOUAREWATCHER:");
                newWatcherSock.start();
            }
        } catch (IOException ex) {
            return;
        }
    }

    public void close() {
        try {
            mAccept = false;
            mWatchersServerSocket.close();
            mVClientHSock.close();
            mVHostHSock.close();
        } catch (IOException ex) {
            Logger.getLogger(ProxyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeSocket() {
        mAccept = false;
        for (Watcher s : mWatchers) {
            s.closeSocket();
        }
    }
    private static final int SOURCE_VHOST = 0;
    private static final int SOURCE_VCLIENT = 1;
    private static final int SOURCE_WATCHER = 2;

    /**
     * Deliver messages.
     * @param toVHost true to deliver message to the virtual host.
     * @param toVClient true to deliver message to the virtual client.
     * @param toWatchers true to deliver message to watchers.
     * @param only -1 to deliver to multiple watchers, or the watcher index to deliver message only the that watcher.
     * @param exclude -1 to deliver to all watchers, or the watcher index to deliver message except the that watcher.
     * @param source The source of this message.
     */
    private void deliver(String message, boolean toVHost, boolean toVClient, boolean toWatchers, int source) {
        if (toVHost) {
            if (mVHostHSock.getSocket() != null) {
                send(mVHostHSock.getSocket(), message);
            }
        }
        if (toVClient) {
            if (mVClientHSock.getSocket() != null) {
                send(mVClientHSock.getSocket(), message);
            }
        }
        if (toWatchers) {
            int watcherNo = -1;
            boolean exclude = false;
            if (message.indexOf("#TO") >= 0) {
                watcherNo = Integer.parseInt(message.substring(message.indexOf("#TO") + 3, message.indexOf("*")));
                exclude = false;
            } else if (message.indexOf("#EX") >= 0) {
                watcherNo = Integer.parseInt(message.substring(message.indexOf("#EX") + 3, message.indexOf("*")));
                exclude = true;
            }

            int pos = message.indexOf("$");
            if (pos >= 0) {
                message = message.substring(pos);
            } else {
                message = message.substring(message.indexOf("*") + 1);
            }
            try {
                for (Watcher w : mWatchers) {
                    ToWatcherTSock s = w.getSock();
                    if (!s.isConnecting) {
                        continue;
                    }
                    if (watcherNo >= 0) {
                        if ((exclude && s.mNo == watcherNo) || (!exclude && s.mNo != watcherNo)) {
                            continue;
                        }
                    }
                    switch (source) {
                        case SOURCE_VHOST:
                            if (!message.contains("$CURRENT:") && !message.contains("$WATCHCHAT:")) {
                                send(s.getSocket(), "$WATCHHOST:" + message);
                            } else {
                                send(s.getSocket(), message);
                            }
                            break;
                        case SOURCE_VCLIENT:
                            send(s.getSocket(), "$WATCHCLIENT:" + message);
                            break;
                        case SOURCE_WATCHER:
                        default:
                            send(s.getSocket(), message);
                            break;
                    }

                }
            } catch (ConcurrentModificationException e) {
            }

        }
    }

    public void send(Socket soc, String message) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(), "Unicode"));
            out.write(message);
            out.newLine();
            out.flush();
        } catch (SocketException ex) {
            return;
        } catch (Exception ex) {
            return;
        }
    }

    public static String fix(String instr) {
        for (int i = 0; i
                < instr.length(); ++i) {
            if (instr.charAt(i) == '$' || instr.charAt(i) == '#') {
                return instr.substring(i);
            }
        }
        return instr;
    }

    private class SocketThread extends Thread {

        private boolean isToVHost;
        private ServerSocket mServerSocket;
        private Socket mSocket;
        private int mPort;

        public SocketThread(boolean isHost, int port) {
            isToVHost = isHost;
            int shift = 0;
            while (shift < 1000) {
                try {
                    mServerSocket = new ServerSocket(port + shift);
                    if (isToVHost) {
                        mParent.setVHostPort(port + shift);
                    } else {
                        mParent.setVClientPort(port + shift);
                    }
                    break;
                } catch (IOException ex) {
                    ++shift;
                }
            }
            mPort = port + shift;
        }

        public Socket getSocket() {
            return mSocket;
        }

        public int getPort() {
            return mPort;
        }

        public boolean isConnected() {
            return !(mSocket == null);
        }

        @Override
        public void run() {
            try {
                if (mServerSocket == null) {
                    return;
                }
                mSocket = mServerSocket.accept();
                mSocket.setKeepAlive(true);
                if (isToVHost) {
                    mParent.setVHostState("Connected.");
                    if (mVClientHSock.isConnected()) {
                        send(mSocket, "$VIRTUALHOST:");
                        send(mVClientHSock.getSocket(), "$YOUAREAPPLICANT:");
                        mTimeout.cancel();
                    } else {
                        send(mSocket, "$PROXYCONNECTED:");
                    }
                } else {
                    mParent.setVClientState("Connected.");
                    if (mVHostHSock.isConnected()) {
                        send(mVHostHSock.getSocket(), "$VIRTUALHOST:");
                        send(mSocket, "$YOUAREAPPLICANT:");
                        mTimeout.cancel();
                    } else {
                        send(mSocket, "$PROXYCONNECTED:");
                    }
                }
                scan();
                return;
            } catch (IOException ex) {
                return;
            }
        }

        private void scan() {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "Unicode"));
                String ins;
                while ((ins = in.readLine()) != null) {
                    ins = fix(ins);
                    mParent.insertMessage(ins, isToVHost ? Color.BLUE : Color.RED);
                    if (ins.startsWith("$WATCHER:")) {
                        continue;
                    }
                    if (isToVHost) {
                        deliver(ins, !isToVHost, isToVHost, true, SOURCE_VHOST);
                    } else {
                        deliver(ins, !isToVHost, isToVHost, true, SOURCE_VCLIENT);
                    }
                }
            } catch (Exception ex) {
                // Falls down
            }
            mAccept = false;
            if (isToVHost) {
                deliver("$DISCONNECT:", false, true, false, SOURCE_VHOST);
                mParent.setVHostState("Disconnected.");
            } else {
                deliver("$DISCONNECT:", true, false, false, SOURCE_VCLIENT);
                mParent.setVClientState("Disconnected.");
            }
        }

        public void close() {
            try {
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
                mServerSocket = null;
                if (mSocket != null) {
                    mSocket.shutdownInput();
                    mSocket.shutdownOutput();
                    mSocket.close();
                    mSocket = null;
                }
            } catch (IOException ex) {
                Logger.getLogger(ProxyServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * The dependent Sock to communicate with a watcher.
     */
    class ToWatcherTSock extends Thread {

        public int mNo;
        private Socket mSocket;
        private boolean isConnecting = true;

        public ToWatcherTSock(Socket soc, int no) {
            mSocket = soc;
            mNo = no;
        }

        public Socket getSocket() {
            return mSocket;
        }

        public boolean isConnecting() {
            return isConnecting;
        }

        public void closeSocket() {
            try {
                mSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            scan();
        }

        public void scan() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "Unicode"));
                String ins;
                while ((ins = in.readLine()) != null) {
                    ins = fix(ins);
                    ins = "#" + mNo + ins;
                    mParent.insertMessage(ins, Color.BLACK);

                    int watcherNo = -1;
                    if (ins.startsWith("#") && !ins.startsWith("#TO") && !ins.startsWith("#EX")) {
                        watcherNo = Integer.parseInt(ins.substring(1, ins.indexOf("$")));
                        if (ins.indexOf("$") >= 0) {
                            ins = ins.substring(ins.indexOf("$"));
                        }
                    }

                    String cmd;
                    cmd = "$WATCHER:";
                    if (ins.startsWith(cmd)) {
                        deliver(cmd + mNo + " " + ins.substring(cmd.length()), true, true, false, SOURCE_WATCHER);
                        deliver("#EX" + watcherNo + "*" + ins, false, false, true, SOURCE_WATCHER);
                        continue;
                    }
                    cmd = "$DISCONNECT:";
                    if (ins.startsWith(cmd)) {
                        for (Watcher w : mWatchers) {
                            if (watcherNo == w.getId()) {
                                w.getSock().isConnecting = false;
                            }
                        }
                        deliver("$STOPWATCH:" + watcherNo, true, true, true, SOURCE_WATCHER);
                        continue;
                    }
                    cmd = "$WATCHCHAT:";
                    if (ins.startsWith(cmd)) {
                        ins = fix(ins);
                        ins = ins.substring(ins.indexOf("$"));
                        deliver("#EX" + watcherNo + "*" + ins, false, false, true, SOURCE_WATCHER);
                        deliver(ins, true, true, false, SOURCE_WATCHER);
                        continue;
                    }
                    cmd = "$GIVEMECURRENT:";
                    if (ins.startsWith(cmd)) {
                        deliver("#" + watcherNo + ins, true, false, false, SOURCE_WATCHER);
                        continue;
                    }
                    cmd = "$WATCHERPING:";
                    if (ins.startsWith(cmd)) {
                        deliver("#TO" + watcherNo + "*$ACK:", false, false, true, SOURCE_WATCHER);
                        continue;
                    }

                    deliver(ins, true, true, true, SOURCE_WATCHER);
                }
            } catch (Exception ex) {
                isConnecting = false;
                deliver("$STOPWATCH:" + mNo, true, true, true, SOURCE_WATCHER);
            }

        }
        
    }
}
