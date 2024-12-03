package org.dreamabout.sw.jeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Menu extends JPanel implements MouseListener {
    private final int LOC_X=10;
    private final int LOC_Y=410;

    public final int SIZE_X=620;
    public final int SIZE_Y=100;

    private Color color = new Color(255,255,255);

    private final Core program;
    private Moznost[] moznost;
    private String prikaz;

    private String menu[][];

    public Menu(Core c)
    {
        program=c;
        menu = new String[10][9];
    }

    public void init()
    {
        setBounds(LOC_X,LOC_Y,SIZE_X,SIZE_Y);
        setBackground(Color.BLACK);
        setLayout(null);

        moznost = new Moznost[9];

        for(int i=0;i<9;i++)
        {
            moznost[i] = new Moznost("");
            moznost[i].addMouseListener(this);

            switch(i)
            {
                case 0: case 1: case 2: moznost[i].setLocation(10+i*210,25+0); break;
                case 3: case 4: case 5: moznost[i].setLocation(10+(i-3)*210,25+25); break;
                case 6: case 7: case 8: moznost[i].setLocation(10+(i-6)*210,25+50); break;
            }

            add(moznost[i]);
        }

        cls();
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        if(moznost[0].getText().equals(""))return;

        g.setColor(color);
        if(!prikaz.equals(""))g.drawString(prikaz.replaceAll("_"," ")+" +",20,20);
        g.drawRect(0,0,getWidth()-1,getHeight()-1);
    }

    public void cls()
    {
        for(int i=0;i<10;i++)
            for(int j=0;j<9;j++)
                menu[i][j]="";

        for(int i=0;i<9;i++)
            moznost[i].setText("");

        prikaz="";
    }

    public void add(int n,String s)
    {
        for(int i=0;i<9;i++)
            if(menu[n][i].equals(""))
            {
                menu[n][i]=s;
                //System.out.println("menu["+n+"]["+i+"]="+s);
                break;
            }

        repaint();
    }

    public void add(String s1,String s2)
    {
        int n=0;

        for(int i=0;i<9;i++)
            if(menu[0][i].equals(s1))
            {
                n=i;
                break;
            }

        add(n+1,s2);
    }

    public void zobraz(int n)
    {
        for(int i=0;i<9;i++)
            moznost[i].setText(menu[n][i]);

        repaint();
    }

    public void zobraz(String s)
    {
        int n=0;

        for(int i=0;i<9;i++)
            if(menu[0][i].equals(s))
            {
                n=i;
                break;
            }

        zobraz(n+1);
    }

    public String getPrikaz()
    {
        return prikaz.trim();
    }

    public void setMenuColor(Color c)
    {
        color=c;

        for(int i=0;i<moznost.length;i++)
            moznost[i].setButtonColor(c);
    }

    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e)
    {
        String s="";
        Object source=e.getSource();

  /*
   tato vetva je iba prebezpecnost
   keby sa nahodou zasekol program
  */
        if(prikaz.indexOf(" ")>0)
        {
            program.zdrojak.chod();
            return;
        }

        for(int i=0;i<9;i++)
            if(source==moznost[i])
            {
                s=moznost[i].getText();
                break;
            }

        if(s.equals(""))return;

        if(s.equals(Zdrojak.I_OK))
        {
            program.texty.nacitajObrazok(null);
            program.zdrojak.pokracuj();
            return;
        }

        if(s.equals(Zdrojak.I_SPAT))
        {
            program.zdrojak.nastavMenu();
            return;
        }

        if(prikaz.equals(""))
        {
            prikaz=s;
            zobraz(s);
        }
        else
        {
            prikaz+=" "+s;
            program.zdrojak.akcia(prikaz);
        }

        //System.out.println("prikaz "+prikaz);
    }
}
