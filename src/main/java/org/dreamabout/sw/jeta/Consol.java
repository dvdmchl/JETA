package org.dreamabout.sw.jeta;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Consol extends JComponent {
    private static final int LOC_X = 0;
    private static final int LOC_Y = 0;

    private static final int SIZE_X = 420;
    private static final int SIZE_Y = 360;

    private ArrayList<String> text = new ArrayList<>();

    public Consol() {
        setBounds(LOC_X, LOC_Y, SIZE_X, SIZE_Y);
    }

    public void cls() {
        text.clear();
        repaint();
    }

    public void add(String s) {
        text.add(s);
        if (text.size() > 22) text.remove(0);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SIZE_X, SIZE_Y);

        g.setColor(Color.WHITE);
        for (int i = 0; i < 22; i++) {
            if (i == text.size()) break;
            g.drawString(text.get(i), 10, 16 + i * 16);
        }
    }
}