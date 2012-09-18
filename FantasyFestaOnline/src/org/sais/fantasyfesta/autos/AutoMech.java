/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import java.io.Serializable;
import org.sais.fantasyfesta.core.GameInformation;
import java.util.ArrayList;
import java.util.EnumMap;
import org.sais.fantasyfesta.card.*;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.enums.ESupportType;
import org.sais.fantasyfesta.tool.FTool;

/**
 * Handle text-based auto mechanism. Not all auto mechanism are implemented here
 * (like Level checking).
 *
 * @author Romulus
 */
public class AutoMech implements Serializable {

    public Timing timing;
    public Turn_Type turn;
    public CardInfo holder;
    public int effect_index = 0;
    public EPlayer target_player = EPlayer.ICH;
    public ArrayList<Modification> modifications = new ArrayList<Modification>();
    public ArrayList<Action> actions = new ArrayList<Action>();
    public ArrayList<Target_Type> targets = new ArrayList<Target_Type>();
    public ArrayList<Limit> limits = new ArrayList<Limit>();

    public AutoMech(CardInfo holder) {
        this.holder = holder;
    }

    public void addEffect(String effect, int amount) {
        if (timing == Timing.bns || timing == Timing.bas || timing == Timing.bna || timing == Timing.baa) {
            modifications.add(new Modification(Modification_Type.valueOf(effect), amount));
        } else {
            actions.add(new Action(Action_Type.valueOf(effect), amount));
        }
    }

    public void doActions(GameInformation gi, int index, IAutosCallback parent, Card actCard) {
        if (index >= 0 && effect_index != index) {
            return;
        }
        for (Action action : actions) {
            switch (action.type) {
                case dec:
                    if (targetsMyself()) {
                        for (int i = 0; i < action.amount; ++i) {
                            parent.deckout(" - " + holder.getName());
                        }
                    } else {
                        parent.send("$DECKOUT:" + action.amount + " " + holder.getName());
                    }
                    break;
                case dra:
                    if (targetsMyself()) {
                        if (gi.isLibraryTargetable(EPlayer.ICH, EPlayer.ICH)) {
                            parent.draw(action.amount, " - " + holder.getName());
                        }
                    } else {
                        if (gi.isLibraryTargetable(EPlayer.OPP, EPlayer.ICH)) {
                            parent.send("$DRAW:" + action.amount + " " + holder.getName());
                        }
                    }
                    break;
                case hp:
                    if (targetsMyself()) {
                        parent.adjustHP(action.amount, true, true, " - " + holder.getName());
                    } else {
                        // Pache's untargetable and Eirin's Super Genuis
                        if (gi.isDamageDealtable(EPlayer.OPP)) {
                            parent.send("$ADJUSTHP:" + action.amount + " " + holder.getName());
                        }
                    }
                    break;
                case sp:
                    if (targetsMyself()) {
                        parent.adjustSP(action.amount, true, " - " + holder.getName());
                    } else {
                        if (action.amount > 0) {
                            parent.send("$ADJUSTSP:" + action.amount + " " + holder.getName());
                        } else {
                            if (gi.getPlayer(EPlayer.OPP).getSP() >= Math.abs(action.amount)) {
                                parent.send("$ADJUSTSP:" + action.amount + " " + holder.getName());
                            } else {
                                parent.send("$ADJUSTSP:" + (-gi.getPlayer(EPlayer.OPP).getSP()) + " " + holder.getName());
                            }
                        }
                    }
                    break;
                case des:
                    if (holder.isCardType(ECardType.SUPPORT) && ((SupportCardInfo) holder).getSupportType() == ESupportType.LEADER) {
                        parent.moveCard((SupportCard) actCard, actCard.getOwner(), EMoveTarget.DISCARD_PILE, true, false, " - " + holder.getName());
                    } else if (holder.isCardType(ECardType.SPELL)) {
                        parent.moveCard((SpellCard) actCard, actCard.getOwner(), EMoveTarget.DISCARD_PILE, true, false, " - " + holder.getName());
                    } else if (holder.isCardType(ECardType.SUPPORT) && ((SupportCardInfo) holder).getSupportType() == ESupportType.SPELL) {
                        SupportCard card = parent.unAttach(((SupportCard) actCard).getAttachingOn(), true);
                        parent.moveSpellAttachment(card, EMoveTarget.DISCARD_PILE, true);
                    }
                    break;
                case rds:
                    if (targetsOpponent()) {
                        if (gi.isHandTargetable(target_player, EPlayer.ICH)) {
                            for (int i = 0; i < action.amount; ++i) {
                                parent.send("$RANDOMDISCARD:" + holder.getName());
                            }
                        }
                    }
                    break;
                case dsc:
                    if (targetsOpponent()) {
                        parent.send("$SELECTDISCARD:" + action.amount + " " + holder.getName());
                    } else if (targetsMyself()) {
                        parent.discardHint(action.amount, holder.getName());
                    }
                    break;
                case res:
                    parent.moveCard((SpellCard) actCard, actCard.getController(), EMoveTarget.RESERVED, true, false, " - " + holder.getName());
                    break;
                case peek:
                    if (targetsMyself()) {
                        if (action.amount <= 0) {
                            parent.peekWholeLibrary(" - " + holder.getName());
                        } else {
                            parent.peekLibrary(action.amount, " - " + holder.getName());
                        }
                    }
                    break;
                case ltl:
                    if (targetsOpponent()) {
                        parent.send("$SHOWMELIBTOPS:" + action.amount + " " + holder.getName());
                    }
                    break;
                case rvl:
                    if (targetsMyself()) {
                        parent.revealLibTops(action.amount, " - " + holder.getName());
                    }
                    break;
                case rvh:
                    if (targetsOpponent()) {
                        if (gi.isHandTargetable(EPlayer.OPP)) {
                            parent.send("$SHOWHAND:" + holder.getName());
                        }
                    }
                    break;
                case rgl:
                    parent.returnDiscardPileToLibrary();
                    parent.shuffle();
                    break;
            }
        }
    }

