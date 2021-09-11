package me.wolf.wpaintball.game;

import com.cryptomorin.xseries.XMaterial;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.powerup.PowerUp;
import me.wolf.wpaintball.team.Team;
import me.wolf.wpaintball.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("ConstantConditions")
public class GameUtils {

    private final PaintballPlugin plugin;

    public GameUtils(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    // the inventory a player who joins the lobby receives (blocks to choose a team)
    public void giveLobbyInventory(final Player player) {
        player.getInventory().clear();
        final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
        final Arena arena = plugin.getArenaManager().getArenaByPlayer(pbPlayer);

        arena.getTeamList().forEach(team -> player.getInventory().addItem(team.getIcon()));
    }

    // set the player up to go in a game
    public void giveGameInventory(final Player player) {
        final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
        final Arena arena = plugin.getArenaManager().getArenaByPlayer(pbPlayer);

        player.getInventory().clear();
        for (int i = 0; i < 4; i++) {
            player.getInventory().addItem(Utils.createItem(XMaterial.SNOWBALL.parseMaterial(), "Paintball", 16));
        }

        final Team team = arena.getTeamByPlayer(pbPlayer);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setHelmet(team.getIcon());
        player.getInventory().setItem(8, Utils.createItem(XMaterial.GOLD_NUGGET.parseMaterial(), "&eKillCoins", 1));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGameManager().getGameState() == GameState.ACTIVE) {
                    player.getInventory().setItem(8, Utils.createItem(XMaterial.GOLD_NUGGET.parseMaterial(), "&eKillCoins", pbPlayer.getKillCoins())); //updating the killcoins every 20 ticks
                    if (arena.getNewPaintball() > 0) {
                        arena.decrementNewPaintballTimer();
                    } else { // every 6 seconds (configurable) a new paintball will be given
                        player.getInventory().addItem(Utils.createItem(XMaterial.SNOWBALL.parseMaterial(), "Paintball", 1));
                        arena.resetNewPaintballTimer();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // the menu that handles the kill coins and powerup system
    public void openKillCoinMenu(final Player player) {
        final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
        final Inventory inventory = Bukkit.createInventory(null, 27, Utils.colorize("&bKillCoin Shop"));
        pbPlayer.getPowerUpList().stream().filter(PowerUp::isEnabled).forEach(powerUp ->
                inventory.addItem(Utils.createItem(powerUp.getIcon(), powerUp.getIconDisplay() + " &e(&6" + powerUp.getPrice() + " &eKillCoins)", 1)));

        player.openInventory(inventory);
    }

}
