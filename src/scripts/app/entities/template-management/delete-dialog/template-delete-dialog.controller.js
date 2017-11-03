(function() {
    angular
        .module('indigoeln')
        .controller('TemplateDeleteController', TemplateDeleteController);

    /* @ngInject */
    function TemplateDeleteController($uibModalInstance, $stateParams, templateService) {
        var vm = this;

        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete() {
            templateService.delete({
                id: $stateParams.id
            },
                function() {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
