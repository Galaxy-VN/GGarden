package io.github.galaxyvn.ggarden.command.framework;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.framework.annotation.GalaxyExecutable;
import io.github.galaxyvn.ggarden.command.framework.annotation.Inject;
import io.github.galaxyvn.ggarden.command.framework.annotation.Optional;
import io.github.galaxyvn.ggarden.manager.AbstractCommandManager;
import io.github.galaxyvn.ggarden.manager.AbstractLocaleManager;
import io.github.galaxyvn.ggarden.utils.GGardenUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.permissions.Permissible;

/**
 * Ensure a {@code public void} method annotated with {@link GalaxyExecutable} with a first parameter of {@link CommandContext} exists.
 * All following parameters after the first must have a matching {@link GalaxyCommandArgumentHandler} to be valid.
 */
public abstract class GalaxyCommand implements Comparable<GalaxyCommand> {

    protected final GalaxyPlugin galaxyPlugin;
    protected final GalaxyCommandWrapper parent;
    private final Map<String, GalaxySubCommand> subCommands;

    private String activeName;
    private List<String> activeAliases;

    public GalaxyCommand(GalaxyPlugin galaxyPlugin, GalaxyCommandWrapper parent, Class<?>... subCommandClasses) {
        this.galaxyPlugin = galaxyPlugin;
        this.parent = parent;
        this.subCommands = new HashMap<>();
        this.generateSubCommands(subCommandClasses);
        this.validateExecuteMethod();
    }

    protected void setNameAndAliases(String name, List<String> aliases) {
        this.activeName = name;
        this.activeAliases = aliases;
    }

    /**
     * @return the name of the command
     */
    public final String getName() {
        return this.activeName;
    }

    /**
     * @return any aliases that can be used as an alternative to the main command name
     */
    public final List<String> getAliases() {
        return this.activeAliases;
    }

    /**
     * @return the default name of the command
     */
    protected abstract String getDefaultName();

    /**
     * @return any default aliases that can be used as an alternative to the main command name
     */
    protected List<String> getDefaultAliases() {
        return Collections.emptyList();
    }

    /**
     * @return the description key for this command's description to be displayed in the help menu, requires an {@link AbstractLocaleManager} implementation
     */
    public abstract String getDescriptionKey();

    /**
     * @return the required permission to be able to run this command
     */
    public abstract String getRequiredPermission();

