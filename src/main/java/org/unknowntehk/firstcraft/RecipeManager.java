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
        if (!craftLimitsFile.exists()) {
            try {
                craftLimitsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        craftLimitsConfig = YamlConfiguration.loadConfiguration(craftLimitsFile);
        loadCraftLimits();
        loadRecipes();
    }

    private void loadCraftLimits() {
        craftLimits.clear();
        craftLimitsConfig.getKeys(false).forEach(key ->
                craftLimits.put(key, craftLimitsConfig.getBoolean(key))
        );
    }

    private void saveCraftLimits() {
        craftLimits.forEach((key, value) -> craftLimitsConfig.set(key, value));
        try {
            craftLimitsConfig.save(craftLimitsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadRecipes() {
        Bukkit.resetRecipes();
        FileConfiguration config = plugin.getConfig();
        config.getConfigurationSection("recipes").getKeys(false).forEach(recipeKey -> {
            String path = "recipes." + recipeKey;
            ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")), 1);
            ItemMeta meta = item.getItemMeta();
            if (config.contains(path + ".customModelData")) {
                meta.setCustomModelData(config.getInt(path + ".customModelData"));
                item.setItemMeta(meta);
            }
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, recipeKey), item);
            List<String> shape = config.getStringList(path + ".shape");
            recipe.shape(shape.toArray(new String[0]));
            config.getConfigurationSection(path + ".ingredients").getKeys(false).forEach(charKey ->
                    recipe.setIngredient(charKey.charAt(0), Material.valueOf(config.getString(path + ".ingredients." + charKey)))
            );
            Bukkit.addRecipe(recipe);
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
            saveCraftLimits(); // Ensure to save after updating the limits
            executeCommands(modelDataKey, event.getWhoClicked().getName());
        }
    }

    public void executeCommands(String modelDataKey, String playerName) {
        List<String> commands = plugin.getConfig().getStringList("recipes." + modelDataKey + ".commands");
        if (!commands.isEmpty()) {
            commands.forEach(command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", playerName))
            );
        }
    }

    public void reloadRecipes() {
        loadCraftLimits();
        loadRecipes();
    }
}
