package me.wolf.wpaintball.listeners;

import me.wolf.wpaintball.PaintballPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    private final PaintballPlugin plugin;

    public BlockPlace(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent event) {
        if (plugin.getPbPlayers().containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }


}
