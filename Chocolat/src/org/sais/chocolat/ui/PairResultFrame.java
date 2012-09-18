/*
 * PairResultFrame.java
 *
 * Created on 2008/11/28, 21:56
 */
package org.sais.chocolat.ui;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.sais.chocolat.core.Participant;
import org.sais.chocolat.core.Round;
import org.sais.chocolat.core.Table;
import org.sais.chocolat.core.Tournament;
import org.sais.chocolat.irc.IIRCManagerResultSetCallback;

/**
 *
 * @author  Romulus
 */
public class PairResultFrame extends javax.swing.JFrame implements IIRCManagerResultSetCallback {

    private static final int COLUMN_TABLE = 0;
    private static final int COLUMN_P1NAME = 1;
    private static final int COLUMN_P1PTS = 2;
    private static final int COLUMN_P2PTS = 3;
    private static final int COLUMN_P2NAME = 4;
    private static final int COLUMN_P1WIN = 5;
    private static final int COLUMN_DRAW = 6;
    private static final int COLUMN_P2WIN = 7;
    private Round mRound;
    private MainFrame caller;
    private Tournament mTour;
    private ArrayList<Participant> mFreeParticipant = new ArrayList<Participant>();

    /** Creates new form PairResultFrame */
    public PairResultFrame(MainFrame caller_, Round r, Tournament tour) {
        super();
        initComponents();
        initData(r);
        _pairs.setRowHeight(20);
        caller = caller_;
        setIconImage(new ImageIcon(getClass().getResource("/pairing.jpg")).getImage());
        mTour = tour;
    }

