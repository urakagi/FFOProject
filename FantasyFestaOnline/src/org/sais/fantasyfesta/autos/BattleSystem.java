package org.sais.fantasyfesta.autos;

import java.util.EnumMap;
import org.sais.fantasyfesta.autos.AutoMech.Action;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.autos.BattleValues.ChangeType;
import org.sais.fantasyfesta.autos.Effect.ManualModification;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EBulletType;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class BattleSystem {

    private IAutosCallback parent;
    private GameInformation gi;
    private EnumMap<EPlayer, BattleValues> battleValues = new EnumMap<EPlayer, BattleValues>(EPlayer.class);
    private boolean checkingConstant;

    public BattleSystem(IAutosCallback parent, GameInformation gi) {
        this.parent = parent;
        this.gi = gi;
        battleValues.put(EPlayer.ICH, new BattleValues(gi, EPlayer.ICH));
        battleValues.put(EPlayer.OPP, new BattleValues(gi, EPlayer.OPP));
    }

    public void exec() {
        battleValues.put(EPlayer.ICH, new BattleValues(gi, EPlayer.ICH));
        battleValues.put(EPlayer.OPP, new BattleValues(gi, EPlayer.OPP));

        //Basic numbers
        for (EPlayer ep : EPlayer.values()) {
            PlayField f = gi.getField(ep);
            BattleValues v = battleValues.get(ep);
            v.atk = f.getBattleCard().getInfo().getAttackValue();
            v.icp = f.getBattleCard().getInfo().getInterceptValue();
            v.hit = f.getBattleCard().getInfo().getHitValue();
            v.evasion = f.getLeader().getInfo().getEvasionValue();
            v.leader_targetability.reset();
            v.spell_targetability.reset();
            v.isDestinyManip = false;
        }

        checkingConstant = true;
        applyBasicAbility();
        applyTargetable_Prior();
        applyGainShiki();
        applyShiki();
        applyStatics(Timing.bas);
        applyGainAbility();
        checkingConstant = false;
        applyGainAbility_Effects();
        checkingConstant = true;

        applySliver();
        applyStatics(Timing.bns);
        applyOthers();
        applyOthers_DelayEffects();

        checkingConstant = false;
        applyOthers_Effect();
        checkingConstant = true;

        applyLates();
        execBasicAbility();

        for (Effect e : gi.getIllegalEffects()) {
            gi.getEffects().remove(e);
        }
    }

    private void applyBasicAbility() {
        for (EPlayer ep : EPlayer.values()) {
            SpellCard spell = gi.getField(ep).getBattleCard();
            //pierce & barrier
            int isProtection = spell.getInfo().getRuleText().indexOf(FTool.getLocale(111));
            int isPenetration = spell.getInfo().getRuleText().indexOf(FTool.getLocale(110));

            if (isProtection >= 0) {
                battleValues.get(ep).protection = FTool.safeParseInt(String.valueOf(spell.getInfo().getRuleText().charAt(isProtection + 5)));
            } else {
                battleValues.get(ep).protection = 0;
            }

            if (isPenetration >= 0) {
                battleValues.get(ep).penetration = true;
            } else {
                battleValues.get(ep).penetration = false;
            }

            //homing & moving
            int isFastMove = spell.getInfo().getRuleText().indexOf(FTool.getLocale(113));
            int isSlowMove = spell.getInfo().getRuleText().indexOf(FTool.getLocale(114));
            int isHoming = spell.getInfo().getRuleText().indexOf(FTool.getLocale(112));

            if (isFastMove >= 0) {
                battleValues.get(ep).fast = FTool.safeParseInt(String.valueOf(spell.getInfo().getRuleText().charAt(isFastMove + 7)));
            } else {
                battleValues.get(ep).fast = 0;
            }

            if (isSlowMove >= 0) {
                battleValues.get(ep).slow = FTool.safeParseInt(String.valueOf(spell.getInfo().getRuleText().charAt(isSlowMove + 7)));
            } else {
                battleValues.get(ep).slow = 0;
            }

            if (isHoming >= 0) {
                battleValues.get(ep).homing = true;
            } else {
                battleValues.get(ep).homing = false;
            }

            // faith
            int isFaith = spell.getInfo().getRuleText().indexOf(FTool.getLocale(228));
            if (isFaith >= 0) {
                battleValues.get(ep).faith = FTool.safeParseInt(String.valueOf(spell.getInfo().getRuleText().charAt(isFaith + 5)));
            } else {
                battleValues.get(ep).faith = 0;
            }

        }  //of nHost and nCLI
    }

    /**
     * Not suitable for auto-text mechamism
     */
    private void applyTargetable_Prior() {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            BattleValues ov = battleValues.get(rev(ep));

            PlayField f = gi.getField(ep);

            CardSet<Card> cards = new CardSet<Card>();
            cards.addAll(f.getLeaderAttachment());
            cards.add(gi.getScene());
            if (!gi.isAbilityDisabled(f.getBattleCard())) {
                cards.add(f.getBattleCard());
            }
            cards.add(f.getLeader());
            try {
                cards.add(f.getBattleCard().getAttached());
            } catch (SpellCard.NotAttachedException e) {
            }

            for (Card c : cards) {
                switch (c.getCardNo()) {
                    case 3508:
                        if (f.getLeaderLevel() == 4 && isSpellTargetable(c, ep, ep)) {
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 2716:
                        if (isSpellTargetable(c, ep, ep) && !gi.isIchAttackPlayer()) {
                            v.spell_targetability.hexproofAbility();
                        }
                        break;
                    case 9103:
                        ov.disableBasicAbilities();
                        break;
                    case 3400:
                        if (isLeaderTargetable(c, ep, ep) && f.countSupport(-1) >= 3) {
                            v.leader_targetability.hexproofAbility();
                        }
                        break;
                    case 3115:
                        if (!f.getLeader().getInfo().getDesignations().contains(FTool.getLocale(316))) {
                            v.leader_targetability.sealAbility();
                        }
                        break;
                    case 3114:
                        if (f.getBattleCard().getInfo().isOwnedBy(31)) {
                            v.spell_targetability.hexproofAbility();
                        }
                        break;
                    case 3109:
                        if (isLeaderTargetable(c, ep, ep) && isSpellTargetable(c, ep, ep)) {
                            v.leader_targetability.hexproofBoth();
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 3205:
                        PlayField of = gi.getField(rev(ep));
                        if (f.getCharacterLevel(31) >= 1 && !gi.isAttackPlayer(ep)
                        && !(of.getBattleCard().isAttached() && of.countSupport(2116) > 0)) {
                            ov.spell_targetability.sealAbility();
                        }
                        break;
                    case 9024:
                        v.leader_targetability.sealEvent();
                        break;
                    case 521:
                        v.spell_targetability.shroudBoth();
                        v.disableBasicAbilities();
                        v.atk -= 3;
                        v.icp -= 3;
                        v.hit -= 3;
                        break;
                    case 43:
                        v.disableBasicAbilities();
                        break;
                    case 117:
                        v.spell_targetability.sealBoth();
                        break;
                    case 209:
                        v.typechange = ChangeType.Conentrate;
                        break;
                    case 416:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 809:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.typechange = ChangeType.Spread;
                            v.hit += 1;
                        }
                        break;
                    case 1310:
                        v.typechange = ChangeType.Normal;
                        break;
                    case 1617:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.typechange = ChangeType.Conentrate;
                            v.ability_disabled[Ability.Homing.ordinal()] = true;
                        }
                        break;
                    case 9001:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 2904:
                        v.spell_targetability.shroudAbility();
                        break;
                    case 2807:
                        v.spell_targetability.hexproofAbility();
                        break;
                    case 909:
                        v.spell_targetability.shroudBoth();
                        if (f.getCharacterLevel(9) == 4) {
                            ov.spell_targetability.shroudBoth();
                        }
                        break;
                    case 1908:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 1909:
                        if (f.getCharacterLevel(19) == 4) {
                            ov.leader_targetability.shroudBoth();
                        }
                        break;
                    case 2205:
                        if (!gi.isAttackPlayer(ep)) {
                            ov.leader_targetability.shroudAbility();
                        }
                        break;
                    case 1000:
                        v.leader_targetability.hexproofBoth();
                        break;
                    case 2116:
                        v.leader_targetability.hexproofBoth();
                        if (f.getBattleCard().isAttached()) {
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 9102:
                        v.leader_targetability.sealBoth();
                        ov.leader_targetability.sealBoth();
                        break;
                    case 17:
                        if (f.getLeader().getInfo().getDesignations().contains(FTool.getLocale(166))) {
                            v.spell_targetability.hexproofBoth();
                        }
                        break;
                    case 9066:
                        if (f.getBattleCard().isAttached()) {
                            v.spell_targetability.hexproofBoth();
                        } else {
                            v.disableBasicAbilities();
                        }
                        break;
                }

            }

            for (Effect e : gi.getEffects()) {
                if (e.source.isNo(719)) {
                    battleValues.get(rev(e.source.getController())).isDestinyManip = true;
                }
            }

            for (Effect e : gi.getDelayEffects()) {
                switch (e.source.getCardNo()) {
                    case 2311:
                        switch (e.index) {
                            case 1:
                                battleValues.get(e.source.getController()).spell_targetability.shroudBoth();
                                break;
                            case 2:
                                battleValues.get(rev(e.source.getController())).spell_targetability.shroudBoth();
                                break;
                        }
                        break;
                }
            }
        }

    }

    private void applyStatics(Timing timing) {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);

            PlayField f = gi.getField(ep);

            CardSet<Card> cards = new CardSet<Card>();
            cards.addAll(f.getLeaderAttachment());
            cards.add(gi.getScene());
            cards.add(f.getLeader());
            if (!gi.isAbilityDisabled(f.getBattleCard())) {
                cards.add(f.getBattleCard());
            }
            try {
                cards.add(f.getBattleCard().getAttached());
            } catch (SpellCard.NotAttachedException e) {
            }

            for (Card c : cards) {
                for (AutoMech mech : c.getInfo().getAutoMechs()) {
                    if (mech.timing == timing) {
                        if (mech.isLegal(gi, battleValues, ep, this, false)) {
                            mech.doModifications(gi.isAttackPlayer(ep), -1, battleValues, ep);
                        }
                    }
                }
            }
        }
    }

    private void applyGainAbility() {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            BattleValues ov = battleValues.get(rev(ep));

            PlayField f = gi.getField(ep);
            PlayField of = gi.getField(rev(ep));

            CardSet<Card> cards = new CardSet<Card>();
            cards.addAll(f.getLeaderAttachment());
            cards.add(gi.getScene());
            cards.add(f.getLeader());
            if (!gi.isAbilityDisabled(f.getBattleCard())) {
                cards.add(f.getBattleCard());
            }
            try {
                cards.add(f.getBattleCard().getAttached());
            } catch (SpellCard.NotAttachedException e) {
            }

            for (Card c : cards) {
                switch (c.getCardNo()) {
                    case 3606:
                        if (of.getLeader().getInfo().getEvasionValue() <= 2) {
                            v.protection += 1;
                        }
                        break;
                    case 3509:
                        if (f.getLeader().isNo(3500) && isSpellTargetable(c, rev(ep), ep) && gi.isAttackPlayer(ep)) {
                            ov.ability_disabled[Ability.Faith.ordinal()] = true;
                            ov.ability_disabled[Ability.Protection.ordinal()] = true;
                        }
                        break;
                    case 3209:
                        if (gi.isAttackPlayer(ep)) {
                            int amount = of.getLeaderLevel();
                            v.protection += amount;
                            v.hit += amount;
                        }
                        break;
                    case 2914:
                        v.disableBasicAbilities();
                        ov.disableBasicAbilities();
                        break;
                    case 1109:
                        if (f.getBattleCard().isAttached()) {
                            v.slow += 2;
                            v.fast += 2;
                        } else {
                            v.slow += 1;
                            v.fast += 1;
                        }
                        break;
                    case 420:
                        int cnt = f.countCharSupport(4, true);
                        v.atk += cnt;
                        if (isSpellTargetable(c, ep, ep) && cnt >= 3) {
                            v.penetration = true;
                            v.protection += 2;
                        }
                        break;
                    case 1019:
                        if (isSpellTargetable(c, ep, ep) && ((SpellCard) c).isAttached()) {
                            v.protection += 1;
                            v.hit += 1;
                        }
                        break;
                    case 2608:
                        v.protection += f.getHand().size() / 3;
                        break;
                    case 320:
                        if (v.atk >= 4) {
                            v.penetration = true;
                            v.homing = true;
                        }
                        break;
                    case 2104:
                        if (f.getCharacterLevel(22) >= 1 || f.getCharacterLevel(23) >= 1 && isSpellTargetable(c, ep, ep)) {
                            v.faith += 1;
                        }
                        break;
                    case 507:
                        if (isSpellTargetable(c, ep, ep) && gi.getPlayer(ep).getHP() <= 5) {
                            v.penetration = true;
                            v.atk += 1;
                            v.hit += 3;
                        }
                        break;
                    case 606:
                        v.protection += f.getLeaderAttachment().counts(610);
                        for (Effect e : gi.getDelayEffects()) {
                            if (e.source.isNo(618) && e.source.getController() == ep) {
                                switch (e.index) {
                                    case 11:
                                        v.protection += 1;
                                        break;
                                    case 12:
                                        v.protection += 2;
                                        break;
                                    case 13:
                                        v.protection += 3;
                                        break;
                                }
                            }
                        }
                        break;
                    case 1101:
                        if (((SpellCard) c).isAttached()) {
                            if (isSpellTargetable(c, ep, ep) && ((SpellCard) c).getAttachedDontThrow().isNo(1112)) {
                                v.slow += 1;
                            }
                        }
                        break;
                    case 1208:
                        if (isSpellTargetable(c, ep, ep) && gi.getPlayer(ep).getHP() <= 5) {
                            v.protection += 3;
                            v.icp += 1;
                            v.hit += 3;
                        }
                        break;
                    case 1409:
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            battleValues.get(rev(ep)).disableBasicAbilities();
                        }
                        break;
                    case 1901:
                        if (isSpellTargetable(c, ep, ep) && gi.getPlayer(rev(ep)).getHP() >= 20) {
                            v.homing = true;
                            v.atk += 1;
                            v.icp += 1;
                        }
                        break;
                    case 1905:
                        if (isSpellTargetable(c, ep, ep) && gi.getField(rev(ep)).getBattleCard().getInfo().getLevelRequirement() >= 3) {
                            v.protection += 1;
                            v.hit += 1;
                        }
                        break;
                    case 1618:
                        v.slow += f.getCharacterLevel(15);
                        break;
                    case 3804:
                        if (f.getCharacterLevel(33) >= 1 || f.getCharacterLevel(34) >= 1 || f.getCharacterLevel(37) >= 1) {
                            v.faith += 1;
                            v.hit += 1;
                        }
                        break;
                    case 2204:
                        if (f.getCharacterLevel(21) >= 1 || f.getCharacterLevel(23) >= 1) {
                            v.protection += 1;
                        }
                        break;
                    case 2206:
                        if (gi.getField(rev(ep)).getBattleCard().getInfo().getLevelRequirement() <= 1 && isSpellTargetable(c, ep, ep)) {
                            v.faith += 1;
                        }
                        break;
                    case 2102:
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            ov.ability_disabled[Ability.Faith.ordinal()] = true;
                        }
                        break;
                    case 2316:
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            if (!of.isLeaderContainsAttribute(FTool.getLocale(168)) && !of.isLeaderContainsAttribute(FTool.getLocale(237))) {
                                ov.disableBasicAbilities();
                            }
                        }
                        break;
                    case 2216:
                        if (getBattleCardLevelRequirement(f, of) > getBattleCardLevelRequirement(of, f)) {
                            v.faith += 1;
                        }
                        break;
                    case 1717:
                        if (isSpellTargetable(c, rev(ep), ep) && getBattleCardLevelRequirement(of, f) <= 1) {
                            ov.disableBasicAbilities();
                        }
                        break;
                }
            }
        }
    }

    private void applyGainAbility_Effects() {
        for (Effect e : gi.getEffects()) {
            EPlayer ep = e.source.getController();
            BattleValues v = battleValues.get(ep);
            BattleValues ov = battleValues.get(rev(ep));

            Card c = e.source;
            if (gi.isAbilityDisabled(c)) {
                continue;
            }
            PlayField f = gi.getField(ep);
            PlayField of = gi.getField(rev(ep));

            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (e.index != mech.effect_index) {
                    continue;
                }
                if (mech.timing == Timing.baa) {
                    if (mech.isLegal(gi, battleValues, ep, this, false)) {
                        mech.doModifications(gi.isAttackPlayer(ep), e.index, battleValues, ep);
                    } else {
                        if (!AutoMech.Limit.isAdditional(mech.limits)) {
                            error(e);
                        }
                    }
                }
            }

            switch (e.source.getCardNo()) {
                case 3414:
                    if (e.index == 1) {
                        if (f.getBattleCard().isAttached() && isSpellTargetable(c, ep, ep)) {
                            v.faith += 1;
                            v.hit += 1;
                        } else {
                            error(e);
                        }
                    }
                    break;
                case 900:
                    if (isSpellTargetable(c, ep, ep) && !hasBasicAbility(v)) {
                        v.atk += 1;
                    } else {
                        error(e);
                    }
                    break;
                case 51:
                    if (f.isLeaderContainsAttribute(FTool.getLocale(171)) && isSpellTargetable(c, ep, ep)) {
                        int lv = f.getLeaderLevel();
                        v.slow += lv;
                        v.fast += lv;
                    } else {
                        error(e);
                    }
                    break;
                case 3006:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.protection += 1;
                        if (f.getDiscardPile().get(f.getDiscardPile().size() - 1).getInfo().isCardType(ECardType.SPELL)) {
                            v.atk += 1;
                            v.hit += 1;
                        }
                    }
                    break;
                case 2414:
                    switch (e.index) {
                        case 1:
                            ov.typechange = ChangeType.Conentrate;
                            break;
                        case 2:
                            ov.typechange = ChangeType.Normal;
                            break;
                        case 3:
                            ov.typechange = ChangeType.Spread;
                            break;
                    }
                    break;
                case 40:
                    if (v.faith <= 0) {
                        v.faith += 1;
                    }
                    if (ov.faith <= 0) {
                        ov.faith += 1;
                    }
                    break;
                case 14:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.hit += 2;
                        v.protection += 2;
                    } else {
                        error(e);
                    }
                    break;
                case 511:
                    if (isSpellTargetable(c, ep, ep)) {
                        if (v.shiki) {
                            v.homing = true;
                            v.atk += 2;
                            v.icp += 2;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 614:
                    if (isSpellTargetable(c, ep, ep)) {
                        int dolls = f.countDoll();
                        v.protection += dolls;
                        v.hit += dolls;
                        if (dolls >= 5) {
                            v.atk += 2;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 1115:
                    if (isSpellTargetable(c, ep, ep)) {
                        int lv = f.getCharacterLevel(11);
                        v.icp += lv;
                        v.fast += lv;
                        v.slow += lv;
                        v.hit += lv;
                    } else {
                        error(e);
                    }
                    break;
                case 1116:
                    if (isSpellTargetable(c, ep, ep) && f.getBattleCard().isAttached()) {
                        v.atk += 2;
                        v.hit += 1;
                        v.fast += 2;
                        v.slow += 2;
                    } else {
                        error(e);
                    }
                    break;
                case 1516:
                    if (isSpellTargetable(c, ep, ep) && v.shiki) {
                        v.homing = true;
                        v.protection += 2;
                    } else {
                        error(e);
                    }
                    break;
                case 9013:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.homing = true;
                        v.atk += f.getBattleCard().getInfo().getAttackValue();
                    } else {
                        error(e);
                    }
                    break;
                case 1102:
                    if (isSpellTargetable(c, ep, ep) && f.getBattleCard().getAttachedOrNullObject().isNo(1110)) {
                        v.penetration = true;
                        v.hit += 1;
                    } else {
                        error(e);
                    }
                    break;
                case 1107:
                    if (isSpellTargetable(c, ep, ep) && f.getBattleCard().isAttached()) {
                        v.homing = true;
                    } else {
                        error(e);
                    }
                    break;
                case 1506:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.homing = true;
                        if (v.shiki) {
                            v.atk += 1;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 1607:
                    if (isSpellTargetable(c, ep, ep)) {
                        if (v.shiki) {
                            v.slow += 1;
                            v.icp += 1;
                            v.hit += 1;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 1609:
                    if (isSpellTargetable(c, rev(ep), ep) && of.getBattleCard().getInfo().getBulletType() == EBulletType.NORMAL) {
                        ov.typechange = ChangeType.Conentrate;
                    } else {
                        error(e);
                    }
                    break;
                case 2308:
                    if (ov.faith <= 0 && isSpellTargetable(c, ep, ep)) {
                        v.slow += 1;
                        v.fast += 1;
                        if (f.getCharacterLevel(23) == 4) {
                            v.slow += 1;
                            v.fast += 1;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 2213:
                    switch (e.index) {
                        case 1:
                            if (v.faith > 0) {
                                v.icp += 1;
                            }
                            break;
                        case 2:
                            if (ov.faith <= 0) {
                                ov.icp -= 1;
                            }
                            break;
                    }
                    break;
            }
        }

    }

    private void applySliver() {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            PlayField f = gi.getField(ep);
            PlayField of = gi.getField(rev(ep));
            Card c = f.getBattleCard();

            if (!isSpellTargetable(new EffectType(c), ep, ep)) {
                continue;
            }

            if (f.getCharacterLevel(10) > 0) {
                if (c.getInfo().getCharacterRequirement().contains(FTool.getLocale(139))) {
                    v.fast += f.countSpell(1001, ERegion.ACTIVATED);
                    v.slow += f.countSpell(1002, ERegion.ACTIVATED);
                    v.hit += f.countSpell(1003, ERegion.ACTIVATED);
                    v.atk += f.countSpell(1004, ERegion.ACTIVATED);
                    v.icp += f.countSpell(1005, ERegion.ACTIVATED);
                }
                switch (c.getCardNo()) {
                    case 1001:
                        v.fast++;
                        break;
                    case 1002:
                        v.slow++;
                        break;
                    case 1003:
                        v.hit++;
                        break;
                    case 1004:
                        v.atk++;
                        break;
                    case 1005:
                        v.icp++;
                        break;
                }
            }

            int recolloectUndine = f.getActivated().counts(3202);
            if (f.getBattleCard().isNo(3202)) {
                recolloectUndine++;
            }
            if (recolloectUndine > 0 && f.getBattleCard().getInfo().isOwnedBy(32)) {
                v.slow += recolloectUndine;
            }

            if (getBattleCardLevelRequirement(f, of) == 0 && !f.getBattleCard().isNull()) {
                int amount = f.getActivated().counts(58);
                if (f.getBattleCard().isNo(58)) {
                    amount++;
                }
                v.atk += amount;
                v.icp += amount;
                v.hit += amount;
            }
        }
    }

    private void applyGainShiki() {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            PlayField f = gi.getField(ep);

            CardSet<Card> cards = new CardSet<Card>();

            if (!gi.isAbilityDisabled(f.getBattleCard())) {
                cards.add(f.getBattleCard());
            }
            cards.add(f.getBattleCard().getAttachedOrNullObject());

            for (Card c : cards) {
                for (AutoMech mech : c.getInfo().getAutoMechs()) {
                    if (mech.timing == Timing.st) {
                        for (Action a : mech.actions) {
                            if (a.type == AutoMech.Action_Type.ski) {
                                if (mech.isLegal(gi, battleValues, ep, this, checkingConstant)) {
                                    v.shiki = true;
                                }
                            }
                        }
                    }
                }
            }

            cards.clear();
            cards.addAll(f.getLeaderAttachment());
            cards.addAll(f.getActivated());
            cards.add(gi.getScene());
            cards.add(f.getLeader());
            cards.add(f.getBattleCard());
            cards.add(f.getBattleCard().getAttachedOrNullObject());

            for (Card c : cards) {
                switch (c.getCardNo()) {
                    case 1509:
                        if (f.getBattleCard().getInfo().isOwnedBy(15)) {
                            v.shiki = true;
                        }
                        break;
                    case 1614:
                        if (f.isLeaderSpellBattling()) {
                            v.shiki = true;
                        }
                        break;
                }
            }

            for (Effect e : gi.getEffects()) {
                switch (e.source.getCardNo()) {
                    case 1517:
                        if (f.isLeaderSpellBattling() && e.source.getController() == ep) {
                            v.shiki = true;
                        }
                        break;
                }
            }
        }

    }

    private void applyShiki() {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            if (!v.shiki) {
                continue;
            }

            PlayField f = gi.getField(ep);

            Card c = f.getBattleCard();
            if (!gi.isAbilityDisabled(c)) {

                switch (c.getCardNo()) {
                    case 518:
                        if (isSpellTargetable(c, ep, ep)) {
                            if (v.shiki) {
                                v.homing = true;
                                v.hit += 1;
                            }
                        }
                        break;
                    case 1508:
                        if (v.shiki && isSpellTargetable(c, ep, ep)) {
                            v.homing = true;
                            v.fast += 1;
                            v.icp += 1;
                            v.hit += 1;
                        }
                        break;
                }
            }

            switch (f.getLeader().getCardNo()) {
                case 1500:
                    if (v.shiki && isSpellTargetable(c, ep, ep)) {
                        v.protection += 1;
                    }
                    break;
                case 1600:
                    if (v.shiki && isSpellTargetable(c, ep, ep)) {
                        v.slow += 1;
                        v.fast += 1;
                        v.hit += 1;
                    }
                    break;
            }

        }
    }

    private boolean hasBasicAbility(BattleValues bv) {
        return (bv.faith > 0 || bv.homing || bv.penetration || bv.slow > 0 || bv.fast > 0 || bv.protection > 0);
    }

    private void applyOthers() {
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            BattleValues ov = battleValues.get(rev(ep));
            PlayField f = gi.getField(ep);
            PlayField of = gi.getField(rev(ep));

            CardSet<Card> cards = new CardSet<Card>();
            cards.addAll(f.getLeaderAttachment());
            cards.add(gi.getScene());
            cards.add(f.getLeader());
            if (!gi.isAbilityDisabled(f.getBattleCard())) {
                cards.add(f.getBattleCard());
            }
            cards.add(f.getBattleCard().getAttachedOrNullObject());

            for (Card c : cards) {
                switch (c.getCardNo()) {
                    case 2818:
                        if (isSpellTargetable(c, ep, ep)) {
                            if (ov.protection > 0) {
                                v.atk += 2;
                            }
                            if (ov.slow > 0 || ov.fast > 0) {
                                v.hit += 2;
                            }
                        }
                        break;
                    case 2918:
                        if (of.getLeader().getInfo().getHitPoints() >= 22 && isSpellTargetable(c, ep, ep)) {
                            v.atk += 1;
                        }
                        break;
                    case 2519:
                        if (f.getDiscardPile().size() >= 10 && isSpellTargetable(c, ep, ep)) {
                            v.icp += 1;
                            v.protection += 1;
                        }
                        break;
                    case 3609:
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            if (ov.protection > 0) {
                                ov.hit -= 3;
                            }
                        }
                        break;
                    case 3607:
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            if (ov.protection > 0) {
                                ov.atk -= 1;
                                ov.icp -= 1;
                                ov.hit -= 1;
                            }
                        }
                        break;
                    case 3508:
                        if (isSpellTargetable(c, ep, ep) && getBattleCardLevelRequirement(of, f) <= 1
                                && !of.getBattleCard().isNull()) {
                            v.atk -= 2;
                            v.hit -= 1;
                        }
                        break;
                    case 3212:
                        if (f.getBattleCard().getInfo().getCharId() == of.getBattleCard().getInfo().getCharId()
                                && f.getBattleCard().getInfo().getCharId() != 0) {
                            v.atk -= 1;
                            v.icp -= 1;
                            v.hit -= 1;
                        }
                        break;
                    case 3213:
                        if (getBattleCardLevelRequirement(f, of) >= 1
                                && f.getCharacterLevel(f.getBattleCard().getInfo().getCharId()) == 0) {
                            v.atk += 1;
                            v.icp += 1;
                            v.hit += 1;
                        }
                        break;
                    case 3208:
                        if (gi.getPlayer(ep).getHP() <= 5) {
                            v.hit += 2;
                            v.evasion += 3;
                            if (f.getLeaderLevel() == 4) {
                                v.atk += 1;
                                v.icp += 1;
                            }
                        }
                        break;
                    case 2418:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.hit -= f.getBattleCard().getInfo().getHitValue() / 2;
                        }
                        break;
                    case 9012:
                        if (isSpellTargetable(c, ep, ep)) {
                            if (getBattleCardLevelRequirement(f, of) != 1) {
                                v.atk -= 1;
                                v.icp -= 1;
                            }
                        }
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            if (getBattleCardLevelRequirement(of, f) != 1) {
                                ov.atk -= 1;
                                ov.icp -= 1;
                            }
                        }
                        break;
                    case 3009: {
                        int indispile = f.countSpell(-1, ERegion.DISCARD_PILE);
                        int onfield = f.countSpellOnField(-1);
                        int amount = indispile <= onfield ? indispile : onfield;
                        v.atk += amount;
                    }
                    break;
                    case 3708: {
                        int onfield = f.countSpellOnField(-1);
                        int same = f.countSpellOnField(c.getCardNo());
                        v.atk += same;
                        v.faith += same;
                        if (onfield >= 8) {
                            v.atk += 1;
                            v.icp += 1;
                            v.hit += 1;
                        }
                    }
                    break;
                    case 1102:
                        if (isSpellTargetable(c, ep, ep) && f.getBattleCard().getAttachedOrNullObject().isNo(1110)) {
                            v.hit += 1;
                        }
                        break;
                    case 3204:
                        if (f.getLeaderAttachment().size() > 0 && isSpellTargetable(c, ep, ep)) {
                            v.protection += 1;
                        }
                        break;
                    case 2603:
                        if (f.getLeaderAttachment().size() > 0 && isSpellTargetable(c, ep, ep)) {
                            v.atk += 1;
                        }
                        break;
                    case 2601:
                        if (getBattleCardLevelRequirement(of, f) <= 1) {
                            v.protection += 1;
                        }
                        break;
                    case 919:
                        if (gi.getPlayer(rev(ep)).getHP() - gi.getPlayer(ep).getHP() >= 5) {
                            v.evasion += 2;
                        }
                        break;
                    case 2507:
                        if (gi.countGhost(rev(ep)) <= 0) {
                            v.atk -= 1;
                        }

                        break;
                    case 2502:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.atk += gi.countGhost(rev(ep));
                        }

                        break;
                    case 2501:
                        if (gi.getPlayer(ep).getHP() <= 10) {
                            v.icp += 1;
                            v.hit += 2;
                        }

                        break;
                    case 220:
                        if (f.getBattleCard().getAttachedOrNullObject().getInfo().isOwnedBy(2)) {
                            v.atk += 1;
                        }
                        break;
                    case 2106:
                        if (!hasBasicAbility(ov)) {
                            ov.hit -= 1;
                        }

                        break;
                    case 601:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.hit += f.countDoll();
                        }

                        break;
                    case 604:
                        if (isSpellTargetable(c, ep, ep) && f.countDoll() >= 3) {
                            v.hit += 3;
                        }

                        break;
                    case 715:
                        if (isSpellTargetable(c, ep, ep) && f.getBattleCard().getAttachedOrNullObject().isNo(709)) {
                            v.atk += 1;
                            v.hit += 1;
                        }

                        break;
                    case 905:
                        if (isSpellTargetable(c, ep, ep) && battleValues.get(rev(ep)).slow > 0 && gi.isAttackPlayer(ep)) {
                            v.hit += 3;
                        }

                        break;
                    case 1103:
                        if (isSpellTargetable(c, ep, ep) && f.getBattleCard().getAttachedOrNullObject().isNo(1111)) {
                            battleValues.get(rev(ep)).evasion -= 1;
                        }

                        break;
                    case 1601:
                        if (isSpellTargetable(c, ep, ep) && v.shiki) {
                            v.atk += 1;
                        }

                        break;
                    case 1609:
                        if (isSpellTargetable(c, rev(ep), ep) && f.getCharacterLevel(16) == 4) {
                            battleValues.get(rev(ep)).ability_disabled[Ability.Homing.ordinal()] = true;
                        }
                        break;
                    case 1902:
                        if (isSpellTargetable(c, ep, ep) && of.getLeader().getInfo().getEvasionValue() >= 3) {
                            v.hit += 4;
                        }
                        break;
                    case 1906:
                        if (isSpellTargetable(c, ep, ep)) {
                            if (battleValues.get(rev(ep)).penetration) {
                                v.fast += 2;
                                v.slow += 2;
                            }
                            if (battleValues.get(rev(ep)).homing) {
                                v.protection += 1;
                            }
                        }
                        break;
                    case 2006:
                        if (f.getBattleCard().isAttached() && isSpellTargetable(c, ep, ep)) {
                            v.hit -= 3;
                        }
                        break;
                    case 2009:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.atk += f.getCharacterLevel(20);
                            v.hit += f.getCharacterLevel(20);
                        }

                        break;
                    case 1320:
                        if (isSpellTargetable(c, ep, ep)) {
                            v.atk -= f.countSupport(1320) + of.countSupport(1320);
                        }
                        break;
                    case 718:
                        if (isSpellTargetable(c, ep, ep)) {
                            if (f.getHand().size() == 0) {
                                v.hit += 4;
                            }
                        }
                        break;
                    case 2112:
                        if (hasBasicAbility(battleValues.get(rev(ep)))) {
                            v.atk += 1;
                            v.hit += 1;
                        }
                        break;
                    case 9043:
                        if (isSpellTargetable(c, ep, ep)) {
                            int cnt = f.countSupport(9043);
                            v.atk += cnt;
                            v.icp += cnt;
                            v.hit += cnt;
                        }
                        break;
                    case 1220:
                        if (gi.getPlayer(rev(ep)).getSP() <= 1 && isSpellTargetable(c, rev(ep), ep)) {
                            ov.atk -= 1;
                            ov.icp -= 1;
                        }
                        break;
                    case 2515:
                        if (of.getBattleCard().isAttached()) {
                            if (of.getBattleCard().getAttachedOrNullObject().getInfo().getRuleText().contains(FTool.getLocale(230)) || gi.getScene().isNo(2513)) {
                                if (isSpellTargetable(c, rev(ep), ep)) {
                                    ov.atk -= 1;
                                    ov.icp -= 1;
                                }
                            }
                        }
                        break;
                    case 609:
                        if (isSpellTargetable(c, ep, ep) && f.isLeaderSpellBattling() && f.getLeaderAttachment().counts(609) >= 3) {
                            v.hit += 1;
                        }

                        break;
                    case 610: {
                        int amount = f.getLeaderAttachment().counts(610);
                        for (Effect e : gi.getDelayEffects()) {
                            if (e.source.isNo(618) && e.source.getController() == ep) {
                                switch (e.index) {
                                    case 11:
                                        amount += 1;
                                        break;
                                    case 12:
                                        amount += 2;
                                        break;
                                    case 13:
                                        amount += 3;
                                        break;
                                }
                            }
                        }
                        if (isLeaderTargetable(c, ep, ep) && amount >= 3) {
                            v.evasion += 1;
                        }
                        break;
                    }
                    case 611:
                        if (isSpellTargetable(c, ep, ep) && f.isLeaderSpellBattling() && f.getLeaderAttachment().counts(611) >= 3) {
                            v.atk += 1;
                            v.icp += 1;
                        }

                        break;
                    case 1018:
                        if (isSpellTargetable(c, ep, ep) && f.isLeaderSpellBattling() && f.getActivated().size() >= 2) {
                            v.atk += 1;
                            v.icp += 1;
                            v.hit += 1;
                            if (f.getActivated().size() >= 4) {
                                v.homing = true;
                                v.penetration = true;
                            }

                        }
                        break;
                    case 1517:
                        if ((battleValues.get(rev(ep)).penetration || battleValues.get(rev(ep)).homing) && isSpellTargetable(c, rev(ep), ep)) {
                            battleValues.get(rev(ep)).atk -= 1;
                        }

                        break;
                    case 9045:
                        if (v.shiki && isSpellTargetable(c, ep, ep)) {
                            v.atk += 1;
                            v.icp += 1;
                            v.hit += 1;
                        }

                        break;
                    case 9063:
                        if (isSpellTargetable(c, ep, ep) && of.getHand().size() <= 1) {
                            v.atk += 1;
                            v.icp += 1;
                            v.hit += 1;
                        }

                        if (isSpellTargetable(c, rev(ep), ep) && f.getHand().size() <= 1) {
                            battleValues.get(rev(ep)).atk -= 1;
                            battleValues.get(rev(ep)).icp -= 1;
                            battleValues.get(rev(ep)).hit -= 1;
                        }

                        break;
                    case 2914:
                        v.disableBasicAbilities();
                        ov.disableBasicAbilities();
                        break;
                    case 2614: {
                        if (f.isLeaderSpellBattling()) {
                            int amount = f.getHand().size() - of.getHand().size();
                            if (amount < 0) {
                                amount = 0;
                            }

                            if (amount > 3) {
                                amount = 3;
                            }

                            v.atk += amount;
                            v.hit += amount;
                        }
                    }
                    break;
                    case 2413:
                        if (v.fast <= 0) {
                            v.hit -= 1;
                        }
                        break;
                    case 2313:
                        if (!(f.isLeaderContainsAttribute(FTool.getLocale(168)) || f.isLeaderContainsAttribute(FTool.getLocale(237)))) {
                            if (v.faith <= 0) {
                                v.atk -= 1;
                                v.icp -= 1;
                                v.hit -= 1;
                            }
                        }
                        break;
                    case 817:
                        if (f.isLeaderContainsAttribute(FTool.getLocale(169)) && f.getBattleCard().getAttachedOrNullObject().isNo(816)) {
                            int cnt = f.countSupport(816);
                            v.atk += cnt;
                            v.icp += cnt;
                            v.hit += cnt;
                        }
                        break;
                    case 1114:
                        if (f.getBattleCard().isAttached()) {
                            v.atk += 1;
                            v.hit += 1;
                        }
                        break;
                    case 1514:
                        if (v.shiki) {
                            v.atk += 1;
                            v.icp += 1;
                            v.hit += 1;
                        } else {
                            v.ability_disabled[Ability.Pierce.ordinal()] = true;
                            v.ability_disabled[Ability.Homing.ordinal()] = true;
                        }
                        break;
                    case 1613:
                        if (v.shiki) {
                            v.fast += 2;
                        }
                        break;
                    case 2014:
                        v.atk += f.getDiscardPile().size() / 6;
                        v.hit += f.getDiscardPile().size() / 6;
                        break;
                    case 2214:
                        if (getBattleCardLevelRequirement(of, f) <= 1) {
                            ov.atk -= 1;
                            ov.icp -= 1;
                        }
                        break;
                    case 600:
                        if (f.isLeaderSpellBattling()) {
                            v.atk += f.countDoll() / 2;
                            v.icp += f.countDoll() / 2;
                        }
                        break;
                    case 2300:
                        if (f.getBattleCard().getInfo().getSpellPointRequirement() <= 1 && isSpellTargetable(c, ep, ep)) {
                            v.atk -= 1;
                        }
                        if (of.getBattleCard().getInfo().getSpellPointRequirement() <= 1 && isSpellTargetable(c, rev(ep), ep)) {
                            ov.atk -= 1;
                        }
                        break;

                }

            }
        }
    }

    private void applyOthers_DelayEffects() {
    }

    private void applyOthers_Effect() {
        //Speical handling of No.2012
        int count_2012 = 0;

        for (Effect e : gi.getEffects()) {
            if (e.index == -1) {
                applyManualModification((ManualModification) e);
                continue;
            }

            EPlayer ep = e.source.getController();
            BattleValues v = battleValues.get(ep);
            BattleValues ov = battleValues.get(rev(ep));

            Card c = e.source;
            if (gi.isAbilityDisabled(c)) {
                continue;
            }
            PlayField f = gi.getField(ep);
            PlayField of = gi.getField(rev(ep));

            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (e.index != mech.effect_index) {
                    continue;
                }

                if (mech.timing == Timing.bna) {
                    if (mech.isLegal(gi, battleValues, ep, this, false)) {
                        mech.doModifications(gi.isAttackPlayer(ep), e.index, battleValues, ep);
                    } else {
                        if (!AutoMech.Limit.isAdditional(mech.limits)) {
                            error(e);
                        }

                    }
                }
            }

            switch (e.source.getCardNo()) {
                case 3712:
                    if (isSpellTargetable(e.type, rev(ep), ep)) {
                        ov.atk -= f.getDiscardPile().counts(e.source.getCardNo()) + 1;
                        if (f.getLeader().isNo(3700)) {
                            ov.hit -= f.getDiscardPile().counts(e.source.getCardNo()) + 1;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 3614:
                    v.atk -= 1;
                    v.icp -= 1;
                    v.hit -= 1;
                    break;
                case 3505:
                    if (isSpellTargetable(e.type, ep, ep)) {
                        if (e.index == 1 && ov.evasion >= 6) {
                            v.icp += 1;
                            v.hit += 4;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 217:
                    if (e.index == 2 && isSpellTargetable(e.source, ep, ep) && v.penetration) {
                        v.atk += 1;
                    }
                    break;
                case 3116:
                    if (!isLeaderTargetable(e.source, e.source.getController(), e.source.getController())) {
                        error(e);
                    }
                    break;
                case 2916:
                    if (isLeaderTargetable(c, ep, ep) && isLeaderTargetable(c, rev(ep), ep)) {
                        v.evasion -= f.getLeaderLevel();
                        ov.evasion -= f.getLeaderLevel();
                    } else {
                        error(e);
                    }
                    break;
                case 2518:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        if (of.getBattleCard().isAttached()) {
                            if (of.getBattleCard().getAttachedOrNullObject().getInfo().getRuleText().contains(FTool.getLocale(230)) || gi.getScene().isNo(2513)) {
                                ov.disableBasicAbilities();
                            }
                        }
                    } else {
                        error(e);
                    }
                case 2118:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        if (ov.atk > 3) {
                            ov.atk = 3;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 3112:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.atk += 1;
                        v.hit += 1;
                        if (of.getBattleCard().isNull()) {
                            v.atk += 1;
                            v.hit += 1;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 48:
                    if (isNormal(ep) && isSpellTargetable(c, ep, ep)) {
                        v.hit += getBattleCardLevelRequirement(f, of);
                    } else {
                        error(e);
                    }
                    break;
                case 9093:
                    if (e.index == 1) {
                        if (isSpellTargetable(c, rev(ep), ep)) {
                            ov.disableBasicAbilities();
                        } else {
                            error(e);
                        }
                    }
                case 1721:
                    if (f.countIntrusment() >= 3 && isSpellTargetable(c, ep, ep)) {
                        v.atk += 1;
                        v.icp += 1;
                    }
                    break;
                case 2815:
                    if (isSpellTargetable(c, rev(ep), ep) && ov.hasBasicAblitiy()) {
                        int lv = getBattleCardLevelRequirement(of, f);
                        ov.atk -= lv;
                        ov.icp -= lv;
                        ov.hit -= lv;
                    }
                    break;
                case 2416:
                    if (isLeaderTargetable(c, ep, ep) && f.getBattleCard().isAttached()) {
                        v.evasion += 1;
                    }
                    break;
                case 1518:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.atk += f.getCharacterLevel(16);
                    } else {
                        error(e);
                    }
                    break;
                case 2115:
                    if (isSpellTargetable(c, rev(ep), ep) && ov.atk >= 5) {
                        ov.atk -= 3;
                        ov.icp -= 3;
                        ov.hit -= 3;
                    } else {
                        error(e);
                    }
                    break;
                case 2111:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        if (e.index > 0) {
                            ov.ability_disabled[e.index - 1] = true;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 1805:
                    v.atk += 2;
                    v.icp += 1;
                    v.hit += 2;
                    break;
                case 9027:
                    if (v.shiki) {
                        v.atk += 2;
                        v.hit += 4;
                        v.fast += 3;
                    }
                    break;
                case 9075:
                    ov.hit -= 1;
                    break;
                case 2608:
                    if (isSpellTargetable(c, rev(ep), ep) && f.getCharacterLevel(26) == 4) {
                        ov.ability_disabled[Ability.Pierce.ordinal()] = true;
                    }
                    break;
                case 2412:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        if (isSpread(rev(ep))) {
                            ov.disableBasicAbilities();
                        }

                        if (isConcentrate(rev(ep))) {
                            ov.atk -= 2;
                        }

                        if (isNormal(rev(ep))) {
                            ov.hit -= 2;
                        }

                    } else {
                        error(e);
                    }

                    break;
                case 2212:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        if (ov.faith <= 0) {
                            ov.atk -= 2;
                            ov.icp -= 2;
                        } else {
                            ov.atk -= 1;
                            ov.icp -= 1;
                        }
                    }

                    break;
                case 21:
                    if (isLeaderTargetable(c, ep, ep)) {
                        if (isNormal(rev(ep)) || ov.homing) {
                            v.evasion += 4;
                        }

                    } else {
                        error(e);
                    }

                    break;
                case 29:
                    if (isSpellTargetable(c, ep, ep)) {
                        if (getBattleCardLevelRequirement(f, of) <= 1) {
                            v.hit += 2;
                        }

                    } else {
                        error(e);
                    }

                    break;
                case 30:
                    if (isLeaderTargetable(c, ep, ep)) {
                        if (ov.penetration) {
                            v.evasion += 4;
                        }

                    } else {
                        error(e);
                    }

                    break;
                case 419:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        int cnt = 0;
                        cnt += f.countCharSupport(f.getLeader().getInfo().getCharId(), false);
                        cnt += of.countCharSupport(f.getLeader().getInfo().getCharId(), false);
                        ov.atk -= cnt;
                    } else {
                        error(e);
                    }

                    break;
                case 716:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        for (int i = 0; i
                                < ov.ability_disabled.length; ++i) {
                            ov.ability_disabled[i] = true;
                        }
                    } else {
                        error(e);
                    }
                    break;
                case 719:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        ov.spell_targetability.shroudBoth();
                    } else {
                        // Don't error() here because the first ability will show up error message.
                    }
                    break;
                case 918:
                    if (isSpellTargetable(c, ep, ep) && isSpellTargetable(c, rev(ep), ep)) {
                        for (int i = 0; i
                                < v.ability_disabled.length; ++i) {
                            v.ability_disabled[i] = true;
                            ov.ability_disabled[i] = true;
                        }

                    } else {
                        error(e);
                    }

                    break;
                case 1318:
                    if (!gi.getScene().isNull() && gi.getScene().getOwner() == ep) {
                        ov.atk -= 3;
                        ov.icp -= 3;
                    }
                    break;
                case 1418:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        int delta = ov.hit - of.getBattleCard().getInfo().getHitValue();
                        if (delta > 0) {
                            ov.hit -= 2 * delta;
                        }

                    } else {
                        error(e);
                    }
                    break;
                case 1513:
                    if (v.shiki && isSpellTargetable(c, rev(ep), ep) && getBattleCardLevelRequirement(of, f) <= 1) {
                        ov.atk -= 4;
                        ov.icp -= 4;
                        ov.hit -= 4;
                    }
                    break;
                case 1615:
                    if (isSpellTargetable(c, ep, ep) && v.fast > 0) {
                        v.atk += v.fast / 2 > 3 ? 3 : v.fast / 2;
                        v.hit += v.fast / 2 > 3 ? 3 : v.fast / 2;
                    } else {
                        error(e);
                    }

                    break;
                case 2012:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.atk += 1 + f.getDiscardPile().counts(2012);
                        ++count_2012;
                    } else {
                        error(e);
                    }

                    break;
                case 9055:
                    if (isSpellTargetable(c, rev(ep), ep)) {
                        if (f.getLeader().isNo(700)) {
                            ov.disableBasicAbilities();
                        }

                    } else {
                        error(e);
                    }

                    break;
                case 607:
                    if (isSpellTargetable(c, ep, ep) && f.getCharacterLevel(6) == 4) {
                        v.hit += f.countDoll();
                    } else {
                        error(e);
                    }

                    break;
                case 3503:
                    if (e.index == 1) {
                        if (of.getBattleCard().isAttached()) {
                            gi.getPlayer(EPlayer.ICH).replenish(1);
                        }
                    }
                    break;
                case 1408:
                    if (e.index == 1) {
                        if (isSpellTargetable(c, rev(ep), ep) && of.getBattleCard().isAttached()) {
                            ov.hit -= 2;
                        } else {
                            error(e);
                        }

                    }
                    break;
                case 1709:
                    if (isSpellTargetable(c, ep, ep) && f.getCharacterLevel(17) == 4) {
                        v.hit += f.countIntrusment();
                    } else {
                        error(e);
                    }

                    break;
                case 1808:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.atk += f.getDiscardPile().counts(1810);
                    } else {
                        error(e);
                    }

                    break;
                case 1908:
                    if (isSpellTargetable(c, ep, ep) && of.getHand().size() <= 3) {
                        v.atk += 1;
                        v.icp += 1;
                        v.hit += 1;
                    } else {
                        error(e);
                    }

                    break;
                case 2009:
                    if (isSpellTargetable(c, ep, ep)) {
                        v.atk += f.getDiscardPile().counts(2009);
                        v.hit += f.getDiscardPile().counts(2009);
                    } else {
                        error(e);
                    }
                    break;
                case 1300:
                    if (isSpellTargetable(c, ep, ep) && hasBasicAbility(v)) {
                        v.hit += 1;
                    } else {
                        error(e);
                    }
                    break;
                case 1117:
                    if (isLeaderTargetable(c, ep, ep)) {
                        int delta = 6 - gi.getPlayer(ep).getHP();
                        v.evasion += delta > 0 ? delta : 0;
                        v.border = 0;
                    } else {
                        error(e);
                    }

                    break;
                case 1614:
                    if (isSpellTargetable(c, ep, ep) && v.shiki) {
                        v.atk += 1;
                        v.hit += 1;
                    } else {
                        error(e);
                    }

                    break;
                case 1812:
                    if (isSpellTargetable(c, ep, ep) && f.isLeaderSpellBattling()) {
                        v.atk += 1;
                        v.hit += 1;
                    } else {
                        error(e);
                    }

                    break;
                case 2100:
                case 2300:
                    if (isSpellTargetable(c, ep, ep) && v.faith > 0) {
                        v.hit += 1;
                    } else {
                        error(e);
                    }

                    break;
                case 2500:
                    if (of.getBattleCard().isAttached()) {
                        if (isSpellTargetable(c, rev(ep), ep) && of.getBattleCard().getAttachedOrNullObject().getInfo().getRuleText().contains(FTool.getLocale(230))) {
                            ov.atk -= 1;
                            ov.icp -= 1;
                        }

                    }
                    break;
            }

            if (count_2012 == 2) {
                v.atk -= 1;
            }

            if (count_2012 == 3) {
                v.atk -= 2;
            }

        }
    }

    private void applyManualModification(ManualModification e) {
        try {
            for (EPlayer ep : EPlayer.values()) {
                battleValues.get(ep).atk += e.m_atk.get(ep);
                battleValues.get(ep).icp += e.m_icp.get(ep);
                battleValues.get(ep).hit += e.m_hit.get(ep);
                battleValues.get(ep).evasion += e.m_evasion.get(ep);
                battleValues.get(ep).faith += e.m_faith.get(ep);
            }
        } catch (NullPointerException ex) {
            return;
        }
    }

    private void applyLates() {
        for (EPlayer ep : EPlayer.values()) {
            PlayField f = gi.getField(ep);
            PlayField of = gi.getField(rev(ep));
            BattleValues v = battleValues.get(ep);
            BattleValues ov = battleValues.get(rev(ep));

            switch (f.getBattleCard().getCardNo()) {
                case 2309:
                    if (f.getCharacterLevel(21) == 1) {
                        if (of.getBattleCard().getInfo().getAttackValue() >= 5 && isSpellTargetable(f.getBattleCard(), rev(ep), ep)) {
                            ov.atk -= 2;
                            ov.icp -= 2;
                            ov.hit -= 2;
                        }

                    }
                    if (f.getCharacterLevel(22) == 1) {
                        if (getBattleCardLevelRequirement(of, f) <= 1 && isSpellTargetable(f.getBattleCard(), rev(ep), ep)) {
                            ov.atk -= 2;
                            ov.icp -= 2;
                            ov.hit -= 2;
                        }

                    }
                    break;
            }
        }

        /*for (Effect e : gi.getEffects()) {
         if (e.index == -1) {
         continue;
         }
        
         EPlayer ep = e.source.getController();
         BattleValues v = bv.get(ep);
         }*/

        // "Does not change" abilities
        for (EPlayer ep : EPlayer.values()) {
            PlayField f = gi.getField(ep);

            CardSet<Card> cards = new CardSet<Card>();
            cards.addAll(f.getLeaderAttachment());
            cards.add(gi.getScene());
            cards.add(f.getBattleCard());
            cards.add(f.getLeader());
            try {
                cards.add(f.getBattleCard().getAttached());
            } catch (SpellCard.NotAttachedException e) {
            }

            for (Card c : cards) {
                for (AutoMech mech : c.getInfo().getAutoMechs()) {
                    for (AutoMech.Modification m : mech.modifications) {
                        if (m.type == AutoMech.Modification_Type.unc && mech.timing == Timing.bns) {
                            if (mech.isLegal(gi, battleValues, ep, this, false)) {
                                EPlayer tp = mech.getTargetPlayer(ep);
                                battleValues.get(tp).atk = gi.getField(tp).getBattleCard().getInfo().getAttackValue();
                                battleValues.get(tp).icp = gi.getField(tp).getBattleCard().getInfo().getInterceptValue();
                                battleValues.get(tp).hit = gi.getField(tp).getBattleCard().getInfo().getHitValue();
                            }
                        }
                    }
                }

                switch (c.getCardNo()) {
                    case 3317:
                        battleValues.get(rev(ep)).evasion = gi.getField(rev(ep)).getLeader().getInfo().getEvasionValue();
                        break;
                }
            }
        }

        for (Effect e : gi.getEffects()) {
            for (AutoMech mech : e.source.getInfo().getAutoMechs()) {
                for (AutoMech.Modification m : mech.modifications) {
                    if (m.type == AutoMech.Modification_Type.unc && mech.effect_index == e.index) {
                        EPlayer ep = e.source.getController();
                        if (mech.isLegal(gi, battleValues, ep, this, false)) {
                            EPlayer tp = mech.getTargetPlayer(ep);
                            BattleValues v = battleValues.get(tp);
                            PlayField f = gi.getField(tp);
                            v.atk = f.getBattleCard().getInfo().getAttackValue();
                            v.icp = f.getBattleCard().getInfo().getInterceptValue();
                            v.hit = f.getBattleCard().getInfo().getHitValue();
                        }
                    }
                }
            }
        }

    }

    void execBasicAbility() {
        // Barrier and pierce
        for (EPlayer ep : EPlayer.values()) {
            BattleValues v = battleValues.get(ep);
            PlayField f = gi.getField(ep);

            if (v.protection < 0) {
                v.protection = 0;
            }
            if (!battleValues.get(rev(ep)).penetration || battleValues.get(rev(ep)).ability_disabled[Ability.Pierce.ordinal()]) {
                if (!v.ability_disabled[Ability.Protection.ordinal()]) {
                    battleValues.get(rev(ep)).atk -= v.protection;
                    battleValues.get(rev(ep)).icp -= v.protection;
                }
            }

            // Move and homing
            if ((!battleValues.get(rev(ep)).homing || battleValues.get(rev(ep)).ability_disabled[Ability.Homing.ordinal()]) && !f.getBattleCard().isNull()) {
                if (!v.ability_disabled[Ability.Move.ordinal()]) {
                    if (isSpread(rev(ep))) {
                        v.evasion += v.slow;
                    }

                    if (isConcentrate(rev(ep))) {
                        v.evasion += v.fast;
                    }

                }
            }

            if (f.getBattleCard().isNull()) {
                v.atk = 0;
                v.icp = 0;
                v.hit = -9;
            }

        }
    }

    private EPlayer rev(EPlayer ep) {
        if (ep == EPlayer.ICH) {
            return EPlayer.OPP;
        } else {
            return EPlayer.ICH;
        }

    }

    private boolean isConcentrate(EPlayer ep) {
        return ((battleValues.get(ep).typechange == ChangeType.NoChange && gi.getField(ep).getBattleCard().getInfo().getBulletType() == EBulletType.CONCENTRATION)
                || battleValues.get(ep).typechange == ChangeType.Conentrate);
    }

    private boolean isSpread(EPlayer ep) {
        return ((battleValues.get(ep).typechange == ChangeType.NoChange && gi.getField(ep).getBattleCard().getInfo().getBulletType() == EBulletType.SPREAD)
                || battleValues.get(ep).typechange == ChangeType.Spread);
    }

    private boolean isNormal(EPlayer ep) {
        return ((battleValues.get(ep).typechange == ChangeType.NoChange && gi.getField(ep).getBattleCard().getInfo().getBulletType() == EBulletType.NORMAL)
                || battleValues.get(ep).typechange == ChangeType.Normal);
    }

    public boolean isSpellTargetable(Card c, EPlayer targetPlayer, EPlayer actPlayer) {
        return isSpellTargetable(new EffectType(c), targetPlayer, actPlayer);
    }

    public boolean isSpellTargetable(EffectType type, EPlayer targetPlayer, EPlayer actPlayer) {
        SpellCard bc = gi.getField(targetPlayer).getBattleCard();
        if (bc.isNull()) {
            return false;
        }
        if (bv(targetPlayer).isDestinyManip && checkingConstant && targetPlayer != actPlayer) {
            return false;
        }
        return bv(targetPlayer).spell_targetability.isTargetable(targetPlayer == actPlayer, type);
    }

    public boolean isLeaderTargetable(Card c, EPlayer targetPlayer, EPlayer actPlayer) {
        return isLeaderTargetable(new EffectType(c), targetPlayer, actPlayer);
    }

    public boolean isLeaderTargetable(EffectType type, EPlayer targetPlayer, EPlayer actPlayer) {
        return bv(targetPlayer).leader_targetability.isTargetable(targetPlayer == actPlayer, type);
    }

    public EnumMap<EPlayer, BattleValues> getBattleValues() {
        return battleValues;
    }

    private BattleValues bv(EPlayer player) {
        return battleValues.get(player);
    }

    public enum Ability {

        Move,
        Protection,
        Pierce,
        Homing,
        Faith
    }

    private void error(Effect e) {
        gi.getIllegalEffects().add(e);
        parent.illegalUse(true);
    }

    public void handleBattleEnd() {
        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.add(gi.getScene());
        cards.add(f.getLeader());
        if (!gi.isAbilityDisabled(f.getBattleCard())) {
            cards.add(f.getBattleCard());
        }
        cards.add(f.getBattleCard().getAttachedOrNullObject());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == AutoMech.Timing.b8) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, this, false)) {
                        mech.doActions(gi, -1, parent, c);
                    }
                }

                switch (c.getCardNo()) {
                    case 9083:
                        if (f.getBattleCard().isNull() && of.getBattleCard().isNull()) {
                            if (gi.isDamageDealtable(EPlayer.ICH)) {
                                parent.adjustHP(-1, true, true, " - " + c.getName());
                            }
                        }
                        break;
                }
            }
        }

        for (Effect e : gi.getDelayEffects()) {
            for (AutoMech mech : e.source.getInfo().getAutoMechs()) {
                if (mech.timing == AutoMech.Timing.b9 || mech.timing == AutoMech.Timing.a1) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, this, false)) {
                        if (e.index == mech.effect_index) {
                            mech.doActions(gi, -1, parent, e.source);
                        }
                    }
                }
            }
        }
    }

    private int getBattleCardLevelRequirement(PlayField f, PlayField of) {
        int ret = f.getBattleCard().getInfo().getLevelRequirement();
        if (f.getLeaderAttachment().hasCard(2218) && of.getLeader().getInfo().getDesignations().contains(FTool.getLocale(237))) {
            if (ret > 0) {
                ret -= 1;
            }
        }
        if (f.getBattleCard().getAttachedOrNullObject().isNo(9100)) {
            ret = 1;
        }
        return ret;
    }

}
