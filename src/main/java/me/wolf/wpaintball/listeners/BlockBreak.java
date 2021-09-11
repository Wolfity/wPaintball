package me.wolf.wpaintball.listeners;

import me.wolf.wpaintball.PaintballPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    private final PaintballPlugin plugin;

    public BlockBreak(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(final BlockBreakEvent event) {
        if (plugin.getPbPlayers().containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
