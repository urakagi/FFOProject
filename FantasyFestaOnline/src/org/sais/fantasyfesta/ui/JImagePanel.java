/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author user
 */
public class JImagePanel extends JPanel {

    private ImageIcon image;
    private float alpha = 1.0f;


    public JImagePanel()
    {
        super();
     //   this.setBackground(Color.BLUE);
        
    }

    public JImagePanel(ImageIcon image) {
        this();
        this.image = image;

    }
        public JImagePanel(ImageIcon image,float alpha) {
        this();
        this.image = image;
        this.alpha = alpha;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

       if(image!=null)
       {

           Image img = image.getImage();

        Graphics g1 =g.create();
        java.awt.AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        ((Graphics2D)g).setComposite(ac);
        ((Graphics2D)g).drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
        super.paintChildren(g1);

       }

    }

    private void repaintComps(Container con)
    {
        for(Component c:con.getComponents())
        {
            c.repaint();
            if(c instanceof Container)
            {
                repaintComps((Container)c);
            }
        }
    }

    public ImageIcon getImage() {
        return image;
    }

    public void setImage(ImageIcon image) {
        this.image = image;
    }

    public void setImage(String name)
 {
        this.image = new ImageIcon();

        if (name != null && !name.equals("")) {
            File f = new File(name);
            if (f.exists()) {
                this.image = new ImageIcon(name);
            } else if (name.startsWith("http://")) {
                try {

                    if (FTool.isURLAvailable(name)) {
                        URL url = new URL(name);
                        this.image = new ImageIcon(url);
                    }

                } catch (MalformedURLException ex) {
                    Logger.getLogger(JImagePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }




    }
    public void setImage(URL url)
    {
        this.image = new ImageIcon(url);
    }

    public void loadImageResource(String name)
    {
        this.image = new ImageIcon(getClass().getClassLoader().getResource(name) );
    }

    public  float getAlpha() {
        return alpha;
    }

    public  void setAlpha(float alpha) {
        this.alpha = alpha;
    }

}
