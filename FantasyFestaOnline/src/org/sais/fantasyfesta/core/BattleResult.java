/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class BattleResult {
    
    private boolean hit;
    private boolean faithed;
    private int damage;
    private boolean attacking;

    public BattleResult(boolean attacking) {
        this.attacking = attacking;
    }

    public BattleResult(String socketString) {
        String[] s = socketString.split(" ");
        this.hit = Boolean.parseBoolean(s[0]);
        this.faithed = Boolean.parseBoolean(s[1]);
        this.damage = Integer.parseInt(s[2]);
        this.attacking = Boolean.parseBoolean(s[3]);
    }
    
    public int getDamage() {
        return damage;
    }
    
    public String getResultString() {
        if (!hit) {
            return FTool.getLocale(299);
        }
        if (faithed) {
            return FTool.getLocale(300);
        }
        return FTool.parseLocale(attacking ? 297 : 298, String.valueOf(damage));
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isFaithed() {
        return faithed;
    }

    public void setFaithed(boolean faithed) {
        this.faithed = faithed;
        if (faithed) {
            this.damage = 0;
        }
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
        if (!hit) {
            this.faithed = false;
            this.damage = 0;
        }
    }
    
    public String getSocketString() {
        return this.isHit() + " " + this.isFaithed() + " " + this.damage + " " + this.attacking;
    }
    
}
