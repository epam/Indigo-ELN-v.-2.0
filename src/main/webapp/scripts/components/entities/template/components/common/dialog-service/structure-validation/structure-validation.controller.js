angular.module('indigoeln').controller('StructureValidationController',
    function ($scope, $uibModalInstance, batches, searchQuery, AppValues) {
        $scope.batches = batches;
        $scope.searchQuery = searchQuery;
        $scope.selectedBatch = null;
        $scope.defaultSaltCodeName = AppValues.getDefaultSaltCode().name;

        $scope.selectBatch = function (batch) {
            if ($scope.selectedBatch) { 
                $scope.selectedBatch.$$isSelected = false;
            }
            batch.$$isSelected  = true;
            $scope.selectedBatch = batch;
        };

        $scope.save = function () {
            $uibModalInstance.close($scope.selectedBatch);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

