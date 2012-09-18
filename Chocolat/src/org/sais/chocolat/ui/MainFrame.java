/*
 * Recipient.java
 *
 * Created on 2008/11/28, 19:54
 */
package org.sais.chocolat.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.sais.chocolat.core.DataFileWriter;
import org.sais.chocolat.core.Participant;
import org.sais.chocolat.core.Round;
import org.sais.chocolat.core.Tournament;
import org.sais.chocolat.irc.IIRCManagerCallback;
import org.sais.chocolat.irc.IIRCManagerResultSetCallback;
import org.sais.chocolat.irc.IRCManager;
import org.sais.chocolat.xml.XMLUtils;

/**
 *
 * @author  Romulus
 */
public class MainFrame extends javax.swing.JFrame implements IIRCManagerCallback {

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_DECK = 5;
    private static String SLOT = "  == Slot ==";
    private Tournament tour = new Tournament();
    private boolean silenttable = false;
    private IRCManager mIRCManager;

    public MainFrame() {
        try {
            if (!new File("nodebug").exists()) {  //Debug Mode, to disable log put a nodebug at excuteroot
                System.setOut(new PrintStream(new FileOutputStream("message_play.log")));
                System.setErr(new PrintStream(new FileOutputStream("message_error.log")));
            }
        } catch (Exception e) {
        }

        initComponents();
        _participants.setRowHeight(20);
        _participants.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                if (!silenttable) {
                    tableChange(e);
                }
            }
        });
        for (int i = 0; i < _participants.getModel().getRowCount(); ++i) {
            _participants.getModel().setValueAt(SLOT, i, 1);
        }
        setIconImage(new ImageIcon(getClass().getResource("/note.jpg")).getImage());
    }

    private void addParticipant() {
        silenttable = true;
        TableModel model = _participants.getModel();

        int id = tour.enroll();
        int rows = tour.countParticipants() - 1;

        model.setValueAt(new Integer(id), rows, 0);
        model.setValueAt("", rows, 1);
        model.setValueAt(new Integer(0), rows, 2);
        model.setValueAt(new Float(0f), rows, 3);
        model.setValueAt(new Boolean(true), rows, 4);

        ++rows;
        silenttable = false;
    }

    private void deleteEnrolled() {
        silenttable = true;
        int idx = _participants.getSelectedRow();
        int rows = tour.countParticipants() - 1;
        TableModel model = _participants.getModel();

        if (idx < 0) {
            return;
        }

        if (model.getValueAt(idx, 0) == null) {
            return;
        }

        int playerid = (Integer) model.getValueAt(idx, 0);

        for (int r = idx; r < rows; ++r) {
            for (int c = 0; c < _participants.getColumnCount(); ++c) {
                model.setValueAt(model.getValueAt(r + 1, c), r, c);
            }
        }
        for (int c = 0; c < _participants.getColumnCount(); ++c) {
            model.setValueAt(null, rows, c);
        }
        model.setValueAt(SLOT, rows, 1);

        tour.delete(playerid);

        silenttable = false;
    }

    private void tableChange(TableModelEvent evt) {
        TableModel model = _participants.getModel();

        if (model.getValueAt(evt.getLastRow(), 0) == null) {
            return;
        }
        tour.setParticipantName((Integer) model.getValueAt(evt.getLastRow(), 0), (String) model.getValueAt(evt.getLastRow(), 1));
        tour.setParticipantDeck((Integer) model.getValueAt(evt.getLastRow(), 0), (String) model.getValueAt(evt.getLastRow(), COLUMN_DECK));
        tour.setIsHostable((Integer) model.getValueAt(evt.getLastRow(), 0), (Boolean) model.getValueAt(evt.getLastRow(), 4));
        tour.setParticipantIP((Integer) model.getValueAt(evt.getLastRow(), 0), (String) model.getValueAt(evt.getLastRow(), 6));
    }

    /**
     * Pair next round.
     * @return The generated PairResultFrame
     */
    public IIRCManagerResultSetCallback pair() {
        if (tour.round.size() > 0) {
            if (!tour.getCurrentRound().isResultSet()) {
                return null;
            }
        }

        tour.pair();
        Round r = tour.getCurrentRound();

        _message.setText("Round " + tour.round.size());

        Vector<String> v = new Vector<String>(tour.round.size());
        for (int i = 0; i < tour.round.size(); ++i) {
            v.add("Round " + (i + 1));
        }
        ComboBoxModel mod = new DefaultComboBoxModel(v);
        _roundlist.setModel(mod);

        flushPairing();

        PairResultFrame resultFrame = new PairResultFrame(this, r, tour);
        resultFrame.setLocation(40, 90);
        resultFrame.setVisible(true);

        return resultFrame;
    }

    private void repair() {
        tour.repair();
        Round r = tour.getCurrentRound();

        _message.setText("Round " + tour.round.size());

        Vector<String> v = new Vector<String>(tour.round.size());
        for (int i = 0; i < tour.round.size(); ++i) {
            v.add("Round " + (i + 1));
        }
        ComboBoxModel mod = new DefaultComboBoxModel(v);
        _roundlist.setModel(mod);

        flushPairing();

        PairResultFrame p = new PairResultFrame(this, r, tour);
        p.setLocation(40, 90);
        p.setVisible(true);
    }

    private void modifyRound() {
        PairResultFrame p = new PairResultFrame(this, tour.round.get(_roundlist.getSelectedIndex()), tour);
        p.setLocation(40, 90);
        p.setVisible(true);
    }

    private void pairLates() {
        tour.pairLates();
        flushPairing();

        PairResultFrame p = new PairResultFrame(this, tour.getCurrentRound(), tour);
        p.setLocation(40, 90);
        p.setVisible(true);
    }

    public void invokeUpdate() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    update();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void update() {
        silenttable = true;
        DataFileWriter.flushResult(tour);

        ArrayList<Participant> a = new ArrayList<Participant>();
        for (Integer id : tour.participant.keySet()) {
            a.add(tour.participant.get(id));
        }
        Collections.sort(a);
        Collections.reverse(a);

        int row = 0;
        for (Participant p : a) {
            _participants.setValueAt(p.id, row, 0);
            _participants.setValueAt(p.name, row, 1);
            _participants.setValueAt(p.getPoint(), row, 2);
            _participants.setValueAt(p.getOpp(), row, 3);
            _participants.setValueAt(p.isHostable, row, 4);
            _participants.setValueAt(p.deck, row, 5);
            _participants.setValueAt(p.ip, row, 6);
            _participants.setValueAt(row + 1, row, 7);
            ++row;
        }
        for (int c = 0; c < _participants.getColumnCount(); ++c) {
            _participants.setValueAt(null, row, c);
        }
        _participants.setValueAt(SLOT, row, 1);

        DataFileWriter.flushStanding(tour);

        Vector<String> v = new Vector<String>(tour.round.size());
        for (int i = 0; i < tour.round.size(); ++i) {
            v.add("Round " + (i + 1));
        }
        ComboBoxModel mod = new DefaultComboBoxModel(v);
        _roundlist.setModel(mod);

        _message.setText("Round " + tour.round.size());
        _tourname.setText(tour.name);
        _promoter.setText(tour.promoter);
        showDate();

        silenttable = false;
    }

    private void flushStanding() {
        DataFileWriter.flushStanding(tour);
        JOptionPane.showMessageDialog(null, "Results flushed.");
    }

    private void flushPairing() {
        tour.name = _tourname.getText();
        DataFileWriter.flushPairing(tour);
    }

    private void save() {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tour.name + ".xml"), "UTF-8"), 8192);
            tour.writeXML(out);
            out.close();

            JOptionPane.showMessageDialog(this, tour.name + ".xml Saved.");
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void load() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new xmlFilter());

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        tour = XMLUtils.parse(chooser.getSelectedFile());
        if (mIRCManager != null) {
            mIRCManager.setTournament(tour);
        }

        update();
    }

    private void importDeck() {
        BufferedReader in = null;
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setAcceptAllFileFilterUsed(true);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) {
                return;
            }

            ArrayList<String> decks = new ArrayList<String>();
            in = new BufferedReader(new InputStreamReader(new FileInputStream(chooser.getSelectedFile()), "UTF-8"));
            String line = in.readLine();
            while (line != null) {
                decks.add(line);
                line = in.readLine();
            }
            in.close();

            for (String s : decks) {
                for (int id : tour.participant.keySet()) {
                    Participant p = tour.participant.get(id);
                    if (s.contains(p.name)) {
                        p.deck = s.substring(p.name.length() + 1);
                    }
                }
            }

            update();

        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void enterIRC() {
        mIRCManager = new IRCManager(this, tour);
    }

    public IIRCManagerResultSetCallback nextRound() {
        return pair();
    }

    private void showDate() {
        if (tour.date.getTime() > 0) {
            _date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tour.date) + " 開催");
        } else {
            _date.setText("開催時刻不明");
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

        jScrollPane1 = new javax.swing.JScrollPane();
        _participants = new javax.swing.JTable() {
            public boolean isCellEditable(int row,int column){
                if (column == COLUMN_ID) {
                    return false;
                }
                return row < tour.participant.size() ? true : false;
            }
        }
        ;
        _addparticipant = new javax.swing.JButton();
        _deleteenrolled = new javax.swing.JButton();
        _pair = new javax.swing.JButton();
        _message = new javax.swing.JLabel();
        _tourname = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        _flushstanding = new javax.swing.JButton();
        _flushpairing = new javax.swing.JButton();
        _modifyresult = new javax.swing.JButton();
        _pairlates = new javax.swing.JButton();
        _roundlist = new javax.swing.JComboBox();
        _repair = new javax.swing.JButton();
        _save = new javax.swing.JButton();
        _load = new javax.swing.JButton();
        _loaddecksheet = new javax.swing.JButton();
        _reflushresults = new javax.swing.JButton();
        _enterirc = new javax.swing.JButton();
        _date = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        _promoter = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chocolat (Lv31)");

        _participants.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        _participants.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
        _participants.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Points", "Opp%", "Host", "deck", "IP", "Rank"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        _participants.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(_participants);
        _participants.getColumnModel().getColumn(0).setPreferredWidth(5);
        _participants.getColumnModel().getColumn(2).setResizable(false);
        _participants.getColumnModel().getColumn(2).setPreferredWidth(10);
        _participants.getColumnModel().getColumn(3).setResizable(false);
        _participants.getColumnModel().getColumn(3).setPreferredWidth(25);
        _participants.getColumnModel().getColumn(4).setResizable(false);
        _participants.getColumnModel().getColumn(4).setPreferredWidth(5);
        _participants.getColumnModel().getColumn(6).setResizable(false);
        _participants.getColumnModel().getColumn(6).setPreferredWidth(15);
        _participants.getColumnModel().getColumn(7).setResizable(false);
        _participants.getColumnModel().getColumn(7).setPreferredWidth(5);

        _addparticipant.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _addparticipant.setText("Add Participant");
        _addparticipant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _addparticipantActionPerformed(evt);
            }
        });

        _deleteenrolled.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _deleteenrolled.setText("Delete Enrolled");
        _deleteenrolled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _deleteenrolledActionPerformed(evt);
            }
        });

        _pair.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _pair.setText("Pair");
        _pair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _pairActionPerformed(evt);
            }
        });

        _message.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
        _message.setText("Round 0");

        _tourname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _tournameActionPerformed(evt);
            }
        });
        _tourname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                _tournameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                _tournameKeyReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        jLabel1.setText("Name of the Tournament");

        _flushstanding.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _flushstanding.setText("Flush Standing");
        _flushstanding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _flushstandingActionPerformed(evt);
            }
        });

        _flushpairing.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _flushpairing.setText("Flush Pairing");
        _flushpairing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _flushpairingActionPerformed(evt);
            }
        });

        _modifyresult.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _modifyresult.setText("Modify Round");
        _modifyresult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _modifyresultActionPerformed(evt);
            }
        });

        _pairlates.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _pairlates.setText("Pair Lates");
        _pairlates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _pairlatesActionPerformed(evt);
            }
        });

        _roundlist.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N

        _repair.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _repair.setText("Repair");
        _repair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _repairActionPerformed(evt);
            }
        });

        _save.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _save.setText("Save");
        _save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _saveActionPerformed(evt);
            }
        });

        _load.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _load.setText("Load");
        _load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _loadActionPerformed(evt);
            }
        });

        _loaddecksheet.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _loaddecksheet.setText("Import Deck");
        _loaddecksheet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _loaddecksheetActionPerformed(evt);
            }
        });

        _reflushresults.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12)); // NOI18N
        _reflushresults.setText("Reflush Results");
        _reflushresults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _reflushresultsActionPerformed(evt);
            }
        });

        _enterirc.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _enterirc.setText("Enter IRC");
        _enterirc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _enterircActionPerformed(evt);
            }
        });

        _date.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        _date.setText("開催時刻");

        jLabel2.setFont(new java.awt.Font("MS PGothic", 0, 12)); // NOI18N
        jLabel2.setText("主催者");

        _promoter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _promoterActionPerformed(evt);
            }
        });
        _promoter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                _promoterKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                _promoterKeyReleased(evt);
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
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_deleteenrolled, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                                .addContainerGap())
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_addparticipant, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                                .addContainerGap())
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_pairlates)
                                    .addComponent(_roundlist, 0, 157, Short.MAX_VALUE)
                                    .addComponent(_modifyresult)
                                    .addComponent(_flushpairing)
                                    .addComponent(_flushstanding)
                                    .addComponent(_repair)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_save)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_load)))
                                .addContainerGap(24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_enterirc)
                                    .addComponent(_reflushresults)
                                    .addComponent(_loaddecksheet)
                                    .addComponent(_pair)
                                    .addComponent(_date))
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_message, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(73, 73, 73)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_promoter, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_tourname, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_message, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(_tourname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_promoter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_date)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_addparticipant)
                        .addGap(5, 5, 5)
                        .addComponent(_deleteenrolled)
                        .addGap(18, 18, 18)
                        .addComponent(_pair)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_pairlates)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_flushstanding)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_flushpairing)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_modifyresult)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_roundlist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(_repair)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_save)
                            .addComponent(_load))
                        .addGap(18, 18, 18)
                        .addComponent(_loaddecksheet)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_reflushresults)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(_enterirc))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void _addparticipantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__addparticipantActionPerformed
    addParticipant();
}//GEN-LAST:event__addparticipantActionPerformed

