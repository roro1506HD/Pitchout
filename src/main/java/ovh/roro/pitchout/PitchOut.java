package ovh.roro.pitchout;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.roro.pitchout.command.StartCommand;
import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.GameState;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.listener.EntityListener;
import ovh.roro.pitchout.listener.PlayerListener;
import ovh.roro.pitchout.listener.WorldListener;
import ovh.roro.pitchout.util.ScoreboardSign;

/**
 * @author roro1506_HD
 */
public class PitchOut extends JavaPlugin {

    private static PitchOut instance;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        GameManager gameManager = new GameManager();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new WorldListener(), this);
        pluginManager.registerEvents(new EntityListener(), this);

        SimpleCommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();
        commandMap.register(getDescription().getName(), new StartCommand());

        if (Bukkit.getWorld("gameworld") != null)
            Bukkit.unloadWorld("gameworld", false);

        try {
            FileUtils.deleteDirectory(new File("gameworld"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Player player : Bukkit.getOnlinePlayers())
            gameManager.addPlayer(player);

        gameManager.getAllPlayers().forEach(tempPlayer -> tempPlayer.getWaitingScoreboard().updatePlayers());
    }

    @Override
    public void onDisable() {
        GameManager.getInstance().getAllPlayers().stream()
                .map(GamePlayer::getScoreboard)
                .forEach(ScoreboardSign::destroy);
    }

    public static PitchOut getInstance() {
        return instance;
    }
}
