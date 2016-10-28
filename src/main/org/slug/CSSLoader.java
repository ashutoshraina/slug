package org.slug;

import java.io.*;

public class CSSLoader {
    private static String EMPTY = "";
    public String getCSS() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream in = classLoader.getResourceAsStream("style.css");
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            for (int c = br.read(); c != -1; c = br.read()) {
                sb.append((char) c);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return EMPTY;
        } catch (IOException e) {
            return EMPTY;
        }

    }
}
