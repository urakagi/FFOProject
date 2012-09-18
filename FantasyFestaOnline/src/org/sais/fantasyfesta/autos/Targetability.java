/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.fantasyfesta.autos;

/**
 *
 * @author Romulus
 */
public class Targetability {

    private boolean self_ability = true;
    private boolean self_event = true;
    private boolean opp_ability = true;
    private boolean opp_event = true;

    public boolean isTargetable(boolean isTargetingSelf, EffectType effectType) {
        if (isTargetingSelf) {
            if (effectType.isEvent()) {
                return self_event;
            } else if (effectType.isAbility()) {
                return self_ability;
            }
        } else {
            if (effectType.isEvent()) {
                return opp_event;
            } else if (effectType.isAbility()) {
                return opp_ability;
            }
        }
        return true;
    }

    public void shroudBoth() {
        self_ability = false;
        self_event = false;
        opp_ability = false;
        opp_event = false;
    }

    public void sealBoth() {
        self_ability = false;
        self_event = false;
    }

    public void hexproofBoth() {
        opp_ability = false;
        opp_event = false;
    }

    public void shroudAbility() {
        self_ability = false;
        opp_ability = false;
    }

    public void hexproofAbility() {
        opp_ability = false;
    }

    public void hexproofEvent() {
        opp_event = false;
    }
    
    public void sealEvent() {
        self_event = false;
    }

    public void sealAbility() {
        self_ability = false;
    }

    public void reset() {
        self_ability = true;
        self_event = true;
        opp_ability = true;
        opp_event = true;
    }

}
