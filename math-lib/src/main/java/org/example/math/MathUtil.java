package org.example.math;

import org.slf4j.*;
import org.example.log.Log;
import org.example.log.LogImpl;

public class MathUtil {

    private static final Log log = new LogImpl();
    private static final Logger slf4jLog = LoggerFactory.getLogger(MathUtil.class);

    /**
     * Divide a by b
     * @param a
     * @param b
     * @return
     */
    public static int divide(int a, int b) {
        slf4jLog.info("Logging like a pro?");
        if(b==0)
            log.log("Division by zero. This will not end well.");
        else
            log.log("Normal division of "+a+" by "+b);
        return a / b;
    }

}