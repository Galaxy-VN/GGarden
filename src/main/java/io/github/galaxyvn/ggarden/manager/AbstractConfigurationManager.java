package io.github.galaxyvn.ggarden.manager;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.config.CommentedFileConfiguration;
import io.github.galaxyvn.ggarden.config.GalaxySetting;
import io.github.galaxyvn.ggarden.config.SingularGalaxySetting;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractConfigurationManager extends Manager {

    private static final String[] FOOTER = new String[] {
            "That's everything! You reached the end of the configuration.",
            "Enjoy the plugin!"
    };

    private final Class<? extends GalaxySetting> settingEnum;
    private CommentedFileConfiguration configuration;
    private Map<String, GalaxySetting> cachedValues;

    public AbstractConfigurationManager(GalaxyPlugin galaxyPlugin, Class<? extends GalaxySetting> settingEnum) {
        super(galaxyPlugin);

        if (!settingEnum.isEnum())
            throw new IllegalArgumentException("settingEnum class must be of type Enum");

        this.settingEnum = settingEnum;
    }

    @Override
    public final void reload() {
        File configFile = new File(this.galaxyPlugin.getDataFolder(), "config.yml");
        boolean setHeaderFooter = !configFile.exists();
        boolean changed = setHeaderFooter;

        this.configuration = CommentedFileConfiguration.loadConfiguration(configFile);

        if (setHeaderFooter)
            this.configuration.addComments(this.getHeader());

        for (GalaxySetting setting : this.getSettings().values()) {
            setting.reset();
            changed |= setting.setIfNotExists(this.configuration);
        }

        if (setHeaderFooter)
            this.configuration.addComments(FOOTER);

        if (changed)
            this.configuration.save();
    }

    @Override
    public final void disable() {
        for (GalaxySetting setting : this.getSettings().values())
            setting.reset();
    }

    /**
     * @return the header to place at the top of the configuration file
     */
    protected abstract String[] getHeader();

    /**
     * @return the config.yml as a CommentedFileConfiguration
     */
    public final CommentedFileConfiguration getConfig() {
        return this.configuration;
    }

    /**
     * @return the values of the setting enum
     */
    public Map<String, GalaxySetting> getSettings() {
        if (this.cachedValues == null) {
            try {
                GalaxySetting[] galaxySettings = (GalaxySetting[]) this.settingEnum.getDeclaredMethod("values").invoke(null);
                this.cachedValues = new LinkedHashMap<>();
                for (GalaxySetting galaxySetting : galaxySettings)
                    this.cachedValues.put(galaxySetting.getKey(), galaxySetting);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
                this.cachedValues = Collections.emptyMap();
            }

            this.injectAdditionalSettings();
        }

        return this.cachedValues;
    }

    /**
     * Injects additional settings into the config
     */
    private void injectAdditionalSettings() {
        Map<String, GalaxySetting> values = this.cachedValues;
        this.cachedValues = new LinkedHashMap<>();

        if (this.galaxyPlugin.hasLocaleManager())
            this.cachedValues.put("locale", new SingularGalaxySetting(this.galaxyPlugin, "locale", "en_US", "The locale to use in the /locale folder"));

        this.cachedValues.putAll(values);

        if (this.galaxyPlugin.hasDataManager()) {
            Arrays.asList(
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings", null, "Settings for if you want to use MySQL for data management"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.enabled", false, "Enable MySQL", "If false, SQLite will be used instead"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.hostname", "", "MySQL Database Hostname"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.port", 3306, "MySQL Database Port"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.database-name", "", "MySQL Database Name"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.user-name", "", "MySQL Database User Name"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.user-password", "", "MySQL Database User Password"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.use-ssl", false, "If the database connection should use SSL", "You should enable this if your database supports SSL"),
                    new SingularGalaxySetting(this.galaxyPlugin, "mysql-settings.connection-pool-size", 3, "The number of connections to make to the database")
            ).forEach(x -> this.cachedValues.put(x.getKey(), x));
        }
    }

}
