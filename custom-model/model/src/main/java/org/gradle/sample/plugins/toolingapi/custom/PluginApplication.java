package org.gradle.sample.plugins.toolingapi.custom;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PluginApplication {
    Map<File, List<ResourceFilter>> getResourceFilters();
}
