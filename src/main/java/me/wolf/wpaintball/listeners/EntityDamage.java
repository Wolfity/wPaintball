package me.wolf.wpaintball.listeners;

import me.wolf.wpaintball.PaintballPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    private final PaintballPlugin plugin;

    public EntityDamage(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (plugin.getPbPlayers().containsKey(event.getEntity().getUniqueId()) || plugin.getPbPlayers().containsKey(event.getDamager().getUniqueId())) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    event.setCancelled(true);
                }
            }
        }
    }


}
