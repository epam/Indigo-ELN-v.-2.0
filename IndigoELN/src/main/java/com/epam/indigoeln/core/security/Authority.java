package com.epam.indigoeln.core.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * Constants for Spring Security authorities.
 */
public enum Authority implements GrantedAuthority {
    ANONYMOUS,

    USER_EDITOR,
    ROLE_EDITOR,
    CONTENT_EDITOR,
    TEMPLATE_EDITOR,

    PROJECT_READER,
    PROJECT_CREATOR,
    PROJECT_REMOVER,

    NOTEBOOK_READER,
    NOTEBOOK_CREATOR,
    NOTEBOOK_REMOVER,

    EXPERIMENT_READER,
    EXPERIMENT_CREATOR,
    EXPERIMENT_REMOVER;

    @Override
    public String getAuthority() {
        return name();
    }

    public final static String[] COMPONENT_READERS = new String[] {
            EXPERIMENT_READER.name(), CONTENT_EDITOR.name()};
    public final static String[] COMPONENT_EDITORS = new String[] {
            EXPERIMENT_CREATOR.name(), CONTENT_EDITOR.name()};

    public final static String[] TEMPLATE_READERS = new String[] {
            TEMPLATE_EDITOR.name(), EXPERIMENT_CREATOR.name(),
            NOTEBOOK_CREATOR.name(), PROJECT_CREATOR.name()};

    public final static String[] USER_READERS = new String[] {
            USER_EDITOR.name(), EXPERIMENT_CREATOR.name(),
            NOTEBOOK_CREATOR.name(), PROJECT_CREATOR.name()};

    public final static String[] ROLE_READERS = new String[] {
            USER_EDITOR.name(), ROLE_EDITOR.name()};

    public final static String[] PROJECT_READERS = new String[] {
            PROJECT_READER.name(), CONTENT_EDITOR.name()};
    public final static String[] PROJECT_CREATORS = new String[] {
            PROJECT_CREATOR.name(), CONTENT_EDITOR.name()};
    public final static String[] PROJECT_REMOVERS = new String[] {
            PROJECT_REMOVER.name(), CONTENT_EDITOR.name()};

    public final static String[] NOTEBOOK_READERS = new String[] {
            NOTEBOOK_READER.name(), CONTENT_EDITOR.name()};
    public final static String[] NOTEBOOK_CREATORS = new String[] {
            NOTEBOOK_CREATOR.name(), CONTENT_EDITOR.name()};
    public final static String[] NOTEBOOK_REMOVERS = new String[] {
            NOTEBOOK_REMOVER.name(), CONTENT_EDITOR.name()};

    public final static String[] EXPERIMENT_READERS = new String[] {
            EXPERIMENT_READER.name(), CONTENT_EDITOR.name()};
    public final static String[] EXPERIMENT_CREATORS = new String[] {
            EXPERIMENT_CREATOR.name(), CONTENT_EDITOR.name()};
    public final static String[] EXPERIMENT_REMOVERS = new String[] {
            EXPERIMENT_REMOVER.name(), CONTENT_EDITOR.name()};


}