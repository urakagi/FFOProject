/*
 * DeckManager.java
 *
 * Created on 2010/1/28, 下午 04:49:21
 */
package org.sais.fantasyfesta.deck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.sais.chocolat.analyzer.ChocolatsAnalyzer;
import org.sais.chocolat.analyzer.data.AnalyzeParameter;
import org.sais.chocolat.analyzer.data.CADeck;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.cardlabel.UniLabel;
import org.sais.fantasyfesta.core.ReadInnerFile;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.tool.FTool;
import org.sais.fantasyfesta.ui.ListItem;
import org.sais.fantasyfesta.ui.MyCellRenderer;

/**
 *
 * @author Romulus
 */
public class ChocolatsArchive extends javax.swing.JFrame {

    private ChocolatsArchiveCore mCore;
    private IDeckManagerCallback mGame;
    private ArrayList<JCheckBox> mCharFilters = new ArrayList<JCheckBox>();
    private ArrayList<JCheckBox> mTagFilters = new ArrayList<JCheckBox>();
    private ArrayList<DeckInfo> mDisplayedInfos = new ArrayList<DeckInfo>();
    private Deck mDeck = null;
    private File mDeckFile = null;
    private ArrayList<Integer> mDeckList = new ArrayList<Integer>();
    private HashMap<Integer, Integer> mDeckCardAmount = new HashMap<Integer, Integer>();
    private boolean mDisableFilterListener = false;
    private ChocolatsAnalyzer mAnalyzer;
    private boolean mLinked = false;

    public ChocolatsArchive(IDeckManagerCallback game) {
        initComponents();
        this.setIconImage(new ImageIcon(new ReadInnerFile("chocolat.jpg").u).getImage());
        mGame = game;
        init();
        setLocation(FTool.readIntegerConfig("archive_x", 0), FTool.readIntegerConfig("archive_y", 0));
    }

