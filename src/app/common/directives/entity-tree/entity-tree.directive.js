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
