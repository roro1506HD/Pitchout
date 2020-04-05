package ovh.roro.pitchout.util.property;

import ovh.roro.pitchout.util.property.event.ClickEvent;
import ovh.roro.pitchout.util.property.event.HoverEvent;

/**
 * This class is the a simple text which does not support translations.
 *
 * @author roro1506_HD
 */
public class TextProperty extends Property {

    /**
     * Empty {@link TextProperty} instance.
     */
    public static final TextProperty EMPTY = new TextProperty("");

    private String text;

    public TextProperty(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void toString(StringBuilder builder) {
        builder.append(this.text);
        super.toString(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextProperty append(Property property) {
        return (TextProperty) super.append(property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextProperty setInsertion(String insertion) {
        return (TextProperty) super.setInsertion(insertion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextProperty setHoverEvent(HoverEvent hoverEvent) {
        return (TextProperty) super.setHoverEvent(hoverEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextProperty setClickEvent(ClickEvent clickEvent) {
        return (TextProperty) super.setClickEvent(clickEvent);
    }

    /**
     * Returns the current text of this {@link TextProperty}
     *
     * @return the current text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets this {@link TextProperty} text to the provided one.
     *
     * @param text The new text
     * @return the current {@link TextProperty}
     */
    public TextProperty setText(String text) {
        this.text = text;
        return this;
    }
}
