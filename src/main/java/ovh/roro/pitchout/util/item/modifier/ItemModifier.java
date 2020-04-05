package ovh.roro.pitchout.util.item.modifier;

import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import ovh.roro.pitchout.util.item.IItemModifier;
import ovh.roro.pitchout.util.item.ItemBuilder;

/**
 * @author roro1506_HD
 */
public class ItemModifier implements IItemModifier {

    private final ItemBuilder builder;

    private NBTTagString name;
    private NBTTagList lore;

    private NBTTagByte unbreakable;
    private NBTTagList enchantments;
    private NBTTagList storedEnchantments;
    private NBTTagInt hideFlags;

    public ItemModifier(ItemBuilder builder) {
        this.builder = builder;

        NBTTagCompound display = builder.getTag().getCompound("display");

        this.name = (NBTTagString) display.get("Name");
        this.lore = display.getList("Lore", 8);

        this.unbreakable = (NBTTagByte) builder.getTag().get("Unbreakable");
        this.enchantments = builder.getTag().getList("Enchantments", 10);
        this.storedEnchantments = builder.getTag().getList("StoredEnchantments", 10);
        this.hideFlags = new NBTTagInt(builder.getTag().getInt("HideFlags"));
    }

    public ItemModifier addLore(String... lore) {
        for (String s : lore)
            this.lore.add(new NBTTagString(s));
        return this;
    }

    public ItemModifier setName(String title) {
        this.name = new NBTTagString(title);
        return this;
    }

    public ItemModifier setUnbreakable(boolean unbreakable) {
        if (unbreakable)
            this.unbreakable = new NBTTagByte((byte) 1);
        else
            this.unbreakable = null;
        return this;
    }

    public ItemModifier addEnchantment(Enchantment enchantment, int level) {
        NBTTagCompound enchantmentTag = new NBTTagCompound();

        enchantmentTag.setShort("id", (short) enchantment.getId());
        enchantmentTag.setShort("lvl", (short) level);

        this.enchantments.add(enchantmentTag);
        return this;
    }

    public ItemModifier addStoredEnchantment(Enchantment enchantment, int level) {
        NBTTagCompound enchantmentTag = new NBTTagCompound();

        enchantmentTag.setShort("id", (short) enchantment.getId());
        enchantmentTag.setShort("lvl", (short) level);

        this.storedEnchantments.add(enchantmentTag);
        return this;
    }

    public ItemModifier addItemFlags(ItemFlag... itemFlags) {
        int flags = this.hideFlags.d();

        for (ItemFlag flag : itemFlags)
            flags |= 1 << flag.ordinal();

        this.hideFlags = new NBTTagInt(flags);
        return this;
    }

    public ItemModifier setGlowing(boolean glowing) {
        if (!glowing) {
            this.enchantments = new NBTTagList();
            return this;
        }

        NBTTagCompound enchantment = new NBTTagCompound();

        enchantment.setShort("id", (short) 250);
        enchantment.setShort("lvl", (short) 1);

        this.enchantments.add(enchantment);
        return this;
    }

    @Override
    public ItemBuilder apply() {
        NBTTagCompound displayTag = new NBTTagCompound();

        if (this.name != null)
            displayTag.set("Name", this.name);

        if (!this.lore.isEmpty())
            displayTag.set("Lore", this.lore);

        if (this.unbreakable != null)
            this.builder.addNBTTag("Unbreakable", this.unbreakable);

        if (!this.enchantments.isEmpty())
            this.builder.addNBTTag("ench", this.enchantments);

        if (!this.storedEnchantments.isEmpty())
            this.builder.addNBTTag("StoredEnchantments", this.storedEnchantments);

        if (this.hideFlags.d() != 0)
            this.builder.addNBTTag("HideFlags", this.hideFlags);

        return this.builder.addNBTTag("display", displayTag);
    }
}
