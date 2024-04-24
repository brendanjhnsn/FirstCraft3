package org.unknowntehk.firstcraft;

import org.bukkit.plugin.java.JavaPlugin;

public class FirstCraftPlugin extends JavaPlugin {
    private RecipeManager recipeManager;
    private RecipeGUI recipeGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        recipeManager = new RecipeManager(this);
        recipeGUI = new RecipeGUI(this, recipeManager);
        getCommand("recipes").setExecutor(new RecipeCommand(this, recipeGUI));
        getServer().getPluginManager().registerEvents(recipeGUI, this);
        getServer().getPluginManager().registerEvents(recipeManager, this);
        getLogger().info("FirstCraft enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FirstCraft disabled!");
    }
}
