package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import org.bukkit.Color;

public class BukkitColorArgumentHandler extends AbstractColorArgumentHandler<Color> {

    public BukkitColorArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Color.class);
    }

    @Override
    protected Color rgbToColor(int r, int g, int b) {
        return Color.fromRGB(r, g, b);
    }

}
