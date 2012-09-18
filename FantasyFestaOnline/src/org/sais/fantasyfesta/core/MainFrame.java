/*
 * MainFrame.java
 *
 * Created on 2007/1/4 1:21
 */
package org.sais.fantasyfesta.core;

import org.sais.fantasyfesta.ui.CardViewer;
import org.sais.fantasyfesta.ui.MyCellRenderer;
import org.sais.fantasyfesta.ui.ListItem;
import org.sais.fantasyfesta.ui.JImagePanel;
import org.sais.fantasyfesta.multimedia.*;
import org.sais.fantasyfesta.tool.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import org.sais.fantasyfesta.autos.*;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.CharacterCard;
import org.sais.fantasyfesta.card.EventCard;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.card.cardlabel.*;
import org.sais.fantasyfesta.core.SwingUpdater.UpdateTask;
import org.sais.fantasyfesta.deck.DeckEditor;
import org.sais.fantasyfesta.deck.ChocolatsArchive;
import org.sais.fantasyfesta.deck.decFilter;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.district.RevealDistrict;
import org.sais.fantasyfesta.enums.EGameMode;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;

/**
 *
 * @author  Romulus
 */
public class MainFrame extends javax.swing.JFrame implements IMainUI, ActionListener {

    // Constants
    private final int cheight = 80;
    private final int cwidth = 55;
    private final int LANDSCAPE_SHIFT = 22;     //How many dots to slide standby cards
    private final int PORTRAIT_SHIFT = 30;
    private final int LEADER_ATTACHMENT_SHIFT = 14;
    // Core
    private GameCore core;
    // ListModels for tab
    private DefaultListModel HandModel = new DefaultListModel();
    private DefaultListModel LibModel = new DefaultListModel();
    private DefaultListModel GraveModel = new DefaultListModel();
    private DefaultListModel OppGraveModel = new DefaultListModel();
    private DefaultListModel RevealModel = new DefaultListModel();
    private CardLayout mCommandPanel;
    private boolean mChoosingAttach = false;     //Choosing attach target?
    private boolean mWholeLibPeeking = false;       //Need to shuffle after leaving lib tag?
    private RevealDistrict mRevealDistrict = new RevealDistrict();
    private BGMPlayable bgmplayer = new BGMPlayer();
    float alpha = 1.0f;
    Container container = getContentPane();
    public SwingUpdater mSwingUpdater;
    private Watcher mSelectedWatcher = null;

    public MainFrame(GameCore core) {
        // Background alpha
        if (FTool.readConfig("alpha") != null && !FTool.readConfig("alpha").equals("")) {
            alpha = Float.parseFloat(FTool.readConfig("alpha"));
            if (Float.isNaN(alpha) || alpha > 1.0) {
                alpha = 1.0f;
            }
        }

        this.core = core;
        initComponents();

        // Font and other UI configuration
        FTool.setMyFont(container, new Font(FTool.readConfig("font"), 0, 12));
        _turnPhase.setFont(new Font(FTool.readConfig("font"), 0, 14));
        FTool.setMyOpaque(LeftUp);
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(new ReadInnerFile("mainicon.jpg").u));

        initialize();
        clearField();

        changeBackground(java.util.ResourceBundle.getBundle("Global").getString("defaultscene.name"));

