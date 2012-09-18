/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
abstract public class CardSetDistrict implements District {

    protected CardSet<Card> set = new CardSet<Card>();

    @Override
    abstract public ERegion getRegion();

    @Override
    public Card getCard() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CardSet getSet() {
        return set;
    }

    public void add(Card card) {
        set.add(card);
        card.setDistrict(this);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public int remove(GameCore core, Card actCard) {
        int ret = set.indexOf(actCard);
        set.remove(actCard);
        return ret;
    }
}
