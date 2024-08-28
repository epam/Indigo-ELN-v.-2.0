/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.util;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.IncorrectAuthoritiesException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.security.Authority.*;

public final class AuthoritiesUtil {

    public static final String[] TEMPLATE_READERS = new String[]{
            TEMPLATE_EDITOR.name(), EXPERIMENT_CREATOR.name(),
            NOTEBOOK_CREATOR.name(), PROJECT_CREATOR.name(),
            CONTENT_EDITOR.name()};

    public static final String[] USER_READERS = new String[]{
            USER_EDITOR.name(), CONTENT_EDITOR.name(), EXPERIMENT_CREATOR.name(),
            NOTEBOOK_CREATOR.name(), PROJECT_CREATOR.name()};

    public static final String[] DICTIONARY_READERS = new String[]{
            DICTIONARY_EDITOR.name(), CONTENT_EDITOR.name(),
            EXPERIMENT_CREATOR.name(), NOTEBOOK_CREATOR.name(), PROJECT_CREATOR.name(),
            GLOBAL_SEARCH.name()};

    public static final String[] ROLE_READERS = new String[]{
            USER_EDITOR.name(), ROLE_EDITOR.name()};

    public static final String[] PROJECT_READERS = new String[]{
            PROJECT_READER.name(), CONTENT_EDITOR.name()};
    public static final String[] PROJECT_CREATORS = new String[]{
            PROJECT_CREATOR.name(), CONTENT_EDITOR.name()};
    public static final String[] PROJECT_REMOVERS = new String[]{
            PROJECT_REMOVER.name(), CONTENT_EDITOR.name()};

    public static final String[] NOTEBOOK_READERS = new String[]{
            NOTEBOOK_READER.name(), CONTENT_EDITOR.name()};
    public static final String[] NOTEBOOK_CREATORS = new String[]{
            NOTEBOOK_CREATOR.name(), CONTENT_EDITOR.name()};
    public static final String[] NOTEBOOK_REMOVERS = new String[]{
            NOTEBOOK_REMOVER.name(), CONTENT_EDITOR.name()};

    public static final String[] EXPERIMENT_READERS = new String[]{
            EXPERIMENT_READER.name(), CONTENT_EDITOR.name()};
    public static final String[] EXPERIMENT_CREATORS = new String[]{
            EXPERIMENT_CREATOR.name(), CONTENT_EDITOR.name()};
    public static final String[] EXPERIMENT_REMOVERS = new String[]{
            EXPERIMENT_REMOVER.name(), CONTENT_EDITOR.name()};

    private static final List<Authority> PROJECT_READERS_LIST;
    private static final List<Authority> NOTEBOOK_READERS_LIST;
    private static final List<Authority> EXPERIMENT_READERS_LIST;

    static {
        PROJECT_READERS_LIST = Arrays.stream(PROJECT_READERS)
                .map(Authority::valueOf)
                .collect(Collectors.toList());
        NOTEBOOK_READERS_LIST = Arrays.stream(NOTEBOOK_READERS)
                .map(Authority::valueOf)
                .collect(Collectors.toList());
        EXPERIMENT_READERS_LIST = Arrays.stream(EXPERIMENT_READERS)
                .map(Authority::valueOf)
                .collect(Collectors.toList());
    }

    private AuthoritiesUtil() {

    }

    public static void checkAuthorities(Set<Authority> authorities) {
        if (authorities.contains(Authority.PROJECT_CREATOR) && !authorities.contains(Authority.PROJECT_READER)) {
            throw IncorrectAuthoritiesException.create(Authority.PROJECT_READER);
        } else if (authorities.contains(Authority.NOTEBOOK_CREATOR)
                && !authorities.contains(Authority.NOTEBOOK_READER)) {
            throw IncorrectAuthoritiesException.create(Authority.NOTEBOOK_READER);
        } else if (authorities.contains(Authority.EXPERIMENT_CREATOR)
                && !authorities.contains(Authority.EXPERIMENT_READER)) {
            throw IncorrectAuthoritiesException.create(Authority.EXPERIMENT_READER);
        }
    }

    public static boolean canReadExperiment(User user) {
        return EXPERIMENT_READERS_LIST.stream()
                .anyMatch(user.getAuthorities()::contains);
    }

    public static boolean canReadProject(User user) {
        return PROJECT_READERS_LIST.stream()
                .anyMatch(user.getAuthorities()::contains);
    }

    public static boolean canReadNotebook(User user) {
        return NOTEBOOK_READERS_LIST.stream()
                .anyMatch(user.getAuthorities()::contains);
    }
}