        if (_autorecord.isSelected()) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {

                @Override
                public void run() {
                    getCore().startRecord();
                    JOptionPane.showMessageDialog(MainFrame.this, "●REC");
                }
            }, 3000);
        }
        setupShutDown();

        this.setLocation(FTool.readIntegerConfig("mainframex", 0), FTool.readIntegerConfig("mainframey", 0));
        if (core.isWatcher()) {
            setWatchMode(core.isWatcher());
        }
    }

    private void initialize() {
        // The Timer to update Swing components
        mSwingUpdater = new SwingUpdater(this);

        _handList.setCellRenderer(new MyCellRenderer());
        _libList.setCellRenderer(new MyCellRenderer());
        _discardPileList.setCellRenderer(new MyCellRenderer());
        _oppDiscardPileList.setCellRenderer(new MyCellRenderer());
        RevealList.setCellRenderer(new MyCellRenderer());
        _handList.setModel(HandModel);
        _libList.setModel(LibModel);
        _discardPileList.setModel(GraveModel);
        _oppDiscardPileList.setModel(OppGraveModel);
        RevealList.setModel(RevealModel);

        if (core.isWatcher()) {
            _regionTab.remove(0);
            _regionTab.remove(0);
        }

        _ownLeader.setIcon(new ImageIcon(new ReadInnerFile("OwnChar.jpg").u));
        _oppLeader.setIcon(new ImageIcon(new ReadInnerFile("OppChar.jpg").u));
        _ownBattle.setIcon(new ImageIcon(new ReadInnerFile("OwnSpell.jpg").u));
        _ownBattleSU.setIcon(new ImageIcon(new ReadInnerFile("OwnSupport.jpg").u));
        _oppBattle.setIcon(new ImageIcon(new ReadInnerFile("OppSpell.jpg").u));
        _oppBattleSU.setIcon(new ImageIcon(new ReadInnerFile("OppSupport.jpg").u));
        _scene.setIcon(new ImageIcon(new ReadInnerFile("OwnSupport.jpg").u));
        _ownEvent.setIcon(new ImageIcon(new ReadInnerFile("OwnEvent.jpg").u));
        _oppEvent.setIcon(new ImageIcon(new ReadInnerFile("OppEvent.jpg").u));
        mCommandPanel = (CardLayout) Commands.getLayout();
        //Load and name command pages
      /*
         *All pages must be loaded and named here
         */
        mCommandPanel.addLayoutComponent(_handCommand, "hand");
        mCommandPanel.addLayoutComponent(lib, "lib");
        mCommandPanel.addLayoutComponent(_chooseAttach, "choose_attach");
        mCommandPanel.addLayoutComponent(battle_sab, "battle_sab");
        mCommandPanel.addLayoutComponent(cmd_reveal, "reveal");
        mCommandPanel.addLayoutComponent(_watching, "watching");
        mCommandPanel.addLayoutComponent(_recordview, "recordview");
        mCommandPanel.addLayoutComponent(_allgraveyard, "allgraveyard");
        mCommandPanel.addLayoutComponent(blank, "blank");
        if (core.isWatcher()) {
            mCommandPanel.show(Commands, "watching");
        } else {
            mCommandPanel.show(Commands, "hand");
        }

        _watcherping.setVisible(core.isWatcher());
        _pingmenu.setVisible(core.isWatcher());

        //Read Config
        if (!FTool.readConfig("autodraw").equals("")) {
            _autodraw.setSelected(Boolean.parseBoolean(FTool.readConfig("autodraw")));
        }
        if (!FTool.readConfig("firstmp").equals("")) {
            _firstmp.setSelected(Boolean.parseBoolean(FTool.readConfig("firstmp")));
        }
        if (!FTool.readConfig("randomscene").equals("")) {
            _randomscene.setSelected(Boolean.parseBoolean(FTool.readConfig("randomscene")));
        }
        if (!FTool.readConfig("mute").equals("")) {
            MuteMenu.setSelected(Boolean.parseBoolean(FTool.readConfig("mute")));
            FTool.mute = MuteMenu.isSelected();
        }
        if (!FTool.readConfig("dontrobfocus").equals("")) {
            _dontrobfocus.setSelected(FTool.readBooleanConfig("dontrobfocus", true));
        }
        if (!FTool.readConfig("verbosereplay").equals("")) {
            _verbosereplay.setSelected(Boolean.parseBoolean(FTool.readConfig("verbosereplay")));
        }
        if (!FTool.readConfig("watchersreplay").equals("")) {
            _watchersreplay.setSelected(Boolean.parseBoolean(FTool.readConfig("watchersreplay")));
        }
        if (!FTool.readConfig("customizedicon").equals("")) {
            CardImageSetter.sUseCardImage = Boolean.parseBoolean(FTool.readConfig("customizedicon"));
            FTool.cardviewer = new CardViewer();
        }
        if (!FTool.readConfig("externbgmplayer").equals("")) {
            if (Boolean.parseBoolean(FTool.readConfig("externbgmplayer"))) {
                if (!FTool.readConfig("externbgmplayerpath").equals("")) {
                    File f = new File(FTool.readConfig("externbgmplayerpath"));
                    if (f.exists()) {
                        bgmplayer = new ExternBGMPlayerManager(FTool.readConfig("externbgmplayerpath"));
                    }
                }
            }
        }
        if (!FTool.readConfig("autorecord").equals("")) {
            _autorecord.setSelected(Boolean.parseBoolean(FTool.readConfig("autorecord")));
        }
        if (!FTool.readConfig("hidetimer").equals("")) {
            _hidetimer.setSelected(Boolean.parseBoolean(FTool.readConfig("hidetimer")));
        }
        refreshDeckHistory();

        if (core.getMode() == EGameMode.RECORDVIEW) {
            showCommandPanel("recordview");
        }

        ((DefaultCaret) _messagebox.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }

    public GameCore getCore() {
        return core;
    }

    @Override
    public void refreshDeckHistory() {
        JMenuItem[] lastdeck = new JMenuItem[5];
        lastdeck[0] = _lastdeck1;
        lastdeck[1] = _lastdeck2;
        lastdeck[2] = _lastdeck3;
        lastdeck[3] = _lastdeck4;
        lastdeck[4] = _lastdeck5;
        for (int i = 0; i < 5; ++i) {
            String str = FTool.readConfig("lastdeck" + String.valueOf(i + 1));
            if (str == null || str.equals("")) {
                lastdeck[i].setVisible(false);
            } else {
                lastdeck[i].setVisible(true);
                lastdeck[i].setText(str.split("\t")[0]);
            }
        }
    }

    @Override
    public void clearField() {
        removeSceneLabel(SupportCard.newNull());
        mRevealDistrict.clear();
        _ownBattle.setVisible(false);
        _ownBattleSU.setVisible(false);
        _oppBattle.setVisible(false);
        _oppBattleSU.setVisible(false);
        _ownEvent.setVisible(false);
        _oppEvent.setVisible(false);
        _scene.setVisible(false);
        _ownReserved.removeAll();
        _oppReserved.removeAll();
        _ownActivated.removeAll();
        _oppActivated.removeAll();
        _ownLeaderAttach.removeAll();
        _oppLeaderAttach.removeAll();
        _handList.removeAll();
        _libList.removeAll();
        _discardPileList.removeAll();
        _oppDiscardPileList.removeAll();
        _ownReserved.repaint();
        _oppReserved.repaint();
        _ownActivated.repaint();
        _oppActivated.repaint();
        _ownLeaderAttach.repaint();
        _oppLeaderAttach.repaint();
        _regionTab.setSelectedIndex(0);
    }

    @Override
    public GameTimer.IGameTimerCallback getTimerUpdateListener() {
        return new GameTimer.IGameTimerCallback() {

            @Override
            public void time(String[] timeStrings) {
                _owntimer.setText(timeStrings[0]);
                _opptimer.setText(timeStrings[1]);
            }
        };
    }

    @Override
    public void showCard(CardInfo info) {   //Translate flaged text into colored text
        FTool.showCard(info, SCArea);
    }

    @Override
    public void showTurn(GameInformation gi) {
        if (gi.isAttackPlayer(EPlayer.ICH)) {
            _turnPhase.setText("T" + gi.getTurn() + " " + FTool.getLocale(26) + FTool.getLocale(15 + gi.getPhase().ordinal()) + FTool.getLocale(192));
            _turnPhase.setForeground(Color.blue);
            setTimerTurn(EPlayer.ICH);
        } else {
            _turnPhase.setText("T" + gi.getTurn() + "  " + FTool.getLocale(27) + FTool.getLocale(15 + gi.getPhase().ordinal()) + FTool.getLocale(192));
            _turnPhase.setForeground(Color.red);
            setTimerTurn(EPlayer.OPP);
        }
    }

    @Override
    public void setTimerTurn(EPlayer ep) {
        if (_hidetimer.isSelected()) {
            _owntimer.setVisible(false);
            _opptimer.setVisible(false);
            return;
        }
        core.setTimerActivePlayer(ep);
        if (ep == EPlayer.ICH) {
            _owntimer.setForeground(Color.YELLOW);
            _owntimer.setBackground(Color.BLACK);
            _owntimer.setOpaque(true);
            _opptimer.setForeground(Color.BLACK);
            _opptimer.setOpaque(false);
        } else {
            _owntimer.setForeground(Color.BLACK);
            _owntimer.setOpaque(false);
            _opptimer.setForeground(Color.YELLOW);
            _opptimer.setBackground(Color.BLACK);
            _opptimer.setOpaque(true);
        }
    }

    /**
     * Push a CardLabel into Panel.
     * @param label
     * @param controller
     * @param region
     * @param size
     * @param isAttached
     */
    @Override
    public void addLabel(CardLabel label, int size) {
        Card card = label.getCard();
        ERegion region = card.getRegion();
        JPanel panel = getPanel(card.getController(), region);
        if (panel == null) {
            return;
        }
        Dimension p;
        int shift;
        boolean extend = false;

        switch (region) {
            case ACTIVATED:
                p = new Dimension(cwidth, cheight);
                shift = PORTRAIT_SHIFT;
                if (size > 9) {
                    extend = true;
                }
                break;
            case RESERVED:
            default:
                p = new Dimension(cheight, cwidth);
                shift = LANDSCAPE_SHIFT;
                if (size > 11) {
                    extend = true;
                }
                break;
            case LEADER_ATTACHMENTS:
                p = new Dimension(cwidth, cheight);
                shift = LEADER_ATTACHMENT_SHIFT;
                if (size > 4) {
                    extend = true;
                }
                break;
        }

        label.setSize(p);

        panel.add(label);

        label.getParent().setComponentZOrder(label, 0);
        label.setLocation(5 + shift * (size - 1), 5);
        CardImageSetter.set(label, card.getInfo(), card.getController(), card.getRegion() == ERegion.RESERVED ? JLabel.HORIZONTAL : JLabel.VERTICAL);

        if (extend) {
            panel.setPreferredSize(new Dimension(panel.getWidth() + shift, panel.getHeight()));
        }

        if (label instanceof SpellLabel) {
            SpellLabel sp = ((SpellLabel) label);
            if (sp.getCard().isAttached()) {
                sp.setAttachmentSize(cwidth, cheight);
                if (region == ERegion.RESERVED) {
                    sp.setAttachmentLocation(label.getX() + (cheight - cwidth) / 2, label.getY());
                } else {
                    sp.setAttachmentLocation(label.getX(), label.getY() + 13);
                }
                panel.add(sp.getAttachLabel());
            }
        }

        if (extend) {
            panel.setPreferredSize(new Dimension(panel.getWidth() + shift, panel.getHeight()));
        }

        panel.repaint();
    }

    @Override
    public void removeLabel(CardLabel label, EPlayer controller, ERegion region) {
        JPanel panel = getPanel(controller, region);
        if (panel != null) {
            if (label instanceof SpellLabel) {
                SpellLabel sp = ((SpellLabel) label);
                if (sp.getCard().isAttached()) {
                    panel.remove(sp.getAttachLabel());
                }
            }

            panel.remove(label);
            repaintPanel(panel, controller, region);
        }
    }

    private JPanel getPanel(EPlayer controller, ERegion region) {
        JPanel ret = null;
        switch (controller) {
            case ICH:
            default:
                switch (region) {
                    case ACTIVATED:
                        return _ownActivated;
                    case RESERVED:
                        return _ownReserved;
                    case LEADER_ATTACHMENTS:
                        return _ownLeaderAttach;
                }
                break;
            case OPP:
                switch (region) {
                    case ACTIVATED:
                        return _oppActivated;
                    case RESERVED:
                        return _oppReserved;
                    case LEADER_ATTACHMENTS:
                        return _oppLeaderAttach;
                }
                break;
        }
        return null;
    }

    private void repaintPanel(JPanel pane, EPlayer controller, ERegion region) {
        CardSet set;
        int shift;
        switch (region) {
            case ACTIVATED:
                set = core.getGameInformation().getField(controller).getActivated();
                shift = PORTRAIT_SHIFT;
                break;
            case RESERVED:
                set = core.getGameInformation().getField(controller).getReserved();
                shift = LANDSCAPE_SHIFT;
                break;
            case LEADER_ATTACHMENTS:
                set = core.getGameInformation().getField(controller).getLeaderAttachment();
                shift = LEADER_ATTACHMENT_SHIFT;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        for (int i = 0; i < set.size(); ++i) {
            CardLabel label = ((Card) set.get(i)).getLabel();
            label.getParent().setComponentZOrder(label, 0);
            label.setLocation(5 + shift * i, 5);
            if (label instanceof SpellLabel) {
                SpellLabel sp = (SpellLabel) label;
                if (region == ERegion.RESERVED) {
                    sp.setAttachmentLocation(sp.getX() + (cheight - cwidth) / 2, sp.getY());
                } else {
                    sp.setAttachmentLocation(sp.getX(), sp.getY() + 13);
                }
            }
            pane.setPreferredSize(new Dimension(pane.getWidth() - shift, pane.getHeight()));
        }
        pane.repaint();
    }

    @Override
    public void showBattleValue(EnumMap<EPlayer, BattleValues> battleValues) {
        if (core.getGameInformation().isAttackPlayer(EPlayer.ICH)) {
            _ownpow.setText(battleValues.get(EPlayer.ICH).getAttackValue());
            _opppow.setText(battleValues.get(EPlayer.OPP).getInterceptValue());
        } else {
            _ownpow.setText(battleValues.get(EPlayer.ICH).getInterceptValue());
            _opppow.setText(battleValues.get(EPlayer.OPP).getAttackValue());
        }
        _ownfaith.setText(battleValues.get(EPlayer.ICH).getFaith());
        _ownhit.setText(battleValues.get(EPlayer.ICH).getHit());
        _ownevasion.setText(battleValues.get(EPlayer.ICH).getEvasion());
        _oppfaith.setText(battleValues.get(EPlayer.OPP).getFaith());
        _opphit.setText(battleValues.get(EPlayer.OPP).getHit());
        _oppevasion.setText(battleValues.get(EPlayer.OPP).getEvasion());
    }

    private boolean changeBattleSab() {
        return changeBattleSab(true);
    }

    private boolean changeBattleSab(boolean playmes) {
        String[] values = new String[10];
        values[0] = _ownpow.getText();
        values[1] = _ownfaith.getText();
        values[2] = _ownhit.getText();
        values[3] = _ownevasion.getText();
        values[4] = _opppow.getText();
        values[5] = _oppfaith.getText();
        values[6] = _opphit.getText();
        values[7] = _oppevasion.getText();
        values[8] = String.valueOf(_border.isSelected());
        values[9] = null;
        if (playmes) {
            noLogPlayMessage(FTool.parseLocale(33, core.getGameInformation().myName()));
        }
        return core.changeBattleValues(values, true);
    }

    @Override
    public void showCommandPanel(String tag) {
        mCommandPanel.show(Commands, tag);
    }

    @Override
    public void setBattleCardLabel(SpellLabel label) {
        SpellCard card = label.getCard();
        JLabel bc, bcsu;
        if (card.getController() == EPlayer.ICH) {
            bc = _ownBattle;
            bcsu = _ownBattleSU;
        } else {
            bc = _oppBattle;
            bcsu = _oppBattleSU;
        }

        if (CardImageSetter.sUseCardImage) {
            CardImageSetter.set(bc, card.getInfo(), card.getController(), JLabel.VERTICAL);
            if (card.isAttached()) {
                CardImageSetter.set(bcsu, card.getAttachedDontThrow().getInfo(), card.getAttachedDontThrow().getController(), JLabel.VERTICAL);
            }
        }

        bc.setVisible(true);
        bc.setToolTipText(card.getName());

        if (card.isAttached()) {
            bcsu.setVisible(true);
            bcsu.setToolTipText(card.getAttachedDontThrow().getName());
        }

        if (card.getController() == EPlayer.ICH) {
            FTool.playSound("combat.wav");
            _ownSpell.setText(card.getName());
        } else {
            _oppSpell.setText(card.getName());
        }

        if (core.getGameInformation().isIchAttackPlayer()) {
            _ownpowlabel.setText(FTool.getLocale(24));
            _opppowlabel.setText(FTool.getLocale(25));
        } else {
            _ownpowlabel.setText(FTool.getLocale(25));
            _opppowlabel.setText(FTool.getLocale(24));
        }
    }

    @Override
    public void removeBattleCardLabel(SpellLabel label) {
        if (label.getCard().isIchControl()) {
            _ownBattle.setVisible(false);
            _ownBattleSU.setVisible(false);
        } else {
            _oppBattle.setVisible(false);
            _oppBattleSU.setVisible(false);
        }
    }

    public void battleCancel() {
        if (!core.getGameInformation().isIchAttackPlayer() && core.getGameInformation().getField(EPlayer.ICH).getBattleCard().isNull()) {
            _through.doClick();
        }

        _ownhit.setText("-99");
        _opphit.setText("-99");
        _changebattlesab.doClick();
        _battlego.doClick();
    }

    @Override
    public void postBattle() {
        _ownBattle.setVisible(false);
        _ownBattleSU.setVisible(false);
        _oppBattle.setVisible(false);
        _oppBattleSU.setVisible(false);
    }

    @Override
    public void through(boolean active) {
        if (active) {
            _ownBattle.setVisible(false);
            _ownBattleSU.setVisible(false);
            setTimerTurn(EPlayer.OPP);
            _ownpow.setText("0");
            _ownhit.setText("-1");
            _ownevasion.setText(String.valueOf(core.getGameInformation().getField(EPlayer.ICH).getLeader().getInfo().getEvasionValue()));
        } else {
            _oppBattle.setVisible(false);
            _oppBattleSU.setVisible(false);
            setTimerTurn(EPlayer.ICH);
            _opppow.setText("0");
            _opphit.setText("-1");
            _oppevasion.setText(String.valueOf(core.getGameInformation().getField(EPlayer.OPP).getLeader().getInfo().getEvasionValue()));
        }
    }

    @Override
    public void resetBattleSab() {
        _ownSpell.setText("");
        _ownpow.setText("0");
        _ownhit.setText("0");
        _ownevasion.setText("0");
        _oppSpell.setText("");
        _opppow.setText("0");
        _opphit.setText("0");
        _oppevasion.setText("0");
        _border.setSelected(false);
    }

    @Override
    public void setEventLabel(EventCard card) {
        JLabel label = card.getController() == EPlayer.ICH ? _ownEvent : _oppEvent;
        switch (card.getController()) {
            case ICH:
                _ownEvent.setToolTipText(card.getName());
                CardImageSetter.set(_ownEvent, card.getInfo(), card.getController(), JLabel.VERTICAL);
                _ownEvent.setVisible(true);
                setTimerTurn(EPlayer.OPP);
                break;
            case OPP:
                break;
        }
        label.setToolTipText(card.getName());
        CardImageSetter.set(label, card.getInfo(), card.getController(), JLabel.VERTICAL);
        label.setVisible(true);
        setTimerTurn(FTool.rev(card.getController()));
    }

    @Override
    public void removeEventLabel(EventCard card) {
        if (card.isIchControl()) {
            _ownEvent.setVisible(false);
        } else {
            _oppEvent.setVisible(false);
        }
    }

    @Override
    public void setSceneLabel(SupportCard card) {
        _scene.setToolTipText(card.getName());
        CardImageSetter.set(_scene, card.getInfo(), card.getOwner(), JLabel.VERTICAL);
        _scene.setVisible(true);
    }

    @Override
    public void removeSceneLabel(SupportCard card) {
        bgmplayer.stopplay();
        changeBackground(java.util.ResourceBundle.getBundle("Global").getString("defaultscene.name"));
        _scene.setVisible(false);
    }

    @Override
    public void changeBGM(String cardname) {
        bgmplayer.stopplay();
        if (FTool.readBooleanConfig("mute", false)) {
            return;
        }
        try {
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream("SceneBGM.txt"), "Unicode"));
            String buff = fr.readLine();
            int limit = 0;
            while (buff != null && limit < 300) {
                if (cardname.equals(buff)) {
                    String fname = fr.readLine();
                    if (fname.equals("")) {
                        fr.close();
                        fr = new BufferedReader(new InputStreamReader(new FileInputStream("SceneBGM.txt"), "Unicode"));
                        fr.readLine();
                        buff = fr.readLine();
                        if (buff.equals("")) {
                            return;
                        } else {
                            fname = buff;
                        }
                    }

                    bgmplayer.play(fname);
                }

                buff = fr.readLine();
                ++limit;
            }

            fr.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "No BGM File.");
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void changeBackground(String cardname) {
        if (!new File("background.txt").exists() || cardname.length() == 0) {
            return;
        }
        if (!FTool.readBooleanConfig("background", false)) {
            return;
        }
        try {
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream("background.txt"), "Unicode"));
            String buff = fr.readLine();
            int limit = 0;
            while (buff != null && limit < 300) {
                if (cardname.equals(buff)) {
                    String fname = fr.readLine();
                    if ("".equals(fname)) {
                        fr.close();
                        fr = new BufferedReader(new InputStreamReader(new FileInputStream("background.txt"), "Unicode"));
                        fr.readLine();
                        buff = fr.readLine();
                        if ("".equals(buff)) {
                            return;
                        } else {
                            fname = buff;
                        }
                    }
                    ((JImagePanel) LeftUp).setImage(fname);
                    ((JImagePanel) LeftUp).setAlpha(alpha);
                    ((JImagePanel) LeftUp).repaint();

                }

                buff = fr.readLine();
                ++limit;
            }

            fr.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "No Background File.");
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void startChooseAttach() {
        showCommandPanel("choose_attach");
        mChoosingAttach = true;
    }

    @Override
    public void stopAttach() {
        mChoosingAttach = false;
    }

    @Override
    public boolean isChoosingAttach() {
        return mChoosingAttach;
    }

    @Override
    public boolean doAttach(SupportCard support, SpellLabel target) {
        JPanel pane;
        SpellCard tc = target.getCard();

        switch (tc.getRegion()) {
            case RESERVED:
                if (tc.isIchControl()) {
                    pane = _ownReserved;
                } else {
                    pane = _oppReserved;
                }
                break;
            case ACTIVATED:
                if (tc.isIchControl()) {
                    pane = _ownActivated;
                } else {
                    pane = _oppActivated;
                }
                break;
            default:
                return false;
        }

        pane.add(support.getLabel());
        target.setAttachmentSize(cwidth, cheight);

        switch (tc.getRegion()) {
            case RESERVED:
                target.setAttachmentLocation(target.getX() + (cheight - cwidth) / 2, target.getY());
                break;
            case ACTIVATED:
                target.setAttachmentLocation(target.getX(), target.getY() + 13);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean unAttach(SpellLabel label, SupportLabel supportLabel) {
        JPanel pane;
        SpellCard card = label.getCard();

        switch (card.getRegion()) {
            case RESERVED:
                if (card.isIchControl()) {
                    pane = _ownReserved;
                } else {
                    pane = _oppReserved;
                }
                break;
            case ACTIVATED:
                if (card.isIchControl()) {
                    pane = _ownActivated;
                } else {
                    pane = _oppActivated;
                }
                break;
            default:
                return false;
        }

        if (supportLabel != null) {
            pane.remove(supportLabel);
        }
        pane.repaint();

        return true;
    }

    //Flow Commands
    /**
     * Show region of unilabels.
     * 
     * @param region
     */
    @Override
    public void showRegion(final ERegion region) {
        UpdateTask u = new UpdateTask(UpdateTask.TaskList.showRegion);
        u.addParameter(region);
        mSwingUpdater.add(u);

        // hand sharing
        if (region == ERegion.HAND) {
            core.deliverHand();
        }

    }

    @SuppressWarnings("unchecked")
    public void swing_showRegion(final ERegion region) {
        if (core.isWatcher() && (region == ERegion.HAND || region == ERegion.LIBRARY)) {
            return;
        }
        DefaultListModel mod;
        final JList list;
        CardSet<Card> cset;

        switch (region) {
            case HAND:
                mod = HandModel;
                list = _handList;
                cset = core.getGameInformation().getField(EPlayer.ICH).getHand();
                break;
            case LIBRARY:
                _regionTab.setSelectedIndex(1);
                int amount = FTool.safeParseInt(_peeklibamount.getText());
                showLibrary(amount);
                return;
            case DISCARD_PILE:
                mod = GraveModel;
                list = _discardPileList;
                cset = core.getGameInformation().getField(EPlayer.ICH).getDiscardPile();
                break;
            case OPP_DISCARD_PILE:
                mod = OppGraveModel;
                list = _oppDiscardPileList;
                cset = core.getGameInformation().getField(EPlayer.OPP).getDiscardPile();
                break;
            case REVEAL:
                mod = RevealModel;
                list = RevealList;
                cset = mRevealDistrict.getSet();
                break;
            case WATCHER:
            default:
                mod = new DefaultListModel();
                list = _WatcherList;
                for (Watcher w : core.getWatchers()) {
                    mod.addElement(w.getName());
                }
                list.setModel(mod);
                list.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        int index = list.locationToIndex(evt.getPoint());
                        if (index < 0) {
                            return;
                        }
                        if ((evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown()) && core.isHost()) {
                            _watcherpopup.show(MainFrame.this, evt.getXOnScreen() - MainFrame.this.getX(), evt.getYOnScreen() - MainFrame.this.getY());
                            mSelectedWatcher = core.getWatchers().get(index);
                        }
                    }
                });
                return;
        }

        ArrayList<ListItem> litem = new ArrayList<ListItem>();
        for (Card c : Collections.synchronizedList(cset)) {
            litem.add(new ListItem(c.getInfo().getBackgroundColor(), c.getInfo().getName(), (UniLabel) c.getLabel()));
        }

        do {
            mod.removeAllElements();
            for (ListItem item : litem) {
                mod.addElement(item);
            }
        } while (list.locationToIndex(new Point(1, 1)) < 0 && mod.size() > 0);

        final JList jlist = list;
        if (list.getToolTipText() == null) {
            list.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int index = jlist.locationToIndex(evt.getPoint());
                    if (index < 0) {
                        return;
                    }
                    showCard(((ListItem) (jlist.getModel().getElementAt(index))).label.getInfo());
                    /*if (mCore.isWatcher()) {
                    return;
                    }*/
                    try {
                        if (evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown()) {
                            ((ListItem) (jlist.getModel().getElementAt(index))).label.doMouseEvent(evt);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        list.setToolTipText("");
    }

    @Override
    public int getLibraryPeekingAmount() {
        return FTool.safeParseInt(_peeklibamount.getText());
    }

    @Override
    public void setLibraryPeekingAmount(int amount) {
        _peeklibamount.setText(String.valueOf(amount));
    }

    public void showLibrary(int amount) {    //Watch the inverse show order! LibraryCommand() will receive the correct order of card array!
        UpdateTask u = new UpdateTask(UpdateTask.TaskList.showLibrary);
        u.addParameter(new Integer(amount));
        mSwingUpdater.add(u);
    }

    public void swing_showLibrary(int amount) {
        if (core.isWatcher()) {
            return;
        }
        ArrayList<Card> slib = new ArrayList<Card>(core.getGameInformation().getField(EPlayer.ICH).getLibrary());
        if (amount > slib.size()) {
            amount = slib.size();
        }

        Collections.reverse(slib);
        LibModel.removeAllElements();
        for (int i = 0; i < amount; ++i) {
            Card c = slib.get(i);
            LibModel.addElement(new ListItem(c.getInfo().getBackgroundColor(), c.getName(), (UniLabel) c.getLabel()));
        }

        if (_libList.getToolTipText() == null) {
            _libList.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    try {
                        int index = _libList.locationToIndex(evt.getPoint());
                        if (index < 0) {
                            return;
                        }
                        if (evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown()) {
                            ((ListItem) (_libList.getModel().getElementAt(index))).label.doMouseEvent(evt);
                        }
                        showCard(((ListItem) (_libList.getModel().getElementAt(index))).label.getInfo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        _libList.setToolTipText("");
    }

    //NetWork Commands
    @Override
    public void displayName() {
        GameInformation gi = core.getGameInformation();
        _myname.setText(gi.getPlayer(EPlayer.ICH).getName() + " // " + gi.getPlayer(EPlayer.ICH).getDeck().getDeckName());
        _oppname.setText(gi.getPlayer(EPlayer.OPP).getName() + " // " + gi.getPlayer(EPlayer.OPP).getDeck().getDeckName());
    }

    public void setCharacterLabels(EPlayer ep) {
        JLabel[] labels = new JLabel[4];
        if (ep == EPlayer.ICH) {
            labels = new JLabel[]{OwnChars0, OwnChars1, OwnChars2, OwnChars3};
        } else {
            labels = new JLabel[]{OppChars0, OppChars1, OppChars2, OppChars3};
        }

        if (core.getGameInformation().getField(ep).getLeader().isNull()) {
            labels[0].setText("???");
            labels[1].setText("???");
            labels[2].setText("???");
            labels[3].setText("???");
            return;
        }

        PlayField f = core.getGameInformation().getField(ep);

        for (int i = 0; i < 4; ++i) {
            String shortname;
            if (i == 0) {
                shortname = f.getLeader().getName().split(FTool.getLocale(81))[0];
            } else {
                shortname = f.getCharsExceptLeader().get(i - 1).getName().split(FTool.getLocale(81))[0];
            }
            labels[i].setText(shortname);
        }

        f.getLeader().getLabel().setPopupMenu(f.getChars());

        if (CardImageSetter.sUseCardImage) {
            CardImageSetter.set(ep == EPlayer.ICH ? _ownLeader : _oppLeader, f.getLeader().getInfo(), f.getLeader().getController(), JLabel.VERTICAL);
        }
    }

    @Override
    public void insertMessage(String message, Color color) {
        insertNoLogMessage(message, color);
        core.writeReplay(message);
    }

    @Override
    public void insertNoLogMessage(String message, Color color) {
        try {
            // Decide here, as new messages may be too many then stop the auto-scroll
            boolean scrollToBottom = 
                    _messagebox.getVisibleRect().y + _messagebox.getVisibleRect().height > _messagebox.getHeight() - _messagebox.getVisibleRect().height / 2;
                    
            SimpleAttributeSet set = new SimpleAttributeSet();
            SCArea.setCharacterAttributes(set, true);
            Document doc = _messagebox.getStyledDocument();
            StyleConstants.setForeground(set, color);
            doc.insertString(doc.getLength(), message + '\n', set);
          
            if (scrollToBottom) {
                _messagebox.setCaretPosition(_messagebox.getDocument().getLength() - 1);
            }
            if (!_dontrobfocus.isSelected()) {
                _chatbox.requestFocus();
            }
            FTool.playSound("beep.wav");
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void refreshCounter() {
        if (OppChars0.getText().equals("???")) {
            return;
        }
        GameInformation gi = core.getGameInformation();
        OwnHP.setText(String.valueOf(gi.getPlayer(EPlayer.ICH).getHP()));
        OppHP.setText(String.valueOf(gi.getPlayer(EPlayer.OPP).getHP()));
        OwnMP.setText(String.valueOf(gi.getPlayer(EPlayer.ICH).getSP()));
        OppMP.setText(String.valueOf(gi.getPlayer(EPlayer.OPP).getSP()));
        OwnHandCnt.setText(String.valueOf(gi.getField(EPlayer.ICH).getHand().size()));
        OppHandCnt.setText(String.valueOf(gi.getField(EPlayer.OPP).getHand().size()));
        OwnLibCnt.setText(String.valueOf(gi.getField(EPlayer.ICH).getLibrary().size()));
        OppLibCnt.setText(String.valueOf(gi.getField(EPlayer.OPP).getLibrary().size()));
        _owngravecnt.setText(String.valueOf(gi.getField(EPlayer.ICH).getDiscardPile().size()));
        _oppgravecnt.setText(String.valueOf(gi.getField(EPlayer.OPP).getDiscardPile().size()));
    }

    @Override
    public void setBattleSab(String[] s) {
        _ownpow.setText(s[0]);
        _ownfaith.setText(s[1]);
        _ownhit.setText(s[2]);
        _ownevasion.setText(s[3]);
        _opppow.setText(s[4]);
        _oppfaith.setText(s[5]);
        _opphit.setText(s[6]);
        _oppevasion.setText(s[7]);

        if (core.isWatcher()) {
            mCommandPanel.show(Commands, "battle_sab");
        }
    }

    @Override
    public void noLogPlayMessage(String message) {
        if (message.length() == 0 || core.isWatcher()) {
            return;
        }
        insertNoLogMessage(message, Color.BLACK);
        core.send("$NOLOGMSG:" + message);
    }

    @Override
    public void playMessage(String message) {
        if (message.length() == 0 || core.isWatcher()) {
            return;
        }
        insertMessage(message, Color.BLACK);
        core.send("$MSG:" + message);
    }

    public void setWatchMode(boolean isWatcher) {
        _endturn.setEnabled(!isWatcher);
        _through.setEnabled(!isWatcher);
        _pass.setEnabled(!isWatcher);
        _commandmenu.setEnabled(!isWatcher);
        _newgame.setEnabled(!isWatcher);
        _startgamemenu.setEnabled(!isWatcher);
        _changebattlesab.setEnabled(!isWatcher);
        _border.setEnabled(!isWatcher);
        _watcherping.setEnabled(isWatcher);
        _watcherping.setVisible(isWatcher);
        _pingmenu.setEnabled(isWatcher);
        _pingmenu.setVisible(isWatcher);
        _record.setEnabled(false);
        switch (core.getMode()) {
            case WATCHER:
                _battlego.setEnabled(false);
                mCommandPanel.show(Commands, "watching");
                break;
            case RECORDVIEW:
                _battlego.setEnabled(true);
                _battlego.setText(_next.getText());
                mCommandPanel.show(Commands, "recordview");
                break;
        }
    }

    @Override
    public void showCharacters(EPlayer ep) {
        GameInformation gi = core.getGameInformation();
        CharacterCard leader = gi.getField(ep).getLeader();
        if (leader.isNull()) {
            return;
        }
        JLabel[] chlabels;
        JLabel leaderlabel;
        if (ep == EPlayer.ICH) {
            chlabels = new JLabel[]{OwnChars0, OwnChars1, OwnChars2, OwnChars3};
            leaderlabel = _ownLeader;
        } else {
            chlabels = new JLabel[]{OppChars0, OppChars1, OppChars2, OppChars3};
            leaderlabel = _oppLeader;
        }
        chlabels[0].setText(leader.getInfo().getShortName());
        ArrayList<CharacterCard> others = gi.getField(ep).getCharsExceptLeader();
        for (int i = 0; i < 3; ++i) {
            chlabels[i + 1].setText(others.get(i).getInfo().getShortName());
        }
        CardImageSetter.set(leaderlabel, leader.getInfo(), ep, JLabel.VERTICAL);
        leaderlabel.setToolTipText(leader.getName());
        mCommandPanel.show(Commands, "hand");
    }

    @Override
    public void setRevealCards(String cards) {
        mRevealDistrict.clear();

        // Get region
        String[] buf = cards.split("//");
        mRevealDistrict.setRegion(ERegion.valueOf(buf[0]));

        if (buf.length > 1) {
            // Cards
            buf = buf[1].split(" ");
            for (String c : buf) {
                Card card = CardDatabase.getInfo(Integer.parseInt(c)).createCard(EPlayer.OPP, EPlayer.OPP, new NullDistrict());
                mRevealDistrict.add(card);
                card.updateLabel(core);
            }
        }

        if (!core.isWatcher()) {
            mCommandPanel.show(Commands, "reveal");
            _regionTab.setSelectedIndex(4);
        } else {
            _regionTab.setSelectedIndex(2);
        }

        showRegion(ERegion.REVEAL);
    }

    @Override
    public void loadDeck(String rootdir) {
        JFileChooser chooser = null;
        if (rootdir == null) {
            chooser = new JFileChooser();
            String last = FTool.readConfig("lastdeckfolder");
            if (last.length() == 0) {
                last = "deck";
            }
            if (new File(last).exists()) {
                chooser.setCurrentDirectory(new File(last));
            } else {
                chooser.setCurrentDirectory(new File("."));
            }

            chooser.setAcceptAllFileFilterUsed(true);
            chooser.addChoosableFileFilter(new decFilter());
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) {
                return;
            }
        }

        File file;
        if (rootdir == null) {
            file = chooser.getSelectedFile();
        } else {
            file = new File(rootdir);
        }
        FTool.updateConfig("lastdeckfolder", file.getParent());
        core.loadDeck(file);
    }

    //ALWAYS SET actionindex before taking ANY actions!
    //ALWAYS CALL ActionDone() after you finish ANY actions expect some exceptions!
    @Override
    public void actionDone() {
        switch (core.getMode()) {
            case WATCHER:
                if (core.isBattling()) {
                    mCommandPanel.show(Commands, "battle_sab");
                } else {
                    mCommandPanel.show(Commands, "watching");
                }
                return;
            case RECORDVIEW:
                mCommandPanel.show(Commands, "recordview");
                return;
        }

        switch (_regionTab.getSelectedIndex()) {
            case 0:     //hand OR battlesab
                if (core.isBattling()) {
                    mCommandPanel.show(Commands, "battle_sab");
                } else {
                    mCommandPanel.show(Commands, "hand");
                }
                showRegion(ERegion.HAND);
                break;
            case 1:
                mCommandPanel.show(Commands, "lib");
                showRegion(ERegion.LIBRARY);
                break;
            case 2:
                mCommandPanel.show(Commands, "allgraveyard");
                showRegion(ERegion.DISCARD_PILE);
                break;
            case 3:
                showRegion(ERegion.OPP_DISCARD_PILE);
                break;
            case 4:
                mCommandPanel.show(Commands, "reveal");
                showRegion(ERegion.REVEAL);
                break;
        }

        core.sendAndRefreshCounter();
    }

    @Override
    public void peekWholeLibrary(String extramessage) {
        int amount = core.getGameInformation().getField(EPlayer.ICH).getLibrary().size();
        _peeklibamount.setText(String.valueOf(amount));
        mWholeLibPeeking = true;
        core.peekLibrary(amount, extramessage);
    }

    @Override
    public void updateField() {
        GameInformation gi = core.getGameInformation();
        for (EPlayer ep : EPlayer.values()) {
            PlayField f = gi.getField(ep);
            for (int i = 0; i < f.getActivated().size(); ++i) {
                addLabel(f.getActivated().get(i).getLabel(), i + 1);
            }
            for (int i = 0; i < f.getReserved().size(); ++i) {
                addLabel(f.getReserved().get(i).getLabel(), i + 1);
            }
            for (int i = 0; i < f.getLeaderAttachment().size(); ++i) {
                addLabel(f.getLeaderAttachment().get(i).getLabel(), i + 1);
            }
            setCharacterLabels(ep);
            if (!f.getEventCard().isNull()) {
                setEventLabel(f.getEventCard());
            }
            if (!f.getBattleCard().isNull()) {
                setBattleCardLabel(f.getBattleCard().getSpellLabel());
            }
        }
        if (!gi.getScene().isNull()) {
            setSceneLabel(gi.getScene());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mSwingUpdater == null) {
            return;
        }
        UpdateTask task = mSwingUpdater.pop();
        if (task == null) {
            return;
        }
        switch (task.task) {
            case showLibrary:
                swing_showLibrary((Integer) task.parameters.get(0));
                break;
            case showRegion:
                swing_showRegion((ERegion) task.parameters.get(0));
                break;
        }

    }

    @Override
    public void stopPeekingLibrary() {
        mWholeLibPeeking = false;
        _regionTab.setSelectedIndex(0);
    }

    private void setupShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                bgmplayer.close();
                System.out.println("Close From Runtime ");
            }
        });

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _watcherpopup = new javax.swing.JPopupMenu();
        _watcheroption = new javax.swing.JMenuItem();
        Right = new javax.swing.JPanel();
        _regionTab = new javax.swing.JTabbedPane();
        HandPane = new javax.swing.JScrollPane();
        _handList = new javax.swing.JList();
        LibPane = new javax.swing.JScrollPane();
        _libList = new javax.swing.JList();
        GravePane = new javax.swing.JScrollPane();
        _discardPileList = new javax.swing.JList();
        OppGravePane = new javax.swing.JScrollPane();
        _oppDiscardPileList = new javax.swing.JList();
        RevealPane = new javax.swing.JScrollPane();
        RevealList = new javax.swing.JList();
        _WatcherPane = new javax.swing.JScrollPane();
        _WatcherList = new javax.swing.JList();
        cmdGlobal = new javax.swing.JPanel();
        _endturn = new javax.swing.JButton();
        _through = new javax.swing.JButton();
        _pass = new javax.swing.JButton();
        CommandContainer = new javax.swing.JPanel();
        Commands = new javax.swing.JPanel();
        lib = new javax.swing.JPanel();
        lib_TopToGra = new javax.swing.JButton();
        _peekLib = new javax.swing.JButton();
        _peeklibamount = new javax.swing.JTextField();
        lib_Shuffle = new javax.swing.JButton();
        lib_reveal = new javax.swing.JButton();
        _lib_TopToBottom = new javax.swing.JButton();
        _libBottomToTop = new javax.swing.JButton();
        _chooseAttach = new javax.swing.JPanel();
        _attachCancel = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        _handCommand = new javax.swing.JPanel();
        hand_Throw = new javax.swing.JButton();
        handdraw = new javax.swing.JButton();
        reveal = new javax.swing.JButton();
        _handalltolib = new javax.swing.JButton();
        blank = new javax.swing.JPanel();
        battle_sab = new javax.swing.JPanel();
        _oppSpell = new javax.swing.JLabel();
        _ownSpell = new javax.swing.JLabel();
        _opppowlabel = new javax.swing.JLabel();
        _ownpowlabel = new javax.swing.JLabel();
        _ownpow = new javax.swing.JTextField();
        _opppow = new javax.swing.JTextField();
        _opphitlabel = new javax.swing.JLabel();
        _ownhitlabel = new javax.swing.JLabel();
        _opphit = new javax.swing.JTextField();
        _ownhit = new javax.swing.JTextField();
        _oppdodgelabel = new javax.swing.JLabel();
        _owndodgelabel = new javax.swing.JLabel();
        _oppevasion = new javax.swing.JTextField();
        _ownevasion = new javax.swing.JTextField();
        _border = new javax.swing.JCheckBox();
        _changebattlesab = new javax.swing.JButton();
        _battlego = new javax.swing.JButton();
        _oppfaithlabel = new javax.swing.JLabel();
        _ownfaithlabel = new javax.swing.JLabel();
        _oppfaith = new javax.swing.JTextField();
        _ownfaith = new javax.swing.JTextField();
        cmd_reveal = new javax.swing.JPanel();
        revealend = new javax.swing.JButton();
        _watching = new javax.swing.JPanel();
        _blanklabel = new javax.swing.JLabel();
        _allgraveyard = new javax.swing.JPanel();
        _allgravetolib = new javax.swing.JButton();
        _recordview = new javax.swing.JPanel();
        _next = new javax.swing.JButton();
        _turnskip = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        SCArea = new javax.swing.JTextPane();
        LeftUp = new JImagePanel();
        ;
        _ownLeader = new javax.swing.JLabel();
        _oppLeader = new javax.swing.JLabel();
        OppChars0 = new javax.swing.JLabel();
        OwnChars0 = new javax.swing.JLabel();
        _oppBattle = new javax.swing.JLabel();
        _scene = new javax.swing.JLabel();
        _oppEvent = new javax.swing.JLabel();
        _ownEvent = new javax.swing.JLabel();
        OwnChars1 = new javax.swing.JLabel();
        OwnChars2 = new javax.swing.JLabel();
        OwnChars3 = new javax.swing.JLabel();
        OppChars1 = new javax.swing.JLabel();
        OppChars2 = new javax.swing.JLabel();
        OppChars3 = new javax.swing.JLabel();
        _ownmplabel = new javax.swing.JLabel();
        OwnMP = new javax.swing.JTextField();
        lable10 = new javax.swing.JLabel();
        OwnHandCnt = new javax.swing.JLabel();
        OwnLibCnt = new javax.swing.JLabel();
        label3 = new javax.swing.JLabel();
        OwnHP = new javax.swing.JTextField();
        _ownhplabel = new javax.swing.JLabel();
        _myname = new javax.swing.JLabel();
        label13 = new javax.swing.JLabel();
        OppMP = new javax.swing.JTextField();
        OppHP = new javax.swing.JTextField();
        label12 = new javax.swing.JLabel();
        _opptimer = new javax.swing.JLabel();
        label11 = new javax.swing.JLabel();
        label4 = new javax.swing.JLabel();
        OppHandCnt = new javax.swing.JLabel();
        OppLibCnt = new javax.swing.JLabel();
        _turnPhase = new javax.swing.JLabel();
        _ownBattle = new javax.swing.JLabel();
        _ownBattleSU = new javax.swing.JLabel();
        _oppBattleSU = new javax.swing.JLabel();
        _OwnStandbyScroll = new javax.swing.JScrollPane();
        _ownReserved = new javax.swing.JPanel();
        _OwnActivatedScroll = new javax.swing.JScrollPane();
        _ownActivated = new javax.swing.JPanel();
        _OwnLeaderSUScroll = new javax.swing.JScrollPane();
        _ownLeaderAttach = new javax.swing.JPanel();
        _OppActivatedScroll = new javax.swing.JScrollPane();
        _oppActivated = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _oppReserved = new javax.swing.JPanel();
        _OppLeaderSUScroll = new javax.swing.JScrollPane();
        _oppLeaderAttach = new javax.swing.JPanel();
        label5 = new javax.swing.JLabel();
        _oppgravecnt = new javax.swing.JLabel();
        label6 = new javax.swing.JLabel();
        _owngravecnt = new javax.swing.JLabel();
        _oppname = new javax.swing.JLabel();
        _owntimer = new javax.swing.JLabel();
        LeftDown = new javax.swing.JPanel();
        _chatbox = new javax.swing.JTextField();
        SendChat = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        _messagebox = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        _startgamemenu = new javax.swing.JMenu();
        _loaddeck = new javax.swing.JMenuItem();
        _randomloaddeck = new javax.swing.JMenuItem();
        _newgame = new javax.swing.JMenuItem();
        _decidefirstlast = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        _lastdeck1 = new javax.swing.JMenuItem();
        _lastdeck2 = new javax.swing.JMenuItem();
        _lastdeck3 = new javax.swing.JMenuItem();
        _lastdeck4 = new javax.swing.JMenuItem();
        _lastdeck5 = new javax.swing.JMenuItem();
        _commandmenu = new javax.swing.JMenu();
        HPUp = new javax.swing.JMenuItem();
        HPDown = new javax.swing.JMenuItem();
        MPUp = new javax.swing.JMenuItem();
        MPDown = new javax.swing.JMenuItem();
        _passmenu = new javax.swing.JMenuItem();
        _throughmenu = new javax.swing.JMenuItem();
        _endturnmenu = new javax.swing.JMenuItem();
        _drawcard = new javax.swing.JMenuItem();
        _shuffleMenu = new javax.swing.JMenuItem();
        _ping = new javax.swing.JMenuItem();
        _pass2 = new javax.swing.JMenuItem();
        _mulligan = new javax.swing.JMenuItem();
        _battlecancel = new javax.swing.JMenuItem();
        _passclock = new javax.swing.JMenuItem();
        Option = new javax.swing.JMenu();
        MuteMenu = new javax.swing.JCheckBoxMenuItem();
        _autodraw = new javax.swing.JCheckBoxMenuItem();
        _firstmp = new javax.swing.JCheckBoxMenuItem();
        _dontrobfocus = new javax.swing.JCheckBoxMenuItem();
        _verbosereplay = new javax.swing.JCheckBoxMenuItem();
        _randomscene = new javax.swing.JCheckBoxMenuItem();
        _watchersreplay = new javax.swing.JCheckBoxMenuItem();
        _hidetimer = new javax.swing.JCheckBoxMenuItem();
        _showdeckeditor = new javax.swing.JMenuItem();
        _showdeckmanager = new javax.swing.JMenuItem();
        _pingmenu = new javax.swing.JMenu();
        _watcherping = new javax.swing.JMenuItem();
        _record = new javax.swing.JMenu();
        _startrecord = new javax.swing.JMenuItem();
        _endrecord = new javax.swing.JMenuItem();
        _autorecord = new javax.swing.JCheckBoxMenuItem();
        EndGame = new javax.swing.JMenu();
        ToTitle = new javax.swing.JMenuItem();
        _exit = new javax.swing.JMenuItem();

        _watcheroption.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _watcheroption.setText("手札を共有");
        _watcheroption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _watcheroptionActionPerformed(evt);
            }
        });
        _watcherpopup.add(_watcheroption);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("MainFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Right.setPreferredSize(new java.awt.Dimension(220, 595));
        Right.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        _regionTab.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        _regionTab.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("panel.font"), 0, 12));
        _regionTab.setPreferredSize(new java.awt.Dimension(200, 158));
        _regionTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _regionTabMouseClicked(evt);
            }
        });
        _regionTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                _regionTabStateChanged(evt);
            }
        });

        _handList.setBackground(new java.awt.Color(204, 255, 255));
        _handList.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        HandPane.setViewportView(_handList);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("MainFrame"); // NOI18N
        _regionTab.addTab(bundle.getString("Hand.txt"), HandPane); // NOI18N

        _libList.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        LibPane.setViewportView(_libList);

        _regionTab.addTab(bundle.getString("Deck.txt"), LibPane); // NOI18N

        _discardPileList.setBackground(new java.awt.Color(204, 204, 204));
        _discardPileList.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        GravePane.setViewportView(_discardPileList);

        _regionTab.addTab(bundle.getString("Graveyard.txt"), GravePane); // NOI18N

        OppGravePane.setBackground(new java.awt.Color(255, 255, 204));

        _oppDiscardPileList.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        OppGravePane.setViewportView(_oppDiscardPileList);

        _regionTab.addTab(bundle.getString("OppGraveyard.txt"), OppGravePane); // NOI18N

        RevealList.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        RevealPane.setViewportView(RevealList);

        _regionTab.addTab(bundle.getString("Public.txt"), RevealPane); // NOI18N

        _WatcherPane.setViewportView(_WatcherList);

        _regionTab.addTab(bundle.getString("Audience.txt"), _WatcherPane); // NOI18N

        Right.add(_regionTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, -1, 210));
        _regionTab.getAccessibleContext().setAccessibleName("");

        cmdGlobal.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(0, 153, 51)));
        cmdGlobal.setPreferredSize(new java.awt.Dimension(200, 55));
        cmdGlobal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        _endturn.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _endturn.setText(bundle.getString("TurnEnd.txt")); // NOI18N
        _endturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _endturnActionPerformed(evt);
            }
        });
        cmdGlobal.add(_endturn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 5, -1, 20));

        _through.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _through.setText(bundle.getString("Through.txt")); // NOI18N
        _through.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _throughActionPerformed(evt);
            }
        });
        cmdGlobal.add(_through, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, 20));

        _pass.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _pass.setText(bundle.getString("PASS.txt")); // NOI18N
        _pass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _passActionPerformed(evt);
            }
        });
        cmdGlobal.add(_pass, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, -1, 20));

        Right.add(cmdGlobal, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 590, -1, -1));

        CommandContainer.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 255, 51)));
        CommandContainer.setPreferredSize(new java.awt.Dimension(200, 130));
        CommandContainer.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Commands.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 255, 51)));
        Commands.setPreferredSize(new java.awt.Dimension(200, 130));
        Commands.setLayout(new java.awt.CardLayout());

        lib_TopToGra.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        lib_TopToGra.setText(bundle.getString("TopToGrave.txt")); // NOI18N
        lib_TopToGra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lib_TopToGraActionPerformed(evt);
            }
        });

        _peekLib.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _peekLib.setText(bundle.getString("Look.txt")); // NOI18N
        _peekLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _peekLibActionPerformed(evt);
            }
        });

        _peeklibamount.setText("0");

        lib_Shuffle.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        lib_Shuffle.setText("シャッフル");
        lib_Shuffle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lib_ShuffleActionPerformed(evt);
            }
        });

        lib_reveal.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        lib_reveal.setText(bundle.getString("ShowDeck.txt")); // NOI18N
        lib_reveal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lib_revealActionPerformed(evt);
            }
        });

        _lib_TopToBottom.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _lib_TopToBottom.setText(bundle.getString("ToBottom.txt")); // NOI18N
        _lib_TopToBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lib_TopToBottomActionPerformed(evt);
            }
        });

        _libBottomToTop.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _libBottomToTop.setText("底→上");
        _libBottomToTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _libBottomToTopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout libLayout = new javax.swing.GroupLayout(lib);
        lib.setLayout(libLayout);
        libLayout.setHorizontalGroup(
            libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(libLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(libLayout.createSequentialGroup()
                        .addComponent(_peeklibamount, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_peekLib)
                            .addComponent(lib_reveal)))
                    .addGroup(libLayout.createSequentialGroup()
                        .addComponent(lib_TopToGra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_lib_TopToBottom))
                    .addGroup(libLayout.createSequentialGroup()
                        .addComponent(_libBottomToTop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lib_Shuffle)))
                .addContainerGap())
        );
        libLayout.setVerticalGroup(
            libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(libLayout.createSequentialGroup()
                .addGroup(libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(libLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(_peekLib, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lib_reveal, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, libLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(_peeklibamount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)))
                .addGap(10, 10, 10)
                .addGroup(libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lib_TopToGra, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_lib_TopToBottom, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(libLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_libBottomToTop, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lib_Shuffle, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        Commands.add(lib, "card5");

        _attachCancel.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _attachCancel.setText(bundle.getString("Cancel.txt")); // NOI18N
        _attachCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _attachCancelActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        jLabel2.setText(bundle.getString("SelectTarget.txt")); // NOI18N

        javax.swing.GroupLayout _chooseAttachLayout = new javax.swing.GroupLayout(_chooseAttach);
        _chooseAttach.setLayout(_chooseAttachLayout);
        _chooseAttachLayout.setHorizontalGroup(
            _chooseAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_chooseAttachLayout.createSequentialGroup()
                .addGroup(_chooseAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_chooseAttachLayout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(_attachCancel))
                    .addGroup(_chooseAttachLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel2)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        _chooseAttachLayout.setVerticalGroup(
            _chooseAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _chooseAttachLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(_attachCancel)
                .addGap(40, 40, 40))
        );

        Commands.add(_chooseAttach, "card11");

        hand_Throw.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        hand_Throw.setText(bundle.getString("Discard.txt")); // NOI18N
        hand_Throw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hand_ThrowActionPerformed(evt);
            }
        });

        handdraw.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        handdraw.setText(bundle.getString("Draw.txt")); // NOI18N
        handdraw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handdrawActionPerformed(evt);
            }
        });

        reveal.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        reveal.setText(bundle.getString("Show.txt")); // NOI18N
        reveal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revealActionPerformed(evt);
            }
        });

        _handalltolib.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _handalltolib.setText(bundle.getString("AllBackToDeck.txt")); // NOI18N
        _handalltolib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _handalltolibActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _handCommandLayout = new javax.swing.GroupLayout(_handCommand);
        _handCommand.setLayout(_handCommandLayout);
        _handCommandLayout.setHorizontalGroup(
            _handCommandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_handCommandLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(_handCommandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hand_Throw)
                    .addGroup(_handCommandLayout.createSequentialGroup()
                        .addComponent(handdraw)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reveal))
                    .addComponent(_handalltolib))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        _handCommandLayout.setVerticalGroup(
            _handCommandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_handCommandLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(hand_Throw)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_handalltolib)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_handCommandLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(handdraw)
                    .addComponent(reveal))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        Commands.add(_handCommand, "card13");

        javax.swing.GroupLayout blankLayout = new javax.swing.GroupLayout(blank);
        blank.setLayout(blankLayout);
        blankLayout.setHorizontalGroup(
            blankLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );
        blankLayout.setVerticalGroup(
            blankLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 128, Short.MAX_VALUE)
        );

        Commands.add(blank, "card14");

        battle_sab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        battle_sab.add(_oppSpell, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        battle_sab.add(_ownSpell, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, -1, -1));

        _opppowlabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _opppowlabel.setText(bundle.getString("OAtk.txt")); // NOI18N
        _opppowlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _opppowlabelMouseClicked(evt);
            }
        });
        battle_sab.add(_opppowlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, -1, -1));

        _ownpowlabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownpowlabel.setText(bundle.getString("Atk.txt")); // NOI18N
        _ownpowlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownpowlabelMouseClicked(evt);
            }
        });
        battle_sab.add(_ownpowlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, -1, -1));

        _ownpow.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownpow.setText("0");
        _ownpow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _ownpowActionPerformed(evt);
            }
        });
        battle_sab.add(_ownpow, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 20, -1));

        _opppow.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _opppow.setText("0");
        _opppow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _opppowActionPerformed(evt);
            }
        });
        battle_sab.add(_opppow, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 20, -1));

        _opphitlabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _opphitlabel.setText(bundle.getString("Hit.txt")); // NOI18N
        _opphitlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _opphitlabelMouseClicked(evt);
            }
        });
        battle_sab.add(_opphitlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, -1, -1));

        _ownhitlabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownhitlabel.setText(bundle.getString("Hit.txt")); // NOI18N
        _ownhitlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownhitlabelMouseClicked(evt);
            }
        });
        battle_sab.add(_ownhitlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 90, -1, -1));

        _opphit.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _opphit.setText("0");
        _opphit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _opphitActionPerformed(evt);
            }
        });
        battle_sab.add(_opphit, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 40, 20, -1));

        _ownhit.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownhit.setText("0");
        _ownhit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _ownhitActionPerformed(evt);
            }
        });
        battle_sab.add(_ownhit, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, 20, -1));

        _oppdodgelabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _oppdodgelabel.setText(bundle.getString("Dodge.txt")); // NOI18N
        _oppdodgelabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _oppdodgelabelMouseClicked(evt);
            }
        });
        battle_sab.add(_oppdodgelabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 20, -1, -1));

        _owndodgelabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _owndodgelabel.setText(bundle.getString("Dodge.txt")); // NOI18N
        _owndodgelabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _owndodgelabelMouseClicked(evt);
            }
        });
        battle_sab.add(_owndodgelabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, -1, -1));

        _oppevasion.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _oppevasion.setText("0");
        _oppevasion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _oppevasionActionPerformed(evt);
            }
        });
        battle_sab.add(_oppevasion, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 20, -1));

        _ownevasion.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownevasion.setText("0");
        _ownevasion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _ownevasionActionPerformed(evt);
            }
        });
        battle_sab.add(_ownevasion, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 20, -1));

        _border.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _border.setText(bundle.getString("Daredevil.txt")); // NOI18N
        _border.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _border.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _border.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _borderActionPerformed(evt);
            }
        });
        battle_sab.add(_border, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 80, -1, -1));

        _changebattlesab.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _changebattlesab.setText(bundle.getString("Change.txt")); // NOI18N
        _changebattlesab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _changebattlesabActionPerformed(evt);
            }
        });
        battle_sab.add(_changebattlesab, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, -1, -1));

        _battlego.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _battlego.setText(bundle.getString("OK.txt")); // NOI18N
        _battlego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _battlegoActionPerformed(evt);
            }
        });
        battle_sab.add(_battlego, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, -1, -1));

        _oppfaithlabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _oppfaithlabel.setText(bundle.getString("Faith.txt")); // NOI18N
        _oppfaithlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _oppfaithlabelMouseClicked(evt);
            }
        });
        battle_sab.add(_oppfaithlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        _ownfaithlabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownfaithlabel.setText(bundle.getString("Faith.txt")); // NOI18N
        _ownfaithlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownfaithlabelMouseClicked(evt);
            }
        });
        battle_sab.add(_ownfaithlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, -1));

        _oppfaith.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _oppfaith.setText("0");
        _oppfaith.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _oppfaithActionPerformed(evt);
            }
        });
        battle_sab.add(_oppfaith, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 20, -1));

        _ownfaith.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownfaith.setText("0");
        _ownfaith.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _ownfaithActionPerformed(evt);
            }
        });
        battle_sab.add(_ownfaith, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 20, -1));

        Commands.add(battle_sab, "card14");

        revealend.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        revealend.setText(bundle.getString("End.txt")); // NOI18N
        revealend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revealendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cmd_revealLayout = new javax.swing.GroupLayout(cmd_reveal);
        cmd_reveal.setLayout(cmd_revealLayout);
        cmd_revealLayout.setHorizontalGroup(
            cmd_revealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cmd_revealLayout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(revealend)
                .addContainerGap(79, Short.MAX_VALUE))
        );
        cmd_revealLayout.setVerticalGroup(
            cmd_revealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cmd_revealLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(revealend)
                .addContainerGap(54, Short.MAX_VALUE))
        );

        Commands.add(cmd_reveal, "card15");

        _blanklabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _blanklabel.setText(bundle.getString("WatchingMode.txt")); // NOI18N

        javax.swing.GroupLayout _watchingLayout = new javax.swing.GroupLayout(_watching);
        _watching.setLayout(_watchingLayout);
        _watchingLayout.setHorizontalGroup(
            _watchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_watchingLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(_blanklabel, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );
        _watchingLayout.setVerticalGroup(
            _watchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_watchingLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(_blanklabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(63, Short.MAX_VALUE))
        );

        Commands.add(_watching, "card16");

        _allgravetolib.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        _allgravetolib.setText(bundle.getString("AllBackToDeck.txt")); // NOI18N
        _allgravetolib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _allgravetolibActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _allgraveyardLayout = new javax.swing.GroupLayout(_allgraveyard);
        _allgraveyard.setLayout(_allgraveyardLayout);
        _allgraveyardLayout.setHorizontalGroup(
            _allgraveyardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_allgraveyardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(_allgravetolib)
                .addContainerGap(43, Short.MAX_VALUE))
        );
        _allgraveyardLayout.setVerticalGroup(
            _allgraveyardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_allgraveyardLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(_allgravetolib)
                .addContainerGap(78, Short.MAX_VALUE))
        );

        Commands.add(_allgraveyard, "card17");

        _next.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _next.setText("次へ");
        _next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _nextActionPerformed(evt);
            }
        });

        _turnskip.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _turnskip.setText("次ターン");
        _turnskip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _turnskipActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _recordviewLayout = new javax.swing.GroupLayout(_recordview);
        _recordview.setLayout(_recordviewLayout);
        _recordviewLayout.setHorizontalGroup(
            _recordviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_recordviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_turnskip)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(_next)
                .addGap(18, 18, 18))
        );
        _recordviewLayout.setVerticalGroup(
            _recordviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_recordviewLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(_recordviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_turnskip)
                    .addComponent(_next))
                .addContainerGap(85, Short.MAX_VALUE))
        );

        Commands.add(_recordview, "card10");

        CommandContainer.add(Commands, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        Right.add(CommandContainer, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, -1, -1));

        jScrollPane3.setPreferredSize(new java.awt.Dimension(200, 230));

        SCArea.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(102, 102, 255)));
        SCArea.setFont(new java.awt.Font(bundle.getString("txtfield.font"), 0, 12));
        jScrollPane3.setViewportView(SCArea);

        Right.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, 240));

        getContentPane().add(Right, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 0, -1, 650));

        LeftUp.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        LeftUp.setPreferredSize(new java.awt.Dimension(580, 500));
        LeftUp.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        _ownLeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OwnChar.jpg"))); // NOI18N
        _ownLeader.setToolTipText("");
        _ownLeader.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownLeaderMouseClicked(evt);
            }
        });
        LeftUp.add(_ownLeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 300, -1, -1));

        _oppLeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OppChar.jpg"))); // NOI18N
        _oppLeader.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _oppLeaderMouseClicked(evt);
            }
        });
        LeftUp.add(_oppLeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, -1, -1));

        OppChars0.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OppChars0.setForeground(new java.awt.Color(255, 0, 0));
        OppChars0.setText("ロード待ち");
        LeftUp.add(OppChars0, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        OwnChars0.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OwnChars0.setForeground(new java.awt.Color(255, 0, 0));
        OwnChars0.setText("ロード待ち");
        LeftUp.add(OwnChars0, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, -1, -1));

        _oppBattle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OppSpell.jpg"))); // NOI18N
        _oppBattle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _oppBattleMouseClicked(evt);
            }
        });
        LeftUp.add(_oppBattle, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 100, -1, -1));

        _scene.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OwnSupport.jpg"))); // NOI18N
        _scene.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _sceneMouseClicked(evt);
            }
        });
        LeftUp.add(_scene, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 210, -1, -1));

        _oppEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OppEvent.jpg"))); // NOI18N
        _oppEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _oppEventMouseClicked(evt);
            }
        });
        LeftUp.add(_oppEvent, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 200, -1, -1));

        _ownEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OwnEvent.jpg"))); // NOI18N
        _ownEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownEventMouseClicked(evt);
            }
        });
        LeftUp.add(_ownEvent, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 220, -1, -1));

        OwnChars1.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OwnChars1.setText("　");
        LeftUp.add(OwnChars1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, -1, -1));

        OwnChars2.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OwnChars2.setText("　");
        LeftUp.add(OwnChars2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, -1, -1));

        OwnChars3.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OwnChars3.setText("　");
        LeftUp.add(OwnChars3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, -1, -1));

        OppChars1.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OppChars1.setText("　");
        LeftUp.add(OppChars1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        OppChars2.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OppChars2.setText("　");
        LeftUp.add(OppChars2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        OppChars3.setFont(new java.awt.Font(bundle.getString("char.font"), 0, 12));
        OppChars3.setText("　");
        LeftUp.add(OppChars3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        _ownmplabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownmplabel.setText(bundle.getString("MP.msg")); // NOI18N
        _ownmplabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownmplabelMouseClicked(evt);
            }
        });
        LeftUp.add(_ownmplabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 330, -1, -1));

        OwnMP.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
        OwnMP.setText("0");
        OwnMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OwnMPActionPerformed(evt);
            }
        });
        LeftUp.add(OwnMP, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 330, 30, -1));

        lable10.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        lable10.setText("山札");
        LeftUp.add(lable10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, -1, -1));

        OwnHandCnt.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        OwnHandCnt.setText("0");
        LeftUp.add(OwnHandCnt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 370, 20, -1));

        OwnLibCnt.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        OwnLibCnt.setText("0");
        LeftUp.add(OwnLibCnt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 350, 20, -1));

        label3.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label3.setText(bundle.getString("Hand.msg")); // NOI18N
        LeftUp.add(label3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, -1, -1));

        OwnHP.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
        OwnHP.setText("0");
        OwnHP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OwnHPActionPerformed(evt);
            }
        });
        LeftUp.add(OwnHP, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 310, 30, -1));

        _ownhplabel.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _ownhplabel.setText(bundle.getString("HP.msg")); // NOI18N
        _ownhplabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownhplabelMouseClicked(evt);
            }
        });
        LeftUp.add(_ownhplabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, -1, -1));

        _myname.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _myname.setText(bundle.getString("OwnName.txt")); // NOI18N
        LeftUp.add(_myname, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, -1, -1));

        label13.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label13.setText(bundle.getString("MP.msg")); // NOI18N
        LeftUp.add(label13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        OppMP.setEditable(false);
        OppMP.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
        LeftUp.add(OppMP, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, 30, -1));

        OppHP.setEditable(false);
        OppHP.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
        OppHP.setText("0");
        LeftUp.add(OppHP, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, 30, -1));

        label12.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label12.setText(bundle.getString("HP.msg")); // NOI18N
        LeftUp.add(label12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        _opptimer.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _opptimer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        _opptimer.setText("00:00");
        _opptimer.setOpaque(true);
        LeftUp.add(_opptimer, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 40, -1));

        label11.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label11.setText(bundle.getString("Hand.msg")); // NOI18N
        LeftUp.add(label11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, -1, -1));

        label4.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label4.setText(bundle.getString("Deck.msg")); // NOI18N
        LeftUp.add(label4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        OppHandCnt.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        OppHandCnt.setText("0");
        LeftUp.add(OppHandCnt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 150, 20, -1));

        OppLibCnt.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        OppLibCnt.setText("0");
        LeftUp.add(OppLibCnt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 130, 20, -1));

        _turnPhase.setBackground(new java.awt.Color(255, 153, 204));
        _turnPhase.setFont(new java.awt.Font(java.util.ResourceBundle.getBundle("MainFrame").getString("phase.font"), 0, 22));
        _turnPhase.setForeground(new java.awt.Color(0, 153, 0));
        _turnPhase.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        _turnPhase.setText("T0");
        LeftUp.add(_turnPhase, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, -1, -1));

        _ownBattle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OwnSpell.jpg"))); // NOI18N
        _ownBattle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownBattleMouseClicked(evt);
            }
        });
        LeftUp.add(_ownBattle, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 300, -1, -1));

        _ownBattleSU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OwnSupport.jpg"))); // NOI18N
        _ownBattleSU.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _ownBattleSUMouseClicked(evt);
            }
        });
        LeftUp.add(_ownBattleSU, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 320, -1, -1));

        _oppBattleSU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/OppSupport.jpg"))); // NOI18N
        _oppBattleSU.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _oppBattleSUMouseClicked(evt);
            }
        });
        LeftUp.add(_oppBattleSU, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 120, -1, -1));

        _ownReserved.setPreferredSize(new java.awt.Dimension(340, 85));

        javax.swing.GroupLayout _ownReservedLayout = new javax.swing.GroupLayout(_ownReserved);
        _ownReserved.setLayout(_ownReservedLayout);
        _ownReservedLayout.setHorizontalGroup(
            _ownReservedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        _ownReservedLayout.setVerticalGroup(
            _ownReservedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 88, Short.MAX_VALUE)
        );

        _OwnStandbyScroll.setViewportView(_ownReserved);

        LeftUp.add(_OwnStandbyScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 405, 350, 90));

        _OwnActivatedScroll.setPreferredSize(new java.awt.Dimension(350, 100));

        _ownActivated.setPreferredSize(new java.awt.Dimension(340, 90));

        javax.swing.GroupLayout _ownActivatedLayout = new javax.swing.GroupLayout(_ownActivated);
        _ownActivated.setLayout(_ownActivatedLayout);
        _ownActivatedLayout.setHorizontalGroup(
            _ownActivatedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        _ownActivatedLayout.setVerticalGroup(
            _ownActivatedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 98, Short.MAX_VALUE)
        );

        _OwnActivatedScroll.setViewportView(_ownActivated);

        LeftUp.add(_OwnActivatedScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 300, -1, -1));

        _OwnLeaderSUScroll.setPreferredSize(new java.awt.Dimension(120, 90));

        _ownLeaderAttach.setPreferredSize(new java.awt.Dimension(115, 85));

        javax.swing.GroupLayout _ownLeaderAttachLayout = new javax.swing.GroupLayout(_ownLeaderAttach);
        _ownLeaderAttach.setLayout(_ownLeaderAttachLayout);
        _ownLeaderAttachLayout.setHorizontalGroup(
            _ownLeaderAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );
        _ownLeaderAttachLayout.setVerticalGroup(
            _ownLeaderAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 88, Short.MAX_VALUE)
        );

        _OwnLeaderSUScroll.setViewportView(_ownLeaderAttach);

        LeftUp.add(_OwnLeaderSUScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 405, -1, -1));

        _OppActivatedScroll.setPreferredSize(new java.awt.Dimension(350, 100));

        _oppActivated.setPreferredSize(new java.awt.Dimension(340, 90));

        javax.swing.GroupLayout _oppActivatedLayout = new javax.swing.GroupLayout(_oppActivated);
        _oppActivated.setLayout(_oppActivatedLayout);
        _oppActivatedLayout.setHorizontalGroup(
            _oppActivatedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        _oppActivatedLayout.setVerticalGroup(
            _oppActivatedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 98, Short.MAX_VALUE)
        );

        _OppActivatedScroll.setViewportView(_oppActivated);

        LeftUp.add(_OppActivatedScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 100, -1, -1));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(350, 90));

        _oppReserved.setPreferredSize(new java.awt.Dimension(345, 85));

        javax.swing.GroupLayout _oppReservedLayout = new javax.swing.GroupLayout(_oppReserved);
        _oppReserved.setLayout(_oppReservedLayout);
        _oppReservedLayout.setHorizontalGroup(
            _oppReservedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        _oppReservedLayout.setVerticalGroup(
            _oppReservedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 88, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(_oppReserved);

        LeftUp.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 5, -1, -1));

        _OppLeaderSUScroll.setPreferredSize(new java.awt.Dimension(120, 90));

        _oppLeaderAttach.setPreferredSize(new java.awt.Dimension(115, 85));

        javax.swing.GroupLayout _oppLeaderAttachLayout = new javax.swing.GroupLayout(_oppLeaderAttach);
        _oppLeaderAttach.setLayout(_oppLeaderAttachLayout);
        _oppLeaderAttachLayout.setHorizontalGroup(
            _oppLeaderAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );
        _oppLeaderAttachLayout.setVerticalGroup(
            _oppLeaderAttachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 88, Short.MAX_VALUE)
        );

        _OppLeaderSUScroll.setViewportView(_oppLeaderAttach);

        LeftUp.add(_OppLeaderSUScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 5, -1, -1));

        label5.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label5.setText(bundle.getString("Grave.msg")); // NOI18N
        LeftUp.add(label5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, -1, -1));

        _oppgravecnt.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _oppgravecnt.setText("0");
        LeftUp.add(_oppgravecnt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 20, -1));

        label6.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        label6.setText(bundle.getString("Grave.msg")); // NOI18N
        LeftUp.add(label6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, -1, -1));

        _owngravecnt.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _owngravecnt.setText("0");
        LeftUp.add(_owngravecnt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, 20, -1));

        _oppname.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _oppname.setText(bundle.getString("OppName.txt")); // NOI18N
        LeftUp.add(_oppname, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, -1, -1));

        _owntimer.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 12));
        _owntimer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        _owntimer.setText("00:00");
        _owntimer.setOpaque(true);
        LeftUp.add(_owntimer, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 40, -1));

        getContentPane().add(LeftUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        LeftDown.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        LeftDown.setPreferredSize(new java.awt.Dimension(580, 90));
        LeftDown.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        _chatbox.setFont(new java.awt.Font(bundle.getString("default.font"), 0, 14));
        _chatbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _chatboxActionPerformed(evt);
            }
        });
        LeftDown.add(_chatbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 500, 20));

        SendChat.setFont(new java.awt.Font(bundle.getString("button.font"), 0, 12));
        SendChat.setText(bundle.getString("SendMessage.txt")); // NOI18N
        SendChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendChatActionPerformed(evt);
            }
        });
        LeftDown.add(SendChat, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 110, 60, 20));

        _messagebox.setFont(new java.awt.Font(bundle.getString("chat.font"), 0, 12));
        jScrollPane2.setViewportView(_messagebox);

        LeftDown.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 560, 100));

        getContentPane().add(LeftDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 505, -1, 140));

        _startgamemenu.setText(bundle.getString("Game/Deck.txt")); // NOI18N
        _startgamemenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));

        _loaddeck.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        _loaddeck.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _loaddeck.setText(bundle.getString("LoadDeck.txt")); // NOI18N
        _loaddeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _loaddeckActionPerformed(evt);
            }
        });
        _startgamemenu.add(_loaddeck);

        _randomloaddeck.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        _randomloaddeck.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _randomloaddeck.setText("ランダムデッキロード");
        _randomloaddeck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _randomloaddeckActionPerformed(evt);
            }
        });
        _startgamemenu.add(_randomloaddeck);

        _newgame.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        _newgame.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _newgame.setText(bundle.getString("NewGame.txt")); // NOI18N
        _newgame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _newgameActionPerformed(evt);
            }
        });
        _startgamemenu.add(_newgame);

        _decidefirstlast.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD0, java.awt.event.InputEvent.CTRL_MASK));
        _decidefirstlast.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _decidefirstlast.setText("先後手決め");
        _decidefirstlast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _decidefirstlastActionPerformed(evt);
            }
        });
        _startgamemenu.add(_decidefirstlast);
        _startgamemenu.add(jSeparator1);

        _lastdeck1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.CTRL_MASK));
        _lastdeck1.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _lastdeck1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lastdeck1ActionPerformed(evt);
            }
        });
        _startgamemenu.add(_lastdeck1);

        _lastdeck2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.CTRL_MASK));
        _lastdeck2.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _lastdeck2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lastdeck2ActionPerformed(evt);
            }
        });
        _startgamemenu.add(_lastdeck2);

        _lastdeck3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.CTRL_MASK));
        _lastdeck3.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _lastdeck3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lastdeck3ActionPerformed(evt);
            }
        });
        _startgamemenu.add(_lastdeck3);

        _lastdeck4.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _lastdeck4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lastdeck4ActionPerformed(evt);
            }
        });
        _startgamemenu.add(_lastdeck4);

        _lastdeck5.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _lastdeck5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _lastdeck5ActionPerformed(evt);
            }
        });
        _startgamemenu.add(_lastdeck5);

        jMenuBar1.add(_startgamemenu);

        _commandmenu.setText(bundle.getString("Command.txt")); // NOI18N
        _commandmenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _commandmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _commandmenuActionPerformed(evt);
            }
        });

        HPUp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.CTRL_MASK));
        HPUp.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        HPUp.setText(bundle.getString("HP+1.txt")); // NOI18N
        HPUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HPUpActionPerformed(evt);
            }
        });
        _commandmenu.add(HPUp);

        HPDown.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.CTRL_MASK));
        HPDown.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        HPDown.setText(bundle.getString("HP-1.txt")); // NOI18N
        HPDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HPDownActionPerformed(evt);
            }
        });
        _commandmenu.add(HPDown);

        MPUp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.SHIFT_MASK));
        MPUp.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        MPUp.setText(bundle.getString("MP+1.txt")); // NOI18N
        MPUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MPUpActionPerformed(evt);
            }
        });
        _commandmenu.add(MPUp);

        MPDown.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.SHIFT_MASK));
        MPDown.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        MPDown.setText(bundle.getString("MP-1.txt")); // NOI18N
        MPDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MPDownActionPerformed(evt);
            }
        });
        _commandmenu.add(MPDown);

        _passmenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        _passmenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _passmenu.setText(bundle.getString("PASS.txt")); // NOI18N
        _passmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _passmenuActionPerformed(evt);
            }
        });
        _commandmenu.add(_passmenu);

        _throughmenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        _throughmenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _throughmenu.setText(bundle.getString("Through.txt")); // NOI18N
        _throughmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _throughmenuActionPerformed(evt);
            }
        });
        _commandmenu.add(_throughmenu);

        _endturnmenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        _endturnmenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _endturnmenu.setText(bundle.getString("TurnEnd.txt")); // NOI18N
        _endturnmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _endturnmenuActionPerformed(evt);
            }
        });
        _commandmenu.add(_endturnmenu);

        _drawcard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        _drawcard.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _drawcard.setText(bundle.getString("Draw.txt")); // NOI18N
        _drawcard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _drawcardActionPerformed(evt);
            }
        });
        _commandmenu.add(_drawcard);

        _shuffleMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        _shuffleMenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _shuffleMenu.setText("シャッフル");
        _shuffleMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _shuffleMenuActionPerformed(evt);
            }
        });
        _commandmenu.add(_shuffleMenu);

        _ping.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        _ping.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _ping.setText("Ping");
        _ping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _pingActionPerformed(evt);
            }
        });
        _commandmenu.add(_ping);

        _pass2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        _pass2.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _pass2.setText(bundle.getString("PASS_2.txt")); // NOI18N
        _pass2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _pass2ActionPerformed(evt);
            }
        });
        _commandmenu.add(_pass2);

        _mulligan.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        _mulligan.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _mulligan.setText("マリガン");
        _mulligan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mulliganActionPerformed(evt);
            }
        });
        _commandmenu.add(_mulligan);

        _battlecancel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        _battlecancel.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _battlecancel.setText("戦闘キャンセル");
        _battlecancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _battlecancelActionPerformed(evt);
            }
        });
        _commandmenu.add(_battlecancel);

        _passclock.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        _passclock.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _passclock.setText("クロック渡し");
        _passclock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _passclockActionPerformed(evt);
            }
        });
        _commandmenu.add(_passclock);

        jMenuBar1.add(_commandmenu);

        Option.setText(bundle.getString("Option.txt")); // NOI18N
        Option.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        Option.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OptionActionPerformed(evt);
            }
        });

        MuteMenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        MuteMenu.setText(bundle.getString("Mute.txt")); // NOI18N
        MuteMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MuteMenuActionPerformed(evt);
            }
        });
        Option.add(MuteMenu);

        _autodraw.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _autodraw.setSelected(true);
        _autodraw.setText(bundle.getString("AutoDraw.txt")); // NOI18N
        _autodraw.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _autodrawItemStateChanged(evt);
            }
        });
        Option.add(_autodraw);

        _firstmp.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _firstmp.setSelected(true);
        _firstmp.setText("先攻呪力あり");
        _firstmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _firstmpActionPerformed(evt);
            }
        });
        Option.add(_firstmp);

        _dontrobfocus.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _dontrobfocus.setText(bundle.getString("AutoFocusOff.txt")); // NOI18N
        _dontrobfocus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _dontrobfocusActionPerformed(evt);
            }
        });
        Option.add(_dontrobfocus);

        _verbosereplay.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _verbosereplay.setSelected(true);
        _verbosereplay.setText(bundle.getString("Replay.txt")); // NOI18N
        _verbosereplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _verbosereplayActionPerformed(evt);
            }
        });
        Option.add(_verbosereplay);

        _randomscene.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _randomscene.setText("ランダムシーン");
        _randomscene.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _randomsceneActionPerformed(evt);
            }
        });
        Option.add(_randomscene);

        _watchersreplay.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _watchersreplay.setText("観戦時リプレイを記録");
        _watchersreplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _watchersreplayActionPerformed(evt);
            }
        });
        Option.add(_watchersreplay);

        _hidetimer.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _hidetimer.setText("タイマー非表示");
        _hidetimer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _hidetimerActionPerformed(evt);
            }
        });
        Option.add(_hidetimer);

        _showdeckeditor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        _showdeckeditor.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _showdeckeditor.setText(bundle.getString("DeckEditor.txt")); // NOI18N
        _showdeckeditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _showdeckeditorActionPerformed(evt);
            }
        });
        Option.add(_showdeckeditor);

        _showdeckmanager.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SLASH, java.awt.event.InputEvent.CTRL_MASK));
        _showdeckmanager.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _showdeckmanager.setText("ショコラズアーカイブ");
        _showdeckmanager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _showdeckmanagerActionPerformed(evt);
            }
        });
        Option.add(_showdeckmanager);

        jMenuBar1.add(Option);

        _pingmenu.setText("Ping");
        _pingmenu.setEnabled(false);
        _pingmenu.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));

        _watcherping.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        _watcherping.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _watcherping.setText("Ping");
        _watcherping.setEnabled(false);
        _watcherping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _watcherpingActionPerformed(evt);
            }
        });
        _pingmenu.add(_watcherping);

        jMenuBar1.add(_pingmenu);

        _record.setText("レコード");
        _record.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N

        _startrecord.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        _startrecord.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _startrecord.setText("レコード開始");
        _startrecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _startrecordActionPerformed(evt);
            }
        });
        _record.add(_startrecord);

        _endrecord.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK));
        _endrecord.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _endrecord.setText("レコード終了");
        _endrecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _endrecordActionPerformed(evt);
            }
        });
        _record.add(_endrecord);

        _autorecord.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _autorecord.setText("自動レコード");
        _autorecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _autorecordActionPerformed(evt);
            }
        });
        _record.add(_autorecord);

        jMenuBar1.add(_record);

        EndGame.setText(bundle.getString("Exit.txt")); // NOI18N
        EndGame.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));

        ToTitle.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        ToTitle.setText(bundle.getString("ToTitle.txt")); // NOI18N
        ToTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ToTitleActionPerformed(evt);
            }
        });
        EndGame.add(ToTitle);

        _exit.setText("ゲーム終了");
        _exit.setFont(new java.awt.Font(bundle.getString("menu.font"), 0, 12));
        _exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _exitActionPerformed(evt);
            }
        });
        EndGame.add(_exit);

        jMenuBar1.add(EndGame);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void _pingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__pingActionPerformed
      core.ping();
  }//GEN-LAST:event__pingActionPerformed

  private void _drawcardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__drawcardActionPerformed
      core.draw(1, "");
  }//GEN-LAST:event__drawcardActionPerformed

  private void OptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OptionActionPerformed
  }//GEN-LAST:event_OptionActionPerformed
                  private void _endturnmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__endturnmenuActionPerformed
                      _endturn.doClick();
  }//GEN-LAST:event__endturnmenuActionPerformed

  private void _throughmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__throughmenuActionPerformed
      _through.doClick();
  }//GEN-LAST:event__throughmenuActionPerformed

  private void _passmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__passmenuActionPerformed
      _pass.doClick();
  }//GEN-LAST:event__passmenuActionPerformed

  private void _commandmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__commandmenuActionPerformed
}//GEN-LAST:event__commandmenuActionPerformed

  private void _passActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__passActionPerformed
      core.pass(true);
  }//GEN-LAST:event__passActionPerformed

  private void _oppdodgelabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__oppdodgelabelMouseClicked
      try {
          if (evt.getButton() == MouseEvent.BUTTON1) {
              _oppevasion.setText(String.valueOf(FTool.safeParseInt(_oppevasion.getText()) + 1));
          } else {
              _oppevasion.setText(String.valueOf(FTool.safeParseInt(_oppevasion.getText()) - 1));
          }
      } catch (Exception ex) {
          insertNoLogMessage(FTool.getLocale(109), Color.RED);
          return;
      }
}//GEN-LAST:event__oppdodgelabelMouseClicked

  private void _opphitlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__opphitlabelMouseClicked


      try {
          if (evt.getButton() == MouseEvent.BUTTON1) {
              _opphit.setText(String.valueOf(FTool.safeParseInt(_opphit.getText()) + 1));
          } else {
              _opphit.setText(String.valueOf(FTool.safeParseInt(_opphit.getText()) - 1));
          }

      } catch (Exception ex) {
          insertNoLogMessage(FTool.getLocale(109), Color.RED);
          return;
      }

}//GEN-LAST:event__opphitlabelMouseClicked

   private void _opppowlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__opppowlabelMouseClicked


       try {
           if (evt.getButton() == MouseEvent.BUTTON1) {
               _opppow.setText(String.valueOf(FTool.safeParseInt(_opppow.getText()) + 1));
           } else {
               _opppow.setText(String.valueOf(FTool.safeParseInt(_opppow.getText()) - 1));
           }

       } catch (Exception ex) {
           insertNoLogMessage(FTool.getLocale(109), Color.RED);
           return;
       }

}//GEN-LAST:event__opppowlabelMouseClicked

   private void _owndodgelabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__owndodgelabelMouseClicked


       try {
           if (evt.getButton() == MouseEvent.BUTTON1) {
               _ownevasion.setText(String.valueOf(FTool.safeParseInt(_ownevasion.getText()) + 1));
           } else {
               _ownevasion.setText(String.valueOf(FTool.safeParseInt(_ownevasion.getText()) - 1));
           }

       } catch (Exception ex) {
           insertNoLogMessage(FTool.getLocale(109), Color.RED);
           return;
       }

}//GEN-LAST:event__owndodgelabelMouseClicked

  private void _ownhitlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownhitlabelMouseClicked


      try {
          if (evt.getButton() == MouseEvent.BUTTON1) {
              _ownhit.setText(String.valueOf(FTool.safeParseInt(_ownhit.getText()) + 1));
          } else {
              _ownhit.setText(String.valueOf(FTool.safeParseInt(_ownhit.getText()) - 1));
          }

      } catch (Exception ex) {
          insertNoLogMessage(FTool.getLocale(109), Color.RED);
          return;
      }

}//GEN-LAST:event__ownhitlabelMouseClicked

   private void _ownpowlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownpowlabelMouseClicked
       try {
           if (evt.getButton() == MouseEvent.BUTTON1) {
               _ownpow.setText(String.valueOf(FTool.safeParseInt(_ownpow.getText()) + 1));
           } else {
               _ownpow.setText(String.valueOf(FTool.safeParseInt(_ownpow.getText()) - 1));
           }
       } catch (Exception ex) {
           insertNoLogMessage(FTool.getLocale(109), Color.RED);
           return;
       }
}//GEN-LAST:event__ownpowlabelMouseClicked

  private void lib_revealActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lib_revealActionPerformed
      int amount = FTool.safeParseInt(_peeklibamount.getText());
      if (amount <= 0) {
          amount = core.getGameInformation().getField(EPlayer.ICH).getLibrary().size();
      }
      _peeklibamount.setText("0");
      core.revealLibTops(amount, "");
  }//GEN-LAST:event_lib_revealActionPerformed

   private void revealendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revealendActionPerformed
       mRevealDistrict.clear();
       _regionTab.setSelectedIndex(0);
       actionDone();
       noLogPlayMessage(core.getGameInformation().myName() + FTool.getLocale(107));
  }//GEN-LAST:event_revealendActionPerformed

  private void _regionTabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__regionTabMouseClicked
      if (HandModel == null) {
          return;
      }
      JTabbedPane pane = (JTabbedPane) evt.getSource();
      int sel = pane.getSelectedIndex();
      if (core.isWatcher()) {
          sel += 2;
      }
      switch (sel) {
          case 0:  //hand tab selected
              if (!core.isWatcher()) {
                  mCommandPanel.show(Commands, "hand");
              }
              showRegion(ERegion.HAND);
              break;
          case 1:  //library tab selected
              if (!core.isWatcher()) {
                  mCommandPanel.show(Commands, "lib");
              }
              break;
          case 2:  //Own graveyard tab selected
              if (!core.isWatcher()) {
                  mCommandPanel.show(Commands, "allgraveyard");
              }
              showRegion(ERegion.DISCARD_PILE);
              break;
          case 3:  //Opp graveyard tab selected
              if (!core.isWatcher()) {
                  mCommandPanel.show(Commands, "blank");
              }
              showRegion(ERegion.OPP_DISCARD_PILE);
              break;
          case 4:  //Reveal selected
              showRegion(ERegion.REVEAL);
              break;
          case 5:
              showRegion(ERegion.WATCHER);
              break;
          default:
              mCommandPanel.show(Commands, "blank");
      }

  }//GEN-LAST:event__regionTabMouseClicked

  private void _regionTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event__regionTabStateChanged
      if (mCommandPanel == null) {
          return;
      }
      if (core.isWatcher()) {
          return;
      }
      JTabbedPane pane = (JTabbedPane) evt.getSource();
      int sel = pane.getSelectedIndex();
      if (sel != 1) {
          setLibraryPeekingAmount(0);
          if (mWholeLibPeeking) {
              playMessage(core.getGameInformation().myName() + FTool.getLocale(49));
              core.shuffle();
              mWholeLibPeeking = false;
          }
      }

      LibModel.removeAllElements();
      switch (sel) {
          case 0:  //hand tab selected
              mCommandPanel.show(Commands, "hand");
              showRegion(ERegion.HAND);
              break;
          case 1:  //library tab selected
              mCommandPanel.show(Commands, "lib");
              showRegion(ERegion.LIBRARY);
              break;
          case 2:  //Own graveyard tab selected
              mCommandPanel.show(Commands, "blank");
              showRegion(ERegion.DISCARD_PILE);
              break;
          case 3:  //Opp graveyard tab selected
              showRegion(ERegion.OPP_DISCARD_PILE);
              break;
          case 4:
              showRegion(ERegion.REVEAL);
              mCommandPanel.show(Commands, "reveal");
              break;
          default:
              mCommandPanel.show(Commands, "blank");
      }

  }//GEN-LAST:event__regionTabStateChanged

  private void _dontrobfocusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__dontrobfocusActionPerformed
      FTool.updateConfig("dontrobfocus", String.valueOf(_dontrobfocus.isSelected()));
   }//GEN-LAST:event__dontrobfocusActionPerformed

   private void _autodrawItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__autodrawItemStateChanged
       FTool.updateConfig("autodraw", String.valueOf(_autodraw.isSelected()));
   }//GEN-LAST:event__autodrawItemStateChanged

   private void _showdeckeditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__showdeckeditorActionPerformed
       try {
           DeckEditor dew;
           dew = new DeckEditor();
           dew.setVisible(true);
       } catch (Exception ex) {
           ex.printStackTrace();
       }
   }//GEN-LAST:event__showdeckeditorActionPerformed

  private void ToTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ToTitleActionPerformed
      if (JOptionPane.showConfirmDialog(null, FTool.getLocale(80), FTool.getLocale(1), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          core.backToTitle();
          bgmplayer.stopplay();
          this.setVisible(false);
          this.dispose();
      }
   }//GEN-LAST:event_ToTitleActionPerformed

   private void MuteMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MuteMenuActionPerformed
       bgmplayer.stopplay();
       FTool.updateConfig("mute", String.valueOf(MuteMenu.isSelected()));
       FTool.mute = Boolean.parseBoolean(String.valueOf(MuteMenu.isSelected()));
    }//GEN-LAST:event_MuteMenuActionPerformed

   private void revealActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revealActionPerformed
       core.showHand("");
   }//GEN-LAST:event_revealActionPerformed

  private void _borderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__borderActionPerformed
      changeBattleSab();
   }//GEN-LAST:event__borderActionPerformed

  private void _ownevasionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__ownevasionActionPerformed
      changeBattleSab();
}//GEN-LAST:event__ownevasionActionPerformed

  private void _ownhitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__ownhitActionPerformed
      changeBattleSab();
}//GEN-LAST:event__ownhitActionPerformed

  private void _ownpowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__ownpowActionPerformed
      changeBattleSab();
}//GEN-LAST:event__ownpowActionPerformed

  private void _opphitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__opphitActionPerformed
      changeBattleSab();
}//GEN-LAST:event__opphitActionPerformed

   private void _oppevasionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__oppevasionActionPerformed
       changeBattleSab();
}//GEN-LAST:event__oppevasionActionPerformed

   private void _opppowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__opppowActionPerformed
       changeBattleSab();
}//GEN-LAST:event__opppowActionPerformed

   private void _ownBattleSUMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownBattleSUMouseClicked
       core.getGameInformation().getField(EPlayer.ICH).getBattleCard().getAttachedOrNullObject().getLabel().doMouseEvent(evt);
       if (evt.getButton() == MouseEvent.BUTTON1) {
           mCommandPanel.show(Commands, "battle_sab");
           showCard(core.getGameInformation().getField(EPlayer.ICH).getBattleCard().getAttachedOrNullObject().getInfo());
           return;
       }
   }//GEN-LAST:event__ownBattleSUMouseClicked

  private void _oppBattleSUMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__oppBattleSUMouseClicked
      core.getGameInformation().getField(EPlayer.OPP).getBattleCard().getAttachedOrNullObject().getLabel().doMouseEvent(evt);
      if (evt.getButton() == MouseEvent.BUTTON1) {
          mCommandPanel.show(Commands, "battle_sab");
          showCard(core.getGameInformation().getField(EPlayer.OPP).getBattleCard().getAttachedOrNullObject().getInfo());
          return;
      }/*
      showCard(mPlayers[OPP].battleCard.getAttach());
      if (mPlayers[OPP].battleCard.isAttached() && (evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown())) {
      _singlepopoption.setText(mPlayers[OPP].battleCard.getAttachLabel().getInfo().cardname);
      viewcardnum = mPlayers[OPP].battleCard.getAttach();
      _singlepopup.show(MainFrame.this, evt.getXOnScreen() - MainFrame.this.getX(), evt.getYOnScreen() - MainFrame.this.getY());
      }*/
   }//GEN-LAST:event__oppBattleSUMouseClicked

  private void _oppBattleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__oppBattleMouseClicked
      core.getGameInformation().getField(EPlayer.OPP).getBattleCard().getLabel().doMouseEvent(evt);
      if (evt.getButton() == MouseEvent.BUTTON1) {
          mCommandPanel.show(Commands, "battle_sab");
          showCard(core.getGameInformation().getField(EPlayer.OPP).getBattleCard().getInfo());
          return;
      }/*
      
      /*if (evt.getButton() == MouseEvent.BUTTON1) {
      mCommandPanel.show(Commands, "battle_sab");
      showCard(core.getGameInformation().getField(EPlayer.OPP).getBattleCard().getInfo());
      return;
      }
      showCard(mPlayers[OPP].battleCard.mCardnum);
      _singlepopoption.setText(mPlayers[OPP].battleCard.getInfo().cardname);
      viewcardnum = mPlayers[OPP].battleCard.mCardnum;
      _singlepopup.show(MainFrame.this, evt.getXOnScreen() - MainFrame.this.getX(), evt.getYOnScreen() - MainFrame.this.getY());*/

   }//GEN-LAST:event__oppBattleMouseClicked

  private void MPDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MPDownActionPerformed
      core.adjustSP(-1, true, "");
   }//GEN-LAST:event_MPDownActionPerformed

  private void MPUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MPUpActionPerformed
      core.adjustSP(1, true, "");
   }//GEN-LAST:event_MPUpActionPerformed

  private void HPDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HPDownActionPerformed
      core.adjustHP(-1, true, false, "");
   }//GEN-LAST:event_HPDownActionPerformed

   private void HPUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HPUpActionPerformed
       core.adjustHP(1, true, false, "");
  }//GEN-LAST:event_HPUpActionPerformed

   private void _throughActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThroughActionPerformed
       core.through();
   }//GEN-LAST:event_ThroughActionPerformed

   private void _battlegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__battlegoActionPerformed
       if (core.getMode() == EGameMode.RECORDVIEW) {
           core.nextAction();
           return;
       }
       if (!core.isIchBattleCardSet()) {
           core.through();
       }
       if (changeBattleSab(false)) {
           core.confirmBattle();
       }
}//GEN-LAST:event__battlegoActionPerformed

   private void _changebattlesabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__changebattlesabActionPerformed
       changeBattleSab();
}//GEN-LAST:event__changebattlesabActionPerformed