    public void invokeUpdate() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    initData(mTour.getCurrentRound());
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(PairResultFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(PairResultFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initData(Round r) {
        mRound = r;
        this.setTitle("Round " + r.sn + " Paring and Result");
        int row = 0;
        for (Table t : r.getTables()) {
            if (t == null) {
                JOptionPane.showMessageDialog(null, "Table is null!");
            }
            Participant p1 = t.getP1();
            Participant p2 = t.getP2();
            _pairs.setValueAt(p1.name, row, COLUMN_P1NAME);
            _pairs.setValueAt(p1.getPoint(), row, COLUMN_P1PTS);

            if (p1.games.get(t) != null) {
                p1.games.remove(t);
            }

            if (p2 == null) {
                _pairs.setValueAt("*BYE*", row, COLUMN_P2NAME);
            } else {
                if (p2.games.get(t) != null) {
                    p2.games.remove(t);
                }
                _pairs.setValueAt(p2.name, row, COLUMN_P2NAME);
                _pairs.setValueAt(p2.getPoint(), row, COLUMN_P2PTS);
            }

            switch (t.getWinner()) {
                case Table.PLAYER1:
                    _pairs.setValueAt(true, row, COLUMN_P1WIN);
                    _pairs.setValueAt(null, row, COLUMN_DRAW);
                    _pairs.setValueAt(null, row, COLUMN_P2WIN);
                    break;
                case Table.PLAYER2:
                    _pairs.setValueAt(null, row, COLUMN_P1WIN);
                    _pairs.setValueAt(null, row, COLUMN_DRAW);
                    _pairs.setValueAt(true, row, COLUMN_P2WIN);
                    break;
                case Table.PROXY_DRAW:
                    _pairs.setValueAt(null, row, COLUMN_P1WIN);
                    _pairs.setValueAt(true, row, COLUMN_DRAW);
                    _pairs.setValueAt(null, row, COLUMN_P2WIN);
                    break;
            }

            _pairs.setValueAt(row + 1, row, COLUMN_TABLE);

            _force1.removeAllItems();
            _force2.removeAllItems();

            ++row;
        }
    }

    public void invokeSave() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    save();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(PairResultFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(PairResultFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void save() {
        //Check
        for (int i = 0; i < 64; ++i) {
            if (_pairs.getValueAt(i, COLUMN_P1NAME) == null) {
                break;
            }
            int count = 0;
            Boolean b;

            b = (Boolean) _pairs.getValueAt(i, COLUMN_P1WIN);
            if (b != null) {
                if (b) {
                    count++;
                }
            }

            b = (Boolean) _pairs.getValueAt(i, COLUMN_P2WIN);
            if (b != null) {
                if (b) {
                    count++;
                }
            }

            b = (Boolean) _pairs.getValueAt(i, COLUMN_DRAW);
            if (b != null) {
                if (b) {
                    count++;
                }
            }

            if (count != 1) {
                JOptionPane.showMessageDialog(this, "Incorrect Results (You must check won for a participant with bye)");
                return;
            }
        }

        //Save
        for (int i = 0; i < mRound.getTables().size(); ++i) {
            if (_pairs.getValueAt(i, COLUMN_P1WIN) != null) {
                if ((Boolean) _pairs.getValueAt(i, COLUMN_P1WIN)) {
                    mRound.getTables().get(i).setWinner(Table.PLAYER1);
                }
            }

            if (_pairs.getValueAt(i, COLUMN_P2WIN) != null) {
                if ((Boolean) _pairs.getValueAt(i, COLUMN_P2WIN)) {
                    mRound.getTables().get(i).setWinner(Table.PLAYER2);
                }
            }

            if (_pairs.getValueAt(i, COLUMN_DRAW) != null) {
                if ((Boolean) _pairs.getValueAt(i, COLUMN_DRAW)) {
                    mRound.getTables().get(i).setWinner(Table.PROXY_DRAW);
                }
            }

        }
        caller.update();
        this.dispose();
    }

    private void forcePair() {
        int tableno = _pairs.getSelectedRow();
        if (tableno < 0) {
            return;
        }

        Table table = mRound.getTables().get(tableno);
        if (table != null) {
            JOptionPane.showMessageDialog(null, "Table is not freed.");
            return;
        }

        Participant p1 = (Participant) _force1.getSelectedItem();
        Participant p2 = (Participant) _force2.getSelectedItem();

        mFreeParticipant.remove(p1);
        if (!p1.equals(p2)) {
            mFreeParticipant.remove(p2);
        }

        if (p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "This will pair a BYE to this participant.");
        }

        _pairs.setValueAt(p1.name, tableno, COLUMN_P1NAME);
        _pairs.setValueAt(p1.getPoint(), tableno, COLUMN_P1PTS);

        if (p2.equals(p1)) {
            _pairs.setValueAt("*BYE*", tableno, COLUMN_P2NAME);
            p2 = null;
        } else {
            _pairs.setValueAt(p2.name, tableno, COLUMN_P2NAME);
            _pairs.setValueAt(p2.getPoint(), tableno, COLUMN_P2PTS);
        }

        mRound.getTables().set(tableno, new Table(tableno, p1, p2));

        _force1.removeItem(p1);
        _force2.removeItem(p1);

        if (p2 != null) {
            _force1.removeItem(p2);
            _force2.removeItem(p2);
        }

        _pairs.setRowSelectionInterval(_pairs.getSelectedRow() + 1, _pairs.getSelectedRow() + 1);
        _force1.requestFocus();

    }

    private void freeAll() {
        if (JOptionPane.showConfirmDialog(this, "Do you really want to free all tables?") != JOptionPane.YES_OPTION) {
            return;
        }

        for (int i = 0; i < mRound.getTables().size(); ++i) {
            freeTable(i);
        }
    }

    private void freeTable(int tableno) {
        if (tableno < 0) {
            return;
        }

        Table table = mRound.getTables().get(tableno);
        if (table == null) {
            JOptionPane.showMessageDialog(null, "Table is already freed.");
            return;
        }

        mFreeParticipant.add(table.getP1());

        Participant p = table.getP2();
        if (p != null) {
            mFreeParticipant.add(p);
        }

        _force1.removeAllItems();
        _force2.removeAllItems();
        Collections.sort(mFreeParticipant, new Comparator() {

            public int compare(Object o1, Object o2) {
                return ((Participant) o1).name.compareTo(((Participant) o2).name);
            }
        });
        for (Participant pa : mFreeParticipant) {
            _force1.addItem(pa);
            _force2.addItem(pa);
        }

        mRound.getTables().set(tableno, null);
        _pairs.setValueAt(null, tableno, COLUMN_P1NAME);
        _pairs.setValueAt(null, tableno, COLUMN_P1PTS);
        _pairs.setValueAt(null, tableno, COLUMN_P2PTS);
        _pairs.setValueAt(null, tableno, COLUMN_P2NAME);

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
    _pairs = new javax.swing.JTable();
    _save = new javax.swing.JButton();
    _freetable = new javax.swing.JButton();
    _forcepair = new javax.swing.JButton();
    _force1 = new javax.swing.JComboBox();
    _force2 = new javax.swing.JComboBox();
    _freeall = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("FFOTour Result");

    _pairs.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    _pairs.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 14)); // NOI18N
    _pairs.setModel(new javax.swing.table.DefaultTableModel(
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
        {null, null, null, null, null, null, null, null}
      },
      new String [] {
        "Table", "Player1", "pts", "pts", "Player2", "P1 Won", "Draw", "P2 Won"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
      };
      boolean[] canEdit = new boolean [] {
        true, false, false, false, false, true, true, true
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    _pairs.getTableHeader().setReorderingAllowed(false);
    jScrollPane1.setViewportView(_pairs);
    _pairs.getColumnModel().getColumn(0).setPreferredWidth(5);
    _pairs.getColumnModel().getColumn(1).setPreferredWidth(50);
    _pairs.getColumnModel().getColumn(2).setPreferredWidth(10);
    _pairs.getColumnModel().getColumn(3).setPreferredWidth(10);
    _pairs.getColumnModel().getColumn(4).setPreferredWidth(50);
    _pairs.getColumnModel().getColumn(5).setPreferredWidth(10);
    _pairs.getColumnModel().getColumn(6).setPreferredWidth(10);
    _pairs.getColumnModel().getColumn(7).setPreferredWidth(10);

    _save.setText("SAVE");
    _save.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _saveActionPerformed(evt);
      }
    });

    _freetable.setText("Free Table");
    _freetable.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _freetableActionPerformed(evt);
      }
    });

    _forcepair.setText("Force Pair");
    _forcepair.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _forcepairActionPerformed(evt);
      }
    });

    _force1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    _force1.setEditor(null);
    _force1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _force1ActionPerformed(evt);
      }
    });
    _force1.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        _force1KeyPressed(evt);
      }
    });

    _force2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    _force2.setEditor(null);
    _force2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _force2ActionPerformed(evt);
      }
    });
    _force2.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        _force2KeyPressed(evt);
      }
    });

    _freeall.setText("Free ALL Tables");
    _freeall.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        _freeallActionPerformed(evt);
      }
    });

    jLabel1.setText("VS");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(_freetable)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(_freeall)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 191, Short.MAX_VALUE)
            .addComponent(_save)
            .addGap(11, 11, 11))
          .addGroup(layout.createSequentialGroup()
            .addComponent(_forcepair)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(_force1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(_force2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(_freetable)
          .addComponent(_save)
          .addComponent(_freeall))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(_forcepair)
          .addComponent(_force1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1)
          .addComponent(_force2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

private void _saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__saveActionPerformed
    save();
}//GEN-LAST:event__saveActionPerformed

private void _force2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__force2ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event__force2ActionPerformed

private void _force1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__force1ActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event__force1ActionPerformed

private void _freetableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__freetableActionPerformed
    freeTable(_pairs.getSelectedRow());
}//GEN-LAST:event__freetableActionPerformed

private void _forcepairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__forcepairActionPerformed
    forcePair();
}//GEN-LAST:event__forcepairActionPerformed

private void _freeallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__freeallActionPerformed
    freeAll();
}//GEN-LAST:event__freeallActionPerformed

private void _force1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__force1KeyPressed

    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        forcePair();
    }
}//GEN-LAST:event__force1KeyPressed

private void _force2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event__force2KeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        forcePair();
    }
}//GEN-LAST:event__force2KeyPressed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox _force1;
  private javax.swing.JComboBox _force2;
  private javax.swing.JButton _forcepair;
  private javax.swing.JButton _freeall;
  private javax.swing.JButton _freetable;
  private javax.swing.JTable _pairs;
  private javax.swing.JButton _save;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JScrollPane jScrollPane1;
  // End of variables declaration//GEN-END:variables
}
