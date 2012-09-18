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
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.CharacterCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class CharacterLabel extends CardLabel {

    private CardSet<SupportCard> mAttachedby = new CardSet<SupportCard>();

    public CharacterLabel(ICardLabelCallback parent, Card card) {
        super(parent, card);
        addMouseListener(new CharacterMouseListener());
    }
    
    @Override
    public CharacterCard getCard() {
        return (CharacterCard) mCard;
    }

    public CardSet<SupportCard> getAttachSet() {
        return mAttachedby;
    }

    public void setAttachSet(CardSet<SupportCard> attachments) {
        mAttachedby = attachments;
    }

    public void setPopupMenu(ArrayList<CharacterCard> chars) {
        if (mCard == null || chars.isEmpty()) {
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

        if (mCard.isOppControl() || mWatcherMode) {
            setTargetMenu();
            add(mMenu);
            return;
        }

        setBattleAbilityMenu();
        mMenu.addSeparator();

        int[] charhash = new int[4];
        int[] indexhash = new int[4];
        int cnt = 0;

        int nowchar = chars.get(0).getCardNo();
        charhash[cnt] = nowchar;
        indexhash[cnt] = 0;
        ++cnt;

        for (int k = 1; k < 4; ++k) {
            if (!chars.get(k).isNo(nowchar)) {
                nowchar = chars.get(k).getCardNo();
                charhash[cnt] = nowchar;
                indexhash[cnt] = k;
                ++cnt;
            }
        }

        for (int k = 0; k < cnt; ++k) {
            JMenuItem item = new JMenuItem(CardDatabase.getInfo(charhash[k]).getName() + FTool.getLocale(179));
            item.addActionListener(new CharacterCommandListener(indexhash[k]));
            item.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("char.font"), 0, 14));
            mMenu.add(item);
        }

        setChoiceMenu();
        setTargetMenu();

        add(mMenu);
    }

    class CharacterCommandListener implements ActionListener {

        int changeto;

        CharacterCommandListener(int changeto) {
            this.changeto = changeto;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mParent.changeLeader(true, changeto);
        }
    }

    class CharacterMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            try {
                if (mParent == null) {
                    return;
                }
                mParent.showCard(mCard);

                if (evt.getButton() == MouseEvent.BUTTON1) {
                    if (!mWatcherMode) {
                        mParent.attach(CharacterLabel.this, true);
                    }
                } else {
                    if (mMenu == null && mWatcherMode) {
                        ArrayList<CharacterCard> cards = new ArrayList<CharacterCard>(4);
                        cards.add((CharacterCard) mCard.getInfo().createCard());
                        cards.add(CharacterCard.newNull());
                        cards.add(CharacterCard.newNull());
                        cards.add(CharacterCard.newNull());
                        setPopupMenu(cards);
                    }
                    if (mMenu != null) {
                        mMenu.show(evt.getComponent(), evt.getX(), evt.getY() - MENUSHIFT);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    @Override
    protected void handleUseChoice(int index, ActionEvent e) {
        fireChoice(index, e);
    }
    
}
