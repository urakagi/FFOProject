/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.card.cardlabel.SpellLabel;
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
public class BattleDistrict implements District {

    private SpellCard mCard = SpellCard.newNull();
    private SpellCard mLastCard = SpellCard.newNull();

    @Override
    public ERegion getRegion() {
        return ERegion.BATTLE;
    }

    @Override
    public SpellCard getCard() {
        return mCard;
    }

    @Override
    public CardSet getSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        mCard = SpellCard.newNull();
    }

    @Override
    public int remove(GameCore core, Card actCard) {
        core.getMainUI().removeBattleCardLabel(mCard.getSpellLabel());
        mCard = SpellCard.newNull();
        return -1;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        if (!(actCard instanceof SpellCard)) {
            return;
        }
        mCard = (SpellCard) actCard;
        mLastCard = SpellCard.newNull();
        if (mCard instanceof SpellCard) {
            try {
                ((SpellCard) mCard).getAttached().setDistrict(this);
            } catch (NotAttachedException e) {
            }
        }
        core.getMainUI().setBattleCardLabel((SpellLabel) mCard.getLabel());
    }

    public void setCard(SpellCard card) {
        this.mCard = card;
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
        // When move a card from battle, record it as "last battled card"
        this.mLastCard = this.mCard;
        
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
            case RESERVED:
                localeIndex = 274;
                word3 = "";
                break;
            default:
                return;
        }

        // Handling attachments
        switch (to) {
            case HAND:
            case DISCARD_PILE:
                if (card instanceof SpellCard) {
                    unattach(core, (SpellCard) card, active);
                }
                break;
        }

        GameInformation gi = core.getGameInformation();
        if (!simple) {
            core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(), card.getName(), word3) + extraMessage);
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
    
    public void clearLastCard() {
        mLastCard = SpellCard.newNull();
    }
    
    public SpellCard getLastCard() {
        return mLastCard;
    }
}
