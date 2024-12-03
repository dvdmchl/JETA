package org.dreamabout.sw.jeta;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Texty extends JComponent {
    private final Font FONT = new Font("SainSerif",Font.BOLD,16);
    private final String NAME_SHOW_IMAGE = "SHOW.PNG";
    private final Image image;

    private final int LOC_X=10;
    private final int LOC_Y=10;

    public final int SIZE_X=620;
    public final int SIZE_Y=400;

    private Core program;
    private ArrayList riadok;
    private ArrayList farba;
    private Image obrazok;

    public Texty(Core c)
    {
        program=c;
        image = program.getImage(NAME_SHOW_IMAGE);
        riadok = new ArrayList();
        farba = new ArrayList();
    }

    public void init()
    {
        setBounds(LOC_X,LOC_Y,SIZE_X,SIZE_Y);
        //add("�iadna textovka nebola na��tan�..");
    }

    public void cls()
    {
        riadok.clear();
        farba.clear();
        nacitajObrazok(null);
    }

    public void add(String s1)
    {
        add(s1,"255 255 255");
    }

    public void add(String s1,String s2)
    {
        if(riadok.size()==20)riadok.remove(0);
        if(farba.size()==20)farba.remove(0);

        nacitajObrazok(null);
        riadok.add(s1);
        farba.add(s2);

        repaint();
    }

    public void nacitajObrazok(String s)
    {
        if(s==null)
        {
            obrazok=null;
            return;
        }

        try{
            obrazok = new ImageIcon(program.zdrojak.getAdresarSuboru()+s).getImage();
        }catch(Exception e)
        {
            program.napis("Obrazok "+s+" sa nenasiel !",'E');
        }

        repaint();
    }

    public void paintComponent(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,SIZE_X,SIZE_Y);

        if(!program.zdrojak.isNacitanaTextovka())
        {
            g.drawImage(image,0,0,this);
            obrazok=null;
            return;
        }

        if(obrazok!=null)
        {
            g.drawImage(obrazok,(int)((getWidth()-obrazok.getWidth(this))/2),
                    (int)((getHeight()-obrazok.getHeight(this))/2),this);
            return;
        }

        g.setFont(FONT);

        for(int i=0;i<20;i++)
        {
            if(i<riadok.size())
            {
                g.setColor(program.zdrojak.getCreateColor((String)farba.get(i)));
                g.drawString((String)riadok.get(i),10,16+i*20);
            }
        }
    }
}
