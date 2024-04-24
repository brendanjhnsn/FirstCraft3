package org.unknowntehk.firstcraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeGUI implements Listener {
    private final FirstCraftPlugin plugin;
    private final RecipeManager recipeManager;
    private int pageIndex = 0;

    public RecipeGUI(FirstCraftPlugin plugin, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
    }

    public void openRecipesGUI(Player player) {
        List<String> recipes = new ArrayList<>(plugin.getConfig().getConfigurationSection("recipes").getKeys(false));
        if (recipes.size() > 0) {
            displayPage(player, recipes);
        }
    }

    private void displayPage(Player player, List<String> recipes) {
        Inventory gui = Bukkit.createInventory(null, 27, "Crafting Recipes");
        // Assuming only one recipe per page
        String recipeKey = recipes.get(pageIndex);
        Map<Character, ItemStack> ingredients = recipeManager.getIngredients(recipeKey);
        List<String> shape = plugin.getConfig().getStringList("recipes." + recipeKey + ".shape");

        // Clear the inventory first
        gui.clear();

        // Set ingredients in the 3x3 grid
        int[] gridSlots = {2, 3, 4, 11, 12, 13, 20, 21, 22}; // Positions for a 3x3 grid on the left
        int slotIndex = 0;
        for (String line : shape) {
            for (char ingredientChar : line.toCharArray()) {
                if (ingredientChar != ' ') {
                    gui.setItem(gridSlots[slotIndex], ingredients.get(ingredientChar));
                }
                slotIndex++;
            }
        }

        // Set the result item on the right
        ItemStack resultItem = recipeManager.createRecipeItem(recipeKey);
        gui.setItem(15, resultItem); // Output slot to the right of the grid

        // Add navigation buttons if needed
        if (pageIndex > 0) {
            gui.setItem(9, createControlItem("Previous"));
        }
        if (pageIndex < recipes.size() - 1) {
            gui.setItem(17, createControlItem("Next"));
        }

        player.openInventory(gui);
    }

    private ItemStack createControlItem(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Crafting Recipes")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                List<String> recipes = new ArrayList<>(plugin.getConfig().getConfigurationSection("recipes").getKeys(false));
                if ("Next".equals(itemName) && pageIndex < recipes.size() - 1) {
                    pageIndex++;
                    displayPage(player, recipes);
                } else if ("Previous".equals(itemName) && pageIndex > 0) {
                    pageIndex--;
                    displayPage(player, recipes);
                }
            }
        }
    }
}
