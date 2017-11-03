angular
    .module('indigoeln.componentsModule')
    .controller('SetUnitValueController', function($scope, name, unitNames, $uibModalInstance, unitsConverter) {
        var vm = this;

        init();
        function init() {
            vm.name = name;
            vm.unitNames = unitNames;
            vm.unit = unitNames[0];

            vm.save = save;
            vm.clear = clear;
        }

        function save() {
            $uibModalInstance.close({
                value: unitsConverter.convert(vm.value, vm.unit).val(), unit: vm.unit
            });
        }

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
    });
