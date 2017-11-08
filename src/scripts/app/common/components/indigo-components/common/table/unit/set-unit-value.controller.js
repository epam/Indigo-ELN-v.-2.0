SetUnitValueController.$inject = ['name', 'unitNames', '$uibModalInstance', 'unitsConverter'];

function SetUnitValueController(name, unitNames, $uibModalInstance, unitsConverter) {
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
}

module.exports = SetUnitValueController;
