package ru.tioplaya;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorizeText {
    boolean yep = false;

    public String colorizeText(String text) {
        if (text == null) {
            yep = false;
            return text;
        }
        if (text.matches("&*")) {
            yep = true;
        }
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String hexCode = text.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replaceAll("#", "x");
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch)
                builder.append("&").append(c);
            text = text.replaceAll(hexCode, builder.toString());
            matcher = pattern.matcher(text);
        }
        if (!yep) {
            text = text.replaceAll("&", "§");
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    // TODO градиент не робит (переливающийся)
}
