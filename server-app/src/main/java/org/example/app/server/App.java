package org.example.app.server;

import org.example.log.Log;
import org.example.log.LogImpl;
import org.example.math.*;

public class App {

    private static final Log log = new LogImpl();

    public static void main(String[] args) {
        int c = MathUtil.divide(9,3);
        log.log("Result is "+c);
    }

}
