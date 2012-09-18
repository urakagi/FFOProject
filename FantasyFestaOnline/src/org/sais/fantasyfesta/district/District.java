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
abstract public interface District {

    public ERegion getRegion();

    public Card getCard();

    public CardSet getSet();

    public void clear();

    public int remove(GameCore core, Card actCard);

    public void add(GameCore core, Card actCard, EMoveTarget to);

    public abstract void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage);

}
