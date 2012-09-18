/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.chocolat.analyzer;

import java.util.HashMap;

/**
 *
 * @author Romulus
 */
public class Tools {

    private static HashMap<String, Long> mTimers = new HashMap<String, Long>();

    public static void logInterval(String key, String... message) {
        if (mTimers.containsKey(key)) {
            long now = System.currentTimeMillis();
            if (message.length > 0) {
                System.out.println(message[0] + " - " + (now - mTimers.get(key)) + " ms.");
                mTimers.put(key, now);
            } else {
                System.out.println("Interval " + (now - mTimers.get(key)) + " ms.");
                mTimers.put(key, now);
            }
        } else {
            mTimers.put(key, System.currentTimeMillis());
        }
    }

}
