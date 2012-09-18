/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import java.util.ArrayList;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public class ReplenishEndBattleBeginManager {

    /**
     * Manager to handle end of fill phase and beginning of battle phase. Acter is always ICH.
     *
     * @param parent
     * @param gi
     */
    public static void exec(IAutosCallback parent, GameInformation gi) {

        final PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.addAll(f.getActivated());
        cards.add(gi.getScene());
        for (Card c : f.getReserved()) {
            SpellCard sc = (SpellCard) c;
            if (sc.isAttached()) {
                cards.add(sc.getAttachedDontThrow());
            }
        }
        for (Card c : f.getActivated()) {
            SpellCard sc = (SpellCard) c;
            if (sc.isAttached()) {
                cards.add(sc.getAttachedDontThrow());
            }
        }

        for (Card c : cards) {
            if (gi.isAbilityDisabled(c)) {
                continue;
            }
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.f9 || mech.timing == Timing.b1) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        mech.doActions(gi, -1, parent, c);
                    }
                }
            }

            switch (c.getCardNo()) {
                case 1217:
                    if (!gi.isAttackPlayer(EPlayer.ICH) && f.getHand().size() <= 3 && gi.getPlayer(EPlayer.OPP).getSP() > 0) {
                        parent.send("$ADJUSTSP:-1 " + c.getName());
                    }
                    break;
                case 1717:
                    if (!f.getLeader().isNo(8000)) {
                        parent.moveCard(c, c.getController(), EMoveTarget.HAND, true, false, " - " + c.getName());
                    }
                    break;
                case 1718:
                    if (!f.getLeader().isNo(8001)) {
                        parent.moveCard(c, c.getController(), EMoveTarget.HAND, true, false, " - " + c.getName());
                    }
                    break;
                case 1719:
                    if (!f.getLeader().isNo(8002)) {
                        parent.moveCard(c, c.getController(), EMoveTarget.HAND, true, false, " - " + c.getName());
                    }
                    break;
                case 2907:
                    if (gi.isAttackPlayer(EPlayer.ICH) && gi.isLeaderTargetable(c, EPlayer.ICH)) {
                        parent.adjustHP(-1, true, true, " - " + c.getName());
                    }
                    break;
                case 1215:
                    if (c.getRegion() == ERegion.RESERVED && gi.isAttackPlayer(EPlayer.ICH) && gi.getPlayer(EPlayer.ICH).getSP() > 0) {
                        parent.adjustSP(-1, true, " - " + c.getName());
                    }
                    break;
                case 608:
                    if (f.countDoll() < 4) {
                        parent.moveCard(c, c.getController(), EMoveTarget.RESERVED, true, false, " - " + c.getName());
                    }
                    break;
                case 1515:
                    if (c.getRegion() == ERegion.ACTIVATED && gi.isDamageDealtable(EPlayer.OPP, EPlayer.ICH) 
                            && gi.isLeaderTargetable(c, EPlayer.OPP, EPlayer.ICH) && gi.isAttackPlayer(EPlayer.ICH)) {
                        parent.send("$ADJUSTHP:-1 " + c.getName());
                    }
                    break;
                case 56:
                    batchMoveSpell(f, parent, new BatchMoveCondition() {

                        @Override
                        public boolean isTarget(Card c) {
                            return c.getInfo().getLevelRequirement() == 0;
                        }
                    });
                    break;
                case 514:
                    batchMoveSpell(f, parent, new BatchMoveCondition() {

                        @Override
                        public boolean isTarget(Card c) {
                            return c.getInfo().getLevelRequirement() <= 1;
                        }
                    });
                    break;
                case 1315:
                    batchMoveSpell(f, parent, new BatchMoveCondition() {

                        @Override
                        public boolean isTarget(Card c) {
                            return !c.getInfo().isOwnedBy(f.getLeader().getInfo().getCharId());
                        }
                    });
                    break;
                case 9019:
                    batchMoveSpell(f, parent, new BatchMoveCondition() {

                        @Override
                        public boolean isTarget(Card c) {
                            return c.getInfo().getLevelRequirement() >= 3;
                        }
                    });
                    break;
            }

        }

        cards.clear();
        cards.addAll(of.getLeaderAttachment());
        for (Card c : cards) {
            switch (c.getCardNo()) {
                
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            if (e.index < 0) {
                continue;
            }
            CardInfo info = CardDatabase.getInfo(e.source.getCardNo());
            for (AutoMech mech : info.getAutoMechs()) {
                if (mech.timing == Timing.f9 || mech.timing == Timing.b1) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        if (e.index == mech.effect_index) {
                            mech.doActions(gi, -1, parent, info.createCard());
                        }
                    }
                }
            }
        }
    }

    private static void batchMoveSpell(PlayField f, IAutosCallback parent, BatchMoveCondition cond) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card c : f.getActivated()) {
            if (cond.isTarget(c)) {
                cards.add(c);
            }
        }
        for (Card c : cards) {
            parent.moveCard(c, c.getController(), EMoveTarget.RESERVED, true, false, " - " + c.getName());
        }
    }

    interface BatchMoveCondition {

        boolean isTarget(Card c);
    }
}
