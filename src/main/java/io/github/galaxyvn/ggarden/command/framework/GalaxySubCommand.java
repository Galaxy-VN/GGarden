package io.github.galaxyvn.ggarden.command.framework;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import java.util.List;

/**
 * Create one or more subclasses within a {@link GalaxyCommand} that extend this class.
 */
public abstract class GalaxySubCommand extends GalaxyCommand {

    public GalaxySubCommand(GalaxyPlugin galaxyPlugin, GalaxyCommandWrapper parent) {
        super(galaxyPlugin, parent);
        super.setNameAndAliases(this.getDefaultName(), this.getDefaultAliases());
    }

    @Override
    protected final void setNameAndAliases(String name, List<String> aliases) {
        throw new IllegalStateException("GalaxySubCommands cannot have their name or aliases changed");
    }

    @Override
    public String getDescriptionKey() {
        return null;
    }

    @Override
    public String getRequiredPermission() {
        return null;
    }

    @Override
    public final boolean hasHelp() {
        return false;
    }

}
