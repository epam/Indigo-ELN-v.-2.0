angular.module('indigoeln').controller('EditSolubilityController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.solubility = data || {};
        $scope.solubility.data = $scope.solubility.data || [];

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
            $scope.solubility.data.push({solventName: {}, type: {}, value: {}, comment: ''});
        };

        $scope.remove = function (solvent) {
            $scope.solubility.data = _.without($scope.solubility.data, solvent);
        };

        $scope.removeAll = function () {
            $scope.solubility.data = [];
        };

        var resultToString = function () {
            var solubilityStrings = _.map($scope.solubility.data, function(solubility) {
                if (solubility.value.operator && solubility.value.value && solubility.value.unit) {
                    return solubility.value.operator.name + ' ' + solubility.value.value + ' ' +
                        solubility.value.unit.name + ' in ' + solubility.solventName.name;
                } else if (solubility.value.value && solubility.value.value.name) {
                    return solubility.value.value.name + ' in ' + solubility.solventName.name;
                } else {
                    return '';
                }
            });
            return _.compact(solubilityStrings).join(', ');
        };

        $scope.save = function () {
            $scope.solubility.asString = resultToString();
            $uibModalInstance.close($scope.solubility);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
