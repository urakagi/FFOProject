/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Timer;

/**
 *
 * @author Romulus
 */
public class GameTimer {

    private Timer mTimer;
    private long[] mTimeUsed;
    private int mNowTurn = 0;

    public GameTimer(int amount, IGameTimerCallback listener) {
        mTimeUsed = new long[amount];
        mTimer = new Timer(1000, new GameTimerListener(listener));
        mTimer.start();
    }

    public void switchToNext() {
        mNowTurn++;
        if (mNowTurn >= mTimeUsed.length) {
            mNowTurn = 0;
        }
    }

    public int getNowTurn() {
        return mNowTurn;
    }

    public void setNowTurn(int index) {
        mNowTurn = index;
    }

    public void stop() {
        mTimer.stop();
    }

    public String getTimeString(int index) {
        return new SimpleDateFormat("mm:ss").format(new Date(mTimeUsed[index]));
    }

    public long getEplisedTime(int index) {
        return mTimeUsed[index];
    }

    public void setElpisedTime(int index, long time) {
        mTimeUsed[index] = time;
    }

    class GameTimerListener implements ActionListener {

        private IGameTimerCallback mListener;

        public GameTimerListener(IGameTimerCallback listener) {
            mListener = listener;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mTimeUsed[mNowTurn] += 1000;
            if (mListener != null) {
                mListener.time(new String[]{getTimeString(0), getTimeString(1)});
            }
        }
    }

    public interface IGameTimerCallback {

        void time(String[] timeStrings);
    }
}
