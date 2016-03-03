'use strict';

angular.module('indigoeln')
    .controller('ProductBatchSummarySetUnitValueController', function ($scope, name, unitNames, $uibModalInstance) {
        $scope.name = name;
        $scope.initUnit = function () {
            $scope.unit = unitNames[0];
        };
        $scope.setUnit = function (unit) {
            $scope.unit = unit;
        };
        $scope.unitNames = unitNames;
        $scope.save = function () {
            $uibModalInstance.close({value: $scope.value, unit: $scope.unit});
        };

        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

    });
