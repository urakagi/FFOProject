package org.sais.chocolat.analyzer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class Continuous {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
    int length = 0;
    Calendar from = null;
    Calendar to = null;

    String getDisplayMessage() {
        if (from == null) {
            return "0";
        }
        return length + " (" + format.format(from.getTime()) + "-" + format.format(to.getTime()) + ")";
    }
}
