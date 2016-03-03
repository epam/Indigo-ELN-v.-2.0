'use strict';

angular.module('indigoeln')
    .controller('ProductBatchSummarySetSourceController', function ($scope, name, sourceValues, sourceDetailExternal,
                                                                    sourceDetailInternal, $uibModalInstance) {
        $scope.name = name;
        $scope.sourceValues = sourceValues;
        $scope.source = sourceValues[0];
        $scope.sourceDetailExternal = sourceDetailExternal;
        $scope.sourceDetailInternal = sourceDetailInternal;
        $scope.save = function () {
            $uibModalInstance.close({source: $scope.source, sourceDetail: $scope.sourceDetail});
        };
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
