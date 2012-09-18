///*
 /* simpleIRC.java
 *
 * Created on 2007/09/17, 21:56
 */
package org.sais.fantasyfesta.net;

import org.sais.fantasyfesta.core.*;
import org.sais.fantasyfesta.tool.FTool;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.io.*;
import java.net.ConnectException;
import javax.swing.*;
import javax.swing.text.*;
import org.jibble.pircbot.*;

/**
 *
 * @author  Romulus
 */
public class FFIRC extends javax.swing.JFrame implements ServerSelectDialog.ServerSelectDialogCallback {

    String default_channel;
    IRCEngine engine;
    Startup parent;
    int mMode;
    int mLocation;
    public static final int LANGUAGE_JAPANESE = 0;
    public static final int LANGUAGE_CHINESE = 1;
    public static final int MODE_DUEL = 2;

    /** Creates new form simpleIRC */
    public FFIRC(Startup parent, int mode) {
        this.setIconImage(new ImageIcon(new ReadInnerFile("ffirc.jpg").u).getImage());
        this.parent = parent;
        mMode = mode;
        initComponents();

        this.setVisible(true);
        this.setLocation(50, 50);

        if (!(FTool.readConfig("ircserver").equals("")) && !(FTool.readConfig("ircport").equals(""))) {
            connect(FTool.readConfig("ircserver"), Integer.parseInt(FTool.readConfig("ircport")));
        }

    }

    private class connectThread extends Thread {

        private String mServer;
        private int mPort;

        public connectThread(String server, int port) {
            mServer = server;
            mPort = port;
        }

        @Override
        public void run() {
            connectExec(mServer, mPort);
        }

    }

    private void connect(String server, int port) {
        showMessage("Connecting to " + server + ":" + port + "...");
        new connectThread(server, port).start();
    }

    private void connectExec(String server, int port) {
        String encoding;
        switch (mMode) {
            case LANGUAGE_JAPANESE:
                default_channel = FTool.getLocale(108);
                encoding = "ISO-2022-JP";
                break;
            case LANGUAGE_CHINESE:
                default_channel = "#FFO";
                encoding = "UTF-8";
                Font meliu = _font_meiliu.getFont();
                _messages.setFont(meliu);
                _inputMessage.setFont(meliu);
                _topic.setFont(meliu);
                break;
            case MODE_DUEL:
                default_channel = "#対戦ノ宴";
                encoding = "ISO-2022-JP";
                break;
            default:
                default_channel = FTool.getLocale(108);
                encoding = "ISO-2022-JP";
        }

        engine = new IRCEngine(this, default_channel);
        engine.setVerbose(false);
        if (FTool.readConfig("ircnick").equals("")) {
            FTool.updateConfig("ircnick", "FFnoname" + String.valueOf((int) (Math.random() * 1000)));
        }
        _nick.setText(FTool.readConfig("ircnick"));

        try {
            engine.setEncoding(encoding);
            engine.server = server;
            engine.port = port;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        while (true) {
            try {
                engine.connect(engine.server, engine.port);
                break;
            } catch (ConnectException ex) {
                JOptionPane.showMessageDialog(this, "Cannot connect to server.");
                ex.printStackTrace();
                break;
            } catch (NickAlreadyInUseException ex) {
                _nick.setText(_nick.getText() + String.valueOf((int) (100 * Math.random())));
                engine.setNick(_nick.getText());
            } catch (IrcException ex) {
                try {
                    SimpleAttributeSet set = new SimpleAttributeSet();
                    _messages.setCharacterAttributes(set, true);
                    Document doc = _messages.getStyledDocument();
                    doc.insertString(doc.getLength(), FTool.getNowTimeString("mm:ss") + " " + ex.getMessage(), set);
                    if (_messages.getDocument().getLength() > 0) {
                        _messages.setCaretPosition(_messages.getDocument().getLength() - 1);
                    }
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    return;
                }
                JOptionPane.showMessageDialog(this, "IrcException: " + ex.getMessage());
                ex.printStackTrace();
                break;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "IOException: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }

        engine.joinChannel(default_channel);
        _inputMessage.requestFocus();
    }

    public void setListModel(DefaultListModel mod) {
        _playerlist.setModel(mod);
    }

    JTextPane getMessagePane() {
        return _messages;
    }

    void sendInputedMessage(boolean isNotice) {
        if (_inputMessage.getText().equals("")) {
            this.setState(Frame.ICONIFIED);
            return;
        }
        String message = _inputMessage.getText();
        message = message.replace((char) 65374, '~');

        if (isNotice) {
            engine.sendNotice(default_channel, message);
            showMessage("<" + engine.getNick() + "> " + message, Color.RED);
            _inputMessage.setText("");
            _inputMessage.requestFocus();
        } else {
            engine.sendMessage(default_channel, message);
            showMessage("<" + engine.getNick() + "> " + message);
            _inputMessage.setText("");
            _inputMessage.requestFocus();
        }
    }

    void showMessage(String message, Color color) {
        try {
            SimpleAttributeSet set = new SimpleAttributeSet();
            _messages.setCharacterAttributes(set, true);
            Document doc = _messages.getStyledDocument();
            if (_messages.getDocument().getLength() > 1024 * 1024 * 1) {
                doc.remove(0, 1024);
            }

            StyleConstants.setForeground(set, color);
            doc.insertString(doc.getLength(), FTool.getNowTimeString("HH:mm") + " " + message + "\n", set);
            if (_messages.getDocument().getLength() > 0) {
                _messages.setCaretPosition(_messages.getDocument().getLength() - 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    void showMessage(String message) {
        showMessage(message, Color.BLACK);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    _inputMessage = new javax.swing.JTextField();
    _send = new javax.swing.JButton();
    _nick = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    _changenick = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    _messages = new javax.swing.JTextPane();
    jScrollPane2 = new javax.swing.JScrollPane();
    _playerlist = new javax.swing.JList();
    _exit = new javax.swing.JButton();
    _notice = new javax.swing.JButton();
    _topic = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    _settopic = new javax.swing.JButton();
    _sendDCCFile = new javax.swing.JButton();
    _hidemainwindow = new javax.swing.JButton();
    _enabletopic = new javax.swing.JCheckBox();
    _font_meiliu = new javax.swing.JLabel();
    _connect = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Fantasy Festa IRC");
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    _inputMessage.setFont(new java.awt.Font("MS PGothic", 0, 14));
    _inputMessage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    _inputMessage.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _inputMessageActionPerformed(evt);
      }
    });

    _send.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _send.setText("Send");
    _send.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _sendActionPerformed(evt);
      }
    });

    _nick.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));

