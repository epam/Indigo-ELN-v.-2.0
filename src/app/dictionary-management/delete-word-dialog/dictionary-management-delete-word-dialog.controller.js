/* @ngInject */
function DictionaryManagementDeleteWordDialogController($uibModalInstance) {
    var vm = this;

    vm.dismiss = dismiss;
    vm.confirmDeleteWord = confirmDeleteWord;

    function dismiss() {
        $uibModalInstance.dismiss('cancel');
    }

    function confirmDeleteWord() {
        $uibModalInstance.close(true);
    }
}

module.exports = DictionaryManagementDeleteWordDialogController;
