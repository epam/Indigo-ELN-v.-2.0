(function () {
    angular
        .module('indigoeln')
        .controller('TemplateDeleteController', TemplateDeleteController);

    /* @ngInject */
    function TemplateDeleteController($uibModalInstance, $stateParams, Template) {
        var self = this;

        self.clear          = clear;
        self.confirmDelete  = confirmDelete;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete() {
            Template.delete({id: $stateParams.id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
