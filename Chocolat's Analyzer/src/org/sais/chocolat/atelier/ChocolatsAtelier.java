/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChocolatsAtelier.java
 *
 * Created on 2009/11/23, 下午 04:34:17
 */
package org.sais.chocolat.atelier;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.sais.chocolat.analyzer.AnalyzeResult;
import org.sais.chocolat.analyzer.data.CADeck;
import org.sais.chocolat.analyzer.ReadInnerFile;
import org.sais.chocolat.analyzer.Replay;

/**
 *
 * @author Romulus
 */
public class ChocolatsAtelier extends javax.swing.JFrame {

    private AnalyzeResult mResult;

    public ChocolatsAtelier(AnalyzeResult result) {
        mResult = result;
        setIconImage(new ImageIcon(ReadInnerFile.getURL("chocolat3.jpg")).getImage());
        initComponents();
    }

    class CCanvas extends JPanel {

        public CCanvas() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            long start = Long.MAX_VALUE;
            long end = Long.MIN_VALUE;
            for (final Replay r : mResult.replays) {
                long t = CADeck.convertToCalendar(r.file.getName()).getTimeInMillis();
                if (t < start) {
                    start = t;
                } else if (t > end) {
                    end = t;
                }
            }
            double pxPerDay = getWidth() / ((end - start) / 86400000.);
            double pxPerSec = getHeight() / 86400.;
            SimpleDateFormat f = new SimpleDateFormat("yy-MM-dd");
            _start.setText(f.format(new Date(start)));
            _end.setText(f.format(new Date(end)));
            _half.setText(f.format(new Date(start + (end - start) / 2)));

            int x;
            int y;
            for (final Replay r : mResult.replays) {
                Calendar c = CADeck.convertToCalendar(r.file.getName());
                if (c == null) {
                    c = Calendar.getInstance();
                    c.setTimeInMillis(r.file.lastModified());
                }

                x = (int) ((c.getTimeInMillis() - start) / 86400000 * pxPerDay);
                y = (int) ((c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND)) * pxPerSec);