private void OwnMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OwnMPActionPerformed
    core.setSP(FTool.safeParseInt(OwnMP.getText()), "");
   }//GEN-LAST:event_OwnMPActionPerformed

  private void OwnHPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OwnHPActionPerformed
      core.setHP(FTool.safeParseInt(OwnHP.getText()), "");
   }//GEN-LAST:event_OwnHPActionPerformed

  private void _chatboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__chatboxActionPerformed
      SendChatActionPerformed(evt);
   }//GEN-LAST:event__chatboxActionPerformed

   private void SendChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendChatActionPerformed
       if (_chatbox.getText().length() > 0) {
           String myName = core.getGameInformation().myName();
           core.sendChat(_chatbox.getText());
           _chatbox.setText("");
       }
  }//GEN-LAST:event_SendChatActionPerformed

  private void _ownBattleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownBattleMouseClicked
      core.getGameInformation().getField(EPlayer.ICH).getBattleCard().getLabel().doMouseEvent(evt);
      if (evt.getButton() == MouseEvent.BUTTON1) {
          mCommandPanel.show(Commands, "battle_sab");
          showCard(core.getGameInformation().getField(EPlayer.ICH).getBattleCard().getInfo());
          return;
      }
   }//GEN-LAST:event__ownBattleMouseClicked

   private void _ownEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownEventMouseClicked
       core.getGameInformation().getField(EPlayer.ICH).getEventCard().getLabel().doMouseEvent(evt);
       if (evt.getButton() == MouseEvent.BUTTON1) {
           mCommandPanel.show(Commands, "battle_sab");
           showCard(core.getGameInformation().getField(EPlayer.ICH).getEventCard().getInfo());
           return;
       }
       /*showCard(mPlayers[ICH].eventCard.mCardnum);
       if (core.isWatcher()) {
       _singlepopoption.setText(mPlayers[ICH].eventCard.getInfo().cardname);
       viewcardnum = mPlayers[ICH].eventCard.mCardnum;
       _singlepopup.show(_ownEvent, evt.getX(), evt.getY());
       } else {
       mPlayers[ICH].eventCard.doMouseEvent(evt);
       }*/
   }//GEN-LAST:event__ownEventMouseClicked

  private void lib_ShuffleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lib_ShuffleActionPerformed
      core.shuffle();
   }//GEN-LAST:event_lib_ShuffleActionPerformed

   private void lib_TopToGraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lib_TopToGraActionPerformed
       core.deckout("");
   }//GEN-LAST:event_lib_TopToGraActionPerformed

  private void hand_ThrowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hand_ThrowActionPerformed
      core.randomDiscard("");
   }//GEN-LAST:event_hand_ThrowActionPerformed

   private void _oppLeaderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__oppLeaderMouseClicked
       if (evt.getButton() == MouseEvent.BUTTON1) {
           if (core.attach(core.getGameInformation().getField(EPlayer.OPP).getLeader().getLabel(), true)) {
               return;
           }
           showCard(core.getGameInformation().getField(EPlayer.OPP).getLeader().getInfo());
           return;
       }
       showCard(core.getGameInformation().getField(EPlayer.OPP).getLeader().getInfo());
       core.getGameInformation().getField(EPlayer.OPP).getLeader().getLabel().doMouseEvent(evt);
   }//GEN-LAST:event__oppLeaderMouseClicked

   private void _ownLeaderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownLeaderMouseClicked
       if (evt.getButton() == MouseEvent.BUTTON1) {
           if (core.attach(core.getGameInformation().getField(EPlayer.ICH).getLeader().getLabel(), true)) {
               return;
           }
           showCard(core.getGameInformation().getField(EPlayer.ICH).getLeader().getInfo());
           return;
       }
       showCard(core.getGameInformation().getField(EPlayer.ICH).getLeader().getInfo());
       core.getGameInformation().getField(EPlayer.ICH).getLeader().getLabel().doMouseEvent(evt);
       /*       showCard(mPlayers[ICH].leader.mCardnum);
       
       if (core.isWatcher()) {
       _singlepopoption.setText(mPlayers[ICH].leader.getInfo().cardname);
       viewcardnum = mPlayers[ICH].leader.mCardnum;
       _singlepopup.show(_ownLeader, evt.getX(), evt.getY());
       } else {
       mPlayers[ICH].leader.doMouseEvent(evt);
       }*/
   }//GEN-LAST:event__ownLeaderMouseClicked

   private void _attachCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__attachCancelActionPerformed
       mChoosingAttach = false;
       actionDone();
   }//GEN-LAST:event__attachCancelActionPerformed

  private void handdrawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handdrawActionPerformed
      core.draw(1, "");
   }//GEN-LAST:event_handdrawActionPerformed

    private void _peekLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__peekLibActionPerformed
        int amount = FTool.safeParseInt(_peeklibamount.getText());
        if (amount <= 0) {
            peekWholeLibrary("");
        } else {
            core.peekLibrary(amount, "");
        }
   }//GEN-LAST:event__peekLibActionPerformed

    private void _endturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__endturnActionPerformed
        if (core.getGameInformation().isIchAttackPlayer()) {
            core.nextTurn(true);
        }
}//GEN-LAST:event__endturnActionPerformed

    private void _sceneMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            if (core.attach(core.getGameInformation().getScene().getLabel(), true)) {
                return;
            }
            showCard(core.getGameInformation().getScene().getInfo());
            return;
        }
        showCard(core.getGameInformation().getScene().getInfo());
        core.getGameInformation().getScene().getLabel().doMouseEvent(evt);
        /*if ((evt.getButton() != MouseEvent.BUTTON1 || evt.isControlDown()) && !core.isWatcher()) {
        _ScenePopup.show(_scene, evt.getX(), evt.getY());
        } else if (core.isWatcher()) {
        _singlepopoption.setText(FTool.cdata[mSceneCard.mCardnum].cardname);
        viewcardnum = mSceneCard.mCardnum;
        _singlepopup.show(_scene, evt.getX(), evt.getY());
        }
        
        showCard(mSceneCard.mCardnum);*/
    }

