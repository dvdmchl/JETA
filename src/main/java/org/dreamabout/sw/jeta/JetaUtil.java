package org.dreamabout.sw.jeta;

import lombok.experimental.UtilityClass;

import java.net.URL;

@UtilityClass
public class JetaUtil {

    public static URL getResource(String name) {
        return JetaUtil.class.getClassLoader().getResource(name);
    }
}
