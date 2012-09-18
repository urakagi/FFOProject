package org.sais.chocolatsnote.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class CountHash<T> {

    HashMap<T, Integer> mHashMap = new HashMap<T, Integer>();

    public void increase(T key, int amount) {
        if (key == null) {
            return;
        }
        if (mHashMap.containsKey(key)) {
            mHashMap.put(key, mHashMap.get(key) + amount);
        } else {
            mHashMap.put(key, amount);
        }
    }

    public ArrayList<Entry<T, Integer>> getSortedList(final boolean decremental) {
        ArrayList<Entry<T, Integer>> ret = new ArrayList<Entry<T, Integer>>();
        ret.addAll(mHashMap.entrySet());
        Collections.sort(ret, new Comparator<Entry<T, Integer>>() {

            public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
                if (decremental) {
                    return ((Integer) o2.getValue()) - ((Integer) o1.getValue());
                } else {
                    return ((Integer) o1.getValue()) - ((Integer) o2.getValue());
                }
            }
        });
        return ret;
    }

    public ArrayList<Entry<T, Integer>> getSortedList(final boolean decremental, final Comparator<Entry<T, Integer>> subComparator) {
        ArrayList<Entry<T, Integer>> ret = new ArrayList<Entry<T, Integer>>();
        ret.addAll(mHashMap.entrySet());
        Collections.sort(ret, new Comparator<Entry<T, Integer>>() {

            public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
                int value = ((Integer) o2.getValue()) - ((Integer) o1.getValue());
                if (value == 0) {
                    return subComparator.compare(o1, o2);
                }
                return decremental ? value : -value;
            }
        });
        return ret;
    }

    public int get(T key) {
        return mHashMap.get(key) == null ? 0 : mHashMap.get(key);
    }
}
