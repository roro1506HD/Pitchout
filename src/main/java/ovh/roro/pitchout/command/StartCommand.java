package ovh.roro.pitchout.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.roro.pitchout.game.GameManager;
import ovh.roro.pitchout.game.GameState;
import ovh.roro.pitchout.util.item.ItemRegistry;

/**
 * @author roro1506_HD
 */
public class StartCommand extends Command {

    public StartCommand() {
        super("start");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player && !sender.isOp())
            return false;

        if (GameManager.getInstance().getState() != GameState.WAITING) {
            sender.sendMessage("§cUne partie est déjà en cours !");
            if (sender instanceof Player)
                ((Player) sender).getInventory().addItem(ItemRegistry.BLACK_HOLE);
            return false;
        }

        if (GameManager.getInstance().getPlayers().size() < 2) {
            sender.sendMessage("§cVous ne pouvez pas lancer une partie avec seulement un joueur !");
            return false;
        }

        GameManager.getInstance().startGame();
        return true;
    }
}