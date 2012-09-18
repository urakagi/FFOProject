/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Romulus
 */
public class CurrentState {

    HashSet<CurrentStateItem> mItems = new HashSet<CurrentStateItem>();

    public String getResult() {
        String rtn = "";

        for (CurrentStateItem item : mItems) {
            rtn += item.getTransferString();
        }

        return rtn;
    }

    public CurrentStateItem makeItem(String itemname) {
        CurrentStateItem item = new CurrentStateItem(itemname);
        mItems.add(item);
        return item;
    }

    public CurrentStateItem getItem(String itemname) {
        for (CurrentStateItem item : mItems) {
            if (item.mItemName.equals(itemname)) {
                return item;
            }
        }
        return null;
    }

    public static CurrentState decode(String statestring) {
        CurrentState state = new CurrentState();

        String[] items = statestring.split(CurrentStateItem.endsymbol);
        for (String itemstring : items) {
            String[] id_splited = itemstring.split(CurrentStateItem.identifier);
            String itemname = id_splited[0];
            CurrentStateItem item = state.makeItem(itemname);
            if (id_splited.length > 1) {
                String[] itemcontext = itemstring.split(CurrentStateItem.identifier)[1].split(CurrentStateItem.seperator);

                for (String context : itemcontext) {
                    item.addValue(context);
                }
            }
        }

        return state;
    }

    public class CurrentStateItem {

        String mItemName;
        ArrayList<String> mValues = new ArrayList<String>();
        public static final String ownerSeprator = "/@";
        public static final String identifier = "=->";
        public static final String seperator = "::";
        public static final String endsymbol = "!end!";

        protected CurrentStateItem(String itemname) {
            mItemName = itemname;
        }

        public void addValue(String value) {
            mValues.add(value);
        }

        public void addValue(int value) {
            mValues.add(String.valueOf(value));
        }

        public void addValue(int[] values) {
            for (int v : values) {
                mValues.add(String.valueOf(v));
            }
        }

        protected String getTransferString() {
            String values = "";
            for (String s : mValues) {
                values += s + seperator;
            }
            return mItemName + identifier + values + endsymbol;
        }

        public ArrayList<String> getValues() {
            return mValues == null ? new ArrayList<String>(0) : mValues;
        }

        public String getSingle() {
            if (mValues.size() == 0) {
                return "";
            }
            return mValues.get(0);
        }
    }
}
