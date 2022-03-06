package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;

import java.util.Collections;
import java.util.List;

public class StringArgumentHandler extends GalaxyCommandArgumentHandler<String> {

    public StringArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, String.class);
    }

    @Override
    protected String handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        if (input.trim().isEmpty())
            throw new HandledArgumentException("argument-handler-string");
        return input;
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Collections.singletonList(argumentInfo.toString());
    }

}
