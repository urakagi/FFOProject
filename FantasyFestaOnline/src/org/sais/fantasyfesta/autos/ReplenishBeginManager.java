package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.core.GameInformation;
import org.sais.fantasyfesta.autos.AutoMech.Timing;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.core.PlayField;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class ReplenishBeginManager {

    public static void exec(IAutosCallback caller, GameInformation gi) {

        PlayField f = gi.getField(EPlayer.ICH);
        PlayField of = gi.getField(EPlayer.OPP);

        CardSet<Card> cards = new CardSet<Card>();
        cards.addAll(f.getLeaderAttachment());
        cards.add(gi.getScene());

        for (Card c : cards) {
            for (AutoMech mech : c.getInfo().getAutoMechs()) {
                if (mech.timing == Timing.f1) {
                    if (mech.isLegal(gi, null, EPlayer.ICH, null, false)) {
                        mech.doActions(gi, -1, caller, c);
                    }
                }
            }

            switch (c.getCardNo()) {
                case 9104:
                    if (gi.isIchAttackPlayer() && gi.getPlayer(EPlayer.ICH).getHP() < gi.getPlayer(EPlayer.OPP).getHP()) {
                        caller.adjustHP(1, true, false, c.getName());
                    }
                    break;
                case 2612:
                    if (f.getHand().size() > of.getHand().size() && gi.isIchAttackPlayer()) {
                        caller.adjustSP(1, true, " - " + c.getName());
                    }
                    break;
                case 1015:
                    if (gi.isIchAttackPlayer()) {
                        int getmp = f.getActivated().size();
                        if (getmp > 0) {
                            caller.adjustSP(getmp, true, " - " + c.getName());
                        }
                    }
                    break;
                case 1217:
                    if (f.getHand().size() >= 3 && gi.getPlayer(EPlayer.OPP).getSP() > 0 && gi.isIchAttackPlayer()) {
                        caller.send("$ADJUSTSP:-1 " + c.getName());
                    }
                    break;
                case 9033:
                    if (gi.isIchAttackPlayer()) {
                        caller.peekLibrary(3, " - " + c.getName());
                    }
                    break;
                case 9060:
                    if (gi.getScene().isNo(9)) {
                        // Kourin
                        break;
                    }
                    if (gi.isIchAttackPlayer()) {
                        caller.peekLibrary(1, " - " + c.getName());
                    }
                    break;
                case 10:
                    if (gi.isIchAttackPlayer()) {
                        caller.adjustSP(f.getLeaderLevel() / 2, true, " - " + c.getName());
                    }
                    break;
                case 23:
                    if (gi.isIchAttackPlayer()) {
                        int diff = f.getCharCount() - 2;
                        if (gi.getPlayer(EPlayer.ICH).getSP() > 0 || diff > 0) {
                            caller.adjustSP(diff, true, " - " + c.getName());
                        }
                    }
                    break;
                case 613:
                    if (f.getLeaderAttachment().size() > of.getLeaderAttachment().size() && gi.isIchAttackPlayer()) {
                        caller.adjustSP(2, true, " - " + c.getName());
                    }
                    break;
            }
        }

        // Only Koakuma so use special case handling
        if (f.getCharacterLevel(10) > 1) {
            for (Card c : f.getActivated()) {
                SupportCard sc;
                try {
                    sc = ((SpellCard) c).getAttached();
                    if (sc.isNo(1010) && gi.isIchAttackPlayer()) {
                        caller.adjustSP(1, true, " - " + sc.getName());
                    }
                } catch (NotAttachedException ex) {
                }
            }
        }
    }
}
