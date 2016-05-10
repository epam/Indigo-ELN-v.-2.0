'use strict';

angular.module('indigoeln').controller('EditResidualSolventsController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.solvents = data || {};
        $scope.solvents.data = $scope.solvents.data || [];

        $scope.addSolvent = function () {
            $scope.solvents.data.push({name: '', eq: '', comment: ''});
        };

        $scope.remove = function (solvent) {
            $scope.solvents.data = _.without($scope.solvents.data, solvent);
        };

        $scope.removeAll = function () {
            $scope.solvents.data = [];
        };

        var resultToString = function () {
            var solventStrings = _.map($scope.solvents.data, function(solvent) {
                if (solvent.name && solvent.eq) {
                    return solvent.eq + ' mols of ' + solvent.name.name;
                } else {
                    return '';
                }
            });
            return _.compact(solventStrings).join(', ');
        };

        $scope.save = function () {
            $scope.solvents.asString = resultToString();
            $uibModalInstance.close($scope.solvents);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
