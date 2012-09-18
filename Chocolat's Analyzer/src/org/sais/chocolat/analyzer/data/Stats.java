/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.analyzer.data;

import java.util.ArrayList;

public class Stats extends ArrayList {

    public static final int SORT_COUNT = 0;
    public static final int SORT_WON = 1;
    public static final int SORT_LOST = 2;
    public int firstWon = 0;
    public int firstLost = 0;
    public int lastWon = 0;
    public int lastLost = 0;
    public int notDecided = 0;
    public static int sortType = SORT_COUNT;

    public void push(DeckResult result) {
        switch (result.dice) {
            case -1:
                if (result.result > 0) {
                    lastWon++;
                } else if (result.result < 0) {
                    lastLost++;
                } else {
                    notDecided++;
                }
                break;
            case 1:
                if (result.result > 0) {
                    firstWon++;
                } else if (result.result < 0) {
                    firstLost++;
                } else {
                    notDecided++;
                }
                break;
            case 0:
                notDecided++;
                break;
            default:
                assert false;
        }
    }

    public int getFirsts() {
        return firstWon + firstLost;
    }

    public int getLasts() {
        return lastLost + lastWon;
    }

    public int getWins() {
        return firstWon + lastWon;
    }

    public int getLosts() {
        return firstLost + lastLost;
    }

    public int getGameCount() {
        return firstWon + firstLost + lastLost + lastWon;
    }

    public double getFirstPercentage() {
        return firstWon + firstLost == 0 ? 0.0 : (firstWon * 10000 / (firstWon + firstLost)) / 100.0;
    }

    public double getLastPercentage() {
        return lastWon + lastLost == 0 ? 0.0 : (lastWon * 10000 / (lastWon + lastLost)) / 100.0;
    }

    public double getTotalPercentage() {
        return lastWon + lastLost + firstWon + firstLost == 0 ? 0.0 : ((lastWon + firstWon) * 10000 / (lastWon + lastLost + firstWon + firstLost)) / 100.0;
    }

    public void writeResult(String title, StringBuilder builder) {
        builder.append(title + "\r\n");
        builder.append(getFirsts() + " firsts, " + firstWon + "-" + firstLost + " (" + getFirstPercentage() + "%)\r\n");
        builder.append(getLasts() + " lasts, " + lastWon + "-" + lastLost + " (" + getLastPercentage() + "%)\r\n");
        builder.append((getFirsts() + getLasts()) + " totals, " + (lastWon + firstWon) + "-" + (lastLost + firstLost) + " (" + (getTotalPercentage()) + "%)" + (notDecided > 0 ? (" - " + notDecided + " NG\r\n") : "\r\n"));
        builder.append("\r\n");
    }

    public void add(Stats s) {
        this.firstLost += s.firstLost;
        this.firstWon += s.firstWon;
        this.lastLost += s.lastLost;
        this.lastWon += s.lastWon;
        this.modCount += s.modCount;
        this.notDecided += s.notDecided;
    }

    @Override
    public int size() {
        switch (sortType) {
            case SORT_COUNT:
            default:
                return getFirsts() + getLasts();
            case SORT_LOST:
                return (int) ((1 - getTotalPercentage()) * 10000);
            case SORT_WON:
                return (int) (getTotalPercentage() * 10000);
        }
    }
}
