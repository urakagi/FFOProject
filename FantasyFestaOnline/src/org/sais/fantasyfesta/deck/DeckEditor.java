package org.sais.fantasyfesta.deck;
/*
 * DeckEditorWindow.java
 *
 * Created on 2007/2/7 10:38
 */

import org.sais.fantasyfesta.core.*;
import org.sais.fantasyfesta.ui.CardViewer;
import org.sais.fantasyfesta.ui.MyCellRenderer;
import org.sais.fantasyfesta.ui.ListItem;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.EventCardInfo;
import org.sais.fantasyfesta.card.SpellCardInfo;
import org.sais.fantasyfesta.card.SupportCardInfo;
import org.sais.fantasyfesta.card.cardlabel.CardImageSetter;
import org.sais.fantasyfesta.tool.FTool;
import org.sais.fantasyfesta.card.cardlabel.UniLabel;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CharacterCardInfo;

/**
 *
 * @author  Romulus
 */
public class DeckEditor extends javax.swing.JFrame {

    private JCheckBox[] chars, kinds;
    private final int mCharacterCount = 40; /* Incuding lv0 and cowork and original */

    private final int OCHAR_BEGIN_INDEX = 40;
    private Deck mDeck = new Deck();
    private ArrayList<CardInfo> mCardList = new ArrayList<CardInfo>();
    private ArrayList<CardInfo> mDeckList = new ArrayList<CardInfo>();
    private HashMap<CardInfo, Integer> mDeckCardAmount = new HashMap<CardInfo, Integer>();
    private CardInfo mLeader = CharacterCardInfo.newNull();
    private MouseListener clistClickAdapter;
    private MouseListener decklistClickAdapter;
    private String mDeckPath = null;
    private boolean disableEvent = false;
    /**
     * The map of chars[x] to card number yy00 characters
     */
    private static final int[] ORDER_ID_MAP = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, CardInfo.ID_COWORK};

    public DeckEditor() {
        initComponents();
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(new ReadInnerFile("deckeditoricon.jpg").u));
        initilize();
        loadList();
        if (!FTool.readConfig("originalcard").equals("")) {
            _veroriginal.setSelected(Boolean.parseBoolean(FTool.readConfig("showoriginalcards")));
        }
        if (FTool.readBooleanConfig("sortbynum", true)) {
            applyFilters();
        } else {
            // This will call applyFilters();
            _clistsorttype.setSelected(true);
        }

        showDeckList(-1);
        _amount.setText(_cardlist.getModel().getSize() + FTool.getLocale(92));
        if (!FTool.readConfig("customizedicon").equals("")) {
            CardImageSetter.sUseCardImage = Boolean.parseBoolean(FTool.readConfig("customizedicon"));
            FTool.deckeditorcardviewer = new CardViewer();
            FTool.deckeditorcardviewer.setFocusableWindowState(false);
        }
        if (!FTool.readConfig("lclickshowpic").equals("")) {
            _leftclickshowcustomizedicon4.setSelected(Boolean.parseBoolean(FTool.readConfig("lclickshowpic")));
        }

        setLocation(FTool.readIntegerConfig("deckeditor_x", 0), FTool.readIntegerConfig("deckeditor_y", 0));
    }

    public DeckEditor(File deckFile) {
        this();
        if (deckFile != null) {
            loadDeck(deckFile);
        }
    }

    private void initilize() {
        chars = new JCheckBox[mCharacterCount];
        chars[0] = char0;
        chars[1] = char1;
        chars[2] = char2;
        chars[3] = char3;
        chars[4] = char4;
        chars[5] = char5;
        chars[6] = char6;
        chars[7] = char7;
        chars[8] = char8;
        chars[9] = char9;
        chars[10] = char10;
        chars[11] = char11;
        chars[12] = char12;
        chars[13] = char13;
        chars[14] = char14;
        chars[15] = char15;
        chars[16] = char16;
        chars[17] = char17;
        chars[18] = char18;
        chars[19] = char19;
        chars[20] = char20;
        chars[21] = _char21;
        chars[22] = _char22;
        chars[23] = _char23;
        chars[24] = _char24;
        chars[25] = _char25;
        chars[26] = _char26;
        chars[27] = _char27;
        chars[28] = _char28;
        chars[29] = _char29;
        chars[30] = _char30;
        chars[31] = _char31;
        chars[32] = _char32;
        chars[33] = _char33;
        chars[34] = _char34;
        chars[35] = _char35;
        chars[36] = _char36;
        chars[37] = _char37;
        chars[38] = _char38;
        /*
        if (!FTool.readConfig("originalcard").equals("true")) {
            _veroriginal.setSelected(false);
            _veroriginal.setVisible(false);
            _chartab.remove(1);
            for (int i = OCHAR_BEGIN_INDEX; i <= mCharacterCount - 2; ++i) {
                chars[i].setSelected(false);
                chars[i].setVisible(false);
            }
        }*/
        chars[mCharacterCount - 1] = char90;
        for (int i = 0; i < mCharacterCount; ++i) {
            chars[i].addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    applyFilters();
                }
            });
        }
        kinds = new JCheckBox[4];
        kinds[0] = kind0;
        kinds[1] = kind1;
        kinds[2] = kind2;
        kinds[3] = kind3;
        for (int i = 0; i < 4; ++i) {
            kinds[i].addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    applyFilters();
                }
            });
        }
        _clistsort.add(_clistsortnum);
        _clistsort.add(_clistsorttype);
    }

    void showCard(UniLabel label) {   //Translate flaged text into colored text
        FTool.showCard(label.getInfo(), SCArea);
    }

    private void loadList() {
        DefaultListModel mod = new DefaultListModel();
        Collection<CardInfo> cards = CardDatabase.listCards();
        ListItem[] litem = new ListItem[cards.size()];
        mod.removeAllElements();
        _cardlist.setModel(mod);
        _cardlist.setCellRenderer(new MyCellRenderer());
        int i = 0;
        for (CardInfo info : cards) {
            litem[i] = new ListItem(info.getBackgroundColor(), info.getName(), new UniLabel(null, info.createCard()));
            mod.addElement(litem[i]);
            ++i;
        }
        _cardlist.setModel(mod);
    }

    private void showFilteredList(ArrayList<CardInfo> list) {
        MyListModel mod = new MyListModel();
        ListItem litem;
        mod.removeAllElements();
        _cardlist.setModel(mod);
        _cardlist.setCellRenderer(new MyCellRenderer());
        ListSelectionListener[] listeners = _cardlist.getListSelectionListeners();
        for (int k = 0; k < listeners.length; ++k) {
            _cardlist.removeListSelectionListener(listeners[k]);
        }
        for (CardInfo info : list) {
            if (info.isNull()) {
                continue;
            }
            Card card = info.createCard();
            UniLabel label = new UniLabel(null, card);
            litem = new ListItem(info.getBackgroundColor(), info.getName(), label);
            mod.addElementwithoutFire(litem);
        }


        mod.fire();
        for (int k = 0; k < listeners.length; ++k) {
            _cardlist.addListSelectionListener(listeners[k]);
        }
        _cardlist.setModel(mod);
        MouseListener[] ms = _cardlist.getMouseListeners();
        for (int i = 0; i < ms.length; ++i) {
            if (ms[i].equals(clistClickAdapter)) {
                _cardlist.removeMouseListener(ms[i]);
            }
        }
        clistClickAdapter = new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispatchShowCard(_cardlist, evt);
                if (_cardlist.locationToIndex(evt.getPoint()) >= 0) {
                    synchronizeList(0);
                }
                if (evt.getClickCount() >= 2) {
                    addtoDeck(1);
                }
            }
        };
        _cardlist.addMouseListener(clistClickAdapter);
    }

    void dispatchShowCard(JList list, MouseEvent evt) {
        int index = list.locationToIndex(evt.getPoint());
        if (index < 0) {
            return;
        }
        UniLabel label = ((ListItem) (list.getModel().getElementAt(index))).label;
        showCard(label);
        if (CardImageSetter.sUseCardImage && (evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown())) {
            FTool.deckeditorcardviewer.show(label.getInfo());
        }
    }

    void showSelected(JList list) {
        int index = list.getSelectedIndex();
        UniLabel label = ((ListItem) (list.getModel().getElementAt(index))).label;
        if (index >= 0) {
            showCard(label);
            if (CardImageSetter.sUseCardImage) {
                if (_leftclickshowcustomizedicon4.isSelected()) {
                    FTool.deckeditorcardviewer.show(label.getInfo());
                }
            }
        }
    }

    private void showDeckList(int setindex) {
        sortList(mDeckList);
        floatCharacters();
        int spellCnt = 0;
        int totalCnt = 0;
        for (CardInfo info : mDeckList) {
            if (info.isCardType(ECardType.SPELL)) {
                spellCnt += mDeckCardAmount.get(info);
            }
            if (!info.isCharacterCard()) {
                totalCnt += mDeckCardAmount.get(info);
            }
        }
        _decksab.setText(FTool.getLocale(93) + " " + spellCnt + " " + FTool.getLocale(92) + " / " + FTool.getLocale(94) + " " + totalCnt + " " + FTool.getLocale(92));

        DefaultListModel mod = new DefaultListModel();
        mod.removeAllElements();
        _decklist.setModel(mod);
        _decklist.setCellRenderer(new MyCellRenderer());
        ListItem litem;
        for (CardInfo info : mDeckList) {
            if (info.isCharacterCard()) {
                if (info.getCardNo() == mLeader.getCardNo()) {
                    litem = new ListItem(new Color(0xFFD0D0), "Leader Lv" + mDeckCardAmount.get(info) + " "
                            + info.getName(), new UniLabel(null, info.createCard()));
                } else {
                    litem = new ListItem(new Color(0xFFD0D0), "Lv" + mDeckCardAmount.get(info) + " "
                            + info.getName(), new UniLabel(null, info.createCard()));
                }
            } else {
                litem = new ListItem(info.getBackgroundColor(), mDeckCardAmount.get(info) + "x "
                        + info.getName(), new UniLabel(null, info.createCard()));
            }
            mod.addElement(litem);
        }
        _decklist.setModel(mod);
        MouseListener[] ms = _decklist.getMouseListeners();
        for (int i = 0; i < ms.length; ++i) {
            if (ms[i].equals(decklistClickAdapter)) {
                _decklist.removeMouseListener(ms[i]);
            }
        }
        decklistClickAdapter = new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispatchShowCard(_decklist, evt);
                if (_decklist.locationToIndex(evt.getPoint()) >= 0) {
                    synchronizeList(1);
                }
                if (evt.getClickCount() >= 2) {
                    removefromDeck(1);
                }
            }
        };
        _decklist.addMouseListener(decklistClickAdapter);
        if (setindex >= 0) {
            _decklist.setSelectedIndex(setindex);
        }
    }

    private void synchronizeList(int acter) {   //0:CardList 1:DeckList
        JList act;
        JList sync;
        ArrayList<CardInfo> actlist;
        ArrayList<CardInfo> synclist;
        if (acter == 0) {
            act = _cardlist;
            actlist = mCardList;
            sync = _decklist;
            synclist = mDeckList;
        } else {
            act = _decklist;
            actlist = mDeckList;
            sync = _cardlist;
            synclist = mCardList;
        }
        if (act.getSelectedIndex() < 0) {
            return;
        }
        int index = -1;
        for (int i = 0; i < synclist.size(); ++i) {
            if (synclist.get(i).equals(actlist.get(act.getSelectedIndex()))) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            sync.setSelectedIndex(index);
            sync.ensureIndexIsVisible(index);
        }
    }

    private void applyFilters() {
        if (disableEvent) {
            return;
        }
        //Order matters! Always put applyCharFilter() at first!
        initCardListWithCharFilter();
        if (_fitcharlv.isSelected()) {
            applyCharLVFilter();
        }
        applyKindFilter();
        applySpecifiedFilter();
        applyAdvancedFilter();
        applyVersionFilter();

        sortList(mCardList);
        showFilteredList(mCardList);
        _amount.setText(_cardlist.getModel().getSize() + FTool.getLocale(92));
    }

    //Start of Filters
    private void initCardListWithCharFilter() {
        mCardList = new ArrayList<CardInfo>();
        for (int i = 0; i < mCharacterCount; ++i) {
            if (chars[i].isSelected()) {
                mCardList.addAll(getCharCardList(ORDER_ID_MAP[i]));
                if (!chars[mCharacterCount - 1].isSelected() && !_fitcharlv.isSelected() && i > 0) {
                    mCardList.addAll(getCharCoworkCardList(CardInfo.CHAR_NAMES[i - 1], mCardList));
                }
            }
        }
    }

    private Collection<CardInfo> getCharCardList(int id) {
        Collection<CardInfo> ret = new HashSet<CardInfo>();
        if (id == 17) {
            ret.add(CardDatabase.getInfo(8000));
            ret.add(CardDatabase.getInfo(8001));
            ret.add(CardDatabase.getInfo(8002));
        }
        if (id == 19) {
            ret.add(CardDatabase.getInfo(8100));
            ret.add(CardDatabase.getInfo(8101));
        }
        for (int c = id * 100; c < id * 100 + 99; ++c) {
            if (CardDatabase.getInfo(c)
                    == null) {
                continue;
            } else {
                ret.add(CardDatabase.getInfo(c));
            }
        }
        if (id == CardInfo.ID_COWORK) {
            // Cowork Card, 90xx and 91xx
            for (int c = 9001; c <= 9199; ++c) {
                if (CardDatabase.getInfo(c) == null) {
                    continue;
                } else {
                    ret.add(CardDatabase.getInfo(c));
                }
            }
            //Original Cowork Card
            for (int c = 10101; c <= 10199; ++c) {
                if (CardDatabase.getInfo(c) == null) {
                    continue;
                } else {
                    ret.add(CardDatabase.getInfo(c));
                }
            }
        }
        return ret;
    }

    private Collection<CardInfo> getCharCoworkCardList(String charName, ArrayList<CardInfo> list) {
        Collection<CardInfo> ret = new HashSet<CardInfo>();

        for (int c = 9001; c < 9299; ++c) {
            CardInfo info = CardDatabase.getInfo(c);
            if (info == null) {
                continue;
            }
            if (info.getCharacterRequirement().contains(charName)) {
                if (!list.contains(info)) {
                    ret.add(info);
                }
            }
        }

        return ret;
    }

    private void applyKindFilter() {
        ArrayList<CardInfo> filteredList;
        filteredList = new ArrayList<CardInfo>();
        for (CardInfo info : mCardList) {
            if (info.isNull()) {
                continue;
            }
            if (kinds[info.getCardType().ordinal()].isSelected()) {
                filteredList.add(info);
            }
        }
        mCardList = filteredList;
    }

    private void applySpecifiedFilter() {
        ArrayList<CardInfo> filteredList = new ArrayList<CardInfo>();

        //Card Number
        try {
            int no = FTool.safeParseInt(_cardnum.getText());
            if (!CardDatabase.getInfo(no).isNull()) {
                mCardList.clear();
                mCardList.add(CardDatabase.getInfo(no));
                return;
            }
        } catch (NumberFormatException ex) {
            _cardnum.setText("");
        }

        //Card Name
        if (!_cardname.getText().equals("")) {
            filteredList = new ArrayList<CardInfo>();
            for (CardInfo info : mCardList) {
                if (info.getName().contains(_cardname.getText())) {
                    filteredList.add(info);
                }
            }
            mCardList = filteredList;
        }

        //Card Text
        applyCardTextSearch();

        //MP
        if (!(_mpmax.getText().equals("") && _mpmin.getText().equals(""))) {
            filteredList = new ArrayList<CardInfo>();
            try {
                int min = FTool.safeParseInt(_mpmin.getText());
                int max = FTool.safeParseInt(_mpmax.getText());

                if (max == 0 && min != 0) {
                    max = 99;
                }

                for (CardInfo info : mCardList) {
                    if (info.getSpellPointRequirement() >= min && info.getSpellPointRequirement() <= max) {
                        filteredList.add(info);
                    }
                }

                mCardList = filteredList;
            } catch (NumberFormatException ex) {
                _mpmin.setText("");
                _mpmax.setText("");
            }
        }

        //Level
        if (!(_lvmax.getText().equals("") && _lvmin.getText().equals(""))) {
            filteredList = new ArrayList<CardInfo>();
            try {
                int min = FTool.safeParseInt(_lvmin.getText());
                int max = FTool.safeParseInt(_lvmax.getText());
                if (max == 0 && min != 0) {
                    max = 99;
                }

                for (CardInfo info : mCardList) {
                    if (info.getLevelRequirement() >= min && info.getLevelRequirement() <= max) {
                        filteredList.add(info);
                    }
                }
                mCardList = filteredList;
            } catch (NumberFormatException ex) {
                _lvmin.setText("");
                _lvmax.setText("");
            }
        }
    }

    private void applyCardTextSearch() {
        if (!_searchtext.getText().equals("")) {
            ArrayList<CardInfo> filteredList = new ArrayList<CardInfo>();
            String text;

            if (_useregexp.isSelected()) {
                String regexp = _searchtext.getText();
                for (CardInfo info : mCardList) {
                    text = _usecolorsymbol.isSelected()
                            ? info.getRuleText()
                            : info.getTextWithoutColorSymbolAndNewLine();
                    if (text.split(regexp).length > 1) {
                        filteredList.add(info);
                    }
                }
            } else {
                String condition = _searchtext.getText().replace("　", " ");
                String[] terms = condition.split(" ");
                for (CardInfo info : mCardList) {
                    boolean match = true;
                    text = _usecolorsymbol.isSelected()
                            ? info.getRuleText()
                            : info.getTextWithoutColorSymbolAndNewLine();
                    for (String term : terms) {
                        if (term.contains("|")) {
                            // OR term
                            match = false;
                            for (String or_term : term.split("\\|")) {
                                if (or_term.startsWith("-")) {
                                    if (!text.contains(or_term.substring(1))) {
                                        match = true;
                                        break;
                                    }
                                } else {
                                    if (text.contains(or_term)) {
                                        match = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (term.startsWith("-")) {
                                if (text.contains(term.substring(1))) {
                                    match = false;
                                    break;
                                }
                            } else {
                                if (!text.contains(term)) {
                                    match = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (match) {
                        filteredList.add(info);
                    }
                }
            }

            mCardList = filteredList;
        }
    }

    private void showCardTextSearchHelp() {
        JOptionPane.showMessageDialog(null, FTool.getLocale(231).replace("\\n", "\n"));
    }

    private void applyAdvancedFilter() {
        ArrayList<CardInfo> filteredList;
        int[] sc = getAdvFilters();

        filteredList = new ArrayList<CardInfo>();
        for (CardInfo info : mCardList) {
            boolean match = true;
            switch (info.getCardType()) {
                case SPELL:
                    SpellCardInfo spellInfo = (SpellCardInfo) info;
                    //Attack
                    if (!(sc[0] != 0 && sc[1] == 99)) {
                        if (spellInfo.getAttackValue() < sc[0] || spellInfo.getAttackValue() > sc[1]) {
                            match = false;
                            break;
                        }
                    }

                    //Intercept
                    if (!(sc[2] == 0 && sc[3] == 99)) {
                        if (spellInfo.getInterceptValue() < sc[2] || spellInfo.getInterceptValue() > sc[3]) {
                            match = false;
                            break;
                        }
                    }

                    //Hit
                    if (!(sc[4] == 0 && sc[5] == 99)) {
                        if (spellInfo.getHitValue() < sc[4] || spellInfo.getHitValue() > sc[5]) {
                            match = false;
                            break;
                        }
                    }

                    //Bullet Type
                    if (sc[6 + spellInfo.getBulletType().ordinal()] == 0) {
                        match = false;
                        break;
                    }

                    break;
                case SUPPORT:
                    if (sc[9 + ((SupportCardInfo) info).getSupportType().ordinal()] == 0) {
                        match = false;
                        break;
                    }
                    break;
                case EVENT:
                    if (sc[12 + ((EventCardInfo) info).getTiming().ordinal()] == 0) {
                        match = false;
                        break;
                    }
                    break;
                default:
                    break;
            }
            if (match) {
                filteredList.add(info);
            }
        }
        mCardList = filteredList;
    }

    private void sortList(ArrayList<CardInfo> list) {
        if (_clistsorttype.isSelected()) {
            Collections.sort(list, new Comparator<CardInfo>() {

                @Override
                public int compare(CardInfo o1, CardInfo o2) {
                    if (o1.getCardType() != o2.getCardType()) {
                        return o1.getCardType().ordinal() - o2.getCardType().ordinal();
                    } else {
                        return compareCardnum(o1, o2);
                    }
                }
            });
            return;
        } else {
            Collections.sort(list, new Comparator<CardInfo>() {

                @Override
                public int compare(CardInfo o1, CardInfo o2) {
                    return compareCardnum(o1, o2);
                }
            });
            return;
        }
    }

    private int compareCardnum(CardInfo o1, CardInfo o2) {
        int c1 = o1.getCardNo();
        int c2 = o2.getCardNo();
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

    private void addtoDeck(int amount) {
        if (_cardlist.getSelectedIndex() < 0) {
            return;
        }
        CardInfo info = mCardList.get(_cardlist.getSelectedIndex());
        if (!mDeckList.contains(info)) {
            mDeckList.add(info);
        }
        if (mDeckCardAmount.containsKey(info)) {
            int nowAmount = mDeckCardAmount.get(info);
            int max = info.isCharacterCard() ? 4 : 3;
            mDeckCardAmount.put(info, nowAmount + amount > max ? max : nowAmount + amount);
        } else {
            mDeckCardAmount.put(info, amount);
        }
        showDeckList(-1);
        synchronizeList(0);
    }

    private void removefromDeck(int amount) {
        int index = _decklist.getSelectedIndex();
        if (index < 0) {
            return;
        }
        CardInfo info = mDeckList.get(index);
        int nowAmount = mDeckCardAmount.get(info);
        mDeckCardAmount.put(info, nowAmount + amount < 0 ? 0 : nowAmount - amount);
        if (nowAmount <= amount) {
            mDeckList.remove(info);
            mDeckCardAmount.remove(info);
        }

        showDeckList(index);
        synchronizeList(1);
    }

    private void floatCharacters() {
        ArrayList<CardInfo> sortedList = new ArrayList<CardInfo>();
        for (CardInfo info : mDeckList) {
            if (info.equals(mLeader)) {
                sortedList.add(info);
                break;
            }
        }
        for (CardInfo info : mDeckList) {
            if (info.isCharacterCard() && !info.equals(mLeader)) {
                sortedList.add(info);
            }
        }
        for (CardInfo info : mDeckList) {
            if (!info.isCharacterCard()) {
                sortedList.add(info);
            }
        }
        mDeckList = sortedList;
    }

    private void newDeck() {
        mLeader = CharacterCardInfo.newNull();
        mDeckCardAmount.clear();
        mDeckList.clear();
        _deckname.setText("New Deck");
        _tags.setText("");
        mDeck = new Deck();
        mDeckPath = null;
        showDeckList(-1);
    }

    private void loadDeck(File file) {
        try {
            newDeck();
            mDeckPath = file.getPath();
            Deck.Editor.load(file, mDeck);
            _deckname.setText(mDeck.getDeckName());
            mLeader = CardDatabase.getInfo(mDeck.characters[0]);
            mDeckList.add(CardDatabase.getInfo(mDeck.characters[0]));
            mDeckCardAmount.put(CardDatabase.getInfo(mDeck.characters[0]), 1);
            for (int i = 1; i < 4; ++i) {
                CardInfo info = CardDatabase.getInfo(mDeck.characters[i]);
                if (!mDeckList.contains(info)) {
                    mDeckList.add(info);
                }
                if (mDeckCardAmount.containsKey(info)) {
                    mDeckCardAmount.put(info, mDeckCardAmount.get(info) + 1);
                } else {
                    mDeckCardAmount.put(info, 1);
                }
            }
            for (int i = 0; i < 40; ++i) {
                CardInfo info = CardDatabase.getInfo(mDeck.cards[i]);
                if (!mDeckList.contains(info)) {
                    mDeckList.add(info);
                }
                if (mDeckCardAmount.containsKey(info)) {
                    mDeckCardAmount.put(info, mDeckCardAmount.get(info) + 1);
                } else {
                    mDeckCardAmount.put(info, 1);
                }
            }
            StringBuilder b = new StringBuilder();
            for (String tag : mDeck.tags) {
                if (b.length() > 0) {
                    b.append(" ");
                }
                b.append(tag);
            }
            _tags.setText(b.toString());
            showDeckList(-1);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, FTool.getLocale(23));
        }
    }

    private void loadDeck() {
        JFileChooser chooser = new JFileChooser();
        File f;
        String sdir = FTool.readConfig("lastdeckfolder");
        if (sdir.equals("")) {
            sdir = "deck";
        }
        if (new File(sdir).exists()) {
            f = new File(sdir);
        } else {
            f = new File(".");
        }
        chooser.setCurrentDirectory(f);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new decFilter());

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        loadDeck(file);
    }

    private boolean checkDeck() {
        //If has 40 cards
        int tcnt = 0;
        for (CardInfo info : mDeckCardAmount.keySet()) {
            if (!info.isCharacterCard()) {
                tcnt += mDeckCardAmount.get(info);
            }
        }
        if (tcnt != 40) {
            JOptionPane.showMessageDialog(null, FTool.getLocale(97), FTool.getLocale(96), JOptionPane.OK_OPTION);
            return false;
        }
        // Check if leader is set
        if (mLeader.isNull()) {
            JOptionPane.showMessageDialog(null, FTool.getLocale(98), FTool.getLocale(96), JOptionPane.OK_OPTION);
            return false;
        }
        //If has characters with total level 4
        int chcnt = 0;
        for (CardInfo info : mDeckCardAmount.keySet()) {
            if (info.isCharacterCard()) {
                chcnt += mDeckCardAmount.get(info);
            }
        }
        if (chcnt != 4) {
            JOptionPane.showMessageDialog(null, FTool.getLocale(99), FTool.getLocale(96), JOptionPane.OK_OPTION);
            return false;
        }
        return true;
    }

    private HashMap<Integer, Integer> getDeckLV() {
        HashMap<Integer, Integer> lvs = new HashMap<Integer, Integer>();

        for (CardInfo info : mDeckCardAmount.keySet()) {
            if (info.isCharacterCard()) {
                int id = info.getCharId();
                if (lvs.containsKey(id)) {
                    lvs.put(id, lvs.get(id) + mDeckCardAmount.get(info));
                } else {
                    lvs.put(id, mDeckCardAmount.get(info));
                }
            }
        }

        return lvs;
    }

    void applyCharLVFilter() {
        ArrayList<CardInfo> filteredList = new ArrayList<CardInfo>();
        HashMap<Integer, Integer> lvs = getDeckLV();

        for (CardInfo info : mCardList) {
            int charid = info.getCharId();
            if (info.isCharacterCard()) {
                if (lvs.get(charid) >= 1) {
                    filteredList.add(info);
                }
                continue;
            }

            //NoLV always pass
            if (charid == 0) {
                filteredList.add(info);
                continue;
            }

            if (charid < 90) {
                //Normal cards
                if (!lvs.containsKey(charid)) {
                    continue;
                }
                if (lvs.get(charid) >= info.getLevelRequirement()) {
                    filteredList.add(info);
                    continue;
                }

            } else {
                //Cowork cards
                boolean match = true;

                String[] levelString = info.getCharacterRequirement().split(" ");
                int clv = info.getLevelRequirement();
                // 1:1 or 1:1:1
                if (clv == 2 || clv == 3) {
                    clv = 1;
                } else if (clv == 4) {
                    if (levelString[0].equals(levelString[1])) {
                        // 2:2
                        clv = 2;
                    } else {
                        // 1:1:1:1
                        clv = 1;
                    }
                }

                // Check cowork card level
                for (int j = 1; j < ORDER_ID_MAP.length - 1; ++j) {
                    int reqLV = info.getCharacterRequirement().indexOf(CardInfo.CHAR_NAMES[j - 1]);
                    int lv = lvs.containsKey(ORDER_ID_MAP[j]) ? lvs.get(ORDER_ID_MAP[j]) : 0;
                    if (reqLV >= 0 && lv < clv) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    filteredList.add(info);
                }
            }
        }

        mCardList = filteredList;
    }

    public int[] getAdvFilters() {
        int[] rtn = new int[16];
        if (atkmin.getText().equals("")) {
            rtn[0] = 0;
        } else {
            rtn[0] = FTool.safeParseInt(atkmin.getText());
        }
        if (atkmax.getText().equals("")) {
            rtn[1] = 99;
        } else {
            rtn[1] = FTool.safeParseInt(atkmax.getText());
        }
        if (cptmin.getText().equals("")) {
            rtn[2] = 0;
        } else {
            rtn[2] = FTool.safeParseInt(cptmin.getText());
        }
        if (cptmax.getText().equals("")) {
            rtn[3] = 99;
        } else {
            rtn[3] = FTool.safeParseInt(cptmax.getText());
        }
        if (hitmin.getText().equals("")) {
            rtn[4] = 0;
        } else {
            rtn[4] = FTool.safeParseInt(hitmin.getText());
        }
        if (hitmax.getText().equals("")) {
            rtn[5] = 99;
        } else {
            rtn[5] = FTool.safeParseInt(hitmax.getText());
        }
        if (type0.isSelected()) {
            rtn[6] = 1;
        } else {
            rtn[6] = 0;
        }
        if (type1.isSelected()) {
            rtn[7] = 1;
        } else {
            rtn[7] = 0;
        }
        if (type2.isSelected()) {
            rtn[8] = 1;
        } else {
            rtn[8] = 0;
        }
        if (type3.isSelected()) {
            rtn[9] = 1;
        } else {
            rtn[9] = 0;
        }
        if (type4.isSelected()) {
            rtn[10] = 1;
        } else {
            rtn[10] = 0;
        }
        if (type5.isSelected()) {
            rtn[11] = 1;
        } else {
            rtn[11] = 0;
        }
        if (type6.isSelected()) {
            rtn[12] = 1;
        } else {
            rtn[12] = 0;
        }
        if (type8.isSelected()) {
            rtn[13] = 1;
        } else {
            rtn[13] = 0;
        }
        if (type6.isSelected() || type8.isSelected()) {
            rtn[15] = 1;
        } else {
            rtn[15] = 0;
        }
        return rtn;
    }

    private void applyVersionFilter() {
        ArrayList<CardInfo> filteredList = new ArrayList<CardInfo>();
        for (CardInfo info : mCardList) {
            boolean isSelected = false;
            switch (info.getVersion()) {
                case 1:
                    isSelected = _ver1.isSelected();
                    break;
                case 2:
                    isSelected = _ver2.isSelected();
                    break;
                case 3:
                    isSelected = _ver3.isSelected();
                    break;
                case 4:
                    isSelected = _ver4.isSelected();
                    break;
                case 5:
                    isSelected = _ver5.isSelected();
                    break;
                case 6:
                    isSelected = _ver6.isSelected();
                    break;
                case 7:
                    isSelected = _ver7.isSelected();
                    break;
                case 8:
                    isSelected = _ver8.isSelected();
                    break;
                case 100:
                    isSelected = _veroriginal.isSelected();
                    break;
            }
            if (isSelected) {
                filteredList.add(info);
            }
        }

        mCardList = filteredList;
    }

    //end of Filters
    private void checkOrUncheckAllVersion() {
        disableEvent = true;
        _ver1.setSelected(_allver.isSelected());
        _ver2.setSelected(_allver.isSelected());
        _ver3.setSelected(_allver.isSelected());
        _ver4.setSelected(_allver.isSelected());
        _ver5.setSelected(_allver.isSelected());
        _ver6.setSelected(_allver.isSelected());
        _ver7.setSelected(_allver.isSelected());
        _ver8.setSelected(_allver.isSelected());
        disableEvent = false;
        applyFilters();
    }

    private void saveDeck(boolean renameTo) {
        if (!checkDeck()) {
            return;
        }

        if (renameTo && mDeckPath == null) {
            JOptionPane.showMessageDialog(this, "No original deck file", "Error", JOptionPane.OK_OPTION);
        }

        JFileChooser chooser = new JFileChooser();
        String sdir = FTool.readConfig("lastdeckfolder");
        if (sdir.equals("")) {
            sdir = "deck";
        }

        if (!new File(sdir).exists()) {
            new File(sdir).mkdirs();
        }

        chooser.setCurrentDirectory(new File(sdir));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new decFilter());
        if (mDeckPath == null || renameTo) {
            chooser.setSelectedFile(new File(_deckname.getText() + ".dec"));
        } else {
            chooser.setSelectedFile(new File(mDeckPath));
        }

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        //Convert dlist to deck
        int i;
        sortList(mDeckList);
        floatCharacters();

        mDeck.setDeckName(_deckname.getText());
        int listcnt = 0;
        int deckchcnt = 0;
        for (i = 0; i
                < mDeckCardAmount.get(mLeader); ++i) {
            mDeck.characters[i] = mLeader.getCardNo();
            ++deckchcnt;
        }

        ++listcnt;
        while (deckchcnt < 4) {
            for (int j = 0; j
                    < mDeckCardAmount.get(mDeckList.get(listcnt)); ++j) {
                mDeck.characters[deckchcnt] = mDeckList.get(listcnt).getCardNo();
                ++deckchcnt;
            }

            ++listcnt;
        }

        int deckccnt = 0;
        while (deckccnt < 40) {
            for (int j = 0; j
                    < mDeckCardAmount.get(mDeckList.get(listcnt)); ++j) {
                mDeck.cards[deckccnt] = mDeckList.get(listcnt).getCardNo();
                ++deckccnt;
            }

            ++listcnt;
        }

        String tags = _tags.getText();
        tags = tags.replace("　", " ");
        mDeck.tags.clear();
        for (String s : tags.split(" ")) {
            s = s.trim();
            if (s.length() > 0) {
                mDeck.tags.add(s);
            }
        }

        try {
            File file = chooser.getSelectedFile();
            if (file.getName().length() < 4) {
                file = new File(file.getPath() + ".dec");
            } else if (!file.getName().substring(file.getName().length() - 4).endsWith(".dec")) {
                file = new File(file.getPath() + ".dec");
            }

            FTool.updateConfig("lastdeckfolder", file.getParent());
            Deck.Editor.save(file, mDeck);
            if (renameTo) {
                new File(mDeckPath).delete();
            }
            mDeckPath =
                    file.getPath();
            JOptionPane.showMessageDialog(null, file.getName() + FTool.getLocale(95));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, FTool.getLocale(23));
        }

    }

    void showTextDeck() {
        JTextArea jmessage = new JTextArea("");
        JScrollPane js = new JScrollPane(jmessage);
        js.setPreferredSize(new Dimension(100, 350));

        String message = "";
        for (int i = 0; i
                < _decklist.getModel().getSize(); ++i) {
            message += (((ListItem) (_decklist.getModel().getElementAt(i))).getValue() + "\r\n");
        }

        jmessage.setText(message);

        JOptionPane.showMessageDialog(this, js, "TEXT FORM DECK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void transfer() {
        JTextArea jdeck = new JTextArea("");
        JScrollPane js = new JScrollPane(jdeck);
        js.setPreferredSize(new Dimension(100, 350));
        JOptionPane.showMessageDialog(this, js, "TRANSFER FROM TEXT", JOptionPane.QUESTION_MESSAGE);
        String txt = jdeck.getText();
        if (txt.length() < 5) {
            return;
        }

        newDeck();
        String[] lines = txt.split("\n");
        for (String line : lines) {
            String[] s;
            if (line.startsWith("Leader")) {
                s = line.split(" ");
                if (s.length == 4) {
                    s[2] += " " + s[3];
                }

                CardInfo info = CardDatabase.getInfo(s[2]);
                if (info != null) {
                    mLeader = info;
                    mDeckList.add(info);
                    if (mDeckCardAmount.containsKey(info)) {
                        mDeckCardAmount.put(info, FTool.safeParseInt(s[1].substring(2, 3)) + mDeckCardAmount.get(info));
                    } else {
                        mDeckCardAmount.put(info, FTool.safeParseInt(s[1].substring(2, 3)));
                    }

                }
            } else if (line.startsWith("Lv")) {
                s = line.split(" ");
                if (s.length == 3) {
                    s[1] += " " + s[2];
                }

                CardInfo info = CardDatabase.getInfo(s[1]);
                if (info != null) {
                    mDeckList.add(info);
                    if (mDeckCardAmount.containsKey(info)) {
                        mDeckCardAmount.put(info, FTool.safeParseInt(s[0].substring(2, 3)) + mDeckCardAmount.get(info));
                    } else {
                        mDeckCardAmount.put(info, FTool.safeParseInt(s[0].substring(2, 3)));
                    }

                }
            } else if (line.length() > 1) {
                if (line.charAt(1) == 'x') {
                    s = line.split(" ");
                    if (s.length == 3) {
                        s[1] += " " + s[2];
                    }

                    CardInfo info = CardDatabase.getInfo(s[1]);
                    mDeckList.add(info);
                    if (mDeckCardAmount.containsKey(info)) {
                        mDeckCardAmount.put(info, FTool.safeParseInt(s[0].substring(0, 1)) + mDeckCardAmount.get(info));
                    } else {
                        mDeckCardAmount.put(info, FTool.safeParseInt(s[0].substring(0, 1)));
                    }

                } else if (line.charAt(2) == 'x') {
                    //Wiki Form
                    s = line.split(" ");
                    CardInfo info = CardDatabase.getInfo(s[2].substring(1, s[2].length() - 1));
                    if (info != null) {
                        mDeckList.add(info);
                        if (mDeckCardAmount.containsKey(info)) {
                            mDeckCardAmount.put(info, FTool.safeParseInt(s[0]) + mDeckCardAmount.get(info));
                        } else {
                            mDeckCardAmount.put(info, FTool.safeParseInt(s[0]));
                        }

                    }
                }
            }
        }

        showDeckList(-1);
    }

    private void recycle() {
        if (mDeckPath == null) {
            JOptionPane.showMessageDialog(this, "No original deck file", "Error", JOptionPane.OK_OPTION);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, "Really recycle this deck?", "Recycle deck", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            File f = new File(mDeckPath);
            File recy = new File("deck/recycle");
            if (!recy.exists()) {
                recy.mkdirs();
            }
            f.renameTo(new File("deck/recycle/" + f.getName()));
            newDeck();
        }
    }

    private void showAlias() {
        setAliasList();
        _aliasdialog.setSize(380, 360);
        _aliasdialog.setVisible(true);
    }

    private void addAlias() {
        String alias = _aliasname.getText();
        if (alias.length() > 0) {
            if (!mDeck.aliases.contains(alias)) {
                mDeck.aliases.add(alias);
            }
        }
        _aliasname.setText("");
        setAliasList();
    }

    private void removeAlias() {
        String alias = (String) _aliaslist.getSelectedValue();
        if (alias != null) {
            mDeck.aliases.remove(alias);
            setAliasList();
        }
    }

    private void setAliasList() {
        _aliaslist.removeAll();
        Vector<String> aliases = new Vector<String>();
        for (String alias : mDeck.aliases) {
            aliases.add(alias);
        }
        _aliaslist.setListData(aliases);
    }

    private void fitCharCards() {
        int[] charlvs = new int[CardInfo.CHAR_NAMES.length];
        for (CardInfo info : mDeckList) {
            if (info.isCharacterCard()) {
                continue;
            }
            String[] cons = info.getCharacterRequirement().split(" ");
            int[] thislv = new int[CardInfo.CHAR_NAMES.length];
            for (String s : cons) {
                int index = getCharIndex(s);
                if (index < 0) {
                    continue;
                }
                thislv[index]++;
                if (thislv[index] > charlvs[index]) {
                    charlvs[index] = thislv[index];
                }
            }
        }
        for (int i = 0; i < charlvs.length; ++i) {
            int amount = charlvs[i];
            CardInfo info;
            if (i == 16) {
                info = CardDatabase.getInfo(8000);
            } else if (i == 18) {
                info = CardDatabase.getInfo(8100);
            } else {
                info = CardDatabase.getInfo(ORDER_ID_MAP[i + 1] * 100);
            }
            if (amount > 0) {
                if (!mDeckList.contains(info)) {
                    mDeckList.add(info);
                }
                mDeckCardAmount.put(info, amount);
            } else {
                mDeckList.remove(info);
                mDeckCardAmount.remove(info);
            }
        }
        showDeckList(-1);
        synchronizeList(0);
    }

    private int getCharIndex(String s) {
        for (int i = 0; i < CardInfo.CHAR_NAMES.length; ++i) {
            if (s.equals(CardInfo.CHAR_NAMES[i])) {
                return i;
            }
        }
        return -1;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _clistsort = new javax.swing.ButtonGroup();
        _advancedfilterdialog = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        type2 = new javax.swing.JCheckBox();
        type1 = new javax.swing.JCheckBox();
        type0 = new javax.swing.JCheckBox();
        atkmin = new javax.swing.JTextField();
        cptmin = new javax.swing.JTextField();
        hitmin = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        atkmax = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cptmax = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        hitmax = new javax.swing.JTextField();
        _applyadvancedfilter = new javax.swing.JButton();
        _reset = new javax.swing.JButton();
        Cancel = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        type3 = new javax.swing.JCheckBox();
        type4 = new javax.swing.JCheckBox();
        type5 = new javax.swing.JCheckBox();
        type6 = new javax.swing.JCheckBox();
        type8 = new javax.swing.JCheckBox();
        jLabel19 = new javax.swing.JLabel();
        _aliasdialog = new javax.swing.JDialog();
        jScrollPane4 = new javax.swing.JScrollPane();
        _aliaslist = new javax.swing.JList();
        _aliasname = new javax.swing.JTextField();
        _endalias = new javax.swing.JButton();
        _addalias = new javax.swing.JButton();
        _deletealias = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        SCArea = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        _cardlist = new javax.swing.JList();
        _allcheck = new javax.swing.JButton();
        _alluncheck = new javax.swing.JButton();
        _amount = new javax.swing.JLabel();
        add1 = new javax.swing.JButton();
        add2 = new javax.swing.JButton();
        add3 = new javax.swing.JButton();
        remove1 = new javax.swing.JButton();
        remove2 = new javax.swing.JButton();
        remove3 = new javax.swing.JButton();
        setleader = new javax.swing.JButton();
        _clistsortnum = new javax.swing.JRadioButton();
        _clistsorttype = new javax.swing.JRadioButton();
        _fitcharlv = new javax.swing.JCheckBox();
        _filterpanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        _cardnum = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        _cardname = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        _searchtext = new javax.swing.JTextField();
        _selectionpanel = new javax.swing.JPanel();
        kind0 = new javax.swing.JCheckBox();
        kind1 = new javax.swing.JCheckBox();
        kind2 = new javax.swing.JCheckBox();
        kind3 = new javax.swing.JCheckBox();
        _versionpanel = new javax.swing.JPanel();
        _ver1 = new javax.swing.JCheckBox();
        _ver2 = new javax.swing.JCheckBox();
        _ver3 = new javax.swing.JCheckBox();
        _ver4 = new javax.swing.JCheckBox();
        _ver5 = new javax.swing.JCheckBox();
        _ver6 = new javax.swing.JCheckBox();
        _ver7 = new javax.swing.JCheckBox();
        _ver8 = new javax.swing.JCheckBox();
        _allver = new javax.swing.JCheckBox();
        _filterbuttonpanel = new javax.swing.JPanel();
        _showscfilter = new javax.swing.JButton();
        _applyfilter = new javax.swing.JButton();
        _resetspecifiedfilter = new javax.swing.JButton();
        _veroriginal = new javax.swing.JCheckBox();
        _mplvfilterpanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        _mpmin = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        _mpmax = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        _lvmin = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        _lvmax = new javax.swing.JTextField();
        _usecolorsymbol = new javax.swing.JCheckBox();
        _cardtextsearchhelp = new javax.swing.JButton();
        _useregexp = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        _chartab = new javax.swing.JTabbedPane();
        _charpanel = new javax.swing.JPanel();
        char0 = new javax.swing.JCheckBox();
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
        char90 = new javax.swing.JCheckBox();
        _originalcharpanel = new javax.swing.JPanel();
        _deckfilepanel = new javax.swing.JPanel();
        _deckname = new javax.swing.JTextField();
        _recycle = new javax.swing.JButton();
        _tags = new javax.swing.JTextField();
        _rename = new javax.swing.JButton();
        _savedeck = new javax.swing.JButton();
        _transfer = new javax.swing.JButton();
        _decksab = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        _decklist = new javax.swing.JList();
        _newdeck = new javax.swing.JButton();
        _loaddeck = new javax.swing.JButton();
        _showtextdeck = new javax.swing.JButton();
        _alias = new javax.swing.JButton();
        _insertchar = new javax.swing.JButton();
        jMenuBar6 = new javax.swing.JMenuBar();
        _options4 = new javax.swing.JMenu();
        _leftclickshowcustomizedicon4 = new javax.swing.JCheckBoxMenuItem();
        jMenu6 = new javax.swing.JMenu();
        closeeditor5 = new javax.swing.JMenuItem();
        quit5 = new javax.swing.JMenuItem();

        jLabel1.setFont(new Font(java.util.ResourceBundle.getBundle("DeckEditorWindow").getString("default.font"),0,12));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("DeckEditorWindow"); // NOI18N
        jLabel1.setText(bundle.getString("atk.txt")); // NOI18N

        jLabel9.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel9.setText(bundle.getString("catk.txt")); // NOI18N

        jLabel10.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel10.setText(bundle.getString("hit.txt")); // NOI18N

        jLabel11.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel11.setText(bundle.getString("Type.txt")); // NOI18N

        type2.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type2.setSelected(true);
        type2.setText(bundle.getString("Normal.txt")); // NOI18N
        type2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        type1.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type1.setSelected(true);
        type1.setText(bundle.getString("Concentrate.txt")); // NOI18N
        type1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        type0.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type0.setSelected(true);
        type0.setText(bundle.getString("Spread.txt")); // NOI18N
        type0.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type0.setMargin(new java.awt.Insets(0, 0, 0, 0));

        atkmin.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        cptmin.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        hitmin.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel12.setText("～");

        atkmax.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel13.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel13.setText("～");

        cptmax.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        jLabel14.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        jLabel14.setText("～");

        hitmax.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        _applyadvancedfilter.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _applyadvancedfilter.setText(bundle.getString("OK.txt")); // NOI18N
        _applyadvancedfilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _applyadvancedfilterActionPerformed(evt);
            }
        });

        _reset.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _reset.setText(bundle.getString("Reset.txt")); // NOI18N
        _reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _resetActionPerformed(evt);
            }
        });

        Cancel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        Cancel.setText(bundle.getString("Cancel.txt")); // NOI18N
        Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 18));
        jLabel15.setText(bundle.getString("SpellCardFilter.txt")); // NOI18N

        jLabel16.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 18));
        jLabel16.setText(bundle.getString("SupportCardFilter.txt")); // NOI18N

        jLabel17.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 18));
        jLabel17.setText(bundle.getString("EventCardFilter.txt")); // NOI18N

        jLabel18.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel18.setText(bundle.getString("Dispose.txt")); // NOI18N

        type3.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type3.setSelected(true);
        type3.setText(bundle.getString("Leader.txt")); // NOI18N
        type3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        type4.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type4.setSelected(true);
        type4.setText(bundle.getString("Spell.txt")); // NOI18N
        type4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type4.setMargin(new java.awt.Insets(0, 0, 0, 0));

        type5.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type5.setSelected(true);
        type5.setText("シーン");
        type5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type5.setMargin(new java.awt.Insets(0, 0, 0, 0));

        type6.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type6.setSelected(true);
        type6.setText(bundle.getString("Fill.txt")); // NOI18N
        type6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type6.setMargin(new java.awt.Insets(0, 0, 0, 0));

        type8.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        type8.setSelected(true);
        type8.setText(bundle.getString("Battle.txt")); // NOI18N
        type8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        type8.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel19.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel19.setText(bundle.getString("Use.txt")); // NOI18N

        javax.swing.GroupLayout _advancedfilterdialogLayout = new javax.swing.GroupLayout(_advancedfilterdialog.getContentPane());
        _advancedfilterdialog.getContentPane().setLayout(_advancedfilterdialogLayout);
        _advancedfilterdialogLayout.setHorizontalGroup(
            _advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11))
                                .addGap(19, 19, 19)
                                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(hitmin)
                                            .addComponent(cptmin)
                                            .addComponent(atkmin, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                                .addComponent(jLabel12)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(atkmax, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cptmax))
                                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                                .addComponent(jLabel14)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(hitmax, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(type1)
                                    .addComponent(type2)
                                    .addComponent(type0))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                            .addComponent(jLabel17)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel16)
                                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                                .addComponent(jLabel18)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(type4)
                                                    .addComponent(type3)
                                                    .addComponent(type5)))))
                                    .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                        .addComponent(jLabel19)
                                        .addGap(18, 18, 18)
                                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(type8)
                                            .addComponent(type6))))))
                        .addGap(44, 44, 44))
                    .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                        .addComponent(_applyadvancedfilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Cancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_reset)
                        .addContainerGap(116, Short.MAX_VALUE))))
        );
        _advancedfilterdialogLayout.setVerticalGroup(
            _advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(atkmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(cptmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(hitmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(atkmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18)
                            .addComponent(type3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(cptmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(hitmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(_advancedfilterdialogLayout.createSequentialGroup()
                                .addComponent(type4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(type5)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(type0)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(type1)
                    .addComponent(jLabel19)
                    .addComponent(type6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(type2)
                    .addComponent(type8))
                .addGap(18, 18, 18)
                .addGroup(_advancedfilterdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_applyadvancedfilter)
                    .addComponent(Cancel)
                    .addComponent(_reset))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        _aliaslist.setFont(new java.awt.Font("MS PGothic", 0, 14)); // NOI18N
        _aliaslist.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(_aliaslist);

        _aliasname.setFont(new java.awt.Font("MS PGothic", 0, 14)); // NOI18N
        _aliasname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _aliasnameActionPerformed(evt);
            }
        });

        _endalias.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _endalias.setText("終了");
        _endalias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _endaliasActionPerformed(evt);
            }
        });

        _addalias.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _addalias.setText("追加");
        _addalias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _addaliasActionPerformed(evt);
            }
        });

        _deletealias.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _deletealias.setText("削除");
        _deletealias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _deletealiasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _aliasdialogLayout = new javax.swing.GroupLayout(_aliasdialog.getContentPane());
        _aliasdialog.getContentPane().setLayout(_aliasdialogLayout);
        _aliasdialogLayout.setHorizontalGroup(
            _aliasdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_aliasdialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_aliasdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addComponent(_aliasname, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_aliasdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_addalias)
                    .addComponent(_deletealias)
                    .addComponent(_endalias))
                .addContainerGap())
        );
        _aliasdialogLayout.setVerticalGroup(
            _aliasdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_aliasdialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_aliasdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_aliasname, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_addalias))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(_aliasdialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_aliasdialogLayout.createSequentialGroup()
                        .addComponent(_deletealias)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 205, Short.MAX_VALUE)
                        .addComponent(_endalias))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Fantasy Festa Online Deckeditor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        SCArea.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jScrollPane1.setViewportView(SCArea);

        _cardlist.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _cardlist.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        _cardlist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _cardlistValueChanged(evt);
            }
        });
        _cardlist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                _cardlistKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(_cardlist);

        _allcheck.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _allcheck.setText(bundle.getString("SelectAll.txt")); // NOI18N
        _allcheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _allcheckActionPerformed(evt);
            }
        });

        _alluncheck.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _alluncheck.setText(bundle.getString("UnselectAll.txt")); // NOI18N
        _alluncheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _alluncheckActionPerformed(evt);
            }
        });

        _amount.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _amount.setText("281枚");

        add1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        add1.setText("+1");
        add1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add1ActionPerformed(evt);
            }
        });

        add2.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        add2.setText("+2");
        add2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add2ActionPerformed(evt);
            }
        });

        add3.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        add3.setText("+3");
        add3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add3ActionPerformed(evt);
            }
        });

        remove1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        remove1.setText("-1");
        remove1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove1ActionPerformed(evt);
            }
        });

        remove2.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        remove2.setText("-2");
        remove2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove2ActionPerformed(evt);
            }
        });

        remove3.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        remove3.setText("-3");
        remove3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove3ActionPerformed(evt);
            }
        });

        setleader.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        setleader.setText("L");
        setleader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setleaderActionPerformed(evt);
            }
        });

        _clistsortnum.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _clistsortnum.setSelected(true);
        _clistsortnum.setText(bundle.getString("SortedByNo.txt")); // NOI18N
        _clistsortnum.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _clistsortnum.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _clistsortnum.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _clistsortnumItemStateChanged(evt);
            }
        });

        _clistsorttype.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _clistsorttype.setText(bundle.getString("SortedByType.txt")); // NOI18N
        _clistsorttype.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _clistsorttype.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _clistsorttype.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _clistsorttypeItemStateChanged(evt);
            }
        });

        _fitcharlv.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 14));
        _fitcharlv.setText(bundle.getString("FitCharLV.txt")); // NOI18N
        _fitcharlv.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _fitcharlvItemStateChanged(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel2.setText(bundle.getString("CardNO.txt")); // NOI18N

        _cardnum.setMinimumSize(new java.awt.Dimension(50, 19));
        _cardnum.setPreferredSize(new java.awt.Dimension(50, 19));
        _cardnum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _cardnumActionPerformed(evt);
            }
        });
        _cardnum.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _cardnumFocusGained(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel3.setText(bundle.getString("CardName.txt")); // NOI18N

        _cardname.setPreferredSize(new java.awt.Dimension(70, 19));
        _cardname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _cardnameActionPerformed(evt);
            }
        });
        _cardname.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _cardnameFocusGained(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel6.setText(bundle.getString("Text.txt")); // NOI18N

        _searchtext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _searchtextActionPerformed(evt);
            }
        });
        _searchtext.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _searchtextFocusGained(evt);
            }
        });

        _selectionpanel.setLayout(new java.awt.GridLayout(1, 4, 2, 8));

        kind0.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        kind0.setSelected(true);
        kind0.setText("Char");
        kind0.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        kind0.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _selectionpanel.add(kind0);

        kind1.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        kind1.setSelected(true);
        kind1.setText("Spell");
        kind1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        kind1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _selectionpanel.add(kind1);

        kind2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        kind2.setSelected(true);
        kind2.setText("Support");
        kind2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        kind2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _selectionpanel.add(kind2);

        kind3.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        kind3.setSelected(true);
        kind3.setText("Event");
        kind3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        kind3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _selectionpanel.add(kind3);

        _versionpanel.setLayout(new java.awt.GridLayout(3, 4, 2, 8));

        _ver1.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver1.setSelected(true);
        _ver1.setText("1幕");
        _ver1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver1ItemStateChanged(evt);
            }
        });
        _versionpanel.add(_ver1);

        _ver2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver2.setSelected(true);
        _ver2.setText("2幕");
        _ver2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver2ItemStateChanged(evt);
            }
        });
        _versionpanel.add(_ver2);

        _ver3.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver3.setSelected(true);
        _ver3.setText("3幕");
        _ver3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver3ItemStateChanged(evt);
            }
        });
        _versionpanel.add(_ver3);

        _ver4.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver4.setSelected(true);
        _ver4.setText("4幕");
        _ver4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver4ItemStateChanged(evt);
            }
        });
        _ver4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _ver4ActionPerformed(evt);
            }
        });
        _versionpanel.add(_ver4);

        _ver5.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver5.setSelected(true);
        _ver5.setText("5幕");
        _ver5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver5ItemStateChanged(evt);
            }
        });
        _versionpanel.add(_ver5);

        _ver6.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver6.setSelected(true);
        _ver6.setText("6幕");
        _ver6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver6ItemStateChanged(evt);
            }
        });
        _ver6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _ver6ActionPerformed(evt);
            }
        });
        _versionpanel.add(_ver6);

        _ver7.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver7.setSelected(true);
        _ver7.setText("7幕");
        _ver7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver7ItemStateChanged(evt);
            }
        });
        _versionpanel.add(_ver7);

        _ver8.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _ver8.setSelected(true);
        _ver8.setText("8幕");
        _ver8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _ver8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _ver8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _ver8ItemStateChanged(evt);
            }
        });
        _versionpanel.add(_ver8);

        _allver.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _allver.setSelected(true);
        _allver.setText("全て");
        _allver.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _allver.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _allver.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _allverItemStateChanged(evt);
            }
        });
        _versionpanel.add(_allver);

        _showscfilter.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _showscfilter.setText(bundle.getString("MoreFilter.txt")); // NOI18N
        _showscfilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _showscfilterActionPerformed(evt);
            }
        });

        _applyfilter.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _applyfilter.setText(bundle.getString("ApplyFilter.txt")); // NOI18N
        _applyfilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _applyfilterActionPerformed(evt);
            }
        });

        _resetspecifiedfilter.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _resetspecifiedfilter.setText(bundle.getString("Reset.txt")); // NOI18N
        _resetspecifiedfilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _resetspecifiedfilterActionPerformed(evt);
            }
        });

        _veroriginal.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _veroriginal.setText("オリカを表示");
        _veroriginal.setEnabled(false);
        _veroriginal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _veroriginalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _filterbuttonpanelLayout = new javax.swing.GroupLayout(_filterbuttonpanel);
        _filterbuttonpanel.setLayout(_filterbuttonpanelLayout);
        _filterbuttonpanelLayout.setHorizontalGroup(
            _filterbuttonpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_filterbuttonpanelLayout.createSequentialGroup()
                .addComponent(_resetspecifiedfilter)
                .addGap(53, 53, 53)
                .addComponent(_veroriginal))
            .addGroup(_filterbuttonpanelLayout.createSequentialGroup()
                .addComponent(_showscfilter, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_applyfilter, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        _filterbuttonpanelLayout.setVerticalGroup(
            _filterbuttonpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_filterbuttonpanelLayout.createSequentialGroup()
                .addGroup(_filterbuttonpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_showscfilter)
                    .addComponent(_applyfilter))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_filterbuttonpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_resetspecifiedfilter)
                    .addComponent(_veroriginal)))
        );

        _mplvfilterpanel.setLayout(new javax.swing.BoxLayout(_mplvfilterpanel, javax.swing.BoxLayout.LINE_AXIS));

        jLabel4.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel4.setText("呪力");
        _mplvfilterpanel.add(jLabel4);

        _mpmin.setMinimumSize(new java.awt.Dimension(35, 19));
        _mpmin.setPreferredSize(new java.awt.Dimension(35, 19));
        _mplvfilterpanel.add(_mpmin);

        jLabel8.setText("～");
        _mplvfilterpanel.add(jLabel8);

        _mpmax.setMinimumSize(new java.awt.Dimension(35, 19));
        _mpmax.setPreferredSize(new java.awt.Dimension(35, 19));
        _mplvfilterpanel.add(_mpmax);

        jLabel5.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        jLabel5.setText(bundle.getString("LV.txt")); // NOI18N
        _mplvfilterpanel.add(jLabel5);

        _lvmin.setMinimumSize(new java.awt.Dimension(35, 19));
        _lvmin.setPreferredSize(new java.awt.Dimension(35, 19));
        _mplvfilterpanel.add(_lvmin);

        jLabel7.setText("～");
        _mplvfilterpanel.add(jLabel7);

        _lvmax.setMaximumSize(new java.awt.Dimension(999999, 99999));
        _lvmax.setMinimumSize(new java.awt.Dimension(35, 19));
        _lvmax.setPreferredSize(new java.awt.Dimension(35, 19));
        _mplvfilterpanel.add(_lvmax);

        _usecolorsymbol.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _usecolorsymbol.setText("色記号");
        _usecolorsymbol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _usecolorsymbolActionPerformed(evt);
            }
        });

        _cardtextsearchhelp.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _cardtextsearchhelp.setText("ヘルプ");
        _cardtextsearchhelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _cardtextsearchhelpActionPerformed(evt);
            }
        });

        _useregexp.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _useregexp.setText("正規表現");
        _useregexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _useregexpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _filterpanelLayout = new javax.swing.GroupLayout(_filterpanel);
        _filterpanel.setLayout(_filterpanelLayout);
        _filterpanelLayout.setHorizontalGroup(
            _filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(_mplvfilterpanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _filterpanelLayout.createSequentialGroup()
                    .addComponent(_usecolorsymbol)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_useregexp, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(_cardtextsearchhelp))
                .addComponent(_filterbuttonpanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _filterpanelLayout.createSequentialGroup()
                    .addComponent(jLabel6)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_searchtext))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, _filterpanelLayout.createSequentialGroup()
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_cardnum, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_cardname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(_selectionpanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                .addComponent(_versionpanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        _filterpanelLayout.setVerticalGroup(
            _filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_filterpanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_selectionpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_versionpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(_filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_filterpanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel2))
                    .addGroup(_filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(_cardnum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(_cardname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addGap(7, 7, 7)
                .addGroup(_filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(_searchtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_filterpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_usecolorsymbol)
                    .addComponent(_useregexp)
                    .addComponent(_cardtextsearchhelp))
                .addGap(5, 5, 5)
                .addComponent(_mplvfilterpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(_filterbuttonpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel20.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("DeckEditorWindow").getString("default.font"), 0, 12));
        jLabel20.setText(bundle.getString("SortedBy.txt")); // NOI18N

        _chartab.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));

        _charpanel.setLayout(new java.awt.GridLayout(14, 3));

        char0.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        char0.setSelected(true);
        char0.setText(bundle.getString("LV0Card.txt")); // NOI18N
        char0.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char0.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char0);

        char1.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char1.setForeground(new java.awt.Color(0, 0, 204));
        char1.setSelected(true);
        char1.setText("博麗霊夢");
        char1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char1);

        char2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char2.setForeground(new java.awt.Color(0, 0, 204));
        char2.setSelected(true);
        char2.setText("霧雨魔理沙");
        char2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char2);

        char3.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char3.setForeground(new java.awt.Color(0, 0, 204));
        char3.setSelected(true);
        char3.setText("十六夜咲夜");
        char3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char3);

        char4.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char4.setForeground(new java.awt.Color(0, 0, 204));
        char4.setSelected(true);
        char4.setText("魂魄妖夢");
        char4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char4);

        char5.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char5.setForeground(new java.awt.Color(0, 0, 204));
        char5.setSelected(true);
        char5.setText("八雲紫");
        char5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char5);

        char6.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char6.setForeground(new java.awt.Color(0, 0, 204));
        char6.setSelected(true);
        char6.setText("アリス");
        char6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char6);

        char7.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char7.setForeground(new java.awt.Color(0, 0, 204));
        char7.setSelected(true);
        char7.setText("レミリア");
        char7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char7);

        char8.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char8.setForeground(new java.awt.Color(0, 0, 204));
        char8.setSelected(true);
        char8.setText("西行寺幽々子");
        char8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char8);

        char9.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char9.setForeground(new java.awt.Color(153, 102, 0));
        char9.setSelected(true);
        char9.setText("フランドール");
        char9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char9);

        char10.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char10.setForeground(new java.awt.Color(153, 102, 0));
        char10.setSelected(true);
        char10.setText("パチュリー");
        char10.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char10);

        char11.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char11.setForeground(new java.awt.Color(153, 102, 0));
        char11.setSelected(true);
        char11.setText("紅美鈴");
        char11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char11);

        char12.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char12.setForeground(new java.awt.Color(153, 102, 0));
        char12.setSelected(true);
        char12.setText("蓬莱山輝夜");
        char12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char12);

        char13.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char13.setForeground(new java.awt.Color(153, 102, 0));
        char13.setSelected(true);
        char13.setText("八意永琳");
        char13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char13.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char13);

        char14.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char14.setForeground(new java.awt.Color(153, 102, 0));
        char14.setSelected(true);
        char14.setText("鈴仙");
        char14.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char14.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char14);

        char15.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char15.setForeground(new java.awt.Color(0, 0, 204));
        char15.setSelected(true);
        char15.setText("八雲藍");
        char15.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char15.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char15);

        char16.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char16.setForeground(new java.awt.Color(0, 0, 204));
        char16.setSelected(true);
        char16.setText("橙");
        char16.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char16.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char16);

        char17.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char17.setForeground(new java.awt.Color(0, 0, 204));
        char17.setSelected(true);
        char17.setText("プリバー");
        char17.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char17.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char17);

        char18.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char18.setForeground(new java.awt.Color(0, 0, 204));
        char18.setSelected(true);
        char18.setText("藤原妹紅");
        char18.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char18.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char18);

        char19.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char19.setForeground(new java.awt.Color(0, 0, 204));
        char19.setSelected(true);
        char19.setText("上白沢慧音");
        char19.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char19.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char19);

        char20.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        char20.setForeground(new java.awt.Color(0, 0, 204));
        char20.setSelected(true);
        char20.setText("伊吹萃香");
        char20.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char20.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char20);

        _char21.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char21.setForeground(new java.awt.Color(153, 102, 0));
        _char21.setSelected(true);
        _char21.setText("洩矢諏訪子");
        _char21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char21.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char21);

        _char22.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char22.setForeground(new java.awt.Color(153, 102, 0));
        _char22.setSelected(true);
        _char22.setText("八坂神奈子");
        _char22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _char22ActionPerformed(evt);
            }
        });
        _charpanel.add(_char22);

        _char23.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char23.setForeground(new java.awt.Color(153, 102, 0));
        _char23.setSelected(true);
        _char23.setText("東風谷早苗");
        _char23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char23.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char23);

        _char24.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char24.setForeground(new java.awt.Color(153, 102, 0));
        _char24.setSelected(true);
        _char24.setText("射命丸文");
        _char24.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char24.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char24);

        _char25.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char25.setForeground(new java.awt.Color(153, 102, 0));
        _char25.setSelected(true);
        _char25.setText("小野塚小町");
        _char25.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char25.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char25);

        _char26.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char26.setForeground(new java.awt.Color(153, 102, 0));
        _char26.setSelected(true);
        _char26.setText("河城にとり");
        _char26.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char26.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char26);

        _char27.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char27.setForeground(new java.awt.Color(0, 0, 204));
        _char27.setSelected(true);
        _char27.setText("比那名居天子");
        _char27.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char27.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char27);

        _char28.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char28.setForeground(new java.awt.Color(0, 0, 204));
        _char28.setSelected(true);
        _char28.setText("永江衣玖");
        _char28.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char28.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char28);

        _char29.setFont(new java.awt.Font("          ", 0, 12)); // NOI18N
        _char29.setForeground(new java.awt.Color(0, 0, 204));
        _char29.setSelected(true);
        _char29.setText("霊烏路空");
        _char29.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char29.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char29);

        _char30.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char30.setForeground(new java.awt.Color(0, 0, 204));
        _char30.setSelected(true);
        _char30.setText("火焔猫燐");
        _char30.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char30.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char30);

        _char31.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char31.setForeground(new java.awt.Color(153, 102, 0));
        _char31.setSelected(true);
        _char31.setText("古明地こいし");
        _char31.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char31.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char31);

        _char32.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char32.setForeground(new java.awt.Color(153, 102, 0));
        _char32.setSelected(true);
        _char32.setText("古明地さとり");
        _char32.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char32.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char32);

        _char33.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char33.setForeground(new java.awt.Color(153, 102, 0));
        _char33.setSelected(true);
        _char33.setText("聖白蓮");
        _char33.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char33.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char33);

        _char34.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char34.setForeground(new java.awt.Color(153, 102, 0));
        _char34.setSelected(true);
        _char34.setText("寅丸星");
        _char34.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char34.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char34);

        _char35.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char35.setForeground(new java.awt.Color(0, 0, 204));
        _char35.setSelected(true);
        _char35.setText("封獣ぬえ");
        _char35.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char35.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char35);

        _char36.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char36.setForeground(new java.awt.Color(0, 0, 204));
        _char36.setSelected(true);
        _char36.setText("多々良小傘");
        _char36.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char36.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char36);

        _char37.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _char37.setForeground(new java.awt.Color(0, 0, 204));
        _char37.setSelected(true);
        _char37.setText("ナズーリン");
        _char37.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char37.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char37);

        _char38.setFont(new java.awt.Font("          ", 0, 12)); // NOI18N
        _char38.setForeground(new java.awt.Color(0, 0, 204));
        _char38.setSelected(true);
        _char38.setText("村紗水蜜");
        _char38.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _char38.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(_char38);

        char90.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        char90.setSelected(true);
        char90.setText(bundle.getString("CooperationCard.txt")); // NOI18N
        char90.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        char90.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _charpanel.add(char90);

        _chartab.addTab("キャラクター", _charpanel);

        _originalcharpanel.setLayout(new java.awt.GridLayout(7, 3));
        _chartab.addTab("オリカ", _originalcharpanel);

        _deckname.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 14));
        _deckname.setText(bundle.getString("DeckName.txt")); // NOI18N
        _deckname.setPreferredSize(new java.awt.Dimension(228, 19));
        _deckname.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                _decknameFocusGained(evt);
            }
        });

        _recycle.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
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

        _rename.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _rename.setText("MOVE");
        _rename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _renameActionPerformed(evt);
            }
        });

        _savedeck.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _savedeck.setText(bundle.getString("Save.txt")); // NOI18N
        _savedeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _savedeckActionPerformed(evt);
            }
        });

        _transfer.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _transfer.setText("TRANS");
        _transfer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _transferActionPerformed(evt);
            }
        });

        _decksab.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _decksab.setText("スペカ０枚");

        jLabel21.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        jLabel21.setText("タグ");

        _decklist.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _decklist.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        _decklist.setVisibleRowCount(40);
        _decklist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _decklistValueChanged(evt);
            }
        });
        _decklist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                _decklistKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(_decklist);

        _newdeck.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _newdeck.setText(bundle.getString("New.txt")); // NOI18N
        _newdeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _newdeckActionPerformed(evt);
            }
        });

        _loaddeck.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _loaddeck.setText(bundle.getString("Load.txt")); // NOI18N
        _loaddeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _loaddeckActionPerformed(evt);
            }
        });

        _showtextdeck.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _showtextdeck.setText("TEXT");
        _showtextdeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _showtextdeckActionPerformed(evt);
            }
        });

        _alias.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        _alias.setText("ALIAS");
        _alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _aliasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _deckfilepanelLayout = new javax.swing.GroupLayout(_deckfilepanel);
        _deckfilepanel.setLayout(_deckfilepanelLayout);
        _deckfilepanelLayout.setHorizontalGroup(
            _deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_deckfilepanelLayout.createSequentialGroup()
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(_deckfilepanelLayout.createSequentialGroup()
                        .addComponent(_rename)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_recycle))
                    .addGroup(_deckfilepanelLayout.createSequentialGroup()
                        .addComponent(_showtextdeck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_transfer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_alias))
                    .addComponent(_decksab)
                    .addGroup(_deckfilepanelLayout.createSequentialGroup()
                        .addComponent(_savedeck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_loaddeck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_newdeck))
                    .addComponent(_deckname, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addGroup(_deckfilepanelLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_tags)))
                .addContainerGap())
        );
        _deckfilepanelLayout.setVerticalGroup(
            _deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_deckfilepanelLayout.createSequentialGroup()
                .addComponent(_deckname, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_tags, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_decksab)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_savedeck)
                    .addComponent(_loaddeck)
                    .addComponent(_newdeck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_showtextdeck)
                    .addComponent(_transfer)
                    .addComponent(_alias))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_deckfilepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_rename)
                    .addComponent(_recycle))
                .addContainerGap())
        );

        _insertchar.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _insertchar.setText("調整");
        _insertchar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _insertcharActionPerformed(evt);
            }
        });

        _options4.setText(bundle.getString("Option.txt")); // NOI18N
        _options4.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));

        _leftclickshowcustomizedicon4.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _leftclickshowcustomizedicon4.setSelected(true);
        _leftclickshowcustomizedicon4.setText(bundle.getString("ShowImage.txt")); // NOI18N
        _leftclickshowcustomizedicon4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _leftclickshowcustomizediconActionPerformed(evt);
            }
        });
        _options4.add(_leftclickshowcustomizedicon4);

        jMenuBar6.add(_options4);

        jMenu6.setText(bundle.getString("Exit.txt")); // NOI18N
        jMenu6.setActionCommand(bundle.getString("Exit.txt")); // NOI18N
        jMenu6.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));

        closeeditor5.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        closeeditor5.setText(bundle.getString("CloseDeckEditor.txt")); // NOI18N
        closeeditor5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeeditorActionPerformed(evt);
            }
        });
        jMenu6.add(closeeditor5);

        quit5.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        quit5.setText(bundle.getString("EndGame.txt")); // NOI18N
        quit5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitActionPerformed(evt);
            }
        });
        jMenu6.add(quit5);

        jMenuBar6.add(jMenu6);

        setJMenuBar(jMenuBar6);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(setleader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(remove3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(remove2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(remove1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(add3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(add2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(add1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_clistsortnum)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_clistsorttype)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(_amount)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_chartab, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(_allcheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_alluncheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_insertchar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                                .addComponent(_fitcharlv)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_filterpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(_deckfilepanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_deckfilepanel, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(add1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(add2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(add3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(remove1)
                                .addGap(2, 2, 2)
                                .addComponent(remove2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(remove3)
                                .addGap(18, 18, 18)
                                .addComponent(setleader))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(_clistsortnum)
                                    .addComponent(_clistsorttype)
                                    .addComponent(_amount)
                                    .addComponent(jLabel20))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_chartab, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_allcheck)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(_fitcharlv)
                                .addComponent(_insertchar))
                            .addComponent(_alluncheck)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_filterpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   private void _clistsorttypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__clistsorttypeItemStateChanged
       applyFilters();
       showDeckList(-1);
       saveSortPreference();
   }//GEN-LAST:event__clistsorttypeItemStateChanged

   private void _clistsortnumItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__clistsortnumItemStateChanged
   }//GEN-LAST:event__clistsortnumItemStateChanged

   private void _newdeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__newdeckActionPerformed
       newDeck();
}//GEN-LAST:event__newdeckActionPerformed

   private void _loaddeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__loaddeckActionPerformed
       loadDeck();

}//GEN-LAST:event__loaddeckActionPerformed

   private void _savedeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__savedeckActionPerformed
       saveDeck(false);

}//GEN-LAST:event__savedeckActionPerformed

   private void setleaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setleaderActionPerformed
       if (_decklist.getSelectedIndex() < 0) {
           return;
       }
       if (!mDeckList.get(_decklist.getSelectedIndex()).isCharacterCard()) {
           return;
       }
       mLeader = mDeckList.get(_decklist.getSelectedIndex());
       showDeckList(-

1);
   }//GEN-LAST:event_setleaderActionPerformed

   private void remove3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove3ActionPerformed
       removefromDeck(3);
   }//GEN-LAST:event_remove3ActionPerformed

   private void remove2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove2ActionPerformed
       removefromDeck(2);
   }//GEN-LAST:event_remove2ActionPerformed

   private void remove1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove1ActionPerformed
       removefromDeck(1);
   }//GEN-LAST:event_remove1ActionPerformed

   private void add3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add3ActionPerformed
       addtoDeck(3);
   }//GEN-LAST:event_add3ActionPerformed

   private void add2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add2ActionPerformed
       addtoDeck(2);
   }//GEN-LAST:event_add2ActionPerformed

   private void add1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add1ActionPerformed
       addtoDeck(1);
   }//GEN-LAST:event_add1ActionPerformed

   private void _searchtextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__searchtextActionPerformed
       applyFilters();
       _searchtext.setSelectionStart(0);
       _searchtext.setSelectionEnd(_searchtext.getText().length());
}//GEN-LAST:event__searchtextActionPerformed

   private void _searchtextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__searchtextFocusGained
       _searchtext.setSelectionStart(0);
       _searchtext.setSelectionEnd(_searchtext.getText().length());
}//GEN-LAST:event__searchtextFocusGained

   private void _cardnameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__cardnameFocusGained
       _cardname.setSelectionStart(0);
       _cardname.setSelectionEnd(_cardname.getText().length());
}//GEN-LAST:event__cardnameFocusGained

   private void _alluncheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__alluncheckActionPerformed
       disableEvent = true;
       for (int i = 0; i < mCharacterCount; ++i) {
           chars[i].setSelected(false);
       }
       disableEvent = false;

       applyFilters();
}//GEN-LAST:event__alluncheckActionPerformed

   private void _allcheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__allcheckActionPerformed
       for (int i = 0; i < mCharacterCount; ++i) {
           chars[i].setSelected(true);
       }

       disableEvent = true;
       if (!FTool.readConfig("originalcard").equals("true")) {
           for (int i = OCHAR_BEGIN_INDEX; i < mCharacterCount - 1; ++i) {
               chars[i].setSelected(false);
               chars[i].setVisible(false);
           }

       }
       disableEvent = false;

       applyFilters();
}//GEN-LAST:event__allcheckActionPerformed

   private void _cardnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__cardnameActionPerformed
       applyFilters();
       _cardname.setSelectionStart(0);
       _cardname.setSelectionEnd(_cardname.getText().length());
}//GEN-LAST:event__cardnameActionPerformed

   private void quitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitActionPerformed
       if (JOptionPane.showConfirmDialog(null, FTool.getLocale(0), FTool.getLocale(1), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
           System.exit(0);
       }
   }//GEN-LAST:event_quitActionPerformed

   private void closeeditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeeditorActionPerformed
       if (JOptionPane.showConfirmDialog(null, FTool.getLocale(90), "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
           this.setVisible(false);
           this.dispose();
       }
   }//GEN-LAST:event_closeeditorActionPerformed

