/*
 * Startup.java
 *
 * Created on 2007/2/3 12:45
 */
package org.sais.fantasyfesta.core;

import java.awt.Color;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.fantasyfesta.deck.DeckEditor;
import org.sais.fantasyfesta.net.FFIRC;
import org.sais.fantasyfesta.tool.FTool;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.*;
import java.net.*;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.deck.ChocolatsArchive;
import org.sais.fantasyfesta.enums.EGameMode;
import org.sais.fantasyfesta.net.socketthread.ClientSocketThread;
import org.sais.fantasyfesta.net.socketthread.HostSocketThread;
import org.sais.fantasyfesta.recorder.RecordViewClientSocketThread;
import org.sais.fantasyfesta.recorder.RecordViewHostFSock;
import org.sais.fantasyfesta.ui.CardList;
import org.sais.fantasyfesta.ui.FrontFrame;

/**
 *
 * @author  Romulus
 */
public class Startup extends javax.swing.JFrame implements ITitleUI {

    private static final boolean DEBUG = false;
    //
    public static final String VERSION = "9.1.15";
    //
    private boolean noupdate = false;
    private HostSocketThread mHostSocketThread;

    public Startup() throws Exception {
        this.setIconImage(new ImageIcon(new ReadInnerFile("mainicongray.jpg").u).getImage());
        if (!new File("nodebug").exists()) {  //Debug Mode, to disable log put a nodebug at excuteroot
            if (!new File("logs").exists()) {
                new File("logs").mkdir();
            }
            for (int i = 0; i < 10; ++i) {
                File out = new File("logs/message_play_" + i + ".log");
                File err = new File("logs/message_error_" + i + ".log");
                if (!out.exists() || i == 9) {
                    System.setOut(new PrintStream(new FileOutputStream(out), true, "UTF-8"));
                    System.setErr(new PrintStream(new FileOutputStream(err), true, "UTF-8"));
                    if (i >= 5) {
                        File oldout = new File("logs/message_play_" + (i - 5) + ".log");
                        File olderr = new File("logs/message_error_" + (i - 5) + ".log");
                        oldout.delete();
                        olderr.delete();
                        if (i == 9) {
                            new File("logs/message_play_0.log").delete();
                            new File("logs/message_error_0.log").delete();
                            new File("logs/message_play_1.log").delete();
                            new File("logs/message_error_1.log").delete();
                            new File("logs/message_play_2.log").delete();
                            new File("logs/message_error_2.log").delete();
                            new File("logs/message_play_3.log").delete();
                            new File("logs/message_error_3.log").delete();
                            new File("logs/message_play_4.log").delete();
                            new File("logs/message_error_4.log").delete();
                        }
                    } else {
                        File oldout = new File("logs/message_play_" + (i + 5) + ".log");
                        File olderr = new File("logs/message_error_" + (i + 5) + ".log");
                        oldout.delete();
                        olderr.delete();
                    }
                    break;
                }
            }
        }
        initComponents();

        FTool.setLocale();

        this.setLocation(250, 200);
        // this.setVisible(true);
        versionlabel.setText("VER " + VERSION);
        this.setTitle("Fantasy Festa Online " + versionlabel.getText());
        readconfig();
        if (!new File("nodebug").exists()) {  //Debug Mode, to disable log put a nodebug at excuteroot
            checkupdate(!noupdate, FTool.readBooleanConfig("originalcard", false));
        }

        if (FTool.readConfig("originalcard").toLowerCase().equals("true")) {
            this.setTitle("Fantasy Festa Online " + versionlabel.getText() + "(" + FTool.readConfig("kver") + ")");
        }

        CardDatabase.readCardBase();
        _ip.requestFocus();
    }

    private void host() {
        if (mHostSocketThread == null) {
            mHostSocketThread = new HostSocketThread(this, new InetSocketAddress(FTool.safeParseInt(_port.getText())));
            mHostSocketThread.start();
        }
    }

