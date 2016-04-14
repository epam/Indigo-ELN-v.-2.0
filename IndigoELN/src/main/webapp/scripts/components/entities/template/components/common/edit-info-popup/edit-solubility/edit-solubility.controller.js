'use strict';

angular.module('indigoeln').controller('EditSolubilityController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.solubility = data;

        $scope.solubilityTypeSelect = [
            {name: 'Quantitative'},
            {name: 'Qualitative'}];

        $scope.qualitativeSolubilitySelect = [
            {name: 'Soluble'},
            {name: 'Unsoluble'},
            {name: 'Precipitate'}];

        $scope.unitSelect = [
            {name: 'g/ml'}];

        $scope.operatorSelect = [
            {name: '>'},
            {name: '<'},
            {name: '='},
            {name: '~'}];

        $scope.addSolvent = function () {
            $scope.solubility.push({solventName: {}, type: {}, value: {}, comment: ''});
        };

        $scope.remove = function (solvent) {
            $scope.solubility = _.without($scope.solubility, solvent);
        };

        $scope.removeAll = function () {
            $scope.solubility = [];
        };

        $scope.save = function () {
            $uibModalInstance.close($scope.solubility);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
