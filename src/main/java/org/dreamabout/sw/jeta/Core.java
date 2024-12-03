package org.dreamabout.sw.jeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class Core extends JFrame implements ActionListener, KeyListener {
    public final String TITLE = "JETA ver 1.8.1";

    public final String INFO =
            "Dušan Ďurech ( Oroborus )\n" +
                    "http://oroborus.wz.cz\n" +
                    "Dusan.D@Centrum.Sk";


    private final Image IKONA = getImage("IKONA.PNG");
    private final ImageIcon oroborusImage = getImageIcon("oroborus.jpg");

    private final boolean SCREEN;
    public final boolean NO_SOUND;
    public final boolean NO_IMAGE;

    private final int MAX_X = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private final int MAX_Y = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    private final int SIZE_X = 640;
    private final int SIZE_Y1 = 430;
    private final int SIZE_Y2 = 580;

    private JMenuBar lista;
    private JMenu menu1;
    private JMenuItem moznost1;
    private JMenuItem moznost2;
    private JMenuItem moznost3;
    private JMenuItem moznost4;
    private JMenuItem moznost5;
    private JMenuItem moznost6;
    private JMenuItem moznost7;
    private JMenuItem moznost8;
    private JMenuItem moznost9;

    private JFileChooser vyberSubor;
    private JPanel panel;
    public Texty texty;
    public Menu menu;

    public Zdrojak zdrojak;
    public FileJETA fileJETA;
    public Debugger debugger;

    public Core(boolean b1, boolean b2, boolean b3) {
        SCREEN = b1;
        NO_SOUND = b2;
        NO_IMAGE = b3;
    }

    public void init() {
        vyberSubor = new JFileChooser();
        vyberSubor.setDialogTitle("Vyber s�bor");
        vyberSubor.setCurrentDirectory(new File("."));

        texty = new Texty(this);
        texty.init();

        menu = new Menu(this);
        menu.init();

        debugger = new Debugger(this);
        debugger.init();

        panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(null);
        panel.add(texty);
        panel.add(menu);

        zdrojak = new Zdrojak(this);
        fileJETA = new FileJETA(this);

        if (!SCREEN) panel.setBounds(0, 0, SIZE_X, SIZE_Y2);
        else panel.setBounds((MAX_X - SIZE_X) / 2, (MAX_Y - SIZE_Y2) / 2, SIZE_X, SIZE_Y2);

        moznost1 = new JMenuItem("Otvor..");
        moznost1.addActionListener(this);
        moznost1.setMnemonic('o');

        moznost2 = new JMenuItem("Restartuj");
        moznost2.addActionListener(this);
        moznost2.setMnemonic('r');

        moznost3 = new JMenuItem("Ulož");
        moznost3.addActionListener(this);
        moznost3.setMnemonic('u');

        moznost4 = new JMenuItem("Načítaj");
        moznost4.addActionListener(this);
        moznost4.setMnemonic('n');

        moznost5 = new JMenuItem("Debugger..");
        moznost5.addActionListener(this);
        moznost5.setMnemonic('b');

        moznost6 = new JMenuItem("Zašifruj subor..");
        moznost6.addActionListener(this);
        moznost6.setMnemonic('f');

        moznost7 = new JMenuItem("Manuál..");
        moznost7.addActionListener(this);
        moznost7.setMnemonic('m');

        moznost8 = new JMenuItem("Autor..");
        moznost8.addActionListener(this);
        moznost8.setMnemonic('o');

        moznost9 = new JMenuItem("Koniec");
        moznost9.addActionListener(this);
        moznost9.setMnemonic('k');

        menu1 = new JMenu("Program");
        menu1.setMnemonic('p');

        menu1.add(moznost1);
        menu1.addSeparator();
        menu1.add(moznost2);
        menu1.add(moznost3);
        menu1.add(moznost4);
        menu1.addSeparator();
        menu1.add(moznost5);
        menu1.add(moznost6);
        menu1.addSeparator();
        menu1.add(moznost7);
        menu1.add(moznost8);
        menu1.addSeparator();
        menu1.add(moznost9);

        lista = new JMenuBar();
        lista.add(menu1);

        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(IKONA);
        setJMenuBar(lista);

        getContentPane().setBackground(Color.BLACK);
        getContentPane().setLayout(null);
        getContentPane().add(panel);

        setResizable(false);
        addKeyListener(this);
        if (SCREEN) setUndecorated(true);

        setUvod();
    }

    public void setVelkost(int x, int y) {
        if (SCREEN) {
            setVisible(false);
            setLocation(0, 0);
            setSize(MAX_X, MAX_Y);
            setVisible(true);
            return;
        }

        setVisible(false);
        setLocation((MAX_X - x) / 2, (MAX_Y - y) / 2);
        setSize(x, y);
        setVisible(true);
    }

    public void setUvod() {
        texty.cls();
        menu.cls();
        moznost2.setEnabled(false);
        moznost3.setEnabled(false);
        moznost4.setEnabled(false);
        moznost5.setEnabled(false);
        setVelkost(SIZE_X, SIZE_Y1);
    }

    public final ImageIcon getImageIcon(String s) {
        return new ImageIcon( JetaUtil.getResource(s));
    }

    public final Image getImage(String s) {
        return getImageIcon(s).getImage();
    }

    public final void napis(String s) {
        napis(s, 'P');
    }

    public final void napis(String s, char mode) {
        final String FUCK_TITLE = "Fuck error - don't panic !";
        final int n;

        switch (mode) {
            case 'I':
                n = JOptionPane.INFORMATION_MESSAGE;
                break;
            case 'W':
                n = JOptionPane.WARNING_MESSAGE;
                break;
            case 'E':
                n = JOptionPane.ERROR_MESSAGE;
                break;
            case 'P':
                n = JOptionPane.PLAIN_MESSAGE;
                break;
            default:
                napis("Nastala chyba pri zobrazovani okna !\n" +
                        "Zle nastavenie parametra (mode=" + mode + ")", 'E');
                return;
        }

        JOptionPane.showMessageDialog(this, s, (mode == 'E' ? FUCK_TITLE : TITLE), n);
    }

    public final String otazka(String q, String[] f) {
        JOptionPane jop = new JOptionPane(q, JOptionPane.QUESTION_MESSAGE, 0, null, f);
        JDialog jdl = jop.createDialog(this, TITLE);

        jdl.setModal(true);
        jdl.setVisible(true);

        return (String) jop.getValue();
    }

    private void otvor() {
        int hodnota = vyberSubor.showOpenDialog(this);
        if (hodnota == JFileChooser.APPROVE_OPTION) otvor(vyberSubor.getSelectedFile().getPath());
    }

    public void otvor(String s) {
        if (!zdrojak.nacitaj(s)) {
            setUvod();
            return;
        }

        moznost2.setEnabled(true);
        moznost3.setEnabled(true);
        moznost4.setEnabled(true);
        moznost5.setEnabled(!zdrojak.isSifrovany());

        setVelkost(SIZE_X, SIZE_Y2);
    }

    private void reset() {
        zdrojak.nacitaj();
    }

    public void uloz() {
        uloz(zdrojak.getNazovTextovky() + ".sav");
    }

    public void uloz(String subor) {
        try {
            File file = new File(subor);
            FileOutputStream dos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(dos);

            oos.writeObject(zdrojak.getMiesto());
            oos.writeObject(zdrojak.tabulka);
            oos.close();

            napis("Pozícia bola uložena do súboru : " + subor, 'I');

        } catch (Exception e) {
            napis("Nastala chyba pri uložení pozície !\n" + e.toString(), 'E');
        }
    }

    public void nacitaj() {
        nacitaj(zdrojak.getNazovTextovky() + ".sav");
    }

    public void nacitaj(String subor) {
        if (!(new File(subor)).exists()) {
            napis("Subor " + subor + " sa nenasiel !", 'W');
            return;
        }

        String s = "";

        try {
            File file = new File(subor);
            FileInputStream dis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(dis);

            s = (String) ois.readObject();
            zdrojak.tabulka = (Tabulka) ois.readObject();
            ois.close();

            napis("Pozícia bola načítaná zo súboru : " + subor, 'I');
        } catch (Exception e) {
            napis("Nastala chyba pri načítaní pozície !\n" + e.toString(), 'E');
        }

        zdrojak.chod(s);
    }

    private void spustDebuger() {
        debugger.setVisible(!debugger.isVisible());
        debugger.clsOUT();
    }

    private void zasifruj() {
        int hodnota = vyberSubor.showOpenDialog(this);
        if (hodnota == JFileChooser.APPROVE_OPTION) fileJETA.zasifruj(vyberSubor.getSelectedFile().getPath());
    }

    private void manual() {
        try {
            Runtime.getRuntime().exec("cmd.exe /c manual.TXT");
        } catch (Exception e) {
            napis("Nastala chyba pri zobrazení manuálu.\n" + e, 'E');
        }
    }

    private void oPrograme() {
        JOptionPane.showMessageDialog(this, INFO, TITLE, JOptionPane.PLAIN_MESSAGE, oroborusImage);
    }

    public void exit() {
        dispose();
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == moznost1) otvor();
        if (source == moznost2) reset();
        if (source == moznost3) uloz();
        if (source == moznost4) nacitaj();
        if (source == moznost5) spustDebuger();
        if (source == moznost6) zasifruj();
        if (source == moznost7) manual();
        if (source == moznost8) oPrograme();
        if (source == moznost9) exit();
    }

    public String toString() {
        return "program=\"" + TITLE + "\" cas=\"" + System.currentTimeMillis() +
                "\" java version=\"" + System.getProperty("java.version") + "\"";
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 32) lista.setVisible(!lista.isVisible());
    }

}
