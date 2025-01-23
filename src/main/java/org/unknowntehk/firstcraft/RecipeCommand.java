package org.unknowntehk.firstcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecipeCommand implements CommandExecutor {
    private final FirstCraftPlugin plugin;
    private final RecipeGUI recipeGUI;

    public RecipeCommand(FirstCraftPlugin plugin, RecipeGUI recipeGUI) {
        this.plugin = plugin;
        this.recipeGUI = recipeGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            recipeGUI.openRecipesGUI(player);
            return true;
        }
        return false;
    }
};