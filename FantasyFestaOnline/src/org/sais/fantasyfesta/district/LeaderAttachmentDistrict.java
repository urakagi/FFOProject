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
public class LeaderAttachmentDistrict extends CardSetDistrict {

    @Override
    public ERegion getRegion() {
        return ERegion.LEADER_ATTACHMENTS;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        set.add(actCard);
        core.getMainUI().addLabel(actCard.getLabel(), set.size());
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        int localeIndex;
        switch (to) {
            case HAND:
                localeIndex = 266;
                break;
            case DISCARD_PILE:
                localeIndex = 267;
                break;
            case LIBRARY_TOP:
                localeIndex = 268;
                break;
            case LIBRARY_BOTTOM:
                localeIndex = 269;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (!simple) {
            GameInformation gi = core.getGameInformation();
            core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(), card.getName(), FTool.getLocale(262)) + extraMessage);
        }
    }
}
