package org.example.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MathUtil {

    private static final Logger log = LoggerFactory.getLogger(MathUtil.class);

    /**
     * Divide a by b
     * @param a
     * @param b
     * @return
     */
    public static int divide(int a, int b) {
        log.info("Logging like a pro");
        if(b==0)
            log.warn("Division by zero. This will not end well.");
        else
            log.debug("Normal division of "+a+" by "+b);
        return a / b;
    }

}