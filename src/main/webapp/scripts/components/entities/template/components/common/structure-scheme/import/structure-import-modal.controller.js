angular
    .module('indigoeln')
    .controller('StructureImportModalController', StructureImportModalController);

/* @ngInject */
function StructureImportModalController($scope, $uibModalInstance) {
    var vm = this;
    vm.import = function() {
        $uibModalInstance.close($scope.content);
    };

    vm.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };
}
