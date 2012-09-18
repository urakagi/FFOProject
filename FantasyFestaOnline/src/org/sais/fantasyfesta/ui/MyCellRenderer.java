/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.ui;

import java.awt.*;
import javax.swing.*;
import org.sais.fantasyfesta.card.cardlabel.UniLabel;

public class MyCellRenderer extends UniLabel implements ListCellRenderer {

    public MyCellRenderer() {
        setOpaque(true);
    }
    // Set the attributes of the
    //class and return a reference

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value, // value to display
            int index, // cell index
            boolean iss, // is selected
            boolean chf) // cell has focus?
    {
        // Set the text and
        //background color for rendering
        ListItem item = (ListItem) value;
        setBackground(item.getColor());
        setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("list.font"), 0, 14));

        // Set a border if the
        //list item is selected
        if (iss) {
            setBorder(BorderFactory.createLineBorder(
                    Color.blue, 2));
        } else {
            setBorder(BorderFactory.createLineBorder(
                    list.getBackground(), 2));
        }

        setText(item.getValue());
        return this;
    }
}

