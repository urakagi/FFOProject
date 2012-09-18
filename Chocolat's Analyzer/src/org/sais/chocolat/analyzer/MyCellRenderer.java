/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolat.analyzer;

import java.awt.*;
import javax.swing.*;

public class MyCellRenderer extends JLabel implements ListCellRenderer {

    public MyCellRenderer() {
        super();
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
        setFont(new Font("ＭＳ Ｐゴシック", 0, 14));
        //setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("list.font"), 0, 14));

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

