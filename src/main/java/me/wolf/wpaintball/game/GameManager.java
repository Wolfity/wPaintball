package me.wolf.wpaintball.game;

import lombok.Getter;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.arena.ArenaState;
import me.wolf.wpaintball.constants.Constants;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.team.Team;
import me.wolf.wpaintball.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("ConstantConditions")
public class GameManager {

    private final PaintballPlugin plugin;

    public GameManager(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    @Getter
    private GameState gameState;

    // easily managing gamestates and arena states
    public void setGameState(final GameState gameState, final Arena arena) {
        this.gameState = gameState;
        switch (gameState) {
            case RECRUITING: // game is looking for players
                arena.setArenaState(ArenaState.READY);
                enoughPlayers(arena);
                break;
            case LOBBY_COUNTDOWN: // minimum amount of players are reached, counting down till game start
                arena.setArenaState(ArenaState.COUNTDOWN);
                lobbyCountdown(arena);
                break;
            case ACTIVE: // the game has begun
                arena.setArenaState(ArenaState.INGAME);
                gameTimer(arena);
                break;
            case END: // the game timer ran out or a team ran out of lives, wait 10 seconds before completely warping everyone out
                arena.setArenaState(ArenaState.END);
                sendGameEndNotification(arena);
                Bukkit.getScheduler().runTaskLater(plugin, () -> endGame(arena), 200L);
                break;
        }
    }

    // handles the countdown timer in the lobby, if it's over, start the game
    private void lobbyCountdown(final Arena arena) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.LOBBY_COUNTDOWN) {
                    this.cancel();
                }
                if (arena.getLobbyCountdown() > 0) {
                    arena.decrementLobbyCountdown();
                    arena.getArenaMembers().stream().filter(Objects::nonNull).forEach(pbPlayer -> {
                        final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
                        player.sendMessage(Constants.Messages.LOBBY_COUNTDOWN.replace("{countdown}", String.valueOf(arena.getLobbyCountdown())));
                    });
                } else {
                    this.cancel();
                    arena.resetLobbyCountdownTimer();
                    setGameState(GameState.ACTIVE, arena);
                    arena.getArenaMembers().forEach(pbPlayer -> {
                        final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
                        player.sendMessage(Constants.Messages.GAME_STARTED);
                        teleportToSpawns(arena);
                        plugin.getGameUtils().giveGameInventory(player);
                    });
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // the general timer of the game, handles game duration per arena
    private void gameTimer(final Arena arena) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.ACTIVE) {
                    this.cancel();
                }
                if (arena.getGameTimer() > 0) {
                    arena.decrementGameTimer();
                    arena.getArenaMembers().
                            stream().
                            filter(Objects::nonNull).forEach(pbPlayer -> plugin.getScoreboard().gameScoreboard(Bukkit.getPlayer(pbPlayer.getUuid()), arena));
                } else {
                    this.cancel();
                    setGameState(GameState.END, arena);
                    arena.resetGameTimer();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // Ending the game, teleporting players to the world, clearing the Sets/Lists , etc
    private void endGame(final Arena arena) {
        arena.getArenaMembers().stream().filter(Objects::nonNull).forEach(pbPlayer -> {

            final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
            player.getInventory().clear();
            teleportToWorld(arena);

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.SURVIVAL);


            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            player.getInventory().setHelmet(null);
            plugin.getPbPlayers().remove(player.getUniqueId());


        });
        //clearing out the team lists & arena members + resetting both teams lives, so the game can properly restart
        arena.getTeamList().forEach(team -> team.getTeamMembers().clear());
        arena.getTeamList().forEach(team -> team.resetTeamLives(arena));
        arena.getArenaMembers().clear();
        arena.resetGameTimer();
        setGameState(GameState.RECRUITING, arena);
    }

