package org.example.app.client;

import org.example.log.Log;
import org.example.log.LogImpl;
import org.example.math.MathUtil;

public class App {

    private static final Log log = new LogImpl();

    public void onModuleLoad() {
        int c = MathUtil.divide(9,3);
        log.log("Result is "+c);
    }

}
