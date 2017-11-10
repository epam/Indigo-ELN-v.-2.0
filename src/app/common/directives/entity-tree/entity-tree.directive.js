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
function EntityTreeController(entityTreeService, $timeout, experimentService, $scope, scrollService, $q) {
    var vm = this;

    init();

    function init() {
        vm.experimentsCollection = {};
        vm.toggle = toggle;
        vm.getSref = getSref;
        vm.getPopoverExperiment = _.throttle(getPopoverExperiment, 300);

        onChangedSelectedFullId();

        bindEvents();
    }

    function bindEvents() {
        $scope.$watch('vm.selectedFullId', onChangedSelectedFullId);
    }

    function onChangedSelectedFullId() {
        if (vm.selectedFullId) {
            checkParents(vm.selectedFullId).then(function() {
                $timeout(function() {
                    scrollService.scrollTo('#' + vm.elementId + '_' + vm.selectedFullId, {
                        container: vm.$element,
                        offset: 200
                    });
                });
            });
        } else {
            loadProject();
        }
    }

    function checkParents(fullId) {
        var path = _.split(fullId, '-');

        return loadProject(path)
            .then(function(project) {
                if (path.length > 1) {
                    project.isCollapsed = false;

                    return loadNotebook(path, project)
                        .then(function(notebook) {
                            if (path.length > 2) {
                                notebook.isCollapsed = false;

                                return loadExperiment(path, notebook);
                            }

                            return notebook;
                        });
                }

                return project;
            });
    }

    function updateTree(projects) {
        if (vm.tree !== projects) {
            vm.tree = projects;
        }
    }

    function loadProject(path) {
        return entityTreeService.getProjects(_.first(path), vm.isAll).then(function(projects) {
            updateTree(projects);

            if (path) {
                return findNodeById(projects, path[0]) || $q.reject();
            }

            return undefined;
        });
    }

    function loadNotebook(path, project) {
        return entityTreeService.getNotebooks(path[0], path[1], vm.isAll).then(function(notebooks) {
            project.children = notebooks;

            return findNodeById(notebooks, path[1]) || $q.reject();
        });
    }

    function loadExperiment(path, notebook) {
        return entityTreeService.getExperiments(path[0], path[1], path[3], vm.isAll).then(function(experiments) {
            notebook.children = experiments;

            return findNodeById(experiments, path[2]) || $q.reject();
        });
    }

    function findNodeById(nodes, id) {
        return _.find(nodes, function(node) {
            return node.original.id === id;
        });
    }

    function getPopoverExperiment(node) {
        if (vm.popoverExperiment === node) {
            return;
        }

        vm.popoverExperiment = node;

        if (!vm.experimentsCollection[node.original.fullId]) {
            experimentService.get(getParams(node.params)).$promise.then(function(experiment) {
                vm.experimentsCollection[node.original.fullId] = experiment;
            });
        }
    }

    function getSref(node) {
        if (isProject(node)) {
            return 'entities.project-detail(' + angular.toJson(getParams(node.params)) + ')';
        } else if (isNotebook(node)) {
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

    function isExperiment(node) {
        return node.params.length === 3;
    }

    function toggle(node) {
        vm.onSelectNode({node: node});

        if (isProject(node)) {
            node.children = entityTreeService.getNotebooks(node.id, null, vm.isAll).then(function(children) {
                $timeout(function() {
                    node.children = children;
                });
            });
        } else if (isNotebook(node)) {
            entityTreeService.getExperiments(node.params[0], node.params[1], vm.isAll).then(function(children) {
                $timeout(function() {
                    node.children = children;
                });
            });
        } else if (isExperiment(node)) {
            return;
        }

        node.isCollapsed = !node.isCollapsed;
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
