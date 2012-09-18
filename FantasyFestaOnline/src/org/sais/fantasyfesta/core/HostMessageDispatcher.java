/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.awt.Color;
import org.sais.fantasyfesta.net.socketthread.HostSocketThread;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class HostMessageDispatcher extends MessageDispatcher {

    public HostMessageDispatcher(HostSocketThread parentSock) {
        super(parentSock);
        mParentSocketThread = parentSock;
    }

    @Override
    public void dispatch(String instr) {
        dispatchHost(instr);
        dispathCommon(instr, true);
    }

    public boolean dispatchHost(String ins) {
        int watcherNo = -1;
        if (ins.startsWith("#")) {
            watcherNo = FTool.safeParseInt(ins.substring(1, ins.indexOf("$")));
            ins = fix(ins);
        }

        String cmd;
        cmd = "$DIFFERENTVERSION:";
        if (ins.startsWith(cmd)) {
            mParentSocketThread.getCore().getMainUI().insertMessage(FTool.parseLocale(232,
                    Startup.VERSION, ins.substring(cmd.length())), Color.RED);
            return true;
        }
        cmd = "$WATCHCHAT:";
        if (ins.startsWith(cmd)) {
            mParentSocketThread.getCore().getMainUI().insertMessage(ins.substring(cmd.length()), new Color(120, 60, 0));
            ((HostSocketThread) mParentSocketThread).sendWithoutDeliver(mParentSocketThread.getSocket(), ins);
            ((HostSocketThread) mParentSocketThread).deliverExcept(ins, watcherNo);
            return true;
        }
        cmd = "$GIVEMECURRENT:";
        if (ins.startsWith(cmd)) {
            mParentSocketThread.getCore().sendCurrent(watcherNo);
            return true;
        }
        cmd = "$WATCHER:";
        if (ins.startsWith(cmd)) {
            Watcher watcher = new Watcher(ins.substring(9), watcherNo);
            mParentSocketThread.getCore().addnewWatcher(watcher);
            mParentSocketThread.getCore().getMainUI().insertMessage(watcher.getName() + FTool.getLocale(119), Color.MAGENTA);
            send(mParentSocketThread.getSocket(), "$WATCHER:" + watcher.toString());
            ((HostSocketThread) mParentSocketThread).deliverExcept("$WATCHER:" + watcher.toString(), watcherNo);
            return true;
        }
        cmd = "$WATCHERPING:";
        if (ins.startsWith(cmd)) {
            ((HostSocketThread) mParentSocketThread).deliverTo("$ACK:", watcherNo);
            return true;
        }
        cmd = "$DISCONNECT:";
        if (ins.startsWith(cmd) && watcherNo >= 0) {
            mParentSocketThread.getCore().removeWatcher(watcherNo);
            send(mParentSocketThread.getSocket(), "$STOPWATCH:" + watcherNo);
            ((HostSocketThread) mParentSocketThread).deliverExcept("$STOPWATCH:" + watcherNo, watcherNo);
            return true;
        }

        return false;
    }

    private static String fix(String instr) {
        return instr.substring(instr.indexOf("$"));
    }
}

