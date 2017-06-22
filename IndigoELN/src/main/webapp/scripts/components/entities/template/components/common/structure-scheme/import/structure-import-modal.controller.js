angular.module('indigoeln')
    .controller('structureImportModalController', function ($scope, $uibModalInstance) {
        $scope.import = function() {
            $uibModalInstance.close($scope.content);
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };
});