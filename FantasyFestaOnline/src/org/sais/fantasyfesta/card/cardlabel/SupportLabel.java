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
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public class SupportLabel extends CardLabel {

    public SupportLabel(ICardLabelCallback parent, Card card) {
        super(parent, card);
        addMouseListener(mMouseListener);
    }

    @Override
    public SupportCard getCard() {
        return (SupportCard) mCard;
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

        if (mWatcherMode) {
            setTargetMenu();
            add(mMenu);
            return;
        }

        if (getCard().getAttachingOn() != null && getCard().getAttachingOn().getRegion() == ERegion.BATTLE) {
            if (getCard().isIchControl()) {
                setSupportAbilityMenu();
                setChoiceMenu();
            }
            add(mMenu);
            return;
        }

        if (getCard().getOwner() == EPlayer.ICH) {
            for (int k = 0; k < 4; ++k) {
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
                        targetregion = EMoveTarget.LIBRARY_TOP;
                        break;
                    case 3:
                    default:
                        targetregion = EMoveTarget.LIBRARY_BOTTOM;
                        break;
                }
                item.addActionListener(new SupportCommandListener(targetregion));
                item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                mMenu.add(item);
            }
        }
        setSupportAbilityMenu();
        setChoiceMenu();
        setTargetMenu();
        add(mMenu);
    }

    private void move(EMoveTarget destination) {
        switch (getCard().getRegion()) {
            case RESERVED:
            case ACTIVATED:
                mParent.unAttach(getCard().getAttachingOn(), true);
                mParent.moveSpellAttachment(getCard(), destination, true);
                break;
            case LEADER_ATTACHMENTS:
            case SCENE:
                mParent.moveCard(getCard(), getCard().getOwner(), destination, true, false, "");
                break;
        }
    }

    class SupportCommandListener implements ActionListener {

        EMoveTarget mTargetRegion;

        SupportCommandListener(EMoveTarget targetregion) {
            mTargetRegion = targetregion;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move(mTargetRegion);
        }
    }

    @Override
    protected void handleUseChoice(int index, ActionEvent e) {
        fireChoice(index, e);
    }
}