    public void doModifications(boolean isAttacking, int index, EnumMap<EPlayer, BattleValues> bv, EPlayer actPlayer) {
        if (index >= 0 && effect_index != index) {
            return;
        }

        BattleValues v = bv.get(actPlayer);
        BattleValues ov = bv.get(rev(actPlayer));

        for (Modification m : modifications) {
            switch (m.type) {
                case atk:
                    if (targetsMyself()) {
                        v.atk += m.amount;
                    } else {
                        ov.atk += m.amount;
                    }
                    break;
                case pro:
                    if (targetsMyself()) {
                        v.protection += m.amount;
                    } else {
                        ov.protection += m.amount;
                    }
                    break;
                case fai:
                    if (targetsMyself()) {
                        v.faith += m.amount;
                    } else {
                        ov.faith += m.amount;
                    }
                    break;
                case fas:
                    if (targetsMyself()) {
                        v.fast += m.amount;
                    } else {
                        ov.fast += m.amount;
                    }
                    break;
                case hit:
                    if (targetsMyself()) {
                        v.hit += m.amount;
                    } else {
                        ov.hit += m.amount;
                    }
                    break;
                case icp:
                    if (targetsMyself()) {
                        v.icp += m.amount;
                    } else {
                        ov.icp += m.amount;
                    }
                    break;
                case slo:
                    if (targetsMyself()) {
                        v.slow += m.amount;
                    } else {
                        ov.slow += m.amount;
                    }
                    break;
                case eva:
                    if (targetsMyself()) {
                        v.evasion += m.amount;
                    } else {
                        ov.evasion += m.amount;
                    }
                    break;
                case hom:
                    if (targetsMyself()) {
                        if (m.amount >= 0) {
                            v.homing = true;
                        } else {
                            v.ability_disabled[BattleSystem.Ability.Homing.ordinal()] = true;
                        }
                    } else {
                        if (m.amount >= 0) {
                            ov.homing = true;
                        } else {
                            ov.ability_disabled[BattleSystem.Ability.Homing.ordinal()] = true;
                        }
                    }
                    break;
                case prc:
                    if (targetsMyself()) {
                        if (m.amount >= 0) {
                            v.penetration = true;
                        } else {
                            v.ability_disabled[BattleSystem.Ability.Pierce.ordinal()] = true;
                        }
                    } else {
                        if (m.amount >= 0) {
                            ov.penetration = true;
                        } else {
                            ov.ability_disabled[BattleSystem.Ability.Pierce.ordinal()] = true;
                        }
                    }
                    break;
                case ks:
                    if (targetsMyself()) {
                        v.border += m.amount;
                        if (v.border < 0) {
                            v.border = 0;
                        }
                    } else {
                        ov.border += m.amount;
                        if (ov.border < 0) {
                            ov.border = 0;
                        }
                    }
                    break;
            }
        }
    }

