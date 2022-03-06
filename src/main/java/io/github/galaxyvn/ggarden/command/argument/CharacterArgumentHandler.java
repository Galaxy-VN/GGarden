package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.Collections;
import java.util.List;

public class CharacterArgumentHandler extends GalaxyCommandArgumentHandler<Character> {

    public CharacterArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Character.class);
    }

    @Override
    protected Character handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        if (input.length() != 1)
            throw new HandledArgumentException("argument-handler-character", StringPlaceholders.single("input", input));
        return input.charAt(0);
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Collections.singletonList(argumentInfo.toString());
    }

}