private void _fitcharlvItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__fitcharlvItemStateChanged
    if (_fitcharlv.isSelected()) {
        HashMap<Integer, Integer> lvs = getDeckLV();
        for (int i = 1; i < mCharacterCount - 1; ++i) {
            chars[i].setSelected(lvs.containsKey(ORDER_ID_MAP[i]));
        }
        chars[0].setSelected(true);
        chars[mCharacterCount - 1].setSelected(true);
    }

    applyFilters();
}//GEN-LAST:event__fitcharlvItemStateChanged

private void _cardnumFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__cardnumFocusGained
    _cardnum.setSelectionStart(0);
    _cardnum.setSelectionEnd(8);
}//GEN-LAST:event__cardnumFocusGained

private void _cardnumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__cardnumActionPerformed
    applyFilters();
    _cardnum.setSelectionStart(0);
    _cardnum.setSelectionEnd(4);
}//GEN-LAST:event__cardnumActionPerformed

private void _applyadvancedfilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__applyadvancedfilterActionPerformed
    applyFilters();
    _advancedfilterdialog.setVisible(false);
}//GEN-LAST:event__applyadvancedfilterActionPerformed

private void _resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__resetActionPerformed
    atkmax.setText("");
    atkmin.setText("");
    cptmax.setText("");
    cptmin.setText("");
    hitmax.setText("");
    hitmin.setText("");
    type0.setSelected(true);
    type1.setSelected(true);
    type2.setSelected(true);
    type3.setSelected(true);
    type4.setSelected(true);
    type5.setSelected(true);
    type6.setSelected(true);
    type8.setSelected(true);
}//GEN-LAST:event__resetActionPerformed

