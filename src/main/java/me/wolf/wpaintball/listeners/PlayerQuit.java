package me.wolf.wpaintball.listeners;

import me.wolf.wpaintball.PaintballPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    private final PaintballPlugin plugin;

    public PlayerQuit(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (plugin.getPbPlayers().containsKey(player.getUniqueId())) {
            plugin.getGameManager().removePlayer(player);
        }
    }

}
