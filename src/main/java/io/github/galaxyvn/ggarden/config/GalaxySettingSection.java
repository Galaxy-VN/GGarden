package io.github.galaxyvn.ggarden.config;

import java.util.Arrays;
import java.util.List;

public class GalaxySettingSection {

    private final List<GalaxySettingValue> values;

    public GalaxySettingSection(GalaxySettingValue... values) {
        this.values = Arrays.asList(values);
    }

    public List<GalaxySettingValue> getValues() {
        return this.values;
    }

}
