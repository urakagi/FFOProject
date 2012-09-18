/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import java.io.File;
import javax.swing.ImageIcon;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class SceneDistrict implements District {

    private SupportCard mCard = SupportCard.newNull();

    @Override
    public ERegion getRegion() {
        return ERegion.SCENE;
    }

    @Override
    public SupportCard getCard() {
        return this.mCard;
    }

    @Override
    public CardSet getSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        this.mCard = SupportCard.newNull();
    }

    @Override
    public int remove(GameCore core, Card actCard) {
        if (mCard.isNull()) {
            return -1;
        }
        core.getMainUI().removeSceneLabel(mCard);
        this.mCard = SupportCard.newNull();
        return -1;
    }

    public void set(SupportCard scene) {
        this.mCard = scene;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        if (!mCard.isNull()) {
            core.moveCard(mCard, mCard.getOwner(), EMoveTarget.DISCARD_PILE, false, true, "");
        }
        
        mCard = (SupportCard) actCard;
        mCard.setController(EPlayer.ICH);

        if (mCard.getOwner() == EPlayer.ICH) {
            // Tenko leader ability
            GameInformation gi = core.getGameInformation();
            if (gi.getField(EPlayer.ICH).getLeader().isNo(2700) && gi.getPhase() == EPhase.ACTIVATION) {
                core.adjustSP(1, true, " - " + CardDatabase.getInfo(2700).getName());
            }
        }

        if (FTool.readConfig("playbgm").equals("true")) {
            core.getMainUI().changeBGM(mCard.getName());
        }
        if (FTool.readConfig("background").equals("true")) {
            core.getMainUI().changeBackground(mCard.getName());
        }

        if (new File("dir").exists()) {
            if (new File("dir/" + mCard.getCardNo() + ".jpg").exists()) {
                mCard.getLabel().setIcon(new ImageIcon("dir/" + mCard.getCardNo() + ".jpg"));
            }
        }

        core.getMainUI().setSceneLabel(mCard);
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
        GameInformation gi = core.getGameInformation();
        if (!simple) {
            core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                    card.getName(), FTool.getLocale(262)) + extraMessage);
        }
    }
}
