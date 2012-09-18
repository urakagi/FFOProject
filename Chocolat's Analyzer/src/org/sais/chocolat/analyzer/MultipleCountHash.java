package org.sais.chocolat.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

class MultipleCountHash<T> {

    HashMap<T, Integer> mIndecies = new HashMap<T, Integer>();
    ArrayList<ArrayList<Integer>> mValues;

    public MultipleCountHash(int itemCount) {
        mValues = new ArrayList<ArrayList<Integer>>(itemCount);
        for (int i = 0; i < itemCount; ++i) {
            mValues.add(new ArrayList<Integer>());
        }
    }

    public void increase(T key, int itemIndex, int amount) {
        if (key == null) {
            return;
        }
        if (mIndecies.containsKey(key)) {
            ArrayList<Integer> a = mValues.get(itemIndex);
            int index = mIndecies.get(key);
            a.set(index, a.get(index) + amount);
        } else {
            mIndecies.put(key, mValues.get(0).size());
            for (int i=0;i<mValues.size();++i) {
                mValues.get(i).add(i == itemIndex ? amount : 0);
            }
        }
    }

    public ArrayList<T> getSortedKeys() {
        ArrayList<T> ret = new ArrayList<T>();
        for (T s : mIndecies.keySet()) {
            ret.add(s);
        }
        Collections.sort(ret, new Comparator<T>() {

            public int compare(T o1, T o2) {
                int index1 = mIndecies.get(o1);
                int index2 = mIndecies.get(o2);
                for (ArrayList<Integer> a : mValues) {
                    if (a.get(index1) != a.get(index2)) {
                        return a.get(index1) - a.get(index2);
                    }
                }
                return 0;
            }
        });
        Collections.reverse(ret);
        return ret;
    }

    public ArrayList<Integer> get(T key) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        int index = mIndecies.get(key);
        for (ArrayList<Integer> a : mValues) {
            ret.add(a.get(index));
        }
        return ret;
    }
}
