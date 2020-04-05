package ovh.roro.pitchout.util.property;

import java.util.ArrayList;
import java.util.List;
import ovh.roro.pitchout.util.property.event.ClickEvent;
import ovh.roro.pitchout.util.property.event.HoverEvent;

/**
 * The main class of our {@link Property} API. These components were made to be used in translatable environments. They
 * also can be used as Text Properties and so on.
 *
 * @author roro1506_HD
 */
public class Property {

    private List<Property> children;
    private String insertion;
    private HoverEvent hoverEvent;
    private ClickEvent clickEvent;

    /**
     * Translatable <code>toString</code> method. This method is used to return a {@link String} containing this
     * translated text and this children translated texts.
     *
     * @return the translated {@link String}
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.toString(builder);
        return builder.toString();
    }

    /**
     * Internal <code>toString()</code> method. This method is used to append this translated text and this children
     * translated texts.
     *
     * @param builder The {@link StringBuilder} which is going to be appended by this translated text and this children
     * translated texts.
     */
    void toString(StringBuilder builder) {
        if (this.children != null)
            for (Property property : this.children)
                property.toString(builder);
    }

    /**
     * This method is used to append another {@link Property}, making her a children of this one.
     *
     * @param property The children {@link Property} that's going to be appended to this {@link Property}
     * @return this {@link Property}
     */
    public Property append(Property property) {
        if (this.children == null)
            this.children = new ArrayList<>();

        this.children.add(property);
        return this;
    }

    /**
     * Returns this {@link Property}'s insertion.
     * <p>
     * Insertion is a {@link ClickEvent} with action {@code ClickAction.SUGGEST_COMMAND} triggered by shift-clicking the
     * message.
     *
     * @return this {@link Property}'s insertion.
     */
    public String getInsertion() {
        return this.insertion;
    }

    /**
     * Sets this {@link Property}'s insertion.
     * <p>
     * Insertion is a {@link ClickEvent} with action {@code ClickAction.SUGGEST_COMMAND} triggered by shift-clicking the
     * message.
     *
     * @param insertion The suggested text
     * @return this {@link Property}
     */
    public Property setInsertion(String insertion) {
        this.insertion = insertion;
        return this;
    }

    /**
     * Returns this {@link Property}'s {@link HoverEvent}
     *
     * @return this {@link Property}'s {@link HoverEvent}
     */
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    /**
     * Sets this {@link Property}'s {@link HoverEvent}
     *
     * @param hoverEvent The {@link HoverEvent}
     * @return this {@link Property}
     */
    public Property setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    /**
     * Returns this {@link Property}'s {@link ClickEvent}
     *
     * @return this {@link Property}'s {@link ClickEvent}
     */
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    /**
     * Sets this {@link Property}'s {@link ClickEvent}
     *
     * @param clickEvent The {@link ClickEvent}
     * @return this {@link Property}
     */
    public Property setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    /**
     * Returns this {@link Property}'s children. This does not include this {@link Property}
     *
     * @return this {@link Property} children
     */
    public List<Property> getChildren() {
        return this.children;
    }

    /**
     * Sets this {@link Property}'s children.
     *
     * @param children The new children
     */
    public void setChildren(List<Property> children) {
        this.children = children;
    }
}
