/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse.gradeleague;

/**
 *
 * @author Romulus
 */
public class Champion implements Scorable {

    private String winner;
    private String tour;
    
    public Champion(String winner, String tour) {
        this.winner = winner;
        this.tour = tour;
    }
    
    public Champion(String segLine) {
        String[] s = segLine.split(" ");
        winner = s[0];
        tour = s[2];
    }
    
    public String getWinnerName() {
        return winner;
    }

    public String getResultString(String name) {
        return "☆" + tour;
    }

    public String getSegmentLine() {
        return winner + " *^* " + tour;
    }
    
}
