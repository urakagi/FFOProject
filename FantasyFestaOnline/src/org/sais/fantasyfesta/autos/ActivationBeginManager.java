/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class ActivationBeginManager {

    public static void exec(IAutosCallback parent, GameInformation gi) {

        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.add(gi.getScene());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.a1) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        mech.doActions(gi, -1, parent, c);
                    }
                }
            }

        }
    }
}
