/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.enums.EBulletType;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * Battle values. Handled by host player.
 *
 * @author Romulus
 */
public class BattleValues {

    private GameInformation gi;
    private EPlayer playerIndex;
    public int atk;
    public int icp;
    public int hit;
    public int evasion;
    public int slow;
    public int fast;
    public int protection;
    public int faith;
    public boolean penetration;
    public boolean homing;
    public boolean[] ability_disabled = new boolean[5];
    public ChangeType typechange = ChangeType.NoChange;
    public boolean shiki = false;
    public Targetability spell_targetability = new Targetability();
    public Targetability leader_targetability = new Targetability();
    public boolean isDestinyManip = false;
    public int border;
    public boolean executeBorder;

    public BattleValues(GameInformation gi, EPlayer playerIndex) {
        if (gi == null) {
            return;
        }
        this.gi = gi;
        this.playerIndex = playerIndex;
        border = gi.getField(playerIndex).getLeader().getInfo().getBorderValue();

        for (int i = 0; i < 4; ++i) {
            ability_disabled[i] = false;
        }
    }

    public GameInformation getGameInformation() {
        return gi;
    }

    public String getAttackValue() {
        return String.valueOf(atk);
    }

    public String getInterceptValue() {
        return String.valueOf(icp);
    }

    public String getEvasion() {
        return String.valueOf(evasion);
    }

    public String getHit() {
        return String.valueOf(hit);
    }

    public String getFaith() {
        return String.valueOf(faith);
    }

    public int getBorderValue() {
        return border;
    }

    public void disableBasicAbilities() {
        for (int i = 0; i < ability_disabled.length; ++i) {
            ability_disabled[i] = true;
        }
    }

    public boolean hasBasicAblitiy() {
        return fast > 0 || slow > 0 || homing || penetration || protection > 0 || faith > 0;
    }

    public boolean isConcentrate() {
        return typechange == ChangeType.Conentrate
                || (typechange == ChangeType.NoChange && gi.getField(playerIndex).getBattleCard().getInfo().getBulletType() == EBulletType.CONCENTRATION);

    }

    public boolean isSpread() {
        return typechange == ChangeType.Spread
                || (typechange == ChangeType.NoChange && gi.getField(playerIndex).getBattleCard().getInfo().getBulletType() == EBulletType.SPREAD);
    }

    public boolean isNormal() {
        return typechange == ChangeType.Normal
                || (typechange == ChangeType.NoChange && gi.getField(playerIndex).getBattleCard().getInfo().getBulletType() == EBulletType.NORMAL);
    }

    public enum ChangeType {

        NoChange,
        Conentrate,
        Spread,
        Normal
    }
}
