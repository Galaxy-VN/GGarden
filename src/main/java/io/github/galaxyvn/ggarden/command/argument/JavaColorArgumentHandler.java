package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import java.awt.Color;

public class JavaColorArgumentHandler extends AbstractColorArgumentHandler<Color> {

    public JavaColorArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Color.class);
    }

    @Override
    protected Color rgbToColor(int r, int g, int b) {
        return new Color(r, g, b);
    }

}
