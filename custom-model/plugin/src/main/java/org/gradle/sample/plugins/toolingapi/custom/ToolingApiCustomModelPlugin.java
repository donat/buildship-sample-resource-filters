package org.gradle.sample.plugins.toolingapi.custom;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

public class ToolingApiCustomModelPlugin implements Plugin<Project> {

    private final ToolingModelBuilderRegistry registry;

    @Inject
    public ToolingApiCustomModelPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project project) {
        this.registry.register(new CustomToolingModelBuilder());
    }

    private static class CustomToolingModelBuilder implements ToolingModelBuilder {

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(PluginApplication.class.getName());
        }

        @Override
        public Object buildAll(String modelName, Project rootProject) {
            Set<Project> allProject = rootProject.getAllprojects();
            Map<File, List<ResourceFilter>> projectLocToResourceFilters = new HashMap<>();
            for (Project project : allProject) {
                EclipseModel model = (EclipseModel) project.getExtensions().getByName("eclipse");
                List<ResourceFilter> filters = model.getProject().getResourceFilters().stream().map(ResourceFilterConverter::from).collect(Collectors.toList());
                projectLocToResourceFilters.put(project.getProjectDir(), filters);

            }
            return new PluginApplicationImpl(projectLocToResourceFilters);
        }
    }
}