    void onHostDisconnect() {
        if (mHostSocketThread != null) {
            mHostSocketThread.closeSocket();
        }
    }

    private void client() {
        networkclient.setEnabled(false);
        String addr = _ip.getText().trim();
        int port = FTool.safeParseInt(_port.getText());
        if (addr.matches("^(\\d+\\-){3}\\d+$")) {
            addr = addr.replace('-', '.');
        }
        if (addr.equals("")) {
            addr = "127.0.0.1";
        }
        ClientSocketThread cf = new ClientSocketThread(this, new InetSocketAddress(addr, port), false);
        cf.start();
        networkclient.setEnabled(true);
    }

    private void watch() {
        _networkwatch.setEnabled(false);
        String addr = _ip.getText().trim();
        int port = FTool.safeParseInt(_port.getText());
        if (addr.matches("^(\\d+\\-){3}\\d+$")) {
            addr = addr.replace('-', '.');
        }
        if (addr.equals("")) {
            addr = "127.0.0.1";
        }
        ClientSocketThread cs = new ClientSocketThread(this, new InetSocketAddress(addr, port), true);
        cs.start();
        _networkwatch.setEnabled(true);
    }

    private void openRecord() {
        JFileChooser chooser = new JFileChooser();
        File dir = new File("record");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        chooser.setCurrentDirectory(dir);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                String name = f.getName();
                return name.startsWith("Record") && name.endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Fantasy Festa Online Record (*.txt)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        RecordViewHostFSock host = new RecordViewHostFSock(file);
        RecordViewClientSocketThread soc = new RecordViewClientSocketThread(this, "localhost", host.getPort());
        soc.start();
    }

    private void debug() {
        GameCore core = new GameCore(EGameMode.DEBUG, null, new InetSocketAddress(12700), this);
        core.launchDebugGame(getPlayerName());
        this.setVisible(false);
    }

    @Override
    public String getPlayerName() {
        return hn.getText();
    }

    @Override
    public void setMessage(String message) {
        _messagebar.setText(message);
    }

    private void readconfig() {
        if (!FTool.readConfig("name").equals("")) {
            hn.setText(FTool.readConfig("name"));
        }
        if (FTool.readConfig("semiautoupdate").toLowerCase().equals("off")) {
            noupdate = true;
        }
    }

    private void checkupdate(boolean core, boolean orica) throws IOException {
        _messagebar.setText("Checking Update...");
        UpdateWorker worker = new UpdateWorker(core, orica);
        worker.execute();
    }

    @Override
    public void clearHostSock() {
        mHostSocketThread = null;
    }

    class UpdateWorker extends SwingWorker<Void, String> {

        static final int MODE_CORE = 0;
        static final int MODE_ORICA = 1;
        private boolean mCheckCore;
        private boolean mCheckOrica;
        private int mMode;
        private boolean mHasUpdate = false;

        public UpdateWorker(boolean mCheckCore, boolean mCheckOrica) {
            this.mCheckCore = mCheckCore;
            // this.mCheckOrica = mCheckOrica;
            // Disable orica in this time
            this.mCheckOrica = false;
        }

        @Override
        protected Void doInBackground() throws Exception {
            String latest;
            String version;
            String updateURL;
            String localfile;

            if (mCheckOrica) {
                latest = FTool.getKLatest();
                if (latest == null) {
                    publish("Cannot connect to version server.");
                    return null;
                }
                version = FTool.readConfig("kver");
                updateURL = "http://dl.dropbox.com/u/5736424/originalcarddatabase.txt";
                localfile = "originalcarddatabase.txt";
                if (!latest.equals(version)) {
                    downloadUpdate(MODE_ORICA, latest, version, updateURL, localfile);
                    mHasUpdate = true;
                    mMode = MODE_ORICA;
                }
                FTool.updateConfig("kver", latest);
            }

            if (mCheckCore) {
                String[] latestinfo = FTool.getLatest();
                if (latestinfo == null) {
                    publish("Cannot connect to version server.");
                    return null;
                }
                latest = latestinfo[0];
                version = Startup.VERSION;
                updateURL = "http://dl.dropbox.com/u/5736424/FFO/FantasyFestaCore.zip";
                localfile = "FantasyFestaCore.zip";
                if (!latest.equals(version) && !latest.startsWith("7.")) {
                    downloadUpdate(MODE_CORE, latest, version, updateURL, localfile);
                    mHasUpdate = true;
                    mMode = MODE_CORE;
                }
            }

            return null;
        }

