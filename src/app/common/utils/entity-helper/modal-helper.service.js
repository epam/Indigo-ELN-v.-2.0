var newExperimentModal = require('../../../experiment/create-new-experiment-modal/create-new-experiment-modal.html');
var newNotebookModal = require('../../../notebook/select-parent/notebook-select-parent.html');

/* @ngInject */
function modalHelper($uibModal) {
    var dlg;

    return {
        openCreateNewExperimentModal: openCreateNewExperimentModal,
        openCreateNewNotebookModal: openCreateNewNotebookModal,
        close: close
    };

    function openCreateNewExperimentModal(resolve) {
        close();
        dlg = $uibModal.open({
            animation: true,
            template: newExperimentModal,
            controller: 'CreateNewExperimentModalController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        });

        return dlg.result;
    }

    function openCreateNewNotebookModal(resolve) {
        close();
        dlg = $uibModal.open({
            animation: true,
            template: newNotebookModal,
            controller: 'NotebookSelectParentController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        });

        return dlg.result;
    }

    function close() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = modalHelper;
