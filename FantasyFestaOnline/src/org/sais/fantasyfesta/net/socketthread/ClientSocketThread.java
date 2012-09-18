/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.net.socketthread;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.fantasyfesta.core.Startup;
import org.sais.fantasyfesta.core.ClientMessageDispatcher;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.enums.EGameMode;
import org.sais.fantasyfesta.recorder.Recorder;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class ClientSocketThread extends SocketThread {

    protected boolean scan = true;
    public boolean isRico = false;
    protected boolean isVitualHost = false;
    protected Recorder mRecorder;
    protected EGameMode mMode = EGameMode.CLIENT;
    protected boolean isGotCurrent = false;
    protected ArrayList<String> mSuspendedMessages = new ArrayList<String>();
    protected InetSocketAddress mAddress;

    public ClientSocketThread(Startup parent, InetSocketAddress address, boolean iamwatcher) {
        try {
            System.out.println("I'm a Client.");
            this.mParent = parent;
            mMode = iamwatcher ? EGameMode.WATCHER : EGameMode.CLIENT;
            this.mAddress = address;
            mSocket = new Socket();
            mSocket.connect(address, 12000);
            if (iamwatcher) {
                send(mSocket, "$IWANTTOWATCH:");
            } else {
                send(mSocket, "$IWANTTOPLAY:");
            }
        } catch (IOException ex) {
            showErrorMessage(ex.toString());
            parent.setMessage("Failed to connect to host - " + ex.getMessage());
        }
    }

    public ClientSocketThread(Recorder recorder, InetSocketAddress address, boolean isHost) {
        try {
            mMode = EGameMode.RECORDVIEW;
            mRecorder = recorder;
            this.isRico = isHost;
            this.mAddress = address;
            mSocket = new Socket();
            mSocket.connect(address, 12000);
            send(mSocket, "$IWANTTOWATCH:");
        } catch (IOException ex) {
            showErrorMessage(ex.toString());
            Logger.getLogger(ClientSocketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected ClientSocketThread() {
    }

    public void createCore() {
        if (isWatcher()) {
            core = new GameCore(EGameMode.WATCHER, this, mAddress, mParent);
            core.launch();
        } else if (isVitualHost) {
            core = new GameCore(EGameMode.HOST, this, mAddress, mParent);
            core.launch();
        } else {
            core = new GameCore(EGameMode.CLIENT, this, mAddress, mParent);
            core.launch();
        }
    }

    public void setVitualHost(boolean isVitualHost) {
        this.isVitualHost = isVitualHost;
    }

    public boolean isVitualHost() {
        return isVitualHost;
    }

    @Override
    public void run() {
        try {
            scan();
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage(ex.toString());
        }
    }

    @Override
    public void scan() {
        switch (mMode) {
            case CLIENT:
                mDispatcher = new ClientMessageDispatcher(mParent, this);
                break;
            case WATCHER:
                mDispatcher = new ClientMessageDispatcher(mParent, this);
                break;
            case RECORDVIEW:
                if (isRico) {
                    send(mSocket, "$WATCHER:" + FTool.parseLocale(238, Startup.VERSION));
                } else {
                    send(mSocket, "$WATCHER:" + FTool.parseLocale(239, Startup.VERSION));
                }
                send(mSocket, "$GIVEMECURRENT:");
                break;
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "Unicode"));
            String instr;
            while ((instr = in.readLine()) != null && scan) {
                instr = fix(instr);
                if (DEBUG_LEVEL >= 1) {
                    String prefix = isWatcher() ? "WatchF " : "ClientF ";
                    System.out.println(prefix + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + " / " + instr);
                }
                if (isWatcher() && !isGotCurrent && !instr.startsWith("$YOUAREWATCHER:") && !instr.startsWith("$NOGAMEYET:")) {
                    if (instr.startsWith("$CURRENT:")) {
                        mSuspendedMessages.add(0, instr);
                        engage();
                    } else {
                        mSuspendedMessages.add(instr);
                    }
                } else {
                    switch (mMode) {
                        case CLIENT:
                        default:
                            mDispatcher.dispatch(instr);
                            break;
                        case RECORDVIEW:
                            mRecorder.record(instr);
                            break;
                    }
                }
            }
        } catch (SocketException ex) {
            if (ex.getMessage().equals("Connection reset")) {
                showErrorMessage(FTool.getLocale(88));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void engage() {
        isGotCurrent = true;
        for (String instr : mSuspendedMessages) {
            switch (mMode) {
                case CLIENT:
                default:
                    mDispatcher.dispatch(instr);
                    break;
                case RECORDVIEW:
                    mRecorder.record(instr);
                    break;
            }
        }
        mSuspendedMessages.clear();
    }

    public boolean isWatcher() {
        return mMode == EGameMode.WATCHER;
    }

    @Override
    public void closeSocket() {
        if (mSocket != null) {
            send(mSocket, "$DISCONNECT:");
            try {
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();
            } catch (IOException ex) {
            }
            mSocket = null;
        }
        scan = false;
    }
}
