package ovh.roro.pitchout.game;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ovh.roro.pitchout.PitchOut;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.game.scoreboard.defaults.GameScoreboard;
import ovh.roro.pitchout.game.scoreboard.defaults.WaitingScoreboard;
import ovh.roro.pitchout.util.ScoreboardTextAnimator;
import ovh.roro.pitchout.util.firework.FireworkUtil;
import ovh.roro.pitchout.util.scanner.MapScanner;

/**
 * @author roro1506_HD
 */
public class GameManager {

    private static GameManager instance;

    private final Map<UUID, GamePlayer> playersByUuid;
    private final Map<String, List<Location>> gameLocations;
    private final List<File> availableMaps;
    private final List<Team> teams;
    private final Random random;
    private final Scoreboard scoreboard;
    private final ScoreboardTextAnimator textAnimator;
    private final ScheduledExecutorService service;

    private final GameLoop gameLoop;

    private GameState state;
    private int timeElapsed;

    public GameManager() {
        instance = this;

        this.playersByUuid = new HashMap<>();
        this.gameLocations = new HashMap<>();
        this.availableMaps = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.random = new Random();
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.textAnimator = new ScoreboardTextAnimator("play.epicube.fr", "§e", "§6", "§c");
        this.service = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "Pitchout Scoreboard Executor"));

        this.gameLoop = new GameLoop();

        this.state = GameState.WAITING;

        PitchOut.getInstance().getLogger().info("Scanning available maps...");

        for (File file : Objects.requireNonNull(PitchOut.getInstance().getDataFolder().listFiles())) {
            if (file.isDirectory())
                this.availableMaps.add(file);
        }

        PitchOut.getInstance().getLogger().info("Scanned " + this.availableMaps.size() + " available map" + (this.availableMaps.size() == 1 ? "" : "s"));

        if (this.availableMaps.isEmpty())
            Bukkit.shutdown();

        this.verifyTeam("lives-5", "§e5§c❤ §a");
        this.verifyTeam("lives-4", "§e4§c❤ §e");
        this.verifyTeam("lives-3", "§e3§c❤ §6");
        this.verifyTeam("lives-2", "§e2§c❤ §c");
        this.verifyTeam("lives-1", "§e1§c❤ §4");
        this.verifyTeam("lives-0", "§e0§c❤ §7");

        this.service.scheduleAtFixedRate(() -> {
            boolean changed = this.textAnimator.next();

            if (!changed)
                return;

            String text = this.textAnimator.getActualText();

            for (GamePlayer player : this.getAllPlayers())
                player.getScoreboard().setLine(0, text);
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public static GameManager getInstance() {
        return instance;
    }

    private void verifyTeam(String name, String prefix) {
        Team team = this.scoreboard.getTeam(name);

        if (team == null)
            team = this.scoreboard.registerNewTeam(name);

        this.teams.add(team);

        team.setPrefix(prefix);
        team.setSuffix("§r");

        team.getEntries().forEach(team::removeEntry);
    }

    private void scanSigns() {
        World world = Bukkit.getWorld("gameworld");
        List<String[]> detectedSigns = new MapScanner().scanSigns(world);

        PitchOut.getInstance().getLogger().info("Analyzing " + detectedSigns.size() + " signs...");

        for (String[] detectedSign : detectedSigns) {
            if (detectedSign[0].equals("\"LOCATION\"")) {
                String key = detectedSign[1].substring(1, detectedSign[1].length() - 1);

                List<Location> locations = this.gameLocations.computeIfAbsent(key, s -> new ArrayList<>());

                String[] parts = detectedSign[2].substring(1, detectedSign[2].length() - 1).split(",");
                Location location;

                if (parts.length > 4)
                    location = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]));
                else
                    location = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));

                locations.add(location);
                PitchOut.getInstance().getLogger().info("Adding sign key : " + key + "(" + locations.size() + ")");

                String[] locationParts = detectedSign[4].split(":");
                world.getBlockAt(Integer.parseInt(locationParts[0]), Integer.parseInt(locationParts[1]), Integer.parseInt(locationParts[2])).setType(Material.AIR);
            }
            //IslandWars.getInstance().getLogger().info("Found sign at " + detectedSign[4] + " (" + detectedSign[0] + ")");
        }
    }

    public void startGame() {
        // Creating map
        File usedMap = this.availableMaps.get(this.random.nextInt(this.availableMaps.size()));

        Bukkit.broadcastMessage("§8[§9PO§8] §e§7Carte choisie : §e" + usedMap.getName() + "§7 !");
        Bukkit.broadcastMessage("§8[§9PO§8] §e§7Création du monde...");

        try {
            FileUtils.copyDirectory(usedMap, new File("gameworld"));
            World world = new WorldCreator("gameworld").createWorld();

            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doMobLoot", "false");
        } catch (Exception ex) {
            ex.printStackTrace();
            Bukkit.broadcastMessage("§8[§9PO§8] §e§cUne erreur est survenue.");
            return;
        }

        scanSigns();

        this.state = GameState.IN_GAME;
        this.gameLoop.reset();

        List<Location> spawns = this.getLocation("spawn");

        Collections.shuffle(spawns);

        Queue<Location> spawnsQueue = new ArrayDeque<>(spawns);

        for (GamePlayer gamePlayer : getPlayers()) {
            Player player = gamePlayer.getCraftPlayer();

            // Reset player
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0.0F);
            player.getActivePotionEffects().stream()
                    .map(PotionEffect::getType)
                    .forEach(player::removePotionEffect);

            // Teleport player
            gamePlayer.sendMessage("§8[§9PO§8] §e§7Téléportation en cours...");
            player.teleport(spawnsQueue.remove());

            // Initialize player
            gamePlayer.giveItems();
            gamePlayer.setLives(5);
            gamePlayer.setCurrentTeam(this.scoreboard.getTeam("lives-5"));
            gamePlayer.giveProtection();

            // Switch scoreboard
            gamePlayer.getScoreboard().clearLines();
            gamePlayer.getGameScoreboard().initScoreboard();
        }

        this.gameLoop.startTask();
        Bukkit.getScheduler().runTaskTimer(PitchOut.getInstance(), () -> {
            for (GamePlayer player : this.getPlayers())
                if (player.getCraftPlayer().getLocation().getBlock().getType() == Material.PISTON_MOVING_PIECE || player.getCraftPlayer().getLocation().getY() < 0)
                    player.kill();
        }, 1, 1);
    }

    public void checkWin() {
        List<GamePlayer> players = this.getPlayers();

        if (players.size() == 2) {
            for (GamePlayer player : getAllPlayers())
                player.sendTitle("§6FINAL", "", 10, 20, 10);

            Bukkit.broadcastMessage("§6-----------------------------------------------------");
            Bukkit.broadcastMessage("§8[§9PO§8] §6Match final: §a" + players.get(0).getName() + " §7contre §a" + players.get(1).getName());
            Bukkit.broadcastMessage("§8[§9PO§8] §7Les éjections sont deux fois plus puissantes !");
            Bukkit.broadcastMessage("§6-----------------------------------------------------");
        } else if (players.size() == 1) {
            List<GamePlayer> sortedPlayers = this.getAllPlayers().stream()
                    .sorted(Comparator.comparingInt(GamePlayer::getPlace))
                    .collect(Collectors.toList());

            Bukkit.broadcastMessage("§8[§9PO§8] §6§lVictoire de §a" + players.get(0).getName() + " §e§k!§b§k!§a§k!§c§k!§d§k! §b§lFélicitations §e§k!§b§k!§a§k!§c§k!§d§k!");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§6-----------------------------------------------------");
            Bukkit.broadcastMessage("§f     ● §7Classement : §8(§fJoueur§8)");
            Bukkit.broadcastMessage("§e     1. §a" + (sortedPlayers.size() == 0 ? "§cPersonne" : sortedPlayers.get(0).getName()));
            Bukkit.broadcastMessage("§e     2. §a" + (sortedPlayers.size() <= 1 ? "§cPersonne" : sortedPlayers.get(1).getName()));
            Bukkit.broadcastMessage("§e     3. §a" + (sortedPlayers.size() <= 2 ? "§cPersonne" : sortedPlayers.get(2).getName()));
            Bukkit.broadcastMessage("§6-----------------------------------------------------");

            this.finishGame();
        }
    }

    public void finishGame() {
        this.state = GameState.FINISHED;

        Bukkit.getScheduler().cancelTask(this.gameLoop.getTaskId());

        for (GamePlayer gamePlayer : getPlayers()) {
            gamePlayer.getCraftPlayer().setGameMode(GameMode.ADVENTURE);
            gamePlayer.getCraftPlayer().setAllowFlight(true);
            gamePlayer.getCraftPlayer().getInventory().clear();
            gamePlayer.getCraftPlayer().getInventory().setArmorContents(null);
        }

        for (GamePlayer gamePlayer : this.getAllPlayers()) {
            gamePlayer.getScoreboard().clearLines();
            gamePlayer.getFinishScoreboard().initScoreboard();
        }

        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                if (++this.timer >= 20) {
                    resetGame();
                    cancel();
                    return;
                }

                for (GamePlayer gamePlayer : getPlayers()) {
                    Firework firework = gamePlayer.getCraftPlayer().getWorld().spawn(gamePlayer.getCraftPlayer().getLocation(), Firework.class);
                    FireworkMeta fireworkMeta = firework.getFireworkMeta();
                    fireworkMeta.setPower(random.nextInt(3) + 1);
                    fireworkMeta.addEffect(FireworkUtil.getRandomFirework());
                    firework.setFireworkMeta(fireworkMeta);
                }
            }
        }.runTaskTimer(PitchOut.getInstance(), 20, 20);
    }

    private void resetGame() {
        this.state = GameState.WAITING;
        this.timeElapsed = 0;
        this.gameLocations.clear();

        for (Team team : this.teams)
            team.getEntries().forEach(team::removeEntry);

        for (GamePlayer player : this.getAllPlayers())
            player.reset();

        for (GamePlayer player : this.getAllPlayers())
            player.getWaitingScoreboard().updatePlayers();

        Bukkit.unloadWorld("gameworld", false);

        try {
            FileUtils.deleteDirectory(new File("gameworld"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Bukkit.getScheduler().cancelTasks(PitchOut.getInstance());
    }

    public int getTimeElapsed() {
        return this.timeElapsed;
    }

    int increaseTimeElapsed() {
        return ++this.timeElapsed;
    }

    public GamePlayer addPlayer(Player player) {
        GamePlayer gamePlayer = this.playersByUuid.computeIfAbsent(player.getUniqueId(), uuid -> new GamePlayer(player));

        gamePlayer.initialize();

        if (this.state == GameState.WAITING)
            getAllPlayers().stream()
                    .map(GamePlayer::getWaitingScoreboard)
                    .forEach(WaitingScoreboard::updatePlayers);
        else
            getAllPlayers().stream()
                    .map(GamePlayer::getGameScoreboard)
                    .forEach(GameScoreboard::updatePlayers);

        return gamePlayer;
    }

    public boolean removePlayer(Player player) {
        GamePlayer gamePlayer = this.playersByUuid.remove(player.getUniqueId());

        if (gamePlayer == null)
            return false;

        if (this.state == GameState.WAITING)
            getAllPlayers().stream()
                    .map(GamePlayer::getWaitingScoreboard)
                    .forEach(WaitingScoreboard::updatePlayers);

        return this.state == GameState.IN_GAME;
    }

    public Random getRandom() {
        return random;
    }

    public GameState getState() {
        return this.state;
    }

    public List<GamePlayer> getPlayers() {
        return this.playersByUuid.values().stream()
                .filter(gamePlayer -> !gamePlayer.isEliminated())
                .collect(Collectors.toList());
    }

    public List<GamePlayer> getAllPlayers() {
        return new ArrayList<>(this.playersByUuid.values());
    }

    public GamePlayer getPlayer(Player player) {
        return this.playersByUuid.get(player.getUniqueId());
    }

    public GamePlayer getPlayer(UUID uuid) {
        return this.playersByUuid.get(uuid);
    }

    public List<Location> getLocation(String key) {
        return this.gameLocations.get(key);
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }
}
