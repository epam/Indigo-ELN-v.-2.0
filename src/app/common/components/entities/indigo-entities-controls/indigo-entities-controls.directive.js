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
            activeTab: '=',
            onCloseTab: '&',
            onCloseAllTabs: '&',
            onCloseNonActiveTabs: '&',
            onSave: '&'
        }
    };
}

IndigoEntitiesControlsController.$inject = ['$state', 'entitiesBrowserService',
    'modalHelper', 'projectsForSubCreationService'];

function IndigoEntitiesControlsController($state, entitiesBrowserService, modalHelper,
                                          projectsForSubCreationService) {
    var vm = this;

    $onInit();

    function $onInit() {
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
        vm.isDashboard = false;

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
        });
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
}

module.exports = indigoEntitiesControls;
