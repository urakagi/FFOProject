/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EDamageType;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class DamageManuplateManager {

    /**
     * Get the damage difference with special effects.
     * @param gi
     * @return
     */
    public static int exec(int origin, GameCore core, EDamageType damageType, boolean active) {
        int ret = origin;
        GameInformation gi = core.getGameInformation();
        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        if (!gi.isAbilityDisabled(f.getLastBattleCard())) {
            cards.add(f.getLastBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card card : cards) {
            switch (card.getCardNo()) {
                case 3501:
                    ret -= 1;
                    playMessage(core, card, -1);
                    break;
                case 2618:
                    if (damageType == EDamageType.EVENT_ABILITY) {
                        ret -= 1;
                        playMessage(core, card, -1);
                    }
                    break;
                case 9103:
                    if (damageType != EDamageType.COST) {
                        ret -= 1;
                        playMessage(core, card, -1);
                    }
                    break;
                case 9003:
                    if (damageType == EDamageType.SPELL) {
                        playMessage(core, card, -ret);
                        ret = 0;
                    }
                    break;
                case 2707:
                    if (!gi.isIchAttackPlayer()) {
                        ret -= 1;
                        playMessage(core, card, -1);
                    }
                    break;
                case 2910:
                    if (damageType == EDamageType.SPELL && origin > 0) {
                        ret += 1;
                        playMessage(core, card, 1);
                    }
                    break;
                case 621:
                    ret -= 1;
                    playMessage(core, card, -1);
                    break;
                case 9088:
                    if (ret >= 5) {
                        playMessage(core, card, -ret);
                        ret = 0;
                    }
                    break;
            }
        }

        cards = new CardSet<Card>();
        cards.addAll(of.getLeaderAttachment());
        cards.add(of.getBattleCard());
        cards.add(of.getBattleCard().getAttachedOrNullObject());

        for (Card card : cards) {
            switch (card.getCardNo()) {
                case 9129:
                    if (origin > 0 && damageType != EDamageType.COST) {
                        ret += 1;
                        playMessage(core, card, 1);
                    }
                    break;
                case 9103:
                    if (origin > 0 && damageType != EDamageType.COST) {
                        ret += 1;
                        playMessage(core, card, 1);
                    }
                    break;
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            if (e.source.isIchControl()) {
                // Controlled by ICH
                switch (e.source.getCardNo()) {
                    case 3116:
                        ret -= 3;
                        playMessage(core, e.source, -3);
                        break;
                    case 1316:
                    case 3318:
                        if (damageType != EDamageType.COST) {
                            ret -= 1;
                            playMessage(core, e.source, -1);
                        }
                        break;
                    case 9092:
                        if (damageType == EDamageType.SPELL) {
                            ret /= 2;
                            playMessage(core, e.source, -(origin - ret));
                        }
                        break;
                }
            } else {
                // Controlled by OPP
                switch (e.source.getCardNo()) {
                    case 1611:
                    case 3318:
                        ret += 1;
                        playMessage(core, e.source, 1);
                        break;
                    case 9089:
                        if (origin > 0
                                && (gi.getPhase() == EPhase.REPLENISHING || gi.getPhase() == EPhase.BATTLE)) {
                            ret += 1;
                            playMessage(core, e.source, 1);
                        }
                        break;
                }
            }
        }
        return ret < 0 ? 0 : ret;
    }

    private static void playMessage(GameCore core, Card card, int amount) {
        core.getMainUI().playMessage(FTool.parseLocale(301, card.getName(),
                String.valueOf(amount < 0 ? -amount : amount),
                amount < 0 ? FTool.getLocale(303) : FTool.getLocale(302)));
    }
}