private void CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelActionPerformed
    _advancedfilterdialog.setVisible(false);
}//GEN-LAST:event_CancelActionPerformed

private void _ver1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver1ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver1ItemStateChanged

private void _ver2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver2ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver2ItemStateChanged

private void _ver3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver3ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver3ItemStateChanged

private void _ver4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver4ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver4ItemStateChanged

private void _leftclickshowcustomizediconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__leftclickshowcustomizediconActionPerformed
    FTool.updateConfig("lclickshowpic", String.valueOf(_leftclickshowcustomizedicon4.isSelected()));
}//GEN-LAST:event__leftclickshowcustomizediconActionPerformed

private void _cardlistValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__cardlistValueChanged
    if (_cardlist.getSelectedIndex() >= 0) {
        showSelected(_cardlist);
    }
}//GEN-LAST:event__cardlistValueChanged

private void _decklistValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__decklistValueChanged
    if (_decklist.getSelectedIndex() >= 0) {
        showSelected(_decklist);
    }
}//GEN-LAST:event__decklistValueChanged

private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    if (FTool.deckeditorcardviewer != null) {
        FTool.deckeditorcardviewer.dispose();
    }
}//GEN-LAST:event_formWindowClosed

private void _showtextdeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__showtextdeckActionPerformed
// TODO add your handling code here:
    showTextDeck();
}//GEN-LAST:event__showtextdeckActionPerformed

