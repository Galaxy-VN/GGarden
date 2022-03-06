package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.Arrays;
import java.util.List;

public class BooleanArgumentHandler extends GalaxyCommandArgumentHandler<Boolean> {

    public BooleanArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Boolean.class);
    }

    @Override
    protected Boolean handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        try {
            return Boolean.parseBoolean(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-boolean", StringPlaceholders.single("input", input));
        }
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Arrays.asList("true", "false");
    }

}