        private void downloadUpdate(int mode, String latest, String version, String updateURL, String localfile) {
            try {
                publish("Downloading Update File...");
                URL url = new URL(updateURL);
                URLConnection conn = url.openConnection();
                int size = conn.getContentLength();
                BufferedInputStream dr = new BufferedInputStream(url.openStream(), 8192);
                if (mode == MODE_CORE) {
                    File backup = new File("FantasyFestaCore.zip");
                    if (backup.exists()) {
                        backup.renameTo(new File("FantasyFestaCore(v" + Startup.VERSION + ").zip"));
                    }
                }
                BufferedOutputStream local = new BufferedOutputStream(new FileOutputStream(localfile));
                int transcate = dr.read();
                int progress = 0;
                while (transcate >= 0) {
                    local.write(transcate);
                    transcate = dr.read();
                    ++progress;
                    if (progress % 27550 == 0) {
                        if (mode == MODE_CORE) {
                            publish(String.format("Downloading Core...%.1f%%", progress * 100. / size));
                        } else {
                            publish(String.format("Downloading Original Cards...%.1f%%", progress * 100. / size));
                        }
                    }
                }
                local.flush();
                local.close();
                dr.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, FTool.getLocale(100));
                ex.printStackTrace();
                publish("Failed when download!");
            }

        }

        @Override
        protected void process(List<String> chunks) {
            _messagebar.setText(chunks.get(0));
            super.process(chunks);
        }

