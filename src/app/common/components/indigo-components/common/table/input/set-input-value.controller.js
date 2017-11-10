SetInputValueController.$inject = ['name', '$uibModalInstance'];

function SetInputValueController(name, $uibModalInstance) {
    var vm = this;

    init();

    function init() {
        vm.name = name;

        vm.save = save;
        vm.clear = clear;
    }

    function save() {
        $uibModalInstance.close(vm.value);
    }

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = SetInputValueController;
