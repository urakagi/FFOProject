package org.sais.fantasyfesta.core;

import org.sais.fantasyfesta.net.socketthread.SocketThread;
import java.awt.Color;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.enums.ECostType;
import org.sais.fantasyfesta.enums.EMoveTarget;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

abstract public class MessageDispatcher {

    protected SocketThread mParentSocketThread;

    /*
    TODO:
     * Changed 0/1 player to ICH/OPP. Remeber to change all the parsers.
     */
    public MessageDispatcher(SocketThread parentSock) {
        this.mParentSocketThread = parentSock;
    }

    abstract public void dispatch(String ins);

    public void dispathCommon(String ins, boolean passive) {
        EPlayer ep = passive ? EPlayer.OPP : EPlayer.ICH;
        String cmd;
        GameCore core = mParentSocketThread.getCore();
        GameInformation gi = core.getGameInformation();
        IMainUI ui = core.getMainUI();

        cmd = "$DECKNAME:";
        if (ins.startsWith(cmd)) {
            gi.getPlayer(ep).getDeck().setDeckName(ins.substring(cmd.length()));
            ui.displayName();
            return;
        }
        cmd = "$DECKCHAR:";
        if (ins.startsWith(cmd)) {
            String[] s = ins.substring(cmd.length()).split(" ");
            core.setDeck(ep, s);
            return;
        }
        cmd = "$LOADEDDECK:";
        if (ins.startsWith(cmd)) {
            if (passive) {
                core.oppDeckLoaded();
            }
            return;
        }
        cmd = "$CHANGELEADER:";
        if (ins.startsWith(cmd)) {
            core.changeLeader(!passive, Integer.parseInt(ins.substring(cmd.length())));
        }
        cmd = "$GENERATEREPLAY:";
        if (ins.startsWith(cmd)) {
            core.generateReplay();
            return;
        }
        cmd = "$PLAYERNAME:";
        if (ins.startsWith(cmd)) {
            gi.getPlayer(ep).setName(ins.substring(cmd.length()));
            ui.displayName();
            return;
        }
        cmd = "$CHAT:";
        if (ins.startsWith(cmd)) {
            ui.insertMessage(ins.substring(cmd.length()), passive ? new Color(0x008000) : Color.BLUE);
            return;
        }
        cmd = "$WATCHCHAT:";
        if (ins.startsWith(cmd)) {
            ui.insertMessage(ins.substring(cmd.length()), new Color(120, 60, 0));
            return;
        }
        cmd = "$MSG:";
        if (ins.startsWith(cmd)) {
            ui.insertMessage(ins.substring(cmd.length()), Color.BLACK);
            return;
        }
        cmd = "$NOLOGMSG:";
        if (ins.startsWith(cmd)) {
            ui.insertNoLogMessage(ins.substring(cmd.length()), Color.BLACK);
            return;
        }
        cmd = "$REPLAY:";
        if (ins.startsWith(cmd)) {
            core.writeReplay(ins.substring(cmd.length()));
            return;
        }
        cmd = "$REPLAYWITHNEWLINE:";
        if (ins.startsWith(cmd)) {
            core.writeReplay("\r\n" + ins.substring(cmd.length()));
            return;
        }
        cmd = "$YOUFIRST:";
        if (ins.startsWith(cmd)) {
            gi.setAttackPlayer(EPlayer.ICH);
            if (FTool.readConfig("firstmp").equals("false")) {
                core.setSP(0, "");
            } else {
                core.setSP(1, "");
            }
            ui.showTurn(gi);
            return;
        }
        cmd = "$YOULAST:";
        if (ins.startsWith(cmd)) {
            gi.setAttackPlayer(EPlayer.OPP);
            core.setSP(0, "");
            ui.showTurn(gi);
            return;
        }
        cmd = "$MOVE:";
        if (ins.startsWith(cmd)) {
            String[] s = ins.substring(cmd.length()).split(" ");
            handleCardMoving(s, passive);
            return;
        }
        cmd = "$ATTACH:";
        if (ins.startsWith(cmd)) {
            String[] s = ins.substring(cmd.length()).split(" ");
            int supNo = Integer.parseInt(s[0]);
            EPlayer controller = passive ? FTool.rev(EPlayer.valueOf(s[1])) : EPlayer.valueOf(s[1]);
            ERegion region = ERegion.valueOf(s[2]);
            int index = Integer.parseInt(s[3]);
            SupportCard sup = CardDatabase.getSupportInfo(supNo).createCard(controller, owner(passive), new NullDistrict());
            SpellCard target = (SpellCard) gi.getField(controller).getDistrcit(region).getSet().get(index);
            core.attach(sup, target.getLabel(), !passive);
            return;
        }
        cmd = "$UNATTACH:";
        if (ins.startsWith(cmd)) {
            String[] s = ins.substring(cmd.length()).split(" ");
            EPlayer controller = passive ? FTool.rev(EPlayer.valueOf(s[0])) : EPlayer.valueOf(s[0]);
            ERegion region = ERegion.valueOf(s[1]);
            int index = Integer.parseInt(s[2]);
            SpellCard target = (SpellCard) gi.getField(controller).getDistrcit(region).getSet().get(index);
            core.unAttach(target, !passive);
            return;
        }
        cmd = "$SPELLATTACHMOVE:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            SupportCard card = CardDatabase.getSupportInfo(Integer.parseInt(s[0])).createCard(EPlayer.ICH, EPlayer.ICH, new NullDistrict());
            core.moveSpellAttachment(card, EMoveTarget.valueOf(s[1]), !passive);
        }
        cmd = "$BURYALONG:";
        if (ins.startsWith(cmd)) {
            int no = Integer.parseInt(extract(ins, cmd));
            SupportCard card = CardDatabase.getSupportInfo(no).createCard(EPlayer.ICH, EPlayer.ICH, new NullDistrict());
            core.handleSupportBuryAlong(card, !passive);
        }
        cmd = "$COUNTER:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            gi.getPlayer(ep).setHP(FTool.safeParseInt(s[0]));
            gi.getPlayer(ep).setSP(FTool.safeParseInt(s[1]));

