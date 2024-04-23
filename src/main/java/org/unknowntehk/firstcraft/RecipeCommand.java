package org.unknowntehk.firstcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecipeCommand implements CommandExecutor {
    private FirstCraftPlugin plugin;
    private RecipeManager recipeManager;

    public RecipeCommand(FirstCraftPlugin plugin, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player)sender;
        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("firstcraft.reload")) {
            plugin.reloadPluginConfig();
            sender.sendMessage("Configuration reloaded.");
            return true;
        }

        // Optionally, you could add logic to display a GUI with recipes here.
        player.sendMessage("Use the GUI to view recipes (functionality not implemented).");
        return true;
    }
}
