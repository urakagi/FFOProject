///*
 /* simpleIRC.java
 *
 * Created on 2007/09/17, 21:56
 */
package org.sais.chocolat.irc;

import java.awt.Color;
import java.awt.Frame;
import java.io.*;
import java.net.ConnectException;
import javax.swing.*;
import javax.swing.text.*;
import org.jibble.pircbot.*;
import org.sais.chocolat.tool.Tools;

/**
 *
 * @author  Romulus
 */
public class IRCWatchFrame extends javax.swing.JFrame implements ServerSelectDialog.ServerSelectDialogCallback {

    IRCAgent mAgent;
    IRCManager mManager;
    int mLanguage;
    int mLocation;

    /** Creates new form simpleIRC */
    public IRCWatchFrame(IRCManager manager) {
        this.mManager = manager;
        initComponents();

        this.setVisible(true);
        this.setLocation(50, 50);

        if (!(Tools.readConfig("ircserver").equals("")) && !(Tools.readConfig("ircport").equals(""))) {
            connect(Tools.readConfig("ircserver"), Integer.parseInt(Tools.readConfig("ircport")));
        }

        manager.setDebuggable(_debuggable.isSelected());
    }

    class ConnectThread extends Thread {

        private String server;
        private int port;

        public ConnectThread(String server, int port) {
            this.server = server;
            this.port = port;
        }

