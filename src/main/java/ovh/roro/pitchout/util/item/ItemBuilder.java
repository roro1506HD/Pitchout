package ovh.roro.pitchout.util.item;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ovh.roro.pitchout.util.item.modifier.BannerModifier;
import ovh.roro.pitchout.util.item.modifier.BookModifier;
import ovh.roro.pitchout.util.item.modifier.ItemModifier;
import ovh.roro.pitchout.util.item.modifier.SkullModifier;

/**
 * TODO: Fully implement all behaviors
 *
 * @author roro1506_HD
 */
public class ItemBuilder implements Cloneable {

    private static final Map<Class, Function<ItemBuilder, ? extends IItemModifier>> ITEM_MODIFIERS = new ConcurrentHashMap<>();
    private NBTTagCompound tagCompound;

    private ItemBuilder(Material material, int amount) {
        this.tagCompound = new NBTTagCompound();
        this.setMaterial(material);
        this.setAmount(amount);
    }

    private ItemBuilder(Material material) {
        this(material, 1);
    }

    private ItemBuilder(NBTTagCompound tagCompound) {
        this.tagCompound = tagCompound;
    }

    /**
     * Creates a builder from the provided NBT Tag
     *
     * @param material The material of the item. Can be modified later
     * @return the newly-created builder
     * @see ItemBuilder#of(Material, int)
     */
    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    /**
     * Creates a builder from the provided NBT Tag
     *
     * @param material The material of the item. Can be modified later
     * @param amount The amount of items stacked
     * @return the newly-created builder
     */
    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    /**
     * Creates a builder from the provided NBT Tag
     *
     * @param tagCompound The tag to create the builder from
     * @return the newly-created builder
     */
    public static ItemBuilder of(NBTTagCompound tagCompound) {
        return new ItemBuilder(tagCompound);
    }

    /**
     * Creates a builder from the provided ItemStack
     *
     * @param itemStack The item to create the builder from
     * @return the newly-created builder
     */
    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()));
    }

    /**
     * Creates a builder from another builder. Basically clones it
     *
     * @param builder The builder to copy tag from
     * @return the newly-created builder
     */
    public static ItemBuilder of(ItemBuilder builder) {
        return new ItemBuilder(builder.tagCompound);
    }

    public static <T extends IItemModifier> void registerModifier(Class<T> clazz, Function<ItemBuilder, T> constructor) {
        Preconditions.checkState(!ITEM_MODIFIERS.containsKey(clazz), "Modifier already registered");

        ITEM_MODIFIERS.put(clazz, constructor);
    }

    /**
     * Sets the material
     *
     * @param material The material
     * @return this builder
     */
    public ItemBuilder setMaterial(Material material) {
        this.tagCompound.setShort("id", (short) material.getId());
        return this;
    }

    /**
     * Sets the amount
     *
     * @param amount The amount of stacked items
     * @return this builder
     */
    public ItemBuilder setAmount(int amount) {
        this.tagCompound.setByte("Count", (byte) amount);
        return this;
    }

    /**
     * Adds a NBT Tag to the "tag" tag
     *
     * @param key The tag's key
     * @param nbtBase The tag's value
     * @return this builder
     */
    public ItemBuilder addNBTTag(String key, NBTBase nbtBase) {
        this.getTag().set(key, nbtBase);
        return this;
    }

    public ItemBuilder reset() {
        this.tagCompound = new NBTTagCompound();
        return this;
    }

    /**
     * Returns a {@link IItemModifier} to easily modify this builder
     *
     * @param clazz The modifier class
     * @return The modifier instance
     */
    public <T extends IItemModifier> T getModifier(Class<T> clazz) {
        Preconditions.checkArgument(ITEM_MODIFIERS.containsKey(clazz), "No modifier matching " + clazz.getSimpleName() + " found.");

        // noinspection unchecked
        return (T) ITEM_MODIFIERS.get(clazz).apply(this);
    }

    /**
     * Transforms this ItemBuilder into a {@link net.minecraft.server.v1_8_R3.ItemStack}
     *
     * @return the {@link net.minecraft.server.v1_8_R3.ItemStack} corresponding to this builder
     */
    public net.minecraft.server.v1_8_R3.ItemStack toNMSItemStack() {
        return net.minecraft.server.v1_8_R3.ItemStack.createStack(this.tagCompound);
    }

    /**
     * Transforms this ItemBuilder into a {@link ItemStack}
     *
     * @return the {@link ItemStack} corresponding to this builder
     */
    public ItemStack toBukkitItemStack() {
        return CraftItemStack.asBukkitCopy(this.toNMSItemStack());
    }

    /**
     * Gets or creates the "tag" tag
     *
     * @return the "tag" tag
     */
    public NBTTagCompound getTag() {
        if (!this.tagCompound.hasKey("tag"))
            this.tagCompound.set("tag", new NBTTagCompound());

        return this.tagCompound.getCompound("tag");
    }

    /**
     * Creates a new {@link ItemBuilder} and copies every data into it
     *
     * @return the newly created {@link ItemBuilder}
     */
    @Override
    public ItemBuilder clone() {
        return new ItemBuilder((NBTTagCompound) this.tagCompound.clone());
    }

    static {
        registerModifier(BookModifier.class, BookModifier::new);
        registerModifier(ItemModifier.class, ItemModifier::new);
        registerModifier(SkullModifier.class, SkullModifier::new);
        registerModifier(BannerModifier.class, BannerModifier::new);
    }
}
