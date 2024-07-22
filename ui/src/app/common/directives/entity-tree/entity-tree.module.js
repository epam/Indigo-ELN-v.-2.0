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

require('./entity-tree.less');
var entityTree = require('./entity-tree.directive');
var simpleNode = require('./simple-node/simple-node.directive');

var entityTreeService = require('./entity-tree.service');
var entityTreeFactory = require('./entity-tree.factory');

var run = require('./entity-tree.run');
var appSidebar = require('../../../app-layout/app-sidebar/app-sidebar.module');

var dependencies = [
    appSidebar
];

module.exports = angular
    .module('indigoeln.entityTree', dependencies)

    .directive('entityTree', entityTree)
    .directive('simpleNode', simpleNode)

    .factory('entityTreeFactory', entityTreeFactory)
    .factory('entityTreeService', entityTreeService)

    .run(run)

    .name;

