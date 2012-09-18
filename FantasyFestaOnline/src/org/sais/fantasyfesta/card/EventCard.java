/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import org.sais.fantasyfesta.card.cardlabel.CardLabel;
import org.sais.fantasyfesta.card.cardlabel.EventLabel;
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
public class EventCard extends Card {

    public EventCard(EventCardInfo info, EPlayer controller, EPlayer owner, District district) {
        super(info, controller, owner, district);
    }

    @Override
    public EventCardInfo getInfo() {
        return (EventCardInfo) info;
    }

    public EventLabel getEventLabel() {
        return (EventLabel) label;
    }

    @Override
    public CardLabel updateLabel(ICardLabelCallback caller) {
        switch (this.getRegion()) {
            case EVENT:
                label = new EventLabel(caller, this);
                ((EventLabel) label).setPopupMenu();
                return label;
            default:
                label = new UniLabel(caller, this);
                return label;
        }
    }

    public static EventCard newNull() {
        return new EventCard(EventCardInfo.newNull(), EPlayer.ICH, EPlayer.ICH, new NullDistrict()) {
        };
    }
}
