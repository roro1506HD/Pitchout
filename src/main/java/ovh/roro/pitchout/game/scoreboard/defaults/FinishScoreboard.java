package ovh.roro.pitchout.game.scoreboard.defaults;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.game.scoreboard.IScoreboard;

/**
 * @author roro1506_HD
 */
public class FinishScoreboard implements IScoreboard {

    private final GamePlayer player;

    public FinishScoreboard(GamePlayer player) {
        this.player = player;
    }

    @Override
    public void initScoreboard() {
        int index = 0;

        this.player.getScoreboard().setLine(index++, "§eplay.epicube.fr");
        this.player.getScoreboard().setLine(index++, "§eID : §7pitchoutE1");
        this.player.getScoreboard().setLine(index++, "§a");

        List<GamePlayer> sortedPlayers = GameManager.getInstance().getAllPlayers().stream()
                .sorted(Comparator.comparingInt(value -> -value.getKills()))
                .collect(Collectors.toList());

        this.player.getScoreboard().setLine(index++, "§e§l3. §f" + (sortedPlayers.size() <= 2 ? "§cPersonne" : sortedPlayers.get(2).getName() + " §a" + sortedPlayers.get(2).getKills()));
        this.player.getScoreboard().setLine(index++, "§e§l2. §f" + (sortedPlayers.size() <= 1 ? "§cPersonne" : sortedPlayers.get(1).getName() + " §a" + sortedPlayers.get(1).getKills()));
        this.player.getScoreboard().setLine(index++, "§e§l1. §f" + (sortedPlayers.size() == 0 ? "§cPersonne" : sortedPlayers.get(0).getName() + " §a" + sortedPlayers.get(0).getKills()));

        this.player.getScoreboard().setLine(index++, "§a");

        this.player.getScoreboard().setLine(index, "§eTop kill :");
    }
}
