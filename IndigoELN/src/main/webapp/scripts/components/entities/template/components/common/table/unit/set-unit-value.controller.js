angular.module('indigoeln')
    .controller('SetUnitValueController', function($scope, name, unitNames, $uibModalInstance) {
        $scope.name = name;
        $scope.initUnit = function() {
            $scope.unit = unitNames[0];
        };
        $scope.setUnit = function(unit) {
            $scope.unit = unit;
        };
        $scope.unitNames = unitNames;
        $scope.save = function() {
            $uibModalInstance.close({
                value: $u($scope.value, $scope.unit).val(), unit: $scope.unit
            });
        };

        $scope.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
    });
