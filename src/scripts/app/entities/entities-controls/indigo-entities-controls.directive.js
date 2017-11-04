/* @ngInject */
function indigoEntitiesControls() {
    return {
        restrict: 'E',
        template: require('./entities-controls.html'),
        controller: indigoEntitiesControlsController,
        bindToController: true,
        controllerAs: 'vm',
        scope: {
            onCloseTab: '&',
            onCloseAllTabs: '&',
            onCloseNonActiveTabs: '&',
            onSave: '&'
        }
    };

    function indigoEntitiesControlsController($state, entitiesBrowser, modalHelper, projectsForSubCreation, appRoles) {
        var vm = this;

        vm.CONTENT_EDITOR = appRoles.CONTENT_EDITOR;
        vm.PROJECT_CREATOR = appRoles.PROJECT_CREATOR;
        vm.NOTEBOOK_CREATOR = appRoles.NOTEBOOK_CREATOR;
        vm.EXPERIMENT_CREATOR = appRoles.EXPERIMENT_CREATOR;
        vm.GLOBAL_SEARCH = appRoles.GLOBAL_SEARCH;
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
        vm.onCloseTabClick = onCloseTabClick;
        vm.createExperiment = createExperiment;
        vm.createNotebook = createNotebook;

        function init() {
            entitiesBrowser.getTabs(function(tabs) {
                vm.entities = tabs;
            });
        }

        function onTabClick(tab) {
            entitiesBrowser.goToTab(tab);
        }

        function openSearch() {
            $state.go('entities.search-panel');
        }

        function canSave() {
            return !!entitiesBrowser.saveCurrentEntity && !!entitiesBrowser.getCurrentForm() && entitiesBrowser.getCurrentForm().$dirty;
        }

        function save() {
            vm.onSave();
        }

        function canPrint() {
            var actions = entitiesBrowser.getEntityActions();

            return actions && actions.print;
        }

        function print() {
            entitiesBrowser.getEntityActions().print();
        }

        function canDuplicate() {
            var actions = entitiesBrowser.getEntityActions();

            return actions && actions.duplicate;
        }

        function duplicate() {
            entitiesBrowser.getEntityActions().duplicate();
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
                    return projectsForSubCreation.query().$promise;
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

module.exports = indigoEntitiesControls;
