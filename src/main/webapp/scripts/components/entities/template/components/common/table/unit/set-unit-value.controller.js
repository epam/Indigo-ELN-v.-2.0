angular
    .module('indigoeln')
    .controller('SetUnitValueController', function($scope, name, unitNames, $uibModalInstance) {
        var vm = this;

        init();

        function init() {
            vm.name = name;
            vm.unitNames = unitNames;
            vm.unit = unitNames[0];

            vm.save = save;
            vm.clear = clear;
            vm.setUnit = setUnit;
        }

        function save() {
            $uibModalInstance.close({
                value: $u($scope.value, $scope.unit).val(), unit: $scope.unit
            });
        }

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function setUnit(unit) {
            vm.unit = unit;
        }
    });
