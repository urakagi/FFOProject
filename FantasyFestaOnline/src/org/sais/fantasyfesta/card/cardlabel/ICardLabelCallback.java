/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card.cardlabel;

import org.sais.fantasyfesta.autos.IAutosCallback;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.enums.ECostType;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public interface ICardLabelCallback {

    public abstract void moveCard(Card card, EPlayer newController, EMoveTarget to, boolean active, boolean simple, String extraMessage);

    void showCard(Card card);

    public abstract boolean attach(CardLabel target, boolean active);

    SupportCard unAttach(SpellCard card, boolean active);

    IAutosCallback getAutosCallback();

    GameInformation getGameInformation();

    public void changeLeader(boolean active, int changeTo);

    public void useAbility(Card card, int index, ECostType costType, int cost, boolean active);

    public void setTarget(boolean active, Card card);

    public void invokeChoice(boolean active, Card card, int index);

    public boolean isWatcher();
    
    public void moveSpellAttachment(SupportCard card, EMoveTarget target, boolean active);

    public void sendRecollectEvent(Card mCard);
}
