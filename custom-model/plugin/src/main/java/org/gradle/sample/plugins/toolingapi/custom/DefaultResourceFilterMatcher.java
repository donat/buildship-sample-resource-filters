package org.gradle.sample.plugins.toolingapi.custom;

import java.io.Serializable;
import java.util.Set;

public class DefaultResourceFilterMatcher implements ResourceFilterMatcher, Serializable {

    private static final long serialVersionUID = 5037720853233852513L;
    private final String id;
    private final String arguments;
    private final Set<ResourceFilterMatcher> children;

    public DefaultResourceFilterMatcher(String id, String arguments, Set<ResourceFilterMatcher> children) {
        this.id = id;
        this.arguments = arguments;
        this.children = children;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getArguments() {
        return this.arguments;
    }

    @Override
    public Set<ResourceFilterMatcher> getChildren() {
        return this.children;
    }

}
