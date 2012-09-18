/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.chocolat.irc;

/**
 *
 * @author Romulus
 */
public interface IIRCManagerCallback {
    void invokeUpdate();
    IIRCManagerResultSetCallback nextRound();
}
