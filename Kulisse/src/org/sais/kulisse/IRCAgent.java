/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.kulisse;

import java.awt.Color;
import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 *
 * @author Romulus
 */
public class IRCAgent extends PircBot {

    private IRCWatchFrame watchframe;
    private IRCManager manager;
    private String channel;
    public String server = null;
    public int port = 6667;

    public IRCAgent() {
    }

    public IRCAgent(IRCWatchFrame parent, IRCManager manager, String channel) {
        this.watchframe = parent;
        this.channel = channel;
        this.manager = manager;
        if (Tools.readConfig("ircnick") == null || Tools.readConfig("ircnick").equals("")) {
            Tools.updateConfig("ircnick", "Kulisse");
        }
        this.setName(Tools.readConfig("ircnick") + "_" + IRCWatchFrame.VERSION);
    }

    public void setNick(String nick) {
        this.setName(nick);
    }

    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action) {
        watchframe.showMessage("<" + sender + "> act: " + action);
    }

    @Override
    protected void onChannelInfo(String channel, int userCount, String topic) {
        watchframe.setTitle("Fantasy Festa IRC: " + topic);
    }

    @Override
    protected void onConnect() {
        watchframe.showMessage("Connected to server.", Color.RED);
    }

    @Override
    protected void onDisconnect() {
        watchframe.showMessage("Disconnected from server.", Color.RED);
    }

    @Override
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        User[] users = this.getUsers(channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        watchframe.setListModel(mod);
        watchframe.showMessage("ChangeNick: " + oldNick + " -> " + newNick, Color.MAGENTA);
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        User[] users = this.getUsers(this.channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        watchframe.setListModel(mod);
        watchframe.showMessage("Login: " + sender + "(" + login + ")", Color.MAGENTA);
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        User[] users = this.getUsers(channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        watchframe.setListModel(mod);
        watchframe.showMessage("Quit: " + sourceNick + sourceLogin + "(" + sourceHostname + ")" + " " + reason, Color.MAGENTA);
    }

    @Override
    protected void onPart(String channel, String sender, String login, String hostname) {
        User[] users = this.getUsers(this.channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        watchframe.setListModel(mod);
        watchframe.showMessage("Part: " + sender + login + "(" + hostname + ")", Color.MAGENTA);
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        Tools.updateConfig("ircserver", server);
        Tools.updateConfig("ircport", String.valueOf(port));
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        watchframe.setListModel(mod);
        try {
            SimpleAttributeSet set = new SimpleAttributeSet();
            JTextPane _messages = watchframe.getMessagePane();
            _messages.setCharacterAttributes(set, true);
            Document doc = _messages.getStyledDocument();
            if (_messages.getDocument().getLength() > 0) {
                _messages.setCaretPosition(_messages.getDocument().getLength() - 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        manager.onMessage(channel, sender, login, hostname, message, false);
    }

    @Override
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
        manager.onMessage(target, sourceNick, sourceLogin, sourceHostname, notice, true);
    }

    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        if (changed) {
            watchframe.showMessage(setBy + " changed topic to " + topic, Color.BLUE);
        }
        watchframe.setTitle("Fantasy Festa IRC: " + topic);
    }

    @Override
    protected void onServerResponse(int code, String response) {
        watchframe.showMessage(response, Color.MAGENTA);
    }

    @Override
    protected void onUnknown(String line) {
        watchframe.showMessage(line, Color.RED);
    }

    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        watchframe.showMessage("PM from " + sender + ": " + message, Color.GREEN);
    }

    @Override
    protected void onIncomingFileTransfer(DccFileTransfer transfer) {
        File file = transfer.getFile();
        if (!new File("DCCFiles").exists()) {
            new File("DCCFiles").mkdir();
        }
        if (Tools.readConfig("autodcc").equals("true")) {
            file = new File(".\\DCCFiles\\" + file.getName());
            watchframe.showMessage("Recieving " + file.getName() + "...", Color.MAGENTA);
            transfer.receive(file, true);
        } else {
            if (JOptionPane.showConfirmDialog(null, transfer.getNick() + " is trying to send " + transfer.getFile().getName() + "(" + transfer.getSize() + " bytes) to you. Accept?", "DCC File Transfer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                file = new File(".\\DCCFiles\\" + file.getName());
                watchframe.showMessage("Recieving " + file.getName() + "...", Color.MAGENTA);
                transfer.receive(file, true);
            }
        }
    }

    @Override
    protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
        if (transfer.isIncoming()) {
            watchframe.showMessage(transfer.getFile().getName() + " recieved.", Color.MAGENTA);
        }
        if (transfer.isOutgoing()) {
            watchframe.showMessage(transfer.getFile().getName() + " sent.", Color.MAGENTA);
        }
    }
}
