/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.enums.ESupportType;

/**
 *
 * @author Romulus
 */
public class SupportMoveTriggerManager {

    /**
     * Will trigger on ANY support moving, be sure to check the controller!
     *
     * @param card
     * @param caller
     * @param gi
     * @param from
     * @param newController
     * @param to
     */
    public static void exec(SupportCard card, IAutosCallback caller, GameInformation gi, ERegion from, EPlayer newController, EMoveTarget to) {
        if (caller.isWatcher()) {
            return;
        }

        if (gi.getDelayEffects().hasCard(9034) && card.getOwner() == EPlayer.ICH
                && (to == EMoveTarget.ACTIVATED || to == EMoveTarget.RESERVED || to == EMoveTarget.LEADER_ATTACHMENTS || to == EMoveTarget.SCENE)) {
            caller.adjustSP(2, true, " - " + CardDatabase.getInfo(9034).getName());
        }

        if (newController == EPlayer.ICH && gi.getField(EPlayer.ICH).getLeader().isNo(2600)
                && card.getInfo().isOwnedBy(26) && isPlayerField(to)
                && gi.getPhase() == EPhase.ACTIVATION) {
            caller.draw(1, " - " + CardDatabase.getInfo(2600).getName());
        }

        CardSet<SupportCard> cards = new CardSet<SupportCard>();
        cards.addAll(gi.getField(EPlayer.ICH).getLeaderAttachment());
        cards.add(gi.getScene());

        for (SupportCard c : cards) {
            // Tiggered when some other support is set to ich's field
            if (newController == EPlayer.ICH && isPlayerField(to)) {
                switch (c.getCardNo()) {
                    case 9121:
                        if (c.getController() == EPlayer.ICH) {
                            caller.send("$ADJUSTHP:-1 " + c.getName());
                        }
                        break;
                    case 3413:
                        caller.adjustHP(1, true, false, " - " + c.getName());
                        break;
                    case 3412:
                        if (gi.getPhase() == EPhase.ACTIVATION) {
                            if (card.getInfo().isOwnedBy(34)) {
                                caller.adjustSP(1, true, " - " + c.getName());
                            }
                        }
                        break;
                    case 1216:
                        if (gi.getPhase() == EPhase.REPLENISHING || gi.getPhase() == EPhase.ACTIVATION) {
                            caller.draw(1, " - " + c.getName());
                        }
                        break;
                    case 9097:
                        if (card.getInfo().getSupportType() == ESupportType.SCENE && card.getOwner() == EPlayer.ICH
                                && (gi.getPhase() == EPhase.REPLENISHING || gi.getPhase() == EPhase.ACTIVATION)) {
                            caller.draw(1, " - " + c.getName());
                        }
                        break;
                }
            }
            // Indivisual limitations
            switch (c.getCardNo()) {
                case 3013:
                    if (isPlayerField(from) && card.isIchControl()) {
                        if ((from == ERegion.ACTIVATED || from == ERegion.RESERVED) && to == EMoveTarget.DISCARD_PILE) {
                            caller.adjustHP(1, true, true, " - " + gi.getScene().getName());
                        }
                    }
                    break;
            }
        }

        for (AutoMech mech : card.getInfo().getAutoMechs()) {
            if (mech.timing == AutoMech.Timing.etb && isPlayerField(to)) {
                if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                    mech.doActions(gi, -1, caller, card);
                }
            }
        }

        switch (card.getCardNo()) {
            case 9043:
            case 2712:
            case 1519:
            case 3412:
                if (newController == EPlayer.ICH && gi.isLibraryTargetable(EPlayer.ICH) && isPlayerField(to)) {
                    caller.peekWholeLibrary(" - " + card.getName());
                }
                break;
            case 3218:
                if (newController == EPlayer.ICH && gi.isLibraryTargetable(EPlayer.ICH) && isPlayerField(to)
                && gi.getPhase() == EPhase.ACTIVATION) {
                    caller.peekWholeLibrary(" - " + card.getName());
                }
                break;
            case 3418:
                if (newController == EPlayer.ICH && gi.isHealable(EPlayer.ICH) && isPlayerField(to)) {
                    caller.adjustHP(1, true, false, " - " + card.getName());
                }
                break;
            case 3711:
                if (newController == EPlayer.ICH && gi.isLibraryTargetable(EPlayer.ICH)
                        && isPlayerField(to) && gi.getPhase() == EPhase.ACTIVATION) {
                    caller.draw(1, " - " + card.getName());
                }
                break;
            case 3316:
                if (newController == EPlayer.ICH && isPlayerField(to)) {
                    caller.draw(2, " - " + card.getName());
                    caller.adjustHP(2, true, false, " - " + card.getName());
                }
                break;
            case 2510:
                if (isPlayerField(from) && (to == EMoveTarget.DISCARD_PILE || to == EMoveTarget.HAND)
                        && (gi.getPhase() == EPhase.REPLENISHING || gi.getPhase() == EPhase.BATTLE)) {
                    if (card.getController() == EPlayer.ICH) {
                        caller.randomDiscard(" - " + card.getName());
                    } else {
                        caller.send("$RANDOMDISCARD:" + card.getName());
                    }
                }
                break;
            case 2511:
                if (isPlayerField(from) && (to == EMoveTarget.DISCARD_PILE || to == EMoveTarget.HAND)
                        && (gi.getPhase() == EPhase.REPLENISHING || gi.getPhase() == EPhase.BATTLE)) {
                    if (card.getController() == EPlayer.ICH && gi.isDamageDealtable(EPlayer.ICH)) {
                        caller.adjustHP(-1, true, true, " - " + card.getName());
                    } else if (gi.isDamageDealtable(EPlayer.OPP)) {
                        caller.send("$ADJUSTHP:-1 " + card.getName());
                    }
                }
                break;
            case 2516:
                if (isPlayerField(from) && (to == EMoveTarget.DISCARD_PILE || to == EMoveTarget.HAND)
                        && (gi.getPhase() == EPhase.REPLENISHING || gi.getPhase() == EPhase.BATTLE)) {
                    if (card.getController() == EPlayer.ICH && gi.isDamageDealtable(EPlayer.ICH)) {
                        caller.adjustSP(-1, true, " - " + card.getName());
                    } else if (gi.isDamageDealtable(EPlayer.OPP)) {
                        caller.send("$ADJUSTSP:-1 " + card.getName());
                    }
                }
                break;
        }
    }

    private static boolean isPlayerField(ERegion region) {
        return region == ERegion.ACTIVATED || region == ERegion.RESERVED || region == ERegion.LEADER_ATTACHMENTS;
    }

    private static boolean isPlayerField(EMoveTarget region) {
        return region == EMoveTarget.ACTIVATED || region == EMoveTarget.RESERVED || region == EMoveTarget.LEADER_ATTACHMENTS;
    }
}
