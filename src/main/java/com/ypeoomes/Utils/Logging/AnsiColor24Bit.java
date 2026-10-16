package com.ypeoomes.Utils.Logging;

public class AnsiColor24Bit {
    public static String RESET = "\u001B[0m";

    public static String from(int red, int green, int blue) {
        return "\u001B[38;2;" + Math.clamp(red, 0, 255) + ";" + Math.clamp(green, 0, 255) + ";" + Math.clamp(blue, 0, 255) + "m";
    }

    public static String backgroundFrom(int red, int green, int blue) {
        return "\u001B[48;2;" + Math.clamp(red, 0, 255) + ";" + Math.clamp(green, 0, 255) + ";" + Math.clamp(blue, 0, 255) + "m";
    }
}