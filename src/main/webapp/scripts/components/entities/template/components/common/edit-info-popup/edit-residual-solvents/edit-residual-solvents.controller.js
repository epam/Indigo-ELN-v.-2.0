angular
    .module('indigoeln')
    .controller('EditResidualSolventsController', EditResidualSolventsController);

/* @ngInject */
function EditResidualSolventsController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.solvents = data || {};
        vm.solvents.data = vm.solvents.data || [];
        vm.label = 123;
        vm.save = save;
        vm.cancel = cancel;
        vm.addSolvent = addSolvent;
        vm.remove = remove;
        vm.removeAll = removeAll;
    }

    function addSolvent() {
        vm.solvents.data.push({
            name: '', eq: '', comment: ''
        });
    }

    function remove(solvent) {
        vm.solvents.data = _.without(vm.solvents.data, solvent);
    }

    function removeAll() {
        vm.solvents.data = [];
    }

    function resultToString() {
        var solventStrings = _.map(vm.solvents.data, function(solvent) {
            if (solvent.name && solvent.eq) {
                return solvent.eq + ' mols of ' + solvent.name.name;
            }

            return '';
        });

        return _.compact(solventStrings).join(', ');
    }

    function save() {
        vm.solvents.asString = resultToString();
        $uibModalInstance.close(vm.solvents);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}