            int handDelta = Integer.parseInt(s[2]) - gi.getField(ep).getHand().size();
            int libDelta = Integer.parseInt(s[3]) - gi.getField(ep).getLibrary().size();
            if (handDelta > 0) {
                for (int k = 0; k < handDelta; ++k) {
                    gi.getField(ep).getHand().add(Card.newNull());
                }
            } else {
                for (int k = 0; k > handDelta; --k) {
                    gi.getField(ep).getHand().remove(0);
                }
            }
            if (libDelta > 0) {
                for (int k = 0; k < libDelta; ++k) {
                    gi.getField(ep).getLibrary().add(Card.newNull());
                }
            } else {
                for (int k = 0; k > libDelta; --k) {
                    gi.getField(ep).getLibrary().remove(0);
                }
            }
            gi.getField(ep).getDiscardPile().clear();
            for (int k = 0; k < FTool.safeParseInt(s[4]); ++k) {
                Card card = CardDatabase.getInfo(Integer.parseInt(s[5 + k])).createCard(ep, ep, gi.getField(ep).getDiscardPileDistrict());
                gi.getField(ep).getDiscardPile().add(card);
                card.updateLabel(core);
            }
            ui.refreshCounter();
            return;
        }
        cmd = "$BATTLESAB:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(GameCore.BATTLESAB_SEP);
            // Client's without BattleValues extra
            if (passive || core.isWatcher()) {
                core.changeBattleValues(s, !passive);
            }
            core.setBattleOK(!passive, !passive);
            return;
        }
        cmd = "$BATTLERESULT:";
        if (ins.startsWith(cmd)) {
            core.execBattleResult(extract(ins, cmd));
            return;
        }
        cmd = "$NEXT:";
        if (ins.startsWith(cmd)) {
            core.nextTurn(!passive);
            return;
        }
        cmd = "$RANDOMDISCARD:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.randomDiscard(" - " + s[0]);
            return;
        }
        cmd = "$DECKOUT:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            for (int i = 0; i < FTool.safeParseInt(s[0]); ++i) {
                core.deckout(" - " + s[1]);
            }
            return;
        }
        cmd = "$DRAW:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.draw(FTool.safeParseInt(s[0]), " - " + s[1]);
            return;
        }
        cmd = "$SOUND:";
        if (ins.startsWith(cmd)) {
            FTool.playSound(extract(ins, cmd));
            return;
        }
        cmd = "$DISCONNECT:";
        if (ins.startsWith(cmd)) {
            core.getMainUI().insertMessage(FTool.getLocale(88), Color.RED);
            mParentSocketThread.closeSocket();
            return;
        }
        cmd = "$REVEAL:";
        if (ins.startsWith(cmd)) {
            ui.setRevealCards(extract(ins, cmd));
            return;
        }
        cmd = "$SHOWHAND:";
        if (ins.startsWith(cmd)) {
            String s = extract(ins, cmd);
            core.showHand(" - " + s);
            return;
        }
        cmd = "$SHOWMELIBTOPS:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.revealLibTops(FTool.safeParseInt(s[0]), " - " + s[1]);
            return;
        }
        cmd = "$PING:";
        if (ins.startsWith(cmd)) {
            if (!core.isWatcher()) {
                send(mParentSocketThread.getSocket(), "$ACK:");
            }
            return;
        }
        cmd = "$ACK:";
        if (ins.startsWith(cmd)) {
            core.recvACK();
            return;
        }
        cmd = "$MOVEREVEAL:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.moveReveal(ERegion.valueOf(s[0]), EMoveTarget.valueOf(s[1]), Integer.parseInt(s[2]));
            return;
        }
        cmd = "$RECOLLECT:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.recollect(ERegion.valueOf(s[0]), Integer.parseInt(s[1]));
            return;
        }
        cmd = "$VERSION:";
        if (ins.startsWith(cmd)) {
            if (!Startup.VERSION.equals(ins.substring(9))) {
                String mes = FTool.parseLocale(232, Startup.VERSION, ins.substring(cmd.length()));
                ui.insertMessage(FTool.getLocale(127) + mes, Color.RED);
                send(mParentSocketThread.getSocket(), "$WRONGVERSION:" + mes);
            }
            return;
        }
        cmd = "$WRONGVERSION:";
        if (ins.startsWith(cmd)) {
            if (mParentSocketThread.getCore() != null) {
                ui.insertMessage(FTool.getLocale(127) + extract(ins, cmd), Color.RED);
            }
            return;
        }
        cmd = "$ILLEGAL:";
        if (ins.startsWith(cmd)) {
            core.illegalUse(!passive);
            return;
        }
        cmd = "$DELAYEVENT:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            Card card = CardDatabase.getInfo(Integer.parseInt(s[0])).createCard(ep, owner(passive), new NullDistrict());
            core.pushDelayEvent(card, Integer.parseInt(s[1]));
            return;
        }
        cmd = "$DOORREQUEST:";
        if (ins.startsWith(cmd)) {
            if (!core.isWatcher()) {
                int selection = JOptionPane.showOptionDialog(null, FTool.getLocale(249), FTool.getLocale(248), JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new String[]{FTool.getLocale(250), FTool.getLocale(251)}, FTool.getLocale(250));
                ui.insertMessage(FTool.parseLocale(252, gi.myName(), FTool.getLocale(253 + selection)), Color.BLACK);
                send(mParentSocketThread.getSocket(), "$DOORSELECT:" + selection);
            }
            return;
        }
        cmd = "$PASS:";
        if (ins.startsWith(cmd)) {
            core.pass(!passive);
            return;
        }
        cmd = "$DOORSELECT:";
        if (ins.startsWith(cmd)) {
            if (!core.isWatcher()) {
                int selection = Integer.parseInt(ins.substring(cmd.length()));
                core.openDoor(selection);
            }
        }

        // Messages which a watcher should not handle
        if (!core.isWatcher()) {
            dispatchNonWatcher(ins, passive);
        }
    }

    private void dispatchNonWatcher(String ins, boolean passive) {
        String cmd;
        EPlayer ep = passive ? EPlayer.OPP : EPlayer.ICH;
        GameCore core = mParentSocketThread.getCore();
        GameInformation gi = core.getGameInformation();
        IMainUI ui = core.getMainUI();

        cmd = "$NEWGAME:";
        if (ins.startsWith(cmd)) {
            if (JOptionPane.showConfirmDialog(null, FTool.getLocale(83), "New Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                core.send("$NEWGAMEOK:");
                ui.noLogPlayMessage(FTool.getLocale(84));
                core.mNewgameWithoutLoadingDeck = true;
                core.newGame();
            } else {
                ui.noLogPlayMessage(gi.myName() + FTool.getLocale(85));
            }
            return;
        }
        cmd = "$NEWGAMEOK:";
        if (ins.startsWith(cmd)) {
            core.newGame();
            core.mNewgameWithoutLoadingDeck = true;
            return;
        }
        cmd = "$INVOKEBATTLE:";
        if (ins.startsWith(cmd)) {
            core.startBattle();
            return;
        }
        cmd = "$NOATTACK:";
        if (ins.startsWith(cmd)) {
            core.noAttack(!passive);
            return;
        }
        cmd = "$BATTLEGO:";
        if (ins.startsWith(cmd)) {
            core.execBattle(!passive);
            return;
        }
        cmd = "$BATTLEOK:";
        if (ins.startsWith(cmd)) {
            core.setBattleOK(!passive, true);
            return;
        }
        cmd = "$ABILITY:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            int cardNo = Integer.parseInt(s[0]);
            int index = Integer.parseInt(s[1]);
            Card card = CardDatabase.getInfo(cardNo).createCard(ep, owner(passive), new NullDistrict());
            core.useAbility(card, index, ECostType.MISC, 0, !passive);
            return;
        }
        cmd = "$CHOICE:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            int cardNo = Integer.parseInt(s[0]);
            int index = Integer.parseInt(s[1]);
            Card card = CardDatabase.getInfo(cardNo).createCard(ep, owner(passive), new NullDistrict());
            core.invokeChoice(!passive, card, index);
            return;
        }
        cmd = "$ADJUSTHP:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.adjustHP(FTool.safeParseInt(s[0]), !passive, true, " - " + s[1]);
            return;
        }
        cmd = "$HPCOST:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.adjustHP(-FTool.safeParseInt(s[0]), !passive, false, " - " + s[1]);
            return;
        }
        cmd = "$ADJUSTSP:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.adjustSP(FTool.safeParseInt(s[0]), !passive, " - " + s[1]);
            return;
        }
        cmd = "$SELECTDISCARD:";
        if (ins.startsWith(cmd)) {
            String[] s = extract(ins, cmd).split(" ");
            core.discardHint(Integer.parseInt(s[0]), s[1]);
            return;
        }
    }

    public boolean send(Socket soc, String message) { //return false when error
        try {
            if (!soc.isConnected()) {
                soc = null;
                return false;
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(), "Unicode"));
            out.write(message);
            out.newLine();
            out.flush();
            return true;
        } catch (SocketException se) {
            se.printStackTrace();
            try {
                showErrorMessage(se.getCause().toString());
                soc.close();
                soc = null;
            } catch (IOException ex) {
                showErrorMessage(ex.toString());
                ex.printStackTrace();
            }
            return false;
        } catch (Exception ex) {
            showErrorMessage(ex.toString());
            ex.printStackTrace();
            return false;
        }
    }

    protected void showErrorMessage(String message) {
        if (core().getMainUI() != null) {
            core().getMainUI().insertMessage(message, Color.RED);
        }
    }

    protected static String extract(String ins, String cmd) {
        return ins.substring(cmd.length());
    }

    protected EPlayer owner(boolean passive) {
        return passive ? EPlayer.OPP : EPlayer.ICH;
    }

    protected EPlayer rev(EPlayer ep) {
        return ep == EPlayer.ICH ? EPlayer.OPP : EPlayer.ICH;
    }

    protected GameCore core() {
        return mParentSocketThread.getCore();
    }

    protected GameInformation gi() {
        return mParentSocketThread.getCore().getGameInformation();
    }

    protected void handleCardMoving(String[] s, boolean passive) {
        int cardNo = Integer.parseInt(s[0]);
        EPlayer orgController = passive ? FTool.rev(EPlayer.valueOf(s[1])) : EPlayer.valueOf(s[1]);
        ERegion from = ERegion.valueOf(s[2]);
        int index = Integer.parseInt(s[3]);
        EPlayer newController = passive ? FTool.rev(EPlayer.valueOf(s[4])) : EPlayer.valueOf(s[4]);
        EMoveTarget to = EMoveTarget.valueOf(s[5]);

        Card card;
        switch (from) {
            case HAND:
            case DISCARD_PILE:
            case LIBRARY:
                card = CardDatabase.getInfo(cardNo).createCard(newController, orgController, 
                        gi().getField(orgController).getDistrcit(from));
                break;
            case EVENT:
            case SCENE:
                card = (Card) gi().getField(orgController).getDistrcit(from).getCard();
                break;
            case ACTIVATED:
            case RESERVED:
            case LEADER_ATTACHMENTS:
            default:
                card = (Card) gi().getField(orgController).getDistrcit(from).getSet().get(index);
                break;
            case BATTLE:
                card = gi().getField(orgController).getBattleCard();
                break;
            case REVEAL:
                card = CardDatabase.getInfo(cardNo).createCard(newController, orgController, 
                        new NullDistrict());
                break;
        }

        core().moveCard(card, newController, to, !passive, true, "");
    }
}
