(function() {
    angular
        .module('indigoeln')
        .directive('indigoEntitiesControls', indigoEntitiesControls);

    function indigoEntitiesControls() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/entities/entities-controls.html',
            controller: controller,
            controllerAs: 'vm',
            scope: {
                onCloseTab: '&',
                onCloseAllTabs: '&'
            }
        };

        /* @ngInject */
        function controller($scope, $state, EntitiesBrowser, modalHelper, ProjectsForSubCreation) {
            var vm = this;

            // TODO: move it ti file
            vm.CONTENT_EDITOR = 'CONTENT_EDITOR';
            vm.PROJECT_CREATOR = 'PROJECT_CREATOR';
            vm.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
            vm.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
            vm.GLOBAL_SEARCH = 'GLOBAL_SEARCH';
            vm.PROJECT_CREATORS = [vm.CONTENT_EDITOR, vm.PROJECT_CREATOR].join(',');
            vm.NOTEBOOK_CREATORS = [vm.CONTENT_EDITOR, vm.NOTEBOOK_CREATOR].join(',');
            vm.EXPERIMENT_CREATORS = [vm.CONTENT_EDITOR, vm.EXPERIMENT_CREATOR].join(',');
            vm.ENTITY_CREATORS = [vm.CONTENT_EDITOR, vm.PROJECT_CREATOR, vm.NOTEBOOK_CREATOR, vm.EXPERIMENT_CREATOR].join(',');
            vm.isDashboard = false;

            init();

            vm.onTabClick = onTabClick;
            vm.openSearch = openSearch;
            vm.canSave = canSave;
            vm.save = save;
            vm.canPrint = canPrint;
            vm.print = print;
            vm.canDuplicate = canDuplicate;
            vm.duplicate = duplicate;
            vm.onCloseAllTabs = onCloseAllTabs;
            vm.onCloseTabClick = onCloseTabClick;
            vm.createExperiment = createExperiment;
            vm.createNotebook = createNotebook;

            function init() {
                EntitiesBrowser.getTabs(function(tabs) {
                    vm.entities = tabs;
                });
            }

            function onTabClick(tab) {
                EntitiesBrowser.goToTab(tab);
            }

            function openSearch() {
                $state.go('entities.search-panel');
            }

            function canSave() {
                return !!EntitiesBrowser.saveCurrentEntity && !!EntitiesBrowser.getCurrentForm() && EntitiesBrowser.getCurrentForm().$dirty;
            }

            function save() {
                EntitiesBrowser.saveCurrentEntity();
            }

            function canPrint() {
                var actions = EntitiesBrowser.getEntityActions();

                return actions && actions.print;
            }

            function print() {
                EntitiesBrowser.getEntityActions().print();
            }

            function canDuplicate() {
                var actions = EntitiesBrowser.getEntityActions();

                return actions && actions.duplicate;
            }

            function duplicate() {
                EntitiesBrowser.getEntityActions().duplicate();
            }

            function onCloseAllTabs(exceptCurrent) {
                if ($scope.onCloseAllTabs) {
                    $scope.onCloseAllTabs({
                        exceptCurrent: exceptCurrent
                    });
                }
            }

            function onCloseTabClick($event, tab) {
                if ($scope.onCloseTab) {
                    $scope.onCloseTab({
                        $event: $event, tab: tab
                    });
                }
            }

            function createExperiment() {
                var resolve = {
                    notebookId: function() {
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
                        return ProjectsForSubCreation.query().$promise;
                    }
                };
                modalHelper.openCreateNewNotebookModal(resolve).then(function(projectId) {
                    $state.go('entities.notebook-new', {
                        parentId: projectId
                    });
                });
            }
        }
    }
})();
