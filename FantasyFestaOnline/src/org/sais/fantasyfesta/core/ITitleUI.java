/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

/**
 *
 * @author Romulus
 */
public interface ITitleUI {
    
    public void setVisible(boolean visible);

    public void clearHostSock();
    
    public String getTitle();

    public String getPlayerName();

    public void setMessage(String string);
    
}
