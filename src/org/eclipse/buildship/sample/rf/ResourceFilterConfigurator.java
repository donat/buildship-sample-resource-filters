package org.eclipse.buildship.sample.rf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gradle.sample.plugins.toolingapi.custom.PluginApplication;
import org.gradle.sample.plugins.toolingapi.custom.ResourceFilter;
import org.gradle.sample.plugins.toolingapi.custom.ResourceFilterAppliesTo;
import org.gradle.sample.plugins.toolingapi.custom.ResourceFilterMatcher;
import org.gradle.sample.plugins.toolingapi.custom.ResourceFilterType;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;


public class ResourceFilterConfigurator implements ProjectConfigurator {

    private Map<File, List<ResourceFilter>> resourceFilters;

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 3);
        String initScriptPath = createInitScript(Activator.getBundleLocation(), progress.newChild(1));
        this.resourceFilters = collectJavaPluginInfo(context.getGradleBuild(), initScriptPath, progress.newChild(1));
    }

    private static String createInitScript(File pluginLocation, IProgressMonitor monitor) {
        String initScriptContent = ""
                + "\n initscript {"
                + "\n     repositories {"
                + "\n         maven {"
                + "\n             url new File('" + pluginLocation.getAbsolutePath() + "/custom-model/repo').toURI().toURL()"
                + "\n         }"
                + "\n     }"
                + "\n"
                + "\n     dependencies {"
                + "\n         classpath 'org.gradle.sample.plugins.toolingapi:plugin:1.0'"
                + "\n     }"
                + "\n }"
                + "\n"
                + "\n allprojects {"
                + "\n    apply plugin: org.gradle.sample.plugins.toolingapi.custom.ToolingApiCustomModelPlugin"
                + "\n }";

        File initScript = new File(System.getProperty("java.io.tmpdir"), "buildship-resource-filter-init.gradle");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(initScript));
            writer.write(initScriptContent);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store init script");
        }

        return initScript.getAbsolutePath();
    }

    private static Map<File, List<ResourceFilter>> collectJavaPluginInfo(GradleBuild gradleBuild, String initScriptPath, IProgressMonitor monitor) {
        try {
            return gradleBuild.withConnection(connection -> {
                PluginApplication model = connection.model(PluginApplication.class).withArguments("--init-script", initScriptPath).get();
                return model.getResourceFilters();
            }, monitor);
        } catch (Exception e) {
            Activator.getInstance().getLog().log(new Status(IStatus.WARNING, "org.eclipse.buildship.sample.custommodel", "Failed to query custom model", e));
            return Collections.emptyMap();
        }
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        IProject project = context.getProject();
        IPath location = project.getLocation();
        if (location == null) {
            return;
        }
        List<ResourceFilter> filters = this.resourceFilters.getOrDefault(location.toFile(), Collections.emptyList());

        if (!filters.isEmpty()) {
            defineFilters(project, filters, monitor);
        }
    }

    private void defineFilters(IProject project, List<ResourceFilter> filters, IProgressMonitor monitor) {
        try {
            Map<String, ResourceFilter> filterIds = filters.stream().collect(Collectors.toMap(ResourceFilterConfigurator::filterUid, Function.identity()));
            Map<String, IResourceFilterDescription> existingFilterIds = Arrays.stream(project.getFilters()).collect(Collectors.toMap(ResourceFilterConfigurator::filterUid, Function.identity()));


            Set<String> idsToRemove = new HashSet<>(existingFilterIds.keySet());
            idsToRemove.removeAll(filterIds.keySet());

            Set<String> idsToAdd = new HashSet<>(filterIds.keySet());
            idsToAdd.removeAll(existingFilterIds.keySet());

            for (String id : idsToRemove) {
                existingFilterIds.get(id).delete(IResource.NONE, monitor);
            }

            for (String id : idsToAdd) {
                ResourceFilter filter = filterIds.get(id);

                ResourceFilterType type = filter.getType();
                ResourceFilterAppliesTo appliesTo = filter.getAppliesTo();
                ResourceFilterMatcher matcher = filter.getMatcher();

                int typeId = type == ResourceFilterType.INCLUDE_ONLY ? 1 : 2;
                int appliesToId = appliesTo == ResourceFilterAppliesTo.FILES ? 4 : appliesTo == ResourceFilterAppliesTo.FILES ? 8 : 12;
                project.createFilter(typeId | appliesToId, createMatcherDescription(matcher), IResource.BACKGROUND_REFRESH, monitor);
            }

        } catch (CoreException e) {
            Activator.getInstance().getLog().log(e.getStatus());
        }
    }

    private static FileInfoMatcherDescription createMatcherDescription(ResourceFilterMatcher matcher) {
        if (matcher.getChildren().isEmpty()) {
            return new FileInfoMatcherDescription(matcher.getId(), matcher.getArguments());
        }

        FileInfoMatcherDescription[] arguments = matcher.getChildren().stream().map(ResourceFilterConfigurator::createMatcherDescription).toArray(FileInfoMatcherDescription[]::new);
        return new FileInfoMatcherDescription(matcher.getId(), arguments);

    }

    private static String filterUid(ResourceFilter resourceFilter) {
        ResourceFilterType type = resourceFilter.getType();
        ResourceFilterAppliesTo appliesTo = resourceFilter.getAppliesTo();
        int typeId = type == ResourceFilterType.INCLUDE_ONLY ? 1 : 2;
        int appliesToId = appliesTo == ResourceFilterAppliesTo.FILES ? 4 : appliesTo == ResourceFilterAppliesTo.FILES ? 8 : 12;
        return String.valueOf(typeId | appliesToId) + "_" + matcherUid(resourceFilter.getMatcher());
    }

    private static String filterUid(IResourceFilterDescription resourceFilter) {
        return resourceFilter.getType() + "_" + matcherUid(resourceFilter.getFileInfoMatcherDescription());
    }

    private static String matcherUid(ResourceFilterMatcher matcher) {
       if (matcher.getChildren().isEmpty()) {
           return matcher.getId() + matcher.getArguments();
       }
       return matcher.getId() + matcher.getChildren().stream().map(ResourceFilterConfigurator::matcherUid).collect(Collectors.joining());
    }

    private static String matcherUid(FileInfoMatcherDescription matcher) {
        Object args = matcher.getArguments();
        if (args == null) {
            args = "";
        }

        if (args instanceof String) {
            return String.valueOf(matcher.getId()) + matcher.getArguments();
        }
        else if (args instanceof FileInfoMatcherDescription[]) {
            return String.valueOf(matcher.getId()) + Arrays.stream((FileInfoMatcherDescription[])args).map(ResourceFilterConfigurator::matcherUid).collect(Collectors.joining());
        } else {
            throw new RuntimeException("Unsupported filter type: " + args.getClass());
        }
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
    }

}
