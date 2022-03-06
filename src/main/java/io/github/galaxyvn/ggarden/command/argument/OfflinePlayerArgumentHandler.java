package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class OfflinePlayerArgumentHandler extends GalaxyCommandArgumentHandler<OfflinePlayer> {

    public OfflinePlayerArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, OfflinePlayer.class);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected OfflinePlayer handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        return Bukkit.getOfflinePlayer(argumentParser.next());
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