private void _transferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__transferActionPerformed
// TODO add your handling code here:
    transfer();
}//GEN-LAST:event__transferActionPerformed

private void _ver5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver5ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver5ItemStateChanged

private void _veroriginalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__veroriginalActionPerformed
    applyFilters();
    FTool.updateConfig("showoriginalcards", String.valueOf(_veroriginal.isSelected()));
}//GEN-LAST:event__veroriginalActionPerformed

private void _resetspecifiedfilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__resetspecifiedfilterActionPerformed
    _cardnum.setText("");
    _cardname.setText("");
    _searchtext.setText("");
    _mpmax.setText("");
    _mpmin.setText("");
    _lvmax.setText("");
    _lvmin.setText("");
    _reset.doClick();
    applyFilters();
}//GEN-LAST:event__resetspecifiedfilterActionPerformed

private void _applyfilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__applyfilterActionPerformed
    applyFilters();
}//GEN-LAST:event__applyfilterActionPerformed

private void _showscfilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__showscfilterActionPerformed
    _advancedfilterdialog.setSize(350, 270);
    _advancedfilterdialog.setLocation(60, 80);
    _advancedfilterdialog.setVisible(true);
}//GEN-LAST:event__showscfilterActionPerformed

private void _usecolorsymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__usecolorsymbolActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__usecolorsymbolActionPerformed

