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
public class HandDistrict extends CardSetDistrict {

    @Override
    public ERegion getRegion() {
        return ERegion.HAND;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        set.add(actCard);
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        String from = FTool.getLocale(263);
        int localeIndex;
        switch (to) {
            case DISCARD_PILE:
                if (!simple) {
                    FTool.playSound("discard.wav");
                }
                localeIndex = 267;
                break;
            case LIBRARY_TOP:
                if (!simple) {
                    FTool.playSound("discard.wav");
                }
                localeIndex = 268;
                break;
            case LIBRARY_BOTTOM:
                if (!simple) {
                    FTool.playSound("discard.wav");
                }
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

        if (!simple && !core.isWatcher()) {
            GameInformation gi = core.getGameInformation();
            switch (to) {
                case RESERVED:
                    core.getMainUI().noLogPlayMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                            card.getName(), from, gi.getPlayer(newController).getName()) + extraMessage);
                    core.writeAndSendReplay(FTool.parseLocale(308, card.getName()));
                    break;
                case SCENE:
                    core.getMainUI().noLogPlayMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                            card.getName(), from, gi.getPlayer(newController).getName()) + extraMessage);
                    core.writeAndSendReplay(FTool.parseLocale(281, gi.getPlayer(card.getOwner()).getName(), card.getName()));
                    break;
                case EVENT:
                    core.getMainUI().noLogPlayMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                            card.getName(), from, gi.getPlayer(newController).getName()) + extraMessage);
                    core.writeAndSendReplay(FTool.parseLocale(282, gi.getPlayer(card.getController()).getName(), card.getName()));
                    break;
                default:
                    core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                        card.getName(), from, gi.getPlayer(newController).getName()) + extraMessage);
                    break;
            }
        }
    }
}
