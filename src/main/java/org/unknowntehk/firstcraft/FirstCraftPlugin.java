package org.unknowntehk.firstcraft;

import org.bukkit.plugin.java.JavaPlugin;

public class FirstCraftPlugin extends JavaPlugin {
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.recipeManager = new RecipeManager(this);
        getCommand("recipes").setExecutor(new RecipeCommand(this, recipeManager));
        getServer().getPluginManager().registerEvents(recipeManager, this);
        getLogger().info("FirstCraft enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FirstCraft disabled!");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        recipeManager.reloadRecipes();
    }
}
