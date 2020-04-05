package ovh.roro.pitchout.util.property.event;

import ovh.roro.pitchout.util.property.Property;

/**
 * This class is similar to {@link net.md_5.bungee.api.chat.ClickEvent}. Both are made for Chat Formatting, but this one
 * is made for our {@link Property} API.
 *
 * @author roro1506_HD
 */
public final class ClickEvent {

    private final ClickAction action;
    private final String value;

    /**
     * Creates a {@link ClickEvent} (similar to {@link net.md_5.bungee.api.chat.ClickEvent} but made for our {@link
     * Property} API)
     *
     * @param action The {@link ClickAction} this event will be doing
     * @param value The value which will be used by the action.
     */
    public ClickEvent(ClickAction action, String value) {
        this.action = action;
        this.value = value;
    }

    /**
     * Returns the {@link ClickAction} of this event
     *
     * @return this {@link ClickAction}
     */
    public ClickAction getAction() {
        return this.action;
    }

    /**
     * Returns the value of this event
     *
     * @return this event's value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * This class represents the type of click action.
     */
    public enum ClickAction {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        TWITCH_USER_INFO,
        SUGGEST_COMMAND,
        CHANGE_PAGE
    }
}
