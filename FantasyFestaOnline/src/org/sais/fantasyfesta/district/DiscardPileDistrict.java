/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class DiscardPileDistrict extends CardSetDistrict {

    @Override
    public ERegion getRegion() {
        return ERegion.DISCARD_PILE;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        set.add(actCard);
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        switch (card.getOwner()) {
            case ICH:
                int localeIndex;
                switch (to) {
                    case HAND:
                        localeIndex = 266;
                        break;
                    case LIBRARY_TOP:
                        localeIndex = 268;
                        break;
                    case LIBRARY_BOTTOM:
                        localeIndex = 269;
                        break;
                    case ACTIVATED:
                        localeIndex = 270;
                        break;
                    case EVENT:
                        localeIndex = 277;
                        break;
                    case LEADER_ATTACHMENTS:
                        localeIndex = 278;
                        break;
                    case RESERVED:
                        localeIndex = 271;
                        break;
                    case SCENE:
                        localeIndex = 272;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }

                GameInformation gi = core.getGameInformation();

                if (!simple && !core.isWatcher()) {
                    core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                            card.getName(), FTool.getLocale(265), gi.getPlayer(newController).getName()) + extraMessage);
                }
                break;
            case OPP:
                switch (to) {
                    case EVENT:
                        if (active) {
                            core.getMainUI().playMessage(FTool.parseLocale(315, core.getGameInformation().myName(), card.getName()
                                    , core.getGameInformation().getPlayer(EPlayer.OPP).getName()));
                        }
                        break;
                    default:
                        if (active) {
                            core.send("$RECOLLECT:" + getRegion() + " " + set.indexOf(card));
                            core.getMainUI().playMessage(FTool.parseLocale(292, core.getGameInformation().myName(), FTool.getLocale(265), card.getName()));
                        }
                        break;
                }
                break;
        }
    }
}
