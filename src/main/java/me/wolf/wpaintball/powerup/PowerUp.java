package me.wolf.wpaintball.powerup;

import lombok.Data;
import org.bukkit.Material;

import java.util.Objects;

@Data
public class PowerUp {


    private final String name, iconDisplay;
    private final int price;
    private int duration;
    private final int defaultDuration;
    private final Material icon;
    private final boolean isEnabled;
    private boolean isActive;


    public PowerUp(final String name, int price, final int duration, final int defaultDuration, final Material icon, final String iconDisplay, final boolean isEnabled) {
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.defaultDuration = defaultDuration;
        this.icon = icon;
        this.iconDisplay = iconDisplay;
        this.isEnabled = isEnabled;
        this.isActive = false;
    }

    public void decrementDuration() {
        duration--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PowerUp powerUp = (PowerUp) o;
        return name.equals(powerUp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
