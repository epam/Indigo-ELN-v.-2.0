package com.epam.indigoeln.core.repository;

/**
 * Constants for Repository classes
 */
public final class RepositoryConstants {
    private RepositoryConstants() {
    }

    public static final String ENTITY_SHORT_FIELDS_SET = "{'$id' : 1, 'name' : 1, 'creationDate' : 1, 'lastEditDate' : 1, " +
            "'author.id' : 1, 'author.login' : 1, 'author.firstName' : 1, " +
            "'author.lastName' : 1, 'author.email' : 1, " +
            "'lastModifiedBy.id' : 1, 'lastModifiedBy.login' : 1, 'lastModifiedBy.firstName' : 1, " +
            "'lastModifiedBy.lastName' : 1, 'lastModifiedBy.email' : 1}";
}
