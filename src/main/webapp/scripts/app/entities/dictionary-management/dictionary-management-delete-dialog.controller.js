angular.module('indigoeln')
    .controller('DictionaryManagementDeleteController', function ($scope, $uibModalInstance, Dictionary, entity) {
        var vm = this;

        vm.confirmationTitle = 'Confirm delete operation';
        vm.confirmationQuestion = 'Are you sure you want to delete this Dictionary with its associated Words?';
        vm.confirmationComment = '<span class="semi-b">NOTE:</span> this dictionary can be in use. Setting words to be inactive instead of removing dictionary is more reliable way.';
        vm.cancelButtonText = 'Cancel';
        vm.deleteButtonText = 'Delete';

        $scope.dismiss = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.confirmDelete = function () {
            Dictionary.delete({id: entity.id},
                function () {
                    $uibModalInstance.close(true);
                },
                function () {
                    $uibModalInstance.close(false);
                });
        };
    });