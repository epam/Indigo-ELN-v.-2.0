(function () {
    angular
        .module('indigoeln')
        .controller('DictionaryManagementDeleteWordController', DictionaryManagementDeleteWordController);

    /* @ngInject */
    function DictionaryManagementDeleteWordController($uibModalInstance) {
        var self = this;

        self.dismiss = dismiss;
        self.confirmDeleteWord = confirmDeleteWord;

        function dismiss() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDeleteWord() {
            $uibModalInstance.close(true);
        }
    }
})();