    public boolean isLegal(GameInformation gi, EnumMap<EPlayer, BattleValues> bv, EPlayer actPlayer, BattleSystem sys, boolean isActivated) {
        switch (turn) {
            case at:
                if (!gi.isAttackPlayer(actPlayer)) {
                    return false;
                }
                break;
            case ic:
                if (gi.isAttackPlayer(actPlayer)) {
                    return false;
                }
                break;
            case bo:
                // Always pass
                break;
        }

        for (Target_Type red : targets) {
            switch (red) {
                case d0:
                    if (!gi.isLibraryTargetable(actPlayer, actPlayer)) {
                        return false;
                    }
                    break;
                case d1:
                    if (!gi.isLibraryTargetable(rev(actPlayer), actPlayer)) {
                        return false;
                    }
                    break;
                case g0:
                    break;
                case g1:
                    break;
                case h0:
                    break;
                case h1:
                    if (!gi.isHandTargetable(rev(actPlayer))) {
                        return false;
                    }
                    break;
                case l0:
                    if (sys != null) {
                        if (!sys.isLeaderTargetable(getEffectType(), actPlayer, actPlayer)) {
                            return false;
                        }
                    }
                    if (!gi.isLeaderTargetable(getEffectType(), actPlayer, actPlayer)) {
                        return false;
                    }
                    break;
                case l1:
                    if (sys != null) {
                        if (!sys.isLeaderTargetable(getEffectType(), rev(actPlayer), actPlayer)) {
                            return false;
                        }
                    }
                    if (!gi.isLeaderTargetable(getEffectType(), rev(actPlayer), actPlayer)) {
                        return false;
                    }
                    break;
                case s0:
                    if (sys == null) {
                        break;
                    }
                    if (!sys.isSpellTargetable(getEffectType(), actPlayer, actPlayer)) {
                        return false;
                    }
                    break;
                case s1:
                    if (sys == null) {
                        break;
                    }
                    if (!sys.isSpellTargetable(getEffectType(), rev(actPlayer), actPlayer)) {
                        return false;
                    }
                    break;
            }
        }

        /*
         * Detect if limit is violated. Because there may be several limits, only do return false or break to next limit.
         */
        for (Limit limit : limits) {
            switch (limit.type) {
                case isNor:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(actPlayer).isNormal()) {
                        return false;
                    }
                    break;
                case isSpr:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(actPlayer).isSpread()) {
                        return false;
                    }
                    break;
                case isCon:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(actPlayer).isConcentrate()) {
                        return false;
                    }
                    break;
                case oppCon:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(rev(actPlayer)).isConcentrate()) {
                        return false;
                    }
                    break;
                case oppNor:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(rev(actPlayer)).isNormal()) {
                        return false;
                    }
                    break;
                case oppSpr:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(rev(actPlayer)).isSpread()) {
                        return false;
                    }
                    break;
                case lvEq:
                    if (gi.getField(actPlayer).getCharacterLevel(FTool.safeParseInt(limit.extra1)) != FTool.safeParseInt(limit.extra2)) {
                        return false;
                    }
                    break;
                case lvGr:
                    if (gi.getField(actPlayer).getCharacterLevel(FTool.safeParseInt(limit.extra1)) < FTool.safeParseInt(limit.extra2)) {
                        return false;
                    }
                    break;
                case isLd:
                    if (limit.extra1.equals("attr")) {
                        if (!gi.getField(actPlayer).isLeaderContainsAttribute(limit.extra2)) {
                            return false;
                        }
                    } else if (limit.extra1.equals("!attr")) {
                        if (gi.getField(actPlayer).isLeaderContainsAttribute(limit.extra2)) {
                            return false;
                        }
                    } else if (limit.extra1.equals("eq")) {
                        if (!(gi.getField(actPlayer).getLeader().isNo(FTool.safeParseInt(limit.extra2)))) {
                            return false;
                        }
                    } else if (limit.extra1.equals("!eq")) {
                        if (gi.getField(actPlayer).getLeader().isNo(FTool.safeParseInt(limit.extra2))) {
                            return false;
                        }
                    } else if (limit.extra1.equals("atb")) {
                        if (!(gi.getField(actPlayer).getLeaderAttachment().hasCard(FTool.safeParseInt(limit.extra2)))) {
                            return false;
                        }
                    } else if (limit.extra1.equals("attx")) {
                        boolean attached = false;
                        for (Card c : gi.getField(actPlayer).getLeaderAttachment()) {
                            SupportCardInfo info = (SupportCardInfo) c.getInfo();
                            if (info.getRuleText().contains(limit.extra2)) {
                                attached = true;
                            }
                        }
                        if (!attached) {
                            return false;
                        }
                    }
                    break;
                case oppLd:
                    if (limit.extra1.equals("attr")) {
                        if (!gi.getField(rev(actPlayer)).isLeaderContainsAttribute(limit.extra2)) {
                            return false;
                        }
                    } else if (limit.extra1.equals("!attr")) {
                        if (gi.getField(rev(actPlayer)).isLeaderContainsAttribute(limit.extra2)) {
                            return false;
                        }
                    } else if (limit.extra1.equals("eq")) {
                        if (!(gi.getField(rev(actPlayer)).getLeader().isNo(FTool.safeParseInt(limit.extra2)))) {
                            return false;
                        }
                    } else if (limit.extra1.equals("attx")) {
                        boolean attached = false;
                        for (Card c : gi.getField(rev(actPlayer)).getLeaderAttachment()) {
                            SupportCardInfo info = (SupportCardInfo) c.getInfo();
                            if (info.getRuleText().contains(limit.extra2)) {
                                attached = true;
                            }
                        }
                        if (!attached) {
                            return false;
                        }
                    }
                    break;
                case isSp:
                    if (bv == null) {
                        return false;
                    }
                    if (limit.extra1.equals("ldSp")) {
                        if (!gi.getField(actPlayer).isLeaderSpellBattling()) {
                            return false;
                        }
                    }
                    if (limit.extra1.equals("!ldSp")) {
                        if (gi.getField(actPlayer).isLeaderSpellBattling()) {
                            return false;
                        }
                    }
                    if (limit.extra1.equals("ownb")) {
                        if (!gi.getField(actPlayer).getBattleCard().getInfo().isOwnedBy(FTool.safeParseInt(limit.extra2))) {
                            return false;
                        }
                    }
                    break;
                case oppSp:
                    // Only reseverd
                    break;
                case hdGr:
                    if (gi.getField(actPlayer).getHand().size() < FTool.safeParseInt(limit.extra1)) {
                        return false;
                    }
                    break;
                case ac:
                    if (!isActivated) {
                        return false;
                    }
                    break;
                case sce:
                    if (gi.getScene().getOwner() != actPlayer || gi.getScene().isNull()) {
                        return false;
                    }
                    break;
                case thr: {
                    int threshold = Integer.parseInt(limit.extra1);
                    int amount = gi.getField(actPlayer).countSpell(-1, ERegion.DISCARD_PILE);
                    if (amount < threshold) {
                        return false;
                    }
                }
                break;
                case supthr: {
                    int character = Integer.parseInt(limit.extra1);
                    int threshold = Integer.parseInt(limit.extra2);
                    int amount = 0;
                    for (Card c : (CardSet<Card>) gi.getField(actPlayer).getDiscardPileDistrict().getSet()) {
                        if (c.getInfo().isCardType(ECardType.SUPPORT) && c.getInfo().isOwnedBy(character)) {
                            amount++;
                        }
                    }
                    if (amount < threshold) {
                        return false;
                    }
                }
                break;
                case oppBa:
                    if (bv == null) {
                        return false;
                    }
                    if (!bv.get(rev(actPlayer)).hasBasicAblitiy()) {
                        return false;
                    }
                    break;
                case suGr:
                    if (gi.getField(actPlayer).countSupport(-1) < Integer.parseInt(limit.extra1)) {
                        return false;
                    }
                    break;
                case isAtch:
                    if (!gi.getField(actPlayer).getBattleCard().isAttached()) {
                        return false;
                    }
                    break;
                case oppAtch:
                    if (!gi.getField(rev(actPlayer)).getBattleCard().isAttached()) {
                        return false;
                    }
                    break;
                case naz: {
                    PlayField f = gi.getField(actPlayer);
                    int cardno = f.getBattleCard().getCardNo();
                    int amount = f.countSpell(cardno, ERegion.BATTLE)
                            + f.countSpell(cardno, ERegion.ACTIVATED)
                            + f.countSpell(cardno, ERegion.RESERVED);
                    if (amount < 3) {
                        return false;
                    }
                    break;
                }
            }
        }
        return true;
    }

    private EPlayer rev(EPlayer p) {
        if (p == EPlayer.ICH) {
            return EPlayer.OPP;
        } else {
            return EPlayer.ICH;
        }
    }

    private EffectType getEffectType() {
        return new EffectType(holder);
    }

    private boolean targetsMyself() {
        return target_player == EPlayer.ICH;
    }

    private boolean targetsOpponent() {
        return target_player == EPlayer.OPP;
    }

    public enum Timing {

        f1, // Beginning of Fill
        f9, // End of Fill
        b1, // Beginning of Battle
        b8, // End of Battle and battled with this card
        b9, // End of Battle, event or ability
        a1, // Beginning of Activate
        a9, // End of Activate
        bns, // Battle - Normal Static
        bas, // Battle - Gain ability Static
        bna, // Battle - Normal activated
        baa, // Battle - Gain ability activated
        in, // Instant
        tr, // Trigger
        st, // Static
        ht, // Hit
        oht, // Opponent hit
        dm, // Damages
        ac, // Activate
        ms, // When misses
        oms, // When opponent misses
        ev, // 自分がイベントを使った
        opev, // 相手がイベントを使った
        ab, // 自分が特殊能力を使った
        opab, // 相手が特殊能力を使った
        etb, // Enters the battlefield
    }

    public enum Turn_Type {

        at,
        ic,
        bo,
    }

    public class Modification implements Serializable {

        public Modification_Type type;
        public int amount;

        public Modification(Modification_Type type, int amount) {
            this.type = type;
            this.amount = amount;
        }
    }

    public enum Modification_Type {

        atk, // Attack
        icp, // Intercept
        hit, // Hit
        pro, // Protection
        slo, // Slow move
        fas, // Fast move
        fai, // Faith
        eva, // Evasion
        hom, // Homing
        prc, // Pierce
        unc, // Attack, intercept, hit does not change
        ks, // Modify kesshi
    }

    public class Action implements Serializable {

        Action_Type type;
        public int amount;

        public Action(Action_Type type, int amount) {
            this.type = type;
            this.amount = amount;
        }
    }

    public enum Action_Type {

        hp, // HP change
        sp, // SP change
        dec, // Deckout
        dra, // Draw
        rgl, // Retrun grave to library
        ski, // Shikikami
        des, // Destory this card
        rds, // Random discard
        dsc, // Discard at chosen propmt
        res, // Make this spell standby
        peek, // Show top cardlabels of library
        ltl, // Look opp's top cardlabels of library
        rvl, // Reveal top cardlabels of library
        rvh, // Reveal hand
        nul, // Do nothing
    }

    public enum Target_Type implements Serializable {

        d0, // My deck
        d1, // Opp's deck
        h0, // My hand
        h1, // Opp's hand
        g0, // My grave
        g1, // Opp's grave
        l0, // My leader
        l1, // Opp's leader
        s0, // My spell
        s1, // Opp's spell
    }

    public static class Limit implements Serializable {

        public Limit_Type type;
        public String extra1;
        public String extra2;

        public Limit(Limit_Type type, String extra1, String extra2) {
            this.type = type;
            this.extra1 = extra1;
            this.extra2 = extra2;
        }

        public static boolean isAdditional(ArrayList<Limit> limits) {
            for (Limit l : limits) {
                if (l.type == Limit_Type.add) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum Limit_Type {

        add, // This is an additional effect (Don't show error message)
        isSpr, // Is target spell Spread
        isCon, // ~ Concentrate
        isNor, // ~ Normal
        oppSpr, // Is opp's spell ~
        oppCon,
        oppNor,
        /**
         * Limit level equal to extra2. Extra1 is the limited character's
         * charcard number, extra2 is the amount.
         */
        lvEq,
        /**
         * Limit level greater than extra2. Extra1 is the limited character's
         * charcard number, extra2 is the amount.
         */
        lvGr,
        /**
         * Is my leader something. Extra1 is indicator (attribute or character),
         * extra2 is value.
         */
        isLd,
        /**
         * Is opp's leader something. Extra1 is indicator (attribute or
         * character), extra2 is value.
         */
        oppLd,
        /**
         * Is my spell something. Extra1 is indicator (attribute or character),
         * extra2 is value.
         */
        isSp,
        /**
         * Is opp's spell something. Extra1 is indicator (attribute or
         * character), extra2 is value.
         */
        oppSp,
        /**
         * If my hand' amount is extra1 or more.
         */
        hdGr,
        /**
         * Only activated spells.
         */
        ac,
        /**
         * When own scene is in play
         */
        sce,
        /**
         * If the number spell card in own grave is extra1 or more. Threshold.
         */
        thr,
        /**
         * If the number of specified support card in own grave is extra1 or
         * more. Support Threshold.
         */
        supthr,
        /**
         * If opponent's battle card has basic ability
         */
        oppBa,
        /**
         * If my field has support card amount at least
         */
        suGr,
        /**
         * If my spell is attached
         */
        isAtch,
        /**
         * If opponent's spell is attached
         */
        oppAtch,
        /**
         * If battle spell has extra1 amount in my field (Nazrin concept)
         */
        naz
    }

    public static boolean isBattleTiming(Timing timing) {
        return (timing == Timing.baa || timing == Timing.bna || timing == Timing.bas || timing == Timing.bns);
    }

    public boolean isDelayTiming() {
        return (timing == Timing.f9 || timing == Timing.b1 || timing == Timing.b9 || timing == Timing.a1 || timing == Timing.a9 || timing == Timing.oms || timing == Timing.ms || timing == Timing.st);
    }

    public EPlayer getTargetPlayer(EPlayer ep) {
        return ep == EPlayer.ICH ? target_player : rev(target_player);
    }
}
