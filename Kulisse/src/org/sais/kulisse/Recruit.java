/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.kulisse;

/**
 *
 * @author Romulus
 */
public class Recruit {
    public String ip;
    public String nick;
    public long time;
    public String extraMessage;

    public Recruit(String ip, String nick, long time, String extraMessage) {
        this.ip = ip;
        this.nick = nick;
        this.time = time;
        this.extraMessage = extraMessage;
    }
}
