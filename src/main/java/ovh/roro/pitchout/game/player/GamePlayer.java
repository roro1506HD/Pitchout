package ovh.roro.pitchout.game.player;

import java.util.List;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import ovh.roro.pitchout.PitchOut;
import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.GameState;
import ovh.roro.pitchout.game.scoreboard.defaults.FinishScoreboard;
import ovh.roro.pitchout.game.scoreboard.defaults.GameScoreboard;
import ovh.roro.pitchout.game.scoreboard.defaults.WaitingScoreboard;
import ovh.roro.pitchout.util.ScoreboardSign;
import ovh.roro.pitchout.util.firework.CustomEntityFirework;
import ovh.roro.pitchout.util.firework.FireworkUtil;
import ovh.roro.pitchout.util.item.ItemRegistry;

/**
 * @author roro1506_HD
 */
public class GamePlayer {

    private final UUID uuid;
    private final String name;
    private final CraftPlayer craftPlayer;

    private final ScoreboardSign scoreboard;
    private final WaitingScoreboard waitingScoreboard;
    private final GameScoreboard gameScoreboard;
    private final FinishScoreboard finishScoreboard;

    private GamePlayer lastAttacker;
    private int kills;
    private int killsSinceLastDeath;
    private int lives;
    private int place;
    private Team currentTeam;

    public GamePlayer(Player player) {
        this.craftPlayer = (CraftPlayer) player;

        this.uuid = player.getUniqueId();
        this.name = player.getName();

        this.scoreboard = new ScoreboardSign(this, "§8- §6§lPitchout §a0:00 §8-");
        this.scoreboard.create();
        this.waitingScoreboard = new WaitingScoreboard(this);
        this.gameScoreboard = new GameScoreboard(this);
        this.finishScoreboard = new FinishScoreboard(this);

        this.place = -1;
    }

    public void initialize() {
        this.craftPlayer.getInventory().clear();
        this.craftPlayer.getInventory().setArmorContents(null);
        this.craftPlayer.setMaxHealth(20.0D);
        this.craftPlayer.setHealth(20.0D);
        this.craftPlayer.setFoodLevel(20);
        this.craftPlayer.setSaturation(20.0F);
        this.craftPlayer.setExhaustion(20.0F);
        this.craftPlayer.setWalkSpeed(0.2F);
        this.craftPlayer.setLevel(0);
        this.craftPlayer.setExp(0.0F);
        this.craftPlayer.setGameMode(GameMode.ADVENTURE);
        this.craftPlayer.setAllowFlight(false);
        this.craftPlayer.setFlying(false);
        this.craftPlayer.teleport(new Location(Bukkit.getWorlds().get(0), 0.5D, 67.5D, 0.5D, 0.0F, 0.0F));

        this.craftPlayer.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(this.craftPlayer::removePotionEffect);

        this.waitingScoreboard.initScoreboard();
    }

    public void giveItems() {
        this.craftPlayer.getInventory().setItem(0, ItemRegistry.BOW);
        this.craftPlayer.getInventory().setItem(1, ItemRegistry.SPADE);
        this.craftPlayer.getInventory().setItem(2, ItemRegistry.FISHING_ROD);
        this.craftPlayer.getInventory().setItem(9, ItemRegistry.ARROW);
    }

    public void incrementKills() {
        this.kills++;
        this.killsSinceLastDeath++;

        if (this.kills % 5 == 0) {
            this.sendMessage("§8[§9PO§8] §b5 §7éjections ! §e+1 Bombe(s).");
            this.craftPlayer.getInventory().addItem(ItemRegistry.BOMB);
        }

        if (this.killsSinceLastDeath % 5 == 0) {
            this.sendMessage("§8[§9PO§8] §b5 §7éjections sans tomber ! §e+1 Trou(s) Noir.");
            this.craftPlayer.getInventory().addItem(ItemRegistry.BLACK_HOLE);
        }
    }

    public void decrementLives() {
        if (this.lives == 1) {
            this.place = GameManager.getInstance().getPlayers().size();
            return;
        }

        this.setLives(this.lives - 1);
        this.killsSinceLastDeath = 0;
    }

