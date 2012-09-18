/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import org.sais.fantasyfesta.enums.ECardType;
import java.util.ArrayList;

/**
 *
 * @author Romulus
 */
public class CardSet<T extends Card> extends ArrayList<T> {

    public boolean hasCard(int cardNo) {
        for (Card c : this) {
            if (c.isNo(cardNo)) {
                return true;
            }
        }
        return false;
    }

    public int counts(int cardNo) {
        int ret = 0;
        for (Card c : this) {
            if (c.isNo(cardNo) || cardNo < 0) {
                ++ret;
            }
        }
        return ret;
    }

    public int countSpell(int cardNo) {
        int ret = 0;
        for (Card c : this) {
            if (c.isNo(cardNo)
                    || (c.getInfo().isCardType(ECardType.SPELL) && cardNo < 0)) {
                ++ret;
            }
        }
        return ret;
    }

    public int countType(ECardType type) {
        int ret = 0;
        for (Card c : this) {
            if (c.getInfo().isCardType(type)) {
                ++ret;
            }
        }
        return ret;
    }

    public String getListString() {
        String ret = "";
        for (Card c : this) {
            ret += c.getCardNo() + " ";
        }
        return ret;
    }
}
