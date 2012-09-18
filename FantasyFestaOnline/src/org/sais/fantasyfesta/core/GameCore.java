/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import com.sun.org.apache.bcel.internal.classfile.Constant;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.sais.fantasyfesta.autos.ActivateManager;
import org.sais.fantasyfesta.autos.AttachmentTriggerManager;
import org.sais.fantasyfesta.autos.BattleSystem;
import org.sais.fantasyfesta.autos.BattleSystem.Ability;
import org.sais.fantasyfesta.autos.BattleValues;
import org.sais.fantasyfesta.autos.DamageManuplateManager;
import org.sais.fantasyfesta.autos.ExtraBattleCostManager;
import org.sais.fantasyfesta.autos.HitDamagedManager;
import org.sais.fantasyfesta.autos.IAutosCallback;
import org.sais.fantasyfesta.autos.PrecheckEffectManager;
import org.sais.fantasyfesta.autos.ReplenishBeginManager;
import org.sais.fantasyfesta.autos.ReplenishEndBattleBeginManager;
import org.sais.fantasyfesta.autos.SpellMoveTriggerManager;
import org.sais.fantasyfesta.autos.SupportMoveTriggerManager;
import org.sais.fantasyfesta.autos.TurnEndManager;
import org.sais.fantasyfesta.autos.UseEventAbilityTriggerManager;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardInfo;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.CharacterCard;
import org.sais.fantasyfesta.card.EventCard;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.card.SupportCardInfo;
import org.sais.fantasyfesta.card.cardlabel.CardLabel;
import org.sais.fantasyfesta.card.cardlabel.ICardLabelCallback;
import org.sais.fantasyfesta.card.cardlabel.SpellLabel;
import org.sais.fantasyfesta.core.CurrentState.CurrentStateItem;
import org.sais.fantasyfesta.deck.ChocolatsArchive;
import org.sais.fantasyfesta.deck.Deck;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.EventDistrict;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.district.RevealDistrict;
import org.sais.fantasyfesta.enums.ECostType;
import org.sais.fantasyfesta.enums.EDamageType;
import org.sais.fantasyfesta.enums.EGameMode;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPhase;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.net.socketthread.ClientSocketThread;
import org.sais.fantasyfesta.net.socketthread.SocketThread;
import org.sais.fantasyfesta.net.socketthread.HostSocketThread;
import org.sais.fantasyfesta.recorder.Recorder;
import org.sais.fantasyfesta.tool.Constants;
import org.sais.fantasyfesta.tool.FTool;
import org.sais.fantasyfesta.ui.BackgroundManager;

/**
 *
 * @author Romulus
 */
public class GameCore implements IAutosCallback, ICardLabelCallback, ChocolatsArchive.IDeckManagerCallback {

    public static final String BATTLESAB_SEP = "//";
    //
    private EGameMode mMode;
    private IMainUI ui;
    private ITitleUI mTitleUI;
    private GameInformation gi;
    private boolean bv_inited = false;
    private BattleSystem mBattleSystem;
    private BattleResult mBattleResult;
    private boolean mIchBattleCardSet = false;
    public boolean mNewgameWithoutLoadingDeck = false;
    // Network
    private InetSocketAddress mHostAddress;
    private SocketThread mSocketThread = null;
    private long mPingTime;
    // Deck loading state
    private EDeckLoadingState mDeckLoadingState = EDeckLoadingState.INIT_GAME;
    // Watcher
    private String mWatcherName = "";

    enum EDeckLoadingState {

        INIT_GAME,
        ICH_LOADED,
        OPP_LOADED,
    }
    // Battle Confirm
    private boolean mBattling = false;
    private boolean mHostBattleOK = false;
    private boolean mClientBattleOK = false;
    // Pass Confirm
    private EnumMap<EPlayer, Boolean> passed = new EnumMap<EPlayer, Boolean>(EPlayer.class);
    // Replay, record
    private File mReplayFile;
    private long mReplayStartTime;
    private BufferedWriter mReplayWriter;
    private Recorder mRecorder;
    // Watcher
    private ArrayList<Watcher> mWatchers = new ArrayList<Watcher>();
    private ArrayList<Watcher> mShareHands = new ArrayList<Watcher>();
    // Timer
    private GameTimer mPlayingTimer;
    // Attaching
    private SupportCard cardWaitingAttach = SupportCard.newNull();
    // Door selection
    private int mFLSelection = -1;

    public GameCore(EGameMode mode, SocketThread socketThread, InetSocketAddress hostAddress, ITitleUI title) {
        this.mMode = mode;
        this.mTitleUI = title;
        this.ui = new MainFrame(this);
        this.gi = new GameInformation(this);
        this.mSocketThread = socketThread;
        this.mHostAddress = hostAddress;
        passed.put(EPlayer.ICH, false);
        passed.put(EPlayer.OPP, false);
        if (mode == EGameMode.WATCHER) {
            mWatcherName = mTitleUI.getPlayerName();
        } else {
            ich().setName(mTitleUI.getPlayerName());
        }
    }

    public void launch() {
        if (mMode == EGameMode.RECORDVIEW) {
            launchRecordView();
        } else if (isHost()) {
            launchHost();
        } else if (isWatcher()) {
            launchWatcher();
        } else if (mSocketThread instanceof ClientSocketThread && ((ClientSocketThread) mSocketThread).isVitualHost()) {
            launchVitualHost();
        } else {
            launchClient();
        }
        mTitleUI.setMessage("Stand by Ready.");
        mTitleUI.setVisible(false);
        ui.setVisible(true);
    }

    private void launchRecordView() {
        ui.setTitle(mTitleUI.getTitle() + " (RECORD)");
        send("$GIVEMECURRENT:");
    }

    private void launchHost() {
        send("$PLAYERNAME:" + gi.myName());
        ui.setTitle(mTitleUI.getTitle() + " (HOST)");
        send("$VERSION:" + Startup.VERSION);
    }

    private void launchVitualHost() {
        send("$PLAYERNAME:" + gi.myName());
        ui.setTitle(mTitleUI.getTitle() + " (VITUALHOST)");
        send("$VERSION:" + Startup.VERSION);
    }

    private void launchClient() {
        send("$PLAYERNAME:" + gi.myName());
        ui.setTitle(mTitleUI.getTitle() + " (CLIENT)");
    }

    private void launchWatcher() {
        send("$WATCHER:" + mTitleUI.getPlayerName());
        ui.setTitle(mTitleUI.getTitle() + " (WATCHER)");
        send("$GIVEMECURRENT:");
    }

