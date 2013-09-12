package org.sais.fantasyfesta.core;

import java.util.EnumMap;
import java.util.HashSet;
import org.sais.fantasyfesta.autos.ActivationBeginManager;
import org.sais.fantasyfesta.autos.Effect;
import org.sais.fantasyfesta.autos.EffectQueue;
import org.sais.fantasyfesta.autos.EffectType;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.district.SceneDistrict;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class GameInformation {

    private GameCore core;
    private EnumMap<EPlayer, PlayField> fields = new EnumMap<EPlayer, PlayField>(EPlayer.class);
    private EnumMap<EPlayer, Player> players = new EnumMap<EPlayer, Player>(EPlayer.class);
    private SceneDistrict sceneDistrict = new SceneDistrict();
    private EffectQueue effects = new EffectQueue();
    private EffectQueue delayEffects = new EffectQueue();
    private HashSet<Effect> illegalEffects = new HashSet<Effect>();
    private int turn = 1;     // Current Turn
    private EPlayer attackPlayer = EPlayer.ICH;
    private EPhase phase = EPhase.REPLENISHING;

    public GameInformation(GameCore core) {
        for (EPlayer ep : EPlayer.values()) {
            players.put(ep, new Player("", EPlayer.ICH));
        }
        this.core = core;
        
        for (EPlayer ep : EPlayer.values()) {
            fields.put(ep, new PlayField(this));
            players.get(ep).setHP(0);
            players.get(ep).setSP(0);
        }

        if (!core.isWatcher()) {
            fields.get(EPlayer.ICH).clear(players.get(EPlayer.ICH), core);
            players.get(EPlayer.ICH).setHP(fields.get(EPlayer.ICH).getLeader().getInfo().getHitPoints());
        }

    }

    public PlayField getField(EPlayer player) {
        return fields.get(player);
    }

    public Player getPlayer(EPlayer playerIndex) {
        return players.get(playerIndex);
    }

    public EffectQueue getDelayEffects() {
        return delayEffects;
    }

    public void setDelayEffects(EffectQueue delay_events) {
        this.delayEffects = delay_events;
    }

    public EffectQueue getEffects() {
        return effects;
    }

    public void setEffects(EffectQueue events) {
        this.effects = events;
    }

    public HashSet<Effect> getIllegalEffects() {
        return illegalEffects;
    }

    public void setIllegalEffects(HashSet<Effect> illegal_events) {
        this.illegalEffects = illegal_events;
    }

    public boolean isIchAttackPlayer() {
        return isAttackPlayer(EPlayer.ICH);
    }

    public boolean isAttackPlayer(EPlayer targetPlayer) {
        return attackPlayer == targetPlayer;
    }

    public EPlayer getAttackPlayer() {
        return attackPlayer;
    }
    
    public void setAttackPlayer(EPlayer attackPlayer) {
        this.attackPlayer = attackPlayer;
    }

    public int getTurn() {
        return turn;
    }

    public EPhase getPhase() {
        return phase;
    }

    public void setPhase(EPhase phase) {
        this.phase = phase;
        if (phase == EPhase.ACTIVATION) {
            ActivationBeginManager.exec(core, this);
        }
        core.getMainUI().showTurn(this);
    }

    public SupportCard getScene() {
        return sceneDistrict.getCard();
    }

    public void setScene(SupportCard scene) {
        sceneDistrict.set(scene);
    }

    public void clear() {
        for (EPlayer ep : EPlayer.values()) {
            fields.get(ep).clear(players.get(ep), core);
            players.get(ep).setHP(fields.get(ep).getLeader().getInfo().getHitPoints());
            players.get(ep).setSP(0);
            sceneDistrict.clear();
        }
        
        clearEffectQueues();

        turn = 1;
        phase = EPhase.REPLENISHING;
    }
    
    public void clearEffectQueues() {
        effects.clear();
        delayEffects.clear();
        illegalEffects.clear();
    }

    /**
     * Call with actPlayer=ICH.
     *
     * @param targetPlayer
     * @return
     */
    public boolean isHandTargetable(EPlayer targetPlayer) {
        return isHandTargetable(targetPlayer, EPlayer.ICH);
    }

    public boolean isHandTargetable(EPlayer targetPlayer, EPlayer actPlayer) {
        if (fields.get(targetPlayer).getLeaderAttachment().hasCard(22) && targetPlayer != actPlayer) {
            return false;
        }
        if (getScene().isNo(33)) {
            return false;
        }
        if (targetPlayer != actPlayer && getField(targetPlayer).getLeader().isNo(3100)) {
            return false;
        }
        return true;
    }

    public boolean isLibraryTargetable(EPlayer targetPlayer) {
        return isLibraryTargetable(targetPlayer, EPlayer.ICH);
    }

    public boolean isLibraryTargetable(EPlayer targetPlayer, EPlayer actPlayer) {
        if (getScene().isNo(33)) {
            return false;
        }
        if (targetPlayer != actPlayer && getField(targetPlayer).getLeader().isNo(3100)) {
            return false;
        }
        return true;
    }

    public boolean isDiscardPileTargetable(EPlayer targetPlayer) {
        return isDiscardPileTargetable(targetPlayer, EPlayer.ICH);
    }

    public boolean isDiscardPileTargetable(EPlayer targetPlayer, EPlayer actPlayer) {
        if (getScene().isNo(33)) {
            return false;
        }
        if (targetPlayer != actPlayer && getField(targetPlayer).getLeader().isNo(3100)) {
            return false;
        }
        return true;
    }

    public boolean isLeaderTargetable(Card c, EPlayer targetPlayer) {
        return isLeaderTargetable(c, targetPlayer, EPlayer.ICH);
    }

    public boolean isLeaderTargetable(EffectType type, EPlayer targetPlayer) {
        return isLeaderTargetable(type, targetPlayer, EPlayer.ICH);
    }

    public boolean isLeaderTargetable(Card c, EPlayer targetPlayer, EPlayer actPlayer) {
        return isLeaderTargetable(new EffectType(c), targetPlayer, actPlayer);
    }

    public boolean isLeaderTargetable(EffectType type, EPlayer targetPlayer, EPlayer actPlayer) {
        if (targetPlayer != actPlayer && getField(targetPlayer).getLeader().isNo(1000)) {
            return false;
        }
        if (targetPlayer != actPlayer && getField(targetPlayer).getLeaderAttachment().hasCard(2116)) {
            return false;
        }
        if (type.isEvent() && getField(targetPlayer).getLeaderAttachment().hasCard(9024)) {
            return false;
        }
        if (getScene().isNo(3115) && type.isAbility()
            && !getField(targetPlayer).getLeader().getInfo().getDesignations().contains(FTool.getLocale(316))) {
            return false;
        }
        return true;
    }

    public boolean isDamageDealtable(EPlayer targetPlayer) {
        return isDamageDealtable(targetPlayer, EPlayer.ICH);
    }

    public boolean isDamageDealtable(EPlayer targetPlayer, EPlayer actPlayer) {
        if (fields.get(targetPlayer).getLeaderAttachment().hasCard(1313)) {
            return false;
        }
        if (fields.get(targetPlayer).getActivated().hasCard(3603) && actPlayer != targetPlayer) {
            return false;
        }
        return true;
    }

    public boolean isHealable(EPlayer targetPlayer) {
        if ((getScene().isNo(912) && !fields.get(targetPlayer).isLeaderContainsAttribute(FTool.getLocale(171)))
                || getScene().isNo(42) || getScene().isNo(2914)) {
            return false;
        }
        return true;
    }

    public int countGhost(EPlayer targetPlayer) {
        int rtn = 0;

        PlayField f = fields.get(targetPlayer);
        CardSet<SpellCard> spells = new CardSet<SpellCard>();
        spells.addAll(f.getActivated());
        spells.addAll(f.getReserved());
        spells.add(f.getBattleCard());

        for (Card c : spells) {
            SpellCard s = (SpellCard) c;
            if (s.isAttached()) {
                if (getScene().isNo(2513) || s.getAttachedDontThrow().getInfo().getRuleText().contains(FTool.getLocale(230))) {
                    rtn++;
                }
            }
        }

        return rtn;
    }

    public String myName() {
        return players.get(EPlayer.ICH).getName();
    }

    SceneDistrict getSceneDistrict() {
        return sceneDistrict;
    }

    public void nextTurn() {
        clearEffectQueues();
        ++turn;
        attackPlayer = FTool.rev(attackPlayer);
        setPhase(EPhase.REPLENISHING);
    }

    void setTurn(int turn) {
        this.turn = turn;
    }
    
    public PlayField getOppositeField(PlayField me) {
        if (fields.get(EPlayer.ICH) == me) {
            return fields.get(EPlayer.OPP);
        } else {
            return fields.get(EPlayer.ICH);
        }
    }

    public boolean isAbilityDisabled(Card c) {
        if (c instanceof SpellCard && phase == EPhase.BATTLE) {
            if (((SpellCard) c).getAttachedOrNullObject().isNo(3515)) {
                return true;
            }
        }
        return false;
    }

}