private void _newgameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__newgameActionPerformed
    if (JOptionPane.showConfirmDialog(this, FTool.getLocale(78), "New Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        core.requestNewGame();
    }
}//GEN-LAST:event__newgameActionPerformed

private void _loaddeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__loaddeckActionPerformed
    loadDeck(null);
}//GEN-LAST:event__loaddeckActionPerformed

private void _handalltolibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__handalltolibActionPerformed
    core.sendHandBack();
}//GEN-LAST:event__handalltolibActionPerformed

private void _allgravetolibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__allgravetolibActionPerformed
    core.returnDiscardPileToLibrary();
}//GEN-LAST:event__allgravetolibActionPerformed

private void _lastdeck1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lastdeck1ActionPerformed
    loadDeck(FTool.readConfig("lastdeck1").split("\t")[1]);
}//GEN-LAST:event__lastdeck1ActionPerformed

private void _lastdeck2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lastdeck2ActionPerformed
    loadDeck(FTool.readConfig("lastdeck2").split("\t")[1]);
}//GEN-LAST:event__lastdeck2ActionPerformed

private void _lastdeck3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lastdeck3ActionPerformed
    loadDeck(FTool.readConfig("lastdeck3").split("\t")[1]);
}//GEN-LAST:event__lastdeck3ActionPerformed

