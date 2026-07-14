package player.chops.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import player.chops.gui.ShopGUI;
import player.chops.utils.Utils;

public class ShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            ShopGUI.openOwnShopsGUI(player, 0);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "admin":
                if (player.hasPermission("playsershops.admin")) {
                    ShopGUI.openAdminGUI(player, 0);
                } else {
                    player.sendMessage(Utils.colorize("&cNo permission."));
                }
                break;
            default:
                player.sendMessage(Utils.colorize("&cUsage: /lojinha"));
                break;
        }
        return true;
    }
}
