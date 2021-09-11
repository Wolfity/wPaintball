package me.wolf.wpaintball.listeners;

import me.wolf.wpaintball.PaintballPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryListeners implements Listener {

    private final PaintballPlugin plugin;

    public InventoryListeners(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (plugin.getPbPlayers().containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(final PlayerPickupItemEvent event) {
        if (plugin.getPbPlayers().containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    // players cant move items in their inventory whilst ingame
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (plugin.getPbPlayers().containsKey(event.getWhoClicked().getUniqueId())) {
                final List<ItemStack> items = new ArrayList<>();
                items.add(event.getCurrentItem());
                items.add(event.getCursor());
                items.add((event.getClick() == ClickType.NUMBER_KEY) ?
                        event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem());
                for (ItemStack item : items) {
                    if (item != null)
                        event.setCancelled(true);
                }
            }
        }
    }
}