                Color color;
                if (r.result == null) {
                    color = new Color(0xFFFFE0);
                } else if (r.result.endsWith("勝")) {
                    color = Color.GREEN;
                } else if (r.result.endsWith("敗")) {
                    color = Color.RED;
                } else {
                    color = new Color(0xFFFFE0);
                }
                g.setColor(color);
                g.fillRect(x, y, 2, 2);
            }
        }
    }

    class PCanvas extends JPanel {

        double pxPerFrame;
        double pxPerGame;
        long start;
        long end;
        int[] frames;
        int frameWidth;
        int frameIncrease;

        DecimalFormat df = new DecimalFormat("0.##");

        public PCanvas() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            start = Long.MAX_VALUE;
            end = Long.MIN_VALUE;
            for (final Replay r : mResult.replays) {
                long t = CADeck.convertToCalendar(r.file.getName()).getTimeInMillis();
                if (t < start) {
                    start = t;
                } else if (t > end) {
                    end = t;
                }
            }
            SimpleDateFormat f = new SimpleDateFormat("yy-MM-dd");
            _start1.setText(f.format(new Date(start)));
            _end1.setText(f.format(new Date(end)));

            // Fixed Frames
            int totalFrames = 80;
            int totalDays = (int) ((end - start) / 3600000 / 24);
            frameIncrease = totalDays / (totalFrames + 1) + 1;
            if (frameIncrease < 1) {
                frameIncrease = 1;
            }
            frameWidth = (int) (frameIncrease * 2);
            totalFrames = totalDays / frameIncrease - 1;
            int[] count = new int[totalDays + 1];
            pxPerFrame = 1.0 * getWidth() / totalFrames;
            _powermessage.setText(frameWidth + " days per frame with " + frameIncrease + " days increment");
            int maxHeight = getHeight() - 20;

            for (final Replay r : mResult.replays) {
                Calendar c = CADeck.convertToCalendar(r.file.getName());
                if (c == null) {
                    c = Calendar.getInstance();
                    c.setTimeInMillis(r.file.lastModified());
                }

                int day = (int) ((c.getTimeInMillis() - start) / 3600000 / 24);
                count[day]++;
            }

            frames = new int[totalFrames];
            for (int i = 0; i < totalFrames; ++i) {
                for (int k = i * frameIncrease; k < i * frameIncrease + frameWidth; ++k) {
                    frames[i] += count[k];
                }
            }

            int max = Integer.MIN_VALUE;
            for (int i = 0; i < frames.length; ++i) {
                if (frames[i] > max) {
                    max = frames[i];
                }
            }
            pxPerGame = 1.0 * maxHeight / max;
            jLabel16.setText(df.format(max / frameWidth));
            jLabel13.setText(df.format(max / 2 / frameWidth));

            for (int i = 0; i < frames.length; ++i) {
                if (i == 0) {
                    g.setColor(Color.CYAN);
                } else {
                    if (frames[i] > frames[i - 1]) {
                        g.setColor(Color.GREEN);
                    } else if (frames[i] < frames[i - 1]) {
                        g.setColor(Color.RED);
                    } else {
                        g.setColor(Color.CYAN);
                    }
                }
                int y = getHeight() - (int) (frames[i] * pxPerGame);
                g.fillRect((int) (i * pxPerFrame) + 1, y, (int) pxPerFrame - 1, getHeight() - y);
                g.setColor(Color.WHITE);
            }
        }

        public void showFrame(MouseEvent evt) {
            if (frames == null) {
                return;
            }
            int frame = (int) (evt.getX() / pxPerFrame);

            Date sd = new Date(start + ((long) frame * frameIncrease) * 24 * 3600000);
            Date ed = new Date(start + ((long) frame * frameIncrease + frameWidth - 1) * 24 * 3600000);

            SimpleDateFormat f = new SimpleDateFormat("yy/MM/dd (EEE)");
            _powercursor.setText(f.format(sd) + "-" + f.format(ed) + ", " + df.format(1.0 * frames[frame] / frameWidth) + " games per day.");
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new CCanvas();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        _start = new javax.swing.JLabel();
        _end = new javax.swing.JLabel();
        _half = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        _powerpanel = new javax.swing.JPanel();
        jPanel4 = new PCanvas();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        _start1 = new javax.swing.JLabel();
        _end1 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        _powermessage = new javax.swing.JLabel();
        _powercursor = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("ショコラのアトリエ");

        jTabbedPane1.setFont(new java.awt.Font("ＭＳ Ｐゴシック", 0, 12));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 657, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 447, Short.MAX_VALUE)
        );

        jLabel2.setText("24");

        jLabel3.setText("12");

        jLabel4.setText("06");

        jLabel5.setText("18");

        _start.setText("jLabel6");

        _end.setText("jLabel7");

        _half.setText("jLabel8");

        jLabel6.setText("00");

        jLabel7.setText("12");

        jLabel8.setText("06");

        jLabel9.setText("00");

        jLabel10.setText("18");

        jLabel11.setText("24");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(_start)
                        .addGap(250, 250, 250)
                        .addComponent(_half)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 302, Short.MAX_VALUE)
                        .addComponent(_end))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel11)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addGap(101, 101, 101)
                        .addComponent(jLabel7)
                        .addGap(97, 97, 97)
                        .addComponent(jLabel10)
                        .addGap(87, 87, 87)
                        .addComponent(jLabel11))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4)
                            .addGap(101, 101, 101)
                            .addComponent(jLabel3)
                            .addGap(97, 97, 97)
                            .addComponent(jLabel5)
                            .addGap(87, 87, 87)
                            .addComponent(jLabel2))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_start)
                    .addComponent(_end)
                    .addComponent(_half)))
        );

        jTabbedPane1.addTab("リプレイ図", jPanel2);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel4MouseMoved(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 665, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 435, Short.MAX_VALUE)
        );

        jLabel12.setText("0");

        jLabel13.setText("25");

        _start1.setText("jLabel6");

        _end1.setText("jLabel7");

        jLabel16.setText("50");

        _powermessage.setText("jLabel1");

        _powercursor.setText("jLabel1");

        javax.swing.GroupLayout _powerpanelLayout = new javax.swing.GroupLayout(_powerpanel);
        _powerpanel.setLayout(_powerpanelLayout);
        _powerpanelLayout.setHorizontalGroup(
            _powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_powerpanelLayout.createSequentialGroup()
                .addGroup(_powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_powerpanelLayout.createSequentialGroup()
                        .addGroup(_powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addGroup(_powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel13)
                                .addComponent(jLabel12)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(_powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(_powerpanelLayout.createSequentialGroup()
                                .addComponent(_start1)
                                .addGap(133, 133, 133)
                                .addComponent(_powercursor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 427, Short.MAX_VALUE)
                                .addComponent(_end1))))
                    .addGroup(_powerpanelLayout.createSequentialGroup()
                        .addGap(224, 224, 224)
                        .addComponent(_powermessage)))
                .addContainerGap())
        );
        _powerpanelLayout.setVerticalGroup(
            _powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_powerpanelLayout.createSequentialGroup()
                .addComponent(_powermessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(_powerpanelLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                        .addComponent(jLabel13)
                        .addGap(177, 177, 177)
                        .addComponent(jLabel12))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_powerpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_end1)
                    .addComponent(_start1)
                    .addComponent(_powercursor))
                .addContainerGap())
        );

        jTabbedPane1.addTab("勢い", _powerpanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 505, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel4MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseMoved
        ((PCanvas) jPanel4).showFrame(evt);
    }//GEN-LAST:event_jPanel4MouseMoved
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel _end;
    private javax.swing.JLabel _end1;
    private javax.swing.JLabel _half;
    private javax.swing.JLabel _powercursor;
    private javax.swing.JLabel _powermessage;
    private javax.swing.JPanel _powerpanel;
    private javax.swing.JLabel _start;
    private javax.swing.JLabel _start1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
