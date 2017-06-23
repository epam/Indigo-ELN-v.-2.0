(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentCompleteModalController', ExperimentCompleteModalController);

    /* @ngInject */
    function ExperimentCompleteModalController($uibModalInstance, fullExperimentName) {
        var vm = this;

        vm.fullExperimentName = fullExperimentName;

        vm.dismiss = dismiss;
        vm.confirmCompletion = confirmCompletion;

        function dismiss() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmCompletion() {
            $uibModalInstance.close(true);
        }
    }
})();