        @Override
        protected void done() {
            if (!mHasUpdate) {
                publish("Stand By Ready.");
            } else {
                if (mMode == MODE_CORE) {
                    _messagebar.setText("Extract FantasyFestaCore.zip and restart please.");
                } else {
                    _messagebar.setText("Original cards refreshed.");
                }
            }
            super.done();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        versionlabel = new javax.swing.JLabel();
        _networkhost = new GradientButton(new Color(204, 204, 255));
        _deckeditor = new GradientButton(new Color(204, 255, 255));
        exit = new GradientButton(Color.WHITE);
        singlemode = new javax.swing.JButton();
        hn = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        _ip = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        _port = new javax.swing.JTextField();
        networkclient = new GradientButton(new Color(204, 255, 204));
        _messagebar = new javax.swing.JTextField();
        _openrecord = new javax.swing.JButton();
        _irc_connect = new javax.swing.JButton();
        _networkwatch = new GradientButton(new Color(255, 255, 204));
        _irc_connect_taiwan = new javax.swing.JButton();
        _irc_connect1 = new javax.swing.JButton();
        _showcardlist = new javax.swing.JButton();
        _deckmanager = new GradientButton(new Color(255, 204, 204));
        configButton = new GradientButton(new Color(255, 204, 255));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/title.PNG"))); // NOI18N

        versionlabel.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        versionlabel.setText("Ver xx.yy.zz");

        _networkhost.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        _networkhost.setText("NETWORK MODE - HOST");
        _networkhost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _networkhostActionPerformed(evt);
            }
        });

        _deckeditor.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        _deckeditor.setText("DECK EDITOR");
        _deckeditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _deckeditorActionPerformed(evt);
            }
        });

        exit.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        exit.setText("EXIT");
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });

        singlemode.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        singlemode.setText("DEBUG");
        singlemode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singlemodeActionPerformed(evt);
            }
        });

        hn.setBackground(new java.awt.Color(238, 255, 255));
        hn.setFont(new java.awt.Font("MS PGothic", 0, 18)); // NOI18N
        hn.setText("名前がない程度の能力");
        hn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hnFocusLost(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel3.setText("NAME");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel5.setText("TO HOST");

        _ip.setBackground(new java.awt.Color(238, 255, 255));
        _ip.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel6.setText("PORT");

        _port.setBackground(new java.awt.Color(238, 255, 255));
        _port.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        _port.setText("12700");

        networkclient.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        networkclient.setText("NETWORK MODE - CLIENT");
        networkclient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                networkclientActionPerformed(evt);
            }
        });

        _messagebar.setBackground(new java.awt.Color(255, 244, 246));
        _messagebar.setEditable(false);
        _messagebar.setFont(new java.awt.Font("MS PGothic", 0, 14)); // NOI18N
        _messagebar.setText("Stand by ready.");

        _openrecord.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        _openrecord.setText("OPEN RECORD");
        _openrecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _openrecordActionPerformed(evt);
            }
        });

        _irc_connect.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _irc_connect.setText("IRC （チャットルーム、普段はこちら）");
        _irc_connect.setMargin(new java.awt.Insets(2, 10, 2, 10));
        _irc_connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _irc_connectActionPerformed(evt);
            }
        });

        _networkwatch.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        _networkwatch.setText("NETWORK MODE - WATCH");
        _networkwatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _networkwatchActionPerformed(evt);
            }
        });

        _irc_connect_taiwan.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _irc_connect_taiwan.setText("中文");
        _irc_connect_taiwan.setMargin(new java.awt.Insets(2, 10, 2, 10));
        _irc_connect_taiwan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _irc_connect_taiwanActionPerformed(evt);
            }
        });

        _irc_connect1.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _irc_connect1.setText("IRC （対戦専用雑談禁止）");
        _irc_connect1.setMargin(new java.awt.Insets(2, 10, 2, 10));
        _irc_connect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _irc_connect1ActionPerformed(evt);
            }
        });

        _showcardlist.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        _showcardlist.setText("CARD LIST");
        _showcardlist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _showcardlistActionPerformed(evt);
            }
        });

        _deckmanager.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        _deckmanager.setText("CHOCOLAT'S ARCHIVE");
        _deckmanager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _deckmanagerActionPerformed(evt);
            }
        });

        configButton.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        configButton.setText("SETTINGS");
        configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(161, Short.MAX_VALUE)
                .addComponent(versionlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(130, 130, 130))
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_messagebar, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(_networkhost)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hn, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_irc_connect, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_openrecord))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_deckeditor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_deckmanager)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_showcardlist))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_ip, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_port))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(networkclient)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_networkwatch)))
                        .addGap(20, 20, 20))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_irc_connect1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_irc_connect_taiwan, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(singlemode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                        .addComponent(exit)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(hn)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(configButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_networkhost, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(networkclient)
                    .addComponent(_networkwatch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(_ip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_deckeditor)
                    .addComponent(_deckmanager)
                    .addComponent(_showcardlist))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_irc_connect)
                    .addComponent(_openrecord))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_irc_connect1)
                    .addComponent(_irc_connect_taiwan)
                    .addComponent(singlemode)
                    .addComponent(exit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_messagebar, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void _networkwatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__networkwatchActionPerformed
      _messagebar.setText("Connecting...");
      watch();
  }//GEN-LAST:event__networkwatchActionPerformed

  private void _irc_connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__irc_connectActionPerformed
      try {
          new FFIRC(this, FFIRC.LANGUAGE_JAPANESE);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
  }//GEN-LAST:event__irc_connectActionPerformed

   private void _deckeditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__deckeditorActionPerformed
       try {
           DeckEditor dew;
           dew = new DeckEditor();
           dew.setVisible(true);
       } catch (Exception ex) {
           ex.printStackTrace();
       }
   }//GEN-LAST:event__deckeditorActionPerformed

   private void _openrecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__openrecordActionPerformed
       openRecord();
   }//GEN-LAST:event__openrecordActionPerformed

   private void hnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hnFocusLost
       try {
           FTool.updateConfig("name", hn.getText());
       } catch (Exception ex) {
           ex.printStackTrace();
       }
   }//GEN-LAST:event_hnFocusLost

   private void singlemodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singlemodeActionPerformed
       debug();
   }//GEN-LAST:event_singlemodeActionPerformed

   private void networkclientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_networkclientActionPerformed
       _messagebar.setText("Connecting...");
       onHostDisconnect();
       client();
   }//GEN-LAST:event_networkclientActionPerformed

   private void _networkhostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__networkhostActionPerformed
       _messagebar.setText("Waiting for Connection...");
       host();
   }//GEN-LAST:event__networkhostActionPerformed

   private void exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitActionPerformed
       System.exit(0);
   }//GEN-LAST:event_exitActionPerformed

