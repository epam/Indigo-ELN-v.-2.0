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

var template = require('./entity-tree.html');

function entityTree() {
    return {
        scope: {
            selectedFullId: '=',
            isAll: '=',
            onSelectNode: '&'
        },
        controller: EntityTreeController,
        controllerAs: 'vm',
        bindToController: true,
        template: template,
        link: {
            pre: function($scope, $element, $attr, vm) {
                vm.elementId = 'entity-tree-' + $scope.$id;
                $element.attr('id', vm.elementId);
                vm.$element = $element;
            }
        }
    };
}

/* @ngInject */
function EntityTreeController(entityTreeService, $timeout, $scope, scrollToService, $element) {
    var vm = this;

    init();

    function init() {
        vm.toggle = toggle;
        vm.getSref = getSref;
        vm.getPopoverExperiment = _.throttle(getPopoverExperiment, 300);
        vm.hidePopover = hidePopover;

        bindEvents();

        loadProjects().then(function() {
            onChangedSelectedFullId();
        });
    }

    function hidePopover() {
        vm.popoverExperiment = null;
    }

    function bindEvents() {
        var selectedFullIdListener = $scope.$watch('vm.selectedFullId', onChangedSelectedFullId);

        var entityUpdate = $scope.$on('entity-updated', function(event, data) {
            onUpdateEntity(data.entity, data.version);
        });

        var subEntityChanged = $scope.$on('sub_entity_changes', function(event, data) {
            onSubEntityChanged(data.entity);
        });

        $element.bind('mouseout', hidePopover);

        $scope.$on('$destroy', function() {
            selectedFullIdListener();
            entityUpdate();
            subEntityChanged();

            $element.unbind('mouseout', hidePopover);
        });
    }

    function scrollToSelectedNode() {
        if (!vm.selectedFullId) {
            return;
        }

        $timeout(function() {
            scrollToService.scrollTo('#' + vm.elementId + '_' + vm.selectedFullId, {
                container: vm.$element,
                offset: 200
            });
        });
    }

    function onSubEntityChanged(entityParams) {
        if (!entityParams) {
            entityTreeService.refreshProjects(vm.isAll);

            return;
        }
        if (entityParams.notebookId) {
            entityTreeService.refreshExperiments(entityParams.projectId, entityParams.notebookId, vm.isAll);

            return;
        }
        entityTreeService.refreshNotebooks(entityParams.projectId, vm.isAll);
    }

    function onUpdateEntity(entityParams, version) {
        if (entityParams.experimentId) {
            var expId = entityParams.projectId + '-' + entityParams.notebookId + '-' + entityParams.experimentId;
            entityTreeService.updateExperiment(expId, version);

            return;
        }
        if (entityParams.notebookId) {
            var fullId = entityParams.projectId + '-' + entityParams.notebookId;
            entityTreeService.updateNotebook(fullId, version);

            return;
        }

        entityTreeService.updateProject(entityParams.projectId, version);
    }

    function onChangedSelectedFullId() {
        if (!vm.selectedFullId) {
            return;
        }

        var path = _.split(vm.selectedFullId, '-');
        var project = _.find(vm.tree, {fullId: path[0]});

        if (!project) {
            return;
        }
        project.isCollapsed = false;
        if (!path[1]) {
            scrollToSelectedNode();
        }

        entityTreeService.getNotebooks(path[0], vm.isAll)
            .then(function(notebooks) {
                var notebook = _.find(notebooks, {id: path[1]});

                if (!notebook) {
                    return;
                }
                notebook.isCollapsed = false;
                if (!path[2]) {
                    scrollToSelectedNode();
                }

                entityTreeService.getExperiments(path[0], path[1], vm.isAll)
                    .then(scrollToSelectedNode);
            });
    }

    function loadProjects() {
        return entityTreeService.getProjects(vm.isAll).then(function(projects) {
            vm.tree = projects;

            return vm.tree;
        });
    }

    function getPopoverExperiment(node) {
        if (vm.popoverExperiment && vm.popoverExperiment.fullId === node.fullId) {
            return;
        }

        vm.popoverExperiment = node;
    }

    function getSref(node) {
        var params = angular.toJson(getParams(node.params));
        if (isProject(node)) {
            return 'entities.project-detail(' + params + ')';
        }

        if (isNotebook(node)) {
            return 'entities.notebook-detail(' + params + ')';
        }

        return 'entities.experiment-detail(' + params + ')';
    }

    function isProject(node) {
        return node.params.length === 1;
    }

    function isNotebook(node) {
        return node.params.length === 2;
    }

    function toggle(node) {
        node.isCollapsed = !node.isCollapsed;
        if (!node.isCollapsed) {
            var path = _.split(node.fullId, '-');
            if (path.length === 2) {
                entityTreeService.getExperiments(path[0], path[1], vm.isAll);
                return;
            }
            entityTreeService.getNotebooks(path[0], vm.isAll);
        }

        return true;
    }

    function getParams(params) {
        return {
            projectId: params[0],
            notebookId: params[1],
            experimentId: params[2]
        };
    }
}

module.exports = entityTree;
