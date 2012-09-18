/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse.gradeleague;

import java.util.ArrayList;

/**
 *
 * @author chung-mingtsai
 */
public class Player implements Comparable<Player> {

    private int mId;
    private String mName;
    private int mGrade;
    private int mPoints;
    private ArrayList<Scorable> mScores = new ArrayList<Scorable>();
    private static final int[] GRADE_POINTS = new int[]{0, 5, 7, 9, 11, 13, 15, 17, 19};
    private static final int[] UNDER_POINTS = new int[]{0, 4, 3, 2};

    public Player(String dataLine) {
        String[] s = dataLine.split(" ");
        mId = Integer.parseInt(s[0]);
        mName = s[1];
        mGrade = Integer.parseInt(s[2]);
        mPoints = Integer.parseInt(s[3]);
    }

    private Player() {
        mName = "";
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getGrade() {
        return mGrade;
    }

    public int getPoints() {
        return mPoints;
    }

    public ArrayList<Scorable> getScores() {
        return mScores;
    }

    public void putScore(Scorable score) {
        mScores.add(score);
    }
    
    public void win(Player loser) {
        mScores.add(new Game(getName(), loser.getName()));
        loser.mScores.add(new Game(getName(), loser.getName()));
        this.increasePoint();
        loser.decreasePoint();
    }

    public void champ(String tourName) {
        mScores.add(new Champion(getName(), tourName));
        this.increasePoint();
        this.increasePoint();
    }

    private void increasePoint() {
        mPoints += 1;
        if (getPoints() >= getGradeThreshold()) {
            mGrade += 1;
            mPoints = 0;
            if (getGrade() == 0) {
                mGrade = 1;
            }
        }
    }

    private void decreasePoint() {
        mPoints -= 1;
        if (getPoints() < 0) {
            if (getGrade() < 0) {
                mPoints = 0;
            } else {
                if (getPoints() <= -2) {
                    mGrade -= 1;
                    if (mGrade == 0) {
                        mGrade = -1;
                        mPoints = 2;
                    } else {
                        mPoints = GRADE_POINTS[mGrade] - 2;
                    }
                }
            }
        }
    }
    
    public int getGradeThreshold() {
        if (getGrade() < 0) {
            return UNDER_POINTS[-getGrade()];
        } else {
            return GRADE_POINTS[getGrade()];
        }
    }
    
    private static final String[] KANJI_NUMBERS = {"零", "初", "二", "三", "四", "五", "六", "七", "八", "九"};

    public String getGradeString() {
        if (getGrade() < 0) {
            return -getGrade() + "級";
        } else {
            return KANJI_NUMBERS[getGrade()] + "段";
        }
    }

    public String getPointString() {
        if (getGrade() > 0) {
            return getPoints() + "/" + GRADE_POINTS[getGrade()];
        } else {
            return getPoints() + "/" + UNDER_POINTS[-getGrade()];
        }
    }

    public String getDataLine() {
        return getId() + " " + getName() + " " + getGrade() + " " + getPoints();
    }
    
    public int getForNextGrade() {
        return getGradeThreshold() - getPoints();
    }

    public boolean isNull() {
        return mName.length() == 0;
    }

    public static Player newNull() {
        return new Player();
    }

    public int compareTo(Player t) {
        if (this.getGrade() != t.getGrade()) {
            return t.getGrade() - this.getGrade();
        }
        if (this.getPoints() != t.getPoints()) {
            return t.getPoints() - this.getPoints();
        }
        return -this.getName().compareTo(t.getName());
    }
}
