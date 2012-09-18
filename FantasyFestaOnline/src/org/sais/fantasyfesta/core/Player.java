package org.sais.fantasyfesta.core;

import org.sais.fantasyfesta.deck.Deck;
import org.sais.fantasyfesta.enums.EPlayer;

public class Player {

    private String name = "";
    private Deck deck = Deck.newNull();
    private int HP = 0;
    private int SP = 0;
    private EPlayer who = EPlayer.ICH;

    public Player(String name, EPlayer who) {
        this.name = name;
        this.who = who;
    }

    public int getHP() {
        return HP;
    }

    public void setHP(int HP) {
        this.HP = HP;
    }

    public void heal(int amount) {
        this.HP += amount;
    }

    public void damage(int amount) {
        this.HP -= amount;
    }

    public int getSP() {
        return SP;
    }

    public void setSP(int SP) {
        this.SP = SP;
    }

    public void replenish(int delta) {
        this.SP += delta;
    }

    public void consume(int delta) {
        this.SP -= delta;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EPlayer getWho() {
        return who;
    }

    public void setWho(EPlayer who) {
        this.who = who;
    }

    public boolean isDeckLoaded() {
        return deck != null;
    }
    
}
