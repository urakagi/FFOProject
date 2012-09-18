/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card.cardlabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class SpellLabel extends CardLabel {

    public SpellLabel(ICardLabelCallback parent, SpellCard card) {
        super(parent, card);
        addMouseListener(new SpellMouseListener());
    }

    @Override
    public SpellCard getCard() {
        return (SpellCard) mCard;
    }

    public void setAttachmentSize(int width, int height) {
        SupportLabel label;
        try {
            label = getCard().getAttached().getSupportLabel();
            label.setSize(width, height);
        } catch (NotAttachedException ex) {
            return;
        }
    }

    public void setAttachmentLocation(int x, int y) {
        SupportLabel label;
        try {
            label = getCard().getAttached().getSupportLabel();
            if (label == null) {
                return;
            }
            label.setLocation(x, y);
        } catch (NotAttachedException ex) {
            return;
        }
    }

    public SupportLabel getAttachLabel() {
        try {
            return getCard().getAttached().getSupportLabel();
        } catch (NotAttachedException ex) {
            return null;
        }
    }

    public void setPopupMenu() {
        if (mCard == null) {
            return;
        }
        if (mMenu != null) {
            remove(mMenu);
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
            add(mMenu);
            return;
        }

        EPlayer newController;
        EMoveTarget targetregion;
        JMenuItem item = null;
        if (mCard.isIchControl()) {
            switch (getCard().getRegion()) {
                case RESERVED:
                    for (int k = 0; k < 6; ++k) {
                        switch (k) {
                            case 0:
                                item = new JMenuItem(FTool.getLocale(176));
                                newController = getCard().getController();
                                targetregion = EMoveTarget.ACTIVATED;
                                break;
                            case 1:
                                mMenu.addSeparator();
                                item = new JMenuItem(FTool.getLocale(172));
                                newController = getCard().getOwner();
                                targetregion = EMoveTarget.DISCARD_PILE;
                                break;
                            case 2:
                                item = new JMenuItem(FTool.getLocale(173));
                                newController = getCard().getOwner();
                                targetregion = EMoveTarget.HAND;
                                break;
                            case 3:
                                item = new JMenuItem(FTool.getLocale(174));
                                newController = getCard().getOwner();
                                targetregion = EMoveTarget.LIBRARY_TOP;
                                break;
                            case 4:
                            default:
                                item = new JMenuItem(FTool.getLocale(188));
                                newController = getCard().getController();
                                targetregion = EMoveTarget.ACTIVATED_NOCOST;
                                break;
                            case 5:
                                item = new JMenuItem(FTool.getLocale(312));
                                newController = EPlayer.OPP;
                                targetregion = EMoveTarget.RESERVED;
                                break;
                        }
                        item.addActionListener(new SpellCommandListener(newController, targetregion));
                        item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                        mMenu.add(item);
                    }
                    break;
                case ACTIVATED:
                    for (int k = 0; k < 6; ++k) {
                        switch (k) {
                            case 0:
                                item = new JMenuItem(FTool.getLocale(178));
                                newController = getCard().getController();
                                targetregion = EMoveTarget.BATTLE;
                                break;
                            case 1:
                                mMenu.addSeparator();
                                item = new JMenuItem(FTool.getLocale(177));
                                newController = getCard().getController();
                                targetregion = EMoveTarget.RESERVED;
                                break;
                            case 2:
                                item = new JMenuItem(FTool.getLocale(172));
                                newController = getCard().getOwner();
                                targetregion = EMoveTarget.DISCARD_PILE;
                                break;
                            case 3:
                                item = new JMenuItem(FTool.getLocale(173));
                                newController = getCard().getOwner();
                                targetregion = EMoveTarget.HAND;
                                break;
                            case 4:
                            default:
                                item = new JMenuItem(FTool.getLocale(174));
                                newController = getCard().getOwner();
                                targetregion = EMoveTarget.LIBRARY_TOP;
                                break;
                            case 5:
                                item = new JMenuItem(FTool.getLocale(312));
                                newController = EPlayer.OPP;
                                targetregion = EMoveTarget.RESERVED;
                                break;
                        }
                        item.addActionListener(new SpellCommandListener(newController, targetregion));
                        item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                        mMenu.add(item);
                    }
                    // Handle special abilities which can be used in battle
                    int cardNo = mCard.getInfo().getCardNo();
                    if (cardNo == 1801 || cardNo == 1419) {
                        setBattleAbilityMenu();
                    }
                    break;
                case BATTLE:
                    setBattleAbilityMenu();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            setChoiceMenu();
        } else {
            switch (mCard.getRegion()) {
                case RESERVED:
                    item = new JMenuItem(FTool.getLocale(311));
                    newController = EPlayer.ICH;
                    targetregion = EMoveTarget.ACTIVATED;
                    item.addActionListener(new SpellCommandListener(newController, targetregion));
                    item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                    mMenu.add(item);
                    break;
                default:
                    // Nothing
                    break;
            }
            setTargetMenu();
        }
        add(mMenu);
    }

    private void move(EPlayer newController, EMoveTarget destination, String extramessage) {
        mParent.moveCard(getCard(), newController, destination, true, false, extramessage);
    }

    class SpellCommandListener implements ActionListener {

        private EPlayer newController;
        private EMoveTarget mTargetRegion;

        SpellCommandListener(EPlayer newController, EMoveTarget targetRegion) {
            this.newController = newController;
            this.mTargetRegion = targetRegion;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move(newController, mTargetRegion, "");
        }
    }

    class SpellMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (mParent == null) {
                return;
            }
            mParent.showCard(mCard);

            if (evt.getButton() == MouseEvent.BUTTON1 && !evt.isControlDown()) {
                if (!mWatcherMode) {
                    mParent.attach(SpellLabel.this, true);
                }
            } else {
                mMenu.show(evt.getComponent(), evt.getX(), evt.getY() - MENUSHIFT);
            }
        }
    }

    @Override
    protected void handleUseChoice(int index, ActionEvent e) {
        fireChoice(index, e);
    }
}
