package org.dreamabout.sw.jeta;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

class PlayerMid {
    private Sequencer skladba = null;
    public final String FILE;

    public PlayerMid(Core program, String s) {
        FILE = s;

        try {
            skladba = MidiSystem.getSequencer();
            skladba.open();
            skladba.setSequence(new BufferedInputStream(new FileInputStream(new File(s))));

            skladba.addMetaEventListener(
                    new MetaEventListener() {
                        public void meta(MetaMessage e) {
                            if (e.getType() == 47)//vono to skoncilo prehravat
                            {
                                skladba.setMicrosecondPosition(0);
                                start();
                            }
                        }
                    }
            );

        } catch (Exception e) {
            program.napis("Nastala chyba pri nacitani skladby " + s + "\n" + e, 'E');
        }
    }

    public void start() {
        skladba.start();
    }

    public void stop() {
        skladba.stop();
    }

    public void close() {
        skladba.close();
    }
}
