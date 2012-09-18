/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import java.util.Collections;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.deck.Deck;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class LibraryDistrict extends CardSetDistrict {

    @Override
    public ERegion getRegion() {
        return ERegion.LIBRARY;
    }

    public void loadDeck(Deck deck, GameCore core) {
        set.clear();
        for (int i = 0; i < 40; ++i) {
            Card card = CardDatabase.getInfo(deck.cards[i]).createCard(EPlayer.ICH, EPlayer.ICH, this);
            set.add(card);
            card.updateLabel(core);
        }
    }

    public void shuffle() {
        Collections.shuffle(set);
    }

    public Card simplePop() {
        if (set.size() == 0) {
            return null;
        }
        return set.remove(set.size() - 1);
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
        switch (to) {
            case LIBRARY_TOP:
                set.add(actCard);
                break;
            case LIBRARY_BOTTOM:
                set.add(0, actCard);
                break;
            case SHUFFLE_THEN_TOP:
                shuffle();
                FTool.playSound("shuffle.wav");
                set.add(actCard);
                core.getMainUI().stopPeekingLibrary();
                break;
        }
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
                localeIndex = 285;
                break;
            case LIBRARY_BOTTOM:
                localeIndex = 286;
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
            case HIDE_TO_HAND:
                localeIndex = 279;
                break;
            case SHUFFLE_THEN_TOP:
                localeIndex = 280;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        GameInformation gi = core.getGameInformation();
        if (!simple && !core.isWatcher()) {
            core.getMainUI().playMessage(FTool.parseLocale(localeIndex, gi.getPlayer(active ? EPlayer.ICH : EPlayer.OPP).getName(),
                    card.getName(), FTool.getLocale(264), gi.getPlayer(newController).getName()) + extraMessage);
        }
        if (to != EMoveTarget.LIBRARY_TOP) {
            core.getMainUI().setLibraryPeekingAmount(core.getMainUI().getLibraryPeekingAmount() - 1);
        }
        if (to == EMoveTarget.SHUFFLE_THEN_TOP) {
            core.getMainUI().setLibraryPeekingAmount(0);
        }
    }
}
