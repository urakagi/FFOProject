/*
 * ServerSelectDialog.java
 *
 * Created on 2008/11/19, 20:37
 */

package org.sais.kulisse;

/**
 *
 * @author  Romulus
 */
public class ServerSelectDialog extends javax.swing.JDialog {
  private ServerSelectDialogCallback mCaller;

    public ServerSelectDialog(java.awt.Frame parent, boolean modal, ServerSelectDialogCallback caller) {
        super(parent, modal);
        initComponents();

        mCaller = caller;
}

    interface ServerSelectDialogCallback {
      void onSelected(String server, int port);
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
        _serverlist = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        _connect = new javax.swing.JButton();
        _server = new javax.swing.JTextField();
        _port = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        _serverlist.setFont(new java.awt.Font("MingLiU", 0, 12)); // NOI18N
        _serverlist.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "日本 - irc.tokyo.wide.ad.jp", "日本 - irc.fujisawa.wide.ad.jp", "台灣 - irc.seed.net.tw", "USA - us.ircnet.org", "USA - ircnet.eversible.com" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        _serverlist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _serverlistMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(_serverlist);

        jLabel1.setFont(new java.awt.Font("MingLiU", 0, 12));
        jLabel1.setText("Port");

        _connect.setFont(new java.awt.Font("MingLiU", 0, 12));
        _connect.setText("Connect Now");
        _connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _connectActionPerformed(evt);
            }
        });

        _server.setFont(new java.awt.Font("MingLiU", 0, 12));

        _port.setFont(new java.awt.Font("MingLiU", 0, 12));
        _port.setText("6668");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(_server, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_connect)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_port, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(_port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(_connect))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(_server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void _serverlistMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event__serverlistMouseClicked
  int index = _serverlist.locationToIndex(evt.getPoint());
  if (index >= 0) {
    _server.setText(_serverlist.getModel().getElementAt(index).toString().split(" - ")[1]);
  }
}//GEN-LAST:event__serverlistMouseClicked

private void _connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__connectActionPerformed
  mCaller.onSelected(_server.getText(), Integer.parseInt(_port.getText()));
  this.dispose();
}//GEN-LAST:event__connectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _connect;
    private javax.swing.JTextField _port;
    private javax.swing.JTextField _server;
    private javax.swing.JList _serverlist;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
