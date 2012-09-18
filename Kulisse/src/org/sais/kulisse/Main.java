/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.kulisse;

/**
 *
 * @author Romulus
 */
public class Main implements IIRCManagerCallback {

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        new IRCManager(this);
    }

    public void invokeUpdate() {
        return;
    }

}
