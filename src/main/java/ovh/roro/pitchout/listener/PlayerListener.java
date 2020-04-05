package ovh.roro.pitchout.listener;

import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Items;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ovh.roro.pitchout.PitchOut;
import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.GameState;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.util.ParticleUtil;
import ovh.roro.pitchout.util.item.ItemRegistry;

/**
 * @author roro1506_HD
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (GameManager.getInstance().getState() != GameState.WAITING)
            event.disallow(Result.KICK_OTHER, "§cUne partie est en cours ! \n\n§7Veuillez attendre la fin de celle-ci\n§7pour vous connecter !");
        else if (GameManager.getInstance().getPlayers().size() >= 8)
            event.disallow(Result.KICK_OTHER, "§cLa partie est pleine ! \n\n§7Veuillez attendre la fin de celle-ci\n§7puis réessayez !");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GameManager.getInstance().addPlayer(player);

        if (GameManager.getInstance().getState() == GameState.WAITING)
            GameManager.getInstance().getAllPlayers().forEach(tempPlayer -> tempPlayer.getWaitingScoreboard().updatePlayers());

        event.setJoinMessage("§8[§9PO§8] §e" + gamePlayer.getName() + " §aa rejoint la partie ! §7(§e" + GameManager.getInstance().getPlayers().size() + "§7/§e8§7)");

        if (GameManager.getInstance().getPlayers().size() == 8)
            GameManager.getInstance().startGame();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (GameManager.getInstance().removePlayer(player)) {
            Bukkit.broadcastMessage("§8[§9PO§8] §e" + player.getName() + " §7s'est déconnecté, et est par conséquent éliminé.");
            GameManager.getInstance().checkWin();
        }

        event.setQuitMessage(null);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setDamage(0);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == State.CAUGHT_ENTITY && event.getCaught() instanceof Player && !event.getCaught().getUniqueId().equals(event.getPlayer().getUniqueId())) {
            Vector direction = event.getPlayer().getLocation().getDirection();

            direction.setY(0.1D);

            event.getCaught().setVelocity(direction.normalize().multiply(GameManager.getInstance().getPlayers().size() == 2 ? 2 : 1));
            GameManager.getInstance().getPlayer((Player) event.getCaught()).setLastAttacker(GameManager.getInstance().getPlayer(event.getPlayer()));
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        ((CraftArrow) event.getProjectile()).getHandle().knockbackStrength *= (GameManager.getInstance().getPlayers().size() == 2 ? 2 : 1);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (ItemRegistry.BOMB.isSimilar(event.getItem())) {
            event.setCancelled(true);

            ((CraftPlayer) event.getPlayer()).getHandle().inventory.a(Items.SKULL);

            TNTPrimed tntPrimed = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation(), TNTPrimed.class);

            tntPrimed.setIsIncendiary(false);
            tntPrimed.setFuseTicks(60);
            tntPrimed.setVelocity(event.getPlayer().getLocation().getDirection().normalize());

            try {
                Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
                sourceField.setAccessible(true);
                sourceField.set(((CraftTNTPrimed) tntPrimed).getHandle(), ((CraftPlayer) event.getPlayer()).getHandle());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            new BukkitRunnable() {
                private final TNTPrimed tnt = tntPrimed;

                @Override
                public void run() {
                    if (!this.tnt.isValid()) {
                        super.cancel();
                        return;
                    }

                    if (!this.tnt.isOnGround())
                        ParticleUtil.addParticle(EnumParticle.FLAME, this.tnt.getLocation(), 0.25D, 0.25D, 0.25D, 0, 5);
                    else
                        ParticleUtil.playCircle(EnumParticle.SMOKE_NORMAL, this.tnt.getLocation(), 100, 5);
                }
            }.runTaskTimer(PitchOut.getInstance(), 1L, 1L);
        } else if (ItemRegistry.BLACK_HOLE.isSimilar(event.getItem())) {
            event.setCancelled(true);

            ((CraftPlayer) event.getPlayer()).getHandle().inventory.a(Items.ENDER_EYE);

            event.getPlayer().launchProjectile(EnderPearl.class);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof CraftingInventory)
            event.setCancelled(true);
    }
}
