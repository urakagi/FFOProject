/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card.cardlabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.EventCard;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class EventLabel extends CardLabel {

    public EventLabel(ICardLabelCallback parent, Card card) {
        super(parent, card);
        addMouseListener(mMouseListener);
    }

    @Override
    public EventCard getCard() {
        return (EventCard) mCard;
    }

    public void setPopupMenu() {
        if (mCard == null) {
            return;
        }
        mMenu = new JPopupMenu();
        mMenu.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
        JMenuItem title = new JMenuItem(mCard.getInfo().getName());
        if (CardImageSetter.sUseCardImage) {
            title.addActionListener(new CardViewListener(mCard.getInfo()));
        }
        mMenu.add(title);
        mMenu.addSeparator();
        if (mCard.isOppControl() || mWatcherMode) {
            add(mMenu);
            return;
        }

        for (int k = 0; k < 3; ++k) {
            JMenuItem item = new JMenuItem(FTool.getLocale(172 + k));
            EMoveTarget targetregion;
            switch (k) {
                case 0:
                    targetregion = EMoveTarget.DISCARD_PILE;
                    break;
                case 1:
                    targetregion = EMoveTarget.HAND;
                    break;
                case 2:
                default:
                    targetregion = EMoveTarget.LIBRARY_TOP;
                    break;
            }
            item.addActionListener(new EventCommandListener(targetregion));
            item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
            mMenu.add(item);
        }

        setChoiceMenu();
        setTargetMenu();

        add(mMenu);
    }

    private void move(EMoveTarget destination) {
        mParent.moveCard(getCard(), getCard().getOwner(), destination, getCard().isIchControl(), false, "");
    }

    class EventCommandListener implements ActionListener {

        EMoveTarget mTargetRegion;

        EventCommandListener(EMoveTarget targetregion) {
            this.mTargetRegion = targetregion;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move(mTargetRegion);
        }
    }
}
