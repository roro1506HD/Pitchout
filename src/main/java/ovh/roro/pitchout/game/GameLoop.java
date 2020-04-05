package ovh.roro.pitchout.game;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.Bukkit;
import ovh.roro.pitchout.PitchOut;
import ovh.roro.pitchout.game.player.GamePlayer;

/**
 * @author roro1506_HD
 */
class GameLoop implements Runnable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("m:ss");

    private int taskId;

    GameLoop() {
    }

    void reset() {

    }

    public void startTask() {
        this.taskId = Bukkit.getScheduler().runTaskTimer(PitchOut.getInstance(), this, 20L, 20L).getTaskId();
    }

    @Override
    public void run() {
        GameManager gameManager = GameManager.getInstance();

        int time = gameManager.increaseTimeElapsed();
        for (GamePlayer player : gameManager.getAllPlayers())
            player.getScoreboard().setObjectiveName("§8- §6§lPitchout §a" + FORMATTER.format(LocalTime.ofNanoOfDay(Math.max((600 - time) * 1000000000L, 0L))) + " §8-");

        if (time == 0) {
            Bukkit.broadcastMessage("§eEGALITE (déso j'ai pas le message originel)");
            gameManager.finishGame();
        }
    }

    public int getTaskId() {
        return this.taskId;
    }
}
