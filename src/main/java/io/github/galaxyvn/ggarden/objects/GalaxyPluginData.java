package io.github.galaxyvn.ggarden.objects;

public class GalaxyPluginData {

    private final String name;
    private final String version;
    private final String updateVersion;
    private final String website;
    private final String galaxyGardenVersion;

    public GalaxyPluginData(String name, String version, String updateVersion, String website, String galaxyGardenVersion) {
        this.name = name;
        this.version = version;
        this.updateVersion = updateVersion;
        this.website = website;
        this.galaxyGardenVersion = galaxyGardenVersion;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getUpdateVersion() {
        return this.updateVersion;
    }

    public String getWebsite() {
        return this.website;
    }

    public String getGalaxyGardenVersion() {
        return this.galaxyGardenVersion;
    }
}
