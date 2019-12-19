package org.gradle.sample.plugins.toolingapi.custom;

import java.io.Serializable;


public class DefaultResourceFilter implements ResourceFilter, Serializable {

    private static final long serialVersionUID = 2872636291252597281L;
    private final ResourceFilterAppliesTo appliesTo;
    private final ResourceFilterType type;
    private final boolean recursive;
    private final ResourceFilterMatcher matcher;

    public DefaultResourceFilter(ResourceFilterAppliesTo appliesTo, ResourceFilterType type, boolean recursive, ResourceFilterMatcher matcher) {
        this.appliesTo = appliesTo;
        this.type = type;
        this.recursive = recursive;
        this.matcher = matcher;
    }

    @Override
    public ResourceFilterAppliesTo getAppliesTo() {
        return this.appliesTo;
    }

    @Override
    public ResourceFilterType getType() {
        return this.type;
    }

    @Override
    public boolean isRecursive() {
        return this.recursive;
    }

    @Override
    public ResourceFilterMatcher getMatcher() {
        return this.matcher;
    }
}