    jLabel1.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    jLabel1.setText("Nick");

    _changenick.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _changenick.setText("Change");
    _changenick.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _changenickActionPerformed(evt);
      }
    });

    jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    _messages.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    _messages.setEditable(false);
    _messages.setFont(new java.awt.Font("MS PGothic", 0, 14));
    jScrollPane1.setViewportView(_messages);

    _playerlist.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    _playerlist.setFont(new java.awt.Font("MS PGothic", 0, 14));
    _playerlist.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Stand by." };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane2.setViewportView(_playerlist);

    _exit.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _exit.setText("EXIT");
    _exit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _exitActionPerformed(evt);
      }
    });

    _notice.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _notice.setText("Notice");
    _notice.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _noticeActionPerformed(evt);
      }
    });

    _topic.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _topic.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _topicActionPerformed(evt);
      }
    });

    jLabel2.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    jLabel2.setText("Topic");

    _settopic.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _settopic.setText("Set");
    _settopic.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _settopicActionPerformed(evt);
      }
    });

    _sendDCCFile.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _sendDCCFile.setText("Send File (DCC)");
    _sendDCCFile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _sendDCCFileActionPerformed(evt);
      }
    });

    _hidemainwindow.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
    _hidemainwindow.setText("Hide Main Window");
    _hidemainwindow.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _hidemainwindowActionPerformed(evt);
      }
    });

    _enabletopic.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        _enabletopicItemStateChanged(evt);
      }
    });

    _font_meiliu.setFont(new java.awt.Font("MingLiU", 0, 12));

    _connect.setFont(new java.awt.Font("MingLiU", 0, 12));
    _connect.setText("選擇伺服器／サーバーを選ぶ／SELECT SERVER");
    _connect.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _connectActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 412, Short.MAX_VALUE)
            .addComponent(_hidemainwindow)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(_exit))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(_notice)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(_inputMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_send)))
                .addGap(8, 8, 8))
              .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(_enabletopic, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_topic, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(8, 8, 8)
                    .addComponent(_settopic))
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(_nick, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(_changenick)
                    .addGap(12, 12, 12)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE))
              .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(_sendDCCFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)))
          .addComponent(_connect))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(_font_meiliu))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(414, 414, 414)
            .addComponent(_font_meiliu))
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(_inputMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(_send)))
              .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(_nick, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1)
                .addComponent(_changenick))
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(_notice)
                .addComponent(_sendDCCFile)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel2)
                .addComponent(_settopic)
                .addComponent(_topic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(_enabletopic))
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(_exit)
                .addComponent(_hidemainwindow)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(_connect)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void _sendDCCFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__sendDCCFileActionPerformed
