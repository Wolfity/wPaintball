package me.wolf.wpaintball;

import lombok.Getter;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.arena.ArenaManager;
import me.wolf.wpaintball.commands.impl.PaintballCommand;
import me.wolf.wpaintball.file.FileManager;
import me.wolf.wpaintball.game.GameListeners;
import me.wolf.wpaintball.game.GameManager;
import me.wolf.wpaintball.game.GameUtils;
import me.wolf.wpaintball.listeners.*;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.scoreboard.Scoreboards;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

@Getter
public class PaintballPlugin extends JavaPlugin {

    @Getter
    private PaintballPlugin plugin;

    private ArenaManager arenaManager;
    private GameManager gameManager;
    private Scoreboards scoreboard;
    private GameUtils gameUtils;
    private FileManager fileManager;

    private final Set<Arena> arenas = new HashSet<>();
    private final Map<UUID, PBPlayer> pbPlayers = new HashMap<>();

    private File folder;

    @Override
    public void onEnable() {
        plugin = this;

        folder = new File(plugin.getDataFolder() + "/arenas");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        registerCommands();
        registerListeners();
        registerManagers();

        getConfig().options().copyDefaults();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        arenaManager.saveArenas();
    }

    private void registerCommands() {
        Collections.singletonList(
                new PaintballCommand(this)
        ).forEach(this::registerCommand);

    }

    private void registerListeners() {
        Arrays.asList(
                new GameListeners(this),
                new PlayerQuit(this),
                new BlockBreak(this),
                new BlockPlace(this),
                new FoodChange(this),
                new EntityDamage(this),
                new InventoryListeners(this)
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void registerManagers() {
        this.fileManager = new FileManager(this);
        this.arenaManager = new ArenaManager(this);
        arenaManager.loadArenas();
        this.gameManager = new GameManager(this);
        this.scoreboard = new Scoreboards(this);
        this.gameUtils = new GameUtils(this);

    }

    private void registerCommand(final Command command) {
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(command.getLabel(), command);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
