/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.card.SupportCard;
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
public class ActivatedDistrict extends CardSetDistrict {

    @Override
    public ERegion getRegion() {
        return ERegion.ACTIVATED;
    }

    @Override
    public void add(GameCore core, Card card, EMoveTarget to) {
        set.add(card);
        try {
            ((SpellCard) card).getAttached().setDistrict(this);
        } catch (NotAttachedException e) {
        }
        core.getMainUI().addLabel(card.updateLabel(core), set.size());
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        int localeIndex;
        String word3;

        switch (to) {
            case HAND:
                localeIndex = 266;
                word3 = FTool.getLocale(262);
                break;
            case DISCARD_PILE:
                localeIndex = 267;
                word3 = FTool.getLocale(262);
                break;
            case LIBRARY_TOP:
                localeIndex = 268;
                word3 = FTool.getLocale(262);
                break;
            case LIBRARY_BOTTOM:
                localeIndex = 269;
                word3 = FTool.getLocale(262);
                break;
            case BATTLE:
                localeIndex = 273;
                word3 = "";
                break;
            case RESERVED:
                if (card.getController() == newController) {
                    localeIndex = 274;
                    word3 = "";
                } else {
                    localeIndex = 309;
                    word3 = core.getGameInformation().getPlayer(EPlayer.OPP).getName();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        switch (to) {
            case HAND:
            case DISCARD_PILE:
            case LIBRARY_TOP:
            case LIBRARY_BOTTOM:
                if (card instanceof SpellCard) {
                    unattach(core, (SpellCard) card, active);
                }
                break;
        }

        if (!simple) {
            GameInformation gi = core.getGameInformation();
            if (to == EMoveTarget.BATTLE) {
                core.getMainUI().noLogPlayMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(), card.getName(), word3) + extraMessage);
            } else {
                core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(), card.getName(), word3) + extraMessage);
            }
        }
    }

    private void unattach(GameCore core, SpellCard card, boolean active) {
        try {
            SupportCard sup = card.unattach();
            core.getMainUI().unAttach(card.getSpellLabel(), sup.getSupportLabel());
            if (active) {
                core.handleSupportBuryAlong(sup, true);
            }
        } catch (NotAttachedException ex) {
            // Do nothing
        }
    }
}