// TODO add your handling code here:
      if (_playerlist.getSelectedIndex() == -1) {
          JOptionPane.showMessageDialog(null, "Please select target using NameList.");
          return;
      }
      String target = (String) (_playerlist.getModel().getElementAt(_playerlist.getSelectedIndex()));
      if (target.equals("") || target == null) {
          return;
      }
      JFileChooser chooser = new JFileChooser();
      if (FTool.readConfig("dccdir") != null && !FTool.readConfig("dccdir").equals("")) {
          chooser.setCurrentDirectory(new File(FTool.readConfig("dccdir")));
      } else {
          chooser.setCurrentDirectory(new File("."));
      }
      chooser.setAcceptAllFileFilterUsed(true);
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.CANCEL_OPTION) {
          return;
      }
      File file = chooser.getSelectedFile();
      FTool.updateConfig("dccdir", file.getParentFile().getPath());
      DccFileTransfer transfer = engine.dccSendFile(file, target, 120000);
      showMessage("Sending " + file.getName() + "...", Color.MAGENTA);
  }//GEN-LAST:event__sendDCCFileActionPerformed

  private void _settopicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__settopicActionPerformed
      if (_topic.getText().length() < 3) {
          return;
      }
      if (!_enabletopic.isSelected()) {
          return;
      }
      engine.setTopic(default_channel, _topic.getText());
      _topic.setText("");
      _inputMessage.requestFocus();
  }//GEN-LAST:event__settopicActionPerformed

  private void _topicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__topicActionPerformed
      if (_topic.getText().length() < 3) {
          return;
      }
      engine.setTopic(default_channel, _topic.getText());
      _topic.setText("");
      _inputMessage.requestFocus();
  }//GEN-LAST:event__topicActionPerformed

  private void _noticeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__noticeActionPerformed
      sendInputedMessage(true);
  }//GEN-LAST:event__noticeActionPerformed

  private void _exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__exitActionPerformed
      if (engine != null) {
          engine.disconnect();
          engine.dispose();
      }
      if (!parent.isVisible()) {
          System.exit(0);
      }
      this.setVisible(false);
      this.dispose();
  }//GEN-LAST:event__exitActionPerformed

  private void _changenickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__changenickActionPerformed
      engine.changeNick(_nick.getText());
      FTool.updateConfig("ircnick", _nick.getText());
  }//GEN-LAST:event__changenickActionPerformed

  private void _sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__sendActionPerformed
      sendInputedMessage(false);
  }//GEN-LAST:event__sendActionPerformed

  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      if (engine != null) {
          engine.disconnect();
          engine.dispose();
      }

      if (!parent.isVisible()) {
          System.exit(0);
      }
  }//GEN-LAST:event_formWindowClosing

  private void _inputMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__inputMessageActionPerformed
      sendInputedMessage(false);
  }//GEN-LAST:event__inputMessageActionPerformed

  private void _hidemainwindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__hidemainwindowActionPerformed
      // TODO add your handling code here:
      if (_hidemainwindow.getText().substring(0, 4).equals("Hide")) {
          parent.setVisible(false);
          _hidemainwindow.setText("Show Main Window");
      } else {
          parent.setVisible(true);
          _hidemainwindow.setText("Hide Main Window");
      }
  }//GEN-LAST:event__hidemainwindowActionPerformed

private void _enabletopicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__enabletopicItemStateChanged
// TODO add your handling code here:
    _topic.setEnabled(_enabletopic.isEnabled());
}//GEN-LAST:event__enabletopicItemStateChanged

private void _connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__connectActionPerformed
    ServerSelectDialog selectserver = new ServerSelectDialog(this, true, this);
    selectserver.setLocation(60, 100);
    selectserver.setVisible(true);
}//GEN-LAST:event__connectActionPerformed
    /**
     * @param args the command line arguments
     */
    /*  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
    public void run(){
    new FFIRC().setVisible(true);
    }
    });
    }*/
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton _changenick;
  private javax.swing.JButton _connect;
  private javax.swing.JCheckBox _enabletopic;
  private javax.swing.JButton _exit;
  private javax.swing.JLabel _font_meiliu;
  private javax.swing.JButton _hidemainwindow;
  private javax.swing.JTextField _inputMessage;
  private javax.swing.JTextPane _messages;
  private javax.swing.JTextField _nick;
  private javax.swing.JButton _notice;
  private javax.swing.JList _playerlist;
  private javax.swing.JButton _send;
  private javax.swing.JButton _sendDCCFile;
  private javax.swing.JButton _settopic;
  private javax.swing.JTextField _topic;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  // End of variables declaration//GEN-END:variables

    @Override
    public void onSelected(String server, int port) {
        if (engine != null) {
            if (engine.isConnected()) {
                engine.disconnect();
                engine.dispose();
            }
        }
        connect(server, port);
    }
}

