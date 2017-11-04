/* @ngInject */
function modalHelper($uibModal) {
    return {
        openCreateNewExperimentModal: openCreateNewExperimentModal,
        openCreateNewNotebookModal: openCreateNewNotebookModal
    };

    function openCreateNewExperimentModal(resolve) {
        return $uibModal.open({
            animation: true,
            templateUrl: 'scripts/app/entities/experiment/create-new-experiment-modal/create-new-experiment-modal.html',
            controller: 'CreateNewExperimentModalController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        }).result;
    }

    function openCreateNewNotebookModal(resolve) {
        return $uibModal.open({
            animation: true,
            templateUrl: 'scripts/app/entities/notebook/notebook-select-parent.html',
            controller: 'NotebookSelectParentController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        }).result;
    }
}

module.exports = modalHelper;
