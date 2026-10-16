package com.ypeoomes.Utils.Logging;

public class Log {
    public static void info(String msg, String origin) {
        if (msg.contains("\n")) { // max 1 level of recursion
            String[] messages = msg.split("\n");
            for (String message : messages) {
                info(message, origin);
            }
        } else {
            System.out.println(AnsiColor.RESET + "[" + origin + "] [INFO] " + msg + AnsiColor.RESET);
        }
    }
    public static void warn(String msg, String origin) {
        if (msg.contains("\n")) { // max 1 level of recursion
            String[] messages = msg.split("\n");
            for (String message : messages) {
                warn(message, origin);
            }
        } else {
            System.out.println(AnsiColor.INTENSE_YELLOW + "[" + origin + "] [WARN] " + msg + AnsiColor.RESET);
        }
    }
    public static void error(String msg, String origin) {
        if (msg.contains("\n")) { // max 1 level of recursion
            String[] messages = msg.split("\n");
            for (String message : messages) {
                error(message, origin);
            }
        } else {
            System.out.println(AnsiColor.RED + "[" + origin + "] [ERROR] " + msg + AnsiColor.RESET);
        }
    }
}