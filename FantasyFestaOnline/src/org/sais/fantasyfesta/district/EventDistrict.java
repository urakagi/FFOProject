/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.autos.Effect;
import org.sais.fantasyfesta.autos.PrecheckEffectManager;
import org.sais.fantasyfesta.autos.UseEventAbilityTriggerManager;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.EventCard;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class EventDistrict implements District {

    private EventCard card = EventCard.newNull();

    @Override
    public ERegion getRegion() {
        return ERegion.EVENT;
    }

    @Override
    public EventCard getCard() {
        return card;
    }

    @Override
    public CardSet getSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        card = EventCard.newNull();
    }

    @Override
    public int remove(GameCore core, Card actCard) {
        core.getMainUI().removeEventLabel(card);
        card = EventCard.newNull();
        return -1;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        if (card.getController() == EPlayer.ICH && !card.isNull()) {
            core.moveCard(card, card.getOwner(), EMoveTarget.DISCARD_PILE, true, false, "");
            core.sendAndRefreshCounter();
        }

        card = (EventCard) actCard;
        core.getMainUI().setEventLabel(card);
        core.setPassed(card.getController(), false);

        UseEventAbilityTriggerManager.exec(core, core.getGameInformation(), card, Effect.INDEX_EVENT);

        int event_type = PrecheckEffectManager.FLAG_BATTLE;
        if (card.isIchControl()) {
            event_type = PrecheckEffectManager.handle(card, Effect.INDEX_EVENT, core, core.getGameInformation());
        }

        if ((event_type & PrecheckEffectManager.FLAG_BATTLE) == PrecheckEffectManager.FLAG_BATTLE) {
            if (core.isHost()) {
                core.pushBattleEvent(card, Effect.INDEX_EVENT);
                core.reExecuteBattleSystem();
            }
        }

        if ((event_type & PrecheckEffectManager.FLAG_DELAY) == PrecheckEffectManager.FLAG_DELAY) {
            if (card.isIchControl()) {
                core.pushDelayEvent(card, Effect.INDEX_EVENT);
            }
        }
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        int localeIndex;
        switch (to) {
            case HAND:
                localeIndex = 266;
                break;
            case DISCARD_PILE:
                localeIndex = 267;
                break;
            case LIBRARY_TOP:
                localeIndex = 268;
                break;
            case LIBRARY_BOTTOM:
                localeIndex = 269;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        
        GameInformation gi = core.getGameInformation();
        if (!simple) {
            if (to == EMoveTarget.DISCARD_PILE) {
                core.getMainUI().noLogPlayMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                        card.getName(), "") + extraMessage);
            } else {
                core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                        card.getName(), "") + extraMessage);
            }
        }
    }

    public void setCard(EventCard card) {
        this.card = card;
    }
}
