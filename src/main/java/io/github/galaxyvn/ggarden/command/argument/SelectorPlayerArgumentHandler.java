package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.command.framework.types.SelectorPlayer;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectorPlayerArgumentHandler extends GalaxyCommandArgumentHandler<SelectorPlayer> {

    public SelectorPlayerArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, SelectorPlayer.class);
    }

    @Override
    protected SelectorPlayer handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        if (input.startsWith("@")) {
            // Running a selector, try to find exactly one entity which must be a player
            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(argumentParser.getContext().getSender(), input);
            } catch (Exception e) {
                throw new HandledArgumentException("argument-handler-player-selector-syntax");
            }

            if (entities.isEmpty())
                throw new HandledArgumentException("argument-handler-player-selector-none");

            if (entities.size() > 1)
                throw new HandledArgumentException("argument-handler-player-selector-multiple");

            Entity selected = entities.get(0);
            if (!(selected instanceof Player))
                throw new HandledArgumentException("argument-handler-player-selector-entity");

            return new SelectorPlayer((Player) selected);
        }

        Player player = Bukkit.getPlayer(input);
        if (player == null)
            throw new HandledArgumentException("argument-handler-player", StringPlaceholders.single("input", input));
        return new SelectorPlayer(player);
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        List<String> suggestions = new ArrayList<>(Arrays.asList("@p", "@r"));
        suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        return suggestions;
    }

}
