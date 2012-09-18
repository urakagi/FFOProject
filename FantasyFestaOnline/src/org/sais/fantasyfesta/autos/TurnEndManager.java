/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.*;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public class TurnEndManager {

    static public void exec(IAutosCallback parent, GameInformation gi) {

        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.add(gi.getScene());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.a9) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        mech.doActions(gi, -1, parent, c);
                    }
                }
            }

            switch (c.getCardNo()) {
                case 9117:
                    {
                        if (c.isIchControl()) {
                            int amount = f.getDiscardPile().size() + of.getDiscardPile().size();
                            if (amount >= 40) {
                                parent.send("$DECKOUT:" + 40 + " " + c.getName());
                            }
                        }
                    }
                    break;
                case 617:
                    int amount = gi.getField(EPlayer.ICH).getLeaderAttachment().counts(617);
                    for (Effect e : gi.getDelayEffects()) {
                        if (e.source.isNo(618) && e.source.isIchControl()) {
                            switch (e.index) {
                                case 14:
                                    amount += 1;
                                    break;
                                case 15:
                                    amount += 2;
                                    break;
                            }
                        }
                    }
                    if (amount >= 3 && gi.isDamageDealtable(EPlayer.OPP)
                            && gi.isLeaderTargetable(c, EPlayer.OPP) && gi.isAttackPlayer(EPlayer.ICH)) {
                        parent.send("$ADJUSTHP:-1 " + c.getName());
                    }
                    break;
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            if (e.index < 0) {
                continue;
            }

            CardInfo info = CardDatabase.getInfo(e.source.getCardNo());
            for (AutoMech mech : info.getAutoMechs()) {
                if (mech.timing == Timing.a9) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        if (e.index == mech.effect_index) {
                            mech.doActions(gi, -1, parent, info.createCard());
                        }
                    }
                }
            }

            switch (e.source.getCardNo()) {
                case 9044:
                    parent.setHP(0, " - " + CardDatabase.getInfo(9044).getName());
                    break;
            }

        }

    }
}
