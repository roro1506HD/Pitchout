package ovh.roro.pitchout.game.scoreboard.defaults;

import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.game.scoreboard.IScoreboard;

/**
 * @author roro1506_HD
 */
public class WaitingScoreboard implements IScoreboard {

    private final GamePlayer player;

    private int playersIndex = -1;

    public WaitingScoreboard(GamePlayer player) {
        this.player = player;
    }

    @Override
    public void initScoreboard() {
        int index = 0;

        this.player.getScoreboard().setObjectiveName("§8- §6§lPitchout §a0:00 §8-");

        this.player.getScoreboard().setLine(index++, "§eplay.epicube.fr");
        this.player.getScoreboard().setLine(index++, "§eID : §7pitchoutE1");
        this.player.getScoreboard().setLine(index++, "§a");

        if (this.playersIndex == -1)
            this.playersIndex = index;

        index = this.updatePlayers(index);

        this.player.getScoreboard().setLine(index++, "§eAlliance : §a✔");
        this.player.getScoreboard().setLine(index, "§a");
    }

    public void updatePlayers() {
        this.updatePlayers(this.playersIndex);
    }

    private int updatePlayers(int index) {
        this.player.getScoreboard().setLine(index++, "§eChallenger(s) : §b" + GameManager.getInstance().getPlayers().size());
        return index;
    }
}