private void _cardtextsearchhelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__cardtextsearchhelpActionPerformed
    showCardTextSearchHelp();
}//GEN-LAST:event__cardtextsearchhelpActionPerformed

private void _decklistKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__decklistKeyReleased
    synchronizeList(1);
    switch (evt.getKeyCode()) {
        case KeyEvent.VK_ADD:
        case KeyEvent.VK_RIGHT:
            addtoDeck(1);
            break;
        case KeyEvent.VK_SUBTRACT:
        case KeyEvent.VK_LEFT:
            removefromDeck(1);
            break;
        case KeyEvent.VK_L:
        case KeyEvent.VK_D:
            if (_decklist.getSelectedIndex() < 0) {
                return;
            }
            if (!mDeckList.get(_decklist.getSelectedIndex()).isCharacterCard()) {
                return;
            }
            mLeader = mDeckList.get(_decklist.getSelectedIndex());
            showDeckList(-1);
            break;
    }
}//GEN-LAST:event__decklistKeyReleased

private void _cardlistKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__cardlistKeyReleased
    synchronizeList(0);
    switch (evt.getKeyCode()) {
        case KeyEvent.VK_ADD:
        case KeyEvent.VK_RIGHT:
            addtoDeck(1);
            break;
        case KeyEvent.VK_SUBTRACT:
        case KeyEvent.VK_LEFT:
            removefromDeck(1);
            break;
        case KeyEvent.VK_L:
        case KeyEvent.VK_D:
            if (_decklist.getSelectedIndex() < 0) {
                return;
            }
            if (!mDeckList.get(_decklist.getSelectedIndex()).isCharacterCard()) {
                return;
            }
            mLeader = mDeckList.get(_decklist.getSelectedIndex());
            showDeckList(-1);
            break;
    }
}//GEN-LAST:event__cardlistKeyReleased

