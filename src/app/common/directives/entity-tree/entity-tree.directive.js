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
function EntityTreeController(entityTreeService, $timeout, experimentService, $scope, scrollToService) {
    var vm = this;

    init();

    function init() {
        vm.experimentsCollection = {};
        vm.toggle = toggle;
        vm.getSref = getSref;
        vm.getPopoverExperiment = _.throttle(getPopoverExperiment, 300);

        bindEvents();

        loadProjects().then(function() {
            onChangedSelectedFullId();
        });
    }

    function bindEvents() {
        var selectedFullIdListener = $scope.$watch('vm.selectedFullId', onChangedSelectedFullId);

        $scope.$on('$destroy', function() {
            selectedFullIdListener();
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
        if (vm.popoverExperiment === node) {
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
