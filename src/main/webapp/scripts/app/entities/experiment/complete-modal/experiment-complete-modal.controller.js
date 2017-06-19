(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentCompleteModalController', ExperimentCompleteModalController);

    /* @ngInject */
    function ExperimentCompleteModalController($uibModalInstance, fullExperimentName) {
        var self = this;

        self.fullExperimentName = fullExperimentName;

        self.dismiss = dismiss;
        self.confirmCompletion = confirmCompletion;

        function dismiss() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmCompletion() {
            $uibModalInstance.close(true);
        }
    }
})();