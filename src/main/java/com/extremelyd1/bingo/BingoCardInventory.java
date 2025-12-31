package com.extremelyd1.bingo;

import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.game.team.PlayerTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Bukkit inventory with the bingo card items.
 */
public class BingoCardInventory {

    /**
     * The Bukkit inventory to store the items.
     */
    private final Inventory inventory;

    // fallen's fork: better bingo item display
    private BingoItem[][] bingoItems;

    /**
     * Create a bingo card inventory with the given bingo card.
     * @param bingoItems The bingo card items to make the inventory from.
     */
    public BingoCardInventory(BingoItem[][] bingoItems) {
        // Create the inventory and set the items in it
        inventory = Bukkit.createInventory(
                null,
                9 * 5,
                Component.text("Bingo Card").color(NamedTextColor.AQUA)
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
                List<Component> lores = new ArrayList<>();
                if (teams.isEmpty()) {
                    lores.add(Component.text("Collected by no team").color(NamedTextColor.GRAY));
                } else {
                    var text = Component.text("Collected by:").color(NamedTextColor.GRAY);
                    for (PlayerTeam team : teams) {
                        text = text.append(Component
                                .text("- ")
                                .color(NamedTextColor.DARK_GRAY)
                                .append(Component.text(team.getName()).color(team.getColor()))
                                .append(Component.text(" team").color(NamedTextColor.GRAY))
                        );
                    }
                    lores.add(text);
                }
                itemStack.lore(lores);
                if (!teams.isEmpty()) {
                    itemStack.setAmount(teams.size() + 10);
                }
                // fallen's fork: better bingo item display ends

                inventory.setItem(y * 9 + x + 2, itemStack);
            }
        }
    }

    /**
     * Show the given player this inventory.
     * @param player The player to show the inventory to.
     */
    public void show(Player player) {
        player.openInventory(inventory);
    }

    /**
     * Whether the given inventory is the bingo card inventory.
     * @param inventory The inventory to check.
     * @return True if the given inventory is the bingo card inventory.
     */
    public boolean isBingoCard(Inventory inventory) {
        return this.inventory == inventory;
    }
}
