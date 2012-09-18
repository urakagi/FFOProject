/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class RevealDistrict extends CardSetDistrict {

    private ERegion mRegion;

    public void setRegion(ERegion region) {
        this.mRegion = region;
    }

    @Override
    public ERegion getRegion() {
        return mRegion;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        switch (mRegion) {
            case HAND:
                core.send("$MOVEREVEAL:" + mRegion + " " + to + " " + set.indexOf(card));
                core.actionDone();
                break;
            case LIBRARY:
                switch (to) {
                    case DISCARD_PILE:
                        core.send("$MOVEREVEAL:" + mRegion + " " + to + " " + set.indexOf(card));
                        core.actionDone();
                        break;
                    case RESERVED:
                        core.send("$RECOLLECT:" + mRegion + " " + set.indexOf(card));
                        core.getMainUI().playMessage(FTool.parseLocale(292, core.getGameInformation().myName(), FTool.getLocale(264), card.getName()));
                        break;
                }
                break;
        }
    }
}