private void _lastdeck4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lastdeck4ActionPerformed
    loadDeck(FTool.readConfig("lastdeck4").split("\t")[1]);
}//GEN-LAST:event__lastdeck4ActionPerformed

private void _lastdeck5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lastdeck5ActionPerformed
    loadDeck(FTool.readConfig("lastdeck5").split("\t")[1]);
}//GEN-LAST:event__lastdeck5ActionPerformed

private void _ownhplabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownhplabelMouseClicked
    if (evt.getButton() == MouseEvent.BUTTON1) {
        core.setHP(core.getGameInformation().getPlayer(EPlayer.ICH).getHP() + 1, "");
    } else {
        core.setHP(core.getGameInformation().getPlayer(EPlayer.ICH).getHP() - 1, "");
    }
}//GEN-LAST:event__ownhplabelMouseClicked

private void _ownmplabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownmplabelMouseClicked
    if (evt.getButton() == MouseEvent.BUTTON1) {
        core.setSP(core.getGameInformation().getPlayer(EPlayer.ICH).getSP() + 1, "");
    } else {
        core.setSP(core.getGameInformation().getPlayer(EPlayer.ICH).getSP() - 1, "");
    }
}//GEN-LAST:event__ownmplabelMouseClicked

private void _verbosereplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__verbosereplayActionPerformed
// TODO add your handling code here:
    FTool.updateConfig("verbosereplay", String.valueOf(_verbosereplay.isSelected()));
}//GEN-LAST:event__verbosereplayActionPerformed

