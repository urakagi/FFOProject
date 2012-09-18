/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse.gradeleague;

/**
 *
 * @author chung-mingtsai
 */
public class Game implements Scorable {
    
    private String winner;
    private String loser;
    
    public Game(String segmentLine) {
        String[] s = segmentLine.split(" O-X ");
        winner = s[0];
        loser = s[1];
    }
    
    public Game(String winner, String loser) {
        this.winner = winner;
        this.loser = loser;
    }
    
    public String getResultString(String name) {
        if (winner.equalsIgnoreCase(name)) {
            return "○" + loser;
        }
        if (loser.equalsIgnoreCase(name)) {
            return "●" + winner;
        }
        return winner + " O-X " + loser;
    }
    
    public String getWinnerName() {
        return winner;
    }
    
    public String getLoserName() {
        return loser;
    }
    
    public String getSegmentLine() {
        return winner + " O-X " + loser;
    }
    
}
