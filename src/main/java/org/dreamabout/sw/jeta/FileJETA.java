package org.dreamabout.sw.jeta;

import java.io.*;
import java.util.ArrayList;

class FileJETA {
    public static final String BIG_JETA = ".JETA";
    public static final String SMALL_JETA = ".jeta";

    private final Core program;

    public FileJETA(Core c) {
        program = c;
    }

    public void zasifruj(String subor) {
        final String NAME = System.currentTimeMillis() + "SCRIPT.JETA";
        ArrayList text = new ArrayList();
        ArrayList sif = new ArrayList();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(subor)));
            String s = null;

            while ((s = br.readLine()) != null)
                text.add(s.trim());

            br.close();

        } catch (Exception e) {
            program.napis("Nastala chyba pri nacitani herneho scriptu\n" + e);
        }

        byte[] key = new byte[256];
        for (int i = 0; i < 256; i++)
            key[i] = (byte) (Math.random() * 64 + 1);

        int pocet = text.size();
        int dlzka = 0;
        int index = 0;

        byte[] buffer = new byte[256];
        for (int i = 0; i < pocet; i++) {
            buffer = ((String) text.get(i)).getBytes();
            dlzka = buffer.length;

            for (int j = 0; j < dlzka; j++) {
                buffer[j] += key[index++];
                if (index == 256) index = 0;
            }

            sif.add(buffer);
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(NAME)));

            oos.writeObject(key);
            oos.writeObject(sif);
            oos.close();

            program.napis("Script " + subor + " bol zasiforvany do suboru " + NAME, 'I');
        } catch (Exception e) {
            program.napis("Nastala chyba pri ulozeni zasifrovanych udajov do suboru " + NAME + "!\n" + e, 'E');
        }
    }

    public ArrayList odsifruj(String subor) {
        byte[] key = new byte[256];
        ArrayList text = new ArrayList();
        ArrayList sif = new ArrayList();

        try {
            ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File(subor)));

            key = (byte[]) oos.readObject();
            sif = (ArrayList) oos.readObject();
            oos.close();

        } catch (Exception e) {
            program.napis("Nastala chyba pri nacitani zasifrovanych udajov zo suboru " + subor + "!\n" + e, 'E');
        }

        int pocet = sif.size();
        int dlzka = 0;
        int index = 0;

        byte[] buffer = new byte[256];
        for (int i = 0; i < pocet; i++) {
            buffer = ((byte[]) sif.get(i));
            dlzka = buffer.length;

            for (int j = 0; j < dlzka; j++) {
                buffer[j] -= key[index++];
                if (index == 256) index = 0;
            }

            text.add(new String(buffer));
        }

        return (ArrayList) (text.clone());
    }
}
