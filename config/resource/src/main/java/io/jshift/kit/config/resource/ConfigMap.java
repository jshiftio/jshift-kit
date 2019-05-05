package io.jshift.kit.config.resource;

import java.util.ArrayList;
import java.util.List;

public class ConfigMap {

    private List<ConfigMapEntry> entries = new ArrayList<>();

    public void addEntry(ConfigMapEntry configMapEntry) {
        this.entries.add(configMapEntry);
    }

    public List<ConfigMapEntry> getEntries() {
        return entries;
    }
}

