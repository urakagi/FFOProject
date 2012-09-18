/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.deck;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Romulus
 */
public class ChocolatsArchiveCore {

    private ArrayList<DeckInfo> mInfos = new ArrayList<DeckInfo>();
    private ArrayList<String> mTags = new ArrayList<String>();

    public ChocolatsArchiveCore(File mRootDir) {
        scan(mRootDir);
        extractTags();
    }

    private void scan(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory() && !f.getName().equals("recycle")) {
                scan(f);
            } else if (f.isFile()) {
                if (f.getName().endsWith(".dec")) {
                    mInfos.add(new DeckInfo(f));
                }
            }
        }
    }

    private void extractTags() {
        for (DeckInfo i : mInfos) {
            for (String s : i.getTags()) {
                if (!mTags.contains(s)) {
                    mTags.add(s);
                }
            }
        }
        Collections.sort(mTags);
    }

    public ArrayList<String> getTags() {
        return mTags;
    }

    public ArrayList<DeckInfo> getInfos() {
        return mInfos;
    }

}
