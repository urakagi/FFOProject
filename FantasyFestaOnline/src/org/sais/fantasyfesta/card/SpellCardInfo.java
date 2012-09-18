/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import java.awt.Color;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.enums.EBulletType;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class SpellCardInfo extends CardInfo {

    static final int TEXT_TYPE_INDEX = 1;
    private int mAttackValue = 0;
    private int mInterceptValue = 0;
    private int mHitValue = 0;
    private EBulletType mBulletType = EBulletType.CONCENTRATION;

    @Override
    public String dump(String[] loc) {
        return FTool.parseLocale(19, String.valueOf(getSpellPointRequirement())) + "\n"
                + FTool.parseLocale(20, getCharacterRequirement()) + "\n"
                + FTool.parseLocale(5, getAttackValue()) + "    "
                + FTool.parseLocale(6, getInterceptValue()) + "\n"
                + FTool.parseLocale(7, getHitValue()) + "    "
                + FTool.parseLocale(8, FTool.getLocale(9 + getBulletType().ordinal()));
    }

    @Override
    public String getDefaultIconFilePrefix() {
        return "Spell";
    }

    @Override
    public ECardType getCardType() {
        return ECardType.SPELL;
    }

    @Override
    public void setFifthLine(String pp) {
        String[] values = pp.split("-");
        mAttackValue = FTool.safeParseInt(values[0]);
        mInterceptValue = FTool.safeParseInt(values[1]);
        mHitValue = FTool.safeParseInt(values[2]);
        mBulletType = EBulletType.values()[FTool.safeParseInt(values[3])];
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(0xD0D0FF);
    }

    @Override
    public SpellCard createCard(EPlayer controller, EPlayer owner, District district) {
        return new SpellCard(this, controller, owner, district);
    }

    public int getAttackValue() {
        return mAttackValue;
    }

    public EBulletType getBulletType() {
        return mBulletType;
    }

    public int getHitValue() {
        return mHitValue;
    }

    public int getInterceptValue() {
        return mInterceptValue;
    }

    public static SpellCardInfo newNull() {
        return new SpellCardInfo();
    }
;
}
