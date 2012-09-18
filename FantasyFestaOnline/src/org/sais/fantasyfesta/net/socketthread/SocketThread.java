/*
 * Sock.java
 *
 * Created on 2007//2/3 1:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author Romulus
 */
package org.sais.fantasyfesta.net.socketthread;

import java.awt.Color;
import org.sais.fantasyfesta.core.*;

import java.io.*;
import java.net.*;

public abstract class SocketThread extends Thread {

    public final int DEBUG_LEVEL = 1;
    protected Startup mParent;
    protected GameCore core;
    protected Socket mSocket;
    protected MessageDispatcher mDispatcher;

    public abstract void closeSocket();

    public static String fix(String instr) {
        for (int i = 0; i < instr.length(); ++i) {
            if (instr.charAt(i) == '$' || instr.charAt(i) == '#') {
                return instr.substring(i);
            }
        }
        return instr;
    }

    abstract public void scan();

    public Socket getSocket() {
        return mSocket;
    }

    public void send(Socket soc, String message) {
        try {
            if (DEBUG_LEVEL >= 2) {
                System.out.println(this.getClass().getName() + " send: " + message);
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(), "Unicode"));
            out.write(message);
            out.newLine();
            out.flush();
        } catch (SocketException ex) {
            // Disconnected
            return;
        } catch (Exception ex) {
            showErrorMessage(ex.toString());
            ex.printStackTrace();
        }
    }

    public GameCore getCore() {
        return core;
    }

    protected void showErrorMessage(String message) {
        if (core == null) {
            if (mParent.isVisible()) {
                // In startup
                mParent.setMessage(message);
            }
        } else {
            if (core.getMainUI() != null) {
                core.getMainUI().insertMessage(message, Color.RED);
            }
        }
    }

    static public enum ClientType {

        Unknown,
        Player,
        Watcher
    }
}
