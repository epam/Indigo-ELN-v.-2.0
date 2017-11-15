/* @ngInject */
function DictionaryManagementDeleteDialogController($uibModalInstance, dictionaryService, entity) {
    var vm = this;

    vm.dismiss = dismiss;
    vm.confirmDelete = confirmDelete;

    function dismiss() {
        $uibModalInstance.dismiss('cancel');
    }

    function confirmDelete() {
        dictionaryService.delete(
            {
                id: entity.id
            },
            function() {
                $uibModalInstance.close(true);
            },
            function() {
                $uibModalInstance.close(false);
            }
        );
    }
}

module.exports = DictionaryManagementDeleteDialogController;
