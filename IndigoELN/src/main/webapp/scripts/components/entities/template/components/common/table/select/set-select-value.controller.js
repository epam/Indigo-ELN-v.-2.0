angular
    .module('indigoeln')
    .controller('SetSelectValueController', SetSelectValueController);

/* @ngInject */
function SetSelectValueController(id, name, values, dictionary, $uibModalInstance) {
    var vm = this;

    init();

    function init() {
        vm.id = id;
        vm.name = name;
        vm.values = values;
        vm.dictionary = dictionary;
        vm.wrapper = {
            value: {}
        };

        vm.save = save;
        vm.clear = clear;
    }

    function save() {
        $uibModalInstance.close(vm.wrapper.value);
    }

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }
}
