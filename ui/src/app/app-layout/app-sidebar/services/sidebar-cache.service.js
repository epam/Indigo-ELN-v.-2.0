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
function sidebarCache(CacheFactory, principalService) {
    var aDay = 86400000;
    var aMinute = 3600;

    var cache = CacheFactory('sidebarCache', {
        storageMode: 'localStorage',
        maxAge: aDay * 7,
        deleteOnExpire: 'aggressive',
        recycleFreq: aMinute * 5
    });

    return {
        put: put,
        get: get
    };

    function put(path, data) {
        cache.put(principalService.getIdentity().id + path, data);
    }

    function get(path) {
        return cache.get(principalService.getIdentity().id + path);
    }
}

module.exports = sidebarCache;