    public void kill() {
        GameManager gameManager = GameManager.getInstance();
        List<Location> spawns = gameManager.getLocation("spawn");

        this.decrementLives();

        CustomEntityFirework.spawn(this.craftPlayer.getLocation(), FireworkUtil.getFireworkEffect(FireworkUtil.getRandomColor(), Type.BALL_LARGE, false, false), Bukkit.getOnlinePlayers());

        this.craftPlayer.setVelocity(new Vector());
        this.craftPlayer.teleport(spawns.get(gameManager.getRandom().nextInt(spawns.size())));

        if (gameManager.getState() != GameState.IN_GAME)
            return;

        GamePlayer attacker = this.lastAttacker;

        if (attacker != null)
            attacker.incrementKills();

        if (this.place != -1) {
            Bukkit.broadcastMessage("§8[§9PO§8] §e" + this.name + " §7a été éliminé" + (attacker == null ? "" : " par §a" + attacker.getName()));
            this.sendMessage("§8[§9PO§8] §7Vous n'avez plus de vie et êtes éliminé !");
            this.sendMessage("§8[§9PO§8] §7Vous êtes maintenant un §fFantôme§7, veuillez attendre la fin de la partie");
            this.craftPlayer.setGameMode(GameMode.SPECTATOR);
            this.craftPlayer.getInventory().clear();
            this.craftPlayer.getInventory().setArmorContents(new ItemStack[0]);
            this.setCurrentTeam(gameManager.getScoreboard().getTeam("lives-0"));

            gameManager.getAllPlayers().stream()
                    .map(GamePlayer::getGameScoreboard)
                    .forEach(GameScoreboard::updatePlayers);

            gameManager.checkWin();
            return;
        }

        if (attacker != null)
            this.sendMessage("§8[§9PO§8] §c" + attacker.getName() + " §7vous a éjecté, il vous reste §b" + this.lives + " §7vie(s).");
        else
            this.sendMessage("§8[§9PO§8] §7Vous êtes tombé, il vous reste §b" + this.lives + " §7vie(s).");

        this.setCurrentTeam(gameManager.getScoreboard().getTeam("lives-" + this.lives));
        this.giveProtection();
    }

    public void giveProtection() {
        this.craftPlayer.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        Bukkit.getScheduler().runTaskLater(PitchOut.getInstance(), () -> this.craftPlayer.getInventory().setChestplate(null), 60L);
    }

    public int getLives() {
        return this.lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
        this.craftPlayer.setMaxHealth(this.lives * 2.0D);
    }

    public void setCurrentTeam(Team currentTeam) {
        if (this.currentTeam != null)
            this.currentTeam.removeEntry(this.name);

        this.currentTeam = currentTeam;
        this.currentTeam.addEntry(this.name);
    }

    public void reset() {
        this.scoreboard.clearLines();
        this.lives = 5;
        this.place = -1;
        this.kills = 0;
        this.killsSinceLastDeath = 0;
        this.lastAttacker = null;

        if (this.currentTeam != null) {
            this.currentTeam.removeEntry(this.name);
            this.currentTeam = null;
        }

        this.craftPlayer.getHandle().o(0);

        this.initialize();
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        PlayerConnection playerConnection = this.craftPlayer.getHandle().playerConnection;

        if (title == null && subtitle == null) {
            playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
            return;
        }

        playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut));
        playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle)));
        playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title)));
    }

    public void sendActionBar(String message) {
        this.craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
    }

    public void sendMessage(String message) {
        this.craftPlayer.sendMessage(message);
    }

    public void sendPackets(Packet<?>... packets) {
        PlayerConnection playerConnection = this.craftPlayer.getHandle().playerConnection;

        for (Packet<?> packet : packets)
            playerConnection.sendPacket(packet);
    }

    public boolean isEliminated() {
        return this.place != -1;
    }

    public int getPlace() {
        return this.place;
    }

    public int getKills() {
        return this.kills;
    }

    public void setLastAttacker(GamePlayer lastAttacker) {
        this.lastAttacker = lastAttacker;
    }

    public GamePlayer getLastAttacker() {
        return this.lastAttacker;
    }

    public ScoreboardSign getScoreboard() {
        return this.scoreboard;
    }

    public WaitingScoreboard getWaitingScoreboard() {
        return this.waitingScoreboard;
    }

    public GameScoreboard getGameScoreboard() {
        return this.gameScoreboard;
    }

    public FinishScoreboard getFinishScoreboard() {
        return this.finishScoreboard;
    }

    public CraftPlayer getCraftPlayer() {
        return this.craftPlayer;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }
}
