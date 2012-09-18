/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card.cardlabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.ChoiceEffect;
import org.sais.fantasyfesta.enums.ECostType;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
abstract public class CardLabel extends JLabel {

    protected static final int MENUSHIFT = 0;
    protected ICardLabelCallback mParent;
    protected Card mCard;
    protected boolean mWatcherMode = false;
    protected JPopupMenu mMenu;
    protected StandardMouseListener mMouseListener = new StandardMouseListener();

    protected CardLabel(final ICardLabelCallback parent, Card card) {
        this.mParent = parent;
        this.mCard = card;
        if (parent != null) {
            this.mWatcherMode = parent.isWatcher();
        }
        if (!(this instanceof UniLabel)) {
            setToolTipText(card.getInfo().getName());
            CardImageSetter.set(this, card.getInfo(), card.getController(), VERTICAL);
        }
        card.setLabel(this);
    }

    protected CardLabel() {
    }

    public Card getCard() {
        return mCard;
    }

    public CardInfo getInfo() {
        return mCard.getInfo();
    }

    public void doMouseEvent(MouseEvent evt) {
        processMouseEvent(evt);
    }

    protected void setAbilityMenu(boolean limitBattle) {
        mMenu.addSeparator();

        String text = mCard.getInfo().getRuleText();
        int offset = 0;
        int found;
        ECostType costType;
        int cost = 0;
        int index = 1;  //Start from 1, 0 is event
        String display = "";
        while ((found = text.indexOf("^", offset)) >= 0) {
            if (limitBattle ? isBattleAbility(text, found) : isSupportAbility(text, found)) {
                int costOffset = text.indexOf("]", found);
                if (text.substring(costOffset + 1, costOffset + FTool.getLocale(219).length() + 1).equals(FTool.getLocale(219)) && text.charAt(costOffset + FTool.getLocale(219).length() + 1) != 'X') {
                    costType = ECostType.SP;
                    cost = Integer.parseInt(text.substring(costOffset + FTool.getLocale(219).length() + 1, costOffset + FTool.getLocale(219).length() + 2));
                    display = text.substring(costOffset + 1, costOffset + FTool.getLocale(219).length() + 2);
                } else if (text.substring(costOffset + 1, costOffset + FTool.getLocale(220).length() + 1).equals(FTool.getLocale(220)) && text.charAt(costOffset + FTool.getLocale(220).length() + 1) != 'X') {
                    costType = ECostType.HP;
                    cost = Integer.parseInt(text.substring(costOffset + FTool.getLocale(220).length() + 1, costOffset + FTool.getLocale(220).length() + 2));
                    display = text.substring(costOffset + 1, costOffset + FTool.getLocale(220).length() + 2);
                } else if (text.substring(costOffset + 1, costOffset + FTool.getLocale(221).length() + 1).equals(FTool.getLocale(221)) && text.charAt(costOffset + FTool.getLocale(221).length() + 1) != 'X') {
                    costType = ECostType.DECK;
                    cost = Integer.parseInt(text.substring(costOffset + FTool.getLocale(221).length() + 1 , costOffset + FTool.getLocale(221).length() + 2));
                    display = text.substring(costOffset + 1, costOffset + FTool.getLocale(221).length() + 2);
                } else if (text.substring(costOffset + 1, costOffset + FTool.getLocale(225).length() + 1).equals(FTool.getLocale(225))) {
                    costType = ECostType.FREE;
                } else {
                    costType = ECostType.MISC;
                    display = text.substring(costOffset + 1, costOffset + 4);
                }

                if (costType != ECostType.FREE) {
                    JMenuItem item = new JMenuItem(FTool.getLocale(222) + " - " + display);
                    item.addActionListener(new UseAbilityListener(costType, cost, index));
                    item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
                    mMenu.add(item);
                }

            }

            offset = found + 1;
            ++index;
        }

    }

    protected void setTargetMenu() {
        if (mWatcherMode) {
            return;
        }

        mMenu.addSeparator();
        JMenuItem item = new JMenuItem(FTool.getLocale(260));
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mParent.setTarget(true, mCard);
            }
        });
        item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
        mMenu.add(item);
    }

    protected void setChoiceMenu() {
        ArrayList<ChoiceEffect> choices = mCard.getInfo().getChoiceEffects();
        if (choices.size() > 0) {
            mMenu.addSeparator();
        }

        for (ChoiceEffect eff : choices) {
            JMenuItem item = new JMenuItem(eff.getMenuText());
            item.addActionListener(new UseChoiceEffectListener(eff.getEffectIndex(), eff.getMenuText()));
            item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("popmenu.font"), 0, 14));
            mMenu.add(item);
        }
    }

    protected boolean isBattleAbility(String text, int found) {
        return text.substring(found, found + FTool.getLocale(216).length()).equals(FTool.getLocale(216))
                || (text.substring(found, found + FTool.getLocale(217).length()).equals(FTool.getLocale(217)))
                || (text.substring(found, found + FTool.getLocale(218).length()).equals(FTool.getLocale(218)));
    }

    protected boolean isSupportAbility(String text, int found) {
        return text.substring(found, found + 2).equals(FTool.getLocale(226));
    }

    protected void setBattleAbilityMenu() {
        setAbilityMenu(true);
    }

    protected void setSupportAbilityMenu() {
        setAbilityMenu(false);
    }

    protected class StandardMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            try {
                if (mParent == null) {
                    return;
                }
                mParent.showCard(mCard);

                if (evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown()) {
                    if (mMenu != null) {
                        mMenu.show(evt.getComponent(), evt.getX(), evt.getY() - MENUSHIFT);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class CardTypeMismatchException extends Exception {

        CardTypeMismatchException(String message) {
            super(message);
        }
    }

    public static class CardViewListener implements ActionListener {

        private CardInfo mCardInfo;

        public CardViewListener(CardInfo cardInfo) {
            this.mCardInfo = cardInfo;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FTool.cardviewer.show(mCardInfo);
        }
    }

    class UseAbilityListener implements ActionListener {

        ECostType mCostType;
        int mCost;
        int mIndex;

        public UseAbilityListener(ECostType costType, int cost, int index) {
            this.mCostType = costType;
            this.mCost = cost;
            this.mIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mParent.useAbility(mCard, mIndex, mCostType, mCost, true);
        }
    }

    class UseChoiceEffectListener implements ActionListener {

        int mIndex;
        String mText;

        public UseChoiceEffectListener(int index, String text) {
            mIndex = index;
            mText = text;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handleUseChoice(mIndex, e);
            mParent.invokeChoice(true, mCard, mIndex);
        }
    }

    protected void handleUseChoice(int index, ActionEvent e) {
        // Do nothing in default
    }

    protected void fireChoice(int index, ActionEvent e) {
        for (MenuElement m : mMenu.getSubElements()) {
            JMenuItem item = (JMenuItem) m;
            for (ActionListener act : item.getActionListeners()) {
                if (act instanceof UseAbilityListener) {
                    if (index / 10 == ((UseAbilityListener) act).mIndex) {
                        act.actionPerformed(e);
                    }
                }
            }
        }
    }
}
