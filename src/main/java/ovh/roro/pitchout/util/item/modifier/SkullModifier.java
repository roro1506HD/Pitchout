package ovh.roro.pitchout.util.item.modifier;

import java.util.UUID;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import ovh.roro.pitchout.util.item.IItemModifier;
import ovh.roro.pitchout.util.item.ItemBuilder;

/**
 * @author roro1506_HD
 */
public class SkullModifier implements IItemModifier {

    private final ItemBuilder builder;

    private String texture;
    private String signature;

    public SkullModifier(ItemBuilder builder) {
        this.builder = builder;

        NBTTagCompound texture = builder.getTag().getCompound("SkullOwner").getCompound("Properties").getList("textures", 10).get(0);

        this.texture = texture.getString("Value");
        this.signature = texture.getString("Signature");
    }

    public SkullModifier setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public SkullModifier setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    @Override
    public ItemBuilder apply() {
        NBTTagCompound skullOwner = new NBTTagCompound();
        NBTTagCompound properties = new NBTTagCompound();
        NBTTagList textures = new NBTTagList();
        NBTTagCompound texture = new NBTTagCompound();

        texture.setString("Value", this.texture);

        if (this.signature != null)
            texture.setString("Signature", this.signature);

        textures.add(texture);

        skullOwner.setString("Id", UUID.randomUUID().toString());
        properties.set("textures", textures);

        skullOwner.set("Properties", properties);

        return this.builder.addNBTTag("SkullOwner", skullOwner);
    }
}