private void _useregexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__useregexpActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__useregexpActionPerformed

private void _renameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__renameActionPerformed
    saveDeck(true);
}//GEN-LAST:event__renameActionPerformed

private void _recycleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__recycleActionPerformed
    recycle();
}//GEN-LAST:event__recycleActionPerformed

private void _tagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__tagsActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__tagsActionPerformed

private void _tagsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__tagsFocusGained
    // TODO add your handling code here:
}//GEN-LAST:event__tagsFocusGained

private void _decknameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event__decknameFocusGained
    _deckname.setSelectionStart(0);
    _deckname.setSelectionEnd(_deckname.getText().length());
}//GEN-LAST:event__decknameFocusGained

private void _aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__aliasActionPerformed
    showAlias();
}//GEN-LAST:event__aliasActionPerformed

private void _addaliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__addaliasActionPerformed
    addAlias();
}//GEN-LAST:event__addaliasActionPerformed

private void _deletealiasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__deletealiasActionPerformed
    removeAlias();
}//GEN-LAST:event__deletealiasActionPerformed

private void _endaliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__endaliasActionPerformed
    _aliasdialog.setVisible(false);
}//GEN-LAST:event__endaliasActionPerformed

private void _aliasnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__aliasnameActionPerformed
    addAlias();
}//GEN-LAST:event__aliasnameActionPerformed

