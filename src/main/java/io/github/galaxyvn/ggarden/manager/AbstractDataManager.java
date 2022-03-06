package io.github.galaxyvn.ggarden.manager;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.config.GalaxySetting;
import io.github.galaxyvn.ggarden.database.DataMigration;
import io.github.galaxyvn.ggarden.database.DatabaseConnector;
import io.github.galaxyvn.ggarden.database.MySQLConnector;
import io.github.galaxyvn.ggarden.database.SQLiteConnector;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

public abstract class AbstractDataManager extends Manager {

    protected DatabaseConnector databaseConnector;

    public AbstractDataManager(GalaxyPlugin galaxyPlugin) {
        super(galaxyPlugin);
    }

    @Override
    public final void reload() {
        try {
            AbstractConfigurationManager configurationManager = this.galaxyPlugin.getManager(AbstractConfigurationManager.class);
            Map<String, GalaxySetting> galaxySettings = configurationManager.getSettings();

            if (galaxySettings.get("mysql-settings.enabled").getBoolean()) {
                String hostname = galaxySettings.get("mysql-settings.hostname").getString();
                int port = galaxySettings.get("mysql-settings.port").getInt();
                String database = galaxySettings.get("mysql-settings.database-name").getString();
                String username = galaxySettings.get("mysql-settings.user-name").getString();
                String password = galaxySettings.get("mysql-settings.user-password").getString();
                boolean useSSL = galaxySettings.get("mysql-settings.use-ssl").getBoolean();
                int poolSize = galaxySettings.get("mysql-settings.connection-pool-size").getInt();

                this.databaseConnector = new MySQLConnector(this.galaxyPlugin, hostname, port, database, username, password, useSSL, poolSize);
                this.galaxyPlugin.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this.galaxyPlugin);
                this.databaseConnector.cleanup();
                this.galaxyPlugin.getLogger().info("Data handler connected using SQLite.");
            }
        } catch (Exception ex) {
            this.galaxyPlugin.getLogger().severe("Fatal error trying to connect to database. Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this.galaxyPlugin);
        }
    }

    @Override
    public final void disable() {
        if (this.databaseConnector == null)
            return;

        // Wait for all database connections to finish
        long now = System.currentTimeMillis();
        long deadline = now + 5000; // Wait at most 5 seconds
        synchronized (this.databaseConnector.getLock()) {
            while (!this.databaseConnector.isFinished() && now < deadline) {
                try {
                    this.databaseConnector.getLock().wait(deadline - now);
                    now = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.databaseConnector.closeConnection();
    }

    /**
     * @return true if the database connection is established, otherwise false
     */
    public final boolean isConnected() {
        return this.databaseConnector != null;
    }

    /**
     * @return The connector to the database
     */
    public final DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.galaxyPlugin.getDescription().getName().toLowerCase() + '_';
    }

    /**
     * @return all data migrations for the DataMigrationManager to handle
     */
    public abstract List<Class<? extends DataMigration>> getDataMigrations();
    
}
