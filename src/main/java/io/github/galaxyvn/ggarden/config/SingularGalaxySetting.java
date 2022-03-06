package io.github.galaxyvn.ggarden.config;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.manager.AbstractConfigurationManager;

public class SingularGalaxySetting implements GalaxySetting {

    private final GalaxyPlugin galaxyPlugin;
    private final String key;
    private final Object defaultValue;
    private final String[] comments;
    private Object value = null;

    public SingularGalaxySetting(GalaxyPlugin galaxyPlugin, String key, Object defaultValue, String... comments) {
        this.galaxyPlugin = galaxyPlugin;
        this.key = key;
        this.defaultValue = defaultValue;
        this.comments = comments != null ? comments : new String[0];
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String[] getComments() {
        return this.comments;
    }

    @Override
    public Object getCachedValue() {
        return this.value;
    }

    @Override
    public void setCachedValue(Object value) {
        this.value = value;
    }

    @Override
    public CommentedFileConfiguration getBaseConfig() {
        return this.galaxyPlugin.getManager(AbstractConfigurationManager.class).getConfig();
    }
    
}
