/*
 * ListItem.java
 *
 * Created on 2007�~1��27��, �U�� 3:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.sais.chocolat.analyzer;

import java.awt.*;

/**
 *
 * @author Romulus
 */
public class ListItem {

    private Color mColor;
    private String mDisplayText;

    public ListItem(Color c, String displayText) {
        mColor = c;
        mDisplayText = displayText;
    }

    public Color getColor() {
        return mColor;
    }

    public String getValue() {
        return mDisplayText;
    }

    @Override
    public String toString() {
        return mDisplayText;
    }

}

