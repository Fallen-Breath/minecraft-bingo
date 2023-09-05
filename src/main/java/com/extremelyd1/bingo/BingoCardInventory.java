package com.extremelyd1.bingo;

import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Bukkit inventory with the bingo card items
 */
public class BingoCardInventory {

    /**
     * The Bukkit inventory to store the items
     */
    private final Inventory inventory;

    // fallen's fork: better bingo item display
    private BingoItem[][] bingoItems;

    /**
     * Create a bingo card inventory with the given bingo card
     * @param bingoItems The bingo card items to make the inventory from
     */
    public BingoCardInventory(BingoItem[][] bingoItems) {
        // Create the inventory and set the items in it
        inventory = Bukkit.createInventory(
                null,
                9 * 5,
                ChatColor.AQUA + "Bingo Card"
        );

        // fallen's fork: better bingo item display
        this.bingoItems = bingoItems;
        this.rebuildInventory();
    }

    public void rebuildInventory() {
        for (int y = 0; y < BingoCard.BOARD_SIZE; y++) {
            for (int x = 0; x < BingoCard.BOARD_SIZE; x++) {
                Material material = bingoItems[y][x].getMaterial();
                ItemStack itemStack = new ItemStack(material, 1);

                // fallen's fork: better bingo item display
                List<PlayerTeam> teams = new ArrayList<>();
                bingoItems[y][x].getCollectors().forEach(teams::add);
                List<String> lores = new ArrayList<>();
                if (teams.isEmpty()) {
                    lores.add(ChatColor.GRAY + "Collected by no team");
                } else {
                    lores.add(ChatColor.GRAY + "Collected by:");
                    for (PlayerTeam team : teams) {
                        lores.add(ChatColor.DARK_GRAY + "- " + team.getColor() + team.getName() + ChatColor.GRAY + " team");
                    }
                }
                itemStack.setLore(lores);
                if (!teams.isEmpty()) {
                    itemStack.setAmount(teams.size() + 10);
                }
                // fallen's fork: better bingo item display ends

                inventory.setItem(y * 9 + x + 2, itemStack);
            }
        }
    }

    /**
     * Show the given player this inventory
     * @param player The player to show the inventory to
     */
    public void show(Player player) {
        player.openInventory(inventory);
    }

}
