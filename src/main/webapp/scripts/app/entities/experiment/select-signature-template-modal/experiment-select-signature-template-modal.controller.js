(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentSelectSignatureTemplateModalController', ExperimentSelectSignatureTemplateModalController);

    /* @ngInject */
    function ExperimentSelectSignatureTemplateModalController($uibModalInstance, result) {
        var self = this;

        self.items = result.Templates;

        self.dismiss                        = dismiss;
        self.selectSignatureTemplateModal   = selectSignatureTemplateModal;

        function dismiss() {
            $uibModalInstance.dismiss('cancel');
        }

        function selectSignatureTemplateModal() {
            $uibModalInstance.close(self.selectedTemplate);
        }
    }
})();