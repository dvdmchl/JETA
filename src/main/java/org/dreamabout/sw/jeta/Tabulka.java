package org.dreamabout.sw.jeta;

import java.io.Serializable;
import java.util.HashMap;

class Tabulka implements Serializable {
    private HashMap ident;

    public Tabulka() {
        ident = new HashMap();
    }

    public void set(String s1, String s2) {
        if (s2.charAt(0) != ':') ident.put(s1, s2);
        else ident.put(s1, get(s2));

        //System.out.println(s1+" = "+get(s1));
    }

    public String get(String s) {
        Object obj = ident.get(s);

        if (obj == null) {
            set(s, "0");
            return get(s);
        }

        return (String) obj;
    }

    public int size() {
        return ident.size();
    }

    public void reset() {
        ident.clear();
    }
}
