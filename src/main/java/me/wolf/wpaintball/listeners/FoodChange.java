package me.wolf.wpaintball.listeners;

import me.wolf.wpaintball.PaintballPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodChange implements Listener {

    private final PaintballPlugin plugin;

    public FoodChange(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFood(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (plugin.getPbPlayers().containsKey(event.getEntity().getUniqueId())) {
                ((Player) event.getEntity()).setSaturation(20);
                event.setCancelled(true);
            }
        }
    }

}
