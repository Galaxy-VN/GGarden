package io.github.galaxyvn.ggarden.command.framework;

/**
 * Holds information about a parsed argument
 */
public class ArgumentInstance {

    private final GalaxyCommandArgumentInfo argumentInfo;
    private final GalaxyCommandArgumentHandler<?> argumentHandler;
    private final String argument;

    public ArgumentInstance(GalaxyCommandArgumentInfo argumentInfo, GalaxyCommandArgumentHandler<?> argumentHandler, String argument) {
        this.argumentInfo = argumentInfo;
        this.argumentHandler = argumentHandler;
        this.argument = argument;
    }
    public GalaxyCommandArgumentInfo getArgumentInfo() {
        return this.argumentInfo;
    }

    public GalaxyCommandArgumentHandler<?> getArgumentHandler() {
        return this.argumentHandler;
    }

    public String getArgument() {
        return this.argument;
    }

}
