package org.example.log;

public class LogImpl implements Log {

    private static String getTime() {
        return System.currentTimeMillis() + "";
    }

    /**
     * Logs to stdout with a timestamp.
     */
    public void log(String s) {
        System.out.println(getTime() + " " + s+" (javac)");
    }

}