private void _lib_TopToBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__lib_TopToBottomActionPerformed
    core.putLibraryTopToBottom();
}//GEN-LAST:event__lib_TopToBottomActionPerformed

private void _pass2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__pass2ActionPerformed
    _pass.doClick();
}//GEN-LAST:event__pass2ActionPerformed

private void _nowatcherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__nowatcherActionPerformed
}//GEN-LAST:event__nowatcherActionPerformed

private void _oppEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__oppEventMouseClicked
    core.getGameInformation().getField(EPlayer.OPP).getEventCard().getLabel().doMouseEvent(evt);
    if (evt.getButton() == MouseEvent.BUTTON1) {
        mCommandPanel.show(Commands, "battle_sab");
        showCard(core.getGameInformation().getField(EPlayer.OPP).getEventCard().getInfo());
        return;
    }

}//GEN-LAST:event__oppEventMouseClicked

private void _watcherpingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__watcherpingActionPerformed
    core.ping();
}//GEN-LAST:event__watcherpingActionPerformed

private void _shuffleMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__shuffleMenuActionPerformed
    core.shuffle();
}//GEN-LAST:event__shuffleMenuActionPerformed

private void _firstmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__firstmpActionPerformed
    FTool.updateConfig("firstmp", String.valueOf(_firstmp.isSelected()));
}//GEN-LAST:event__firstmpActionPerformed

