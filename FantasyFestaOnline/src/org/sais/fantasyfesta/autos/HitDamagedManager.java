/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import java.util.EnumMap;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class HitDamagedManager {

    private IAutosCallback mParent;
    private GameInformation gi;
    private BattleSystem sys;

    public static void exec(IAutosCallback parent, GameInformation gi, BattleSystem sys, EnumMap<EPlayer, Boolean> hit, EnumMap<EPlayer, Integer> damaged) {
        new HitDamagedManager(parent, gi, sys).exec(hit, damaged);
    }

    private HitDamagedManager(IAutosCallback parent, GameInformation gi, BattleSystem sys) {
        this.mParent = parent;
        this.gi = gi;
        this.sys = sys;
    }

    private void exec(EnumMap<EPlayer, Boolean> hit, EnumMap<EPlayer, Integer> damaged) {
        if (hit.get(EPlayer.OPP)) {
            doHit();
            if (damaged.get(EPlayer.OPP) > 0) {
                doDamaged();
            }
        } else {
            doMiss();
        }

        if (hit.get(EPlayer.ICH)) {
            doOppHit();
        } else {
            doOppMiss();
        }
    }

    private void doHit() {
        PlayField f = gi.getField(EPlayer.ICH);

        CardSet<Card> cards = new CardSet<Card>();
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.ht) {
                    if (mech.isLegal(gi, sys.getBattleValues(), EPlayer.ICH, sys, false)) {
                        mech.doActions(gi, -1, mParent, c);
                    }
                }
            }
            switch (c.getCardNo()) {
                case 3701:
                case 3704:
                    if (gi.isLibraryTargetable(EPlayer.ICH)) {
                        mParent.peekLibrary(1, " - " + c.getName());
                    }
                    break;
                case 620:
                    mParent.peekLibrary(f.getCharacterLevel(6), " - " + c.getName());
                    break;
            }
        }
    }

    private void doOppHit() {
        PlayField f = gi.getField(EPlayer.ICH);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.oht) {
                    if (mech.isLegal(gi, sys.getBattleValues(), EPlayer.ICH, sys, false)) {
                        mech.doActions(gi, -1, mParent, c);
                    }
                }
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            for (AutoMech mech : e.source.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.oht) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        mech.doActions(gi, -1, mParent, e.source);
                    }
                }
            }
        }

    }

    private void doDamaged() {
        PlayField f = gi.getField(EPlayer.ICH);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.dm) {
                    if (mech.isLegal(gi, sys.getBattleValues(), EPlayer.ICH, sys, false)) {
                        mech.doActions(gi, -1, mParent, c);
                    }
                }
            }
        }
    }

    private void doMiss() {
        PlayField f = gi.getField(EPlayer.ICH);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.ms) {
                    if (mech.isLegal(gi, sys.getBattleValues(), EPlayer.ICH, sys, false)) {
                        mech.doActions(gi, -1, mParent, c);
                    }
                }
            }
        }
    }

    private void doOppMiss() {
        PlayField f = gi.getField(EPlayer.ICH);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.oms) {
                    if (mech.isLegal(gi, sys.getBattleValues(), EPlayer.ICH, sys, false)) {
                        mech.doActions(gi, -1, mParent, c);
                    }
                }
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            for (AutoMech mech : e.source.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.oms) {
                    if (mech.isLegal(gi, sys.getBattleValues(), EPlayer.ICH, sys, false)) {
                        mech.doActions(gi, -1, mParent, e.source);
                    }
                }
            }
        }
    }
}
