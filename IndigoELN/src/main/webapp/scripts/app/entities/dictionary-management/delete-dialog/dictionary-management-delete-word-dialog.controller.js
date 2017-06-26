(function() {
    angular
        .module('indigoeln')
        .controller('DictionaryManagementDeleteWordController', DictionaryManagementDeleteWordController);

    /* @ngInject */
    function DictionaryManagementDeleteWordController($uibModalInstance) {
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
})();
