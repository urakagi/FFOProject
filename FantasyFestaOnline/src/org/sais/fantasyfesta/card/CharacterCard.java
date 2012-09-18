/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import org.sais.fantasyfesta.card.cardlabel.CharacterLabel;
import org.sais.fantasyfesta.card.cardlabel.ICardLabelCallback;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class CharacterCard extends Card {

    public CharacterCard(CharacterCardInfo info, EPlayer controller, EPlayer owner, District district) {
        super(info, controller, owner, district);
    }
    
    @Override
    public CharacterLabel getLabel() {
        if (label == null) {
            label = new CharacterLabel(null, this);
        }
        return (CharacterLabel) label;
    }

    @Override
    public CharacterCardInfo getInfo() {
        return (CharacterCardInfo) info;
    }

    public void setInfo(CardInfo info) {
        this.info = info;
    }

    public static CharacterCard newNull() {
        return new CharacterCard(CharacterCardInfo.newNull(), EPlayer.ICH, EPlayer.ICH, new NullDistrict()) {
        };
    }

    @Override
    public CharacterLabel updateLabel(ICardLabelCallback caller) {
        label = new CharacterLabel(caller, this);
        return (CharacterLabel) label;
    }

}
