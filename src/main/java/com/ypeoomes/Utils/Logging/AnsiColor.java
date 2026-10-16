package com.ypeoomes.Utils.Logging;

public enum AnsiColor {
    // Reset any text changes
    RESET ("\u001B[0m"),

    // Regular colors
    BLACK ("\u001B[30m"),
    RED   ("\u001B[31m"),
    GREEN ("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE  ("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN  ("\u001B[36m"),
    WHITE ("\u001B[37m"),

    // Bold colors
    BOLD_BLACK ("\u001B[1;30m"),
    BOLD_RED   ("\u001B[1;31m"),
    BOLD_GREEN ("\u001B[1;32m"),
    BOLD_YELLOW("\u001B[1;33m"),
    BOLD_BLUE  ("\u001B[1;34m"),
    BOLD_PURPLE("\u001B[1;35m"),
    BOLD_CYAN  ("\u001B[1;36m"),
    BOLD_WHITE ("\u001B[1;37m"),

    // Underlined colors
    UNDERLINE_BLACK ("\u001B[4;30m"),
    UNDERLINE_RED   ("\u001B[4;31m"),
    UNDERLINE_GREEN ("\u001B[4;32m"),
    UNDERLINE_YELLOW("\u001B[4;33m"),
    UNDERLINE_BLUE  ("\u001B[4;34m"),
    UNDERLINE_PURPLE("\u001B[4;35m"),
    UNDERLINE_CYAN  ("\u001B[4;36m"),
    UNDERLINE_WHITE ("\u001B[4;37m"),

    // High intensity color
    INTENSE_BLACK ("\u001B[0;90m"),
    INTENSE_RED   ("\u001B[0;91m"),
    INTENSE_GREEN ("\u001B[0;92m"),
    INTENSE_YELLOW("\u001B[0;93m"),
    INTENSE_BLUE  ("\u001B[0;94m"),
    INTENSE_PURPLE("\u001B[0;95m"),
    INTENSE_CYAN  ("\u001B[0;96m"),
    INTENSE_WHITE ("\u001B[0;97m"),

    // Background color
    BG_BLACK ("\u001B[40m"),
    BG_RED   ("\u001B[41m"),
    BG_GREEN ("\u001B[42m"),
    BG_YELLOW("\u001B[43m"),
    BG_BLUE  ("\u001B[44m"),
    BG_PURPLE("\u001B[45m"),
    BG_CYAN  ("\u001B[46m"),
    BG_WHITE ("\u001B[47m"),

    // Background with high intensity
    BG_INTENSE_BLACK ("\u001B[0;100m"),
    BG_INTENSE_RED   ("\u001B[0;101m"),
    BG_INTENSE_GREEN ("\u001B[0;102m"),
    BG_INTENSE_YELLOW("\u001B[0;103m"),
    BG_INTENSE_BLUE  ("\u001B[0;104m"),
    BG_INTENSE_PURPLE("\u001B[0;105m"),
    BG_INTENSE_CYAN  ("\u001B[0;106m"),
    BG_INTENSE_WHITE ("\u001B[0;107m");

    private String value = "\001B[0m";

    private AnsiColor(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}