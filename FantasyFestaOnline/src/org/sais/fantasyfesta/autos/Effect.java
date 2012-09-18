/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.autos;

import java.util.EnumMap;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.enums.EPlayer;

/**
 *
 * @author Romulus
 */
public class Effect {

    public static final int INDEX_EVENT = 0;

    public Card source;
    public EffectType type;
    public int index;

    public Effect(Card source, int index) {
        this.source = source;
        this.type = new EffectType(source.getInfo());
        this.index = index;
    }

    public static class ManualModification extends Effect {

        EnumMap<EPlayer, Integer> m_atk;
        EnumMap<EPlayer, Integer> m_icp;
        EnumMap<EPlayer, Integer> m_hit;
        EnumMap<EPlayer, Integer> m_evasion;
        EnumMap<EPlayer, Integer> m_faith;

        public ManualModification() {
            super(Card.newNull(), -1);
            m_atk = new EnumMap<EPlayer, Integer>(EPlayer.class);
            m_icp = new EnumMap<EPlayer, Integer>(EPlayer.class);
            m_hit = new EnumMap<EPlayer, Integer>(EPlayer.class);
            m_evasion = new EnumMap<EPlayer, Integer>(EPlayer.class);
            m_faith = new EnumMap<EPlayer, Integer>(EPlayer.class);
            m_atk.put(EPlayer.ICH, 0);
            m_atk.put(EPlayer.OPP, 0);
            m_icp.put(EPlayer.ICH, 0);
            m_icp.put(EPlayer.OPP, 0);
            m_hit.put(EPlayer.ICH, 0);
            m_hit.put(EPlayer.OPP, 0);
            m_evasion.put(EPlayer.ICH, 0);
            m_evasion.put(EPlayer.OPP, 0);
            m_faith.put(EPlayer.ICH, 0);
            m_faith.put(EPlayer.OPP, 0);
        }
    }
}
