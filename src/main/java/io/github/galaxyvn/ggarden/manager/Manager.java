package io.github.galaxyvn.ggarden.manager;

import io.github.galaxyvn.ggarden.GalaxyPlugin;

public abstract class Manager {

    protected final GalaxyPlugin galaxyPlugin;

    public Manager(GalaxyPlugin galaxyPlugin) {
        this.galaxyPlugin = galaxyPlugin;
    }

    /**
     * Reloads the Manager's settings
     */
    public abstract void reload();

    /**
     * Cleans up the Manager's resources
     */
    public abstract void disable();
}
