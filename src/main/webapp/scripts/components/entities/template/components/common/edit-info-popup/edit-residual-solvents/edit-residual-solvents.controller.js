'use strict';

angular.module('indigoeln').controller('EditResidualSolventsController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.solvents = data;

        $scope.addSolvent = function () {
            $scope.solvents.push({name: '', eq: '', comment: ''});
        };

        $scope.remove = function (solvent) {
            $scope.solvents = _.without($scope.solvents, solvent);
        };

        $scope.removeAll = function () {
            $scope.solvents = [];
        };

        $scope.save = function () {
            $uibModalInstance.close($scope.solvents);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
