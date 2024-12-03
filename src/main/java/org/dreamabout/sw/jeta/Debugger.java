package org.dreamabout.sw.jeta;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Debugger extends JFrame implements ActionListener {
    public final int LOC_X = 10;
    public final int LOC_Y = 10;

    public final int SIZE_X = 420;
    public final int SIZE_Y = 420;

    private final Core program;

    private final String TITLE = "Debugger - JETA";
    private final String FILE_DBG = ".DBG";
    private final String C_CLS = "CLS";
    private final String C_SAVE = "SAVE";
    private final String C_LOAD = "LOAD";

    private Consol out;
    private JTextField riadok;

    public Debugger(Core c) {
        program = c;
    }

    public void init() {
        setTitle(TITLE);
        setLocation(LOC_X, LOC_Y);
        setSize(SIZE_X, SIZE_Y);

        out = new Consol();

        riadok = new JTextField("");
        riadok.setBounds(0, 360, 420, 32);
        riadok.addActionListener(this);

        getContentPane().setLayout(null);
        getContentPane().add(out);
        getContentPane().add(riadok);

        setResizable(false);
    }

    public void addOUT(String s) {
        out.add(s);
    }

    public void clsOUT() {
        out.cls();
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();

        if (s.trim().equals("")) return;

        out.add(">" + s);
        riadok.setText("");

        if (s.equalsIgnoreCase(C_CLS)) {
            out.cls();
            return;
        }

        if (s.equalsIgnoreCase(C_SAVE)) {
            program.uloz(program.zdrojak.getNazovTextovky() + FILE_DBG);
            return;
        }

        if (s.equalsIgnoreCase(C_LOAD)) {
            program.nacitaj(program.zdrojak.getNazovTextovky() + FILE_DBG);
            return;
        }

        if (s.startsWith(":") && s.indexOf(" ") > 0) {
            program.zdrojak.zmen(s);
            program.zdrojak.chod();
            return;
        }

        if (s.startsWith(":") && s.indexOf(" ") == -1) {
            out.add("PREMENNA " + s + " = " + program.zdrojak.tabulka.get(s));
            return;
        }

        program.zdrojak.chod(s);
    }
}
