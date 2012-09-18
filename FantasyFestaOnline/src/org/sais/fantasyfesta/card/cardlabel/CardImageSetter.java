/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card.cardlabel;

import java.awt.Color;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.core.ReadInnerFile;

/**
 *
 * @author Romulus
 */
public class CardImageSetter {

    public static boolean sUseCardImage = false;

    /**
     * Set customized icon.
     * <p>
     * Can be used for normal JLabel.
     * @param label
     * @param controller
     * @param cardnum
     * @param direction JLabel#HORIZONTAL or JLabel#VERTICAL
     */
    public static void set(JLabel label, CardInfo info, EPlayer controller, int direction) {
        if (info == null || info.isNull() || label instanceof UniLabel) {
            return;
        }
        int cardNo = info.getCardNo();
        if (cardNo / 100 == 99) {
            cardNo -= 900;
        }

        if (sUseCardImage) {
            String suffix = "";
            if (controller == EPlayer.OPP) {
                suffix += "opp";
            }
            File dir = new File("icon");
            if (dir.exists()) {
                File f;
                if (direction == JLabel.HORIZONTAL) {
                    suffix += "side";
                }
                f = new File(dir + "/" + cardNo + suffix + ".jpg");
                if (f.exists()) {
                    label.setIcon(new ImageIcon(f.getPath()));
                    label.setBorder(new EtchedBorder(Color.WHITE, Color.BLACK));
                    return;
                } else {
                    if ("oppside".equals(suffix)) {
                        suffix = "side";
                        f = new File(dir + "/" + cardNo + suffix + ".jpg");
                        if (f.exists()) {
                            label.setIcon(new ImageIcon(f.getPath()));
                            label.setBorder(new EtchedBorder(Color.WHITE, Color.BLACK));
                            return;
                        }
                    }
                }
            }
        }

        String fname = "";
        if (controller == EPlayer.ICH) {
            fname = "Own";
        } else {
            fname = "Opp";
        }
        fname += info.getDefaultIconFilePrefix();
        if (direction == JLabel.HORIZONTAL) {
            fname += "Side";
        }
        fname += ".jpg";
        label.setIcon(new ImageIcon(ReadInnerFile.getURL(fname)));
        label.setBorder(null);
    }
}
