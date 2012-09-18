/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.district;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CharacterCard;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author Romulus
 */
public class CharactersDistrict extends CardSetDistrict {

    private CharacterCard leader = CharacterCard.newNull();
    private ArrayList<CharacterCard> chars = new ArrayList<CharacterCard>(4);

    @Override
    public ERegion getRegion() {
        return ERegion.CHARACTERS;
    }

    @Override
    public void add(GameCore core, Card actCard, EMoveTarget to) {
    }

    @Override
    public void handleMoving(GameCore core, Card card, EMoveTarget to, EPlayer newController, boolean active, boolean simple, String extraMessage) {
    }

    public ArrayList<CharacterCard> getCharsExceptLeader() {
        ArrayList<CharacterCard> ret = new ArrayList<CharacterCard>(chars);
        Collections.sort(ret, new Comparator<CharacterCard>() {

            @Override
            public int compare(CharacterCard o1, CharacterCard o2) {
                if (o1.getCardNo() == leader.getCardNo()) {
                    return -1;
                }
                if (o2.getCardNo() == leader.getCardNo()) {
                    return 1;
                }
                return o1.getCardNo() - o2.getCardNo();
            }
        });
        ret.remove(leader);
        return ret;
    }

    public ArrayList<CharacterCard> getChars() {
        return chars;
    }

    public void setChars(ArrayList<CharacterCard> chars) {
        this.chars = chars;
        for (CharacterCard card : this.chars) {
            card.setDistrict(this);
        }
    }

    public CharacterCard getLeader() {
        return leader;
    }

    public void setLeader(CharacterCard leader) {
        this.leader = leader;
        this.leader.setDistrict(this);
    }
}
