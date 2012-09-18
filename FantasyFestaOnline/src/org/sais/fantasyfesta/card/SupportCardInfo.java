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
import org.sais.fantasyfesta.enums.ESupportType;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class SupportCardInfo extends CardInfo {

    static final int TEXT_TYPE_INDEX = 2;
    private ESupportType mSupportType;

    @Override
    public String dump(String[] loc) {
        return FTool.parseLocale(19, String.valueOf(getSpellPointRequirement())) + "\n"
                + FTool.parseLocale(20, getCharacterRequirement()) + "\n"
                + FTool.parseLocale(21, FTool.getLocale(12 + getSupportType().ordinal()));
    }

    @Override
    public String getDefaultIconFilePrefix() {
        return "Support";
    }

    @Override
    public ECardType getCardType() {
        return ECardType.SUPPORT;
    }

    @Override
    public void setFifthLine(String pp) {
        mSupportType = ESupportType.values()[FTool.safeParseInt(pp)];
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(0xEEEEA0);
    }

    @Override
    public SupportCard createCard(EPlayer controller, EPlayer owner, District district) {
        return new SupportCard(this, controller, owner, district);
    }

    @Override
    public void payCost(Player player, GameCore core) {
        super.payCost(player, core);
        // extra HP cost
        int index = getRuleText().indexOf(FTool.getLocale(125));
        if (index >= 0) {
            core.adjustHP(-FTool.safeParseInt(String.valueOf(getRuleText().charAt(index + FTool.getLocale(125).length()))),
                    true, true, " - " + getName());
        }
        // extra deck cost
        index = getRuleText().indexOf(FTool.getLocale(126));
        if (index >= 0) {
            for (int i = 0; i < Integer.parseInt(String.valueOf(getRuleText().charAt(index + FTool.getLocale(126).length()))); ++i) {
                core.deckout(" - " + getName());
            }
        }
    }

    public ESupportType getSupportType() {
        return mSupportType;
    }

    public static SupportCardInfo newNull() {
        return new SupportCardInfo();
    }
}
