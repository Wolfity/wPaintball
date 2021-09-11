package me.wolf.wpaintball.game;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.arena.ArenaState;
import me.wolf.wpaintball.constants.Constants;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.powerup.PowerUp;
import me.wolf.wpaintball.team.Team;
import me.wolf.wpaintball.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class GameListeners implements Listener {

    private final PaintballPlugin plugin;

    public GameListeners(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    // defining what happens after a player has been hit by a snowball
    @EventHandler
    public void onSnowballHit(final EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Snowball) {
            final LivingEntity shooter = (LivingEntity) ((Snowball) event.getDamager()).getShooter();
            final Player killed = (Player) event.getEntity();

            if (plugin.getPbPlayers().containsKey(shooter.getUniqueId()) && plugin.getPbPlayers().containsKey(killed.getUniqueId())) { // checking if they exist ingame
                if (plugin.getGameManager().getGameState() != GameState.END) {
                    final PBPlayer pbKiller = plugin.getPbPlayers().get(shooter.getUniqueId());
                    final PBPlayer pbKilled = plugin.getPbPlayers().get(killed.getUniqueId());
                    final Arena arena = plugin.getArenaManager().getArenaByPlayer(pbKiller);
                    final Team team = arena.getTeamByPlayer(pbKilled);

                    if (team.getTeamMembers().contains(pbKiller)) return;
                    if (pbKilled.equals(pbKiller)) return;

                    if (!isOnImmunity(pbKilled)) {
                        setImmune(pbKilled);
                        team.decrementLives();
                        pbKiller.incrementKillCoins();
                        pbKiller.incrementKills();
                        sendKillNotification(killed, (Player) shooter, arena);
                        killed.teleport(team.getSpawnLocation().get(0).toBukkitLocation());
                        pbKilled.resetImmunity(pbKilled);

                        restoreInventory(pbKilled, arena);
                        if (team.getLives() <= 0) {
                            if (plugin.getGameManager().getGameState() != GameState.END) {
                                plugin.getGameManager().setGameState(GameState.END, arena);
                            }
                        }

                    } else event.setCancelled(true);

                    event.setCancelled(true);
                }
            }
        }
    }

    // doing some basic checks to allow a player to select their team by clicking the block
    @EventHandler
    public void onTeamSelect(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();

        for (final Arena arena : plugin.getArenas()) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                if (arena.getArenaState() != ArenaState.INGAME) {
                    if (plugin.getPbPlayers().containsKey(player.getUniqueId())) {
                        final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
                        if (player.getItemInHand().getType() == XMaterial.REDSTONE_BLOCK.parseMaterial()) {
                            if (!arena.getTeamList().get(0).getTeamMembers().contains(pbPlayer)) {
                                arena.getTeamList().get(1).removeTeamMember(pbPlayer);
                                arena.getTeamList().get(0).addTeamMember(pbPlayer);
                                updateScoreboard(player, arena);
                                player.sendMessage(Constants.Messages.JOINED_TEAM.replace("{team}", "Red"));
                            } else player.sendMessage(Utils.colorize("&cAlready in this team!"));
                        } else if (player.getItemInHand().getType() == XMaterial.LAPIS_BLOCK.parseMaterial()) {
                            if (!arena.getTeamList().get(1).getTeamMembers().contains(pbPlayer)) {
                                arena.getTeamList().get(0).removeTeamMember(pbPlayer);
                                arena.getTeamList().get(1).addTeamMember(pbPlayer);
                                updateScoreboard(player, arena);
                                player.sendMessage(Constants.Messages.JOINED_TEAM.replace("{team}", "Blue"));
                            } else player.sendMessage(Utils.colorize("&cAlready in this team!"));

                        }
                    }
                }
            }
        }
    }

    // Checking if a player clicks the kill coin, if so open the killstreak menu
    @EventHandler
    public void onKillCoinMenu(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        if (plugin.getPbPlayers().containsKey(player.getUniqueId())) {
            if (player.getItemInHand().getType() == XMaterial.GOLD_NUGGET.parseMaterial()) {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    plugin.getGameUtils().openKillCoinMenu(player);
                }
            }
        }
    }

    @EventHandler
    public void onSnowballPowerups(final ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball && event.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player) event.getEntity().getShooter();
            if (plugin.getPbPlayers().containsKey(shooter.getUniqueId())) {
                // spawn extra snowballs for triple & quintuple shot
                final PBPlayer pbPlayer = plugin.getPbPlayers().get(shooter.getUniqueId());
                if (pbPlayer.getPowerupByName("Triple Shot").isActive()) {
                    extraSnowball(event.getEntity(), shooter, 30D);
                    extraSnowball(event.getEntity(), shooter, -30D);
                } else if (pbPlayer.getPowerupByName("Quintuple Shot").isActive()) {
                    extraSnowball(event.getEntity(), shooter, 30D);
                    extraSnowball(event.getEntity(), shooter, -30D);
                    extraSnowball(event.getEntity(), shooter, 45D);
                    extraSnowball(event.getEntity(), shooter, -45D);
                } else if (pbPlayer.getPowerupByName("Strongarm").isActive()) { // multiplying the snowballs current velocity
                    event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(1.5D));
                } else if (pbPlayer.getPowerupByName("Super Strongarm").isActive()) {
                    event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(2D));
                }

            }

        }
    }

    // activating the powerups and making sure the player has enough killcoins
    @EventHandler
    public void onPowerupSelect(final InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) return;
            if (plugin.getPbPlayers().containsKey(event.getWhoClicked().getUniqueId())) {
                final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
                // looping through the powerup list and detecting which one got clicked.
                pbPlayer.getPowerUpList().forEach(powerUp -> {
                    if (event.getCurrentItem().getType() == powerUp.getIcon()) {
                        if (canAfford(pbPlayer, powerUp)) {
                            if (!powerUp.isActive()) {
                                player.sendMessage(Constants.Messages.ACTIVATED_POWERUP.replace("{powerup}", powerUp.getName()));
                                activatePowerup(pbPlayer, powerUp.getName());
                                player.playSound(player.getLocation(), XSound.BLOCK_ANVIL_USE.parseSound(), 1f, 1f);
                                pbPlayer.setKillCoins(pbPlayer.getKillCoins() - powerUp.getPrice());
                            } else {
                                player.sendMessage(Constants.Messages.POWERUP_ISACTIVE);
                                player.playSound(player.getLocation(), XSound.ENTITY_ZOMBIE_DEATH.parseSound(), 1f, 1f);
                            }
                        } else {
                            player.sendMessage(Constants.Messages.CAN_NOT_AFFORD);
                            player.playSound(player.getLocation(), XSound.ENTITY_VILLAGER_NO.parseSound(), 1f, 1f);
                        }
                    }
                });
            }
        }
    }

    //updating the lobby scoreboard
    private void updateScoreboard(final Player player, final Arena arena) {
        plugin.getScoreboard().lobbyScoreboard(player, arena);
    }

    // Sending the notification that someone has been killed to all players in the same arena
    private void sendKillNotification(final Player killed, final Player shooter, final Arena arena) {
        arena.getArenaMembers().stream().filter(Objects::nonNull).forEach(pbPlayer -> {
            final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
            player.sendMessage(Utils.colorize("&b" + killed.getName() + " &2was shot by &b" + shooter.getName()));
        });
    }

    // Checking if a player has an active immunity timer
    private boolean isOnImmunity(final PBPlayer pbKilled) {
        return pbKilled.getRespawnImmunity() > 0;
    }

    // set a players spawn immunity when killed
    private void setImmune(final PBPlayer pbKilled) {
        pbKilled.resetImmunity(pbKilled);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pbKilled.getRespawnImmunity() > 0) {
                    pbKilled.decrementImmunity();
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // in this method we run some basic checks to see if a power up is active, if not, activate it, create timers for the duration of the powerup
    private void activatePowerup(final PBPlayer pbPlayer, final String powerupName) {
        final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
        final PowerUp powerUp = pbPlayer.getPowerupByName(powerupName);
        final Arena arena = plugin.getArenaManager().getArenaByPlayer(pbPlayer);
        final Team team = arena.getTeamByPlayer(pbPlayer);
        switch (powerupName) {
            case "Triple Shot":
                powerUp.setActive(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (powerUp.getDuration() > 0) {
                            powerUp.decrementDuration();
                        } else {
                            this.cancel();
                            powerUp.setActive(false);
                            powerUp.setDuration(powerUp.getDefaultDuration());
                            player.sendMessage(Utils.colorize("&cPowerup Expired!"));
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
                break;
            case "Quintuple Shot":
                powerUp.setActive(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (powerUp.getDuration() > 0) {
                            powerUp.decrementDuration();
                        } else {
                            this.cancel();
                            powerUp.setActive(false);
                            powerUp.setDuration(powerUp.getDefaultDuration());
                            player.sendMessage(Utils.colorize("&cPowerup Expired!"));
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
                break;
            case "+3":
                team.setLives(team.getLives() + 3);
                break;
            case "+5":
                team.setLives(team.getLives() + 5);
                break;
            case "Strongarm":
                powerUp.setActive(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (powerUp.getDuration() > 0) {
                            powerUp.decrementDuration();
                        } else {
                            this.cancel();
                            powerUp.setActive(false);
                            powerUp.setDuration(powerUp.getDefaultDuration());
                            player.sendMessage(Utils.colorize("&cPowerup Expired!"));
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
                break;
            case "Super Strongarm":
                powerUp.setActive(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (powerUp.getDuration() > 0) {
                            powerUp.decrementDuration();
                        } else {
                            this.cancel();
                            powerUp.setActive(false);
                            powerUp.setDuration(powerUp.getDefaultDuration());
                            player.sendMessage(Utils.colorize("&cPowerup Expired!"));
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
                break;
            case "ammo":
                player.getInventory().addItem(Utils.createItem(XMaterial.SNOWBALL.parseMaterial(), "Paintball", 16));
                break;
            case "nuke":
                powerUp.setActive(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (powerUp.getDuration() > 0) {
                            powerUp.decrementDuration();
                            arena.getArenaMembers().forEach(pbPlayer -> {
                                final Player allPlayers = Bukkit.getPlayer(pbPlayer.getUuid());
                                allPlayers.playSound(allPlayers.getLocation(), XSound.ENTITY_CREEPER_PRIMED.parseSound(), 1f, 1f);
                                allPlayers.sendMessage(Utils.colorize("&c" + player.getDisplayName() + "'s &bnuke impacts in &3" + powerUp.getDuration() + " &bseconds!"));
                            });
                        } else {
                            final Team enemyTeam = arena.getEnemyTeam(pbPlayer); // filtering out the people who are on respawn immunity
                            enemyTeam.getTeamMembers().stream().filter(enemyPlayer -> enemyPlayer.getRespawnImmunity() == 0).forEach(enemyPlayer -> {
                                final Player enemy = Bukkit.getPlayer(enemyPlayer.getUuid());
                                enemy.getWorld().createExplosion(enemy.getLocation(), 0, false, false);
                                enemy.teleport(enemyTeam.getSpawnLocation().get(0).toBukkitLocation());
                                enemy.playSound(enemy.getLocation(), XSound.ENTITY_DRAGON_FIREBALL_EXPLODE.parseSound(), 1f, 1f);

                                enemyTeam.setLives(enemyTeam.getLives() - enemyTeam.getTeamMembers().size());
                                pbPlayer.setKills(pbPlayer.getKills() + enemyTeam.getTeamMembers().size());
                                pbPlayer.setKillCoins(pbPlayer.getKillCoins() + enemyTeam.getTeamMembers().size());
                                if (enemyTeam.getLives() <= 0) {
                                    plugin.getGameManager().setGameState(GameState.END, arena);
                                }
                            });
                            this.cancel();
                            powerUp.setActive(false);
                            powerUp.setDuration(powerUp.getDefaultDuration());
                            player.sendMessage(Utils.colorize("&cPowerup Expired!"));
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    // checking if the player has enough killcoins to afford the powerup
    private boolean canAfford(final PBPlayer pbPlayer, final PowerUp powerUp) {
        return pbPlayer.getKillCoins() >= powerUp.getPrice();
    }

    // spawns an extra snowball in a set xVelocity. Used for triple and quintuple shot
    private void extraSnowball(final Entity entity, final Player shooter, final double xVelocity) {
        final Snowball snowball = shooter.getWorld().spawn(shooter.getEyeLocation(), Snowball.class);
        snowball.setVelocity(entity.getVelocity().setX(Math.toRadians(xVelocity)));
        snowball.setShooter(shooter);
    }

    // restore inventory after dying
    private void restoreInventory(final PBPlayer pbPlayer, final Arena arena) {
        final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
        player.getInventory().clear();
        for (int i = 0; i < 4; i++) {
            player.getInventory().addItem(Utils.createItem(XMaterial.SNOWBALL.parseMaterial(), "Paintball", 16));
        }

        final Team team = arena.getTeamByPlayer(pbPlayer);
        player.getInventory().setHelmet(team.getIcon());
        player.getInventory().setItem(8, Utils.createItem(XMaterial.GOLD_NUGGET.parseMaterial(), "&eKillCoins", 1));
    }
}