private void _deleteenrolledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__deleteenrolledActionPerformed
    deleteEnrolled();
}//GEN-LAST:event__deleteenrolledActionPerformed

private void _pairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__pairActionPerformed
    pair();
}//GEN-LAST:event__pairActionPerformed

private void _flushstandingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__flushstandingActionPerformed
    flushStanding();
}//GEN-LAST:event__flushstandingActionPerformed

private void _flushpairingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__flushpairingActionPerformed
    flushPairing();
}//GEN-LAST:event__flushpairingActionPerformed

private void _modifyresultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__modifyresultActionPerformed

    modifyRound();
}//GEN-LAST:event__modifyresultActionPerformed

private void _pairlatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__pairlatesActionPerformed

    pairLates();
}//GEN-LAST:event__pairlatesActionPerformed

private void _repairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__repairActionPerformed

    repair();
}//GEN-LAST:event__repairActionPerformed

private void _saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__saveActionPerformed
    save();
}//GEN-LAST:event__saveActionPerformed

private void _loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__loadActionPerformed
    load();
}//GEN-LAST:event__loadActionPerformed

private void _tournameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__tournameActionPerformed
    tour.name = _tourname.getText();
}//GEN-LAST:event__tournameActionPerformed

private void _tournameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__tournameKeyPressed
    tour.name = _tourname.getText();
}//GEN-LAST:event__tournameKeyPressed

