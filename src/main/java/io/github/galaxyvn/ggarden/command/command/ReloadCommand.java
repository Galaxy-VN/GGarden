package io.github.galaxyvn.ggarden.command.command;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.CommandContext;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommand;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandWrapper;
import io.github.galaxyvn.ggarden.command.framework.annotation.GalaxyExecutable;
import io.github.galaxyvn.ggarden.manager.AbstractLocaleManager;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends GalaxyCommand {

    public ReloadCommand(GalaxyPlugin galaxyPlugin, GalaxyCommandWrapper parent) {
        super(galaxyPlugin, parent);
    }

    @GalaxyExecutable
    public void execute(CommandContext context) {
        this.galaxyPlugin.reload();
        this.galaxyPlugin.getManager(AbstractLocaleManager.class).sendCommandMessage(context.getSender(), "command-reload-reloaded");
    }

    @Override
    public String getDefaultName() {
        return "reload";
    }

    @Override
    public List<String> getDefaultAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescriptionKey() {
        return "command-reload-description";
    }

    @Override
    public String getRequiredPermission() {
        return this.galaxyPlugin.getName().toLowerCase() + ".reload";
    }

}
