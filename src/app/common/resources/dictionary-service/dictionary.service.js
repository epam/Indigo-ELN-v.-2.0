/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function dictionaryService($resource, CacheFactory, apiUrl) {
    var dictionaryCache = CacheFactory.createCache('dictionaryCache', {
        maxAge: 5 * 60 * 1000,
        deleteOnExpire: 'passive'
    });

    return $resource(apiUrl + 'dictionaries/:id', {}, {
        query: {
            method: 'GET',
            isArray: true
        },
        get: {
            method: 'GET'
        },
        all: {
            url: apiUrl + 'dictionaries/all',
            method: 'GET',
            isArray: true
        },
        getByName: {
            url: apiUrl + 'dictionaries/byName/:name',
            method: 'GET',
            cache: dictionaryCache
        },
        save: {
            method: 'POST'
        },
        update: {
            method: 'PUT'
        },
        delete: {
            method: 'DELETE'
        }
    });
}

module.exports = dictionaryService;
