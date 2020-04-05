package ovh.roro.pitchout.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;

/**
 * @author roro1506_HD
 */
public class StringUtil {

    /**
     * Split a long line into provided length lines
     *
     * @param line The line to split
     * @param maxChars The maximum length of a line
     * @return The splitted lines
     */
    public static String[] splitLine(String line, int maxChars) {
        List<String> result = new ArrayList<>();
        String[] split = line.split("\n");
        String lastSplit = "";

        for (String part : split) {
            part = lastSplit + part;

            if (ChatColor.stripColor(part).length() <= maxChars) {
                result.add(part);
                continue;
            }

            String[] words = part.split(" ");
            String currentLine = "";

            for (String word : words) {
                if (ChatColor.stripColor(currentLine + word).length() > maxChars && ChatColor.stripColor(word).length() <= maxChars) {
                    result.add(currentLine.trim());
                    currentLine = ChatColor.getLastColors(currentLine) + word + " ";
                } else
                    currentLine += word + " ";
            }

            if (!ChatColor.stripColor(currentLine.trim()).isEmpty())
                result.add(currentLine.trim());

            lastSplit = ChatColor.getLastColors(result.get(result.size() - 1));
        }

        return result.toArray(new String[0]);
    }
}