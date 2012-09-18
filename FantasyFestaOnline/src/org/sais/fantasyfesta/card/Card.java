/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.card.cardlabel.CardLabel;
import org.sais.fantasyfesta.card.cardlabel.ICardLabelCallback;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.district.CardSetDistrict;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public abstract class Card implements Comparable<Card> {

    protected CardInfo info;
    protected EPlayer controller;
    protected EPlayer owner;
    protected District district;
    protected CardLabel label;

    public Card(CardInfo info, EPlayer controller, EPlayer owner, District district) {
        if (info == null) {
            new Throwable().printStackTrace();
        }
        this.info = info;
        this.controller = controller;
        this.owner = owner;
        this.district = district;
    }

    abstract public CardLabel updateLabel(ICardLabelCallback caller);

    public void setLabel(CardLabel label) {
        this.label = label;
    }

    public CardInfo getInfo() {
        return info;
    }

    public boolean isNo(int no) {
        return info.isNo(no);
    }

    public int getCardNo() {
        return info.getCardNo();
    }

    public String getName() {
        return info.getName();
    }

    public boolean isIchControl() {
        return controller == EPlayer.ICH;
    }

    public boolean isOppControl() {
        return controller == EPlayer.OPP;
    }

    public EPlayer getController() {
        return controller;
    }

    public void setController(EPlayer controller) {
        this.controller = controller;
    }

    public EPlayer getOwner() {
        return owner;
    }

    public CardLabel getLabel() {
        return label;
    }

    public ERegion getRegion() {
        return district.getRegion();
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District destination) {
        this.district = destination;
    }

    public int getPosition() {
        if (getDistrict() instanceof CardSetDistrict) {
            return getDistrict().getSet().indexOf(this);
        } else {
            return -1;
        }
    }

    public int leave(GameCore core) {
        District orgDistrict = getDistrict();
        this.district = new NullDistrict();
        int ret = orgDistrict.remove(core, this);
        removeLabel();
        return ret;
    }

    public boolean isNull() {
        return info.isNo(0);
    }

    public static Card newNull() {
        return new Null(CardDatabase.getInfo(0), EPlayer.ICH, EPlayer.ICH, ERegion.NULL) {
        };
    }

    public void removeLabel() {
        this.label = null;
    }

    @Override
    public int compareTo(Card o) {
        if (this.getCardNo() == o.getCardNo()) {
            return -1;
        }
        return this.getCardNo() - o.getCardNo();
    }

    public static class Null extends Card {

        public Null(CardInfo info, EPlayer controller, EPlayer owner, ERegion region) {
            super(info, controller, owner, null);
        }

        @Override
        public CardLabel updateLabel(ICardLabelCallback caller) {
            return null;
        }
    }
}
