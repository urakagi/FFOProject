package org.sais.fantasyfesta.autos;

import java.util.ArrayList;
import java.util.EnumMap;
import org.sais.fantasyfesta.autos.Effect.ManualModification;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.enums.EPlayer;

public class EffectQueue extends ArrayList<Effect> {

    public EffectQueue() {
    }

    public void add(Card source, int index) {
        add(new Effect(source, index));
    }

    public void addManualModification(EnumMap<EPlayer, Integer> atk, EnumMap<EPlayer, Integer> icp, EnumMap<EPlayer, Integer> faith, EnumMap<EPlayer, Integer> hit, EnumMap<EPlayer, Integer> dodge) {
        ManualModification mmf = new ManualModification();
        mmf.m_atk = atk;
        mmf.m_icp = icp;
        mmf.m_hit = hit;
        mmf.m_evasion = dodge;
        mmf.m_faith = faith;
        add(mmf);
    }

    public boolean hasCard(int cardNo) {
        for (int i = 0; i < this.size(); ++i) {
            if (this.get(i).source.isNo(cardNo)) {
                return true;
            }
        }
        return false;
    }

}
