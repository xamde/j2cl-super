package org.example.app.server;

import org.example.math.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        int c = MathUtil.divide(9,3);
        log.info("Result is "+c);
    }

}
