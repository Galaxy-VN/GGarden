package io.github.galaxyvn.ggarden;

import io.github.galaxyvn.ggarden.command.GgdCommand;
import io.github.galaxyvn.ggarden.manager.AbstractCommandManager;
import io.github.galaxyvn.ggarden.manager.AbstractConfigurationManager;
import io.github.galaxyvn.ggarden.manager.AbstractDataManager;
import io.github.galaxyvn.ggarden.manager.AbstractLocaleManager;
import io.github.galaxyvn.ggarden.manager.DataMigrationManager;
import io.github.galaxyvn.ggarden.manager.Manager;
import io.github.galaxyvn.ggarden.manager.PluginUpdateManager;
import io.github.galaxyvn.ggarden.objects.GalaxyPluginData;
import io.github.galaxyvn.ggarden.utils.CommandMapUtils;
import io.github.galaxyvn.ggarden.utils.GGardenUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GalaxyPlugin extends JavaPlugin {

    /**
     * The GalaxyPlugin identifier
     */
    public static final String GGARDEN_VERSION = "@version@";

    /**
     * The plugin ID on Spigot
     */
    private final int spigotId;

    /**
     * The plugin ID on bStats
     */
    private final int bStatsId;

    /**
     * The classes that extend the abstract managers
     */
    private final Class<? extends AbstractConfigurationManager> configurationManagerClass;
    private final Class<? extends AbstractDataManager> dataManagerClass;
    private final Class<? extends AbstractLocaleManager> localeManagerClass;
    private final Class<? extends AbstractCommandManager> commandManagerClass;

    /**
     * The plugin managers
     */
    private final Map<Class<? extends Manager>, Manager> managers;

    public GalaxyPlugin(int spigotId,
                      int bStatsId,
                      Class<? extends AbstractConfigurationManager> configurationManagerClass,
                      Class<? extends AbstractDataManager> dataManagerClass,
                      Class<? extends AbstractLocaleManager> localeManagerClass,
                      Class<? extends AbstractCommandManager> commandManagerClass) {
        if (configurationManagerClass != null && Modifier.isAbstract(configurationManagerClass.getModifiers()))
            throw new IllegalArgumentException("configurationManagerClass cannot be abstract");
        if (dataManagerClass != null && Modifier.isAbstract(dataManagerClass.getModifiers()))
            throw new IllegalArgumentException("dataManagerClass cannot be abstract");
        if (localeManagerClass != null && Modifier.isAbstract(localeManagerClass.getModifiers()))
            throw new IllegalArgumentException("localeManagerClass cannot be abstract");
        if (commandManagerClass != null && Modifier.isAbstract(commandManagerClass.getModifiers()))
            throw new IllegalArgumentException("commandManagerClass cannot be abstract");

        this.spigotId = spigotId;
        this.bStatsId = bStatsId;
        this.configurationManagerClass = configurationManagerClass;
        this.dataManagerClass = dataManagerClass;
        this.localeManagerClass = localeManagerClass;
        this.commandManagerClass = commandManagerClass;

        this.managers = new LinkedHashMap<>();
    }

    @Override
    public final void onEnable() {
        // Log that we are loading
        this.getLogger().info("Initializing using GGarden v" + GGARDEN_VERSION);

        // bStats Metrics
        if (this.bStatsId != -1) {
            Metrics metrics = new Metrics(this, this.bStatsId);
            this.addCustomMetricsCharts(metrics);
        }

        // Check if the library is relocated properly
        if (!GGardenUtils.isRelocated()) {
            GGardenUtils.getLogger().severe("DEVELOPER ERROR!!! GGarden has not been relocated! Plugin has been forcefully disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load managers
        this.reload();

        // Run the plugin's enable code
        this.enable();

        // Inject the plugin class into the spigot services manager
        this.injectService();
    }

    @Override
    public final void onDisable() {
        // Run the plugin's disable code
        this.disable();

        // Shut down the managers
        this.disableManagers();
        this.managers.clear();
    }

    /**
     * Called during {@link JavaPlugin#onEnable}
     */
    protected abstract void enable();

    /**
     * Called during {@link JavaPlugin#onDisable}
     */
    protected abstract void disable();

    /**
     * @return the order in which Managers should be loaded
     */
    protected abstract List<Class<? extends Manager>> getManagerLoadPriority();

    /**
     * Registers any custom bStats Metrics charts for the plugin
     *
     * @param metrics The Metrics instance
     */
    protected void addCustomMetricsCharts(Metrics metrics) {
        // Must be overridden for any functionality.
    }

    /**
     * Reloads the plugin's managers
     */
    public void reload() {
        this.disableManagers();
        this.managers.values().forEach(Manager::reload);

        List<Class<? extends Manager>> managerLoadPriority = new ArrayList<>();

        if (this.hasConfigurationManager())
            managerLoadPriority.add(this.configurationManagerClass);

        if (this.hasDataManager()) {
            managerLoadPriority.add(this.dataManagerClass);
            managerLoadPriority.add(DataMigrationManager.class);
        }

        if (this.hasLocaleManager())
            managerLoadPriority.add(this.localeManagerClass);

        if (this.hasCommandManager())
            managerLoadPriority.add(this.commandManagerClass);

        managerLoadPriority.addAll(this.getManagerLoadPriority());

        if (this.spigotId != -1)
            managerLoadPriority.add(PluginUpdateManager.class);

        managerLoadPriority.forEach(this::getManager);
    }

    /**
     * Runs {@link Manager#disable} on all managers in the reverse order that they were loaded
     */
    private void disableManagers() {
        List<Manager> managers = new ArrayList<>(this.managers.values());
        Collections.reverse(managers);
        managers.forEach(Manager::disable);
    }

    /**
     * Gets a manager instance
     *
     * @param managerClass The class of the manager to get
     * @param <T> extends Manager
     * @return A new or existing instance of the given manager class
     */
    @SuppressWarnings("unchecked")
    public final <T extends Manager> T getManager(Class<T> managerClass) {
        if (this.managers.containsKey(managerClass))
            return (T) this.managers.get(managerClass);

        // Get the actual class if the abstract one is requested
        if (this.hasConfigurationManager() && managerClass == AbstractConfigurationManager.class) {
            return this.getManager((Class<T>) this.configurationManagerClass);
        } else if (this.hasDataManager() && managerClass == AbstractDataManager.class) {
            return this.getManager((Class<T>) this.dataManagerClass);
        } else if (this.hasLocaleManager() && managerClass == AbstractLocaleManager.class) {
            return this.getManager((Class<T>) this.localeManagerClass);
        } else if (this.hasCommandManager() && managerClass == AbstractCommandManager.class) {
            return this.getManager((Class<T>) this.commandManagerClass);
        }

        try {
            T manager = managerClass.getConstructor(GalaxyPlugin.class).newInstance(this);
            this.managers.put(managerClass, manager);
            manager.reload();
            return manager;
        } catch (Exception ex) {
            throw new ManagerNotFoundException(managerClass, ex);
        }
    }

    /**
     * @return the ID of the plugin on Spigot, or -1 if not tracked
     */
    public int getSpigotId() {
        return this.spigotId;
    }

    /**
     * @return the ID of this plugin on bStats, or -1 if not tracked
     */
    public int getBStatsId() {
        return this.bStatsId;
    }

    private void injectService() {
        // Search for other GalaxyGarden services
        boolean exists = !this.getLoadedGalaxyPluginsData().isEmpty();

        // Register our service
        Bukkit.getServicesManager().register(GalaxyPlugin.class, this, this, ServicePriority.Normal);

        // If we aren't the first then don't continue
        if (exists)
            return;

        // Register /ggd command
        CommandMapUtils.registerCommand("galaxygarden", new GgdCommand(this));
    }

    /**
     * @return data of all GalaxyPlugins installed on the server
     */
    public List<GalaxyPluginData> getLoadedGalaxyPluginsData() {
        List<GalaxyPluginData> data = new ArrayList<>();

        ServicesManager servicesManager = Bukkit.getServicesManager();
        for (Class<?> service : servicesManager.getKnownServices()) {
            try {
                String gGardenVersion = (String) service.getField("GGARDEN_VERSION").get(null);
                Method updateVersionMethod = service.getMethod("getUpdateVersion");

                for (RegisteredServiceProvider<?> provider : servicesManager.getRegistrations(service)) {
                    Plugin plugin = provider.getPlugin();
                    String pluginName = plugin.getName();
                    String pluginVersion = plugin.getDescription().getVersion();
                    String website = plugin.getDescription().getWebsite();
                    String updateVersion = (String) updateVersionMethod.invoke(plugin);
                    data.add(new GalaxyPluginData(pluginName, pluginVersion, updateVersion, website, gGardenVersion));
                }
            } catch (ReflectiveOperationException | ClassCastException ignored) { }
        }

        return data;
    }

    /**
     * @return the data folder for GGarden
     */
    public File getGGardenDataFolder() {
        File configDir = new File(this.getDataFolder().getParentFile(), "GalaxyGarden");
        if (!configDir.exists())
            configDir.mkdirs();
        return configDir;
    }

    /**
     * @return the version of the latest update of this plugin, or null if there is none
     */
    public String getUpdateVersion() {
        return this.getManager(PluginUpdateManager.class).getUpdateVersion();
    }

    public boolean hasConfigurationManager() {
        return this.configurationManagerClass != null;
    }

    public boolean hasDataManager() {
        return this.dataManagerClass != null;
    }

    public boolean hasLocaleManager() {
        return this.localeManagerClass != null;
    }

    public boolean hasCommandManager() {
        return this.commandManagerClass != null;
    }

    /**
     * An exception thrown when a Manager fails to load
     */
    private static class ManagerNotFoundException extends RuntimeException {

        public ManagerNotFoundException(Class<? extends Manager> managerClass, Throwable cause) {
            super("Failed to load " + managerClass.getSimpleName(), cause);
        }

    }

}