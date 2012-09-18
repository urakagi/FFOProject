package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.card.*;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class ActivateManager {

    public static void exec(IAutosCallback parent, GameInformation gi, SpellCard actCard, boolean nocost) {
        if (!nocost) {
            PlayField f = gi.getField(EPlayer.ICH);
            PlayField of = gi.getField(EPlayer.OPP);

            // Extra activate HP cost
            int index = actCard.getInfo().getRuleText().indexOf(FTool.getLocale(117));
            if (index >= 0) {
                int cost = Integer.parseInt(actCard.getInfo().getRuleText().substring(index + 13, index + 14));
                parent.adjustHP(-cost, true, true, " - " + actCard.getName());
            }

            // Ich's cards and scene
            CardSet<Card> cards = new CardSet<Card>();
            cards.addAll(f.getLeaderAttachment());
            cards.add(f.getLeader());
            if (!gi.isAbilityDisabled(actCard)) {
                cards.add(actCard);
            }
            try {
                cards.add(actCard.getAttached());
            } catch (NotAttachedException ex) {
            }
            cards.add(gi.getScene());

            for (Card c : cards) {
                for (AutoMech mech : c.getInfo().getAutoMechs()) {
                    if (mech.timing == AutoMech.Timing.ac) {
                        if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                            mech.doActions(gi, -1, parent, actCard);
                        }
                    }
                }
                switch (c.getCardNo()) {
                    case 3405:
                        if (f.getLeaderAttachment().hasCard(3414)
                                && gi.isHealable(EPlayer.ICH) && gi.isLeaderTargetable(c, EPlayer.ICH)) {
                            parent.adjustHP(1, true, false, " - " + c.getName());
                        }
                        break;
                    case 2000:
                        // Suika's leader ability
                        if (nocost || gi.getPhase() != EPhase.ACTIVATION) {
                            break;
                        }
                        for (int i = 0; i < actCard.getInfo().getLevelRequirement(); ++i) {
                            parent.deckout(" - " + c.getName());
                        }
                        break;
                    case 2200:
                        // Kanako leader ability
                        if (actCard.getInfo().getSpellPointRequirement() == 1) {
                            parent.adjustSP(-1, true, " - " + c.getName());
                        }
                        break;
                    case 52:
                        if (!nocost) {
                            parent.adjustSP(-1, true, " - " + c.getName());
                        }
                        break;
                    case 1215:
                        if (!nocost) {
                            parent.adjustSP(-2, true, " - " + c.getName());
                        }
                        break;
                    case 1312:
                        if (gi.isHealable(EPlayer.ICH) && gi.isLeaderTargetable(c, EPlayer.ICH)) {
                            parent.adjustHP(1, true, true, " - " + c.getName());
                        }
                        break;
                    case 1314:
                        if (nocost || gi.getPhase() != EPhase.ACTIVATION) {
                            break;
                        }
                        if (gi.isDamageDealtable(EPlayer.ICH)) {
                            parent.adjustHP(-1, true, true, " - " + c.getInfo().getName());
                        }
                        parent.adjustSP(1, true, " - " + c.getInfo().getName());
                        break;
                    case 32:
                        if (!nocost && gi.getPhase() == EPhase.ACTIVATION) {
                            parent.adjustSP(0 - actCard.getInfo().getLevelRequirement(), true, " - " + gi.getScene().getName());
                        }
                        break;
                    case 317:
                        if (gi.getPhase() == EPhase.ACTIVATION || gi.getPhase() == EPhase.BATTLE) {
                            parent.deckout(" - " + gi.getScene().getName());
                        }
                        break;
                }
            }

            // Opp's cards
            cards = new CardSet<Card>();
            cards.addAll(of.getLeaderAttachment());
            cards.add(of.getLeader());

            for (Card c : cards) {
                switch (c.getCardNo()) {
                    case 2200:
                        // Kanako leader ability
                        if (actCard.getInfo().getSpellPointRequirement() == 1) {
                            parent.adjustSP(-1, true, " - " + c.getName());
                        }
                        break;
                    case 9049:
                        if (actCard.getInfo().getLevelRequirement() <= 1 && !nocost) {
                            parent.adjustSP(-1, true, " - " + c.getName());
                        }
                        break;
                }
            }

            if (gi.getDelayEffects().hasCard(9053) && gi.getPhase() == EPhase.ACTIVATION) {
                parent.adjustSP(2, true, " - " + CardDatabase.getInfo(9053).getName());
            }
        }

    }
}
