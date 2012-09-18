/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.enums.ECardType;

/**
 *
 * @author Romulus
 */
public class EffectType {

    private static final int TYPE_EVENT = 0;
    private static final int TYPE_ABILITY = 1;
    private static final int TYPE_MANUAL = 2;
    private int type;

    /**
     *
     * @param info null if is a manual modification
     */
    public EffectType(CardInfo info) {
        if (info.isNull()) {
            this.type = TYPE_MANUAL;
        } else {
            this.type = info.isCardType(ECardType.EVENT) ? TYPE_EVENT : TYPE_ABILITY;
        }
    }

    public EffectType(Card c) {
        this(c.getInfo());
    }

    public boolean isEvent() {
        return type == TYPE_EVENT;
    }

    public boolean isAbility() {
        return type == TYPE_ABILITY;
    }
}
