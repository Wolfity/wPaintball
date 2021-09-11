package me.wolf.wpaintball.commands.impl;

import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.commands.BaseCommand;
import me.wolf.wpaintball.constants.Constants;
import me.wolf.wpaintball.game.GameState;
import me.wolf.wpaintball.team.Team;
import me.wolf.wpaintball.utils.CustomLocation;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("ConstantConditions")
public class PaintballCommand extends BaseCommand {

    private final PaintballPlugin plugin;

    public PaintballCommand(final PaintballPlugin plugin) {
        super("pb");
        this.plugin = plugin;

    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        final Player player = (Player) sender;

        if (args.length < 1 || args.length > 2) {
            tell(Constants.Messages.HELP);
        }
        if (isAdmin()) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("admin")) {
                    tell(Constants.Messages.ADMIN_HELP);
                } else if (args[0].equalsIgnoreCase("setworldspawn")) {
                    plugin.getConfig().set("WorldSpawn", player.getLocation());
                    plugin.saveConfig();
                    tell(Constants.Messages.SET_WORLD_SPAWN);
                }
            } else if (args.length == 2) {
                final String arenaName = args[1];
                if (args[0].equalsIgnoreCase("createarena")) {
                    plugin.getArenaManager().createArena(arenaName);
                    tell(Constants.Messages.ARENA_CREATED.replace("{arena}", arenaName));
                } else if (args[0].equalsIgnoreCase("deletearena")) {
                    if (plugin.getArenaManager().getArena(arenaName) != null) {
                        plugin.getArenaManager().deleteArena(arenaName);
                    } else tell(Constants.Messages.ARENA_NOT_FOUND);
                } else if (args[0].equalsIgnoreCase("setlobby")) {
                    setArenaLobby(player, arenaName);
                } else if (args[0].equalsIgnoreCase("setredspawn")) {
                    setGameSpawn(player, arenaName, "red");
                } else if (args[0].equalsIgnoreCase("setbluespawn")) {
                    setGameSpawn(player, arenaName, "blue");
                } else if (args[0].equalsIgnoreCase("forcestart")) {
                    plugin.getGameManager().setGameState(GameState.LOBBY_COUNTDOWN, plugin.getArenaManager().getArena(arenaName));
                } else if (args[0].equalsIgnoreCase("tp")) {
                    if (plugin.getArenaManager().getArena(arenaName) != null) {
                        player.teleport(plugin.getArenaManager().getArena(arenaName).getWaitingRoomLoc().toBukkitLocation());
                        tell(Constants.Messages.TELEPORTED_TO_ARENA);
                    } else tell(Constants.Messages.ARENA_NOT_FOUND);
                }
            }
        } else tell(Constants.Messages.NO_PERMISSION);
        if (args.length == 2) {
            final String arenaName = args[1];
            if (args[0].equalsIgnoreCase("help")) {
                tell(Constants.Messages.HELP);
            } else if (args[0].equalsIgnoreCase("join")) {
                if (plugin.getArenaManager().getArena(arenaName) != null) {
                    plugin.getGameManager().addPlayer(player, plugin.getArenaManager().getArena(arenaName));
                }
            }
        } else if (args.length == 1 && args[0].

                equalsIgnoreCase("leave")) {
            plugin.getGameManager().removePlayer(player);
        }
    }

    // setting a game spawn for a team
    private void setGameSpawn(final Player player, final String arenaName, final String teamColor) {
        final Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena != null) {
            if (!plugin.getArenaManager().isGameActive(arena)) {
                if (teamColor.equalsIgnoreCase("red")) {
                    final Team team = arena.getTeamByName("red");
                    team.getSpawnLocation().add(CustomLocation.fromBukkitLocation(player.getLocation()));
                    int i = 1;
                    for (CustomLocation location : team.getSpawnLocation()) {
                        arena.getArenaConfig().set("Red-Spawn-Locations." + i, location.serialize());
                        i++;
                    }
                    tell(Constants.Messages.SET_GAME_SPAWN);
                } else if (teamColor.equalsIgnoreCase("blue")) {
                    final Team team = arena.getTeamByName("blue");
                    team.getSpawnLocation().add(CustomLocation.fromBukkitLocation(player.getLocation()));
                    int i = 1;
                    for (CustomLocation location : team.getSpawnLocation()) {
                        this.plugin.getArenaManager().getArena(arenaName).getArenaConfig().set("Blue-Spawn-Locations." + i, location.serialize());
                        i++;
                    }
                    tell(Constants.Messages.SET_GAME_SPAWN);
                }
                arena.saveArena(arenaName);
                Bukkit.getWorld(player.getWorld().getName()).save();
            } else tell(Constants.Messages.CAN_NOT_MODIFY);
        } else tell(Constants.Messages.ARENA_NOT_FOUND);
    }

    // setting the lobby spawn for the specified arena
    private void setArenaLobby(final Player player, final String arenaName) {
        if (plugin.getArenaManager().getArena(arenaName) != null) {
            final Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (!plugin.getArenaManager().isGameActive(arena)) {
                arena.getArenaConfig().set("LobbySpawn", player.getLocation().serialize());
                arena.setWaitingRoomLoc(CustomLocation.fromBukkitLocation(player.getLocation()));
                arena.saveArena(arenaName);
                tell(Constants.Messages.SET_LOBBY_SPAWN);
            } else tell(Constants.Messages.CAN_NOT_MODIFY);
        } else tell(Constants.Messages.ARENA_NOT_FOUND);
    }
}