        @Override
        public void run() {
        String encoding = "ISO-2022-JP";

        mAgent = new IRCAgent(IRCWatchFrame.this, mManager, _channel.getText());
        mManager.setAgent(mAgent);
        mAgent.setVerbose(false);
        if (Tools.readConfig("ircnick").equals("")) {
            Tools.updateConfig("ircnick", "Chocolat" + String.valueOf((int) (Math.random() * 1000)));
        }
        _nick.setText(Tools.readConfig("ircnick"));

        try {
            mAgent.setEncoding(encoding);
            mAgent.server = server;
            mAgent.port = port;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        while (true) {
            try {
                mAgent.connect(mAgent.server, mAgent.port);
                break;
            } catch (ConnectException ex) {
                JOptionPane.showMessageDialog(IRCWatchFrame.this, "Cannot connect to server.");
                ex.printStackTrace();
                break;
            } catch (NickAlreadyInUseException ex) {
                _nick.setText(_nick.getText() + String.valueOf((int) (100 * Math.random())));
                mAgent.setNick(_nick.getText());
            } catch (IrcException ex) {
                try {
                    SimpleAttributeSet set = new SimpleAttributeSet();
                    _messages.setCharacterAttributes(set, true);
                    Document doc = _messages.getStyledDocument();
                    doc.insertString(doc.getLength(), Tools.getNowTimeString("mm:ss") + " " + ex.getMessage(), set);
                    if (_messages.getDocument().getLength() > 0) {
                        _messages.setCaretPosition(_messages.getDocument().getLength() - 1);
                    }
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    return;
                }
                JOptionPane.showMessageDialog(IRCWatchFrame.this, "IrcException: " + ex.getMessage());
                ex.printStackTrace();
                break;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(IRCWatchFrame.this, "IOException: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }

        mAgent.joinChannel(_channel.getText());
        _inputMessage.requestFocus();
        }



    }

    private void connect(String server, int port) {
        new ConnectThread(server, port).start();
        showMessage("Connecting to " + server + ":" + port + "...");
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
            mAgent.sendNotice(_channel.getText(), message);
            showMessage("<" + mAgent.getNick() + "> " + message, Color.RED);
            _inputMessage.setText("");
            _inputMessage.requestFocus();
        } else {
            mAgent.sendMessage(_channel.getText(), message);
            showMessage("<" + mAgent.getNick() + "> " + message);
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
            doc.insertString(doc.getLength(), Tools.getNowTimeString("HH:mm") + " " + message + "\n", set);
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
        _enabletopic = new javax.swing.JCheckBox();
        _font_meiliu = new javax.swing.JLabel();
        _connect = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        _channel = new javax.swing.JTextField();
        _debuggable = new javax.swing.JCheckBox();

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
        _nick.setText("Chocolat");

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

        jLabel3.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 12));
        jLabel3.setText("Channel");

        _channel.setFont(new java.awt.Font("MS PGothic", 0, 12));
        _channel.setText("#幻想ノ宴");

        _debuggable.setSelected(true);
        _debuggable.setText("Debuggable");
        _debuggable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _debuggableActionPerformed(evt);
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 548, Short.MAX_VALUE)
                        .addComponent(_exit))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(_notice)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_inputMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_send)))
                                .addGap(8, 8, 8))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_nick, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_changenick))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_enabletopic, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_topic, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(8, 8, 8)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(_channel, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(_settopic))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 5, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_connect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 215, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(_sendDCCFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                            .addComponent(_debuggable))))
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
                            .addComponent(_exit))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_connect)
                            .addComponent(jLabel3)
                            .addComponent(_channel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_debuggable))))
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
      if (Tools.readConfig("dccdir") != null && !Tools.readConfig("dccdir").equals("")) {
          chooser.setCurrentDirectory(new File(Tools.readConfig("dccdir")));
      } else {
          chooser.setCurrentDirectory(new File("."));
      }
      chooser.setAcceptAllFileFilterUsed(true);
      int result = chooser.showOpenDialog(this);
      if (result == JFileChooser.CANCEL_OPTION) {
          return;
      }
      File file = chooser.getSelectedFile();
      Tools.updateConfig("dccdir", file.getParentFile().getPath());
      DccFileTransfer transfer = mAgent.dccSendFile(file, target, 120000);
      showMessage("Sending " + file.getName() + "...", Color.MAGENTA);
  }//GEN-LAST:event__sendDCCFileActionPerformed

  private void _settopicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__settopicActionPerformed
      if (_topic.getText().length() < 3) {
          return;
      }
      if (!_enabletopic.isSelected()) {
          return;
      }
      mAgent.setTopic(_channel.getText(), _topic.getText());
      _topic.setText("");
      _inputMessage.requestFocus();
  }//GEN-LAST:event__settopicActionPerformed

  private void _topicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__topicActionPerformed
      if (_topic.getText().length() < 3) {
          return;
      }
      mAgent.setTopic(_channel.getText(), _topic.getText());
      _topic.setText("");
      _inputMessage.requestFocus();
  }//GEN-LAST:event__topicActionPerformed

  private void _noticeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__noticeActionPerformed
      sendInputedMessage(true);
  }//GEN-LAST:event__noticeActionPerformed

  private void _exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__exitActionPerformed
      if (mAgent != null) {
          mAgent.disconnect();
          mAgent.dispose();
      }
      this.setVisible(false);
      this.dispose();
  }//GEN-LAST:event__exitActionPerformed

  private void _changenickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__changenickActionPerformed
      mAgent.changeNick(_nick.getText());
      Tools.updateConfig("ircnick", _nick.getText());
  }//GEN-LAST:event__changenickActionPerformed

  private void _sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__sendActionPerformed
      sendInputedMessage(false);
  }//GEN-LAST:event__sendActionPerformed

  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      if (mAgent != null) {
          try {
              mAgent.disconnect();
              mAgent.dispose();
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
  }//GEN-LAST:event_formWindowClosing

  private void _inputMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__inputMessageActionPerformed
      sendInputedMessage(false);
  }//GEN-LAST:event__inputMessageActionPerformed

private void _enabletopicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__enabletopicItemStateChanged
// TODO add your handling code here:
    _topic.setEnabled(_enabletopic.isEnabled());
}//GEN-LAST:event__enabletopicItemStateChanged

private void _connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__connectActionPerformed
    ServerSelectDialog selectserver = new ServerSelectDialog(this, true, this);
    selectserver.setLocation(60, 100);
    selectserver.setVisible(true);
}//GEN-LAST:event__connectActionPerformed

private void _debuggableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__debuggableActionPerformed
    mManager.setDebuggable(_debuggable.isSelected());
}//GEN-LAST:event__debuggableActionPerformed
    /**
     * @param args the command line arguments
     */
    /*  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
    public void run(){
    new IRCWatchFrame().setVisible(true);
    }
    });
    }*/
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _changenick;
    private javax.swing.JTextField _channel;
    private javax.swing.JButton _connect;
    private javax.swing.JCheckBox _debuggable;
    private javax.swing.JCheckBox _enabletopic;
    private javax.swing.JButton _exit;
    private javax.swing.JLabel _font_meiliu;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onSelected(String server, int port) {
        if (mAgent != null) {
            if (mAgent.isConnected()) {
                mAgent.disconnect();
                mAgent.dispose();
            }
        }
        connect(server, port);
    }
}