    public void launchDebugGame(String name) {
        try {
            mMode = EGameMode.DEBUG;
            gi.getPlayer(EPlayer.ICH).setName(name);
            ui.setVisible(true);
            ui.setTitle(mTitleUI.getTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void newGame() {
        gi.clear();
        ui.clearField();

        if (mPlayingTimer != null) {
            mPlayingTimer.stop();
        }
        mPlayingTimer = new GameTimer(2, ui.getTimerUpdateListener());

        generateReplay();

        mBattling = false;

        if (mNewgameWithoutLoadingDeck) {
            send("$DECKNAME:" + gi.getPlayer(EPlayer.ICH).getDeck().getDeckName());
            send("$DECKCHAR:" + gi.getPlayer(EPlayer.ICH).getDeck().getCharsStringForSocket());
            mNewgameWithoutLoadingDeck = false;
        }

        if (isHost()) {
            flipCoin();
            if (gi.isAttackPlayer(EPlayer.ICH)) {
                if (FTool.readConfig("firstmp").equals("false")) {
                    ich().setSP(0);
                } else {
                    ich().setSP(1);
                }
            } else {
                ich().setSP(0);
            }
        }

        if (opp().getDeck() != null) {
            sendAndRefreshCounter();
        }

        ui.showCard(CardInfo.newNull());
        ui.actionDone();
        ui.showTurn(gi);
    }

    public void generateReplay() {
        String filename;

        if (isWatcher()) {
            if (FTool.readBooleanConfig("watchersreplay", false)) {
                filename = "replay/Watching_Replay_" + FTool.getNowTimeString("yy_MM_dd_HH_mm_ss") + ".txt";
            } else {
                return;
            }
        } else {
            filename = "replay/Replay_" + FTool.getNowTimeString("yy_MM_dd_HH_mm_ss") + ".txt";
        }

        //Generate replay file
        if (!(new File("replay").exists())) {
            new File("replay").mkdirs();
        }

        finishReplay();

        try {
            mReplayFile = new File(filename);
            mReplayWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mReplayFile), "Unicode"));
            mReplayStartTime = System.currentTimeMillis();
            mReplayWriter.write("FFO Replay Version 2.1");
            mReplayWriter.newLine();
            mReplayWriter.newLine();
            mReplayWriter.write(ich().getName() + "//" + ich().getDeck().getDeckName() + "//" + ich().getDeck().getCharsStringForReplay());
            mReplayWriter.newLine();
            mReplayWriter.write(opp().getName() + "//" + opp().getDeck().getDeckName() + "//" + opp().getDeck().getCharsStringForReplay());
            mReplayWriter.newLine();
            mReplayWriter.newLine();
            mReplayWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void finishReplay() {
        if (mReplayWriter != null) {
            try {
                mReplayWriter.newLine();
                mReplayWriter.write("=== 試合時間 " + MSecToMMMSS((int) (System.currentTimeMillis() - mReplayStartTime))
                        + " （" + new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(Calendar.getInstance().getTime()) + " 終了） ===");
                mReplayWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(GameCore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void updateBattleValues(String[] s) {
        if (mBattleSystem == null) {
            return;
        }

        //OPP Leading
        EnumMap<EPlayer, Integer> atk = new EnumMap<EPlayer, Integer>(EPlayer.class);
        EnumMap<EPlayer, Integer> icp = new EnumMap<EPlayer, Integer>(EPlayer.class);
        EnumMap<EPlayer, Integer> hit = new EnumMap<EPlayer, Integer>(EPlayer.class);
        EnumMap<EPlayer, Integer> evasion = new EnumMap<EPlayer, Integer>(EPlayer.class);
        EnumMap<EPlayer, Integer> faith = new EnumMap<EPlayer, Integer>(EPlayer.class);

        if (gi.isAttackPlayer(EPlayer.ICH)) {
            if (bv_inited) {
                atk.put(EPlayer.ICH, FTool.safeParseInt(s[0]) - mBattleSystem.getBattleValues().get(EPlayer.ICH).atk);
                atk.put(EPlayer.OPP, 0);
                icp.put(EPlayer.OPP, FTool.safeParseInt(s[4]) - mBattleSystem.getBattleValues().get(EPlayer.OPP).icp);
                icp.put(EPlayer.ICH, 0);
            }
            mBattleSystem.getBattleValues().get(EPlayer.ICH).atk = FTool.safeParseInt(s[0]);
            mBattleSystem.getBattleValues().get(EPlayer.OPP).icp = FTool.safeParseInt(s[4]);
        } else {
            if (bv_inited) {
                icp.put(EPlayer.ICH, FTool.safeParseInt(s[0]) - mBattleSystem.getBattleValues().get(EPlayer.ICH).icp);
                icp.put(EPlayer.OPP, 0);
                atk.put(EPlayer.ICH, 0);
                atk.put(EPlayer.OPP, FTool.safeParseInt(s[4]) - mBattleSystem.getBattleValues().get(EPlayer.OPP).atk);
            }
            mBattleSystem.getBattleValues().get(EPlayer.ICH).icp = FTool.safeParseInt(s[0]);
            mBattleSystem.getBattleValues().get(EPlayer.OPP).atk = FTool.safeParseInt(s[4]);
        }

        if (bv_inited) {
            faith.put(EPlayer.ICH, FTool.safeParseInt(s[1]) - mBattleSystem.getBattleValues().get(EPlayer.ICH).faith);
            hit.put(EPlayer.ICH, FTool.safeParseInt(s[2]) - mBattleSystem.getBattleValues().get(EPlayer.ICH).hit);
            evasion.put(EPlayer.ICH, FTool.safeParseInt(s[3]) - mBattleSystem.getBattleValues().get(EPlayer.ICH).evasion);
            faith.put(EPlayer.OPP, FTool.safeParseInt(s[5]) - mBattleSystem.getBattleValues().get(EPlayer.OPP).faith);
            hit.put(EPlayer.OPP, FTool.safeParseInt(s[6]) - mBattleSystem.getBattleValues().get(EPlayer.OPP).hit);
            evasion.put(EPlayer.OPP, FTool.safeParseInt(s[7]) - mBattleSystem.getBattleValues().get(EPlayer.OPP).evasion);
        }

        mBattleSystem.getBattleValues().get(EPlayer.ICH).faith = FTool.safeParseInt(s[1]);
        mBattleSystem.getBattleValues().get(EPlayer.ICH).hit = FTool.safeParseInt(s[2]);
        mBattleSystem.getBattleValues().get(EPlayer.ICH).evasion = FTool.safeParseInt(s[3]);
        mBattleSystem.getBattleValues().get(EPlayer.OPP).faith = FTool.safeParseInt(s[5]);
        mBattleSystem.getBattleValues().get(EPlayer.OPP).hit = FTool.safeParseInt(s[6]);
        mBattleSystem.getBattleValues().get(EPlayer.OPP).evasion = FTool.safeParseInt(s[7]);

        if (s[8] != null) {
            mBattleSystem.getBattleValues().get(EPlayer.ICH).executeBorder = Boolean.parseBoolean(s[8]);
        }
        if (s[9] != null) {
            mBattleSystem.getBattleValues().get(EPlayer.OPP).executeBorder = Boolean.parseBoolean(s[9]);
        }

        // Only from host, not reved
        if (s.length > 10) {
            mBattleSystem.getBattleValues().get(EPlayer.OPP).border = Integer.parseInt(s[10]);
            mBattleSystem.getBattleValues().get(EPlayer.ICH).border = Integer.parseInt(s[11]);
            mBattleSystem.getBattleValues().get(EPlayer.OPP).ability_disabled[BattleSystem.Ability.Faith.ordinal()] = Boolean.parseBoolean(s[12]);
            mBattleSystem.getBattleValues().get(EPlayer.ICH).ability_disabled[BattleSystem.Ability.Faith.ordinal()] = Boolean.parseBoolean(s[13]);
        }

        if (bv_inited) {
            gi.getEffects().addManualModification(atk, icp, faith, hit, evasion);
        } else {
            bv_inited = true;
        }

        ui.setBattleSab(s);
    }

    public void pushBattleEvent(Card card, int index) {
        gi.getEffects().add(card, index);
    }

    public void pushDelayEvent(Card card, int index) {
        gi.getDelayEffects().add(card, index);
    }

    public EGameMode getMode() {
        return mMode;
    }

    @Override
    public boolean isHost() {
        return mMode == EGameMode.HOST || mMode == EGameMode.DEBUG;
    }

    @Override
    public boolean isWatcher() {
        return mMode == EGameMode.WATCHER || mMode == EGameMode.RECORDVIEW;
    }

    public boolean isRecordView() {
        return mMode == EGameMode.RECORDVIEW;
    }

    public void writeReplay(String message) {
        if (mReplayWriter != null) {
            try {
                mReplayWriter.write(message + "\r\n");
                mReplayWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(GameCore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void writeAndSendReplay(String message) {
        try {
            if (mReplayWriter != null) {
                mReplayWriter.write(message + "\r\n");
                mReplayWriter.flush();
            }
            send("$REPLAY:" + message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void nextAction() {
        send("$NEXTACTION:");
    }

    public void skipTurn() {
        send("$SKIPTURN:");
    }

    public void startRecord() {
        if (isRecordView() || isWatcher()) {
            return;
        }
        if (mRecorder != null) {
            mRecorder.stop();
        }
        mRecorder = new Recorder(mHostAddress, isHost());
    }

    public void stopRecord() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    public IMainUI getMainUI() {
        return ui;
    }

    @Override
    public GameInformation getGameInformation() {
        return gi;
    }

    private boolean isDebugMode() {
        return mMode == EGameMode.DEBUG;
    }

    @Override
    public void send(String message) {
        if (mMode == EGameMode.DEBUG) {
            return;
        }
        if (isWatcher()) {
            if (message.startsWith("$CHAT:")) {
                message = "$WATCHCHAT:" + message.substring("$CHAT:".length());
            } else if (message.startsWith("$DISCONNECT:")) {
            } else if (message.startsWith("$WATCHER")) {
                // Various watcher commands
            } else if (message.startsWith("$GIVEMECURRENT:")) {
            } else if (message.startsWith("$NEXTACTION:")) {
            } else if (message.startsWith("$SKIPTURN:")) {
            } else {
                return;
            }
        }

        mSocketThread.send(mSocketThread.getSocket(), message);
    }

    private void flipCoin() {
        if (isDebugMode()) {
            gi.setAttackPlayer(EPlayer.ICH);
            return;
        }
        if (Math.random() >= 0.5) {
            send("$YOULAST:");
            ui.playMessage(FTool.parseLocale(289, ich().getName()));
            gi.setAttackPlayer(EPlayer.ICH);
        } else {
            send("$YOUFIRST:");
            ui.playMessage(FTool.parseLocale(289, opp().getName()));
            gi.setAttackPlayer(EPlayer.OPP);
        }
    }

    private Player ich() {
        return gi.getPlayer(EPlayer.ICH);
    }

    private Player opp() {
        return gi.getPlayer(EPlayer.OPP);
    }

    private PlayField f() {
        return gi.getField(EPlayer.ICH);
    }

    private PlayField of() {
        return gi.getField(EPlayer.OPP);
    }

    @Override
    public void sendAndRefreshCounter() {
        ui.refreshCounter();
        if (isWatcher() || isDebugMode()) {
            return;
        }
        send("$COUNTER:" + ich().getHP() + " " + ich().getSP() + " " + f().getHand().size() + " "
                + f().getLibrary().size() + " " + f().getDiscardPile().size() + " " + f().getDiscardPile().getListString());
    }

    public void setTimerActivePlayer(EPlayer player) {
        if (mPlayingTimer != null) {
            mPlayingTimer.setNowTurn(player.ordinal());
        }
    }

    @Override
    public void moveCard(Card card, EPlayer newController, EMoveTarget to, boolean active, boolean simple, String extraMessage) {
        if (card.isNull()) {
            return;
        }
        if (isWatcher()) {
            simple = true;
        }
        if (active && to == EMoveTarget.BATTLE) {
            // If already have a battle card
            if (!gi.getField(card.getController()).getBattleCard().isNull()) {
                return;
            }
            // If try to start battle in passive turn
            if (!gi.isAttackPlayer(card.getController()) && gi.getField(FTool.rev(card.getController())).getBattleCard().isNull()) {
                return;
            }
        }
        if (to == EMoveTarget.ATTACH) {
            cardWaitingAttach = (SupportCard) card;
            ui.startChooseAttach();
            return;
        }

        EPlayer orgController = card.getController();
        District fromDistrict = card.getDistrict();
        ERegion from = card.getRegion();

        if (active) {
            payCost(card, newController, to);
        }
        fromDistrict.handleMoving(this, card, to, newController, active, simple, extraMessage);
        handleCardMoving(card, newController, to);
        int index = removeCard(card);
        updateCardDistrict(card, newController, to, active, index);

        if (active) {
            if (fromDistrict instanceof RevealDistrict) {
                send("$MOVE:" + card.getCardNo() + " " + orgController + " " + ERegion.REVEAL.name() + " " + index + " " + newController + " " + to);
            } else {
                send("$MOVE:" + card.getCardNo() + " " + orgController + " " + from + " " + index + " " + newController + " " + to);
            }
        }
        if (to == EMoveTarget.BATTLE) {
            setBattleCard((SpellCard) card, active);
        }

        actionDone();
    }

    private void payCost(Card card, EPlayer newController, EMoveTarget to) {
        switch (card.getInfo().getCardType()) {
            case SPELL:
                if (card.getRegion() == ERegion.RESERVED && to == EMoveTarget.ACTIVATED && card.getController() == newController) {
                    card.getInfo().payCost(gi.getPlayer(EPlayer.ICH), this);
                }
                break;
            case EVENT:
            case SUPPORT:
                if (card.getRegion() == ERegion.HAND && (to == EMoveTarget.ATTACH || to == EMoveTarget.EVENT
                        || to == EMoveTarget.SCENE || to == EMoveTarget.LEADER_ATTACHMENTS
                        || to == EMoveTarget.RESERVED || to == EMoveTarget.ACTIVATED)) {
                    card.getInfo().payCost(gi.getPlayer(EPlayer.ICH), this);
                }
                break;
        }
    }

    private void handleCardMoving(Card card, EPlayer newController, EMoveTarget to) {
        if (card.isIchControl()) {
            switch (card.getInfo().getCardType()) {
                case SPELL:
                    SpellMoveTriggerManager.exec((SpellCard) card, this, gi, card.getRegion(), to);
                    if (card.getRegion() == ERegion.RESERVED
                            && (to == EMoveTarget.ACTIVATED || to == EMoveTarget.ACTIVATED_NOCOST)
                            && card.getController() == newController) {
                        ActivateManager.exec(this, gi, (SpellCard) card, to == EMoveTarget.ACTIVATED_NOCOST);
                    }
                    break;
                case EVENT:
                    break;
                case SUPPORT:
                    SupportMoveTriggerManager.exec((SupportCard) card, this, gi, card.getRegion(), newController, to);
                    break;
            }
        }
    }

    private int removeCard(Card card) {
        EPlayer orgController = card.getController();
        ERegion orgRegion = card.getRegion();
        CardLabel orgLabel = card.getLabel();
        int ret = card.leave(this);
        ui.removeLabel(orgLabel, orgController, orgRegion);
        return ret;
    }

    private void updateCardDistrict(Card card, EPlayer newController, EMoveTarget to, boolean active, int orgIndex) {
        District district;
        switch (to) {
            case RESERVED:
                district = gi.getField(newController).getReservedDistrict();
                break;
            case ACTIVATED:
            case ACTIVATED_NOCOST:
                district = gi.getField(newController).getActivatedDistrict();
                break;
            case LEADER_ATTACHMENTS:
                district = gi.getField(newController).getLeaderAttachmentDistrict();
                break;
            case BATTLE:
                district = gi.getField(newController).getBattleCardDistrict();
                break;
            case SCENE:
                district = gi.getSceneDistrict();
                break;
            case EVENT:
                district = gi.getField(newController).getEventCardDistrict();
                break;
            case HAND:
            case HIDE_TO_HAND:
                district = gi.getField(newController).getHandDistrict();
                break;
            case LIBRARY_TOP:
            case LIBRARY_BOTTOM:
            case SHUFFLE_THEN_TOP:
                district = gi.getField(newController).getLibraryDistrict();
                break;
            case DISCARD_PILE:
                district = gi.getField(newController).getDiscardPileDistrict();
                break;
            default:
                throw new UnsupportedOperationException("Invalid move target.");
        }

        card.setController(newController);
        card.setDistrict(district);
        card.updateLabel(this);

        if (card instanceof SpellCard) {
            SpellCard sp = (SpellCard) card;
            if (sp.isAttached()) {
                SupportCard sup = sp.getAttachedDontThrow();
                sup.setController(newController);
                sup.setDistrict(district);
                sup.updateLabel(this);
            }
        }

        district.add(this, card, to);
    }

    @Override
    public void returnDiscardPileToLibrary() {
        for (int i = 0; i < f().getDiscardPile().size();) {
            moveCard(f().getDiscardPile().get(0), EPlayer.ICH, EMoveTarget.LIBRARY_TOP, false, false, "");
        }
        ui.playMessage(ich().getName() + FTool.getLocale(30) + FTool.getLocale(129));
        ui.actionDone();
    }

    private void sendBattleSab() {
        if (mBattleSystem == null) {
            return;
        }
        BattleValues ich = mBattleSystem.getBattleValues().get(EPlayer.ICH);
        BattleValues opp = mBattleSystem.getBattleValues().get(EPlayer.OPP);
        send("$BATTLESAB:" + (gi.isAttackPlayer(EPlayer.ICH) ? ich.getAttackValue() : ich.getInterceptValue())
                + GameCore.BATTLESAB_SEP + ich.getFaith() + GameCore.BATTLESAB_SEP
                + ich.getHit() + GameCore.BATTLESAB_SEP + ich.getEvasion() + GameCore.BATTLESAB_SEP
                + (gi.isAttackPlayer(EPlayer.ICH) ? opp.getInterceptValue() : opp.getAttackValue())
                + GameCore.BATTLESAB_SEP
                + opp.faith + GameCore.BATTLESAB_SEP + opp.hit + GameCore.BATTLESAB_SEP
                + opp.evasion + GameCore.BATTLESAB_SEP
                + ich.executeBorder + GameCore.BATTLESAB_SEP + opp.executeBorder
                + (isHost() ? (GameCore.BATTLESAB_SEP + ich.border
                + GameCore.BATTLESAB_SEP + opp.border + GameCore.BATTLESAB_SEP
                + ich.ability_disabled[BattleSystem.Ability.Faith.ordinal()]
                + GameCore.BATTLESAB_SEP + opp.ability_disabled[BattleSystem.Ability.Faith.ordinal()])
                : ""));
    }

    /**
     *
     * @param values Ich's power, faith, hit, evasion, opp's 4, ich do border
     */
    public void adjustBattleValue(int[] values, boolean execBorder) {
        BattleValues ich = mBattleSystem.getBattleValues().get(EPlayer.ICH);
        BattleValues opp = mBattleSystem.getBattleValues().get(EPlayer.OPP);
        if (gi.isAttackPlayer(EPlayer.ICH)) {
            ich.atk = values[0];
            opp.icp = values[4];
        } else {
            ich.icp = values[0];
            opp.atk = values[4];
        }
        ich.faith = values[1];
        ich.hit = values[2];
        ich.evasion = values[3];
        opp.faith = values[5];
        opp.hit = values[6];
        opp.evasion = values[7];
        ich.executeBorder = execBorder;
    }

    public void setBattleCard(SpellCard card, boolean active) {
        if (card.getController() == EPlayer.ICH) {
            ui.setTimerTurn(EPlayer.OPP);
        } else {
            ui.setTimerTurn(EPlayer.ICH);
        }

        if (!card.isNull()) {
            FTool.playSound("combat.wav");
        }

        mBattling = true;

        if (active) {
            mIchBattleCardSet = true;
        }

        if (!isHost()) {
            mBattleSystem = new BattleSystem(this, gi);
            mBattleResult = new BattleResult(gi.isIchAttackPlayer());
        }

        if (isWatcher()) {
            return;
        }

        if (active) {
            ExtraBattleCostManager.exec(this, gi, card);
            sendAndRefreshCounter();
        }

        // Start battle
        if (card.isNull() && (!gi.isIchAttackPlayer() && active)
                || (gi.isIchAttackPlayer() && !active)) {
            invokeBattle();
        }
    }

    private void invokeBattle() {
        if (isHost()) {
            startBattle();
        } else {
            send("$INVOKEBATTLE:");
        }
        ui.showCommandPanel("battle_sab");
    }

    /**
     * Can only called when is host.
     */
    public void startBattle() {
        if (!isHost()) {
            throw new UnsupportedOperationException("startBattle() called by a client.");
        }
        mBattleSystem = new BattleSystem(this, gi);
        mBattleResult = new BattleResult(gi.isIchAttackPlayer());
        calculateBattleValues();

        SpellCard attackSpell = gi.getField(gi.getAttackPlayer()).getBattleCard();
        SpellCard interceptSpell = gi.getField(FTool.rev(gi.getAttackPlayer())).getBattleCard();
        String attackPlayerName = gi.getPlayer(gi.getAttackPlayer()).getName();
        String interceptPlayerName = gi.getPlayer(FTool.rev(gi.getAttackPlayer())).getName();
        if (attackSpell.isNull()) {
            // No attack
        } else {
            if (interceptSpell.isNull()) {
                // Intercept through
                String replay = FTool.parseLocale(290, attackPlayerName, attackSpell.getName());
                writeAndSendReplay(replay);
            } else {
                // Both have spell
                String replay = FTool.parseLocale(291, attackPlayerName, attackSpell.getName(), interceptSpell.getName(), interceptPlayerName);
                writeAndSendReplay(replay);
            }
        }
    }

    public void calculateBattleValues() {
        if (mBattleSystem == null) {
            return;
        }
        mBattleSystem.exec();
        sendBattleSab();
        bv_inited = true;
        ui.showBattleValue(mBattleSystem.getBattleValues());
    }

    public void resetBattleSab() {
        ui.resetBattleSab();
        if (isHost()) {
            gi.getEffects().clear();
        }
    }

    /**
     * Only caculate Ich's damage, hit, and faith. Will get information of opponent's from socket.
     */
    public void execBattle(boolean active) {
        if (isHost()) {
            // Final synchronize
            sendBattleSab();
        }

        BattleValues v = mBattleSystem.getBattleValues().get(EPlayer.ICH);
        BattleValues ov = mBattleSystem.getBattleValues().get(EPlayer.OPP);

        int oppPower = gi.isIchAttackPlayer() ? ov.icp : ov.atk;
        if (v.ability_disabled[Ability.Faith.ordinal()]) {
            v.faith = 0;
        }
        if (ov.ability_disabled[Ability.Faith.ordinal()]) {
            ov.faith = 0;
        }

        if (ov.hit == v.evasion && v.executeBorder) {
            ich().consume(v.border);
            mBattleResult.setHit(false);
        } else if (ov.hit > v.evasion || (ov.hit == v.evasion && !v.executeBorder)) {
            mBattleResult.setHit(true);
            int damage = DamageManuplateManager.exec(oppPower, this, EDamageType.SPELL, active);
            if (damage <= v.faith - ov.faith && v.faith > 0 && damage > 0) {
                mBattleResult.setFaithed(true);
            } else {
                ich().damage(damage);
                mBattleResult.setFaithed(false);
                mBattleResult.setDamage(damage);
            }
        } else {
            mBattleResult.setHit(false);
        }

        if (active) {
            send("$BATTLEGO:");
        } else {
            send("$BATTLERESULT:" + mBattleResult.getSocketString());
        }
    }

    /**
     * Called by both player.
     * @param socketString
     */
    public void execBattleResult(String socketString) {
        if (isWatcher()) {
            if (mBattling) {
                FTool.playSound("combatd.wav");
                mBattling = false;
                actionDone();
            }
            return;
        }

        BattleResult oppResult = new BattleResult(socketString);

        if (isHost()) {
            send("$BATTLERESULT:" + mBattleResult.getSocketString());
            // Clear battle values
            mHostBattleOK = false;
            mClientBattleOK = false;
        }

        if (gi.isIchAttackPlayer()) {
            displayBattleResultText(oppResult);
        }

        EnumMap<EPlayer, Boolean> hits = new EnumMap<EPlayer, Boolean>(EPlayer.class);
        hits.put(EPlayer.ICH, mBattleResult.isHit());
        hits.put(EPlayer.OPP, oppResult.isHit());
        EnumMap<EPlayer, Integer> damages = new EnumMap<EPlayer, Integer>(EPlayer.class);
        damages.put(EPlayer.ICH, mBattleResult.getDamage());
        damages.put(EPlayer.OPP, oppResult.getDamage());
        HitDamagedManager.exec(this, gi, mBattleSystem, hits, damages);

        mBattleSystem.handleBattleEnd();
        moveBattleCard();

        resetBattleSab();
        mBattling = false;
        mIchBattleCardSet = false;

        FTool.playSound("combatd.wav");

        gi.setPhase(EPhase.ACTIVATION);
        actionDone();
    }

    private void displayBattleResultText(BattleResult oppResult) {
        if (f().getBattleCard().isNull()) {
            //Impossible
            ui.insertMessage(FTool.getLocale(304), Color.RED);
        } else if (of().getBattleCard().isNull()) {
            //Opponent Through
            ui.playMessage(FTool.parseLocale(295, gi.myName(), opp().getName(), oppResult.getResultString()));
        } else {
            //No Through
            ui.playMessage(FTool.parseLocale(296, ich().getName(), opp().getName(),
                    mBattleResult.getResultString(), oppResult.getResultString()));
        }
    }

    private void moveBattleCard() {
        EMoveTarget moveTo = mBattleResult.isHit() ? EMoveTarget.RESERVED : EMoveTarget.ACTIVATED;
        moveCard(gi.getField(EPlayer.ICH).getBattleCard(), EPlayer.ICH, moveTo, true, true, "");
        ui.postBattle();
        ui.setTimerTurn(gi.getAttackPlayer());
    }

    public boolean isIchBattleCardSet() {
        return mIchBattleCardSet;
    }

    public void through() {
        if (gi.isIchAttackPlayer()) {
            if (f().getBattleCard().isNull()) {
                noAttack(true);
            }
        } else {
            if (!mBattling) {
                // Meaningless through
                return;
            }
            // No intercept
            ui.noLogPlayMessage(FTool.parseLocale(70, gi.myName()));
            setBattleCard(SpellCard.newNull(), true);
            ui.through(true);
        }
    }

    /**
     * Attack player does not attack.
     * @param active 
     */
    public void noAttack(boolean active) {
        if (active && !isWatcher()) {
            setBattleCard(SpellCard.newNull(), true);
            ui.noLogPlayMessage(FTool.parseLocale(193, gi.myName()));
            send("$NOATTACK:");
        }
        mBattleSystem = new BattleSystem(this, gi);
        mBattleSystem.handleBattleEnd();
        mBattling = false;
        gi.setPhase(EPhase.ACTIVATION);
    }

    public void setPassed(EPlayer ep, boolean pass) {
        passed.put(ep, pass);
    }

    @Override
    public boolean attach(CardLabel target, boolean active) {
        return attach(cardWaitingAttach, target, active);
    }

    public boolean attach(SupportCard support, CardLabel target, boolean active) {
        if (active && !ui.isChoosingAttach() && !isWatcher()) {
            return false;
        }
        switch (target.getCard().getRegion()) {
            case RESERVED:
            case ACTIVATED:
                SpellCard sp = (SpellCard) target.getCard();
                if (sp.isAttached()) {
                    return false;
                }
                if (active && support.getRegion() == ERegion.HAND) {
                    support.getInfo().payCost(ich(), this);
                }
                EMoveTarget emt = target.getCard().getRegion() == ERegion.RESERVED ? EMoveTarget.RESERVED : EMoveTarget.ACTIVATED;
                SupportMoveTriggerManager.exec(support, this, gi, support.getRegion(), target.getCard().getController(), emt);
                removeCard(support);
                sp.attach(support);
                support.updateLabel(this);
                ui.doAttach(support, (SpellLabel) target);
                if (support.isIchControl()) {
                    AttachmentTriggerManager.exec(this, support, target.getCard());
                }
                if (active) {
                    send("$ATTACH:" + support.getCardNo() + " " + sp.getController() + " " + sp.getRegion() + " " + sp.getPosition());
                    ui.playMessage(FTool.parseLocale(284, ich().getName(), support.getName(),
                            gi.getPlayer(sp.getController()).getName(), sp.getName()));
                }
                break;
            case CHARACTERS:
                moveCard(support, target.getCard().getController(), EMoveTarget.LEADER_ATTACHMENTS, true, false, "");
                break;
            default:
                return false;
        }
        ui.stopAttach();
        ui.actionDone();
        return true;
    }

    @Override
    public SupportCard unAttach(SpellCard card, boolean active) {
        try {
            SupportCard ret = card.unattach();
            ui.unAttach(card.getSpellLabel(), ret.getSupportLabel());
            if (active) {
                send("$UNATTACH:" + card.getController() + " " + card.getRegion() + " " + card.getPosition());
            }
            return ret;
        } catch (NotAttachedException ex) {
            return SupportCard.newNull();
        }
    }

    /**
     * Change HP. No message sending, only number adjustments.
     * <p>Always adjust Ich's HP.</p>
     * @param ep
     * @param amount
     * @param extramessage
     */
    @Override
    public void adjustHP(int amount, boolean active, boolean isDamage, String extramessage) {
        /* Only do ICH damage changes here */
        if (amount < 0) {
            if (isDamage) {
                int damage = DamageManuplateManager.exec(-amount, this, isDamage ? EDamageType.EVENT_ABILITY : EDamageType.COST, active);
                amount = -damage;
            }
        }

        // Non-healable checking. No extramessage means manual, let it pass.
        if (amount > 0 && extramessage.length() > 0 && !gi.isHealable(EPlayer.ICH)) {
            return;
        }

        Player player = gi.getPlayer(EPlayer.ICH);

        if (amount > 0) {
            player.heal(amount);
            ui.playMessage(FTool.parseLocale(71, player.getName(), "＋", String.valueOf(amount), String.valueOf(player.getHP())) + extramessage);
            if (f().getLeaderAttachment().hasCard(819)) {
                player.heal(1);
                ui.playMessage(FTool.parseLocale(71, player.getName(), "＋", "1", String.valueOf(player.getHP())) + " - " + CardDatabase.getInfo(819).getName());
            }
        } else if (amount < 0) {
            player.damage(-amount);
            ui.playMessage(FTool.parseLocale(71, player.getName(), "－", String.valueOf(-amount), String.valueOf(player.getHP())) + extramessage);
        }

        checkMaxHP();
        sendAndRefreshCounter();
    }

    @Override
    public void setHP(int value, String extramessage) {
        if (isWatcher()) {
            return;
        }
        int delta = value - ich().getHP();
        if (delta == 0) {
            return;
        }
        String positivesign = "";
        if (delta >= 0) {
            positivesign = "+";
        }
        ich().setHP(value);
        ui.playMessage(ich().getName() + FTool.getLocale(67) + value + " (" + positivesign + delta + ")" + FTool.getLocale(69) + extramessage);
        checkMaxHP();
        sendAndRefreshCounter();
    }

    /**
     * Check if HP is reaching the leader's max.
     * <p>Only called by Ich.</>
     */
    private void checkMaxHP() {
        if (ich().getHP() > f().getLeader().getInfo().getHitPoints()) {
            ich().setHP(f().getLeader().getInfo().getHitPoints());
            ui.playMessage(ich().getName() + FTool.getLocale(191) + " (" + String.valueOf(ich().getHP()) + ")");
        }
    }

    @Override
    public void adjustSP(int delta, boolean active, String extramessage) {
        Player player = ich();
        if (delta < 0) {
            if (player.getSP() < Math.abs(delta)) {
                ui.playMessage(FTool.parseLocale(233, player.getName(), String.valueOf(player.getSP() + delta)) + extramessage);
                player.setSP(0);
            } else {
                player.consume(-delta);
            }
            ui.playMessage(FTool.parseLocale(73, player.getName(), "－", String.valueOf(-delta), String.valueOf(player.getSP())) + extramessage);
        } else if (delta > 0) {
            player.replenish(delta);
            ui.playMessage(FTool.parseLocale(73, player.getName(), "＋", String.valueOf(delta), String.valueOf(player.getSP())) + extramessage);
        }
        sendAndRefreshCounter();
    }

    @Override
    public void setSP(int value, String extramessage) {
        if (isWatcher()) {
            return;
        }
        int delta = value - ich().getSP();
        if (delta == 0) {
            return;
        }
        String positivesign = "";
        if (delta >= 0) {
            positivesign = "+";
        }
        ich().setSP(value);
        ui.playMessage(gi.myName() + FTool.getLocale(68) + value + " (" + positivesign + delta + ")" + FTool.getLocale(69));
        sendAndRefreshCounter();
    }

    @Override
    public void deckout(String extramessage) {
        if (isWatcher()) {
            return;
        }
        Card card = f().getLibraryDistrict().simplePop();
        if (card == null) {
            return;
        }
        f().getDiscardPileDistrict().add(card);
        ui.playMessage(FTool.parseLocale(55, gi.myName(), card.getName()) + extramessage);
        sendAndRefreshCounter();
    }

    public void actionDone() {
        ui.actionDone();
    }

    @Override
    public IAutosCallback getAutosCallback() {
        return this;
    }

    @Override
    public void changeLeader(boolean active, int changeto) {
        PlayField f = active ? f() : of();
        EPlayer ep = active ? EPlayer.ICH : EPlayer.OPP;
        f.setLeader(f.getChars().get(changeto));
        f.getLeader().updateLabel(this);
        f.getLeader().getLabel().setPopupMenu(f.getChars());
        ui.showCharacters(ep);
        if (active) {
            send("$CHANGELEADER:" + changeto);
            ui.playMessage(gi.myName() + FTool.getLocale(30) + FTool.getLocale(103) + f.getLeader().getName() + FTool.getLocale(104));
            checkMaxHP();
            sendAndRefreshCounter();
        }
    }

    public void showHand(String extraMessage) {
        StringBuilder buf = new StringBuilder();
        for (Card c : f().getHand()) {
            buf.append(c.getCardNo()).append(" ");
        }

        send("$REVEAL:" + ERegion.HAND + "//" + buf);
        ui.noLogPlayMessage(gi.myName() + FTool.getLocale(105) + extraMessage);
    }

    @Override
    public void revealLibTops(int amount, String extraMessage) {
        StringBuilder buf = new StringBuilder();
        CardSet<Card> lib = f().getLibrary();
        if (amount > lib.size()) {
            amount = lib.size();
        }
        ArrayList<Card> show = new ArrayList<Card>(lib.subList(lib.size() - amount, lib.size()));
        Collections.reverse(show);
        for (Card c : show) {
            buf.append(c.getCardNo()).append(" ");
        }

        send("$REVEAL:" + ERegion.LIBRARY + "//" + buf);

        if (amount == lib.size()) {
            ui.noLogPlayMessage(FTool.parseLocale(106, gi.myName()) + extraMessage);
        } else {
            ui.noLogPlayMessage(FTool.parseLocale(158, gi.myName(), String.valueOf(amount)) + extraMessage);
        }
    }

    public ArrayList<Watcher> getWatchers() {
        return mWatchers;
    }

    public void deliverHand() {
        String hands = "";
        for (Card c : gi.getField(EPlayer.ICH).getHand()) {
            hands += c.getCardNo() + " ";
        }
        if (hands.length() > 0) {
            hands = hands.substring(0, hands.length() - 1);
        }
        for (Watcher w : mShareHands) {
            ((HostSocketThread) mSocketThread).deliverTo("$REVEAL:" + ERegion.HAND + "//" + hands, w.getId());
        }
    }

    @Override
    public void nextTurn(boolean active) {
        if (isWatcher()) {
            gi.nextTurn();
            FTool.playSound("endoft.wav");
            return;
        }

        TurnEndManager.exec(this, getGameInformation());

        bv_inited = false;
        passed.put(EPlayer.ICH, false);
        passed.put(EPlayer.OPP, false);

        FTool.playSound("endoft.wav");

        if (gi.isAttackPlayer(EPlayer.ICH) && active) {
            send("$NEXT:");

            if (!isWatcher()) {
                ui.noLogPlayMessage(FTool.getLocale(41) + gi.getTurn() + FTool.getLocale(40));
            }
        }
        
        gi.getField(EPlayer.ICH).getBattleCardDistrict().clearLastCard();

        if (mMode == EGameMode.DEBUG) {
            gi.nextTurn();
        }
        gi.nextTurn();

        // Random Scene
        if (FTool.readBooleanConfig("randomscene", false) && gi.getTurn() % 5 == 2) {
            ArrayList<String> scenes = BackgroundManager.listScene();
            CardInfo info = CardDatabase.getInfo(scenes.get(new Random().nextInt(scenes.size())));
            SupportCard scene = ((SupportCardInfo) info).createCard(EPlayer.ICH, EPlayer.ICH, gi.getSceneDistrict());
            gi.getSceneDistrict().set(scene);
            ui.playMessage(FTool.getLocale(227) + scene.getName());
        }

        // Replenish phase begins
        ReplenishBeginManager.exec(this, getGameInformation());

        if (gi.isIchAttackPlayer()) {
            ich().replenish(f().getReserved().size() + 1);
            sendAndRefreshCounter();
            if (FTool.readBooleanConfig("autodraw", true)) {
                draw(1, null);
            } else {
                ui.playMessage(FTool.getLocale(102));
            }
        }
        ui.showTurn(gi);


        Player attackPlayer = gi.getPlayer(gi.getAttackPlayer());
        Player interceptPlayer = gi.getPlayer(FTool.rev(gi.getAttackPlayer()));
        PlayField attackField = gi.getField(gi.getAttackPlayer());
        PlayField interceptField = gi.getField(FTool.rev(gi.getAttackPlayer()));

        if (gi.isIchAttackPlayer()) {
            String replay = FTool.parseLocale(310, String.valueOf(gi.getTurn()),
                    attackPlayer.getName(),
                    String.valueOf(attackPlayer.getHP()),
                    String.valueOf(interceptPlayer.getHP()),
                    String.valueOf(attackPlayer.getSP()),
                    String.valueOf(interceptPlayer.getSP()),
                    String.valueOf(attackField.getHand().size()),
                    String.valueOf(interceptField.getHand().size()),
                    // Opponent's deck will be drawn by 1, devrease here
                    String.valueOf(gi.isIchAttackPlayer() ? attackField.getLibrary().size() : attackField.getLibrary().size() - 1),
                    String.valueOf(interceptField.getLibrary().size()),
                    String.valueOf(attackField.countSpellOnField(-1)),
                    String.valueOf(interceptField.countSpellOnField(-1)),
                    mPlayingTimer != null ? mPlayingTimer.getTimeString(0) : "NaN",
                    mPlayingTimer != null ? mPlayingTimer.getTimeString(1) : "NaN",
                    gi.getScene().isNull() ? "なし" : gi.getScene().getName());

            writeAndSendReplay("");
            writeAndSendReplay(replay);

            if (FTool.readBooleanConfig("verbosereplay", true)) {
                replay = FTool.getLocale(317);
                for (Card c : f().getHand()) {
                    replay += c.getName() + "//";
                }
                writeReplay(replay);
            }

        }

        if (FTool.readBooleanConfig("autodraw", true)) {
            actionDone();
        }
    }

    /**
     * opp's 
     * @param values
     * @return
     */
    public boolean changeBattleValues(String[] values, boolean active) {
        if (isHost() && active || !isHost() && !active) {
            mHostBattleOK = false;
        } else {
            mClientBattleOK = false;
        }

        if (!active) {
            // Exchange player
            String[] p = new String[9];
            for (int i = 0; i < 4; ++i) {
                p[i] = values[4 + i];
            }
            for (int i = 4; i < 8; ++i) {
                p[i] = values[i - 4];
            }
            for (int i = 0; i < 8; ++i) {
                values[i] = p[i];
            }
            values[9] = p[8];
            values[8] = null;
        }

        updateBattleValues(values);

        if (active) {
            sendBattleSab();
        }
        return true;
    }

    public void addnewWatcher(Watcher watcher) {
        mWatchers.add(watcher);
    }

    public void removeWatcher(int id) {
        Watcher target = null;
        for (Watcher w : mWatchers) {
            if (w.getId() == id) {
                target = w;
                break;
            }
        }
        if (target != null) {
            ui.insertNoLogMessage(target.getName() + FTool.getLocale(120), Color.MAGENTA);
            mWatchers.remove(target);
        }
    }

    @Override
    public void useAbility(Card card, int index, ECostType costType, int cost, boolean active) {
        ui.setTimerTurn(active ? EPlayer.OPP : EPlayer.ICH);

        UseEventAbilityTriggerManager.exec(this, gi, card, index);

        int ability_type = PrecheckEffectManager.FLAG_BATTLE;
        if (active) {
            payAbilityCost(card, costType, cost);
            ability_type = PrecheckEffectManager.handle(card, index, this, gi);
            ui.playMessage(FTool.parseLocale(223, gi.myName(), card.getName(), String.valueOf(index)));
            send("$ABILITY:" + card.getCardNo() + " " + index);
            sendAndRefreshCounter();
        }

        if ((ability_type & PrecheckEffectManager.FLAG_BATTLE) == PrecheckEffectManager.FLAG_BATTLE) {
            if (isHost()) {
                pushBattleEvent(card, index);
                if (mBattling) {
                    calculateBattleValues();
                }
            }
        }
        if ((ability_type & PrecheckEffectManager.FLAG_DELAY) == PrecheckEffectManager.FLAG_DELAY) {
            if (active) {
                pushDelayEvent(card, index);
            }
        }
    }

    private void payAbilityCost(Card card, ECostType costType, int cost) {
        switch (costType) {
            case SP:
                ich().consume(cost);
                break;
            case HP:
                ich().damage(cost);
                break;
            case DECK:
                for (int i = 0; i < cost; ++i) {
                    deckout("");
                }
                break;
        }

        // Secondary costs
        switch (card.getCardNo()) {
            case 2018:
                ich().consume(1);
                break;
        }

    }

    @Override
    public void setTarget(boolean active, Card card) {
        ui.playMessage(FTool.parseLocale(261, (active ? ich() : opp()).getName(), card.getName()));
    }

    @Override
    public void invokeChoice(boolean active, Card card, int index) {
        int choice_type = PrecheckEffectManager.FLAG_BATTLE;
        if (active) {
            choice_type = PrecheckEffectManager.handle(card, index, this, gi);
            ui.playMessage(FTool.parseLocale(236, gi.myName(),
                    card.getName(), card.getInfo().getChoiceEffect(index).getMenuText()));
            send("$CHOICE:" + card.getCardNo() + " " + index);
            sendAndRefreshCounter();
        } else {
            choice_type = PrecheckEffectManager.getType(card, index, this, gi);
        }

        if ((choice_type & PrecheckEffectManager.FLAG_BATTLE) == PrecheckEffectManager.FLAG_BATTLE) {
            if (isHost()) {
                pushBattleEvent(card, index);
                if (mBattling) {
                    calculateBattleValues();
                }
            }
        }
        if ((choice_type & PrecheckEffectManager.FLAG_DELAY) == PrecheckEffectManager.FLAG_DELAY) {
            pushDelayEvent(card, index);
        }
    }

    public void reExecuteBattleSystem() {
        if (mBattling) {
            calculateBattleValues();
        }
    }

    public void sendCurrent(int watcherNo) {
        if (mSocketThread != null) {
            if (mSocketThread.getClass().equals(HostSocketThread.class)) {
                ((HostSocketThread) mSocketThread).deliverTo("$CURRENT:" + currentState(), watcherNo);
            } else {
                send("#TO" + watcherNo + "*$CURRENT:" + currentState());
            }
        } else {
            send("#TO" + watcherNo + "*$CURRENT:" + currentState());
        }
    }

    public String currentState() {
        CurrentState state = new CurrentState();
        CurrentStateItem operator;

        //Change to easy expressions

        for (EPlayer ep : EPlayer.values()) {
            Player player = gi.getPlayer(ep);
            PlayField f = gi.getField(ep);

            operator = state.makeItem("NAME_" + ep);
            operator.addValue(player.getName());

            operator = state.makeItem("LEADER_" + ep);
            operator.addValue(f.getLeader().getCardNo());

            operator = state.makeItem("LS_" + ep);
            for (Card s : f.getLeaderAttachment()) {
                operator.addValue(s.getCardNo() + CurrentStateItem.ownerSeprator + s.getOwner().name());
            }

            operator = state.makeItem("CHARS_" + ep);
            for (int ch : player.getDeck().getCharacters()) {
                operator.addValue(ch);
            }

            operator = state.makeItem("DECKNAME_" + ep);
            operator.addValue(player.getDeck().getDeckName());

            operator = state.makeItem("HP_" + ep);
            operator.addValue(player.getHP());

            operator = state.makeItem("SP_" + ep);
            operator.addValue(player.getSP());

            operator = state.makeItem("HANDSIZE_" + ep);
            operator.addValue(f.getHand().size());

            operator = state.makeItem("LIBSIZE_" + ep);
            operator.addValue(f.getLibrary().size());

            operator = state.makeItem("DISCARDPILELIST_" + ep);
            for (Card dc : f.getDiscardPile()) {
                operator.addValue(dc.getCardNo());
            }

            operator = state.makeItem("RESERVED_" + ep);
            for (Card c : f.getReserved()) {
                operator.addValue(c.getCardNo() + CurrentStateItem.ownerSeprator + c.getOwner().name());
            }

            operator = state.makeItem("RESERVEDATTACH_" + ep);
            for (Card c : f.getReserved()) {
                operator.addValue(((SpellCard) c).getAttachedOrNullObject().getCardNo() + CurrentStateItem.ownerSeprator + ((SpellCard) c).getAttachedOrNullObject().getOwner().name());
            }

            operator = state.makeItem("ACTIVATED_" + ep);
            for (Card c : f.getActivated()) {
                operator.addValue(c.getCardNo() + CurrentStateItem.ownerSeprator + c.getOwner().name());
            }

            operator = state.makeItem("ACTIVATEDATTACH_" + ep);
            for (Card c : f.getActivated()) {
                operator.addValue(((SpellCard) c).getAttachedOrNullObject().getCardNo() + CurrentStateItem.ownerSeprator + ((SpellCard) c).getAttachedOrNullObject().getOwner().name());
            }

            operator = state.makeItem("BATTLECARD_" + ep);
            operator.addValue(f.getBattleCard().getCardNo() + CurrentStateItem.ownerSeprator
                    + f.getBattleCard().getOwner().name());

            operator = state.makeItem("BATTLECARDATTACH_" + ep);
            operator.addValue(f.getBattleCard().getAttachedOrNullObject().getCardNo() + CurrentStateItem.ownerSeprator
                    + f.getBattleCard().getAttachedOrNullObject().getOwner().name());

            operator = state.makeItem("EVENT_" + ep);
            operator.addValue(f.getEventCard().getCardNo());

            operator = state.makeItem("TIMER_" + ep);
            if (mPlayingTimer != null) {
                operator.addValue((int) mPlayingTimer.getEplisedTime(ep.ordinal()));
            } else {
                operator.addValue(0);
            }

        }

        operator = state.makeItem("SCENE");
        operator.addValue(gi.getScene().getCardNo() + CurrentStateItem.ownerSeprator + gi.getScene().getOwner().name());

        operator = state.makeItem("TURN");
        operator.addValue(gi.getTurn());

        operator = state.makeItem("PHASE");
        operator.addValue(gi.getPhase().name());

        operator = state.makeItem("HOSTTURN");
        operator.addValue(String.valueOf(gi.isAttackPlayer(EPlayer.ICH)));

        operator = state.makeItem("HOSTTIMERRUNNING");
        if (mPlayingTimer != null) {
            operator.addValue(mPlayingTimer.getNowTurn());
        } else {
            operator.addValue(0);
        }

        operator = state.makeItem("WATCHER");
        for (Watcher w : mWatchers) {
            operator.addValue(w.toString());
        }

        return state.getResult();
    }

    @Override
    public void randomDiscard(String extramessage) {
        if (isWatcher()) {
            return;
        }
        if (f().getHand().size() == 0) {
            return;
        }
        int rnd = new Random().nextInt(f().getHand().size());
        Card card = f().getHand().get(rnd);
        moveCard(card, EPlayer.ICH, EMoveTarget.DISCARD_PILE, true, true, "");
        FTool.playSound("discard.wav");
        ui.playMessage(FTool.parseLocale(54, gi.myName(), card.getName()) + extramessage);
        actionDone();
    }

    @Override
    public void peekWholeLibrary(String extramessage) {
        ui.peekWholeLibrary(extramessage);
    }

    @Override
    public void peekLibrary(int amount, String extramessage) {
        if (amount == f().getLibrary().size()) {
            ui.playMessage(FTool.parseLocale(209, gi.myName()) + extramessage);
        } else {
            ui.playMessage(FTool.parseLocale(210, gi.myName(), String.valueOf(amount)) + extramessage);
        }
        ui.setLibraryPeekingAmount(amount);
        ui.showRegion(ERegion.LIBRARY);
        actionDone();
    }

    public void sendHandBack() {
        while (f().getHand().size() > 0) {
            moveCard(f().getHand().get(0), EPlayer.ICH, EMoveTarget.LIBRARY_TOP, false, true, "");
        }
        ui.playMessage(gi.myName() + FTool.getLocale(30) + FTool.getLocale(128));
        actionDone();
    }

    @Override
    public void shuffle() {
        f().shuffle();
        ui.playMessage(FTool.parseLocale(56, ich().getName()));
        ui.actionDone();
    }

    @Override
    public void draw(int amount, String extraMessage) {
        for (int i = 0; i < amount; ++i) {
            f().draw(this);
        }
        if (extraMessage == null) {
            ui.noLogPlayMessage(FTool.parseLocale(283, gi.myName(), String.valueOf(amount)));
        } else {
            ui.playMessage(FTool.parseLocale(283, gi.myName(), String.valueOf(amount)) + extraMessage);
        }
        actionDone();
    }

    public void mulligan() {
        if (gi.getTurn() > 2) {
            return;
        }
        send("$CHAT:" + gi.myName() + ": " + FTool.getLocale(242));
        showHand(" - " + FTool.getLocale(242));
        sendHandBack();
        f().shuffle();
        draw(6, " - " + FTool.getLocale(242));
    }

    public void recvACK() {
        if (System.currentTimeMillis() - mPingTime <= 80000) {
            ui.insertNoLogMessage("Ping: " + String.valueOf(System.currentTimeMillis() - mPingTime) + "ms.", Color.MAGENTA);
        }
    }

    public void loadDeck(File file) {
        try {
            Deck deck = Deck.Editor.load(file);
            ich().setDeck(deck);
            ArrayList<CharacterCard> cs = new ArrayList<CharacterCard>(4);
            CharacterCard leader = CharacterCard.newNull();
            for (int ch : deck.getCharacters()) {
                CharacterCard card = CardDatabase.getCharacterInfo(ch).createCard(EPlayer.ICH, EPlayer.ICH, new NullDistrict());
                cs.add(card);
                if (ch == deck.getLeader()) {
                    leader = card;
                }
            }
            leader.updateLabel(this);
            f().setChars(cs);
            f().setLeader(leader);
            leader.getLabel().setPopupMenu(cs);

            ui.displayName();
            ui.showCharacters(EPlayer.ICH);

            //Shift history
            String[] hist = new String[5];
            hist[0] = FTool.readConfig("lastdeck5");
            hist[1] = FTool.readConfig("lastdeck4");
            hist[2] = FTool.readConfig("lastdeck3");
            hist[3] = FTool.readConfig("lastdeck2");
            hist[4] = FTool.readConfig("lastdeck1");

            String now = ich().getDeck().getDeckName() + "\t" + file.getPath();
            int hit = -1;
            for (int i = 0; i < 5; ++i) {
                if (now.equals(hist[i])) {
                    hit = i;
                    break;
                }
            }
            if (hit > 0) {
                int flag = 0;
                for (int j = 0; j < 4; ++j) {
                    if (j == hit) {
                        flag = 1;
                    }
                    FTool.updateConfig("lastdeck" + String.valueOf(5 - j), hist[j + flag]);
                }
                FTool.updateConfig("lastdeck1", now);
            } else {
                FTool.updateConfig("lastdeck5", hist[1]);
                FTool.updateConfig("lastdeck4", hist[2]);
                FTool.updateConfig("lastdeck3", hist[3]);
                FTool.updateConfig("lastdeck2", hist[4]);
                FTool.updateConfig("lastdeck1", now);
            }

            ui.refreshDeckHistory();

            switch (mDeckLoadingState) {
                case INIT_GAME:
                    of().setLeaderAndChars(EPlayer.OPP, -200, new int[]{-200, -300, -300, -300});
                    ui.showCharacters(EPlayer.OPP);
                    ui.noLogPlayMessage(FTool.parseLocale(287, gi.myName()));
                    mDeckLoadingState = EDeckLoadingState.ICH_LOADED;
                    send("$LOADEDDECK:");
                    break;
                case OPP_LOADED:
                    sendDeck();
                    ui.noLogPlayMessage(FTool.parseLocale(287, gi.myName()));
                    break;
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
            ui.insertNoLogMessage(FTool.getLocale(23), Color.RED);
        }

        if (mMode == EGameMode.DEBUG) {
            newGame();
        }
    }

    private void sendDeck() {
        Deck deck = getGameInformation().getPlayer(EPlayer.ICH).getDeck();
        send("$DECKNAME:" + deck.getDeckName());
        send("$DECKCHAR:" + deck.getCharsStringForSocket());
    }

    public void oppDeckLoaded() {
        of().setLeaderAndChars(EPlayer.OPP, -100, new int[]{-100, -100, -100, -100});
        f().setLeaderAndChars(EPlayer.ICH, -200, new int[]{-200, -300, -300, -300});
        ui.showCharacters(EPlayer.OPP);
        ui.showCharacters(EPlayer.ICH);
        mDeckLoadingState = EDeckLoadingState.OPP_LOADED;
    }

    public void setDeck(EPlayer ep, String[] s) {
        if (mDeckLoadingState == EDeckLoadingState.ICH_LOADED) {
            sendDeck();
        }
        gi.getPlayer(ep).getDeck().setCharacters(s);
        gi.getField(ep).setLeaderAndChars(ep, Integer.parseInt(s[0]),
                new int[]{Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3])});
        ui.showCharacters(ep);
        if (!isWatcher() || ep == EPlayer.ICH) {
            completeLoadingDeck();
        }
    }

    private void completeLoadingDeck() {
        sendAndRefreshCounter();
        if (!isWatcher()) {
            ui.insertNoLogMessage(FTool.parseLocale(288, ich().getName(), ich().getDeck().getDeckName(), ich().getDeck().getCharsStringForDisplay(),
                    opp().getName(), opp().getDeck().getDeckName(), opp().getDeck().getCharsStringForDisplay()), Color.RED);
            generateReplay();
        } else {
            try {
                ui.insertNoLogMessage(FTool.parseLocale(288, ich().getName(), ich().getDeck().getDeckName(), ich().getDeck().getCharsStringForDisplay(),
                        opp().getName(), opp().getDeck().getDeckName(), opp().getDeck().getCharsStringForDisplay()), Color.RED);
            } catch (Exception e) {
                // Emergecy avoiding, FIXME
            }
        }
        mDeckLoadingState = EDeckLoadingState.INIT_GAME;
        newGame();
    }

    public void randomLoadDeck() {
        File root = new File("deck");
        if (!root.exists()) {
            return;
        }
        ArrayList<File> decks = new ArrayList<File>();
        addFiles(root, decks);
        File random_deck = decks.get(new Random().nextInt(decks.size()));
        loadDeck(new File(random_deck.getPath()));
        ui.playMessage(FTool.parseLocale(234, gi.myName()));
    }

    private void addFiles(File dir, ArrayList<File> decks) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory() && !f.getName().equals("recycle")) {
                addFiles(f, decks);
            } else {
                if (f.getName().endsWith("dec")) {
                    decks.add(f);
                }
            }
        }
    }

    public void pass(boolean active) {
        ui.insertNoLogMessage((active ? gi.myName() : opp().getName()) + FTool.getLocale(30) + FTool.getLocale(118), Constants.DEEP_ORANGE);

        if (active) {
            send("$PASS:");
            ui.setTimerTurn(EPlayer.OPP);
        } else {
            ui.setTimerTurn(EPlayer.ICH);
        }

        passed.put(active ? EPlayer.ICH : EPlayer.OPP, true);
        if (passed.get(EPlayer.ICH) && passed.get(EPlayer.OPP)) {
            if (gi.getPhase() == EPhase.REPLENISHING) {
                ReplenishEndBattleBeginManager.exec(this, getGameInformation());
                gi.setPhase(EPhase.BATTLE);
                checkBattlable();
            }
            passed.put(EPlayer.ICH, false);
            passed.put(EPlayer.OPP, false);
        }

    }
    
    @Override
    public void discardHint(int amount, String name) {
        ui.insertMessage(FTool.parseLocale(318, amount) + " - " + name, Constants.DEEP_ORANGE);
    }

    /**
     * Check if can battle.
     */
    private void checkBattlable() {
        if (!gi.isIchAttackPlayer()) {
            return;
        }
        if (f().getActivated().size() == 0 && f().getBattleCard().isNull()) {
            noAttack(true);
        }
    }

    public boolean isBattling() {
        return mBattling;
    }

    public void sendDecideFirstLast() {
        if (isWatcher()) {
            return;
        }
        mFLSelection = JOptionPane.showOptionDialog(null, FTool.getLocale(243), FTool.getLocale(244), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, new String[]{FTool.getLocale(245), FTool.getLocale(246)}, FTool.getLocale(245));
        ui.playMessage(FTool.parseLocale(247, gi.myName()));
        send("$DOORREQUEST:");
    }

    public void openDoor(int selection) {
        if (isWatcher()) {
            return;
        }
        if (selection < 0) {
            ui.insertMessage(FTool.parseLocale(257, gi.myName()), Color.RED);
            return;
        }
        if (selection == mFLSelection) {
            send("$YOULAST:");
            ui.playMessage(FTool.parseLocale(255, gi.myName()));
            if (FTool.readBooleanConfig("firstmp", true)) {
                ich().setSP(1);
            }
            gi.setAttackPlayer(EPlayer.ICH);
        } else {
            send("$YOUFIRST:");
            ui.playMessage(FTool.parseLocale(256, opp().getName()));
            ich().setSP(0);
            gi.setAttackPlayer(EPlayer.OPP);
        }
        sendAndRefreshCounter();
        ui.showTurn(gi);
        mFLSelection = -1;
    }

    public void ping() {
        ui.insertNoLogMessage("Ping sent.", Color.MAGENTA);
        setPingTime();
        if (isWatcher()) {
            send("$WATCHERPING:");
        } else {
            send("$PING:");
        }
    }

    public void setPingTime() {
        mPingTime = System.currentTimeMillis();
    }

    public void backToTitle() {
        mTitleUI.setVisible(true);
        if (mSocketThread != null) {
            mSocketThread.closeSocket();
            mSocketThread = null;
        }
        mTitleUI.clearHostSock();
    }

    public void confirmBattle() {
        if (mBattleSystem == null) {
            ui.playMessage(FTool.getLocale(305));
            return;
        }

        sendBattleSab();

        if (isHost()) {
            if (mClientBattleOK) {
                execBattle(true);
            } else {
                mHostBattleOK = true;
                ui.noLogPlayMessage(gi.myName() + FTool.getLocale(30) + FTool.getLocale(32));
            }
        } else {
            ui.noLogPlayMessage(gi.myName() + FTool.getLocale(30) + FTool.getLocale(32));
            send("$BATTLEOK:");
            ui.setTimerTurn(EPlayer.OPP);
        }
    }

    public void sendChat(String message) {
        if (isWatcher()) {
            send("$CHAT:" + mWatcherName + ": " + message);
            ui.insertMessage(mWatcherName + ": " + message, new Color(120, 60, 0));
        } else {
            send("$CHAT:" + gi.myName() + ": " + message);
            ui.insertMessage(gi.myName() + ": " + message, Color.BLUE);
        }
    }

    public void requestNewGame() {
        ui.playMessage(gi.myName() + FTool.getLocale(75));
        send("$NEWGAME:");
    }

    public void putLibraryTopToBottom() {
        moveCard(f().getLibrary().get(f().getLibrary().size() - 1), EPlayer.ICH, EMoveTarget.LIBRARY_BOTTOM, true, false, "");
    }

    public void putLibraryBottomToTop() {
        moveCard(f().getLibrary().get(0), EPlayer.ICH, EMoveTarget.LIBRARY_TOP, true, false, "");
        actionDone();
    }

    public ArrayList<Watcher> getShareHands() {
        return mShareHands;
    }

    @Override
    public void illegalUse(boolean active) {
        ui.insertMessage(FTool.getLocale(211), Color.RED);
        if (active) {
            send("$ILLEGAL:");
        }
    }

    @Override
    public void showCard(Card card) {
        ui.showCard(card.getInfo());
    }

    @Override
    public void directLoadDeck(File deckFile) {
        loadDeck(deckFile);
    }

    private EPlayer ep(boolean active) {
        return active ? EPlayer.ICH : EPlayer.OPP;
    }

    public void setBattleOK(boolean active, boolean ok) {
        if (isHost()) {
            if (active) {
                mHostBattleOK = ok;
            } else {
                mClientBattleOK = ok;
            }
        } else {
            if (active) {
                mClientBattleOK = ok;
            } else {
                mHostBattleOK = ok;
            }
        }

        if (ok) {
            if (isHost() && mBattling && mHostBattleOK && mClientBattleOK) {
                execBattle(true);
            } else {
                ui.setTimerTurn(EPlayer.ICH);
            }
        }
    }

    public void moveReveal(ERegion region, EMoveTarget to, int index) {
        switch (region) {
            case HAND:
                moveCard(f().getHand().get(index), EPlayer.ICH, to, false, false, FTool.getLocale(306));
                break;
            case LIBRARY:
                moveCard(f().getLibrary().get(f().getLibrary().size() - index - 1), EPlayer.ICH, to, false, false, FTool.getLocale(306));
                break;
        }
    }

    public void recollect(ERegion region, int index) {
        switch (region) {
            case LIBRARY:
                f().getLibrary().remove(f().getLibrary().size() - index - 1);
                actionDone();
                break;
            case DISCARD_PILE:
                f().getDiscardPile().remove(index);
                break;
        }
    }

    @Override
    public void sendRecollectEvent(Card card) {
        for (int index = 0; index < of().getDiscardPile().size(); ++index) {
            Card c = of().getDiscardPile().get(index);
            if (card.getCardNo() == c.getCardNo()) {
                send("$RECOLLECT:" + ERegion.DISCARD_PILE.name() + " " + index);
                break;
            }
        }
    }

    @Override
    public void moveSpellAttachment(SupportCard card, EMoveTarget destination, boolean active) {
        District district;
        switch (destination) {
            case HAND:
                district = f().getHandDistrict();
                break;
            case DISCARD_PILE:
                district = f().getDiscardPileDistrict();
                break;
            case LIBRARY_TOP:
            case LIBRARY_BOTTOM:
                district = f().getLibraryDistrict();
                break;
            default:
                return;
        }

        card.getDistrict().handleMoving(this, card, destination, card.getOwner(), true, false, "");
        SupportMoveTriggerManager.exec(card, this, gi, card.getRegion(), card.getOwner(), destination);

        if (card.getOwner() == EPlayer.ICH) {
            card.setController(EPlayer.ICH);
            card.setDistrict(district);
            card.updateLabel(this);
            district.add(this, card, destination);
            sendAndRefreshCounter();
        } else if (active) {
            send("$SPELLATTACHMOVE:" + card.getCardNo() + " " + destination);
        }
    }

    public void handleSupportBuryAlong(SupportCard card, boolean active) {
        if (card.getOwner() == EPlayer.ICH) {
            ui.playMessage(FTool.parseLocale(293, card.getName()));
            f().getDiscardPileDistrict().add(card);
            card.updateLabel(this);
            sendAndRefreshCounter();
        } else if (active) {
            send("$BURYALONG:" + card.getCardNo());
        }

        SupportMoveTriggerManager.exec(card, this, gi, card.getRegion(), card.getOwner(), EMoveTarget.DISCARD_PILE);
    }

    public static class CurrentStateDecoder {

        public static void exec(GameCore core, String statestring) {
            GameInformation gi = core.getGameInformation();
            IMainUI ui = core.getMainUI();
            CurrentState state = CurrentState.decode(statestring);

            core.mPlayingTimer = new GameTimer(2, ui.getTimerUpdateListener());

            for (EPlayer ep : EPlayer.values()) {
                EPlayer inep = FTool.rev(ep);
                PlayField f = gi.getField(ep);
                Player pl = gi.getPlayer(ep);

                pl.setName(state.getItem("NAME_" + inep).getSingle());

                for (String ls : state.getItem("LS_" + inep).getValues()) {
                    String[] s = ls.split(CurrentStateItem.ownerSeprator);
                    SupportCard card = CardDatabase.getSupportInfo(Integer.parseInt(s[0])).createCard(ep, EPlayer.valueOf(s[1]), f.getLeaderAttachmentDistrict());
                    card.updateLabel(core);
                    f.getLeaderAttachment().add(card);
                }

                ArrayList<CharacterCard> chars = new ArrayList<CharacterCard>();
                for (String s : state.getItem("CHARS_" + inep).getValues()) {
                    chars.add(CardDatabase.getCharacterInfo(Integer.parseInt(s)).createCard(ep, ep, new NullDistrict()));
                }
                f.setChars(chars);

                int leaderNo = Integer.parseInt(state.getItem("LEADER_" + inep).getSingle());
                for (CharacterCard cc : f.getChars()) {
                    if (cc.getCardNo() == leaderNo) {
                        cc.updateLabel(core);
                        f.setLeader(cc);
                    }
                }

                pl.getDeck().setDeckName(state.getItem("DECKNAME_" + inep).getSingle());

                pl.setHP(Integer.parseInt(state.getItem("HP_" + inep).getSingle()));
                pl.setSP(Integer.parseInt(state.getItem("SP_" + inep).getSingle()));

                CurrentState.CurrentStateItem item = state.getItem("HANDSIZE_" + inep);
                for (int k = 0; k < Integer.parseInt(item.getSingle()); ++k) {
                    f.getHand().add(Card.newNull());
                }

                item = state.getItem("LIBSIZE_" + inep);
                for (int k = 0; k < Integer.parseInt(item.getSingle()); ++k) {
                    f.getLibrary().add(Card.newNull());
                }

                for (String gcard : state.getItem("DISCARDPILELIST_" + inep).getValues()) {
                    f.getDiscardPile().add(CardDatabase.getInfo(Integer.parseInt(gcard)).createCard());
                }

                for (String info : state.getItem("RESERVED_" + inep).getValues()) {
                    String[] s = info.split(CurrentStateItem.ownerSeprator);
                    SpellCard card = CardDatabase.getSpellInfo(Integer.parseInt(s[0])).createCard(ep, EPlayer.valueOf(s[1]), f.getReservedDistrict());
                    card.updateLabel(core);
                    f.getReserved().add(card);
                }

                int i = 0;
                for (String info : state.getItem("RESERVEDATTACH_" + inep).getValues()) {
                    String[] s = info.split(CurrentStateItem.ownerSeprator);
                    SupportCard card = CardDatabase.getSupportInfo(Integer.parseInt(s[0])).createCard(ep, EPlayer.valueOf(s[1]), f.getReservedDistrict());
                    card.updateLabel(core);
                    if (!card.isNull()) {
                        ((SpellCard) f.getReserved().get(i)).attach(card);
                    }
                    ++i;
                }

                for (String info : state.getItem("ACTIVATED_" + inep).getValues()) {
                    String[] s = info.split(CurrentStateItem.ownerSeprator);
                    SpellCard card = CardDatabase.getSpellInfo(Integer.parseInt(s[0])).createCard(ep, EPlayer.valueOf(s[1]), f.getActivatedDistrict());
                    card.updateLabel(core);
                    f.getActivated().add(card);
                }

                i = 0;
                for (String info : state.getItem("ACTIVATEDATTACH_" + inep).getValues()) {
                    String[] s = info.split(CurrentStateItem.ownerSeprator);
                    SupportCard card = CardDatabase.getSupportInfo(Integer.parseInt(s[0])).createCard(ep, EPlayer.valueOf(s[1]), f.getActivatedDistrict());
                    card.updateLabel(core);
                    if (!card.isNull()) {
                        ((SpellCard) f.getActivated().get(i)).attach(card);
                    }
                    ++i;
                }

                String[] s = state.getItem("BATTLECARD_" + inep).getSingle().split(CurrentStateItem.ownerSeprator);
                int battlecard = Integer.parseInt(s[0]);
                EPlayer owner = EPlayer.valueOf(s[1]);
                if (battlecard > 0) {
                    SpellCard card = CardDatabase.getSpellInfo(battlecard).createCard(ep, owner, f.getBattleCardDistrict());
                    card.updateLabel(core);
                    f.setBattleCard(card);
                    s = state.getItem("BATTLECARDATTACH_" + inep).getSingle().split(CurrentStateItem.ownerSeprator);
                    int battlecardattach = Integer.parseInt(s[0]);
                    owner = EPlayer.valueOf(s[1]);
                    if (battlecardattach > 0) {
                        SupportCard scard = CardDatabase.getSupportInfo(battlecardattach).createCard(ep, owner, f.getBattleCardDistrict());
                        scard.setLabel(scard.updateLabel(core));
                        card.attach(scard);
                    }
                }

                int event = FTool.safeParseInt(state.getItem("EVENT_" + inep).getSingle());
                if (event > 0) {
                    EventCard card = CardDatabase.getEventInfo(event).createCard(ep, ep, new EventDistrict());
                    card.updateLabel(core);
                    f.setEventCard(card);
                } else {
                    ui.removeEventLabel(EventCard.newNull());
                }

                if (core.mPlayingTimer != null) {
                    int timer = FTool.safeParseInt(state.getItem("TIMER_" + inep).getSingle());
                    core.mPlayingTimer.setElpisedTime(ep.ordinal(), timer);
                }

            }

            String[] s = state.getItem("SCENE").getSingle().split(CurrentStateItem.ownerSeprator);
            int scene = FTool.safeParseInt(s[0]);
            if (scene > 0) {
                SupportCard sceneCard = CardDatabase.getSupportInfo(scene).createCard(EPlayer.ICH, EPlayer.valueOf(s[1]), gi.getSceneDistrict());
                sceneCard.setLabel(sceneCard.updateLabel(core));
                gi.setScene(sceneCard);
            } else {
                ui.removeSceneLabel(SupportCard.newNull());
            }

            gi.setTurn(FTool.safeParseInt(state.getItem("TURN").getSingle()));
            gi.setPhase(EPhase.valueOf(state.getItem("PHASE").getSingle()));
            gi.setAttackPlayer(!Boolean.parseBoolean(state.getItem("HOSTTURN").getSingle()) ? EPlayer.ICH : EPlayer.OPP);

            if (Boolean.parseBoolean(state.getItem("HOSTTIMERRUNNING").getSingle())) {
                ui.setTimerTurn(EPlayer.OPP);
            } else {
                ui.setTimerTurn(EPlayer.ICH);
            }

            try {
                for (String watcher : state.getItem("WATCHER").getValues()) {
                    core.addnewWatcher(Watcher.makeFromString(watcher));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }

            // Update UI
            ui.displayName();
            ui.showTurn(gi);
            ui.refreshCounter();
            ui.updateField();
        }
    }

    public static String MSecToMMMSS(int milisec) {
        String sign = milisec > -1000 ? "" : "-";
        milisec = Math.abs(milisec);
        int sec = milisec / 1000;
        int min = sec / 60;

        sec %= 60;

        String ssec = (sec < 10 ? "0" : "") + String.valueOf(sec);
        String smin = (min < 10 ? "0" : "") + String.valueOf(min);
        return sign + smin + "'" + ssec + "\"";
    }
}
