/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.fantasyfesta.net.socketthread.SocketThread;

/**
 *
 * @author Romulus
 */
public class RecordViewHostFSock extends SocketThread {

    private BufferedReader mReader;
    private ServerSocket mServerSocket;
    private int mPort;

    public RecordViewHostFSock(File file) {
        try {
            mReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Unicode"));
        } catch (IOException ex) {
            Logger.getLogger(RecordViewHostFSock.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        mPort = 13700;
        while (mPort < 14700) {
            try {
                mServerSocket = new ServerSocket();
                mServerSocket.setReuseAddress(true);
                mServerSocket.bind(new InetSocketAddress(mPort));
                break;
            } catch (IOException ex) {
                mPort += 10;
            }
        }
        this.start();
    }

    @Override
    public void closeSocket() {
        try {
            if (mSocket != null) {
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();
                mSocket = null;
            }
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            mSocket = mServerSocket.accept();
            mDispatcher = new RecordViewHostMessageDispatcher(this);
            send(mSocket, "$RECORDSTART:");
            scan();
        } catch (IOException ex) {
            Logger.getLogger(RecordViewHostFSock.class.getName()).log(Level.SEVERE, null, ex);
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
                    Calendar c = Calendar.getInstance();
                    System.out.println("RVHostF " + DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()) + "/ " + instr);
                }
                mDispatcher.dispatch(instr);
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int getPort() {
        return mPort;
    }

    public BufferedReader getReader() {
        return mReader;
    }

    @Override
    protected void finalize() throws Throwable {
        closeSocket();
        if (mReader != null) {
            mReader.close();
        }
        super.finalize();
    }
}
