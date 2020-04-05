package ovh.roro.pitchout.util.property;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.v1_8_R3.ChatClickable;
import net.minecraft.server.v1_8_R3.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ChatHoverable;
import net.minecraft.server.v1_8_R3.ChatHoverable.EnumHoverAction;
import net.minecraft.server.v1_8_R3.ChatModifier;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;

public class PropertyTransformer {

    private static final Pattern INCREMENTAL_PATTERN = Pattern.compile("(\\u00A7[0-9a-fk-or])|(\\n)", Pattern.CASE_INSENSITIVE);
    private static final Map<Character, EnumChatFormat> FORMAT_MAP = ImmutableMap.<Character, EnumChatFormat>builder()
            .put('1', EnumChatFormat.DARK_BLUE)
            .put('2', EnumChatFormat.DARK_GREEN)
            .put('3', EnumChatFormat.DARK_AQUA)
            .put('4', EnumChatFormat.DARK_RED)
            .put('5', EnumChatFormat.DARK_PURPLE)
            .put('6', EnumChatFormat.GOLD)
            .put('7', EnumChatFormat.GRAY)
            .put('8', EnumChatFormat.DARK_GRAY)
            .put('9', EnumChatFormat.BLUE)
            .put('a', EnumChatFormat.GREEN)
            .put('b', EnumChatFormat.AQUA)
            .put('c', EnumChatFormat.RED)
            .put('d', EnumChatFormat.LIGHT_PURPLE)
            .put('e', EnumChatFormat.YELLOW)
            .put('f', EnumChatFormat.WHITE)
            .put('k', EnumChatFormat.OBFUSCATED)
            .put('l', EnumChatFormat.BOLD)
            .put('m', EnumChatFormat.STRIKETHROUGH)
            .put('n', EnumChatFormat.UNDERLINE)
            .put('o', EnumChatFormat.ITALIC)
            .put('r', EnumChatFormat.RESET)
            .build();

    public static String toString(Property property) {
        return toComponent(property, false).getText();
    }

    public static IChatBaseComponent toComponent(Property property) {
        return toComponent(property, true);
    }

    private static IChatBaseComponent toComponent(Property property, boolean transform) {
        if (property instanceof FormattableProperty)
            return toComponent(((FormattableProperty) property).getProperty(), transform);

        String text = null;
        if (property instanceof TextProperty)
            text = ((TextProperty) property).getText();

        IChatBaseComponent component = transform ? transform(text) : new ChatComponentText(text);

        if (property.getInsertion() != null)
            component.getChatModifier().setInsertion(property.getInsertion());

        if (property.getClickEvent() != null)
            component.getChatModifier().setChatClickable(new ChatClickable(EnumClickAction.values()[property.getClickEvent().getAction().ordinal()], property.getClickEvent().getValue()));

        if (property.getHoverEvent() != null)
            component.getChatModifier().setChatHoverable(new ChatHoverable(EnumHoverAction.values()[property.getHoverEvent().getAction().ordinal()], toComponent(property.getHoverEvent().getProperty(), transform)));

        if (property.getChildren() != null)
            property.getChildren().stream()
                    .map(children -> toComponent(children, transform))
                    .forEach(component::addSibling);

        return component;
    }

    public static IChatBaseComponent[] toComponents(Property[] properties) {
        return Arrays.stream(properties)
                .map(PropertyTransformer::toComponent)
                .toArray(IChatBaseComponent[]::new);
    }

    private static IChatBaseComponent transform(String message) {
        IChatBaseComponent component = new ChatComponentText("");

        if (message == null)
            return component;

        Matcher matcher = INCREMENTAL_PATTERN.matcher(message);
        ChatModifier chatModifier = new ChatModifier();
        int currentIndex = 0;
        String match;
        while (matcher.find()) {
            int groupId = 0;
            int index;

            do
                groupId++;
            while ((match = matcher.group(groupId)) == null);

            if ((index = matcher.start(groupId)) > currentIndex)
                component.addSibling(new ChatComponentText(message.substring(currentIndex, index)).setChatModifier(chatModifier.clone()));

            switch (groupId) {
                case 1:
                    EnumChatFormat chatFormat = FORMAT_MAP.get(match.toLowerCase().charAt(1));
                    if (chatFormat == null || chatFormat == EnumChatFormat.RESET)
                        chatModifier = new ChatModifier();
                    else if (chatFormat.isFormat())
                        switch (chatFormat) {
                            case BOLD:
                                chatModifier.setBold(true);
                                break;
                            case ITALIC:
                                chatModifier.setItalic(true);
                                break;
                            case STRIKETHROUGH:
                                chatModifier.setStrikethrough(true);
                                break;
                            case UNDERLINE:
                                chatModifier.setUnderline(true);
                                break;
                            case OBFUSCATED:
                                chatModifier.setRandom(true);
                                break;
                        }
                    else
                        chatModifier = new ChatModifier().setColor(chatFormat);
                    break;
                case 2:
                    component.addSibling(new ChatComponentText("\n"));
                    break;
            }

            currentIndex = matcher.end(groupId);
        }

        if (currentIndex < message.length())
            component.addSibling(new ChatComponentText(message.substring(currentIndex)).setChatModifier(chatModifier.clone()));

        return component;
    }
}