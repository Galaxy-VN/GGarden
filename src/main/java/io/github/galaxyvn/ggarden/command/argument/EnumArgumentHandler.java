package io.github.galaxyvn.ggarden.command.argument;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.ArgumentParser;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentInfo;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumArgumentHandler<T extends Enum<T>> extends GalaxyCommandArgumentHandler<T> {

    public EnumArgumentHandler(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin, null); // This is a special case and will be handled by the preprocessor
    }

    @Override
    protected T handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        T[] enumConstants = this.getHandledType().getEnumConstants();
        Optional<T> value = Stream.of(enumConstants)
                .filter(x -> x.name().equalsIgnoreCase(input))
                .findFirst();

        if (!value.isPresent()) {
            String messageKey;
            StringPlaceholders placeholders = StringPlaceholders.builder("enum", this.handledType.getSimpleName())
                    .addPlaceholder("input", input)
                    .addPlaceholder("types", Stream.of(enumConstants).map(x -> x.name().toLowerCase()).collect(Collectors.joining(", ")))
                    .build();
            if (enumConstants.length <= 10) {
                messageKey = "argument-handler-enum-list";
            } else {
                messageKey = "argument-handler-enum";
            }
            throw new HandledArgumentException(messageKey, placeholders);
        }

        return value.get();
    }

    @Override
    protected List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return Stream.of(this.getHandledType().getEnumConstants())
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void preProcess(GalaxyCommandArgumentInfo argumentInfo) {
        this.handledType = (Class<T>) argumentInfo.getType();
    }

}
