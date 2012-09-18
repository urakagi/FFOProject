/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.recorder;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.Startup;
import org.sais.fantasyfesta.enums.EGameMode;
import org.sais.fantasyfesta.net.socketthread.ClientSocketThread;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class RecordViewClientSocketThread extends ClientSocketThread {

    public RecordViewClientSocketThread(Startup parent, String addr, int port) {
        if (addr.matches("^(\\d+\\-){3}\\d+$")) {
            addr = addr.replace('-', '.');
        }
        if (addr.equals("")) {
            addr = "127.0.0.1";
        }
        mParent = parent;
        mMode = EGameMode.RECORDVIEW;
        mSocket = new Socket();
        try {
            mSocket.connect(new InetSocketAddress(InetAddress.getByName(addr), port), 12000);
        } catch (IOException ex) {
            Logger.getLogger(RecordViewClientSocketThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        mDispatcher = new RecordViewClientMessageDispatcher(parent, this);
    }

    @Override
    public void run() {
        try {
            scan();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getClass() + "\n" + ex.getMessage(), "run() in RVClientSock", JOptionPane.ERROR_MESSAGE);
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
                    String prefix = "RVClientF:";
                    System.out.println(prefix + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + " / " + instr);
                }
                mDispatcher.dispatch(instr);
            }
        } catch (SocketException ex) {
            if (ex.getMessage().equals("Connection reset")) {
                core.getMainUI().insertMessage(FTool.getLocale(88), Color.RED);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void createCore() {
        core = new GameCore(EGameMode.RECORDVIEW, this, mAddress, mParent);
        core.launch();
    }

    @Override
    public void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            mSocket = null;
        }
    }
}
