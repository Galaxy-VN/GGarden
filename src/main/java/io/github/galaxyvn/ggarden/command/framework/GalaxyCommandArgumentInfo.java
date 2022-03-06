package io.github.galaxyvn.ggarden.command.framework;

import io.github.galaxyvn.ggarden.command.framework.annotation.Optional;

import java.lang.reflect.Parameter;

public class GalaxyCommandArgumentInfo {

    private final Parameter parameter;
    private final int index;

    public GalaxyCommandArgumentInfo(Parameter parameter, int index) {
        this.parameter = parameter;
        this.index = index;
    }

    /**
     * @return the Class type of this argument
     */
    public Class<?> getType() {
        return this.parameter.getType();
    }

    /**
     * @return the index of this argument in the command syntax
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @return the name of this argument
     */
    public String getName() {
        return this.parameter.getName();
    }

    /**
     * @return true if this argument is optional, false otherwise
     */
    public boolean isOptional() {
        return this.parameter.isAnnotationPresent(Optional.class);
    }

    /**
     * @return true if this is a RoseSubCommand, false otherwise
     */
    public boolean isSubCommand() {
        return this.getType() == GalaxySubCommand.class;
    }

    @Override
    public String toString() {
        if (this.isOptional()) {
            return "[" + this.getName() + "]";
        } else {
            return "<" + this.getName() + ">";
        }
    }
}