private void _loaddecksheetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__loaddecksheetActionPerformed
    importDeck();
}//GEN-LAST:event__loaddecksheetActionPerformed

private void _reflushresultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__reflushresultsActionPerformed
    DataFileWriter.reflushAllResults(tour);
    JOptionPane.showMessageDialog(null, "Results flushed.");
}//GEN-LAST:event__reflushresultsActionPerformed

private void _enterircActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__enterircActionPerformed
    enterIRC();
}//GEN-LAST:event__enterircActionPerformed

private void _tournameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__tournameKeyReleased
    tour.name = _tourname.getText();
}//GEN-LAST:event__tournameKeyReleased

private void _promoterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__promoterActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__promoterActionPerformed

private void _promoterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__promoterKeyPressed
    // TODO add your handling code here:
}//GEN-LAST:event__promoterKeyPressed

private void _promoterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__promoterKeyReleased
    // TODO add your handling code here:
}//GEN-LAST:event__promoterKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _addparticipant;
    private javax.swing.JLabel _date;
    private javax.swing.JButton _deleteenrolled;
    private javax.swing.JButton _enterirc;
    private javax.swing.JButton _flushpairing;
    private javax.swing.JButton _flushstanding;
    private javax.swing.JButton _load;
    private javax.swing.JButton _loaddecksheet;
    private javax.swing.JLabel _message;
    private javax.swing.JButton _modifyresult;
    private javax.swing.JButton _pair;
    private javax.swing.JButton _pairlates;
    private javax.swing.JTable _participants;
    private javax.swing.JTextField _promoter;
    private javax.swing.JButton _reflushresults;
    private javax.swing.JButton _repair;
    private javax.swing.JComboBox _roundlist;
    private javax.swing.JButton _save;
    private javax.swing.JTextField _tourname;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}

class xmlFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        if (f.getName().length() > 4) {
            if (f.getName().substring(f.getName().length() - 4).toLowerCase().equals(".xml")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Chocolat's XML Database (*.xml)";
    }

    public xmlFilter() {
    }
}
