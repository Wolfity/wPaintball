package me.wolf.wpaintball.team;

import lombok.Data;
import lombok.Setter;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.arena.Arena;
import me.wolf.wpaintball.player.PBPlayer;
import me.wolf.wpaintball.utils.CustomLocation;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Data
public class Team {

    private final PaintballPlugin plugin;


    private final Set<PBPlayer> teamMembers = new HashSet<>();
    private int lives;
    private final String name;
    private final ItemStack icon;
    @Setter
    private List<CustomLocation> spawnLocation;

    public Team(final PaintballPlugin plugin, final String name, final int lives, final ItemStack icon) {
        this.plugin = plugin;
        this.lives = lives;
        this.name = name;
        this.icon = icon;
        this.spawnLocation = new ArrayList<>();

    }

    public void decrementLives() {
        if (lives > 0) {
            lives--;
        }
    }

    public void addSpawnLocation(final CustomLocation location) {
        if (!spawnLocation.contains(location)) {
            spawnLocation.add(location);
        }
    }

    public void addTeamMember(final PBPlayer pbPlayer) {
        teamMembers.add(pbPlayer);
    }

    public void removeTeamMember(final PBPlayer pbPlayer) {
        teamMembers.remove(pbPlayer);
    }

    public void resetTeamLives(final Arena arena) {
        arena.getTeamList().forEach(team -> team.setLives(plugin.getConfig().getInt("team-lives")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return name.equals(team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
