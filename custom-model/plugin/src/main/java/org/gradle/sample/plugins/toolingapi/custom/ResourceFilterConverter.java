package org.gradle.sample.plugins.toolingapi.custom;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceFilterConverter {

    public static ResourceFilter from(org.gradle.plugins.ide.eclipse.model.ResourceFilter gradleFilter) {
        return new DefaultResourceFilter(createAppliesTo(gradleFilter), createFilterType(gradleFilter), gradleFilter.isRecursive(), createMatcher(gradleFilter.getMatcher()));
    }

    private static ResourceFilterAppliesTo createAppliesTo(org.gradle.plugins.ide.eclipse.model.ResourceFilter gradleFilter) {
        return ResourceFilterAppliesTo.valueOf(gradleFilter.getAppliesTo().name());
    }

    private static ResourceFilterType createFilterType(org.gradle.plugins.ide.eclipse.model.ResourceFilter gradleFilter) {
        return ResourceFilterType.valueOf(gradleFilter.getType().name());
    }

    private static ResourceFilterMatcher createMatcher(org.gradle.plugins.ide.eclipse.model.ResourceFilterMatcher gradleMatcher) {
        Set<ResourceFilterMatcher> children = new LinkedHashSet<>(gradleMatcher.getChildren().stream().map(ResourceFilterConverter::createMatcher).collect(Collectors.toList()));
        return new DefaultResourceFilterMatcher(gradleMatcher.getId(), gradleMatcher.getArguments(), children);
    }
}
