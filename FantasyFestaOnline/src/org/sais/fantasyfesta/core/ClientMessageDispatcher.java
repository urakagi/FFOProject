/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.awt.Color;
import javax.swing.JOptionPane;
import org.sais.fantasyfesta.tool.FTool;
import org.sais.fantasyfesta.net.socketthread.ClientSocketThread;
import org.sais.fantasyfesta.net.socketthread.SocketThread;

/**
 *
 * @author Romulus
 */
public class ClientMessageDispatcher extends MessageDispatcher {

    protected Startup mStartup;

    public ClientMessageDispatcher(Startup parent, ClientSocketThread parentSock) {
        super(parentSock);
        mStartup = parent;
        mParentSocketThread = parentSock;
    }

    @Override
    public void dispatch(String instr) {
        if (dispatchClient(instr)) {
            return;
        }
        if (dispatchWatcher(instr)) {
            return;
        }
        dispathCommon(instr, true);
    }

    public boolean dispatchWatcher(String ins) {
        String cmd;
        cmd = "$WATCHHOST:";
        if (ins.startsWith(cmd)) {
            ins = SocketThread.fix(ins.substring(cmd.length()));
            dispathCommon(ins, true);
            return true;
        }
        cmd = "$WATCHCLIENT:";
        if (ins.startsWith(cmd)) {
            ins = SocketThread.fix(ins.substring(cmd.length()));
            dispathCommon(ins, false);
            return true;
        }
        cmd = "$PROXYCONNECTED:";
        if (ins.startsWith(cmd)) {
            mStartup.setMessage("Connected to Proxy AKIRA, waiting for opponent...");
            return true;
        }
        cmd = "$YOUAREWATCHER:";
        if (ins.startsWith(cmd)) {
            if (!((ClientSocketThread) mParentSocketThread).isWatcher()) {
                mStartup.setMessage("Game has already started.");
                mParentSocketThread.closeSocket();
                mParentSocketThread = null;
                return true;
            }
            ((ClientSocketThread) mParentSocketThread).createCore();
            return true;
        }
        cmd = "$CURRENT:";
        if (ins.startsWith(cmd)) {
            GameCore.CurrentStateDecoder.exec(mParentSocketThread.getCore(), ins.substring(cmd.length()));
            return true;
        }
        return false;
    }

    public boolean dispatchClient(String ins) {
        String cmd;
        if (ins.startsWith("$YOUAREAPPLICANT:")) {
            ((ClientSocketThread) mParentSocketThread).createCore();
            return true;
        }
        if (ins.startsWith("$VIRTUALHOST:")) {
            ((ClientSocketThread) mParentSocketThread).setVitualHost(true);
            ((ClientSocketThread) mParentSocketThread).createCore();
            return true;
        }
        if (ins.startsWith("$GAMESTARTED:")) {
            mStartup.setMessage("Game has already started.");
            return true;
        }
        if (ins.startsWith("$NOGAMEYET:")) {
            mStartup.setMessage("No game running yet.");
            return true;
        }
        cmd = "$WATCHER:";
        if (ins.startsWith(cmd)) {
            try {
                Watcher watcher = Watcher.makeFromString(ins.substring(cmd.length()));
                mParentSocketThread.getCore().addnewWatcher(watcher);
                mParentSocketThread.getCore().getMainUI().insertMessage(watcher.getName() + FTool.getLocale(119), Color.MAGENTA);
            } catch (Exception ex) {
                return false;
            }
            return true;
        }
        if (ins.startsWith("$STOPWATCH:")) {
            mParentSocketThread.getCore().removeWatcher(FTool.safeParseInt(ins.substring(11)));
            return true;
        }

        // For virtual host
        int watcherNo = -1;
        if (ins.startsWith("#") && !ins.startsWith("#TO") && !ins.startsWith("#EX")) {
            watcherNo = FTool.safeParseInt(ins.substring(1, ins.indexOf("$")));
            ins = fix(ins);
        }

        if (watcherNo >= 0) {
            cmd = "$GIVEMECURRENT:";
            if (ins.startsWith(cmd)) {
                mParentSocketThread.getCore().sendCurrent(watcherNo);
                return true;
            }
        }

        return false;
    }

    private static String fix(String instr) {
        return instr.substring(instr.indexOf("$"));
    }
}
