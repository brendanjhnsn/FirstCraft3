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

        // Register commands
        getCommand("recipes").setExecutor(new RecipeCommand(this, recipeGUI));
        getCommand("reloadrecipes").setExecutor(new ReloadCommand(this)); // Register ReloadCommand

        // Register event listeners
        getServer().getPluginManager().registerEvents(recipeGUI, this);
        getServer().getPluginManager().registerEvents(recipeManager, this);

        getLogger().info("FirstCraft enabled!");
    }


    @Override
    public void onDisable() {
        getLogger().info("FirstCraft disabled!");
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public void setRecipeManager(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }
}