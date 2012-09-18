/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class AttachmentTriggerManager {
    
    static public void exec(IAutosCallback caller, SupportCard attachment, Card target) {
        if (caller.isWatcher()) {
            return;
        }
        
        switch (attachment.getCardNo()) {
            case 9105:
                if (target instanceof SpellCard) {
                    caller.moveCard(target, EPlayer.OPP, EMoveTarget.RESERVED, true, false, " - " + CardDatabase.getCardName(9105));
                }
                break;
        }
    }
    
}
