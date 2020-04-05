package ovh.roro.pitchout.util.firework;

import java.util.Collection;
import net.minecraft.server.v1_8_R3.EntityFireworks;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class CustomEntityFirework extends EntityFireworks {

    private final Collection<? extends Player> players;
    private boolean gone;

    public CustomEntityFirework(final World world, final Collection<? extends Player> collection) {
        super(world);
        this.gone = false;
        this.players = collection;
        this.a(0.25f, 0.25f);
    }

    public void t_() {
        if (this.gone || this.world.isClientSide)
            return;

        this.gone = true;
        if (this.players != null && this.players.size() > 0) {
            for (final Player player : this.players)
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityStatus(this, (byte) 17));

            this.die();
            return;
        }
        this.world.broadcastEntityEffect(this, (byte) 17);
        this.die();
    }

    public static void spawn(final Location location, final FireworkEffect effect, final Collection<? extends Player> collection) {
        try {
            final CustomEntityFirework firework = new CustomEntityFirework(((CraftWorld) location.getWorld()).getHandle(), collection);
            
            final FireworkMeta meta = ((Firework) firework.getBukkitEntity()).getFireworkMeta();
            meta.addEffect(effect);
            ((Firework) firework.getBukkitEntity()).setFireworkMeta(meta);

            firework.setPosition(location.getX(), location.getY(), location.getZ());

            if (((CraftWorld) location.getWorld()).getHandle().addEntity(firework))
                firework.setInvisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}