class IRCEngine extends PircBot {

    FFIRC parent;
    String default_channel;
    public String server = null;
    public int port = 6667;

    public IRCEngine() {
    }

    public IRCEngine(FFIRC Parent, String channel) {
        this.parent = Parent;
        default_channel = channel;
        if (FTool.readConfig("ircnick") == null || FTool.readConfig("ircnick").equals("")) {
            FTool.updateConfig("ircnick", "noname" + String.valueOf((int) (Math.random() * 100)));
        }
        this.setName(FTool.readConfig("ircnick"));
    }

    public void setNick(String nick) {
        this.setName(nick);
    }

    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action) {
        if (FTool.readConfig("ircsound").equals("true")) {
            FTool.playSound("notify.wav");
        }
        parent.showMessage("<" + sender + "> act: " + action);
    }

    @Override
    protected void onChannelInfo(String channel, int userCount, String topic) {
        parent.setTitle("Fantasy Festa IRC: " + topic);
    }

    @Override
    protected void onConnect() {
        parent.showMessage("Connected to server.", Color.RED);
    }

    @Override
    protected void onDisconnect() {
        parent.showMessage("Disconnected from server.", Color.RED);
    }

    @Override
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        User[] users = this.getUsers(default_channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        parent.setListModel(mod);
        parent.showMessage("ChangeNick: " + oldNick + " -> " + newNick, Color.MAGENTA);
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        User[] users = this.getUsers(default_channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        parent.setListModel(mod);
        parent.showMessage("Login: " + sender + "(" + login + ")", Color.MAGENTA);
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        User[] users = this.getUsers(default_channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        parent.setListModel(mod);
        parent.showMessage("Quit: " + sourceNick + sourceLogin + "(" + sourceHostname + ")" + " " + reason, Color.MAGENTA);
    }

    @Override
    protected void onPart(String channel, String sender, String login, String hostname) {
        User[] users = this.getUsers(default_channel);
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        parent.setListModel(mod);
        parent.showMessage("Part: " + sender + login + "(" + hostname + ")", Color.MAGENTA);
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        FTool.updateConfig("ircserver", server);
        FTool.updateConfig("ircport", String.valueOf(port));
        DefaultListModel mod = new DefaultListModel();
        for (int i = 0; i < users.length; ++i) {
            mod.addElement(users[i].getNick());
        }
        parent.setListModel(mod);
        try {
            SimpleAttributeSet set = new SimpleAttributeSet();
            JTextPane _messages = parent.getMessagePane();
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
        if (FTool.readConfig("ircsound").equals("true")) {
            FTool.playSound("notify.wav");
        }
        parent.showMessage("<" + sender + "> " + message);
    }

    @Override
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
        parent.showMessage("<" + sourceNick + "> " + notice, Color.RED);
        // Tools.playSound("notify.wav");
    }

    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        if (changed) {
            parent.showMessage(setBy + " changed topic to" + topic, Color.BLUE);
        }
        parent.setTitle("Fantasy Festa IRC: " + topic);
    }

    @Override
    protected void onServerResponse(int code, String response) {
        parent.showMessage(response, Color.MAGENTA);
    }

    @Override
    protected void onUnknown(String line) {
        parent.showMessage(line, Color.RED);
    }

    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        parent.showMessage("PM from " + sender + ": " + message, Color.GREEN);
    }

    @Override
    protected void onIncomingFileTransfer(DccFileTransfer transfer) {
        File file = transfer.getFile();
        if (!new File("DCCFiles").exists()) {
            new File("DCCFiles").mkdir();
        }
        if (FTool.readConfig("autodcc").equals("true")) {
            file = new File(".\\DCCFiles\\" + file.getName());
            parent.showMessage("Recieving " + file.getName() + "...", Color.MAGENTA);
            transfer.receive(file, true);
        } else {
            if (JOptionPane.showConfirmDialog(null, transfer.getNick() + " is trying to send " + transfer.getFile().getName() + "(" + transfer.getSize() + " bytes) to you. Accept?", "DCC File Transfer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                file = new File(".\\DCCFiles\\" + file.getName());
                parent.showMessage("Recieving " + file.getName() + "...", Color.MAGENTA);
                transfer.receive(file, true);
            }
        }
    }

    @Override
    protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
        if (transfer.isIncoming()) {
            parent.showMessage(transfer.getFile().getName() + " recieved.", Color.MAGENTA);
        }
        if (transfer.isOutgoing()) {
            parent.showMessage(transfer.getFile().getName() + " sent.", Color.MAGENTA);
        }
    }
}
