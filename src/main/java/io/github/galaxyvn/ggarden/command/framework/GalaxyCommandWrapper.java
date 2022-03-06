package io.github.galaxyvn.ggarden.command.framework;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.command.BaseCommand;
import io.github.galaxyvn.ggarden.command.command.HelpCommand;
import io.github.galaxyvn.ggarden.command.command.ReloadCommand;
import io.github.galaxyvn.ggarden.config.CommentedFileConfiguration;
import io.github.galaxyvn.ggarden.manager.AbstractCommandManager;
import io.github.galaxyvn.ggarden.manager.AbstractLocaleManager;
import io.github.galaxyvn.ggarden.utils.ClassUtils;
import io.github.galaxyvn.ggarden.utils.CommandMapUtils;
import io.github.galaxyvn.ggarden.utils.StringPlaceholders;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public abstract class GalaxyCommandWrapper extends BukkitCommand {

    private String activeName;
    private List<String> activeAliases;

    protected final GalaxyPlugin galaxyPlugin;
    protected final List<GalaxyCommand> commands;
    protected final Map<String, GalaxyCommand> commandLookupMap;
    protected final AbstractCommandManager commandManager;
    protected final AbstractLocaleManager localeManager;

    public GalaxyCommandWrapper(GalaxyPlugin galaxyPlugin) {
        super("");
        this.galaxyPlugin = galaxyPlugin;
        this.commands = new ArrayList<>();
        this.commandLookupMap = new HashMap<>();
        this.commandManager = galaxyPlugin.getManager(AbstractCommandManager.class);
        this.localeManager = galaxyPlugin.getManager(AbstractLocaleManager.class);
    }

    public void register() {
        try {
            // Load commands
            List<Class<? extends GalaxyCommand>> commandClasses = new ArrayList<>();

            if (this.includeBaseCommand())
                commandClasses.add(BaseCommand.class);

            if (this.includeHelpCommand())
                commandClasses.add(HelpCommand.class);

            if (this.includeReloadCommand())
                commandClasses.add(ReloadCommand.class);

            this.getCommandPackages().stream().map(x -> ClassUtils.getClassesOf(this.galaxyPlugin, x, GalaxyCommand.class)).forEach(commandClasses::addAll);

            for (Class<? extends GalaxyCommand> commandClass : commandClasses) {
                // Ignore abstract/interface classes
                if (Modifier.isAbstract(commandClass.getModifiers()) || Modifier.isInterface(commandClass.getModifiers()))
                    continue;

                // Subcommands get loaded within commands
                if (GalaxySubCommand.class.isAssignableFrom(commandClass))
                    continue;

                GalaxyCommand command = commandClass.getConstructor(GalaxyPlugin.class, GalaxyCommandWrapper.class).newInstance(this.galaxyPlugin, this);
                this.commands.add(command);
            }

            // Register commands
            File commandsDirectory = new File(this.galaxyPlugin.getDataFolder(), "commands");
            commandsDirectory.mkdirs();

            File commandConfigFile = new File(commandsDirectory, this.getDefaultName() + ".yml");
            boolean exists = commandConfigFile.exists();
            CommentedFileConfiguration commandConfig = CommentedFileConfiguration.loadConfiguration(commandConfigFile);

            boolean modified = false;
            if (!exists) {
                commandConfig.addComments("This file lets you change the name and aliases for the " + this.getDefaultName() + " command.",
                        "If you edit the name/aliases at the top of this file, you will need to restart the server to see all the changes applied properly.");
                modified = true;
            }

            // Write default config values if they don't exist
            if (!commandConfig.contains("name")) {
                commandConfig.set("name", this.getDefaultName());
                modified = true;
            }

            // Write default alias values if they don't exist
            if (!commandConfig.contains("aliases")) {
                commandConfig.set("aliases", new ArrayList<>(this.getDefaultAliases()));
                modified = true;
            }

            // Write subcommands
            if (!this.commands.isEmpty()) {
                ConfigurationSection subcommandsSection = commandConfig.getConfigurationSection("subcommands");
                if (subcommandsSection == null) {
                    subcommandsSection = commandConfig.createSection("subcommands");
                    modified = true;
                }

                for (GalaxyCommand command : this.commands) {
                    // Skip base command
                    if (command.getDefaultName().isEmpty()) {
                        command.setNameAndAliases("", Collections.emptyList());
                        this.commandLookupMap.put("", command);
                        continue;
                    }

                    ConfigurationSection commandSection = subcommandsSection.getConfigurationSection(command.getDefaultName());
                    if (commandSection == null) {
                        commandSection = subcommandsSection.createSection(command.getDefaultName());
                        modified = true;
                    }

                    if (!commandSection.contains("name")) {
                        commandSection.set("name", command.getDefaultName());
                        modified = true;
                    }

                    if (!commandSection.contains("aliases")) {
                        commandSection.set("aliases", new ArrayList<>(command.getDefaultAliases()));
                        modified = true;
                    }

                    String name = commandSection.getString("name", command.getDefaultName());
                    List<String> aliases = commandSection.getStringList("aliases");

                    command.setNameAndAliases(name, aliases);

                    // Add to command lookup map
                    this.commandLookupMap.put(name.toLowerCase(), command);
                    aliases.forEach(x -> this.commandLookupMap.put(x.toLowerCase(), command));
                }
            }

            if (modified)
                commandConfig.save();

            // Load command config values
            this.activeName = commandConfig.getString("name");
            this.activeAliases = commandConfig.getStringList("aliases");

            // Finally, register the command with the server
            CommandMapUtils.registerCommand(this.galaxyPlugin.getName().toLowerCase(), this);
        } catch (Exception e) {
            this.galaxyPlugin.getLogger().severe("Fatal error initializing command argument handlers");
            e.printStackTrace();
        }
    }

    public void unregister() {
        this.commands.clear();
        this.commandLookupMap.clear();

        CommandMapUtils.unregisterCommand(this);
    }

    public GalaxyCommand getCommand(String commandName) {
        return this.commandLookupMap.get(commandName);
    }

    public List<GalaxyCommand> getCommands() {
        return this.commandLookupMap.values().stream()
                .distinct()
                .filter(x -> !x.getDefaultName().isEmpty())
                .sorted(Comparator.comparing(GalaxyCommand::getName))
                .collect(Collectors.toList());
    }

    public GalaxySubCommand getSubCommand(GalaxyCommand command, String commandName) {
        return command.getSubCommands().get(commandName.toLowerCase());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        try {
            GalaxyCommand command = this.getCommand(args.length == 0 ? "" : args[0]);
            if (command == null) {
                this.localeManager.sendCommandMessage(sender, "unknown-command", StringPlaceholders.single("cmd", this.getName()));
                return true;
            }

            boolean isOverridden = false;
            if (command instanceof BaseCommand) {
                BaseCommand baseCommand = (BaseCommand) command;
                String override = baseCommand.getOverrideCommand();
                if (override != null) {
                    GalaxyCommand overrideCommand = this.getCommand(override);
                    if (overrideCommand != null) {
                        command = overrideCommand;
                        isOverridden = true;
                    }
                }
            }

            String[] cmdArgs;
            if (args.length > 0) {
                cmdArgs = new String[args.length - 1];
                System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);
            } else {
                cmdArgs = new String[0];
            }

            CommandContext context = new CommandContext(sender, cmdArgs);
            ArgumentParser argumentParser = new ArgumentParser(context, new LinkedList<>(Arrays.asList(cmdArgs)));

            this.runCommand(sender, command, argumentParser, new ArrayList<>(), 0, isOverridden);
        } catch (Exception e) {
            e.printStackTrace();
            this.localeManager.sendCommandMessage(sender, "unknown-command-error");
        }
        return true;
    }

    private void runCommand(CommandSender sender, GalaxyCommand command, ArgumentParser argumentParser, List<Object> parsedArgs, int commandLayer, boolean skipPermissionCheck) throws ReflectiveOperationException {
        if (!skipPermissionCheck && !command.canUse(sender)) {
            this.localeManager.sendCommandMessage(sender, "no-permission");
            return;
        }

        if (command.isPlayerOnly() && !(sender instanceof Player)) {
            this.localeManager.sendCommandMessage(sender, "only-player");
            return;
        }

        // Start parsing parameters based on the command requirements, print errors out as we go
        for (GalaxyCommandArgumentInfo argumentInfo : command.getArgumentInfo()) {
            if (!argumentParser.hasNext()) {
                // All other arguments are optional, this is fine
                if (argumentInfo.isOptional())
                    break;

                // Ran out of arguments while parsing
                if (command.hasSubCommand()) {
                    this.localeManager.sendCommandMessage(sender, "missing-arguments-extra", StringPlaceholders.single("amount", command.getNumRequiredArguments()));
                } else {
                    this.localeManager.sendCommandMessage(sender, "missing-arguments", StringPlaceholders.single("amount", parsedArgs.size() + command.getNumRequiredArguments() + commandLayer));
                }
                return;
            }

            if (argumentInfo.isSubCommand()) {
                GalaxySubCommand subCommand = this.getSubCommand(command, argumentParser.next());
                if (subCommand == null) {
                    this.localeManager.sendCommandMessage(sender, "invalid-subcommand");
                    return;
                }

                this.runCommand(sender, subCommand, argumentParser, parsedArgs, commandLayer + 1, false);
                return;
            }

            try {
                Object parsedArgument = this.commandManager.resolveArgumentHandler(argumentInfo.getType()).handle(argumentInfo, argumentParser);
                if (parsedArgument == null) {
                    this.localeManager.sendCommandMessage(sender, "invalid-argument-null", StringPlaceholders.single("name", argumentInfo.toString()));
                    return;
                }

                parsedArgs.add(parsedArgument);
            } catch (GalaxyCommandArgumentHandler.HandledArgumentException e) {
                String message = this.localeManager.getCommandLocaleMessage(e.getMessage(), e.getPlaceholders());
                this.localeManager.sendCommandMessage(sender, "invalid-argument", StringPlaceholders.single("message", message));
                return;
            }
        }

        this.executeCommand(argumentParser.getContext(), command, parsedArgs);
    }

    private void executeCommand(CommandContext context, GalaxyCommand command, List<Object> parsedArgs) throws ReflectiveOperationException {
        Stream.Builder<Object> argumentBuilder = Stream.builder().add(context);
        parsedArgs.forEach(argumentBuilder::add);

        // Fill optional parameters with nulls
        for (int i = parsedArgs.size(); i < command.getNumParameters(); i++)
            argumentBuilder.add(null);

        command.getExecuteMethod().invoke(command, argumentBuilder.build().toArray());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 0)
            return new ArrayList<>(this.commandLookupMap.keySet());

        if (args.length <= 1)
            return this.commandLookupMap.keySet().stream()
                    .filter(x -> StringUtil.startsWithIgnoreCase(x, args[args.length - 1]))
                    .collect(Collectors.toList());

        GalaxyCommand command = this.getCommand(args[0]);
        if (command == null)
            return Collections.emptyList();

        String[] cmdArgs = new String[args.length - 1];
        System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);
        CommandContext context = new CommandContext(sender, cmdArgs);
        ArgumentParser argumentParser = new ArgumentParser(context, new LinkedList<>(Arrays.asList(cmdArgs)));

        return this.tabCompleteCommand(sender, command, argumentParser);
    }

    private List<String> tabCompleteCommand(CommandSender sender, GalaxyCommand command, ArgumentParser argumentParser) {
        if (!command.canUse(sender) || (command.isPlayerOnly() && !(sender instanceof Player)))
            return Collections.emptyList();

        // Consume all arguments until there are no more, then print those results
        for (GalaxyCommandArgumentInfo argumentInfo : command.getArgumentInfo()) {
            if (argumentInfo.isSubCommand()) {
                if (!argumentParser.hasNext())
                    return new ArrayList<>(command.getSubCommands().keySet());

                String input = argumentParser.next();
                GalaxySubCommand subCommand = this.getSubCommand(command, input);
                if (subCommand == null)
                    return command.getSubCommands().keySet()
                            .stream()
                            .filter(x -> StringUtil.startsWithIgnoreCase(x, input))
                            .collect(Collectors.toList());

                if (argumentParser.hasNext())
                    return this.tabCompleteCommand(sender, subCommand, argumentParser);

                return Collections.emptyList();
            }

            List<String> suggestions = this.commandManager.resolveArgumentHandler(argumentInfo.getType()).suggest(argumentInfo, argumentParser);
            String input = argumentParser.previous();
            if (!argumentParser.hasNext())
                return suggestions.stream()
                        .filter(x -> StringUtil.startsWithIgnoreCase(x, input))
                        .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return this.galaxyPlugin.getName().toLowerCase() + ".basecommand";
    }

    @Override
    public String getName() {
        return this.activeName;
    }

    @Override
    public List<String> getAliases() {
        return this.activeAliases;
    }

    public abstract String getDefaultName();

    public abstract List<String> getDefaultAliases();

    public abstract List<String> getCommandPackages();

    public abstract boolean includeBaseCommand();

    public abstract boolean includeHelpCommand();

    public abstract boolean includeReloadCommand();

}
