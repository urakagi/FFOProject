/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.net.socketthread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import org.sais.fantasyfesta.core.Startup;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class WatcherManagerServer extends SocketThread {

    private ServerSocket mServerSocket;
    /**
     * The socket to recieve data from the host's HostSock.
     */
    private ServerSocket mToHostServerSocket;
    private Socket mToHostSocket;
    /**
     * The socket to data to watchers' ClientSock.
     */
    private ArrayList<ToWatcherFSock> mToWatcherSocket = new ArrayList<ToWatcherFSock>();
    private boolean mAccept = true;

    public WatcherManagerServer(int listenport, int hostport) {
        try {
            mServerSocket = new ServerSocket(listenport);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int failCount = 0;
        while (failCount < 10) {
            try {
                mToHostServerSocket = new ServerSocket(hostport - failCount);
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
                ++failCount;
            }
        }
    }

    @Override
    public void scan() {
    }

    public int getToHostPort() {
        return mToHostServerSocket.getLocalPort();
    }

    class WatcherAccepter extends Thread {

        @Override
        public void run() {
            try {
                while (mAccept) {
                    Socket incomingSoc;
                    incomingSoc = mServerSocket.accept();
                    ToWatcherFSock newWatcher = new ToWatcherFSock(incomingSoc, mToWatcherSocket.size());
                    mToWatcherSocket.add(newWatcher);
                    send(incomingSoc, "$YOUAREWATCHER:" + Startup.VERSION);
                    newWatcher.start();
                }
            } catch (IOException ex) {
                System.out.println("Connection reset.");
                return;
            }
        }
    }

    @Override
    public void run() {
        new WatcherAccepter().start();
        try {
            mToHostSocket = mToHostServerSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(mToHostSocket.getInputStream(), "Unicode"));
            String ins;
            while ((ins = in.readLine()) != null) {
                if (DEBUG_LEVEL >= 2) {
                    System.out.println("W.Server " + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + " / " + ins);
                }
                int watcherNo = -1;
                boolean exclude = false;
                if (ins.indexOf("#TO") >= 0) {
                    watcherNo = FTool.safeParseInt(ins.substring(ins.indexOf("#TO") + 3, ins.indexOf("*")));
                    exclude = false;
                } else if (ins.indexOf("#EX") >= 0) {
                    watcherNo = FTool.safeParseInt(ins.substring(ins.indexOf("#EX") + 3, ins.indexOf("*")));
                    exclude = true;
                }

                int pos = ins.indexOf("$");
                if (pos >= 0) {
                    ins = ins.substring(pos);
                } else {
                    ins = ins.substring(ins.indexOf("*") + 1);
                }
                try {
                    for (ToWatcherFSock s : mToWatcherSocket) {
                        if (!s.isConnecting) {
                            continue;
                        }
                        if (watcherNo >= 0) {
                            if ((exclude && s.mNo == watcherNo) || (!exclude && s.mNo != watcherNo)) {
                                continue;
                            }
                        }
                        send(s.getSocket(), ins);
                    }
                } catch (ConcurrentModificationException e) {
                }
            }
        } catch (SocketException ex) {
            System.out.println("Connection Reset");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void close() {
        try {
            closeSocket();
            mAccept = false;
            mServerSocket.setReuseAddress(true);
            mServerSocket.close();
            mToHostServerSocket.setReuseAddress(true);
            mToHostServerSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void closeSocket() {
        try {
            mAccept = false;
            for (ToWatcherFSock s : mToWatcherSocket) {
                s.closeSocket();
            }
            mToHostSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The dependent Sock to communicate with a watcher.
     */
    private class ToWatcherFSock extends SocketThread {

        public int mNo;
        private boolean isConnecting = true;

        public ToWatcherFSock(Socket soc, int no) {
            mSocket = soc;
            mNo = no;
        }

        @Override
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

        @Override
        public void scan() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "Unicode"));
                String instr;
                while ((instr = in.readLine()) != null) {
                    instr = fix(instr);
                    instr = "#" + mNo + instr;
                    if (DEBUG_LEVEL >= 1) {
                        System.out.println("ToWatcherF " + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + " / " + instr);
                    }
                    // Exclude failed applicant
                    if (instr.startsWith("$IWANTTOPLAY:")) {
                        closeSocket();
                        break;
                    }
                    send(mToHostSocket, instr);
                }
            } catch (Exception ex) {
                isConnecting = false;
                send(mToHostSocket, "#" + mNo + "$DISCONNECT:");
            }

        }

        public boolean isConnecting() {
            return isConnecting;
        }
    }
}
