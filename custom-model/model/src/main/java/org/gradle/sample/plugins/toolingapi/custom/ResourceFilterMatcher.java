package org.gradle.sample.plugins.toolingapi.custom;

import java.util.Set;

public interface ResourceFilterMatcher {
    String getId();
    String getArguments();
    Set<ResourceFilterMatcher> getChildren();
}
