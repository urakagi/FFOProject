/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import org.sais.fantasyfesta.card.cardlabel.CardLabel;
import org.sais.fantasyfesta.card.cardlabel.ICardLabelCallback;
import org.sais.fantasyfesta.card.cardlabel.SpellLabel;
import org.sais.fantasyfesta.card.cardlabel.UniLabel;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class SpellCard extends Card {

    private SupportCard mAttachedby = null;

    public SpellCard(SpellCardInfo info, EPlayer controller, EPlayer owner, District district) {
        super(info, controller, owner, district);
    }

    public void attach(SupportCard card) {
        if (card == null) {
            return;
        }
        mAttachedby = card;
        mAttachedby.attach(this);
    }

    public SupportCard unattach() throws NotAttachedException {
        if (mAttachedby != null) {
            SupportCard ret = mAttachedby;
            ret.unattach();
            mAttachedby = null;
            return ret;
        } else {
            throw new NotAttachedException();
        }
    }

    public SupportCard getAttached() throws NotAttachedException {
        if (mAttachedby == null) {
            throw new NotAttachedException();
        } else {
            return mAttachedby;
        }
    }

    /**
     * Get attachment, only use if is inside a if (isAttached) condition.
     *
     * @return
     */
    public SupportCard getAttachedDontThrow() {
        return mAttachedby;
    }

    /**
     * Get attachment.
     *
     * @return the attachment. Supportcard's null-object if is not attached.
     */
    public SupportCard getAttachedOrNullObject() {
        if (mAttachedby == null) {
            return SupportCard.newNull();
        } else {
            return mAttachedby;
        }
    }

    public boolean isAttached() {
        return mAttachedby != null;
    }

    public SpellLabel getSpellLabel() {
        return (SpellLabel) label;
    }

    @Override
    public SpellCardInfo getInfo() {
        return (SpellCardInfo) info;
    }

    @Override
    public CardLabel updateLabel(ICardLabelCallback caller) {
        switch (this.getRegion()) {
            case RESERVED:
            case ACTIVATED:
            case BATTLE:
                label = new SpellLabel(caller, this);
                ((SpellLabel) label).setPopupMenu();
                return label;
            default:
                label = new UniLabel(caller, this);
                return label;
        }
    }

    public static class NotAttachedException extends Exception {
    }

    public static SpellCard newNull() {
        return new SpellCard(SpellCardInfo.newNull(), EPlayer.ICH, EPlayer.ICH, new NullDistrict()) {
        };
    }
    
}
