package ovh.roro.pitchout.util.property.event;

import ovh.roro.pitchout.util.property.Property;

/**
 * This class is similar to {@link net.md_5.bungee.api.chat.HoverEvent}. Both are made for Chat Formatting, but this one
 * is simpler to use and made for our {@link Property} API.
 *
 * @author roro1506_HD
 */
public class HoverEvent {

    private final HoverAction action;
    private final Property property;

    /**
     * Creates a {@link HoverEvent} (similar to {@link net.md_5.bungee.api.chat.HoverEvent} but made for our {@link
     * Property} API)
     *
     * @param action The {@link HoverAction} this event will be doing
     * @param property The {@link Property} this event will be showing
     */
    public HoverEvent(HoverAction action, Property property) {
        this.action = action;
        this.property = property;
    }

    /**
     * Returns the {@link HoverAction} of this event
     *
     * @return this {@link HoverAction}
     */
    public HoverAction getAction() {
        return this.action;
    }

    /**
     * Returns the {@link Property} of this event
     *
     * @return this event's value
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * This class represents the type of hover action.
     */
    public enum HoverAction {
        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }
}
