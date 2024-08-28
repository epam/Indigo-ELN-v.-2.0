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

var template = require('./indigo-entities-controls.html');
var roles = require('../../../../permissions/permission-roles.json');

function indigoEntitiesControls() {
    return {
        restrict: 'E',
        template: template,
        controller: IndigoEntitiesControlsController,
        bindToController: true,
        controllerAs: 'vm',
        scope: {
            isDashboard: '=',
            activeTab: '=',
            onCloseTab: '&',
            onCloseAllTabs: '&',
            onCloseNonActiveTabs: '&',
            onSave: '&'
        }
    };
}

/* @ngInject */
function IndigoEntitiesControlsController($state, entitiesBrowserService, modalHelper,
                                          projectsForSubCreationService, $scope) {
    var vm = this;

    init();

    function init() {
        vm.CONTENT_EDITOR = roles.CONTENT_EDITOR;
        vm.PROJECT_CREATOR = roles.PROJECT_CREATOR;
        vm.NOTEBOOK_CREATOR = roles.NOTEBOOK_CREATOR;
        vm.EXPERIMENT_CREATOR = roles.EXPERIMENT_CREATOR;
        vm.GLOBAL_SEARCH = roles.GLOBAL_SEARCH;
        vm.PROJECT_CREATORS = [vm.CONTENT_EDITOR, vm.PROJECT_CREATOR].join(',');
        vm.NOTEBOOK_CREATORS = [vm.CONTENT_EDITOR, vm.NOTEBOOK_CREATOR].join(',');
        vm.EXPERIMENT_CREATORS = [vm.CONTENT_EDITOR, vm.EXPERIMENT_CREATOR].join(',');
        vm.ENTITY_CREATORS = [vm.CONTENT_EDITOR, vm.PROJECT_CREATOR, vm.NOTEBOOK_CREATOR, vm.EXPERIMENT_CREATOR]
            .join(',');

        vm.onTabClick = onTabClick;
        vm.openSearch = openSearch;
        vm.canPrint = canPrint;
        vm.print = print;
        vm.canDuplicate = canDuplicate;
        vm.duplicate = duplicate;
        vm.onCloseTabClick = onCloseTabClick;
        vm.createExperiment = createExperiment;
        vm.createNotebook = createNotebook;

        entitiesBrowserService.getTabs(function(tabs) {
            vm.entities = tabs;
            vm.entitiesCount = _.size(vm.entities);
        });

        initEvents();
    }

    function onTabClick(tab) {
        entitiesBrowserService.goToTab(tab);
    }

    function openSearch() {
        $state.go('entities.search-panel');
    }

    function canPrint() {
        var actions = entitiesBrowserService.getEntityActions();

        return actions && actions.print;
    }

    function print() {
        entitiesBrowserService.getEntityActions().print();
    }

    function canDuplicate() {
        var actions = entitiesBrowserService.getEntityActions();

        return actions && actions.duplicate;
    }

    function duplicate() {
        entitiesBrowserService.getEntityActions().duplicate();
    }

    function onCloseTabClick($event, tab) {
        vm.onCloseTab({
            $event: $event, tab: tab
        });
    }

    function createExperiment() {
        var resolve = {
            fullNotebookId: function() {
                return null;
            }
        };

        modalHelper.openCreateNewExperimentModal(resolve).then(function(result) {
            $state.go('entities.experiment-detail', {
                notebookId: result.notebookId,
                projectId: result.projectId,
                experimentId: result.id
            });
        });
    }

    function createNotebook() {
        var resolve = {
            parents: function() {
                return projectsForSubCreationService.query().$promise;
            }
        };
        modalHelper.openCreateNewNotebookModal(resolve).then(function(projectId) {
            $state.go('entities.notebook-new', {
                parentId: projectId
            });
        });
    }

    function initEvents() {
        $scope.$on('$destroy', function() {
            modalHelper.close();
        });
    }
}

module.exports = indigoEntitiesControls;