private void _randomsceneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__randomsceneActionPerformed
    FTool.updateConfig("randomscene", String.valueOf(_randomscene.isSelected()));
}//GEN-LAST:event__randomsceneActionPerformed

private void _oppfaithlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__oppfaithlabelMouseClicked
    try {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            _oppfaith.setText(String.valueOf(FTool.safeParseInt(_oppfaith.getText()) + 1));
        } else {
            _oppfaith.setText(String.valueOf(FTool.safeParseInt(_oppfaith.getText()) - 1));
        }
    } catch (Exception ex) {
        insertNoLogMessage(FTool.getLocale(109), Color.RED);
        return;
    }
}//GEN-LAST:event__oppfaithlabelMouseClicked

private void _ownfaithlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__ownfaithlabelMouseClicked
    try {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            _ownfaith.setText(String.valueOf(FTool.safeParseInt(_ownfaith.getText()) + 1));
        } else {
            _ownfaith.setText(String.valueOf(FTool.safeParseInt(_ownfaith.getText()) - 1));
        }
    } catch (Exception ex) {
        insertNoLogMessage(FTool.getLocale(109), Color.RED);
        return;
    }
}//GEN-LAST:event__ownfaithlabelMouseClicked

private void _oppfaithActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__oppfaithActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__oppfaithActionPerformed