private void _irc_connect_taiwanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__irc_connect_taiwanActionPerformed
    try {//GEN-LAST:event__irc_connect_taiwanActionPerformed
            new FFIRC(this, FFIRC.LANGUAGE_CHINESE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

        private void _irc_connect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__irc_connect1ActionPerformed
            try {
                new FFIRC(this, FFIRC.MODE_DUEL);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }//GEN-LAST:event__irc_connect1ActionPerformed

        private void _showcardlistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__showcardlistActionPerformed
            try {
                new CardList().setVisible(true);
            } catch (Exception ex) {
                Logger.getLogger(Startup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//GEN-LAST:event__showcardlistActionPerformed

        private void _deckmanagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__deckmanagerActionPerformed
            new ChocolatsArchive(null).setVisible(true);
        }//GEN-LAST:event__deckmanagerActionPerformed

private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
    new FrontFrame().setVisible(true);
}//GEN-LAST:event_configButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FTool.setLocale();
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    if (DEBUG) {
                        // For debug
                        Startup a = new Startup();
                        a.setVisible(true);
                        Startup b = new Startup();
                        b.setVisible(true);
                        a.host();
                        b.client();
                    } else {
                        new Startup().setVisible(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _deckeditor;
    private javax.swing.JButton _deckmanager;
    private javax.swing.JTextField _ip;
    private javax.swing.JButton _irc_connect;
    private javax.swing.JButton _irc_connect1;
    private javax.swing.JButton _irc_connect_taiwan;
    private javax.swing.JTextField _messagebar;
    private javax.swing.JButton _networkhost;
    private javax.swing.JButton _networkwatch;
    private javax.swing.JButton _openrecord;
    private javax.swing.JTextField _port;
    private javax.swing.JButton _showcardlist;
    private javax.swing.JButton configButton;
    private javax.swing.JButton exit;
    private javax.swing.JTextField hn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JButton networkclient;
    private javax.swing.JButton singlemode;
    private javax.swing.JLabel versionlabel;
    // End of variables declaration//GEN-END:variables
}

class GradientButton extends JButton {

    private Color mColor;

    public GradientButton(Color c) {
        super();
        setContentAreaFilled(false);
        mColor = c;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Color dark = mColor.darker();
        Color bright = mColor.brighter();

        GradientPaint p = new GradientPaint(0, 0,
                mColor, 0, (int) (getHeight() * 0.25), bright);
        Paint oldPaint = g2.getPaint();
        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), (int) (getHeight() * 0.25));
        g2.setPaint(oldPaint);

        p = new GradientPaint(0, (int) (getHeight() * 0.25),
                bright, 0, getHeight(), dark);
        oldPaint = g2.getPaint();
        g2.setPaint(p);
        g2.fillRect(0, (int) (getHeight() * 0.25), getWidth(), getHeight());
        g2.setPaint(oldPaint);

        super.paintComponent(g);
    }
}
