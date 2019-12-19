package org.gradle.sample.plugins.toolingapi.custom;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PluginApplicationImpl implements Serializable, PluginApplication {
    private static final long serialVersionUID = 1L;

    private Map<File, List<ResourceFilter>> projectLocToResourceFilters;

     public PluginApplicationImpl(Map<File, List<ResourceFilter>> projectLocToResourceFilters) {
        this.projectLocToResourceFilters = projectLocToResourceFilters;
    }

     @Override
    public Map<File, List<ResourceFilter>> getResourceFilters() {
        return this.projectLocToResourceFilters;
    }
}
