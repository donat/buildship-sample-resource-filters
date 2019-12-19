package org.gradle.sample.plugins.toolingapi.custom;


public interface ResourceFilter {
    ResourceFilterAppliesTo getAppliesTo();
    ResourceFilterType getType();
    boolean isRecursive();
    ResourceFilterMatcher getMatcher();
}
