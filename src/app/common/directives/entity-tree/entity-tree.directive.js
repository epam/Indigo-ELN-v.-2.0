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
function EntityTreeController(entityTreeService, $timeout, experimentService, $scope, scrollToService, $element,
                              notebookService, projectService) {
    var vm = this;

    init();

    function init() {
        vm.experimentsCollection = {};
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
            if (data.entity.experimentId) {
                var expId = data.entity.projectId + '-' + data.entity.notebookId + '-' + data.entity.experimentId;
                var expNode = entityTreeService.getExperimentByFullId(expId);
                var expVersion = _.get(expNode, 'version', -1);
                if (expVersion < data.version) {
                    experimentService.get(data.entity).$promise.then(function(exp) {
                        vm.experimentsCollection[expId] = exp;
                        entityTreeService.updateExperiment(exp);
                    });
                }

                return;
            }
            if (data.entity.notebookId) {
                var fullId = data.entity.projectId + '-' + data.entity.notebookId;
                var notebookNode = entityTreeService.getNotebookByFullId(fullId);
                var notebookVersion = _.get(notebookNode, 'version', -1);
                if (notebookVersion < data.version) {
                    notebookService.get(data.entity).$promise.then(function(notebook) {
                        entityTreeService.updateNotebook(notebook);
                    });
                }

                return;
            }

            var projNode = entityTreeService.getProjectById(data.entity.projectId);
            var projVersion = _.get(projNode, 'version', -1);
            if (projVersion < data.version) {
                projectService.get(data.entity).$promise.then(function(project) {
                    entityTreeService.updateProject(project);
                });
            }
        });

        $element.bind('mouseout', hidePopover);

        $scope.$on('$destroy', function() {
            selectedFullIdListener();
            entityUpdate();

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

    function onChangedSelectedFullId() {
        if (!vm.selectedFullId) {
            return;
        }

        var path = _.split(vm.selectedFullId, '-');
        var project = findNodeById(vm.tree, path[0]);

        if (!project) {
            return;
        }
        project.isCollapsed = false;
        if (!path[1]) {
            scrollToSelectedNode();
        }

        entityTreeService.getNotebooks(path[0], vm.isAll)
            .then(function(notebooks) {
                var notebook = findNodeById(notebooks, path[1]);

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

    function findNodeById(nodes, id) {
        return _.find(nodes, function(node) {
            return node.original.id === id;
        });
    }

    function getPopoverExperiment(node) {
        if (vm.popoverExperiment && vm.popoverExperiment.fullId === node.fullId) {
            return;
        }

        vm.popoverExperiment = node;

        var nodeVersion = _.get(node, 'original.version', null);
        var cache = vm.experimentsCollection[node.original.fullId];
        var cachedVersion = _.get(cache, 'version', null);

        if (!cache || nodeVersion !== cachedVersion) {
            if (cache) {
                delete vm.experimentsCollection[node.original.fullId];
            }
            experimentService.get(getParams(node.params)).$promise.then(function(experiment) {
                vm.experimentsCollection[node.original.fullId] = experiment;
                entityTreeService.updateExperiment(experiment);
            });
        }
    }

    function getSref(node) {
        if (isProject(node)) {
            return 'entities.project-detail(' + angular.toJson(getParams(node.params)) + ')';
        }

        if (isNotebook(node)) {
            return 'entities.notebook-detail(' + angular.toJson(getParams(node.params)) + ')';
        }

        return 'entities.experiment-detail(' + angular.toJson(getParams(node.params)) + ')';
    }

    function isProject(node) {
        return node.params.length === 1;
    }

    function isNotebook(node) {
        return node.params.length === 2;
    }

    function toggle(node) {
        node.isCollapsed = !node.isCollapsed;
        vm.onSelectNode({node: node});

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
