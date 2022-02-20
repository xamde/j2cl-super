package org.example.math;

import org.example.log.Log;
import org.example.log.LogImpl;

public class MathUtil {

    private static final Log log = new LogImpl();

    /**
     * Divide a by b
     * @param a
     * @param b
     * @return
     */
    public static int divide(int a, int b) {
        if(b==0)
            log.log("Division by zero. This will not end well.");
        else
            log.log("Normal division of "+a+" by "+b);
        return a / b;
    }

}