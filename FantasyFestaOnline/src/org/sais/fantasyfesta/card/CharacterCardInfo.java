/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import java.awt.Color;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class CharacterCardInfo extends CardInfo {

    static final int TEXT_TYPE_INDEX = 0;
    static final private String[] SINGLE_TEXT_NAMES = new String[]{
              "霊", "魔", "咲", "妖", "紫", "ア", "レ", "幽", "フ", "パ"
            , "紅", "輝", "永", "鈴", "藍", "橙", "プ", "妹", "慧", "萃"
            , "諏", "神", "早", "文", "小", "に", "天", "衣", "空", "燐"
            , "こ", "さ", "白", "星", "ぬ", "傘", "ナ", "水"};
    //
    private String mDesignations;
    private int mHitPoints;
    private int mEvasionValue;
    private int mBorderValue;

    @Override
    public String dump(String[] loc) {
        return FTool.parseLocale(91, getDesignations()) + "\n"
                + FTool.parseLocale(2, getHitPoints()) + "\n"
                + FTool.parseLocale(3, getEvasionValue()) + "\n"
                + FTool.parseLocale(4, getBorderValue());
    }

    @Override
    public String getDefaultIconFilePrefix() {
        return "Char";
    }

    @Override
    public ECardType getCardType() {
        return ECardType.CHARACTER;
    }
    
    @Override
    public void setFifthLine(String pp) {
        String[] values = pp.split("-");
        mDesignations = values[0];
        mHitPoints = FTool.safeParseInt(values[1]);
        mEvasionValue = FTool.safeParseInt(values[2]);
        mBorderValue = FTool.safeParseInt(values[3]);
    }

    @Override
    public void setCharacterRequirement(String pp) {
        return;
    }

    @Override
    public void setSPRequirement(String pp) {
        return;
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(0xFFD0D0);
    }
    
    public String getShortName() {
        return getName().split(FTool.getLocale(81))[0];
    }

    public String getSingleTextName() {
        return SINGLE_TEXT_NAMES[getCharId() - 1];
    }

    @Override
    public CharacterCard createCard(EPlayer controller, EPlayer owner, District district) {
        return new CharacterCard(this, controller, owner, district);
    }

    public int getBorderValue() {
        return mBorderValue;
    }

    public String getDesignations() {
        return mDesignations;
    }

    public int getEvasionValue() {
        return mEvasionValue;
    }

    public int getHitPoints() {
        return mHitPoints;
    }

    public static CharacterCardInfo newNull() {
        return new CharacterCardInfo();
    }
}
