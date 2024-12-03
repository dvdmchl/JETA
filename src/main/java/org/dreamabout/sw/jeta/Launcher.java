package org.dreamabout.sw.jeta;

import javax.swing.*;

public class Launcher {

    public static final String O_FILL = "fill";
    public static final String O_PLATFORM = "platform";
    public static final String O_NOSOUND = "nosound";
    public static final String O_NOIMAGE = "noimage";

    public static void main(String[] args) {
        boolean fill = false;
        boolean platform = false;
        boolean nosound = false;
        boolean noimage = false;
        String subor = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-" + O_PLATFORM)) platform = true;
            if (args[i].equals("-" + O_FILL)) fill = true;
            if (args[i].equals("-" + O_NOSOUND)) nosound = true;
            if (args[i].equals("-" + O_NOIMAGE)) noimage = true;
            if (!args[i].startsWith("-")) subor = args[i].trim();
        }

        if (platform) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("e=" + e);
            }
        }

        Core core = new Core(fill, nosound, noimage);

        try {
            core.init();
            if (subor != null) core.otvor(subor);
        } catch (Exception e) {
            core.napis("Chyba typu :" + e, 'E');
        }
    }
}
