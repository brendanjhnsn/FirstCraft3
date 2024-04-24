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
                plugin.getLogger().severe("Could not create craftLimits.yml: " + e.getMessage());
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
            plugin.getLogger().severe("Could not save craftLimits.yml: " + e.getMessage());
        }
    }

    public void loadRecipes() {
        Bukkit.resetRecipes();
        FileConfiguration config = plugin.getConfig();
        config.getConfigurationSection("recipes").getKeys(false).forEach(recipeKey -> {
            ItemStack item = createRecipeItem(recipeKey);
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, recipeKey), item);
            List<String> shape = config.getStringList("recipes." + recipeKey + ".shape");
            recipe.shape(shape.toArray(new String[0]));
            config.getConfigurationSection("recipes." + recipeKey + ".ingredients").getKeys(false).forEach(charKey ->
                    recipe.setIngredient(charKey.charAt(0), Material.valueOf(config.getString("recipes." + recipeKey + ".ingredients." + charKey)))
            );
            Bukkit.addRecipe(recipe);
        });
    }

    public ItemStack createRecipeItem(String recipeKey) {
        FileConfiguration config = plugin.getConfig();
        String path = "recipes." + recipeKey;
        Material material = Material.valueOf(config.getString(path + ".material"));
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(config.getString(path + ".display_name", "Special Item"));
            meta.setLore(config.getStringList(path + ".lore"));
            if (config.contains(path + ".customModelData")) {
                meta.setCustomModelData(config.getInt(path + ".customModelData"));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public Map<Character, ItemStack> getIngredients(String recipeKey) {
        Map<Character, ItemStack> ingredientMap = new HashMap<>();
        FileConfiguration config = plugin.getConfig();
        String path = "recipes." + recipeKey + ".ingredients";
        if (config.getConfigurationSection(path) != null) {
            for (String key : config.getConfigurationSection(path).getKeys(false)) {
                Material mat = Material.valueOf(config.getString(path + "." + key));
                ItemStack stack = new ItemStack(mat, 1);
                ingredientMap.put(key.charAt(0), stack);
            }
        }
        return ingredientMap;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        ItemMeta meta = result.getItemMeta();
        if (meta != null && meta.hasCustomModelData()) {
            String recipeKey = findMatchingRecipeKey(meta);
            if (recipeKey == null) return; // Stop if no matching key is found

            if (craftLimits.getOrDefault(recipeKey, false)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage("This unique item has already been crafted.");
                return;
            }
            craftLimits.put(recipeKey, true);
            saveCraftLimits(); // Ensure to save after updating the limits
            executeCommands(recipeKey, event.getWhoClicked().getName());
        }
    }

    private String findMatchingRecipeKey(ItemMeta meta) {
        String configPath = "recipes";
        FileConfiguration config = plugin.getConfig();
        for (String key : config.getConfigurationSection(configPath).getKeys(false)) {
            int modelDataConfig = config.getInt(configPath + "." + key + ".customModelData");
            if (meta.getCustomModelData() == modelDataConfig) {
                return key;
            }
        }
        return null;
    }

    public void executeCommands(String recipeKey, String playerName) {
        List<String> commands = plugin.getConfig().getStringList("recipes." + recipeKey + ".commands");
        if (!commands.isEmpty()) {
            commands.forEach(command -> {
                String formattedCommand = command.replace("{player}", playerName);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
                plugin.getLogger().info("Executing Command: " + formattedCommand);
            });
        }
    }
}
