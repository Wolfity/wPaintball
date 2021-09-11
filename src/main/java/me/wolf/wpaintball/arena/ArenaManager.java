package me.wolf.wpaintball.arena;

import lombok.Getter;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.utils.CustomLocation;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
@Getter
public final class ArenaManager {

    private final PaintballPlugin plugin;


    public ArenaManager(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    public Arena createArena(final String arenaName) {
        for (final Arena arena : plugin.getArenas())
            if (arena.getName().equalsIgnoreCase(arenaName))
                return getArena(arenaName);

        final Arena arena = new Arena(arenaName, 10, 600, 1, 5, 5, 5, plugin);
        plugin.getArenas().add(arena);
        return arena;
    }


    public void deleteArena(final String name) {
        final Arena arena = getArena(name);
        if (arena == null) return;

        arena.getArenaConfigFile().delete();
        plugin.getArenas().remove(arena);

    }

    // get an arena by passing in it's name
    public Arena getArena(final String name) {
        for (final Arena arena : plugin.getArenas())
            if (arena.getName().equalsIgnoreCase(name))
                return arena;

        return null;
    }

    // getting an arena by passing in a player, looping over all arenas to see if the player is in there
    public Arena getArenaByPlayer(final PBPlayer pbPlayer) {
        for (final Arena arena : plugin.getArenas()) {
            if (arena.getArenaMembers().contains(pbPlayer)) {
                return arena;
            }
        }
        return null;
    }

    public Arena getFreeArena() {
        return plugin.getArenas().stream().filter(arena -> arena.getArenaState() == ArenaState.READY).findFirst().orElse(null);
    }

    public boolean isGameActive(final Arena arena) {
        return arena.getArenaState() == ArenaState.INGAME ||
                arena.getArenaState() == ArenaState.END ||
                arena.getArenaState() == ArenaState.COUNTDOWN;
    }

    public void loadArenas() {
        final File folder = new File(plugin.getDataFolder() + "/arenas");

        if (folder.listFiles() == null) {
            Bukkit.getLogger().info("&3No arenas has been found!");
            return;
        }


        for (final File file : Objects.requireNonNull(folder.listFiles())) {
            final Arena arena = createArena(file.getName().replace(".yml", ""));

            arena.setWaitingRoomLoc(CustomLocation.deserialize(arena.getArenaConfig().getString("LobbySpawn")));

            for (final String key : arena.getArenaConfig().getConfigurationSection("Red-Spawn-Locations").getKeys(false))
                arena.getTeamList().get(0).addSpawnLocation(CustomLocation.deserialize(arena.getArenaConfig().getString("Red-Spawn-Locations." + key)));

            for (final String key : arena.getArenaConfig().getConfigurationSection("Blue-Spawn-Locations").getKeys(false))
                arena.getTeamList().get(1).addSpawnLocation(CustomLocation.deserialize(arena.getArenaConfig().getString("Blue-Spawn-Locations." + key)));


            Bukkit.getLogger().info("&aLoaded arena &e" + arena.getName());

        }

    }

    public void saveArenas() {
        for (final Arena arena : plugin.getArenas()) {
            arena.saveArena(arena.getName());
        }

    }

    private void deleteMap(File dir) {
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                this.deleteMap(file);
            }
            file.delete();
        }

        dir.delete();
    }

}

