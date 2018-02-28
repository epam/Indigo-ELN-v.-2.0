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

var template = require('./sidebar.html');
var roles = require('../../../permissions/permission-roles.json');

function sidebar() {
    return {
        scope: true,
        template: template,
        controller: SidebarController,
        bindToController: true,
        controllerAs: 'vm'
    };
}

/* @ngInject */
function SidebarController($scope, $state, $stateParams, sidebarCache) {
    var vm = this;

    vm.CONTENT_EDITOR = roles.CONTENT_EDITOR;
    vm.USER_EDITOR = roles.USER_EDITOR;
    vm.ROLE_EDITOR = roles.ROLE_EDITOR;
    vm.TEMPLATE_EDITOR = roles.TEMPLATE_EDITOR;
    vm.DICTIONARY_EDITOR = roles.DICTIONARY_EDITOR;
    vm.ADMINISTRATION_AUTHORITIES = [vm.USER_EDITOR, vm.ROLE_EDITOR, vm.TEMPLATE_EDITOR, vm.DICTIONARY_EDITOR]
        .join(',');
    vm.$state = $state;

    vm.getTreeItemById = getTreeItemById;
    vm.toggleAdministration = toggleAdministration;
    vm.toggleProjects = toggleProjects;
    vm.toggleMyProjects = toggleMyProjects;
    vm.onSelectNode = onSelectNode;

    init();

    function init() {
        vm.allProjectIsCollapsed = sidebarCache.get('allProjectIsCollapsed');
        vm.bookmarksIsCollapsed = sidebarCache.get('bookmarksIsCollapsed');
        vm.adminToggled = sidebarCache.get('adminToggled');
        vm.selectedFullId = getFullIdFromParams($stateParams);

        bindEvents();
    }

    function onSelectNode(fullId) {
        if (vm.selectedFullId !== fullId) {
            vm.selectedFullId = fullId;
        }
    }

    function getTreeItemById(items, itemId) {
        return _.find(items, function(projectItem) {
            return projectItem.id === itemId;
        });
    }

    function toggleAdministration() {
        vm.adminToggled = !vm.adminToggled;
        sidebarCache.put('adminToggled', vm.adminToggled);
    }

    function toggleProjects() {
        vm.allProjectIsCollapsed = !vm.allProjectIsCollapsed;
        sidebarCache.put('allProjectIsCollapsed', vm.allProjectIsCollapsed);
    }

    function toggleMyProjects() {
        vm.bookmarksIsCollapsed = !vm.bookmarksIsCollapsed;
        sidebarCache.put('bookmarksIsCollapsed', vm.bookmarksIsCollapsed);
    }

    function bindEvents() {
        $scope.$on('$stateChangeSuccess', function(event, toState, toParams) {
            vm.onSelectNode(getFullIdFromParams(toParams));
        });
    }

    function getFullIdFromParams(toParams) {
        return _.compact([toParams.projectId, toParams.notebookId, toParams.experimentId])
            .join('-')
            .toString();
    }
}

module.exports = sidebar;