private void _insertcharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__insertcharActionPerformed
    fitCharCards();
}//GEN-LAST:event__insertcharActionPerformed

private void _char22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__char22ActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__char22ActionPerformed

private void _ver6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver6ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver6ItemStateChanged

private void _ver7ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver7ItemStateChanged
    applyFilters();
}//GEN-LAST:event__ver7ItemStateChanged

private void _allverItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__allverItemStateChanged
    checkOrUncheckAllVersion();
}//GEN-LAST:event__allverItemStateChanged
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
            FTool.updateConfig("deckeditor_x", String.valueOf(x));
            FTool.updateConfig("deckeditor_y", String.valueOf(y));
        }
    }, 1300);
}//GEN-LAST:event_formComponentMoved

    private void _ver4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__ver4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__ver4ActionPerformed

    private void _ver8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__ver8ItemStateChanged
        applyFilters();
    }//GEN-LAST:event__ver8ItemStateChanged

    private void _ver6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__ver6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__ver6ActionPerformed
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Cancel;
    private javax.swing.JTextPane SCArea;
    private javax.swing.JButton _addalias;
    private javax.swing.JDialog _advancedfilterdialog;
    private javax.swing.JButton _alias;
    private javax.swing.JDialog _aliasdialog;
    private javax.swing.JList _aliaslist;
    private javax.swing.JTextField _aliasname;
    private javax.swing.JButton _allcheck;
    private javax.swing.JButton _alluncheck;
    private javax.swing.JCheckBox _allver;
    private javax.swing.JLabel _amount;
    private javax.swing.JButton _applyadvancedfilter;
    private javax.swing.JButton _applyfilter;
    private javax.swing.JList _cardlist;
    private javax.swing.JTextField _cardname;
    private javax.swing.JTextField _cardnum;
    private javax.swing.JButton _cardtextsearchhelp;
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
    private javax.swing.ButtonGroup _clistsort;
    private javax.swing.JRadioButton _clistsortnum;
    private javax.swing.JRadioButton _clistsorttype;
    private javax.swing.JPanel _deckfilepanel;
    private javax.swing.JList _decklist;
    private javax.swing.JTextField _deckname;
    private javax.swing.JLabel _decksab;
    private javax.swing.JButton _deletealias;
    private javax.swing.JButton _endalias;
    private javax.swing.JPanel _filterbuttonpanel;
    private javax.swing.JPanel _filterpanel;
    private javax.swing.JCheckBox _fitcharlv;
    private javax.swing.JButton _insertchar;
    private javax.swing.JCheckBoxMenuItem _leftclickshowcustomizedicon4;
    private javax.swing.JButton _loaddeck;
    private javax.swing.JTextField _lvmax;
    private javax.swing.JTextField _lvmin;
    private javax.swing.JPanel _mplvfilterpanel;
    private javax.swing.JTextField _mpmax;
    private javax.swing.JTextField _mpmin;
    private javax.swing.JButton _newdeck;
    private javax.swing.JMenu _options4;
    private javax.swing.JPanel _originalcharpanel;
    private javax.swing.JButton _recycle;
    private javax.swing.JButton _rename;
    private javax.swing.JButton _reset;
    private javax.swing.JButton _resetspecifiedfilter;
    private javax.swing.JButton _savedeck;
    private javax.swing.JTextField _searchtext;
    private javax.swing.JPanel _selectionpanel;
    private javax.swing.JButton _showscfilter;
    private javax.swing.JButton _showtextdeck;
    private javax.swing.JTextField _tags;
    private javax.swing.JButton _transfer;
    private javax.swing.JCheckBox _usecolorsymbol;
    private javax.swing.JCheckBox _useregexp;
    private javax.swing.JCheckBox _ver1;
    private javax.swing.JCheckBox _ver2;
    private javax.swing.JCheckBox _ver3;
    private javax.swing.JCheckBox _ver4;
    private javax.swing.JCheckBox _ver5;
    private javax.swing.JCheckBox _ver6;
    private javax.swing.JCheckBox _ver7;
    private javax.swing.JCheckBox _ver8;
    private javax.swing.JCheckBox _veroriginal;
    private javax.swing.JPanel _versionpanel;
    private javax.swing.JButton add1;
    private javax.swing.JButton add2;
    private javax.swing.JButton add3;
    private javax.swing.JTextField atkmax;
    private javax.swing.JTextField atkmin;
    private javax.swing.JCheckBox char0;
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
    private javax.swing.JCheckBox char90;
    private javax.swing.JMenuItem closeeditor5;
    private javax.swing.JTextField cptmax;
    private javax.swing.JTextField cptmin;
    private javax.swing.JTextField hitmax;
    private javax.swing.JTextField hitmin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JCheckBox kind0;
    private javax.swing.JCheckBox kind1;
    private javax.swing.JCheckBox kind2;
    private javax.swing.JCheckBox kind3;
    private javax.swing.JMenuItem quit5;
    private javax.swing.JButton remove1;
    private javax.swing.JButton remove2;
    private javax.swing.JButton remove3;
    private javax.swing.JButton setleader;
    private javax.swing.JCheckBox type0;
    private javax.swing.JCheckBox type1;
    private javax.swing.JCheckBox type2;
    private javax.swing.JCheckBox type3;
    private javax.swing.JCheckBox type4;
    private javax.swing.JCheckBox type5;
    private javax.swing.JCheckBox type6;
    private javax.swing.JCheckBox type8;
    // End of variables declaration//GEN-END:variables
}
