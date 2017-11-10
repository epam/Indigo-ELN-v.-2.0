/* @ngInject */
function ExperimentSelectSignatureTemplateModalController($uibModalInstance, result) {
    var vm = this;

    vm.items = result.Templates;

    vm.dismiss = dismiss;
    vm.selectSignatureTemplateModal = selectSignatureTemplateModal;

    function dismiss() {
        $uibModalInstance.dismiss('cancel');
    }

    function selectSignatureTemplateModal() {
        $uibModalInstance.close(vm.selectedTemplate);
    }
}

module.exports = ExperimentSelectSignatureTemplateModalController;
