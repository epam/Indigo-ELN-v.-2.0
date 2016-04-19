'use strict';

angular.module('indigoeln').controller('EditPurityController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.externalSupplier = data || {};

        $scope.externalSupplierCodeAndNameSelect = [
            {name: 'SPP1 - Supplier 1'},
            {name: 'SPP2 - Supplier 2'},
            {name: 'SPP3 - Supplier 3'}];

        var resultToString = function () {
            if ($scope.externalSupplier.codeAndName && $scope.externalSupplier.catalogRegistryNumber) {
                return '<' + $scope.externalSupplier.codeAndName.name + '> ' +
                    $scope.externalSupplier.catalogRegistryNumber;
            }
        };

        $scope.save = function () {
            $scope.externalSupplier.asString = resultToString();
            $uibModalInstance.close($scope.externalSupplier);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
