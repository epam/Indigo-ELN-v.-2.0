angular.module('indigoeln')
    .controller('structureImportModalController', structureImportModalController);

/* @ngInject */
function structureImportModalController($scope, $uibModalInstance) {
    var vm = this;
    vm.import = function() {
        $uibModalInstance.close($scope.content);
    };

    vm.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };
}