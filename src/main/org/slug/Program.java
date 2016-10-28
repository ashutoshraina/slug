package org.slug;

import static org.slug.MicroserviceFactoryKt.customGenerator;

public class Program {
    public static void main(String args[]) {
        String css = new CSSLoader().loadCSS();
        customGenerator(css);
    }
}
