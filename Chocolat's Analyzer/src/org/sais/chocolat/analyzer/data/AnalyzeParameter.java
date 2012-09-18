package org.sais.chocolat.analyzer.data;

/**
 *
 * @author Romulus
 */
public class AnalyzeParameter {

    public String deckFilter;
    public String deckNameFilter;
    public String oppNameFilter;
    public String oppDeckFilter;
    public boolean writeFile;
    public boolean listReplay;
    public double minSize;
    public String from;
    public String to;
    public String firsts;
    public String lasts;
    public boolean calcCards;
    public boolean listAllCards;
    public boolean useAtelier;
    public boolean addreverse;
    public int startVerIndex;
    public int endVerIndex;

    public AnalyzeParameter(String deckFilter, String deckNameFilter, String oppNameFilter, String oppDeckFilter, boolean writeFile, 
            boolean listReplay, double minSize, String from, String to, String firsts, String lasts, boolean calcCards, boolean listAllCards, boolean useAtelier,
            boolean addreverse, int startVerIndex, int endVerIndex) {
        this.deckFilter = deckFilter;
        this.deckNameFilter = deckNameFilter;
        this.oppNameFilter = oppNameFilter;
        this.oppDeckFilter = oppDeckFilter;
        this.writeFile = writeFile;
        this.listReplay = listReplay;
        this.minSize = minSize;
        this.from = from;
        this.to = to;
        this.firsts = firsts;
        this.lasts = lasts;
        this.calcCards = calcCards;
        this.listAllCards = listAllCards;
        this.useAtelier = useAtelier;
        if (useAtelier) {
            this.listReplay = true;
        }
        this.addreverse = addreverse;
        this.startVerIndex = startVerIndex;
        this.endVerIndex = endVerIndex;
    }

}
