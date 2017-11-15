SetUnitValueController.$inject = ['name', 'unitNames', '$uibModalInstance', 'unitsConverterService'];

function SetUnitValueController(name, unitNames, $uibModalInstance, unitsConverterService) {
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
            value: unitsConverterService.convert(vm.value, vm.unit).val(), unit: vm.unit
        });
    }

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = SetUnitValueController;