    /**
     * @return the method annotated with {@link GalaxyExecutable}
     */
    public Method getExecuteMethod() {
        return Stream.of(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(GalaxyExecutable.class))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * @return a list of all arguments part of the {@link GalaxyExecutable}
     */
    public List<GalaxyCommandArgumentInfo> getArgumentInfo() {
        List<GalaxyCommandArgumentInfo> argumentInfo = new ArrayList<>();
        Parameter[] parameters = this.getParameters();
        for (int i = 0; i < parameters.length; i++)
            argumentInfo.add(new GalaxyCommandArgumentInfo(parameters[i], i));
        return argumentInfo;
    }

    /**
     * @return a Map of all {@link GalaxySubCommand} registered for this command
     */
    public Map<String, GalaxySubCommand> getSubCommands() {
        return Collections.unmodifiableMap(this.subCommands);
    }

    /**
     * @return the index of the {@link GalaxySubCommand} argument in the command syntax or -1 if there is no subcommand
     */
    public int getSubCommandArgumentIndex() {
        return this.getArgumentInfo().stream()
                .filter(GalaxyCommandArgumentInfo::isSubCommand)
                .map(GalaxyCommandArgumentInfo::getIndex)
                .findFirst()
                .orElse(-1);
    }

    /**
     * @return true if there is a {@link GalaxySubCommand} within this command's arguments, false otherwise
     */
    public boolean hasSubCommand() {
        return this.getSubCommandArgumentIndex() != -1;
    }

    /**
     * @return a displayable output of this command's parameters
     */
    public String getArgumentsString() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<GalaxyCommandArgumentInfo> iterator = this.getArgumentInfo().iterator();
        while (iterator.hasNext()) {
            GalaxyCommandArgumentInfo argument = iterator.next();
            stringBuilder.append(argument);

            if (argument.isSubCommand())
                break;

            if (iterator.hasNext())
                stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }

    /**
     * @return the number of total arguments for this command
     */
    public int getNumParameters() {
        return this.getParameters().length - (!this.getExecuteMethod().getParameters()[0].isAnnotationPresent(Inject.class) ? 0 : 1);
    }

    /**
     * @return the number of optional arguments for this command
     */
    public int getNumOptionalParameters() {
        return Math.toIntExact(this.getArgumentInfo().stream().filter(GalaxyCommandArgumentInfo::isOptional).count());
    }

    /**
     * @return the number of required arguments for this command
     */
    public int getNumRequiredArguments() {
        return this.getNumParameters() - this.getNumOptionalParameters();
    }

    /**
     * @return an array of Parameters for this command's {@link GalaxyExecutable}
     */
    private Parameter[] getParameters() {
        return Stream.of(this.getExecuteMethod().getParameters())
                .filter(x -> x.getType() != CommandContext.class)
                .filter(x -> !x.isAnnotationPresent(Inject.class))
                .toArray(Parameter[]::new);
    }

    /**
     * Checks if this command can be run by a Permissible
     *
     * @param permissible The Permissible to check
     * @return true if the Permissible can execute this command, false otherwise
     */
    public boolean canUse(Permissible permissible) {
        return this.getRequiredPermission() == null || permissible.hasPermission(this.getRequiredPermission());
    }

    /**
     * @return true if this command will be displayed in the help menu, false otherwise
     */
    public boolean hasHelp() {
        return true;
    }

    /**
     * @return true if this command can only be run by a Player, false otherwise
     */
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int compareTo(GalaxyCommand other) {
        return this.getDefaultName().compareTo(other.getDefaultName());
    }

    /**
     * Validates that the command has a valid {@link GalaxyExecutable} layout.
     * The following conditions must be met:
     * <ul>
     *     <li>Must have a {@code public void} method annotated with {@link GalaxyExecutable}</li>
     *     <li>First parameter must be a {@link CommandContext}</li>
     *     <li>All parameters must have a registered {@link GalaxyCommandArgumentHandler} (excluding {@link CommandContext} and {@link GalaxySubCommand})</li>
     *     <li>Primitive typed parameters must not be marked as {@link Optional}, use wrapped types instead</li>
     *     <li>If a parameter is marked as {@link Optional} then all subsequent parameters must also be marked as {@link Optional}</li>
     *     <li>If the last parameter is a {@link GalaxySubCommand}, there must be at least one registered {@link GalaxySubCommand}</li>
     *     <li>No parameters are allowed after the {@link GalaxySubCommand}</li>
     * </ul>
     *
     * @throws InvalidGalaxyCommandArgumentsException if any of the above conditions are not met
     */
    private void validateExecuteMethod() {
        try {
            this.getExecuteMethod();
        } catch (IllegalStateException e) {
            throw new InvalidGalaxyCommandArgumentsException("No method marked as RoseExecutable detected");
        }

        Parameter[] rawParameters = this.getExecuteMethod().getParameters();
        if (rawParameters.length == 0 || rawParameters[0].getType() != CommandContext.class)
            throw new InvalidGalaxyCommandArgumentsException("First method parameter is not a CommandContext");

        AbstractCommandManager commandManager = this.galaxyPlugin.getManager(AbstractCommandManager.class);
        boolean first = true;
        boolean optionalFound = false;
        boolean subCommandFound = false;
        for (Parameter parameter : rawParameters) {
            if (first) {
                first = false;
                continue;
            } else if (parameter.getType() == CommandContext.class) {
                throw new InvalidGalaxyCommandArgumentsException("Only the first parameter may be a CommandContext");
            }

            if (subCommandFound)
                throw new InvalidGalaxyCommandArgumentsException("Parameters after a RoseSubCommand are not allowed");

            if (optionalFound && !parameter.isAnnotationPresent(Optional.class))
                throw new InvalidGalaxyCommandArgumentsException("Parameter '" + parameter.getType().getSimpleName() + " " + parameter.getName() + "' must be marked as Optional because a previous parameter was already marked as Optional");

            if (parameter.getType() != GalaxySubCommand.class) {
                try {
                    commandManager.resolveArgumentHandler(parameter.getType());
                } catch (IllegalStateException e) {
                    throw new InvalidGalaxyCommandArgumentsException("Parameter '" + parameter.getType().getSimpleName() + " " + parameter.getName() + "' is missing a RoseCommandArgumentHandler");
                }
            } else {
                subCommandFound = true;
            }

            if (parameter.isAnnotationPresent(Optional.class)) {
                if (parameter.getType().isPrimitive())
                    throw new InvalidGalaxyCommandArgumentsException("Parameter '" + parameter.getType().getSimpleName() + " " + parameter.getName() + "' is primitive but is marked as Optional. Change to a " + GGardenUtils.getPrimitiveAsWrapper(parameter.getType()) + " instead");

                optionalFound = true;
            }
        }

        if (subCommandFound && this.subCommands.isEmpty())
            throw new InvalidGalaxyCommandArgumentsException("No subcommands are registered but at least one is required");
    }

    /**
     * Locates and registers {@link GalaxySubCommand} classes as subcommands
     *
     * @param subCommandClasses An array of provided classes
     */
    private void generateSubCommands(Class<?>[] subCommandClasses) {
        Set<Class<?>> subClasses = new HashSet<>(Arrays.asList(subCommandClasses));
        subClasses.addAll(Arrays.asList(this.getClass().getDeclaredClasses()));

        for (Class<?> clazz : subClasses) {
            if (!GalaxySubCommand.class.isAssignableFrom(clazz))
                continue;

            @SuppressWarnings("unchecked")
            Class<GalaxySubCommand> subCommandClass = (Class<GalaxySubCommand>) clazz;
            GalaxySubCommand subCommandInstance;
            try {
                Constructor<GalaxySubCommand> pluginConstructor = subCommandClass.getConstructor(GalaxyPlugin.class, GalaxyCommandWrapper.class);
                subCommandInstance = pluginConstructor.newInstance(this.galaxyPlugin, this.parent);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Invalid GalaxySubCommand constructor for [" + subCommandClass.getName() + "]. Requires a constructor that accepts a GalaxyPlugin and a GalaxyCommandWrapper.");
            }

            this.subCommands.put(subCommandInstance.getDefaultName().toLowerCase(), subCommandInstance);
            List<String> aliases = subCommandInstance.getAliases();
            if (aliases != null)
                for (String alias : aliases)
                    this.subCommands.put(alias.toLowerCase(), subCommandInstance);
        }
    }

    public static class InvalidGalaxyCommandArgumentsException extends RuntimeException {

        public InvalidGalaxyCommandArgumentsException(String message) {
            super(message);
        }

    }
}