private void _ownfaithActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__ownfaithActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__ownfaithActionPerformed

private void _watchersreplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__watchersreplayActionPerformed
    FTool.updateConfig("watchersreplay", String.valueOf(_watchersreplay.isSelected()));
}//GEN-LAST:event__watchersreplayActionPerformed

private void _randomloaddeckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__randomloaddeckActionPerformed
    core.randomLoadDeck();
}//GEN-LAST:event__randomloaddeckActionPerformed

private void _battlecancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__battlecancelActionPerformed
    battleCancel();
}//GEN-LAST:event__battlecancelActionPerformed

private void _startrecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__startrecordActionPerformed
    core.startRecord();
}//GEN-LAST:event__startrecordActionPerformed

private void _endrecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__endrecordActionPerformed
    core.stopRecord();
    JOptionPane.showMessageDialog(this, "Record stopped.");
}//GEN-LAST:event__endrecordActionPerformed

private void _nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__nextActionPerformed
    core.nextAction();
}//GEN-LAST:event__nextActionPerformed

private void _turnskipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__turnskipActionPerformed
    core.skipTurn();
}//GEN-LAST:event__turnskipActionPerformed

private void _autorecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__autorecordActionPerformed
    FTool.updateConfig("autorecord", String.valueOf(_autorecord.isSelected()));
}//GEN-LAST:event__autorecordActionPerformed

private void _watcheroptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__watcheroptionActionPerformed
    if (mSelectedWatcher != null) {
        core.getShareHands().add(mSelectedWatcher);
    }
    core.deliverHand();
    insertNoLogMessage(FTool.parseLocale(240, mSelectedWatcher.getName()), Color.RED);
}//GEN-LAST:event__watcheroptionActionPerformed

private void _mulliganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mulliganActionPerformed
    core.mulligan();
}//GEN-LAST:event__mulliganActionPerformed

private void _hidetimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__hidetimerActionPerformed
    if (_hidetimer.isSelected()) {
        _owntimer.setVisible(false);
        _opptimer.setVisible(false);
    } else {
        _owntimer.setVisible(true);
        _opptimer.setVisible(true);
    }
}//GEN-LAST:event__hidetimerActionPerformed

private void _showdeckmanagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__showdeckmanagerActionPerformed
    new ChocolatsArchive(core).setVisible(true);
}//GEN-LAST:event__showdeckmanagerActionPerformed

private void _decidefirstlastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__decidefirstlastActionPerformed
    core.sendDecideFirstLast();
}//GEN-LAST:event__decidefirstlastActionPerformed

private void _libBottomToTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__libBottomToTopActionPerformed
    core.putLibraryBottomToTop();
}//GEN-LAST:event__libBottomToTopActionPerformed

private void _passclockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__passclockActionPerformed
    setTimerTurn(EPlayer.OPP);
}//GEN-LAST:event__passclockActionPerformed

private void _exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__exitActionPerformed
    if (JOptionPane.showConfirmDialog(null, FTool.getLocale(0), FTool.getLocale(1), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        System.exit(0);
    }
}//GEN-LAST:event__exitActionPerformed
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
            FTool.updateConfig("mainframex", String.valueOf(x));
            FTool.updateConfig("mainframey", String.valueOf(y));
        }
    }, 1300);
}//GEN-LAST:event_formComponentMoved

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    core.finishReplay();
}//GEN-LAST:event_formWindowClosing
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CommandContainer;
    private javax.swing.JPanel Commands;
    private javax.swing.JMenu EndGame;
    private javax.swing.JScrollPane GravePane;
    private javax.swing.JMenuItem HPDown;
    private javax.swing.JMenuItem HPUp;
    private javax.swing.JScrollPane HandPane;
    private javax.swing.JPanel LeftDown;
    private javax.swing.JPanel LeftUp;
    private javax.swing.JScrollPane LibPane;
    private javax.swing.JMenuItem MPDown;
    private javax.swing.JMenuItem MPUp;
    private javax.swing.JCheckBoxMenuItem MuteMenu;
    private javax.swing.JLabel OppChars0;
    private javax.swing.JLabel OppChars1;
    private javax.swing.JLabel OppChars2;
    private javax.swing.JLabel OppChars3;
    private javax.swing.JScrollPane OppGravePane;
    private javax.swing.JTextField OppHP;
    private javax.swing.JLabel OppHandCnt;
    private javax.swing.JLabel OppLibCnt;
    private javax.swing.JTextField OppMP;
    private javax.swing.JMenu Option;
    private javax.swing.JLabel OwnChars0;
    private javax.swing.JLabel OwnChars1;
    private javax.swing.JLabel OwnChars2;
    private javax.swing.JLabel OwnChars3;
    private javax.swing.JTextField OwnHP;
    private javax.swing.JLabel OwnHandCnt;
    private javax.swing.JLabel OwnLibCnt;
    private javax.swing.JTextField OwnMP;
    private javax.swing.JList RevealList;
    private javax.swing.JScrollPane RevealPane;
    private javax.swing.JPanel Right;
    private javax.swing.JTextPane SCArea;
    private javax.swing.JButton SendChat;
    private javax.swing.JMenuItem ToTitle;
    private javax.swing.JScrollPane _OppActivatedScroll;
    private javax.swing.JScrollPane _OppLeaderSUScroll;
    private javax.swing.JScrollPane _OwnActivatedScroll;
    private javax.swing.JScrollPane _OwnLeaderSUScroll;
    private javax.swing.JScrollPane _OwnStandbyScroll;
    private javax.swing.JList _WatcherList;
    private javax.swing.JScrollPane _WatcherPane;
    private javax.swing.JButton _allgravetolib;
    private javax.swing.JPanel _allgraveyard;
    private javax.swing.JButton _attachCancel;
    private javax.swing.JCheckBoxMenuItem _autodraw;
    private javax.swing.JCheckBoxMenuItem _autorecord;
    private javax.swing.JMenuItem _battlecancel;
    private javax.swing.JButton _battlego;
    private javax.swing.JLabel _blanklabel;
    private javax.swing.JCheckBox _border;
    private javax.swing.JButton _changebattlesab;
    private javax.swing.JTextField _chatbox;
    private javax.swing.JPanel _chooseAttach;
    private javax.swing.JMenu _commandmenu;
    private javax.swing.JMenuItem _decidefirstlast;
    private javax.swing.JList _discardPileList;
    private javax.swing.JCheckBoxMenuItem _dontrobfocus;
    private javax.swing.JMenuItem _drawcard;
    private javax.swing.JMenuItem _endrecord;
    private javax.swing.JButton _endturn;
    private javax.swing.JMenuItem _endturnmenu;
    private javax.swing.JMenuItem _exit;
    private javax.swing.JCheckBoxMenuItem _firstmp;
    private javax.swing.JPanel _handCommand;
    private javax.swing.JList _handList;
    private javax.swing.JButton _handalltolib;
    private javax.swing.JCheckBoxMenuItem _hidetimer;
    private javax.swing.JMenuItem _lastdeck1;
    private javax.swing.JMenuItem _lastdeck2;
    private javax.swing.JMenuItem _lastdeck3;
    private javax.swing.JMenuItem _lastdeck4;
    private javax.swing.JMenuItem _lastdeck5;
    private javax.swing.JButton _libBottomToTop;
    private javax.swing.JList _libList;
    private javax.swing.JButton _lib_TopToBottom;
    private javax.swing.JMenuItem _loaddeck;
    private javax.swing.JTextPane _messagebox;
    private javax.swing.JMenuItem _mulligan;
    private javax.swing.JLabel _myname;
    private javax.swing.JMenuItem _newgame;
    private javax.swing.JButton _next;
    private javax.swing.JPanel _oppActivated;
    private javax.swing.JLabel _oppBattle;
    private javax.swing.JLabel _oppBattleSU;
    private javax.swing.JList _oppDiscardPileList;
    private javax.swing.JLabel _oppEvent;
    private javax.swing.JLabel _oppLeader;
    private javax.swing.JPanel _oppLeaderAttach;
    private javax.swing.JPanel _oppReserved;
    private javax.swing.JLabel _oppSpell;
    private javax.swing.JLabel _oppdodgelabel;
    private javax.swing.JTextField _oppevasion;
    private javax.swing.JTextField _oppfaith;
    private javax.swing.JLabel _oppfaithlabel;
    private javax.swing.JLabel _oppgravecnt;
    private javax.swing.JTextField _opphit;
    private javax.swing.JLabel _opphitlabel;
    private javax.swing.JLabel _oppname;
    private javax.swing.JTextField _opppow;
    private javax.swing.JLabel _opppowlabel;
    private javax.swing.JLabel _opptimer;
    private javax.swing.JPanel _ownActivated;
    private javax.swing.JLabel _ownBattle;
    private javax.swing.JLabel _ownBattleSU;
    private javax.swing.JLabel _ownEvent;
    private javax.swing.JLabel _ownLeader;
    private javax.swing.JPanel _ownLeaderAttach;
    private javax.swing.JPanel _ownReserved;
    private javax.swing.JLabel _ownSpell;
    private javax.swing.JLabel _owndodgelabel;
    private javax.swing.JTextField _ownevasion;
    private javax.swing.JTextField _ownfaith;
    private javax.swing.JLabel _ownfaithlabel;
    private javax.swing.JLabel _owngravecnt;
    private javax.swing.JTextField _ownhit;
    private javax.swing.JLabel _ownhitlabel;
    private javax.swing.JLabel _ownhplabel;
    private javax.swing.JLabel _ownmplabel;
    private javax.swing.JTextField _ownpow;
    private javax.swing.JLabel _ownpowlabel;
    private javax.swing.JLabel _owntimer;
    private javax.swing.JButton _pass;
    private javax.swing.JMenuItem _pass2;
    private javax.swing.JMenuItem _passclock;
    private javax.swing.JMenuItem _passmenu;
    private javax.swing.JButton _peekLib;
    private javax.swing.JTextField _peeklibamount;
    private javax.swing.JMenuItem _ping;
    private javax.swing.JMenu _pingmenu;
    private javax.swing.JMenuItem _randomloaddeck;
    private javax.swing.JCheckBoxMenuItem _randomscene;
    private javax.swing.JMenu _record;
    private javax.swing.JPanel _recordview;
    private javax.swing.JTabbedPane _regionTab;
    private javax.swing.JLabel _scene;
    private javax.swing.JMenuItem _showdeckeditor;
    private javax.swing.JMenuItem _showdeckmanager;
    private javax.swing.JMenuItem _shuffleMenu;
    private javax.swing.JMenu _startgamemenu;
    private javax.swing.JMenuItem _startrecord;
    private javax.swing.JButton _through;
    private javax.swing.JMenuItem _throughmenu;
    private javax.swing.JLabel _turnPhase;
    private javax.swing.JButton _turnskip;
    private javax.swing.JCheckBoxMenuItem _verbosereplay;
    private javax.swing.JMenuItem _watcheroption;
    private javax.swing.JMenuItem _watcherping;
    private javax.swing.JPopupMenu _watcherpopup;
    private javax.swing.JCheckBoxMenuItem _watchersreplay;
    private javax.swing.JPanel _watching;
    private javax.swing.JPanel battle_sab;
    private javax.swing.JPanel blank;
    private javax.swing.JPanel cmdGlobal;
    private javax.swing.JPanel cmd_reveal;
    private javax.swing.JButton hand_Throw;
    private javax.swing.JButton handdraw;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel label11;
    private javax.swing.JLabel label12;
    private javax.swing.JLabel label13;
    private javax.swing.JLabel label3;
    private javax.swing.JLabel label4;
    private javax.swing.JLabel label5;
    private javax.swing.JLabel label6;
    private javax.swing.JLabel lable10;
    private javax.swing.JPanel lib;
    private javax.swing.JButton lib_Shuffle;
    private javax.swing.JButton lib_TopToGra;
    private javax.swing.JButton lib_reveal;
    private javax.swing.JButton reveal;
    private javax.swing.JButton revealend;
    // End of variables declaration//GEN-END:variables
}
