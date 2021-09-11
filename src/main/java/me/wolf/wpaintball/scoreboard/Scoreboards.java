package me.wolf.wpaintball.scoreboard;

import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

@SuppressWarnings("ConstantConditions")
public class Scoreboards {

    private final PaintballPlugin plugin;

    public Scoreboards(final PaintballPlugin plugin) {
        this.plugin = plugin;
    }

    public void lobbyScoreboard(final Player player, final Arena arena) {
        final int maxPlayers = arena.getArenaConfig().getInt("max-players");
        final String name = arena.getName();
        final int currentPlayers = arena.getArenaMembers().size();

        final String teamName = arena.getTeamNameByPlayer(plugin.getPbPlayers().get(player.getUniqueId()));


        final ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        final Objective objective = scoreboard.registerNewObjective("pb", "pb");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Utils.colorize("&c&lPB Waiting Room"));

        final Team players = scoreboard.registerNewTeam("players");
        players.addEntry(Utils.colorize("&bPlayers: "));
        players.setPrefix("");
        players.setSuffix(Utils.colorize("&b" + currentPlayers + "&3/&b" + maxPlayers));
        objective.getScore(Utils.colorize("&bPlayers: ")).setScore(1);

        final Team empty1 = scoreboard.registerNewTeam("empty1");
        empty1.addEntry(" ");
        empty1.setPrefix("");
        empty1.setSuffix("");
        objective.getScore(" ").setScore(2);

        final Team map = scoreboard.registerNewTeam("map");
        map.addEntry(Utils.colorize("&bMap: &2"));
        map.setPrefix("");
        map.setSuffix(Utils.colorize(name));
        objective.getScore(Utils.colorize("&bMap: &2")).setScore(3);

        final Team empty2 = scoreboard.registerNewTeam("empty2");
        empty2.addEntry("  ");
        empty2.setPrefix("");
        empty2.setSuffix("");
        objective.getScore("  ").setScore(4);

        final Team playerTeam = scoreboard.registerNewTeam("playerTeam");
        playerTeam.addEntry(Utils.colorize("&bTeam: "));
        playerTeam.setPrefix("");
        playerTeam.setSuffix(Utils.colorize(teamName));
        objective.getScore(Utils.colorize("&bTeam: ")).setScore(5);

        final Team empty3 = scoreboard.registerNewTeam("empty3");
        empty3.addEntry("   ");
        empty3.setPrefix("");
        empty3.setSuffix("");
        objective.getScore("   ").setScore(6);

        player.setScoreboard(scoreboard);
    }

    public void gameScoreboard(final Player player, final Arena arena) {
        final int maxPlayers = arena.getArenaConfig().getInt("max-players");
        final String name = arena.getName();
        final int currentPlayers = arena.getArenaMembers().size();

        final PBPlayer pbPlayer = plugin.getPbPlayers().get(player.getUniqueId());

        final me.wolf.wpaintball.team.Team playersTeam = arena.getTeamByPlayer(pbPlayer);
        final me.wolf.wpaintball.team.Team enemyTeam = arena.getEnemyTeam(pbPlayer);

        final ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        final Objective objective = scoreboard.registerNewObjective("pb", "pb");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Utils.colorize("&a&lPaintball"));

        final Team players = scoreboard.registerNewTeam("players");
        players.addEntry(Utils.colorize("&bPlayers: "));
        players.setPrefix("");
        players.setSuffix(Utils.colorize("&b" + currentPlayers + "&3/&b" + maxPlayers));
        objective.getScore(Utils.colorize("&bPlayers: ")).setScore(1);

        final Team empty1 = scoreboard.registerNewTeam("empty1");
        empty1.addEntry(" ");
        empty1.setPrefix("");
        empty1.setSuffix("");
        objective.getScore(" ").setScore(2);

        final Team time = scoreboard.registerNewTeam("time");
        time.addEntry(Utils.colorize("&bTime Left: "));
        time.setPrefix("");
        time.setSuffix(Utils.colorize("&3" + arena.getGameTimer()));
        objective.getScore(Utils.colorize("&bTime Left: ")).setScore(3);

        final Team empty2 = scoreboard.registerNewTeam("empty2");
        empty2.addEntry("  ");
        empty2.setPrefix("");
        empty2.setSuffix("");
        objective.getScore("  ").setScore(4);

        final Team kills = scoreboard.registerNewTeam("kills");
        kills.addEntry(Utils.colorize("&bKills: "));
        kills.setPrefix("");
        kills.setSuffix(Utils.colorize("&3" + pbPlayer.getKills()));
        objective.getScore(Utils.colorize("&bKills: ")).setScore(5);


        final Team empty3 = scoreboard.registerNewTeam("empty3");
        empty3.addEntry("   ");
        empty3.setPrefix("");
        empty3.setSuffix("");
        objective.getScore("   ").setScore(6);

        final Team map = scoreboard.registerNewTeam("map");
        map.addEntry(Utils.colorize("&bMap: &2"));
        map.setPrefix("");
        map.setSuffix(Utils.colorize(name));
        objective.getScore(Utils.colorize("&bMap: &2")).setScore(7);

        final Team empty4 = scoreboard.registerNewTeam("empty4");
        empty4.addEntry("    ");
        empty4.setPrefix("");
        empty4.setSuffix("");
        objective.getScore("    ").setScore(8);

        final Team playerTeam = scoreboard.registerNewTeam("playerTeam");
        playerTeam.addEntry(Utils.colorize("&b" + playersTeam.getName() + "&3 "));
        playerTeam.setPrefix("");
        playerTeam.setSuffix(Utils.colorize(playersTeam.getLives() + " &7[YOU]"));
        objective.getScore(Utils.colorize("&b" + playersTeam.getName() + "&3 ")).setScore(9);

        final Team otherTeam = scoreboard.registerNewTeam("otherTeam");
        otherTeam.addEntry(Utils.colorize("&b" + enemyTeam.getName() + "&3 "));
        otherTeam.setPrefix("");
        otherTeam.setSuffix(Utils.colorize(String.valueOf(enemyTeam.getLives())));
        objective.getScore(Utils.colorize("&b" + enemyTeam.getName() + "&3 ")).setScore(10);

        final Team empty5 = scoreboard.registerNewTeam("empty5");
        empty5.addEntry("     ");
        empty5.setPrefix("");
        empty5.setSuffix("");
        objective.getScore("     ").setScore(11);
        player.setScoreboard(scoreboard);


        final Team empty6 = scoreboard.registerNewTeam("empty6");
        empty6.addEntry("      ");
        empty6.setPrefix("");
        empty6.setSuffix("");
        objective.getScore("      ").setScore(12);
        player.setScoreboard(scoreboard);

    }

}
