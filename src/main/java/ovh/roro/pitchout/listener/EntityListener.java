package ovh.roro.pitchout.listener;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;
import ovh.roro.pitchout.PitchOut;
import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.GameState;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.util.ParticleUtil;
import ovh.roro.pitchout.util.firework.CustomEntityFirework;
import ovh.roro.pitchout.util.firework.FireworkUtil;
import ovh.roro.pitchout.util.item.ItemRegistry;

/**
 * @author roro1506_HD
 */
public class EntityListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameManager.getInstance().getState() != GameState.IN_GAME || (event.getEntity() instanceof Player && ItemRegistry.CHESTPLATE.isSimilar(((Player) event.getEntity()).getInventory().getChestplate())))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (GameManager.getInstance().getState() != GameState.IN_GAME || (event.getEntity() instanceof Player && ItemRegistry.CHESTPLATE.isSimilar(((Player) event.getEntity()).getInventory().getChestplate())))
            return;

        if (event.getDamager() instanceof Player) {
            event.setCancelled(true);

            GameManager.getInstance().getPlayer(event.getEntity().getUniqueId()).setLastAttacker(GameManager.getInstance().getPlayer(event.getDamager().getUniqueId()));

            net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) event.getEntity()).getHandle();
            EntityPlayer attacker = ((CraftPlayer) event.getDamager()).getHandle();

            if (!entity.damageEntity(DamageSource.GENERIC, 0.0F))
                return;

            double d0 = attacker.locX - entity.locX;

            double d1;
            for (d1 = attacker.locZ - entity.locZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D)
                d0 = (Math.random() - Math.random()) * 0.01D;

            if (entity instanceof EntityLiving) {
                ((EntityLiving) entity).aw = (float) (MathHelper.b(d1, d0) * 180.0D / 3.1415927410125732D - (double) attacker.yaw);
                ((EntityLiving) entity).a(entity, 0.0F, d0, d1);
            }

            int strength = EnchantmentManager.a(attacker);
            float yaw = event.getDamager().getLocation().getYaw();

            double oldX = entity.motX;
            double oldY = entity.motY;
            double oldZ = entity.motZ;

            strength *= GameManager.getInstance().getPlayers().size() == 2 ? 2 : 1;

            entity.g(-MathHelper.sin(yaw * 3.1415927F / 180.0F) * strength * 0.5F, 0.1D, MathHelper.cos(yaw * 3.1415927F / 180.0F) * strength * 0.5F);

            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).playerConnection.sendPacket(new PacketPlayOutEntityVelocity(entity));
                entity.velocityChanged = false;
                entity.motX = oldX;
                entity.motY = oldY;
                entity.motZ = oldZ;
            }
        } else if (event.getDamager() instanceof Arrow)
            GameManager.getInstance().getPlayer(event.getEntity().getUniqueId()).setLastAttacker(GameManager.getInstance().getPlayer(((Player) ((Arrow) event.getDamager()).getShooter()).getUniqueId()));
        else if (event.getDamager() instanceof TNTPrimed) {
            GameManager.getInstance().getPlayer(event.getEntity().getUniqueId()).setLastAttacker(GameManager.getInstance().getPlayer((((TNTPrimed) event.getDamager()).getSource()).getUniqueId()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed) {
            event.setCancelled(true);

            Location location = event.getEntity().getLocation();

            for (Entity entity : event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 5, 5, 5))
                if (entity.getLocation().distanceSquared(location) <= 25 && entity instanceof Player)
                    entity.setVelocity(entity.getLocation().toVector().subtract(location.toVector()).normalize().multiply(1.5D));

            ParticleUtil.addParticle(EnumParticle.EXPLOSION_HUGE, location, 1.0D, 0.0D, 0.0D, 1, 20);
            ParticleUtil.addParticle(EnumParticle.SMOKE_NORMAL, location, 1.0D, 0.0D, 0.0D, 1, 20);

            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.ENDER_PEARL)
            return;

        event.setCancelled(true);

        Location to = event.getTo();
        Player attacker = event.getPlayer();

        to.add(0, 1, 0);

        new BukkitRunnable() {
            private int timer = 0;
            @Override
            public void run() {
                ParticleUtil.addParticle(EnumParticle.SMOKE_LARGE, to.clone().add(-0.1D + Math.random() * 0.2D, -0.1D + Math.random() * 0.2D, -0.1D + Math.random() * 0.2D), 0.0D, 0.0D, 0.0D, 0, 1);
                ParticleUtil.addParticle(EnumParticle.SPELL_WITCH, to.clone().add(-0.1D + Math.random() * 0.2D, -0.1D + Math.random() * 0.2D, -0.1D + Math.random() * 0.2D), 0.0D, 0.0D, 0.0D, 0, 1);
                ParticleUtil.addParticle(EnumParticle.PORTAL, to, 0.0D, 0.0D, 0.0D, 2, 4);

                for (Entity entity : to.getWorld().getNearbyEntities(to, 10, 10, 10)) {
                    double distance = entity.getLocation().distanceSquared(to);

                    if (distance <= 100 && ((Player) entity).getGameMode() != GameMode.SPECTATOR)
                        entity.setVelocity(to.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(0.1D).multiply((100 - distance) * 0.05D));
                }

                this.timer++;

                if (this.timer > 100L) {
                    this.cancel();
                    CustomEntityFirework.spawn(to, FireworkUtil.getFireworkEffect(Color.BLACK, Type.BALL_LARGE, false, false), Bukkit.getOnlinePlayers());

                    for (Entity entity : to.getWorld().getNearbyEntities(to, 1, 1, 1)) {
                        if (entity instanceof Player && ((Player) entity).getGameMode() != GameMode.SPECTATOR) {
                            GamePlayer gamePlayer = GameManager.getInstance().getPlayer(entity.getUniqueId());

                            gamePlayer.setLastAttacker(GameManager.getInstance().getPlayer(attacker));
                            gamePlayer.kill();
                        }
                    }
                }
            }
        }.runTaskTimer(PitchOut.getInstance(), 1L, 1L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow)
            event.getEntity().remove();
    }
}
