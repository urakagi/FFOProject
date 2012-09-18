/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.awt.Color;
import java.util.EnumMap;
import org.sais.fantasyfesta.autos.BattleValues;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.EventCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.card.cardlabel.CardLabel;
import org.sais.fantasyfesta.card.cardlabel.SpellLabel;
import org.sais.fantasyfesta.card.cardlabel.SupportLabel;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public interface IMainUI {

    public void clearField();

    public GameTimer.IGameTimerCallback getTimerUpdateListener();

    public void playMessage(String message);

    public void noLogPlayMessage(String message);
        
    public abstract void actionDone();

    public void refreshCounter();

    public void showCard(CardInfo info);

    public void showTurn(GameInformation gi);

    public void addLabel(CardLabel label, int size);

    public void removeLabel(CardLabel label, EPlayer controller, ERegion region);

    public void showBattleValue(EnumMap<EPlayer, BattleValues> battleValues);

    public void setTimerTurn(EPlayer ep);

    public void showCommandPanel(String tag);

    public void setBattleCardLabel(SpellLabel card);

    public void removeBattleCardLabel(SpellLabel card);

    public void resetBattleSab();

    public void postBattle();

    public void through(boolean active);

    public void setEventLabel(EventCard card);

    public void removeEventLabel(EventCard card);

    public void setSceneLabel(SupportCard card);

    public void removeSceneLabel(SupportCard card);

    public void changeBGM(String name);

    public void changeBackground(String cardname);

    public boolean doAttach(SupportCard support, SpellLabel target);

    public boolean unAttach(SpellLabel label, SupportLabel supportLabel);

    public boolean isChoosingAttach();

    public void stopAttach();

    public void showCharacters(EPlayer ep);

    public void setBattleSab(String[] s);

    public void insertMessage(String message, Color color);
    
    public void insertNoLogMessage(String message, Color color);
    
    public void showRegion(final ERegion region);

    public int getLibraryPeekingAmount();

    public void setLibraryPeekingAmount(int amount);
    
    public void loadDeck(String rootdir);
    
    public void displayName();
    
    public void refreshDeckHistory();
    
    public void setTitle(String title);

    public void setVisible(boolean b);

    public void setRevealCards(String cards);

    public void updateField();

    public void startChooseAttach();

    public void peekWholeLibrary(String extramessage);
    
    public void stopPeekingLibrary();
}
