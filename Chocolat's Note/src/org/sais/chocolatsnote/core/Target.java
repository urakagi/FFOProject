/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.chocolatsnote.core;

import java.util.ArrayList;

/**
 *
 * @author Romulus
 */
public class Target implements Comparable<Target>, Cloneable {

    public String name;
    public int rating = 1600;
    public int rating2 = 1600;
    public int ratingMF = 0;
    public int appear = 0;
    public int totalLv = 0;
    public int leaderCnt = 0;
    public int won = 0;
    public int lost = 0;
    public int drawn = 0;
    public static boolean sSortByPercentage = false;

    public static Target findByName(ArrayList<Target> list, String name) {
        for (Target p : list) {
            if (p.name.equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public static Target getOrNew(ArrayList<Target> list, String name) {
        for (Target p : list) {
            if (p.name.equalsIgnoreCase(name)) {
                return p;
            }
        }
        Target ret = new Target(name);
        list.add(ret);
        return ret;
    }

    public Target(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Target.class) {
            return false;
        }
        return ((Target) obj).name.equalsIgnoreCase(this.name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.name.toLowerCase() != null ? this.name.toLowerCase().hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Target o) {
        if (sSortByPercentage) {
            double ret = 1.0 * o.won / (o.won + o.lost + o.drawn) - 1.0 * this.won / (this.won + this.lost + this.drawn);
            return ret == 0 ? 0 : ret > 0 ? 1 : -1;
        }
        if (o.rating != this.rating) {
            return o.rating - this.rating;
        }
        return o.name.compareTo(this.name);
    }

    @Override
    public Target clone() throws CloneNotSupportedException {
        return (Target) super.clone();
    }
}
