package org.sais.chocolat.analyzer.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CADeck implements Comparable<CADeck> {

    public String myname;
    public DeckType deckType;
    public HashMap<DeckType, ArrayList<DeckResult>> oppDecks;
    public HashMap<DeckType, ArrayList<DeckResult>> oppDecksMerged;
    public int resultCount = 0;
    public Stats stats = new Stats();
    public Calendar firstused;
    public Calendar lastused;

    public CADeck(String myname, String deckstring) {
        deckType = new DeckType(myname, deckstring);
        oppDecks = new HashMap<DeckType, ArrayList<DeckResult>>();
        oppDecksMerged = new HashMap<DeckType, ArrayList<DeckResult>>();
        Calendar old = Calendar.getInstance();
        old.set(1970, 1, 1);
        lastused = old;
        Calendar fut = Calendar.getInstance();
        fut.set(2999, 1, 1);
        firstused = fut;
    }

    public void addResult(String oppname, String oppdeckstring, DeckResult result) {
        DeckType index = null;
        for (DeckType d : oppDecks.keySet()) {
            if (d.user.equals(oppname) && d.charstring.equals(oppdeckstring)) {
                index = d;
                break;
            }
        }
        if (index == null) {
            ArrayList<DeckResult> temp = new ArrayList<DeckResult>();
            temp.add(result);
            oppDecks.put(new DeckType(oppname, oppdeckstring), temp);
        } else {
            oppDecks.get(index).add(result);
        }
        stats.push(result);
        ++resultCount;
    }

    public DeckType getDeckType() {
        return deckType;
    }

    public String getDisplayName() {
        return deckType.getDisplayName() + " (" + firstused.get(Calendar.YEAR) + "/" + firstused.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.JAPAN) + "/" + firstused.get(Calendar.DAY_OF_MONTH) + "-" + lastused.get(Calendar.YEAR) + "/" + lastused.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.JAPAN) + "/" + lastused.get(Calendar.DAY_OF_MONTH) + ")";
    }

    public void setDate(File f) {
        Calendar cal = convertToCalendar(f.getName());
        if (cal == null) {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(f.lastModified());
        }
        if (cal.before(firstused)) {
            firstused = cal;
        }
        if (cal.after(lastused)) {
            lastused = cal;
        }
    }

    public static Calendar convertToCalendar(String filename) {
        String[] s = filename.split("_");
        if (s.length != 7) {
            return null;
        }
        Calendar ret = Calendar.getInstance();
        ret.set(Integer.parseInt(s[1]) + 2000, Integer.parseInt(s[2]) - 1, Integer.parseInt(s[3]), Integer.parseInt(s[4]), Integer.parseInt(s[5]) - 1, Integer.parseInt(s[6].substring(0, 2)));
        return ret;
    }

    public Stats getStats() {
        return stats;
    }

    public long getFirstUsed() {
        return firstused.getTimeInMillis();
    }

    public long getLastUsed() {
        return lastused.getTimeInMillis();
    }

    public int compareTo(CADeck o) {
        return -this.stats.size() + o.stats.size();
    }

}
