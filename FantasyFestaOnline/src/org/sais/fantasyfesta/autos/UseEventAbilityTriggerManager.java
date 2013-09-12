/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 * Will trigger on both players' event and ability, but only check ich's cards.
 * @author Romulus
 */
public class UseEventAbilityTriggerManager {

    public static void exec(IAutosCallback parent, GameInformation gi, Card source, int index) {
        Effect e = new Effect(source, index);

        if (e.type.isEvent()) {
            if (gi.getDelayEffects().hasCard(9034) && source.isIchControl()) {
                parent.adjustSP(2, true, " - " + CardDatabase.getInfo(9034).getName());
            }
            if (gi.getScene().isNo(317) && source.isIchControl()) {
                parent.deckout(" - " + CardDatabase.getInfo(317).getName());
            }
        }

        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.add(gi.getScene());
        cards.add(f.getLeader());
        cards.add(f.getBattleCard());
        try {
            cards.add(f.getBattleCard().getAttached());
        } catch (NotAttachedException ex) {
        }

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.isLegal(gi, null, mech.target_player, null, false)) {
                    if (mech.timing == Timing.ev && e.type.isEvent() && source.isIchControl()) {
                        mech.doActions(gi, mech.effect_index, parent, source);
                    }
                    if (mech.timing == Timing.opev && e.type.isEvent() && !source.isIchControl()) {
                        mech.doActions(gi, mech.effect_index, parent, source);
                    }
                    if (mech.timing == Timing.ab && e.type.isAbility() && source.isIchControl()) {
                        mech.doActions(gi, mech.effect_index, parent, source);
                    }
                    if (mech.timing == Timing.opab && e.type.isAbility() && source.getController() == EPlayer.OPP) {
                        mech.doActions(gi, mech.effect_index, parent, source);
                    }
                }
            }

            switch (c.getCardNo()) {
                case 3614:
                    if (e.type.isEvent() /*&& source.isIchControl()*/ && gi.getPhase() == EPhase.BATTLE) {
                        EPlayer pl = source.isIchControl() ? EPlayer.ICH : EPlayer.OPP;
                    	Effect.ManualModification effect = new Effect.ManualModification();
                    	effect.m_atk.put(pl, -1);
                    	effect.m_hit.put(pl, -1);
                    	effect.m_icp.put(pl, -1);
                        gi.getEffects().add(effect);
                    }
                    break;
                case 2507:
                    if (!source.isIchControl()) {
                        parent.send("$HPCOST:1 " + c.getName());
                    }
                    break;
                case 1704:
                    if (source.getInfo().getRuleText().contains(FTool.getLocale(213)) && source.isIchControl()
                            && e.type.isAbility() && source.getInfo().isCardType(ECardType.SUPPORT)) {
                        parent.adjustSP(1, true, " - " + c.getName());
                    }
                    break;
                case 1709:
                    if (source.getInfo().getRuleText().contains(FTool.getLocale(213)) && source.isIchControl()
                            && e.type.isAbility() && source.getInfo().isCardType(ECardType.SUPPORT)) {
                        parent.adjustSP(2, true, " - " + c.getName());
                    }
                    break;
                case 2813:
                    if (c.isIchControl() && !gi.getField(EPlayer.ICH).getLeader().isNo(2800)
                            && !source.getInfo().isOwnedBy(gi.getField(EPlayer.ICH).getLeader().getInfo().getCharId())
                            && source.getInfo().isCardType(ECardType.EVENT)
                            && source.isOppControl()) {
                        parent.adjustSP(-2, true, " - " + c.getName());
                    }
                    break;
            }

        }

        cards.clear();
        cards.addAll(of.getLeaderAttachment());
        cards.add(of.getLeader());
        
        for (Card c : cards) {
            switch (c.getCardNo()) {

            }
        }

    }
}
