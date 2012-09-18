/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.fantasyfesta.tool;

import java.awt.Color;

/**
 *
 * @author Romulus
 */
public class Constants {
    
    public static final Color DEEP_ORANGE = new Color(215, 71, 33);

    public static final int REGION_ACTIVATED = 0;
    public static final int REGION_STANDNBY = 1;
    public static final int REGION_BATTLECARD = 2;
    public static final int REGION_HAND = 3;
    public static final int REGION_LIBRARY = 4;
    public static final int REGION_GRAVE = 5;
    public static final int REGION_PLAYED_EVENT = 6;

    static public final int ATTACHTARGET_STANDBYSPELL = 0;
    static public final int ATTACHTARGET_ACTIVATEDSPELL = 1;
    static public final int ATTACHTARGET_LEADER = 2;

    static public final int BATTLERESULT_FALL = 0;
    static public final int BATTLERESULT_DODGE = 1;
    static public final int BATTLERESULT_FAITH = 2;
    
    static public final int PHASE_FILL = 0;
    static public final int PHASE_BATTLE = 1;
    static public final int PHASE_ACTIVATE = 2;

}
