package ovh.roro.pitchout.util.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ovh.roro.pitchout.util.item.modifier.ItemModifier;
import ovh.roro.pitchout.util.item.modifier.SkullModifier;

/**
 * @author roro1506_HD
 */
public class ItemRegistry {

    public static final ItemStack SPADE = ItemBuilder.of(Material.DIAMOND_SPADE)
            .getModifier(ItemModifier.class)
            .addEnchantment(Enchantment.KNOCKBACK, 4)
            .addItemFlags(ItemFlag.HIDE_ENCHANTS)
            .setUnbreakable(true)
            .setName("§3Pitchnette")
            .apply()
            .toBukkitItemStack();

    public static final ItemStack BOW = ItemBuilder.of(Material.BOW)
            .getModifier(ItemModifier.class)
            .addEnchantment(Enchantment.ARROW_KNOCKBACK, 3)
            .addEnchantment(Enchantment.ARROW_INFINITE, 1)
            .addItemFlags(ItemFlag.HIDE_ENCHANTS)
            .setUnbreakable(true)
            .setName("§3Ejector")
            .apply()
            .toBukkitItemStack();

    public static final ItemStack FISHING_ROD = ItemBuilder.of(Material.FISHING_ROD)
            .getModifier(ItemModifier.class)
            .setUnbreakable(true)
            .setName("§3Harpon")
            .apply()
            .toBukkitItemStack();

    public static final ItemStack ARROW = new ItemStack(Material.ARROW);

    public static final ItemStack CHESTPLATE = new ItemStack(Material.CHAINMAIL_CHESTPLATE);

    public static final ItemStack BOMB = ItemBuilder.of(new ItemStack(Material.SKULL_ITEM, 1, (short) 3))
            .getModifier(ItemModifier.class)
            .setName("§cBombe §lBONUS")
            .apply()
            .getModifier(SkullModifier.class)
            .setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWI5OTRiNDFmMDdmODdiMzI4MTg2YWNmY2JkYWJjNjk5ZDViMTg0N2ZhYmIyZTQ5ZDVhYmMyNzg2NTE0M2E0ZSJ9fX0=")
            .apply()
            .toBukkitItemStack();

    public static final ItemStack BLACK_HOLE = ItemBuilder.of(Material.EYE_OF_ENDER)
            .getModifier(ItemModifier.class)
            .setName("§5Trou noir §c§lBONUS")
            .apply()
            .toBukkitItemStack();
}
