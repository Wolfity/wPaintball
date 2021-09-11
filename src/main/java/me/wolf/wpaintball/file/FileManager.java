package me.wolf.wpaintball.file;

import lombok.Getter;
import me.wolf.wpaintball.PaintballPlugin;
import me.wolf.wpaintball.utils.Utils;

public class FileManager {

    @Getter
    public YamlConfig powerUpsConfig;

    public FileManager(final PaintballPlugin plugin) {
        try {
            powerUpsConfig = new YamlConfig("powerups.yml", plugin);

        } catch (final Exception e) {
            System.out.println(Utils.colorize("&4Something went wrong while loading the yml files"));
        }

    }

    public void reloadConfigs() {
        powerUpsConfig.reloadConfig();

    }


}
