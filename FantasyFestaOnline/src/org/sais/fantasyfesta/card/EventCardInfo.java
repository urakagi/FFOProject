/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import java.awt.Color;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.Player;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class EventCardInfo extends CardInfo {

    static final int TEXT_TYPE_INDEX = 3;
    private EPhase mTiming;

    @Override
    public String dump(String[] loc) {
        return FTool.parseLocale(19, String.valueOf(getSpellPointRequirement())) + "\n" 
                + FTool.parseLocale(20, getCharacterRequirement()) + "\n" 
                + FTool.parseLocale(22, FTool.getLocale(15 + getTiming().ordinal()));
    }

    @Override
    public String getDefaultIconFilePrefix() {
        return "Event";
    }

    @Override
    public ECardType getCardType() {
        return ECardType.EVENT;
    }

    @Override
    public void setFifthLine(String pp) {
        mTiming = EPhase.values()[FTool.safeParseInt(pp)];
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(0xD0FFD0);
    }

    @Override
    public EventCard createCard(EPlayer controller, EPlayer owner, District district) {
        return new EventCard(this, controller, owner, district);
    }

    public EPhase getTiming() {
        return mTiming;
    }

    @Override
    public void payCost(Player player, GameCore core) {
        super.payCost(player, core);
        int index = getRuleText().indexOf(FTool.getLocale(124));
        if (index >= 0) {
            core.adjustHP(-FTool.safeParseInt(String.valueOf(getRuleText().charAt(getRuleText().indexOf(FTool.getLocale(124)) + 13))), 
                    true, true, " - " + getName());
        }
        index = getRuleText().indexOf(FTool.getLocale(190));
        if (index >= 0) {
            int amount = FTool.safeParseInt(String.valueOf(getRuleText().charAt(index + FTool.getLocale(190).length())));
            for (int i = 0; i < amount; ++i) {
                core.deckout(" - " + getName());
            }
        }
    }

    public static EventCardInfo newNull() {
        return new EventCardInfo();
    }
}
