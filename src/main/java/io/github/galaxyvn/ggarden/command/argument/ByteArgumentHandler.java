package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.Collections;
import java.util.List;

public class ByteArgumentHandler extends GalaxyCommandArgumentHandler<Byte> {

    public ByteArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, Byte.class);
    }

    @Override
    protected Byte handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        try {
            return Byte.parseByte(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-byte", StringPlaceholders.single("input", input));
        }
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Collections.singletonList(argumentInfo.toString());
    }


}
