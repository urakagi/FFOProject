/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.recorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.sais.fantasyfesta.net.socketthread.ClientSocketThread;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class Recorder {

    private File mFile;
    private BufferedWriter mOut;
    private ClientSocketThread mSocketThread;

    public Recorder(InetSocketAddress addr, boolean isHost) {
        try {
            if (!new File("record").exists()) {
                new File("record").mkdirs();
            }
            mFile = new File("record/Record_" + FTool.getNowTimeString("yy_MM_dd_HH_mm_ss") + ".txt");
            mFile.createNewFile();
            mOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mFile), "Unicode"));
            mSocketThread = new ClientSocketThread(this, addr, isHost); // Record using callbacks
            mSocketThread.start();
        } catch (IOException ex) {
            Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getClass() + "\n" + ex.getMessage(), "Recorder failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void record(String instr) {
        try {
            mOut.write(instr);
            mOut.newLine();
            mOut.flush();
        } catch (IOException ex) {
            Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        try {
            mOut.flush();
            mOut.close();
            mSocketThread.closeSocket();
        } catch (IOException ex) {
            Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }
}
