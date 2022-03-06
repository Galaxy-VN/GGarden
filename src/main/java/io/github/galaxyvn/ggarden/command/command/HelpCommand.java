package io.github.galaxyvn.ggarden.command.command;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.CommandContext;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommand;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandWrapper;
import io.github.galaxyvn.ggarden.command.framework.annotation.GalaxyExecutable;
import io.github.galaxyvn.ggarden.manager.AbstractLocaleManager;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.Collections;
import java.util.List;

public class HelpCommand extends GalaxyCommand {

    public HelpCommand(GalaxyPlugin galaxyPlugin, GalaxyCommandWrapper parent) {
        super(galaxyPlugin, parent);
    }

    @GalaxyExecutable
    public void execute(CommandContext context) {
        AbstractLocaleManager localeManager = this.galaxyPlugin.getManager(AbstractLocaleManager.class);

        localeManager.sendCommandMessage(context.getSender(), "command-help-title");
        for (GalaxyCommand command : this.parent.getCommands()) {
            if (!command.hasHelp() || !command.canUse(context.getSender()))
                continue;

            StringPlaceholders stringPlaceholders = StringPlaceholders.builder("cmd", this.parent.getName())
                    .addPlaceholder("subcmd", command.getName().toLowerCase())
                    .addPlaceholder("args", command.getArgumentsString())
                    .addPlaceholder("desc", localeManager.getLocaleMessage(command.getDescriptionKey()))
                    .build();
            localeManager.sendSimpleCommandMessage(context.getSender(), "command-help-list-description", stringPlaceholders);
        }
    }

    @Override
    public String getDefaultName() {
        return "help";
    }

    @Override
    public List<String> getDefaultAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescriptionKey() {
        return "command-help-description";
    }

    @Override
    public String getRequiredPermission() {
        return null;
    }

}
