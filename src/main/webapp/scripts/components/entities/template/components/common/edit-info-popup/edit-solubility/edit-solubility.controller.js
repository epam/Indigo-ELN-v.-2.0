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
                var solvent = solubility.solventName && solubility.solventName.name ? solubility.solventName.name : null;
                var type = solubility.type && solubility.type.name ? solubility.type.name : null;
                var value = solubility.value && solubility.value.value ? solubility.value : null;
                if (!type || !value || !solvent) {
                    return '';
                }
                var result = '';
                if (type === 'Quantitative') {
                    result = value.operator.name + ' ' + value.value + ' ' + value.unit.name; // > 5 g/mL
                } else {
                    result = value.value.name; // Unsoluble
                }
                return result + ' in ' + solvent; // in Acetic acid
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
