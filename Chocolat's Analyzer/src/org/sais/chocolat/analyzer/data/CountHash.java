package org.sais.chocolat.analyzer.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class CountHash<T> {

    private HashMap<T, Integer> mHashMap = new HashMap<T, Integer>();
    private HashMap<T, Integer> mCountHashMap = new HashMap<T, Integer>();

    public void increase(T key, int amount) {
        if (key == null) {
            return;
        }
        if (mHashMap.containsKey(key)) {
            mHashMap.put(key, mHashMap.get(key) + amount);
            mCountHashMap.put(key, mCountHashMap.get(key) + 1);
        } else {
            mHashMap.put(key, amount);
            mCountHashMap.put(key, 1);
        }
    }

    public ArrayList<Entry> getSortedList(final boolean decremental) {
        ArrayList<Entry> ret = new ArrayList<Entry>();
        ret.addAll(mHashMap.entrySet());
        Collections.sort(ret, new Comparator<Entry>() {

            public int compare(Entry o1, Entry o2) {
                if (decremental) {
                    return ((Integer) o2.getValue()) - ((Integer) o1.getValue());
                } else {
                    return ((Integer) o1.getValue()) - ((Integer) o2.getValue());
                }
            }
        });
        return ret;
    }

    public HashMap<T, Integer> getHash() {
        return mHashMap;
    }

    public int get(T key) {
        return mHashMap.get(key) == null ? 0 : mHashMap.get(key);
    }

    public double getAverage(T key) {
        return mHashMap.get(key) * 1. / mCountHashMap.get(key);
    }

}
