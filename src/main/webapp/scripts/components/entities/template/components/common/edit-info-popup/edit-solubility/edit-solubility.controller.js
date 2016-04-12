'use strict';

angular.module('indigoeln').controller('EditSolubilityController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.model = {};

        $scope.cancel = function () {
            $uibModalInstance.close({});
        };
    });
