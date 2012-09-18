/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.deck;

import java.io.File;

public class decFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        if (f.getName().length() > 4) {
            if (f.getName().substring(f.getName().length() - 4).toLowerCase().equals(".dec")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Fantasy Festa Online Deck Files (*.dec)";
    }

    public decFilter() {
    }
}

