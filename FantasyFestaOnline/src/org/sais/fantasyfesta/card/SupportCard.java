/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import org.sais.fantasyfesta.card.cardlabel.CardLabel;
import org.sais.fantasyfesta.card.cardlabel.ICardLabelCallback;
import org.sais.fantasyfesta.card.cardlabel.SupportLabel;
import org.sais.fantasyfesta.card.cardlabel.UniLabel;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class SupportCard extends Card {

    private SpellCard mAttachingOn = null;
    
    public SupportCard(SupportCardInfo info, EPlayer controller, EPlayer owner, District district) {
        super(info, controller, owner, district);
    }

    public void attach(SpellCard card) {
        mAttachingOn = card;
        setController(card.getController());
        setDistrict(card.getDistrict());
    }

    public void unattach() {
        mAttachingOn = null;
    }

    public SpellCard getAttachingOn() {
        return mAttachingOn;
    }

    public SupportLabel getSupportLabel() {
        return (SupportLabel) label;
    }

    @Override
    public SupportCardInfo getInfo() {
        return (SupportCardInfo) info;
    }

    @Override
    public CardLabel updateLabel(ICardLabelCallback caller) {
        switch (this.getRegion()) {
            case RESERVED:
            case ACTIVATED:
            case BATTLE:
            case LEADER_ATTACHMENTS:
            case SCENE:
                label = new SupportLabel(caller, this);
                ((SupportLabel) label).setPopupMenu();
                return label;
            default:
                label = new UniLabel(caller, this);
                return label;
        }
    }

    public static SupportCard newNull() {
        return new SupportCard(SupportCardInfo.newNull(), EPlayer.ICH, EPlayer.ICH, new NullDistrict()) {
        };
    }

}
