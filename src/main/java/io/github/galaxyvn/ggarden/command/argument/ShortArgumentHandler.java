package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.Collections;
import java.util.List;

public class ShortArgumentHandler extends GalaxyCommandArgumentHandler<Short> {

    public ShortArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Short.class);
    }

    @Override
    protected Short handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        try {
            return Short.parseShort(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-short", StringPlaceholders.single("input", input));
        }
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Collections.singletonList(argumentInfo.toString());
    }

}
