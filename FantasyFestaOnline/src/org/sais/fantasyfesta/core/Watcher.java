/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

/**
 *
 * @author Romulus
 */
public class Watcher {

    private int id;
    private String name;

    public Watcher(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public static Watcher makeFromString(String s) {
        String sid = s.split(" ")[0];
        int id = Integer.parseInt(sid);
        String name = s.substring(sid.length() + 1);
        return new Watcher(name, id);
    }

    @Override
    public String toString() {
        return id + " " + name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
