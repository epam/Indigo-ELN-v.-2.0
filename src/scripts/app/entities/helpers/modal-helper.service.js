var newExperimentModal = require('../experiment/create-new-experiment-modal/create-new-experiment-modal.html');
var newNotebookModal = require('../notebook/notebook-select-parent.html');

/* @ngInject */
function modalHelper($uibModal) {
    return {
        openCreateNewExperimentModal: openCreateNewExperimentModal,
        openCreateNewNotebookModal: openCreateNewNotebookModal
    };

    function openCreateNewExperimentModal(resolve) {
        return $uibModal.open({
            animation: true,
            template: newExperimentModal,
            controller: 'CreateNewExperimentModalController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        }).result;
    }

    function openCreateNewNotebookModal(resolve) {
        return $uibModal.open({
            animation: true,
            template: newNotebookModal,
            controller: 'NotebookSelectParentController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        }).result;
    }
}

module.exports = modalHelper;
