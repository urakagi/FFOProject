/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class ExtraBattleCostManager {

    public static void exec(IAutosCallback parent, GameInformation gi, SpellCard actCard) {
        if (actCard.isNull()) {
            return;
        }

        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);
        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.add(gi.getScene());
        cards.add(actCard);
        try {
            cards.add(actCard.getAttached());
        } catch (NotAttachedException ex) {
        }

        // Additional SP cost
        int index = actCard.getInfo().getRuleText().indexOf(FTool.parseLocale(115));
        if (index >= 0) {
            parent.adjustSP(-Integer.parseInt(String.valueOf(actCard.getInfo().getRuleText().charAt(index + FTool.parseLocale(115).length()))), true, " - " + actCard.getName());
        }

        // Additional deck cost
        index = actCard.getInfo().getRuleText().indexOf(FTool.parseLocale(116));
        if (index >= 0) {
            int amount = FTool.safeParseInt(String.valueOf(actCard.getInfo().getRuleText().charAt(index + FTool.parseLocale(116).length())));
            for (int i = 0; i < amount; ++i) {
                parent.deckout(" - " + actCard.getName());
            }
        }

        // Other extra costs
        for (Card c : cards) {
            // Untargatble cards special handling
            if (actCard.isNo(2904)) {
                break;
            }
            
            switch (c.getCardNo()) {
                case 2218:
                    if (actCard.getInfo().getAttackValue() >= 5) {
                        parent.adjustSP(-2, true, " - " + c.getName());
                    }
                    break;
                case 2617:
                    if (!gi.getField(EPlayer.ICH).isLeaderSpellBattling()) {
                        parent.adjustSP(-2, true, " - " + c.getName());
                    }
                    break;
                case 1012:
                    if (gi.getField(EPlayer.ICH).getBattleCard().getInfo().getLevelRequirement() >= 3) {
                        parent.adjustSP(-2, true, " - " + c.getName());
                    }
                    break;
                case 1214:
                    if (!gi.getField(EPlayer.ICH).isLeaderContainsAttribute(FTool.getLocale(166))) {
                        parent.adjustSP(-1, true, " - " + c.getName());
                    }
                    break;
                case 2508:
                    int cost = 3 - gi.countGhost(EPlayer.OPP);
                    if (cost > 0) {
                        parent.adjustSP(-cost, true, " - " + c.getName());
                    }
                    break;
                case 52:
                    if (gi.getScene().isNo(32)) {
                        parent.adjustSP(-1, true, " - " + c.getName());
                    }
                    break;
                case 1420:
                    parent.adjustHP(-1, true, true, " - " + c.getName());
                    parent.adjustSP(-1, true, " - " + c.getName());
                    parent.deckout(c.getName());
                    break;
                case 2511:
                    parent.adjustHP(-1, true, false, " - " + c.getName());
                    break;
                case 3512:
                    if (!f.getLeader().isNo(3500)) {
                        parent.adjustSP(-1, true, " - " + c.getName());
                    }
                    break;
                case 3513:
                    if (f.getBattleCard().isAttached()) {
                        parent.adjustHP(-1, true, false, " - " + c.getName());
                    }
                    break;
                case 1215:
                    if (gi.isAttackPlayer(EPlayer.ICH)) {
                        parent.adjustSP(-2, true, " - " + c.getName());
                    }
                    break;
            }
        }

        // Opp's cards
        cards = new CardSet<Card>();
        cards.addAll(of.getLeaderAttachment());
        for (Card c : cards) {
            switch (c.getCardNo()) {
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            switch (e.source.getCardNo()) {
                case 9075:
                    if (e.source.getController() == EPlayer.OPP) {
                        parent.adjustHP(-1, true, true, " - " + CardDatabase.getCardName(9075));
                        parent.adjustSP(-1, true, " - " + CardDatabase.getCardName(9075));
                    }
                    break;
                case 1212:
                    parent.adjustSP(-f.getBattleCard().getInfo().getLevelRequirement(), true, " - " + CardDatabase.getCardName(1212));
                    break;
                case 9086:
                    if (!e.source.isIchControl()) {
                        parent.adjustHP(-4, true, true, " - " + CardDatabase.getCardName(9086));
                    }
                    break;
            }
        }
    }
}
