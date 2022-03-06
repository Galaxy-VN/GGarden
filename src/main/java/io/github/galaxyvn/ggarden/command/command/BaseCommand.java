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

public class BaseCommand extends GalaxyCommand {

    public BaseCommand(GalaxyPlugin galaxyPlugin, GalaxyCommandWrapper parent) {
        super(galaxyPlugin , parent);
    }

    @GalaxyExecutable
    public void execute(CommandContext context) {
        AbstractLocaleManager localeManager = this.galaxyPlugin.getManager(AbstractLocaleManager.class);

        String baseColor = localeManager.getLocaleMessage("base-command-color");
        localeManager.sendCustomMessage(context.getSender(), baseColor + "Running <g:#46f0df:#5331eb:#8a2387>" + this.galaxyPlugin.getDescription().getName() + baseColor + " v" + this.galaxyPlugin.getDescription().getVersion());
        localeManager.sendCustomMessage(context.getSender(), baseColor + "Plugin created by: <g:#46f0df:#8a2387>" + this.galaxyPlugin.getDescription().getAuthors().get(0));
        localeManager.sendSimpleMessage(context.getSender(), "base-command-help", StringPlaceholders.single("cmd", this.parent.getName()));
    }

    @Override
    public String getDefaultName() {
        return "";
    }

    @Override
    public List<String> getDefaultAliases() {
        return Collections.emptyList();
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
    public boolean hasHelp() {
        return false;
    }

    /**
     * @return the override command name, or null if this command should be executed as normal
     */
    public String getOverrideCommand() {
        return null;
    }

}