    // Check if there are enough players when the game is in the recruiting stage, if so, start the lobby countdown
    private void enoughPlayers(final Arena arena) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState == GameState.RECRUITING) {
                    if (arena.getArenaMembers().size() >= arena.getArenaConfig().getInt("min-players")) {
                        setGameState(GameState.LOBBY_COUNTDOWN, arena);
                    } else {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // after the game has ended, teleport all players back to the "world spawn"
    private void teleportToWorld(final Arena arena) {
        arena.getArenaMembers().forEach(pbPlayer -> {
            final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
            final Location worldLoc = (Location) plugin.getConfig().get("WorldSpawn");
            player.teleport(worldLoc);
            plugin.getPbPlayers().remove(player.getUniqueId());
        });
    }

    // teleport the players to their team spawns
    private void teleportToSpawns(final Arena arena) {
        for (final Team team : arena.getTeamList()) {
            team.getTeamMembers().forEach(pbPlayer -> {
                final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
                player.teleport(team.getSpawnLocation().get(0).toBukkitLocation());
            });
        }
    }

    public void teleportToLobby(final Player player, final Arena arena) {
        player.teleport(arena.getWaitingRoomLoc().toBukkitLocation());
    }

    // Here we add a player to an arena, and create a custom object
    public void addPlayer(final Player player, final Arena arena) {

        if (!plugin.getArenaManager().isGameActive(arena)) {
            if (!arena.getArenaMembers().contains(plugin.getPbPlayers().get(player.getUniqueId()))) {
                if (arena.getArenaMembers().isEmpty()) {
                    setGameState(GameState.RECRUITING, arena);
                }
                if (arena.getArenaMembers().size() <= arena.getArenaConfig().getInt("max-players")) {

                    plugin.getPbPlayers().put(player.getUniqueId(), new PBPlayer(player.getUniqueId(), plugin));
                    final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
                    arena.getArenaMembers().add(pbPlayer);

                    assignRandomTeam(player);
                    plugin.getScoreboard().lobbyScoreboard(player, arena);
                    teleportToLobby(player, arena);
                    plugin.getGameUtils().giveLobbyInventory(player);

                    enoughPlayers(arena);
                    player.sendMessage(Constants.Messages.JOINED_ARENA.replace("{arena}", arena.getName()));
                } else player.sendMessage(Constants.Messages.ARENA_IS_FULL);
            } else player.sendMessage(Constants.Messages.ALREADY_IN_ARENA);
        } else player.sendMessage(Constants.Messages.GAME_IN_PROGRESS);
    }

    // remove a player from the game, teleport them, clear the custom player object
    public void removePlayer(final Player player) {
        for (final Arena arena : plugin.getArenas()) {
            if (arena.getArenaMembers().contains(plugin.getPbPlayers().get(player.getUniqueId()))) {
                final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
                arena.getArenaMembers().remove(pbPlayer);
                plugin.getPbPlayers().remove(player.getUniqueId());

                player.teleport((Location) plugin.getConfig().get("WorldSpawn"));
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                player.getInventory().clear();
                player.sendMessage(Constants.Messages.LEFT_ARENA.replace("{arena}", arena.getName()));

                leaveGameCheck(arena);
                arena.getArenaMembers()
                        .stream().filter(Objects::nonNull)
                        .forEach(arenaMember -> Bukkit.getPlayer(arenaMember.getUuid()).sendMessage(Constants.Messages.PLAYER_LEFT_GAME.replace("{player}", player.getDisplayName())));
            } else player.sendMessage(Constants.Messages.NOT_IN_ARENA);
        }
    }

    // if a player leaves the game, check if he was the only one in there, if so reset to the appropriate gamestate
    private void leaveGameCheck(final Arena arena) {
        if (gameState == GameState.ACTIVE) {
            if (arena.getArenaMembers().size() <= 1) {
                setGameState(GameState.END, arena);
                arena.resetLobbyCountdownTimer();
                arena.resetGameTimer();
                arena.getTeamList().forEach(team -> team.getTeamMembers().clear());
            }
        } else if (gameState == GameState.LOBBY_COUNTDOWN) {
            if (arena.getArenaMembers().size() <= 1) {
                setGameState(GameState.RECRUITING, arena);
                arena.resetLobbyCountdownTimer();
                arena.resetGameTimer();
                arena.getTeamList().forEach(team -> team.getTeamMembers().clear());
            }
        }
    }

    private void sendGameEndNotification(final Arena arena) {
        arena.getArenaMembers().stream().filter(Objects::nonNull).forEach(pbPlayer -> {
            final Player player = Bukkit.getPlayer(pbPlayer.getUuid());
            player.sendMessage(Constants.Messages.GAME_ENDED
                    .replace("{topkiller}", Bukkit.getPlayer(getTopKiller().get(0).getUuid()).getDisplayName())
                    .replace("{kills}", String.valueOf(getTopKiller().get(0).getKills()))
                    .replace("{winningteam}", Utils.getWinningTeam(arena).getName()));
        });
    }

    private void assignRandomTeam(final Player player) {
        final int randomNumber = new Random().nextInt(2);
        final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());
        final Arena arena = plugin.getArenaManager().getArenaByPlayer(pbPlayer);
        if (randomNumber == 0) {
            arena.getTeamList().get(0).addTeamMember(pbPlayer);
            player.sendMessage(Constants.Messages.JOINED_TEAM.replace("{team}", arena.getTeamList().get(0).getName()));
        } else {
            arena.getTeamList().get(1).addTeamMember(pbPlayer);
            player.sendMessage(Constants.Messages.JOINED_TEAM.replace("{team}", arena.getTeamList().get(1).getName()));
        }
    }

    private List<PBPlayer> getTopKiller() {
        final List<PBPlayer> pbPlayerList = new ArrayList<>(plugin.getPbPlayers().values());
        pbPlayerList.sort(Comparator.comparing(PBPlayer::getKills));

        Collections.reverse(pbPlayerList);
        return pbPlayerList.stream().limit(1).collect(Collectors.toList());
    }

}
