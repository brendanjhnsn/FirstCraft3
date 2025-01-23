package org.unknowntehk.firstcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final FirstCraftPlugin plugin;

    public ReloadCommand(FirstCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("firstcraft.reload")) {
                player.sendMessage("You do not have permission to execute this command.");
                return true;
            }
        }

        plugin.reloadConfig();
        plugin.getRecipeManager().loadRecipes(); // Assuming you have a method to reload recipes in RecipeManager
        sender.sendMessage("FirstCraft configuration reloaded.");
        return true;
    }
}
