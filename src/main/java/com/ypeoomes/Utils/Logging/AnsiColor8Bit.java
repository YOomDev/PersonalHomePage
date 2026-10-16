package com.ypeoomes.Utils.Logging;

public class AnsiColor8Bit {
    public static String RESET = "\u001B[0m";

    public static String from(int color) {
        return "\u001B[38;5;" + Math.clamp(color, 0, 255) + "m";
    }

    public static String backgroundFrom(int color) {
        return "\u001B[48;5;" + Math.clamp(color, 0, 255) + "m";
    }
}