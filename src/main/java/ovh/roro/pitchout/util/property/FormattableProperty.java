package ovh.roro.pitchout.util.property;

import ovh.roro.pitchout.util.StringUtil;
import ovh.roro.pitchout.util.property.event.ClickEvent;
import ovh.roro.pitchout.util.property.event.HoverEvent;

/**
 * This class is used to format another {@link Property}. It keeps line breaks and create new ones to not exceed 40
 * chars long lines.
 *
 * @author roro1506_HD
 */
public class FormattableProperty extends Property {

    private final Property property;

    public FormattableProperty(Property property) {
        this.property = property;
    }

    @Override
    void toString(StringBuilder builder) {
        builder.append(String.join("\n", StringUtil.splitLine(this.property.toString(), 40)));
        super.toString(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormattableProperty append(Property property) {
        return (FormattableProperty) super.append(property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormattableProperty setInsertion(String insertion) {
        return (FormattableProperty) super.setInsertion(insertion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormattableProperty setHoverEvent(HoverEvent hoverEvent) {
        return (FormattableProperty) super.setHoverEvent(hoverEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormattableProperty setClickEvent(ClickEvent clickEvent) {
        return (FormattableProperty) super.setClickEvent(clickEvent);
    }

    public Property getProperty() {
        return this.property;
    }
}
