/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.enums.ECostType;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public interface IAutosCallback {

    void adjustHP(int amount, boolean active, boolean isDamage, String extramessage);

    void adjustSP(int amount, boolean active, String extramessage);

    void deckout(String extramessage);

    void send(String message);

    void setHP(int value, String extramessage);

    void setSP(int value, String extramessage);

    void draw(int amount, String extramessage);

    void illegalUse(boolean active);

    void returnDiscardPileToLibrary();

    void shuffle();

    void nextTurn(boolean active);

    public abstract void moveCard(Card card, EPlayer newController, EMoveTarget to, boolean active, boolean simple, String extraMessage);
    
    boolean isHost();

    void randomDiscard(String extraMessage);

    public void useAbility(Card card, int index, ECostType costType, int cost, boolean active);

    public void sendAndRefreshCounter();
    
    public void peekWholeLibrary(String extramessage);

    void peekLibrary(int amount, String extramessage);

    void revealLibTops(int amount, String extraMessage);

    SupportCard unAttach(SpellCard card, boolean active);
    
    public void moveSpellAttachment(SupportCard card, EMoveTarget destination, boolean active);

    public boolean isWatcher();

    public void discardHint(int amount, String name);
}
