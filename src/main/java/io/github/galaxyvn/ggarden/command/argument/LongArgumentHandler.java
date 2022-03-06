package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.Collections;
import java.util.List;

public class LongArgumentHandler extends GalaxyCommandArgumentHandler<Long> {

    public LongArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Long.class);
    }

    @Override
    protected Long handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-long", StringPlaceholders.single("input", input));
        }
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Collections.singletonList(argumentInfo.toString());
    }

}
