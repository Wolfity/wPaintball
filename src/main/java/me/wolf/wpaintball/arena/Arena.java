package me.wolf.wpaintball.arena;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.team.Team;
import me.wolf.wpaintball.utils.CustomLocation;
import me.wolf.wpaintball.utils.Utils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("ConstantConditions")
@Getter
public class Arena {

    private final PaintballPlugin plugin;

    private final String name;
    @Setter
    private ArenaState arenaState = ArenaState.READY;
    @Setter
    private CustomLocation waitingRoomLoc;
    private FileConfiguration arenaConfig;
    private final Set<PBPlayer> arenaMembers = new HashSet<>();
    private final List<Team> teamList;


    @Setter
    private int lobbyCountdown, gameTimer, minPlayer, maxPlayers, newPaintball, respawnImmunity;


    public File arenaConfigFile;

    protected Arena(final String name, final int lobbyCountdown, final int gameTimer, final int minPlayer, final int maxPlayers, final int newPaintball, final int respawnImmunity, final PaintballPlugin plugin) {
        this.plugin = plugin;
        this.name = name;
        createConfig(name);
        this.lobbyCountdown = lobbyCountdown;
        this.gameTimer = gameTimer;
        this.minPlayer = minPlayer;
        this.maxPlayers = maxPlayers;
        this.newPaintball = newPaintball;
        this.respawnImmunity = respawnImmunity;


        this.teamList = Arrays.asList(
                new Team(plugin, "Red", plugin.getConfig().getInt("team-lives"), Utils.createItem(XMaterial.REDSTONE_BLOCK.parseMaterial(), "&cRed Team", 1)),
                new Team(plugin, "Blue", plugin.getConfig().getInt("team-lives"), Utils.createItem(XMaterial.LAPIS_BLOCK.parseMaterial(), "&9Blue Team", 1)));
    }


    public void saveArena(final String arenaName) {
        arenaConfig.set("LobbySpawn", waitingRoomLoc.serialize());

        int i = 1;
        for (final CustomLocation location : plugin.getArenaManager().getArena(arenaName).getTeamList().get(0).getSpawnLocation()) {
            this.arenaConfig.set("Red-Spawn-Locations." + i, location.serialize());
            i++;
        }
        int j = 1;
        for (final CustomLocation location : plugin.getArenaManager().getArena(arenaName).getTeamList().get(1).getSpawnLocation()) {
            this.arenaConfig.set("Blue-Spawn-Locations." + j, location.serialize());
            j++;
        }


        try {
            arenaConfig.save(arenaConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createConfig(final String cfgName) {
        arenaConfigFile = new File(plugin.getDataFolder() + "/arenas", cfgName.toLowerCase() + ".yml");
        arenaConfig = new YamlConfiguration();
        try {
            arenaConfig.load(arenaConfigFile);
            arenaConfig.save(arenaConfigFile);
        } catch (IOException | InvalidConfigurationException ignore) {

        }
        if (!arenaConfigFile.exists()) {
            arenaConfigFile.getParentFile().mkdirs();
            try {
                arenaConfigFile.createNewFile();
                arenaConfig.load(arenaConfigFile);
                arenaConfig.set("min-players", 1);
                arenaConfig.set("max-players", 5);
                arenaConfig.set("lobby-countdown", 10);
                arenaConfig.set("new-paintball-per-time", 6);
                arenaConfig.set("game-timer", 600);
                arenaConfig.set("respawn-immunity", 5);
                arenaConfig.save(arenaConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void decrementGameTimer() {
        gameTimer--;
    }

    public void decrementLobbyCountdown() {
        lobbyCountdown--;
    }

    public void resetGameTimer() {
        this.gameTimer = arenaConfig.getInt("game-timer") + 1;
    }

    public void resetLobbyCountdownTimer() {
        this.lobbyCountdown = arenaConfig.getInt("lobby-countdown") + 1;
    }

    public void decrementNewPaintballTimer() {
        newPaintball--;
    }

    public void resetNewPaintballTimer() {
        this.newPaintball = arenaConfig.getInt("new-paintball-per-time");
    }


    // getting the team by it's name
    public Team getTeamByName(final String name) {
        for (final Team team : getTeamList()) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    // getting the name of  the team by passing in a custom player objectt
    public String getTeamNameByPlayer(final PBPlayer pbPlayer) {
        for (final Team team : getTeamList()) {
            if (team.getTeamMembers().contains(pbPlayer)) {
                return team.getName();
            }
        }
        return "None";
    }

    // get The team object by passing in a custom player object
    public Team getTeamByPlayer(final PBPlayer pbPlayer) {
        for (final Team team : getTeamList()) {
            if (team.getTeamMembers().contains(pbPlayer)) {
                return team;
            }
        }
        return null;
    }

    //get the team the player is not in
    public Team getEnemyTeam(final PBPlayer pbPlayer) {
        for (final Team team : getTeamList()) {
            if (!team.getTeamMembers().contains(pbPlayer)) {
                return team;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return name.equals(arena.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}