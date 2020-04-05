package ovh.roro.pitchout.util;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author roro1506_HD
 */
public class ParticleUtil {

    public static void addParticle(Player player, EnumParticle particle, Location location, double offsetX, double offsetY, double offsetZ, double speed, int amount) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldParticles(particle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) offsetX, (float) offsetY, (float) offsetZ, (float) speed, amount));
    }

    public static void addParticle(EnumParticle particle, Location location, double offsetX, double offsetY, double offsetZ, double speed, int amount) {
        Bukkit.getOnlinePlayers().forEach(player -> addParticle(player, particle, location, offsetX, offsetY, offsetZ, speed, amount));
    }

    public static void playCircle(EnumParticle particle, Location location, int amount, double radius) {
        for (int i = 0; i < amount; i++) {
            double angle, x, z;

            angle = 2 * Math.PI * i / amount;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;

            location.add(x, 0, z);

            ParticleUtil.addParticle(particle, location, 0, 0, 0, 0, 1);

            location.subtract(x, 0, z);
        }
    }
}
