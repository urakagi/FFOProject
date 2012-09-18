/*
 * ListItem.java
 *
 * Created on 2007�~1��27��, �U�� 3:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.ui;

import java.awt.*;
import org.sais.fantasyfesta.card.cardlabel.UniLabel;

/**
 *
 * @author Romulus
 */
public class ListItem {

    private Color color;
    private String value;
    public UniLabel label;

    public ListItem(Color c, String title, UniLabel card) {
        color = c;
        value = title;
        this.label = card;
    }

    public Color getColor() {
        return color;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label.getCard().getInfo().getName();
    }

}

