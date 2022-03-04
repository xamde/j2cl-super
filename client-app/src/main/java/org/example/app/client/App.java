package org.example.app.client;

import org.example.math.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public void onModuleLoad() {
        int c = MathUtil.divide(9,3);
        log.info("Result is "+c);
    }

}
