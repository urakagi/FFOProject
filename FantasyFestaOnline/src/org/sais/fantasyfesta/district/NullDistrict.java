/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public class NullDistrict implements District {

    @Override
    public ERegion getRegion() {
        return ERegion.NULL;
    }

    @Override
    public Card getCard() {
        return Card.newNull();
    }

    @Override
    public CardSet getSet() {
        return new CardSet();
    }

    @Override
    public void clear() {
    }

    @Override
    public int remove(GameCore core, Card actCard) {
        return -1;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
    }

    @Override
    public void handleMoving(GameCore core,Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
    }
}
