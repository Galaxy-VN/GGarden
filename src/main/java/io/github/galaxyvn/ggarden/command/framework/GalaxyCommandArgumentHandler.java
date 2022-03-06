package io.github.galaxyvn.ggarden.command.framework;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;

import java.util.List;

public abstract class GalaxyCommandArgumentHandler<T> {

    protected GalaxyPlugin galaxyPlugin;
    protected Class<T> handledType;

    public GalaxyCommandArgumentHandler(GalaxyPlugin galaxyPlugin, Class<T> handledType) {
        this.galaxyPlugin = galaxyPlugin;
        this.handledType = handledType;
    }

    /**
     * The internal method for converting a String input into the handled type
     *
     * @param argumentInfo The argument info
     * @param argumentParser The argument parser
     * @return The String input converted to the handled object type, or null if the conversion failed
     */
    protected abstract T handleInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException;

    /**
     * The internal method for suggesting arguments
     *
     * @param argumentInfo The argument info
     * @param argumentParser The argument parser
     * @return A List of possible argument suggestions
     */
    protected abstract List<String> suggestInternal(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser);

    /**
     * Converts a String input from an argument instance into the handled type
     *
     * @param argumentInfo The argument info
     * @param argumentParser The argument parser
     * @return The String input converted to the handled object type, or null if the conversion failed
     */
    public final T handle(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        if (!argumentParser.hasNext())
            throw new HandledArgumentException("No more arguments are available, is there an error in the command syntax?");
        this.preProcess(argumentInfo);
        return this.handleInternal(argumentInfo, argumentParser);
    }

    /**
     * Gets command argument suggestions for the given argument instance
     *
     * @param argumentInfo The argument info
     * @param argumentParser The argument parser
     * @return A List of possible argument suggestions
     */
    public final List<String> suggest(GalaxyCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        this.preProcess(argumentInfo);
        return this.suggestInternal(argumentInfo, argumentParser);
    }

    /**
     * @return the Class that this argument handler handles
     */
    public Class<T> getHandledType() {
        return this.handledType;
    }

    /**
     * Allows an argument handler to preprocess the argument before handling or suggesting
     *
     * @param argumentInfo The argument info to be handled
     */
    public void preProcess(GalaxyCommandArgumentInfo argumentInfo) {

    }

    /**
     * Thrown when an argument has an issue while parsing, the exception message is the reason why the argument failed to parse
     */
    public static class HandledArgumentException extends RuntimeException {

        private final StringPlaceholders placeholders;

        public HandledArgumentException(String message, StringPlaceholders placeholders) {
            super(message);
            this.placeholders = placeholders;
        }

        public HandledArgumentException(String message) {
            this(message, StringPlaceholders.empty());
        }

        public StringPlaceholders getPlaceholders() {
            return this.placeholders;
        }

    }

}
