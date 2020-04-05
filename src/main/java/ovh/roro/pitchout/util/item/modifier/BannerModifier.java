package ovh.roro.pitchout.util.item.modifier;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import ovh.roro.pitchout.util.item.IItemModifier;
import ovh.roro.pitchout.util.item.ItemBuilder;

/**
 * @author roro1506_HD
 */
public class BannerModifier implements IItemModifier {

    private final ItemBuilder builder;

    private NBTTagList patterns;

    private NBTTagInt base;

    public BannerModifier(ItemBuilder builder) {
        this.builder = builder;

        this.patterns = builder.getTag().getList("Patterns", 10);
        this.base = (NBTTagInt) builder.getTag().get("Base");
    }

    public BannerModifier setBase(DyeColor color) {
        this.base = new NBTTagInt(color.getDyeData());
        return this;
    }

    public BannerModifier addPattern(Pattern pattern) {
        NBTTagCompound patternTag = new NBTTagCompound();

        patternTag.set("Color", new NBTTagInt(pattern.getColor().getDyeData()));
        patternTag.set("Pattern", new NBTTagString(pattern.getPattern().getIdentifier()));

        this.patterns.add(patternTag);

        return this;
    }

    @Override
    public ItemBuilder apply() {
        NBTTagCompound blockEntityTag = new NBTTagCompound();

        if (this.base != null)
            blockEntityTag.set("Base", this.base);

        blockEntityTag.set("Patterns", this.patterns);

        return this.builder.addNBTTag("BlockEntityTag", blockEntityTag);
    }
}