    private void init() {
        String s = FTool.readConfig("replaypath");
        if (s.length() > 0) {
            _replaypath.setText(s);
        }
        s = FTool.readConfig("deckpath");
        if (s.length() > 0) {
            _deckpath.setText(s);
        }
        File root = new File(_deckpath.getText());
        if (!root.exists()) {
            root.mkdirs();
        }

        ListSelectionModel selectionModel = _table.getSelectionModel();
        selectionModel.addListSelectionListener(mSelectionListener);

        _clistsort.add(_clistsortnum);
        _clistsort.add(_clistsorttype);

        _dlistsort.add(_dlist_sortby_editedtime);
        _dlistsort.add(_dlist_sortby_lastused);
        _dlistsort.add(_dlist_sortby_usecount);
        _dlistsort.add(_dlist_sortby_winning_percentage);

        try {
            s = FTool.readConfig("sortby_editedtime");
            if (s.length() > 0) {
                _dlist_sortby_editedtime.setSelected(Boolean.parseBoolean(s));
            }
            s = FTool.readConfig("sortby_lastused");
            if (s.length() > 0) {
                _dlist_sortby_lastused.setSelected(Boolean.parseBoolean(s));
            }
            s = FTool.readConfig("sortby_usecount");
            if (s.length() > 0) {
                _dlist_sortby_usecount.setSelected(Boolean.parseBoolean(s));
            }
            s = FTool.readConfig("sortby_winning_percentage");
            if (s.length() > 0) {
                _dlist_sortby_winning_percentage.setSelected(Boolean.parseBoolean(s));
            }
            s = FTool.readConfig("sort_threshold");
            if (s.length() > 0) {
                _thresholdinwinning.setText(s);
            }
        } catch (Exception e) {
            // Ignore
        }

        _excludetag.addActionListener(mFilterChangedListener);
        mCore = new ChocolatsArchiveCore(root);
        initCharFilters();
        initTagFilters();
        clearDeck();
        applyFilters();
        try {
            _linkanalyzer.setSelected(Boolean.parseBoolean(FTool.readConfig("linkanalyzer")));
        } catch (Exception ex) {
            _linkanalyzer.setSelected(false);
        }
        if (_linkanalyzer.isSelected()) {
            linkAnalyzer();
        }
        if (!FTool.readBooleanConfig("sortbynum", true)) {
            _clistsorttype.setSelected(true);
        }

    }
    ListSelectionListener mSelectionListener = new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            try {
                loadDeckByTableSelection();
            } catch (Exception ex) {
                ex.printStackTrace();
                clearDeck();
                JOptionPane.showMessageDialog(null, "Invalid deck.");
                return;
            }
        }
    };

    private void loadDeckByTableSelection() {
        try {
            if (_table.getSelectedRowCount() == 0) {
                return;
            }
            int row = _table.getSelectedRow();
            mDeckFile = mDisplayedInfos.get(row).getFile();
            mDeck = Deck.Editor.load(mDeckFile);
            mDeckList.clear();
            mDeckCardAmount.clear();
            _deckname.setText(mDeck.getDeckName());
            _tags.setText(mDeck.getTagString());
            int current = -1;
            for (int card : mDeck.characters) {
                if (current == card) {
                    mDeckCardAmount.put(card, mDeckCardAmount.get(card) + 1);
                    continue;
                }
                mDeckList.add(card);
                mDeckCardAmount.put(card, 1);
                current = card;
            }
            for (int card : mDeck.cards) {
                if (current == card) {
                    mDeckCardAmount.put(card, mDeckCardAmount.get(card) + 1);
                    continue;
                }
                mDeckList.add(card);
                mDeckCardAmount.put(card, 1);
                current = card;
            }
            showDeckList();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChocolatsArchive.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex, FTool.getLocale(23), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ChocolatsArchive.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex, FTool.getLocale(23), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initCharFilters() {
        mCharFilters = new ArrayList<JCheckBox>();
        mCharFilters.add(char1);
        mCharFilters.add(char2);
        mCharFilters.add(char3);
        mCharFilters.add(char4);
        mCharFilters.add(char5);
        mCharFilters.add(char6);
        mCharFilters.add(char7);
        mCharFilters.add(char8);
        mCharFilters.add(char9);
        mCharFilters.add(char10);
        mCharFilters.add(char11);
        mCharFilters.add(char12);
        mCharFilters.add(char13);
        mCharFilters.add(char14);
        mCharFilters.add(char15);
        mCharFilters.add(char16);
        mCharFilters.add(char17);
        mCharFilters.add(char18);
        mCharFilters.add(char19);
        mCharFilters.add(char20);
        mCharFilters.add(_char21);
        mCharFilters.add(_char22);
        mCharFilters.add(_char23);
        mCharFilters.add(_char24);
        mCharFilters.add(_char25);
        mCharFilters.add(_char26);
        mCharFilters.add(_char27);
        mCharFilters.add(_char28);
        mCharFilters.add(_char29);
        mCharFilters.add(_char30);
        mCharFilters.add(_char31);
        mCharFilters.add(_char32);
        mCharFilters.add(_char33);
        mCharFilters.add(_char34);
        mCharFilters.add(_char35);
        mCharFilters.add(_char36);
        mCharFilters.add(_char37);
        mCharFilters.add(_char38);

        for (JCheckBox j : mCharFilters) {
            j.addActionListener(mFilterChangedListener);
        }
    }
    private ActionListener mFilterChangedListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!mDisableFilterListener) {
                applyFilters();
            }
        }
    };

    private void initTagFilters() {
        _tagpanel.removeAll();
        Font font = _tagpanel.getFont();
        for (String tag : mCore.getTags()) {
            JCheckBox box = new JCheckBox(tag);
            box.setFont(font);
            box.addActionListener(mFilterChangedListener);
            _tagpanel.add(box);
            mTagFilters.add(box);
        }
    }

    private void applyFilters() {
        _table.clearSelection();
        DefaultTableModel model = (DefaultTableModel) _table.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        mDisplayedInfos.clear();
        for (DeckInfo d : mCore.getInfos()) {
            boolean pass = true;
            for (int i = 0; i < mCharFilters.size(); ++i) {
                if (mCharFilters.get(i).isSelected()) {
                    int c = i + 1;
                    if (!d.containsChar(c)) {
                        pass = false;
                        break;
                    }
                }
            }
            if (!pass) {
                continue;
            }
            for (JCheckBox j : mTagFilters) {
                if (j.isSelected()) {
                    if ((!_excludetag.isSelected() && !d.containsTag(j.getText())) || (_excludetag.isSelected() && d.containsTag(j.getText()))) {
                        pass = false;
                        break;
                    }
                }
            }
            if (!pass) {
                continue;
            }
            if (_lv3more.isSelected()) {
                for (int i = 0; i < mCharFilters.size(); ++i) {
                    if (mCharFilters.get(i).isSelected()) {
                        int c = i + 1;
                        if (d.getCharLV(c) < 3) {
                            pass = false;
                            break;
                        }
                    }
                }
            }
            if (!pass) {
                continue;
            }
            if (_lv2.isSelected()) {
                for (int i = 0; i < mCharFilters.size(); ++i) {
                    if (mCharFilters.get(i).isSelected()) {
                        int c = i + 1;
                        if (d.getCharLV(c) != 2) {
                            pass = false;
                            break;
                        }
                    }
                }
            }
            if (!pass) {
                continue;
            }
            if (mLinked) {
                String deckname = d.getName();
                d.clearCADeck();
                for (CADeck deck : mAnalyzer.getStatsByDeckName()) {
                    String deckstring = deck.getDeckType().charstring;
                    if (deckstring.equals(deckname)) {
                        d.addCADeck(deck);
                    }
                    for (String alias : d.getAliases()) {
                        if (deckstring.equals(alias)) {
                            d.addCADeck(deck);
                            break;
                        }
                    }
                }
            }
            mDisplayedInfos.add(d);
        }

        Collections.sort(mDisplayedInfos, new Comparator<DeckInfo>() {

            @Override
            public int compare(DeckInfo o1, DeckInfo o2) {
                if (_dlist_sortby_editedtime.isSelected()) {
                    return o1.compareTo(o2);
                } else if (_dlist_sortby_lastused.isSelected()) {
                    if (!o1.hasCA()) {
                        return 1;
                    }
                    if (!o2.hasCA()) {
                        return -1;
                    }
                    long c = o2.getCALastUsed() - o1.getCALastUsed();
                    if (c > 0) {
                        return 1;
                    }
                    if (c < 0) {
                        return -1;
                    }
                } else if (_dlist_sortby_usecount.isSelected()) {
                    if (!o1.hasCA()) {
                        return 1;
                    }
                    if (!o2.hasCA()) {
                        return -1;
                    }
                    int ret = o2.getCAUseCount() - o1.getCAUseCount();
                    if (ret != 0) {
                        return ret;
                    }
                } else if (_dlist_sortby_winning_percentage.isSelected()) {
                    if (!o1.hasCA()) {
                        return 1;
                    }
                    if (!o2.hasCA()) {
                        return -1;
                    }
                    int threshold;
                    try {
                        threshold = Integer.parseInt(_thresholdinwinning.getText());
                    } catch (NumberFormatException ex) {
                        threshold = 0;
                    }

                    if (o1.getCAUseCount() < threshold && o2.getCAUseCount() < threshold) {
                        double c = o2.getCAWinningPercentage() - o1.getCAWinningPercentage();
                        if (c > 0) {
                            return 1;
                        }
                        if (c < 0) {
                            return -1;
                        }
                    }
                    if (o1.getCAUseCount() < threshold) {
                        return 1;
                    }
                    if (o2.getCAUseCount() < threshold) {
                        return -1;
                    }
                    double c = o2.getCAWinningPercentage() - o1.getCAWinningPercentage();
                    if (c > 0) {
                        return 1;
                    }
                    if (c < 0) {
                        return -1;
                    }
                    int ret = o2.getCAUseCount() - o1.getCAUseCount();
                    if (ret != 0) {
                        return ret;
                    }
                }
                return o1.compareTo(o2);
            }
        });

        for (DeckInfo d : mDisplayedInfos) {
            if (d.hasCA()) {
                model.addRow(new Object[]{
                            d.getCharConstruct(),
                            d.getName(),
                            d.getLastModifiedString(),
                            d.getUsagePeriodString(),
                            d.getCAWins() + "-" + d.getCALosts() + " (" + mDecimalFormat.format(d.getCAWinningPercentage()) + ")",
                            d.getTagString()
                        });
            } else {
                model.addRow(new Object[]{
                            d.getCharConstruct(),
                            d.getName(),
                            d.getLastModifiedString(),
                            "",
                            "",
                            d.getTagString()
                        });
            }
        }
        _table.setModel(model);
        _amount.setText(mDisplayedInfos.size() + "デッキ");
    }
    private static final DecimalFormat mDecimalFormat = new DecimalFormat("#00.00%");

    private int compareCardnum(Integer c1, Integer c2) {
        if (c1 / 100 == 80) {
            c1 = 17000 + c1 % 10;
        } else if (c1 / 100 == 81) {
            c1 = 19000 + c1 % 10;
        } else {
            c1 *= 10;
        }
        if (c2 / 100 == 80) {
            c2 = 17000 + c2 % 10;
        } else if (c2 / 100 == 81) {
            c2 = 19000 + c2 % 10;
        } else {
            c2 *= 10;
        }
        return c1 - c2;
    }

    private void sortList(ArrayList<Integer> list) {
        if (_clistsorttype.isSelected()) {
            Collections.sort(list, new Comparator<Integer>() {

                @Override
                public int compare(Integer o1, Integer o2) {
                    ECardType c1 = CardDatabase.getInfo(o1).getCardType();
                    ECardType c2 = CardDatabase.getInfo(o2).getCardType();
                    if (c1 != c2) {
                        return c1.ordinal() - c2.ordinal();
                    } else {
                        return compareCardnum(o1, o2);
                    }
                }
            });
            return;
        } else {
            Collections.sort(list, new Comparator<Integer>() {

                @Override
                public int compare(Integer o1, Integer o2) {
                    return compareCardnum(o1, o2);
                }
            });
            return;
        }
    }

    private void floatCharacters() {
        ArrayList<Integer> sortedList = new ArrayList<Integer>();
        for (Integer c : mDeckList) {
            if (c == mDeck.characters[0]) {
                sortedList.add(c);
                break;
            }
        }
        for (Integer c : mDeckList) {
            if (CardDatabase.getInfo(c).isCardType(ECardType.CHARACTER) && c != mDeck.characters[0]) {
                sortedList.add(c);
            }
        }
        for (Integer c : mDeckList) {
            if (!CardDatabase.getInfo(c).isCardType(ECardType.CHARACTER)) {
                sortedList.add(c);
            }
        }
        mDeckList = sortedList;
    }

    private void showDeckList() {
        sortList(mDeckList);
        floatCharacters();
        int spellCnt = 0;
        int totalCnt = 0;
        for (Integer c : mDeckList) {
            if (CardDatabase.getInfo(c).isCardType(ECardType.SPELL)) {
                spellCnt += mDeckCardAmount.get(c);
            }
            if (!CardDatabase.getInfo(c).isCardType(ECardType.CHARACTER)) {
                totalCnt += mDeckCardAmount.get(c);
            }
        }
        _decksab.setText(FTool.getLocale(93) + " " + spellCnt + " " + FTool.getLocale(92) + " / " + FTool.getLocale(94) + " " + totalCnt + " " + FTool.getLocale(92));

        DefaultListModel mod = new DefaultListModel();
        mod.removeAllElements();
        _decklist.setModel(mod);
        _decklist.setCellRenderer(new MyCellRenderer());
        ListItem litem;
        for (Integer c : mDeckList) {
            CardInfo info = CardDatabase.getInfo(c);
            if (info.isCardType(ECardType.CHARACTER)) {
                if (c == mDeck.characters[0]) {
                    litem = new ListItem(new Color(0xFFD0D0), "Leader Lv" + mDeckCardAmount.get(c) + " "
                            + info.getName(), new UniLabel(null, info.createCard()));
                } else {
                    litem = new ListItem(new Color(0xFFD0D0), "Lv" + mDeckCardAmount.get(c) + " "
                            + info.getName(), new UniLabel(null, info.createCard()));
                }
            } else {
                litem = new ListItem(info.getBackgroundColor(), mDeckCardAmount.get(c) + "x "
                        + info.getName(), new UniLabel(null, info.createCard()));
            }
            mod.addElement(litem);
        }
        _decklist.setModel(mod);
    }

    private void clearDeck() {
        DefaultListModel mod = new DefaultListModel();
        mod.removeAllElements();
        _decklist.setModel(mod);
        _decklist.setCellRenderer(new MyCellRenderer());
        _deckname.setText("");
        _tags.setText("");
        mDeckFile = null;
    }

    private void recycle() {
        if (mDeckFile == null) {
            JOptionPane.showMessageDialog(this, "No original deck file", "Error", JOptionPane.OK_OPTION);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, "Really recycle this deck?", "Recycle deck", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            File recy = new File("deck/recycle");
            if (!recy.exists()) {
                recy.mkdirs();
            }
            mDeckFile.renameTo(new File("deck/recycle/" + mDeckFile.getName()));
            clearDeck();
        }
        refresh();
    }

    private void showTextDeck() {
        JTextArea jmessage = new JTextArea("");
        JScrollPane js = new JScrollPane(jmessage);
        js.setPreferredSize(new Dimension(100, 350));

        String message = "";
        for (int i = 0; i < _decklist.getModel().getSize(); ++i) {
            message += (((ListItem) (_decklist.getModel().getElementAt(i))).getValue() + "\r\n");
        }

        jmessage.setText(message);

        JOptionPane.showMessageDialog(this, js, "TEXT FORM DECK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void edit() {
        try {
            new DeckEditor(mDeckFile).setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(ChocolatsArchive.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void play() {
        if (mGame == null) {
            JOptionPane.showMessageDialog(this, "Not launched from a game.");
            return;
        }
        mGame.directLoadDeck(mDeckFile);
        // this.dispose();
    }

    private void refresh() {
        mCore = new ChocolatsArchiveCore(new File(_deckpath.getText()));
        initTagFilters();
        applyFilters();
        clearDeck();
    }

    private void clearFilters() {
        mDisableFilterListener = true;
        for (JCheckBox j : mCharFilters) {
            j.setSelected(false);
        }
        for (JCheckBox j : mTagFilters) {
            j.setSelected(false);
        }
        applyFilters();
        mDisableFilterListener = false;
    }

    private void setDeckName() {
        String deckname = _deckname.getText();
        if (mDeck == null || mDeckFile == null) {
            return;
        }
        mDeck.setDeckName(deckname);
        Deck.Editor.save(mDeckFile, mDeck);
        refresh();
        for (int i = 0; i < _table.getModel().getRowCount(); ++i) {
            if (deckname.equals(_table.getValueAt(i, 1))) {
                _table.changeSelection(i, 1, false, false);
                break;
            }
        }
    }

    private void setDeckTags() {
        String deckname = _deckname.getText();
        if (mDeck == null || mDeckFile == null) {
            return;
        }
        mDeck.setTags(_tags.getText());
        Deck.Editor.save(mDeckFile, mDeck);
        refresh();
        for (int i = 0; i < _table.getModel().getRowCount(); ++i) {
            if (deckname.equals(_table.getValueAt(i, 1))) {
                _table.changeSelection(i, 1, false, false);
                break;
            }
        }
    }

    private void linkAnalyzer() {
        new AnalyzerLinker().execute();
    }

    public interface IDeckManagerCallback {

        void directLoadDeck(File deckFile);
    }

    private class AnalyzerLinker extends SwingWorker<Void, Void> implements ChocolatsAnalyzer.IAnalyzerCallback {

        @Override
        protected Void doInBackground() {
            AnalyzeParameter param = new AnalyzeParameter("", "", _oppnamefilter.getText(), _oppdeckfilter.getText(), false, false, 2.0, _from.getText(), _to.getText(), "", "", false, false, false, false, 0, 0);
            mAnalyzer = new ChocolatsAnalyzer();
            mAnalyzer.setCallback(this);
            mAnalyzer.exec(new String[]{}, param, _replaypath.getText(), false);
            return null;
        }

        @Override
        public void setLinkTotal(int max) {
            _linkprogress.setMinimum(0);
            _linkprogress.setMaximum(max);
            _linkprogress.setValue(0);
            _linkprogress.setStringPainted(true);
            _linkprogress.setString("0 / " + max);
        }

        @Override
        public void setLinkProgress(int progress) {
            if (_linkprogress.getMaximum() == 0) {
                return;
            }
            _linkprogress.setValue(progress);
            _linkprogress.setString(progress + " / " + _linkprogress.getMaximum());
            setProgress(progress / _linkprogress.getMaximum());
        }

        @Override
        protected void done() {
            setLinkProgress(_linkprogress.getMaximum());
            _linkprogress.setForeground(new Color(0x408F20));
            mLinked = true;
            refresh();
        }
    }

    private void saveSortPreference() {
        if (_clistsort.isSelected(_clistsortnum.getModel())) {
            FTool.updateConfig("sortbynum", "true");
        } else {
            FTool.updateConfig("sortbynum", "false");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _clistsort = new javax.swing.ButtonGroup();
        _dlistsort = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        _table = new javax.swing.JTable();
        _chartab = new javax.swing.JTabbedPane();
        _charpanel = new javax.swing.JPanel();
        _padding = new javax.swing.JCheckBox();
        char1 = new javax.swing.JCheckBox();
        char2 = new javax.swing.JCheckBox();
        char3 = new javax.swing.JCheckBox();
        char4 = new javax.swing.JCheckBox();
        char5 = new javax.swing.JCheckBox();
        char6 = new javax.swing.JCheckBox();
        char7 = new javax.swing.JCheckBox();
        char8 = new javax.swing.JCheckBox();
        char9 = new javax.swing.JCheckBox();
        char10 = new javax.swing.JCheckBox();
        char11 = new javax.swing.JCheckBox();
        char12 = new javax.swing.JCheckBox();
        char13 = new javax.swing.JCheckBox();
        char14 = new javax.swing.JCheckBox();
        char15 = new javax.swing.JCheckBox();
        char16 = new javax.swing.JCheckBox();
        char17 = new javax.swing.JCheckBox();
        char18 = new javax.swing.JCheckBox();
        char19 = new javax.swing.JCheckBox();
        char20 = new javax.swing.JCheckBox();
        _char21 = new javax.swing.JCheckBox();
        _char22 = new javax.swing.JCheckBox();
        _char23 = new javax.swing.JCheckBox();
        _char24 = new javax.swing.JCheckBox();
        _char25 = new javax.swing.JCheckBox();
        _char26 = new javax.swing.JCheckBox();
        _char27 = new javax.swing.JCheckBox();
        _char28 = new javax.swing.JCheckBox();
        _char29 = new javax.swing.JCheckBox();
        _char30 = new javax.swing.JCheckBox();
        _char31 = new javax.swing.JCheckBox();
        _char32 = new javax.swing.JCheckBox();
        _char33 = new javax.swing.JCheckBox();
        _char34 = new javax.swing.JCheckBox();
        _char35 = new javax.swing.JCheckBox();
        _char36 = new javax.swing.JCheckBox();
        _char37 = new javax.swing.JCheckBox();
        _char38 = new javax.swing.JCheckBox();
        _tagpanel = new javax.swing.JPanel();
        _analyzer_param_panel = new javax.swing.JPanel();
        _oppnamefilter = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        _oppdeckfilter = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        _from = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        _to = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        _from_v5 = new javax.swing.JButton();
        _relink = new javax.swing.JButton();
        _to_v4 = new javax.swing.JButton();
        _deckfilepanel = new javax.swing.JPanel();
        _deckname = new javax.swing.JTextField();
        _recycle = new javax.swing.JButton();
        _tags = new javax.swing.JTextField();
        _decksab = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        _decklist = new javax.swing.JList();
        _showtextdeck = new javax.swing.JButton();
        _edit = new javax.swing.JButton();
        _play = new javax.swing.JButton();
        _clistsortnum = new javax.swing.JRadioButton();
        _clistsorttype = new javax.swing.JRadioButton();
        jLabel20 = new javax.swing.JLabel();
        _refresh = new javax.swing.JButton();
        _excludetag = new javax.swing.JCheckBox();
        _amount = new javax.swing.JLabel();
        _clearfilter = new javax.swing.JButton();
        _linkanalyzer = new javax.swing.JCheckBox();
        _jlabel1 = new javax.swing.JLabel();
        _replaypath = new javax.swing.JTextField();
        _linkprogress = new javax.swing.JProgressBar();
        _jlabel2 = new javax.swing.JLabel();
        _deckpath = new javax.swing.JTextField();
        _dlist_sortby_editedtime = new javax.swing.JRadioButton();
        _dlist_sortby_usecount = new javax.swing.JRadioButton();
        _dlist_sortby_lastused = new javax.swing.JRadioButton();
        _dlist_sortby_winning_percentage = new javax.swing.JRadioButton();
        _thresholdinwinning = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        _lv3more = new javax.swing.JCheckBox();
        _lv2 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("ショコラズアーカイブ");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        _table.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "構成", "デッキ名", "編集", "使用区間", "勝敗", "タグ"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        _table.setRowHeight(18);
        jScrollPane1.setViewportView(_table);
        _table.getColumnModel().getColumn(0).setPreferredWidth(48);
        _table.getColumnModel().getColumn(2).setPreferredWidth(30);
        _table.getColumnModel().getColumn(4).setPreferredWidth(50);

        _chartab.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N

        _charpanel.setLayout(new java.awt.GridLayout(13, 3));

        _padding.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _padding.setText("レイアウト調整");
        _padding.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _padding.setEnabled(false);
        _padding.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_padding);

        char1.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char1.setForeground(new java.awt.Color(0, 0, 204));
        char1.setText("博麗霊夢");
        char1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char1);

        char2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char2.setForeground(new java.awt.Color(0, 0, 204));
        char2.setText("霧雨魔理沙");
        char2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char2);

        char3.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char3.setForeground(new java.awt.Color(0, 0, 204));
        char3.setText("十六夜咲夜");
        char3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char3);

        char4.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char4.setForeground(new java.awt.Color(0, 0, 204));
        char4.setText("魂魄妖夢");
        char4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char4);

        char5.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char5.setForeground(new java.awt.Color(0, 0, 204));
        char5.setText("八雲紫");
        char5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char5);

        char6.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char6.setForeground(new java.awt.Color(0, 0, 204));
        char6.setText("アリス");
        char6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char6);

        char7.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char7.setForeground(new java.awt.Color(0, 0, 204));
        char7.setText("レミリア");
        char7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char7);

        char8.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char8.setForeground(new java.awt.Color(0, 0, 204));
        char8.setText("西行寺幽々子");
        char8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char8);

        char9.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char9.setForeground(new java.awt.Color(153, 102, 0));
        char9.setText("フランドール");
        char9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char9);

        char10.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char10.setForeground(new java.awt.Color(153, 102, 0));
        char10.setText("パチュリー");
        char10.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char10);

        char11.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char11.setForeground(new java.awt.Color(153, 102, 0));
        char11.setText("紅美鈴");
        char11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char11);

        char12.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char12.setForeground(new java.awt.Color(153, 102, 0));
        char12.setText("蓬莱山輝夜");
        char12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char12);

        char13.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char13.setForeground(new java.awt.Color(153, 102, 0));
        char13.setText("八意永琳");
        char13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char13.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char13);

        char14.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char14.setForeground(new java.awt.Color(153, 102, 0));
        char14.setText("鈴仙");
        char14.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char14.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char14);

        char15.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char15.setForeground(new java.awt.Color(0, 0, 204));
        char15.setText("八雲藍");
        char15.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char15.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char15);

        char16.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char16.setForeground(new java.awt.Color(0, 0, 204));
        char16.setText("橙");
        char16.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char16.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char16);

        char17.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char17.setForeground(new java.awt.Color(0, 0, 204));
        char17.setText("プリバー");
        char17.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char17.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char17);

        char18.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char18.setForeground(new java.awt.Color(0, 0, 204));
        char18.setText("藤原妹紅");
        char18.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char18.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char18);

        char19.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char19.setForeground(new java.awt.Color(0, 0, 204));
        char19.setText("上白沢慧音");
        char19.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char19.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char19);

        char20.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char20.setForeground(new java.awt.Color(0, 0, 204));
        char20.setText("伊吹萃香");
        char20.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char20.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char20);

        _char21.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char21.setForeground(new java.awt.Color(153, 102, 0));
        _char21.setText("洩矢諏訪子");
        _char21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char21.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char21);

        _char22.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char22.setForeground(new java.awt.Color(153, 102, 0));
        _char22.setText("八坂神奈子");
        _char22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char22.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char22ActionPerformed(evt);
            }
        });
        _charpanel.add(_char22);

        _char23.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char23.setForeground(new java.awt.Color(153, 102, 0));
        _char23.setText("東風谷早苗");
        _char23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char23.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char23);

        _char24.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char24.setForeground(new java.awt.Color(153, 102, 0));
        _char24.setText("射命丸文");
        _char24.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char24.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char24);

        _char25.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char25.setForeground(new java.awt.Color(153, 102, 0));
        _char25.setText("小野塚小町");
        _char25.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char25.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char25);

        _char26.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char26.setForeground(new java.awt.Color(153, 102, 0));
        _char26.setText("河城にとり");
        _char26.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char26.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char26);

        _char27.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char27.setForeground(new java.awt.Color(0, 0, 204));
        _char27.setText("比那名居天子");
        _char27.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char27.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char27);

        _char28.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char28.setForeground(new java.awt.Color(0, 0, 204));
        _char28.setText("永江衣玖");
        _char28.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char28.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char28);

        _char29.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char29.setForeground(new java.awt.Color(0, 0, 204));
        _char29.setText("霊烏路空");
        _char29.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char29.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char29);

        _char30.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char30.setForeground(new java.awt.Color(0, 0, 204));
        _char30.setText("火焔猫燐");
        _char30.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char30.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char30ActionPerformed(evt);
            }
        });
        _charpanel.add(_char30);

        _char31.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char31.setForeground(new java.awt.Color(153, 102, 0));
        _char31.setText("古明地こいし");
        _char31.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char31.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char31ActionPerformed(evt);
            }
        });
        _charpanel.add(_char31);

        _char32.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char32.setForeground(new java.awt.Color(153, 102, 0));
        _char32.setText("古明地さとり");
        _char32.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char32.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char32ActionPerformed(evt);
            }
        });
        _charpanel.add(_char32);

        _char33.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char33.setForeground(new java.awt.Color(153, 102, 0));
        _char33.setText("聖白蓮");
        _char33.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char33.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char33ActionPerformed(evt);
            }
        });
        _charpanel.add(_char33);

        _char34.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char34.setForeground(new java.awt.Color(153, 102, 0));
        _char34.setText("寅丸星");
        _char34.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char34.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char34ActionPerformed(evt);
            }
        });
        _charpanel.add(_char34);

        _char35.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char35.setForeground(new java.awt.Color(153, 102, 0));
        _char35.setText("封獣ぬえ");
        _char35.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char35.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char35ActionPerformed(evt);
            }
        });
        _charpanel.add(_char35);

        _char36.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char36.setForeground(new java.awt.Color(153, 102, 0));
        _char36.setText("多々良小傘");
        _char36.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char36.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char36ActionPerformed(evt);
            }
        });
        _charpanel.add(_char36);

        _char37.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char37.setForeground(new java.awt.Color(153, 102, 0));
        _char37.setText("ナズーリン");
        _char37.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char37.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char37ActionPerformed(evt);
            }
        });
        _charpanel.add(_char37);

        _char38.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char38.setForeground(new java.awt.Color(153, 102, 0));
        _char38.setText("村紗水蜜");
        _char38.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char38.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _char38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char38ActionPerformed(evt);
            }
        });
        _charpanel.add(_char38);

        _chartab.addTab("キャラクター", _charpanel);

        _tagpanel.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _chartab.addTab("タグ", _tagpanel);

        _analyzer_param_panel.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N

        _oppnamefilter.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel3.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel3.setText("相手プレイヤー名限定");

        _oppdeckfilter.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel4.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel4.setText("相手デッキ限定");

        _from.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel12.setText("日付 (2009-04-10 形式)");

        _to.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel10.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel10.setText("から");

        jLabel11.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel11.setText("まで");

        _from_v5.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _from_v5.setText("五幕から");
        _from_v5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _from_v5ActionPerformed(evt);
            }
        });

        _relink.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _relink.setText("リリンク");
        _relink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _relinkActionPerformed(evt);
            }
        });

        _to_v4.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _to_v4.setText("四幕まで");
        _to_v4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _to_v4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _analyzer_param_panelLayout = new javax.swing.GroupLayout(_analyzer_param_panel);
        _analyzer_param_panel.setLayout(_analyzer_param_panelLayout);
        _analyzer_param_panelLayout.setHorizontalGroup(
            _analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_analyzer_param_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_analyzer_param_panelLayout.createSequentialGroup()
                        .addComponent(_oppdeckfilter, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(_from_v5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(_to_v4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _analyzer_param_panelLayout.createSequentialGroup()
                        .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(_oppnamefilter, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _analyzer_param_panelLayout.createSequentialGroup()
                                .addComponent(_from, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _analyzer_param_panelLayout.createSequentialGroup()
                                .addComponent(_to, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11))))
                    .addComponent(_relink))
                .addContainerGap())
        );
        _analyzer_param_panelLayout.setVerticalGroup(
            _analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_analyzer_param_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_oppnamefilter, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(_from, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_analyzer_param_panelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_oppdeckfilter, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(_analyzer_param_panelLayout.createSequentialGroup()
                        .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(_to, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(_analyzer_param_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_from_v5)
                            .addComponent(_to_v4))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_relink)
                .addContainerGap())
        );

        _chartab.addTab("ショコライザー絞込", _analyzer_param_panel);

        _deckname.setFont(new java.awt.Font("MS PGothic", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("DeckEditorWindow"); // NOI18N
        _deckname.setText(bundle.getString("DeckName.txt")); // NOI18N
        _deckname.setPreferredSize(new java.awt.Dimension(228, 19));
        _deckname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _decknameActionPerformed(evt);
            }
        });
        _deckname.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _decknameFocusGained(evt);
            }
        });

        _recycle.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _recycle.setText("RECYCLE");
        _recycle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _recycleActionPerformed(evt);
            }
        });

        _tags.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _tags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _tagsActionPerformed(evt);
            }
        });
        _tags.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _tagsFocusGained(evt);
            }
        });

        _decksab.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _decksab.setText("スペカ０枚");

        jLabel21.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        jLabel21.setText("タグ");

        _decklist.setFont(new java.awt.Font("MS PGothic", 0, 14)); // NOI18N
        _decklist.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        _decklist.setVisibleRowCount(40);
        jScrollPane3.setViewportView(_decklist);

        _showtextdeck.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _showtextdeck.setText("TEXT");
        _showtextdeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _showtextdeckActionPerformed(evt);
            }
        });

        _edit.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _edit.setText("EDIT");
        _edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _editActionPerformed(evt);
            }
        });

        _play.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _play.setText("PLAY");
        _play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _playActionPerformed(evt);
            }
        });

        _clistsortnum.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _clistsortnum.setSelected(true);
        _clistsortnum.setText(bundle.getString("SortedByNo.txt")); // NOI18N
        _clistsortnum.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _clistsortnum.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _clistsortnum.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _clistsortnumItemStateChanged(evt);
            }
        });

        _clistsorttype.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _clistsorttype.setText(bundle.getString("SortedByType.txt")); // NOI18N
        _clistsorttype.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _clistsorttype.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _clistsorttype.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _clistsorttypeItemStateChanged(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("DeckEditorWindow").getString("default.font"), 0, 12));
        jLabel20.setText("Sorted by:");

        javax.swing.GroupLayout _deckfilepanelLayout = new javax.swing.GroupLayout(_deckfilepanel);
        _deckfilepanel.setLayout(_deckfilepanelLayout);
        _deckfilepanelLayout.setHorizontalGroup(
            _deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(_deckname, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _deckfilepanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _deckfilepanelLayout.createSequentialGroup()
                        .addComponent(_showtextdeck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_recycle))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _deckfilepanelLayout.createSequentialGroup()
                        .addComponent(_edit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_play))
                    .addComponent(_decksab, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _deckfilepanelLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_tags))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _deckfilepanelLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_clistsortnum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_clistsorttype)))
                .addContainerGap())
            .addComponent(jScrollPane3)
        );
        _deckfilepanelLayout.setVerticalGroup(
            _deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_deckfilepanelLayout.createSequentialGroup()
                .addComponent(_deckname, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(_tags, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(_decksab)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_edit)
                    .addComponent(_play))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_showtextdeck)
                    .addComponent(_recycle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_clistsorttype)
                    .addComponent(jLabel20)
                    .addComponent(_clistsortnum))
                .addGap(52, 52, 52))
        );

        _refresh.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _refresh.setText("REFRESH");
        _refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _refreshActionPerformed(evt);
            }
        });

        _excludetag.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _excludetag.setText("選択したタグを除外");
        _excludetag.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _excludetag.setMargin(new java.awt.Insets(0, 0, 0, 0));

        _amount.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _amount.setText("281デッキ");

        _clearfilter.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _clearfilter.setText("クリア");
        _clearfilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _clearfilterActionPerformed(evt);
            }
        });

        _linkanalyzer.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _linkanalyzer.setText("ショコライザーとリンク");
        _linkanalyzer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _linkanalyzer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _linkanalyzer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _linkanalyzerActionPerformed(evt);
            }
        });

        _jlabel1.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _jlabel1.setText("RPパス");

        _replaypath.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _replaypath.setText("replay");
        _replaypath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _replaypathActionPerformed(evt);
            }
        });
        _replaypath.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _replaypathFocusGained(evt);
            }
        });

        _jlabel2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _jlabel2.setText("デッキパス");

        _deckpath.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _deckpath.setText("deck");
        _deckpath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _deckpathActionPerformed(evt);
            }
        });
        _deckpath.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _deckpathFocusGained(evt);
            }
        });

        _dlist_sortby_editedtime.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _dlist_sortby_editedtime.setSelected(true);
        _dlist_sortby_editedtime.setText("前回編集順");
        _dlist_sortby_editedtime.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _dlist_sortby_editedtime.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _dlist_sortby_editedtime.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _dlist_sortby_editedtimeItemStateChanged(evt);
            }
        });

        _dlist_sortby_usecount.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _dlist_sortby_usecount.setText("使用回数順");
        _dlist_sortby_usecount.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _dlist_sortby_usecount.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _dlist_sortby_usecount.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _dlist_sortby_usecountItemStateChanged(evt);
            }
        });

        _dlist_sortby_lastused.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _dlist_sortby_lastused.setText("前回使用順");
        _dlist_sortby_lastused.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _dlist_sortby_lastused.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _dlist_sortby_lastused.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _dlist_sortby_lastusedItemStateChanged(evt);
            }
        });

        _dlist_sortby_winning_percentage.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _dlist_sortby_winning_percentage.setText("勝率順");
        _dlist_sortby_winning_percentage.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _dlist_sortby_winning_percentage.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _dlist_sortby_winning_percentage.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _dlist_sortby_winning_percentageItemStateChanged(evt);
            }
        });

        _thresholdinwinning.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _thresholdinwinning.setText("10");
        _thresholdinwinning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _thresholdinwinningActionPerformed(evt);
            }
        });
        _thresholdinwinning.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _thresholdinwinningFocusGained(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        jLabel22.setText("ゲーム以上");

        _lv3more.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _lv3more.setText("選択キャラL3+");
        _lv3more.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _lv3more.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _lv3more.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lv3moreActionPerformed(evt);
            }
        });

        _lv2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _lv2.setText("選択キャラL2");
        _lv2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _lv2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _lv2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lv2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_chartab, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_linkanalyzer)
                            .addComponent(_clearfilter)
                            .addComponent(_excludetag, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_amount)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_jlabel1)
                                .addGap(9, 9, 9)
                                .addComponent(_replaypath, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                            .addComponent(_linkprogress, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_jlabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_deckpath, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_dlist_sortby_editedtime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_dlist_sortby_usecount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_dlist_sortby_lastused)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_dlist_sortby_winning_percentage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_thresholdinwinning, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_lv3more)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_lv2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 300, Short.MAX_VALUE)
                        .addComponent(_refresh)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_deckfilepanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_deckfilepanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_lv3more, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_lv2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_refresh))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(_excludetag, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_amount)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(_jlabel2)
                                    .addComponent(_deckpath, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(_linkanalyzer, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_linkprogress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(_jlabel1)
                                    .addComponent(_replaypath, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(8, 8, 8)
                                .addComponent(_clearfilter))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_chartab, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_dlist_sortby_editedtime)
                            .addComponent(_dlist_sortby_usecount)
                            .addComponent(_dlist_sortby_lastused)
                            .addComponent(_dlist_sortby_winning_percentage)
                            .addComponent(_thresholdinwinning, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _char22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char22ActionPerformed
}//GEN-LAST:event__char22ActionPerformed

    private void _decknameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__decknameFocusGained
        _deckname.setSelectionStart(0);
        _deckname.setSelectionEnd(_deckname.getText().length());
}//GEN-LAST:event__decknameFocusGained
    private void _recycleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__recycleActionPerformed
        recycle();
}//GEN-LAST:event__recycleActionPerformed
    private void _tagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__tagsActionPerformed
        setDeckTags();
}//GEN-LAST:event__tagsActionPerformed
    private void _tagsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__tagsFocusGained
        // TODO add your handling code here:
}//GEN-LAST:event__tagsFocusGained

    private void _showtextdeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__showtextdeckActionPerformed
        showTextDeck();
}//GEN-LAST:event__showtextdeckActionPerformed
    private void _editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__editActionPerformed
        edit();
    }//GEN-LAST:event__editActionPerformed
    private void _playActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__playActionPerformed
        play();
    }//GEN-LAST:event__playActionPerformed

    private void _clistsortnumItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__clistsortnumItemStateChanged
        /*applyFilters();
        showFilteredList(flist, flistcnt);
        showDeckList(-1);*/
}//GEN-LAST:event__clistsortnumItemStateChanged

    private void _clistsorttypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__clistsorttypeItemStateChanged
        showDeckList();
        saveSortPreference();
}//GEN-LAST:event__clistsorttypeItemStateChanged
    private void _refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__refreshActionPerformed
        refresh();
    }//GEN-LAST:event__refreshActionPerformed
    private void _clearfilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__clearfilterActionPerformed
        clearFilters();
    }//GEN-LAST:event__clearfilterActionPerformed
    private void _decknameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__decknameActionPerformed
        setDeckName();
    }//GEN-LAST:event__decknameActionPerformed
    private void _linkanalyzerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__linkanalyzerActionPerformed
        FTool.updateConfig("linkanalyzer", String.valueOf(_linkanalyzer.isSelected()));


        if (_linkanalyzer.isSelected()) {
            linkAnalyzer();


        }
    }//GEN-LAST:event__linkanalyzerActionPerformed

    private void _replaypathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__replaypathActionPerformed
        FTool.updateConfig("replaypath", String.valueOf(_replaypath.getText()));
    }//GEN-LAST:event__replaypathActionPerformed

    private void _replaypathFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__replaypathFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event__replaypathFocusGained

    private void _deckpathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__deckpathActionPerformed
        FTool.updateConfig("deckpath", String.valueOf(_deckpath.getText()));
        refresh();
    }//GEN-LAST:event__deckpathActionPerformed

    private void _deckpathFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__deckpathFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event__deckpathFocusGained

    private void _dlist_sortby_editedtimeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__dlist_sortby_editedtimeItemStateChanged
        FTool.updateConfig("sortby_editedtime", String.valueOf(_dlist_sortby_editedtime.isSelected()));
        refresh();
    }//GEN-LAST:event__dlist_sortby_editedtimeItemStateChanged

    private void _dlist_sortby_usecountItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__dlist_sortby_usecountItemStateChanged
        FTool.updateConfig("sortby_usecount", String.valueOf(_dlist_sortby_usecount.isSelected()));
        refresh();
    }//GEN-LAST:event__dlist_sortby_usecountItemStateChanged

    private void _dlist_sortby_lastusedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__dlist_sortby_lastusedItemStateChanged
        FTool.updateConfig("sortby_lastused", String.valueOf(_dlist_sortby_lastused.isSelected()));
        refresh();
    }//GEN-LAST:event__dlist_sortby_lastusedItemStateChanged

    private void _dlist_sortby_winning_percentageItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__dlist_sortby_winning_percentageItemStateChanged
        FTool.updateConfig("sortby_winning_percentage", String.valueOf(_dlist_sortby_winning_percentage.isSelected()));
        refresh();
    }//GEN-LAST:event__dlist_sortby_winning_percentageItemStateChanged

    private void _thresholdinwinningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__thresholdinwinningActionPerformed
        FTool.updateConfig("sort_threshold", _thresholdinwinning.getText());
        refresh();
    }//GEN-LAST:event__thresholdinwinningActionPerformed
    private void _thresholdinwinningFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__thresholdinwinningFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event__thresholdinwinningFocusGained

    private void _from_v5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__from_v5ActionPerformed
        _from.setText("2009-08-15");
    }//GEN-LAST:event__from_v5ActionPerformed
    private void _to_v4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__to_v4ActionPerformed
        _to.setText("2009-08-14");
    }//GEN-LAST:event__to_v4ActionPerformed
    private void _relinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__relinkActionPerformed
        linkAnalyzer();
    }//GEN-LAST:event__relinkActionPerformed
    private void _char30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char30ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char30ActionPerformed

    private void _lv3moreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lv3moreActionPerformed
        applyFilters();
    }//GEN-LAST:event__lv3moreActionPerformed
    private void _lv2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lv2ActionPerformed
        applyFilters();
    }//GEN-LAST:event__lv2ActionPerformed
    private Timer mWriteLocationTimer;

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        final int x = evt.getComponent().getX();
        final int y = evt.getComponent().getY();
        if (mWriteLocationTimer == null) {
            mWriteLocationTimer = new Timer();
        } else {
            mWriteLocationTimer.cancel();
            mWriteLocationTimer = new Timer();
        }
        mWriteLocationTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                FTool.updateConfig("archive_x", String.valueOf(x));
                FTool.updateConfig("archive_y", String.valueOf(y));
            }
        }, 1300);
    }//GEN-LAST:event_formComponentMoved

    private void _char31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char31ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char31ActionPerformed

    private void _char32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char32ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char32ActionPerformed

    private void _char33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char33ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char33ActionPerformed

    private void _char34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char34ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char34ActionPerformed

    private void _char35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char35ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char35ActionPerformed

    private void _char36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char36ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char36ActionPerformed

    private void _char37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char37ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char37ActionPerformed

    private void _char38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char38ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__char38ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel _amount;
    private javax.swing.JPanel _analyzer_param_panel;
    private javax.swing.JCheckBox _char21;
    private javax.swing.JCheckBox _char22;
    private javax.swing.JCheckBox _char23;
    private javax.swing.JCheckBox _char24;
    private javax.swing.JCheckBox _char25;
    private javax.swing.JCheckBox _char26;
    private javax.swing.JCheckBox _char27;
    private javax.swing.JCheckBox _char28;
    private javax.swing.JCheckBox _char29;
    private javax.swing.JCheckBox _char30;
    private javax.swing.JCheckBox _char31;
    private javax.swing.JCheckBox _char32;
    private javax.swing.JCheckBox _char33;
    private javax.swing.JCheckBox _char34;
    private javax.swing.JCheckBox _char35;
    private javax.swing.JCheckBox _char36;
    private javax.swing.JCheckBox _char37;
    private javax.swing.JCheckBox _char38;
    private javax.swing.JPanel _charpanel;
    private javax.swing.JTabbedPane _chartab;
    private javax.swing.JButton _clearfilter;
    private javax.swing.ButtonGroup _clistsort;
    private javax.swing.JRadioButton _clistsortnum;
    private javax.swing.JRadioButton _clistsorttype;
    private javax.swing.JPanel _deckfilepanel;
    private javax.swing.JList _decklist;
    private javax.swing.JTextField _deckname;
    private javax.swing.JTextField _deckpath;
    private javax.swing.JLabel _decksab;
    private javax.swing.JRadioButton _dlist_sortby_editedtime;
    private javax.swing.JRadioButton _dlist_sortby_lastused;
    private javax.swing.JRadioButton _dlist_sortby_usecount;
    private javax.swing.JRadioButton _dlist_sortby_winning_percentage;
    private javax.swing.ButtonGroup _dlistsort;
    private javax.swing.JButton _edit;
    private javax.swing.JCheckBox _excludetag;
    private javax.swing.JTextField _from;
    private javax.swing.JButton _from_v5;
    private javax.swing.JLabel _jlabel1;
    private javax.swing.JLabel _jlabel2;
    private javax.swing.JCheckBox _linkanalyzer;
    private javax.swing.JProgressBar _linkprogress;
    private javax.swing.JCheckBox _lv2;
    private javax.swing.JCheckBox _lv3more;
    private javax.swing.JTextField _oppdeckfilter;
    private javax.swing.JTextField _oppnamefilter;
    private javax.swing.JCheckBox _padding;
    private javax.swing.JButton _play;
    private javax.swing.JButton _recycle;
    private javax.swing.JButton _refresh;
    private javax.swing.JButton _relink;
    private javax.swing.JTextField _replaypath;
    private javax.swing.JButton _showtextdeck;
    private javax.swing.JTable _table;
    private javax.swing.JPanel _tagpanel;
    private javax.swing.JTextField _tags;
    private javax.swing.JTextField _thresholdinwinning;
    private javax.swing.JTextField _to;
    private javax.swing.JButton _to_v4;
    private javax.swing.JCheckBox char1;
    private javax.swing.JCheckBox char10;
    private javax.swing.JCheckBox char11;
    private javax.swing.JCheckBox char12;
    private javax.swing.JCheckBox char13;
    private javax.swing.JCheckBox char14;
    private javax.swing.JCheckBox char15;
    private javax.swing.JCheckBox char16;
    private javax.swing.JCheckBox char17;
    private javax.swing.JCheckBox char18;
    private javax.swing.JCheckBox char19;
    private javax.swing.JCheckBox char2;
    private javax.swing.JCheckBox char20;
    private javax.swing.JCheckBox char3;
    private javax.swing.JCheckBox char4;
    private javax.swing.JCheckBox char5;
    private javax.swing.JCheckBox char6;
    private javax.swing.JCheckBox char7;
    private javax.swing.JCheckBox char8;
    private javax.swing.JCheckBox char9;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables
}
