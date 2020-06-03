package kr.syeyoung.webbrowser.util;

import org.bukkit.ChatColor;

import java.awt.*;

public class ColorHelper {
    public static final Color BLACK = new Color(0x000000);
    public static final Color DARK_BLUE = new Color(0x0000AA);
    public static final Color DARK_GREEN = new Color(0x00AA00);
    public static final Color DARK_AQUA = new Color(0x00AAAA);
    public static final Color DARK_RED = new Color(0xAA0000);
    public static final Color DARK_PURPLE = new Color(0xAA00AA);
    public static final Color GOLD = new Color(0xFFAA00);
    public static final Color GRAY = new Color(0xAAAAAA);
    public static final Color DARK_GRAY = new Color(0x555555);
    public static final Color BLUE = new Color(0X5555ff);
    public static final Color GREEN = new Color(0x55FF55);
    public static final Color AQUA = new Color(0x55FFFF);
    public static final Color RED = new Color(0xFF5555);
    public static final Color LIGHT_PURPLE = new Color(0xFF55FF);
    public static final Color YELLOW = new Color(0xFFFF55);
    public static final Color WHITE = new Color(0xFFFFFF);

    public static Color chatColorToColor(ChatColor color) {
        if (!color.isColor()) return null;

        switch(color) {
            case RED:
                return RED;
            case AQUA:
                return AQUA;
            case BLUE:
                return BLUE;
            case BLACK:
                return BLACK;
            case GOLD:
                return GOLD;
            case GRAY:
                return GRAY;
            case GREEN:
                return GREEN;
            case WHITE:
                return WHITE;
            case YELLOW:
                return YELLOW;
            case DARK_RED:
                return DARK_RED;
            case DARK_AQUA:
                return DARK_AQUA;
            case DARK_BLUE:
                return DARK_BLUE;
            case DARK_GREEN:
                return DARK_GREEN;
            case DARK_GRAY:
                return DARK_GRAY;
            case DARK_PURPLE:
                return DARK_PURPLE;
            case LIGHT_PURPLE:
                return LIGHT_PURPLE;
        }
        return null;
    }

    public static Color chatColorToColor(net.md_5.bungee.api.ChatColor color) {
        switch(color) {
            case RED:
                return RED;
            case AQUA:
                return AQUA;
            case BLUE:
                return BLUE;
            case BLACK:
                return BLACK;
            case GOLD:
                return GOLD;
            case GRAY:
                return GRAY;
            case GREEN:
                return GREEN;
            case WHITE:
                return WHITE;
            case YELLOW:
                return YELLOW;
            case DARK_RED:
                return DARK_RED;
            case DARK_AQUA:
                return DARK_AQUA;
            case DARK_BLUE:
                return DARK_BLUE;
            case DARK_GREEN:
                return DARK_GREEN;
            case DARK_GRAY:
                return DARK_GRAY;
            case DARK_PURPLE:
                return DARK_PURPLE;
            case LIGHT_PURPLE:
                return LIGHT_PURPLE;
        }
        return null;
    }
}
