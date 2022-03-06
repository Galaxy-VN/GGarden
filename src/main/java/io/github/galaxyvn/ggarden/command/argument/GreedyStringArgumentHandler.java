package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.command.framework.types.GreedyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GreedyStringArgumentHandler extends GalaxyCommandArgumentHandler<GreedyString> {

    public GreedyStringArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, GreedyString.class);
    }

    @Override
    protected GreedyString handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        List<String> inputs = new ArrayList<>();
        while (argumentParser.hasNext())
            inputs.add(argumentParser.next());

        String combined = String.join(" ", inputs);
        if (combined.isEmpty())
            throw new HandledArgumentException("argument-handler-string");
        return new GreedyString(combined);
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        while (argumentParser.hasNext())
            argumentParser.next();
        return Collections.singletonList(argumentInfo.toString());
    }

}
