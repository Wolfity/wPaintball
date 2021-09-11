package me.wolf.wpaintball.player;

import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.powerup.PowerUp;
import me.wolf.wpaintball.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Data
public class PBPlayer {

    private final PaintballPlugin plugin;

    private final UUID uuid;
    private int kills, killCoins, respawnImmunity;
    private Set<PowerUp> powerUpList;

    public PBPlayer(final UUID uuid, final PaintballPlugin plugin) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.kills = 0;
        this.killCoins = 1;
        this.powerUpList = new HashSet<>();

        final FileConfiguration powerupsCfg = plugin.getFileManager().getPowerUpsConfig().getConfig();
        for (final String powerup : powerupsCfg.getConfigurationSection("powerups").getKeys(false)) {
            final int price = powerupsCfg.getInt("powerups." + powerup + ".price");
            final int duration = powerupsCfg.getInt("powerups." + powerup + ".duration");
            final int defaultDuration = powerupsCfg.getInt("powerups." + powerup + ".duration");
            final boolean isEnabled = powerupsCfg.getBoolean("powerups." + powerup + ".enabled");
            final Material icon = XMaterial.valueOf(powerupsCfg.getString("powerups." + powerup + ".icon")).parseMaterial();
            final String name = Utils.colorize(powerupsCfg.getString("powerups." + powerup + ".name"));
            final String iconDisplay = powerupsCfg.getString("powerups." + powerup + ".icon-display");

            powerUpList.add(new PowerUp(name, price, duration, defaultDuration, icon, iconDisplay, isEnabled));

        }

    }

    @Nullable
    public PowerUp getPowerupByName(final String name) {
        for (final PowerUp powerUp : powerUpList) {
            if (powerUp.getName().equalsIgnoreCase(name)) {
                return powerUp;
            }
        }
        return null;
    }

    public void incrementKillCoins() {
        killCoins++;
    }

    public void incrementKills() {
        kills++;
    }

    public void decrementImmunity() {
        respawnImmunity--;
    }

    public void resetImmunity(final PBPlayer pbPlayer) {
        this.respawnImmunity = plugin.getArenaManager().getArenaByPlayer(pbPlayer).getArenaConfig().getInt("respawn-immunity");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PBPlayer that = (PBPlayer) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
