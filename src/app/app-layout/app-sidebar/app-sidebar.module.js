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

require('./component/sidebar.less');

var sidebar = require('./component/sidebar.directive');
var allExperimentsService = require('./resources/all-experiments.service');
var allNotebooksService = require('./resources/all-notebooks.service');
var allProjectsService = require('./resources/all-projects.service');
var sidebarCache = require('./services/sidebar-cache.service');

module.exports = angular
    .module('indigoeln.appSidebar', [])

    .directive('sidebar', sidebar)

    .factory('allExperimentsService', allExperimentsService)
    .factory('allNotebooksService', allNotebooksService)
    .factory('allProjectsService', allProjectsService)
    .factory('sidebarCache', sidebarCache)

    .name;
