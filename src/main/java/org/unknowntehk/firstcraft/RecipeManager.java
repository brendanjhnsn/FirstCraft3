package org.unknowntehk.firstcraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager implements Listener {
    private JavaPlugin plugin;
    private Map<String, Boolean> craftLimits = new HashMap<>();
    private File craftLimitsFile;
    private FileConfiguration craftLimitsConfig;

    public RecipeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.craftLimitsFile = new File(plugin.getDataFolder(), "craftLimits.yml");
        loadCraftLimitsConfig();
        loadRecipes();
    }

    private void loadCraftLimitsConfig() {
        if (!craftLimitsFile.exists()) {
            try {
                craftLimitsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        craftLimitsConfig = YamlConfiguration.loadConfiguration(craftLimitsFile);
        craftLimitsConfig.getKeys(false).forEach(key ->
                craftLimits.put(key, craftLimitsConfig.getBoolean(key))
        );
    }

    private void saveCraftLimitsConfig() {
        craftLimits.forEach((key, value) -> craftLimitsConfig.set(key, value));
        try {
            craftLimitsConfig.save(craftLimitsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadRecipes() {
        FileConfiguration config = plugin.getConfig();
        Bukkit.resetRecipes();
        config.getConfigurationSection("recipes").getKeys(false).forEach(recipeKey -> {
            try {
                String path = "recipes." + recipeKey;
                ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")), 1);
                ItemMeta meta = item.getItemMeta();
                if (config.contains(path + ".customModelData")) {
                    meta.setCustomModelData(config.getInt(path + ".customModelData"));
                    item.setItemMeta(meta);
                    craftLimits.put(String.valueOf(meta.getCustomModelData()), false);
                    plugin.getLogger().info("Loading recipe with custom model data: " + meta.getCustomModelData());
                }
                ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, recipeKey), item);
                List<String> shape = config.getStringList(path + ".shape");
                recipe.shape(shape.toArray(new String[0]));
                config.getConfigurationSection(path + ".ingredients").getKeys(false).forEach(charKey ->
                        recipe.setIngredient(charKey.charAt(0), Material.valueOf(config.getString(path + ".ingredients." + charKey)))
                );
                Bukkit.addRecipe(recipe);
                plugin.getLogger().info("Recipe registered successfully: " + recipeKey);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load recipe: " + recipeKey);
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        ItemMeta meta = result.getItemMeta();
        if (meta != null && meta.hasCustomModelData()) {
            String modelDataKey = String.valueOf(meta.getCustomModelData());
            if (craftLimits.getOrDefault(modelDataKey, false)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage("This unique item has already been crafted.");
                return;
            }
            craftLimits.put(modelDataKey, true);
            saveCraftLimitsConfig(); // Save limits after updating
            executeCommands(modelDataKey, event.getWhoClicked().getName());
        }
    }

    private void executeCommands(String modelDataKey, String playerName) {
        List<String> commands = plugin.getConfig().getStringList("recipes." + modelDataKey + ".commands");
        if (!commands.isEmpty()) {
            commands.forEach(command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", playerName))
            );
        } else {
            plugin.getLogger().info("No commands found for model key: " + modelDataKey);
        }
    }

    public void reloadRecipes() {
        craftLimits.clear();
        loadCraftLimitsConfig();
        loadRecipes();
    }
}
