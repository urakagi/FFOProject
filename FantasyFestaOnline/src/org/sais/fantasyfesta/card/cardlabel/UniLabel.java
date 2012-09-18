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
import org.sais.fantasyfesta.tool.FTool;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class UniLabel extends CardLabel {

    protected UniLabel() {
    }

    public UniLabel(ICardLabelCallback parent, Card card) {
        super(parent, card);
        addMouseListener(mMouseListener);
        setPopupMenu();
    }

    private void setPopupMenu() {
        if (mCard == null || mCard.isNull()) {
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

        switch (mCard.getRegion()) {
            case HAND:
                setupHandMenu();
                break;
            case LIBRARY:
                setupLibraryMenu();
                break;
            case DISCARD_PILE:
                setupDiscardPileMenu();
                break;
            default:
                mMenu = null;
                return;
        }

        add(mMenu);
    }

    private void setupHandMenu() {
        EMoveTarget targetregion;
        JMenuItem item;

        if (getCard().getOwner() == EPlayer.ICH) {
            for (int k = 0; k < 4; ++k) {
                switch (k) {
                    case 0:
                        item = new JMenuItem(FTool.getLocale(181));
                        targetregion = translateField(mCard);
                        break;
                    case 1:
                        mMenu.addSeparator();
                        item = new JMenuItem(FTool.getLocale(172));
                        targetregion = EMoveTarget.DISCARD_PILE;
                        break;
                    case 2:
                        item = new JMenuItem(FTool.getLocale(174));
                        targetregion = EMoveTarget.LIBRARY_TOP;
                        break;
                    case 3:
                    default:
                        item = new JMenuItem(FTool.getLocale(175));
                        targetregion = EMoveTarget.LIBRARY_BOTTOM;
                        break;
                }
                item.addActionListener(new UniCommandListener(EPlayer.ICH, targetregion));
                item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                mMenu.add(item);
            }
        } else {
            for (int k = 0; k < 2; ++k) {
                switch (k) {
                    case 0:
                    default:
                        item = new JMenuItem(FTool.getLocale(172));
                        targetregion = EMoveTarget.DISCARD_PILE;
                        break;
                    case 1:
                        item = new JMenuItem(FTool.getLocale(174));
                        targetregion = EMoveTarget.LIBRARY_TOP;
                        break;
                }
                item.addActionListener(new UniCommandListener(EPlayer.OPP, targetregion));
                item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                mMenu.add(item);
            }
        }
    }

    private void setupLibraryMenu() {
        EMoveTarget targetregion;
        JMenuItem item;

        if (getCard().getOwner() == EPlayer.ICH) {
            for (int k = 0; k < 8; ++k) {
                switch (k) {
                    case 0:
                        item = new JMenuItem(FTool.getLocale(180));
                        targetregion = EMoveTarget.HAND;
                        break;
                    case 1:
                        item = new JMenuItem(FTool.getLocale(181));
                        targetregion = translateField(mCard);
                        break;
                    case 2:
                        if (mCard.getInfo().isCardType(ECardType.SPELL)) {
                            item = new JMenuItem(FTool.getLocale(186));
                            targetregion = EMoveTarget.ACTIVATED;
                            break;
                        } else {
                            ++k;
                            // Falls down
                        }
                    case 3:
                        mMenu.addSeparator();
                        item = new JMenuItem(FTool.getLocale(172));
                        targetregion = EMoveTarget.DISCARD_PILE;
                        break;
                    case 4:
                        item = new JMenuItem(FTool.getLocale(182));
                        targetregion = EMoveTarget.SHUFFLE_THEN_TOP;
                        break;
                    case 5:
                        item = new JMenuItem(FTool.getLocale(183));
                        targetregion = EMoveTarget.LIBRARY_TOP;
                        break;
                    case 6:
                        item = new JMenuItem(FTool.getLocale(184));
                        targetregion = EMoveTarget.LIBRARY_BOTTOM;
                        break;
                    case 7:
                    default:
                        item = new JMenuItem(FTool.getLocale(185));
                        targetregion = EMoveTarget.HIDE_TO_HAND;
                        break;
                }
                item.addActionListener(new UniCommandListener(EPlayer.ICH, targetregion));
                item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                mMenu.add(item);
            }
        } else {
            EPlayer newController;
            for (int k = 0; k < 2; ++k) {
                switch (k) {
                    case 0:
                        if (!getCard().getInfo().isCardType(ECardType.SPELL)) {
                            continue;
                        }
                        item = new JMenuItem(FTool.getLocale(181));
                        newController = EPlayer.ICH;
                        targetregion = translateField(mCard);
                        break;
                    case 1:
                    default:
                        mMenu.addSeparator();
                        item = new JMenuItem(FTool.getLocale(172));
                        newController = EPlayer.OPP;
                        targetregion = EMoveTarget.DISCARD_PILE;
                        break;
                }
                item.addActionListener(new UniCommandListener(newController, targetregion));
                item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                mMenu.add(item);
            }
        }

    }

    private void setupDiscardPileMenu() {
        EMoveTarget targetregion = null;
        JMenuItem item = null;

        if (getCard().getOwner() == EPlayer.ICH) {
            for (int k = 0; k < 5; ++k) {
                boolean addthis = true;
                switch (k) {
                    case 0:
                        item = new JMenuItem(FTool.getLocale(181));
                        targetregion = translateField(mCard);
                        break;
                    case 1:
                        mMenu.addSeparator();
                        item = new JMenuItem(FTool.getLocale(173));
                        targetregion = EMoveTarget.HAND;
                        break;
                    case 2:
                        item = new JMenuItem(FTool.getLocale(174));
                        targetregion = EMoveTarget.LIBRARY_TOP;
                        break;
                    case 3:
                        item = new JMenuItem(FTool.getLocale(175));
                        targetregion = EMoveTarget.LIBRARY_BOTTOM;
                        break;
                    case 4:
                    default:
                        if (mCard.getInfo().isCardType(ECardType.SPELL)) {
                            item = new JMenuItem(FTool.getLocale(186));
                            targetregion = EMoveTarget.ACTIVATED;
                        } else {
                            addthis = false;
                        }
                        break;
                }
                if (addthis) {
                    item.addActionListener(new UniCommandListener(EPlayer.ICH, targetregion));
                    item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                    mMenu.add(item);
                }
            }
        } else {
            EPlayer newController = EPlayer.ICH;
            switch (getCard().getInfo().getCardType()) {
                case SPELL:
                    for (int k = 0; k < 1; ++k) {
                        switch (k) {
                            case 0:
                            default:
                                item = new JMenuItem(FTool.getLocale(181));
                                newController = EPlayer.ICH;
                                targetregion = translateField(mCard);
                                break;
                        }
                    }
                    break;
                case EVENT:
                    for (int k = 0; k < 1; ++k) {
                        switch (k) {
                            case 0:
                            default:
                                item = new JMenuItem(FTool.getLocale(314));
                                newController = EPlayer.ICH;
                                targetregion = EMoveTarget.EVENT;
                                break;
                        }
                    }
                    break;
                default:
                    return;
            }
            item.addActionListener(new UniCommandListener(newController, targetregion));
            item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
            mMenu.add(item);
        }
    }

    private EMoveTarget translateField(Card card) {
        switch (card.getInfo().getCardType()) {
            case SPELL:
                return EMoveTarget.RESERVED;
            case SUPPORT:
                switch (((SupportCard) card).getInfo().getSupportType()) {
                    case LEADER:
                    case SPELL:
                        return EMoveTarget.ATTACH;
                    case SCENE:
                        return EMoveTarget.SCENE;
                }
            case EVENT:
                return EMoveTarget.EVENT;
            default:
                throw new UnsupportedOperationException();
        }
    }

    class UniCommandListener implements ActionListener {

        EPlayer newController;
        EMoveTarget targetregion;

        UniCommandListener(EPlayer newController, EMoveTarget targetregion) {
            this.newController = newController;
            this.targetregion = targetregion;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Special handling opp's event
            if (mCard.getOwner() == EPlayer.OPP && newController == EPlayer.ICH && targetregion == EMoveTarget.EVENT) {
                mParent.sendRecollectEvent(mCard);
            }
            mParent.moveCard(mCard, newController, targetregion, true, false, "");
        }
    }
}
