package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import java.util.HashMap;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CharacterCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class PrecheckEffectManager {

    static public final int FLAG_BATTLE = 01;
    static public final int FLAG_INSTANT = 02;
    static public final int FLAG_DELAY = 04;

    /**
     *
     * @param source.getCardNo()
     * @return  The type of event
     */
    public static int handle(Card source, int index, IAutosCallback caller, GameInformation gi) {
        int type = 00;
        for (AutoMech mech : source.getInfo().getAutoMechs()) {
            if (mech.effect_index != index) {
                continue;
            }
            if (AutoMech.isBattleTiming(mech.timing)) {
                type |= FLAG_BATTLE;
            }
            if (mech.timing == Timing.in) {
                if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                    mech.doActions(gi, index, caller, source);
                } else {
                    if (!AutoMech.Limit.isAdditional(mech.limits)) {
                        error(caller);
                    }
                }
                type |= FLAG_INSTANT;
            }
            if (mech.isDelayTiming()) {
                type |= FLAG_DELAY;
            }
        }

        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        switch (source.getCardNo()) {
            // Simple delay events
            case 3116:
            case 2311:
            case 9086:
            case 1611:
            case 1316:
            case 3318:
                caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                return FLAG_DELAY;
            case 3217:
                Card top = f.getLibrary().get(f.getLibrary().size() - 1);
                caller.revealLibTops(1, " - " + source.getName());
                caller.draw(1, " - " + source.getName());
                if (top.getInfo().isOwnedBy(29)) {
                    caller.send("$ADJUSTHP:-1" + " " + top.getName());
                }
                if (top.getInfo().isOwnedBy(30)) {
                    caller.adjustHP(1, true, false, " - " + top.getName());
                }
                return FLAG_INSTANT;
            case 3215:
                caller.send("$ADJUSTSP:" + (-(f.getDiscardPile().counts(3215) + 1)) + " " + source.getName());
                break;
            case 615:
                if (index == 12) {
                    if (gi.isIchAttackPlayer() && gi.isLeaderTargetable(source, EPlayer.OPP)) {
                        int amount = 0;
                        for (int i = 0; i < f.getLeaderAttachment().size();) {
                            SupportCard c = f.getLeaderAttachment().get(i);
                            if (c.getInfo().getRuleText().contains(FTool.getLocale(170))) {
                                ++amount;
                                caller.moveCard(c, EPlayer.ICH, EMoveTarget.DISCARD_PILE, true, false, " - " + CardDatabase.getInfo(615).getName());
                            } else {
                                ++i;
                            }
                        }
                        caller.send("$ADJUSTHP:-" + amount + " " + CardDatabase.getInfo(615).getName());
                        return FLAG_INSTANT;
                    } else {
                        error(caller);
                    }
                }
                break;
            case 50:
                if (f.isLeaderContainsAttribute(FTool.getLocale(215))) {
                    caller.draw(f.getLeaderLevel(), " - " + source.getName());
                } else {
                    error(caller);
                }
                break;
            case 1419:
                caller.moveCard(source, EPlayer.ICH, EMoveTarget.RESERVED, true, false, "");
                return FLAG_BATTLE;
            case 9089:
                if (index == 2) {
                    caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                    return FLAG_DELAY;
                }
                return FLAG_BATTLE;
            case 1805:
                if (index == 1) {
                    caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                    return FLAG_BATTLE;
                }
            case 2208:
                if (of.getBattleCard().getInfo().getLevelRequirement() <= 1 && !of.getBattleCard().isNull()) {
                    caller.adjustSP(2, true, " - " + source.getName());
                }
                return FLAG_BATTLE;
            case 1108:
                if (f.getBattleCard().isAttached()) {
                    caller.adjustSP(1, true, " - " + source.getName());
                }
                break;
            case 1517:
                if (index == 1) {
                    caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                }
                return FLAG_BATTLE;
            case 618:
                // Effect
                if ((index == 11 || index == 12 || index == 13)) {
                    if (!f.getLeaderAttachment().hasCard(610)) {
                        error(caller);
                        return FLAG_INSTANT;
                    }
                    if (index == 12 && f.getLeaderAttachment().counts(618) < 2) {
                        error(caller);
                        return FLAG_INSTANT;
                    }
                    if (index == 13 && f.getLeaderAttachment().counts(618) < 3) {
                        error(caller);
                        return FLAG_INSTANT;
                    }
                }
                if ((index == 14 || index == 15) && !f.getLeaderAttachment().hasCard(617)) {
                    error(caller);
                    return FLAG_INSTANT;
                }
                if (index == 15 && f.getLeaderAttachment().counts(618) < 2) {
                    error(caller);
                    return FLAG_INSTANT;
                }

                // Cost other than regular
                if (f.getLeaderAttachment().counts(618) >= 3) {
                    if (index == 1) {
                        caller.adjustSP(1, true, " - " + source.getName());
                    }
                } else {
                    if (index == 12 || index == 15) {
                        caller.adjustSP(-1, true, " - " + source.getName());
                    }
                }
                return FLAG_DELAY;
            case 9027:
                if (index == 0) {
                    caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                    return FLAG_BATTLE;
                }
            case 9075:
                if (index == 1) {
                    caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                    return FLAG_DELAY | FLAG_BATTLE;
                }
            case 1212:
                caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                return FLAG_INSTANT;
            case 9068:
                if (gi.isLeaderTargetable(source, EPlayer.OPP, EPlayer.ICH)) {
                    int dmg = gi.getPlayer(EPlayer.OPP).getHP() / 2;
                    caller.send("$ADJUSTHP:-" + dmg + " " + source.getName());
                    caller.nextTurn(true);
                }
                break;
            case 319:
                if (gi.isLibraryTargetable(EPlayer.ICH, EPlayer.ICH)) {
                    caller.draw(1, " - " + source.getName());
                    caller.peekLibrary(3, " - " + source.getName());
                } else {
                    error(caller);
                }
                return FLAG_INSTANT;
            case 513:
                if (gi.isAttackPlayer(EPlayer.ICH)
                        && gi.isLeaderTargetable(source, EPlayer.ICH, EPlayer.ICH)) {
                    caller.nextTurn(true);
                }
                return FLAG_INSTANT;
            case 913:
                if (gi.isLeaderTargetable(source, EPlayer.OPP, EPlayer.ICH)) {
                    int dmg = 4 - of.getLeaderLevel();
                    caller.send("$ADJUSTHP:-" + dmg + " " + source.getName());
                } else {
                    error(caller);
                }
                return FLAG_INSTANT;
            case 915:
                if (gi.isAttackPlayer(EPlayer.ICH)) {
                    caller.setSP(0, " - " + source.getName());
                    caller.send("$ADJUSTSP:-" + gi.getPlayer(EPlayer.OPP).getSP() + " " + source.getName());
                } else {
                    error(caller);
                }
                return FLAG_INSTANT;
            case 1115:
                if (gi.getPlayer(EPlayer.ICH).getHP() <= 5) {
                    return 01;
                } else {
                    error(caller);
                    return 00;
                }
            case 1200:
                if (gi.isLibraryTargetable(EPlayer.ICH, EPlayer.ICH)) {
                    caller.peekLibrary(2, " - " + source.getName());
                } else {
                    error(caller);
                }
                return FLAG_INSTANT;
            case 1511:
                if (gi.isLeaderTargetable(source, EPlayer.ICH, EPlayer.ICH)) {
                    if (gi.isHealable(EPlayer.ICH)) {
                        int heal = 0;
                        HashMap<Integer, Boolean> check = new HashMap<Integer, Boolean>();
                        for (CharacterCard ch : f.getChars()) {
                            if (ch.getInfo().getDesignations().contains(FTool.getLocale(167)) && check.get(ch.getCardNo()) == null) {
                                ++heal;
                                check.put(ch.getCardNo(), true);
                            }
                        }
                        caller.adjustHP(heal, true, true, " - " + source.getName());
                    }
                } else {
                    error(caller);
                }
                return FLAG_INSTANT;
            case 118:
            case 1813:
            case 1816:
                caller.moveCard((SupportCard) source, EPlayer.ICH, EMoveTarget.DISCARD_PILE, true, false, "");
                return FLAG_INSTANT;
            case 9031:
                if (gi.isAttackPlayer(EPlayer.ICH) && gi.isLeaderTargetable(source, EPlayer.OPP)) {
                    if (!f.getBattleCard().isNull()) {
                        caller.send("$ADJUSTHP:-" + f.getBattleCard().getInfo().getAttackValue() + " " + source.getName());
                    }
                } else {
                    error(caller);
                }
                return FLAG_INSTANT;
            case 9034:
                if (gi.getDelayEffects().hasCard(source.getCardNo())) {
                    error(caller);
                    return 00;
                }
                caller.send("$DELAYEVENT:" + source.getCardNo() + " " + index);
                return 04;
            case 9044:
                if (gi.getDelayEffects().hasCard(source.getCardNo())) {
                    error(caller);
                    return 00;
                }
                return 04;
            case 9053:
                if (gi.getDelayEffects().hasCard(source.getCardNo())) {
                    error(caller);
                    return 00;
                }
                return 04;
            case 9060:
                caller.shuffle();
                return FLAG_INSTANT;
        }

        if (type != 00) {
            return type;
        }

        return FLAG_BATTLE;
    }

    public static int getType(Card source, int index, IAutosCallback caller, GameInformation gi) {
        switch (source.getCardNo()) {
            case 618:
                return FLAG_DELAY;
        }
        return FLAG_BATTLE;
    }

    static private void error(IAutosCallback caller) {
        caller.illegalUse(true);
    }
}
