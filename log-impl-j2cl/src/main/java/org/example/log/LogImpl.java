package org.example.log;

// importing elemental, not available on server-side

import elemental2.dom.Console;

import java.util.Date;

public class LogImpl implements Log {

    private static String getTime() {
        return new Date().getTime() + "";
    }

    /**
     * Logs to stdout with a timestamp.
     */
    public void log(String s) { // Maintain same public api.
        String msg = getTime() + " " + s;
        new Console().log(msg);
    }

}