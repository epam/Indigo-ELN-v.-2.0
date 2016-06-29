angular.module('indigoeln')
    .controller('SetUnitValueController', function ($scope, name, unitNames, $uibModalInstance) {
        $scope.name = name;
        $scope.initUnit = function () {
            $scope.unit = unitNames[0];
        };
        $scope.setUnit = function (unit) {
            $scope.unit = unit;
        };
        $scope.unitNames = unitNames;
        $scope.$watch('unit', function (val) {
            if (val) {
                $scope.value = null;
            }
        });
        $scope.save = function () {
            $uibModalInstance.close({value: $scope.value, unit: $scope.unit});
        };

        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.unitParsers = [function (viewValue) {
            return $u(viewValue, $scope.unit).val();
        }];
        $scope.unitFormatters = [function (modelValue) {
            return $u(modelValue).as($scope.unit).val();
        }];

    });
