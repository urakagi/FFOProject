/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.net.socketthread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.Startup;
import org.sais.fantasyfesta.core.HostMessageDispatcher;
import org.sais.fantasyfesta.enums.EGameMode;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class HostSocketThread extends SocketThread {

    private ServerSocket mServer;
    private InetSocketAddress mAddress;
    private boolean ready = false;
    private boolean isApplicant = false;
    private WatcherManagerServer mWatcherManagerServer;
    private Socket mDeliverSocket;
    private Thread mDeliverThread;
    private boolean isConnected = true;

    public HostSocketThread(Startup parent, InetSocketAddress address) {
        try {
            System.out.println("I'm the Host.");
            mParent = parent;
            mDispatcher = new HostMessageDispatcher(this);
            mAddress = address;
            mServer = new ServerSocket();
            mServer.setReuseAddress(true);
            mServer.bind(address);
        } catch (IOException ex) {
            Logger.getLogger(HostSocketThread.class.getName()).log(Level.SEVERE, null, ex);
            showErrorMessage(ex.toString());
        }
    }

    public void sendCurrentState(Socket soc) {
        if (soc != null && core != null) {
            send(soc, "$CURRENT:" + core.currentState());
        }
    }

    @Override
    synchronized public void run() {
        try {
            // Only accept participants here!
            while (!isApplicant) {
                mSocket = mServer.accept();
                scan();
            }
            mServer.close();
            core = new GameCore(EGameMode.HOST, this, mAddress, mParent);
            core.launch();

            // Watcher
            mWatcherManagerServer = new WatcherManagerServer(mAddress.getPort(), mAddress.getPort() - 1);
            mWatcherManagerServer.start();
            mDeliverSocket = new Socket();
            mDeliverSocket.connect(new InetSocketAddress(mWatcherManagerServer.getToHostPort()));
            mDeliverThread = new Thread(new DeliverRunnable());
            mDeliverThread.start();

            ready = true;
            isConnected = true;
            scan();
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }

    }

    @Override
    public void scan() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "Unicode"));
            String instr;
            while ((instr = in.readLine()) != null) {
                instr = fix(instr);
                if (DEBUG_LEVEL >= 1) {
                    System.out.println("HostF " + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + "/ " + instr);
                }
                if (instr.startsWith("$IWANTTOPLAY:")) {
                    send(mSocket, "$YOUAREAPPLICANT:");
                    isApplicant = true;
                    return;
                }
                if (instr.startsWith("$IWANTTOWATCH:")) {
                    send(mSocket, "$NOGAMEYET:" + Startup.VERSION);
                    isApplicant = false;
                    mSocket.close();
                    return;
                }
                //Messages dispatch
                if (ready) {
                    deliverAll(instr, false);
                    mDispatcher.dispatch(instr);
                }
            }
        } catch (SocketException ex) {
            if (ex.getMessage().equals("Connection reset")) {
                showErrorMessage(FTool.getLocale(88));
            }
            isConnected = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void closeSocket() {
        try {
            if (mSocket != null) {
                if (isConnected) {
                    send(mSocket, "$DISCONNECT:");
                }
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();
                mSocket = null;
            }
            if (mServer != null) {
                mServer.close();
                mServer = null;
            }
            mDeliverThread = null;
            if (mDeliverSocket != null) {
                mDeliverSocket.close();
                mDeliverSocket = null;
            }
            if (mWatcherManagerServer != null) {
                mWatcherManagerServer.close();
                mWatcherManagerServer = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void send(Socket soc, String message) {
        deliverAll(message, true);
        super.send(soc, message);
    }

    public void sendWithoutDeliver(Socket soc, String message) {
        super.send(soc, message);
    }

    public void deliverAll(String message, boolean isHost) {
        if (mDeliverSocket == null) {
            return;
        }
        if (isHost) {
            super.send(mDeliverSocket, "$WATCHHOST:" + message);
        } else {
            super.send(mDeliverSocket, "$WATCHCLIENT:" + message);
        }
    }

    public void sendAllWithoutPrefix(String message) {
        if (mDeliverSocket == null) {
            return;
        }
        super.send(mSocket, message);
        super.send(mDeliverSocket, message);
    }

    /**
     * Send message to a particular watcher. No pre-command string is added.
     * @param message Message to send.
     * @param watcherNo Number of the indicated watcher.
     */
    public void deliverTo(String message, int watcherNo) {
        if (mDeliverSocket == null) {
            return;
        }
        super.send(mDeliverSocket, "#TO" + watcherNo + "*" + message);
    }

    /**
     * Send message to all watchers except indicated one. Pre-command string is added.
     * @param message Message to send.
     * @param watcherNo Number of the indicated watcher.
     */
    public void deliverExcept(String message, int watcherNo) {
        if (mDeliverSocket == null) {
            return;
        }
        super.send(mDeliverSocket, "#EX" + watcherNo + "*" + message);
    }

    /**
     * The Runnable interface to scan message from WatcherManagerServer.
     */
    class DeliverRunnable implements Runnable {

        @Override
        public void run() {
            scan();
        }

        public void scan() {
            try {
                Thread thisThread = Thread.currentThread();
                BufferedReader in = new BufferedReader(new InputStreamReader(mDeliverSocket.getInputStream(), "Unicode"));
                String instr;
                while (((instr = in.readLine()) != null) && thisThread == mDeliverThread) {
                    instr = fix(instr);
                    if (DEBUG_LEVEL >= 1) {
                        System.out.println("Deliver.Run " + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + " / " + instr);
                    }
                    mDispatcher.dispatch(instr);
                }
            } catch (SocketException ex) {
                System.out.println("Connection reset.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
