package io.github.galaxyvn.ggarden.manager;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.command.argument.EnumArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandArgumentHandler;
import io.github.galaxyvn.ggarden.command.framework.GalaxyCommandWrapper;
import io.github.galaxyvn.ggarden.utils.ClassUtils;
import io.github.galaxyvn.ggarden.utils.GGardenUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public abstract class AbstractCommandManager extends Manager {

    private static final String ARGUMENT_PACKAGE = "io.github.galaxyvn.ggarden.command.argument";

    private final Map<Class<? extends GalaxyCommandArgumentHandler>, GalaxyCommandArgumentHandler<?>> argumentHandlers;
    private List<GalaxyCommandWrapper> commandWrappers;

    public AbstractCommandManager(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin);

        this.argumentHandlers = new HashMap<>();
    }

    @Override
    public final void reload() {
        if (this.commandWrappers == null) {
            this.commandWrappers = new ArrayList<>();
            for (Class<? extends GalaxyCommandWrapper> commandWrapperClass : this.getRootCommands()) {
                try {
                    Constructor<? extends GalaxyCommandWrapper> constructor = commandWrapperClass.getConstructor(GalaxyPlugin.class);
                    GalaxyCommandWrapper commandWrapper = constructor.newInstance(this.galaxyPlugin);
                    this.commandWrappers.add(commandWrapper);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            // Load arguments handlers
            List<Class<GalaxyCommandArgumentHandler>> argumentHandlerClasses = new ArrayList<>(ClassUtils.getClassesOf(this.galaxyPlugin, ARGUMENT_PACKAGE, GalaxyCommandArgumentHandler.class));
            this.getArgumentHandlerPackages().stream().map(x -> ClassUtils.getClassesOf(this.galaxyPlugin, x, GalaxyCommandArgumentHandler.class)).forEach(argumentHandlerClasses::addAll);

            for (Class<GalaxyCommandArgumentHandler> argumentHandlerClass : argumentHandlerClasses) {
                // Ignore abstract/interface classes
                if (Modifier.isAbstract(argumentHandlerClass.getModifiers()) || Modifier.isInterface(argumentHandlerClass.getModifiers()))
                    continue;

                GalaxyCommandArgumentHandler<?> argumentHandler = argumentHandlerClass.getConstructor(GalaxyPlugin.class).newInstance(this.galaxyPlugin);
                this.argumentHandlers.put(argumentHandlerClass, argumentHandler);
            }
        } catch (Exception e) {
            this.galaxyPlugin.getLogger().severe("Fatal error initializing command argument handlers");
            e.printStackTrace();
        }

        this.commandWrappers.forEach(GalaxyCommandWrapper::register);
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public final void disable() {
        this.argumentHandlers.clear();
        this.commandWrappers.forEach(GalaxyCommandWrapper::unregister);
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    public GalaxyCommandArgumentHandler<?> resolveArgumentHandler(Class<?> handledParameterClass) {
        if (Enum.class.isAssignableFrom(handledParameterClass))
            return this.argumentHandlers.get(EnumArgumentHandler.class);

        // Map primitive types to their wrapper handlers
        if (handledParameterClass.isPrimitive())
            handledParameterClass = GGardenUtils.getPrimitiveAsWrapper(handledParameterClass);

        Class<?> finalHandledParameterClass = handledParameterClass;
        return this.argumentHandlers.values()
                .stream()
                .filter(x -> x.getHandledType() != null && x.getHandledType() == finalHandledParameterClass)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tried to resolve a RoseCommandArgumentHandler for an unhandled type"));
    }



    public abstract List<Class<? extends GalaxyCommandWrapper>> getRootCommands();

    public abstract List<String> getArgumentHandlerPackages();

}
