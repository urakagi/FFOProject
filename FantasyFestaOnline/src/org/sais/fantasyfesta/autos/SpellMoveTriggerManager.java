/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.enums.ECostType;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public class SpellMoveTriggerManager {

    public static void exec(SpellCard card, IAutosCallback caller, GameInformation gi, ERegion from, EMoveTarget to) {
        if (caller.isWatcher()) {
            return;
        }

        // Return from discard pile
        if (card.getController() == EPlayer.ICH && from == ERegion.DISCARD_PILE
                && (to == EMoveTarget.ACTIVATED || to == EMoveTarget.RESERVED)) {
            switch (card.getCardNo()) {
                case 1805:
                    caller.useAbility(card, 1, ECostType.FREE, 0, true);
                    break;
                case 1809:
                    caller.adjustHP(1, true, true, " - " + card.getName());
                    break;
                case 1818:
                    caller.draw(2, " - " + card.getName());
                    break;
            }

            if (gi.getField(EPlayer.ICH).getLeaderAttachment().hasCard(1816) && card.getController() == EPlayer.ICH) {
                caller.adjustHP(1, true, true, " - " + CardDatabase.getCardName(1816));
            }
        }

        // Put into play
        if (card.getController() == EPlayer.ICH
                && (from == ERegion.HAND || from == ERegion.DISCARD_PILE) 
                && (to == EMoveTarget.ACTIVATED || to == EMoveTarget.RESERVED)) {
            for (AutoMech mech : card.getInfo().getAutoMechs()) {
                if (mech.timing == AutoMech.Timing.etb) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        mech.doActions(gi, -1, caller, card);
                    }
                }
            }
            
            if (gi.getField(EPlayer.ICH).getLeaderAttachment().hasCard(9121) && card.getController() == EPlayer.ICH) {
                caller.send("$ADJUSTHP:-1 " + CardDatabase.getCardName(9121));
            }

        }

        switch (gi.getScene().getCardNo()) {
            case 3013:
                if (card.getController() == EPlayer.ICH
                        && (from == ERegion.RESERVED || from == ERegion.ACTIVATED) && to == EMoveTarget.DISCARD_PILE) {
                    caller.adjustHP(1, true, true, " - " + gi.getScene().getName());
                }
                break;
        }
    }
}
