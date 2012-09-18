/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import java.io.Serializable;

/**
 *
 * @author Romulus
 */
public class ChoiceEffect implements Serializable {

    private int mEffectIndex;
    private String mMenuText = "";

    public int getEffectIndex() {
        return mEffectIndex;
    }

    public void setEffectIndex(int mEffectIndex) {
        this.mEffectIndex = mEffectIndex;
    }

    public String getMenuText() {
        return mMenuText;
    }

    public void setMenuText(String mMenuText) {
        this.mMenuText = mMenuText;
    }
}
