(function () {
    angular
        .module('indigoeln')
        .controller('DictionaryManagementDeleteController', DictionaryManagementDeleteController);

    /* @ngInject */
    function DictionaryManagementDeleteController($uibModalInstance, Dictionary, entity) {
        var self = this;

        self.dismiss = dismiss;
        self.confirmDelete = confirmDelete;

        function dismiss() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete() {
            Dictionary.delete({id: entity.id},
                function () {
                    $uibModalInstance.close(true);
                },
                function () {
                    $uibModalInstance.close(false);
                });
        }
    }
})();