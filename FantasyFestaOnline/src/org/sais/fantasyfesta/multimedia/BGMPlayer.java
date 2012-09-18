/*
 * BGMPlayer.java
 *
 * Created on 2007/09/26, 23:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.multimedia;

import java.io.*;
import javax.sound.sampled.*;

/**
 *
 * @author Romulus
 */
public class BGMPlayer extends Thread implements BGMPlayable {

    boolean stopped = false;
    boolean change = false;
    boolean isStarted = false;
    boolean closed = false;
    String fname = "";

    public BGMPlayer() {
    }

    synchronized public void run() {
        while (!closed) {
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(new File(fname));
                AudioFormat format = stream.getFormat();
                SourceDataLine bgmline = (SourceDataLine) (AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format)));
                bgmline.open(format);
                bgmline.start();

                int total = 0;
                long totalToRead = new File(fname).length();
                int numBytesRead;
                int numBytesToRead = bgmline.available();
                byte[] myData = new byte[numBytesToRead];
                while (!stopped) {
                    while (total < totalToRead && !stopped) {
                        if (change) {
                            break;
                        }
                        numBytesRead = stream.read(myData, 0, numBytesToRead);
                        if (numBytesRead == -1) {
                            break;
                        }
                        total += numBytesRead;
                        bgmline.write(myData, 0, numBytesRead);
                    }
                    stream.close();
                    stream = AudioSystem.getAudioInputStream(new File(fname));
                    if (change) {
                        bgmline.close();
                        change = false;
                        format = stream.getFormat();
                        bgmline = (SourceDataLine) (AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format)));
                        bgmline.open(format);
                        bgmline.start();
                    }
                    total = 0;
                }
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

    @Override
    public void play(String fname) {
        stopped = false;
        change = true;
        this.fname = fname;
        if (!isStarted) {
            this.start();
            isStarted = true;
        } else {
            this.interrupt();
        }
    }

    @Override
    public void stopplay() {
        stopped = true;
    }

    @Override
    public void close() {
        stopped = true;
    }
}
