package org.sais.chocolat.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


class SortableArrayHashMap<K, V extends ArrayList> extends HashMap<K, V> {

    public ArrayList<K> getSortedKeys() {
        ArrayList<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(this.entrySet());
        Collections.sort(entries, new Comparator<Entry<K, V>>() {

            public int compare(Entry<K, V> obj1, Entry<K, V> obj2) {
                Map.Entry ent1 = (Map.Entry) obj1;
                Map.Entry ent2 = (Map.Entry) obj2;
                V val1 = (V) ent1.getValue();
                V val2 = (V) ent2.getValue();
                return val2.size() - val1.size();
            }
        });
        ArrayList<K> ret = new ArrayList<K>();
        for (Entry<K, V> e : entries) {
            ret.add(e.getKey());
        }
        return ret;
    }
}

class SortableHashMap<K, V extends Double> extends HashMap<K, V> {
    public ArrayList<K> getSortedKeys() {
        ArrayList<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(this.entrySet());
        Collections.sort(entries, new Comparator<Entry<K, V>>() {
            public int compare(Entry<K, V> obj1, Entry<K, V> obj2) {
                Map.Entry ent1 = (Map.Entry) obj1;
                Map.Entry ent2 = (Map.Entry) obj2;
                V val1 = (V) ent1.getValue();
                V val2 = (V) ent2.getValue();
                return val2.compareTo(val1);
            }
        });

        ArrayList<K> ret = new ArrayList<K>();
        for (Entry<K, V> e : entries) {
            ret.add(e.getKey());
        }
        return ret;
    }
}