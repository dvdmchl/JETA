package org.dreamabout.sw.jeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Moznost extends JComponent implements MouseListener {
    private Font FONT = new Font("SainSerif",Font.PLAIN,15);
    private Color COLOR = new Color(255,255,255);
    public final int SIZE_X=180;
    public final int SIZE_Y=20;

    private String text;
    private boolean tu;

    public Moznost(String s)
    {
        tu=false;
        addMouseListener(this);
        setSize(SIZE_X,SIZE_Y);
        setText(s);
    }

    public final void setText(String s)
    {
        text=s;
        repaint();
    }

    public final void setFont(Font f)
    {
        FONT = f;
        repaint();
    }

    public final void setButtonColor(Color c)
    {
        COLOR=c;
        repaint();
    }

    public final String getText()
    {
        return text.trim();
    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e)
    {
        tu=true;
        repaint();
    }

    public void mouseExited(MouseEvent e)
    {
        tu=false;
        repaint();
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void paintComponent(Graphics g)
    {
        //g.setColor(Color.BLACK);
        //g.fillRect(0,0,getWidth(),getHeight());

        if(text.equals(""))return;

        g.setColor(COLOR);
        g.setFont(FONT);
        if(tu)g.drawRect(0,0,SIZE_X-1,SIZE_Y-1);
        g.drawString(text.replaceAll("_"," "),(int)((SIZE_X-getFontMetrics(FONT).stringWidth(text))/2),FONT.getSize());
    }
}
